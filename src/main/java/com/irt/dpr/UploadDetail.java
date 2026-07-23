/*
 *	File Name:	UploadDetail.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	guksm		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class UploadDetail extends com.irt.rbm.QueryableManagerImpl {
	public final static int IDX_CUSTOMER_ID_MAPPING			= 0;
	public final static int IDX_CUSTOMER_MASTER				= 1;
	public final static int IDX_INVENTORY					= 2;
	public final static int IDX_ORDERDETAIL					= 3;
	public final static int IDX_SELLOUT						= 4;
	public final static int IDX_SKU_MAPPING					= 5;
	public final static int IDX_SELLING_SKU_LIST			= 6;

	private final static QueryFactory[] factories = new QueryFactory[] {
		  Schema.findQueryFactory( Schema.DPR_UPLOAD_CIM ), Schema.findQueryFactory( Schema.DPR_UPLOAD_CMT )
		, Schema.findQueryFactory( Schema.DPR_UPLOAD_INV ), Schema.findQueryFactory( Schema.DPR_UPLOAD_ORD )
		, Schema.findQueryFactory( Schema.DPR_UPLOAD_SEO ), Schema.findQueryFactory( Schema.DPR_UPLOAD_SKM )
		, Schema.findQueryFactory( Schema.DPR_UPLOAD_SSL )
	};

	public UploadDetail( SQLHandler handler, int type ) {
		super( handler, factories[type] );
	}
}
