/*
 *	File Name:	PartyQuery.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.sql.*;

/**
 *
 */
public class PartyQuery extends com.irt.sql.QueryableImpl {
	boolean isTP;

	public PartyQuery( boolean isTP ) {
		super( Schema.findQueryable( isTP ? Schema.ECS_TRADEPARTNER : Schema.ECS_PARTY ) );

		this.isTP = isTP;
		if( isTP )
			append( Schema.findQueryable(Schema.ECS_TRADEPARTNER_INFO), "TPI.BUYERGLN(+) = TP.BUYERGLN AND TPI.SELLERGLN(+) = TP.SELLERGLN" );
		else {
			String[] fieldKeyTP = new String[] { "buyerGln", "sellerGln" };
			QueryBufferValid querybufValid = new QueryBufferValid.Condition( QueryBufferValid.CONDITION_OR, fieldKeyTP );

			append( new ConditionalQueryable(querybufValid, new PartyQuery(true)) {
				public boolean appendTable( QueryBuffer querybuf ) {
					if( !super.appendTable(querybuf) ) return false;
					if( ((ConditionQueryBuffer)querybuf).findConditionSimple("buyerGln", "TP.BUYERGLN(+)") )
						querybuf.appendCondition( "TP.SELLERGLN(+) = PTY.GLN" );
					else if( ((ConditionQueryBuffer)querybuf).findConditionSimple( "sellerGln", "TP.SELLERGLN(+)" ) )
						querybuf.appendCondition( "TP.BUYERGLN(+) = PTY.GLN" );

					return true;
				}
			} );

			convertToUnconditionable( fieldKeyTP );
		}
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		boolean hasCondition = super.appendCondition( querybuf );

		if( isTP ) {
			if( querybuf.findConditionSimple( "nonTradeGtin", "ITP_NTP.GTIN(+)" ) ) {
				querybuf.appendTableWithAlias( "ECS_ITEMTP", "ITP_NTP"
						, "ITP_NTP.BUYERGLN(+) = TP.BUYERGLN AND ITP_NTP.SELLERGLN(+) = TP.SELLERGLN AND ITP_NTP.GTIN IS NULL" );
				hasCondition = true;
			}
		}

		return hasCondition;
	}
}
