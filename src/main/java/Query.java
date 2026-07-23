/*
 *	File Name:	Query.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *		query.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2012/12/31		2.2.0	create
 *
**/

import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/Query"})
public class Query extends AbstractServletModel {
	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return true;
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "RBM" );
	}

	@Override
	protected boolean defaultReq( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		String name = ctx.req.getParameter( "name" );
		if( name == null || name.length() == 0 ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		return forward( ctx, systemConfig.getJspPath() + "/query.jsp" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );
		ctx.pageConfig.setSystemPackageCode( "RBM", "Query" );
	}
}
