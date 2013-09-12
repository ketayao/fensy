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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.handler.Handler;
import com.ketayao.fensy.mvc.interceptor.Interceptor;
import com.ketayao.fensy.util.Exceptions;
import com.ketayao.fensy.util.PackageUtils;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年8月13日 上午11:50:23 
 */
public class FensyAction {

	private final static Logger log = LoggerFactory.getLogger(FensyAction.class);
	
	private List<Interceptor> interceptors;
	
	private Handler exceptionHandler;
	
	//ActionServlet参数
	private List<String> actionPackages;
	
	private FensyFilter fensyFilter;
	
	public FensyAction(FensyFilter fensyFilter, List<String> actionPackages, 
			List<Interceptor> interceptors, Handler exceptionHandler) {
		this.fensyFilter = fensyFilter;
		this.actionPackages = actionPackages;
		this.interceptors = interceptors;
		this.exceptionHandler = exceptionHandler;
		_initActions();
	}
	
	private final static String ACTION_DEFAULT_METHOD = "index";
	private final static String ACTION_EXT = "Action";
	
	private final static HashMap<String, Object> actions = new HashMap<String, Object>();
	private final static HashMap<String, Method> methods = new HashMap<String, Method>();
	
	public void destroy() {
		for(Object action : actions.values()){
			try{
				Method dm = action.getClass().getMethod("destroy");
				if(dm != null){
					dm.invoke(action);
					log.error("!!!!!!!!! " + action.getClass().getName() + " destroy !!!!!!!!!");
				}
			}catch(NoSuchMethodException e){
			}catch(Exception e){
				log.error("Unabled to destroy action: " + action.getClass().getName(), e);
			}
		}
	}
	
	/**
	 * 业务逻辑处理
	 * @param req
	 * @param resp
	 * @param is_post_method
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException
	 * @throws Exception 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public boolean process(RequestContext rc) throws Exception{
		// 处理locale
		rc.saveLocale();
		
		String requestURI = rc.getRequestURIAndExcludeContextPath();
		// 类似 /action/ admin/home/index
		
		String temp = requestURI;
		
		temp = _actionURI(temp);
		
		Exception exception = null;
		Object action = null;
		Method methodOfAction = null;
		try {
			Object[] objects = _findMaxMatchAction(temp);
			
			// 加载Action类
			action = objects[1];
			if (action == null) {
				if (log.isDebugEnabled()) {
					log.debug("requestURI=" + requestURI + "--->not found action");
				}
				rc.not_found();
				return false;
			}
		
			String[] parts = StringUtils.split(temp, "/");
			int actionLength = StringUtils.split((String)objects[0], '/').length;
			String actionMethod = null;
			if (parts.length > actionLength) {
				if (NumberUtils.isDigits(parts[actionLength])) {
					actionMethod = ACTION_DEFAULT_METHOD;
					actionLength--;
				} else {
					actionMethod = parts[actionLength];
				}
			} else {
				actionMethod = ACTION_DEFAULT_METHOD;
			}
			
			methodOfAction = _getActionMethod(action, actionMethod);
			if (methodOfAction == null) {
				if (log.isDebugEnabled()) {
					log.debug("requestURI=" + requestURI + "--->not found method Of Action");
				}
				rc.not_found();
				return false;
			}

			for (Interceptor interceptor : interceptors) {
				boolean result = interceptor.preHandle(rc, new Object[]{action, methodOfAction});
				if (!result) {
					return false;
				}
			}
			
			// 路径参数
			List<String> args = new ArrayList<String>();
			for (int i = actionLength + 1; i < parts.length; i++) {
				if (StringUtils.isBlank(parts[i]))
					continue;
				args.add(parts[i]);
			}
			
			// 调用Action方法之准备参数，不能重载
			int arg_c = methodOfAction.getParameterTypes().length;
			Object result = null;
			switch (arg_c) {
			case 0: // login()
				result = methodOfAction.invoke(action);
				break;
			case 1:
				result = methodOfAction.invoke(action, rc);
				break;
			case 2: // login(rc, String[] extParams)
				boolean isLong = methodOfAction.getParameterTypes()[1].equals(long.class);
				result = methodOfAction.invoke(
						action,
						rc,
						isLong ? NumberUtils.toLong(args.get(0), 1L) : args.toArray(new String[args.size()]));
				break;
			default:
				if (log.isDebugEnabled()) {
					log.debug("requestURI=" + requestURI + "--->not found args matach method Of Action");
				}
				rc.not_found();
				return true;
			}
			
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				interceptors.get(i).postHandle(rc, new Object[]{action, methodOfAction}, result);
			}
			
			handleReturns(rc, result);
			if (log.isDebugEnabled()) {
				log.debug("-->requestURI=" + requestURI + ";action=" + action + ";method=" + actionMethod + ";result=" +
						(result != null ? result.toString():"NULL") + ";args=" + args);
			}
			return true;
		} catch (Exception e) {
			String view = exceptionHandler.handle(rc, e);
			handleReturns(rc, (Object)view);
			throw e;			
		} finally {
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				interceptors.get(i).afterCompletion(rc, new Object[]{action, methodOfAction}, exception);
			}
		}
	}
	
	private void handleReturns(RequestContext rc, Object result) throws Exception {
		if (result instanceof String) {
			if (((String) result).startsWith("redirect:")) {
				rc.redirect(StringUtils.substringAfter(((String) result), "redirect:"));
			} else {
				fensyFilter.process(rc, (String)result);
			}
		} 
	}
	
	private final static Pattern HOME_PATTERN = Pattern.compile("^/\\d.*");
	
	private String _actionURI(String uri) {
		Matcher matcher = HOME_PATTERN.matcher(uri);
		if (uri.equals("/") || matcher.matches()) {
			uri = "/" + ACTION_DEFAULT_METHOD + "/" + ACTION_DEFAULT_METHOD + uri;
		}
		
		return uri;
	}
	
	/**
	 * 扫描需要初始化的Action类名。
	 * @param packageName
	 * @return
	 */
	private Map<String, String> _autoScanPackage(String packageName) {
		String[] allClassNames = PackageUtils.getClassNames(packageName);
		Map<String, String> actionClassNames = new HashMap<String, String>(); 
		for (String name : allClassNames) {
			if (name.endsWith(ACTION_EXT)) {
				String path = StringUtils.substringAfter(name, packageName);
				path = path.substring(0, path.length() - ACTION_EXT.length());
				path = path.replaceAll("\\.", "/");
				
				actionClassNames.put(path, name);
			}
		}
		return actionClassNames;
	}
	
	/**
	 * 初始化所有的Action
	 */
	private void _initActions() {
		Map<String, String> actionClassNames = new HashMap<String, String>(); 
		for (String pkg : actionPackages) {
			Map<String, String> map = _autoScanPackage(pkg);
			actionClassNames.putAll(map);
		}
		
		Set<Entry<String, String>> set =  actionClassNames.entrySet();
		for (Entry<String, String> entry : set) {
			try {
				_loadActionOfFullname(entry.getKey().toLowerCase(), entry.getValue());
			} catch (IllegalAccessException e) {
			} catch (InstantiationException e) {
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("actions.size=" + actions.size());
		}
	}
	
	/**
	 * 查找最大路径匹配的对象return new Object[]{act_name, action}
	 * 描述
	 * @param act_name
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected Object[] _findMaxMatchAction(String act_name)
			throws InstantiationException, IllegalAccessException {
		if (act_name.endsWith("/")) {
			act_name = act_name.substring(0, act_name.lastIndexOf("/"));
		}
		Object action = actions.get(act_name.toLowerCase());
		if (action == null && act_name.startsWith("/")) {
			act_name = StringUtils.substringBeforeLast(act_name, "/");
			act_name = (String)_findMaxMatchAction(act_name)[0];
			action = _findMaxMatchAction(act_name)[1];
		}
		return new Object[]{act_name, action};
	}
	
	/**
	 * 加载Action类
	 * @param act_name
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	protected Object _loadAction(String act_name)
			throws InstantiationException, IllegalAccessException {
		return _findMaxMatchAction(act_name)[1];
	}
	
	/**
	 * 创建Action实例
	 * @param act_name
	 * @param cls
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private Object _loadActionOfFullname(String act_name, String cls)
			throws IllegalAccessException, InstantiationException {
		Object action = null;
		try {
			action = Class.forName(cls).newInstance(); 
			try {
				Method action_init_method = action.getClass().getMethod("init");// 初始化方法
				action_init_method.invoke(action);
			} catch (NoSuchMethodException e) {
			} catch (InvocationTargetException excp) {
			}
			if (!actions.containsKey(act_name)) {
				synchronized (actions) {
					actions.put(act_name, action);// 加入缓存
				}
			}
		} catch (ClassNotFoundException excp) {
			if (log.isWarnEnabled()) {
				log.warn(act_name + ":" + Exceptions.getStackTraceAsString(excp));
			}
		}
		return action;
	}
	
	/**
	 * 获取名为{method}的方法
	 * @param action
	 * @param method
	 * @return
	 */
	private Method _getActionMethod(Object action, String method) {
		String key = action.getClass().getName() + '.' + method;
		Method m = methods.get(key);
		if (m != null) {
			return m;
		}
		for (Method m1 : action.getClass().getMethods()) {
			if (m1.getModifiers() == Modifier.PUBLIC && m1.getName().equals(method)) {
				synchronized (methods) {
					methods.put(key, m1);
				}
				return m1;
			}
		}
		return null;
	}
}
