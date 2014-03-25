package com.ketayao.fensy.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.ArrayUtils;

import com.ketayao.fensy.cache.CacheManager;
import com.ketayao.fensy.exception.DBException;

/**
 * 数据库查询助手
 */
@SuppressWarnings("unchecked")
public class QueryHelper {
	
	private final static QueryRunner QUERY_RUNNER = new QueryRunner();
	
	@SuppressWarnings("rawtypes")
	private final static ColumnListHandler COLUMN_LIST_HANDLER = new ColumnListHandler();
	
	@SuppressWarnings("rawtypes")
	private final static ScalarHandler SCALAR_HANDLER = new ScalarHandler();
	
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
	
	/**
	 * 获取数据库连接
	 * @return
	 */
	public static Connection getConnection() {
		try{
			return DBManager.getConnection();
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	/**
	 * 读取某个对象
	 * @param beanClass
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> T read(Class<T> beanClass, String sql, Object... params) {
		try {
			return (T) QUERY_RUNNER.query(getConnection(), sql, 
					isPrimitive(beanClass) ? SCALAR_HANDLER : new BeanHandler<T>(beanClass), params);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	/**
	 * 从缓存中，读取某个对象
	 * @param beanClass
	 * @param cacheRegion
	 * @param key
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> T readFromCache(Class<T> beanClass, String cacheRegion, Serializable key, String sql, Object...params) {
		T obj = (T) CacheManager.get(cacheRegion, key);
		if (obj == null) {
			obj = read(beanClass, sql, params);
			CacheManager.put(cacheRegion, key, (Serializable) obj);
		}
		return obj;
	}
	
	/**
	 * 对象查询
	 * @param <T>
	 * @param beanClass
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> List<T> query(Class<T> beanClass, String sql, Object... params) {
		try {
			return (List<T>) QUERY_RUNNER.query(getConnection(), sql,
					isPrimitive(beanClass) ? COLUMN_LIST_HANDLER : new BeanListHandler<T>(beanClass), params);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 支持缓存的对象查询
	 * @param <T>
	 * @param beanClass
	 * @param cacheRegion
	 * @param key
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> List<T> queryFromCache(Class<T> beanClass, String cacheRegion, Serializable key, String sql, Object... params) {
		List<T> objs = (List<T>) CacheManager.get(cacheRegion, key);
		if (objs == null) {
			objs = query(beanClass, sql, params);
			CacheManager.put(cacheRegion, key, (Serializable) objs);
		}
		return objs;
	}
	
	/**
	 * 分页查询
	 * @param <T>
	 * @param beanClass
	 * @param sql
	 * @param page
	 * @param count
	 * @param params
	 * @return
	 */
	public static <T> List<T> querySlice(Class<T> beanClass, String sql, int page, int count, Object...params) {
		if (page < 0 || count < 0)
			throw new IllegalArgumentException("Illegal parameter of 'page' or 'count', Must be positive.");
		int from = (page - 1) * count;
		count = (count > 0) ? count : Integer.MAX_VALUE;
		return query(beanClass, sql + " LIMIT ?,?",
				ArrayUtils.addAll(params, (Object[]) (new Integer[] { from, count })));
	}
	
	/**
	 * 支持缓存的分页查询
	 * @param <T>
	 * @param beanClass
	 * @param cacheRegion
	 * @param key
	 * @param cacheObjCount
	 * @param sql
	 * @param page
	 * @param count
	 * @param params
	 * @return
	 */
	public static <T> List<T> querySliceCache(Class<T> beanClass, String cacheRegion, Serializable key, int cacheObjCount, String sql, int page, int count, Object...params) {
		List<T> objs = (List<T>) CacheManager.get(cacheRegion, key);
		if (objs == null) {
			objs = querySlice(beanClass, sql, 1, cacheObjCount, params);
			CacheManager.put(cacheRegion, key, (Serializable) objs);
		}
		if (objs == null || objs.size() == 0)
			return objs;
		int from = (page - 1) * count;
		if (from < 0)
			return null;
		if ((from + count) > cacheObjCount)// 超出缓存的范围
			return querySlice(beanClass, sql, page, count, params);
		int end = Math.min(from + count, objs.size());
		if (from >= end)
			return null;
		return objs.subList(from, end);
	}
	
	/**
	 * 执行统计查询语句，语句的执行结果必须只返回一个数值
	 * @param sql
	 * @param params
	 * @return
	 */
	public static long stat(String sql, Object... params) {
		try {
			//Number num = (Number) QUERY_RUNNER.query(getConnection(), sql, SCALAR_HANDLER, params);
			//return (num != null) ? num.longValue() : -1;
			
			Long num = QUERY_RUNNER.query(getConnection(), sql, new ScalarHandler<Long>(), params);
			return (num != null) ? num.longValue() : -1;
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 执行统计查询语句，语句的执行结果必须只返回一个数值
	 * @param cacheRegion
	 * @param key
	 * @param sql
	 * @param params
	 * @return
	 */
	public static long statFromCache(String cacheRegion, Serializable key, String sql, Object... params) {
		Long value = (Long) CacheManager.get(cacheRegion, key);
		if (value == null) {
			value = stat(sql, params);
			CacheManager.put(cacheRegion, key, value);
		}
		return value.longValue();
	}

	/**
	 * 执行INSERT/UPDATE/DELETE语句
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int update(String sql, Object...params) {
		try{
			return QUERY_RUNNER.update(getConnection(), sql, params);
		}catch(SQLException e){
			throw new DBException(e);
		}
	}
	
	/**
	 * 批量执行指定的SQL语句
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int[] batch(String sql, Object[][] params) {
		try{
			return QUERY_RUNNER.batch(getConnection(), sql, params);
		}catch(SQLException e){
			throw new DBException(e);
		}
	}
}
