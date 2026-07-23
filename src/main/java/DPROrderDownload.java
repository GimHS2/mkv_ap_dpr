/*
 *  File Name:  DPROrderDownload.java
 *  Version:    2.2.1
 *
 *  Description:
 *
 *  Note:
 *      error.jsp
 *      pub_list_count.jsp
 *
 *  Modified    (YYYY/MM/DD)    Ver     Content
 *	song7981	2016/06/03		2.2.1	cutFirstStrInbyte 추가
 *  keehe       2008/09/26      2.2.0   create
 *
**/

import com.irt.dpr.Order;
import com.irt.dpr.OrderInfo;
import com.irt.dpr.OrderInfoDetail;
import com.irt.data.*;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataWriter;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.*;
import com.irt.util.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.lang.Number;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPROrderDownload"})
public class DPROrderDownload extends DPRServletModel {
	private final static int DEFAULT_DECIMAL_POINT			= 2;
	private final static String CONFIRMED_ORDERQTY_CRITERION	= "0";

	private final String CARRIAGE_LINE						= "\r\n";

	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

/* WORKING: orderNumber authority */
		if( !conditionMap.containsKey("orderNumber") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.put( "confirmedOrderQty" + Condition.SUFFIX_MIN_VALUE, CONFIRMED_ORDERQTY_CRITERION );
		conditionMap.put( "confirmedOrderQty" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_NONE );

		return conditionMap;
	}

	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {

		return super.doRequest( ctx, isPost );
	}

	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		//setSystemPackageCode
		if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.DWN" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_STATUS_"+ ctx.mode.toUpperCase()) );
	}

	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException{
		OrderInfo infoDB = new OrderInfo( ctx.handler );
		OrderInfoDetail detailDB = new OrderInfoDetail( ctx.handler );

		Map<String, Object> conditionMap = createConditionMap( ctx );
		String errorMessage = null;
		try {
			Order orderDB = new Order( ctx.handler, systemConfig );

			Map<String, Object> parameterMap = new java.util.HashMap<String, Object> ( conditionMap );
			parameterMap.put( "countryCode", getUserCountryCode(ctx) );
			parameterMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

			orderDB.executeEnquiry( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUS, parameterMap );
		} catch ( DataException dataEx ) {
			errorMessage = dataEx.getMessage();
		}

		String orderNumber = (String)conditionMap.get( "orderNumber" );
		String fileName = "order" + ( orderNumber != null ? "_"+ orderNumber + ".txt;" : ".txt;" );
		ctx.res.setContentType( "application/smnet" );
		ctx.res.setHeader( "Content-Disposition", "attachment; filename="+ fileName );
		java.io.Writer writer = null;
		try {
			String encoding = ctx.req.getParameter( "encoding" );
			if ( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );
			if( encoding != null )
				writer = new java.io.PrintWriter( new java.io.OutputStreamWriter(ctx.res.getOutputStream(), encoding) );
			else
				writer = new java.io.PrintWriter( new java.io.OutputStreamWriter(ctx.res.getOutputStream()) );
		} catch( java.io.UnsupportedEncodingException encodeEx ) {
			writer = new java.io.PrintWriter( new java.io.OutputStreamWriter(ctx.res.getOutputStream()) );
		}

		if( errorMessage != null ) {
			try {
				((java.io.PrintWriter)writer).print( errorMessage );
			} finally {
				writer.flush();
				writer.close();
			}

			return true;
		}

		Map<String, Object> infoRecordMap = infoDB.getRecord( conditionMap, new String[] {
			"orderNumber", "customerOrderNumber", "purchaseOrderNumber", "orderDate"
			,"soldPartyCode", "shipPartyCode", "deliveryDate", "confirmedOrderValue", "confirmedOrderDiscount"
			,"confirmedOrderTax", "confirmedOrderTotal", "organizationCode" } );

		if( infoRecordMap == null ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_NO_RECORD_FOUND") );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		}

		List <Map<String, Object>> detailRecordList = detailDB.getRecords( conditionMap, new String[] {
			"orderNumber", "lineNumber", "itemCode", "confirmedOrderQty", "confirmedOrderValue", "infoPrice", "uom", "itemName" } );

		int[] infoLength = new int[] { 3, 6, 8, 15, 13, 13, 8, 13, 13, 11,13,13,10 };
		int[] detailLength = new int[] { 3, 6, 3, 15, 9, 11, 11, 2, 15, 3, 35 };
		int[] instrHeaderLength = new int[] { 3, 6, 4, 70 };
		int[] instrDetailLength = new int[] { 3, 6, 3, 4, 70 };
		int[] trailerLength = new int[] { 3, 5 };

		com.irt.util.FLVWriter infoOut = new com.irt.util.FLVWriter( writer, infoLength, CARRIAGE_LINE );
		com.irt.util.FLVWriter detailOut = new com.irt.util.FLVWriter( writer, detailLength, CARRIAGE_LINE );
		com.irt.util.FLVWriter instructionsHeaderOut = new com.irt.util.FLVWriter( writer, instrHeaderLength, CARRIAGE_LINE );
		com.irt.util.FLVWriter instructionsItemOut = new com.irt.util.FLVWriter( writer, instrDetailLength, CARRIAGE_LINE );
		com.irt.util.FLVWriter trailerOut = new com.irt.util.FLVWriter( writer, trailerLength, CARRIAGE_LINE );

		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat( "yyyyMMdd" );

		long totalLineNumber = 0l;
		double fOrderAmount, fConfirmedNetAmount, fConfirmedDamagedDiscount, fConfirmedTotalValue, fConfirmedTax = 0.0;

		try {
// HDR 부분
			String orderNO = cutPrint(stringCheck(infoRecordMap.get("orderNumber")), true, infoLength, 1 ); // 6
			String customerOrderNumber = (String)infoRecordMap.get( "customerOrderNumber" );
			if( customerOrderNumber == null || customerOrderNumber.length() == 0 )
				customerOrderNumber = (String)infoRecordMap.get( "orderNumber" );

			String poNumber = cutPrint( customerOrderNumber, infoLength, 3 ); // 15

			double iConfirmedNetAmount = convertFormatNumber( infoRecordMap.get("confirmedOrderValue") );
			double iConfirmedTax = convertFormatNumber( infoRecordMap.get("confirmedOrderTax") );
			double iConfirmedDamagedDiscount = convertFormatNumber( infoRecordMap.get("confirmedOrderDiscount") );
			double iConfirmedTotalValue = convertFormatNumber( infoRecordMap.get("confirmedOrderTotal") );
			double iOrderAmount = iConfirmedNetAmount - iConfirmedDamagedDiscount;

			String orderDateStr = null, deliveryDateStr = null;
			if( infoRecordMap.get("orderDate") != null)
				orderDateStr = dateFormat.format( (java.util.Date)infoRecordMap.get("orderDate") );

			if( infoRecordMap.get("deliveryDate") != null)
				deliveryDateStr = dateFormat.format( (java.util.Date)infoRecordMap.get("deliveryDate") );

			infoOut.print( "HDR" );
			infoOut.print( orderNO );
			infoOut.print( orderDateStr );
			infoOut.print( poNumber );
			infoOut.print( infoRecordMap.get("soldPartyCode") );
			infoOut.print( infoRecordMap.get("shipPartyCode") );
			infoOut.print( deliveryDateStr );
			infoOut.print( numberFormat( 13, iOrderAmount) );
			infoOut.print( numberFormat( 13, iConfirmedNetAmount) );
			infoOut.print( numberFormat( 11, iConfirmedDamagedDiscount) );
			infoOut.print( numberFormat( 13, iConfirmedTotalValue) );

			//Thailand = 01
			if( (infoRecordMap.get("organizationCode")).equals("2400") )
				infoOut.print( "01" );
			else
				infoOut.printNull();

			// HDR 모르는 값( PRICAT No );
			infoOut.printNull();
			infoOut.println();

	// DTL 부분 시작
			Map<String, Object> detailOrderMap = null;

			double iConfirmedOrderQty = 0;
			double iInfoPrice = 0;
			double iLineItemAmount = 0;

			if( detailRecordList != null && detailRecordList.size() > 0 ) {
				for( Map<String, Object> recordMap : detailRecordList ) {
					if( detailOrderMap == null || !detailOrderMap.get("lineNumber").equals(recordMap.get("lineNumber")) ) {
						detailOrderMap = recordMap;

						iConfirmedOrderQty = convertFormatNumber( detailOrderMap.get("confirmedOrderQty"), 3 );
						iInfoPrice = convertFormatNumber( detailOrderMap.get("infoPrice") );
						iLineItemAmount = convertFormatNumber( detailOrderMap.get("confirmedOrderValue") );
					}

					String itemName = cutPrint(stringCheck( detailOrderMap.get("itemName") ), 35 );
					itemName = cutFirstStrInbyte( itemName, 35 );

					totalLineNumber+= 1;

					detailOut.print( "DTL" );
					detailOut.print( orderNO );
					detailOut.print( numberFormat( 3, totalLineNumber) );
					detailOut.print( detailOrderMap.get("itemCode") );
					detailOut.print( numberFormat( 9, iConfirmedOrderQty) );
					detailOut.print( numberFormat( 11, iInfoPrice) );
					detailOut.print( numberFormat( 11, iInfoPrice) );
					detailOut.print( "00" );
					detailOut.print( numberFormat( 15, iLineItemAmount) );
					detailOut.print( detailOrderMap.get("uom") );
					detailOut.print( itemName );
					detailOut.println();

				}
			}

			instructionsHeaderOut.print( "INH" );
			instructionsHeaderOut.print( orderNO );
			instructionsHeaderOut.print( "0012" );
			instructionsHeaderOut.printNull();
			instructionsHeaderOut.println();

			instructionsItemOut.print( "IND" );
			instructionsItemOut.print( orderNO );
			instructionsItemOut.print( "000" );
			instructionsItemOut.print( "0004" );
			instructionsItemOut.printNull();
			instructionsItemOut.println();

			trailerOut.print( "TLR" );
			trailerOut.print( numberFormat( 5, totalLineNumber) );
			trailerOut.println();

		} finally {
			infoOut.flush();
			detailOut.flush();
			instructionsHeaderOut.flush();
			instructionsItemOut.flush();
			trailerOut.flush();
			infoOut.close();
			detailOut.close();
			instructionsHeaderOut.close();
			instructionsItemOut.close();
			trailerOut.close();
			writer.close();
		}

		return true;

	}

	protected String cutFirstStrInbyte( String str, int endIndex ) {
		StringBuffer buffer = new StringBuffer();
		int length = 0;

		for( char c: str.toCharArray() ) {
			length += String.valueOf(c).getBytes().length;
			if( length > endIndex ) {
				break;
			}
			buffer.append( c );
		}
		return buffer.toString();
	}

	protected String stringCheck( Object obj ) {
		if( obj == null )
			return null;
		else
			if(obj instanceof String)
				return (String)obj;
			else
				return null;
	}

	protected double convertFormatNumber( Object obj ) {
		return convertFormatNumber( obj, DEFAULT_DECIMAL_POINT );
	}

	protected double convertFormatNumber( Object obj, int decimalPoint ) {
		double fTemp = 0.0;
		if( obj instanceof Number )
			fTemp = ((Number)obj).doubleValue();
		else {
			try {
				if( obj != null )
					fTemp = Double.parseDouble( obj.toString() );
			} catch( NumberFormatException ignore ) {}
		}

		for( int i = 0; i < decimalPoint; i++ )
			fTemp = fTemp * 10;

		return fTemp;
	}

	protected String cutPrint( String string, int length[], int index ) {
		return cutPrint( string, false, length, index );
	}

	protected String cutPrint( String string, boolean isStartCut, int length[], int index ) {
		if( string == null ) return null;

		if( string.length() > length[index] ) {
			int beginIndex = 0;
			int endIndex = length[index];
			int diff = string.length() - length[index];
			if( isStartCut && diff > 0 ) {
				beginIndex = diff;
				endIndex = diff + length[index];
			}

			return cutPrint( string, beginIndex, endIndex );
		}

		return string;
	}

	protected String cutPrint( String string, int endIndex ) {
		return cutPrint( string, 0, endIndex );
	}

	protected String cutPrint( String string, int beginIndex, int endIndex ) {
		if( string == null ) return null;

		if( string.length() >= endIndex ) {
			return string.substring( beginIndex, endIndex );
		} else
			return string;

	}

	protected String numberFormat( int length, Double value ) {
		String format = "";

		if( value < 0 )
			format+="#";
		else
			format+="0";

		for(int i=0;i<length-1;i++){
			format+="0";
		}

		java.text.NumberFormat numberFormat = new java.text.DecimalFormat( format );
		return numberFormat.format( value );
	}

	protected String numberFormat( int length, long value ) {
		String format = "";

		if( value < 0 )
			format+="#";
		else
			format+="0";

		for(int i=0;i<length-1;i++){
			format+="0";
		}

		java.text.NumberFormat numberFormat = new java.text.DecimalFormat( format );
		return numberFormat.format( value );
	}

}
