/*
 *	File Name:	Party.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.2	getDistributionChannelCode() 추가
 *	hankalam	2017/02/28		2.2.1	Party별 allowUOM 기능 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.sql.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class Party extends com.irt.rbm.ManipulableManagerImpl {
	public final static String DEFAULT_UOM = "CSE";
	public final static String PARTYTYPE_DISTRIBUTOR		= "DIST";
	public final static String PARTYTYPE_CUSTOMER			= "CUST";

	public final static String PARTYSTATUS_ACTIVE			= "00";

	private final static Table table = Schema.findTable( Schema.DPR_PARTY_SALES );
	private final static QueryFactory factory = new QueryFactory( new QueryableImpl(Schema.findQueryFactory(Schema.DPR_PARTY_SALES)) {
		{
			QueryBufferValid valid_link = new QueryBufferValid.Condition( "baseLinkType" );
			appendCND( valid_link, new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "baseLinkType", "PLNK_BS.LINKTYPE" )
				, new QueryableFieldImpl( Schema.STRING, "baseLinkPartyCode", "PLNK_BS.LINK_PARTYCD" )
			} );
		}

		@Override
		public boolean appendTable( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condQueryBuffer = (ConditionQueryBuffer)querybuf;
				if( condQueryBuffer.hasConditionValue("baseLinkType") ) {
					QueryBuffer linkQuery = PartyLink.getDistinctPartyLink( condQueryBuffer.getConditionMap() );

					querybuf.appendTable( linkQuery, "PLNK_BS", "PLNK_BS.LINK_PARTYCD = PTYS.PARTYCD" );
				}
			}

			return super.appendTable( querybuf );
		}
	} );

	public Party( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public DataLoader.Loader createDataLoader(String[] fieldKeys, Map<String, ? extends Object> defaultMap,
			String[] updateFieldKeys, final String[] uomValues, int statementType) throws SQLException, UnsupportedOperationException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType ) {
			@Override
			public Map<String, Object> loadLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				String allowUOM = new String();
				for( String uomKey : uomValues ) {
					uomKey = uomKey.toLowerCase();
					String uom = (String)recordMap.get( uomKey + "_uom" );

					if( uom != null && !("Y".equals(uom) || "N".equals(uom)) )
						throw handler.createDataException( DataException.ERR_ERROR, handler.getMessageHandler().getMessage("ERR_UPLOADUOM_INVALID_UOM") );
					if( uom != null && "Y".equals(uom) ) {
						allowUOM += uomKey.toUpperCase() + ",";
					}
					recordMap.remove( uomKey + "_uom" );
				}

				if( allowUOM != null && allowUOM.length() > 0 ) {
					allowUOM = allowUOM.substring( 0, allowUOM.length() - 1 );
					recordMap.put( "allowUOM", allowUOM );
				}

				if( allowUOM.indexOf(",") > 0 )
					throw handler.createDataException( DataException.ERR_ERROR, handler.getMessageHandler().getMessage("ERR_UPLOADUOM_CANNOT_UPDATE_UOM") );

				return super.loadLine( handler, recordMap );
			}
		};
	}

	public static Map<String, Object> createPrimary( String partyCode, String organizationCode, String distributionChannelCode, String divisionCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "partyCode", partyCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "divisionCode", divisionCode );

		return primaryMap;
	}

	public String getDistributionChannelCode( Object partyCode, Object organizationCode, Object divisionCode ) throws SQLException {
		String distributionChannelCode = null;
		Map<String, Object> conditionMap = Record.createMap( "partyCode", partyCode );
		conditionMap.put( "organizationCode", organizationCode );
		conditionMap.put( "divisionCode", divisionCode );
		List<Map<String, Object>> list = getRecords( conditionMap, new String[] { "distributionChannelCode" } );
		if( list != null && list.size() == 1 ) {
			Map<String, Object> partyMap = list.get( 0 );
			distributionChannelCode = (String) partyMap.get( "distributionChannelCode" );
		}

		return distributionChannelCode;
	}

	public String getPartyName( Map<String, Object> primaryMap ) throws SQLException {
		return (String)getFieldValue( primaryMap, "partyName" );
	}

	public void write( List<Map<String, Object>> recordList, SQLHandler handler, DataWriter out, ColumnList columnList, int writingOption, int maxRows ) throws IOException, SQLException {
		if( (writingOption & QueryableManager.OPT_WRITING_TITLE) > 0 )
			SQLManager.writeTitle(handler, out, columnList, writingOption);
		if( out instanceof com.irt.util.SSDataWriter )
			((com.irt.util.SSDataWriter) out).setColumnList(columnList);

		Column[] columns = columnList.getColumns();
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		for( int rownum = 1; rownum <= recordList.size(); rownum++ ) {
			if( maxRows > 0 && rownum > maxRows )
				break;
			Map<String, Object> recordMap = recordList.get(rownum - 1);
			recordMap.put("rowNumber", rownum);
			if( (writingOption & OPT_WRITING_ROWNUMBER) > 0 )
				out.print(rownum);
			if( (writingOption & OPT_WRITING_EXECUTETYPE) > 0 )
				out.print("U");
			for( int c = 0; c < columns.length; c++ )
				out.print(columns[c].getColumnValue(recordMap, msghandler),
						columns[c].getColumnSize());
			out.println();
		}
	}
}
