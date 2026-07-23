/*
 *	File Name:	Memos.java
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
public class Memos extends com.irt.rbm.ManipulableManagerImpl {
	public final static String CREDIT_MEMEOS			= "O";
	public final static String DEBIT_MEMEOS				= "P";
	public final static String QUIT_MEMOS				= "Q";

	private final static Table table = Schema.findTable( Schema.DPR_ORDER_MEMOS );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_MEMOS );

	public Memos( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String memoNumber ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "memoNumber", memoNumber );

		return primaryMap;
	}

	public static Table getTable() {
		return table;
	}

	protected static class Valid implements QueryBufferValid {
		String conditionKey;
		String value;

		public Valid( String conditionKey, String value ) {
			this.conditionKey = conditionKey;
			this.value = value;
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( value == null || value.length() == 0 ) return false;

			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
				if( value.equals(condquerybuf.getConditionValue(conditionKey)) )
					return true;

				return false;
			} else
				return false;
		}
      
    }
}
