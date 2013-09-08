/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年7月26日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 *
 * </pre>
 **/
package com.ketayao.fensy.exception;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年7月26日 下午4:04:31 
 */
public class FensyException extends RuntimeException {

	/** 描述  */
	private static final long serialVersionUID = 834530983809794991L;

	public FensyException() {
		super();
	}

	/**  
	 * @param message
	 * @param cause  
	 */ 
	public FensyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**  
	 * @param message  
	 */ 
	public FensyException(String message) {
		super(message);
	}

	/**  
	 * @param cause  
	 */ 
	public FensyException(Throwable cause) {
		super(cause);
	}
}
