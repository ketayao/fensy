package com.ketayao.fensy.exception;

/**
 * 缓存异常
 */
public class CacheException extends FensyException {

	/** 描述  */
	private static final long serialVersionUID = -5241676738291490786L;

	public CacheException(String s) {
		super(s);
	}

	public CacheException(String s, Throwable e) {
		super(s, e);
	}

	public CacheException(Throwable e) {
		super(e);
	}
	
}
