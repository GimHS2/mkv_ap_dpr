/*
 *	File Name:	Table.java
 *	Version:	2.2.5c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek			2018/10/30		2.2.2c	Table.Field.alterable() 추가
 *	stghr12		2011/02/28		2.2.5	merge 지원
 *	stghr12		2010/09/30		2.2.4	appendCondition(): TYPE_DESCRIPTION일 때 caseSensitive = false
 *	stghr12		2010/03/31		2.2.3	containsData() 추가
 *	stghr12		2010/02/28		2.2.2	getBindFieldArray()를 public으로 변경
 *	stghr12		2008/12/31		2.2.1	getAlterableFieldArray(): updateFieldSet 사용
 *										makeUpdateStatement( fieldKeys, updateSetClauseOnly ) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										AbstractField.TYPE_DATETIME 적용, validDateFormat 삭제
 *	stghr12		2007/10/31		2.1.1	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2006/12/01		2.1.0	dataType을 int형에서 char형으로 변경
 *										STATEMENT_* -> Record.*
 *										getPrimaryFieldArray(), getQueryableFieldArray() 추가
 *										makePrimaryConditionQuery(): primaryKey가 없을 경우 null return.
 *										makeStatement(), makeUpdateStatement(): primaryKey가 없을 경우 처리
 *										setPrimaryConditionQuery(): primaryKey가 없을 경우 처리
 *										Field.appendCondition(): TYPE_TIME 처리 추가
 *	GimHS		2006/08/21		2.0.2	getAlterableFieldArray(): 버그 수정(invalid alterable fieldKeys 체크)
 *	stghr12		2006/07/07		2.0.1	Field.setMinMaxValue( minValue, maxValue, rangeType ); 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
 **/

package com.irt.sql;

import com.irt.data.*;
import com.irt.data.Record;

import java.util.Map;

/**
 *
 */
public class Table extends com.irt.data.ValidableFieldSet implements Queryable {
	String name, alias, extraUpdateQuery;
	Table.Field[] insertFields, insertBindFields, primaryFields, updateFields, updateBindFields;
	ValidableFieldSet updateFieldSet;
	Map<String, QueryableField> qfieldMap;

	public Table( String name, String alias, Table.Field[] tfields ) throws IllegalArgumentException {
		this( name, alias, tfields, null );
	}

	public Table( String name, String alias, Table.Field[] tfields, String extraUpdateQuery ) throws IllegalArgumentException {
		super( tfields );

		qfieldMap = new java.util.HashMap<String, QueryableField>();
		for( Table.Field tfield : tfields )
			qfieldMap.put( tfield.getFieldKey(), tfield );

		// name, alias, extraUpdateQuery 설정
		this.name = name;
		this.alias = alias;
		this.extraUpdateQuery = extraUpdateQuery;

		// tfields 설정
		for( Table.Field tfield : tfields ) {
			if( tfield.table != null )
				throw new IllegalArgumentException( "illegal field '"+ tfield.getFieldKey() +"'" );
			tfield.table = this;
		}

		// count 초기화
		int i_cnt, p_cnt, u_cnt;
		i_cnt = p_cnt = u_cnt = 0;
		for( Table.Field tfield : tfields ) {
			if( tfield.readonly() ) continue;

			i_cnt++;
			if( tfield.primary )
				p_cnt++;
			else if( tfield.alterable )
				u_cnt++;
		}

		// insertFields, updateFields 설정
		insertFields = tfields;
		updateFields = new Table.Field[tfields.length];
		for( int f = 0; f < tfields.length; f++ ) {
			if( tfields[f].readonly() )
				updateFields[f] = tfields[f];
			else if( tfields[f].primary )
				updateFields[f] = new Table.Field( tfields[f], tfields[f].nullable(), true );
			else if( tfields[f].alterable ) {
				if( !tfields[f].nullable() && tfields[f].optional ) {
					updateFields[f] = new Table.Field( tfields[f], true, tfields[f].readonly() );
					updateFields[f].optional = false;
				} else
					updateFields[f] = tfields[f];
			} else
				updateFields[f] = new Table.Field( tfields[f], tfields[f].nullable(), true );
		}
		updateFieldSet = new ValidableFieldSet( updateFields );

		// insertBindFields 설정
		insertBindFields = new Table.Field[i_cnt];
		for( int f = i_cnt = 0; f < insertFields.length; f++ ) {
			if( insertFields[f].readonly() ) continue;

			insertBindFields[i_cnt] = insertFields[f];
			if( insertFields[f].insertQuery != null && !insertFields[f].nullable() )
				insertBindFields[i_cnt] = new Table.Field( insertFields[f], true, insertFields[f].readonly() );
			i_cnt++;
		}

		// primaryFields, updateBindFields 설정
		primaryFields = ( p_cnt > 0 ? new Table.Field[p_cnt] : null );
		updateBindFields = new Table.Field[u_cnt + p_cnt];
		for( int f = p_cnt = u_cnt = 0; f < updateFields.length; f++ ) {
			if( updateFields[f].primary )
				primaryFields[p_cnt++] = updateFields[f];
			else if( updateFields[f].readonly() )
				continue;
			else if( updateFields[f].alterable )
				updateBindFields[u_cnt++] = updateFields[f];
		}
		if( p_cnt > 0 ) System.arraycopy( primaryFields, 0, updateBindFields, u_cnt, p_cnt );
	}

	@Override
	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		return false;
	}

	@Override
	public boolean appendTable( QueryBuffer querybuf ) {
		return querybuf.appendTableWithAlias( name, alias );
	}

	public boolean containsData( Map recordMap, String ... ignoreFieldKeys ) {
		for( Table.Field insertField : insertFields )
			if( !insertField.primary() && !insertField.readonly() ) {
				String fieldKey = insertField.getFieldKey();
				boolean contains = ( recordMap.get(fieldKey) != null );

				if( ignoreFieldKeys != null ) {
					for( int i = 0; contains &&i < ignoreFieldKeys.length; i++ )
						contains = !ignoreFieldKeys[i].equals( fieldKey );
				}
				if( contains ) return true;
			}

		return false;
	}

	@Override
	public boolean existTable( QueryBuffer querybuf ) {
		return querybuf.existTableAlias( alias );
	}

	public Object[] extractBindVariables( Map recordMap, int statementType ) throws FieldException, IllegalArgumentException {
		switch( statementType ) {
		case Record.INSERT:
		case Record.MERGE:
			return Record.extractValues( recordMap, insertBindFields );
		case Record.UPDATE:
			return Record.extractValues( recordMap, updateBindFields );
		case Record.DELETE:
		case Record.QUERY:
			return Record.extractValues( recordMap, primaryFields );
		default:
			throw new IllegalArgumentException( "illegal statementType '"+ statementType +"'" );
		}
	}

	public Object[] extractPrimaryValues( Map primaryMap ) throws FieldException {
		return Record.extractValues( primaryMap, primaryFields );
	}

	public Table.Field[] getAlterableFieldArray( String... fieldKeys ) throws IllegalArgumentException {
		Table.Field[] tfields = new Table.Field[ fieldKeys.length ];

		for( int f = 0; f < fieldKeys.length; f++ ) {
			try {
				tfields[f] = (Table.Field)updateFieldSet.getField( fieldKeys[f] );
				if( tfields[f] == null || !tfields[f].alterable )
					throw new IllegalArgumentException( "illegal alterable fieldKeys["+ f +"] '"+ fieldKeys[f] +"'" );
			} catch( ClassCastException castEx ) {
				throw new IllegalArgumentException( "illegal fieldKeys["+ f +"] '"+ fieldKeys[f] +"'" );
			}
		}

		return tfields;
	}

	public Table.Field[] getBindFieldArray( int statementType ) throws IllegalArgumentException {
		switch( statementType ) {
		case Record.INSERT:
		case Record.MERGE:
			return insertBindFields;
		case Record.UPDATE:
			return updateBindFields;
		case Record.DELETE:
		case Record.QUERY:
			return primaryFields;
		default:
			throw new IllegalArgumentException( "illegal statementType '"+ statementType +"'" );
		}
	}

	public Table.Field[] getPrimaryFieldArray() {
		if( primaryFields == null ) return null;

		Table.Field[] fields = new Table.Field[ primaryFields.length ];
		System.arraycopy( primaryFields, 0, fields, 0, fields.length );

		return fields;
	}

	@Override
	public QueryableField getQueryableField( String fieldKey ) {
		return (QueryableField)getField( fieldKey );
	}

	@Override
	public QueryableField[] getQueryableFieldArray( String... fieldKeys ) throws IllegalArgumentException {
		QueryableField[] fields = new QueryableField[fieldKeys.length];
		for( int f = 0; f < fieldKeys.length; f++ ) {
			if( fieldKeys[f] == null ) continue;
			fields[f] = (QueryableField)getField( fieldKeys[f] );
			if( fields[f] == null )
				throw new IllegalArgumentException( "illegal fieldKey '"+ fieldKeys[f] +"'" );
		}

		return fields;
	}

	@Override
	public Map<String, ? extends QueryableField> getQueryableFieldMap() {
		return qfieldMap;
	}

	public String getTableAlias() {
		return alias;
	}

	public String getTableName() {
		return name;
	}

	public ValidableFieldSet getValidableFieldSet( boolean inserting ) {
		return( inserting ? this : updateFieldSet );
	}

	public String makePrimaryConditionQuery() {
		if( primaryFields == null ) return null;

		StringBuffer sbuf = new StringBuffer();
		for( Table.Field primaryField : primaryFields )
			sbuf.append( " AND " ).append( primaryField.fieldName ).append( " = ?" );

		return sbuf.substring( 5 );
	}

	public String makeStatement( int statementType ) throws IllegalArgumentException {
		StringBuffer sbuf;

		switch( statementType ) {
		case Record.INSERT:
			sbuf = new StringBuffer();
			sbuf.append( "INSERT INTO " ).append( name ).append( "(" );
			for( Table.Field insertField : insertFields )
				if( !insertField.readonly() )
					sbuf.append( insertField.fieldName ).append( "," );
			sbuf.setCharAt( sbuf.length() - 1, ')' );

			sbuf.append( " VALUES (" );
			for( Table.Field insertField : insertFields )
				if( !insertField.readonly() )
					sbuf.append( insertField.insertQuery != null ? insertField.insertQuery : "?" ).append( "," );
			sbuf.setCharAt( sbuf.length() - 1, ')' );

			return sbuf.toString();
		case Record.UPDATE:
			return makeUpdateStatement( updateFields, false );
		case Record.MERGE:
			StringBuffer sbuf_src = new StringBuffer();
			StringBuffer sbuf_col = new StringBuffer();
			StringBuffer sbuf_val = new StringBuffer();
			for( Table.Field insertField : insertBindFields ) {
				sbuf_src.append( ", ? "+ insertField.fieldName );
				sbuf_col.append( ", D."+ insertField.fieldName );
				sbuf_val.append( ", S."+ insertField.fieldName );
			}
			sbuf_src.replace( 0, 2, "SELECT " ).append( " FROM DUAL" );
			sbuf_col.delete( 0, 2 );
			sbuf_val.delete( 0, 2 );

			StringBuffer sbuf_cond = new StringBuffer();
			for( Table.Field primaryField: primaryFields )
				sbuf_cond.append( " AND D."+ primaryField.fieldName +" = S."+ primaryField.fieldName );
			sbuf_cond.delete( 0, 5 );

			StringBuffer sbuf_upd = new StringBuffer();
			for( Table.Field updateField : updateBindFields ) {
				if( updateField.primary ) continue;

				sbuf_upd.append( ", D."+ updateField.fieldName );
				if( !updateField.nullable() && updateField.optional )
					sbuf_upd.append( " = NVL(S."+ updateField.fieldName +", D."+ updateField.fieldName +")" );
				else if( updateField.nullable() && !updateField.optional )
					sbuf_upd.append( " = NVL(S."+ updateField.fieldName +", D."+ updateField.fieldName +")" );
				else
					sbuf_upd.append( " = S."+ updateField.fieldName );
			}
			sbuf_upd.delete( 0, 2 );
			if( extraUpdateQuery != null )
				sbuf_upd.append( ", " ).append( extraUpdateQuery );

			sbuf = new StringBuffer();
			sbuf.append( "MERGE INTO "+ name +" D" )
			.append( " USING ( " ).append( sbuf_src ).append( ") S" )
			.append( " ON (" ).append( sbuf_cond ).append( ")" )
			.append( " WHEN MATCHED THEN UPDATE SET " ).append( sbuf_upd )
			.append( " WHEN NOT MATCHED THEN INSERT (" ).append( sbuf_col ).append( ") VALUES (" ).append( sbuf_val ).append( ")" );

			return sbuf.toString();
		case Record.DELETE:
			sbuf = new StringBuffer().append( "DELETE " ).append( name );
			String primaryConditionQuery = makePrimaryConditionQuery();
			if( primaryConditionQuery != null );
			sbuf.append( " WHERE " ).append( primaryConditionQuery );

			return sbuf.toString();
		default:
			throw new IllegalArgumentException( "illegal statementType '"+ statementType +"'" );
		}
	}

	public String makeUpdateStatement( String... fieldKeys ) {
		return makeUpdateStatement( getAlterableFieldArray(fieldKeys), false );
	}

	public String makeUpdateStatement( String[] fieldKeys, boolean updateSetClauseOnly ) {
		return makeUpdateStatement( getAlterableFieldArray(fieldKeys), updateSetClauseOnly );
	}

	String makeUpdateStatement( Table.Field[] tfields, boolean updateSetClauseOnly ) {
		StringBuffer sbuf = new StringBuffer();

		if( !updateSetClauseOnly )
			sbuf.append( "UPDATE " ).append( name ).append( " SET " );

		for( Table.Field tfield : tfields ) {
			if( tfield.readonly() ) continue;

			sbuf.append( tfield.fieldName );
			if( !tfield.nullable() && tfield.optional )
				sbuf.append( " = NVL(?, "+ tfield.fieldName +")," );
			else if( tfield.nullable() && !tfield.optional )
				sbuf.append( " = NVL(?, "+ tfield.fieldName +")," );
			else
				sbuf.append( " = ?," );
		}
		if( extraUpdateQuery != null )
			sbuf.append( extraUpdateQuery );
		else
			sbuf.setCharAt( sbuf.length() - 1, ' ' );

		if( !updateSetClauseOnly ) {
			String primaryConditionQuery = makePrimaryConditionQuery();
			if( primaryConditionQuery != null )
				sbuf.append( " WHERE " ).append( primaryConditionQuery );
		}

		return sbuf.toString();
	}

	public QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) throws FieldException {
		if( primaryFields == null ) return querybuf;

		Map conditionMap = querybuf.getConditionMap();

		querybuf.appendTableWithAlias( name, alias );
		for( Table.Field primaryField : primaryFields ) {
			Object value = primaryField.extractValue( conditionMap );
			querybuf.appendCondition( alias +"."+ primaryField.fieldName +" = ?", value );
		}

		return querybuf;
	}

	public Object[] validate( Map recordMap, boolean inserting ) throws FieldException {
		return Record.validate( recordMap, inserting ? insertBindFields : updateBindFields );
	}

	public Object[] validate( Map recordMap, int statementType ) throws FieldException, IllegalArgumentException {
		switch( statementType ) {
		case Record.INSERT:
		case Record.MERGE:
			return Record.validate( recordMap, insertBindFields );
		case Record.UPDATE:
			return Record.validate( recordMap, updateBindFields );
		case Record.DELETE:
		case Record.QUERY:
			return Record.validate( recordMap, primaryFields );
		default:
			throw new IllegalArgumentException( "illegal statementType '"+ statementType +"'" );
		}
	}

	/**
	 *
	 */
	public static class Field extends com.irt.data.ValidableField implements QueryableField {
		Table table;
		String fieldName;
		String insertQuery;
		boolean primary, optional, alterable;

		protected Field( Table.Field field ) {
			this( field, field.nullable, field.readonly );
		}

		Field( Table.Field field, Table table ) {
			this( field, field.nullable, field.readonly );
			this.table = table;
		}

		Field( Table.Field field, boolean nullable, boolean readonly ) {
			super( field, nullable, readonly );

			this.table = field.table;
			this.fieldName = field.fieldName;
			this.insertQuery = field.insertQuery;
			this.primary = field.primary;
			this.optional = field.optional;
			this.alterable = field.alterable;
		}

		Field( char dataType, String fieldKey, String fieldName, String descriptionKey ) {
			super( dataType, fieldKey, descriptionKey );
			this.fieldName = fieldName;
		}

		public boolean alterable() {
			return alterable;
		}

		@Override
		public boolean appendCondition( ConditionQueryBuffer querybuf ) {
			String query = table.alias +"."+ fieldName;

			switch( dataType ) {
			case AbstractField.TYPE_NONE:
				return false;
			case AbstractField.TYPE_CODE:
				return( querybuf.findConditionCode( fieldKey, query ) > 0 );
			case AbstractField.TYPE_DESCRIPTION:
				return( querybuf.findCondition( fieldKey, query, Condition.CONDTYPE_STARTSWITH, false ) > 0 );
			case AbstractField.TYPE_STRING:
			case AbstractField.TYPE_TIME:
				return( querybuf.findCondition( fieldKey, query ) > 0 );
			case AbstractField.TYPE_INTEGER:
			case AbstractField.TYPE_LONG:
			case AbstractField.TYPE_DOUBLE:
				return querybuf.findConditionNumber( fieldKey, query, Condition.CONDTYPE_EQUALS_MINMAX );
			case AbstractField.TYPE_DATE:
			case AbstractField.TYPE_DATETIME:
				return querybuf.findConditionDate( fieldKey, query, Condition.CONDTYPE_EQUALS_MINMAX );
			default:
				return false;
			}
		}

		@Override
		public boolean appendData( QueryBuffer querybuf ) {
			return querybuf.appendDataWithAlias( table.alias +"."+ fieldName, fieldKey );
		}

		public String getFieldName() {
			return fieldName;
		}

		public Table getTable() {
			return table;
		}

		public boolean primary() {
			return primary;
		}

		public Table.Field setInsertQuery( String insertQuery ) {
			if( table == null ) this.insertQuery = insertQuery;
			return this;
		}

		public Table.Field setMinMaxValue( Number minValue, Number maxValue ) {
			if( table == null ) {
				this.minValue = minValue;
				this.maxValue = maxValue;
			}
			return this;
		}

		public Table.Field setMinMaxValue( Number minValue, Number maxValue, int rangeType ) {
			if( table == null ) {
				this.minValue = minValue;
				this.maxValue = maxValue;
				this.rangeType = rangeType;
			}
			return this;
		}

		public Table.Field setNullable( boolean nullable ) {
			if( table == null ) this.nullable = nullable;
			return this;
		}

		public Table.Field setReadonly( boolean readonly ) {
			if( table == null ) this.readonly = readonly;
			return this;
		}

		public Table.Field setValidCharacters( String validCharacters, boolean trailingBlank ) {
			if( table == null && dataType == AbstractField.TYPE_STRING ) {
				this.validCharacters = validCharacters;
				this.trailingBlank = trailingBlank;
			}
			return this;
		}

		public Table.Field setValidValueList( String prefixKey, String validValueList ) {
			if( table == null && dataType == AbstractField.TYPE_CODE ) {
				this.prefixKey = prefixKey;
				this.validValueList = validValueList;
			}
			return this;
		}
	}
}
