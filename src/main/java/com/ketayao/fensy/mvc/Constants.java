package com.ketayao.fensy.mvc;

public interface Constants {
	/**
	 * action类后缀
	 */
	String ACTION_EXT = "Action";
	/**
	 * 应用默认action前缀
	 */
	String ACTION_DEFAULT = "index";
	/**
	 * action默认执行的方法
	 */
	String ACTION_DEFAULT_METHOD = "index";
	/**
	 * action初始化方法
	 */
	String ACTION_INIT_METHOD = "init";
	/**
	 *  action结束方法
	 */
	String ACTION_DESTROY_METHOD = "destroy";
	/**
	 *	重定向前缀 
	 */
	String ACTION_REDIRECT = "redirect:";
	/**
	 * 默认忽略的后缀
	 */
	String ACTION_IGNORE_EXT = "swf,ico,css,js,jpg,jpeg,gif,png,bmp,doc,xls,pdf,txt,html,htm,zip,rar,xml";
	/**
	 * 默认忽略的URI静态资源地址
	 */
	String ACTION_IGNORE_URI = "/static/";
	/**
	 * 根域名
	 */
	String ACTION_ROOT_DOMAIN = "ketayao.com";

	/**
	 * 配置文件常量
	 */
	String FENSY_CONFIG_FILE = "fensy.properties";
	
	String FENSY_IGNORE_URI = "fensy.ignoreURIs";
	String FENSY_IGNORE_EXT = "fensy.ignoreExts";
	
	String FENSY_ROOT_DOMAIN = "fensy.rootDomain";
	String FENSY_ROOT_DOMAIN_TEMPLATE_PATH = "fensy.rootDomainTemplatePath";
	
	String FENSY_TEMPLATE_PATH = "fensy.templatePath";
	
	String FENSY_OTHER_DOMAIN_AND_TEMPLATE_PATH = "fensy.otherDomainAndTemplatePath";
	
	String FENSY_VIEW = "fensy.views";
	
	/**
	 * action包路径
	 */
	String FENSY_ACTION = "fensy.actions";
	/**
	 * ExceptionHandler
	 */
	String FENSY_EXCEPTION_HANDLE = "fensy.exceptionHandler";
	/**
	 * interceptor包路径
	 */
	String FENSY_INTERCEPTOR_CONFIG = "fensy.interceptorConfig";
	
	String DEFAULT_TEMPLATE_PATH = "/WEB-INF";
	
	String DEFAULT_DOMAIN_TEMPLATE_PATH = "/www";
}
