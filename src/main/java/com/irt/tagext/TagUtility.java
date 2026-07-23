/*
 *	File Name:	TagUtility.java
 *	Version:	2.2.11c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.11c	신규 UI/UX 적용
 *	hankalam	2016/09/30		2.2.11	getValue(), getMap(): XSS 필터링을 위해 request객제 ServletRequestWrapper형으로 변환
 *	GimHS		2012/12/31		2.2.10	CrossBrowsing 적용
 *										  -> JavaScript에서 event object를 사용하는 경우 바로 사용할수 없어서
 *										     a 태그에서 onClick event의 JavaScript:setEventObject(event) 함수를 이용하여 event를 저장하도록 변경
 *	stghr12		2011/03/31		2.2.9	getValue(): request.getParameter() -> request.getParameterValues()
 *										getValueOne() 추가
 *	stghr12		2011/02/28		2.2.8	INPUTBUTTON_SELECT 추가
 *										getInputButtonTag() 오류수정
 *	stghr12		2010/09/30		2.2.7	NullPointerException 처리
 *	stghr12		2010/08/31		2.2.6	parseCondition(): com.irt.data.Record.decodeQueryToMap() 사용
 *	stghr12		2010/07/31		2.2.5	parseCondition(): conditionString == null일 경우 처리
 *										printSelectLevel(): authorizedCodeValues, unauthorizedCodeValues 지원
 *										printSelectLevel(): <span> 출력로직 변경
 *	stghr12		2010/01/31		2.2.4	printSelectLevel(): codeDB.setSort() -> codeDB.appendSort()
 *	stghr12		2009/12/31		2.2.3	parseCondition() 추가, printSelectLevel()에서 conditionMap 지원
 *	stghr12		2009/10/31		2.2.2	version up
 *	stghr12		2009/06/30		2.2.1	getMap(), getValue()를 public으로 변경
 *	stghr12		2008/08/29		2.2.0	pageContext.findAttribute("request") 사용
 *	stghr12		2007/10/31		2.1.1	getMap(): com.irt.servlet.ParameterMap 사용
 *	stghr12		2007/04/30		2.1.0	class TagUtility -> public class TagUtility
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.AbstractField;
import com.irt.data.Condition;
import com.irt.data.ValidableField;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.ServletRequestWrapper;
import com.irt.util.MessageHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 *
 */
public class TagUtility {
	public final static String INPUTBUTTON_CALENDAR		= "calendar";
	public final static String INPUTBUTTON_SELECT		= "select";

	HtmlPage htmlpage;
	Properties properties;
	MessageHandler msghandler;

	public TagUtility( HtmlPage htmlpage ) {
		this.htmlpage = htmlpage;
		this.properties = htmlpage.getProperty();
		this.msghandler = htmlpage.getMessageHandler();
	}

	public static com.irt.data.QueryableManager getQueryableManager( PageContext pageContext, String classId, String className ) throws JspException {
		if( classId != null ) {
			com.irt.sql.HierarchyCodeManager codeDB = (com.irt.sql.HierarchyCodeManager)pageContext.findAttribute( classId );
			if( codeDB != null ) return codeDB;
		}

		if( className != null ) {
			com.irt.sql.SQLHandler handler = (com.irt.sql.SQLHandler)pageContext.findAttribute( "handler" );

			ClassLoader classLoader = TagUtility.class.getClassLoader();
			try {
				java.lang.reflect.Constructor constructor = classLoader.loadClass(className).getConstructor( com.irt.sql.SQLHandler.class );
				return (com.irt.data.QueryableManager)constructor.newInstance( handler );
			} catch( ClassNotFoundException classEx ) {
				throw new JspException( classEx );
			} catch( IllegalAccessException accessEx ) {
				throw new JspException( accessEx );
			} catch( InstantiationException instantEx ) {
				throw new JspException( instantEx );
			} catch( java.lang.reflect.InvocationTargetException invocationEx ) {
				throw new JspException( invocationEx );
			} catch( NoSuchMethodException noSuchMethodEx ) {
				throw new JspException( noSuchMethodEx );
			}
		}

		return null;
	}

	public String getInputAttribute( String inputType, char mandatoryType, String modified ) {
		return getInputAttribute( inputType, mandatoryType, modified, null, null, null );
	}

	public String getInputAttribute( String inputType, char mandatoryType, String modified, String style, String styleId, String styleClass ) {
		StringBuffer attrbuf = new StringBuffer();

		if( modified == null )
			modified = properties.getProperty( "jsp.script.modified" );

		String styleClass_new = null;
		if( mandatoryType == HtmlPage.INPUT_READONLY ) {
			if( !HtmlPage.INPUT_HIDDEN.equals(inputType) ) {
				attrbuf.append( " tabindex='-1'" );
				if( HtmlPage.INPUT_CHECK.equals(inputType) || HtmlPage.INPUT_RADIO.equals(inputType) )
					attrbuf.append( " disabled='true'" );
				else {
					styleClass_new = properties.getProperty( "jsp.styleClass.input.readonly" );
					if( !HtmlPage.INPUT_SELECT.equals(inputType) )
						attrbuf.append( " readonly='true'" );
				}
			}
		} else {
			if( mandatoryType == HtmlPage.INPUT_MANDATORY ) {
				styleClass_new = properties.getProperty( "jsp.styleClass.input."+ inputType +".mandatory" );
				if( styleClass_new == null )
					styleClass_new = properties.getProperty( "jsp.styleClass.input.mandatory" );
			} else if( mandatoryType == HtmlPage.INPUT_OPTIONAL ) {
				styleClass_new = properties.getProperty( "jsp.styleClass.input."+ inputType +".optional" );
				if( styleClass_new == null )
					styleClass_new = properties.getProperty( "jsp.styleClass.input.optional" );
			}

			if( modified != null ) {
				if( HtmlPage.INPUT_CHECK.equals(inputType) || HtmlPage.INPUT_RADIO.equals(inputType) )
					attrbuf.append( " onClick='"+ modified +"'" );
				else
					attrbuf.append( " onChange='"+ modified +"'" );
			}
		}
		if( styleClass == null )
			styleClass = styleClass_new;
		else if( styleClass_new != null )
			styleClass = styleClass_new +" "+ styleClass;

		if( styleId != null ) attrbuf.append( " id='"+ styleId +"'" );
		if( styleClass != null ) attrbuf.append( " class='"+ styleClass +"'" );
		if( style != null ) attrbuf.append( " style='"+ style +"'" );

		return attrbuf.toString();
	}

	public String getInputButtonTag( String type, String key, String href, String naming, String imageSrc, String title, String attribute ) {
		Properties properties = htmlpage.getProperty();

		if( href == null ) {
			href = htmlpage.getProperty().getProperty( "jsp.ibutton."+ type +".href" );
			if( href != null ) {
				Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
				recordMap.put( "form", htmlpage.getFormName() );
				recordMap.put( "key", key );
				recordMap.put( "element", htmlpage.getFormName() +"."+ key );

				href = PatternRecordFormat.getInstance(href).format( recordMap, msghandler );
			}
			if( href == null || href.length() == 0 ) return null;
		}

		if( imageSrc == null ) {
			imageSrc = htmlpage.getProperty().getProperty( "jsp.ibutton."+ type +".image" );
			if( imageSrc == null || imageSrc.length() == 0 ) return null;
		}

		String attribute_a = " tabindex='-1'";
		if( naming != null ) attribute_a += " onContextMenu='"+ naming +"'";
		if( title != null ) attribute_a += " title='"+ title +"'";

		return "<a href='JavaScript:void(0);' onClick='JavaScript:setEventObject(event);"+ href +"' "+ attribute_a +"><img src='"+ imageSrc +"' "+ ( attribute == null ? "" : attribute ) +"/></a>";
	}

	public static Map getMap( PageContext pageContext, String id ) {
		if( "request".equals(id) ) {
			javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)pageContext.findAttribute( id );
			if( request == null ) request = (javax.servlet.http.HttpServletRequest)pageContext.getRequest();

			ServletRequestWrapper requestWrapper = new ServletRequestWrapper( request );
			return new com.irt.servlet.ParameterMap( requestWrapper );
		} else
			return (Map)pageContext.findAttribute( id );
	}

	public static String getStyleAttribute( String style, String styleId, String styleClass ) {
		String attribute = "";

		if( styleId != null ) attribute += " id='"+ styleId +"'";
		if( styleClass != null ) attribute += " class='"+ styleClass +"'";
		if( style != null ) attribute += " style='"+ style +"'";

		return attribute;
	}

	public static Object getValue( PageContext pageContext, String id, String key ) {
		if( "request".equals(id) ) {
			javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)pageContext.findAttribute( id );
			if( request == null ) request = (javax.servlet.http.HttpServletRequest)pageContext.getRequest();

			ServletRequestWrapper requestWrapper = new ServletRequestWrapper( request );
			String[] values = requestWrapper.getParameterValues( key );
			return ( values != null && values.length > 0 ? values : requestWrapper.getParameter(key) );
		} else {
			Map map = (Map)pageContext.findAttribute( id );
			if( map != null )
				return map.get( key );

			return null;
		}
	}

	public static Object getValueOne( PageContext pageContext, String id, String key ) {
		Object object = getValue( pageContext, id, key );

		if( object instanceof Object[] ) {
			if( ((Object[])object).length == 0 )
				return null;
			else
				return ((Object[])object)[0];
		}

		return object;
	}

	public String[][] makeOptionValues( String prefixKey, String[] codeValues ) {
		return makeOptionValues( prefixKey, codeValues, null );
	}

	public String[][] makeOptionValues( String prefixKey, String[] codeValues, String nullValue ) {
		String[][] optionValues = new String[ codeValues.length + (nullValue != null ? 1 : 0) ][2];

		int idx = 0;
		if( nullValue != null ) {
			optionValues[idx][0] = "";
			optionValues[idx++][1] = nullValue;
		}

		for( int v = 0; v < codeValues.length; v++, idx++ ) {
			optionValues[idx][0] = codeValues[v];

			if( prefixKey == null ) {
				if( "ALL".equals(codeValues[v]) )
					optionValues[idx][1] = msghandler.getMessage( "MSG_ALL" );
				else
					optionValues[idx][1] = codeValues[v];
			} else {
				if( "ALL".equals(codeValues[v]) ) {
					try {
						optionValues[idx][1] = msghandler.getMessageValue( prefixKey + "ALL" );
					} catch( java.util.MissingResourceException misEx ) {
						optionValues[idx][1] = msghandler.getMessage( "MSG_ALL" );
					}
				} else
					optionValues[idx][1] = msghandler.getMessage( prefixKey + codeValues[v] );
			}
		}

		return optionValues;
	}

	public String[][] makeOptionValues( Collection<? extends Map> optionList, Object codeField, Object nameField ) {
		return makeOptionValues( optionList, codeField, nameField, null, null );
	}

	public String[][] makeOptionValues( Collection<? extends Map> optionList, Object codeField, Object nameField, Object extraField ) {
		return makeOptionValues( optionList, codeField, nameField, extraField, null );
	}

	public String[][] makeOptionValues( Collection<? extends Map> optionList, Object codeField, Object nameField, Object extraField
						, String nullValue ) {
		int count = 0;
		if( nullValue != null ) count++;
		if( optionList != null ) count += optionList.size();
		if( count == 0 ) return null;

		int idx = 0;
		String[][] optionValues = new String[count][extraField == null ? 2 : 3];
		if( nullValue != null ) {
			optionValues[idx][0] = "";
			optionValues[idx++][1] = nullValue;
		}

		if( optionList != null ) {
			for( java.util.Iterator<? extends Map> iterator = optionList.iterator(); iterator.hasNext(); idx++ ) {
				Object object;
				Map optionMap = iterator.next();

				if( codeField instanceof RecordFormat )
					optionValues[idx][0] = ((RecordFormat)codeField).format( optionMap, msghandler );
				else {
					object = optionMap.get( codeField );
					optionValues[idx][0] = ( object == null ? "" : HtmlUtility.toHtmlString(object.toString()) );
				}

				if( nameField instanceof RecordFormat )
					optionValues[idx][1] = ((RecordFormat)nameField).format( optionMap, msghandler );
				else {
					object = optionMap.get( nameField );
					optionValues[idx][1] = ( object == null ? "" : HtmlUtility.toHtmlString(object.toString()) );
				}

				if( extraField instanceof RecordFormat )
					optionValues[idx][2] = ((RecordFormat)extraField).format( optionMap, msghandler );
				else if( extraField instanceof String ) {
					object = optionMap.get( extraField );
					optionValues[idx][2] = ( object == null ? "" : HtmlUtility.toHtmlString(object.toString()) );
				}
			}
		}

		return optionValues;
	}

	public static Map<String, Object> parseCondition( String conditionString ) {
		return com.irt.data.Record.decodeQueryToMap( conditionString );
	}

	public void printCheckbox( JspWriter out, String name, String checkValue, String description, boolean checked ) throws IOException {
		printCheckbox( out, name, checkValue, description, checked, "" );
	}

	public void printCheckbox( JspWriter out, String name, String checkValue, String description, boolean checked, String attribute )
						throws IOException {
		if( attribute == null ) attribute = "";

		String checkId = name + "_"+ checkValue;

		out.print( "<input type='checkbox' name='"+ name +"' value='"+ checkValue +"' id='"+ checkId + "' "+ attribute );
		if( checked ) out.print( " checked" );
		out.print( "/>" );
		out.print( "<label for='"+ checkId + "'><span>" + description +"</span></label>" );
	}

	public boolean printInputButton( JspWriter out, String type, String key, String href, String naming, String imageSrc, String title
						, String attribute ) throws IOException {
		String html = getInputButtonTag( type, key, href, naming, imageSrc, title, attribute );

		if( html != null ) {
			out.print( html );
			return true;
		} else
			return false;
	}

	public int printOptions( JspWriter out, Object value, String[][] optionValues ) throws IOException {
		if( optionValues == null ) return 0;

		int selectedCount = 0;
		Object[] values = (Object[])( value instanceof Object[] ? value : null );

		for( int v = 0; v < optionValues.length; v++ ) {
			String codeValue = optionValues[v][0];
			Object extraValue = ( optionValues[v].length > 2 ? optionValues[v][2] : null );

			out.print( "<option value='"+ codeValue +"'" );
			if( extraValue != null ) out.print( " extraValue='"+ extraValue +"'" );
			if( codeValue.equals(value) ) {
				selectedCount++;
				out.print( " selected" );
			} else if( values != null ) {
				for( int i = 0; i < values.length; i++ )
					if( codeValue.equals(values[i]) ) {
						selectedCount++;
						out.print( " selected" );
						break;
					}
			}
			out.print( ">"+ optionValues[v][1] +"</option>" );
		}

		return selectedCount;
	}

	public void printRadio( JspWriter out, String name, Object value, String prefixKey, String[] codeValues ) throws IOException {
		printRadio( out, name, value, makeOptionValues(prefixKey, codeValues), "" );
	}

	public void printRadio( JspWriter out, String name, Object value, String prefixKey, String[] codeValues, String attribute ) throws IOException {
		printRadio( out, name, value, makeOptionValues(prefixKey, codeValues), attribute );
	}

	public void printRadio( JspWriter out, String name, Object value, String[][] optionValues ) throws IOException {
		printRadio( out, name, value, optionValues, "" );
	}

	public void printRadio( JspWriter out, String name, Object value, String[][] optionValues, String attribute ) throws IOException {
		if( attribute == null ) attribute = "";

		for( int v = 0; v < optionValues.length; v++ ) {
			String checkId = name +"_"+ optionValues[v][0];

			if( v > 0 ) out.print( "&nbsp;" );

			out.print( "<input type='radio' name='"+ name +"' value='"+ optionValues[v][0] +"' id='"+ checkId +"' "+ attribute );
			if( optionValues[v][0].equals(value) ) out.print( " checked" );
			out.println( "/>" );
			out.print( "<label for='"+ checkId +"'><span>" );
			out.print( optionValues[v][1] );
			out.println( "</span></label>" );
		}
	}

	public void printSelect( JspWriter out, String name, Object value, String prefixKey, String[] codeValues ) throws IOException {
		printSelect( out, name, value, makeOptionValues(prefixKey, codeValues), "" );
	}

	public void printSelect( JspWriter out, String name, Object value, String prefixKey, String[] codeValues, String attribute ) throws IOException {
		printSelect( out, name, value, makeOptionValues(prefixKey, codeValues), attribute );
	}

	public void printSelect( JspWriter out, String name, Object value, Collection<? extends Map> optionList, Object codeField, Object nameField
						, String nullValue ) throws IOException {
		printSelect( out, name, value, makeOptionValues(optionList, codeField, nameField, null, nullValue), "" );
	}

	public void printSelect( JspWriter out, String name, Object value, Collection<? extends Map> optionList, Object codeField, Object nameField
						, String nullValue, String attribute ) throws IOException {
		printSelect( out, name, value, makeOptionValues(optionList, codeField, nameField, null, nullValue), attribute );
	}

	public void printSelect( JspWriter out, String name, Object value, String[][] optionValues ) throws IOException {
		printSelect( out, name, value, optionValues, "" );
	}

	public void printSelect( JspWriter out, String name, Object value, String[][] optionValues, String attribute ) throws IOException {
		if( attribute == null ) attribute = "";

		out.print( "<select name='"+ name +"' "+ attribute +">" );
		printOptions( out, value, optionValues );
		out.print( "</select>" );
	}

	public void printSelectMultiple( JspWriter out, String name, Object value, String[][] optionValues, String nullValue
						, String attribute_s, String attribute_m ) throws IOException {
		int selectedCount = 0;
		String MULTIPLE_SELECT_VALUE = "__MULTIPLE_SELECT__";

		out.print( "<select name='"+ name +"' "+ attribute_m +">" );
		if( nullValue != null )
			out.print( "<option value=''>"+ nullValue +"</option>" );
		if( optionValues != null ) selectedCount = printOptions( out, value, optionValues );
		out.print( "</select>" );

		if( selectedCount > 1 ) value = new String[] { MULTIPLE_SELECT_VALUE };
		out.print( "<select name='_"+ name +"' "+ attribute_s +">" );
		if( nullValue != null )
			out.print( "<option value=''>"+ nullValue +"</option>" );
		if( optionValues != null ) {
			if( nullValue == null )
				out.print( "<option value=''></option>" );
			out.print( "<option value='"+ MULTIPLE_SELECT_VALUE +"'"+ ( selectedCount > 1 ? " selected" : "" ) +">" );
			out.print( msghandler.getMessage("jsp.MSG_MULTIPLE_SELECT") +"</option>" );
			printOptions( out, value, optionValues );
		}
		out.print( "</select>" );
	}

	public void printText( JspWriter out, String name, Object value, int maxlength ) throws IOException {
		printText( out, "text", name, value, maxlength, "" );
	}

	public void printText( JspWriter out, String name, Object value, int maxlength, String attribute ) throws IOException {
		printText( out, "text", name, value, maxlength, attribute );
	}

	public void printText( JspWriter out, String type, String name, Object value, int maxlength, String attribute ) throws IOException {
		if( attribute == null ) attribute = "";

		out.print( "<input type='"+ type +"' name='"+ name +"' value='"+ HtmlUtility.toHtmlString(value) +"' "+ attribute );
		if( maxlength > 0 ) out.print( " maxlength='"+ maxlength +"'" );
		out.print( "/>" );
	}

	public void putValidationScript( ValidableField field, char mandatoryType, String elementName ) {
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();

		recordMap.put( "element", elementName );

		if( mandatoryType == HtmlPage.INPUT_MANDATORY ) {
			String scriptMandatory = properties.getProperty( "jsp.script.check.mandatory" );
			if( scriptMandatory != null )
				htmlpage.putValidationScript( PatternRecordFormat.getInstance(scriptMandatory).format(recordMap, msghandler) +"\n" );
		}

		if( field != null ) {
			switch( field.getDataType() ) {
			case AbstractField.TYPE_INTEGER:
			case AbstractField.TYPE_LONG:
				String scriptNumberFormatInt = properties.getProperty( "jsp.script.check.number.format_i" );
				if( scriptNumberFormatInt != null )
					htmlpage.putValidationScript( PatternRecordFormat.getInstance(scriptNumberFormatInt).format(recordMap, msghandler) +"\n" );

				if( field.getMinValue() != null || field.getMaxValue() != null ) {
					String scriptNumberRange = properties.getProperty( "jsp.script.check.number.range" );
					if( scriptNumberRange != null ) {
						recordMap.put( "minValue", field.getMinValue() );
						recordMap.put( "maxValue", field.getMaxValue() );
						recordMap.put( "rangeType", field.getRangeType() );

						htmlpage.putValidationScript( PatternRecordFormat.getInstance(scriptNumberRange).format(recordMap, msghandler) +"\n" );
					}
				}
				break;
			case AbstractField.TYPE_DOUBLE:
				String scriptNumberFormatDouble = properties.getProperty( "jsp.script.check.number.format_f" );
				if( scriptNumberFormatDouble != null )
					htmlpage.putValidationScript( PatternRecordFormat.getInstance(scriptNumberFormatDouble).format(recordMap, msghandler) +"\n" );

				if( field.getMinValue() != null || field.getMaxValue() != null ) {
					String scriptNumberRange = properties.getProperty( "jsp.script.check.number.range" );
					if( scriptNumberRange != null ) {
						recordMap.put( "minValue", field.getMinValue() );
						recordMap.put( "maxValue", field.getMaxValue() );
						recordMap.put( "rangeType", field.getRangeType() );

						htmlpage.putValidationScript( PatternRecordFormat.getInstance(scriptNumberRange).format(recordMap, msghandler) +"\n" );
					}
				}
				break;
			case AbstractField.TYPE_DATE:
				String scriptDateFormat = properties.getProperty( "jsp.script.check.date.format" );
				if( scriptDateFormat != null )
					htmlpage.putValidationScript( PatternRecordFormat.getInstance(scriptDateFormat).format(recordMap, msghandler) +"\n" );
				break;
			}
		}
	}

	public void printSelectLevel( JspWriter out, com.irt.sql.HierarchyCodeManager codeDB, Map<String, ? extends Object> conditionMap_org
						, String[] codeValues, int startLevel, int lastLevel, int fullListingLevel, int filterListingLevel
						, String name, RecordFormat format, String nullValueKey, boolean multiple
						, char mandatoryType, String modified, String style, String styleId, String styleClass ) throws IOException, SQLException {
		printSelectLevel(
			out, codeDB, conditionMap_org, codeValues, null, null, startLevel, lastLevel, fullListingLevel, filterListingLevel
			, name, format, nullValueKey, multiple, mandatoryType, modified, style, styleId, styleClass
		);
	}

	public void printSelectLevel( JspWriter out, com.irt.sql.HierarchyCodeManager codeDB, Map<String, ? extends Object> conditionMap_org
						, String[] codeValues, String[] authorizedCodeValues, String[] unauthorizedCodeValues
						, int startLevel, int lastLevel, int fullListingLevel, int filterListingLevel
						, String name, RecordFormat format, String nullValueKey, boolean multiple
						, char mandatoryType, String modified, String style, String styleId, String styleClass ) throws IOException, SQLException {
		com.irt.sql.HierarchyCodeField codeField = codeDB.getCodeField();
		String codeFieldKey = codeField.getFieldKey();
		String classCodeFieldKey = codeField.getClassCodeField().getFieldKey();

		String[] fieldKeys = null;
		{
			Set<String> fieldKeySet = new java.util.HashSet<String>();
			fieldKeySet.add( codeFieldKey );
			format.addFieldKeyToSet( fieldKeySet );
			fieldKeySet.toArray( fieldKeys = new String[fieldKeySet.size()] );
		}

		if( startLevel <= 0 )
			startLevel += codeField.getLastLevel();
		if( startLevel <= 0 ) return;

		if( lastLevel <= 0 )
			lastLevel += codeField.getLastLevel();
		if( lastLevel > codeField.getLastLevel() )
			lastLevel = codeField.getLastLevel();
		if( lastLevel < startLevel ) return;

		codeValues = com.irt.sql.HierarchyCodeField.getAuthorizedCodeValues( codeValues, authorizedCodeValues, unauthorizedCodeValues );
		if( codeValues == null ) codeValues = authorizedCodeValues;

		int upperLevel = 0;
		String[] upperCodeValues = null;
		if( codeValues != null && codeValues.length > 0 ) {
			String commonCode = codeField.getCommonUpperLevelCode( codeValues );
			if( commonCode != null ) {
				upperCodeValues = codeField.getUpperLevelCodes( commonCode, true );
				upperLevel = ( upperCodeValues == null ? 0 : upperCodeValues.length );
			}
		}

		String onFocus = properties.getProperty( "jsp.script.multisel.focus" );
		String onBlur = properties.getProperty( "jsp.script.multisel.blur" );
		if( onFocus == null ) throw new IllegalArgumentException( "cannot find 'jsp.script.multisel.focus'." );
		if( onBlur == null ) throw new IllegalArgumentException( "cannot find 'jsp.script.multisel.blur'." );

		String attribute = getInputAttribute( HtmlPage.INPUT_SELECT, mandatoryType, modified, style, styleId, styleClass );

		String attribute_s = getInputAttribute( HtmlPage.INPUT_SELECT, mandatoryType, modified, style, styleId, styleClass );
		attribute_s += " onFocus='"+ onFocus +"'";

		String attribute_m = getInputAttribute(
			HtmlPage.INPUT_SELECT, mandatoryType, null
			, "position: absolute; display: none;"+ (style == null ? "" : style), styleId, styleClass
		);
		attribute_m += " onBlur='"+ onBlur +"' multiple";

		codeDB.appendSort( codeFieldKey );
		for( int level = startLevel; level <= lastLevel; level++ ) {
			Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();
			List<Map<String, Object>> optionList = null;

			String nullValue = null;
			if( nullValueKey != null )
				nullValue = msghandler.getMessage( java.text.MessageFormat.format(nullValueKey, String.valueOf(level)) );

			if( conditionMap_org != null ) conditionMap.putAll( conditionMap_org );
			conditionMap.put( classCodeFieldKey, String.valueOf(level) );
			if( level <= fullListingLevel )
				optionList = codeDB.getRecords( conditionMap, fieldKeys );
			else if( level - 2 < upperLevel ) {
				conditionMap.put( codeFieldKey, upperCodeValues[level - 2] );
				conditionMap.put( codeFieldKey + Condition.SUFFIX_TYPE, Condition.CONDTYPE_STARTSWITH );
				optionList = codeDB.getRecords( conditionMap, fieldKeys );
			} else if( level <= filterListingLevel ) {
				if( upperLevel > 0 ) {
					conditionMap.put( codeFieldKey, upperCodeValues[upperLevel - 1] );
					conditionMap.put( codeFieldKey + Condition.SUFFIX_TYPE, Condition.CONDTYPE_STARTSWITH );
				}
				optionList = codeDB.getRecords( conditionMap, fieldKeys );
			}

			if( authorizedCodeValues != null && optionList != null )
				for( java.util.Iterator<Map<String, Object>> iterator = optionList.iterator(); iterator.hasNext(); ) {
					boolean ok = false;
					String codeValue = (String)iterator.next().get( codeFieldKey );

					for( int i = 0; i < authorizedCodeValues.length; i++ )
						if( ok = (codeValue.startsWith(authorizedCodeValues[i]) || authorizedCodeValues[i].startsWith(codeValue)) )
							break;
					if( !ok ) iterator.remove();
				}

			if( unauthorizedCodeValues != null && optionList != null )
				for( java.util.Iterator<Map<String, Object>> iterator = optionList.iterator(); iterator.hasNext(); ) {
					String codeValue = (String)iterator.next().get( codeFieldKey );

					for( int i = 0; i < unauthorizedCodeValues.length; i++ )
						if( codeValue.startsWith(unauthorizedCodeValues[i]) ) {
							iterator.remove();
							break;
						}
				}

			if( lastLevel > fullListingLevel ) out.print( "<span id='content_"+ name + level +"'>" );
			if( multiple ) {
				String[][] optionValues = makeOptionValues( optionList, codeFieldKey, format );

				printSelectMultiple(
					out
					, name + level
					, ( level - 1 < upperLevel ? new String[] { upperCodeValues[level - 1] } : codeValues )
					, optionValues, nullValue, attribute_s, attribute_m
				);
			} else
				printSelect(
					out
					, name + level
					, ( level - 1 < upperLevel ? new String[] { upperCodeValues[level - 1] } : codeValues )
					, optionList, codeFieldKey, format
					, nullValue, attribute
				);
			if( lastLevel > fullListingLevel ) out.print( "</span>" );
			out.print( " " );
		}
	}
}
