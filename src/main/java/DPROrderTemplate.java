/*
 *	File Name:	DPROrderTemplate.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	keehe		2008/09/26		2.2.0	create
 *
 **/

import com.irt.dpr.Template;
import com.irt.data.*;
import com.irt.data.Record;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPROrderTemplate"})
public class DPROrderTemplate extends DPRServletModel {
	public final static String MODE_LOAD			= "load";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "templateKey", conditionMap.get("templateKey") );
		if( conditionMap.containsKey("templateName") ) {
			Condition.putConditionValueOnly( conditionMap, "templateName", conditionMap.get("templateName"), Condition.CONDTYPE_CONTAINS );
		}
		Condition.putConditionValueOnly( conditionMap, "orderKey", conditionMap.get("orderKey") );

		Condition.putConditionValueOnly( conditionMap, "manageUserId", ctx.sessionMng.getUniqId() );

		if(conditionMap.get("updateDateTime") != null ){
			try{
				com.irt.data.Date updateDate = com.irt.data.Date.getInstance( (String)conditionMap.remove("updateDateTime") );
				conditionMap.put( "updateDateTime" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MIN );
				conditionMap.put( "updateDateTime" + Condition.SUFFIX_MIN_VALUE, updateDate  );
				conditionMap.put( "updateDateTime" + Condition.SUFFIX_MAX_VALUE, updateDate.getDate(1)  );
			}catch(java.text.ParseException parseEx ) {
				return null;
			}
		}

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_LOAD.equals(ctx.mode) )
			return list( ctx, false );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		Template db = (Template)ctx.db;

		String templateKey = ctx.req.getParameter( "templateKey" );
		if( templateKey == null || templateKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = Template.createPrimary( templateKey );
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		// auth checking
		/* 다른 servlet에서 이 로직으로 사용.
		boolean authValue = getUserAuthParty( ctx, null );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( authValue );
		else if( authValue )
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPROrderTemplate.MNG") );
		 */
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else
			ctx.pageConfig.setManageAuth( false );

		// 임시
		ctx.pageConfig.setManageAuth( true );
		if( inputting ) {
			if( ctx.pageConfig.hasManageAuth() ) {
				com.irt.rbm.usr.UserUser userDB = new com.irt.rbm.usr.UserUser( ctx.handler );
				Map<String, Object> userConditionMap = new java.util.HashMap<String, Object> ();
				userConditionMap.put( "extraValue1", templateKey );
				//              userConditionMap.put( "userGroupId", "" );

				ctx.req.setAttribute( "users", userDB.getRecords( userConditionMap, new String[] { "uniqId", "userId", "userName", "extraValue1" }) );
				ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

				return registInput( ctx );
			} else {
				ctx.pageConfig.setMode( ctx.mode = MODE_INFO );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_HAS_NOAUTH_COUNTRY_MODIFY") );
			}
		}

		ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet( true) );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_ordertemplate_input.jsp" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplate.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplate.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_LOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplate.LST" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplate.MNG" );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplate.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplate.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new Template( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_TEMPLATE_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		Template db = (Template)ctx.db;
		// Condition Map
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrderTemplate%LIST" );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		//		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ALL );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );
		ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_ordertemplate_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPROrderTemplate.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((Template)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		Template db = (Template)ctx.db;
		Map<String, Object> recordMap = (Map)ctx.req.getAttribute("record");
		Map<String, Object> recordMap2 = new ParameterMap( ctx.req );
		String orderKey2 = Record.extractString( recordMap2, "orderKey" );
		if( recordMap == null ) {
			recordMap = new java.util.HashMap<String, Object>();

			recordMap.put( "manageUserId", ctx.sessionMng.getUserId() );
			recordMap.put( "updateDateTime", com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );
			recordMap.put( "soldPartyName", String.valueOf(db.findOrderSoldTo( orderKey2 )) );

			recordMap.put( "orderKey", ctx.req.getParameter("orderKey") );
			/* getDistributor() */

			ctx.req.setAttribute( "fieldSet", ((Template)ctx.db).getFieldSet(true) );
			ctx.req.setAttribute( "record", recordMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_ordertemplate_input.jsp" );

	}

	@Override
	protected boolean regist( Context ctx ) throws IOException, ServletException, SQLException {
		Template db = (Template)ctx.db;
		Map<String, Object> recordMap = new ParameterMap( ctx.req );

		String orderKey = Record.extractString( recordMap, "orderKey" );
		if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		recordMap.put( "templateKey" , String.valueOf(db.createTemplateKey()) );
		recordMap.put( "orderKey", orderKey );
		recordMap.put( "status", "00" );
		recordMap.put( "soldPartyName", String.valueOf(db.findOrderSoldTo( orderKey )) );
		recordMap.put( "updateDateTime", com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );

		try {
			String errorMessage = db.regist( recordMap, ctx.sessionMng.getUniqId() );

			if( errorMessage != null && errorMessage.length() > 0 ) {
				ctx.handler.rollback();

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_SAVETEMPLATE_FAILED", errorMessage) );
			} else
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_SAVETEMPLATE_SUCCESS") );

			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.req.setAttribute( "fieldSet", ((Template)ctx.db).getFieldSet(false) );
			ctx.req.setAttribute( "record", recordMap );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_ordertemplate_input.jsp" );
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
		ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
		return registInput( ctx );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		Template db = (Template)ctx.db;
		Map<String, Object> recordMap = new ParameterMap( ctx.req );

		try {
			if( inserting ){
				if( !db.regist(recordMap))
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			Map<String, Object> primaryMap = Template.createPrimary( (String)recordMap.get("templateKey") );

			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			ctx.req.setAttribute( "record", db.getRecord(primaryMap) );
			ctx.pageConfig.setManageAuth( true );
			return forward( ctx, systemConfig.getJspPath() + "/dpr_ordertemplate_input.jsp" );
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
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
		ctx.req.setAttribute( "record", recordMap );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_TEMPLATE_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		Template db = (Template)ctx.db;
		String templateKeys[] = ctx.req.getParameterValues( "templateKey" );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( templateKeys == null || templateKeys.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map primaryMap = new java.util.HashMap();
		primaryMap.put( "templateKey", templateKeys );

		List errorList = new java.util.ArrayList();
		for( int i = 0; i < templateKeys.length; i++) {
			try {
				primaryMap.put( "templateKey", templateKeys[i] );
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getName(templateKeys[i]), dataEx) );
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", new String[] { String.valueOf(count) }) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}


}
