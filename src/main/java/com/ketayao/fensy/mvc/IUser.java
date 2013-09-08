/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年7月26日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version          1.0.0
 * Description:		
 *
 * </pre>
 **/
package com.ketayao.fensy.mvc;

/**
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a> Version 1.0.0
 * @since 2013年7月26日 下午4:49:12
 */
public interface IUser {
	/**
	 * 最低权限
	 */
	byte ROLE_GENERAL = 0;
	
	/**
	 * 最高权限
	 */
	byte ROLE_TOP = Byte.MAX_VALUE;

	long getId();

	String getPassword();

	/**  
	 * @return  
	 */
	boolean isBlocked();

	/**  
	 * @return  
	 */
	byte getRole();
}
