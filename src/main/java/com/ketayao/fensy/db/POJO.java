package com.ketayao.fensy.db;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.cache.CacheManager;
import com.ketayao.fensy.exception.DBException;
import com.ketayao.fensy.util.StringUtils;

/**
 * 数据库对象的基类
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since 2013年7月26日 下午4:10:55
 */
public class POJO extends BaseEntity {

    private static final long             serialVersionUID    = -8293013385107724530L;

    protected static final Logger         logger              = LoggerFactory.getLogger(POJO.class);

    protected static final transient char OBJ_COUNT_CACHE_KEY = '#';

    /**
     * 插入对象到数据库表中
     * 
     * @return
     */
    public long save() {
        if (getId() > 0)
            insertObject(this);
        else
            setId(insertObject(this));
        if (isCachedByID()) {
            evictCache(OBJ_COUNT_CACHE_KEY);
            putCache(getId(), this);
        }
        return getId();
    }

    /**
     * 根据id主键删除对象
     * 
     * @return
     */
    public boolean delete() {
        boolean dr = (QueryHelper.update("DELETE FROM " + getTableName() + " WHERE id=?",
            getId()) == 1);

        if (dr) {
            CacheManager.evict(getCacheRegion(), OBJ_COUNT_CACHE_KEY);
            evictCache(dr);
        }
        return dr;
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
        String sql = "SELECT * FROM " + getTableName() + " WHERE id=?";
        return (T) QueryHelper.readFromCache(getClass(), isCachedByID() ? getCacheRegion() : null,
            id, sql, id);
    }

    /**
     * 根据属性查找对象
     * 
     * @param attrName
     * @param attrValue
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends POJO> T getByAttr(String attrName, Object attrValue) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + attrName + " = ?";
        return (T) QueryHelper.read(getClass(), sql, attrValue);
    }

    /**
     * 更新属性
     * 
     * @param attrName
     * @param attrValue
     * @return
     */
    public boolean updateAttr(String attrName, Object attrValue) {
        String sql = "UPDATE " + getTableName() + " SET " + attrName + " = ? WHERE id = ?";
        int ret = QueryHelper.update(sql, attrValue, getId());
        try {
            if (ret > 0) {
                BeanUtils.setProperty(this, attrName, attrValue);
            } else {
                // 更新失败删除缓存对象
                if (isCachedByID()) {
                    evictCache(getId());
                }
                return false;
            }
        } catch (Exception e) {
            // 更新失败删除缓存对象
            if (isCachedByID()) {
                evictCache(getId());
            }
            return false;
        }
        return true;
    }

    /**
     * 更新属性
     * 
     * @param attrNames
     * @param attrValues
     * @return
     */
    public boolean updateAttrs(String[] attrNames, Object[] attrValues) {
        int len = attrNames.length;
        List<String> kvs = new ArrayList<String>(len);
        for (String attr : attrNames) {
            kvs.add(attr + " = ?");
        }

        String sql = "UPDATE " + getTableName() + " SET " + StringUtils.join(kvs, ',')
                     + " WHERE id = ?";
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
                // 更新失败删除缓存对象
                if (isCachedByID()) {
                    evictCache(getId());
                }
                return false;
            }
        } catch (Exception e) {
            // 更新失败删除缓存对象
            if (isCachedByID()) {
                evictCache(getId());
            }
            return false;
        }
        return true;
    }

    /**
     * 直接从数据库获取 描述
     * 
     * @param ids
     * @return
     */
    public List<? extends POJO> batchGet(List<Long> ids) {
        if (ids == null || ids.size() == 0)
            return null;

        StringBuilder sql = new StringBuilder("SELECT * FROM " + getTableName() + " WHERE id IN (");
        for (int i = 1; i <= ids.size(); i++) {
            sql.append('?');
            if (i < ids.size())
                sql.append(',');
        }
        sql.append(')');

        List<? extends POJO> beans = QueryHelper.query(getClass(), sql.toString(),
            ids.toArray(new Object[ids.size()]));
        if (isCachedByID()) {
            for (Object bean : beans) {
                putCache(((POJO) bean).getId(), (Serializable) bean);
            }
        }
        return beans;
    }

    /**
     * 返回所有对象
     * 
     * @return
     */
    public List<? extends POJO> list() {
        String sql = "SELECT * FROM " + getTableName() + " ";
        return QueryHelper.query(getClass(), sql);
    }

    /**
     * 根据条件返回对象集合
     * 
     * @param filter
     * @return
     */
    public List<? extends POJO> list(String filter) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + filter;
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
        return list(page, size, "id DESC");
    }

    /**
     * 根据排序条件，分页列出所有对象
     * 
     * @param page
     * @param size
     * @return
     */
    public List<? extends POJO> list(int page, int size, String orderBy) {
        String sql = "SELECT * FROM " + getTableName() + " ";
        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY " + orderBy;
        }
        return QueryHelper.querySlice(getClass(), sql, page, size);
    }

    /**
     * 根据查询条件，分页列出对象集合
     * 
     * @param page
     * @param size
     * @param filter
     * @return
     */
    public List<? extends POJO> filter(int page, int size, String filter) {
        return filter(page, size, filter, "id DESC");
    }

    /**
     * 根据查询与排序条件，分页列出对象集合
     * 
     * @param page
     * @param size
     * @param filter
     * @param orderBy
     * @return
     */
    public List<? extends POJO> filter(int page, int size, String filter, String orderBy) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + filter;
        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY " + orderBy;
        }
        return QueryHelper.querySlice(getClass(), sql, page, size);
    }

    /**
     * 返回对象总数量
     * 
     * @return
     */
    public long totalCount() {
        if (isCachedByID())
            return (long) QueryHelper.statFromCache(getCacheRegion(), OBJ_COUNT_CACHE_KEY,
                "SELECT COUNT(*) FROM " + getTableName());
        return (long) QueryHelper.stat("SELECT COUNT(*) FROM " + getTableName());
    }

    /**
     * 得到满足条件的对象id集合，并放入查询缓存。 对这个cache的更新操作需要格外留意
     * 
     * @param filter
     * @param params
     * @return
     */
    public List<Long> getIds(String filter, Object... params) {
        return getKeys("id", filter, params);
    }

    /**
     * 得到满足条件的对象id集合，并放入查询缓存。 对这个cache的更新操作需要格外留意
     * 
     * @param filter
     * @param params
     * @return
     */
    public List<Long> getKeys(String key, String filter, Object... params) {
        String sql = "SELECT " + key + " FROM " + getTableName() + " WHERE " + filter;

        StringBuilder cacheKey = new StringBuilder(filter + "=");
        for (Object obj : params) {
            cacheKey.append(obj.toString() + ",");
        }
        cacheKey.deleteCharAt(cacheKey.length() - 1);

        if (logger.isDebugEnabled()) {
            logger.debug("FROM " + getCacheRegion() + ", cacheKey=" + cacheKey);
        }

        return QueryHelper.queryFromCache(Long.class, getQueryCacheRegion(), cacheKey, sql, params);
    }

    /**
     * 根据条件，返回对象数量
     * 
     * @return
     */
    public long totalCount(String filter) {
        return (long) QueryHelper
            .stat("SELECT COUNT(*) FROM " + getTableName() + " WHERE " + filter);
    }

    /**
     * 批量加载对象
     * 
     * @param pids
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List loadList(final List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList(0);

        // 因为要保持有序，所以按照id顺序创建对象
        List<POJO> prjs = new ArrayList<POJO>(ids.size()) {
            private static final long serialVersionUID = 1L;

            {
                for (int i = 0; i < ids.size(); i++)
                    add(null);
            }
        };

        List<Long> noCacheIds = new ArrayList<Long>();
        for (int i = 0; i < ids.size(); i++) {
            long id = ids.get(i);
            POJO obj = (POJO) getCache(id);

            if (obj != null)
                prjs.set(i, obj);
            else {
                noCacheIds.add(id);
            }
        }

        if (noCacheIds.size() > 0) {
            List<? extends POJO> objs = batchGet(noCacheIds);
            if (objs != null)
                for (POJO obj : objs) {
                    prjs.set(ids.indexOf(obj.getId()), obj);
                }
        }

        return prjs;
    }

    /**
     * 插入对象
     * 
     * @param obj
     * @return 返回插入对象的主键
     */
    private static long insertObject(POJO obj) {
        Map<String, Object> pojo = obj.listInsertableFields();
        String[] fields = pojo.keySet().toArray(new String[pojo.size()]);

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(obj.getTableName());
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
                ps.setObject(i + 1, pojo.get(fields[i]));
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
            pojo = null;
        }
    }
}
