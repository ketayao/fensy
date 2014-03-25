package com.ketayao.fensy.test.db;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年3月21日 下午2:32:32 
 */
public class PrimitiveTest {
	// 定义基础类
	private final static List<Class<?>> PRIMITIVE_CLASSES = new ArrayList<Class<?>>(){
		private static final long serialVersionUID = 218015821661758840L;

	{
		add(Boolean.class);
		add(Byte.class);
		add(Character.class);
		add(Short.class);
		add(Integer.class);
		add(Long.class);
		add(Float.class);
		add(Double.class);
	
		add(String.class);
		add(java.util.Date.class);
		add(java.sql.Date.class);
		add(java.sql.Timestamp.class);
	}};
	
	protected final static boolean isPrimitive(Class<?> cls) {
		return cls.isPrimitive() || PRIMITIVE_CLASSES.contains(cls) ;
	}
	
	@Test
	public void test() {
		Assert.assertTrue(long.class.isPrimitive());
		Assert.assertTrue(isPrimitive(Long.class));
	}
	
	@Test
	public void test2() {
		System.out.println(POJOTest.class.getName());
	}

}
