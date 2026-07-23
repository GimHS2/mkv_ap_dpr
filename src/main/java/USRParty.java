/*
 *	File Name:	USRParty.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		usr_party_input.jsp
 *		usr_party_list.jsp
 *		usr_party_name.jsp
 *		usr_party_select.jsp
 *		usr_unique_check.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.usr.*;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/USRParty"})
public class USRParty extends AbstractServletModel {
	public final static String MODE_UNIQUECHECK			= "chk";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		if( !ctx.sessionMng.isSystemAdmin() )
			Condition.putConditionValueOnly( conditionMap, "status", "00" );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_UNIQUECHECK.equals(ctx.mode) ) return uniqueCheck( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		UserParty db = (UserParty)ctx.db;

		// recordMap 읽기 & setAttribute
		String partyId = ctx.req.getParameter( "partyId" );
		if( partyId == null || partyId.length() == 0 )
			partyId = ctx.sessionMng.getPartyId();

		Map<String, Object> recordMap = db.getRecord( UserParty.createPrimary(partyId) );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldOption", UserPartyFieldOption.getInstance(ctx.sessionMng.getUserClass()) );

		// forward
		if( ctx.sessionMng.isAuthorized("USR", "USRParty.MNG") ) {
			if( ctx.sessionMng.isSystemAdmin() )
				ctx.pageConfig.setManageAuth( true );
			else if( ctx.sessionMng.isPartyAdmin() && ctx.sessionMng.getPartyId().equals(recordMap.get("partyId")) )
				ctx.pageConfig.setManageAuth( true );
		}

		if( inputting ) {
			if( !ctx.pageConfig.hasManageAuth() ) return false;
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/usr_party_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );
		ctx.db = new UserParty( ctx.handler );

		// setSystemPackageCode, partyId
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) ) {
			String partyId = ctx.req.getParameter( "partyId" );
			if( partyId == null || partyId.length() == 0 || partyId.equals(ctx.sessionMng.getPartyId()) )
				pageConfig.setSystemPackageCode( "USR", "USRParty.INF" );
			else
				pageConfig.setSystemPackageCode( "USR", "USRParty.INF_ALL" );
		} else if( MODE_SELECT.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRParty.SEL" );
		else if( MODE_LIST.equals( ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRParty.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRParty.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) || MODE_UNIQUECHECK.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRParty.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRParty.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_PARTY_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx, boolean listing ) throws IOException, ServletException, SQLException {
		UserParty db = (UserParty)ctx.db;

		// conditionMap 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );

		// columnList, records 읽기 & conditionMap 저장
		List recordList = null;
		com.irt.data.cols.ColumnList columnList = null;
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		if( listing ) {
			columnList = getColumnList( ctx, "USRParty%LIST" );
			if( ctx.sessionMng.isAuthorized("USR", "USRParty.MNG") )
				ctx.pageConfig.setManageAuth( ctx.sessionMng.isSystemAdmin() );
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		} else {
			ServletUtility.setSort( ctx.req, db, "partyName" );
			recordList = db.getRecords( conditionMap, new String[] { "partyId", "partyName" }, idxVars[0], idxVars[1] );
		}
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		if( listing )
			return forward( ctx, systemConfig.getJspPath() + "/usr_party_list.jsp" );
		else
			return forward( ctx, systemConfig.getJspPath() + "/usr_party_select.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("USR", "USRParty.LST") && !ctx.sessionMng.isAuthorized("USR", "USRParty.SEL") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((UserParty)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean name( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = createConditionMap( ctx );

		if( conditionMap.get("partyId") != null ) {
			List recordList = ((UserParty)ctx.db).getRecords( conditionMap, 0, 2 );
			if( recordList != null && recordList.size() == 1 ) ctx.req.setAttribute( "record", recordList.get(0) );
		}

		return forward( ctx, systemConfig.getJspPath() + "/usr_party_name.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		UserParty db = (UserParty)ctx.db;

		if( ctx.req.getAttribute("record") == null ) {
			if( !ctx.sessionMng.isSystemAdmin() )
				return false;

			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "status", "00" );
			ctx.req.setAttribute( "record", recordMap );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
			ctx.req.setAttribute( "fieldOption", UserPartyFieldOption.getInstance(ctx.sessionMng.getUserClass()) );
		}
		if( com.irt.rbm.sys.TimeZone.usingTimeZone() ) setAttributeMaster( ctx, MASTER_TIMEZONE );

		return forward( ctx, systemConfig.getJspPath() + "/usr_party_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		UserParty db = (UserParty)ctx.db;

		String[] partyIds = ctx.req.getParameterValues( "partyId" );
		if( !ctx.sessionMng.isSystemAdmin() ) return false;
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( partyIds == null || partyIds.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// primaryMap 생성 및 partyId 검사
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		// 레코드 삭제
		int count = 0;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < partyIds.length; i++) {
			try {
				primaryMap.put( "partyId", partyIds[i] );
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "partyName"), dataEx) );
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

	protected boolean uniqueCheck( Context ctx ) throws IOException, ServletException, SQLException {
		UserParty db = (UserParty)ctx.db;

		// recordMap 읽기 & setAttribute
		String partyId = ctx.req.getParameter( "partyId" );
		if( partyId == null || partyId.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = UserParty.createPrimary( partyId );
		Map<String, Object> recordMap = db.getRecord( UserParty.createPrimary(partyId) );
		if( recordMap == null ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_USR_NOT_EXIST_ID") );
			ctx.pageConfig.setProperty( "validable", "Y" );
			recordMap = primaryMap;
		} else
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_USR_EXIST_ID") );
		ctx.req.setAttribute( "record", recordMap );
		ctx.pageConfig.setProperty( "fieldKey", "partyId" );

		return forward( ctx, systemConfig.getJspPath() + "/usr_unique_check.jsp" );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		UserParty db = (UserParty)ctx.db;
		UserPartyFieldOption fieldOption = UserPartyFieldOption.getInstance( ctx.sessionMng.getUserClass() );

		// 레코드 읽기
		ParameterMap recordMap = new ParameterMap( ctx.req );
		if( !ctx.sessionMng.isSystemAdmin() ) {
			if( !ctx.sessionMng.isPartyAdmin() || !ctx.sessionMng.getPartyId().equals(recordMap.get("partyId")) )
				return false;
		}

		Map<String, Object> originalMap = null;
		if( inserting )
			fieldOption.applyOptionToRecordMap( recordMap, null );
		else {
			try {
				originalMap = db.getRecordWithLock( recordMap, fieldOption.getFieldKeys(), true );
			} catch( DataException dataEx ) {}
			fieldOption.applyOptionToRecordMap( recordMap, originalMap );
		}
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );
		ctx.req.setAttribute( "fieldOption", fieldOption );

		try {
			if( inserting ) {
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				if( originalMap == null || !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			ctx.req.setAttribute( "record", db.getRecord(recordMap) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/usr_party_input.jsp" );
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

		String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_PARTY_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}
}
