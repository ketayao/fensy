package com.ketayao.fensy.cache;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存助手
 */
public class CacheManager {
	
	private final static Logger log = LoggerFactory.getLogger(CacheManager.class);
	private static CacheProvider provider;

	static {
		initCacheProvider("com.ketayao.fensy.cache.EhCacheProvider");
	}
	
	private static void initCacheProvider(String prv_name){
		try{
			CacheManager.provider = (CacheProvider)Class.forName(prv_name).newInstance();
			CacheManager.provider.start();
			log.info("Using CacheProvider : " + provider.getClass().getName());
		}catch(Exception e){
			log.error("Unabled to initialize cache provider:" + prv_name + ", using ehcache default.", e);
			CacheManager.provider = new EhCacheProvider();
		}
	}

	private final static Cache _getCache(String cache_name, boolean autoCreate) {
		if(provider == null){
			provider = new EhCacheProvider();
		}
		return provider.buildCache(cache_name, autoCreate);
	}

	/**
	 * 获取缓存中的数据
	 * @param name
	 * @param key
	 * @return
	 */
	public final static Object get(String name, Serializable key){
		//System.out.println("GET1 => " + name+":"+key);
		if(name!=null && key != null)
			return _getCache(name, true).get(key);
		return null;
	}
	
	/**
	 * 获取缓存中的数据
	 * @param <T>
	 * @param resultClass
	 * @param name
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T get(Class<T> resultClass, String name, Serializable key){
		//System.out.println("GET2 => " + name+":"+key);
		if(name!=null && key != null)
			return (T)_getCache(name, true).get(key);
		return null;
	}
	
	/**
	 * 写入缓存
	 * @param name
	 * @param key
	 * @param value
	 */
	public final static void set(String name, Serializable key, Serializable value){
		//System.out.println("SET => " + name+":"+key+"="+value);
		if(name!=null && key != null && value!=null)
			_getCache(name, true).put(key, value);		
	}
	
	/**
	 * 清除缓冲中的某个数据
	 * @param name
	 * @param key
	 */
	public final static void evict(String name, Serializable key){
		if(name!=null && key != null)
			_getCache(name, true).remove(key);		
	}
	
	/**
	 * 清楚所有的缓存
	 * 描述
	 * @param name
	 */
	public final static void clear(String name){
		if(name!=null)
			_getCache(name, true).clear();		
	}

	/**
	 * 清除缓冲中的某个数据
	 * @param name
	 * @param key
	 */
	public final static void justEvict(String name, Serializable key){
		if(name!=null && key != null){
			Cache cache = _getCache(name, false);
			if(cache != null)
				cache.remove(key);
		}
	}

}
