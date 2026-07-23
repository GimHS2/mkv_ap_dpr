package com.irt.dpr;

import com.irt.data.Condition;
import com.irt.rbm.TableAccessor;
import com.irt.rbm.TableDaoException;
import com.irt.rbm.TableDao;
import com.irt.sql.*;

import java.util.Map;

public class ItemEANMap extends com.irt.rbm.ManipulableManagerImpl implements TableAccessor {

	private final static Table table = Schema.findTable( Schema.DPR_ITEM_EANMAP );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ITEM_EANMAP );
	private final static QueryFactory factory_summary = new QueryFactory( new ItemEANMap.SummaryQuery() );

	public ItemEANMap( SQLHandler handler ) {
		this( handler, table, factory, factory_summary );
	}

	private TableDao tao;

	protected ItemEANMap( SQLHandler handler, Table table, QueryFactory factory, QueryFactory factory_summary ) {
		super( handler, table, factory, factory_summary );
		tao = new TableDao( table, handler.getMessageHandler() );
	}

	public static Map<String, Object> createPrimary( String itemCode, String upcStartDate ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "upcStartDate", upcStartDate );

		return primaryMap;
	}

	@Override
	public String[] getPrimaryFieldKeys() {
		return tao.getPrimaryFieldKeys();
	}

	@Override
	public Map<String, Object> extractPrimary( Map<String, Object> map, int idx ) throws TableDaoException {
		return tao.extractPrimary( map, idx );
	}

	@Override
	public Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> map ) throws TableDaoException {
		return tao.extractPrimaryKeyValues( map );
	}

	@Override
	public String[] getBindFieldKeys( int statementType ) {
		return tao.getBindFieldKeys( statementType );
	}

	@Override
	public String[] getReadonlyFieldKeys() {
		return tao.getReadonlyFieldKeys();
	}

	public static class SummaryQuery extends com.irt.sql.QueryableImpl {
		static String[] mapEffectFrom_groupKeys = new String[] { "organizationCode", "itemCode", "mapEanCode" };
		static boolean mapEffectFrom_isAndCondition = true;
		static String self = "IMPEAN";
		static NestedJoinable summary = new NestedJoinable( "IMPEAN" ) {
			{
				initNestedFields( new NestedJoinable.Field[] {
						new NestedJoinable.Field( "MAP_EFFECT_FROM", "MAX(MAP_EFFECT_FROM)" )
				} );
			}

			public QueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap ) {
				ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer( conditionMap );

				int groupKeyCount = 0;
				Object[] groupKeys = inner_querybuf.getConditionValues( Condition.GROUPING_CONDITIONKEY );
				boolean hasGroupCondition = com.irt.data.Condition.containsGroupKey( conditionMap, //
						mapEffectFrom_groupKeys, //
						mapEffectFrom_isAndCondition );
				if( hasGroupCondition ) {
					for( int i = 0; groupKeys != null && i < groupKeys.length; i++ ) {
						groupKeyCount++;
						if( "organizationCode".equals( groupKeys[i] ) )
							inner_querybuf.appendDataWithGroupBy( "IMPEAN.ORGANIZATIONCD" );
						else if( "itemCode".equals( groupKeys[i] ) )
							inner_querybuf.appendDataWithGroupBy( "IMPEAN.ITEMCD" );
						else if( "mapEanCode".equals( groupKeys[i] ) )
							inner_querybuf.appendDataWithGroupBy( "IMPEAN.MAP_EANCODE" );
						else
							groupKeyCount--;
					}
					if( groupKeyCount == 0 )
						inner_querybuf.appendDataWithAlias( "'x'", "DUMMYCODE" );

					factory.setConditionQuery( inner_querybuf );

				}

				return inner_querybuf;
			}
		};

		public SummaryQuery() {
			super( summary );

			QueryBufferValid querybufValid;

			append( new QueryableField[] {
					new QueryableFieldImpl( Schema.INTEGER, false, "mapEffectFrom", "IMPEAN.MAP_EFFECT_FROM",
							summary.getJoinable( "MAP_EFFECT_FROM" ) ),
			} );

			// accessDate, accessWeek, accessMonth, title
			appendCND( new QueryBufferValid.GroupKey( "organizationCode" ),
					new QueryableFieldImpl( Schema.CODE, false, "organizationCode", "IMPEAN.ORGANIZATIONCD" ) );
			appendCND( new QueryBufferValid.GroupKey( "itemCode" ), new QueryableFieldImpl( Schema.CODE, false, "itemCode", "IMPEAN.ITEMCD" ) );
			appendCND( new QueryBufferValid.GroupKey( "mapEanCode" ),
					new QueryableFieldImpl( Schema.CODE, false, "mapEanCode", "IMPEAN.MAP_EANCODE" ) );

			// partyId, partyName
			querybufValid = new QueryBufferValid.GroupKey( new String[] { "partyId", "userId" } );
			Joinable tbl_UPT = new JoinableImpl( "UPT", "USR_PARTY", "UPT.PARTY_ID(+) = USA.PARTYID" );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.STRING, false, "partyId", "USA.PARTYID" ) );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.DESC, false, "partyName", "UPT.PARTY_NAME", tbl_UPT ) );

			// userId, userName
			querybufValid = new QueryBufferValid.GroupKey( "userId" );
			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.PARTYID(+) = USA.PARTYID AND USR.USER_ID(+) = USA.USERID" );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.STRING, false, "userId", "USA.USERID" ) );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.DESC, false, "userName", "USR.USER_NAME", tbl_USR ) );
		}
	}

}
