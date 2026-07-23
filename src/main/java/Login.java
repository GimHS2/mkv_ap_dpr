/*
 *	File Name:	Login.java
 *	Version:	2.2.9
 *
 *	Description:
 *
 *	Note:
 *		login.jsp
 *		error_passwd.jsp
 *		error_session.jsp
 *		systemConfig.getProperty( "noticeClassCode" )
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2022/09/30		2.2.10	changePassword(): partyId, userId 를 request 속성으로 넘기도록 변경
 *	hankalam	2021/11/30		2.2.9	신규 UI/UX 적용
 *	jbaek		2020/03/30		2.2.8	loginInput 화면에 갈때에 지원되지 않는 경우 기본값 사용.
 *	hankalam	2020/03/31		2.2.8	login(): httpOnly 환경변수 갖고오는 로직 삭제, cookieOption 추가
 *										loginInput(): 에러 page에서 로그인 화면으로 이동하는 경우 메시지 표시
 *	hankalam	2017/03/31		2.2.7	쿠키 secure, httpOnly 속성 시스템 환경변수로 적용
 *	hankalam	2016/05/31		2.2.6	웹취약성 수정. POST 로 넘어오는 경우의 locale 파라미터 유효성 검사 추가
 *	song7981	2016/03/31		2.2.5	쿠키 secure, httpOnly 속성 적용(QA는 SSL을 사용안하므로 적용안함)
 *	hankalam	2015/10/30		2.2.4	웹취약성 수정. locale 파라미터 위험문자 검사
 *	jbaek		2014/12/31		2.2.3	maintenanceNotice 적용
 *	lsinji		2008/09/26		2.2.2	logininput에서 defaultlocale을 가져오도록 변경. extends DPRServletModel 변경
 *										login시에 userid가 unique할때 partyid 가져오도록 변경
 *										changePassword에 password security validation 추가
 *										, Country organization를 session에 저장
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.2.0	log4j 사용
 *										com.irt.system.SystemConfig 변경사항 적용: Locale 사용
 *	stghr12		2007/04/30		2.1.0	systemConfig.getProperty("noticeClassCode") 사용
 *										checkAuthorize(): ServletModel 변경사항 적용
 *										loginInput(): handler가 null일 때 처리
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	stghr12		2002/10/18				create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.ServletModelException;
import com.irt.system.SessionManagerException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/Login"})
public class Login extends DPRServletModel {
	public final static String MODE_LOGIN				= "login";
	public final static String MODE_LOGININPUT			= "login";
	public final static String MODE_LOGOUT				= "logout";
	public final static String MODE_PASSWORD			= "passwd";

	public final static String PARAM_KILLUSER			= "killuser";

	protected boolean changePassword( Context ctx ) throws IOException, ServletException, SQLException {
		String partyId = ctx.req.getParameter( "partyId" );
		String userId = ctx.req.getParameter( "userId" );
		String presentPassword = ctx.req.getParameter( "presentPassword" );
		String password = ctx.req.getParameter( "password" );

		boolean validPassword = false;
		try {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "partyId", partyId );
			recordMap.put( "userId", userId );
			ctx.req.setAttribute( "record", recordMap );
			recordMap.put( "password", password );

			validPassword = com.irt.dpr.ValidablePassword.validableCheckPassword( ctx.handler, recordMap );
			if( !validPassword ) {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_INVALID_PASSWORD") );
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				return forward( ctx, systemConfig.getJspPath() + "/error_passwd.jsp" );
			}

			boolean ret = ctx.sessionMng.changePassword( partyId, userId, presentPassword, password );
			if( ret )
				ctx.handler.commit();
			else {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_INVALID_PASSWORD") );
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				return forward( ctx, systemConfig.getJspPath() + "/error_passwd.jsp" );
			}
		} catch( com.irt.data.DataException dataEx ) {
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "internal error.", sqlEx );
		} catch( com.irt.dpr.InvalidPasswordException passwordEx ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage(passwordEx.getErrorKey()) );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			return forward( ctx, systemConfig.getJspPath() + "/error_passwd.jsp" );
		} catch( SessionManagerException sessionEx ) {
			String errorKey = sessionEx.getErrorKey();
			if( errorKey == null ) throw new ServletModelException( sessionEx.getCause() );
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage(errorKey) );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			return forward( ctx, systemConfig.getJspPath() + "/error_passwd.jsp" );
		}

		return login( ctx );
	}

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return true;
	}

	@Override
	protected boolean defaultReq( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( ctx.sessionMng.isLoginUser() ) {
			String url = ctx.pageConfig.getBackURL();
			if( url == null )
				return loginInput( ctx );
			else
				return sendRedirect( ctx, url );
		} else
			return loginInput( ctx );
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( isPost ) {
			if( MODE_LOGIN.equals(ctx.mode) ) return login( ctx );
			if( MODE_PASSWORD.equals(ctx.mode) ) return changePassword( ctx );
		} else {
			if( MODE_LOGININPUT.equals(ctx.mode) ) return loginInput( ctx );
			if( MODE_LOGOUT.equals(ctx.mode) ) return logout( ctx );
		}
		return super.doRequest( ctx, isPost );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );
		ctx.pageConfig.setSystemPackageCode( "RBM", "Login" );
	}

	protected boolean login( Context ctx ) throws IOException, ServletException, SQLException {
		String partyId = ctx.req.getParameter( "partyId" );
		String userId = ctx.req.getParameter( "userId" );
		String password = ctx.req.getParameter( "password" );
		boolean killuser = ( ctx.req.getParameter(PARAM_KILLUSER) != null );

		try {
			ctx.sessionMng.logout();
		} catch( SessionManagerException sessionEx ) {}
		try {
			/* 해당 사용자의 parties가 여러개인지 확인 */
			String[] userParties = new com.irt.dpr.Login(ctx.handler).getUserPartyId( userId );
			if( userParties == null || userParties.length != 1 )
				throw new SessionManagerException( SessionManagerException.INVALID_USERID );
			partyId = userParties[0];

			if( ctx.sessionMng.login(partyId, userId, password, killuser) ) {
				String cookieOption = systemConfig.getCookieOption();
				if( cookieOption != null ) {
					// 쿠키 secure, httpOnly 적용(https일 때만 로그인 됨)
					String sessKey = systemConfig.getSessionKey() +"="+ ctx.sessionMng.getSessionId();
					String domain = "domain"+ "=" + systemConfig.getDomain();
					String path = "path" + "=" + "/";
					ctx.res.setHeader("Set-Cookie", sessKey + ";" + domain + ";" + path + ";" + cookieOption );
				} else {
					Cookie cookie = new Cookie( systemConfig.getSessionKey(), ctx.sessionMng.getSessionId() );
					cookie.setDomain( systemConfig.getDomain() );
					cookie.setPath( "/" );
					ctx.res.addCookie( cookie );
				}

				logger.info( "login(kill:"+ killuser +"): "+ ctx.sessionMng.getPartyId() +":"+ ctx.sessionMng.getUserId() );

				String url = ctx.pageConfig.getBackURL();
				if( url == null ) {
					url = systemConfig.getHomepageURL( ctx.locale, true );

					String locale = ctx.req.getParameter( PARAM_LOCALE );
					if( locale != null && locale.split("_", 3)[0].length() == 2 && locale.indexOf(">") < 0 && locale.indexOf("<") < 0
							&& locale.indexOf("+") < 0 && locale.indexOf(";") < 0 ) url = HtmlUtility.replaceURLQuery(url, PARAM_LOCALE, locale );
				}
				url = HtmlUtility.replaceURLQuery(url, "alerted", "N" );

				/* Country Organization */
				{
					com.irt.dpr.CountryCondition db = new com.irt.dpr.CountryCondition( ctx.handler );
					Map<String, Object> conditionMap = com.irt.data.Record.createMap( "countryCode", ctx.sessionMng.getGln() );
					conditionMap.put( "conditionInd", com.irt.dpr.CountryCondition.CONDITION_INDICATOR_REGISTRED );

					List<Map<String, Object>> recordList = db.getRecords( conditionMap );
					if( recordList != null && recordList.isEmpty() && recordList.size() == 1 ) {
						saveOrganizationCodeToSession( ctx, (String)(recordList.get(0).get("organizationCode")) );
					}
				}

				return sendRedirect( ctx, url );
			}
		} catch( SessionManagerException sessionEx ) {
			String errorKey = sessionEx.getErrorKey();
			if( errorKey == null ) throw new ServletModelException( sessionEx.getCause() );

			if( SessionManagerException.PASSWORD_EXPIRED.equals(errorKey) ) {
				if( ctx.pageConfig.getBackURL() == null ) {
					String url = systemConfig.getHomepageURL( ctx.locale, true );

					String locale = ctx.req.getParameter( PARAM_LOCALE );
					if( locale != null && locale.length() > 0 && locale.indexOf(">") < 0 && locale.indexOf("<") < 0
							&& locale.indexOf("+") < 0 && locale.indexOf(";") < 0 ) url = HtmlUtility.replaceURLQuery(url, PARAM_LOCALE, locale );

					ctx.pageConfig.setBackURL( url );
				}
				Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
				recordMap.put( "partyId", partyId );
				recordMap.put( "userId", userId );
				ctx.req.setAttribute( "record", recordMap );
				return forward( ctx, systemConfig.getJspPath() + "/error_passwd.jsp" );
			}

			ctx.pageConfig.setMessage( ctx.msghandler.getMessage(errorKey) );

			if( SessionManagerException.EXCEED_MAX_ACCESSCOUNT.equals(errorKey) )
				ctx.pageConfig.setProperty( "killuser", "true" );
			if( ctx.req.getParameter("sessionerr") != null )
				return forward( ctx, systemConfig.getJspPath() + "/error_session.jsp" );
		}

		return loginInput( ctx );
	}

	protected boolean loginInput( Context ctx ) throws IOException, ServletException, SQLException {
		if( ctx.sessionMng.isLoginUser() ) {
			String url = ctx.pageConfig.getBackURL();
			if( url == null ) {
				url = systemConfig.getHomepageURL( ctx.locale, true );

				String locale = ctx.req.getParameter( PARAM_LOCALE );
				if( locale != null && locale.split("_", 3)[0].length() == 2 && locale.indexOf(">") < 0 && locale.indexOf("<") < 0
						&& locale.indexOf("+") < 0 && locale.indexOf(";") < 0 ) url = HtmlUtility.replaceURLQuery(url, PARAM_LOCALE, locale );
			}
			url = HtmlUtility.replaceURLQuery(url, "alerted", "N" );

			/* Country Organization */
			{
				com.irt.dpr.CountryCondition db = new com.irt.dpr.CountryCondition( ctx.handler );
				Map<String, Object> conditionMap = com.irt.data.Record.createMap( "countryCode", ctx.sessionMng.getGln() );
				conditionMap.put( "conditionInd", com.irt.dpr.CountryCondition.CONDITION_INDICATOR_REGISTRED );

				List<Map<String, Object>> recordList = db.getRecords( conditionMap );
				if( recordList != null && recordList.isEmpty() && recordList.size() == 1 ) {
					saveOrganizationCodeToSession( ctx, (String)(recordList.get(0).get("organizationCode")) );
				}
			}

			return sendRedirect( ctx, url );
		}
		String csvLocales = ctx.pageConfig.getProperty("partySupportLocale");
		if( csvLocales != null && csvLocales.length() > 0 ) {
			List<String> supLocales = java.util.Arrays.asList(csvLocales.split(",\\s+?"));
			if( !supLocales.contains(ctx.locale) ) {
				ctx.locale = new java.util.Locale( supLocales.get(0) );
			}
		}
		ctx.pageConfig.setProperty( "locale", ctx.locale.getDisplayLanguage() );
		String boardClassCode = systemConfig.getProperty( "noticeClassCode" );
		if( boardClassCode != null ) {
			com.irt.sql.SQLHandler handler = null;
			try {
				com.irt.system.SystemConfig rbm_systemConfig = com.irt.custom.SystemConfig.getInstance( "RBM" );

				handler = rbm_systemConfig.createSQLHandler( rbm_systemConfig.getMessageHandler(ctx.locale) );
				if( handler == null )
					throw new ServletModelException( ServletModelException.INVALID_DATAHANDLER );

				com.irt.rbm.rbm.Board db = new com.irt.rbm.rbm.Board( handler );

				String[] fieldKeys = new String[] { "boardClassCode", "boardNumber", "title", "createDateTime" };
				ctx.req.setAttribute( "notices", db.getRecords(boardClassCode, fieldKeys, 0, 10) );

				//setMaintenanceNotice( ctx.req, ctx.res, ctx.locale );
			} finally {
				if( handler != null ) handler.close();
			}
		}
		String message = ctx.req.getParameter( "msg" );
		if( message != null && message.length() > 0 )
			ctx.pageConfig.setMessage( message );


		return forward( ctx, systemConfig.getJspPath() + "/login.jsp" );
	}

	protected boolean logout( Context ctx ) throws IOException, ServletException, SQLException {
		if( ctx.sessionMng.isLoginUser() ) {
			try {
				ctx.sessionMng.logout();
			} catch( SessionManagerException sessionEx ) {
				throw new ServletModelException( sessionEx );
			}
			Cookie cookie = new Cookie( systemConfig.getSessionKey(), "" );
			cookie.setDomain( systemConfig.getDomain() );
			cookie.setPath( "/" );
			cookie.setMaxAge( 0 );
			ctx.res.addCookie( cookie );
			logger.info( "logout: "+ ctx.sessionMng.getPartyId() +":"+ ctx.sessionMng.getUserId() );
		}

		String url = ctx.pageConfig.getBackURL();
		if( url == null ) {
			url = systemConfig.getHomepageURL( ctx.locale, false );

			String locale = ctx.req.getParameter( PARAM_LOCALE );
			if( locale != null && locale.length() > 0 && locale.indexOf(">") < 0 && locale.indexOf("<") < 0
					&& locale.indexOf("+") < 0 && locale.indexOf(";") < 0 ) url = HtmlUtility.replaceURLQuery(url, PARAM_LOCALE, locale );
		}
		String message = ctx.req.getParameter( "msg" );
		if( message != null && message.length() > 0 ) {
			message = java.net.URLEncoder.encode( message, "UTF-8" );
			url = HtmlUtility.replaceURLQuery(url, "msg", message );
		}

		return sendRedirect( ctx, url );
	}
}
