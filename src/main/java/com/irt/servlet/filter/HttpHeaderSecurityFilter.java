/*
 *	File Name:	HttpHeaderSecurityFilter.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/09/29		2.2.0	create
**/

package com.irt.servlet.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.res.StringManager;

public class HttpHeaderSecurityFilter implements Filter {
	protected static final StringManager sm = StringManager.getManager( HttpHeaderSecurityFilter.class );

	// HSTS
	private static final String HSTS_HEADER_NAME = "Strict-Transport-Security";
	private boolean hstsEnabled = true;
	private int hstsMaxAgeSeconds = 0;
	private boolean hstsIncludeSubDomains = false;
	private boolean hstsPreload = false;
	private String hstsHeaderValue;

	// Click-jacking protection
	private static final String ANTI_CLICK_JACKING_HEADER_NAME = "X-Frame-Options";
	private boolean antiClickJackingEnabled = true;
	private XFrameOption antiClickJackingOption = XFrameOption.DENY;
	private URI antiClickJackingUri;
	private String antiClickJackingHeaderValue;

	// Block content sniffing
	private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME = "X-Content-Type-Options";
	private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE = "nosniff";
	private boolean blockContentTypeSniffingEnabled = true;

	// Cross-site scripting filter protection
	private static final String XSS_PROTECTION_HEADER_NAME = "X-XSS-Protection";
	private static final String XSS_PROTECTION_HEADER_VALUE = "1; mode=block";
	private boolean xssProtectionEnabled = true;

	// Referrer-Policy
	private static final String REFERRER_POLICY_HEADER_NAME = "Referrer-Policy";
	private boolean referrerPolicyEnabled = false;
	private String referrerPolicyValue;

	// Referrer-Policy
	private static final String CSP_HEADER_NAME = "Content-Security-Policy";
	private boolean cspEnabled = false;
	private String cspValue;

	// Feature-Policy
	private static final String FEATURE_POLICY_HEADER_NAME = "Feature-Policy";
	private boolean featurePolicyEnabled = false;
	private String featurePolicyValue;

	@Override
	public void init( FilterConfig filterConfig ) throws ServletException {
		Enumeration<String> paramNames = filterConfig.getInitParameterNames();
		while( paramNames.hasMoreElements() ) {
			String paramName = paramNames.nextElement();
			if( !IntrospectionUtils.setProperty(this, paramName, filterConfig.getInitParameter(paramName)) ) {
				String msg = sm.getString( "HttpHeaderSecurityFilter.noSuchProperty", paramName, this.getClass().getName() );
				throw new ServletException( msg );
			}
		}

		StringBuilder hstsValue = new StringBuilder( "max-age=" );
		hstsValue.append(hstsMaxAgeSeconds);
		if (hstsIncludeSubDomains) {
			hstsValue.append( ";includeSubDomains" );
		}
		if (hstsPreload) {
			hstsValue.append( ";preload" );
		}
		hstsHeaderValue = hstsValue.toString();

		// Anti click-jacking
		StringBuilder cjValue = new StringBuilder( antiClickJackingOption.headerValue );
		if( antiClickJackingOption == XFrameOption.ALLOW_FROM ) {
			cjValue.append( ' ' );
			cjValue.append( antiClickJackingUri );
		}
		antiClickJackingHeaderValue = cjValue.toString();
	}

	@Override
	public void destroy() {}

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
		if( response instanceof HttpServletResponse ) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			if( response.isCommitted() ) {
				throw new ServletException( sm.getString("HttpHeaderSecurityFilter.committed") );
			}

			// HSTS
			if( hstsEnabled ) {
				httpResponse.setHeader( HSTS_HEADER_NAME, hstsHeaderValue );
			}

			// anti click-jacking
			if( antiClickJackingEnabled ) {
				httpResponse.setHeader( ANTI_CLICK_JACKING_HEADER_NAME, antiClickJackingHeaderValue );
			}

			// Block content type sniffing
			if( blockContentTypeSniffingEnabled ) {
				httpResponse.setHeader( BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME, BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE );
			}

			// cross-site scripting filter protection
			if( xssProtectionEnabled ) {
				httpResponse.setHeader( XSS_PROTECTION_HEADER_NAME, XSS_PROTECTION_HEADER_VALUE );
			}

			// Referrer-Policy
			if( referrerPolicyEnabled ) {
				httpResponse.setHeader( REFERRER_POLICY_HEADER_NAME, referrerPolicyValue );
			}

			// Content-Security-Policy
			if( cspEnabled ) {
				httpResponse.setHeader( CSP_HEADER_NAME, cspValue );
			}

			// Feature-Policy
			if( featurePolicyEnabled ) {
				httpResponse.setHeader( FEATURE_POLICY_HEADER_NAME, featurePolicyValue );
			}
		}

		chain.doFilter( request, response );
	}

	public boolean isHstsEnabled() {
		return hstsEnabled;
	}

	public void setHstsEnabled( boolean hstsEnabled ) {
		this.hstsEnabled = hstsEnabled;
	}

	public int getHstsMaxAgeSeconds() {
		return hstsMaxAgeSeconds;
	}

	public void setHstsMaxAgeSeconds( int hstsMaxAgeSeconds ) {
		if( hstsMaxAgeSeconds < 0 ) {
			this.hstsMaxAgeSeconds = 0;
		} else {
			this.hstsMaxAgeSeconds = hstsMaxAgeSeconds;
		}
	}

	public boolean isHstsIncludeSubDomains() {
		return hstsIncludeSubDomains;
	}

	public void setHstsIncludeSubDomains( boolean hstsIncludeSubDomains ) {
		this.hstsIncludeSubDomains = hstsIncludeSubDomains;
	}

	public boolean isHstsPreload() {
		return hstsPreload;
	}

	public void setHstsPreload( boolean hstsPreload ) {
		this.hstsPreload = hstsPreload;
	}

	public boolean isAntiClickJackingEnabled() {
		return antiClickJackingEnabled;
	}

	public void setAntiClickJackingEnabled( boolean antiClickJackingEnabled ) {
		this.antiClickJackingEnabled = antiClickJackingEnabled;
	}

	public String getAntiClickJackingOption() {
		return antiClickJackingOption.toString();
	}

	public void setAntiClickJackingOption( String antiClickJackingOption ) {
		for( XFrameOption option : XFrameOption.values() ) {
			if( option.getHeaderValue().equalsIgnoreCase(antiClickJackingOption) ) {
				this.antiClickJackingOption = option;
				return;
			}
		}
		throw new IllegalArgumentException( sm.getString("httpHeaderSecurityFilter.clickjack.invalid", antiClickJackingOption) );
	}

	public String getAntiClickJackingUri() {
		return antiClickJackingUri.toString();
	}

	public boolean isBlockContentTypeSniffingEnabled() {
		return blockContentTypeSniffingEnabled;
	}

	public void setBlockContentTypeSniffingEnabled( boolean blockContentTypeSniffingEnabled ) {
		this.blockContentTypeSniffingEnabled = blockContentTypeSniffingEnabled;
	}

	public void setAntiClickJackingUri( String antiClickJackingUri ) {
		URI uri;
		try {
			uri = new URI( antiClickJackingUri );
		} catch( URISyntaxException e ) {
			throw new IllegalArgumentException( e );
		}
		this.antiClickJackingUri = uri;
	}

	public boolean isXssProtectionEnabled() {
		return xssProtectionEnabled;
	}

	public void setXssProtectionEnabled( boolean xssProtectionEnabled ) {
		this.xssProtectionEnabled = xssProtectionEnabled;
	}


	public boolean isReferrerPolicyEnabled() {
		return referrerPolicyEnabled;
	}

	public void setReferrerPolicyEnabled( boolean referrerPolicyEnabled ) {
		this.referrerPolicyEnabled = referrerPolicyEnabled;
	}

	public String getReferrerPolicyValue() {
		return referrerPolicyValue;
	}

	public void setReferrerPolicyValue( String referrerPolicyValue ) {
		this.referrerPolicyValue = referrerPolicyValue;
	}

	public boolean isCspEnabled() {
		return cspEnabled;
	}

	public void setCspEnabled( boolean cspEnabled ) {
		this.cspEnabled = cspEnabled;
	}

	public String getCspValue() {
		return cspValue;
	}

	public void setCspValue( String cspValue ) {
		this.cspValue = cspValue;
	}

	public boolean isFeaturePolicyEnabled() {
		return featurePolicyEnabled;
	}

	public void setFeaturePolicyEnabled( boolean featurePolicyEnabled ) {
		this.featurePolicyEnabled = featurePolicyEnabled;
	}

	public String getFeaturePolicyValue() {
		return featurePolicyValue;
	}

	public void setFeaturePolicyValue( String featurePolicyValue ) {
		this.featurePolicyValue = featurePolicyValue;
	}

	private enum XFrameOption {
		DENY( "DENY" ),
		SAME_ORIGIN( "SAMEORIGIN" ),
		ALLOW_FROM( "ALLOW-FROM" );

		private final String headerValue;

		private XFrameOption( String headerValue ) {
			this.headerValue = headerValue;
		}

		public String getHeaderValue() {
			return headerValue;
		}
	}
}
