package com.ketayao.fensy.mvc.view;

import java.util.HashMap;
import java.util.Map;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年2月27日 下午3:36:58 
 */
public abstract class ViewMap {
	public final static String VIEW_JSP = "jsp";
	public final static String VIEW_FREEMARKER = "freemarker";
	
	private static Map<String, View> views = new HashMap<String, View>();
	
	static {
		views.put(VIEW_JSP, new JSPView());
		views.put(VIEW_FREEMARKER, new FreeMarkerView());
	}

	/**
	 * @return the views
	 */
	public static View getView(String name) {
		return views.get(name);
	}
}
