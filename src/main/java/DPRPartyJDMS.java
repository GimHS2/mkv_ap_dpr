/*
 *	File Name:	DPRPartyJDMS.java
 *	Version:	2.2.1
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
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	lsinji		2009/06/30		2.2.0	create
 *
**/

import com.irt.dpr.PartyJDMS;
import com.irt.data.DataResult;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRPartyJDMS"})
public class DPRPartyJDMS extends DPRServletModel {
	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		return new ParameterMap( ctx.req, true );

	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_REGIST.equals(ctx.mode) )
			return update( ctx, true );
		else if( MODE_REMOVE.equals(ctx.mode) )
			return update( ctx, false );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRParty.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRParty.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new PartyJDMS( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PARTYJDMS_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		PartyJDMS db = (PartyJDMS)ctx.db;

		ParameterMap parameterMap = new ParameterMap( ctx.req );
		String[] partyCodes = parameterMap.getParameterValues( "partyCode" );
		String[] organizationCode = parameterMap.getParameterValues( "organizationCode" );
		String[] distributionChannelCode = parameterMap.getParameterValues( "distributionChannelCode" );
		String[] divisionCode = parameterMap.getParameterValues( "divisionCode" );

		if( partyCodes == null || partyCodes.length == 0 ) {
			new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		Collection<Map<String, Object>> records = new java.util.ArrayList<Map<String, Object>> ();
		for( int i = 0; i < partyCodes.length; i++ ) {
			Map<String, Object> record = new java.util.HashMap<String, Object> ();

			record.put( "partyCode", partyCodes[i] );
			record.put( "organizationCode", organizationCode[i] );
			record.put( "distributionChannelCode", distributionChannelCode[i] );
			record.put( "divisionCode", divisionCode[i] );

			records.add( record );
		}

		DataResult result = new DataResult();
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>> ();
		try {
			if( inserting )
				result = db.registEachAll( records );
			else
				result = db.deleteEachAll( records );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		if( result.getException() != null )
			logger.error( result.getException() );

		ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_PARTY_JDMS_MANAGE_CNT"
				, String.valueOf(result.getCount()), String.valueOf(result.getSuccessCount()), String.valueOf(result.getErrorCount()) ));

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}
}
