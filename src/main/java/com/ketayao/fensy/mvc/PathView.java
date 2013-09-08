/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年8月2日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:		
 *
 * </pre>
 **/
package com.ketayao.fensy.mvc;

import com.ketayao.fensy.mvc.view.View;


/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年8月2日 上午8:49:58 
 */
public class PathView {
	private String templatePath;
	private View view;

	public PathView() {
	}

	public PathView(String templatePath, View view) {
		this.templatePath = templatePath;
		this.view = view;
	}

	/**
	 * 返回 templatePath 的值
	 * 
	 * @return templatePath
	 */
	public String getTemplatePath() {
		return templatePath;
	}

	/**
	 * 设置 templatePath 的值
	 * 
	 * @param templatePath
	 */
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	/**
	 * 返回 view 的值
	 * 
	 * @return view
	 */
	public View getView() {
		return view;
	}

	/**
	 * 设置 view 的值
	 * 
	 * @param view
	 */
	public void setView(View view) {
		this.view = view;
	}

	/**   
	 * @return  
	 * @see java.lang.Object#toString()  
	 */
	@Override
	public String toString() {
		return "PathView [templatePath=" + templatePath + ", viewExt=" + view.getExt()
				+ "]";
	}
}
