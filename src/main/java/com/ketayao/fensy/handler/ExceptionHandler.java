/**
 * <pre>
 * Date:			2013年9月6日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:
 * </pre>
 **/
 
package com.ketayao.fensy.handler;

import com.ketayao.fensy.mvc.WebContext;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年9月6日 下午3:54:58 
 */

public interface ExceptionHandler {
	String handle(WebContext rc, Exception exception);
}
