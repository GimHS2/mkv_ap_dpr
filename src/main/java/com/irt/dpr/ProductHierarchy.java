/*
 *	File Name:	ProductHierarchy.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/05/30		2.2.2	Product Hierarchy관리 기능 추가
 *	jbaek		2014/10/30		2.2.1	Product Hierarchy Description 추가: NestedPHDJoinable()
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.DataResult;
import com.irt.data.FieldException;
import com.irt.data.Record;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.HierarchyCodeField;
import com.irt.sql.QueryBuffer;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class ProductHierarchy extends ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_PRODUCT_CATE );

	HierarchyCodeField codeField;

	public ProductHierarchy( SQLHandler handler ) {
		super( handler, table );
		if( this.codeField == null )
			this.codeField = getHierarchyField();
	}

	public static HierarchyCodeField getHierarchyField() {
		return (HierarchyCodeField)table.getQueryableField( "code" );
	}

	public static int getLength( Map<String, ? extends Object> conditionMap ) {
		return ProductHierarchy.getHierarchyField().getLength( conditionMap );
	}

	public static int getLength( String classCode ) {
		if( classCode == null ) return -1;

		HierarchyCodeField field = ProductHierarchy.getHierarchyField();

		return field.getLength( Integer.parseInt(classCode) );
	}

	public List<Map<String, Object>> getExistProductCategoris( Map<String, Object>conditionMap ) throws SQLException {
		String tableType = (String)conditionMap.get( "btype" );
		String condType = (String)conditionMap.get( "ltype" );
		String itemQuery;
		String[] bindVariableKeys;
		if( "itm".equals(tableType) ) {
			itemQuery = "SELECT DISTINCT ITM.PCATECD FROM DPR_ITEM ITM WHERE ITM.ORGANIZATIONCD = ? AND DIST_CHANNELCD = ?";
			bindVariableKeys = new String[] { "displayLanguage", "organizationCode", "distributionChannelCode" };
		} else {
			itemQuery = "SELECT DISTINCT ITM.PCATECD FROM DPR_ITEM ITM, DPR_ORDER_ITEM OITM"
						+ " WHERE ITM.ITEMCD = OITM.ITEMCD AND ITM.ORGANIZATIONCD = OITM.ORGANIZATIONCD"
							+ " AND ITM.DIST_CHANNELCD = OITM.DIST_CHANNELCD"
							+ " AND ITM.ORGANIZATIONCD = ? AND ITM.DIST_CHANNELCD = ? AND OITM.DIVISIONCD = ?";

			bindVariableKeys = new String[] { "displayLanguage", "organizationCode", "distributionChannelCode", "divisionCode" };

			if( "itm".equals(condType) ) {
				itemQuery += " AND OITM.ITEMCD = ?";
				bindVariableKeys = com.irt.util.Arrays.append( bindVariableKeys, "itemCode" );
			} else if( "pty".equals(condType) ) {
				itemQuery += " AND OITM.PARTYCD = ?";
				bindVariableKeys = com.irt.util.Arrays.append( bindVariableKeys, "partyCode" );
			}
		}

		String query = "SELECT DISTINCT PRC.PARENT_PCATECD \"parentCode\", PRC.PCATE_CD \"code\", PRC.CLASSCD \"classCode\""
					+ ", (SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PRC.PCATE_CD"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ? ) \"name\""
				+ " FROM ("
					+ "SELECT PRC.PCATE_CD, PRC.CLASSCD,"
						+" SUBSTR(PRC.PCATE_CD, 1, DECODE(PRC.CLASSCD, 1, 0, 2, 1, 3, 2, 4, 6, 5, 10, 6, 14, 0)) \"PARENT_PCATECD\""
						+ " FROM DPR_PRODUCT_CATE PRC "
					+ " ) PRC"
				+ " START WITH PRC.PCATE_CD IN (" + itemQuery + ")"
				+ " CONNECT BY PRC.PCATE_CD = PRIOR PRC.PARENT_PCATECD"
				+ " ORDER BY \"classCode\", \"code\"";

		Object[] values = Record.extractValues( conditionMap, bindVariableKeys );
		List<Map<String, Object>> recordList = SQLManager.getRecordList( handler, query, values );

		return recordList;
	}

	public static QueryBuffer getInnerHierarchyQuery( Map<String, Object>conditionMap ) {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

		Object hierarchyCondition = conditionMap.get( "hierarchyCondition" );
		if( hierarchyCondition != null ) {
			String[] conditions = (String [])hierarchyCondition;

			StringBuffer sbuf = new StringBuffer(0);
			sbuf.append( "CASE " );
			for( int i = 0; i < conditions.length; i++ ) {
				sbuf.append( "WHEN " )
					.append( "ITM.PCATECD LIKE ? || '%' THEN ? " );
				querybuf.addBindVariables( querybuf.getBindVariableCount(), conditions[i], conditions[i] );
			}
			sbuf.append( " ELSE NULL END" );

			querybuf.appendData( sbuf.toString() + "\"PCATECD\"" );
		} else
			querybuf.appendData( "'x' \"PCATECD\"" );

		querybuf.appendData( "ITM.ITEMCD" );
		querybuf.appendData( "ITM.COUNTRYCD" );
		querybuf.appendData( "ITM.ORGANIZATIONCD" );
		querybuf.appendData( "ITM.DIST_CHANNELCD" );

		querybuf.appendTableWithAlias( "DPR_ITEM", "ITM" );
		querybuf.findCondition( "countryCode", "ITM.COUNTRYCD" );
		querybuf.findCondition( "organizationCode", "ITM.ORGANIZATIONCD" );
		querybuf.findCondition( "distributionChannelCode", "ITM.DIST_CHANNELCD" );
		querybuf.findCondition( "status", "ITM.STATUS" );

		return querybuf;
	}

	public static class NestedPHDJoinable extends com.irt.sql.NestedJoinable {
		String alias;
		Map<String, Object> hierarchyCondition;

		public NestedPHDJoinable( String alias, Map<String, Object> hierarchyCondition ) {
			super( alias );
			if( hierarchyCondition != null )
				this.hierarchyCondition = hierarchyCondition;
			else
				this.hierarchyCondition = new java.util.HashMap<String, Object> ();

			initNestedFields( new NestedJoinable.Field[] {
				  new NestedJoinable.Field( "ITEMCD", "ITM.ITEMCD" )
				, new NestedJoinable.Field( "ORGANIZATIONCD", "ITM.ORGANIZATIONCD" )
				, new NestedJoinable.Field( "COUNTRYCD", "ITM.COUNTRYCD" )
				, new NestedJoinable.Field( "DIST_CHANNELCD", "ITM.DIST_CHANNELCD" )
				, new NestedJoinable.Field( "PCATECD", "ITM.PCATECD" )
				, new NestedJoinable.Field( "PARENT_DISPLAY_PCATENAME", "NVL(MSTDOP.MASTER_NAME, MSTDBP.MASTER_NAME)" )
				, new NestedJoinable.Field( "DISPLAY_PCATENAME", "NVL(MSTDO.MASTER_NAME, MSTDB.MASTER_NAME)" )
			} );
		}

		@Override
		public QueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap ) {
			ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer( conditionMap );

			inner_querybuf.appendData( "ITM.ITEMCD" );
			inner_querybuf.appendData( "ITM.ORGANIZATIONCD" );
			inner_querybuf.appendData( "ITM.COUNTRYCD" );
			inner_querybuf.appendData( "ITM.DIST_CHANNELCD" );
			inner_querybuf.appendData( "ITM.PCATECD" );
			inner_querybuf.appendDataWithAlias( "NVL(MSTDOP.MASTER_NAME, MSTDBP.MASTER_NAME)", "PARENT_DISPLAY_PCATENAME" );
			inner_querybuf.appendDataWithAlias( "NVL(MSTDO.MASTER_NAME, MSTDB.MASTER_NAME)", "DISPLAY_PCATENAME" );

			inner_querybuf.appendTableWithAlias( "DPR_ITEM", "ITM" );

			inner_querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTD" );

			String masterOrganizationCode = (String) conditionMap.get( "masterOrganizationCode" );
			String displayLanguage = (String) conditionMap.get( "displayLanguage" );

			inner_querybuf.appendTableWithAlias( "DPR_MASTER_DESC_ORG", "MSTDOP", "MSTDOP.MASTER_CD(+) = MSTDBP.MASTER_CD"
					+ " AND MSTDOP.MASTER_TYPE(+) = MSTDBP.MASTER_TYPE"
					+ String.format(" AND MSTDOP.LANGCD(+) = '%s'", displayLanguage)
					+ String.format(" AND MSTDOP.ORGANIZATIONCD(+) = '%s'", masterOrganizationCode) );

			inner_querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTDBP", "MSTDBP.MASTER_CD(+) = SUBSTR(MSTD.MASTER_CD, 1, DECODE(LENGTH(MSTD.MASTER_CD), 1,0, 2,1, 6,2, 10,6, 14,10, 18,14, 18))"
					+ " AND MSTDBP.MASTER_TYPE(+) = MSTD.MASTER_TYPE"
					+ " AND MSTDBP.LANGCD(+) = MSTD.LANGCD"
					+ " AND MSTDBP.LANGCD = 'en'");

			inner_querybuf.appendTableWithAlias( "DPR_MASTER_DESC_ORG", "MSTDO", "MSTDO.MASTER_CD(+) = MSTDB.MASTER_CD"
					+ " AND MSTDO.MASTER_TYPE(+) = MSTDB.MASTER_TYPE"
					+ String.format(" AND MSTDO.LANGCD(+) = '%s'", displayLanguage)
					+ String.format(" AND MSTDO.ORGANIZATIONCD(+) = '%s'", masterOrganizationCode) );

			inner_querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTDB", "MSTDB.MASTER_CD(+) = MSTD.MASTER_CD"
					+ " AND MSTDB.MASTER_TYPE(+) = MSTD.MASTER_TYPE"
					+ " AND MSTDB.LANGCD(+) = MSTD.LANGCD" );

			inner_querybuf.appendCondition( "MSTD.MASTER_TYPE = 'PC'" );
			inner_querybuf.appendCondition( "MSTD.MASTER_CD(+) = SUBSTR(ITM.PCATECD, 1, DECODE( 4+1, 1,1, 2,2, 3,6, 4,10, 5,14, 6,18, 18))" );

			return inner_querybuf;
		}
	}


	public static class NestedJoinable extends com.irt.sql.NestedJoinable {
		String alias;
		Map<String, Object> hierarchyCondition;

		public NestedJoinable( String alias, Map<String, Object> hierarchyCondition ) {
			super( alias );
			if( hierarchyCondition != null )
				this.hierarchyCondition = hierarchyCondition;
			else
				this.hierarchyCondition = new java.util.HashMap<String, Object> ();

			initNestedFields( new NestedJoinable.Field[] {
				  new NestedJoinable.Field( "ITEMCD", "ITM.ITEMCD" )
				, new NestedJoinable.Field( "ORGANIZATIONCD", "ITM.ORGANIZATIONCD" )
				, new NestedJoinable.Field( "COUNTRYCD", "ITM.COUNTRYCD" )
				, new NestedJoinable.Field( "DIST_CHANNELCD", "ITM.DIST_CHANNELCD" )
				, new NestedJoinable.Field( "PCATECD", "ITM_C.MASTER_CD" )
				, new NestedJoinable.Field( "DISPLAY_PCATECD", "NVL(ITM_C.MASTER_CD, MSTD.MASTER_CD)" )
				, new NestedJoinable.Field( "DISPLAY_PCATENAME", "NVL(ITM_C.MASTER_NAME, MSTD.MASTER_NAME)" )
			} );
		}

		@Override
		public QueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap ) {
			ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer( conditionMap );

			inner_querybuf.appendData( "ITM.ITEMCD" );
			inner_querybuf.appendData( "ITM.ORGANIZATIONCD" );
			inner_querybuf.appendData( "ITM.COUNTRYCD" );
			inner_querybuf.appendData( "ITM.DIST_CHANNELCD" );
			inner_querybuf.appendDataWithAlias( "ITM_C.MASTER_CD", "PCATECD" );
			inner_querybuf.appendDataWithAlias( "NVL(ITM_C.MASTER_CD, MSTD.MASTER_CD)", "DISPLAY_PCATECD" );
			inner_querybuf.appendDataWithAlias( "NVL(ITM_C.MASTER_NAME, MSTD.MASTER_NAME)", "DISPLAY_PCATENAME" );

			inner_querybuf.appendTableWithAlias( "DPR_ITEM", "ITM" );
			inner_querybuf.appendTable( getItemQueryBufferUsingHierarchyCondition(conditionMap), "ITM_C" );
			inner_querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTD" );

			inner_querybuf.findCondition( "displayLanguage", "MSTD.LANGCD" );
			inner_querybuf.appendCondition( "MSTD.MASTER_TYPE = 'PC'" );

			String defaultLevel = null;
			int codeLength = 0;
			if( hierarchyCondition != null ) {
				defaultLevel = ( hierarchyCondition != null ? (String)hierarchyCondition.get( "defaultHierarchyLevel" ) : null );
				codeLength = ProductHierarchy.getHierarchyField().getLength( Integer.parseInt(defaultLevel) );
			}
			inner_querybuf.appendCondition( "MSTD.MASTER_CD = SUBSTR(ITM.PCATECD, 1, " + codeLength + ")" );
			inner_querybuf.appendCondition( "ITM_C.ITEMCD(+) = ITM.ITEMCD" );
			inner_querybuf.appendCondition( "ITM_C.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD" );
			inner_querybuf.appendCondition( "ITM_C.COUNTRYCD(+) = ITM.COUNTRYCD" );
			inner_querybuf.appendCondition( "ITM_C.DIST_CHANNELCD(+) = ITM.DIST_CHANNELCD" );

			return inner_querybuf;
		}

		private QueryBuffer getItemQueryBufferUsingHierarchyCondition( Map<String, ? extends Object> conditionMap ) {
			ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

			querybuf.appendData( "ITM.COUNTRYCD" );
			querybuf.appendData( "ITM.ORGANIZATIONCD" );
			querybuf.appendData( "ITM.DIST_CHANNELCD" );
			querybuf.appendData( "ITM.ITEMCD" );
			querybuf.appendData( "MSTD.MASTER_CD" );
			querybuf.appendData( "MSTD.MASTER_NAME" );

			querybuf.appendTableWithAlias( "DPR_ITEM", "ITM" );
			querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTD" );

			querybuf.findCondition( "displayLanguage", "MSTD.LANGCD" );
			querybuf.appendCondition( "MSTD.MASTER_TYPE = 'PC'" );
			querybuf.appendCondition( "ITM.PCATECD LIKE MSTD.MASTER_CD || '%'" );
			querybuf.appendCondition( "MSTD.MASTER_TYPE = 'PC'" );

			String[] codes = null;
			if( hierarchyCondition != null ) {
				codes = (String [])hierarchyCondition.get( "hierarchyCondition" );
			}
			querybuf.appendConditionByField( "MSTD.MASTER_CD", codes );
			querybuf.findCondition( "productHierarchyCondition", "MSTD.MASTER_CD" );

			return querybuf;
		}
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		try {
			return ( SQLManager.deleteRecord( handler, codeField, (String)codeField.extractValue(primaryMap) ) > 0 );
		} catch( FieldException fieldEx ) {
			return false;
		}
	}

	public int delete( String code ) throws DataException, SQLException {
		return SQLManager.deleteRecord( handler, codeField, code );
	}

	@Override
	public DataResult deleteAll( Collection<Map<String, Object>> records ) throws SQLException {
		return SQLManager.deleteRecordAll( handler, codeField, records );
	}

	public HierarchyCodeField getCodeField() {
		return codeField;
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		try {
			if( !SQLManager.lockTable( handler, codeField, (String)codeField.extractValue(recordMap) ) )
				throw handler.createDataException( DataException.ERR_NO_UPPERLEVELCODE, recordMap );

			return super.regist( recordMap );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}

}
