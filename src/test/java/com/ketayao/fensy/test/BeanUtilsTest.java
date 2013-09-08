/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Filename:		com.ketayao.fensy.test.BeanUtilsTest.java
 * Class:			BeanUtilsTest
 * Date:			2013年8月19日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version          3.1.0
 * Description:		
 *
 * </pre>
 **/
 
package com.ketayao.fensy.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import com.ketayao.fensy.bean.UserBean;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version  3.1.0
 * @since   2013年8月19日 上午10:46:11 
 */

public class BeanUtilsTest {
	@Test
	public void testClass() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		UserBean obj = new UserBean();
		
		Map<String, Object> props = BeanUtils.describe(obj);
		if (obj.getId() <= 0)
			props.remove("id");
		props.remove("class");

		Map<String, Object> priMap = new HashMap<String, Object>();
		for (Entry<String, Object> entry : props.entrySet()) {
			Field field = obj.getClass().getDeclaredField(entry.getKey());
			
			if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
				//props.remove(key);
				priMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		
        //Class<?> clazz = UserBean.class;
        
//		FieldUtils.getDeclaredField(obj.getClass(), "")
//		
//        Field[] fields = obj.getClass().getDeclaredFields();
//        for (Field field : fields) {
//        	System.out.println("field.getDeclaringClass()=" + field.getType());
//        	if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
//        		System.out.println("-----------field.getDeclaringClass()=" + field.getType());
//        	}
//        	
//		}
        //clazz.getFields();
	}
	
	
	@Test
	public void testDescribe() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		UserBean obj = new UserBean();
		Map<String, Object> pojo_bean = BeanUtils.describe(obj);
		pojo_bean.remove("id");
		pojo_bean.remove("class");
		
		for (Entry<String, Object> entry : pojo_bean.entrySet()) {
			String key = entry.getKey();
		
			Field field = FieldUtils.getDeclaredField(obj.getClass(), entry.getKey());
			if (field != null && field.getDeclaringClass().isPrimitive()) {
				pojo_bean.remove(entry.getKey());
			}
		}
		
		String[] fields = pojo_bean.keySet().toArray(
				new String[pojo_bean.size()]);
		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append("userBean");
		sql.append('(');
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				sql.append(',');
			sql.append(fields[i]);
		}
		sql.append(") VALUES(");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				sql.append(',');
			sql.append('?');
		}
		sql.append(')');
		
		System.out.println(sql.toString());
		for (Entry<String, Object> entry : pojo_bean.entrySet()) {
			System.out.println(entry.getKey() + "--" + entry.getValue());
		}
	}
	
}
