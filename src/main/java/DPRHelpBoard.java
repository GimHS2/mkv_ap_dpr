/*
 *	File Name:	DPRHelpBoard.java
 *	Version:	2.2.0c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.1c	org.slf4j.MDC forward.
 *	hankalam	2017/02/28		2.2.0c	create
 *
**/

import com.irt.servlet.*;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRHelpBoard"})
public class DPRHelpBoard extends DPRServletModel {
	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		java.util.Map<String, String> copy = org.slf4j.MDC.getCopyOfContextMap();
		ctx.req.getRequestDispatcher("/servlet/ICSHelpBoard").forward( ctx.req, ctx.res );
		org.slf4j.MDC.setContextMap(copy);
		return true;
	}
}
