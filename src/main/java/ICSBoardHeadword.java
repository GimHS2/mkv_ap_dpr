/*
 *	File Name:	ICSBoardHeadword.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		ics_board_headword_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

import com.irt.data.DataException;
import com.irt.html.HtmlUtility;
import com.irt.ics.BoardHeadword;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/ICSBoardHeadword"})
public class ICSBoardHeadword extends ICSBoardServlet {
	public final static String MODE_REQUEST				= "req";

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_REGIST.equals(ctx.mode) ) return regist( ctx );
		if( MODE_MODIFY.equals(ctx.mode) ) return modify( ctx );
		if( MODE_REQUEST.equals(ctx.mode) ) return request( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoardHeadword.MNG" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", null );
		else if( MODE_REGISTINPUT.equals(ctx.mode) || MODE_REGIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoardHeadword.MNG" );
		else if( MODE_MODIFYINPUT.equals(ctx.mode) || MODE_MODIFY.equals(ctx.mode))
			pageConfig.setSystemPackageCode( "ICS", "ICSBoardHeadword.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoardHeadword.DEL" );
		else if( MODE_REQUEST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoardHeadword.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new BoardHeadword( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_ICS_BOARD_HEADWORD_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		BoardHeadword db = (BoardHeadword)ctx.db;

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "ICSBoardHeadword%LIST" );

		String boardClassCode = ctx.req.getParameter( "boardClassCode" );
		if( boardClassCode == null || boardClassCode.length() == 0 )
			ctx.pageConfig.setProperty( "listmsg", ctx.msghandler.getMessage("MSG_CONDITION_NEEDED") );
		else {
			if( !isAuthorized(ctx, boardClassCode, "HW") ) return false;

			int[] idxVars = ctx.pageConfig.getListIndexVariables();
			Map<String, Object> conditionMap = ServletUtility.createMap( "boardClassCode", boardClassCode );
			List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
			String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

			ctx.req.setAttribute( "records", recordList );
			if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		}
		ctx.req.setAttribute( "columnList", columnList );

		return forward( ctx, systemConfig.getJspPath() + "/ics_board_headword_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("ICS", "ICSBoardHeadword.LST") ) return false;
			conditionMap = ServletUtility.createMap( "boardClassCode", ctx.req.getParameter("boardClassCode") );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((BoardHeadword)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean modify( Context ctx ) throws IOException, ServletException, SQLException {
		BoardHeadword db = (BoardHeadword)ctx.db;

		ParameterMap paramMap = new ParameterMap( ctx.req );
		String boardClassCode = paramMap.getParameter( "boardClassCode" );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( !isAuthorized(ctx, boardClassCode, "HW") )
			return false;

		try {
			paramMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			if( !db.modify(paramMap) )
				throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
		}

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	@Override
	protected boolean regist( Context ctx ) throws IOException, ServletException, SQLException {
		BoardHeadword db = (BoardHeadword)ctx.db;

		ParameterMap paramMap = new ParameterMap( ctx.req );
		String boardClassCode = paramMap.getParameter( "boardClassCode" );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( !isAuthorized(ctx, boardClassCode, "HW") )
			return false;

		try {
			paramMap.put( "headwordCode", db.generateHeadwordCode(boardClassCode) );
			paramMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

			db.regist( paramMap );
		} catch ( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
		}

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		BoardHeadword db = (BoardHeadword)ctx.db;

		String boardClassCode = ctx.req.getParameter( "boardClassCode" );
		String[] codes = ctx.req.getParameterValues( "headwordNumber" );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( codes == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( !isAuthorized(ctx, boardClassCode, "HW") )
			return false;

		int count = 0;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < codes.length; i++ ) {
			Map<String, Object> primaryMap = BoardHeadword.createPrimary( codes[i] );
			try {
				if( boardClassCode.equals(db.getFieldValue(primaryMap, "boardClassCode")) && db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(codes[i], dataEx) );
			}
		}

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		}else{
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	protected boolean request( Context ctx ) throws IOException, ServletException, SQLException {
		BoardHeadword db = (BoardHeadword)ctx.db;

		String headwordCode = ctx.req.getParameter( "headwordCode" );
		if( headwordCode == null || headwordCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		ctx.res.setContentType( "text/html; charset=utf-8" );
		ctx.res.setHeader( "Cache-Control", "no-cache" );

		Map<String, Object> recordMap = db.getRecord( BoardHeadword.createPrimary(headwordCode) );
		if( recordMap != null ) {
			if( !isAuthorized(ctx, (String)recordMap.get("boardClassCode"), "HW") ) return false;
			ctx.res.getWriter().write( recordMap.get("headwordName").toString() );
		}

		return true;
	}
}
