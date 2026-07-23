/*
 *	File Name:	USRUser.java
 *	Version:	2.2.3c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		usr_unique_check.jsp
 *		usr_user_find_account_input.jsp
 *		usr_user_find_account_result.jsp
 *		usr_user_input.jsp
 *		usr_user_list.jsp
 *		usr_user_name.jsp
 *		usr_user_select.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.3c	신규 UI/UX 적용
 *	hankalam	2021/09/30		2.2.3c	resetFindPassword(): 여러개의 이메일이 등록됐을 경우 입력한 이메일로 검색하여 메일 전송되도록 수정
 *	jbaek		2020/12/31		2.2.3c	upload(), download() 추가
 *	hankalam	2020/09/28		2.2.3c	아이디/비밀번호 찾기 기능 findAccountInput(), findUserId(), readFile(), getTitleKey(),
 *										makeRandomString(), resetFindPassword(), sendMail() 추가
 *	jbaek		2019/11/30		2.2.2c	User 기본 조직 기능 추가
 *	lsinji		2008/09/26		2.2.2	setLocationInformation() 삭제, ECS 부분 삭제
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										list(): select일 경우에도 setManageAuth() 수행
 *										update(): 자기정보 수정이 안되는 버그 수정
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경, log4j 사용
 *										createConditionMap(): new ParameterMap(req) -> new ParameterMap(req, true), throws IOException 삭제
 *										listCount(): 저장된 conditionMap이 없을 경우 처리
 *										update(): ServletModelException.NO_RECORD_FOUND 대신 DataException.ERR_NO_RECORD_UPDATE 사용
 *										update(): originalMap을 가져올 때 getRecordWithLock() 사용
 *	stghr12		2007/04/30		2.1.0	com.irt.html.form.ColumnList -> com.irt.data.cols.ColumnList
 *										new DataException() -> ctx.handler.createDataException()
 *										Condition.putConditionValueOnly() 사용
 *										ServletUtility.setSort() 사용
 *										UserUserFieldOption 사용
 *										checkAuthorize(): ServletModel 변경사항 적용
 *										list(): setAttribute("groups") 추가
 *										remove(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/10/14		2.0.1	remove()에서 getBackURL()이 없을 경우 NEEDED_PARAMETER 에러발생
 *	stghr12		2006/06/01		2.0.0	version up
 *	stghr12		2004/06/21		1.0.0	create
 *
 **/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.ManipulableManager;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.rbm.UploadLog;
import com.irt.rbm.usr.*;
import com.irt.dpr.UserEmployee;
import com.irt.dpr.PartyAuth;
import com.irt.servlet.*;
import com.irt.system.SessionManagerException;
import com.irt.util.MapUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/USRUser"})
public class USRUser extends DPRServletModel {
	public final static String MODE_FINDACCOUNT			= "fndacc";
	public final static String MODE_FINDACCOUNTID		= "fndid";
	public final static String MODE_FINDACCOUNTPASSWORD	= "fndpwd";
	public final static String MODE_RESETFINDPASSWORD	= "rsetpwd";
	public final static String MODE_PASSWORD			= "passwd";
	public final static String MODE_CHANGE_USERNAME		= "usrname";
	public final static String MODE_UNIQUECHECK			= "chk";
	public final static String MODE_ORG_CONDITION		= "org";

	private final String USERSTATUS_EXPIRE				= "PW";
	private final String USERSTATUS_LOCK				= "LK";
	private final String USERSTATUS_NORMAL				= "00";

	protected boolean changePassword( Context ctx ) throws IOException, ServletException, SQLException {
		String partyId = ctx.req.getParameter( "partyId" );
		String userId = ctx.req.getParameter( "userId" );
		String presentPassword = ctx.req.getParameter( "presentPassword" );
		String password = ctx.req.getParameter( "password" );

		if( partyId == null || partyId.length() == 0 || userId == null || userId.length() == 0
				|| presentPassword == null || presentPassword.length() == 0 || password == null || password.length() == 0 ) {

			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		if( !ctx.sessionMng.getPartyId().equals(partyId) || !ctx.sessionMng.getUserId().equals(userId) ) {
			if( !ctx.sessionMng.isAuthorized("USR", "USRUser.MNG") ) {
				throw new ServletModelException( ServletModelException.HAS_NOAUTH );
			}
		}

		boolean validPassword = false;
		try {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "partyId", partyId );
			recordMap.put( "userId", userId );
			recordMap.put( "password", password );

			validPassword = com.irt.dpr.ValidablePassword.validableCheckPassword( ctx.handler, recordMap );
			if( !validPassword ) {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_INVALID_PASSWORD") );
			}

			boolean ret = ctx.sessionMng.changePassword( partyId, userId, presentPassword, password );
			if( ret ) {
				ctx.handler.commit();
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			} else {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_INVALID_PASSWORD") );
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
		} catch( SessionManagerException sessionEx ) {
			String errorKey = sessionEx.getErrorKey();
			if( errorKey == null ) throw new ServletModelException( sessionEx.getCause() );
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage(errorKey) );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		}

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	protected boolean changeUserName( Context ctx ) throws ServletModelException, IOException, SQLException {
		UserUser db = (UserUser)ctx.db;
		String partyId = ctx.req.getParameter( "partyId" );
		String userId = ctx.req.getParameter( "userId" );

		if( partyId == null || partyId.length() == 0 || userId == null || userId.length() == 0 ) {
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		if( !ctx.sessionMng.getPartyId().equals(partyId) || !ctx.sessionMng.getUserId().equals(userId) ) {
			if( !ctx.sessionMng.isAuthorized("USR", "USRUser.MNG") ) {
				throw new ServletModelException( ServletModelException.HAS_NOAUTH );
			}
		}
		ParameterMap recordMap = new ParameterMap( ctx.req );

		try {
			if( !db.modify(recordMap, new String[] { "userName" }) )
				throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );

			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		String url = ctx.pageConfig.getBackURL();

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery( url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		try {
			if( MODE_FINDACCOUNT.equals(ctx.mode) || MODE_FINDACCOUNTID.equals(ctx.mode)
					|| MODE_FINDACCOUNTPASSWORD.equals(ctx.mode) || MODE_RESETFINDPASSWORD.equals(ctx.mode) ) {
				return true;
			}
			ctx.sessionMng.checkLogin();
		} catch( SessionManagerException sessionEx ) {
			throw new ServletModelException( sessionEx );
		}

		try {
			if( !( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) ) ) {
				ctx.sessionMng.checkAuthorize( ctx.pageConfig.getSystemCode(), ctx.pageConfig.getPackageCode() );
			}
		} catch( SessionManagerException sessionEx ) {
			throw new ServletModelException( sessionEx );
		}

		return checkAuthorize( ctx, true, false );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		String partyId = (String)ctx.extraObj;
		Condition.putConditionValueOnly( conditionMap, "partyId", partyId );
		if( !ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.getPartyId().equals(partyId) )
			Condition.putConditionValueOnly( conditionMap, "status", "00" );

		if( conditionMap.containsKey("userId") )
			Condition.putConditionValueOnly( conditionMap, "userId", conditionMap.get("userId"), Condition.CONDTYPE_CONTAINS );
		if( conditionMap.containsKey("userName") )
			Condition.putConditionValueOnly( conditionMap, "userName", conditionMap.get("userName"), Condition.CONDTYPE_CONTAINS );
		if( conditionMap.containsKey("soldPartyCodes") )
			Condition.putConditionValueOnly( conditionMap, "soldPartyCodes", conditionMap.get("soldPartyCodes"), Condition.CONDTYPE_CONTAINS );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_FINDACCOUNT.equals(ctx.mode) || MODE_FINDACCOUNTPASSWORD.equals(ctx.mode) ) return findAccountInput( ctx );
		if( MODE_FINDACCOUNTID.equals(ctx.mode) ) return findUserId( ctx );
		if( MODE_RESETFINDPASSWORD.equals(ctx.mode) ) return resetFindPassword( ctx );
		if( MODE_UNIQUECHECK.equals(ctx.mode) ) return uniqueCheck( ctx );
		if( MODE_ORG_CONDITION.equals(ctx.mode) ) return setCountryCondition( ctx );
		if( MODE_CHANGE_USERNAME.equals(ctx.mode) ) return changeUserName( ctx );
		if( isPost ) {
			if( MODE_PASSWORD.equals(ctx.mode)) {
				return changePassword( ctx );
			}
		}

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser) ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		conditionMap.put( "_multiSoldTo", "Y" );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "USRUser.MSP%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String filename = ctx.msghandler.getMessage( "TITLE_USR_USER_MULTISOLDPARTY_DOWN" );
		DataWriter out = createDataWriter( ctx, filename );

		try{
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, UserUser.OPT_WRITING_TITLE );
		} catch ( SQLException sqlEx ){
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error("internal Error", sqlEx );
		}finally {
			out.flush();
			out.close();
		}
		return true;
	}

	protected boolean findAccountInput( Context ctx ) throws IOException, ServletException, SQLException {
		ParameterMap recordMap = (ParameterMap) ctx.req.getAttribute( "record" );
		UserParty db = new UserParty( ctx.handler );
		List<Map<String, Object>> partyList = db.getRecords( null, new String[] { "partyId" } );
		ctx.req.setAttribute( "parties", partyList );

		String partyId = null;
		if( recordMap == null ) {
			recordMap = new ParameterMap( ctx.req );
			ctx.req.setAttribute( "record", recordMap );
		} else {
			partyId = (String) recordMap.get( "idPartyId" );
			if( partyId == null ) {
				partyId = (String) recordMap.get( "pwPartyId" );
			}
		}

		if( partyId == null ) {
			String locale = ctx.locale.getLanguage().toUpperCase();
			if( "EN".equals(locale) || "ZH".equals(locale) || "ZF".equals(locale) ) {
				locale = "CN";
			} else if( "KO".equals(locale) ) {
				locale = "KR";
			} else if( "VI".equals(locale) ) {
				locale = "VN";
			}
			partyId = "JNJAP_" + locale;
		}
		recordMap.put( "idPartyId", partyId );
		recordMap.put( "pwPartyId", partyId );

		return forward( ctx, systemConfig.getJspPath() + "/usr_user_find_account_input.jsp" );
	}

	protected boolean findUserId( Context ctx ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;
		ParameterMap recordMap = new ParameterMap( ctx.req );

		try {
			String partyId = recordMap.getParameter( "idPartyId" );
			String name = recordMap.getParameter( "name" );
			String email = recordMap.getParameter( "idEmail" );

			if( partyId == null || partyId.length() < 1 || name == null || name.length() < 1 || email == null || email.length() < 1 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			String userId = db.getUserId( partyId, name, email );
			if( userId != null ) {
				recordMap.put( "userId", userId );
				ctx.req.setAttribute( "record", recordMap );
				ctx.req.setAttribute( "resultMessage", ctx.msghandler.getMessage("MSG_USR_FINDID_SUCCESS_2", name, userId) );
				return forward( ctx, systemConfig.getJspPath() + "/usr_user_find_account_result.jsp" );
			} else {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_USR_NOT_EXIST_FINDID") );
			}
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}
		ctx.req.setAttribute( "record", recordMap );
		return findAccountInput( ctx );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;
		UserEmployee employeeDB = new com.irt.dpr.UserEmployee( ctx.handler );
		com.irt.dpr.UserMultiEmployee multiempDB = new com.irt.dpr.UserMultiEmployee( ctx.handler );
		boolean multiSoldTo = RBMSystem.getSystemEnvBool( "DPR", (ctx.sessionMng.getPartyId()+";multiSoldTo"), "JNJAP_CN".equals(ctx.sessionMng.getPartyId()) );

		// recordMap 읽기 & setAttribute
		Map<String, Object> recordMap = null;
		String uniqId = ctx.req.getParameter( "uniqId" );
		String countryCode = null;
		if( uniqId != null && uniqId.length() > 0 ) {
			recordMap = db.getRecord( uniqId );
			if( recordMap == null )
				throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
			countryCode = db.getUserGln( uniqId );
		} else {
			String partyId = ctx.req.getParameter( "partyId" );
			String userId = ctx.req.getParameter( "userId" );

			if( partyId == null || partyId.length() == 0 ) {
				partyId = ctx.sessionMng.getPartyId();
				if( userId == null || userId.length() == 0 )
					userId = ctx.sessionMng.getUserId();
			} else if( userId == null || userId.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			recordMap = db.getRecord( UserUser.createPrimary(partyId, userId) );
			if( recordMap == null )
				throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

			countryCode = db.getUserGln( partyId, userId );
		}
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldOption", UserUserFieldOption.getInstance(ctx.sessionMng.getUserClass()) );

		multiempDB.setSort("organizationCode");
		employeeDB.setSort("organizationCode");
		// setAttribute UserEmployee
		if( multiSoldTo )
			ctx.req.setAttribute( "organizations", multiempDB.getRecords(uniqId, countryCode, true) );
		else
			ctx.req.setAttribute( "organizations", employeeDB.getRecords(uniqId, countryCode, true) );

		// forward
		if( ctx.sessionMng.getUniqId().equals(recordMap.get("uniqId")) ) {
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("USR", "USRUser.INF") );
		} else if( ctx.sessionMng.isAuthorized("USR", "USRUser.MNG") ) {
			if( ctx.sessionMng.isSystemAdmin() )
				ctx.pageConfig.setManageAuth( true );
			else if( ctx.sessionMng.isPartyAdmin() && ctx.sessionMng.getPartyId().equals(recordMap.get("partyId")) ) {
				if( !UserUser.USERCLASS_SYSTEM_ADMIN.equals(recordMap.get("userClass")) )
					ctx.pageConfig.setManageAuth( true );
			}
		}

		setPath( ctx, "jsp.SUBMENU_USERINFO" );

		if( inputting ) {
			if( !ctx.pageConfig.hasManageAuth() ) return false;
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			if( ctx.sessionMng.isSystemAdmin() )
				ctx.req.setAttribute( "groups", (new UserGroup(ctx.handler)).getRecords() );
			else
				ctx.req.setAttribute( "groups", (new UserGroup(ctx.handler)).getRecords((String)recordMap.get("partyId")) );

			return forward( ctx, systemConfig.getJspPath() + "/usr_user_info.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );
		ctx.db = new UserUser( ctx.handler );

		// partyId, selfId, ctx.extraObj
		String partyId = ctx.req.getParameter( "partyId" );
		boolean selfId = ( partyId == null || partyId.length() == 0 || partyId.equals(ctx.sessionMng.getPartyId()) );
		if( MODE_INFO.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) ) {
			String uniqId = ctx.req.getParameter( "uniqId" );
			if( uniqId != null && uniqId.length() > 0 ) {
				try {
					partyId = ((UserUser)ctx.db).getPartyId( uniqId );
				} catch( SQLException sqlEx ) {}
				selfId = ( partyId == null || partyId.equals(ctx.sessionMng.getPartyId()) );
			}
		}
		ctx.extraObj = ( selfId ? ctx.sessionMng.getPartyId() : partyId );

		// setSystemPackageCode, partyId
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", selfId ? "USRUser.INF" : "USRUser.INF_ALL" );
		else if( MODE_SELECT.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", selfId ? "USRUser.SEL" : "USRUser.SEL_ALL" );
		else if( MODE_LIST.equals( ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", selfId ? "USRUser.LST" : "USRUser.LST_ALL" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", selfId ? "USRUser.LST" : "USRUser.LST_ALL" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", selfId ? "USRUser.LST" : "USRUser.LST_ALL" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRUser.MNG" );
		else if( MODE_ORG_CONDITION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRUser.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) || MODE_UNIQUECHECK.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRUser.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRUser.MNG" );
		else if( MODE_PASSWORD.equals(ctx.mode) || MODE_CHANGE_USERNAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", null );
		else if( MODE_FINDACCOUNT.equals(ctx.mode) || MODE_FINDACCOUNTID.equals(ctx.mode)
				|| MODE_FINDACCOUNTPASSWORD.equals(ctx.mode) || MODE_RESETFINDPASSWORD.equals(ctx.mode) ) {
			if( !RBMSystem.getSystemEnvBool( "USR", "SecurityPolicy;PasswordResetable", false ) ) {
				throw new ServletModelException( "ERR_DEACTIVATE_PASSWORDRESETABLE" );
			}
			pageConfig.setSystemPackageCode( "USR", null );
		} else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String messageKey = "TITLE_USR_USER_" + ctx.mode.toUpperCase();
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		setPath( ctx, "jsp.MENU_USER" );
	}

	@Override
	protected boolean list( Context ctx, boolean listing ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;
		boolean multiSoldTo = RBMSystem.getSystemEnvBool( "DPR", (ctx.sessionMng.getPartyId()+";multiSoldTo"), "JNJAP_CN".equals(ctx.sessionMng.getPartyId()) );

		// conditionMap 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );
		String partyId = (String)conditionMap.get( "partyId" );
		if( partyId != null ) {
			if( !ctx.sessionMng.getPartyId().equals(partyId) )
				ctx.req.setAttribute( "record_pty", (new com.irt.rbm.usr.UserParty(ctx.handler)).getRecord(conditionMap) );
			ctx.req.setAttribute( "groups", (new UserGroup(ctx.handler)).getRecords(partyId) );
		}

		if( multiSoldTo ) conditionMap.put( "_multiSoldTo", "Y" );

		// columnList, records 읽기 & conditionMap 저장
		List recordList = null;
		com.irt.data.cols.ColumnList columnList = null;
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		if( listing ) {
			columnList = getColumnList( ctx, "USRUser%LIST", (multiSoldTo? "MSP" : null) );
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		} else {
			if( partyId == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			ServletUtility.setSort( ctx.req, db, "userName" );
			recordList = db.getRecords( conditionMap, new String[] { "uniqId", "partyId", "userId", "userName" }, idxVars[0], idxVars[1] );
		}
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		if( ctx.sessionMng.isAuthorized("USR", "USRUser.MNG") ) {
			if( ctx.sessionMng.isSystemAdmin() )
				ctx.pageConfig.setManageAuth( true );
			else if( ctx.sessionMng.isPartyAdmin() )
				ctx.pageConfig.setManageAuth( ctx.sessionMng.getPartyId().equals(partyId) );
		}

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_USER_MANAGEMENT") );
		ctx.pageConfig.setSubTitle( ctx.msghandler.getMessage("TITLE_USR_USER_" + ctx.mode.toUpperCase()) );
		setPath( ctx, "TITLE_USR_USER_MANAGEMENT" );

		if( listing )
			return forward( ctx, systemConfig.getJspPath() + "/usr_user_list.jsp" );
		else
			return forward( ctx, systemConfig.getJspPath() + "/usr_user_select.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("USR", "USRUser.LST") && !ctx.sessionMng.isAuthorized("USR", "USRUser.SEL") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((UserUser)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	/**
	 * 원하는 길이만큼 임의의 문자열 생성 (특수문자 포함)
	 *
	 * @param length 생성할 문자열의 길이
	 * @return 임의로 생성한 문자열
	 */
	private String makeRandomString( int length ) {
		StringBuffer strValue = new StringBuffer();
		for( char ch = '0'; ch <= 'z'; ch++ ) {
			strValue = strValue.append( ch );
		}

		java.util.Random random = new java.util.Random();
		StringBuffer randomStrBuffer = new StringBuffer();

		for( int i = 0; i < length; i++ ){
			int nRandom = random.nextInt( strValue.length() );
			randomStrBuffer.append( strValue.substring(nRandom, nRandom + 1) );
		}

		return randomStrBuffer.toString();
	}

	@Override
	protected boolean name( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = createConditionMap( ctx );

		if( conditionMap.get("uniqId") != null || (conditionMap.get("partyId") != null && conditionMap.get("userId") != null) ) {
			List recordList = ((UserUser)ctx.db).getRecords( conditionMap, 0, 2 );
			if( recordList != null && recordList.size() == 1 ) ctx.req.setAttribute( "record", recordList.get(0) );
		}

		return forward( ctx, systemConfig.getJspPath() + "/usr_user_name.jsp" );
	}

	/**
	 * 파일 내용 라인단위로 읽어오기.
	 *
	 * @param filePath	읽어올 파일의 절대경로
	 * @return 파일 내용 읽어온 문자열, 파일이 없는 경우나 존재하는데 빈파일인 경우 null 을 반환.
	 * @throws IOException
	 */
	private String readFile( String filePath ) throws IOException {
		StringBuffer fileBuffer = new StringBuffer();
		BufferedReader fileReader = null;

		try {
			fileReader = new BufferedReader( new java.io.FileReader(filePath) );
			String readLine = null;

			do {
				readLine = fileReader.readLine();
				if( readLine == null ) {
					break;
				}
				fileBuffer.append( readLine );
			} while( true );

			if( fileBuffer.length() == 0 ) {
				throw new FileNotFoundException();
			}
		} catch( FileNotFoundException fnEx ){
			logger.error( "error.", fnEx );
			return null;
		} finally {
			if( fileReader != null ){
				fileReader.close();
			}
		}
		return fileBuffer.toString();
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;
		UserUserFieldOption fieldOption = UserUserFieldOption.getInstance( ctx.sessionMng.getUserClass() );

		Map<String, Object> recordMap = (Map<String, Object>)ctx.req.getAttribute("record");
		if( recordMap == null ) {
			String partyId = null;
			if( ctx.sessionMng.isSystemAdmin() )
				partyId = (String)ctx.extraObj;
			else if( ctx.sessionMng.isPartyAdmin() )
				partyId = ctx.sessionMng.getPartyId();
			else
				return false;

			recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "partyId", partyId );
			recordMap.put( "partyName", (new com.irt.rbm.usr.UserParty(ctx.handler)).getPartyName(partyId) );
			recordMap.put( "extraValue1", ctx.sessionMng.getGln() );
			recordMap.put( "userClass", UserUser.USERCLASS_USER );
			recordMap.put( "availAccessCount", fieldOption.getDefaultValue( "availAccessCount" ) );
			recordMap.put( "status", "00" );
			recordMap.put( "extraValue2", "en" );
			ctx.req.setAttribute( "record", recordMap );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
			ctx.req.setAttribute( "fieldOption", fieldOption );
		}
		setAttributeMaster( ctx, MASTER_LANGUAGE );

		// Organization Setting
		List<Map<String, Object>> organizations = (List<Map<String, Object>>)ctx.req.getAttribute( "organizations" );
		if( organizations == null || organizations.size() == 0 ) {
			Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
			conditionMap.put( "countryCode", ctx.sessionMng.getGln() );

			setAttributePartyMasterOnExisting( ctx, conditionMap, PARTYMASTER_ORGANIZATION );
		}

		if( ctx.sessionMng.isSystemAdmin() )
			ctx.req.setAttribute( "groups", (new UserGroup(ctx.handler)).getRecords() );
		else
			ctx.req.setAttribute( "groups", (new UserGroup(ctx.handler)).getRecords((String)recordMap.get("partyId")) );
		ctx.req.setAttribute( "parties", (new UserParty(ctx.handler)).getRecords(null) );

		return forward( ctx, systemConfig.getJspPath() + "/usr_user_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;

		String partyId = null;
		String[] userIds = ctx.req.getParameterValues( "userId" );
		if( ctx.sessionMng.isSystemAdmin() )
			partyId = (String)ctx.extraObj;
		else if( ctx.sessionMng.isPartyAdmin() )
			partyId = ctx.sessionMng.getPartyId();
		else
			return false;
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( partyId == null || userIds == null || userIds.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// primaryMap 생성 및 partyId 검사
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();
		primaryMap.put( "partyId", partyId );
		boolean selfId = ctx.sessionMng.getPartyId().equals( partyId );

		// 레코드 삭제
		int count = 0;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < userIds.length; i++) {
			try {
				primaryMap.put( "userId", userIds[i] );
				if( selfId && ctx.sessionMng.getUserId().equals(userIds[i]) ) {
					String message = ctx.msghandler.getMessage( "MSG_USR_CANNOT_DELETE_SELFID" );
					errorList.add( createErrorMap(db.getFieldValue(primaryMap, "userName"), message) );
				} else if( !ctx.sessionMng.isSystemAdmin() && UserUser.USERCLASS_SYSTEM_ADMIN.equals(db.getFieldValue(primaryMap, "userClass")) ) {
					String message = ctx.msghandler.getMessage( "MSG_USR_CANNOT_DELETE_SYSTEMADMIN" );
					errorList.add( createErrorMap(db.getFieldValue(primaryMap, "userName"), message) );
				} else if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "userName"), dataEx) );
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	protected boolean resetFindPassword( Context ctx ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;
		ParameterMap recordMap = new ParameterMap( ctx.req );

		try {
			ctx.req.setAttribute( "record", recordMap );
			String partyId = recordMap.getParameter( "pwPartyId" );
			String userId = recordMap.getParameter( "userId" );
			String userEmail = recordMap.getParameter( "pwEmail" );

			if( partyId == null || userId == null || userEmail == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			if( !db.existId(partyId, userId) )
				throw ctx.handler.createDataException( ctx.handler.getMessageHandler().getMessage("MSG_USR_NOT_EXIST_FINDID") );

			String dbUserEmail = db.getUserEmail( partyId, userId );
			if( dbUserEmail == null )
				throw ctx.handler.createDataException( ctx.handler.getMessageHandler().getMessage("MSG_USR_NOT_EXIST_EMAIL", userId) );
			else {
				String[] emailList =  dbUserEmail.split( "," );
				boolean exist = false;
				for( String email : emailList ) {
					if( userEmail.equals(email.trim()) ) {
						exist = true;
						break;
					}
				}
				if( !exist ) {
					throw ctx.handler.createDataException( ctx.handler.getMessageHandler().getMessage("MSG_USR_NOT_IDENTICAL_EMAIL") );
				}
			}

			String userStatus = db.getUserStatus( partyId, userId );
			if( !USERSTATUS_NORMAL.equals(userStatus) && !USERSTATUS_EXPIRE.equals(userStatus) && !USERSTATUS_LOCK.equals(userStatus) )
				throw ctx.handler.createDataException( ctx.handler.getMessageHandler().getMessage("MSG_USR_STOPPED_STATUS") );
			if( !db.checkAvailableUser(partyId, userId) )
				throw ctx.handler.createDataException( ctx.handler.getMessageHandler().getMessage("MSG_USR_INVALID_USER") );

			int passwordLength = RBMSystem.getSystemEnvInt( "USR", "SecurityPolicy;MinimumPasswordLength", 12 );
			String randomPassword = makeRandomString( passwordLength );

			// 언어별 경로에 템플릿파일이 존재하지 않으면 기본 언어의 경로에서 템플릿을 가져옴. (Default : en)
			String filePath = systemConfig.getProperty( "mailTemplatePath" );
			File file = new File( filePath, systemConfig.getProperty("mailTemplateFileName") );
			filePath = file.getAbsolutePath();
			/*
			if( !file.isFile() ) {
				filePath = filePath.replace( "\\" + ctx.locale.getLanguage() + "\\", "\\en\\" );
			}
			 */
			String mailContents = readFile( filePath );
			if( mailContents == null )
				throw new ServletModelException( "MSG_USR_CANNOT_LOAD_EMAILTEMPLATE" );

			mailContents = mailContents.replace( "%MSG_TITLE%", ctx.msghandler.getMessage("MSG_RESETPASSWORD_TITLE") );
			mailContents = mailContents.replace( "%MSG_ID%", ctx.msghandler.getMessage("FIELD_USERID") );
			mailContents = mailContents.replace( "%MSG_PW%", ctx.msghandler.getMessage("FIELD_USR_PASSWORD") );
			mailContents = mailContents.replace( "%USER_ID%", userId );
			mailContents = mailContents.replace( "%NEW_PASSWORD%", HtmlUtility.toHtmlString(randomPassword) );

			db.encodePassword( partyId, userId, randomPassword );
			db.updateStatus( partyId, userId, USERSTATUS_EXPIRE );

			Map<String, Object> sendMailMap = new java.util.HashMap<String, Object>();
			sendMailMap.put( "host", systemConfig.getProperty("mailHost") );
			sendMailMap.put( "fromAddress", systemConfig.getProperty("fromAddress") );
			sendMailMap.put( "toAddress", userEmail );
			sendMailMap.put( "subject", ctx.msghandler.getMessage("MSG_USR_FINDPASSWORD_SEND_MAIL_SUBJECT", ctx.msghandler.getMessage("MSG_DPORTAL")) );
			sendMailMap.put( "port", systemConfig.getProperty("mailPort") );
			sendMailMap.put( "id", systemConfig.getProperty("mailHostId") );
			sendMailMap.put( "pw", systemConfig.getProperty("mailHostPw") );
			sendMailMap.put( "contents", mailContents );
			if( !com.irt.util.Utility.sendMail(sendMailMap, logger) )
				throw ctx.handler.createDataException( ctx.handler.getMessageHandler().getMessage("MSG_USR_FINDPASSWORD_SEND_MAIL_FAIL") );

			ctx.req.setAttribute( "resultMessage", ctx.msghandler.getMessage("MSG_USR_TRANSPORT_SEND_MAIL_SUCCESS", userEmail) );
			return forward( ctx, systemConfig.getJspPath() + "/usr_user_find_account_result.jsp" );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		return findAccountInput( ctx );
	}

	protected boolean setCountryCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();

		String partyId = ctx.req.getParameter( "partyId" );
		if( partyId == null || partyId.length() == 0 )
			return true;

		conditionMap.put( "countryCode", new com.irt.rbm.usr.UserParty(ctx.handler).getPartyGln(partyId) );
		setAttributePartyMasterOnExisting( ctx, conditionMap, PARTYMASTER_ORGANIZATION );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_user_org.jsp" );
	}

	protected boolean uniqueCheck( Context ctx ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;

		String partyId = null;
		String userId = ctx.req.getParameter( "userId" );
		if( ctx.sessionMng.isSystemAdmin() )
			partyId = (String)ctx.extraObj;
		else if( ctx.sessionMng.isPartyAdmin() )
			partyId = ctx.sessionMng.getPartyId();
		else
			return false;
		if( partyId == null || userId == null || userId.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = UserUser.createPrimary( partyId, userId );
		Map<String, Object> recordMap = db.getRecord( primaryMap, new String[] { "userId", "userName" } );
		if( recordMap == null ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_USR_NOT_EXIST_ID") );
			ctx.pageConfig.setProperty( "validable", "Y" );
			recordMap = primaryMap;
		} else
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_USR_EXIST_ID") );
		ctx.req.setAttribute( "record", recordMap );
		ctx.pageConfig.setProperty( "fieldKey", "userId" );

		return forward( ctx, systemConfig.getJspPath() + "/usr_unique_check.jsp" );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		UserUser db = (UserUser)ctx.db;
		UserEmployee employeeDB = new com.irt.dpr.UserEmployee( ctx.handler );
		com.irt.dpr.UserMultiEmployee multiempDB = new com.irt.dpr.UserMultiEmployee( ctx.handler );
		PartyAuth authDB = new com.irt.dpr.PartyAuth( ctx.handler );
		boolean multiSoldTo = RBMSystem.getSystemEnvBool( "DPR", (ctx.sessionMng.getPartyId()+";multiSoldTo"), "JNJAP_CN".equals(ctx.sessionMng.getPartyId()) );
		UserUserFieldOption fieldOption = UserUserFieldOption.getInstance( ctx.sessionMng.getUserClass() );
		// 레코드 읽기
		ParameterMap recordMap = new ParameterMap( ctx.req );
		if( ctx.sessionMng.getPartyId().equals(recordMap.get("partyId")) ) {
			if( ctx.sessionMng.getUserId().equals(recordMap.get("userId")) ) {
				if( !ctx.sessionMng.isAuthorized("USR", "USRUser.INF") )
					return false;
			} else if( ctx.sessionMng.isAdminUser() ) {
				if( !ctx.sessionMng.isAuthorized("USR", "USRUser.MNG") )
					return false;
			} else
				return false;
		} else if( !ctx.sessionMng.isSystemAdmin() )
			return false;

		String type = recordMap.getParameter( "type" );

		Map<String, Object> originalMap = null;
		String uniqId = recordMap.getParameter("userId") +"@"+ recordMap.getParameter("partyId");
		recordMap.put( "uniqId", uniqId.length() > 30 ? uniqId.substring(0, 30) : uniqId );

		boolean validPassword = false;
		try {
			validPassword = com.irt.dpr.ValidablePassword.validableCheckPassword( ctx.handler, recordMap );
		} catch( DataException dataEx ) {
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
		}

		if( !validPassword ) {
			String mode = (inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_USER_"+ mode.toUpperCase()) );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INPUT );

			ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );
			ctx.req.setAttribute( "record", recordMap );
			ctx.req.setAttribute( "fieldOption", fieldOption );

			String url = ctx.pageConfig.getBackURL();
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery( url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}

		String password = UserUser.getEncryptionPassword( (String)recordMap.get("password") );
		if( password != null )
			recordMap.put( "password", password );

		String usrDefaultSorg = (String)recordMap.get( "extraValue4" );

		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );
		ctx.req.setAttribute( "fieldOption", fieldOption );
		if( inserting ) {
			fieldOption.applyOptionToRecordMap( recordMap, null );
		} else {
			try {
				String[] fieldKeys = fieldOption.getFieldKeys();
				fieldKeys = com.irt.util.Arrays.append( fieldKeys, new String[] { "uniqId", "password", "userId", "partyId" } );
				originalMap = db.getRecordWithLock( recordMap, fieldKeys, true );
			} catch( DataException dataEx ) {}

			if( originalMap != null ) {
				if( !"info".equals(type) && ctx.sessionMng.getUniqId().equals(originalMap.get("uniqId")) ) {
					String passwd = UserUser.getEncryptionPassword( recordMap.getParameter("presentPassword") );

					if( passwd == null || !passwd.equals(originalMap.get("password")) ) {
						ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_USR_INVALID_PASSWORD") );
						ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_USER_"+ MODE_MODIFYINPUT.toUpperCase()) );
						String url = ctx.pageConfig.getBackURL();
						return sendRedirect( ctx, HtmlUtility.replaceURLQuery( url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
					}
				} else if( !ctx.sessionMng.isSystemAdmin() ) {
					if( UserUser.USERCLASS_SYSTEM_ADMIN.equals(originalMap.get("userClass")) ) {
						return false;
					}
				}
			}

			fieldOption.applyOptionToRecordMap( recordMap, originalMap );
		}
		if( usrDefaultSorg != null && usrDefaultSorg.length() > 0 ) {
			recordMap.put( "extraValue4", usrDefaultSorg );
		}

		String countryCode = (String)recordMap.get( "extraValue1" );
		if( countryCode == null || countryCode.length() == 0 ) {
			if( countryCode == null || countryCode.length() == 0 ) {
				String partyId = (String)recordMap.get( "partyId" );
				if( ctx.sessionMng.getPartyId().equals(partyId) )
					countryCode = ctx.sessionMng.getGln();
				else if( partyId != null && partyId.length() > 0 )
					countryCode = (new com.irt.rbm.usr.UserParty(ctx.handler)).getPartyGln( partyId );
				else
					throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

				recordMap.put( "extraValue1", countryCode );
			}
		}

		List<Map<String, Object>> employeeList = new java.util.ArrayList<Map<String, Object>> ();
		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );
		String[] employeeIds = ctx.req.getParameterValues( "employeeId" );
		if( employeeIds != null && employeeIds.length > 0 ) {
			for( int i = 0 ; i < employeeIds.length; i++ ) {
				if( employeeIds[i] != null && employeeIds[i].length() > 0 ) {
					Map<String, Object> _record = new java.util.HashMap<String, Object> ();
					_record.put( "uniqId", uniqId );
					_record.put( "organizationCode", organizationCodes[i] );
					_record.put( "employeeId", employeeIds[i] );
					_record.put( "countryCode", countryCode );
					_record.put( "updateUserId", ctx.sessionMng.getUniqId() );

					employeeList.add( _record );
				}
			}
		}
		ctx.req.setAttribute( "organizations", employeeList );

		try {
			Map<String, Object> authConditionMap = new java.util.HashMap<String, Object> ();
			authConditionMap.put( "divisionCode", getDivisionCode(ctx) );
			authConditionMap.put( "countryCode", countryCode );
			authConditionMap.put( "uniqId", uniqId );

			if( inserting ) {
				db.regist( recordMap );

				if( multiSoldTo )
					multiempDB.update( employeeList, recordMap.get("uniqId"), inserting );
				else
					employeeDB.update( employeeList, recordMap.get("uniqId"), inserting );
				if( ( multiSoldTo && authDB.updateWithEmployeeIdCsv(employeeList, authConditionMap) )
						|| authDB.updateWithEmployeeId( employeeList, authConditionMap ) )
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
				else {
					ctx.handler.rollback();
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_PUB_SUCCESS_INC_ERROR") );
					ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_USER_"+ MODE_REGISTINPUT.toUpperCase()) );
					ctx.req.setAttribute( "focus", "employeeId" );
					String url = ctx.pageConfig.getBackURL();
					return sendRedirect( ctx, HtmlUtility.replaceURLQuery( url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
				}

			} else {
				if( originalMap == null || !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );

				if( multiSoldTo )
					multiempDB.update( employeeList, recordMap.get("uniqId"), inserting );
				else
					employeeDB.update( employeeList, recordMap.get("uniqId"), inserting );
				if( ( multiSoldTo && authDB.updateWithEmployeeIdCsv(employeeList, authConditionMap) )
						|| authDB.updateWithEmployeeId( employeeList, authConditionMap ) )
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
				else {
					ctx.handler.rollback();
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_PUB_SUCCESS_INC_ERROR") );
					ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_USER_"+ MODE_MODIFYINPUT.toUpperCase()) );
					ctx.req.setAttribute( "focus", "employeeId" );
					String url = ctx.pageConfig.getBackURL();
					return sendRedirect( ctx, HtmlUtility.replaceURLQuery( url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
				}
			}

			multiempDB.setSort("organizationCode");
			employeeDB.setSort("organizationCode");
			// setAttribute UserEmployee
			if( multiSoldTo )
				ctx.req.setAttribute( "organizations", multiempDB.getRecords(uniqId, countryCode, true) );
			else
				ctx.req.setAttribute( "organizations", employeeDB.getRecords(uniqId, countryCode, true) );
			ctx.req.setAttribute( "record", db.getRecord(recordMap) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			com.irt.dpr.ValidablePassword.updatePasswordHistory( ctx.handler, recordMap );

			String url = ctx.pageConfig.getBackURL();
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery( url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		String url = ctx.pageConfig.getBackURL();
		return sendRedirect( ctx, HtmlUtility.replaceURLQuery( url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	@Override
	protected boolean upload(Context ctx) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog( ctx.handler );

		if( !ctx.sessionMng.isAuthorized("USR", "USRUser.PTY.MNG") )
			throw new ServletModelException( ServletModelException.HAS_NOAUTH );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "USRUser.MSP%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			String countryCode = getUserCountryCode(ctx);
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			defaultMap.put( "partyId", ctx.sessionMng.getPartyId() );
			defaultMap.put( "countryCode", countryCode );
			defaultMap.put( "divisionCode", getDivisionCode(ctx) );
			List<Map<String,Object>> organizations = new com.irt.dpr.CountryCondition(ctx.handler)
					.getCountryOrganizations(countryCode, new String[]{"organizationCode"});
			if( organizations != null ) {
				String[] orgArr = MapUtil.extractValues( organizations, "organizationCode" ).toArray(new String[0]);
				String organizationCodes = String.join( ";", orgArr );
				defaultMap.put( "organizationCodes", organizationCodes );
			}

			String uploadType = "USRMSP";
			ManipulableManager db = (ManipulableManager) ctx.db;
			RecordFormat lineNameFormat;
			loader = ((UserUser)db).createDataLoader( fieldKeys, defaultMap, new String[] { "email", "status" }, com.irt.data.Record.UPDATE );

			lineNameFormat = PatternRecordFormat.getInstance( "[${userId}] - [${employeeId}] - [${soldPartyCode}] - [${email}] - [${status}]" );
			//validator = new AuthValidator(ctx);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "USR" );
			resultMap.put( "uploadType", uploadType );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );

			loaderLogger = logDB.createLogger( resultMap, messageFormat, lineNameFormat );
			DataLoader dataLoader = createDataLoader( ctx, ctx.handler, loader, loaderLogger, validator, true );

			try {
				DataReader reader = dataLoader.getDataReader();
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList) && !reader.isEOF(); i++ ) {
					try {
						reader.readNext();
					} catch( DataException dataEx ) {}
					if( reader.getLineString() != null && out != null )
						out.println( reader.getLineString() );
				}

				reader.setTrim(true);

				dataLoader.execute();
				ctx.pageConfig.setMessage( (String)loaderLogger.getResultMap().get("message") );
			} finally {
				dataLoader.close( false );
			}
			loader = null;

			String uploadInputPath = "USRUser?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
			String redirectURL = RBMUploadLog.getUploadLogURL( ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} finally {
			try { if( loader != null ) loader.close(); } catch( Exception ignored ) {}
		}
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> recordMap = new ParameterMap( ctx.req );

		recordMap.put( "supportFileTypesCsv", "XLS" );
		recordMap.put( "defaultFileType", "XLS" );
		recordMap.put( "encoding", "UTF8" );
		ctx.req.setAttribute( "record", recordMap );
		return forward( ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp" );
	}
}
