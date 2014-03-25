package com.ketayao.fensy.test.db;

import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Test;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年3月20日 下午4:54:31 
 */
public class POJOTest {
	@Test
	public void testCRUD() {
		Role role = new Role();
		role.setName("test1");
		role.setDescription("test1");
		role.setCreateTime(new Timestamp(System.currentTimeMillis()));
	
		Assert.assertEquals(true, role.save() > 0);
		
		// 默认从缓存中获取
		Role role2 = role.get(role.getId());
		Assert.assertEquals(role2, role);
		
		Assert.assertEquals("test2", role.getName());
		Assert.assertEquals(true, role.delete());
	}
	
	@Test
	public void testQuery() {
		Role role = new Role();
		role.list(1, 10, "id DESC");
	
		role.filter(1, 10, "id=1", "name DESC");
	}
	
	@Test
	public void testTotalCount() {
		Role role = new Role();
		role.totalCount();
	
		role.totalCount("id=1");
	}
}
