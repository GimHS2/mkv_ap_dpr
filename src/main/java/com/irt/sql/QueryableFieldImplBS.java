/*
 *	File Name:	QueryableFieldImplBS.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	final class로 변경, getInstance() 추가
 *	stghr12		2007/10/31		2.1.0	create
 *
**/

package com.irt.sql;

import com.irt.data.Condition;

/**
 *
 */
public final class QueryableFieldImplBS extends com.irt.data.AbstractFieldWrapper implements QueryableField {
	String[] basisValues;
	QueryableField defaultField;
	QueryableField[] basisFields;

	public QueryableFieldImplBS( QueryableField defaultField, String basisValue, Joinable joinable ) {
		this( defaultField, new String[] { basisValue }, new QueryableField[] { new QueryableFieldWrapper(defaultField, joinable) } );
	}

	public QueryableFieldImplBS( QueryableField defaultField, Joinable joinable0, String basisValue1, Joinable joinable1 ) {
		super( defaultField );

		this.defaultField = new QueryableFieldWrapper( defaultField, joinable0 );
		this.basisValues = new String[] { basisValue1 };
		this.basisFields = new QueryableField[] { new QueryableFieldWrapper(defaultField, joinable1) };
	}

	public QueryableFieldImplBS( QueryableField defaultField, String basisValue, QueryableField basisField ) {
		this( defaultField, new String[] { basisValue }, new QueryableField[] { basisField } );
	}

	public QueryableFieldImplBS( QueryableField defaultField, String basisValue1, QueryableField basisField1
						, String basisValue2, QueryableField basisField2 ) {
		this( defaultField, new String[] { basisValue1, basisValue2 }, new QueryableField[] { basisField1, basisField2 } );
	}

	public QueryableFieldImplBS( QueryableField defaultField, String[] basisValues, QueryableField[] basisFields ) {
		super( defaultField != null ? defaultField : basisFields[0] );

		this.defaultField = defaultField;
		this.basisValues = basisValues;
		this.basisFields = basisFields;
	}

	public static QueryableFieldImplBS getInstance( QueryableField defaultField, String basisValue, QueryableField basisField ) {
		return getInstance( defaultField, new String[] { basisValue }, new QueryableField[] { basisField } );
	}

	public static QueryableFieldImplBS getInstance( QueryableField defaultField, String[] basisValues, QueryableField[] basisFields ) {
		if( defaultField instanceof QueryableFieldImplBS ) {
			QueryableFieldImplBS qfield = (QueryableFieldImplBS)defaultField;

			String[] basisValues_new = new String[ qfield.basisValues.length + basisValues.length ];
			System.arraycopy( qfield.basisValues, 0, basisValues_new, 0, qfield.basisValues.length );
			System.arraycopy( basisValues, 0, basisValues_new, qfield.basisValues.length, basisValues.length );

			QueryableField[] basisFields_new = new QueryableField[ qfield.basisFields.length + basisFields.length ];
			System.arraycopy( qfield.basisFields, 0, basisFields_new, 0, qfield.basisFields.length );
			System.arraycopy( basisFields, 0, basisFields_new, qfield.basisFields.length, basisFields.length );

			return new QueryableFieldImplBS( qfield.defaultField, basisValues_new, basisFields_new );
		} else
			return new QueryableFieldImplBS( defaultField, basisValues, basisFields );
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		Object basisValue = querybuf.getConditionValue( Condition.BASIS_CONDITIONKEY );

		if( basisValue != null ) {
			for( int i = 0; i < basisValues.length; i++ ) {
				if( basisValue.equals(basisValues[i]) )
					return basisFields[i].appendCondition( querybuf );
			}
		}

		if( defaultField != null )
			return defaultField.appendCondition( querybuf );
		else
			return false;
	}

	public boolean appendData( QueryBuffer querybuf ) {
		if( querybuf instanceof ConditionQueryBuffer ) {
			Object basisValue = ((ConditionQueryBuffer)querybuf).getConditionValue( Condition.BASIS_CONDITIONKEY );

			if( basisValue != null ) {
				for( int i = 0; i < basisValues.length; i++ ) {
					if( basisValue.equals(basisValues[i]) )
						return basisFields[i].appendData( querybuf );
				}
			}
		}

		if( defaultField != null )
			return defaultField.appendData( querybuf );
		else
			return false;
	}
}
