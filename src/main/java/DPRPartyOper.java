/*
 *	File Name:	DPRPartyOper.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		dpr_partyoper_input.jsp
 *		dpr_partyoper_list.jsp
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	hankalam	2019/06/28		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.dpr.PartyOperation;
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
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRPartyOper"})
public class DPRPartyOper extends DPRServletModel {
	public final static String MODE_COND_SETTING		= "rtp";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		if( parameterMap.containsKey("availableInd") ) {
			Condition.putConditionValueOnly(conditionMap, "availableInd", parameterMap.get("availableInd"));
		}

		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly(conditionMap, "partyCode", parameterMap.get("partyCode"));

		if( parameterMap.containsKey("shipPartyCode") )
			Condition.putConditionValueOnly(conditionMap, "shipPartyCode", parameterMap.get("shipPartyCode"));

		if( parameterMap.containsKey("distributionChannelCode") ) {
			Condition.putConditionValueOnly(conditionMap, "distributionChannelCode", parameterMap.get("distributionChannelCode"));
		} else if( MODE_REGISTINPUT.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) ) {
			conditionMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );
		}
		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly(conditionMap, "officeCode", parameterMap.get("officeCode"));
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly(conditionMap, "groupCode", parameterMap.get("groupCode"));

		conditionMap.put("countryCode", getUserCountryCode(ctx));
		conditionMap.put("divisionCode", getDivisionCode(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));

		setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);
		if( !conditionMap.containsKey("distributionChannelCode") ) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
			Object[] distChannels = Record.extractObjectArray( distributionChannels, "distributionChannelCode" );
			String[] channels = new String[ distChannels.length + 1 ];
			int i = 0;
			for( Object obj : distChannels ) {
				channels[i] = (String)obj;
				i++;
			}
			channels[i] = "0";
			conditionMap.put( "distributionChannelCode", channels );
		}

		// authUniqId
		if( !ctx.sessionMng.isSystemAdmin() ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );

			if( ctx.sessionMng.isPartyAdmin() || ctx.sessionMng.isCountryAdmin() )
				Condition.putConditionValueOnly( conditionMap, "authCountryValue", "Y" );
			else
				Condition.putConditionValueOnly( conditionMap, "authCountryValue", "N" );
		}

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		PartyOperation db = (PartyOperation)ctx.db;

		// primaryKey 생성
		String organizationCode = ctx.req.getParameter("organizationCode");
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		String officeCode = ctx.req.getParameter( "officeCode" );
		String groupCode = ctx.req.getParameter( "groupCode" );
		String patternType = ctx.req.getParameter( "patternType" );
		String patternIndex = ctx.req.getParameter( "patternIndex" );
		String patternDate = ctx.req.getParameter( "patternDate" );

		if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( patternType == null || patternType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( patternIndex == null || patternIndex.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( patternDate == null || patternDate.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = PartyOperation.createPrimary( organizationCode, distributionChannelCode, officeCode, groupCode, patternType, patternIndex, patternDate );
		String[] nullValueFieldKeys = new String[] { "distributionChannelCode", "officeCode", "groupCode" };
		replaceNullValues( primaryMap, nullValueFieldKeys, NULL_VALUE );

		// recordMap 읽기 & setAttribute
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		ctx.req.setAttribute( "record", recordMap );
		// forward
		if( inputting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPartyOper.MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_partyoper_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyOper.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyOper.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyOper.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyOper.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyOper.MNG" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new PartyOperation( ctx.handler );

		String messageKey = "TITLE_DPR_PARTYOPER_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_PARTNER", "jsp.SUBMENU_PARTYOPER" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		PartyOperation db = (PartyOperation)ctx.db;

		// conditionMap & columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPartyOper%LIST" );

		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPartyOper.MNG") );

		ctx.req.setAttribute("distributionChannels",
				getSelectListNaZeroValue(ctx, (List<Map<String, Object>>)ctx.req.getAttribute("distributionChannels"), "distributionChannelCode", "distributionChannelName"));
		ctx.req.setAttribute("offices",
				getSelectListNaZeroValue(ctx, (List<Map<String, Object>>)ctx.req.getAttribute("offices"), "officeCode", "officeName"));
		ctx.req.setAttribute("groups",
				getSelectListNaZeroValue(ctx, (List<Map<String, Object>>)ctx.req.getAttribute("groups"), "groupCode", "groupName"));

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_partyoper_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyOper.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((PartyOperation)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		Map<String, Object> conditionMap = createConditionMap( ctx );
		Map<String, Object> recordMap = (Map<String, Object>)ctx.req.getAttribute("record");
		if( recordMap == null ) {
			ctx.req.setAttribute( "fieldSet", ((PartyOperation)ctx.db).getFieldSet(true) );
			recordMap = new java.util.HashMap<String, Object>();
			//default sales org
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> ccndMap = ccnd.getRecord( ccnd.createPrimary(ctx.sessionMng.getGln(), organizationCode) );
			recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
			recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );
			ctx.req.setAttribute( "record", recordMap );
		}
		setAttributePartyMasterOnExisting(ctx, conditionMap,
				PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);
		return forward( ctx, systemConfig.getJspPath() + "/dpr_partyoper_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		PartyOperation db = (PartyOperation)ctx.db;

		String[] organizationCode = ctx.req.getParameterValues("organizationCode");
		String[] distributionChannelCode = ctx.req.getParameterValues( "distributionChannelCode" );
		String[] officeCode = ctx.req.getParameterValues( "officeCode" );
		String[] groupCode = ctx.req.getParameterValues( "groupCode" );
		String[] patternTypes = ctx.req.getParameterValues( "patternType" );
		String[] patternIndexes = ctx.req.getParameterValues( "patternIndex" );
		String[] patternDates = ctx.req.getParameterValues( "patternDate" );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode == null || patternTypes == null || patternIndexes == null || patternDates == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode.length == 0 || patternTypes.length != patternIndexes.length || patternTypes.length != patternDates.length )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < patternTypes.length; i++ ) {
			try {
				primaryMap.put( "organizationCode", organizationCode[i] );
				String[] nullValueFieldKeys = new String[] { "distributionChannelCode", "officeCode", "groupCode" };
				replaceNullValues( primaryMap, nullValueFieldKeys, NULL_VALUE );

				primaryMap.put( "distributionChannelCode", distributionChannelCode[i] );
				primaryMap.put( "officeCode", officeCode[i] );
				primaryMap.put( "groupCode", groupCode[i] );
				primaryMap.put( "patternType", patternTypes[i] );
				primaryMap.put( "patternIndex", patternIndexes[i] );
				primaryMap.put( "patternDate", patternDates[i] );

				if( db.delete(primaryMap) ) count++;

				ctx.handler.commit();
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "name"), dataEx) );
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_ECS_PARTYOPER_REMOVE_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		PartyOperation db = (PartyOperation)ctx.db;

		// 레코드 읽기
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String[] nullValueFieldKeys = new String[] { "distributionChannelCode", "officeCode", "groupCode" };
		recordMap = replaceNullValues( recordMap, nullValueFieldKeys, NULL_VALUE );

		try {
			if( inserting ) {
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_ECS_PARTYOPER_REGIST_SUCCESS") );
			} else {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_ECS_PARTYOPER_MODIFY_SUCCESS") );
			}

			ctx.req.setAttribute( "record", db.getRecord(recordMap) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_partyoper_input.jsp" );
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
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PARTYOPER_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		Object distributionChannelCode = conditionMap.get( "distributionChannelCode" );
		if( distributionChannelCode == null )
			conditionMap.put( "distributionChannelCode", new String[] { getDistributionChannelCode(ctx) } );
		else if( distributionChannelCode instanceof String ){
			conditionMap.put( "distributionChannelCode", new String[] { (String)distributionChannelCode } );
		}

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}
}

