/*
 *	File Name:	LoadBalancerDrainingFilter.java
 *	Version:	0.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		0.0.0	create( @experimental )
**/

package com.irt.servlet.filter;

import com.irt.html.HtmlUtility;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * A Filter to detect situations where a load-balanced node receiving a request has been deactivated by the load balancer (JK_LB_ACTIVATION=DIS) and
 * the incoming request has no valid session.
 *
 * In these cases, the user's session cookie should be removed if it exists, any ";jsessionid" parameter should be removed from the request URI, and
 * the client should be redirected to the same URI. This will cause the load-balanced to re-balance the client to another server.
 *
 * A request parameter is added to the redirect URI in order to avoid repeated redirects in the event of an error or misconfiguration.
 *
 * All this work is required because when the activation state of a node is DISABLED, the load-balancer will still send requests to the node if they
 * appear to have a session on that node. Since mod_jk doesn't actually know whether the session id is valid, it will send the request blindly to the
 * disabled node, which makes it take much longer to drain the node than strictly necessary.
 *
 * @see http://tomcat.apache.org/connectors-doc/generic_howto/loadbalancers.html
 *
 * @author Chris Schultz (schultz@apache.org)
 *
 */
public class LoadBalancerDrainingFilter extends HttpFilter {
	private static final Logger logger = Logger.getLogger(LoadBalancerDrainingFilter.class);

	/**
	 * The error code that will be used if redirect-detection detects that a redirect has come back. Defaults to 500 (INTERNAL_SERVER_ERROR).
	 */
	private int _redirectDetectionErrorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

	/**
	 * The HTTP response code that will be used to redirect the request back to the load-balancer for re-balancing. Defaults to 307
	 * (TEMPORARY_REDIRECT).
	 *
	 * HTTP status code 305 (USE_PROXY) might be an option, here. too.
	 */
	private int _redirectStatusCode = HttpServletResponse.SC_TEMPORARY_REDIRECT;

	/**
	 * The request parameter name that will be used for redirect-detection. Defaults to "lbrp".
	 */
	private String _redirectDetectionRequestParameterName = "lbrp";

	/**
	 * The request parameter value that will be used for redirect-detection. Defaults to <code>null</code>.
	 */
	private String _redirectDetectionRequestParameterValue = null;

	/**
	 * The name of the cookie that will be checked for validity. Defaults to "JSESSIONID".
	 */
	private String _sessionCookieName = "JSESSIONID";

	/**
	 * The session cookie path. Defaults to the application's context path plus a trailing "/".
	 */
	private String _sessionCookiePath;

	/**
	 * The name of the cookie which can be set to ignore the "draining" action of this Filter. This will allow a client to contact the server without
	 * being re-balanced to another server. The expected cookie value can be set in the {@link #_ignoreCookieValue}. The cookie name and value must
	 * match to avoid being re-balanced.
	 */
	private String _ignoreCookieName;

	/**
	 * The value of the cookie which can be set to ignore the "draining" action of this Filter. This will allow a client to contact the server without
	 * being re-balanced to another server. The expected cookie name can be set in the {@link #_ignoreCookieValue}. The cookie name and value must
	 * match to avoid being re-balanced.
	 */
	private String _ignoreCookieValue;

	/**
	 * maybe for irt. currently experimental check..
	 */
	private String _nocheckSessionCookieRequestedSessionId = "false";
	private String _useSysOutLogging = "false";

	@Override
	public void init( FilterConfig config ) {
		// super.init(config);

		String s = config.getInitParameter("redirect-detection-error-code");
		if( null != s )
			_redirectDetectionErrorCode = Integer.parseInt(s);

		s = config.getInitParameter("redirect-status-code");
		if( null != s )
			_redirectStatusCode = Integer.parseInt(s);

		s = config.getInitParameter("redirect-detection-request-parameter-name");
		if( null != s )
			_redirectDetectionRequestParameterName = s;

		s = config.getInitParameter("redirect-detection-request-parameter-value");
		if( null != s )
			_redirectDetectionRequestParameterValue = s;

		s = config.getInitParameter("session-cookie-name");
		if( null != s )
			_sessionCookieName = s;

		s = config.getInitParameter("session-cookie-path");
		if( null != s )
			_sessionCookiePath = s;

		s = config.getInitParameter("ignore-cookie-value");
		if( null != s )
			_ignoreCookieValue = s;

		s = config.getInitParameter("nocheck-session-cookie-requested-sessionid");
		if( null != s )
			_nocheckSessionCookieRequestedSessionId = s;

		s = config.getInitParameter("use-sysout-logging");
		if( null != s )
			_useSysOutLogging = s;

		s = config.getInitParameter("ignore-cookie-name");
		if( null != s )
			_ignoreCookieName = s;
		else if( null != _ignoreCookieValue )
			_ignoreCookieValue = null; // Wipe this value out if it exists
	}

	/**
	 * irt specific session id
	 *
	 * @param req
	 * @param sessionCookieName
	 *            : usually sessionKey {@link com.irt.custom.SystemConfig#DEFAULT_SESSION_KEY}
	 * @return
	 */
	public String getSessionId( HttpServletRequest req, String sessionCookieName ) {
		javax.servlet.http.Cookie[] cookies = req.getCookies();
		if( cookies != null ) {
			for( int i = 0; i < cookies.length; i++ )
				if( sessionCookieName.equals(cookies[i].getName()) )
					return cookies[i].getValue();
		}

		return null;
	}

	@Override
	public void doFilter( HttpServletRequest request, HttpServletResponse response, FilterChain chain ) throws IOException, ServletException {
		if( "DIS".equals(request.getAttribute("JK_LB_ACTIVATION"))
				&& null != request.getRequestedSessionId()
				&& !request.isRequestedSessionIdValid() ) {
			if( logger.isDebugEnabled() )
				logger.debug("Load-balancer is in DISABLED state; draining this node");

			boolean ignoreRebalance = false; // Allow certain clients
			Cookie sessionCookie = null;

			// Kill any session cookie present
			final Cookie[] cookies = request.getCookies();

			if( null != cookies ) {
				for( Cookie cookie : cookies ) {
					final String cookieName = cookie.getName();

					if( logger.isTraceEnabled() )
						logger.trace("Checking cookie " + cookieName + "=" + cookie.getValue());

					if( Boolean.getBoolean(_nocheckSessionCookieRequestedSessionId) ) {
						if( Boolean.getBoolean(_useSysOutLogging) ) {
							System.out.println("Cookie cookie "+ cookieName + "="+ cookie.getValue() + " nocheck");
						}
						if( _sessionCookieName.equals(cookieName) )
							sessionCookie = cookie;
					} else {
						if( Boolean.getBoolean(_useSysOutLogging) ) {
							System.out.println("Cookie cookie "+ cookieName + "="+ cookie.getValue() + " check");
						}
						if( _sessionCookieName.equals(cookieName)
								&& request.getRequestedSessionId().equals(cookie.getValue()) )
							sessionCookie = cookie;
					}

					// Is the client presenting a valid ignore-cookie value?
					if( null != _ignoreCookieName
							&& _ignoreCookieName.equals(cookieName)
							&& null != _ignoreCookieValue
							&& _ignoreCookieValue.equals(cookie.getValue()) )
						ignoreRebalance = true;
				}
			}

			if( ignoreRebalance ) {
				if( logger.isDebugEnabled() )
					logger.debug("Client is presenting a valid " + _ignoreCookieName
							+ " cookie, re-balancing is being skipped");
				chain.doFilter(request, response);
				return;
			}

			if( null != sessionCookie ) {
				// TODO: Expire variations on the context path? "/", "/path", "/path/"?
				String sessionCookiePath;
				if( null == _sessionCookiePath ) {
					sessionCookiePath = request.getContextPath();
					if( !sessionCookiePath.endsWith("/") )
						sessionCookiePath = sessionCookiePath + "/";
				} else
					sessionCookiePath = _sessionCookiePath;


				sessionCookie.setPath(sessionCookiePath);
				sessionCookie.setMaxAge(0); // Delete
				sessionCookie.setValue(""); // Purge the cookie's value
				response.addCookie(sessionCookie);

				if( Boolean.getBoolean(_useSysOutLogging) ) {
					System.out.println("Cookie cookie "+ sessionCookie.getName()+ "="+ sessionCookie.getValue() + " . added to response");
				}
			}

			// Re-write the URI if it contains a ;jsessionid parameter
			String uri = request.getRequestURI();
			if( uri.contains(";jsessionid=") )
				uri = uri.replaceFirst(";jsessionid=[^&?]*", "");

			String queryString = request.getQueryString();

			if( null != queryString )
				if( null != _redirectDetectionRequestParameterValue )
					uri = uri + "?" + queryString + "&"
							+ _redirectDetectionRequestParameterName
							+ "=" + _redirectDetectionRequestParameterValue;
				else
					uri = uri + "?" + queryString + "&"
							+ _redirectDetectionRequestParameterName;
			else if( null != _redirectDetectionRequestParameterValue )
				uri = uri + "?"
						+ _redirectDetectionRequestParameterName
						+ "=" + _redirectDetectionRequestParameterValue;
			else
				uri = uri + "?" + _redirectDetectionRequestParameterName;

			if( null == request.getParameter(_redirectDetectionRequestParameterName) ) {
				// NOTE: Do not call response.encodeRedirectURL or the bad
				// sessionid will be restored
				response.setHeader("Location", uri);
				response.setStatus(_redirectStatusCode);
			} else {
				logger.error("This request has already been redirected from a "
						+ " failing node: refusing to redirect again. url="
						+ request.getRequestURI()
						+ ", cookies=" + getCookiesString(_sessionCookieName, request));

				// Request has already been redirected once: don't cause a
				// redirect storm
				response.sendError(_redirectDetectionErrorCode,
						"Refusing to redirect (again) because redirection has already been detected."
								+ " Would have redirected to "
								// + htmlEscape(uri)
								+ HtmlUtility.toHtmlString(uri));
			}
		} else
			chain.doFilter(request, response);
	}

	/**
	 * Turns funky characters into HTML entity equivalents
	 * <p>
	 * e.g. <tt>"bread" & "butter"</tt> => <tt>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</tt>. Update: supports nearly all HTML
	 * entities, including funky accents. See the source code for more detail.
	 **/
	// public static String htmlEscape( String s ) {
	// StringBuilder buf = new StringBuilder();
	// int i;
	// for( i = 0; i < s.length(); ++i ) {
	// char ch = s.charAt(i);
	// String entity = i2e.get(Integer.valueOf((int)ch));
	// if( entity == null ) {
	// if( ( (int)ch ) > 128 ) {
	// buf.append("&#").append(( (int)ch )).append(";");
	// } else {
	// buf.append(ch);
	// }
	// } else {
	// buf.append("&").append(entity).append(";");
	// }
	// }
	// return buf.toString();
	// }

	private String getCookiesString( String cookieName,
			HttpServletRequest request ) {
		if( null == cookieName )
			return null;
		Cookie[] cookies = request.getCookies();
		if( null == cookies || null == cookies )
			return "[]";
		StringBuilder sb = new StringBuilder();
		for( Cookie cookie : cookies )
			sb.append(cookie.getName()).append("=").append(cookie.getValue());

		return sb.toString();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}