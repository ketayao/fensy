/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年8月20日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:		
 *
 * </pre>
 **/
 
package com.ketayao.fensy.mvc.interceptor;

import com.ketayao.fensy.mvc.RequestContext;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年8月20日 上午10:08:56 
 */

public interface Interceptor {

	public boolean preHandle(RequestContext rc, Object handler)
		throws Exception;

	public void postHandle(RequestContext rc, Object handler, Object result)
			throws Exception;

	public void afterCompletion(RequestContext rc, Object handler, Exception ex)
			throws Exception;
}
