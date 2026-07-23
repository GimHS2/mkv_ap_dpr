/*
 *	File Name:	OrderBillingStatusReader.java
 *	Version:	2.2.6(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/12/31		2.2.6	readData() : billingNumber에 값 넣을 때 0 제거하지 않도록 수정
 *	dudwls3720	2025/10/31		2.2.5	readData() : billingNumber, orderNumber에 값 넣을 때 앞에 0 제거하고 넣도록 수정
 *	GimHS		2025/09/30		2.2.4	BTPi interface(REST) 적용
 *	jbaek		2019/03/30		2.2.3	WmDataConverter.getDataMap() 추가
 *	GimHS		2011/04/29		2.2.2	readData(): vatNumber 추가
 *	lsinji		2011/01/31		2.2.1	VAT 계산 로직을 환경 변수로 처리
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.dpr.OrderProcessException;
import com.irt.dpr.util.Loggers;
import com.irt.sql.SQLHandler;
import java.util.Map;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 */
public class OrderBillingStatusReader extends CanonicalXMLReader {
	String orderKey, billingNumber, orderNumber;
	Map<String, Object> dataMap;

	public OrderBillingStatusReader( SQLHandler handler, Logger logger ) {
		super( handler, logger );
		this.orderKey = null;
		this.orderNumber = null;
		this.billingNumber = null;

		dataMap = new java.util.HashMap<String, Object>();
	}

	public OrderBillingStatusReader( SQLHandler hanlder, Logger logger, String orderKey, String orderNumber, String billingNumber ) {
		super( hanlder, logger );
		this.orderKey = orderKey;
		this.orderNumber = orderNumber;
		this.billingNumber = billingNumber;

		dataMap = new java.util.HashMap<String, Object>();
	}

	@Override
	public java.net.URL getSchemaURL() {
		return null;
	}

	public String getPortalUser( String uniqId ) {
		if( uniqId != null && uniqId.indexOf("@") > 0 ) return uniqId.substring( 0, uniqId.indexOf("@") );
		else return uniqId;
	}

	public void process() throws OrderProcessException {
		com.irt.dpr.Billing db = new com.irt.dpr.Billing( handler );
		try {
			@SuppressWarnings("unchecked")
			boolean ret = db.updateHeader( (Map<String, Object>)dataMap.get("header") );
			if( ret )
				logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "OrderBillingStatusReader.process(); header update successfully." );
			else
				logger.error( Loggers.STR_BUSINESS + orderKey + ": " + "OrderBillingStatusReader.process(); header update failed." );

			@SuppressWarnings("unchecked")
			com.irt.data.DataResult result = db.updateDetail( (List<Map<String, Object>>)dataMap.get("lines") );
			if( result.getSuccessCount() == 0 ) {
				logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "OrderBillingStatusReader.process() : failed. (ALL: " + result.getCount() + ", ERR: " + result.getErrorCount() + ")" );
				logger.error( Loggers.STR_BUSINESS + orderKey + ": " + result.getException() );
			} else {
				logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "OrderBillingStatusReader.process() : Success." );
			}

		} catch( com.irt.data.DataException dataEx ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, dataEx );
		} catch( java.sql.SQLException sqlEx ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
		}
	}

	@Override
	public int read() throws CanonicalXMLException {
		return -1;
	}

	public int read( String outputXML ) throws CanonicalXMLException {
		Loggers.business.debug( "{}: {}", orderKey, "start." );
		writeBillingResult( outputXML );

		int ret = 0;
		this.dataMap = readData();
		if( this.dataMap != null && this.dataMap.size() > 0 ) {
			ret = 1;
			Loggers.business.debug( "{}: {}", orderKey, "success." );
		}

		Loggers.business.debug( "{}: {}", orderKey, "end." );
		return ret;
	}

	public Map<String, Object> readData() {
		Map<String, Object> output = new java.util.HashMap<String, Object>();

		String vatNumber = OrderXMLUtility.getTagValue( this.document, "rfc:Z_CW_RT_BILLING_STATUS.Response", "VATINVNO" );
		Map<String, Object> headerMap = OrderXMLUtility.getTagValueMap( this.document, "BILLING_HEADER", "item" );
		String organizationCode = null;
		if( headerMap != null ) {
			String purchaseOrderNumber = null;
			Map<String, Object> map = new java.util.HashMap<String, Object>();

			map.put( "organizationCode", organizationCode = DataUtility.getStringValue(headerMap, "VKORG") );
			configMoneyFractionCorrection(organizationCode);

			map.put( "billingNumber", DataUtility.getStringValue(headerMap, "VBELN") );
			map.put( "vatNumber", vatNumber );
			map.put( "soldPartyCode", DataUtility.getLongValue(headerMap, "KUNAG") );
			map.put( "billingDate", DataUtility.getStringValue(headerMap, "ERDAT") );
			map.put( "invoiceValue", DataUtility.getDoubleMoneyValue(headerMap, "NETWR", isFractionCorrection) );
			map.put( "purchaseOrderNumber", purchaseOrderNumber = DataUtility.getStringValue(headerMap, "BSTNK_VF") );

			try {
				map.put( "orderNumber", new com.irt.dpr.Order(handler).getOrderNumber(purchaseOrderNumber) );
			} catch( java.sql.SQLException sqlEx ) {
				logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "Can't not find orderNumber(" + purchaseOrderNumber +")", sqlEx );
			}

			if( Loggers.business.isTraceEnabled() ) {
				StringBuffer lbuf = new StringBuffer();
				for( String key : map.keySet() )
					lbuf.append( key + ": " + map.get(key) + ", " );
				Loggers.business.trace( "{}: {}", new Object[]{orderKey, lbuf.toString()} );
				lbuf.delete( 0, lbuf.capacity() );
			}

			output.put( "header", map );
		}

		List<Map<String, Object>> items = OrderXMLUtility.getTagValueList( this.document, "BILLING_ITEM", "item" );

		if( items == null || items.size() == 0 )
			return output;

		output.put( OrderCanonicalProcess.WM_PARAMS, OrderCanonicalProcess.WM_PARAMS_BILLING );

		java.util.List<Map<String, Object>> lineList = new java.util.ArrayList<Map<String, Object>> ();
		for( int i = 0; i < items.size() ; i++ ) {
			Map<String, Object> itemMap = items.get(i);
			Map<String, Object> map = new java.util.HashMap<String, Object>();
			map.put( "billingNumber", DataUtility.getStringValue(itemMap, "VBELN") );
			map.put( "vatNumber", vatNumber );
			map.put( "orderNumber", DataUtility.getStringValueTrim(itemMap, "AUBEL") );
			map.put( "lineNumber", DataUtility.getLongValue(itemMap, "POSNR") );
			map.put( "itemCode", DataUtility.getLongValue(itemMap, "MATNR") );
			map.put( "billingQty", DataUtility.getDoubleMetricValue(itemMap, "FKIMG") );
			map.put( "uom", DataUtility.getStringValue(itemMap, "VRKME") );

			/* Thailand, China used difference field. */

			double billingNetAmount, billingTax, billingDamagedDiscount, billingTotal;
			billingNetAmount =  billingTax =  billingDamagedDiscount = billingTotal = 0.0;

			if( com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "VATLogic;"+ organizationCode, false) ) {
				double temp = DataUtility.getDoubleMoneyValue( itemMap, "KZWI5", isFractionCorrection );

				billingNetAmount = DataUtility.getDoubleMoneyValue( itemMap, "KZWI2", isFractionCorrection );
				billingTotal = DataUtility.getDoubleMoneyValue( itemMap, "KZWI4", isFractionCorrection );
				billingTax = billingTotal - temp;
				billingDamagedDiscount = temp - billingNetAmount;
			} else {
				billingNetAmount = DataUtility.getDoubleMoneyValue( itemMap, "NETWR", isFractionCorrection );
				billingTax = DataUtility.getDoubleMoneyValue( itemMap, "MWSBP", isFractionCorrection );
				billingTotal = billingNetAmount + billingTax;
			}

			map.put( "volume", DataUtility.getDoubleMetricValue( itemMap, "VOLUM" ) );
			map.put( "volumeUnit", DataUtility.getStringValue( itemMap, "VOLEH" ) );
			map.put( "weight", DataUtility.getDoubleMetricValue( itemMap, "BRGEW" ) );
			map.put( "weightUnit", DataUtility.getStringValue( itemMap, "GEWEI" ) );
			map.put( "billingNetAmount", billingNetAmount );
			map.put( "billingTax", billingTax );
			map.put( "billingDamagedDiscount", billingDamagedDiscount );
			map.put( "billingValue", billingTotal );

			if( Loggers.business.isTraceEnabled() ) {
				StringBuffer lbuf = new StringBuffer();
				for( String key : map.keySet() )
					lbuf.append( key + ": " + map.get(key) + ", " );
				Loggers.business.trace( "{}: {}", new Object[]{orderKey, lbuf.toString()} );
				lbuf.delete( 0, lbuf.capacity() );
			}

			lineList.add( map );
		}

		output.put( "lines", lineList );

		return output;
	}

	private void writeBillingResult( String outputXML ) {
		OrderLogging orderLogger = new OrderLogging( logger, null );

		java.io.File file = orderLogger.getFile( OrderCanonicalProcess.ORDER_IF_BILLING, "RES_" + billingNumber );
		logger.debug( "XML file name : "+ file.getAbsolutePath() +"("+ file.getName() +")" );
		this.document = orderLogger.makeOrderTraceFile( file, outputXML );
	}
}
