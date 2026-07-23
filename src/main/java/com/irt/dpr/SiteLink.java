/*
 *	File Name:	SiteLink.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/*
 *
 */
public class SiteLink extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_SITE_LINK );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_SITE_LINK );

	public SiteLink( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( Object linkSequence, Object countryCode ) {
		Map<String, Object> primaryMap = Record.createMap( "linkSequence", linkSequence );
		primaryMap.put( "displayCountryCode", countryCode );

		return primaryMap;
	}

	public Object getlinkSequence() throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT seqDPR_LINK.nextval from dual" );
	}

	public int getMaxDisplaySequence( String countryCode ) throws SQLException {
		return SQLManager.getInt( handler,
				" SELECT MAX(DISPLAY_SEQ) "
						+ " FROM DPR_SITE_LINK SLNK "
						+ " WHERE DISPLAY_COUNTRYCD = ?"
						+ " GROUP BY DISPLAY_COUNTRYCD "
						, new Object[] { countryCode } );
	}
}
