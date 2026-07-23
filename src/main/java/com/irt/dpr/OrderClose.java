/*
 *	File Name:	OrderClose.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/06/28		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.rbm.ManipulableManagerImpl;
import com.irt.rbm.TableAccessor;
import com.irt.rbm.TableDao;
import com.irt.rbm.TableDaoException;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

import java.util.Map;

public class OrderClose extends ManipulableManagerImpl implements TableAccessor {
	private final static Table table = Schema.findTable(Schema.DPR_ORDCLOSE);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_ORDCLOSE);

	TableDao tdao;

	public OrderClose( SQLHandler handler ) {
		super(handler, table, factory);
		this.tdao = new TableDao(table, handler.getMessageHandler());
	}

	@Override
	public String[] getPrimaryFieldKeys() {
		return tdao.getPrimaryFieldKeys();
	}

	@Override
	public String[] getBindFieldKeys( int statementType ) {
		return tdao.getBindFieldKeys(statementType);
	}

	@Override
	public String[] getReadonlyFieldKeys() {
		return tdao.getReadonlyFieldKeys();
	}

	@Override
	public Map<String, Object> extractPrimary( Map<String, Object> sourceMap, int valueIndex ) throws TableDaoException {
		return tdao.extractPrimary(sourceMap, valueIndex);
	}

	@Override
	public Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> sourceMap ) throws TableDaoException {
		return tdao.extractPrimaryKeyValues(sourceMap);
	}

}
