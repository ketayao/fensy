/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年9月6日
 * Description:		
 *
 * </pre>
 **/
 
package com.ketayao.fensy.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.mvc.WebContext;
import com.ketayao.fensy.util.Exceptions;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年9月6日 下午3:51:17 
 */

public class SimpleExceptionHandler implements ExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(SimpleExceptionHandler.class);
	
	private static final String ERROR_PAGE = "500";
	
	/**
	 * 
	 * @param rc
	 * @param exception
	 * @return  
	 * @see com.ketayao.fensy.handler.ExceptionHandler#handle(com.ketayao.fensy.mvc.WebContext, java.lang.Exception)
	 */
	@Override
	public String handle(WebContext rc, Exception exception) {
		log.error(Exceptions.getStackTraceAsString(exception));
		
		rc.setRequestAttr("exception", exception);
		rc.setRequestAttr("errorMsg", exception.getCause().getMessage());
		return ERROR_PAGE;
	}
	
}
