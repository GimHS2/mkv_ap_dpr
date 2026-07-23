/*
 *	File Name:	DPRMain.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.4	homebase optimization
 *	jbaek		2017/09/30		2.2.3	info(): add notices sort order by updated date and create date
 *	hankalam	2017/02/28		2.2.2	Notice Header Point 기능 추가
 *	jbaek		2014/06/30		2.2.1	Document Manage 기능 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.data.Record;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRMain"})
public class DPRMain extends DPRServletModel {
	public final static String MODE_SETORG				= "def";

	public final static String TYPE_SITELINK			= "site";
	public final static String TYPE_NOTICE				= "notice";

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean defaultReq( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		return forward( ctx, systemConfig.getJspPath() + "/dpr_main_page.jsp" );
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_SETORG.equals(ctx.mode) ) {
			return setOrganization( ctx );
		}

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRMain.INF" );
		else if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRMain.INF" );
		else if( MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_SETORG.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		if( MODE_SETORG.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_MAIN_SETTING_ORG") );
		else
			pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_MAIN_PAGE") );
	}

	@Override
	protected boolean info( Context ctx ) throws IOException, ServletException, SQLException {
		String type = ctx.req.getParameter( "type" );

		String countryCode = getUserCountryCode( ctx );

		if( TYPE_NOTICE.equals(type) ) {
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

				com.irt.ics.Board db = new com.irt.ics.Board( ctx.handler );
				ServletUtility.setSort( ctx.req, db, "boardType#DESC", "boardGroupNumber#DESC", "boardGroupDisplaySeq#ASC", "updateDateTime#DESC", "createDateTime#DESC" );

				Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
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

				String[] fieldKeys = new String[] { "boardClassCode", "boardNumber", "boardType", "title", "registUserName", "readedByUser", "updateDateTime", "attachedFileInd" };
				Integer skipRows = 0;
				Integer maxRows = 10;
				try { skipRows = Integer.parseInt(ctx.req.getParameter("skipRows")); } catch( NumberFormatException ignored ) { }
				try { maxRows = Integer.parseInt(ctx.req.getParameter("maxRows")); } catch( NumberFormatException ignored ) { }
				ctx.req.setAttribute( "notices", db.getRecords(conditionMap, fieldKeys, skipRows, maxRows) );
			}

			return forward( ctx, systemConfig.getJspPath() + "/dpr_main_notice.jsp" );
		} else {
			com.irt.dpr.SiteLink db = new com.irt.dpr.SiteLink( ctx.handler );

			Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
			if( countryCode != null && countryCode.length() > 0 )
				conditionMap.put( "displayCountryCode", countryCode );

			String[] fieldKeys = new String[] { "linkURL", "description" };
			ServletUtility.setSort( ctx.req, db, new String[] { "displaySequence" } );
			ctx.req.setAttribute( "sites", db.getRecords(conditionMap, fieldKeys, 0, 10) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_main_sitelink.jsp" );
		}
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		setAttributePartyMaster( ctx, Record.createMap("countryCode", getUserCountryCode(ctx)), PARTYMASTER_ORGANIZATION );
		ctx.pageConfig.setProperty( "organizationCode", getSavedOrganizationCode(ctx) );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_main_org_input.jsp" );
	}

	protected boolean setOrganization( Context ctx ) throws IOException, ServletException, SQLException {
		String organizationCode = ctx.req.getParameter( "organizationCode" );

		boolean ret = saveOrganizationCodeToSession( ctx, null );
		String completed = ( ret ? "OK" : "ER" );

		setAttributePartyMaster( ctx, Record.createMap("countryCode", getUserCountryCode(ctx)), PARTYMASTER_ORGANIZATION );
		ctx.pageConfig.setProperty( "completed", completed );
		ctx.pageConfig.setProperty( "organizationCode", getSavedOrganizationCode(ctx) );
		ctx.pageConfig.setProperty( "savedOrganizationCode", getSavedOrganizationCode(ctx) );

		String returnPage = ctx.req.getParameter( "rtype" );
		if( "main".equals(returnPage) )
			return sendRedirect( ctx, ctx.pageConfig.getBackURL() );
		else
			return forward( ctx, systemConfig.getJspPath() + "/dpr_main_org_input.jsp" );
	}
}
