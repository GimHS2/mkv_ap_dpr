/*
 *	File Name:	TimeSelectInputTag.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/04/22		2.2.2	getValue() -> getValueOne()
 *	stghr12		2009/10/31		2.2.1	inputType -> mandatoryType
 *	stghr12		2007/11/30		2.2.0	표시방법 변경: XX시 XX분 -> XX:XX
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="select"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)defaultValue
 * <li>(O)hasBlank
 * <li>(O)id
 * <li>(O)increaseMinute
 * <li>(M)key
 * <li>(O)mandatory
 * <li>(O)maxHour
 * <li>(O)maxMinute
 * <li>(O)minHour
 * <li>(O)minMinute
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)readonly
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 */
public class TimeSelectInputTag extends InputTag {
	boolean hasBlank;
	int minHour = 0, maxHour = 23;
	int minMinute = 0, maxMinute = 59, increaseMinute = 1;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			Object value = getValueOne();
			String h_value = null;
			String m_value = null;
			if( value != null ) {
				if( value instanceof String ) {
					String time_value = (String)value;
					if( time_value != null && time_value.length() == 5 && time_value.charAt(2) == ':' ) {
						h_value = time_value.substring( 0, 2 );
						m_value = time_value.substring( 3, 5 );
					}
				} else
					value = value.toString();
			}

			if( mandatoryType == INPUT_INFORMATION ) {
				if( h_value != null ) {
					out.print( HtmlUtility.toHtmlString(h_value) );
					out.print( ":" );
					out.print( HtmlUtility.toHtmlString(m_value) );
				} else
					out.print( HtmlUtility.toHtmlString(value) );
			} else {
				String name = getName();
				String attribute = getAttribute( HtmlPage.INPUT_SELECT );

				if( mandatoryType == INPUT_READONLY ) {
					out.print( "<select name='"+ name +"_h'"+ attribute +">" );
					out.print( "<option value='"+ HtmlUtility.toHtmlString(h_value) +"'>"+ HtmlUtility.toHtmlString(h_value) +"</option>" );
					out.print( "</select>" );

					out.print( ":" );
					out.print( "<select name='"+ name +"_m'"+ attribute +">" );
					out.print( "<option value='"+ HtmlUtility.toHtmlString(m_value) +"'>"+ HtmlUtility.toHtmlString(m_value) +"</option>" );
					out.print( "</select>" );
				} else {
					out.print( "<select name='"+ name +"_h'"+ attribute +">" );
					printOptions( h_value, minHour, maxHour, 1 );
					out.print( "</select>" );

					out.print( ":" );
					out.print( "<select name='"+ name +"_m'"+ attribute +">" );
					printOptions( m_value, minMinute, maxMinute, increaseMinute );
					out.print( "</select>" );
				}
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}

	private void printOptions( String value, int minValue, int maxValue, int increaseValue ) throws java.io.IOException {
		boolean printed = ( value == null );
		JspWriter out = pageContext.getOut();

		if( hasBlank ) out.print( "<option value=''></option>" );
		for( int v = minValue; v <= maxValue; v += increaseValue ) {
			String optionValue = ( v < 10 ? "0" + String.valueOf(v) : String.valueOf(v) );

			if( printed )
				out.print( "<option value='"+ optionValue +"'>"+ optionValue +"</option>" );
			else {
				int compare = value.compareTo( optionValue );
				if( compare == 0 ) {
					printed = true;
					out.print( "<option value='"+ optionValue +"' selected>"+ optionValue +"</option>" );
				} else if( compare < 0 ) {
					printed = true;
					out.print( "<option value='"+ HtmlUtility.toHtmlString(value) +"'>"+ HtmlUtility.toHtmlString(value) +"</option>" );
					out.print( "<option value='"+ optionValue +"'>"+ optionValue +"</option>" );
				} else
					out.print( "<option value='"+ optionValue +"'>"+ optionValue +"</option>" );
			}
		}
		if( !printed )
			out.print( "<option value='"+ HtmlUtility.toHtmlString(value) +"'>"+ HtmlUtility.toHtmlString(value) +"</option>" );
	}

	public void setHasBlank( boolean hasBlank ) {
		this.hasBlank = hasBlank;
	}

	public void setIncreaseMinute( int increaseMinute ) {
		this.increaseMinute = ( increaseMinute <= 0 ? 1 : increaseMinute );
	}

	public void setMaxHour( int maxHour ) {
		this.maxHour = ( maxHour > 23 ? 23 : maxHour );
	}

	public void setMaxMinute( int maxMinute ) {
		this.maxMinute = ( maxMinute > 59 ? 59 : maxMinute );
	}

	public void setMinHour( int minHour ) {
		this.minHour = ( minHour < 0 ? 0 : minHour );
	}

	public void setMinMinute( int minMinute ) {
		this.minMinute = ( minMinute < 0 ? 0 : minMinute );
	}
}
