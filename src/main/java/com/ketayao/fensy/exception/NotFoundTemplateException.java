/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年7月29日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 *
 * </pre>
 **/
package com.ketayao.fensy.exception;

import java.util.List;

import com.ketayao.fensy.mvc.view.View;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年7月29日 下午3:18:59 
 */
public class NotFoundTemplateException extends FensyException {

	/** 描述  */
	private static final long serialVersionUID = 4297542590778361854L;

	/**  
	 *   
	 */ 
	public NotFoundTemplateException() {
		super();
	}

	/**  
	 * @param message
	 * @param cause  
	 */ 
	public NotFoundTemplateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**  
	 * @param message  
	 */ 
	public NotFoundTemplateException(String message) {
		super(message);
	}

	/**  
	 * @param cause  
	 */ 
	public NotFoundTemplateException(Throwable cause) {
		super(cause);
	}
	
	/**  
	 * @param cause  
	 */ 
	public static NotFoundTemplateException build(String path, List<View> viewList) {
		StringBuilder msg = new StringBuilder();
		msg.append("Not found template:" + path + ", ext is");
		for (View fensyView : viewList) {
			msg.append(" " + fensyView.getExt());
		}
		msg.append(".");
		
		return new NotFoundTemplateException(msg.toString());
	}
	
}
