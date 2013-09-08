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
 * @since   2013年7月26日 下午3:09:30 
 */

public class DBException extends FensyException {
	/** 描述  */
	private static final long serialVersionUID = -6119350953024979597L;

	/**  
	 *   
	 */ 
	public DBException() {
		super();
	}

	/**  
	 * @param message
	 * @param cause  
	 */ 
	public DBException(String message, Throwable cause) {
		super(message, cause);
	}

	/**  
	 * @param message  
	 */ 
	public DBException(String message) {
		super(message);
	}

	/**  
	 * @param cause  
	 */ 
	public DBException(Throwable cause) {
		super(cause);
	}
	
}
