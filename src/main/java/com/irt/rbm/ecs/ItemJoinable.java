/*
 *	File Name:	ItemJoinable.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	usingSellerGtin -> com.irt.rbm.ecs.TradeItem.usingSellerGtin()
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.Condition;
import com.irt.sql.*;

/**
 *
 */
public class ItemJoinable implements com.irt.sql.Joinable {
	String alias, balias;
	boolean usingParentTP;

	public ItemJoinable( String alias, String balias ) {
		this( alias, balias, true );
	}

	public ItemJoinable( String alias, String balias, boolean usingParentTP ) {
		this.alias = alias;
		this.balias = balias;
		this.usingParentTP = usingParentTP;
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		if( !querybuf.appendTableWithAlias("ECS_ITEM", alias) ) return false;

		if( querybuf instanceof ConditionQueryBuffer ) {
			ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;

			if( Condition.containsGroupKey(condquerybuf.getConditionMap(), "TP") ) {
				if( TradeItem.usingSellerGtin() )
					querybuf.appendCondition( ""+ alias +".GLN(+) = "+ balias +".SELLERGLN AND "+ alias +".GTIN(+) = "+ balias +".GTIN" );
				else
					querybuf.appendCondition( ""+ alias +".GLN(+) = "+ balias +".BUYERGLN AND "+ alias +".GTIN(+) = "+ balias +".GTIN" );

				return true;
			} else if( usingParentTP && Condition.containsGroupKey(condquerybuf.getConditionMap(), "parentTP") ) {
				if( TradeItem.usingSellerGtin() )
					querybuf.appendCondition( ""+ alias +".GLN(+) = "+ balias +".PARENT_SELLERGLN AND "+ alias +".GTIN(+) = "+ balias +".GTIN" );
				else
					querybuf.appendCondition( ""+ alias +".GLN(+) = "+ balias +".PARENT_BUYERGLN AND "+ alias +".GTIN(+) = "+ balias +".GTIN" );

				return true;
			} else {
				Object gln;

				if( TradeItem.usingSellerGtin() ) {
					gln = Condition.getConditionValueOnly( condquerybuf.getConditionMap(), "sellerGln" );
					if( usingParentTP && gln == null ) gln = Condition.getConditionValueOnly( condquerybuf.getConditionMap(), "parentSellerGln" );
				} else {
					gln = Condition.getConditionValueOnly( condquerybuf.getConditionMap(), "buyerGln" );
					if( usingParentTP && gln == null ) gln = Condition.getConditionValueOnly( condquerybuf.getConditionMap(), "parentBuyerGln" );
				}

				if( gln != null ) {
					querybuf.appendCondition( ""+ alias +".GLN(+) = ? AND "+ alias +".GTIN(+) = "+ balias +".GTIN", gln );
					return true;
				}
			}
		}

		querybuf.appendCondition( ""+ alias +".GLN(+) = IMT.GLN AND "+ alias +".GTIN(+) = IMT.GTIN" );
		querybuf.appendTableWithAlias( "ECS_ITEM_MASTER", "IMT", "IMT.GTIN(+) = "+ balias +".GTIN" );

		return true;
	}

	public boolean existTable( QueryBuffer querybuf ) {
		return querybuf.existTableAlias( alias );
	}
}
