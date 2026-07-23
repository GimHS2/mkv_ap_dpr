/*
 *	File Name:	USREffectUser.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		usr_effectuser_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경, log4j 사용
 *	stghr12		2007/04/30		2.1.0	checkAuthorize(): ServletModel 변경사항 적용
 *										설정된 이후에 userName, partyName 표시되게 수정
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/06/21		1.0.0	create
 *
**/

import com.irt.servlet.*;
import com.irt.system.SessionManagerException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/USREffectUser"})
public class USREffectUser extends AbstractServletModel {
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		try {
			ctx.sessionMng.setUseEffectUser( false );
			try {
				return super.checkAuthorize( ctx );
			} finally {
				ctx.sessionMng.setUseEffectUser( true );
			}
		} catch( SessionManagerException sessionEx ) {
			throw new ServletModelException( sessionEx );
		}
	}

	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) ctx.pageConfig.setMode( ctx.mode = MODE_REGISTINPUT );
		ctx.pageConfig.setSystemPackageCode( "USR", "USREffectUser" );

		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_EFFECTUSER_"+ MODE_REGISTINPUT.toUpperCase()) );
	}

	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "partyId", ctx.sessionMng.getPartyId() );
			recordMap.put( "partyName", ctx.sessionMng.getPartyName() );
			recordMap.put( "userId", ctx.sessionMng.getUserId() );
			recordMap.put( "userName", ctx.sessionMng.getUserName() );
			ctx.req.setAttribute( "record", recordMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/usr_effectuser_input.jsp" );
	}

	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		String partyId = ctx.req.getParameter( "partyId" );
		String userId = ctx.req.getParameter( "userId" );
		if( partyId == null || partyId.length() == 0 || userId == null || userId.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
		recordMap.put( "partyId", partyId );
		recordMap.put( "userId", userId );
		ctx.req.setAttribute( "record", recordMap );

		try {
			if( ctx.sessionMng.setEffectUser( partyId, userId ) ) {
				logger.info( "effective user: "+ ctx.sessionMng.getPartyId() +":"+ ctx.sessionMng.getUserId() );

				if( ctx.pageConfig.getBackURL() != null )
					return sendRedirect( ctx, ctx.pageConfig.getBackURL() );
			}
			recordMap.put( "partyName", ctx.sessionMng.getPartyName() );
			recordMap.put( "userName", ctx.sessionMng.getUserName() );
		} catch( SessionManagerException sessionEx ) {
			String errorKey = sessionEx.getErrorKey();
			if( errorKey == null ) throw new ServletModelException( sessionEx.getCause() );

			ctx.pageConfig.setMessage( ctx.msghandler.getMessage(errorKey) );
		}
		return registInput( ctx );
	}
}
