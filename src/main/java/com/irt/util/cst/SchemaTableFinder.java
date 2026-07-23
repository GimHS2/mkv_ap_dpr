/*
 *	File Name:	SchemaTableFinder.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/01/30		2.2.0c	create
 *
**/

package com.irt.util.cst;

import com.irt.sql.QueryFactory;
import com.irt.sql.Schema;
import com.irt.sql.Table;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Usage:
 * 
 * <pre>
 * public void someTableOperation() {
 *		Table userTable = SchemaTableFinder.findSchemaTable( this.getClass().getClassLoader(), "com.irt.rbm.usr.Schema", "UserUser" );
 *
 *		....
 * }
 * </pre>
 * 
 * <pre>
 * public void someTableOperation() {
 *		Table userTable = SchemaTableFinder
 *							.withSchemaClassName( "com.irt.rbm.usr.Schema" )
 *							.withSchemaTableKey( "UserUser" )
 *							.getTable();
 *		....
 * }
 * </pre>
 *
 */
public class SchemaTableFinder {//@formatter:on
	public static SchemaTableFinder create() {
		return new SchemaTableFinder();
	}

	/**
	 *
	 * @param loader
	 * @param schemaClassName
	 *            : Schema class file's package path with dot. eg) com.irt.rbm.usr.Schema
	 * @param tableOrQueryFactoryKey
	 *            : in Schema class "final static" defined String value in java class. eg) "UserUser"
	 * @return
	 */
	public static QueryFactory findSchemaQueryFactory( ClassLoader loader, String schemaClassName, String tableOrQueryFactoryKey ) {
		Class<? extends Schema> object = null;
		Method method = null;
		QueryFactory factory = null;
		try {
			schemaClassName = resolveInnerClass(schemaClassName);

			object = (Class<? extends Schema>)loader.loadClass(schemaClassName);
			method = object.getMethod("findQueryFactory", String.class);
			factory = (QueryFactory)method.invoke(null, tableOrQueryFactoryKey);
		} catch( Exception ignoredEx ) {
			Logger.getRootLogger().error(
					"[WARNING] loader: " + loader + " schemaClassName: " + schemaClassName + " tableKey: " + tableOrQueryFactoryKey,
					ignoredEx);
		}
		return factory;
	}

	/**
	 *
	 * @param loader
	 * @param schemaClassName
	 *            : Schema class file's package path with dot. eg) com.irt.rbm.usr.Schema
	 * @param tableKey
	 *            : in Schema class "final static" defined String value in java class. eg) "UserUser"
	 * @return
	 */
	public static Table findSchemaTable( ClassLoader loader, String schemaClassName, String tableKey ) {
		Class<? extends Schema> object = null;
		Method method = null;
		Table table = null;
		try {
			schemaClassName = resolveInnerClass(schemaClassName);

			object = (Class<? extends Schema>)loader.loadClass(schemaClassName);
			method = object.getMethod("findTable", String.class);
			table = (Table)method.invoke(null, tableKey);
		} catch( Exception ignoredEx ) {
			Logger.getRootLogger().error(
					"[WARNING] loader: " + loader + " schemaClassName: " + schemaClassName + " tableKey: " + tableKey,
					ignoredEx);
		}
		return table;
	}

	public static Map<Entry<String, String>, Table> getSchemaTableMap( Class<? extends com.irt.sql.Schema>... schemaClasses )
			throws IllegalArgumentException, IllegalAccessException {
		return getSchemaTableMap(new ArrayList<Class<? extends com.irt.sql.Schema>>(java.util.Arrays.asList(schemaClasses)));
	}

	// public static List<Entry<String, String>> getSchemaTableKeys( Class<? extends com.irt.sql.Schema> schemaClass )
	// throws IllegalArgumentException, IllegalAccessException {
	// Map<String, java.lang.reflect.Field> fieldMap = ReflectUtil.getDeclaredFields(schemaClass, null);
	// List<Entry<String, String>> tabkeys = new ArrayList<Entry<String, String>>();
	// for( String fieldName : fieldMap.keySet() ) {
	// java.lang.reflect.Field fd = fieldMap.get(fieldName);
	// if( java.lang.reflect.Modifier.isPublic(fd.getModifiers()) ) {
	// if( java.lang.reflect.Modifier.isStatic(fd.getModifiers()) ) {
	// if( java.lang.reflect.Modifier.isFinal(fd.getModifiers()) ) {
	// Object fieldObjectValue = ReflectUtil.getDeclaredFieldObject(schemaClass, null, fieldName);
	//
	// if( fieldObjectValue != null && fieldObjectValue instanceof String ) {
	// String schemaTableKey = (String)fieldObjectValue;
	// Table table = SchemaTableFinder.findSchemaTable(schemaClass.getClassLoader(), schemaClass.getCanonicalName(),
	// schemaTableKey);
	// if( table != null ) {
	// Map.Entry entry = new AbstractMap.SimpleEntry(schemaClass.getCanonicalName(), schemaTableKey);
	// tabkeys.add(entry);
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// return tabkeys;
	// }

	public static Map<Entry<String, String>, Table> getSchemaTableMap( List<Class<? extends com.irt.sql.Schema>> schemaClasses )
			throws IllegalArgumentException, IllegalAccessException {
		Map<Entry<String, String>, Table> schemaTableMap = new HashMap<Entry<String, String>, Table>();
		for( Class<? extends com.irt.sql.Schema> schemaClass : schemaClasses ) {
			Map<String, java.lang.reflect.Field> fieldMap = ReflectUtil.getDeclaredFields(schemaClass, null);
			for( String fieldName : fieldMap.keySet() ) {
				java.lang.reflect.Field fd = fieldMap.get(fieldName);
				if( java.lang.reflect.Modifier.isPublic(fd.getModifiers()) ) {
					if( java.lang.reflect.Modifier.isStatic(fd.getModifiers()) ) {
						if( java.lang.reflect.Modifier.isFinal(fd.getModifiers()) ) {
							Object fieldObjectValue = ReflectUtil.getDeclaredFieldObject(schemaClass, null, fieldName);

							if( fieldObjectValue != null && fieldObjectValue instanceof String ) {
								String schemaTableKey = (String)fieldObjectValue;
								Table table = SchemaTableFinder.findSchemaTable(schemaClass.getClassLoader(), schemaClass.getCanonicalName(),
										schemaTableKey);
								if( table != null ) {
									Map.Entry entry = new AbstractMap.SimpleEntry(schemaClass.getCanonicalName(), schemaTableKey);
									schemaTableMap.put(entry, table);
								}
							}
						}
					}
				}
			}

		}
		return schemaTableMap;

	}

	private static String resolveInnerClass( String schemaClassName ) {
		String[] hrcy = schemaClassName.split("\\.");
		String in_last = hrcy[hrcy.length - 2];
		String maybe_innerClass = hrcy[hrcy.length - 1];
		if( maybe_innerClass.substring(0, 1).matches("[A-Z]") ) {
			if( in_last != null && in_last.substring(0, 1).matches("[A-Z]") ) {
				schemaClassName = schemaClassName.replaceFirst("." + maybe_innerClass + "$", "\\$" + maybe_innerClass);
			}
		}

		return schemaClassName;
	}

	private String schemaClassName;

	private String schemaTableKey;

	public SchemaTableFinder() {
	}

	public String getSchemaClassName() {
		return schemaClassName;
	}

	public String getSchemaTableKey() {
		return schemaTableKey;
	}

	public Table getTable() {
		return findSchemaTable(this.getClass().getClassLoader(), this.getSchemaClassName(), this.getSchemaTableKey());
	}

	public Table getTableOrThrow() {
		Table table = getTable();
		if( table == null ) {
			throw new ShouldTableBeFound();
		}

		return table;
	}

	public void setSchemaClassName( String className ) {
		this.schemaClassName = className;
	}

	public void setSchemaTableKey( String tableKey ) {
		this.schemaTableKey = tableKey;
	}

	public SchemaTableFinder withSchemaClassName( String className ) {
		setSchemaClassName(className);
		return this;
	}

	public SchemaTableFinder withSchemaTableKey( String tableKey ) {
		setSchemaTableKey(tableKey);
		return this;
	}

	class ShouldTableBeFound extends RuntimeException {

	}

}
