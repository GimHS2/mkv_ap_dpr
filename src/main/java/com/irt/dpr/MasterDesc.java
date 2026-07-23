/*
	*	File Name:	MasterDesc.java
	*	Version:	2.2.0
	*
	*	Description:
	*
	*	Note:
	*
	*	Modified	(YYYY/MM/DD)	Ver		Content
	*	jbaek		2014/09/30		2.2.0		create
**/

package com.irt.dpr;

import java.sql.SQLException;
import java.util.Map;
import com.irt.data.DataLoader;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.sql.*;

/*
	*
	*/
public class MasterDesc extends ManipulableManagerImpl{
	private final static Table table = Schema.findTable( Schema.DPR_MASTER_DESC );
	private final static Table table_org = Schema.findTable( Schema.DPR_MASTER_DESC_ORG );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_MASTER_DESC );

	public final static String DESC_TABLE_MST = Schema.DPR_MASTER_DESC;
	public final static String DESC_TABLE_ORG = Schema.DPR_MASTER_DESC_ORG;// organization별

	public MasterDesc ( SQLHandler handler ) {
		super ( handler, table, factory );
	}

	/** MasterDesc: Master는 SAP에서 interface해오는 것이 기본이고, 사용자(Admin)가 업로드하는 것은 DPR_MASTER_DESC_ORG 테이블  */
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, int statementType )
						throws SQLException, UnsupportedOperationException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table_org, statementType );
	}

	/** MasterDesc: Master는 SAP에서 interface해오는 것이 기본이고, 사용자(Admin)가 업로드하는 것은 DPR_MASTER_DESC_ORG 테이블  */
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
						, int statementType ) throws SQLException, UnsupportedOperationException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table_org, updateFieldKeys, statementType );
	}
}
