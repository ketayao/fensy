/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年8月6日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version          1.0.0
 * Description:		
 *
 * </pre>
 **/
package com.ketayao.fensy.test;

import org.junit.Test;

import com.ketayao.fensy.util.CryptUtils;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version  1.0.0
 * @since   2013年8月6日 下午10:24:57 
 */
public class CryptUtilsTest {
	@Test
	public void test() throws Exception {
		String result = CryptUtils.decrypt("4D35306316BADE9A", "T7r7S2oHiPefD2PC");
		System.out.println(result);
	}
}
