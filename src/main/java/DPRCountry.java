/*
 *	File Name:	DPRCountry.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_country_list.jsp
 *		dpr_country_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.3	신규 UI/UX 적용
 *	jbaek		2020/06/30		2.2.2	Revise Order Feature.
 *	jbaek		2019/11/30		2.2.1	Multiple Country Manager 기능 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.Country;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.html.HtmlPage;
import com.irt.servlet.*;
import com.irt.util.Utility2;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRCountry"})
public class DPRCountry extends DPRServletModel {
	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );

		if( conditionMap.containsKey("countryCode") )
			Condition.putConditionValueOnly( conditionMap, "countryCode", conditionMap.get("countryCode") );
		if( conditionMap.containsKey("countryKey") )
			Condition.putConditionValueOnly( conditionMap, "countryKey", conditionMap.get("countryKey") );
		if( conditionMap.containsKey("organizationCode") )
			Condition.putConditionValueOnly( conditionMap, "organizationCode", conditionMap.get("organizationCode") );

		Condition.putConditionValueOnly( conditionMap, "status", Country.STATUS_NORMAL );

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
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		Country db = (Country)ctx.db;

		String countryCode = ctx.req.getParameter( "countryCode" );
		if( countryCode == null || countryCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = Country.createPrimary( countryCode );
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		String organizationCode = getSavedOrganizationCode(ctx);
		if( Country.isFeature(organizationCode, "useRevOrd") ) {
			recordMap.put("partyRevOrdSndEmail"
					, com.irt.rbm.RBMSystem.getSystemEnv("DPR", ctx.sessionMng.getPartyId()+";"+"partyRevOrdSndEmail"));
			recordMap.put("partyRevOrdRcvEmails"
					, com.irt.rbm.RBMSystem.getSystemEnv("DPR", ctx.sessionMng.getPartyId()+";"+"partyRevOrdRcvEmails"));
			recordMap.put("partyRevOrdMaxLimit"
					, com.irt.rbm.RBMSystem.getSystemEnv("DPR", ctx.sessionMng.getPartyId()+";"+"partyRevOrdMaxLimit"));
		}
		ctx.req.setAttribute( "record", recordMap );

		// auth checking
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else if( ctx.sessionMng.isCountryAdmin() )
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRCountry.MNG") );
		else
			ctx.pageConfig.setManageAuth( false );

		if( inputting ) {
			if( ctx.pageConfig.hasManageAuth() ) {
				com.irt.rbm.usr.UserUser userDB = new com.irt.rbm.usr.UserUser( ctx.handler );
				Map<String, Object> userConditionMap = new java.util.HashMap<String, Object> ();
				userConditionMap.put( "extraValue1", countryCode );

				ctx.req.setAttribute( "users", userDB.getRecords( userConditionMap, new String[] { "uniqId", "userId", "userName", "extraValue1" }) );
				ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

				return registInput( ctx );
			} else {
				ctx.pageConfig.setMode( ctx.mode = MODE_INFO );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_HAS_NOAUTH_COUNTRY_MODIFY") );
			}
		}
		ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_country_input.jsp" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRCountry.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new Country( ctx.handler );

		String messageKey = "TITLE_DPR_COUNTRY_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_PARTNER", "jsp.SUBMENU_COUNTRY" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		Country db = (Country)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRCountry%LIST" );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_country_list.jsp" );
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
		ctx.pageConfig.getListIndexVariables()[2] = ((Country)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		if( ctx.req.getAttribute("record") == null )
			ctx.req.setAttribute( "fieldSet", ((Country)ctx.db).getFieldSet(true) );

		setAttributeMaster( ctx, MASTER_COUNTRY );
		setAttributeMaster( ctx, MASTER_CURRENCY );
		setAttributeMaster( ctx, MASTER_LANGUAGE );
		setAttributeMaster( ctx, MASTER_TIMEZONE );
		ctx.pageConfig.setManageAuth( true );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_country_input.jsp" );
	}

	/* Country Code, USR_PARTY.PARTY_ID */
	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		Country db = (Country)ctx.db;

		ParameterMap recordMap = new ParameterMap( ctx.req );
		if( inserting )
			recordMap.put( "countryCode", String.valueOf(db.createCountryCode()) );

		// countryCode message 처리
		if( !recordMap.containsKey("countryCode") || ((String)recordMap.get("countryCode")).length() == 0 )
			new ServletModelException( ServletModelException.INTERNAL_ERROR );
		if( !recordMap.containsKey("partyId") || ((String)recordMap.get("partyId")).length() == 0 )
			new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( !recordMap.containsKey("status") )
			recordMap.put( "status", "00" );

		// set CountryAuth
		List<Map<String, Object>> countryAuthList = new java.util.LinkedList<Map<String, Object>>();
		if( recordMap.containsKey("authUniqId") ) {
			String[] authUniqIds = recordMap.getParameterValues("authUniqId");
			for( String authUniqId : authUniqIds ) {
				if( authUniqId != null && authUniqId.length() > 0 ) {
					Map<String, Object> record_auth = new java.util.HashMap<String, Object>();
					record_auth.put( "countryCode", recordMap.get("countryCode") );
					record_auth.put( "authValue", com.irt.dpr.CountryAuth.DEFAULT_AUTHORIZATIONVALUE );
					record_auth.put( "updateUserId", ctx.sessionMng.getUniqId() );
					record_auth.put( "status", "00" );

					record_auth.put( "authUniqId", authUniqId.toString() );
					countryAuthList.add( record_auth );
				}
			}

			recordMap.put( "countryAuthList", countryAuthList );
		}

		try {
			com.irt.rbm.usr.UserParty partyDB = new com.irt.rbm.usr.UserParty( ctx.handler );
			if( inserting ) {
				if( db.update( recordMap, Country.INFO_ALL, inserting ) ) {

					Map<String, Object> partyRecord = new java.util.HashMap<String, Object>();
					partyRecord.put( "partyId", recordMap.get("partyId") );
					partyRecord.put( "partyName", recordMap.get("countryName") );
					partyRecord.put( "password", recordMap.get("partyId") );
					partyRecord.put( "partyGln", recordMap.get("countryCode") );
					partyRecord.put( "timeZone", recordMap.get("timeZone") );
					partyRecord.put( "partyClass", "SX" );
					partyRecord.put( "status", Country.STATUS_NORMAL );

					if( !partyDB.regist( partyRecord ) ) {
						throw ctx.handler.createDataException( DataException.ERR_CANNOT_INSERT );
					}
				}

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				if( !db.update(recordMap, Country.INFO_ALL) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );

				Map<String, Object> partyRecord = new java.util.HashMap<String, Object>();
				partyRecord.put( "partyId", recordMap.get("partyId") );
				partyRecord.put( "timeZone", recordMap.get("timeZone") );
				if( !partyDB.modify( partyRecord, new String[] { "timeZone" } ) )
					throw ctx.handler.createDataException( DataException.ERR_CANNOT_UPDATE );

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			Map<String, Object> primaryMap = Country.createPrimary( (String)recordMap.get("countryCode") );

			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

			Map<String, Object> dbRecord = db.getRecord(primaryMap);
			if( Country.isFeature(getSavedOrganizationCode(ctx), "useRevOrd") ) {
				Integer partyRevOrdMaxLimit = Utility2.DataField.toInteger( recordMap, "partyRevOrdMaxLimit", com.irt.dpr.OrderRevise.DEFAULT_MAX_LIMIT_MODIFICATION );
				try {
					com.irt.rbm.RBMSystem.setSystemEnv( ctx.handler, "DPR", ctx.sessionMng.getPartyId()+";"+"partyRevOrdMaxLimit", String.valueOf(partyRevOrdMaxLimit) );
				} catch( DataException dataEx ) {
					logger.error("error", dataEx);
					throw dataEx;
				}
				dbRecord.put("partyRevOrdMaxLimit", partyRevOrdMaxLimit);

				String partyRevOrdSndEmail = recordMap.getParameter("partyRevOrdSndEmail");
				if( partyRevOrdSndEmail != null && partyRevOrdSndEmail.length() > 0 ) {
					try {
						InternetAddress[] addrs = InternetAddress.parse(partyRevOrdSndEmail, true);
						if( addrs == null || addrs.length != 1 )
							throw ctx.handler.createDataException(DataException.ERR_INVALID_VALUE,
									ctx.msghandler.getMessage(DataException.ERR_INVALID_VALUE
											, ctx.msghandler.getMessage("jsp.FIELD_DPR_PARTY_ORDREV_SEND_EMAIL")
											, partyRevOrdSndEmail));
					} catch( AddressException addrEx ) {
						throw ctx.handler.createDataException(DataException.ERR_INVALID_VALUE,
								ctx.msghandler.getMessage(DataException.ERR_INVALID_VALUE
										, ctx.msghandler.getMessage("jsp.FIELD_DPR_PARTY_ORDREV_RECEIVE_EMAILS")
										, partyRevOrdSndEmail));
					}
					try {
						com.irt.rbm.RBMSystem.setSystemEnv( ctx.handler, "DPR", ctx.sessionMng.getPartyId()+";"+"partyRevOrdSndEmail", partyRevOrdSndEmail );
					} catch( DataException dataEx ) {
						logger.error("error", dataEx);
						throw dataEx;
					}
					dbRecord.put("partyRevOrdSndEmail", partyRevOrdSndEmail);
				} else {
					try {
						com.irt.rbm.RBMSystem.removeSystemEnv(ctx.handler, "DPR", ctx.sessionMng.getPartyId()+";"+"partyRevOrdSndEmail");
					} catch( DataException dataEx ) {
						logger.error("error", dataEx);
						throw dataEx;
					}
				}
				String partyRevOrdRcvEmails = recordMap.getParameter("partyRevOrdRcvEmails");
				if( partyRevOrdRcvEmails != null && partyRevOrdRcvEmails.length() > 0 ) {
					try {
						javax.mail.internet.InternetAddress.parse(partyRevOrdRcvEmails, true);
					} catch( AddressException addrEx ) {
						throw ctx.handler.createDataException(DataException.ERR_INVALID_VALUE,
								ctx.msghandler.getMessage(DataException.ERR_INVALID_VALUE, partyRevOrdRcvEmails));
					}
					try {
						com.irt.rbm.RBMSystem.setSystemEnv( ctx.handler, "DPR", ctx.sessionMng.getPartyId()+";"+"partyRevOrdRcvEmails", partyRevOrdRcvEmails );
					} catch( DataException dataEx ) {
						logger.error("error", dataEx);
						throw dataEx;
					}
					dbRecord.put("partyRevOrdRcvEmails", partyRevOrdRcvEmails);
				} else {
					throw ctx.handler.createDataException(DataException.ERR_CANNOT_NULL,
									ctx.msghandler.getMessage(DataException.ERR_CANNOT_NULL,
									ctx.msghandler.getMessage("jsp.FIELD_DPR_PARTY_ORDREV_RECEIVE_EMAILS")));
				}
			}
			ctx.req.setAttribute( "record", dbRecord );
			ctx.pageConfig.setManageAuth( true );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_country_input.jsp" );
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
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_COUNTRY_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}
}
