package com.ketayao.fensy.mvc;

/**
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since 2014年2月28日 上午10:05:57
 */
public class ActionObject {
	// 全路径类名，类似com/ketayao/index
	private String name;
	private Object action;

	/**
	 * @param name
	 * @param action
	 */
	public ActionObject(String name, Object action) {
		super();
		this.name = name;
		this.action = action;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the action
	 */
	public Object getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(Object action) {
		this.action = action;
	}
}
