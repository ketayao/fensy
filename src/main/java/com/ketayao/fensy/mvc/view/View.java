/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年7月28日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 *
 * </pre>
 **/
package com.ketayao.fensy.mvc.view;

import java.io.IOException;

import javax.servlet.ServletException;

import com.ketayao.fensy.mvc.RequestContext;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年7月28日 下午7:48:32 
 */
public interface View {
	void render(RequestContext rc, String templatePath) throws IOException, ServletException;
	
	String getExt();
}
