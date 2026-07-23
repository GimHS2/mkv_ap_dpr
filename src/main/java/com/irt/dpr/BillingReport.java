/*
 *	File Name:	BillingReport.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.FieldException;
import com.irt.data.Record;
import com.irt.rbm.TableAccessor;
import com.irt.rbm.TableDaoException;
import com.irt.rbm.TableDao;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class BillingReport extends com.irt.rbm.ManipulableManagerImpl implements TableAccessor {

	private final static Table table = Schema.findTable(Schema.DPR_BILLING_REPORT);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_BILLING_REPORT);

	private TableDao tao;

	public BillingReport( SQLHandler handler ) {
		this(handler, table, factory, null);
	}

	protected BillingReport( SQLHandler handler, Table table, QueryFactory factory, QueryFactory factory_summary ) {
		super(handler, table, factory, factory_summary);
		tao = new TableDao(table, handler.getMessageHandler());
	}

	public boolean deleteWith( Map<String, Object> primaryMap, String billShipPartyCode ) throws SQLException, DataException {
		String statement = table.makeStatement(Record.DELETE);
		int statementType = Record.DELETE;

		if( billShipPartyCode != null ) {
			if( billShipPartyCode.length() > 0 ) {
				statement = statement + " AND SHIP_PARTYCD = ?";
			}
		}

		PreparedStatement pstmt = handler.getConnection().prepareStatement(statement);
		try {
			SQLManager.bindVariables(pstmt, table.validate(primaryMap, statementType));
			return ( pstmt.executeUpdate() > 0 );
		} catch( FieldException fieldEx ) {
			if( statementType == Record.DELETE )
				return false;
			throw handler.createDataException(fieldEx, primaryMap);
		} catch( SQLException sqlEx ) {
			throw handler.createDataException(sqlEx, primaryMap);
		} finally {
			try {
				pstmt.close();
			} catch( Exception ex ) {
			}
		}
	}

	@Override
	public String[] getPrimaryFieldKeys() {
		return tao.getPrimaryFieldKeys();
	}

	@Override
	public String[] getBindFieldKeys( int statementType ) {
		return tao.getBindFieldKeys(statementType);
	}

	@Override
	public String[] getReadonlyFieldKeys() {
		return tao.getReadonlyFieldKeys();
	}

	@Override
	public Map<String, Object> extractPrimary( Map<String, Object> sourceMap, int valueIndex ) throws TableDaoException {
		return tao.extractPrimary(sourceMap, valueIndex);
	}

	@Override
	public Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> sourceMap ) throws TableDaoException {
		return tao.extractPrimaryKeyValues(sourceMap);
	}


}
