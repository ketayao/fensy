package com.ketayao.fensy.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.Constants;
import com.ketayao.fensy.exception.DBException;

/**
 * 数据库管理
 */
public class DBManager {

	private final static Logger log = LoggerFactory.getLogger(DBManager.class);
	private final static ThreadLocal<Connection> conns = new ThreadLocal<Connection>();
	private static DataSource dataSource;
	private static boolean showSql = false;
	public static String prefixTableName = "";
	
	static {
		initDataSource(null);
	}

	/**
	 * 初始化连接池
	 * @param props
	 * @param show_sql
	 */
	private final static void initDataSource(Properties dbProperties) {
		try {
			if (dbProperties == null) {
				dbProperties = new Properties();
				dbProperties.load(DBManager.class.getResourceAsStream("/" + Constants.FENSY_CONFIG_FILE));
			}

			Properties props = new Properties();
			for (Object key : dbProperties.keySet()) {
				String skey = (String) key;
				
				if (skey.startsWith("jdbc.")) {
					String name = skey.substring(5);
					props.put(name, dbProperties.getProperty(skey));
					if ("showSql".equalsIgnoreCase(name)) {
						showSql = "true".equalsIgnoreCase(dbProperties.getProperty(skey));
					} else if ("prefixTableName".equalsIgnoreCase(name)) {
						prefixTableName = dbProperties.getProperty(skey);
					}
				}
			}
			
			dataSource = (DataSource) Class.forName(props.getProperty("dataSource")).newInstance();
			if (dataSource.getClass().getName().indexOf("c3p0") > 0) {
				// Disable JMX in C3P0
				System.setProperty(
						"com.mchange.v2.c3p0.management.ManagementCoordinator",
						"com.mchange.v2.c3p0.management.NullManagementCoordinator");
			}
			log.info("Using DataSource : " + dataSource.getClass().getName());
			BeanUtils.populate(dataSource, props);// 将props的值注入dataSource属性。

			Connection conn = getConnection();
			DatabaseMetaData mdm = conn.getMetaData();
			log.info("Connected to " + mdm.getDatabaseProductName() + " " + mdm.getDatabaseProductVersion());
			closeConnection();
		} catch (Exception e) {
			throw new DBException(e);
		}
	}
	
	/**
	 * 断开连接池
	 */
	public final static void closeDataSource(){
		try {
			dataSource.getClass().getMethod("close").invoke(dataSource);
		} catch (NoSuchMethodException e){ 
		} catch (Exception e) {
			log.error("Unabled to destroy DataSource!!! ", e);
		}
	}

	/**
	 * 获取连接
	 * @return
	 * @throws SQLException
	 */
	public final static Connection getConnection() throws SQLException {
		Connection conn = conns.get();
		if(conn == null || conn.isClosed()){
			conn = dataSource.getConnection();
			conns.set(conn);
		}
		
		return (showSql && !Proxy.isProxyClass(conn.getClass())) ? 
				new DebugConnection(conn).getConnection() : conn;
	}
	
	/**
	 * 关闭连接
	 */
	public final static void closeConnection() {
		Connection conn = conns.get();
		try {
			if(conn != null && !conn.isClosed()){
				conn.setAutoCommit(true);
				conn.close();
			}
		} catch (SQLException e) {
			log.error("Unabled to close connection!!! ", e);
		}
		conns.remove();
	}

	/**
	 * 用于跟踪执行的SQL语句
	 */
	static class DebugConnection implements InvocationHandler {
		
		private final static Logger log = LoggerFactory.getLogger(DebugConnection.class);
		
		private Connection conn = null;

		public DebugConnection(Connection conn) {
			this.conn = conn;
		}

		/**
		 * Returns the conn.
		 * @return Connection
		 */
		public Connection getConnection() {
			return (Connection) Proxy.newProxyInstance(
						conn.getClass().getClassLoader(), 
                        conn.getClass().getInterfaces(), 
                        this);
		}
		
		public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
			try {
				String method = m.getName();
				if("prepareStatement".equals(method) || "createStatement".equals(method))
					log.info("[SQL] >>> " + args[0]);				
				return m.invoke(conn, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

	}
	
}
