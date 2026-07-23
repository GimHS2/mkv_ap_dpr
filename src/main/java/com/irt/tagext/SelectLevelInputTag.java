/*
 *	File Name:	SelectLevelInputTag.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/07/31		2.2.2	printSelectLevel(): authorizedCodeValues, unauthorizedCodeValues 지원
 *	stghr12		2009/12/31		2.2.1	condition 추가
 *	stghr12		2009/10/31		2.2.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import com.irt.data.format.RecordFormat;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * &lt;input type="select"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)classId
 * <li>(O)className
 * <li>(O)condition
 * <li>(O)defaultValue
 * <li>(O)format
 * <li>(O)id
 * <li>(M)key
 * <li>(O)lastLevel
 * <li>(O)mandatory
 * <li>(O)modified
 * <li>(O)multiple
 * <li>(O)name
 * <li>(O)nullValueKey
 * <li>(O)startLevel
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * </ul>
 */
public class SelectLevelInputTag extends InputTag {
	String classId, className;
	int startLevel = 1, lastLevel = 0;
	boolean multiple = false;
	String nullValueKey;
	Map<String, Object> conditionMap = null;

	public int doEndTag() throws JspException {
		com.irt.sql.HierarchyCodeManager codeDB = (com.irt.sql.HierarchyCodeManager)TagUtility.getQueryableManager( pageContext, classId, className );
		if( codeDB == null ) return EVAL_PAGE;

		Object value = getValue();
		String[] codeValues = null;
		if( value instanceof String )
			codeValues = new String[] { (String)value };
		else if( value instanceof String[] ) {
			if( multiple )
				codeValues = (String[])value;
			else
				codeValues = new String[] { ((String[])value)[0] };
		}

		JspWriter out = pageContext.getOut();
		try {
			printSelectLevel(
				out, utility, codeDB, conditionMap, codeValues, startLevel, lastLevel
				, getName(), format, nullValueKey, multiple, mandatoryType, modified, style, styleId, styleClass
			);
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		} catch( java.sql.SQLException sqlEx ) {
			throw new JspException( sqlEx );
		}
		return EVAL_PAGE;
	}

	public static void printSelectLevel( JspWriter out, TagUtility utility, com.irt.sql.HierarchyCodeManager codeDB, Map<String, Object> conditionMap
			, String[] codeValues, int startLevel, int lastLevel, String name, RecordFormat format, String nullValueKey, boolean multiple
			, char mandatoryType, String modified, String style, String styleId, String styleClass ) throws IOException, SQLException {
		printSelectLevel(
			out, utility, codeDB, conditionMap, codeValues, null, null, startLevel, lastLevel, name, format, nullValueKey, multiple
			, mandatoryType, modified, style, styleId, styleClass
		);
	}

	public static void printSelectLevel( JspWriter out, TagUtility utility, com.irt.sql.HierarchyCodeManager codeDB, Map<String, Object> conditionMap
			, String[] codeValues, String[] authorizedCodeValues, String[] unauthorizedCodeValues, int startLevel, int lastLevel
			, String name, RecordFormat format, String nullValueKey, boolean multiple
			, char mandatoryType, String modified, String style, String styleId, String styleClass ) throws IOException, SQLException {
		Properties properties = utility.htmlpage.getProperty();
		String propertyName = "Select."+ codeDB.getClass().getCanonicalName();

		int fullListingLevel = 1;
		try {
			fullListingLevel = Integer.parseInt( properties.getProperty(propertyName +".fullListingLevel", String.valueOf(fullListingLevel)) );
			if( fullListingLevel <= 0 )
				fullListingLevel += codeDB.getCodeField().getLastLevel();
		} catch( NumberFormatException numEx ) {}

		int filterListingLevel = 0;
		try {
			filterListingLevel = Integer.parseInt( properties.getProperty(propertyName +".filterListingLevel", String.valueOf(filterListingLevel)) );
			if( filterListingLevel <= 0 )
				filterListingLevel += codeDB.getCodeField().getLastLevel();
		} catch( NumberFormatException numEx ) {}

		if( format == null )
			format = com.irt.data.format.PatternRecordFormat.getInstance( properties.getProperty(propertyName +".format", "$H{name}") );

		if( nullValueKey == null )
			nullValueKey = properties.getProperty( propertyName +".nullValueKey" );

		utility.printSelectLevel(
			out, codeDB, conditionMap, codeValues, authorizedCodeValues, unauthorizedCodeValues
			, startLevel, lastLevel, fullListingLevel, filterListingLevel
			, name, format, nullValueKey, multiple, mandatoryType, modified, style, styleId, styleClass
		);
	}

	public void setClassId( String classId ) {
		this.classId = classId;
	}

	public void setClassName( String className ) {
		this.className = className;
	}

	public void setCondition( String conditionString ) {
		this.conditionMap = TagUtility.parseCondition( conditionString );
	}

	public void setLastLevel( int lastLevel ) {
		this.lastLevel = lastLevel;
	}

	public void setMultiple( boolean multiple ) {
		this.multiple = multiple;
	}

	public void setNullValueKey( String nullValueKey ) {
		this.nullValueKey = nullValueKey;
	}

	public void setStartLevel( int startLevel ) {
		this.startLevel = startLevel;
	}
}
