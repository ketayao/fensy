/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年7月26日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:		
 *
 * </pre>
 **/
package com.ketayao.fensy.exception;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年7月26日 下午4:06:29 
 */
public class ActionException extends FensyException {

	/** 描述  */
	private static final long serialVersionUID = -2340732188202382451L;

	/**  
	 *   
	 */ 
	public ActionException() {
		super();
	}

	/**  
	 * @param message
	 * @param cause  
	 */ 
	public ActionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**  
	 * @param message  
	 */ 
	public ActionException(String message) {
		super(message);
	}

	/**  
	 * @param cause  
	 */ 
	public ActionException(Throwable cause) {
		super(cause);
	}
}
