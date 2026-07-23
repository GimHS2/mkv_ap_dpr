/*
 *	File Name:	ServletModel.java
 *	Version:	2.2.7c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.7c	createContext(): Party ID 별 지원되는 Locale 로 자동 변환 로직 추가
 *	hankalam	2020/12/31		2.2.7c	sendRedirect(): URL White List 에 있는 도메인만 redirect 되도록 수정.
 *	jbaek		2020/06/30		2.2.7c	DebugOption.verboseThrow 적용
 *	jbaek		2019/11/30		2.2.7c	slf4j MDC 적용. jsonResponse적용. servletEx에서 method및 라인 찾기 적용.
 *	hankalam	2016/09/30		2.2.7	forward(): pageConfig의 property value XSS 필터링 cleanXSSProperty() 추가
 *	hankalam	2016/08/31		2.2.6	updateLastAccess() 시 에러날 경우 ctx.handler close() 안되던 문제 수정
 *	GimHS		2016/08/31		2.2.6	forward(): 포워드시 ServletRequestWrapper 형의 request 가 넘어가도록 수정
 *	song7981	2016/07/08		2.2.5	DEFAULT_MAX_MODESIZE 추가, updateLastAccess 시 SQLException발생하면 log만 찍도록 변경
 *	jbaek		2014/12/31		2.2.4	execute(): updateLastAccess(..., userAgent ) 추가
 *	jbaek		2014/03/31		2.2.3	rbm2에서 적용: PARAM_DEBUGSQL 추가, SystemAdmin일 때, PARAM_DEBUGSQL이 'Y'이면 debugging하도록 설정
 *	stghr12		2008/08/29		2.2.2	doPost(): setAttribute("request") 추가
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										createContext(): setAttribute("handler") 추가
 *										execute(): elapsedTimeMilli 저장
 *										condname() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										log4j 사용
 *										com.irt.system.SystemConfig 변경사항 적용: Locale, TimeZone 사용, PARAM_LOCALE 추가
 *										execute(): 에러 message 처리방법 변경
 *										getColumnList(): CANNOT_FIND_COLUMNLIST 에러 발생시 columnListName 표시하도록 수정
 *										getSummaryColumnList(): CANNOT_FIND_COLUMNLIST 에러 발생시 columnListName 표시하도록 수정
 *										getSystemConfig(): public -> protected
 *										PARAM_COLUMNLISTTYPE 추가
 *										getColumnList(), getSummaryColumnList(): columnListType 처리 추가
 *										DEFAULT_MAX_POSTSIZE: long으로 변경
 *	stghr12		2007/10/31		2.1.2	PARAM_SAVEDOBJECT_KEY 추가
 *										getSummaryColumnList() 추가
 *	stghr12		2007/04/30		2.1.1	checkAuthorize(), getColumnList(), getColumnResourceBundle(): IOException, SQLException 삭제
 *										downloadInput() 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form 변경사항 반영
 *										encoding을 무조건 UTF-8을 쓰도록 수정
 *										upload(), uploadInput() 추가
 *										ServletModelException.ERROR 에러 메시지 처리로직 변경
 *	stghr12		2006/02/28		2.0.0	version Up
 *	stghr12		2004/02/13		1.0.0	version 관리
 *	stghr12		2002/04/15				create
 *
 **/

package com.irt.servlet;

import com.irt.data.DataException;
import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ColumnResourceBundle;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.sql.SQLHandler;
import com.irt.system.SessionManager;
import com.irt.system.SessionManagerException;
import com.irt.util.MessageHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

/**
 * Servlet을 처리하는 Super Class.
 * SessionManager, SystemConfig등을 생성하여, mode에 맞게 함수들을 호출해 준다.
 * ctx.req.setAttribute 항목
 * <ul type='square'>
 * <li>req.setAttribute( "systemConfig", systemConfig );
 * <li>req.setAttribute( "sessionMng", ctx.sessionMng );
 * <li>req.setAttribute( "msghandler", ctx.msghandler );
 * <li>req.setAttribute( "htmlpage", ctx.pageConfig.getHtmlPage() );
 * <li>req.setAttribute( "property", ctx.pageConfig.getProperty() );
 * <li>req.setAttribute( "queryStorage", ctx.handler.getSavedQuery() );
 * <li>req.setAttribute( "request", new MultipartHttpRequest() );
 * </ul>
 */
public abstract class ServletModel extends javax.servlet.http.HttpServlet {
	private final static long DEFAULT_MAX_POSTSIZE		= 0x6000000;	// 6 MB
	private final static int DEFAULT_MAX_MODESIZE		= 20;

	public final static String PARAM_MODE				= "mode";
	public final static String PARAM_URL				= "url";
	public final static String PARAM_FOCUS				= "focus";
	public final static String PARAM_MENU				= "menu";
	public final static String PARAM_LOCALE				= "locale";
	public final static String PARAM_WINTYPE			= "wintype";
	public final static String PARAM_DEBUGSQL			= "debugSQL";

	public final static String PARAM_ALLROWS			= "all";
	public final static String PARAM_MAXROWS			= "max";
	public final static String PARAM_SKIPROWS			= "skip";
	public final static String PARAM_SORTKEY			= "sort";

	public final static String PARAM_CONDITION_KEY		= "condkey";
	public final static String PARAM_MESSAGE_KEY		= "msgkey";
	public final static String PARAM_SAVEDOBJECT_KEY	= "savedkey";

	public final static String PARAM_FILTER_TYPE		= "filterType";
	public final static String PARAM_FILTER_VALUE		= "filterValue";

	/**
	 * select&naming 기능에서 선택한 값을 입력해야 되는 Name Object의 이름 규칙 파리미터(각 기능별로 값을 정의해서 사용)<br>
	 * 예1) 상품관리 기능의 경우 F: fullname(itemName), S: shortname(name))<br>
	 * 예2) 업체관리 기능의 경우 C: companyName, L: locationName, A: companyName,locationName.
	 */
	public final static String PARAM_SELECT_NAMECLASS	= "namecls";

	public final static String PARAM_COLUMNLISTTYPE		= "ctype";

	/**	조건 **/
	public final static String MODE_COND				= "cond";
	/** 조건 */
	public final static String MODE_CONDITION_NAME		= "condname";
	/**	DEFAULT **/
	public final static String MODE_DEFAULT				= "default";
	/** 다운로드 **/
	public final static String MODE_DOWNLOAD			= "down";
	/** 다운로드 조건 **/
	public final static String MODE_DOWNLOADINPUT		= "idown";
	/**	정보 **/
	public final static String MODE_INFO				= "info";
	/**	목록 표시 **/
	public final static String MODE_LIST				= "list";
	/**	목록 개수 표시 **/
	public final static String MODE_LISTCOUNT			= "cnt";
	/**	수정 **/
	public final static String MODE_MODIFY				= "mod";
	/**	수정창 **/
	public final static String MODE_MODIFYINPUT			= "imod";
	/**	수정(다수) **/
	public final static String MODE_MULTIMODIFY			= "mmod";
	/**	수정창(다수) **/
	public final static String MODE_MULTIMODIFYINPUT	= "immod";
	/**	이름을 구하는 기능 **/
	public final static String MODE_NAME				= "name";
	/**	입력 **/
	public final static String MODE_REGIST				= "reg";
	/**	입력창 **/
	public final static String MODE_REGISTINPUT			= "ireg";
	/**	삭제 **/
	public final static String MODE_REMOVE				= "del";
	/**	선택창 **/
	public final static String MODE_SELECT				= "sel";
	/** 업로드 **/
	public final static String MODE_UPLOAD				= "up";
	/** 업로드 입력창 **/
	public final static String MODE_UPLOADINPUT			= "iup";

	protected SystemConfig systemConfig;

	protected Logger logger;

	protected static class Context {
		public String mode;
		public HttpServletRequest req;
		public HttpServletResponse res;
		public Locale locale;
		public SQLHandler handler;
		public PageConfig pageConfig;
		public MessageHandler msghandler;
		public SessionManager sessionMng;
		public Object db;
		public Object extraObj;
	}

	public ServletModel() {}

	/**
	 *
	 */
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		try {
			ctx.sessionMng.checkLogin();
		} catch( SessionManagerException sessionEx ) {
			throw new ServletModelException( sessionEx );
		}

		try {
			ctx.sessionMng.checkAuthorize( ctx.pageConfig.getSystemCode(), ctx.pageConfig.getPackageCode() );
		} catch( SessionManagerException sessionEx ) {
			throw new ServletModelException( sessionEx );
		}

		return true;
	}

	protected boolean cond( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean condname( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	/**
	 *
	 */
	private Context createContext( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException, SQLException {
		Context ctx = new Context();
		boolean success = false;

		try {
			// ctx.mode, ctx.req, ctx.res 설정 및 setAttribute("systemConfig")
			ctx.mode = req.getParameter( PARAM_MODE );
			if( ctx.mode == null || ctx.mode.length() == 0 ) ctx.mode = MODE_DEFAULT;
			ctx.req = req;
			ctx.res = res;
			req.setAttribute( "systemConfig", systemConfig );

			// ctx.locale, ctx.msghandler
			ctx.msghandler = systemConfig.getMessageHandler( ctx.locale = ServletUtility.getLocale(req) );

			// ctx.sessionMng 설정 및 setAttribute("sessionMng")
			try {
				ctx.sessionMng = systemConfig.createSessionManager( req, ctx.msghandler );
				if( ctx.sessionMng == null )
					throw new ServletModelException( ServletModelException.INVALID_SESSION );

				String[] supportLocales;
				if( ctx.sessionMng.getPartyId() != null ) {
					supportLocales = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;"+ ctx.sessionMng.getPartyId(), "en").split( "," );
				} else {
					supportLocales = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;SupportLocale", "en,zh,th,vi,ko").split( "," );
				}
				boolean hasLocale = false;
				for( String locale : supportLocales ) {
					if( locale.equals(ctx.locale.getLanguage()) ) {
						hasLocale = true;
						break;
					}
				}
				if( !hasLocale ) {
					ctx.sessionMng.close();
					Locale locale = new java.util.Locale( supportLocales[0] );
					ctx.msghandler = systemConfig.getMessageHandler( ctx.locale = locale );

					ctx.sessionMng = systemConfig.createSessionManager( req, ctx.msghandler );
					if( ctx.sessionMng == null )
						throw new ServletModelException( ServletModelException.INVALID_SESSION );
				}

				if( ctx.sessionMng.getUniqId() != null ) {
					org.slf4j.MDC.put("uniqId", ctx.sessionMng.getUniqId());
				}

				req.setAttribute( "sessionMng", ctx.sessionMng );
			} catch( SessionManagerException sessionEx ) {
				if( sessionEx.getErrorKey() == null ) {
					Throwable throwable = sessionEx.getCause();
					if( throwable instanceof SQLException ) throw (SQLException)throwable;
					if( throwable instanceof IOException ) throw (IOException)throwable;
					throw new ServletException( throwable );
				}
				throw new ServletException( sessionEx );
			}

			// ctx.handler & setAttribute("handler") & setAttribute("queryStorage")
			ctx.handler = systemConfig.createSQLHandler( ctx.msghandler, ctx.sessionMng.getTimeZone() );
			if ( ctx.mode.length() > DEFAULT_MAX_MODESIZE )
				throw new ServletModelException( ServletModelException.INVALID_MODE );
			if( ctx.handler == null )
				throw new ServletModelException( ServletModelException.INVALID_DATAHANDLER );
			if( "Y".equals(ctx.req.getParameter(PARAM_DEBUGSQL)) && ctx.sessionMng.isSystemAdmin() )
				ctx.handler.enableDebugging();
			if( ctx.handler.debugging() )
				ctx.req.setAttribute( "queryStorage", ctx.handler.getSavedQuery() );
			req.setAttribute( "handler", ctx.handler );

			success = true;

			return ctx;
		} finally {
			if( !success ) {
				try { if( ctx.handler != null ) ctx.handler.close(); } catch( Exception ignored ) {}
				try { if( ctx.sessionMng != null ) ctx.sessionMng.close(); } catch( Exception ignored ) {}
			}
		}
	}

	protected boolean defaultReq( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean downloadInput( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	/**
	 * 다음 순서로 요청 처리.
	 * 1. ctx = {@link #createContext(HttpServletRequest, HttpServletResponse) createContext}createContext( req, res );
	 * 2. {@link #checkAuthorize(Context) checkAuthorize}( ctx );
	 * 3. {@link #doRequest(Context, boolean) doRequest}( ctx, isPost );
	 */
	void execute( HttpServletRequest req, HttpServletResponse res, boolean isPost ) throws IOException, ServletException {
		String mode = req.getParameter( PARAM_MODE );
		String requestName = ( mode == null ? getClass().getName() : getClass().getName() +"."+ mode );
		org.slf4j.MDC.put("requestName", requestName);

		long elapsedTimeMilli = System.currentTimeMillis();
		try {
			Context ctx = null;
			String errorKey = null;
			String message = null;
			boolean sessionError = false;
			try {
				// ctx 생성 및 ctx.pageConfig 설정
				ctx = createContext(req, res);
				logger.info( requestName +" start." );
				initContext( ctx );
				req.setAttribute( "msghandler", ctx.msghandler );
				req.setAttribute( "htmlpage", ctx.pageConfig.getHtmlPage() );
				req.setAttribute( "property", ctx.pageConfig.getProperty() );

				requestName = getClass().getName() +"."+ ctx.mode;

				if( !checkAuthorize(ctx) || !doRequest(ctx, isPost) )
					throw new ServletModelException( ServletModelException.HAS_NOAUTH );

				ctx.handler.commit();
			} catch( ServletModelException servletEx ) {
				errorKey = servletEx.getErrorKey();
				message = servletEx.getMessage();

				Throwable throwable = servletEx.getRootCause();
				if( errorKey == null ) {
					if( throwable instanceof SessionManagerException ) {
						errorKey = ((SessionManagerException)throwable).getErrorKey();
						if( errorKey == null )
							throwable = throwable.getCause();
						else
							sessionError = SessionManagerException.isSessionError( errorKey );
					}

					if( errorKey == null ) {
						errorKey = ServletModelException.INTERNAL_ERROR;
						if( throwable instanceof SQLException ) throw (SQLException)throwable;
						if( throwable instanceof IOException ) throw (IOException)throwable;
						throw new ServletException( throwable );
					}
				}

				if( ctx != null && ctx.handler.debugging() && ctx.handler.getSavedQuery().getSavedQuerySize() > 0 )
					ctx.req.setAttribute( "queryStorage", ctx.handler.getSavedQuery() );

				if( message == null || message.equals(errorKey) ) {
					MessageHandler msghandler;
					if( ctx != null && ctx.msghandler != null )
						msghandler = ctx.msghandler;
					else
						msghandler = systemConfig.getMessageHandler( ServletUtility.getLocale(req) );
					message = msghandler.getMessage( errorKey );
				}
				logger.info( requestName +" error: ["+ errorKey +"] " + message + " at: "
						+ (servletEx.getStackTrace() == null ? "" : servletEx.getStackTrace()[0]) );

				if( ctx != null && (( (com.irt.rbm.RBMSystem.getSystemEnvBool("SYS", "DebugOption;verboseThrow", false)
						&& !"N".equals(getSystemConfig().getProperty("DebugOption.verboseThrow")))
						|| "Y".equals(getSystemConfig().getProperty("DebugOption.verboseThrow"))
						&& ctx.sessionMng.isSystemAdmin() )
						||
						com.irt.rbm.RBMSystem.getSystemEnvBool("SYS", "DebugOption."+ctx.sessionMng.getUniqId()+";verboseThrow", false))
						) {
					List<Map<String, Object>> errorList = (List<Map<String, Object>>)ctx.req.getAttribute("errors");
					if( errorList == null || errorList.size() == 0 ) {
						if( errorList == null )
							errorList = new ArrayList<Map<String,Object>>();
						Map map = com.irt.data.Record.createMap("name", "errorKey");
						map.put("message", errorKey);
						errorList.add( map );
						map = com.irt.data.Record.createMap("name", "method");
						map.put("message", ctx.req.getMethod());
						errorList.add( map );
						map = com.irt.data.Record.createMap("name", "url");
						map.put("message", ctx.req.getRequestURL());
						errorList.add( map );
						map = com.irt.data.Record.createMap("name", "params");
						map.put("message", (new ParameterMap(ctx.req)));
						errorList.add( map );
						map = com.irt.data.Record.createMap("name", "pageConfig.backURL");
						map.put("message", ctx.pageConfig.getBackURL());
						errorList.add( map );
						map = com.irt.data.Record.createMap("name", "stacktrace");
						map.put("message", Arrays.toString(servletEx.getStackTrace()));
						errorList.add( map );
						if( throwable != null ) {
							if( throwable instanceof com.irt.data.DataException ) {
								com.irt.data.DataException dataEx = (DataException)throwable;
								if( dataEx.getRecordMap() != null ) {
									map = com.irt.data.Record.createMap("name", "recordMap");
									map.put("message", dataEx.getRecordMap());
									errorList.add( map );
								}
							}
							map = com.irt.data.Record.createMap("name", "throwable");
							map.put("message", Arrays.toString(throwable.getStackTrace()));
							errorList.add( map );
						}
					}
					ctx.req.setAttribute("errors", errorList);
				}

				printErrorPage( req, res, errorKey, message, sessionError );
			} finally {
				if( ctx != null ) {
					try {
						if( ctx.pageConfig != null && !sessionError ) {
							ctx.sessionMng.updateLastAccess(
									ctx.pageConfig.getSystemCode(), ctx.pageConfig.getPackageCode(), getClass().getName(), ctx.mode
									, ctx.pageConfig.getHtmlPage().getTitle(), errorKey == null ? "OK" : "ER", errorKey
											, System.currentTimeMillis() - elapsedTimeMilli
											, ctx.pageConfig.getHtmlPage().getUserAgentString()
									);
						}
					} catch( SessionManagerException sessionEx ) {
						Throwable throwable = sessionEx.getCause();
						if( throwable != null ) {
							if( throwable instanceof SQLException ) logger.error( requestName +" internal error.", throwable );
							if( throwable instanceof IOException ) throw (IOException)throwable;
							throw new ServletException( throwable );
						}
					} finally {
						char resultLevel = ctx.pageConfig.getResultLevel();
						if( resultLevel == HtmlPage.PAGE_RESULT_NULL ) {
							resultLevel = errorKey != null ? HtmlPage.PAGE_RESULT_ERROR : HtmlPage.PAGE_RESULT_SUCCESS;
						}
						ctx.pageConfig.setResultLevel( resultLevel );

						ctx.sessionMng.close();
						ctx.handler.rollback();
						ctx.handler.close();
					}
				}

				logger.info( requestName +" end." );
			}
		} catch( SQLException sqlEx ) {
			logger.error( requestName +" internal error.", sqlEx );
			printErrorPage( req, res, sqlEx );
		} catch( IOException ioEx ) {
			logger.error( requestName +" exception throw.", ioEx );
			throw ioEx;
		} catch( RuntimeException runtimeEx ) {
			logger.error( requestName +" exception throw.", runtimeEx );
			throw runtimeEx;
		} finally {
			org.slf4j.MDC.remove("requestName");
			org.slf4j.MDC.remove("uniqId");
		}
	}

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException {
		req.setCharacterEncoding( "UTF-8" );
		execute( req, res, false );
	}

	/**
	 * Post형식 중에 ContentType이 "multipart/form-data"인 경에는 MultipartHttpRequest를 변경.
	 */
	@Override
	public void doPost( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException {
		req.setCharacterEncoding( "UTF-8" );

		String type = req.getContentType();
		if( type != null && type.toLowerCase().startsWith("multipart/form-data")) {
			MultipartHttpRequest mreq = null;
			try {
				mreq = new MultipartHttpRequest( req, systemConfig.getTemporaryDirectory(), DEFAULT_MAX_POSTSIZE );
				mreq.setAttribute( "request", mreq );
				execute( mreq, res, true );
			} catch( IOException ioEx ) {
				if( ioEx.getMessage().indexOf("exceeds limit of ") < 0 ) throw ioEx;
				printErrorPage( req, res, ServletModelException.TOO_LARGE_FILE
						, systemConfig.getMessageHandler(ServletUtility.getLocale(req)).getMessage(ServletModelException.TOO_LARGE_FILE), false );
			} finally {
				if( mreq != null ) mreq.close();
			}
		} else
			execute( req, res, true );
	}

	/**
	 * mode별로 기능 수행.
	 */
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_DEFAULT.equals(ctx.mode) )
			return defaultReq( ctx, isPost );
		else if( isPost ) {
			if( MODE_MODIFY.equals(ctx.mode) ) return modify( ctx );
			if( MODE_MULTIMODIFY.equals(ctx.mode) ) return multiUpdate( ctx );
			if( MODE_REGIST.equals(ctx.mode) ) return regist( ctx );
			if( MODE_REMOVE.equals(ctx.mode) ) return remove( ctx );
			if( MODE_UPLOAD.equals(ctx.mode) ) return upload( ctx );

		} else {
			if( MODE_COND.equals(ctx.mode) ) return cond( ctx );
			if( MODE_CONDITION_NAME.equals(ctx.mode) ) return condname( ctx );
			if( MODE_DOWNLOAD.equals(ctx.mode) ) return download( ctx );
			if( MODE_DOWNLOADINPUT.equals(ctx.mode) ) return downloadInput( ctx );
			if( MODE_INFO.equals(ctx.mode) ) return info( ctx );
			if( MODE_LIST.equals(ctx.mode) ) return list( ctx );
			if( MODE_LISTCOUNT.equals(ctx.mode) ) return listCount( ctx );
			if( MODE_MODIFYINPUT.equals(ctx.mode) ) return modifyInput( ctx );
			if( MODE_MULTIMODIFYINPUT.equals(ctx.mode) ) return multiUpdateInput( ctx );
			if( MODE_NAME.equals(ctx.mode) ) return name( ctx );
			if( MODE_REGISTINPUT.equals(ctx.mode) ) return registInput( ctx );
			if( MODE_REMOVE.equals(ctx.mode) ) return remove( ctx );
			if( MODE_SELECT.equals(ctx.mode) ) return select( ctx );
			if( MODE_UPLOADINPUT.equals(ctx.mode) ) return uploadInput( ctx );
		}
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected static boolean forward( Context ctx, String path ) throws IOException, ServletException {
		if( ctx.req.getParameter("json") != null && ctx.req.getParameter("json").length() > 0 ) {
			String[] jsonAs = ctx.req.getParameter("json").split(",");
			ctx.res.setHeader("Content-Type", "application/json");
			return ServletUtility2.jsonResponse(ctx.req, ctx.res , ctx.pageConfig.getMessage() , true, jsonAs);
		}

		javax.servlet.RequestDispatcher rd = ctx.req.getRequestDispatcher(path);
		ctx.pageConfig.cleanXSSProperty();
		HttpServletRequest request = ctx.req;

		if( ctx.req instanceof MultipartHttpRequest )
			request = (HttpServletRequest)((HttpServletRequestWrapper)ctx.req).getRequest();

		rd.forward( new ServletRequestWrapper(request), ctx.res );

		return true;
	}

	protected ColumnList getColumnList( Context ctx, String columnListName, String... optionKeys ) throws ServletException {
		ColumnList columnList;
		String columnListType = ctx.req.getParameter( PARAM_COLUMNLISTTYPE );

		if( columnListType == null && optionKeys.length == 0 )
			columnList = getColumnResourceBundle( ctx ).getColumnList( columnListName );
		else
			columnList = getColumnResourceBundle( ctx ).getColumnList( columnListName, columnListType, optionKeys );
		if( columnList == null ) {
			String message = ctx.msghandler.getMessage( ServletModelException.CANNOT_FIND_COLUMNLIST, columnListName );
			throw new ServletModelException( ServletModelException.CANNOT_FIND_COLUMNLIST, message );
		}

		return columnList;
	}

	protected abstract ColumnResourceBundle getColumnResourceBundle( Context ctx ) throws ServletException;

	protected Object getSummaryColumnList( Context ctx, String columnListName, String... optionKeys ) throws ServletException {
		String columnListType = ctx.req.getParameter( PARAM_COLUMNLISTTYPE );
		ColumnResourceBundle columnResourceBundle = getColumnResourceBundle( ctx );

		ColumnList columnList;
		if( columnListType == null && optionKeys.length == 0 )
			columnList = columnResourceBundle.getColumnList( columnListName );
		else
			columnList = columnResourceBundle.getColumnList( columnListName, columnListType, optionKeys );
		if( columnList != null ) return columnList;

		String postfix = "";
		int idx = columnListName.indexOf( '%' );
		if( idx > 0 ) {
			idx = columnListName.indexOf( '.', idx );
			if( idx > 0 ) {
				postfix = columnListName.substring( idx );
				columnListName = columnListName.substring( 0, idx );
			}
		}

		idx = 0;
		List<ColumnList> columnListArray = new java.util.ArrayList<ColumnList>();
		do {
			++idx;
			if( columnListType == null && optionKeys.length == 0 )
				columnList = columnResourceBundle.getColumnList( columnListName + idx + postfix );
			else
				columnList = columnResourceBundle.getColumnList( columnListName + idx + postfix, columnListType, optionKeys );

			if( columnList != null ) columnListArray.add( columnList );
		} while( columnList != null );

		if( columnListArray.size() == 0 ) {
			String message = ctx.msghandler.getMessage( ServletModelException.CANNOT_FIND_COLUMNLIST, columnListName );
			throw new ServletModelException( ServletModelException.CANNOT_FIND_COLUMNLIST, message );
		} else if( columnListArray.size() == 1 )
			return columnListArray.get(0);
		else
			return columnListArray.toArray( new ColumnList[columnListArray.size()] );
	}

	protected abstract SystemConfig getSystemConfig();

	protected boolean info( Context ctx ) throws IOException, ServletException, SQLException {
		return info( ctx, false );
	}

	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	/**
	 * super.init(config)를 호출하고 systemConfig를 설정.
	 * @see #getSystemConfig
	 */
	@Override
	public void init( javax.servlet.ServletConfig config ) throws ServletException {
		super.init( config );
		systemConfig = getSystemConfig();
		logger = Logger.getLogger( ServletModel.class );
	}

	/**
	 * ctx.pageConfig, ctx.msghandler 등 설정.
	 */
	protected abstract void initContext( Context ctx ) throws ServletException;

	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		return list( ctx, true );
	}

	protected boolean list( Context ctx, boolean listing ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean modify( Context ctx ) throws IOException, ServletException, SQLException {
		return update( ctx, false );
	}

	protected boolean modifyInput( Context ctx ) throws IOException, ServletException, SQLException {
		return info( ctx, true );
	}

	protected boolean multiUpdate( Context ctx ) throws IOException, ServletException, SQLException {
		return multiUpdate( ctx, false );
	}

	protected boolean multiUpdate( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean multiUpdateInput( Context ctx ) throws IOException, ServletException, SQLException {
		return multiUpdate( ctx, true );
	}

	protected boolean name( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected static Map<String, Object> popConditionMap( Context ctx ) throws ServletException {
		try {
			String conditionKey = ctx.req.getParameter( PARAM_CONDITION_KEY );
			if( conditionKey == null )
				throw new ServletModelException( ServletModelException.INVALID_REQUEST );

			return (Map<String, Object>)ServletUtility.popTemporaryObject( ctx.req, conditionKey );
		} catch( java.util.NoSuchElementException elementEx ) {
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );
		}
	}

	protected abstract void printErrorPage( HttpServletRequest req, HttpServletResponse res, String errorKey, String message, boolean sessionError )
			throws IOException, ServletException;

	protected abstract void printErrorPage( HttpServletRequest req, HttpServletResponse res, Throwable throwable )
			throws IOException, ServletException;

	protected static String pushConditionMapAndSetListIndexVariables( Context ctx, Map<String, Object> conditionMap, Collection records ) {
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		if( records == null ) {
			if( idxVars[0] == 0 ) {
				idxVars[2] = 0;
				idxVars[3] = 0;
				return null;
			}
		} else if( idxVars[1] < 0 || records.size() < idxVars[1] ) {
			idxVars[2] = idxVars[0] + records.size();
			idxVars[3] = records.size();
			return null;
		}

		return ServletUtility.pushTemporaryObject( ctx.req, "condition", conditionMap );
	}

	protected boolean regist( Context ctx ) throws IOException, ServletException, SQLException {
		return update( ctx, true );
	}

	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected static String saveMessage( Context ctx ) {
		return ServletUtility.pushTemporaryObject( ctx.req, PARAM_MESSAGE_KEY, ctx.pageConfig.getMessage() );
	}

	protected boolean select( Context ctx ) throws IOException, ServletException, SQLException {
		return list( ctx, false );
	}

	protected static boolean sendRedirect( Context ctx, String redirectURL ) throws IOException, ServletModelException {
		if( redirectURL.matches("^(?i)(http://.+)|^(?i)(https://.+)|^(?i)(www\\..+)") ) {
			com.irt.custom.SystemConfig systemConfig = com.irt.custom.SystemConfig.getInstance( "RBM" );
			String urlWhiteList = systemConfig.getProperty( "urlWhiteList" );
			if( urlWhiteList != null ) {
				if( !HtmlUtility.checkURL(redirectURL, urlWhiteList.split("\\|")) ) {
					throw new ServletModelException( ServletModelException.INTERNAL_ERROR );
				}
			}
		}

		if( ctx.req.getParameter("json") != null && ctx.req.getParameter("json").length() > 0 ) {
			String[] jsonAs = ctx.req.getParameter("json").split(",");
			jsonAs = com.irt.util.Arrays.append(jsonAs, "_redirectURL_");
			ctx.req.setAttribute("_redirectURL_", redirectURL);
			ctx.res.setHeader("Content-Type", "application/json");
			return ServletUtility2.jsonResponse(ctx.req, ctx.res , ctx.pageConfig.getMessage() , true, jsonAs);
		}
		ctx.res.sendRedirect( redirectURL );
		return true;
	}

	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}
}
