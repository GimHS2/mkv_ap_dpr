/*
 *	File Name:	HttpFilter.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.servlet.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple Abstract Class to convert from ServletRequest,ServletResponse to HttpServletRequest,HttpServletResponse.
 */
public abstract class HttpFilter implements Filter {

	public abstract void doFilter( HttpServletRequest request, HttpServletResponse response, FilterChain chain ) throws IOException, ServletException;

	@Override
	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException {
		if( !( req instanceof HttpServletRequest && res instanceof HttpServletResponse ) ) {
			throw new ServletException("non-HTTP request or response");
		}
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		doFilter(request, response, chain);
	}
}
