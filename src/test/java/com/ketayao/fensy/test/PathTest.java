package com.ketayao.fensy.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年3月21日 下午4:46:19 
 */
public class PathTest {
	@Test
	public void test() {
		String path = System.getProperty("java.io.tmpdir") + RandomStringUtils.randomAlphanumeric(10);
		System.out.println(path);
	}
}
