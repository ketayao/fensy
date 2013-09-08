/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年8月13日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version          1.0.0
 * Description:		
 *
 * </pre>
 **/
package com.ketayao.fensy.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version  1.0.0
 * @since   2013年8月13日 下午2:35:47 
 */
public class UrlTest {
	@Test
	public void test() {
		String url = "/fensy/admin/user/login/12312/df12";
		//Pattern pattern = Pattern.compile("^[/a-z][/_a-zA-Z0-9]*/[^a-z]$");
		Pattern pattern = Pattern.compile("/[a-z]\\w*");
		Matcher matcher = pattern.matcher(url);
		
//		matcher.find();
//		System.out.println(url.substring(matcher.start(), matcher.end()) + ":" + matcher.start() + "-" + matcher.end());
//		
//		matcher.find();
//		System.out.println(url.substring(matcher.start(), matcher.end()) + ":" + matcher.start() + "-" + matcher.end());
////		matcher.find();
////		System.out.println(url.substring(matcher.start(), matcher.end()) + ":" + matcher.start() + "-" + matcher.end());
//		
		int start = 0;
		int end = 0;
		while (matcher.find(start)) {
			System.out.println(url.substring(matcher.start(), matcher.end()) + ":" + matcher.start() + "-" + matcher.end());
			//start = matcher.end();
			if (start != end) {
				end = matcher.end();
				break;
			}
			start = matcher.end();
		}
		
		System.out.println(start + "-" + url.substring(0, 0) + "mm");
		System.out.println(matcher.find());
		System.out.println(matcher.matches());
	}
	
	private final static Pattern METHOD_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*");
	private String urlPatternPrefix;
	
	private String _parse(String url) {
		String temp = url;
		if (StringUtils.isNotBlank(urlPatternPrefix)) {
			temp = StringUtils.substringAfter(url, urlPatternPrefix);
		}
		
		if (!temp.startsWith("/")) {
			temp = "/" + temp;
		}
		
		int end = 0;
		Matcher matcher = METHOD_PATTERN.matcher(temp);
		while (matcher.find()) {
			end = matcher.end();
		}
		
		return null;
	}
}