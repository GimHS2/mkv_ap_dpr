/*
 *	File Name:	DPRBoard.java
 *	Version:	2.2.0c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2017/02/28		2.2.0c	create
 *
**/

import com.irt.servlet.SystemConfig;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRBoard"})
public class DPRBoard extends DPRServletModel {
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
		ctx.req.getRequestDispatcher("/servlet/ICSBoard").forward( ctx.req, ctx.res );
		return true;
	}
}
