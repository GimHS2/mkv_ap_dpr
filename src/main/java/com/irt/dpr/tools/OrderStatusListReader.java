/*
 *	File Name:	OrderStatusListReader.java
 *	Version:	2.2.8(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/12/31		2.2.8	readStatusList() : billingNumber를 0 제거 하지 않도록 수정
 *	dudwls3720	2025/10/31		2.2.7	readStatusList() : orderNumber를 Number -> String 형으로 변경 및 orderNumber, billingNumber키의 값에 앞에 0을 제거하고 값 넣도록 수정
 *	GimHS		2025/09/30		2.2.6	BTPi interface(REST) 적용
 *	jbaek		2019/03/30		2.2.5	WmDataConverter.getDataMap() 오류수정.
 *	jbaek		2014/11/30		2.2.4	line별( order header)로 exception 생성하여 map에 추가.
 *	jbaek		2014/10/30		2.2.3	Goods Issue Date, Credit Release Date 추가
 *	jbaek		2013/01/30		2.2.2	로그  정리
 *	GimHS		2011/07/31		2.2.1	read(): CanonicalXMLException 발생시 Error log에 OrderKey 표시
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.dpr.util.Loggers;
import java.util.Map;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 */
public class OrderStatusListReader extends CanonicalXMLReader {
	String orderKey, orderNumber, uniqId, portalUser;
	List<Map<String, Object>> inputInfo;

	String organizationCode;

	List<Map<String, Object>> headerList;

	public OrderStatusListReader( Logger logger ) {
		super( null, logger );
		this.orderKey = null;
		this.organizationCode = null;
		this.portalUser = null;
		this.uniqId = null;
		this.inputInfo = new java.util.ArrayList<Map<String, Object>>();

		headerList = new java.util.ArrayList<Map<String, Object>>();
	}

	public OrderStatusListReader( Logger logger, String organizationCode, String portalUser, List<Map<String, Object>> inputInfo ) {
		super( null, logger );
		this.orderKey = null;
		this.organizationCode = organizationCode;
		this.portalUser = portalUser;
		this.uniqId = null;
		this.inputInfo = inputInfo;

		headerList = new java.util.ArrayList<Map<String, Object>>();
	}

	public OrderStatusListReader( Logger logger, String orderKey ) {
		super( null, logger );
		this.orderKey = orderKey;
		this.organizationCode = null;
		this.portalUser = portalUser;
		this.uniqId = null;
		this.inputInfo = new java.util.ArrayList<Map<String, Object>>();

		headerList = new java.util.ArrayList<Map<String, Object>>();
	}

	public OrderStatusListReader( Logger logger, String orderKey, String organizationCode ) {
		super( null, logger );
		this.orderKey = orderKey;
		this.organizationCode = organizationCode;
		this.inputInfo = new java.util.ArrayList<Map<String, Object>>();

		headerList = new java.util.ArrayList<Map<String, Object>>();
	}

	@Override
	public java.net.URL getSchemaURL() {
		return null;
	}

	public String getOrderKey() {
		return this.orderKey;
	}

	public Object[] getStoredMap() {
		return new Object[] { headerList };
	}

	@Override
	public int read() throws CanonicalXMLException {
		return -1;
	}

	public int read( String outputXML ) throws CanonicalXMLException {
		if( outputXML == null )
			throw new CanonicalXMLException( "No read Data" );

		try {
			logger.info( Loggers.STR_BUSINESS + this.uniqId + ": " + "OrderStatusListReader start." );

			writeStatusListResult( outputXML );

			List<Map<String, Object>> values = OrderXMLUtility.getTagValueList( document, "OrderStatusListResponse", "STATUS_RECORDS2" );
			if( values == null || values.size() == 0 ) {
				// throw new CanonicalXMLException( "No read Data" );
				logger.warn( "### No read Data ###" );
				return 0;
			}

			int recordCount = readStatusList( values );

			logger.info( Loggers.STR_BUSINESS + this.uniqId + ": " + "OrderStatusListReader end." );

			return recordCount;
		} catch( CanonicalXMLException xmlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = xmlEx.getMessage();
			logger.error( Loggers.STR_BUSINESS + this.uniqId + ": " + "OrderStatusListReader error ", xmlEx );
			throw xmlEx;
		}
	}


	public int readStatusList( List<Map<String, Object>> values ) throws CanonicalXMLException {
		logger.info( Loggers.STR_BUSINESS + this.uniqId + ": " + "<header/> and <header/> reading start." );

		for( int n = 0; n < values.size(); n++ ) {
			Map<String, Object> valueMap = values.get(n);
			Map<String, Object> map = new java.util.HashMap<String, Object>();

			String orderNumber = DataUtility.getStringValueTrim(valueMap, "ORDER_ID");
			map.put( "orderNumber", orderNumber );
			map.put( "orderKey", getInputInfo( orderNumber, "orderKey") );
			String _organizationCode = DataUtility.getStringValue( valueMap, "SALES_ORG_ID" );
			if( _organizationCode == null || _organizationCode.length() == 0 )
				_organizationCode = organizationCode;
			map.put( "organizationCode", _organizationCode );
			configMoneyFractionCorrection(_organizationCode);

			map.put( "purchaseOrderNumber", DataUtility.getStringValue(valueMap, "PO_NUM") );
			map.put( "soldPartyCode", DataUtility.getLongValue(valueMap, "SOLD_TO") );
			map.put( "shipPartyCode", DataUtility.getLongValue(valueMap, "SHIP_TO") );
			map.put( "orderDate", DataUtility.getStringValue(valueMap, "ORDER_DATE") );
			map.put( "orderType", DataUtility.getStringValue(valueMap, "ORDER_TYPE") );
			map.put( "orderValue", DataUtility.getStringMoneyValue(valueMap, "ORDER_VALUE", isFractionCorrection) );
			map.put( "inDate", DataUtility.getStringValue(valueMap, "RDD") );
			map.put( "inDateConfirm", DataUtility.getStringValue(valueMap, "RDD") );
			map.put( "orderVolume", DataUtility.getDoubleValue(valueMap, "VOLUME") );
			map.put( "orderVolumeUnit", DataUtility.getStringValue(valueMap, "VOLUME_UNIT") );
			map.put( "orderWeight", DataUtility.getDoubleValue(valueMap, "WEIGHT") );
			map.put( "orderWeightUnit", DataUtility.getStringValue(valueMap, "WEIGHT_UNIT") );
			map.put( "deliveryNumber", DataUtility.getStringValue(valueMap, "DELIVERY_ID") );
			map.put( "deliveryDate", DataUtility.getStringValue(valueMap, "DELIVERY_DATE") );
			map.put( "goodsIssueDate", convertDate(DATETYPE_LEN10, DataUtility.getStringValue(valueMap, "GOODS_ISSUE_DATE")) );
			map.put( "creditReleaseDate", convertDate(DATETYPE_LEN10, DataUtility.getStringValue(valueMap, "CREDIT_RELEASE_DATE")) );
			map.put( "billingNumber", DataUtility.getStringValue(valueMap, "BILLING_ID") );
			map.put( "billingDate", DataUtility.getStringValue(valueMap, "BILLING_DATE") );
			map.put( "creaditMemosNumber", DataUtility.getStringValue(valueMap, "CREDIT_MEMO_ID") );
			map.put( "creaditMemosDate", DataUtility.getStringValue(valueMap, "CREDIT_MEMO_DATE") );
			map.put( "creaditMemosValue", DataUtility.getStringMoneyValue(valueMap, "CREDIT_MEMO_VALUE", isFractionCorrection) );
			map.put( "returnCreaditMemosNumber", DataUtility.getStringValue(valueMap, "RETURN_CREDIT_MEMO_ID") );
			map.put( "returnCreaditMemosDate", DataUtility.getStringValue(valueMap, "RETURN_CREDIT_MEMO_DATE") );
			map.put( "returnCreaditMemosValue", DataUtility.getStringMoneyValue(valueMap, "RETURN_CREDIT_MEMO_VALUE", isFractionCorrection) );
			map.put( "debitMemosNumber", DataUtility.getStringValue(valueMap, "DEBIT_MEMO_ID") );
			map.put( "debitMemosDate", DataUtility.getStringValue(valueMap, "DEBIT_MEMO_DATE") );
			map.put( "debitMemosValue", DataUtility.getStringMoneyValue(valueMap, "DEBIT_MEMO_VALUE", isFractionCorrection) );

			// The status process for SAP order deleted
//	String orderStatus = com.irt.dpr.Order.getOrderStatus( WMDataUtility.getStringValue(values[n], "STATUS") );
			String orderStatus = DataUtility.getStringValue(valueMap, "STATUS");
			try{
				if( orderStatus != null ) orderStatus = com.irt.dpr.Order.getOrderStatus( orderStatus );
			} catch ( CanonicalXMLException cxmlEx ) {

				if( CanonicalXMLException.INVALID_ORDERSTATUS.equals(cxmlEx.getErrorKey()) )
					map.put( "lineErrorMessage", cxmlEx.getErrorKey() );
				else
					throw cxmlEx;
			}

			String status = ( com.irt.dpr.Order.ORDERSTATUS_DELETED.equals(orderStatus) ? orderStatus : com.irt.dpr.Order.STATUS_CREATED );
			map.put( "status", status );
			map.put( "orderStatus", orderStatus );
			map.put( "updateUserId", this.uniqId );

			logger.info( Loggers.STR_BUSINESS + map.get("orderKey") + ": " + "header("+ n +"): " + valueMap );
			readMemos( valueMap, map );
			readBilling( valueMap, map );

			headerList.add( map );
		}

		logger.info( Loggers.STR_BUSINESS + this.uniqId + ": " + "<header/> read("+ values.size() +")." );

		return values.size();
	}

	@SuppressWarnings("unchecked")
	private int readMemos( Map<String, Object> valueMap, Map<String, Object> statusMap ) throws CanonicalXMLException {
		if( statusMap == null || statusMap.size() == 0 ) return 0;

		logger.info( Loggers.STR_BUSINESS + statusMap.get("orderKey") + ": " + "<MEMOS/> reading start." );

		List<Map<String, Object>> memos = new java.util.ArrayList<Map<String, Object>>();
		Object obj = valueMap.get( "MEMOS" );
		if( obj instanceof Map )
			memos.add( (Map<String, Object>)obj );
		else if( obj instanceof List )
			memos = (List<Map<String, Object>>)obj;

		List<Map<String,Object>> memosList = new java.util.ArrayList<Map<String,Object>>();
		for( int n = 0; n < memos.size(); n++ ) {
			Map<String, Object> memoMap = memos.get(n);
			Map<String, Object> map = new java.util.HashMap<String, Object>();

			map.put( "memoNumber", DataUtility.getStringValue(memoMap, "VBELN") );
			map.put( "memoDate", DataUtility.getStringValue(memoMap, "ERDAT") );
			map.put( "memoValue", DataUtility.getStringMoneyValue(memoMap, "RFWRT", isFractionCorrection) );
			map.put( "extravalue1", DataUtility.getStringValue(memoMap, "VBTYP_N") );
			map.put( "orderNumber", statusMap.get("orderNumber") );

			memosList.add( map );
			logger.info( Loggers.STR_BUSINESS + map.get("memoNumber") + ": " + memoMap );
		}
		logger.info( Loggers.STR_BUSINESS + statusMap.get("orderKey") + ": " + "<MEMOS/> read.("+ memosList.size() +" lines)" );

		statusMap.put( "memosDocument", memosList );

		return memosList.size();
	}

	@SuppressWarnings("unchecked")
	private int readBilling( Map<String, Object> valueMap, Map<String, Object> statusMap ) throws CanonicalXMLException {
		if( statusMap == null || statusMap.size() == 0 ) return 0;

		logger.info( Loggers.STR_BUSINESS + statusMap.get("orderKey") + ": " + "<BILLING_DOC/> reading start." );

		List<Map<String, Object>> billing = new java.util.ArrayList<Map<String, Object>>();
		Object obj = valueMap.get( "BILLING_DOC" );
		if( obj instanceof Map )
			billing.add( (Map<String, Object>)obj );
		else if( obj instanceof List )
			billing = (List<Map<String, Object>>)obj;

		List<Map<String,Object>> billingList = new java.util.ArrayList<Map<String,Object>>();
		for( int n = 0; n < billing.size(); n++ ) {
			Map<String, Object> billingMap = billing.get(n);
			Map<String, Object> map = new java.util.HashMap<String, Object>();

			map.put( "billingNumber", DataUtility.getStringValue(billingMap, "BILLING_ID") );
			map.put( "billingDate", DataUtility.getStringValue(billingMap, "BILLING_DATE") );
			map.put( "vatNumber", DataUtility.getStringValue(billingMap, "VAT_NO") );
			map.put( "invoiceValue", DataUtility.getDoubleMoneyValue(billingMap, "INVOICE_VALUE", isFractionCorrection) );
			map.put( "orderNumber", statusMap.get("orderNumber") );

			billingList.add( map );
			logger.info( Loggers.STR_BUSINESS + map.get("billingNumber") + ": " + billingMap );
		}
		logger.info( Loggers.STR_BUSINESS + statusMap.get("orderKey") + ": " + "<BILLING_DOC/> read.("+ billingList.size() +" lines)" );

		statusMap.put( "billingDocument", billingList );

		return billingList.size();
	}

	private String getInputInfo( String orderNumber, String key ) {
		if( inputInfo == null || orderNumber == null ) return null;

		for( Map<String, Object> map : inputInfo ) {
			@SuppressWarnings("unchecked")
			List<String []> numbers = (List<String []>)map.get( "orderNumbers" );
			for( @SuppressWarnings("rawtypes")
			java.util.Iterator iterator = numbers.iterator(); iterator.hasNext(); ) {
				String[] values = (String [])iterator.next();

				if( orderNumber != null && orderNumber.equals(values[0]) )
					if( "orderKey".equals(key) )
						return values[1];
					else
						return (String)map.get( key );
			}
		}

		return null;
	}

	public void setUniqId( String uniqId ) {
		this.uniqId = uniqId;
	}

	private void writeStatusListResult( String outputXML ) {
		OrderLogging orderLogger = new OrderLogging( logger, null );

		java.io.File file = orderLogger.getFile( OrderCanonicalProcess.ORDER_IF_STATUSLIST, "RES_"+ portalUser );
		logger.debug( "XML file name : "+ file.getAbsolutePath() +"("+ file.getName() +")" );
		this.document = orderLogger.makeOrderTraceFile( file, outputXML );

	}
}
