/*
 *	File Name:	DPRPartyAuth.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.4	getUserGroupClass() 를 DPRServletModel로 옮김
 *	hankalam	2021/11/30		2.2.3	신규 UI/UX 적용
 *	jbaek		2020/12/31		2.2.2	createConditionMap(): employeeId 조건 삭제, regist(): multipleSoldTo 기능 적용
 *	jbaek		2011/08/31		2.2.1	오류수정(오타: OrganizatinoCode -> OrganizationCode)
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.PartyAuth;
import com.irt.data.Record;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.html.HtmlUtility;
import com.irt.rbm.RBMSystem;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRPartyAuth"})
public class DPRPartyAuth extends DPRServletModel {
	public final static String PARAM_TYPE			= "type";
	public final static String TYPE_ORGANIZATION	= "org";
	public final static String TYPE_PARTY			= "pty";

	public final static String AUTHINDICATOR_AUTH	= "Y";
	public final static String AUTHINDICATOR_NOAUTH	= "N";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		setDefaultParameter( ctx, conditionMap );
		String organizationCode = (String)conditionMap.get( "organizationCode" );

/*
		if( !conditionMap.containsKey("authIndicator") )
			conditionMap.put( "authIndicator", AUTHINDICATOR_AUTH );
*/
		String uniqId = (String)conditionMap.get( "uniqId" );
		if( uniqId == null )
			uniqId = ctx.sessionMng.getUniqId();
		conditionMap.put( "uniqId", uniqId );
		conditionMap.put( "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
		if( !"list".equals( ctx.mode ) )
			conditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );

		if( "A".equals(conditionMap.get("linkSource")) )
			conditionMap.remove( "linkSource" );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_REGIST.equals(ctx.mode) ) {
			return regist( ctx );
		}

		return super.doRequest( ctx, isPost );
	}

	private List getOrganizationList( Context ctx, Object organizationCode ) throws ServletException, SQLException {
		if( organizationCode == null ) return null;

		com.irt.dpr.PartyMaster db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_SALES_ORGANIZATION );

		Map<String, Object> conditionMap = Record.createMap( "organizationCode", organizationCode );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		ServletUtility.setSort( ctx.req, db, new String[] { "organizationCode" } );
		return db.getRecords( conditionMap, new String[] { "organizationCode", "organizationName" } );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
//		PartyAuth db = (PartyAuth)ctx.db;

		if( inputting ) {
			Map<String, Object> recordMap = new ParameterMap( ctx.req );

			if( !recordMap.containsKey("organizationCode") || !recordMap.containsKey("partyCode") ) {
				ctx.req.setAttribute( "record", recordMap );

				if( recordMap.containsKey("organizationCode") ) {
					ctx.req.setAttribute( "authOrganizations", getOrganizationList(ctx, recordMap.get("organizationCode")) );
				}

				return registInput( ctx );
			}
		}

		setPartyAuthInfomation( ctx );

		ctx.req.setAttribute( "columnList_org", getColumnList(ctx, "DPRPartyAuth.ORG%LIST") );
		ctx.req.setAttribute( "columnList_pty", getColumnList(ctx, "DPRPartyAuth.PTY%LIST") );

ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_partyauth_input.jsp" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );
		String basisKey = null;

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyAuth.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyAuth.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFYINPUT.equals(ctx.mode) || MODE_MODIFY.equals(ctx.mode)) {
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyAuth.MNG" );
			basisKey = "M";
		} else if( MODE_REGISTINPUT.equals(ctx.mode) || MODE_REGIST.equals(ctx.mode)) {
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyAuth.MNG" );
			basisKey = "R";
		} else if( MODE_REMOVE.equals(ctx.mode) ) {
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyAuth.MNG" );
		} else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String type = ctx.req.getParameter( PARAM_TYPE );
		if( type == null ) type = TYPE_ORGANIZATION;
		ctx.pageConfig.setProperty( PARAM_TYPE, type );

		ctx.db = new PartyAuth( ctx.handler );
		ctx.extraObj = basisKey;

		String messageKey = "TITLE_DPR_PARTYAUTH_";
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		ctx.pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		PartyAuth db = (PartyAuth)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );

		// user Info
		{
			Map<String, Object> userMap = new java.util.HashMap<String, Object>( conditionMap );
			userMap.put( "partyId", ctx.sessionMng.getPartyId() );

			ctx.req.setAttribute( "record_usr", new com.irt.rbm.usr.UserUser(ctx.handler).getRecord(userMap) );
		}

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPartyAuth.PTY%LIST" );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

		String uniqId = ctx.req.getParameter( "uniqId" );
		if( uniqId != null ) {
			ctx.pageConfig.setProperty( "userGroupClass", getUserGroupClass( ctx, ctx.sessionMng.getPartyId(), uniqId ) );
		}

		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );

		setPath( ctx, "jsp.MENU_USER", "TITLE_USR_USER_MANAGEMENT", "TITLE_DPR_PARTYAUTH_" );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_partyauth_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((PartyAuth)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map recordMap = (Map)ctx.req.getAttribute( "record" );

		ctx.req.setAttribute( "columnList_org", getColumnList(ctx, "DPRPartyAuth.ORG%LIST") );
		ctx.req.setAttribute( "columnList_pty", getColumnList(ctx, "DPRPartyAuth.PTY%LIST") );

		if( ctx.req.getAttribute("record") == null ) {
			ctx.req.setAttribute( "fieldSet", ((PartyAuth)ctx.db).getFieldSet(true) );

			if( !recordMap.containsKey("organizationCode") || !recordMap.containsKey("partyCode") ) {
				ctx.req.setAttribute( "record", recordMap );

				if( recordMap.containsKey("organizationCode") )
					ctx.req.setAttribute( "authOrganizations", getOrganizationList(ctx, recordMap.get("organizationCode")) );
			}

			setPartyAuthInfomation( ctx );
		}
		ctx.pageConfig.setManageAuth( true );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_partyauth_input.jsp" );
	}

	@Override
	protected boolean regist( Context ctx ) throws IOException, ServletException, SQLException {
		PartyAuth db = (PartyAuth)ctx.db;
		boolean multiSoldTo = RBMSystem.getSystemEnvBool( "DPR", (ctx.sessionMng.getPartyId()+";multiSoldTo"), "JNJAP_CN".equals(ctx.sessionMng.getPartyId()) );

		String[] listOrganizationCodes = ctx.req.getParameterValues( "listOrganizationCode" );
		String[] partyCodes = ctx.req.getParameterValues( "partyCode" );
		String uniqId = ctx.req.getParameter("uniqId");
		Map<String, Object> recordMap = new java.util.HashMap<String, Object> ();

		/* Organization에 속한 Distributor를 가져옴 */
		String organizationCode = getSavedOrganizationCode( ctx );
		if( listOrganizationCodes == null || listOrganizationCodes.length == 0 ) {
			if( organizationCode == null || organizationCode.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			Map<String, Object> conditionMap = createConditionMap( ctx );
			conditionMap.put( "organizationCode", organizationCode );
			conditionMap.put( "authIndicator", AUTHINDICATOR_NOAUTH );
			conditionMap.remove( "partyCode" );

			List recordList = db.getRecords( conditionMap, new String[] { "partyCode" } );
			if( recordList != null && recordList.size() > 0 ) {
				partyCodes = new String[ recordList.size() ];
				List<String> arrayList = new java.util.ArrayList<String>();
				for( Object obj : recordList )
					arrayList.add( (String)((Map)obj).get("partyCode") );

				arrayList.toArray( partyCodes );
			} else {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_NODATA_PARTY") );
				return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
			}
			recordMap.put( "organizationCode", organizationCode );
		} else if( partyCodes == null || partyCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		recordMap.put( "countryCode", getUserCountryCode(ctx) );
		recordMap.put( "divisionCode", getDivisionCode(ctx) );
		recordMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );
		recordMap.put( "authValue", PartyAuth.DEFAULT_AUTHORIZATIONVALUE );
		if( uniqId == null || uniqId.length() == 0 )
			uniqId = ctx.sessionMng.getUniqId();
		recordMap.put( "uniqId", uniqId );

		if( !multiSoldTo && com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(getUserGroupClass(ctx, ctx.sessionMng.getPartyId(), uniqId) ) ) {
			// check Distributor record
			Map<String, Object> authConditionMap = new java.util.HashMap<String, Object> ( recordMap );

			if( ctx.req.getParameter("organizationCode") == null )
				authConditionMap.put( "organizationCode", listOrganizationCodes[0] );
			else
				authConditionMap.put( "organizationCode", ctx.req.getParameter("organizationCode") );

			authConditionMap.put( "authUniqId", uniqId );
			authConditionMap.put( "authPartyValue", "Y" );

			int cnt = db.getRecordCount( authConditionMap );
			if( cnt >= 1 ) {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_PARTYAUTH_ONLY_ONE_REGIST_TO_DISC") );
				return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
			}
		}

		int count = 0;
		List<Map<String, Object>> errorList = new java.util.LinkedList<Map<String, Object>>();
		for( int i = 0; i < partyCodes.length; i++ ) {
			if( listOrganizationCodes != null )
				recordMap.put( "organizationCode", listOrganizationCodes[i] );

			recordMap.put( "partyCode", partyCodes[i] );

			try {
				if( db.regist(recordMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(recordMap.get("organizationCode") +"/"+ partyCodes[i], dataEx) );
			} catch( SQLException sqlEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(recordMap.get("organizationCode") +"/"+ partyCodes[i], sqlEx) );
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

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		PartyAuth db = (PartyAuth)ctx.db;

        String[] organizationCodes = ctx.req.getParameterValues( "listOrganizationCode" );
        String[] partyCodes = ctx.req.getParameterValues( "partyCode" );
		String uniqId = ctx.req.getParameter( "uniqId" );

        if( ctx.pageConfig.getBackURL() == null )
            throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
        else if( organizationCodes == null || organizationCodes.length == 0 )
            throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
        else if( partyCodes == null || partyCodes.length == 0 )
            throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

        Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "countryCode", getUserCountryCode(ctx) );
		primaryMap.put( "divisionCode", getDivisionCode(ctx) );
		primaryMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );
		if( uniqId == null || uniqId.length() == 0 )
			uniqId =  ctx.sessionMng.getUniqId();
		primaryMap.put( "uniqId", uniqId );

        int count = 0;
        List<Map<String, Object>> errorList = new java.util.LinkedList<Map<String, Object>> ();
        for( int i = 0; i < organizationCodes.length; i++ ) {
            primaryMap.put( "organizationCode", organizationCodes[i] );
            primaryMap.put( "partyCode", partyCodes[i] );

            try {
                if( db.delete(primaryMap) ) {
                    count++;
                    ctx.handler.commit();
                }
            } catch( DataException dataEx ) {
                ctx.handler.rollback();
                errorList.add( createErrorMap(organizationCodes[i] +"/"+ partyCodes[i], dataEx) );
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

	protected void setPartyAuthInfomation( Context ctx ) throws ServletException, SQLException {
		PartyAuth db = (PartyAuth)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );

		if( organizationCodes != null && organizationCodes.length > 0 ) {
			conditionMap.put( "organizationCode", organizationCodes );

			conditionMap.put( "basisKey", ctx.extraObj );

			ServletUtility.setSort( ctx.req, db, new String[] { "partyCode" } );

			ctx.req.setAttribute( "authParties", db.getRecords(conditionMap) );
		}

		if( ctx.req.getAttribute("authOrganizations") == null )
			ctx.req.setAttribute( "authOrganizations", db.getAuthOrganizations(conditionMap) );
	}

/*
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		PartyAuth db = (PartyAuth)ctx.db;

		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		if( !recordMap.containsKey("organizationCode") || !recordMap.containsKey("partyCode") ) {
			return registInput( ctx );
		}

		setPartyAuthInfomation( ctx );

		Object[] party_organizationCodes = Record.extractObjectArray( recordMap, "partyOrganizationCode" );
		Object[] partyCodes = Record.extractObjectArray( recordMap, "partyCode" );

		if( partyCodes == null || partyCodes.length == 0 ) {
			String authPartyAll = (String)recordMap.get( "authPartyAll" );
			if( "Y".equals(authPartyAll) ) {
				Map<String, Object>conditionMap = new java.util.HashMap<String, Object>();
				conditionMap.put( "countryCode", getUserCountryCode(ctx) );
				conditionMap.put( "divisionCode", getDivisionCode(ctx) );
				conditionMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );

				List record_party = new com.irt.dpr.Party(ctx.handler).getRecords( conditionMap, new String[] { "partyCode" } );
				partyCodes = Record.extractObjectArray( record_party, "partyCode" );
			}
		}

		Map<String, Object> recordMap_auth = new ParameterMap( ctx.req );
		recordMap_auth.put( "countryCode", getUserCountryCode(ctx) );
		recordMap_auth.put( "divisionCode", getDivisionCode(ctx) );
		recordMap_auth.put( "distributionChannelCode", getDistributionChannelCode(ctx) );

		if( recordMap.containsKey("uniqId") )
			recordMap_auth.put( "uniqId", recordMap.get("uniqId") );
		else
			recordMap_auth.put( "uniqId", ctx.sessionMng.getUniqId() );

		int count = 0;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		try {
			for( int i = 0; i < partyCodes.length; i++ ) {
				recordMap_auth.put( "partyCode", partyCodes[i] );
				recordMap_auth.put( "organizationCode", party_organizationCodes[i] );

				try {
					if( inserting )
						if( db.regist(recordMap_auth) ) count++;
					else {
						if( db.modify(recordMap_auth) )
							count++;
						else
							throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
					}

					ctx.handler.commit();
				} catch( DataException dataEx ) {
					ctx.handler.rollback();
					errorList.add( createErrorMap( recordMap.get("organizationCode") + "/" + recordMap_auth.get("partyCode"), dataEx ) );
				}

			}
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );

			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage( (inserting ? "MSG_REGIST_SUCCESS" : "MSG_UPDATE_SUCCESS"), String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}
*/
}
