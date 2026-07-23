/*
 *	File Name:	ItemFieldSet.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.1	extends com.irt.data.FieldSet -> extends com.irt.data.ValidableFieldSet
 *	stghr12		2007/04/30		2.1.0	initialize(): iCategoryCode, categoryCode mandatory 삭제
 *										validate(): GTIN 검사 버그 수정
 *	stghr12		2006/03/31		2.0.0	create
 *
 **/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.rbm.RBMDataException;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import java.util.Map;

/**
 *
 */
class ItemFieldSet extends com.irt.data.ValidableFieldSet {
	private boolean inserting;
	private static ItemFieldSet fieldSet_i, fieldSet_u;
	private ValidableField field_gtin = (ValidableField)Schema.findTable(Schema.ECS_ITEM).getField("gtin");

	private ItemFieldSet( boolean inserting, Map<String, ValidableField> fieldMap ) {
		super( fieldMap );
		this.inserting = inserting;
	}

	private static void append( Map<String, ValidableField> fieldMap, Table table ) {
		for( ValidableField field : table.getFieldMap().values() ) {
			String fieldKey = field.getFieldKey();

			if( "gtin".equals(fieldKey) || "gln".equals(fieldKey) ) continue;
			if( !field.nullable() ) field = new ValidableField( field, true, field.readonly() );
			fieldMap.put( fieldKey, field );
		}
	}

	private static void append( Map<String, ValidableField> fieldMap, Table table, String[] fieldKeys ) {
		for( int f = 0; f < fieldKeys.length; f++ ) {
			ValidableField field = (ValidableField)table.getField( fieldKeys[f] );

			if( !field.nullable() ) field = new ValidableField( field, true, field.readonly() );
			fieldMap.put( field.getFieldKey(), field );
		}
	}

	public static ItemFieldSet getInstance( boolean inserting ) {
		if( fieldSet_i == null ) initialize();
		return( inserting ? fieldSet_i : fieldSet_u );
	}

	private static synchronized void initialize() {
		if( fieldSet_i != null ) return;

		Map<String, ValidableField> fieldMap = new java.util.HashMap<String, ValidableField>();
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_HIERARCHY) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_MEASURE) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_PACKAGING) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_HANDLING) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_ORDERING) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_FASHION) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_ATTRIBUTE) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_LINK), new String[] { "lowerLevelGtin", "lowerQty" } );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_ORIGIN), new String[] { "originCode" } );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_DESCRIPTION) );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_SEASON), new String[] { "seasonCode" } );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_MANUFGLN), new String[] { "manufGln" } );
		append( fieldMap, Schema.findTable(Schema.ECS_ITEM_PRIVATEGLN), new String[] { "privateGln", "publicationDate" } );

		Map<String, ValidableField> fieldMap_i = fieldMap;
		Map<String, ValidableField> fieldMap_u = new java.util.HashMap<String, ValidableField>( fieldMap );

		Table table = Schema.findTable( Schema.ECS_ITEM_INFO );
		fieldMap_i.putAll( table.getValidableFieldSet(true).getFieldMap() );
		fieldMap_u.putAll( table.getValidableFieldSet(false).getFieldMap() );

		table = Schema.findTable( Schema.ECS_ITEM );
		fieldMap_i.putAll( table.getValidableFieldSet(true).getFieldMap() );
		fieldMap_u.putAll( table.getValidableFieldSet(false).getFieldMap() );

		fieldMap_i.put( "itemKind", new ValidableField( fieldMap_i.remove( "itemKind" ), false, false ) );
		fieldMap_i.put( "itemUnit", new ValidableField( fieldMap_i.remove( "itemUnit" ), false, false ) );
		fieldMap_i.put( "iCategoryCode", new ValidableField( fieldMap_i.remove( "iCategoryCode" ), true, false ) );
		fieldMap_i.put( "categoryCode", new ValidableField( fieldMap_i.remove( "categoryCode" ), true, false ) );

		fieldMap_u.put( "itemKind", new ValidableField( fieldMap_u.remove( "itemKind" ), false, false ) );
		fieldMap_u.put( "iCategoryCode", new ValidableField( fieldMap_u.remove( "iCategoryCode" ), true, false ) );
		fieldMap_u.put( "categoryCode", new ValidableField( fieldMap_u.remove( "categoryCode" ), true, false ) );

		fieldSet_i = new ItemFieldSet( true, fieldMap_i );
		fieldSet_u = new ItemFieldSet( false, fieldMap_u );
	}

	public void validate( SQLHandler handler, Map recordMap ) throws DataException {
		if( inserting ) {
			try {
				String gtin = (String)field_gtin.validate( recordMap );
				Object gtinType = Record.extractValue( recordMap, "gtinType" );
				if( "E08".equals(gtinType) ) {
					if( gtin.length() != 8 ) throw new FieldException( FieldException.ERR_INVALID_LENGTH, field_gtin, gtin );
					if( !Item.checkGtin(gtin) ) throw handler.createDataException( RBMDataException.ERR_INVALID_GTIN, recordMap );
				} else if( "E13".equals(gtinType) || "P13".equals(gtinType) ) {
					if( gtin.length() != 13 ) throw new FieldException( FieldException.ERR_INVALID_LENGTH, field_gtin, gtin );
					if( !Item.checkGtin(gtin) ) throw handler.createDataException( RBMDataException.ERR_INVALID_GTIN, recordMap );
				} else if( "E14".equals(gtinType) ) {
					if( gtin.length() != 14 ) throw new FieldException( FieldException.ERR_INVALID_LENGTH, field_gtin, gtin );
					if( !Item.checkGtin(gtin) ) throw handler.createDataException( RBMDataException.ERR_INVALID_GTIN, recordMap );
				} else if( "UPC".equals(gtinType) || "NDC".equals(gtinType) ) {
					if( gtin.length() != 12 ) throw new FieldException( FieldException.ERR_INVALID_LENGTH, field_gtin, gtin );
				} else if( "GTN".equals(gtinType) ) {
					if( !Item.checkGtin(gtin) ) throw handler.createDataException( RBMDataException.ERR_INVALID_GTIN, recordMap );
				}
			} catch( FieldException fieldEx ) {
				throw handler.createDataException( fieldEx, recordMap );
			}
		}
	}
}
