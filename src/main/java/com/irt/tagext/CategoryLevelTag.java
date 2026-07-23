/*
 *	File Name:	CategoryLevelTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	codeDB.getCodeField() -> findAttribute( "codeField" );
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.Condition;
import com.irt.data.Record;
import com.irt.data.QueryableManager;
import com.irt.data.format.PatternRecordFormat;
import com.irt.html.HtmlPage;
import com.irt.util.MessageHandler;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * <ul type='square'>
 * <li>(O)categoryCode
 * <li>(O)format
 * <li>(O)nextCodeMessageKey
 * <li>(O)nullCodeMessageKey
 * <li>(O)separateString
 * <li>(O)styleClass
 * <li>(O)suffixString
 * <li>(O)level
 * </ul>
 */
public class CategoryLevelTag extends javax.servlet.jsp.tagext.TagSupport {
	String categoryCode;
	String nullCodeMessageKey, nextCodeMessageKey;
	String styleClass;
	String separateString = "∥";
	String suffixString = " ▼";
	int printingLevel = 0;
	com.irt.data.format.RecordFormat format = PatternRecordFormat.getInstance( "$H{name}" );

	HtmlPage htmlpage;
	MessageHandler msghandler;

	public int doStartTag() throws JspException {
		htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
		msghandler = htmlpage.getMessageHandler();

		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {
		QueryableManager codeDB = (QueryableManager)pageContext.findAttribute( "codeDB" );
		if( codeDB == null ) return EVAL_PAGE;

		com.irt.sql.HierarchyCodeField codeField = (com.irt.sql.HierarchyCodeField)pageContext.findAttribute( "codeField" );
		String[] categoryCodes = codeField.getUpperLevelCodes( categoryCode, true );
		int printingLevel = ( this.printingLevel > 0 ? this.printingLevel : codeField.getLastLevel() + this.printingLevel );
		if( printingLevel == 0 ) return EVAL_PAGE;

		JspWriter out = pageContext.getOut();
		try {
			int lastLevel = -1;
			String attribute = ( styleClass == null ? "" : " class='"+ styleClass +"'" );

			out.print( "<span"+ attribute +">" );
			if( nullCodeMessageKey != null )
				out.print( "<a href='JavaScript:categoryLinkMenu();'"+ attribute +">"+ msghandler.getMessage(nullCodeMessageKey) +"</a>" );
			if( categoryCodes != null ) {
				int level = 1;

				codeDB.setSort( "code" );
				List recordList = codeDB.getRecords( Record.createMap("code", categoryCodes), new String[] { "code", "name", "levelCode" } );
				for( java.util.Iterator iterator = recordList.iterator(); iterator.hasNext(); level++) {
					Map recordMap = (Map)iterator.next();

					out.print( separateString );
					out.print( "<a href='JavaScript:categoryLinkMenu(\"_content_category_"+ level +"\");'"+ attribute +">" );
					out.print( format.format(recordMap, msghandler) + suffixString +"</a>" );
				}
				if( categoryCodes.length < printingLevel ) lastLevel = categoryCodes.length + 1;
			} else
				lastLevel = 1;
			if( lastLevel > 0 && nextCodeMessageKey != null ) {
				out.print( separateString );
				out.print( "<a href='JavaScript:categoryLinkMenu(\"_content_category_"+ lastLevel +"\");'"+ attribute +">" );
				out.print( msghandler.getMessage(nextCodeMessageKey) + suffixString +"</a>" );
			}
			out.print( "</span>" );

			List<? extends Map> masterList;
			if( categoryCodes != null ) {
				Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();

				for( int i = 0; i <= categoryCodes.length; i++ ) {
					conditionMap.put( "classCode", String.valueOf(i+1) );
					if( i > 0 ) {
						conditionMap.put( "code", categoryCodes[i-1] );
						conditionMap.put( "code"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_STARTSWITH );
					}

					masterList = codeDB.getRecords( conditionMap, new String[] { "code", "name", "levelCode" } );
					if( masterList == null ) break;

					printSelect( out, masterList, (i+1), i < categoryCodes.length ? categoryCodes[i] : null );
				}
			} else {
				masterList = codeDB.getRecords( Record.createMap("classCode", "1"), new String[] { "code", "name", "levelCode" } );
				if( masterList != null ) printSelect( out, masterList, 1, null );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		} catch( java.sql.SQLException sqlEx ) {
			throw new JspException( sqlEx );
		}

		return EVAL_PAGE;
	}

	void printSelect( JspWriter out, List<? extends Map> masterList, int level, String categoryCode ) throws java.io.IOException {
		out.print( "<select name='_categoryCode_"+ level +"' id='_content_category_"+ level +"' style='display: none'>" );
		for( java.util.Iterator<? extends Map> iterator = masterList.iterator(); iterator.hasNext(); ) {
			Map recordMap = iterator.next();

			Object code = recordMap.get( "code" );
			if( code.equals(categoryCode) )
				out.print( "<option value='"+ code +"' selected>" );
			else
				out.print( "<option value='"+ code +"'>" );
			out.print( format.format(recordMap, msghandler) );
			out.print( "</option>" );
		}
		out.print( "</select>" );
	}

	public void setCategoryCode( String categoryCode ) {
		this.categoryCode = categoryCode;
	}

	public void setFormat( String format ) {
		this.format = PatternRecordFormat.getInstance( format );
	}

	public void setLevel( int level ) {
		this.printingLevel = level;
	}

	public void setNextCodeMessageKey( String nextCodeMessageKey ) {
		this.nextCodeMessageKey = nextCodeMessageKey;
	}

	public void setNullCodeMessageKey( String nullCodeMessageKey ) {
		this.nullCodeMessageKey = nullCodeMessageKey;
	}

	public void setSeparateString( String separateString ) {
		this.separateString = separateString;
	}

	public void setStyleClass( String styleClass ) {
		this.styleClass = styleClass;
	}

	public void setSuffixString( String suffixString ) {
		this.suffixString = suffixString;
	}
}
