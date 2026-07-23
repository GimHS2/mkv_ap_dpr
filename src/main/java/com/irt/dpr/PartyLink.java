/*
 *	File Name:	PartyLink.java
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

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class PartyLink extends com.irt.rbm.ManipulableManagerImpl {
	public final static String[] LINKTYPE_SOLD				= new String[] { "AG", "SP" };
	public final static String[] LINKTYPE_SHIP				= new String[] { "WE", "SH" };
	public final static String[] LINKTYPE_PAYER				= new String[] { "RG", "PY" };
	public final static String[] LINKTYPE_BILL				= new String[] { "RE", "BP" };//also 'RE' 'BE' typo
	public final static String[] LINKTYPE_EMPLOYEE			= new String[] { "VE", "PE" };

	private final static Table table = Schema.findTable( Schema.DPR_PARTY_LINK );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PARTY_LINK );

	public PartyLink( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String partyCode, String linkType, String linkPartyCode) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "partyCode", partyCode );
		primaryMap.put( "linkType", linkType );
		primaryMap.put( "linkPartyCode", linkPartyCode );

		return primaryMap;
	}

	public static String getLinkType( String value ) {
		String ret = getLinkType( PartyLink.LINKTYPE_SOLD, value );
		if( ret == null )
			if( (ret = getLinkType(PartyLink.LINKTYPE_SHIP, value)) == null )
				if( (ret = getLinkType(PartyLink.LINKTYPE_PAYER, value)) == null )
					if( (ret = getLinkType(PartyLink.LINKTYPE_BILL, value)) == null )
						ret = getLinkType( PartyLink.LINKTYPE_EMPLOYEE, value );

		return ret;
	}

	public static QueryBuffer getDistinctPartyLink( Map<String, ? extends Object> conditionMap ) {
		return getDistinctPartyLink( conditionMap, false );
	}

	public static QueryBuffer getDistinctPartyLink( Map<String, ? extends Object> conditionMap, boolean alias ) {
/* WORKING */
		boolean isEmployee = false;
		Object linkTypes = conditionMap.get( "employeeLinkType" );
		if( linkTypes != null ) {
			String[] types;
			if( linkTypes instanceof String[] )
				types = (String[])linkTypes;
			else
				types = new String[] { linkTypes.toString() };

			if( "VE".equals(getLinkType(types[0])) )
				isEmployee = true;
		}
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );
		if( alias ) {
			querybuf.appendDataWithAlias( "PLNK.PARTYCD", "partyCode" );
			querybuf.appendDataWithAlias( "PLNK.LINKTYPE", "linkType" );
			querybuf.appendDataWithAlias( "PLNK.LINK_PARTYCD", "linkPartyCode" );
		} else {
			querybuf.appendData( "PLNK.PARTYCD" );
			querybuf.appendData( "PLNK.LINKTYPE" );
			querybuf.appendData( "PLNK.LINK_PARTYCD" );
		}
		querybuf.appendDistinct();

		querybuf.appendTableWithAlias( "DPR_PARTY_SALES", "PTYS" );
		querybuf.appendTableWithAlias( "DPR_PARTY_LINK", "PLNK", "PLNK.PARTYCD = PTYS.PARTYCD" );
		if( isEmployee ) {
			querybuf.appendTableWithAlias( "DPR_USER_EMPLOYEE", "UEMP", "UEMP.EMPLOYEEID = PLNK.LINK_PARTYCD" );
		}

		querybuf.findCondition( "organizationCode", "PTYS.ORGANIZATIONCD" );
		querybuf.findCondition( "divisionCode", "PTYS.DIVISIONCD" );
		querybuf.findCondition( "distributionChannelCode", "PTYS.DIST_CHANNELCD" );
		if( isEmployee ) {
			querybuf.findCondition( "employeeLinkType", "PLNK.LINKTYPE" );
			querybuf.findCondition( "employeeId", "PLNK.LINK_PARTYCD" );
		} else {
			querybuf.findCondition( "baseLinkType", "PLNK.LINKTYPE" );
			querybuf.findCondition( "baseLinkPartyCode", "PLNK.LINK_PARTYCD" );
		}

		return querybuf;
	}

	private static String getLinkType( String[] types, String value ) {
		if( types == null ) return null;

		for( int i = 0; i < types.length; i++ )
			if( types[i] != null && types[i].equals(value) )
				return types[0];

		return null;
	} 
}
