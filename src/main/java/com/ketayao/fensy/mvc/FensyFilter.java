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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.db.DBManager;
import com.ketayao.fensy.exception.FensyException;
import com.ketayao.fensy.exception.NotFoundTemplateException;
import com.ketayao.fensy.handler.ExceptionHandler;
import com.ketayao.fensy.handler.Handler;
import com.ketayao.fensy.mvc.interceptor.Interceptor;
import com.ketayao.fensy.mvc.view.JSPView;
import com.ketayao.fensy.mvc.view.View;
import com.ketayao.fensy.util.Exceptions;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年8月13日 上午11:48:42 
 */
public class FensyFilter implements Filter {

	private final static Logger log = LoggerFactory.getLogger(FensyFilter.class);
	
	private FensyAction fensyAction;
	
	private ServletContext context;
	
	private Handler exceptionHandler;
	
	private final static String VIEW_INDEX = "/index";
	private final static Map<String, PathView> template_cache = new HashMap<String, PathView>();
	
	private String rootDomain = "ketayao.com";
	private String default_base;
	
	// 初始化Filter参数。
	private HashMap<String, String> other_base = new HashMap<String, String>();
	private List<String> ignoreURIs = new ArrayList<String>();
	private List<String> ignoreExts = new ArrayList<String>();
	private List<View> viewList = new ArrayList<View>();
	private String templatePathPrefix;
	
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		this.context = cfg.getServletContext();
		
		//模板存放路径
		this.templatePathPrefix = cfg.getInitParameter("templatePathPrefix");
		if (this.templatePathPrefix == null) {
			this.templatePathPrefix = "/WEB-INF/www";
		} else if (this.templatePathPrefix.endsWith("/")) {
			this.templatePathPrefix = this.templatePathPrefix.substring(0,
					this.templatePathPrefix.length() - 1);
		}

		//某些URL前缀不予处理（例如 /img/***）
		String ignores = cfg.getInitParameter("ignore");
		if(ignores != null) {
			for(String ig : StringUtils.split(ignores, ',')) {
				ignoreURIs.add(ig.trim());
			}
		}
		
		//某些URL扩展名不予处理（例如 *.jpg）
		ignores = cfg.getInitParameter("ignoreExts");
		if(ignores != null) {
			for(String ig : StringUtils.split(ignores, ',')) {
				ignoreExts.add('.'+ig.trim());
			}
		}
		
		//创建view，按顺序
		String views = cfg.getInitParameter("viewList");
		if(views != null) {
			for(String view : StringUtils.split(views, ',')) {
				try {
					viewList.add((View)Class.forName(view).newInstance());
				} catch (Exception e) {
					throw new FensyException(e);
				} 
			}
		} else {
			viewList.add(new JSPView());
		}
		
		//主域名，必须指定
		String tmp = cfg.getInitParameter("domain");
		if(StringUtils.isNotBlank(tmp))
			rootDomain = tmp;
		
		//二级域名和对应页面模板路径
		@SuppressWarnings("unchecked")
		Enumeration<String> names = cfg.getInitParameterNames();
		while(names.hasMoreElements()){
			String name = names.nextElement();
			String v = cfg.getInitParameter(name);
			if(v.endsWith("/"))
				v = v.substring(0, v.length()-1);
			if("ignore".equalsIgnoreCase(name) || "ignoreExts".equalsIgnoreCase(name))
				continue;
			if("default".equalsIgnoreCase(name))
				default_base = templatePathPrefix + v;
			else
				other_base.put(name, templatePathPrefix + v);
		}
		
		//exceptionHandler
		String eh = cfg.getInitParameter("exceptionHandler");
		if (eh != null) {
			try {
				exceptionHandler = (Handler)Class.forName(eh).newInstance();
			} catch (Exception e) {
				exceptionHandler = new ExceptionHandler();
			} 
		} else {
			exceptionHandler = new ExceptionHandler();
		}
		
		// init fensyAction
		String tmp2 = cfg.getInitParameter("packages");
		List<String> actionPackages = Arrays.asList(StringUtils.split(tmp2,','));
		
		String interceptorsString = cfg.getInitParameter("interceptors");
		List<String> inter = Arrays.asList(StringUtils.split(interceptorsString,','));
		List<Interceptor> interceptors = new ArrayList<Interceptor>(inter.size()); 
		for (String in : inter) {
			try {
				interceptors.add((Interceptor)Class.forName(in).newInstance());
			} catch (Exception e) {
				log.error("HandlerInterceptors initialize error:" + Exceptions.getStackTraceAsString(e), e);
			} 
		}
		
		fensyAction = new FensyAction(this, actionPackages, interceptors, exceptionHandler);
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain)
			throws IOException, ServletException {
		//自动编码处理	
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		RequestContext rc = RequestContext.begin(this.context, request, response);
		
		String req_uri = rc.getRequestURIAndExcludeContextPath();

		try{
			//过滤URL前缀
			for(String ignoreURI : ignoreURIs){
				if(req_uri.startsWith(ignoreURI)){
					chain.doFilter(rc.request(), rc.response());
					return ;
				}
			}
			//过滤URL后缀
			for(String ignoreExt : ignoreExts){
				if(req_uri.endsWith(ignoreExt)){
					chain.doFilter(rc.request(), rc.response());
					return ;
				}
			}
			
			fensyAction.process(rc);
			//boolean handled = fensyAction.process(rc);
			//if (handled) {
			//	return ;
			//}

			//process(rc, req_uri);
		} catch (Exception e) {
			//rc.error(500);
			exceptionHandler.handle(rc, e);
		} finally {
			if(rc!=null) rc.end();
			DBManager.closeConnection();
		}
	}
	
	public boolean process(RequestContext rc, String req_uri) throws IOException, ServletException {
		//rc.request().setAttribute(REQUEST_URI, req_uri);
		String[] paths = StringUtils.split(req_uri, '/');
		
		PathView pathView = _getTemplate(rc.request(), paths, paths.length);
		if (pathView == null) {
			throw NotFoundTemplateException.build(req_uri, viewList);
		}

		pathView.getView().render(rc, pathView.getTemplatePath());
		
		if (log.isInfoEnabled()) {
			log.info("-->requestURI=" + req_uri + ";pathView=" + pathView);
		}
		
		return true;
	}
	
	private PathView _getTemplate(HttpServletRequest request, String[] paths, int idx_base) {
		String baseTempalte = _getTemplateBase(request);
		StringBuilder template = new StringBuilder(baseTempalte);
		String the_path = null;
		if (idx_base == 0) {//返回默认页面
			the_path = template.toString() + VIEW_INDEX;
//		} else if (idx_base == 1) { //返回模块默认的页面
//			the_path = template.toString() + "/" + paths[0] + VIEW_INDEX;
		} else {
			for (int i = 0; i < idx_base; i++) {
				template.append('/');
				template.append(paths[i]);
			}
			the_path = template.toString();
		}

		PathView pathView = _queryFromCache(the_path);
		if (pathView != null) {
			String params = _makeQueryString(paths, idx_base);
			pathView.setTemplatePath(the_path + pathView.getView().getExt() + params);
		}
		
		if (pathView == null && idx_base > 0) {
			pathView = _getTemplate(request, paths, idx_base - 1);
		} 
		
		return pathView;
	}
	
	/**
	 * http://my.ketayao.com/
	 * 组装查询参数
	 * @param paths
	 * @param idx_base
	 * @return
	 */
	private String _makeQueryString(String[] paths, int idx_base) {
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
	 * @param req
	 * @return
	 */
	private String _getTemplateBase(HttpServletRequest req) {
		String base = null;
		String prefix = req.getServerName().toLowerCase();
		int idx = (rootDomain != null) ? prefix.indexOf(rootDomain) : 0;
		if (idx > 0) {
			prefix = prefix.substring(0, idx - 1);
			base = other_base.get(prefix);
		}
		return (base == null) ? default_base : base;
	}
	
	
	/**
	 * 查询某个页面是否存在，如果存在则缓存此结果，并返回
	 * @param path
	 * @return
	 */
	private PathView _queryFromCache(String path) {
		PathView pathView = template_cache.get(path); 
		if (pathView == null) {
			for (View view : viewList) {
				String pathAndExt = path + view.getExt();
				File testFile = new File(context.getRealPath(pathAndExt));
				boolean isExists = testFile.exists() && testFile.isFile();
				if (isExists) {
					pathView = new PathView(path, view);
					template_cache.put(path, pathView);
					break;
				}
			}
		}
		
		return pathView;
	}
	
	/**   
	 *   
	 * @see javax.servlet.Filter#destroy()  
	 */
	@Override
	public void destroy() {
		if (fensyAction != null) {
			fensyAction.destroy();
		}
	}
}
