/*
 *	File Name:	Schema.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										DATETIME 추가, validDateFormat 삭제
 *	stghr12		2007/10/31		2.1.2	EQU_*를 protected에서 public으로 변경
 *	stghr12		2007/04/30		2.1.1	createQFD(), T, F 추가
 *	stghr12		2006/12/01		2.1.0	dataType을 int형에서 char형으로 변경
 *										dataType상수(NONE, CODE, DESC 등)를 protected에서 public으로 변경
 *										dataType상수 TIME(AbstractField.TYPE_TIME) 추가
 *	stghr12		2006/07/07		2.0.1	rangeType 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import com.irt.data.*;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class Schema {
	public final static char NONE						= AbstractField.TYPE_NONE;
	public final static char CODE						= AbstractField.TYPE_CODE;
	public final static char DESC						= AbstractField.TYPE_DESCRIPTION;
	public final static char STRING						= AbstractField.TYPE_STRING;
	public final static char INTEGER					= AbstractField.TYPE_INTEGER;
	public final static char LONG						= AbstractField.TYPE_LONG;
	public final static char DOUBLE						= AbstractField.TYPE_DOUBLE;
	public final static char DATE						= AbstractField.TYPE_DATE;
	public final static char DATETIME					= AbstractField.TYPE_DATETIME;
	public final static char TIME						= AbstractField.TYPE_TIME;

	public final static int EQU_NN						= ValidableField.RANGETYPE_EQUALS_NONE;
	public final static int EQU_YN						= ValidableField.RANGETYPE_EQUALS_MIN;
	public final static int EQU_NY						= ValidableField.RANGETYPE_EQUALS_MAX;
	public final static int EQU_YY						= ValidableField.RANGETYPE_EQUALS_MINMAX;

	protected final static String READONLY				= "Y----";
	protected final static String PRIMARY				= "NY---";
	protected final static String MANDATORY				= "NNNNY";
	protected final static String OPTIONAL				= "NNY-Y";

	/** Read Only **/
	protected final static String RD					= READONLY;
	/** Primary **/
	protected final static String PM					= PRIMARY;
	/** Mandatory(inserting, updating) & Alterable **/
	protected final static String MD					= MANDATORY;
	/** Mandatory(inserting) & Optional(updating) & Alterable **/
	protected final static String MO					= "NNNYY";
	/** Mandatory(inserting) & Not Alterable **/
	protected final static String MR					= "NNNNN";
	/** Optional(inserting, updating) & Alterable **/
	protected final static String OP					= OPTIONAL;
	/** Optional(inserting) & Not Alterable **/
	protected final static String OR					= "NNY-N";

	/** true **/
	protected final static boolean T					= true;
	/** false **/
	protected final static boolean F					= false;

	private Map<String, Object[]> objectMap;

	protected Schema() {
		this.objectMap = new java.util.HashMap<String, Object[]>();
	}

	/** TYPE_STRING */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, int minLength, int maxLength ) {
		return createFD( option, fieldKey, fieldName, descriptionKey, minLength, maxLength, null, false );
	}

	/** TYPE_STRING */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, int minLength, int maxLength
						, String validCharacters ) {
		return createFD( option, fieldKey, fieldName, descriptionKey, minLength, maxLength, validCharacters, false );
	}

	/** TYPE_STRING */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, int minLength, int maxLength
						, String validCharacters, boolean trailingBlank ) {
		Table.Field tfield = createFD( option, fieldKey, fieldName, descriptionKey, AbstractField.TYPE_STRING, minLength, maxLength );
		return tfield.setValidCharacters( validCharacters, trailingBlank );
	}

	/** TYPE_CODE */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey
						, String prefixKey, String validValueList ) {
		Table.Field tfield = createFD( option, fieldKey, fieldName, descriptionKey, AbstractField.TYPE_CODE );
		return tfield.setValidValueList( prefixKey, validValueList );
	}

	/** TYPE_DOUBLE */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey
						, double minValue, double maxValue ) {
		return createFD( option, fieldKey, fieldName, descriptionKey, AbstractField.TYPE_DOUBLE, new Double(minValue), new Double(maxValue) );
	}

	/** TYPE_DOUBLE */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey
						, double minValue, double maxValue, int rangeType ) {
		return createFD( option, fieldKey, fieldName, descriptionKey, AbstractField.TYPE_DOUBLE, new Double(minValue), new Double(maxValue)
				, rangeType );
	}

	/** TYPE_LONG */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey
						, long minValue, long maxValue ) {
		return createFD( option, fieldKey, fieldName, descriptionKey, AbstractField.TYPE_LONG, new Long(minValue), new Long(maxValue) );
	}

	/** TYPE_LONG */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey
						, long minValue, long maxValue, int rangeType ) {
		return createFD( option, fieldKey, fieldName, descriptionKey, AbstractField.TYPE_LONG, new Long(minValue), new Long(maxValue), rangeType );
	}

	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, char dataType ) {
		return setBooleanOption( new Table.Field( dataType, fieldKey, fieldName, descriptionKey ), option );
	}

	/** TYPE_INTEGER, TYPE_STRING, TYPE_DESCRIPTION */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, char dataType
						, int minLength, int maxLength ) {
		Table.Field tfield = createFD( option, fieldKey, fieldName, descriptionKey, dataType );

		Number minValue = null, maxValue = null;
		if( dataType == AbstractField.TYPE_INTEGER ) {
			minValue = new Integer( minLength );
			maxValue = new Integer( maxLength );
		} else {
			if( minLength > 0 ) minValue = new Integer( minLength );
			if( maxLength == minLength )
				maxValue = minValue;
			else if( maxLength > 0 ) maxValue = new Integer( maxLength );
		}

		return tfield.setMinMaxValue( minValue, maxValue );
	}

	/** TYPE_INTEGER */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, char dataType
						, int minLength, int maxLength, int rangeType ) {
		Table.Field tfield = createFD( option, fieldKey, fieldName, descriptionKey, dataType );

		Number minValue = null, maxValue = null;
		if( dataType == AbstractField.TYPE_INTEGER ) {
			minValue = new Integer( minLength );
			maxValue = new Integer( maxLength );
		}

		return tfield.setMinMaxValue( minValue, maxValue, rangeType );
	}

	/** TYPE_INTEGER, TYPE_LONG, TYPE_DOUBLE */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, char dataType
						, Number minValue, Number maxValue ) {
		Table.Field tfield = createFD( option, fieldKey, fieldName, descriptionKey, dataType );
		return tfield.setMinMaxValue( minValue, maxValue );
	}

	/** TYPE_INTEGER, TYPE_LONG, TYPE_DOUBLE */
	protected static Table.Field createFD( String option, String fieldKey, String fieldName, String descriptionKey, char dataType
						, Number minValue, Number maxValue, int rangeType ) {
		Table.Field tfield = createFD( option, fieldKey, fieldName, descriptionKey, dataType );
		return tfield.setMinMaxValue( minValue, maxValue, rangeType );
	}

	/** TYPE_STRING */
	protected static QueryableFieldImpl createQFD( boolean conditionable, String fieldKey, String query, String descriptionKey ) {
		return new QueryableFieldImpl( AbstractField.TYPE_STRING, conditionable, fieldKey, query, descriptionKey );
	}

	/** TYPE_STRING */
	protected static QueryableFieldImpl createQFD( boolean conditionable, String fieldKey, String query, String descriptionKey, Joinable joinable ) {
		return new QueryableFieldImpl( AbstractField.TYPE_STRING, conditionable, fieldKey, query, descriptionKey, joinable );
	}

	/** TYPE_CODE */
	protected static QueryableFieldImpl createQFD( boolean conditionable, String fieldKey, String query, String descriptionKey
						, String prefixKey ) {
		return new QueryableFieldImpl( AbstractField.TYPE_CODE, conditionable, fieldKey, query, descriptionKey, prefixKey );
	}

	/** TYPE_CODE */
	protected static QueryableFieldImpl createQFD( boolean conditionable, String fieldKey, String query, String descriptionKey
						, String prefixKey, Joinable joinable ) {
		return new QueryableFieldImpl( AbstractField.TYPE_CODE, conditionable, fieldKey, query, descriptionKey, prefixKey, joinable );
	}

	protected static QueryableFieldImpl createQFD( boolean conditionable, String fieldKey, String query, String descriptionKey
						, char dataType ) {
		return new QueryableFieldImpl( dataType, conditionable, fieldKey, query, descriptionKey );
	}

	protected static QueryableFieldImpl createQFD( boolean conditionable, String fieldKey, String query, String descriptionKey
						, char dataType, Joinable joinable ) {
		return new QueryableFieldImpl( dataType, conditionable, fieldKey, query, descriptionKey, joinable );
	}

	public static Table createTable( String name, String alias, Table.Field[] fields ) {
		Table.Field[] fields_new = new Table.Field[ fields.length ];
		for( int f = 0; f < fields.length; f++ ) {
			fields_new[f] = fields[f];
			if( fields_new[f].table != null )
				fields_new[f] = new Table.Field( fields[f], null );
		}

		return new Table( name, alias, fields_new );
	}

	public static Table createTable( String name, String alias, Table.Field[] fields, String extraUpdateQuery ) {
		Table.Field[] fields_new = new Table.Field[ fields.length ];
		for( int f = 0; f < fields.length; f++ ) {
			fields_new[f] = fields[f];
			if( fields_new[f].table != null )
				fields_new[f] = new Table.Field( fields[f], null );
		}

		return new Table( name, alias, fields_new, extraUpdateQuery );
	}

	protected Set<String> getMessageKey() {
		Set<String> messageKeySet = new java.util.HashSet<String>();

		for( Object[] objects : objectMap.values() ) {
			if( objects[0] != null ) {
				for( QueryableField qfield : ((Queryable)objects[0]).getQueryableFieldMap().values() ) {
					messageKeySet.add( qfield.getDescriptionKey() );
					if( qfield instanceof ValidableField ) {
						String prefixKey = qfield.getPrefixKey();
						String[] validValues = ((ValidableField)qfield).getValidValues();
						if( prefixKey != null && validValues != null ) {
							for( int i = 0; i < validValues.length; i++ )
								messageKeySet.add( prefixKey + validValues[i] );
						}
					}
				}
			}

			if( objects[1] != null ) {
				for( QueryableField qfield : ((Queryable)objects[1]).getQueryableFieldMap().values() ) {
					messageKeySet.add( qfield.getDescriptionKey() );
					if( qfield instanceof ValidableField ) {
						String prefixKey = qfield.getPrefixKey();
						String[] validValues = ((ValidableField)qfield).getValidValues();
						if( prefixKey != null && validValues != null ) {
							for( int i = 0; i < validValues.length; i++ )
								messageKeySet.add( prefixKey + validValues[i] );
						}
					}
				}
			}
		}

		return messageKeySet;
	}

	protected Queryable getQueryable( String key ) {
		Object[] objects = objectMap.get( key );
		return (Queryable)( objects == null ? null : objects[1] );
	}

	protected Table getTable( String key ) {
		Object[] objects = objectMap.get( key );
		return (Table)( objects == null ? null : objects[0] );
	}

	protected void putQueryable( String key, Queryable queryable ) {
		Object[] objects = objectMap.put( key, new Object[] { null, queryable } );
		if( objects != null ) {
			objects[1] = queryable;
			objectMap.put( key, objects );
		}
	}

	protected void putTable( String key, Table table ) {
		Object[] objects = objectMap.put( key, new Object[] { table, table } );
		if( objects != null ) {
			objects[0] = table;
			objectMap.put( key, objects );
		}
	}

	private static Table.Field setBooleanOption( Table.Field tfield, String booleanOptionValue ) {
		if( booleanOptionValue.charAt(0) == 'Y' ) {
			tfield.optional = true;
			tfield.primary = tfield.alterable = false;
			tfield.setNullable( true );
			tfield.setReadonly( true );
		} else if( tfield.primary = (booleanOptionValue.charAt(1) == 'Y') ) {
			tfield.optional = tfield.alterable = false;
			tfield.setNullable( false );
		} else if( booleanOptionValue.charAt(2) == 'Y' ) {
			tfield.optional = true;
			tfield.alterable = (booleanOptionValue.charAt(4) == 'Y');
			tfield.setNullable( true );
		} else {
			tfield.optional = (booleanOptionValue.charAt(3) == 'Y');
			tfield.alterable = (booleanOptionValue.charAt(4) == 'Y');
			tfield.setNullable( false );
		}

		return tfield;
	}
}
