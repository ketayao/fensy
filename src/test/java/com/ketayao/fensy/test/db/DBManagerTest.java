package com.ketayao.fensy.test.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import com.ketayao.fensy.db.DBManager;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年3月20日 下午4:44:45 
 */
public class DBManagerTest {
	@Test
	public void testDataSource() throws SQLException {
		Connection connection = DBManager.getConnection();
		connection.close();
	}
}
