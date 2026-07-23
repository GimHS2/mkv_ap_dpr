/*
 *	File Name:	DPRParty.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		pub_common_name.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/07/31		2.2.4	list() : _recordMap에 NULL이 아닐때만 ConditionMap에 값을 넣도록 변경
 *	hankalam	2021/11/30		2.2.3	신규 UI/UX 적용
 *	jbaek		2019/07/30		2.2.2	권한 조건 추가
 *	hankalam	2019/06/29		2.2.2	name() 추가
 *	jbaek		2019/06/29		2.2.2	partyName 조회조건 오류 수정. list에 select()기능 추가.
 *	lsinji		2009/06/30		2.2.1	JDMSIndicator 조건 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.Record;
import com.irt.dpr.MasterLink;
import com.irt.dpr.Party;
import com.irt.html.HtmlPage;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRParty"})
public class DPRParty extends DPRServletModel {//@formatter:off
	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	@Override
	protected boolean cond( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = createConditionMap( ctx );

		String type = ctx.req.getParameter( "datatype" );
		if( MasterLink.MASTERTYPE_SALES_GROUP.equals(type) )
			setAttributePartyMasterOnExisting( ctx, conditionMap, PARTYMASTER_GROUP );
		else
			setAttributePartyMasterOnExisting( ctx, conditionMap, PARTYMASTER_ALL );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_party_cond.jsp" );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();

		/* Condition Value Region Code, Organization Code*/
		String countryCode = Record.extractString( parameterMap, "countryCode" );

/* WORKING : systemAdmin and partyAdmin 처리 */
		if( countryCode == null || countryCode.length() == 0 )
			countryCode = getUserCountryCode( ctx );
		Condition.putConditionValueOnly( conditionMap, "countryCode", countryCode );
		setDefaultParameterMultiDist( ctx, conditionMap );

		if( parameterMap.containsKey("parentPartyCode") )
			Condition.putConditionValueOnly( conditionMap, "parentPartyCode", parameterMap.get("parentPartyCode") );
		if( parameterMap.containsKey("regionCode") )
			Condition.putConditionValueOnly( conditionMap, "regionCode", parameterMap.get("regionCode") );
		if( parameterMap.containsKey("districtCode") )
			Condition.putConditionValueOnly( conditionMap, "districtCode", parameterMap.get("districtCode") );
		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly( conditionMap, "officeCode", parameterMap.get("officeCode") );
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly( conditionMap, "groupCode", parameterMap.get("groupCode") );
		if( parameterMap.containsKey("customerGroupCode") )
			Condition.putConditionValueOnly( conditionMap, "customerGroupCode", parameterMap.get("customerGroupCode") );

		String partyType = (String)conditionMap.get( "parytType" );
		if( Party.PARTYTYPE_CUSTOMER.equals(partyType) && !conditionMap.containsKey("parentPartyCode") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly( conditionMap, "partyCode", parameterMap.get("partyCode") );
		else {
			String customerName = parameterMap.getParameter("customerName");
			if( customerName != null )
				Condition.putConditionValueOnly( conditionMap, "customerName", customerName, Condition.CONDTYPE_CONTAINS );
		}

		Condition.putConditionValueOnly( conditionMap, "status", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		Condition.putConditionValueOnly( conditionMap, "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );

		// authUniqId
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );
			Condition.putConditionValueOnly( conditionMap, "authPartyValue", "Y" );
		}

		Condition.putConditionValueOnly( conditionMap, "JDMSIndicator", parameterMap.get("JDMSIndicator") );

		return conditionMap;
	}

	private boolean checkPrimaryCondition( Map conditionMap ) throws ServletModelException {
		if( !conditionMap.containsKey("partyCode") || !conditionMap.containsKey("organizationCode")  || !conditionMap.containsKey("distributionChannelCode") || !conditionMap.containsKey("divisionCode") )
			return false;
		return true;
	}

	private List getCountryOrganizationList( Context ctx, String countryCode, String uniqId ) throws SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		if( countryCode != null ) {
			conditionMap.put( "countryCode", countryCode );
			conditionMap.put( "conditionInd", com.irt.dpr.CountryCondition.CONDITION_INDICATOR_REGISTRED );

			return new com.irt.dpr.CountryCondition(ctx.handler).getRecords( conditionMap, new String[] { "organizationCode" } );
			/* WORKING: User Organization authorize */
		}

		return null;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx ) throws IOException, ServletException, SQLException {
		Party db = (Party)ctx.db;

		Map<String, Object> primaryMap;
		checkPrimaryCondition( primaryMap = createConditionMap(ctx) );

		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		setLinkParties( ctx, recordMap, primaryMap );

		ctx.req.setAttribute( "record", recordMap );

		// auth checking
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRParty.MNG") );

		ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_party_info.jsp" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRParty.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRParty.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_COND.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRParty.LST" );
		else if( MODE_SELECT.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRParty.LST" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new Party( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PARTY_"+ ctx.mode.toUpperCase()) );

		String messageKey = "TITLE_DPR_PARTY_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_PARTNER", "jsp.SUBMENU_PARTNER" );
	}

	@Override
	protected boolean list( Context ctx, boolean listing ) throws IOException, ServletException, SQLException {
		Party db = (Party)ctx.db;
		// Condition Map
		Map<String, Object> conditionMap = createConditionMap( ctx );

		boolean isSufficiencyCondition = false;
		if( conditionMap.containsKey("organizationCode") && conditionMap.containsKey("divisionCode") && conditionMap.containsKey("distributionChannelCode") )
			isSufficiencyCondition = true;

		String partyType = (String)conditionMap.get( "partyType" );
		if( partyType == null )
			partyType = Party.PARTYTYPE_DISTRIBUTOR;

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRParty%LIST." + partyType );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = null;
		if( !isSufficiencyCondition ) {
			idxVars[2] = 0;
			ctx.pageConfig.setProperty( "listmsg", ctx.msghandler.getMessage("MSG_CONDITION_NEEDED") );
		} else {
			recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
			String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
			if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

			ctx.req.setAttribute( "records", recordList );

			// Condition
			if( com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(ctx.sessionMng.getGroupClass()) ) {
				if( recordList != null && recordList.size() == 1 ) {
					Map<String, Object> _conditionMap = new java.util.HashMap<String, Object> ( conditionMap );
					_conditionMap.put( "partyCode", ((Map<String, Object>)recordList.get(0)).get("partyCode") );
					Map<String, Object> _recordMap = db.getRecord(_conditionMap, new String[] { "regionCode", "customerGroupCode", "districtCode", "officeCode", "groupCode" });
					if( _recordMap != null ) conditionMap.putAll( _recordMap );
				}
			}
		}
		setAttributePartyMasterOnExisting( ctx, conditionMap, PARTYMASTER_ALL );
		setAttributePartner( ctx, conditionMap );

		ctx.req.setAttribute( "countries", new com.irt.dpr.Country(ctx.handler).getRecords(null, new String[] { "countryCode", "countryName" }) );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRParty.MNG") );

		if( listing )
			return forward( ctx, systemConfig.getJspPath() + "/dpr_party_list.jsp" );
		else
			return forward( ctx, systemConfig.getJspPath() + "/dpr_party_select.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRParty.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((Party)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean name( Context ctx ) throws IOException, ServletException, SQLException {
		String partyCode = ctx.req.getParameter( "partyCode" );
		if( partyCode != null && partyCode.length() > 0 ) {
			List<Map<String, Object>> recordList = ((Party)ctx.db).getRecords( createConditionMap(ctx), 0, 2 );
			if( recordList != null && recordList.size() == 1 )
				ctx.req.setAttribute( "record", recordList.get(0) );
			else
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_NO_RECORD_FOUND_TO_SELECT") );
		}
		ctx.pageConfig.setProperty( "field", "partyCode=code, customerName=name" );

		return forward( ctx, systemConfig.getJspPath() + "/pub_common_name.jsp" );
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

	private void setLinkParties( Context ctx, Map<String, Object> recordMap, Map<String, Object> condition ) throws ServletModelException, SQLException {
		com.irt.dpr.PartyLink db = new com.irt.dpr.PartyLink( ctx.handler );

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( condition );

		/* sold-to, ship-to, bill-to, payer Condition */
		String linkType = ctx.req.getParameter( "linkType" );
		String[][] types = null;
		if( linkType != null ) {
			types = new String[][] { { linkType } };
		} else {
			types = new String[][] {
				  com.irt.dpr.PartyLink.LINKTYPE_SOLD
				, com.irt.dpr.PartyLink.LINKTYPE_SHIP
				, com.irt.dpr.PartyLink.LINKTYPE_PAYER
				, com.irt.dpr.PartyLink.LINKTYPE_BILL
			};
		}

		if( recordMap == null )
			recordMap = new java.util.HashMap<String, Object> ();

		ctx.req.setAttribute( "fieldSet_LNK", db.getFieldSet(false) );

		if( types != null ) {
			ServletUtility.setSort( ctx.req, db, new String[] { "partyCode", "linkType" } );
			for( int i = 0; i < types.length; i++ ) {
				conditionMap.put( "linkType", types[i] );
				String type = com.irt.dpr.PartyLink.getLinkType( types[i][0] );

				String attributeName = null;
				if( com.irt.dpr.PartyLink.LINKTYPE_SOLD[0].equals(type) )
					attributeName = "soldParties";
				else if( com.irt.dpr.PartyLink.LINKTYPE_SHIP[0].equals(type) )
					attributeName = "shipParties";
				else if( com.irt.dpr.PartyLink.LINKTYPE_PAYER[0].equals(type) )
					attributeName = "payerParties";
				else if( com.irt.dpr.PartyLink.LINKTYPE_BILL[0].equals(type) )
					attributeName = "billParties";
				else
					attributeName = "baseParites";

				recordMap.put( attributeName, db.getRecords(conditionMap) );
			}
		}
	}
}
