/*
 *	File Name:	QueryableFieldImplAR.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.0	create
 *
**/

package com.irt.sql;

/**
 *
 */
public final class QueryableFieldImplAR extends com.irt.data.AbstractFieldWrapper implements QueryableField {
	QueryableField defaultField;
	ConditionalQueryableField[] conditionalFields;

	public QueryableFieldImplAR( QueryableField defaultField, ConditionalQueryableField... conditionalFields ) {
		super( defaultField != null ? defaultField : conditionalFields[0] );

		if( defaultField instanceof QueryableFieldImplAR ) {
			QueryableFieldImplAR qfield = (QueryableFieldImplAR)defaultField;

			this.defaultField = qfield.defaultField;
			this.conditionalFields = new ConditionalQueryableField[ conditionalFields.length + qfield.conditionalFields.length ];
			System.arraycopy( conditionalFields, 0, this.conditionalFields, 0, conditionalFields.length );
			System.arraycopy( qfield.conditionalFields, 0, this.conditionalFields, conditionalFields.length, qfield.conditionalFields.length );
		} else {
			this.defaultField = defaultField;
			this.conditionalFields = conditionalFields;
		}
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		for( int i = 0; i < conditionalFields.length; i++ ) {
			if( conditionalFields[i].getQueryBufferValid().hasValidCondition(querybuf) )
				return conditionalFields[i].appendCondition( querybuf );
		}

		if( defaultField != null )
			return defaultField.appendCondition( querybuf );
		else
			return false;
	}

	public boolean appendData( QueryBuffer querybuf ) {
		for( int i = 0; i < conditionalFields.length; i++ ) {
			if( conditionalFields[i].getQueryBufferValid().hasValidCondition(querybuf) )
				return conditionalFields[i].appendData( querybuf );
		}

		if( defaultField != null )
			return defaultField.appendData( querybuf );
		else
			return false;
	}
}
