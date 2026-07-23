/*
 *	File Name:	WebMDCServletFilter.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0	create
 *
 **/

package com.irt.custom;

import com.irt.dpr.util.Loggers;
import com.irt.json.Jsoner;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

@WebFilter( filterName = "WebMDCServletFilter", urlPatterns = { "/*" } )
public class WebMDCServletFilter implements Filter {

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {

		insertIntoMDC(request);
		try {
			chain.doFilter(request, response);
		} finally {
			clearMDC();
		}
	}

	private boolean clientLog( HttpServletRequest req ) {
		java.util.Map<String, String[]> pmap = req.getParameterMap();
		java.util.Set<String> keys = null;
		if( pmap != null ) {
			keys = pmap.keySet();
		}

		Loggers.client_sync.debug(Jsoner.getNewInstance().toJson(pmap));

		return true;
	}

	void insertIntoMDC( ServletRequest request ) {

		MDC.put(WebMDCConstants.REQUEST_REMOTE_HOST_MDC_KEY, request.getRemoteHost());

		if( request instanceof HttpServletRequest ) {
			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
			// MDC.put(WebMDCConstants.REQUEST_REQUEST_URI, httpServletRequest.getRequestURI());
			StringBuffer requestURL = httpServletRequest.getRequestURL();
			if( requestURL != null ) {
				MDC.put(WebMDCConstants.REQUEST_REQUEST_URL, requestURL.toString());
			}
			MDC.put(WebMDCConstants.REQUEST_METHOD, httpServletRequest.getMethod());
			//MDC.put(WebMDCConstants.REQUEST_QUERY_STRING, httpServletRequest.getQueryString());
			//MDC.put(WebMDCConstants.REQUEST_X_FORWARDED_FOR, httpServletRequest.getHeader("X-Forwarded-For"));
			if( httpServletRequest.getQueryString() != null ) {
				MDC.put(WebMDCConstants.REQUEST_QUERY_STRING, httpServletRequest.getQueryString());
			}

			if( httpServletRequest.getHeader("X-Forwarded-For") != null ) {
				MDC.put(WebMDCConstants.REQUEST_X_FORWARDED_FOR, httpServletRequest.getHeader("X-Forwarded-For"));
			}
			//			MDC.put(WebMDCConstants.REQUEST_USER_AGENT_MDC_KEY, httpServletRequest.getHeader("User-Agent"));
		}

	}

	void clearMDC() {
		MDC.remove(WebMDCConstants.REQUEST_REMOTE_HOST_MDC_KEY);
		MDC.remove(WebMDCConstants.REQUEST_REQUEST_URI);
		MDC.remove(WebMDCConstants.REQUEST_QUERY_STRING);
		// removing possibly inexistent item is OK
		MDC.remove(WebMDCConstants.REQUEST_REQUEST_URL);
		MDC.remove(WebMDCConstants.REQUEST_METHOD);
		MDC.remove(WebMDCConstants.REQUEST_USER_AGENT_MDC_KEY);
		//		MDC.remove(WebMDCConstants.REQUEST_X_FORWARDED_FOR);
	}

	@Override
	public void init( FilterConfig arg0 ) throws ServletException {
		// do nothing
	}

	public interface WebMDCConstants {
		public static final String REQUEST_REMOTE_HOST_MDC_KEY = "req.remoteHost";
		public static final String REQUEST_USER_AGENT_MDC_KEY = "req.userAgent";
		public static final String REQUEST_REQUEST_URI = "req.requestURI";
		public static final String REQUEST_QUERY_STRING = "req.queryString";
		public static final String REQUEST_REQUEST_URL = "req.requestURL";
		public static final String REQUEST_METHOD = "req.method";
		public static final String REQUEST_X_FORWARDED_FOR = "req.xForwardedFor";
	}

}
