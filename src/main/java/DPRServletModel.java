/*
 *	File Name:	DPRServletModel.java
 *	Version:	2.2.2
 *
 *	Description:
 *		DPR용 Servlet Model
 *
 *	Note:
 *		error.jsp
 *		error_passwd.jsp
 *		error_session.jsp
 *		systemConfig.getProperty( "uploadPath" )
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/02/29		2.2.2	processRbmUploadLogUploadType() : map에 createDateTime, installDateTime 추가
 *	jbaek		2023/07/27		2.2.1	getUserGroupClass() 추가
 *	hankalam	2021/11/30		2.2.0	신규 UI/UX 적용
 *	jbaek		2020/06/30		2.0.10	pageConfig에 savedOrgCd 코드 추가하여 필요없이 sessionMng.getExtraValue() 콜하지 않을 수 있게 함.
 *	jbaek		2019/11/30		2.0.10	User 기본 조직 기능 추가
 *	jbaek		2019/09/30		2.0.10	NPE 오류 수정
 *	jbaek		2019/06/30		2.0.9	Region 마스터 조회 추가, PackDeal 추가, processRbmUploadLog()추가, getUserDistributorCodes(), getUserSoldParties()추가
 *	jbaek		2018/10/30		2.0.8	PartyLoginBlock 기능 추가. countrySingleOrganization map으로 1개 리턴.
 *	jbaek		2017/09/30		2.0.7	codename 기능 추가
 *	song7981	2016/05/20		2.0.6	setAttributePartyMasterOnExisting Office 조건 오류 수정
 *	hankalam	2015/10/30		2.0.5	웹취약성 수정. organizationCode, distributionChannelCode 파라미터 위험문자 검사
 *	jbaek		2014/09/30		2.0.4	Product Hierarchy Level 기능 추가
 *	jbaek		2013/04/30		2.0.3	Sales Mov 관리: AUTHLEVEL 추가( CPFRServletModel 참고), org별 Mov기능 삭제.
 *	jbaek		2012/07/30		2.0.2	regulate minOrderTotal 기능 추가
 *	jbaek		2011/11/30		2.0.1	OrderInputAuth Condition 체크: isOrderInputAuth() 추가하여 Party의 OrderInput Auth Condition 체크
 *	lsinji		2008/09/26		2.0.0	create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.dpr.Item;
import com.irt.dpr.MasterLink;
import com.irt.dpr.PackDealCfg;
import com.irt.dpr.util.CondPred;
import com.irt.json.Jsoner;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.system.SessionManagerException;
import com.irt.util.DaoManager;
import com.irt.util.MapUtil;

/**
 *
 */
public abstract class DPRServletModel extends AbstractServletModel {//@formatter:off
	public final static String SET_ORGANIZATION				= "setOrg";

	protected final static int AUTHLEVEL_MANAGE			= 9;		// 수정 가능
	protected final static int AUTHLEVEL_MANAGE_NOOWN	= 8;		// 수정 가능(담당자 아님)
	protected final static int AUTHLEVEL_VIEW			= 1;		// 조회 가능(수정 불가)
	protected final static int AUTHLEVEL_NODATA			= 0;		// 데이터 없음
	protected final static int AUTHLEVEL_NOAUTH			= -1;		// 권한 없음

	public final static int DPRPARTY_COUNTRY				= 0x00000001 ;
	public final static int DPRPARTY_PARTY					= 0x00000010 ;

	public final static String USERGROUP_MANAGER			= "DPR_MANAGER";
	public final static String USERGROUP_CDM				= "DPR_CDM";
	public final static String USERGROUP_DISTRIBUTOR		= "DPR_DISTRIBUTOR";

	public final static int PARTYMASTER_CUSTOMERGROUP		= 0x00000001;
	public final static int PARTYMASTER_DISTRIBUTIONCHANNEL	= 0x00000002;
	public final static int PARTYMASTER_DIVISION			= 0x00000004;
	public final static int PARTYMASTER_REGION				= 0x00000008;
	public final static int PARTYMASTER_ORGANIZATION		= 0x00000010;
	public final static int PARTYMASTER_DISTRICT			= 0x00000020;
	public final static int PARTYMASTER_OFFICE				= 0x00000040;
	public final static int PARTYMASTER_GROUP				= 0x00000080;
	public final static int PARTYMASTER_ALL					= PARTYMASTER_CUSTOMERGROUP | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_DIVISION | PARTYMASTER_REGION | PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRICT | PARTYMASTER_OFFICE | PARTYMASTER_GROUP;

	public final static int PARTNER_SOLD					= 0x00000001;
	public final static int PARTNER_SHIP					= 0x00000002;
	public final static int PARTNER_ALL						= PARTNER_SOLD | PARTNER_SHIP;

	public final static String MODE_FRAME					= "frm";
	public final static String NULL_VALUE					= "0";
	protected final static String MODE_CODENAME				= "codename";
	protected final static String MODE_COND_SETTING			= "rtp";
	/**
	 * requiredKeys to get json codenames
	 * map key is reqObj
	 * map val is required fieldKey array for reqObj
	 */
	static Map<String, String[]> codenameRequired = new java.util.HashMap<String, String[]>();
	static {
		codenameRequired.put( "shipParties", new String[] { "partyCode" } );
	}

	protected boolean codename( Context ctx ) throws ServletModelException, SQLException {
		Map<String, Object> paramMap = new ParameterMap( ctx.req );
		String reqObj = (String)paramMap.get( "reqObj" );
		if( reqObj == null ) {
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		} else {
			List<Map<String, Object>> reqObjRet = null;

			// defined reqObj processing
			if( reqObj.equals( "shipParties" ) ) {
				boolean requiredKeyMissed = false;
				String[] requiredKeys = codenameRequired.get( reqObj );
				if( requiredKeys != null && requiredKeys.length > 0 ) {
					for( String key : requiredKeys ) {
						if( !paramMap.containsKey( key ) ) {
//							throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
							requiredKeyMissed = true;
						}
					}
				}

				if( requiredKeyMissed ) {
					reqObjRet = new java.util.ArrayList<Map<String, Object>>();
				} else {
					setAttributePartner( ctx, paramMap, PARTNER_SHIP );
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> shipParties = (List<Map<String, Object>>)ctx.req.getAttribute( "shipParties" );
					if( shipParties != null && shipParties.size() > 0 ) {
						reqObjRet = (List<Map<String, Object>>)MapUtil.getRenamedKeyValues( shipParties, new String[] { "linkPartyCode", "linkPartyName" }, new String[] { "code", "name" } );
					}
				}
			}

			// common json processing
			ctx.res.setContentType( "application/json" );
			ctx.res.setCharacterEncoding("UTF-8");
			java.io.PrintWriter pw = null;
			String json = null;
			try {
				try {
					pw = ctx.res.getWriter();
				} catch( IOException ioEx ) {
					logger.error( ServletModelException.INTERNAL_ERROR, ioEx );
					ctx.res.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
					json = "{\"error\": \"" + ioEx.getMessage() + "\"}";
				}

				if( reqObjRet == null ) {
					json = "{\"error\": \"" + ctx.msghandler.getMessage( ServletModelException.NO_RECORD_FOUND ) + "\"}";
				} else {
					json = new Jsoner().toJson( reqObjRet );
				}

				pw.print( json );
				pw.flush();
			} finally {
				if( pw != null )
					pw.close();
			}
		}

		return true;
	}

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, false, true );
	}

	protected boolean checkAuthorize( Context ctx, boolean checkOrganization ) throws ServletException {
		return checkAuthorize( ctx, checkOrganization, true );
	}

	protected boolean checkAuthorize( Context ctx, boolean checkOrganization, boolean checkSuper ) throws ServletException {
		if( checkSuper )
			super.checkAuthorize( ctx );

		if( checkOrganization ) {
			if( !checkSelectedOrganization(ctx) ) {
				com.irt.dpr.CountryCondition db = new com.irt.dpr.CountryCondition( ctx.handler );
				Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
				conditionMap.put( "countryCode", getUserCountryCode(ctx) );
				conditionMap.put( "conditionInd", com.irt.dpr.CountryCondition.CONDITION_INDICATOR_REGISTRED );

				try {
					List<Map<String, Object>> recordList = db.getRecords( conditionMap, new String[] { "organizationCode" } );
					if( recordList != null && recordList.size() == 1 ) {
						String organizationCode = (String)recordList.get(0).get( "organizationCode" );

						return saveOrganizationCodeToSession( ctx, organizationCode );
					}

					setAttributePartyMaster( ctx, Record.createMap("countryCode", getUserCountryCode(ctx)), PARTYMASTER_ORGANIZATION );
					ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_MAIN_SETTING_ORG") );

					throw new ServletModelException( SET_ORGANIZATION, ctx.msghandler.getMessage("ERR_NEEDED_SELECT_ORGANIZATION") );
				} catch( SQLException sqlEx ) {
					throw new ServletModelException( ServletModelException.INTERNAL_ERROR );
				}
			}
		}

		return true;
	}

	protected boolean checkSelectedOrganization( Context ctx ) {
		return (getSavedOrganizationCode(ctx) == null ? false : true );
	}

	@Override
	protected com.irt.servlet.PageConfig createPageConfig( com.irt.servlet.ServletModel.Context ctx ) throws javax.servlet.ServletException {
		PageConfig thisConfig = super.createPageConfig(ctx);
		thisConfig.setProperty("savedOrgCd", getSavedOrganizationCode(ctx));
		try {
			thisConfig.setProperty("dispLang", getDisplayLanguage(ctx));
		} catch( SQLException ignored ) {
		}
		return thisConfig;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {

		if( RBMSystem.getSystemEnvBool("DPR", "LoginBlock;usePartyLoginBlock", false) ) {
			com.irt.custom.PartyLoginBlock loginBlock = (com.irt.custom.PartyLoginBlock)ctx.req.getServletContext().getAttribute("partyLoginBlock");
			if( loginBlock == null ) {
				loginBlock = new com.irt.custom.PartyLoginBlock();
				ctx.req.getServletContext().setAttribute("partyLoginBlock", loginBlock);
			}

			List<Map<String, Object>> noticeList = loginBlock.getNoticeListAt(ctx.handler, ctx.sessionMng.getTimeZone(),
					java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).getTimeInMillis());
			try {
				if( ctx.sessionMng.getPartyId() != null ) {
					setMaintenanceNotice( ctx, ctx.req, ctx.res, ctx.locale );
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> maintenanceNotices = (List<Map<String, Object>>)ctx.req.getAttribute("maintenanceNotices");
					if( !ctx.sessionMng.isSystemAdmin() && maintenanceNotices != null ) {
						com.irt.rbm.rbm.Board rbmBoardDB = new com.irt.rbm.rbm.Board( ctx.handler );
						for( Map<String, Object> noticeMap : maintenanceNotices ) {
							rbmBoardDB.convertLocaleRecord( noticeMap );
							boolean noticeLoginBlock = "Y".equals( noticeMap.get("boardOption2") );
							String language = ctx.locale.getLanguage();
							if( noticeLoginBlock ) {
								String message = (String) noticeMap.get( "content_" + language );
								if( message == null ) {
									message = ctx.msghandler.getMessage( "MSG_MAINTENANCE" );
								} else {
									message = com.irt.util.StringUtil.evalPlaceholder( message, noticeMap, "{{", "}}" );
								}
								ctx.pageConfig.setMessage( message );
								return forward(ctx, this.systemConfig.getJspPath() + "/logout.jsp");
							}
						}
					}
				}

				if( loginBlock.shouldBlockNormalUser(ctx.handler, ctx.sessionMng.getPartyId(), ctx.sessionMng.getUserClass()) ) {
					String message = null;
					for( Map<String, Object> noticeMap : noticeList ) {
						String partyId = (String) noticeMap.get( "partyId" );
						if( partyId != null && partyId.equals(ctx.sessionMng.getPartyId()) ) {
							message = (String) noticeMap.get( "content" );
							break;
						}
					}

					if( message == null ) {
						message = ctx.msghandler.getMessage( "MSG_MAINTENANCE" );
					}
					ctx.pageConfig.setMessage( message );
					return forward(ctx, this.systemConfig.getJspPath() + "/logout.jsp");
				}
			} catch( IOException ioEx ) {
				throw new ServletModelException("ERR_INTERNAL_ERROR");
			}
		}

		boolean isAllCountryBlock = com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "LoginBlock;All", false);
		boolean isChinaBlock = com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "LoginBlock;100053", false);
		boolean isThaiBlock = com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "LoginBlock;100052", false);

		if( isAllCountryBlock || ( isChinaBlock && "100053".equals(getUserCountryCode(ctx)) )
				|| ( isThaiBlock && "100052".equals(getUserCountryCode(ctx)) ) ) {
			if( !"00".equals(ctx.sessionMng.getUserExtraValue(2)) ) {
				try {
					setMaintenanceNotice( ctx, ctx.req, ctx.res, ctx.locale );
					return forward(ctx, this.systemConfig.getJspPath() + "/maintenance.jsp");
				} catch( IOException ioEx ) {
					throw new ServletModelException("ERR_INTERNAL_ERROR");
				}
			}
		}

		// route
		if( MODE_CODENAME.equals(ctx.mode) )
			return codename(ctx);
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );

		return super.doRequest(ctx, isPost);
	}
	public String getDefaultUnitOfMeasure( Context ctx ) throws SQLException, ServletModelException {
		String defaultUnitOfMeasure = ctx.req.getParameter( "defaultUOM" );
		return com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Default;UnitOfMeasure", defaultUnitOfMeasure );
	}

	private Map<String, Object> countrySingleOrganization = new java.util.HashMap<String,Object>();

	public List<Map<String, Object>> getItemList( Context ctx, Map<String, Object> conditionMap ) throws SQLException {
		com.irt.dpr.Item db = new com.irt.dpr.Item( ctx.handler );
		return db.getItemList( conditionMap );
	}

	public String getSavedOrganizationCode( Context ctx ) {
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 || organizationCode.indexOf(">") >= 0
				|| organizationCode.indexOf("+") >= 0 || organizationCode.indexOf(";") >= 0 )
			organizationCode = ctx.sessionMng.getExtraValue();

		// not in session manager. find possible organization code for session user.
		if( organizationCode == null ) {
			try {
				String countryCode = ctx.sessionMng.getGln();
				synchronized( countrySingleOrganization ) {
					if( countrySingleOrganization.get(countryCode) != null ) {
						logger.debug("got organizationCode from cached countryCode:organizationCode map.");
						organizationCode = (String)countrySingleOrganization.get(countryCode);
					} else {
						try {
							List<Map<String,Object>> organizations = new com.irt.dpr.CountryCondition(ctx.handler).getCountryOrganizations(ctx.sessionMng.getGln(), new String[]{"organizationCode"});
							if( organizations != null && organizations.size() == 1 ) {
								String _organizationCode = (String)organizations.get(0).get("organizationCode");
								if( _organizationCode != null && _organizationCode.length() > 0 ) {
									countrySingleOrganization.put(ctx.sessionMng.getGln(), _organizationCode);
									organizationCode = _organizationCode;
								}
							} else {
								String usrDefaultSorg = ctx.sessionMng.getUserExtraValue(3);
								if( usrDefaultSorg != null && usrDefaultSorg.length() > 0 )
									organizationCode = usrDefaultSorg;
								else
									organizationCode = RBMSystem.getSystemEnv("DPR", "PartyDefaultSorg;"+ctx.sessionMng.getPartyId());
							}
						} catch( SQLException sqlEx ) {
							logger.error(sqlEx.getMessage(), sqlEx);
						}
					}
				}

				if( organizationCode != null && organizationCode.length() > 0 )
					ctx.sessionMng.setExtraValue(organizationCode);
			} catch( SessionManagerException sessionEx ) {
				logger.error(sessionEx.getMessage(), sessionEx);
			}
		}

		return organizationCode;
	}

	public String getDivisionCode( Context ctx ) throws SQLException, ServletModelException {
		String divisionCode = ctx.req.getParameter( "divisionCode" );

		return com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Default;division", divisionCode );
	}

	public String getDistributionChannelCode( Context ctx ) throws SQLException, ServletModelException {
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		if( distributionChannelCode == null || distributionChannelCode.length() == 0 || distributionChannelCode.indexOf("<") >= 0 || distributionChannelCode.indexOf(">") >= 0
				|| distributionChannelCode.indexOf("+") >= 0 || distributionChannelCode.indexOf(";") >= 0 )
			distributionChannelCode = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Default;distributionChannel" );

		return distributionChannelCode;
	}

	public String[] getDistributionChannelCodeArray( Context ctx ) throws SQLException, ServletModelException {
		String[] distributionChannelCodes = ctx.req.getParameterValues( "distributionChannelCode" );
		if( distributionChannelCodes == null || distributionChannelCodes.length < 1 ) {
			distributionChannelCodes = new String[1];
			distributionChannelCodes[0] = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Default;distributionChannel" );
		}
		return distributionChannelCodes;
	}

	protected List<Map<String, Object>> getSelectListNaZeroValue( Context ctx, List<Map<String, Object>> listObj, String codeKey, String nameKey ) {
		if( listObj == null )
			listObj = new java.util.ArrayList<Map<String, Object>>();

		Map<String, Object> obj = com.irt.data.Record.createMap(codeKey, "0");
		obj.put(nameKey, ctx.msghandler.getMessage("MSG_NA_0"));

		listObj.add(0, obj);

		return listObj;
	}

	public List<Map<String, Object>> getUserDistributorCodes( Context ctx ) throws SQLException, ServletModelException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put("divisionCode", getDivisionCode(ctx));
		// conditionMap.put("distributionChannelCode", getDistributionChannelCode(ctx));// can be multiple channel with single sold to
		conditionMap.put("uniqId", ctx.sessionMng.getUniqId());
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		if( !ctx.sessionMng.isSystemAdmin() ) {
			conditionMap.put("authIndicator", "Y");
			conditionMap.put("partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE);
			conditionMap.put("baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD);
		}
		conditionMap.put(com.irt.data.Condition.DISTINCT_CONDITIONKEY, "Y");

		List<Map<String, Object>> recordList = new com.irt.dpr.PartyAuth(ctx.handler).getRecords(conditionMap,
				new String[] { "partyCode", "partyName" });
		return recordList;
	}

	public void setUserPartiesCondition( Context ctx, Map<String, Object> conditionMap, String partyFieldKey ) throws SQLException {
		String partyCode = (String) conditionMap.get( partyFieldKey );
		if( partyCode != null ) {
			return;
		}

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Map<String, Object> _condition = new java.util.HashMap<String, Object>( conditionMap );
			if( !ctx.sessionMng.isSystemAdmin() ) {
				_condition.put( "authIndicator", "Y" );
				_condition.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
				_condition.put( "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
			}
			List<Map<String, Object>> recordList = new com.irt.dpr.PartyAuth(ctx.handler).getRecords( conditionMap, new String[] { "partyCode" } );
			if( recordList != null && recordList.size() > 0 ) {
				conditionMap.put( partyFieldKey, Record.extractObjectArray(recordList, "partyCode") );
			}
		}

	}

	public String getUserDistributorCode( Context ctx ) throws SQLException, ServletModelException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put( "divisionCode", getDivisionCode(ctx) );
		conditionMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );
		conditionMap.put( "uniqId", ctx.sessionMng.getUniqId() );
		conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
		if( !ctx.sessionMng.isSystemAdmin() ) {
			conditionMap.put( "authIndicator", "Y" );
			conditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
			conditionMap.put( "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
		}

		List<Map<String, Object>> recordList = new com.irt.dpr.PartyAuth(ctx.handler).getRecords( conditionMap, new String[] { "partyCode" } );
		if( recordList != null && recordList.size() > 0 ) {
			Map<String, Object> recordMap = recordList.get( 0 );
			if( recordMap != null )
				return (String)recordMap.get( "partyCode" );
		}

		return null;
	}

	public String getUserGroupClass( Context ctx, String partyId, String uniqId ) throws SQLException {
		Map<String, Object> userConditionMap = new java.util.HashMap<String, Object> ();
		userConditionMap.put( "partyId", partyId );

		String userId = (uniqId.indexOf("@") > 0 ? uniqId.substring(0, uniqId.indexOf("@")) : uniqId);
		userConditionMap.put( "userId", userId );

		Map<String, Object> userMap = new com.irt.rbm.usr.UserUser(ctx.handler).getRecord( userConditionMap, new String[] { "groupClass" } );

		return (String)userMap.get( "groupClass" );
	}

	public List<Map<String, Object>> getUserSoldParties( Context ctx ) throws SQLException, ServletModelException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put( "divisionCode", getDivisionCode(ctx) );
		conditionMap.put( "uniqId", ctx.sessionMng.getUniqId() );
		conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
		if( !ctx.sessionMng.isSystemAdmin() ) {
			conditionMap.put( "authIndicator", "Y" );
			conditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
			conditionMap.put( "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
		}

		List<Map<String, Object>> recordList = new com.irt.dpr.PartyAuth(ctx.handler).getRecords( conditionMap, new String[] { "partyCode", "soldPartyCode", "distributionChannelCode", "officeCode", "groupCode" } );
		if( recordList != null && recordList.size() > 0 ) {
			return recordList;
		}

		return null;
	}

	public boolean isOrderInputAuth(Context ctx) throws SQLException, ServletModelException {
		com.irt.dpr.PartyAuth pa = new com.irt.dpr.PartyAuth(ctx.handler);

		Map<String, Object> authConditionMap = new java.util.HashMap<String, Object>();
		authConditionMap.put( "authIndicator", "Y" );
		authConditionMap.put( "partyNotOrderable" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y" );
		authConditionMap.put( "partyNotOrderable", "N" );
		authConditionMap.put( "organizationCode", ctx.sessionMng.getExtraValue() );
		authConditionMap.put( "uniqId", ctx.sessionMng.getUniqId() );
		authConditionMap.put( "partyStatus", "00" );
		authConditionMap.put( "notOrderable" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y" );
		authConditionMap.put( "notOrderable", "N" );

		return ( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() || (pa.getRecordCount(authConditionMap) > 0) );
	}

	/**
	 * 'displayLanguage': DPR_MASTER_DESC 와 관련하여 여러 언어로 구성되어 있는 SAP의 데이터를 표시하기 위한 언어 코드.
	 *		메세지 번들및 칼럼 리소스로 웹 화면에 표시하는 'locale' 언어코드와는 다를 수 있음.
	 */
	public String getDisplayLanguage( Context ctx ) throws SQLException, ServletModelException {
		String displayLanguage = null;
		String localeString = ctx.req.getParameter( com.irt.servlet.ServletModel.PARAM_LOCALE );
		if( localeString != null && localeString.length() > 0 ) {
			String[] locales = localeString.split( "_", 3 );

			if( locales.length >= 1 )
				displayLanguage = locales[0];
		}

		if( displayLanguage == null || displayLanguage.length() <= 0 ) {
			localeString = ctx.sessionMng.getUserExtraValue( 1 );
			if( localeString != null )
				displayLanguage = localeString;

			if( displayLanguage == null || displayLanguage.length() <= 0 ) {
				String countryCode = ctx.sessionMng.getUserExtraValue( 0 );
				if( countryCode != null ) {
					Map<String, Object> primaryMap = com.irt.dpr.Country.createPrimary( countryCode );

					displayLanguage = (String)new com.irt.dpr.Country(ctx.handler).getFieldValue( primaryMap, "languageCode" );
				}
			}
		}

		return checkEnforceDisplayLanguage(ctx, displayLanguage);
//		return (displayLanguage != null ? displayLanguage : com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;locale") );
	}

	private String checkEnforceDisplayLanguage( Context ctx, String sofarDisplayLanguage ) {
		String sysDefaultLocale = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;locale");
		if( sofarDisplayLanguage == null )
			return sysDefaultLocale;

		String partyId = ctx.sessionMng.getPartyId();
		if( partyId != null ) {
			String partyEnforceLangFrom = RBMSystem.getSystemEnv("DPR", "EnforceDisplayLanguageFrom;" + partyId);
			if( partyEnforceLangFrom != null && partyEnforceLangFrom.equals(sofarDisplayLanguage) ) {
				return sysDefaultLocale;
			}
		}

		return sofarDisplayLanguage;
	}

	public String getUserCountryCode( Context ctx ) throws ServletModelException {
		String countryCode = ctx.req.getParameter( "countryCode" );

		return ((countryCode != null && countryCode.length() > 0) ? countryCode : ctx.sessionMng.getUserExtraValue(0) );
	}

	@Override
	protected void printErrorPage( HttpServletRequest req, HttpServletResponse res, String errorKey, String message, boolean sessionError ) throws ServletException, IOException {
		if( SET_ORGANIZATION.equals(errorKey) ) {
			PageConfig pageConfig = (PageConfig)req.getAttribute( "htmlpage" );
			if( pageConfig == null ) req.setAttribute( "htmlpage", pageConfig = createPageConfig(req) );
			pageConfig.setMessage( message );

			req.getRequestDispatcher(systemConfig.getJspPath() + "/dpr_main_org_input.jsp").forward( req, res );
		} else {
			super.printErrorPage( req, res, errorKey, message, sessionError );
		}
	}

	protected void processRbmUploadLogUploadType( Context ctx, UploadLog logDB, String uploadType ) throws SQLException, ServletModelException {
		List<Map<String, Object>> validUploadTypes = logDB.getUploadTypes();
		List<Object> validCodes = (List<Object>)MapUtil.extractValues(validUploadTypes, "code");
		if( !validCodes.contains(uploadType) ) {
			DaoManager pkg = new DaoManager(com.irt.rbm.sys.Schema.class.getCanonicalName(),
					com.irt.rbm.sys.Schema.SYS_SYSTEM_PACKAGE);
			pkg.setSQLHandler(ctx.handler);
			Map<String, Object> map = Record.createMap("systemCode", "RBM");
			map.put("packageCode", "RBMUploadLog.TYPE." + uploadType);
			map.put("packageName", getServletName() + "." + uploadType);
			map.put("parentPackageCode", "RBMUploadLog");
			map.put("createDateTime", com.irt.data.Date.getInstance());
			map.put("installDateTime", com.irt.data.Date.getInstance());
			boolean isUploadTypeError = true;
			try {
				if( pkg.getManager().regist(map) ) {
					isUploadTypeError = false;
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				logger.error(dataEx.getMessage(), dataEx);
			}

			if( isUploadTypeError ) {
				throw new ServletModelException(ServletModelException.INVALID_REQUEST,
						ctx.msghandler.getMessage("ERR_INVALID_TYPE_1", uploadType));
			}
		}
	}

	public Map<String, Object> replaceNullValues( Map<String, Object> sourceMap, String[] fieldKeys, String replaceValue ) {
		for( String fieldKey : fieldKeys ) {
			String value = (String) sourceMap.get( fieldKey );
			if( value == null || value.length() == 0 ) {
				sourceMap.put( fieldKey, replaceValue );
			}
		}
		return sourceMap;
	}

	public boolean saveOrganizationCodeToSession( Context ctx, String organizationCode ) throws ServletModelException, SQLException {
		if( organizationCode == null )
			organizationCode = ctx.req.getParameter( "organizationCode" );

		if( organizationCode == null || organizationCode.indexOf("<") >= 0 || organizationCode.indexOf(">") >= 0
				|| organizationCode.indexOf("+") >= 0 || organizationCode.indexOf(";") >= 0 )   return false;
		try {
			ctx.sessionMng.setExtraValue( organizationCode );
		} catch ( com.irt.system.SessionManagerException sessionEx ) {
			return false;
		}

		return true;
	}

	public void setAttributeCountry( Context ctx ) throws ServletModelException, SQLException {
		com.irt.dpr.Country db = new com.irt.dpr.Country( ctx.handler );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		String uniqId = ctx.req.getParameter( "uniqId" );
		if( uniqId == null )
			uniqId = ctx.sessionMng.getUniqId();

		conditionMap.put( "authUserId", uniqId );

		ctx.req.setAttribute( "countries", db.getRecords(conditionMap, new String[] { "countryCode", "countryName" }) );
	}

	public void setAttributePartner( Context ctx, Map<String, Object> conditionMap ) throws ServletModelException, SQLException {
		setAttributePartner( ctx, conditionMap, PARTNER_ALL );
	}

	public void setAttributePartner( Context ctx, Map<String, Object> conditionMap, int type ) throws ServletModelException, SQLException {
		com.irt.dpr.PartyLink db = new com.irt.dpr.PartyLink( ctx.handler );
		Map<String, Object> partnerConditionMap = new java.util.HashMap<String, Object>(conditionMap);
		partnerConditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		if( partnerConditionMap.get("organizationCode") == null ) {
			String organizationCode = getSavedOrganizationCode( ctx );
			if( organizationCode == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			else
				partnerConditionMap.put( "organizationCode", organizationCode );
		}

		if( partnerConditionMap.get("divisionCode") == null )
			partnerConditionMap.put( "divisionCode", com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;division") );
		if( partnerConditionMap.get("distributionChannelCode") == null )
			partnerConditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");
			//partnerConditionMap.put( "distributionChannelCode", com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;distributionChannel") );
		if( partnerConditionMap.get("countryCode") == null )
			partnerConditionMap.put( "countryCode", getUserCountryCode(ctx) );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			partnerConditionMap.put( "authUniqId", ctx.sessionMng.getUniqId() );
			partnerConditionMap.put( "authPartyValue", "Y" );
		}
		partnerConditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
		partnerConditionMap.put( "linkStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );

		String[] fieldKeys = new String[] { "linkPartyCode", "linkPartyName" };
		if( (type & PARTNER_SOLD) > 0 ) {
			partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
			partnerConditionMap.remove( "partyCode" );
			partnerConditionMap.remove( "soldPartyCode" );

			db.setSort( fieldKeys );
			ctx.req.setAttribute( "soldParties", db.getRecords( partnerConditionMap, fieldKeys) );
		}

		if( (type & PARTNER_SHIP) > 0 ) {
			partnerConditionMap = new java.util.HashMap<String, Object>( partnerConditionMap );
			partnerConditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");
			partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SHIP );
			if( partnerConditionMap.get("partyCode") == null && partnerConditionMap.get("soldPartyCode") != null )
				partnerConditionMap.put( "partyCode", partnerConditionMap.remove("soldPartyCode") );

			db.setSort( fieldKeys );
			List<Map<String, Object>> recordList = db.getRecords( partnerConditionMap, fieldKeys);
			ctx.req.setAttribute( "shipParties", recordList );
		}
	}

	public void setAttributePartyMasterOnExisting( Context ctx, Map<String, Object> conditionMap, int type ) throws SQLException, ServletException {
		MasterLink db = new MasterLink( ctx.handler );

		Map<String, Object> masterConditionMap = new java.util.HashMap<String, Object> ();
		masterConditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		masterConditionMap.put( "masterOrganizationCode", getSavedOrganizationCode(ctx) );
		masterConditionMap.put( "masterCode", getSavedOrganizationCode(ctx) );

		if( (type & PARTYMASTER_CUSTOMERGROUP) > 0 ) {
			masterConditionMap.put( "linkType", MasterLink.MASTERTYPE_CUSTOMERGROUP );
			ctx.req.setAttribute( "customerGroups", db.getRecords(masterConditionMap, new String[] { "code", "name" } ) );
		}
		if( (type & PARTYMASTER_DISTRIBUTIONCHANNEL) > 0 ) {
			setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_DISTRIBUTIONCHANNEL );
		}
		if( (type & PARTYMASTER_REGION) > 0 ) {
			masterConditionMap.put( "linkType", MasterLink.MASTERTYPE_REGION );
			ctx.req.setAttribute( "regions", db.getRecords(masterConditionMap, new String[] { "code", "name", "regionCode", "regionName" } ) );
		}
		if( (type & PARTYMASTER_DISTRICT) > 0 ) {
			masterConditionMap.put( "linkType", MasterLink.MASTERTYPE_SALES_DISTRICT );
			ctx.req.setAttribute( "districts", db.getRecords(masterConditionMap, new String[] { "code", "name" } ) );
		}
		if( (type & PARTYMASTER_GROUP) > 0 ) {
			String officeCode = ctx.req.getParameter( "officeCode" );
			Map<String, Object> _masterConditionMap = new java.util.HashMap<String, Object> ( masterConditionMap );
			if( officeCode != null && officeCode.length() > 0 )
				_masterConditionMap.put( "officeCode", officeCode );

			_masterConditionMap.put( "linkType", MasterLink.MASTERTYPE_SALES_GROUP );
			ctx.req.setAttribute( "groups", db.getRecords(_masterConditionMap, new String[] { "code", "name" } ) );
		}
		if( (type & PARTYMASTER_OFFICE) > 0 ) {
			String organizationCode = ctx.req.getParameter( "organizationCode" );
			Map<String, Object> _masterConditionMap = new java.util.HashMap<String, Object> ( masterConditionMap );
			if( organizationCode != null && organizationCode.length() > 0 )
				_masterConditionMap.put( "organizationCode", organizationCode );

			_masterConditionMap.put( "linkType", MasterLink.MASTERTYPE_SALES_OFFICE );
			ctx.req.setAttribute( "offices", db.getRecords(_masterConditionMap, new String[] { "code", "name" } ) );
		}

		if( (type & PARTYMASTER_ORGANIZATION) > 0 )
			setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );
	}

	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		throw new ServletModelException( ServletModelException.INVALID_MODE );
	}

	protected void setAttributeCondition( Context ctx, Map<String, Object> conditionMap, String types ) throws SQLException, ServletException {
		if( types == null || types.length() < 1 ) {
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );

		String[] typeArray = types.split( ";" );
		for( String type : typeArray ) {
			switch( type.trim() ) {
				case "OG": {
					setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );
					break;
				}
				case "DC": {
					setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_DISTRIBUTIONCHANNEL );
					break;
				}
				case "SO": {
					setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_OFFICE );
					break;
				}
				case "SG": {
					setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_GROUP );
					break;
				}
				case "SOLD": {
					setAttributePartner( ctx, conditionMap, PARTNER_SOLD );
					break;
				}
				case "SHIP": {
					setAttributePartner( ctx, conditionMap, PARTNER_SHIP );
					break;
				}
				case "PT": {
					Map <String, Object> tmpConditionMap = new java.util.HashMap<String, Object> ();
					String organizationCode = (String) conditionMap.get("organizationCode");
					if( organizationCode == null ) {
						organizationCode = getSavedOrganizationCode( ctx );
					}
					tmpConditionMap.put( "organizationCode", organizationCode );
					tmpConditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");
					com.irt.dpr.Plant db = new com.irt.dpr.Plant( ctx.handler );
					db.setSort( "linkPlantCode" );
					List<Map<String, Object>> plantList = db.getRecords( tmpConditionMap,new String[] { "linkPlantCode", "linkPlantName" } );

					ctx.req.setAttribute( "plants", plantList );
					break;
				}
			}
		}
		ctx.req.setAttribute( "attributeTypes", typeArray );
	}

	public void setAttributePartyMaster( Context ctx, Map<String, Object> conditionMap, int type ) throws SQLException, ServletException {
		com.irt.rbm.QueryableManagerImpl db;
		Map<String, Object> masterConditionMap = new java.util.HashMap<String, Object> ();
		masterConditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		masterConditionMap.put( "masterOrganizationCode", getSavedOrganizationCode(ctx) );

		if( (type & PARTYMASTER_CUSTOMERGROUP) > 0 ) {
			db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_CUSTOMER_GROUP );
			db.setSort( "customerGroupCode" );
			ctx.req.setAttribute( "customerGroups", db.getRecords(masterConditionMap, new String[] { "customerGroupCode", "customerGroupName" }) );
		}

		if( (type & PARTYMASTER_DISTRIBUTIONCHANNEL) > 0 ) {
			String countryCode = (String)conditionMap.get( "countryCode" );
			String organizationCode = (String)conditionMap.get( "organizationCode" );

			if( countryCode == null || countryCode.length() == 0 || organizationCode == null || organizationCode.length() == 0 ) {
				db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_DISTRIBUTIONCHANNEL );
				db.setSort( "channelCode" );

				ctx.req.setAttribute( "distributionChannels", db.getRecords(masterConditionMap, new String[] { "channelCode", "channelName" } ) );
			} else {
				db = new com.irt.dpr.CountryDistChannel( ctx.handler );

				Map<String, Object> tmpConditionMap = new java.util.HashMap<String, Object> ( masterConditionMap );
				tmpConditionMap.put( "countryCode", countryCode );
				tmpConditionMap.put( "organizationCode", organizationCode );

				ctx.req.setAttribute( "distributionChannels", db.getRecords(tmpConditionMap, new String[] { "distributionChannelCode", "distributionChannelName" } ) );
			}
		}

		if( (type & PARTYMASTER_DIVISION) > 0 ) {
			db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_DIVISION );
			db.setSort( "divisionCode" );
			ctx.req.setAttribute( "divisions", db.getRecords(masterConditionMap, new String[] { "divisionCode", "divisionName" }) );
		}

		if( (type & PARTYMASTER_REGION) > 0 ) {
			db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_REGION );
			Map<String, Object> tmpConditionMap = new java.util.HashMap<String, Object> ( masterConditionMap );
			Object countryKey = conditionMap.get( "countryKey" );

			if( countryKey == null ) {
				String countryCode = ctx.sessionMng.getUserExtraValue( 0 );
				countryKey = (new com.irt.dpr.Country(ctx.handler)).getFieldValue( com.irt.dpr.Country.createPrimary(countryCode), "countryKey" );
			}
			tmpConditionMap.put( "countryKey", countryKey );
			db.setSort( "regionCode" );
			ctx.req.setAttribute( "regions", db.getRecords(tmpConditionMap, new String[] { "regionCode", "regionName" }) );
		}

/* organizationCode에 대한 권한 처리 */
		if( (type & PARTYMASTER_ORGANIZATION) > 0 ) {
			Object countryCode = conditionMap.get( "countryCode" );
			Map<String, Object> tempConditionMap = new java.util.HashMap<String, Object>( masterConditionMap );

			if( countryCode == null )
				db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_SALES_ORGANIZATION );
			else {
				tempConditionMap.put( "countryCode", countryCode );
				tempConditionMap.put( "conditionInd", com.irt.dpr.CountryCondition.CONDITION_INDICATOR_REGISTRED );
				db = new com.irt.dpr.CountryCondition( ctx.handler );
			}

			db.setSort( "organizationCode" );
			ctx.req.setAttribute( "organizations", db.getRecords(tempConditionMap, new String[] { "organizationCode", "organizationName" }) );
		}

		if( (type & PARTYMASTER_DISTRICT) > 0 ) {
			db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_SALES_DISTRICT );
			db.setSort( "districtCode" );
			ctx.req.setAttribute( "districts", db.getRecords(masterConditionMap, new String[] { "districtCode", "districtName" }) );
		}

		if( (type & PARTYMASTER_OFFICE) > 0 ) {
			db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_SALES_OFFICE );
			Map<String, Object> tempConditionMap = new java.util.HashMap<String, Object>( masterConditionMap );
			if( conditionMap.containsKey("organizationCode") )
				tempConditionMap.put( "organizationCode", conditionMap.get("organizationCode") );

			db.setSort( "officeCode" );
			ctx.req.setAttribute( "offices", db.getRecords(tempConditionMap, new String[] { "officeCode", "officeName" }) );
		}

		if( (type & PARTYMASTER_GROUP) > 0 ) {
			String officeCode = ctx.req.getParameter( "officeCode" );
			Map<String, Object> tmpCondition = new java.util.HashMap<String, Object> (masterConditionMap);
			if( conditionMap.containsKey("organizationCode") )
				tmpCondition.put( "organizationCode", conditionMap.get("organizationCode") );
			tmpCondition.put( "officeCode", officeCode );
			tmpCondition.put(Condition.DISTINCT_CONDITIONKEY, "Y");
			db = new com.irt.dpr.PartyMaster( ctx.handler, com.irt.dpr.PartyMaster.IDX_SALES_GROUP );
			db.setSort( "groupCode" );
			ctx.req.setAttribute( "groups", db.getRecords(tmpCondition, new String[] { "groupCode", "groupName" }) );
		}
	}

	public void setDefaultParameter( Context ctx, Map<String, Object> conditionMap ) throws ServletModelException, SQLException {
		String divisionCode = getDivisionCode( ctx );
		String distributionChannelCode = getDistributionChannelCode( ctx );
		String organizationCode = getSavedOrganizationCode( ctx );

		boolean neededParameter = false;
		if( divisionCode == null || divisionCode.length() == 0 ) {
			divisionCode = (String)conditionMap.get( "divisionCode" );
			if( divisionCode == null || divisionCode.length() == 0 )
				neededParameter = true;
		} else if( (distributionChannelCode == null || distributionChannelCode.length() == 0) ) {
			distributionChannelCode = (String)conditionMap.get( "distributionChannelCode" );
			if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
				neededParameter = true;
		} else if( organizationCode == null || organizationCode.length() == 0 ) {
			organizationCode = (String)conditionMap.get( "organizationCode" );
			if( organizationCode == null || organizationCode.length() == 0 )
				neededParameter = true;
		}

		if( neededParameter )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		ctx.pageConfig.setProperty( "divisionCode", divisionCode );
		ctx.pageConfig.setProperty( "distributionChannelCode", distributionChannelCode );
		ctx.pageConfig.setProperty( "organizationCode", organizationCode );

		conditionMap.put( "divisionCode", divisionCode );
		conditionMap.put( "distributionChannelCode", distributionChannelCode );
		conditionMap.put( "organizationCode", organizationCode );
		conditionMap.put( "masterOrganizationCode", organizationCode );
	}

	public void setDefaultParameterMultiDist( Context ctx, Map<String, Object> conditionMap ) throws ServletModelException, SQLException {
		String divisionCode = getDivisionCode( ctx );
		String[] distributionChannelCodes = getDistributionChannelCodeArray( ctx );
		String organizationCode = getSavedOrganizationCode( ctx );

		boolean neededParameter = false;
		if( divisionCode == null || divisionCode.length() == 0 ) {
			divisionCode = (String)conditionMap.get( "divisionCode" );
			if( divisionCode == null || divisionCode.length() == 0 )
				neededParameter = true;
		} else if( (distributionChannelCodes == null || distributionChannelCodes.length == 0) ) {
			distributionChannelCodes = new String[1];
			distributionChannelCodes[0] = (String)conditionMap.get( "distributionChannelCode" );
			if( distributionChannelCodes[0] == null || distributionChannelCodes[0].length() == 0 )
				neededParameter = true;
		} else if( organizationCode == null || organizationCode.length() == 0 ) {
			organizationCode = (String)conditionMap.get( "organizationCode" );
			if( organizationCode == null || organizationCode.length() == 0 )
				neededParameter = true;
		}

		if( neededParameter )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String distributionChannelCode = "";
		for( String code : distributionChannelCodes ) {
			distributionChannelCode += ";" + code;
		}
		ctx.pageConfig.setProperty( "divisionCode", divisionCode );
		ctx.pageConfig.setProperty( "distributionChannelCode", distributionChannelCode.substring(1) );
		ctx.pageConfig.setProperty( "organizationCode", organizationCode );

		conditionMap.put( "divisionCode", divisionCode );
		conditionMap.put( "distributionChannelCode", distributionChannelCodes );
		conditionMap.put( "organizationCode", organizationCode );
		conditionMap.put( "masterOrganizationCode", organizationCode );
	}

	public void setUserAuthParty( Context ctx, Map<String, Object> conditionMap ) throws ServletModelException, SQLException {
		Map<String, Object> tmpConditionMap = new java.util.HashMap<String, Object>( conditionMap );

		if( tmpConditionMap.get("divisionCode") == null )
			tmpConditionMap.put( "divisionCode", com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;division") );
		if( tmpConditionMap.get("distributionChannelCode") == null )
			tmpConditionMap.put( "distributionChannelCode", com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;distributionChannel") );
		if( tmpConditionMap.get("organizationCode") == null )
			tmpConditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );

		tmpConditionMap.put( "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
		tmpConditionMap.put( "status", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );

		tmpConditionMap.remove( "partyCode" );

		com.irt.dpr.Party db = new com.irt.dpr.Party( ctx.handler );

		if( !ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() ) {
			tmpConditionMap.put( "authUniqId", ctx.sessionMng.getUniqId() );

			if( ctx.sessionMng.isCountryAdmin() )
				tmpConditionMap.put( "authCountryValue", "Y" );
			else
				tmpConditionMap.put( "authPartyValue", "Y" );
		}
		db.setSort( new String[] { "partyCode" } );

		ctx.req.setAttribute( "authParties", db.getRecords(tmpConditionMap, new String[] { "partyCode", "partyName" }) );
	}

	public void setAttributePackDealCfg( Context ctx, Map<String,Object> conditionMap ) throws SQLException {
		List<Map<String, Object>> recordList = null;

		String[] fieldKeys = new String[] { "dealCode" };

		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));

		conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");

		PackDealCfg cfg = new PackDealCfg(ctx.handler);

		recordList = cfg.getRecords(conditionMap, fieldKeys);

		ctx.req.setAttribute("packdeals", recordList);
	}

	protected void setAttributeOrganizationBrands( Context ctx ) throws SQLException, ServletModelException {
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");
		CondPred.putIsNotNull(conditionMap, "brandCode");

		 ctx.req.setAttribute("brands", (new Item(ctx.handler)).getBrands(conditionMap));
	}
}
