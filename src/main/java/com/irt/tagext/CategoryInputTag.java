/*
 *	File Name:	CategoryInputTag.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2016/08/31		2.2.3	doEndTag() -> value 에 특수문자 변환 추가
 *	stghr12		2011/04/22		2.2.2	getValue() -> getValueOne()
 *	stghr12		2009/10/31		2.2.1	inputType -> mandatoryType
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.format.PatternRecordFormat;
import com.irt.data.QueryableManager;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="select"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)defaultValue
 * <li>(O)format
 * <li>(O)hasBlank
 * <li>(O)id
 * <li>(M)key
 * <li>(O)listFormat
 * <li>(O)mandatory
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)nullValue | nullValueKey
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)readonly
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 */
public class CategoryInputTag extends InputTag {
	String[] fieldKeys = new String[] { "classCode", "code", "name", "levelCode" };
	boolean hasBlank;
	String nullValue, nullValueKey;
	com.irt.data.format.RecordFormat listFormat;

	public int doEndTag() throws JspException {
		QueryableManager codeDB = (QueryableManager)pageContext.findAttribute( "codeDB" );
		if( codeDB == null ) return EVAL_PAGE;

		JspWriter out = pageContext.getOut();
		try {
			if( listFormat == null )
				listFormat = PatternRecordFormat.getInstance(
					"$f{decode(classCode,1,,2,&nbsp;&nbsp;,3,&nbsp;&nbsp;&nbsp;&nbsp;,&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;)}$H{name}"
				);

			Object value = getValueOne();
			if( value != null && !(value instanceof String) ) value = HtmlUtility.toHtmlString( value.toString() );
			if( mandatoryType == INPUT_INFORMATION ) {
				if( format != null )
					out.print( getFormatValue() );
				else {
					Map<String, Object> recordMap = codeDB.getRecord( com.irt.data.Record.createMap("code", value), fieldKeys );
					out.print( listFormat.format(recordMap, msghandler) );
				}
			} else {
				List<Map<String, Object>> recordList = null;
				if( mandatoryType == INPUT_READONLY ) {
					Map<String, Object> recordMap = codeDB.getRecord( com.irt.data.Record.createMap("code", value), fieldKeys );
					if( recordMap != null ) {
						recordList = new java.util.ArrayList<Map<String, Object>>();
						recordList.add( recordMap );
					}
				} else {
					codeDB.setSort( "code" );
					recordList = codeDB.getRecords( null, new String[] { "classCode", "code", "name", "levelCode" } );
				}

				String name = getName();
				String attribute = getAttribute( HtmlPage.INPUT_SELECT );

				out.print( "<select name='"+ name +"'"+ attribute +">" );
				if( hasBlank ) {
					out.print( "<option value=''>" );
					if( nullValue != null )
						out.print( HtmlUtility.toHtmlString(nullValue) );
					else if( nullValueKey != null )
						out.print( HtmlUtility.toHtmlString(msghandler.getMessage(nullValueKey)) );
					out.print( "</option>" );
				}
				if( recordList != null ) {
					for( Map<String, Object> recordMap : recordList ) {
						Object code = recordMap.get( "code" );
						if( code.equals(value) )
							out.print( "<option value='"+ code +"' selected>" );
						else
							out.print( "<option value='"+ code +"'>" );
						out.print( listFormat.format(recordMap, msghandler) );
						out.print( "</option>" );
					}
				}
				out.print( "</select>" );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		} catch( java.sql.SQLException sqlEx ) {
			throw new JspException( sqlEx );
		}

		return EVAL_PAGE;
	}

	public void setHasBlank( boolean hasBlank ) {
		this.hasBlank = hasBlank;
	}

	public void setNullValue( String nullValue ) {
		this.nullValue = nullValue;
	}

	public void setNullValueKey( String nullValueKey ) {
		this.nullValueKey = nullValueKey;
	}

	public void setListFormat( String listFormat ) {
		this.listFormat = PatternRecordFormat.getInstance( listFormat );
	}
}
