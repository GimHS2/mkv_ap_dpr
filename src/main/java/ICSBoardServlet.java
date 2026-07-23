/*
 *	File Name:	ICSBoardServlet.java
 *	Version:	2.2.1c (dpr)
 *
 *	Description:
 *
 *	Note:
 *		systemConfig.getProperty( "attachPath" )
 *		systemConfig.getProperty( "maxImageFileSize" )
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2016/08/31		2.2.1	property 추가 : maxImageFileSize
 *	stghr12		2010/07/31		2.2.0	create
 *
**/

import com.irt.servlet.ServletModelException;
import java.sql.SQLException;
import javax.servlet.ServletException;

/**
 *
 */
public abstract class ICSBoardServlet extends AbstractServletModel {
	String ATTACHFILE_PATH;

	private static long DEFAULT_IMAGE_FILESIZE_KB = 500;
	long MAX_IMAGE_FILESIZE_KB = DEFAULT_IMAGE_FILESIZE_KB;

	protected int getNumberParameter( Context ctx, String name, boolean mandatory ) throws ServletException {
		String value = ctx.req.getParameter( name );
		if( value == null || value.length() == 0 ) {
			if( mandatory )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			return -1;
		}

		try {
			int number = Integer.parseInt( value );
			if( number < 0 )
				throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

			return number;
		} catch( NumberFormatException numberEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "ICS" );
	}

	@Override
	public void init( javax.servlet.ServletConfig config ) throws ServletException {
		super.init( config );
		this.ATTACHFILE_PATH = systemConfig.getProperty( "attachPath" );

		if( systemConfig.getProperty("maxImageFileSize") != null ) {
			try {
				this.MAX_IMAGE_FILESIZE_KB = Long.parseLong( systemConfig.getProperty( "maxImageFileSize" ) );
			} catch ( NumberFormatException nfe ) {
				logger.debug( "maxImageFileSize property is not parsable", nfe );
			}
		} else {
			logger.debug( "maxImageFileSize is not configured.( default: " + DEFAULT_IMAGE_FILESIZE_KB );
		}
	}

	protected boolean isAuthorized( Context ctx, String boardClassCode, String authType ) throws ServletException, SQLException {
		return ctx.sessionMng.isAuthorized( "ICS", "ICSBoard."+ boardClassCode +"."+ authType );
	}
}
