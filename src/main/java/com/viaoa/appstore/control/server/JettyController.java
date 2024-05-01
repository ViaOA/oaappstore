// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.KeyStore;
import java.security.Principal;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Endpoint;

import org.apache.jasper.servlet.JspServlet;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.spi.JettyHttpServerProvider;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.webapp.Configuration;

import com.viaoa.appstore.control.LogController;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.appstore.servlet.HelloServlet;
import com.viaoa.context.OAUserAccess;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAStr;
import com.viaoa.util.OAString;
import com.viaoa.web.filter.OAUserAccessFilter;
import com.viaoa.web.servlet.HealthCheckServlet;
import com.viaoa.web.servlet.ImageServlet;
import com.viaoa.web.servlet.JNLPServlet;
import com.viaoa.web.servlet.JsonServlet;
import com.viaoa.web.servlet.OARestServlet;
import com.viaoa.web.servlet.PdfServlet;
import com.viaoa.web.servlet.SecurityServlet;

/* sys props for logging
-Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog
-Dorg.eclipse.jetty.LEVEL=DEBUG
*/

/**
 * Embedded Jetty webserver that supports: http, https, webservices (jax-ws), servlets, JSP, file explorer, NCSARequestLog logging Servlets
 * <p>
 * for: REST/json, pdf, images, security, jnlp
 * <p>
 * For OpenAPI/Swagger<br>
 * see: ../src/main/webapp/swagger-ui/index.html
 * <p>
 * https://www.eclipse.org/jetty/documentation/9.4.x/index.html
 * <p>
 * Doc https://github.com/jetty-project
 * <p>
 * embedded jsp: https://www.eclipse.org/jetty/documentation/9.4.x/configuring-jsp.html
 * <p>
 * read: https://github.com/jetty-project/embedded-jetty-jsp
 * <p>
 * code: https://github.com/jetty-project/embedded-jetty-jsp/blob/master/src/main/java/org/eclipse/jetty/demo/Main.java
 * <p>
 * precompiling jsp https://www.eclipse.org/jetty/documentation/9.4.x/jetty-jspc-maven-plugin.html
 * <p>
 * 20180201 updated to support for Jetty 9.4.8.v20171121,
 *
 * @author vincevia
 */
public class JettyController {
	private static Logger LOG = Logger.getLogger(JettyController.class.getName());

	private Server server;
	private boolean bAlwaysUseHttps;
	private int portHttp, portHttps;
	private HashLoginService loginService;

	private OARestServlet servletRest;

	/**
	 * Create a new Jetty controller. To start Jetty, call init(..), and then start()
	 */
	public JettyController() {
	}

	/**
	 * Used to have all all pages use https, instead of http
	 */
	public void setAlwaysUseHttps(boolean b) {
		this.bAlwaysUseHttps = b;
	}

	public boolean getAlwaysUseHttps() {
		return this.bAlwaysUseHttps;
	}

	public void init(final int port, final int sslPort, OAUserAccessFilter filterUserAccess) throws Exception {
		if (server != null) {
			return;
		}
		LOG.config(String.format("JETTY PORTS: http=%d, https=%d", port, sslPort));
		LOG.fine("classpath: " + System.getProperty("java.class.path"));

		// 20180201 not needed, ok to use runtime jvm
		// Set JSP to use Standard JavaC always
		//was: System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

		this.portHttp = port;
		this.portHttps = sslPort;

		//======= thread pool======================================================
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(1);
		threadPool.setMaxThreads(700);
		threadPool.setDaemon(true);
		threadPool.setIdleTimeout(60 * 1000); // ms
		threadPool.setName("jetty_pool");

		server = new Server(threadPool);
		server.setStopAtShutdown(true);
		//was: server.setSendServerVersion(true);
		//was: server.setThreadPool(tp);
		// 20182001 http://www.eclipse.org/jetty/documentation/9.4.8.v20171121/embedding-jetty.html
		server.addBean(new ScheduledExecutorScheduler("jetty_scheduler", true));

		// 20180201 required
		// https://github.com/jetty-project/embedded-jetty-jsp/blob/master/src/main/java/org/eclipse/jetty/demo/Main.java
		// Add annotation scanning (for WebAppContexts)
		Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		// jettyHostName should only set to have url pin the the name. Wont allow using "localhost", etc 
		String jettyHostName = Resource.getValue(Resource.APP_JettyServer);
		if (OAString.isEmpty(jettyHostName)) {
			// jettyHostName = Resource.getValue(Resource.APP_HostName);
		}
		LOG.fine("Jetty server host name: " + jettyHostName);

		// see: http://www.eclipse.org/jetty/documentation/9.4.8.v20171121/embedding-jetty.html
		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSecureScheme("https");
		if (sslPort > 0) {
			httpConfig.setSecurePort(sslPort);
		}
		httpConfig.setOutputBufferSize(32768);
		httpConfig.setRequestHeaderSize(8192);
		httpConfig.setResponseHeaderSize(8192);
		httpConfig.setSendServerVersion(true);
		httpConfig.setSendDateHeader(false);

		ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
		httpConnector.setPort(port);
		httpConnector.setIdleTimeout(30 * 1000);
		if (OAString.isNotEmpty(jettyHostName)) {
		    // The network interface this connector binds to as an IP address or a hostname.  If null or 0.0.0.0, then bind to all interfaces.
			httpConnector.setHost(jettyHostName);
		}
		server.addConnector(httpConnector);

		// SSL Connector
		SslContextFactory sslContextFactory = new SslContextFactory();

		// keystore is selfsigned, generated by build.xml, copy it to the
		// src/oaqb/resource directory
		KeyStore keystore = KeyStore.getInstance("JKS");

		String s = Resource.class.getName();
		int pos = s.lastIndexOf('.');
		s = s.substring(0, pos);
		s = OAString.convert(s, ".", "/");

		String s2 = Resource.getValue(Resource.APP_JavaKeyStore, "oaapp.jks");
		s = "/" + s + "/" + s2;

		// String s = "/com/viaoa/imageeditor/resource/app.jks";
		LOG.fine("Using JKS keystore, location=" + s);
		InputStream is = JettyController.class.getResourceAsStream(s);
		keystore.load(is, "viaoaapp".toCharArray());

		sslContextFactory.setKeyStore(keystore);
		sslContextFactory.setKeyStorePassword("viaoaapp");

		if (sslPort > 0) {
			// SSL HTTP Configuration
			HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
			httpsConfig.addCustomizer(new SecureRequestCustomizer());

			SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
			ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, new HttpConnectionFactory(httpsConfig));
			httpsConnector.setPort(sslPort);
			httpsConnector.setIdleTimeout(500 * 1000);
			server.addConnector(httpsConnector);
			LOG.fine("created SSLSocketConnector on port=" + sslPort);
		}

		//======= web log file ========================================================
        NCSARequestLog ncsaRequestLog = new NCSARequestLog(OAStr.nonNull(Resource.getLogsDirectory(),".") + "/yyyy_mm_dd_jetty.log"); // must use "yyyy_mm_dd", which will be replaced with date format
		ncsaRequestLog.setRetainDays(360);
		ncsaRequestLog.setAppend(true);
		ncsaRequestLog.setExtended(true);
		TimeZone tz = TimeZone.getDefault();
		ncsaRequestLog.setLogTimeZone(tz.getID()); // ex: "GMT"
		ncsaRequestLog.setFilenameDateFormat("yyyyMMdd");

		// handler for logging
		RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog(ncsaRequestLog);

		//======= context handler collection ==========================================
		// this allows for multiple contexts and handlers
		ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
		server.setHandler(contextHandlerCollection);

		//======= Security ===========================================================
		// see: http://www.eclipse.org/jetty/documentation/9.4.8.v20171121/embedded-examples.html  half way down the page

		/* optional, this can be used to set up security
		ConstraintSecurityHandler securityHandler = createSecurityHandler("/*"); // ex:  "/servlet/test/*", ...
		server.setHandler(securityHandler);
		securityHandler.setHandler(contextHandlerCollection); // chain
		*/

		//======= Handlers ===========================================================

		String dirName = Resource.getValue(Resource.APP_JettyDirectory);
		dirName = OAFile.convertFileName(dirName);
		File file = new File(dirName);
		dirName = file.getAbsolutePath();

		String welcomePage = Resource.getValue(Resource.APP_WelcomePage);
		if (OAString.isEmpty(welcomePage)) {
			welcomePage = "index.html";
		}

		// Resource handler for all regular pages
        /*   not needed, will be handled by DefaultServlet
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setResourceBase(dirName);
		resourceHandler.setWelcomeFiles(new String[] { welcomePage });
		resourceHandler.setCacheControl("max-age=3600,public"); // one hour
		// resourceHandler.setCacheControl("private, max-age=0, no-cache, no-store, must-revalidate");

        ContextHandler resourceContextHandler = new ContextHandler("/");
        resourceContextHandler.setHandler(resourceHandler);
		*/
		
		// Servlet handler
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/"); // url prefix
		servletContextHandler.setResourceBase(dirName);

		// 20180201
		// see:  https://github.com/jetty-project/embedded-jetty-jsp/blob/master/src/main/java/org/eclipse/jetty/demo/Main.java
		// Set Classloader of Context to be same (needed for JSTL)
		// JSP requires a non-System classloader, this simply wraps the
		// embedded System classloader in a way that makes it suitable for JSP to use
		ClassLoader classLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
		servletContextHandler.setClassLoader(classLoader);
		// this bean will manually call JettyJasperInitializer on context startup
		servletContextHandler.addBean(new JspStarter(servletContextHandler));

		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
		// http://www.servlets.com/rfcs/rfc2616-sec14.html#sec14.9
		servletContextHandler.setInitParameter("cacheControl", "private, max-age=0, no-cache, no-store, must-revalidate");

		//was: servletContextHandler.setResourceBase(".");
		servletContextHandler.setWelcomeFiles(new String[] { welcomePage });
		SessionHandler sessionHandler = servletContextHandler.getSessionHandler();
		sessionHandler.setMaxInactiveInterval(30 * 60); // (30 minutes)

		SessionCache sessionCache = sessionHandler.getSessionCache();
		if (sessionCache == null) {
			sessionCache = new DefaultSessionCache(sessionHandler);
			SessionDataStore sds = new NullSessionDataStore();
			sessionCache.setSessionDataStore(sds);
			sessionHandler.setSessionCache(sessionCache);
		}

		sessionCache.addLifeCycleListener(new LifeCycle.Listener() {
			@Override
			public void lifeCycleStarting(LifeCycle event) {
			}

			@Override
			public void lifeCycleStarted(LifeCycle event) {
				// todo
			}

			@Override
			public void lifeCycleFailure(LifeCycle event, Throwable cause) {
			}

			@Override
			public void lifeCycleStopping(LifeCycle event) {
			}

			@Override
			public void lifeCycleStopped(LifeCycle event) {
				// todo
			}
		});

		//======= custom servlets, see each for specific usage ======================
		// set up default servlet
		// see init params: https://www.eclipse.org/jetty/documentation/9.3.x/advanced-extras.html
		// this is so that jsp and web pages can be in same directories
		ServletHolder defaultServlet = servletContextHandler.addServlet(DefaultServlet.class, "/");
		defaultServlet.setInitParameter("welcomeServlets", "true");
		defaultServlet.setInitParameter("redirectWelcome", "true");
		defaultServlet.setInitParameter("cacheControl", "private, max-age=0, no-cache, no-store, must-revalidate");
		//was: defaultServlet.setInitParameter("cacheControl", "max-age=3600,public");
		defaultServlet.setInitParameter("resourceBase", dirName);
        defaultServlet.setInitParameter("resourceBase", dirName);
        // commented out, only works if webcomeServlets=false
        // defaultServlet.setInitParameter("dirAllowed", "true");
        
		// Hello Servlet
		HelloServlet servletHello = new HelloServlet();
		servletContextHandler.addServlet(new ServletHolder(servletHello), "/servlet/hello");

		// Image Servlet
		final String packageName = Resource.getValue(Resource.APP_ClassPath) + ".model.oa";
		ImageServlet servletImage = new ImageServlet(packageName, null, null);
		servletContextHandler.addServlet(new ServletHolder(servletImage), "/servlet/img");

		// JSP support
		ServletHolder jsp = servletContextHandler.addServlet(JspServlet.class, "*.jsp");
		String cp = servletContextHandler.getClassPath(); // null/empty
		cp = System.getProperty("java.class.path");

		jsp.setInitOrder(0);
		jsp.setInitParameter("logVerbosityLevel", "DEBUG");
		jsp.setInitParameter("fork", "false");
		jsp.setInitParameter("xpoweredBy", "false");
		jsp.setInitParameter("compilerTargetVM", "1.8");
		jsp.setInitParameter("compilerSourceVM", "1.8");
		jsp.setInitParameter("classpath", cp);
		OAFile.mkdirsForFile("./work/xx");
		jsp.setInitParameter("scratchdir", "./work");
		jsp.setInitParameter("keepgenerated", "true");
		jsp.setInitParameter("development", "true");
		jsp.setInitParameter("saveByteCode", "true");
		// see: http://wiki.eclipse.org/Jetty/Howto/Configure_JSP

		// Filters

		// OAUserAccessFilter
		if (filterUserAccess == null) {
			// wont allow filter servlets to work, since security will not be set up
			filterUserAccess = new OAUserAccessFilter() {
				@Override
				protected OAObject getWebUser(String userId, String password) {
					return null;
				}

				@Override
				protected OAObject getContextUser(OAObject webUser) {
					return null;
				}

				@Override
				protected OAUserAccess getContextUserAccess(OAObject webUser, OAObject contextUser) {
					return null;
				}
			};
		}

		// see: https://stackoverflow.com/questions/14390577/how-to-add-servlet-filter-with-embedded-jetty
		final String name = "OAUserAccessFilter";
		FilterHolder filterHolder = new FilterHolder(filterUserAccess);
		filterHolder.setName(name);

		FilterMapping filterMapping = new FilterMapping();
		filterMapping.setPathSpecs(new String[] { "/servlet/oarest/*", "/servlet/json/*" });
		filterMapping.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
		filterMapping.setFilterName(name);

		servletContextHandler.getServletHandler().addFilter(filterHolder, filterMapping);

		// HealthCheck Servlet
		HealthCheckServlet servletHealthCheck = new HealthCheckServlet();
		servletContextHandler.addServlet(new ServletHolder(servletHealthCheck), "/servlet/healthcheck");

		// OARest Servlet
		this.servletRest = new OARestServlet(packageName);
		servletContextHandler.addServlet(new ServletHolder(servletRest), "/servlet/oarest/*");

		// Json Servlet
		JsonServlet servletJson = new JsonServlet(packageName);
		servletContextHandler.addServlet(new ServletHolder(servletJson), "/servlet/json");

		// Pdf Servlet
		PdfServlet servletPdf = new PdfServlet(packageName, null, null);
		servletContextHandler.addServlet(new ServletHolder(servletPdf), "/servlet/pdf");

		// Security Servlet (test)
		SecurityServlet servletSecure = new SecurityServlet("user", "password");
		servletContextHandler.addServlet(new ServletHolder(servletSecure), "/servlet/security");

		// JNLP Servlet, to set correct values in jnlp, and add client args[]
		JNLPServlet js = new JNLPServlet();
		s = Resource.getValue(Resource.APP_Server, "");
		js.addNameValue(Resource.APP_Server, s);
		s = Resource.getValue(Resource.APP_ServerPort, "");
		js.addNameValue(Resource.APP_ServerPort, s);
		s = Resource.getValue(Resource.APP_ClientApplicationName, "");
		js.addNameValue(Resource.APP_ClientApplicationName, s);
		s = Resource.getValue(Resource.APP_ServerDisplayName, "");
		js.addNameValue(Resource.APP_ServerDisplayName, s);
		s = Resource.getValue(Resource.APP_JettyServer, "");
		js.addNameValue(Resource.APP_JettyServer, s);
		s = Resource.getValue(Resource.APP_JettyPort, "");
		js.addNameValue(Resource.APP_JettyPort, s);
		s = Resource.getValue(Resource.APP_ClientApplicationName, "");
		js.addNameValue("jnlp.title", s);
		servletContextHandler.addServlet(new ServletHolder(js), "*.jnlp");

		// see: http://wiki.eclipse.org/Jetty/Howto/Configure_JSP

		//======= file access for specific directories ==============================
		// find base directory
		/*
		String dirLib = dirName;
		if (dirLib == null) dirLib = "";
		pos = dirLib.lastIndexOf('\\');
		if (pos < 0) pos = dirLib.lastIndexOf('/');
		if (pos > 1) {
		    dirLib = dirLib.substring(0, pos + 1);
		}
		else dirLib = "./";
		*/
		String dirLib = Resource.getValue(Resource.APP_JettyLibDirectory);
		dirLib = OAFile.convertFileName(dirLib);

		// "/lib" directory
		ContextHandler libContextHandler = contextHandlerCollection.addContext("/lib", dirLib);
		// ContextHandler libContextHandler = contextHandlerCollection.addContext("/lib", dirLib + "lib");
		ResourceHandler libResourceHandler = new ResourceHandler();
		//qqq comment out
		libResourceHandler.setDirectoriesListed(true);
		libContextHandler.setHandler(libResourceHandler);

		// "/jar directory
		// ContextHandler jarContextHandler = contextHandlerCollection.addContext("/jar", dirLib + "jar");
		ContextHandler jarContextHandler = contextHandlerCollection.addContext("/jar", dirLib);
		ResourceHandler jarResourceHandler = new ResourceHandler();
		// jarResourceHandler.setDirectoriesListed(true);
		jarContextHandler.setHandler(jarResourceHandler);

		// "/src" directory
		/*
		ContextHandler srcContextHandler = contextHandlerCollection.addContext("/src", dirLib + "src");
		ResourceHandler srcResourceHandler = new ResourceHandler();
		srcResourceHandler.setDirectoriesListed(true);
		srcContextHandler.setHandler(srcResourceHandler);
		*/

		/* replaced with jnlpservlet // "/jnlp" directory ContextHandler jnlpContextHandler =
		 * contextHandlerCollection.addContext("/jnlp", dirLib + "jnlp"); ResourceHandler
		 * jnlpResourceHandler = new ResourceHandler(); jnlpResourceHandler.setDirectoriesListed(true);
		 * jnlpContextHandler.setHandler(jnlpResourceHandler); */

		//======= special purpose handlers ========================================
		// "hook" to allow redirect, by overwriting getRedirectedPage(..)
		Handler redirectHandler = new AbstractHandler() {
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				String path = baseRequest.getPathInfo(); // ex: "/test.jsp"     (not args)
				if (path == null || path.length() == 0) {
					return;
				}

				HttpURI hu = baseRequest.getHttpURI();
				String fullPath = hu.getPath();//was: CompletePath(); // ex: "/test.jsp?a=1&b=2"  (with args)

				String s = path.substring(1);
				String s2 = getRedirectPage(s);
				if (!s.equals(s2)) {
					response.sendRedirect(s2);
					baseRequest.setHandled(true);
				}
			}
		};

		// Custom handler to have all http converted to https
		Handler httpsRedirectHandler = new AbstractHandler() {
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				if (!bAlwaysUseHttps) {
					return;
				}
				String scheme = baseRequest.getScheme();
				if (scheme != null && "https".equals(scheme.toLowerCase())) {
					return;
				}

				String path = baseRequest.getPathInfo(); // ex: /test.jsp   (does not include args/query)
				if (OAString.isEmpty(path)) {
					return;
				}

				HttpURI hu = baseRequest.getHttpURI();
				String fullPath = hu.getPath(); // ex: /test.jsp?a=1&b=2

				String sPort = (JettyController.this.portHttps == 443) ? "" : (":" + JettyController.this.portHttps);
				String s = "https://" + request.getServerName() + sPort + fullPath;
				response.sendRedirect(s);
				baseRequest.setHandled(true);
			}
		};

		// 20180201
		// Custom subdomain handler to allow for "sub.acme.com" to be redirected to "www.acem.com/login.jsp?sub"
		// Note: not used, not added to HandlerList
		Handler subdomainHandler = new AbstractHandler() {
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				String path = baseRequest.getPathInfo(); // ex: /test.jsp (not args)

				String serverName = request.getServerName();
				if (serverName != null && OAString.dcount(serverName, ".") == 3 && (OAString.isEmpty(path) || path.equals("/"))) {
					String subdomain = OAString.field(serverName, ".", 1);
					if (OAString.isNotEmpty(subdomain) && !subdomain.equalsIgnoreCase("www")) {
						String s;
						if (sslPort > 0) {
							String sp = sslPort == 443 ? "" : ":" + sslPort;
							s = "https://www." + OAString.field(serverName, ".", 2, 2) + sp + "/login.jsp?" + subdomain;
						} else {
							String sp = port == 80 ? "" : ":" + port;
							s = "http://www." + OAString.field(serverName, ".", 2, 2) + sp + "/login.jsp?" + subdomain;
						}
						response.sendRedirect(s);
						baseRequest.setHandled(true);
						return;
					}
				}

				String scheme = baseRequest.getScheme();
				if (sslPort > 0 && "http".equals(scheme) && path != null) {
					if (path.indexOf(".jsp") > 0) {
						if (!path.startsWith("/oa")) {
							HttpURI hu = baseRequest.getHttpURI();
							String fullPath = hu.getPath(); // ex: /test.jsp?a=1&b=2

							String sz = sslPort == 443 ? "" : ":" + sslPort;
							String sx = "https://" + request.getServerName() + sz + fullPath;
							response.sendRedirect(sx);
							baseRequest.setHandled(true);
							return;
						}
					}
				}
			}
		};

		//======= create ordered handler list for "/" context ===========================
		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] { 
		        requestLogHandler, httpsRedirectHandler, 
		        redirectHandler, servletContextHandler,
		        // resourceContextHandler, 
				new DefaultHandler() });
		contextHandlerCollection.addHandler(handlerList);

		//======= web services ======================================================
		// the jax-ws will be adding another handler for it's webservices
		/**
		 * Since we will use Jetty to handle connections for jax-ws, to support webservices, must use ContextHandlerCollection (not
		 * HandlerList) see: http://docs.codehaus.org/display/JETTY/J2se6HttpServerSPI see:
		 * http://javasourcecode.org/html/open-source/jetty/jetty-7.4.4.v20110707 org/mortbay/jetty/j2sehttpspi/JettyHttpServer.java.html
		 * method:findContextHandlerCollection(Handler[] handlers
		 */

		/* Jax-ws uses a system property to get the class that it will call to get an HttpServer or
		 * HttpsServer. By default it will use a Sun http server for http, and throw an unsupportted
		 * exception for https. For this we are using Jetty, and will need to use a Jetty provider
		 * "wrapper" to do this. Note: the jettyHttpServerProvider also throws an unsupportted exception
		 * for https. But this is only called by webservice Endpoint.publish(url, ws) if url has https
		 * in it. The work around is to use "http" in the url, but have the client application use
		 * "https" for the url. */
		LOG.fine("setting up JettyHttpServerProvider, so that jax-ws webservices will use Jetty for connections/etc");

		// this is the Jetty implementation of Java HTTP Server SPI "HttpServerProvider"
		s = JettyHttpServerProvider.class.getName();
		System.setProperty("com.sun.net.httpserver.HttpServerProvider", s);

		// see also: com.sun.net.httpserver.HttpsServer
		// this will set a static reference to the Jetty server.
		// JettyHttpServerProvider.setServer(server);

		// Example webservice
		/* wsApp = new MyQBWebConnectorSvcSoapImpl();
		 *
		 * NOTE: even though the client (ex: QBWC) is going to uses https + sslPort, this needs to be
		 * set up with http + clearPort. If not, then the webservice publish will call
		 * JettyHttpServerProvider.createHttpsServer(..) (see above), which will throw an unsupported
		 * exception. Setting it this way will allow the JettyHttpServerProvider to create a server,
		 * based on the jetty Server set up in this code. When the webservice is called using the https
		 * url, the jetty server will use https and support the webservice.
		 *
		 * String urlWebservice = "http://www.viaoaapp.com/ws/quickbooks";
		 *
		 * now, make the webserivce available This will use the system.property set above to create a
		 * HttpServerProvider instance. The Jetty HttpServer will then create a context and handler for
		 * the webservice
		 *
		 * Endpoint.publish(urlWebservice, wsApp);
		 *
		 * this next line would not work, since Endpoint.publish(..) would need an https server, which
		 * then throws an unsupported exception since it can not create an https server. urlWebservice =
		 * "https://www.viaoaapp.com:8443/ws/quickbooks"; */

		// webService example
		/*
		if (portWebservice > 0) {
		    String urlWS = "http://" + hostName + ":" + portWebservice + "/ws/hello";
		    Hello hello = new Hello();
		    addWebservice(urlWS, hello);
		    LOG.fine("added sample webservice " + urlWS);
		}
		*/
	}

	/**
	 * Add a webservice that will be published and controlled by jax-ws + Jetty. example: String urlWebserviceQuickbook =
	 * "https://www.viaoaapp.com:8443/ws/quickbooks"; jc.addWebservice(urlWebserviceQuickbook, wsc.getWebService()); WSDL:
	 * https://www.viaoaapp.com:8443/ws/quickbooks?WSDL
	 *
	 * @param urlWebService url to register the webservice under.
	 * @param webservice
	 */
	public void addWebservice(String urlWebService, Object webservice) {
		LOG.fine("urlWebService=" + urlWebService);
		if (urlWebService == null) {
			return;
		}
		int pos = urlWebService.toLowerCase().indexOf("https:");
		if (pos >= 0) {
			// see notes in this class about using jax=ws for registerig webservices with https.
			urlWebService = urlWebService.substring(0, pos + 4) + urlWebService.substring(pos + 5);
		}
		Endpoint.publish(urlWebService, webservice);
	}

	public void stop() {
		if (server != null) {
			try {
				server.stop();
				server = null;
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Error while stopping Jetty", e);
			}
		}
	}

	public void start() throws Exception {
		server.start();
	}

	public void join() throws Exception {
		server.join();
	}

	/**
	 * Called for each page that is accessed, to be able to redirect.
	 */
	public String getRedirectPage(String currentUrl) {
		return currentUrl;
	}

	/**
	 * Allows for setting security on a path
	 *
	 * @param path example: "/*"
	 * @return
	 */
	public ConstraintSecurityHandler createSecurityHandler(String... paths) {
		if (paths == null || paths.length == 0) {
			paths = new String[] { "/*" };
		}
		// handler
		ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
		// securityHandler.setSessionRenewedOnAuthentication(false);
		securityHandler.setAuthenticator(new BasicAuthenticator());
		securityHandler.setIdentityService(null);
		securityHandler.setRealmName("oaAppStore");

		// this will allow it to
		Constraint constraint = new Constraint();
		constraint.setName("oaAppStore");
		constraint.setRoles(new String[] { "user" });
		constraint.setAuthenticate(true);

		for (String path : paths) {
			ConstraintMapping constraintMapping = new ConstraintMapping();
			constraintMapping.setConstraint(constraint);
			constraintMapping.setPathSpec(path);
			securityHandler.addConstraintMapping(constraintMapping);
		}

		securityHandler.setLoginService(getLoginService());
		return securityHandler;
	}

	protected LoginService getLoginService() {
		if (loginService != null) {
			return loginService;
		}

		loginService = new HashLoginService() {
			@Override
			public UserIdentity login(String userId, Object credentials, ServletRequest request) {
				String password = null;
				Credential credential;

				if (credentials instanceof Credential) {
					credential = (Credential) credentials;
				} else {
					password = credentials.toString();
					credential = Credential.getCredential(password);
				}

				if (!isValidUser(userId, password)) {
					return null;
				}

				Principal userPrincipal = new UserPrincipal(userId, credential);
				Subject subject = new Subject();
				subject.getPrincipals().add(userPrincipal);
				subject.getPrivateCredentials().add(credential);
				String[] roles = new String[] { "user" };
				for (String role : roles) {
					subject.getPrincipals().add(new RolePrincipal(role));
				}
				subject.setReadOnly();
				UserIdentity ui = _identityService.newUserIdentity(subject, userPrincipal, roles);

				return ui;
			}
		};
		return loginService;
	}

	// overwrite this if security is being used
	public boolean isValidUser(String userId, String password) {
		return false;
	}

	/**
	 * 20180201 https://github.com/jetty-project/embedded-jetty-jsp/blob/master/src/main/java/org/eclipse/jetty/demo/Main.java JspStarter
	 * for embedded ServletContextHandlers This is added as a bean that is a jetty LifeCycle on the ServletContextHandler. This bean's
	 * doStart method will be called as the ServletContextHandler starts, and will call the ServletContainerInitializer for the jsp engine.
	 * (required by tomcat lib)
	 */
	public static class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller {
		JettyJasperInitializer jettyInitializer;
		ServletContextHandler contextHandler;

		public JspStarter(ServletContextHandler contextHandlerHandler) {
			this.jettyInitializer = new JettyJasperInitializer();
			this.contextHandler = contextHandlerHandler;
			StandardJarScanner scanner = new StandardJarScanner();
			final JarScanFilter filterOrig = scanner.getJarScanFilter();
			scanner.setJarScanFilter(new JarScanFilter() {
				@Override
				public boolean check(JarScanType jarScanType, String jarName) {
					if (jarName != null && jarName.toLowerCase().contains("derby")) { // skip derby jars, since the jar manifest includes a classpath to other derby jars
						return false;
					}
					// Note: might want to return false as the default, so that it does not scan jar files.
					// return filterOrig.check(jarScanType, jarName);
					return false;
				}
			});
			this.contextHandler.setAttribute("org.apache.tomcat.JarScanner", scanner);
		}

		@Override
		protected void doStart() throws Exception {
			ClassLoader old = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(contextHandler.getClassLoader());
			try {
				jettyInitializer.onStartup(null, contextHandler.getServletContext());
				super.doStart();
			} finally {
				Thread.currentThread().setContextClassLoader(old);
			}
		}
	}

	public OARestServlet getRestServlet() {
		return servletRest;
	}

	/**
	 * Testing: // to access "webcontent" directory http://localhost:8081/test.html http://localhost:8081/test.jsp
	 * http://localhost:8081/servlet/hello?name=test // to get a directory (under "webcontent") http://localhost:8081/data =>
	 * webcontent/data // to access lib directory http://localhost:8081/lib // ex for webservice http://localhost:8081/ws/quickbooks?wsdl
	 * https://www.viaoaapp.com:8443/ws/quickbooks?wsdl
	 */
	public static void main(String[] args) throws Exception {
		new LogController(false);
		/* OASyncClient client = new OASyncClient("localhost", 1099); client.start();
		 *
		 * ServerRoot rootServer = RemoteDelegate.getRemoteApp().getServerRoot(); int cid =
		 * OASyncDelegate.getSyncClient().getConnectionId(); ClientRoot rootClient =
		 * RemoteDelegate.getRemoteApp().getClientRoot(cid); ModelDelegate.initialize(rootServer,
		 * rootClient); */

		JettyController jc = new JettyController();
		jc.init(8082, 8444, null);
		jc.start();
		System.out.println("==============================================");
		System.out.println("JETTY webserver started on ports " + jc.portHttp + " and " + jc.portHttps + "");
		System.out.println("==============================================");
		jc.join();
	}
}
