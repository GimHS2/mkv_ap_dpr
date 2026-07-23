/*
 *	File Name:	DPRSiteLink.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_sitelink_input.jsp
 *		dpr_sitelink_confirm.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	hankalam	2020/12/31		2.2.1	URL White List 에 있는 도메인만 redirect 되도록 수정
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.SiteLink;
import com.irt.data.DataException;
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
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRSiteLink"})
public class DPRSiteLink extends DPRServletModel {
	private final static String MODE_LINK				= "link";
	private final static String MODE_WICOCONRIM			= "wcf";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap(ctx.req, true);
		conditionMap.put( "displayCountryCode", getUserCountryCode(ctx) );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_LINK.equals(ctx.mode) )
			return link( ctx );
		else if( MODE_WICOCONRIM.equals(ctx.mode) )
			return wico( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		SiteLink db = (SiteLink)ctx.db;

		String countryCode = getUserCountryCode( ctx );
		String linkSequence = ctx.req.getParameter( "linkSequence" );

		if( linkSequence == null || linkSequence.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = SiteLink.createPrimary( linkSequence, countryCode );

		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		recordMap.put("countryCode", countryCode);
		ctx.req.setAttribute( "record", recordMap );

		if( inputting ) {
			ctx.req.setAttribute( "countries", new com.irt.dpr.Country(ctx.handler).getRecords(null, new String[] { "countryCode", "partyId", "countryName"} ) );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

			return registInput( ctx );
		}

// 임시
ctx.pageConfig.setManageAuth( true );

		ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_sitelink_input.jsp" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSiteLink.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSiteLink.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSiteLink.MNG" );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSiteLink.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSiteLink.MNG" );
		else if( MODE_LINK.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_WICOCONRIM.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new SiteLink( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_SITELINK_"+ ctx.mode.toUpperCase()) );
	}

	protected boolean link( Context ctx ) throws IOException, ServletException, SQLException {
		SiteLink db = (SiteLink)ctx.db;

		String linkSequence = ctx.req.getParameter( "linkSequence" );
		if( linkSequence == null || linkSequence.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map record = db.getRecord( SiteLink.createPrimary(linkSequence, getUserCountryCode(ctx)) );
		String linkURL = null;
		if( record != null )
			linkURL = (String)record.get( "linkURL" );

		if( linkURL != null )
			return forward( ctx, linkURL );

		return false;
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		SiteLink db = (SiteLink)ctx.db;
		// Condition Map
		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRSiteLink%LIST" );
		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );

		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setAttributeCountry( ctx );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

/* WORKING */
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_sitelink_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRSiteLink.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((SiteLink)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		SiteLink db = (SiteLink)ctx.db;

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();

			String countryCode = getUserCountryCode( ctx );
			recordMap.put( "displayCountryCode", countryCode );
			recordMap.put( "countryName", new com.irt.dpr.Country(ctx.handler).getCountryName(countryCode) );

			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
			ctx.req.setAttribute( "record", recordMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_sitelink_input.jsp" );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		SiteLink db = (SiteLink)ctx.db;

		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		if( inserting )
			recordMap.put( "linkSequence", db.getlinkSequence() );

		if( !recordMap.containsKey("displayCountryCode") )
			recordMap.put( "displayCountryCode", getUserCountryCode(ctx) );

		recordMap.put( "displaySequence", db.getMaxDisplaySequence((String)recordMap.get("displayCountryCode")) + 1 );

		try {
			if( inserting ) {
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			ctx.req.setAttribute( "record", db.getRecord(
				SiteLink.createPrimary( recordMap.get("linkSequence"), recordMap.get("displayCountryCode")) ) );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_sitelink_input.jsp" );
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
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_USER_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}

    @Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
        SiteLink db = (SiteLink)ctx.db;
        String linkSequences[] = ctx.req.getParameterValues( "linkSequence" );
        if( ctx.pageConfig.getBackURL() == null )
            throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
        else if( linkSequences == null || linkSequences.length == 0 )
            throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

        // 레코드 삭제
        int count = 0;
        Map primaryMap = new java.util.HashMap();

        List errorList = new java.util.ArrayList();
        for( int i = 0; i < linkSequences.length; i++) {
            try {
                primaryMap.put( "linkSequence", linkSequences[i] );
                if( db.delete(primaryMap) ) {
                    count++;
                    ctx.handler.commit();
                }
                } catch( DataException dataEx ) {
                    ctx.handler.rollback();
                    errorList.add( createErrorMap(linkSequences[i], dataEx) );
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

    protected boolean wico( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> record = new ParameterMap( ctx.req );
		String requestURL = (String) record.get( "requestURL" );
		com.irt.custom.SystemConfig systemConfig = com.irt.custom.SystemConfig.getInstance( "RBM" );
		String urlWhiteList = systemConfig.getProperty( "urlWhiteList" );
		if( urlWhiteList != null ) {
			if( !HtmlUtility.checkURL(requestURL, urlWhiteList.split("\\|")) ) {
				throw new ServletModelException( ServletModelException.INTERNAL_ERROR );
			}
		}
		ctx.req.setAttribute( "record", record );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_sitelink_confirm.jsp" );
	}
}
