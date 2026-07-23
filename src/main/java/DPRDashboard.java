/*
 *	File Name:	DPRDashboard.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		dpr_dashboard.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/09/27		2.2.2	DPRPartyAuth.AUTH_ALL_READ 추가
 *	jbaek		2023/07/27		2.2.1	Credit Info 기능 개발
 *	hankalam	2021/11/30		2.2.0	create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.dpr.Order;
import com.irt.dpr.OrderDetail;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.usr.UserParty;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;


/*
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRDashboard"})
public class DPRDashboard extends DPRServletModel {
	public static final String MODE_COUNTRY_NOTICE_INFO		= "ninfo";
	public static final int BOARD_ITEM_COUNT_8 = 8;
	public static final int BOARD_ITEM_COUNT_6 = 6;

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_COUNTRY_NOTICE_INFO.equals(ctx.mode) ) return countryNoticeInfo( ctx );

		return super.doRequest( ctx, isPost );
	}

	protected boolean countryNoticeInfo( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> noticeMap = new java.util.HashMap<String, Object>();
		com.irt.custom.PartyLoginBlock loginBlock = (com.irt.custom.PartyLoginBlock)ctx.req.getServletContext().getAttribute("partyLoginBlock");
		if( loginBlock == null ) {
			loginBlock = new com.irt.custom.PartyLoginBlock();
			ctx.req.getServletContext().setAttribute("partyLoginBlock", loginBlock);
		}

		List<Map<String, Object>> noticeList = loginBlock.getNoticeListAt(ctx.handler, ctx.sessionMng.getTimeZone(),
				java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).getTimeInMillis());

		for( int i = 0; i < noticeList.size(); i++ ) {
			Map<String, Object> dataMap = noticeList.get( i );
			String partyId = (String) dataMap.get( "partyId" );
			if( partyId != null && partyId.equals(ctx.sessionMng.getPartyId()) ) {
				noticeMap.putAll( dataMap );
				noticeMap.put( "boardOption1", "T" );
				noticeMap.put( "maintenanceStartDateTime", ((String)noticeMap.get("noticeStartDate")).replace("T", " ") );
				noticeMap.put( "maintenanceEndDateTime", ((String)noticeMap.get("noticeEndDate")).replace("T", " ") );
				noticeMap.put( "timeZone", noticeMap.get("maintenanceTimeZone") );
				break;
			}
		}

		ctx.req.setAttribute( "record", noticeMap );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_boardnotice_info2.jsp" );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		else if( MODE_LIST.equals(ctx.mode) || MODE_COUNTRY_NOTICE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRDashboard.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_DASHBOARD") );
		setPath( ctx, "jsp.MENU_HOME" );
	}

	private void setCreditStatusList( Context ctx ) throws ServletException, SQLException {
		com.irt.dpr.PartyCredit db = new com.irt.dpr.PartyCredit( ctx.handler );

		String[] fieldKeys = new String[]{ "creditPartyCode", "creditPartyName", "soldPartyCode", "creditLimit", "creditLimitCrcy", "accountReceivable", "accountReceivableCrcy", "creditExposure", "creditExposureCrcy", "creditRefDateTime", "creditCurrency", "creditRiskInd" };
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		List<Map<String, Object>> recordList = null;
		String organizationCode = getSavedOrganizationCode( ctx );
		if( organizationCode != null && organizationCode.length() > 0 ) {
			List<Map<String, Object>> dists = getUserSoldParties(ctx);
			String userGroupClass = getUserGroupClass( ctx, ctx.sessionMng.getPartyId(), ctx.sessionMng.getUniqId() );
			if( dists != null && dists.size() >= 1 && ("OR".equals(userGroupClass) || ctx.sessionMng.isSystemAdmin()) ) {
				// serve only first sold-to party
				String soldPartyCode = (String)dists.get(0).get( "partyCode" );
				if( soldPartyCode == null || soldPartyCode.length() == 0 )
					soldPartyCode = (String)dists.get(0).get("soldPartyCode");
				conditionMap.put( "organizationCode", organizationCode );
				conditionMap.put( "distributionChannelCode", dists.get(0).get("distributionChannelCode") );
				conditionMap.put( "soldPartyCode", soldPartyCode );
				recordList = db.getRecords( conditionMap, fieldKeys, 0, 10 );
			}
		}

		if( recordList != null && recordList.size() > 0 )
			ctx.req.setAttribute( "creditStatusList", recordList );
	}

	private void setNotice( Context ctx ) throws ServletModelException, SQLException {
		String countryCode = getUserCountryCode( ctx );
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sites = (List<Map<String, Object>>)ctx.req.getAttribute( "sites" );
		int listCount;
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useCreditStatus") ) {
			listCount = BOARD_ITEM_COUNT_6;
		} else {
			if( sites != null && sites.size() > 0 ) {
				listCount = BOARD_ITEM_COUNT_8;
			} else {
				listCount = BOARD_ITEM_COUNT_6;
			}
		}

		List<Map<String, Object>> notices = new java.util.ArrayList<Map<String, Object>>( listCount );
		List<Map<String, Object>> rbmNoticeList = null;
		List<Map<String, Object>> icsNoticeList = null;
		{
			if( RBMSystem.getSystemEnvBool("DPR", "LoginBlock;usePartyLoginBlock", false) ) {
				com.irt.custom.PartyLoginBlock loginBlock = (com.irt.custom.PartyLoginBlock)ctx.req.getServletContext().getAttribute("partyLoginBlock");
				if( loginBlock == null ) {
					loginBlock = new com.irt.custom.PartyLoginBlock();
					ctx.req.getServletContext().setAttribute("partyLoginBlock", loginBlock);
				}

				List<Map<String, Object>> noticeList = loginBlock.getNoticeListAt(ctx.handler, ctx.sessionMng.getTimeZone(),
						java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).getTimeInMillis());

				for( int i = 0; i < noticeList.size(); i++ ) {
					Map<String, Object> noticeMap = noticeList.get( i );
					String partyId = (String) noticeMap.get( "partyId" );
					if( partyId != null && partyId.equals(ctx.sessionMng.getPartyId()) ) {
						if( rbmNoticeList == null ) {
							rbmNoticeList = new java.util.ArrayList<Map<String, Object>>();
						}
						noticeMap.put( "boardType2", "CS" );
						rbmNoticeList.add( noticeMap );
					}
				}
			}
			com.irt.rbm.rbm.Board rbmBoardDB = new com.irt.rbm.rbm.Board( ctx.handler );
			String boardClassCode = systemConfig.getProperty( "systemNoticeClassCode" );
			String[] fieldKeys = new String[] { "boardClassCode", "boardNumber", "boardType2", "title", "createDateTime", "content", "extraValue"
												, "noticeStartDate", "noticeEndDate", "readedByUser", "updateDateTime" };
			java.util.Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
			java.util.Date today = new java.util.Date();
			conditionMap.put( "boardClassCode", boardClassCode );
			conditionMap.put( "noticeStartDateTime" + com.irt.data.Condition.SUFFIX_MAX_VALUE, today );
			conditionMap.put( "noticeStartDateTime" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MAX );
			conditionMap.put( "noticeStartDateTime" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y" );
			conditionMap.put( "noticeStartDateTime" + com.irt.data.Condition.SUFFIX_IS_TIMESTAMP, "Y" );
			conditionMap.put( "noticeEndDateTime" + com.irt.data.Condition.SUFFIX_MIN_VALUE, today ) ;
			conditionMap.put( "noticeEndDateTime" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN );
			conditionMap.put( "noticeEndDateTime" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y" );
			conditionMap.put( "noticeEndDateTime" + com.irt.data.Condition.SUFFIX_IS_TIMESTAMP, "Y" );

			int recordCount = listCount;
			if( rbmNoticeList != null ) {
				recordCount = recordCount - rbmNoticeList.size();
			}

			List<Map<String, Object>> noticeList = rbmBoardDB.convertLocaleRecordList( rbmBoardDB.getRecords(conditionMap, fieldKeys, 0, recordCount) );
			if( noticeList != null ) {
				for( Map<String, Object> noticeMap : noticeList ) {
					String extraValue = (String)noticeMap.get( "extraValue" );
					if( extraValue != null && extraValue.contains( "maintenanceStart" )
							&&  extraValue.contains( "maintenanceEnd" ) &&  extraValue.contains( "maintenanceTimeZone" ) ) {
						UserParty partyDB = new UserParty( ctx.handler );
						String timezoneStr = (String) partyDB.getFieldValue( Record.createMap("partyId", ctx.sessionMng.getPartyId()), "timeZone" );
						String sdfPattern = "yyyy-MM-dd HH:mm";

						String[] kvs = extraValue.split( ";" );
						TimeZone timezone = TimeZone.getTimeZone( timezoneStr );

						for (int i = 0; i < kvs.length; i++) {
							String[] kv = kvs[i].split( "=" );

							if( "maintenanceStart".equals(kv[0]) || "maintenanceEnd".equals(kv[0]) ) {
								String[] time = kv[1].split( " " );
								noticeMap.put( kv[0], time[0] );
								noticeMap.put( kv[0] + "Time", time[1] );

								try {
									SimpleDateFormat sdf = new SimpleDateFormat( sdfPattern );
									java.util.Date date = sdf.parse( time[0] + " " + time[1] );
									sdf.setTimeZone( timezone );
									noticeMap.put( kv[0] + "DateTime", sdf.format(date) );
								} catch( ParseException e ) {}
							}

							if( "maintenanceTimeZone".equals(kv[0]) ) {
								kv[0] = "timeZone";
								noticeMap.put( kv[0], timezone.getDisplayName(false, TimeZone.SHORT) );
							}
						}
					}
				}

				if( rbmNoticeList == null ) {
					rbmNoticeList = new java.util.ArrayList<Map<String, Object>>();
				}
				rbmNoticeList.addAll( noticeList );
			}
		}

		{
			String boardClassCode = systemConfig.getProperty( "noticeClassCode" );
			if( boardClassCode != null ) {
				String orgnizationcode = getSavedOrganizationCode( ctx );
				if( orgnizationcode != null ) {
					boardClassCode += ( "." + orgnizationcode );
				} else {
					String partyId = ctx.sessionMng.getPartyId();
					if( partyId != null )
						boardClassCode += ( "." + partyId );
				}

				com.irt.ics.Board icsBoardDB = new com.irt.ics.Board( ctx.handler );
				ServletUtility.setSort( ctx.req, icsBoardDB, "boardType#DESC", "boardGroupNumber#DESC", "boardGroupDisplaySeq#ASC", "updateDateTime#DESC", "createDateTime#DESC" );

				java.util.Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
				conditionMap.put( "boardClassCode", boardClassCode );
				conditionMap.put( "userId", ctx.sessionMng.getUniqId() );

				com.irt.data.Date sysDate = null;
				if( sysDate == null ) {
					sysDate = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
				}
				conditionMap.put( "noticeDate", sysDate );

				if( countryCode == null || countryCode.length() == 0 )
					throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
				conditionMap.put( "countryCode", countryCode );

				String[] fieldKeys = new String[] { "boardClassCode", "boardNumber", "boardType", "headwordCode", "headwordSeq", "headwordName", "title", "registUserName"
													, "readedByUser", "updateDateTime", "attachedFileInd", "content" };
				icsNoticeList = icsBoardDB.getRecords( conditionMap, fieldKeys, 0, listCount );
			}


			int rbmNoticeLength = rbmNoticeList != null ? rbmNoticeList.size() : 0;
			int icsNoticeLength = icsNoticeList != null ? icsNoticeList.size() : 0;
			if( rbmNoticeLength + icsNoticeLength > listCount ) {
				if( rbmNoticeLength > listCount / 2 && icsNoticeLength > listCount / 2 ) {
					rbmNoticeLength = listCount / 2;
					icsNoticeLength = listCount / 2;
				} else if( rbmNoticeLength > listCount / 2 ) {
					rbmNoticeLength = listCount - icsNoticeLength;
				} else {
					icsNoticeLength = listCount - rbmNoticeLength;
				}
			}
			if( rbmNoticeList != null ) {
				notices.addAll( rbmNoticeList.subList(0, rbmNoticeLength) );
			}
			if( icsNoticeList != null ) {
				notices.addAll( icsNoticeList.subList(0, icsNoticeLength) );
			}
			ctx.req.setAttribute( "notices", notices );
		}
	}

	private void setOrderList( Context ctx ) throws ServletException, SQLException {
		Order headerDB = new Order(ctx.handler, systemConfig);

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL_READ")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );
			Condition.putConditionValueOnly( conditionMap, "authPartyValue", "Y" );
		}
		Condition.putConditionValueOnly( conditionMap, Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
		String organizationCode = getSavedOrganizationCode(ctx);
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.put( "organizationCode", organizationCode );
		conditionMap.put( "status", Order.STATUS_CREATED );
		conditionMap.put( "recentCount", 10 );

		int count = headerDB.getRecordCount( conditionMap );
		if( count > 0 ) {
			String updateUserId = ctx.sessionMng.getUniqId();

			Map<String, Object> reqConditionMap = new java.util.HashMap<String, Object> ( conditionMap );
			reqConditionMap.put( "countryCode", getUserCountryCode(ctx) );
			reqConditionMap.put( "updateUserId", updateUserId );

			try {
				headerDB.executeEnquiry( com.irt.dpr.Order.ORDER_IF_STATUSLIST, reqConditionMap );
			} catch( DataException dataEx ) {
				ctx.pageConfig.setMessage( dataEx.getMessage() );
			}
		}

		String poOption = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") ) {
			poOption = "PO";
		}
		String orderTypeOption = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
			orderTypeOption = "OT";
		}
		String logisticsQueryOption = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useLogisticsQuery") ) {
			orderTypeOption = "LQ";
		}

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPROrder.DASHBOARD%LIST", poOption, orderTypeOption, logisticsQueryOption );
		ServletUtility.setSort( ctx.req, headerDB, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = headerDB.getRecords( conditionMap, columnList.getFieldKeys() );

		ctx.req.setAttribute( "orderColumnList", columnList );
		ctx.req.setAttribute( "orderList", recordList );
	}

	private void setSiteLink( Context ctx ) throws ServletException, SQLException {
		com.irt.dpr.SiteLink db = new com.irt.dpr.SiteLink( ctx.handler );
		String countryCode = getUserCountryCode( ctx );

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		if( countryCode != null && countryCode.length() > 0 )
			conditionMap.put( "displayCountryCode", countryCode );

		String[] fieldKeys = new String[] { "linkURL", "description", "updateDateTime" };
		ServletUtility.setSort( ctx.req, db, new String[] { "displaySequence" } );
		ctx.req.setAttribute( "sites", db.getRecords(conditionMap, fieldKeys, 0, 10) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		String message = ctx.pageConfig.getMessage();
		if( message != null ) {
			ctx.pageConfig.setProperty( "errorMessage", message );
		}
		setCreditStatusList( ctx );
		setOrderList( ctx );
		setSiteLink( ctx );
		setNotice( ctx );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_dashboard.jsp" );
	}
}
