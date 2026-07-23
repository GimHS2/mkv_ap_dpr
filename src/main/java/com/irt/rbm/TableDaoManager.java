/*
 *	File Name:	TableDaoException.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/06/30		2.2.0c	create
 *
**/

package com.irt.rbm;

import com.irt.servlet.ParameterMap;
import com.irt.servlet.Schemas;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import com.irt.system.SessionManager;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class TableDaoManager extends ManipulableManagerImpl implements TableAccessor {//@formatter:on

	public static final String PARAM_FIELDKEYS = "fieldKeys";

	TableDao dao;

	private String schemaClassName;

	private String schemaTableKey;

	private SessionManager sessionMng;

	public TableDaoManager( SQLHandler handler, Table table, QueryFactory factory ) {
		super(handler, table, factory);
		this.dao = new TableDao(table, handler.getMessageHandler());
	}

	public boolean commit() {
		try {
			handler.commit();
			return true;
		} catch( java.sql.SQLException sqlEx ) {
			return false;
		}
	}

	/**
	 * TODO: needs to resolve when there is no key and when value is array so cannot decide which one is right value from sourceMap to primaryMap ( no
	 * access db or other instance )
	 *
	 * @param sourceMap
	 *            : usually {@link com.irt.servlet.ParameterMap}
	 * @param valueIndex
	 *            : if Object is array then get the value at 'valueIndex'
	 * @return primaryMap
	 * @throws TableDaoException
	 */
	public Map<String, Object> extractPrimary( Map<String, Object> sourceMap, int valueIndex ) throws TableDaoException {
		return dao.extractPrimary(sourceMap, valueIndex);
	}

	/**
	 *
	 * @param sourceMap
	 *            : usually {@link com.irt.servlet.ParameterMap}
	 * @return
	 * @throws TableDaoException
	 */
	public Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> sourceMap ) throws TableDaoException {
		return dao.extractPrimaryKeyValues(sourceMap);
	}

	public String[] getBindFieldKeys( int statementType ) {
		return dao.getBindFieldKeys(statementType);
	}

	public TableDao getDao() {
		return dao;
	}

	private String getPageName() {
		Schemas _schema = null;
		try {
			for( Schemas s : Schemas.values() ) {
				if( s.toString().equals(schemaClassName) ) {
					_schema = s;
					break;
				}
			}
		} catch( IllegalArgumentException ignored ) {
		}

		if( _schema != null ) {
			String systemCode = _schema.name();
			return systemCode + this.schemaTableKey;
		}

		return null;
	}
	/**
	 * @return primaryFieldKeys
	 */
	public String[] getPrimaryFieldKeys() {
		return dao.getPrimaryFieldKeys();
	}

	public QueryFactory getQueryFactory() {
		return factory;
	}

	public String[] getReadonlyFieldKeys() {
		return dao.getReadonlyFieldKeys();
	}

	public String[] getRequestFieldKeys( Map<String, Object> params ) {
		String[] fieldKeys = null;
		if( params.containsKey(PARAM_FIELDKEYS) ) {
			Object fks = params.get(PARAM_FIELDKEYS);
			if( fks instanceof String ) {
				fieldKeys = new String[] { (String)fks };
			} else if( fks instanceof String[] ) {
				fieldKeys = (String[])fks;
			} else {
				throw new UnsupportedOperationException("unsupported fieldKeys: " + fks);
			}
		}

		return fieldKeys;
	}

	public Map<String, Object> getRequestParams( HttpServletRequest req ) {
		return new ParameterMap(req);
	}

	public String getSchemaClassName() {
		return schemaClassName;
	};

	public String getSchemaTableKey() {
		return schemaTableKey;
	}

	public SessionManager getSessionManager() {
		return sessionMng;
	}

//	@Override
//	public boolean modify( Map<String, Object> recordMap ) throws DataException, SQLException {
//
//		String pageName = getPageName();
//
//		if( authService != null )
//			authService.validate(recordMap, sessionMng, pageName, Record.MODIFY);
//
//		return super.modify(recordMap);
//	}

//	@Override
//	public boolean modify( Map<String, Object> recordMap, String[] fieldKeys ) throws DataException, SQLException {
//
//		return super.modify(recordMap, fieldKeys);
//	}

	public boolean rollback() {
		try {
			handler.rollback();
			return true;
		} catch( java.sql.SQLException sqlEx ) {
			return false;
		}
	}

	public void setSchemaClassName( String schemaClassName ) {
		this.schemaClassName = schemaClassName;
	}

	public void setSchemaTableKey( String schemaTableKey ) {
		this.schemaTableKey = schemaTableKey;
	}

	public void setSessionManager( SessionManager sessionMng ) {
		this.sessionMng = sessionMng;
	}

	public TableDaoManager withSchemaClassName( String schemaClassName ) {
		setSchemaClassName(schemaClassName);
		return this;
	}

	public TableDaoManager withSchemaTableKey( String schemaTableKey ) {
		setSchemaTableKey(schemaTableKey);
		return this;
	}

	public TableDaoManager withSessionManager( SessionManager sessionMng ) {
		setSessionManager(sessionMng);
		return this;
	}

}
