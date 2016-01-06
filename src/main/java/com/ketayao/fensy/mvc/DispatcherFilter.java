/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年8月13日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:		
 *
 * </pre>
 **/
package com.ketayao.fensy.mvc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.Constants;
import com.ketayao.fensy.db.DBManager;
import com.ketayao.fensy.exception.ActionException;
import com.ketayao.fensy.exception.NotFoundTemplateException;
import com.ketayao.fensy.handler.ExceptionHandler;
import com.ketayao.fensy.handler.SimpleExceptionHandler;
import com.ketayao.fensy.mvc.interceptor.Interceptor;
import com.ketayao.fensy.mvc.view.View;
import com.ketayao.fensy.mvc.view.ViewMap;
import com.ketayao.fensy.util.ClassUtils;
import com.ketayao.fensy.util.Exceptions;
import com.ketayao.fensy.util.NumberUtils;
import com.ketayao.fensy.util.PropertiesUtils;
import com.ketayao.fensy.util.StringUtils;

/**
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since 2013年8月13日 上午11:48:42
 */
public class DispatcherFilter implements Filter {

    private final static Logger                  log                    = LoggerFactory
        .getLogger(DispatcherFilter.class);

    private ServletContext                       context;

    private ExceptionHandler                     exceptionHandler       = new SimpleExceptionHandler();
    private Map<String, Interceptor>             interceptors           = new LinkedHashMap<String, Interceptor>();

    private final static String                  VIEW_INDEX             = "/"
                                                                          + Constants.ACTION_DEFAULT_METHOD;
    private final static Map<String, PathView>   templates              = new HashMap<String, PathView>();

    private final static HashMap<String, Object> actions                = new HashMap<String, Object>();
    private final static HashMap<String, Method> methods                = new HashMap<String, Method>();

    // 忽略的URI
    private List<String>                         ignoreURIs             = new ArrayList<String>();

    // 忽略的后缀
    private List<String>                         ignoreExts             = new ArrayList<String>();

    // 视图类型
    private List<View>                           viewList               = new ArrayList<View>();

    /**
     * 视图路径=模板根路径+域名模板路径+视图名称
     */
    // 其他子域名模板路径
    private HashMap<String, String>              domainTemplatePathes   = new HashMap<String, String>();

    // 域名
    private String                               rootDomain             = Constants.ACTION_ROOT_DOMAIN;

    private String                               rootDomainTemplatePath = Constants.DEFAULT_DOMAIN_TEMPLATE_PATH;

    // 模板根路径
    private String                               templatePath           = Constants.DEFAULT_TEMPLATE_PATH;

    private String                               defaultTemplatePath;

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        this.context = cfg.getServletContext();

        Map<String, String> config = PropertiesUtils.loadToMap(Constants.FENSY_CONFIG_FILE);

        // 设置上传文件尺寸
        String tmpSzie = config.get(Constants.FENSY_UPLOAD_FILE_MAX_SIZE);
        if (NumberUtils.isNumber(tmpSzie)) {
            WebContext.setMaxSize(NumberUtils.toInt(tmpSzie));
        }

        // 模板存放路径
        String tmp = config.get(Constants.FENSY_TEMPLATE_PATH);
        if (StringUtils.isNotBlank(tmp)) {
            if (tmp.endsWith("/")) {
                tmp = tmp.substring(0, tmp.length() - 1);
            }

            templatePath = tmp;
        }

        // 主域名，必须指定
        tmp = config.get(Constants.FENSY_ROOT_DOMAIN);
        if (StringUtils.isNotBlank(tmp))
            rootDomain = tmp;

        tmp = config.get(Constants.FENSY_ROOT_DOMAIN_TEMPLATE_PATH);
        if (StringUtils.isNotBlank(tmp))
            rootDomainTemplatePath = tmp;

        // 二级域名和对应页面模板路径
        tmp = config.get(Constants.FENSY_OTHER_DOMAIN_AND_TEMPLATE_PATH);
        if (StringUtils.isNotBlank(tmp)) {
            String[] domainAndPath = tmp.split(",");
            for (String dp : domainAndPath) {
                String[] arr = dp.split(":");
                domainTemplatePathes.put(arr[0], templatePath + arr[1]);
            }
        }

        defaultTemplatePath = templatePath + rootDomainTemplatePath;

        // 某些URL前缀不予处理（例如 /img/**）
        String ignores = config.get(Constants.FENSY_IGNORE_URI);
        if (StringUtils.isBlank(ignores)) {
            ignores = Constants.ACTION_IGNORE_URI;
        }
        ignoreURIs.addAll(Arrays.asList(StringUtils.split(ignores, ",")));

        // 某些URL扩展名不予处理（例如 *.jpg）
        ignores = config.get(Constants.FENSY_IGNORE_EXT);
        if (StringUtils.isBlank(ignores)) {
            ignores = Constants.ACTION_IGNORE_EXT;
        }
        for (String ig : StringUtils.split(ignores, ',')) {
            ignoreExts.add('.' + ig.trim());
        }

        // 按顺序创建view
        String views = config.get(Constants.FENSY_VIEW);
        if (StringUtils.isNotBlank(views)) {
            for (String v : StringUtils.split(views, ',')) {
                View view = ViewMap.getView(v.toLowerCase());
                // 从内置的viewMap中查找
                if (view != null) {
                    viewList.add(view);
                    continue;
                }

                try {
                    viewList.add((View) Class.forName(v).newInstance());
                } catch (Exception e) {
                    log.error("视图对象创建出错：" + view, Exceptions.getStackTraceAsString(e));
                }
            }
        }

        // 默认放入jsp视图
        if (viewList.isEmpty()) {
            viewList.add(ViewMap.getView(ViewMap.VIEW_JSP));
        }

        // 初始化exceptionHandler
        String eh = config.get(Constants.FENSY_EXCEPTION_HANDLE);
        if (eh != null) {
            try {
                exceptionHandler = (ExceptionHandler) Class.forName(eh).newInstance();
            } catch (Exception e) {
            }
        }

        // 初始化interceptor
        String tmpInter = config.get(Constants.FENSY_INTERCEPTOR_CONFIG);
        String[] interArr = StringUtils.split(tmpInter, ',');
        for (String in : interArr) {
            try {
                String[] arr = in.split(":");
                Interceptor interceptor = (Interceptor) Class.forName(arr[1]).newInstance();
                interceptors.put(arr[0], interceptor);
            } catch (Exception e) {
                log.error(
                    "HandlerInterceptors initialize error:" + Exceptions.getStackTraceAsString(e),
                    e);
            }
        }

        // 初始化action
        List<String> actionPackages = Arrays
            .asList(StringUtils.split(config.get(Constants.FENSY_ACTION), ','));
        initActions(actionPackages);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String reqURI = request.getRequestURI();

        // 过滤URI前缀
        for (String ignoreURI : ignoreURIs) {
            if (reqURI.startsWith(request.getContextPath() + ignoreURI)) {
                chain.doFilter(request, response);
                return;
            }
        }
        // 过滤URI后缀
        for (String ignoreExt : ignoreExts) {
            if (StringUtils.endsWithIgnoreCase(reqURI, ignoreExt)) {
                chain.doFilter(request, response);
                return;
            }
        }

        WebContext rc = WebContext.begin(this.context, request, response);
        reqURI = rc.getURIAndExcludeContextPath();
        try {
            process(rc, reqURI);
        } catch (Exception e) {
            try {
                handleMethodReturn(rc, exceptionHandler.handle(rc, e));
            } catch (Exception e1) {
                throw new ServletException(e1);
            }
        } finally {
            if (rc != null)
                rc.end();
            DBManager.closeConnection();
        }
    }

    /**
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        for (Object action : actions.values()) {
            try {
                Method dm = action.getClass().getMethod(Constants.ACTION_DESTROY_METHOD);
                dm.invoke(action);
            } catch (NoSuchMethodException e) {
            } catch (Exception e) {
                log.error("Destroy action is error: " + action.getClass().getName(), e);
            }
        }
    }

    /**
     * 逻辑处理
     * 
     * @param rc
     * @param requestURI
     * @throws Exception
     */
    protected void process(WebContext rc, String requestURI) throws Exception {
        // 类似 /action/ admin/home/index
        String selfURI = checkURI(requestURI);
        ActionObject actionObject = findMaxMatchAction(selfURI);

        // 加载Action类
        Object action = actionObject.getAction();
        if (action == null) {
            if (log.isDebugEnabled()) {
                log.debug("requestURI=" + requestURI + "--->not found action");
            }
            rc.notFound();
            return;
        }

        String[] parts = StringUtils.split(selfURI, '/');
        int actionLength = StringUtils.split(actionObject.getName(), '/').length;
        String actionMethodName = Constants.ACTION_DEFAULT_METHOD;
        // 带路径参数
        if (parts.length > actionLength && !NumberUtils.isDigits(parts[actionLength])) {
            actionMethodName = parts[actionLength];
            actionLength++;
        }

        // 查找methodOfAction
        Method methodOfAction = getActionMethod(action, actionMethodName);
        if (methodOfAction == null ||
            // 假如不是post请求，并且还要访问post方法
            (!rc.getRequest().getMethod().equalsIgnoreCase(Constants.REQUEST_POST)
             && !actionMethodName.equalsIgnoreCase(methodOfAction.getName()))) {
            if (log.isDebugEnabled()) {
                log.debug("requestURI=" + requestURI + "--->not found method Of Action");
            }
            rc.notFound();
            return;
        }

        // 路径参数
        List<String> args = new ArrayList<String>();
        for (int i = actionLength; i < parts.length; i++) {
            if (StringUtils.isBlank(parts[i]))
                continue;
            args.add(parts[i]);
        }

        Set<Entry<String, Interceptor>> entries = interceptors.entrySet();
        Exception exception = null;
        try {
            // 运行拦截器的preHandle方法
            for (Entry<String, Interceptor> entry : entries) {
                if (matchPath(entry.getKey(), requestURI)) {
                    boolean result = entry.getValue().preHandle(rc, methodOfAction);
                    if (!result) {
                        return;
                    }
                }
            }

            // 调用Action方法之准备参数，不能重载
            int argLength = methodOfAction.getParameterTypes().length;
            Object returnValue = null;
            switch (argLength) {
                case 0: // login()
                    returnValue = methodOfAction.invoke(action);
                    break;
                case 1:
                    returnValue = methodOfAction.invoke(action, rc);
                    break;
                case 2: // login(rc, String[] extParams) or get(rc, long id);
                    boolean isLong = methodOfAction.getParameterTypes()[1].equals(Long.class)
                                     || methodOfAction.getParameterTypes()[1].equals(long.class);
                    returnValue = methodOfAction.invoke(action, rc,
                        isLong ? NumberUtils.toLong(args.get(0), 1L)
                            : args.toArray(new String[args.size()]));
                    break;
                default:
                    String message = "requestURI=" + requestURI
                                     + "--->not found args matach method Of Action";
                    throw new ActionException(message);
            }

            // 运行拦截器的postHandle方法
            for (Entry<String, Interceptor> entry : entries) {
                if (matchPath(entry.getKey(), requestURI)) {
                    entry.getValue().postHandle(rc, methodOfAction, returnValue);
                }
            }

            handleMethodReturn(rc, returnValue);
            if (log.isDebugEnabled()) {
                log.debug(
                    "requestURI=" + requestURI + ";action=" + action + ";method=" + actionMethodName
                          + ";result=" + (returnValue != null ? returnValue.toString() : "NULL")
                          + ";args=" + args);
            }
        } catch (InvocationTargetException e) {
            exception = new Exception(e.getCause());
            throw exception;
        } catch (Exception e) {
            exception = e;
            throw exception;
        } finally {
            // 倒序运行拦截器的afterCompletion方法
            @SuppressWarnings("unchecked")
            Entry<String, Interceptor>[] entryArr = entries.toArray(new Entry[entries.size()]);
            for (int i = entryArr.length - 1; i >= 0; i--) {
                entryArr[i].getValue().afterCompletion(rc, methodOfAction, exception);
            }
        }
    }

    /**
     * 处理返回值
     * 
     * @param rc
     * @param result
     * @throws Exception
     */
    private void handleMethodReturn(WebContext rc, Object result) throws Exception {
        if (result instanceof String) {
            String returnURI = (String) result;
            if (returnURI.startsWith(Constants.ACTION_REDIRECT)) {
                rc.redirect(StringUtils.substringAfter(returnURI, Constants.ACTION_REDIRECT));
            } else {
                String[] paths = StringUtils.split(returnURI, '/');

                PathView pathView = getTemplate(rc.getRequest(), paths, paths.length);
                if (pathView == null) {
                    throw NotFoundTemplateException.build(returnURI, viewList);
                }

                pathView.getView().render(rc, pathView.getTemplatePath());

                if (log.isInfoEnabled()) {
                    log.info("-->requestURI=" + returnURI + ";pathView=" + pathView);
                }
            }
        }
    }

    /**
     * 默认跳转到首页的匹配模式
     */
    private final static Pattern HOME_PATTERN = Pattern.compile("^/\\d.*");

    /**
     * 检查uri，返回适当的uri
     * 
     * @param uri
     * @return
     */
    private String checkURI(String uri) {
        Matcher matcher = HOME_PATTERN.matcher(uri);
        if (uri.equals("/") || matcher.matches()) {
            uri = "/" + Constants.ACTION_DEFAULT + "/" + Constants.ACTION_DEFAULT_METHOD + uri;
        }

        return uri;
    }

    private PathView getTemplate(HttpServletRequest request, String[] paths, int idx_base) {
        String baseTempalte = getTemplateBase(request);
        StringBuilder template = new StringBuilder(baseTempalte);
        String the_path = null;
        if (idx_base == 0) {// 返回默认页面
            the_path = template.toString() + VIEW_INDEX;
            // } else if (idx_base == 1) { //返回模块默认的页面
            // the_path = template.toString() + "/" + paths[0] + VIEW_INDEX;
        } else {
            for (int i = 0; i < idx_base; i++) {
                template.append('/');
                template.append(paths[i]);
            }
            the_path = template.toString();
        }

        PathView pathView = queryFromCache(the_path);
        if (pathView != null) {
            String params = makeQueryString(paths, idx_base);
            pathView.setTemplatePath(the_path + pathView.getView().getExt() + params);
        }

        if (pathView == null && idx_base > 0) {
            pathView = getTemplate(request, paths, idx_base - 1);
        }

        return pathView;
    }

    /**
     * http://my.ketayao.com/ 组装查询参数
     * 
     * @param paths
     * @param idx_base
     * @return
     */
    private String makeQueryString(String[] paths, int idx_base) {
        StringBuilder params = new StringBuilder();
        int idx = 1;
        for (int i = idx_base; i < paths.length; i++) {
            if (params.length() == 0)
                params.append('?');
            if (i > idx_base)
                params.append('&');
            params.append("p");
            params.append(idx++);
            params.append('=');
            params.append(paths[i]);
        }
        return params.toString();
    }

    /**
     * 得到域名base
     * 
     * @param req
     * @return
     */
    private String getTemplateBase(HttpServletRequest req) {
        String base = null;
        String prefix = req.getServerName().toLowerCase();
        int idx = (rootDomain != null) ? prefix.indexOf(rootDomain) : 0;
        if (idx > 0) {
            prefix = prefix.substring(0, idx - 1);
            base = domainTemplatePathes.get(prefix);
        }
        return (base == null) ? defaultTemplatePath : base;
    }

    /**
     * 查询某个页面是否存在，如果存在则缓存此结果，并返回
     * 
     * @param path
     * @return
     */
    private PathView queryFromCache(String path) {
        PathView pathView = templates.get(path);
        if (pathView == null) {
            for (View view : viewList) {
                String pathAndExt = path + view.getExt();
                File testFile = new File(context.getRealPath(pathAndExt));
                boolean isExists = testFile.exists() && testFile.isFile();
                if (isExists) {
                    pathView = new PathView(path, view);
                    templates.put(path, pathView);
                    break;
                }
            }
        }

        return pathView;
    }

    /**
     * 查找最大路径匹配的对象
     * 
     * @param actName
     * @return
     */
    private ActionObject findMaxMatchAction(String actName) {
        if (actName.endsWith("/")) {
            actName = actName.substring(0, actName.lastIndexOf("/"));
        }
        Object action = actions.get(actName.toLowerCase());
        if (action == null && actName.startsWith("/")) {
            actName = StringUtils.substringBeforeLast(actName, "/");
            actName = (String) findMaxMatchAction(actName).getName();
            action = findMaxMatchAction(actName).getAction();
        }
        return new ActionObject(actName, action);
    }

    /**
     * 初始化所有的Action
     */
    private void initActions(Collection<String> actionPackages) {
        Map<String, String> actionClassNames = new HashMap<String, String>();
        for (String pkg : actionPackages) {
            // 默认扫描子包
            Map<String, String> map = autoScanPackage(pkg, true);
            actionClassNames.putAll(map);
        }

        Set<Entry<String, String>> set = actionClassNames.entrySet();
        for (Entry<String, String> entry : set) {
            Object action = loadActionOfFullName(entry.getValue());
            // synchronized (actions) {
            actions.put(entry.getKey().toLowerCase(), action);// 加入缓存
            // }
        }

        if (log.isDebugEnabled()) {
            log.debug("actions.size=" + actions.size());
        }
    }

    /**
     * 扫描需要初始化的Action类名。
     * 
     * @param packageName
     *            包名
     * @param recursive
     *            是否递归扫描子包
     * @return
     */
    private Map<String, String> autoScanPackage(String packageName, boolean recursive) {
        Collection<String> allClassNames = ClassUtils.getClassNames(packageName, recursive);

        Map<String, String> actionClassNames = new HashMap<String, String>();
        for (String name : allClassNames) {
            if (name.endsWith(Constants.ACTION_EXT)) {
                String path = StringUtils.substringAfter(name, packageName);
                path = path.substring(0, path.length() - Constants.ACTION_EXT.length());
                path = path.replaceAll("\\.", "/");

                // 路径 - 类名
                actionClassNames.put(path, name);
            }
        }
        return actionClassNames;
    }

    /**
     * 创建Action实例
     * 
     * @param cls
     * @return
     */
    private Object loadActionOfFullName(String cls) {
        Object action = null;
        try {
            action = Class.forName(cls).newInstance();
            try {
                Method actionInitMethod = action.getClass().getMethod(Constants.ACTION_INIT_METHOD);// 初始化方法
                actionInitMethod.invoke(action);
            } catch (Exception e) {
            }
        } catch (Exception excp) {
            log.warn("初始化action出错！class=" + cls);
        }

        return action;
    }

    /**
     * 获取名为{methodName}的方法，假如没有找到，则试着查找{"post" + methodName}方法
     * 
     * @param action
     * @param methodName
     * @return
     */
    private Method getActionMethod(Object action, String methodName) {
        // 不能直接调用init和destroy方法
        if (methodName.equals(Constants.ACTION_INIT_METHOD)
            || methodName.equals(Constants.ACTION_INIT_METHOD)) {
            return null;
        }

        String key = action.getClass().getName() + '$' + methodName.toLowerCase();
        Method method = methods.get(key);
        if (method != null) {
            return method;
        }

        for (Method m : action.getClass().getMethods()) {
            if (m.getModifiers() == Modifier.PUBLIC && m.getName().equalsIgnoreCase(methodName)) {
                method = m;
            }
        }

        // 继续查找是否存在post方法。
        methodName = Constants.REQUEST_POST + methodName;
        for (Method m : action.getClass().getMethods()) {
            if (m.getModifiers() == Modifier.PUBLIC && m.getName().equalsIgnoreCase(methodName)) {
                method = m;
            }
        }

        // synchronized (methods) {
        methods.put(key, method);
        // }

        return method;
    }

    /**
     * 匹配路径
     * 
     * @param reg
     * @param uri
     * @return
     */
    private boolean matchPath(String reg, String uri) {
        // /admin/**
        if (reg.contains("/**")) {
            String prefix = StringUtils.substringBefore(reg, "/**");
            // /admin/s/login
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        // /admin/*/login
        if (reg.contains("/*/")) {
            // /admin
            String prefix = StringUtils.substringBefore(reg, "/*/");
            // login
            String suffix = StringUtils.substringAfter(reg, "/*/");
            // /s/login
            String suri = StringUtils.substringAfter(uri, prefix);
            suri = suri.substring(StringUtils.indexOf(suri, '/', 1, suri.length()) + 1);
            if (uri.startsWith(prefix) && suri.equals(suffix)) {
                return true;
            }
        }

        if (uri.equals(reg)) {
            return true;
        }

        return false;
    }
}
