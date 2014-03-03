package com.ketayao.fensy.mvc;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.exception.ActionException;
import com.ketayao.fensy.util.CryptUtils;
import com.ketayao.fensy.util.ResourceUtils;
import com.ketayao.fensy.webutil.Multimedia;
import com.ketayao.fensy.webutil.RequestUtils;

/**
 * 请求上下文
 * 
 * @date 2010-1-13 下午04:18:00
 */
public class RequestContext {

	private final static Logger log = LoggerFactory.getLogger(RequestContext.class);
	
	private final static int MAX_FILE_SIZE = 10 * 1024 * 1024;
	private final static String UTF_8 = "UTF-8";

	private final static ThreadLocal<RequestContext> contexts = new ThreadLocal<RequestContext>();
	private final static boolean isResin;
	private final static String upload_tmp_path;
	private final static String TEMP_UPLOAD_PATH_ATTR_NAME = "$TEMP_UPLOAD_PATH$";

	private static String webroot = null;

	private ServletContext context;
	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Map<String, Cookie> cookies;

	static {
		webroot = getWebrootPath();
		isResin = _checkResinVersion();
		// 上传的临时目录
		upload_tmp_path = webroot + "WEB-INF" + File.separator + "tmp"
				+ File.separator;
		try {
			FileUtils.forceMkdir(new File(upload_tmp_path));
		} catch (IOException excp) {
		}

		// BeanUtils对时间转换的初始化设置
		ConvertUtils.register(new SqlDateConverter(null), java.sql.Date.class);
		ConvertUtils.register(new Converter() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
			SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy-M-d H:m");

			@SuppressWarnings("rawtypes")
			public Object convert(Class type, Object value) {
				if (value == null)
					return null;
				if (value instanceof Date)
					return (value);
				try {
					return sdf_time.parse(value.toString());
				} catch (ParseException e) {
					try {
						return sdf.parse(value.toString());
					} catch (ParseException e1) {
						return null;
					}
				}
			}
		}, java.util.Date.class);
	}

	private final static String getWebrootPath() {
		String root = RequestContext.class.getResource("/").getFile();
		try {
			root = new File(root).getParentFile().getParentFile()
					.getCanonicalPath();
			root += File.separator;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return root;
	}

	/**
	 * 初始化请求上下文，默认转码为UTF8
	 * 
	 * @param ctx
	 * @param req
	 * @param res
	 */
	public static RequestContext begin(ServletContext ctx,
			HttpServletRequest req, HttpServletResponse res) {
		RequestContext rc = new RequestContext();
		rc.context = ctx;
		rc.request = _autoUploadRequest(encodeRequest(req));// 是否是上传请求

		rc.response = res;
		rc.response.setCharacterEncoding(UTF_8);
		
		// 保存request_locale参数设置
		rc.saveLocaleFromRequest();

		rc.session = req.getSession(false); //默认不创建session
		//rc.session = req.getSession();
		rc.cookies = new HashMap<String, Cookie>();// 获取cookie
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie ck : cookies) {
				rc.cookies.put(ck.getName(), ck);
			}
		}
		
		// 处理locale
		rc.saveLocaleFromRequest();
		
		contexts.set(rc);
		return rc;
	}

	/**
	 * 获取当前请求的上下文
	 * 
	 * @return
	 */
	public static RequestContext get() {
		return contexts.get();
	}

	/**
	 * 删除上传临时文件，清除context上下文。
	 */
	public void end() {
		String tmpPath = (String) request
				.getAttribute(TEMP_UPLOAD_PATH_ATTR_NAME);
		if (tmpPath != null) {
			try {
				FileUtils.deleteDirectory(new File(tmpPath));
			} catch (IOException e) {
				log.error("Failed to cleanup upload directory: " + tmpPath, e);
			}
		}
		this.context = null;
		this.request = null;
		this.response = null;
		this.session = null;
		this.cookies = null;
		contexts.remove();
	}
	
	public static final String LOCALE = "___locale";
	
	public Locale getLocale() {
		Cookie cookie = getCookie(LOCALE);
		if (cookie != null) {
			return LocaleUtils.toLocale(cookie.getValue());
		}
		
		return request.getLocale();
	}
	
	public void setLocale(String localeValue) {
		if (localeValue != null) {
			Locale locale = null;
			try {
				locale = LocaleUtils.toLocale(localeValue);
				setCookie(LOCALE, locale.getLanguage() + "_" + locale.getCountry(), RequestContext.MAX_AGE, true);
			} catch (Exception e) {
				log.warn("setLocale is error, localeValue=" + localeValue + "is not used.");
			}
		}
	}
	
	public void saveLocaleFromRequest() {
		String request_locale = getParam("request_locale");
		setLocale(request_locale);
	}

	/**
	 * 自动编码处理
	 * 
	 * @param req
	 * @return
	 */
	private static HttpServletRequest encodeRequest(HttpServletRequest req) {
		if (req instanceof RequestProxy)
			return req;
		HttpServletRequest auto_encoding_req = req;
		if ("POST".equalsIgnoreCase(req.getMethod())) {
			try {
				auto_encoding_req.setCharacterEncoding(UTF_8);
			} catch (UnsupportedEncodingException e) {
			}
		} else if (!isResin)
			auto_encoding_req = new RequestProxy(req, UTF_8);

		return auto_encoding_req;
	}

	/**
	 * 自动文件上传请求的封装
	 * 
	 * @param req
	 * @return
	 */
	private static HttpServletRequest _autoUploadRequest(HttpServletRequest req) {
		if (isMultipart(req)) {
			String path = upload_tmp_path
					+ RandomStringUtils.randomAlphanumeric(10);
			File dir = new File(path);
			if (!dir.exists() && !dir.isDirectory())
				dir.mkdirs();
			try {
				req.setAttribute(TEMP_UPLOAD_PATH_ATTR_NAME, path);
				return new MultipartRequest(req, dir.getCanonicalPath(),
						MAX_FILE_SIZE, UTF_8);
			} catch (NullPointerException e) {
			} catch (IOException e) {
				log.error("Failed to save upload files into temp directory: "
						+ path, e);
			}
		}
		return req;
	}

	public long getId() {
		return getParam("id", 0L);
	}
	
	public String getIp() {
		return RequestUtils.getRemoteAddr(request);
	}
	
	public String getQueryString() {
		return request.getQueryString();
	}

	@SuppressWarnings("unchecked")
	public Enumeration<String> getParams() {
		return request.getParameterNames();
	}
	
	public String getParam(String name) {
		return request.getParameter(name);
	}

	public String getParam(String name, String... def_value) {
		String v = request.getParameter(name);
		return (v != null) ? v : ((def_value.length > 0) ? def_value[0] : null);
	}

	public long getParam(String name, long def_value) {
		return NumberUtils.toLong(getParam(name), def_value);
	}

	public int getParam(String name, int def_value) {
		return NumberUtils.toInt(getParam(name), def_value);
	}

	public byte getParam(String name, byte def_value) {
		return (byte) NumberUtils.toInt(getParam(name), def_value);
	}

	public String[] getParams(String name) {
		return request.getParameterValues(name);
	}

	public void redirect(String uri) throws IOException {
		response.sendRedirect(uri);
	}

	public void forward(String uri) throws ServletException, IOException {
		RequestDispatcher rd = context.getRequestDispatcher(uri);
		rd.forward(request, response);
	}

	public void include(String uri) throws ServletException, IOException {
		RequestDispatcher rd = context.getRequestDispatcher(uri);
		rd.include(request, response);
	}

	public boolean isUpload() {
		return (request instanceof MultipartRequest);
	}
	
	public boolean isRobot() {
		return RequestUtils.isRobot(request);
	}

	public File getFile(String fieldName) {
		if (request instanceof MultipartRequest)
			return ((MultipartRequest) request).getFile(fieldName);
		return null;
	}

	public File getImage(String fieldname) {
		File imgFile = getFile(fieldname);
		return (imgFile != null && Multimedia.isImageFile(imgFile.getName())) ? imgFile
				: null;
	}

	public ActionException fromResource(String bundle, String key,
			Object... args) {
		String res = ResourceUtils.getStringForLocale(request.getLocale(),
				bundle, key, args);
		return new ActionException(res);
	}

	/**
	 * 输出信息到浏览器
	 * 
	 * @param msg
	 * @throws IOException
	 */
	public void print(Object msg) throws IOException {
		response.setContentType("text/html;charset=utf-8");
		if (!UTF_8.equalsIgnoreCase(response.getCharacterEncoding()))
			response.setCharacterEncoding(UTF_8);
		response.getWriter().print(msg);
	}

	public void printJson(String[] key, Object[] value) throws IOException {
		StringBuilder json = new StringBuilder("{");
		for (int i = 0; i < key.length; i++) {
			if (i > 0)
				json.append(',');
			boolean isNum = value[i] instanceof Number;
			json.append("\"");
			json.append(key[i]);
			json.append("\":");
			if (!isNum)
				json.append("\"");
			json.append(value[i]);
			if (!isNum)
				json.append("\"");
		}
		json.append("}");
		print(json.toString());
	}

	public void printJson(String key, Object value) throws IOException {
		printJson(new String[] { key }, new Object[] { value });
	}

	public void error(int code, String... msg) throws IOException {
		if (msg.length > 0)
			response.sendError(code, msg[0]);
		else
			response.sendError(code);
	}

	public void forbidden() throws IOException {
		error(HttpServletResponse.SC_FORBIDDEN);
	}

	public void notFound() throws IOException {
		error(HttpServletResponse.SC_NOT_FOUND);
	}
	
	public Object getRequestAttr(String attr) {
		HttpServletRequest request = getRequest();
		return (request != null) ? request.getAttribute(attr) : null;
	}

	public ServletContext getContext() {
		return context;
	}

	public HttpSession getSession() {
		return session;
	}

	public HttpSession getSession(boolean create) {
		return (session == null && create) ? (session = request.getSession())
				: session;
	}
	
	public Object getSessionAttr(String attr) {
		HttpSession ssn = getSession();
		return (ssn!=null)?ssn.getAttribute(attr):null;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	
	public void setRequestAttr(String key, Object value) {
		request.setAttribute(key, value);
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public Cookie getCookie(String name) {
		return cookies.get(name);
	}

	public void setCookie(String name, String value, int max_age,
			boolean all_sub_domain) {
		RequestUtils.setCookie(request, response, name, value, max_age,
				all_sub_domain);
	}

	public void deleteCookie(String name, boolean all_domain) {
		RequestUtils.deleteCookie(request, response, name, all_domain);
	}

	public String getHeader(String name) {
		return request.getHeader(name);
	}

	public void setHeader(String name, String value) {
		response.setHeader(name, value);
	}

	public void setHeader(String name, int value) {
		response.setIntHeader(name, value);
	}

	public void setHeader(String name, long value) {
		response.setDateHeader(name, value);
	}
	
	public void closeCache() {
		setHeader("Pragma", "No-cache");
		setHeader("Cache-Control", "no-cache");
		setHeader("Expires", 0L);
	}

	public String getServletPath() {
		return request.getServletPath();
	}

	public String getURI() {
		return request.getRequestURI();
	}

	public String getContextPath() {
		return request.getContextPath();
	}
	
	/**
	 * 返回Web应用的路径
	 * 
	 * @return
	 */
	public static String getWebroot() {
		return webroot;
	}
	
	/**
	 * 将HTTP请求参数映射到bean对象中
	 * 
	 * @param req
	 * @param beanClass
	 * @return
	 * @throws Exception
	 */
	public <T> T convertBean(Class<T> beanClass) {
		try {
			T bean = beanClass.newInstance();
			BeanUtils.populate(bean, request.getParameterMap());
			return bean;
		} catch (Exception e) {
			throw new ActionException(e.getMessage());
		}
	}
	
	public void populate(Object bean) throws IllegalAccessException, InvocationTargetException {
		BeanUtils.populate(bean, request.getParameterMap());
	}
	
	/**
	 * 去除contextPath的URI
	 * @param rc
	 * @return
	 */
	public String getURIAndExcludeContextPath() {
		if (getContextPath().equals("")) {
			return getURI();
		} else {
			String t = StringUtils.substringAfter(getURI(), getContextPath());
			if (!t.startsWith("/")) {
				t = "/" + t;
			}
			return t;
		}
	}	

	/**
	 * 3.0 以上版本的 Resin 无需对URL参数进行转码
	 * 
	 * @return
	 */
	private final static boolean _checkResinVersion() {
		try {
			Class<?> verClass = Class.forName("com.caucho.Version");
			String ver = (String) verClass.getDeclaredField("VERSION").get(
					verClass);
			String mainVer = ver.substring(0, ver.lastIndexOf('.'));
			/**
			 * float fVer = Float.parseFloat(mainVer);
			 * System.out.println("----------------> " + fVer);
			 */
			return Float.parseFloat(mainVer) > 3.0;
		} catch (Throwable t) {
		}
		return false;
	}

	private static boolean isMultipart(HttpServletRequest req) {
		return ((req.getContentType() != null) && (req.getContentType()
				.toLowerCase().startsWith("multipart")));
	}
	
	public final static String COOKIE_LOGIN = "fensyid";
	public final static int MAX_AGE = 86400 * 365;
	public final static byte[] E_KEY = new byte[] { '1', '2', '3', '4', '5', '6', '7', '8' };
	
	/**
	 * 保存登录信息
	 * 
	 * @param req
	 * @param res
	 * @param user
	 * @param save
	 */
	public void saveUserInCookie(IUser user, boolean save) {
		String new_value = _genLoginKey(user, getIp(), getHeader("user-agent"));
		int max_age = save ? MAX_AGE : -1;
		deleteCookie(COOKIE_LOGIN, true);
		setCookie(COOKIE_LOGIN, new_value, max_age, true);
	}

	public void deleteUserFromCookie() {
		deleteCookie(COOKIE_LOGIN, true);
	}
	

	/**
	 * 从cookie中读取保存的用户信息
	 * 
	 * @param req
	 * @return
	 */
	public IUser getUserFromCookie() {
		try {
			Cookie cookie = getCookie(COOKIE_LOGIN);
			if (cookie != null && StringUtils.isNotBlank(cookie.getValue())) {
				return getUserByUUID(cookie.getValue());
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 从cookie中读取保存的用户信息
	 * 
	 * @param req
	 * @return
	 */
	public IUser getUserByUUID(String uuid) {
		if (StringUtils.isBlank(uuid))
			return null;
		String ck = _decrypt(uuid);
		final String[] items = StringUtils.split(ck, '|');
		if (items.length == 5) {
			String ua = getHeader("user-agent");
			int ua_code = (ua == null) ? 0 : ua.hashCode();
			int old_ua_code = Integer.parseInt(items[3]);
			if (ua_code == old_ua_code) {
				return new IUser() {
					public boolean isBlocked() {
						return false;
					}

					public long getId() {
						return NumberUtils.toLong(items[0], -1L);
					}

					public String getPassword() {
						return items[1];
					}

					public byte getRole() {
						return IUser.ROLE_GENERAL;
					}
				};
			}
		}
		return null;
	}
	
	/**
	 * 生成用户登录标识字符串
	 * 
	 * @param user
	 * @param ip
	 * @param user_agent
	 * @return
	 */
	public static String _genLoginKey(IUser user, String ip, String user_agent) {
		StringBuilder sb = new StringBuilder();
		sb.append(user.getId());
		sb.append('|');
		sb.append(user.getPassword());
		sb.append('|');
		sb.append(ip);
		sb.append('|');
		sb.append((user_agent == null) ? 0 : user_agent.hashCode());
		sb.append('|');
		sb.append(System.currentTimeMillis());
		return _encrypt(sb.toString());
	}

	/**
	 * 加密
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String _encrypt(String value) {
		byte[] data = CryptUtils.encrypt(value.getBytes(), E_KEY);
		try {
			return URLEncoder.encode(new String(Base64.encodeBase64(data)),
					UTF_8);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * 解密
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String _decrypt(String value) {
		try {
			value = URLDecoder.decode(value, UTF_8);
			if (StringUtils.isBlank(value))
				return null;
			byte[] data = Base64.decodeBase64(value.getBytes());
			return new String(CryptUtils.decrypt(data, E_KEY));
		} catch (UnsupportedEncodingException excp) {
			return null;
		}
	}
	
	/**
	 * 自动解码
	 * 
	 */
	private static class RequestProxy extends HttpServletRequestWrapper {
		private String uri_encoding;

		RequestProxy(HttpServletRequest request, String encoding) {
			super(request);
			this.uri_encoding = encoding;
		}

		/**
		 * 重载getParameter
		 */
		public String getParameter(String paramName) {
			String value = super.getParameter(paramName);
			return _decodeParamValue(value);
		}

		/**
		 * 重载getParameterMap
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Map<String, Object> getParameterMap() {
			Map params = super.getParameterMap();
			HashMap<String, Object> new_params = new HashMap<String, Object>();
			Iterator<String> iter = params.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				Object oValue = params.get(key);
				if (oValue.getClass().isArray()) {
					String[] values = (String[]) params.get(key);
					String[] new_values = new String[values.length];
					for (int i = 0; i < values.length; i++)
						new_values[i] = _decodeParamValue(values[i]);

					new_params.put(key, new_values);
				} else {
					String value = (String) params.get(key);
					String new_value = _decodeParamValue(value);
					if (new_value != null)
						new_params.put(key, new_value);
				}
			}
			return new_params;
		}

		/**
		 * 重载getParameterValues
		 */
		public String[] getParameterValues(String arg0) {
			String[] values = super.getParameterValues(arg0);
			for (int i = 0; values != null && i < values.length; i++)
				values[i] = _decodeParamValue(values[i]);
			return values;
		}

		/**
		 * 参数转码
		 * 
		 * @param value
		 * @return
		 */
		private String _decodeParamValue(String value) {
			if (StringUtils.isBlank(value) || StringUtils.isBlank(uri_encoding)
					|| StringUtils.isNumeric(value))
				return value;
			try {
				return new String(value.getBytes("8859_1"), uri_encoding);
			} catch (Exception e) {
			}
			return value;
		}

	}
}