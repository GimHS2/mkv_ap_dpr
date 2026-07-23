/*
 *	File Name:	CategoryNewTerm.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	생성자 변경
 *	stghr12		2006/06/01		2.0.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.rbm.RBMSystem;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class CategoryNewTerm extends com.irt.rbm.ManipulableManagerImpl {
	public final static String CATEGORYTYPE_CATE				= com.irt.rbm.sys.ClassCode.CATEGORYTYPE_CATE;
	public final static String CATEGORYTYPE_ICATE				= com.irt.rbm.sys.ClassCode.CATEGORYTYPE_ICATE;

	private String categoryType;

	public CategoryNewTerm( SQLHandler handler, String categoryType ) {
		super( handler
			, Schema.findTable( CATEGORYTYPE_CATE.equals(categoryType) ? Schema.ECS_CATE_NEWTERM : Schema.ECS_ICATE_NEWTERM )
			, Schema.findQueryFactory( CATEGORYTYPE_CATE.equals(categoryType) ? Schema.ECS_CATE_NEWTERM : Schema.ECS_ICATE_NEWTERM )
		);

		this.categoryType = categoryType;
	}

	public static Map<String, Object> createPrimary( String categoryCode ) {
		return Record.createMap( "baseCategoryCode", categoryCode );
	}

	public String getCategoryName( String categoryCode ) throws SQLException {
		if( CATEGORYTYPE_CATE.equals(categoryType) )
			return (String)SQLManager.getObjectValue( handler, "SELECT CATE_NAME FROM SYS_CATE WHERE CATE_CD = ?", categoryCode );
		else
			return (String)SQLManager.getObjectValue( handler, "SELECT ICATE_NAME FROM SYS_ICATE WHERE ICATE_CD = ?", categoryCode );
	}

	public int getDefaultNewTermDays() throws SQLException {
		if( CATEGORYTYPE_CATE.equals(categoryType) )
			return RBMSystem.getSystemEnvInt( "ECS", "DefaultNewTerm;Cate", 90 );
		else
			return RBMSystem.getSystemEnvInt( "ECS", "DefaultNewTerm;iCate", 90 );
	}

	public int getNewTermDays( String categoryCode ) throws SQLException {
		if( CATEGORYTYPE_CATE.equals(categoryType) )
			return ((Number)SQLManager.getObjectValue( handler, "SELECT DAYS FROM vwECS_CATE_NEWTERM WHERE CATECD = ?", categoryCode )).intValue();
		else
			return ((Number)SQLManager.getObjectValue( handler, "SELECT DAYS FROM vwECS_ICATE_NEWTERM WHERE ICATECD = ?", categoryCode )).intValue();
	}

	public void setDefaultNewTermDays( int days ) throws DataException, SQLException {
		if( CATEGORYTYPE_CATE.equals(categoryType) )
			RBMSystem.setSystemEnv( handler, "ECS", "DefaultNewTerm;Cate", String.valueOf(days) );
		else
			RBMSystem.setSystemEnv( handler, "ECS", "DefaultNewTerm;iCate", String.valueOf(days) );
	}
}
