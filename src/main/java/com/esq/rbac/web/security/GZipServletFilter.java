package com.esq.rbac.web.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * @author fazia
 * Reference: http://tutorials.jenkov.com/java-servlets/gzip-servlet-filter.html
 * The GZipServletFilter is what intercepts the requests, checks if the client accepts compression or not, and enables compression if it does. 
 * It does so by wrapping the HttpServletResponse in a GZipServletResponseWrapper before passing it down the filter chain.
 */
public class GZipServletFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(GZipServletFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (acceptsGZipEncoding(httpRequest)) {
			log.trace("Inside accept gzip for {}", httpRequest.getRequestURI());
			httpResponse.addHeader("Content-Encoding", "gzip");
			GZipServletResponseWrapper gzipResponse = new GZipServletResponseWrapper(httpResponse);
			chain.doFilter(request, gzipResponse);
			gzipResponse.close();
		} else {
			log.trace("does not accept gzip for {}", httpRequest.getRequestURI());
			chain.doFilter(request, response);
		}
	}

	private boolean acceptsGZipEncoding(HttpServletRequest httpRequest) {
		String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
		return acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1;
	}
}
