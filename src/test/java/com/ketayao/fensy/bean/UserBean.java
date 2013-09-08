/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Filename:		com.ketayao.fensy.test.UserBean.java
 * Class:			UserBean
 * Date:			2013年8月19日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version          3.1.0
 * Description:		
 *
 * </pre>
 **/
 
package com.ketayao.fensy.bean;

import java.util.List;

import com.ketayao.fensy.db.POJO;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Version  3.1.0
 * @since   2013年8月19日 上午10:47:25 
 */

public class UserBean extends POJO {
	private byte sex;
	private String name;
	private List<String> cards;
	private AddressBean address;
	
	/**  
	 * 返回 sex 的值   
	 * @return sex  
	 */
	public byte getSex() {
		return sex;
	}
	/**  
	 * 设置 sex 的值  
	 * @param sex
	 */
	public void setSex(byte sex) {
		this.sex = sex;
	}
	/**  
	 * 返回 name 的值   
	 * @return name  
	 */
	public String getName() {
		return name;
	}
	/**  
	 * 设置 name 的值  
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**  
	 * 返回 cards 的值   
	 * @return cards  
	 */
	public List<String> getCards() {
		return cards;
	}
	/**  
	 * 设置 cards 的值  
	 * @param cards
	 */
	public void setCards(List<String> cards) {
		this.cards = cards;
	}
	/**  
	 * 返回 address 的值   
	 * @return address  
	 */
	public AddressBean getAddress() {
		return address;
	}
	/**  
	 * 设置 address 的值  
	 * @param address
	 */
	public void setAddress(AddressBean address) {
		this.address = address;
	}
}
