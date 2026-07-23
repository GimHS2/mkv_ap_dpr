/*
 *	File Name:	ItemQuery.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	retailCategoryCode, sellerCategoryCode 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										재작성(TradeItemQuery 포함)
 *	stghr12		2006/12/01		2.1.0	ConditionalQueryable 변경사항 적용
 *	stghr12		2006/03/31		2.0.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.sql.*;
import com.irt.rbm.RBMSystem;
import java.util.Set;

/**
 *
 */
public class ItemQuery extends com.irt.sql.QueryableImpl {
	public final static char ITEM						= 'I';
	public final static char ITEMTP						= 'T';
	public final static char ITEMTP_BUYER				= 'B';
	public final static char ITEMTP_SELLER				= 'S';

	private final static int IDX_ITEM					= 0;
	private final static int IDX_ITEMINFO				= 1;
	private final static int IDX_ORDERING				= 2;
	private final static int IDX_HIERARCHY				= 3;
	private final static int IDX_MEASURE				= 4;
	private final static int IDX_PACKAGING				= 5;
	private final static int IDX_HANDLING				= 6;
	private final static int IDX_FASHION				= 7;
	private final static int IDX_ATTRIBUTE				= 8;
	private final static int IDX_DESCRIPTION			= 9;

	private final static Queryable[] queryables = new Queryable[] {
		Schema.findQueryable( Schema.ECS_ITEM )
		, Schema.findQueryable( Schema.ECS_ITEM_INFO )
		, Schema.findQueryable( Schema.ECS_ITEM_ORDERING )
		, Schema.findQueryable( Schema.ECS_ITEM_HIERARCHY )
		, Schema.findQueryable( Schema.ECS_ITEM_MEASURE )
		, Schema.findQueryable( Schema.ECS_ITEM_PACKAGING )
		, Schema.findQueryable( Schema.ECS_ITEM_HANDLING )
		, Schema.findQueryable( Schema.ECS_ITEM_FASHION )
		, Schema.findQueryable( Schema.ECS_ITEM_ATTRIBUTE )
		, Schema.findQueryable( Schema.ECS_ITEM_DESCRIPTION )
	};

	private final static Queryable[] queryableTPs = new Queryable[] {
		Schema.findQueryable( Schema.ECS_ITEMTP )
		, Schema.findQueryable( Schema.ECS_ITEMTP_INFO )
		, Schema.findQueryable( Schema.ECS_ITEMTP_ORDERING )
	};

	private final static QueryBufferValid querybufValidTP = new QueryBufferValid.Condition( QueryBufferValid.CONDITION_OR, "buyerGln", "sellerGln" );

	boolean isTP;
	char queryType;

	public ItemQuery( boolean isTP ) {
		super( isTP ? queryableTPs[IDX_ITEM] : queryables[IDX_ITEM] );

		this.isTP = isTP;
		this.queryType = ' ';
		if( isTP ) {
			append( queryableTPs[IDX_ITEMINFO],	"ITIF.BUYERGLN(+) = ITP.BUYERGLN AND ITIF.SELLERGLN(+) = ITP.SELLERGLN AND ITIF.GTIN(+) = ITP.GTIN" );
			append( queryableTPs[IDX_ORDERING],	"ITOR.BUYERGLN(+) = ITP.BUYERGLN AND ITOR.SELLERGLN(+) = ITP.SELLERGLN AND ITOR.GTIN(+) = ITP.GTIN" );
			append( queryables[IDX_HIERARCHY],	"IHR.GTIN(+) = ITP.HIERARCHY_REFGTIN AND IHR.GLN(+) = ITP.HIERARCHY_REFGLN" );
		} else {
			append( queryables[IDX_ITEMINFO],	"IIF.GTIN(+) = ITM.GTIN AND IIF.GLN(+) = ITM.INFORMATION_GLN" );
			append( queryables[IDX_ORDERING],	"IOR.GTIN(+) = ITM.ORDERING_REFGTIN AND IOR.GLN(+) = ITM.ORDERING_REFGLN" );
			append( queryables[IDX_HIERARCHY],	"IHR.GTIN(+) = ITM.HIERARCHY_REFGTIN AND IHR.GLN(+) = ITM.HIERARCHY_REFGLN" );
			append( queryables[IDX_MEASURE],	"IMS.GTIN(+) = ITM.MEASURE_REFGTIN AND IMS.GLN(+) = ITM.MEASURE_REFGLN" );
			append( queryables[IDX_PACKAGING],	"IPK.GTIN(+) = ITM.PACKAGING_REFGTIN AND IPK.GLN(+) = ITM.PACKAGING_REFGLN" );
			append( queryables[IDX_HANDLING],	"IHD.GTIN(+) = ITM.HANDLING_REFGTIN AND IHD.GLN(+) = ITM.HANDLING_REFGLN" );
			append( queryables[IDX_FASHION],	"IFS.GTIN(+) = ITM.BASE_GTIN AND IFS.GLN(+) = ITM.INFORMATION_GLN" );
			append( queryables[IDX_ATTRIBUTE],	"IAT.GTIN(+) = ITM.GTIN AND IAT.GLN(+) = ITM.GLN" );

			QueryBufferValid querybufValidTP = new QueryBufferValid.Condition( "languageCode" );
			append( new ConditionalQueryable(querybufValidTP, queryables[IDX_DESCRIPTION]) {
				public boolean appendTable( QueryBuffer querybuf ) {
					if( !super.appendTable(querybuf) ) return false;

					querybuf.append( "IDE.GTIN(+) = ITM.GTIN AND IDE.GLN(+) = ITM.INFORMATION_GLN" );
					((ConditionQueryBuffer)querybuf).findConditionSimple( "languageCode", "IDE.LANGCD(+)" );

					return true;
				}
			} );
			convertToUnconditionable( new String[] { "languageCode" } );
		}
	}

	public ItemQuery( char queryType ) {
		this( queryType != ITEM );

		String conditionQuery;

		this.queryType = queryType;
		switch( queryType ) {
		case ITEM:
			Joinable tbl_ITP = new ConditionalQueryable(querybufValidTP, new ItemQuery(true)) {
				public boolean appendTable( QueryBuffer querybuf ) {
					if( !super.appendTable(querybuf) ) return false;

					if( ((ConditionQueryBuffer)querybuf).findConditionSimple( "buyerGln", "ITP.BUYERGLN(+)" ) )
						querybuf.appendCondition( "ITP.SELLERGLN(+) = ITM.GLN AND ITP.SELLERGTIN(+) = ITM.GTIN" );
					else if( ((ConditionQueryBuffer)querybuf).findConditionSimple( "sellerGln", "ITP.SELLERGLN(+)" ) )
						querybuf.appendCondition( "ITP.BUYERGLN(+) = ITM.GLN AND ITP.BUYERGTIN(+) = ITM.GTIN" );

					return true;
				}
			};
			Joinable tbl_IOR = new JoinableImpl( "IOR", "ECS_ITEM_ORDERING"
					, "IOR.GTIN(+) = ITM.ORDERING_REFGTIN AND IOR.GLN(+) = ITM.ORDERING_REFGLN" );
			Joinable tbl_ITOR1 = new JoinableImpl( "ITOR", "ECS_ITEMTP_ORDERING"
					, "ITOR.BUYERGLN(+) = ITP.BUYERGLN AND ITOR.SELLERGLN(+) = ITP.SELLERGLN AND ITOR.GTIN(+) = ITP.GTIN" );
			Joinable tbl_ITOR2 = new JoinableImpl( "ITOR_P", "ECS_ITEMTP_ORDERING"
					, "ITOR_P.BUYERGLN(+) = ITP.PARENT_BUYERGLN AND ITOR_P.SELLERGLN(+) = ITP.PARENT_SELLERGLN AND ITOR_P.GTIN(+) = ITP.GTIN" );
			Joinable joinable = new JoinableWrapper( new Joinable[] { tbl_ITP, tbl_IOR, tbl_ITOR1, tbl_ITOR2 } );

			append( (Queryable)tbl_ITP );
			appendARR( querybufValidTP, new QueryableField[] {
				  new QueryableFieldImpl( Schema.CODE, "orderingInformationType", "NVL2(ITOR.GTIN, 'S', NVL2(ITOR_P.GTIN, 'P', 'O'))", joinable )
				, new QueryableFieldImpl( Schema.CODE, "orderUnit"
						, "NVL2(ITOR.GTIN, ITOR.ORDER_UNIT, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_UNIT, IOR.ORDER_UNIT))", joinable )
				, new QueryableFieldImpl( Schema.INTEGER, "orderingLeadTime"
						, "NVL2(ITOR.GTIN, ITOR.ORDER_LEADTIME, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_LEADTIME, IOR.ORDER_LEADTIME))", joinable )
				, new QueryableFieldImpl( Schema.INTEGER, "minimumOrderQty"
						, "NVL2(ITOR.GTIN, ITOR.ORDER_MINQTY, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_MINQTY, IOR.ORDER_MINQTY))", joinable )
				, new QueryableFieldImpl( Schema.INTEGER, "maximumOrderQty"
						, "NVL2(ITOR.GTIN, ITOR.ORDER_MAXQTY, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_MAXQTY, IOR.ORDER_MAXQTY))", joinable )
				, new QueryableFieldImpl( Schema.INTEGER, "orderQtyMultiple"
						, "NVL2(ITOR.GTIN, ITOR.ORDER_MULTIPLEQTY, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_MULTIPLEQTY, IOR.ORDER_MULTIPLEQTY))", joinable )
			} );
			convertToUnconditionable( new String[] { "buyerGln", "sellerGln" } );

			return;
		case ITEMTP:
			if( "BY".equals( RBMSystem.getSystemEnv("ECS", "ItemOption;TradeGtinInd", "SL") ) )
				conditionQuery = "ITM.GLN(+) = ITP.BUYERGLN AND ITM.GTIN(+) = ITP.GTIN";
			else
				conditionQuery = "ITM.GLN(+) = ITP.SELLERGLN AND ITM.GTIN(+) = ITP.GTIN";
			break;
		case ITEMTP_BUYER:
			conditionQuery = "ITM.GLN(+) = ITP.BUYERGLN AND ITM.GTIN(+) = ITP.BUYERGTIN";
			break;
		case ITEMTP_SELLER:
			conditionQuery = "ITM.GLN(+) = ITP.SELLERGLN AND ITM.GTIN(+) = ITP.SELLERGTIN";
			break;
		default:
			throw new IllegalArgumentException( "illegal queryType '"+ queryType +"'" );
		}

		Joinable tbl_ITM = new JoinableImpl( "ITM", "ECS_ITEM", conditionQuery );
		Joinable tbl_ITM_BY = new JoinableImpl( "ITM_BY", "ECS_ITEM", "ITM_BY.GLN(+) = ITP.BUYERGLN AND ITM_BY.GTIN(+) = ITP.BUYERGTIN" );
		Joinable tbl_ITM_SL = new JoinableImpl( "ITM_SL", "ECS_ITEM", "ITM_SL.GLN(+) = ITP.SELLERGLN AND ITM_SL.GTIN(+) = ITP.SELLERGTIN" );
		Joinable tbl_ITP_P = new JoinableImpl( "ITP_P", "ECS_ITEMTP"
				, "ITP_P.BUYERGLN(+) = ITP.PARENT_BUYERGLN AND ITP_P.SELLERGLN(+) = ITP.PARENT_SELLERGLN AND ITP_P.GTIN(+) = ITP.GTIN" );
		Joinable tbl_ITM_PBY = new JoinableImpl( "ITM_PBY", "ECS_ITEM"
				, "ITM_PBY.GLN(+) = ITP_P.BUYERGLN AND ITM_PBY.GTIN(+) = ITP_P.BUYERGTIN", tbl_ITP_P );
		Joinable tbl_IOR = new JoinableImpl( "IOR", "ECS_ITEM_ORDERING"
				, "IOR.GTIN(+) = ITM.ORDERING_REFGTIN AND IOR.GLN(+) = ITM.ORDERING_REFGLN", tbl_ITM );
		Joinable tbl_ITOR1 = new JoinableImpl( "ITOR", "ECS_ITEMTP_ORDERING"
				, "ITOR.BUYERGLN(+) = ITP.BUYERGLN AND ITOR.SELLERGLN(+) = ITP.SELLERGLN AND ITOR.GTIN(+) = ITP.GTIN" );
		Joinable tbl_ITOR2 = new JoinableImpl( "ITOR_P", "ECS_ITEMTP_ORDERING"
				, "ITOR_P.BUYERGLN(+) = ITP.PARENT_BUYERGLN AND ITOR_P.SELLERGLN(+) = ITP.PARENT_SELLERGLN AND ITOR_P.GTIN(+) = ITP.GTIN" );
		Joinable joinable = new JoinableWrapper( tbl_IOR, tbl_ITOR1, tbl_ITOR2 );

		append( new ItemQuery(false), conditionQuery );
		switch( queryType ) {
		case ITEMTP_BUYER:
			append( new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "buyerItemCode", "ITM.ITEMCODE", "ECS_ITEM_ITEMCODE_BUYER", tbl_ITM )
				, new QueryableFieldImpl( Schema.STRING, "retailItemCode", "NVL2(ITP.PARENT_BUYERGLN, ITM_PBY.ITEMCODE, ITM.ITEMCODE)"
						, "ECS_ITEM_ITEMCODE_RETAIL", tbl_ITM_PBY )
				, new QueryableFieldImpl( Schema.STRING, "sellerItemCode", "ITM_SL.ITEMCODE", tbl_ITM_SL )
				, new QueryableFieldImpl( Schema.STRING, "retailCategoryCode", "NVL2(ITP.PARENT_BUYERGLN, ITM_PBY.PARTY_CATECD, ITM.PARTY_CATECD)"
						, tbl_ITM_PBY )
				, new QueryableFieldImpl( Schema.STRING, "sellerCategoryCode", "ITM_SL.PARTY_CATECD", tbl_ITM_SL )
			} );
			break;
		case ITEMTP_SELLER:
			append( new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "buyerItemCode", "ITM_BY.ITEMCODE", "ECS_ITEM_ITEMCODE_BUYER", tbl_ITM_BY )
				, new QueryableFieldImpl( Schema.STRING, "retailItemCode", "NVL2(ITP.PARENT_BUYERGLN, ITM_PBY.ITEMCODE, ITM_BY.ITEMCODE)"
						, "ECS_ITEM_ITEMCODE_RETAIL", new JoinableWrapper(tbl_ITM_BY, tbl_ITM_PBY) )
				, new QueryableFieldImpl( Schema.STRING, "sellerItemCode", "ITM.ITEMCODE", tbl_ITM )
				, new QueryableFieldImpl( Schema.STRING, "retailCategoryCode", "NVL2(ITP.PARENT_BUYERGLN, ITM_PBY.PARTY_CATECD, ITM_BY.PARTY_CATECD)"
						, new JoinableWrapper(tbl_ITM_BY, tbl_ITM_PBY) )
				, new QueryableFieldImpl( Schema.STRING, "sellerCategoryCode", "ITM.PARTY_CATECD", tbl_ITM )
			} );
			break;
		default:
			append( new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "buyerItemCode", "ITM_BY.ITEMCODE", "ECS_ITEM_ITEMCODE_BUYER", tbl_ITM_BY )
				, new QueryableFieldImpl( Schema.STRING, "retailItemCode", "NVL2(ITP.PARENT_BUYERGLN, ITM_PBY.ITEMCODE, ITM_BY.ITEMCODE)"
						, "ECS_ITEM_ITEMCODE_RETAIL", new JoinableWrapper(tbl_ITM_BY, tbl_ITM_PBY) )
				, new QueryableFieldImpl( Schema.STRING, "sellerItemCode", "ITM_SL.ITEMCODE", tbl_ITM_SL )
				, new QueryableFieldImpl( Schema.STRING, "retailCategoryCode", "NVL2(ITP.PARENT_BUYERGLN, ITM_PBY.PARTY_CATECD, ITM_BY.PARTY_CATECD)"
						, new JoinableWrapper(tbl_ITM_BY, tbl_ITM_PBY) )
				, new QueryableFieldImpl( Schema.STRING, "sellerCategoryCode", "ITM_SL.PARTY_CATECD", tbl_ITM_SL )
			} );
		}

		append( new QueryableField[] {
			  new QueryableFieldImpl( Schema.CODE, "orderingInformationType", "NVL2(ITOR.GTIN, 'S', NVL2(ITOR_P.GTIN, 'P', 'O'))", joinable )
			, new QueryableFieldImpl( Schema.CODE, "orderUnit"
					, "NVL2(ITOR.GTIN, ITOR.ORDER_UNIT, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_UNIT, IOR.ORDER_UNIT))"
					, "ECS_ITEM_ORDERUNIT", joinable )
			, new QueryableFieldImpl( Schema.INTEGER, "orderingLeadTime"
					, "NVL2(ITOR.GTIN, ITOR.ORDER_LEADTIME, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_LEADTIME, IOR.ORDER_LEADTIME))"
					, "ECS_ITEM_ORDERINGLEADTIME", joinable )
			, new QueryableFieldImpl( Schema.INTEGER, "minimumOrderQty"
					, "NVL2(ITOR.GTIN, ITOR.ORDER_MINQTY, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_MINQTY, IOR.ORDER_MINQTY))"
					, "ECS_ITEM_MINIMUMORDERQTY", joinable )
			, new QueryableFieldImpl( Schema.INTEGER, "maximumOrderQty"
					, "NVL2(ITOR.GTIN, ITOR.ORDER_MAXQTY, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_MAXQTY, IOR.ORDER_MAXQTY))"
					, "ECS_ITEM_MAXIMUMORDERQTY", joinable )
			, new QueryableFieldImpl( Schema.INTEGER, "orderQtyMultiple"
					, "NVL2(ITOR.GTIN, ITOR.ORDER_MULTIPLEQTY, NVL2(ITOR_P.GTIN, ITOR_P.ORDER_MULTIPLEQTY, IOR.ORDER_MULTIPLEQTY))"
					, "ECS_ITEM_ORDERQTYMULTIPLE", joinable )
		}, true );
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		boolean hasCondition = super.appendCondition( querybuf );

		if( isTP ) {
			if( querybuf.findConditionKey( "primaryValue", new String[] { "ITP.BUYERGLN", "ITP.SELLERGLN", "ITP.GTIN" } ) > 0 )
				hasCondition = true;

			String nonTradeTP = (String)querybuf.getConditionValue( "nonTradeTP", Schema.STRING );
			if( nonTradeTP != null ) {
				String[] partnerGlns = nonTradeTP.split( ";", 2 );
				if( partnerGlns.length == 2 ) {
					querybuf.appendCondition( "ITP_CNTP.BUYERGLN(+) = ?", partnerGlns[0] );
					querybuf.appendCondition( "ITP_CNTP.SELLERGLN(+) = ?", partnerGlns[1] );
					querybuf.appendTableWithAlias( "ECS_ITEMTP", "ITP_CNTP", "ITP_CNTP.GTIN(+) = ITP.GTIN AND ITP_CNTP.GTIN IS NULL" );
					hasCondition = true;
				}
			}
		} else {
			if( querybuf.findConditionSimple( "nonTradeBuyerGln", "ITP_NTP.BUYERGLN(+)" ) ) {
				querybuf.appendTableWithAlias( "ECS_ITEMTP", "ITP_NTP"
						, "ITP_NTP.SELLERGLN(+) = ITM.GLN AND ITP_NTP.SELLERGTIN(+) = ITM.GTIN AND ITP_NTP.GTIN IS NULL" );
				hasCondition = true;
			}
		}

		return hasCondition;
	}

	public String[] getFieldKeys( int infoType ) {
		boolean useItem, useItemTP;

		switch( queryType ) {
		case ITEM:
		case ITEMTP:
		case ITEMTP_BUYER:
		case ITEMTP_SELLER:
			useItemTP = useItem = true;
			break;
		default:
			useItem = !isTP;
			useItemTP = isTP;
		}

		Set<String> fieldKeySet = new java.util.HashSet<String>();

		if( useItem ) fieldKeySet.addAll( queryables[IDX_ITEM].getQueryableFieldMap().keySet() );
		if( useItemTP ) {
			fieldKeySet.add( "buyerItemCode" );
			fieldKeySet.add( "retailItemCode" );
			fieldKeySet.add( "sellerItemCode" );
			fieldKeySet.add( "retailCategoryCode" );
			fieldKeySet.add( "sellerCategoryCode" );
			fieldKeySet.addAll( queryableTPs[IDX_ITEM].getQueryableFieldMap().keySet() );
		}
		if( (infoType & Item.INFO_ITEMINFO) > 0 ) {
			if( useItem ) fieldKeySet.addAll( queryables[IDX_ITEMINFO].getQueryableFieldMap().keySet() );
			if( useItemTP ) fieldKeySet.addAll( queryableTPs[IDX_ITEMINFO].getQueryableFieldMap().keySet() );
		}
		if( (infoType & Item.INFO_ORDERING) > 0 ) {
			if( useItem ) fieldKeySet.addAll( queryables[IDX_ORDERING].getQueryableFieldMap().keySet() );
			if( useItemTP ) {
				fieldKeySet.add( "orderingInformationType" );
				fieldKeySet.addAll( queryableTPs[IDX_ORDERING].getQueryableFieldMap().keySet() );
			}
		}
		if( (infoType & Item.INFO_HIERARCHY) > 0 )
			fieldKeySet.addAll( queryables[IDX_HIERARCHY].getQueryableFieldMap().keySet() );

		if( useItem ) {
			if( (infoType & Item.INFO_MEASURE) > 0 )
				fieldKeySet.addAll( queryables[IDX_MEASURE].getQueryableFieldMap().keySet() );
			if( (infoType & Item.INFO_PACKAGING) > 0 )
				fieldKeySet.addAll( queryables[IDX_PACKAGING].getQueryableFieldMap().keySet() );
			if( (infoType & Item.INFO_HANDLING) > 0 )
				fieldKeySet.addAll( queryables[IDX_HANDLING].getQueryableFieldMap().keySet() );
			if( (infoType & Item.INFO_FASHION) > 0 )
				fieldKeySet.addAll( queryables[IDX_FASHION].getQueryableFieldMap().keySet() );
			if( (infoType & Item.INFO_ATTRIBUTE) > 0 )
				fieldKeySet.addAll( queryables[IDX_ATTRIBUTE].getQueryableFieldMap().keySet() );
			if( (infoType & Item.INFO_DESCRIPTION) > 0 )
				fieldKeySet.addAll( queryables[IDX_DESCRIPTION].getQueryableFieldMap().keySet() );
		}
		String[] fieldKeys = new String[ fieldKeySet.size() ];
		fieldKeySet.toArray( fieldKeys );

		return fieldKeys;
	}
}
