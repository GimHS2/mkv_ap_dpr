/*
 *	File Name:	JoinableImplBS.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2009/06/30		2.2.0	create
 *
**/

package com.irt.sql;

import com.irt.data.Condition;

/**
 * Joinable를 구현한 Class.
 */
public final class JoinableImplBS implements Joinable {
	String[] basisValues;
	Joinable defaultJoinable;
	Joinable[] basisJoinables;

	public JoinableImplBS( Joinable defaultJoinable, String basisValue, Joinable basisJoinable ) {
		this( defaultJoinable, new String[] { basisValue }, new Joinable[] { basisJoinable } );
	}

	public JoinableImplBS( Joinable defaultJoinable, String basisValue1, Joinable basisJoinable1, String basisValue2, Joinable basisJoinable2 ) {
		this( defaultJoinable, new String[] { basisValue1, basisValue2 }, new Joinable[] { basisJoinable1, basisJoinable2 } );
	}

	public JoinableImplBS( Joinable defaultJoinable, String[] basisValues, Joinable[] basisJoinables ) {
		this.defaultJoinable = defaultJoinable;
		this.basisValues = basisValues;
		this.basisJoinables = basisJoinables;
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		if( querybuf instanceof ConditionQueryBuffer ) {
			Object basisValue = ((ConditionQueryBuffer)querybuf).getConditionValue( Condition.BASIS_CONDITIONKEY );

			if( basisValue != null ) {
				for( int i = 0; i < basisValues.length; i++ ) {
					if( basisValue.equals(basisValues[i]) )
						return basisJoinables[i].appendTable( querybuf );
				}
			}
		}

		if( defaultJoinable != null )
			return defaultJoinable.appendTable( querybuf );
		else
			return false;
	}

	public boolean existTable( QueryBuffer querybuf ) {
		if( querybuf instanceof ConditionQueryBuffer ) {
			Object basisValue = ((ConditionQueryBuffer)querybuf).getConditionValue( Condition.BASIS_CONDITIONKEY );

			if( basisValue != null ) {
				for( int i = 0; i < basisValues.length; i++ ) {
					if( basisValue.equals(basisValues[i]) )
						return basisJoinables[i].existTable( querybuf );
				}
			}
		}

		if( defaultJoinable != null )
			return defaultJoinable.existTable( querybuf );
		else
			return false;
	}
}
