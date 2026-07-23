/*
 *	File Name:	CategoryCode.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/04/30		2.1.0	getCodeField_static() 추가
 *										initCategory() 수정
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	create
 *
**/

package com.irt.rbm.sys;

import com.irt.rbm.RBMSystem;
import com.irt.sql.*;

/**
 *
 */
public class CategoryCode extends ClassCode {
	private static Table table = null;
	private static QueryFactory factory = null;
	private static HierarchyCodeField codeField = null;

	public CategoryCode( SQLHandler handler ) {
		super( handler, table, factory );
		if( table == null ) throw new IllegalArgumentException( "category length unknown" );
	}

	public static HierarchyCodeField getCodeField_static() {
		return codeField;
	}

	public static void initCategory() {
		table = null;
		factory = null;
		codeField = null;

		int[] codeLengths = null;

		if( RBMSystem.getSystemEnvBool("SYS", "CateEnv;UseCate", false) ) {
			String value = RBMSystem.getSystemEnv( "SYS", "CateEnv;CodeLength" );
			if( value != null ) {
				codeLengths = new int[value.length() / 2];
				for( int l = 0; l < value.length() / 2; l++ )
					codeLengths[l] = Integer.parseInt( value.substring(l*2, l*2+2) );
			} else {
				int length = RBMSystem.getSystemEnvInt( "SYS", "CateEnv;CateLast", 0 );
				if( length > 0 && length <= 20/3 ) {
					codeLengths = new int[length];
					for( int l = 0; l < length; l++ ) codeLengths[l] = l * 3 + 3;
				}
			}
		}
		if( codeLengths != null ) {
			Schema.loadCategory( codeLengths );
			table = Schema.findTable( Schema.SYS_CATEGORY );
			factory = Schema.findQueryFactory( Schema.SYS_CATEGORY );
			codeField = (HierarchyCodeField)table.getField( "code" );
		}
	}

	public static boolean useCategoryCode() {
		return( table != null );
	}
}
