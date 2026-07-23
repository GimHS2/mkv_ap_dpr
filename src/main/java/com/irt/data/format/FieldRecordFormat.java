/*
 *	File Name:	FieldRecordFormat.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	extends PattenRecordFormat -> implements RecordFormat
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	isByteLength 추가
 *	stghr12		2006/11/26		2.0.1	encodeType 오류 수정
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data.format;

import com.irt.html.HtmlUtility;
import com.irt.util.MessageHandler;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

/**
 *
 */
class FieldRecordFormat implements RecordFormat {
	int encodeType;
	String fieldKey, prefixKey, numberFormatKey;
	RecordFormat nullValue, prefixValue, suffixValue;
	int maxlength = -1;
	int beginIndex = -1;
	int endIndex = -1;
	boolean isByteLength = false;

	FieldRecordFormat() {}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		fieldKeySet.add( fieldKey );
		if( nullValue != null ) nullValue.addFieldKeyToSet( fieldKeySet );
		if( prefixValue != null ) prefixValue.addFieldKeyToSet( fieldKeySet );
		if( suffixValue != null ) suffixValue.addFieldKeyToSet( fieldKeySet );
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return format( recordMap, msghandler, new StringBuffer() ).toString();
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		if( recordMap == null ) return stringBuffer;

		Object value = recordMap.get( fieldKey );
		if( value == null ) {
			if( nullValue != null ) nullValue.format( recordMap, msghandler, stringBuffer );
			return stringBuffer;
		}

		String svalue = value.toString();
		if( numberFormatKey != null ) {
			String pattern = msghandler.getMessage( numberFormatKey );
			if( pattern != null ) {
				NumberFormat numberFormat = new java.text.DecimalFormat( pattern );
				if( value instanceof Number )
					svalue = numberFormat.format( ((Number)value).doubleValue() );
				else {
					try {
						svalue = numberFormat.format( Double.parseDouble(svalue) );
					} catch( NumberFormatException numEx ) {}
				}
			}
		} else if( prefixKey != null )
			svalue = msghandler.getMessage( prefixKey + svalue );
		else {
			if( isByteLength ) {
				if( maxlength > 0 || (beginIndex == 0 && endIndex > 0) ) {
					int length = ( maxlength > 0 ? maxlength : endIndex );
					byte[] bytes = svalue.getBytes();
					if( bytes.length > length ) {
						svalue = new String( bytes, 0, length );
						if( svalue.length() == (new String(bytes, 0, length+1)).length() )
							svalue = new String( bytes, 0, length-1 );
						if( maxlength > 0 ) svalue += "...";
					}
				}
			} else if( maxlength > 0 ) {
				if( svalue.length() > maxlength )
					svalue = svalue.substring(0, maxlength) + "...";
			} else if( beginIndex >= 0 ) {
				if( svalue.length() > beginIndex ) {
					if( endIndex > 0 && svalue.length() > endIndex )
						svalue = svalue.substring( beginIndex, endIndex );
					else
						svalue = svalue.substring( beginIndex );
				} else
					svalue = "";
			}

			if( encodeType == 'H' )
				svalue = HtmlUtility.toHtmlString( svalue );
			else if( encodeType == 'S' )
				svalue = HtmlUtility.toScriptString( svalue );
		}

		if( prefixValue != null ) prefixValue.format( recordMap, msghandler, stringBuffer );
		stringBuffer.append( svalue );
		if( suffixValue != null ) suffixValue.format( recordMap, msghandler, stringBuffer );

		return stringBuffer;
	}
}
