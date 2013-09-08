/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Filename:		com.ketayao.fensy.test.PatternTest.java
 * Class:			PatternTest
 * Date:			2013年8月26日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version          3.1.0
 * Description:		
 *
 * </pre>
 **/
 
package com.ketayao.fensy.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version  3.1.0
 * @since   2013年8月26日 下午3:02:21 
 */

public class PatternTest {
	private final static String ACTION_DEFAULT_METHOD = "index";
	private final static Pattern SHORT_PATTERN = Pattern.compile("/[a-z]\\w*");
	
	@Test
	public void testActionURI() {
		//String uri = "/fensy/admin/user/login/12312/df12";
		//String uri = "/v/121";
		String uri = "/";
		
		
		Matcher matcher = SHORT_PATTERN.matcher(uri);
		
		int count = 0;
		while (matcher.find()) {
			count++;
			if (count > 0) {
				break;
			}
		}
		
		switch (count) {
		case 0:
			uri = "/" + ACTION_DEFAULT_METHOD + "/" + ACTION_DEFAULT_METHOD + uri;
			break;
		case 1:
			uri = "/" + ACTION_DEFAULT_METHOD + uri;
			break;			
		default:
			break;
		}
		
		System.out.println(uri);
	}
}
