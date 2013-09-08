/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年7月27日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:		
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
 * @since   2013年7月27日 下午4:31:05 
 */
public class JSPView implements View {
	
	/**   
	 * @param rc
	 * @param templatePath
	 * @throws IOException
	 * @throws ServletException  
	 * @see com.ketayao.fensy.mvc.view.View#render(com.ketayao.fensy.mvc.RequestContext, java.lang.String)  
	 */
	@Override
	public void render(RequestContext rc, String templatePath)
			throws IOException, ServletException {
		rc.forward(templatePath);
	}

	/**   
	 * @return  
	 * @see com.ketayao.fensy.mvc.view.View#getExt()  
	 */
	@Override
	public String getExt() {
		return ".jsp";
	};
}
