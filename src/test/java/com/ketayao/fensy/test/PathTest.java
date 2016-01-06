package com.ketayao.fensy.test;

import org.junit.Test;

import com.ketayao.fensy.util.RandomStringUtils;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年3月21日 下午4:46:19 
 */
public class PathTest {
    @Test
    public void test() {
        String path = System.getProperty("java.io.tmpdir")
                      + RandomStringUtils.randomAlphanumeric(10);
        System.out.println(path);
    }
}
