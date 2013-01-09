package module.fileManagement.presentationTier.webdav;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.servlet.DefaultMiltonConfigurator;
import io.milton.servlet.FilterConfigWrapper;
import io.milton.servlet.MiltonConfigurator;
import io.milton.servlet.MiltonServlet;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonFilter extends io.milton.servlet.MiltonFilter {

    private static final Logger log = LoggerFactory.getLogger(MiltonFilter.class);
    private FilterConfigWrapper config;
    private ServletContext servletContext;
    protected HttpManager httpManager;
    protected MiltonConfigurator configurator;
    /**
     * Resources with this as the first part of their path will not be served from milton. Instead, this filter will allow filter
     * processing to continue so they will be served by JSP or a servlet
     */
    private String[] excludeMiltonPaths;

    @Override
    public void init(FilterConfig config) throws ServletException {
	try {
	    this.config = new FilterConfigWrapper(config);
	    this.servletContext = config.getServletContext();

	    String configuratorClassName = config.getInitParameter("milton.configurator");
	    if (configuratorClassName != null) {
		configurator = DefaultMiltonConfigurator.instantiate(configuratorClassName);
	    } else {
		configurator = new DefaultMiltonConfigurator();
	    }
	    log.info("Using configurator: " + configurator.getClass());

	    String sExcludePaths = config.getInitParameter("milton.exclude.paths");
	    log.info("init: exclude paths: " + sExcludePaths);
	    if (sExcludePaths != null) {
		excludeMiltonPaths = sExcludePaths.split(",");
	    }

	    httpManager = configurator.configure(this.config);

	} catch (ServletException ex) {
	    log.error("Exception starting milton servlet", ex);
	    throw ex;
	} catch (Throwable ex) {
	    log.error("Exception starting milton servlet", ex);
	    throw new RuntimeException(ex);
	}
    }

    @Override
    public void destroy() {
	log.debug("destroy");
	if (configurator == null) {
	    return;
	}
	configurator.shutdown();
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse resp, javax.servlet.FilterChain fc)
	    throws IOException, ServletException {
	if (req instanceof HttpServletRequest) {
	    HttpServletRequest hsr = (HttpServletRequest) req;
	    String url = hsr.getRequestURI();
	    // Allow certain paths to be excluded from milton, these might be other servlets, for example
	    if (excludeMiltonPaths != null) {
		for (String s : excludeMiltonPaths) {
		    if (url.startsWith(s)) {
			fc.doFilter(req, resp);
			return;
		    }
		}
	    }
	    doMiltonProcessing((HttpServletRequest) req, (HttpServletResponse) resp);
	} else {
	    fc.doFilter(req, resp);
	}
    }

    private void doMiltonProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	try {
	    MiltonServlet.setThreadlocals(req, resp);
	    Request request = new io.milton.servlet.ServletRequest(req, servletContext);
	    Response response = new io.milton.servlet.ServletResponse(resp);
	    // final Auth authorization = request.getAuthorization();
	    // if (authorization != null) {
	    // final String username = authorization.getUser();
	    // final User user = User.findByUsername(username);
	    // if (user != null) {
	    // log.info("Authenticate user : " + username);
	    // Authenticate.authenticate(user);
	    // } else {
	    // log.warn("User not found : " + username);
	    // }
	    // } else {
	    // log.warn("request not authorized : " + request.getAbsolutePath());
	    // }
	    httpManager.process(request, response);
	} finally {
	    MiltonServlet.clearThreadlocals();
	    resp.getOutputStream().flush();
	    resp.flushBuffer();
	    // pt.ist.fenixWebFramework.security.UserView.setUser(null);
	}
    }

}
