/*
 *	File Name:	Menu.java
 *	Version:	2.2.5
 *
 *	Description:
 *		IMPORTANT! customize 대상파일
 *
 *	Note:
 *		menu_index.jsp
 *		menu_top.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.5	신규 UI/UX 적용
 *	jbaek		2019/11/30		2.2.4	User 기본 조직 기능 추가
 *	jbaek		2015/01/07		2.2.3	menu(): savedOrganization 관련 recordList null체크 추가.
 *	jbaek		2011/11/30		2.2.2	OrderInputAuth Condition 체크: menu(): setProperty( "isOrderInputAuth" ) 추가
 *	lsinji		2008/09/26		2.2.1	initContext(): DPR 추가
 *										menu(): setProperty( "organizationCount" ) 추가
 *	stghr12		2008/03/31		2.2.0	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.1.1	initContext(): SRD 추가
 *	stghr12		2007/04/30		2.1.0	no-changed
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/06/18		1.0.0	create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/Menu"})
public class Menu extends DPRServletModel {
	public final static String MODE_INDEX				= "index";
	public final static String MODE_MENU				= "menu";

	@Override
	protected boolean defaultReq( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		return index( ctx );
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_INDEX.equals(ctx.mode)) return index( ctx );
		if( MODE_MENU.equals(ctx.mode)) return menu( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "RBM" );
	}

	protected boolean index( Context ctx ) throws IOException, ServletException, SQLException {
		String alerted = ctx.req.getParameter( "alerted" );
		if( "N".equals(alerted) ) {
			int passChangedDays = new com.irt.dpr.Login(ctx.handler).getPassChangePasswordDays( ctx.sessionMng.getUniqId() );
			int defaultDays = com.irt.rbm.RBMSystem.getSystemEnvInt( "DPR", "Default;ExpireAlertDay", 10 );
			int termOfExpireDays = com.irt.rbm.RBMSystem.getSystemEnvInt( "DPR", "Default;TermOfExpireDays", 90 );
			if( termOfExpireDays - defaultDays <= passChangedDays ) {
				ctx.req.setAttribute( "expireAlert", "Y" );
				ctx.req.setAttribute( "remainExpireAlertDay", String.valueOf(termOfExpireDays - passChangedDays) );
			}
			ctx.pageConfig.setTitle( systemConfig.getSystemName() );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_user_alert.jsp" );
		} else {
			com.irt.dpr.CountryCondition db = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> conditionMap = com.irt.data.Record.createMap( "countryCode", ctx.sessionMng.getGln());
			conditionMap.put( "conditionInd", com.irt.dpr.CountryCondition.CONDITION_INDICATOR_REGISTRED );
			conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

			java.util.List<Map<String, Object>> recordList = db.getRecords( conditionMap, new String[] { "organizationCode", "organizationName" } );
			int count = 0;
			if( recordList != null )
				count = recordList.size();

			ctx.pageConfig.setProperty( "organizationCount", String.valueOf(count) );

			String savedOrganizationCode = getSavedOrganizationCode(ctx);
			String mesgOrganization = null;
			if( count == 1 ) {
				savedOrganizationCode = (String)recordList.get(0).get( "organizationCode" );
				mesgOrganization = (String)recordList.get(0).get( "organizationName" );

				saveOrganizationCodeToSession( ctx, savedOrganizationCode );
			}

			if( savedOrganizationCode != null && savedOrganizationCode.length() > 0 && recordList != null ) {
				ctx.pageConfig.setProperty( "savedOrganizationCode", savedOrganizationCode );

				if( mesgOrganization == null ) {
					for( int i = 0; i < recordList.size(); i++ ) {
						Map<String, Object> map = recordList.get(i);
						if( ((String)map.get("organizationCode")).equals(savedOrganizationCode) ) {
							mesgOrganization = (String)map.get( "organizationName" );

							break;
						}
					}

					mesgOrganization = ctx.msghandler.getMessage( "jsp.menu_top.MSG_ORGANIZATION_USING" ) + mesgOrganization;
				}

				ctx.pageConfig.setProperty( "organizationName", (String)recordList.get(0).get("organizationName") );

				ctx.pageConfig.setProperty( "isOrderInputAuth" , isOrderInputAuth(ctx) ? "Y" : "N" );
			} else
				mesgOrganization = ctx.msghandler.getMessage( "ERR_NEEDED_SELECT_ORGANIZATION" );

			setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );
			ctx.pageConfig.setProperty( "mesgOrganization", mesgOrganization );

			ctx.pageConfig.setTitle( systemConfig.getSystemName() );
			return forward( ctx, systemConfig.getJspPath() + "/dpr_main_page.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );

		String type = ctx.req.getParameter( "type" );
		if( "ecs".equals(type) )
			ctx.pageConfig.setSystemPackageCode( "RBM", "Menu.ECS" );
		else if( "pds".equals(type) )
			ctx.pageConfig.setSystemPackageCode( "RBM", "Menu.PDS" );
		else if( "cpfr".equals(type) )
			ctx.pageConfig.setSystemPackageCode( "RBM", "Menu.CPFR" );
		else if( "srd".equals(type) )
			ctx.pageConfig.setSystemPackageCode( "RBM", "Menu.SRD" );
		else if( "dpr".equals(type) )
			ctx.pageConfig.setSystemPackageCode( "RBM", "Menu.DPR" );
		else
			ctx.pageConfig.setSystemPackageCode( "RBM", "Menu" );
	}

	protected boolean menu( Context ctx ) throws IOException, ServletException, SQLException {
		com.irt.dpr.CountryCondition db = new com.irt.dpr.CountryCondition( ctx.handler );
		Map<String, Object> conditionMap = com.irt.data.Record.createMap( "countryCode", ctx.sessionMng.getGln());
		conditionMap.put( "conditionInd", com.irt.dpr.CountryCondition.CONDITION_INDICATOR_REGISTRED );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		java.util.List<Map<String, Object>> recordList = db.getRecords( conditionMap, new String[] { "organizationCode", "organizationName" } );
		int count = 0;
		if( recordList != null )
			count = recordList.size();

		ctx.pageConfig.setProperty( "organizationCount", String.valueOf(count) );

		String savedOrganizationCode = getSavedOrganizationCode(ctx);
		String mesgOrganization = null;
		if( count == 1 ) {
			savedOrganizationCode = (String)recordList.get(0).get( "organizationCode" );
			mesgOrganization = (String)recordList.get(0).get( "organizationName" );

			saveOrganizationCodeToSession( ctx, savedOrganizationCode );
		}

		if( savedOrganizationCode != null && savedOrganizationCode.length() > 0 && recordList != null ) {
			ctx.pageConfig.setProperty( "savedOrganizationCode", savedOrganizationCode );

			if( mesgOrganization == null ) {
				for( int i = 0; i < recordList.size(); i++ ) {
					Map<String, Object> map = recordList.get(i);
					if( ((String)map.get("organizationCode")).equals(savedOrganizationCode) ) {
						mesgOrganization = (String)map.get( "organizationName" );

						break;
					}
				}

				mesgOrganization = ctx.msghandler.getMessage( "jsp.menu_top.MSG_ORGANIZATION_USING" ) + mesgOrganization;
			}

			ctx.pageConfig.setProperty( "organizationName", (String)recordList.get(0).get("organizationName") );

			ctx.pageConfig.setProperty( "isOrderInputAuth" , isOrderInputAuth(ctx) ? "Y" : "N" );
		} else
			mesgOrganization = ctx.msghandler.getMessage( "ERR_NEEDED_SELECT_ORGANIZATION" );

		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );
		ctx.pageConfig.setProperty( "mesgOrganization", mesgOrganization );

		return forward( ctx, systemConfig.getJspPath() + "/menu_top.jsp" );
	}
}
