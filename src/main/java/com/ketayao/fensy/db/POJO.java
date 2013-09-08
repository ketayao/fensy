package com.ketayao.fensy.db;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.cache.CacheManager;
import com.ketayao.fensy.exception.DBException;

/**
 * 数据库对象的基类
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a> 
 * @since 2013年7月26日 下午4:10:55
 */
public class POJO implements Serializable {

	/** 描述  */
	private static final long serialVersionUID = 5067257505248053269L;
	
	protected Logger logger = LoggerFactory.getLogger(POJO.class);
	
	protected final static transient char OBJ_COUNT_CACHE_KEY = '#';
	private long ___key_id;

	public long getId() {
		return ___key_id;
	}

	public void setId(long id) {
		this.___key_id = id;
	}

	private String __this_table_name;

	public void evictCache(Serializable key) {
		CacheManager.evict(cacheRegion(), key);
	}

	public void setCache(Serializable key, Serializable value) {
		CacheManager.set(cacheRegion(), key, value);
	}

	public Object getCache(Serializable key) {
		return CacheManager.get(cacheRegion(), key);
	}

	public POJO get(java.math.BigInteger id) {
		if (id == null)
			return null;
		return get(id.longValue());
	}

	/**
	 * 根据主键读取对象详细资料，根据预设方法自动判别是否需要缓存
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends POJO> T get(long id) {
		if (id <= 0)
			return null;
		String sql = "SELECT * FROM " + tableName() + " WHERE id=?";
		boolean cached = isObjectCachedByID();
		return (T) QueryHelper.read_cache(getClass(), cached ? cacheRegion()
				: null, id, sql, id);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends POJO> T getByAttr(String attrName, Object attrValue) {
		String sql = "SELECT * FROM " + tableName() + " WHERE " + attrName
				+ " = ?";
		return (T) QueryHelper.read(getClass(), sql, attrValue);
	}

	/**
	 * 直接从数据库获取
	 * 描述
	 * @param ids
	 * @return
	 */
	public List<? extends POJO> batchGet(List<Long> ids) {
		if (ids == null || ids.size() == 0)
			return null;
		StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName()
				+ " WHERE id IN (");
		for (int i = 1; i <= ids.size(); i++) {
			sql.append('?');
			if (i < ids.size())
				sql.append(',');
		}
		sql.append(')');
		List<? extends POJO> beans = QueryHelper.query(getClass(),
				sql.toString(), ids.toArray(new Object[ids.size()]));
		if (isObjectCachedByID()) {
			for (Object bean : beans) {
				CacheManager.set(cacheRegion(), ((POJO) bean).getId(),
						(Serializable) bean);
			}
		}
		return beans;
	}
	
	// 对这个cache的更新操作需要格外留意
	public List<Long> ids(String filter, Object... params) {
		String sql = "SELECT id FROM " + tableName() + " WHERE " + filter;
		
		String cacheKey = filter;
		for (Object obj : params) {
			cacheKey += obj.toString();
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("from " + cacheRegion() + ",cacheKey is " + cacheKey);
		}
		
		return QueryHelper.query_cache(Long.class, queryCacheRegion(), cacheKey,
				sql, params);
	}
	
	public List<Long> otherIds(String otherId, String filter, Object... params) {
		String sql = "SELECT " + otherId +  " FROM " + tableName() + " WHERE " + filter;
		
		String cacheKey = sql;
		for (Object obj : params) {
			cacheKey += obj;
		}
		
		return QueryHelper.query_cache(Long.class, queryCacheRegion(), cacheKey,
				sql, params);
	}
	
	public List<? extends POJO> list() {
		String sql = "SELECT * FROM " + tableName() + " ";
		return QueryHelper.query(getClass(), sql);
	}
	
	public List<? extends POJO> list(String filter) {
		String sql = "SELECT * FROM " + tableName() + " WHERE " + filter;
		return QueryHelper.query(getClass(), sql);
	}
	
	/**
	 * 分页列出所有对象
	 * 
	 * @param page
	 * @param size
	 * @return
	 */
	public List<? extends POJO> list(int page, int size) {
		String sql = "SELECT * FROM " + tableName() + " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}
	
	public List<? extends POJO> list(int page, int size, String orderBy) {
		String sql = "SELECT * FROM " + tableName() + " ";
		if (StringUtils.isNotBlank(orderBy)) {
			sql += "ORDER BY " + orderBy;
		}
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}

	public List<? extends POJO> filter(String filter, int page, int size) {
		String sql = "SELECT * FROM " + tableName() + " WHERE " + filter
				+ " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}
	
	public List<? extends POJO> filter(String filter, int page, int size, String orderBy) {
		String sql = "SELECT * FROM " + tableName() + " WHERE " + filter + " ORDER BY " + orderBy;
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}
	
	/**
	 * 统计此对象的总记录数
	 * 
	 * @return
	 */
	public int totalCount() {
		if (this.isObjectCachedByID())
			return (int) QueryHelper.stat_cache(cacheRegion(),
					OBJ_COUNT_CACHE_KEY, "SELECT COUNT(*) FROM " + tableName());
		return (int) QueryHelper.stat("SELECT COUNT(*) FROM " + tableName());
	}

	/**
	 * 统计此对象的总记录数
	 * 
	 * @return
	 */
	public int totalCount(String filter) {
		return (int) QueryHelper.stat("SELECT COUNT(*) FROM " + tableName()
				+ " WHERE " + filter);
	}
	
	 /**
     * 批量加载项目
     * @param pids
     * @return
     */
	@SuppressWarnings("rawtypes")
	public List loadList(List<Long> p_pids) {
		if (p_pids == null)
			return null;
		final List<Long> pids = new ArrayList<Long>(p_pids.size());
		for (Number obj : p_pids) {
			pids.add(obj.longValue());
		}
		String cache = this.cacheRegion();
		List<POJO> prjs = new ArrayList<POJO>(pids.size()) {
			private static final long serialVersionUID = 1L;

			{
				for (int i = 0; i < pids.size(); i++)
					add(null);
			}
		};
		List<Long> no_cache_ids = new ArrayList<Long>();
		for (int i = 0; i < pids.size(); i++) {
			long pid = pids.get(i);
			POJO obj = (POJO) CacheManager.get(cache, pid);

			if (obj != null)
				prjs.set(i, obj);
			else {
				no_cache_ids.add(pid);
			}
		}

		if (no_cache_ids.size() > 0) {
			List<? extends POJO> no_cache_prjs = batchGet(no_cache_ids);
			if (no_cache_prjs != null)
				for (POJO obj : no_cache_prjs) {
					prjs.set(pids.indexOf(obj.getId()), obj);
				}
		}

		no_cache_ids = null;

		// Check Users
//		if (prjs != null && isAutoLoadUser()) {
//			List<Long> no_cache_userids = new ArrayList<Long>();
//			String user_cache = User.INSTANCE.cacheRegion();
//			for (POJO pojo : prjs) {
//				if (pojo == null)
//					continue;
//				long userid = pojo.getAutoLoadUser();
//				if (userid > 0 && !no_cache_userids.contains(userid)) {
//					POJO user = (POJO) CacheManager.get(user_cache, userid);
//					if (user == null) {
//						no_cache_userids.add(userid);
//					}
//				}
//			}
//			if (no_cache_userids.size() > 0)
//				User.INSTANCE.batchGet(no_cache_userids);
//
//			no_cache_userids = null;
//		}

		return prjs;
	}

	/**
	 * 插入对象到数据库表中
	 * 
	 * @return
	 */
	public long save() {
		if (getId() > 0)
			_insertObject(this);
		else
			setId(_insertObject(this));
		if (this.isObjectCachedByID())
			CacheManager.evict(cacheRegion(), OBJ_COUNT_CACHE_KEY);
		return getId();
	}

	/**
	 * 根据id主键删除对象
	 * 
	 * @return
	 */
	public boolean delete() {
		boolean dr = evict(QueryHelper.update("DELETE FROM " + tableName()
				+ " WHERE id=?", getId()) == 1);
		if (dr) {
			CacheManager.evict(cacheRegion(), OBJ_COUNT_CACHE_KEY);
			evict(true);
		}
		return dr;
	}

	public boolean updateAttr(String attrName, Object attrValue) {
		String sql = "update " + tableName() + " set " + attrName
				+ " = ? where id = ?";
		int ret = QueryHelper.update(sql, attrValue, getId());
		try {
			if (ret > 0) {
				BeanUtils.setProperty(this, attrName, attrValue);
			} else {
				evict(true); // 更新失败删除缓存对象
				return false;
			}
		} catch (Exception e) {
			evict(true);// 更新失败删除缓存对象
			return false;
		}
		return true;
	}

	public boolean updateAttrs(String[] attrNames, Object[] attrValues) {
		int len = attrNames.length;
		List<String> kvs = new ArrayList<String>(len);
		for (String attr : attrNames) {
			kvs.add(attr + " = ?");
		}

		String sql = "update " + tableName() + " set "
				+ StringUtils.join(kvs, ',') + " where id = ?";
		List<Object> vals = new ArrayList<Object>();
		for (Object val : attrValues) {
			vals.add(val);
		}
		vals.add(getId());

		int ret = QueryHelper.update(sql, vals.toArray());
		try {
			if (ret > 0) {
				for (int i = 0; i < len; i++) {
					BeanUtils.setProperty(this, attrNames[i], attrValues[i]);
				}
			} else {
				evict(true);// 更新失败删除缓存对象
				return false;
			}
		} catch (Exception e) {
			evict(true);// 更新失败删除缓存对象
			return false;
		}
		return true;
	}
	
	/**
	 * 根据条件决定是否清除对象缓存
	 * 
	 * @param er
	 * @return
	 */
	public boolean evict(boolean er) {
		if (er && isObjectCachedByID())
			CacheManager.evict(cacheRegion(), getId());
		return er;
	}

	/**
	 * 清除指定主键的对象缓存
	 * 
	 * @param obj_id
	 */
	public void evict(long obj_id) {
		CacheManager.evict(cacheRegion(), obj_id);
	}
	
	/**
	 * 返回默认的对象对应的表名
	 * 
	 * @return
	 */
	protected String tableName() {
		if (__this_table_name == null)
			__this_table_name = "lovej_"
					+ Inflector.getInstance().tableize(getClass());
			//__this_table_name = Inflector.getInstance().tableize(getClass());
		return __this_table_name;
	}
	
	/**
	 * 返回对象对应的查询缓存区域名
	 * 
	 * @return
	 */
	protected String queryCacheRegion() {
		return cacheRegion();
	}
	
	/**
	 * 返回对象对应的缓存区域名
	 * 
	 * @return
	 */
	protected String cacheRegion() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 是否根据ID缓存对象，此方法对Get(long id)有效
	 * 
	 * @return
	 */
	protected boolean isObjectCachedByID() {
		return true;
	}
	
	/**
	 * 列出要插入到数据库的域集合，子类可以覆盖此方法
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> listInsertableFields() {
		try {
			Map<String, Object> props = BeanUtils.describe(this);
			if (getId() <= 0)
				props.remove("id");
			props.remove("class");
			
			Map<String, Object> priMap = new HashMap<String, Object>();
			for (Entry<String, Object> entry : props.entrySet()) {
				Field field = this.getClass().getDeclaredField(entry.getKey());
				
				if (QueryHelper._isPrimitive(field.getType())) {
					priMap.put(entry.getKey(), entry.getValue());
				}
			}
			
			return priMap;
		} catch (Exception e) {
			throw new RuntimeException("Exception when Fetching fields of "
					+ this, e);
		}
	}
	
	/**
	 * 插入对象
	 * 
	 * @param obj
	 * @return 返回插入对象的主键
	 */
	private static long _insertObject(POJO obj) {
		Map<String, Object> pojo_bean = obj.listInsertableFields();
		String[] fields = pojo_bean.keySet().toArray(
				new String[pojo_bean.size()]);
		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(obj.tableName());
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
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = QueryHelper.getConnection().prepareStatement(sql.toString(),
					PreparedStatement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < fields.length; i++) {
				ps.setObject(i + 1, pojo_bean.get(fields[i]));
			}
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			return rs.next() ? rs.getLong(1) : -1;
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			sql = null;
			fields = null;
			pojo_bean = null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		// 不同的子类尽管ID是相同也是不相等的
		if (!getClass().equals(obj.getClass()))
			return false;
		POJO wb = (POJO) obj;
		return wb.getId() == getId();
	}

}
