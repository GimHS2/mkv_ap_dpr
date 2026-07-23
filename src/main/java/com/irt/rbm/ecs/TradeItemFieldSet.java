/*
 *	File Name:	TradeItemFieldSet.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.0	extends com.irt.data.FieldSet -> extends com.irt.data.ValidableFieldSet
 *										getInstance(): 오류 수정
 *	stghr12		2006/03/31		2.0.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.ValidableField;
import com.irt.sql.Table;
import java.util.Map;

/**
 *
 */
class TradeItemFieldSet extends com.irt.data.ValidableFieldSet {
	private static TradeItemFieldSet fieldSet_i, fieldSet_u;

	private TradeItemFieldSet( Map<String, ValidableField> fieldMap ) {
		super( fieldMap );
	}

	private static void append( Map<String, ValidableField> fieldMap, Table table ) {
		for( ValidableField field : table.getFieldMap().values() ) {
			String fieldKey = field.getFieldKey();

			if( "buyerGln".equals(fieldKey) || "sellerGln".equals(fieldKey) || "gln".equals(fieldKey) ) continue;
			if( !field.nullable() ) field = new ValidableField( field, true, field.readonly() );
			fieldMap.put( fieldKey, field );
		}
	}

	public static TradeItemFieldSet getInstance( boolean inserting ) {
		if( fieldSet_i == null ) initialize();
		return( inserting ? fieldSet_i : fieldSet_u );
	}

	static synchronized void initialize() {
		if( fieldSet_i != null ) return;

		Map<String, ValidableField> fieldMap = new java.util.HashMap<String, ValidableField>();
		append( fieldMap, Schema.findTable(Schema.ECS_ITEMTP_INFO) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEMTP_ORDERING) );

		Map<String, ValidableField> fieldMap_i = fieldMap;
		Map<String, ValidableField> fieldMap_u = new java.util.HashMap<String, ValidableField>( fieldMap );

		Table table = Schema.findTable( Schema.ECS_ITEMTP );
		fieldMap_i.putAll( table.getFieldMap() );
		fieldMap_u.putAll( table.getValidableFieldSet(false).getFieldMap() );

		fieldSet_i = new TradeItemFieldSet( fieldMap_i );
		fieldSet_u = new TradeItemFieldSet( fieldMap_u );
	}
}
