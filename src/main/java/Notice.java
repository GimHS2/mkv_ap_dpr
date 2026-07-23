/*
 *	File Name:	Notice.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *		notice.jsp
 *		systemConfig.getProperty( "noticeClassCode" )
 *		systemConfig.getProperty( "noticePublicInd" )
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.1	ctx.req.getParameter( "boardClassCode" ) add.
 *	stghr12		2008/03/31		2.2.0	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										info(): Board.increaseViewCount() 호출하도록 추가
 *	stghr12		2007/04/30		2.1.0	systemConfig.getProperty("noticeClassCode"), systemConfig.getProperty("noticePublicInd") 사용
 *										defaultReq() 삭제
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

import com.irt.data.DataException;
import com.irt.rbm.rbm.Board;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/Notice"})
public class Notice extends AbstractServletModel {
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		String noticePublicInd = systemConfig.getProperty( "noticePublicInd" );
		if( "Y".equals(noticePublicInd) )
			return true;
		else
			return super.checkAuthorize( ctx );
	}

	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "RBM" );
	}

	protected boolean info( Context ctx ) throws IOException, ServletException, SQLException {
		Board db = new Board( ctx.handler );

		String boardNumber = ctx.req.getParameter( "boardNumber" );
		if( boardNumber == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		try {
			int number = Integer.parseInt( boardNumber );

			String[] fieldKeys = new String[] { "boardClassCode", "boardNumber", "boardOption", "title", "content", "createDateTime" };
			Map recordMap = db.getRecord( Board.createPrimary( (String)ctx.extraObj, number ), fieldKeys );
			if( recordMap == null )
				throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
			ctx.req.setAttribute( "record", recordMap );

			if( ctx.sessionMng.isLoginUser() ) {
				try {
					db.increaseViewCount( (String)ctx.extraObj, number, ctx.sessionMng.getUniqId() );
				} catch( DataException dataEx ) {
					logger.error( "internal error.", dataEx );
				} catch( SQLException sqlEx ) {
					logger.error( "internal error.", sqlEx );
				}
			}

			return forward( ctx, systemConfig.getJspPath() + "/notice.jsp" );
		} catch( NumberFormatException numEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}
	}

	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) ctx.pageConfig.setMode( ctx.mode = MODE_INFO );

		
		String boardClassCode = ctx.req.getParameter( "boardClassCode" );
		if( boardClassCode == null )
			boardClassCode = systemConfig.getProperty( "noticeClassCode" );

		if( boardClassCode != null )
			ctx.pageConfig.setSystemPackageCode( "RBM", "RBMBoard."+ boardClassCode +".INF" );
		else
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );

		ctx.extraObj = boardClassCode;
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_NOTICE") );
	}
}
