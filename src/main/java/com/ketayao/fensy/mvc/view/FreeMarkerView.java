/**
 * <pre>
 * Copyright:		Copyright(C) 2011-2012, ketayao.com
 * Date:			2013年8月12日
 * Author:			<a href="mailto:ketayao@gmail.com">ketayao</a>
 * Description:		
 *
 * </pre>
 **/
package com.ketayao.fensy.mvc.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ketayao.fensy.exception.FensyException;
import com.ketayao.fensy.mvc.RequestContext;

import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.AllHttpScopesHashModel;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/** 
 * 	
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2013年8月12日 上午10:42:17 
 */
public class FreeMarkerView implements View {
	private static final Logger logger = LoggerFactory.getLogger(FreeMarkerView.class);
 	
	private transient static final Configuration config = new Configuration();
	
	private boolean init = false;
	
	private String encoding;

	private TaglibFactory taglibFactory;

	private ServletContextHashModel servletContextHashModel;

    private static final String TEMPLATE_PATH = "TemplatePath";
    
	/**
	 * Set the encoding of the FreeMarker template file. Default is determined
	 * by the FreeMarker Configuration: "ISO-8859-1" if not specified otherwise.
	 * <p>Specify the encoding in the FreeMarker Configuration rather than per
	 * template if all your templates share a common encoding.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Return the encoding for the FreeMarker template.
	 */
	protected String getEncoding() {
		return this.encoding;
	}

	/**
	 * freemarker can not load freemarker.properies automatically
	 */
	public static Configuration getConfiguration() {
		return config;
	}

    protected void init(ServletContext servletContext) throws IOException, ServletException {
    	if (init == true) {
    		return;
    	}
        Properties p = new Properties();
        
		try {
			p.load(FreeMarkerView.class.getClassLoader().getResourceAsStream(
					"freemarker.properties"));
		} catch (IOException e) {
			logger.warn("not found freemarker.properties......");
			init = true;
			return;
		}
		config.setServletContextForTemplateLoading(servletContext, null); 
				//p.getProperty(TEMPLATE_PATH));	// "WEB-INF/templates"
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setObjectWrapper(getObjectWrapper());
		
		try {
			config.setSettings(p);
		} catch (TemplateException e) {
			logger.error("freemarker init error");
			throw new ServletException(e);
		}
		
		setEncoding(config.getDefaultEncoding());
		
    	this.taglibFactory = new TaglibFactory(servletContext);
		GenericServlet servlet = new GenericServletAdapter();
		try {
			servlet.init(new DelegatingServletConfig(servletContext));
		}
		catch (ServletException ex) {
			throw new ServletException("Initialization of GenericServlet adapter failed", ex);
		}
		this.servletContextHashModel = new ServletContextHashModel(servlet, getObjectWrapper());
		
		init = true;
    }
    
	/**
	 * Return the configured FreeMarker {@link ObjectWrapper}, or the
	 * {@link ObjectWrapper#DEFAULT_WRAPPER default wrapper} if none specified.
	 * @see freemarker.template.Configuration#getObjectWrapper()
	 */
	protected ObjectWrapper getObjectWrapper() {
		ObjectWrapper ow = getConfiguration().getObjectWrapper();
		return (ow != null ? ow : ObjectWrapper.DEFAULT_WRAPPER);
	}
    
	protected SimpleHash buildTemplateModel(Map<String, Object> model, RequestContext rc) {
		AllHttpScopesHashModel fmModel = new AllHttpScopesHashModel(getObjectWrapper(), rc.context(), rc.request());
		fmModel.put(FreemarkerServlet.KEY_JSP_TAGLIBS, this.taglibFactory);
		fmModel.put(FreemarkerServlet.KEY_APPLICATION, this.servletContextHashModel);
		fmModel.put(FreemarkerServlet.KEY_SESSION, buildSessionModel(rc.request(), rc.response()));
		fmModel.put(FreemarkerServlet.KEY_REQUEST, new HttpRequestHashModel(rc.request(), rc.response(), getObjectWrapper()));
		fmModel.put(FreemarkerServlet.KEY_REQUEST_PARAMETERS, new HttpRequestParametersHashModel(rc.request()));
		fmModel.putAll(model);
		return fmModel;
	}
	
	/**
	 * Build a FreeMarker {@link HttpSessionHashModel} for the given request,
	 * detecting whether a session already exists and reacting accordingly.
	 * @param request current HTTP request
	 * @param response current servlet response
	 * @return the FreeMarker HttpSessionHashModel
	 */
	private HttpSessionHashModel buildSessionModel(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return new HttpSessionHashModel(session, getObjectWrapper());
		}
		else {
			return new HttpSessionHashModel(null, request, response, getObjectWrapper());
		}
	}
	
	/**
	 * Retrieve the FreeMarker template specified by the given name,
	 * using the encoding specified by the "encoding" bean property.
	 * <p>Can be called by subclasses to retrieve a specific template,
	 * for example to render multiple templates into a single view.
	 * @param name the file name of the desired template
	 * @param locale the current locale
	 * @return the FreeMarker template
	 * @throws IOException if the template file could not be retrieved
	 */
	protected Template getTemplate(String name, Locale locale) throws IOException {
		return (getEncoding() != null ?
				getConfiguration().getTemplate(name, locale, getEncoding()) :
				getConfiguration().getTemplate(name, locale));
	}
	
	/**   
	 * @param rc
	 * @param templatePath
	 * @throws IOException
	 * @throws ServletException  
	 * @see com.ketayao.fensy.mvc.view.View#render(com.ketayao.fensy.mvc.RequestContext, java.lang.String)  
	 */
	@Override
	public void render(RequestContext rc, String templatePath)
			throws IOException, ServletException {
		
		init(rc.context());
		
		rc.response().setContentType("text/html; charset=" + getEncoding());
		
		// Expose all standard FreeMarker hash models.
		Map<String, Object> model = new HashMap<String, Object>();
		SimpleHash fmModel = buildTemplateModel(model, rc);

		// Grab the locale-specific version of the template.
		Locale locale = rc.locale();

		PrintWriter writer = null;
        try {
			Template template = getTemplate(templatePath, locale);
			writer = rc.response().getWriter();
			template.process(fmModel, writer);		// Merge the data-model and the template
		} catch (Exception e) {
			e.printStackTrace();
			throw new FensyException(e);
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}

	/**   
	 * @return  
	 * @see com.ketayao.fensy.mvc.view.View#getExt()  
	 */
	@Override
	public String getExt() {
		return ".ftl";
	}

	/**
	 * Simple adapter class that extends {@link GenericServlet}.
	 * Needed for JSP access in FreeMarker.
	 */
	@SuppressWarnings("serial")
	private static class GenericServletAdapter extends GenericServlet {

		@Override
		public void service(ServletRequest servletRequest, ServletResponse servletResponse) {
			// no-op
		}
	}
	
	/**
	 * Internal implementation of the {@link ServletConfig} interface,
	 * to be passed to the servlet adapter.
	 */
	private class DelegatingServletConfig implements ServletConfig {
		private ServletContext servletContext;
		
		public DelegatingServletConfig(ServletContext servletContext) {
			this.servletContext = servletContext;
		}

		public String getServletName() {
			return servletContext.getServletContextName();
		}

		public ServletContext getServletContext() {
			return servletContext;
		}

		public String getInitParameter(String paramName) {
			return null;
		}

		public Enumeration<String> getInitParameterNames() {
			return Collections.enumeration(new HashSet<String>());
		}
	}
}
