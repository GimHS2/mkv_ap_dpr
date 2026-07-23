/*
 *	File Name:	DPRCountryCondition.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_country_condition_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	jbaek		2012/07/30		2.2.1	최소 발주 가능 금액 설정 기능 추가(multiUpdate)
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.CountryCondition;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRCountryCondition"})
public class DPRCountryCondition extends DPRServletModel {
	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		Condition.putConditionValueOnly( conditionMap, "countryCode", conditionMap.get("countryCode") );
		Condition.putConditionValueOnly( conditionMap, "organizationCode", conditionMap.get("organizationCode") );

		if( !conditionMap.containsKey("conditionInd") )
			Condition.putConditionValueOnly( conditionMap, "conditionInd", CountryCondition.CONDITION_INDICATOR_REGISTRED );
		else
			Condition.putConditionValueOnly( conditionMap, "conditionInd", conditionMap.get("conditionInd") );

		// authUniqId
		 if( !ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() ) {
			 Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );

			if( ctx.sessionMng.isCountryAdmin() )
				Condition.putConditionValueOnly( conditionMap, "authCountryValue", "Y" );
			else
				Condition.putConditionValueOnly( conditionMap, "authCountryValue", "N" );
		}

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		/* get method시에 MODE_REGIST */
		if( MODE_REGIST.equals(ctx.mode) )
			return regist( ctx );
		else
			return super.doRequest( ctx, isPost );

	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.MNG" );
		else if( MODE_MULTIMODIFY.equals(ctx.mode) || MODE_MULTIMODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new CountryCondition( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_COUNTRY_CONDITION_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		CountryCondition db = (CountryCondition)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRCountryCondition%LIST" );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys("countryCode"), idxVars[0], idxVars[1] );

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else if( ctx.sessionMng.isCountryAdmin() )
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRCountry.MNG") );
		else
			ctx.pageConfig.setManageAuth( false );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_country_condition_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRCountry.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((CountryCondition)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean multiUpdate( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		CountryCondition db = (CountryCondition)ctx.db;

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String backUrl = ctx.pageConfig.getBackURL();
		backUrl.replace("mod", "list");

		// 레코드 읽기
		ParameterMap recordMap = new ParameterMap( ctx.req );
		List values = recordMap.extractGroupList( "value" );
		ctx.req.setAttribute( "condition", recordMap );

		try {
			for( Iterator iterator = values.iterator(); iterator.hasNext(); ) {
				Map map = (Map)iterator.next();
				map.put( "countryCode" , map.get("countryCode") );
				map.put( "organizationCode" , map.get("organizationCode")  );
				map.put( "status" , map.get("status") );
				map.put( "minOrderTotal", map.get("minOrderTotal") );
				if( !db.modify(map) )
					throw new DataException( DataException.ERR_NO_RECORD_UPDATE, ctx.msghandler.getMessage(DataException.ERR_NO_RECORD_UPDATE) );
			}
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		}

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery(backUrl, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		CountryCondition db = (CountryCondition)ctx.db;

		String[] countryCodes = ctx.req.getParameterValues( "countryCode" );
		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( countryCodes == null || countryCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCodes == null || organizationCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "countryCode", countryCodes[0] );

		int count = 0;
		List<Map<String, Object>> errorList = new java.util.LinkedList<Map<String, Object>> ();
		for( int i = 0; i < organizationCodes.length; i++ ) {
			primaryMap.put( "organizationCode", organizationCodes[i] );

			try {
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(organizationCodes[i], dataEx) );
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

	@Override
	protected boolean regist( Context ctx ) throws IOException, ServletException, SQLException {
		CountryCondition db = (CountryCondition)ctx.db;

		String[] countryCodes = ctx.req.getParameterValues( "countryCode" );
		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );

		if( countryCodes == null || countryCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCodes == null || organizationCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> recordMap = new java.util.HashMap<String, Object> ();
		recordMap.put( "countryCode", countryCodes[0] );
		recordMap.put( "status", "00" );

		int count = 0;
		List<Map<String, Object>> errorList = new java.util.LinkedList<Map<String, Object>>();
		for( int i = 0; i < organizationCodes.length; i++ ) {
			recordMap.put( "organizationCode", organizationCodes[i] );
			try {
				if( db.regist(recordMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(organizationCodes[i], dataEx) );
			} catch( SQLException sqlEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(organizationCodes[i], sqlEx) );
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}
}
