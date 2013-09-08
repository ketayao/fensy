/**
 * <pre>
 * Date:			2013年9月6日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:
 * </pre>
 **/
 
package com.ketayao.fensy.handler;

import com.ketayao.fensy.mvc.RequestContext;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年9月6日 下午3:54:58 
 */

public interface Handler {
	String handle(RequestContext rc, Exception exception);
}
