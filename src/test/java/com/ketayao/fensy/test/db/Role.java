package com.ketayao.fensy.test.db;

import java.util.Date;

import com.ketayao.fensy.db.POJO;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年3月20日 下午4:51:17 
 */
public class Role extends POJO {
	private static final long serialVersionUID = -2860430873124309927L;
	public static final Role INSTANCE = new Role();
	private String name;
	private String description;
	private Date createTime;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
