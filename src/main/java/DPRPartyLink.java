/*
 *	File Name:	DPRPartyLink.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.PartyLink;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataWriter;
import com.irt.data.Record;
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
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRPartyLink"})
public class DPRPartyLink extends DPRServletModel {

	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		/* Condition Value Region Code, Organization Code*/
		Object partyCode = conditionMap.get( "partyCode" );
		if( partyCode == null || ((String)partyCode).length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else
			Condition.putConditionValueOnly( conditionMap, "partyCode", conditionMap.get("partyCode") );

		return conditionMap;
	}

	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyLink.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new PartyLink( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PARTYLINK_"+ ctx.mode.toUpperCase()) );
	}

	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		PartyLink db = (PartyLink)ctx.db;
		// Condition Map
		Map<String, Object> conditionMap = createConditionMap( ctx );
		
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPartyLink%LIST" );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_partylink_list.jsp" );
	}

	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyLink.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((PartyLink)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}
}
