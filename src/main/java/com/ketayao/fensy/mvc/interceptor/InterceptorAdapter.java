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
 * @since   2013年8月20日 上午9:48:18 
 */

public abstract class InterceptorAdapter implements Interceptor {

	/**
	 * This implementation always returns {@code true}.
	 */
	public boolean preHandle(RequestContext rc, Object handler)
		throws Exception {
		return true;
	}

	/**
	 * This implementation is empty.
	 */
	public void postHandle(RequestContext rc, Object handler, Object result)
			throws Exception {
	}

	/**
	 * This implementation is empty.
	 */
	public void afterCompletion(RequestContext rc, Object handler, Exception ex)
			throws Exception {
	}
	
}
