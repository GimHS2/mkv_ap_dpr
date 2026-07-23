/*
 *	File Name:	HierarchyCodeLinkTag.java
 *	Version:	2.2.5
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2014/08/29		2.2.5	CrossBrowsing 적용: Mobile user agent의 경우 Hierarchy code link를 표시 안하도록 수정
 *	GimHS		2012/12/31		2.2.4	CrossBrowsing 적용:
 *										  -> JavaScript에서 event object를 사용하는 경우 바로 사용할수 없어서
 *										     a 태그에서 onClick event의 JavaScript:setEventObject(event) 함수를 이용하여 event를 저장하도록 변경
 *										  -> a 태그에서 level 속성을 지원하지 않아 tabIndex 속성으로 대체
 *	stghr12		2011/03/31		2.2.3	TagUtility.getValue() -> TagUtility.getValueOne()
 *	stghr12		2010/08/31		2.2.2	code(현재레벨코드), ucode(상위레벨코드) 설정추가
 *	GimHS		2010/07/10		2.2.1	condition 추가
 *	stghr12		2009/10/31		2.2.0	create(CategoryLevelTag -> HierarchyCodeLinkTag)
 *
**/

package com.irt.tagext;

import com.irt.data.Condition;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.html.HtmlPage;
import com.irt.util.MessageHandler;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * <ul type='square'>
 * <li>(O)classId
 * <li>(O)className
 * <li>(O)code
 * <li>(O)condition
 * <li>(M)format
 * <li>(O)id
 * <li>(O)key
 * <li>(O)lastLevel
 * <li>(O)listFormat
 * <li>(M)modified
 * <li>(M)name
 * <li>(O)nextCodeMessageKey
 * <li>(O)nullCodeMessageKey
 * <li>(O)separate
 * <li>(O)styleClass
 * <li>(O)suffix
 * </ul>
 */
public class HierarchyCodeLinkTag extends javax.servlet.jsp.tagext.TagSupport {
	String classId, className;
	String id, key, code, name;
	String nullCodeMessageKey, nextCodeMessageKey;
	String modified;
	String styleClass;
	String separate, suffix;
	RecordFormat format, listFormat;
	int lastLevel = 0;
	Map<String, Object> conditionMap = null;

	public int doEndTag() throws JspException {
		com.irt.sql.HierarchyCodeManager codeDB = (com.irt.sql.HierarchyCodeManager)TagUtility.getQueryableManager( pageContext, classId, className );
		if( codeDB == null ) return EVAL_PAGE;

		HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
		MessageHandler msghandler = htmlpage.getMessageHandler();

		if( htmlpage.isMobileAgent() ) return EVAL_PAGE;

		String separate = this.separate;
		if( separate == null )
			separate = htmlpage.getProperty().getProperty( "tags.HierarchyCodeLinkTag.separate" );

		String suffix = this.suffix;
		if( suffix == null )
			suffix = htmlpage.getProperty().getProperty( "tags.HierarchyCodeLinkTag.suffix" );

		com.irt.sql.HierarchyCodeField codeField = codeDB.getCodeField();
		String codeFieldKey = codeField.getFieldKey();
		String classCodeFieldKey = codeField.getClassCodeField().getFieldKey();

		int lastLevel = this.lastLevel;
		if( lastLevel <= 0 )
			lastLevel += codeField.getLastLevel();
		if( lastLevel > codeField.getLastLevel() )
			lastLevel = codeField.getLastLevel();
		if( lastLevel <= 0 ) return EVAL_PAGE;

		String code = this.code;
		if( code == null && id != null && key != null ) {
			Object object = TagUtility.getValueOne( pageContext, id, key );
			code = ( object != null ? object.toString() : null );
		}

		String[] codes = codeField.getUpperLevelCodes( code, true );
		if( codes == null && lastLevel > 1 )
			lastLevel = 1;
		else if( codes != null && lastLevel > codes.length + 1 )
			lastLevel = codes.length + 1;

		String[] fieldKeys = null;
		{
			Set<String> fieldKeySet = new java.util.HashSet<String>();
			fieldKeySet.add( codeFieldKey );
			format.addFieldKeyToSet( fieldKeySet );
			if( listFormat != null )
				listFormat.addFieldKeyToSet( fieldKeySet );
			fieldKeySet.toArray( fieldKeys = new String[fieldKeySet.size()] );
		}

		JspWriter out = pageContext.getOut();
		try {
			TagUtility utility = new TagUtility( htmlpage );

			String attribute = ( styleClass == null ? "" : " class='"+ styleClass +"'" );

			List<Map<String, Object>> recordList = null;
			if( codes != null ) {
				Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();
				if( this.conditionMap != null ) conditionMap.putAll( this.conditionMap );
				Condition.putConditionValueOnly( conditionMap, codeFieldKey, codes );

				codeDB.setSort( codeFieldKey );
				recordList = codeDB.getRecords( conditionMap, fieldKeys );
			}

			out.print( "<span "+ attribute +">" );
			for( int level = 1; level <= lastLevel; level++ ) {
				Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();
				List<Map<String, Object>> optionList;

				if( this.conditionMap != null ) conditionMap.putAll( this.conditionMap );
				conditionMap.put( classCodeFieldKey, String.valueOf(level) );
				if( codes != null && level > 1 && level-2 < codes.length ) {
					conditionMap.put( codeFieldKey, codes[level-2] );
					conditionMap.put( codeFieldKey + Condition.SUFFIX_TYPE, Condition.CONDTYPE_STARTSWITH );
				}
				optionList = codeDB.getRecords( conditionMap, fieldKeys );
				if( optionList == null ) break;

				String code1 = ( codes != null && level-1 < codes.length ? codes[level-1] : null );
				String code2 = ( codes != null && level > 1 && level-2 < codes.length ? codes[level-2] : null );
				utility.printSelect(
					out
					, name + level
					, code1
					, utility.makeOptionValues( optionList, codeFieldKey, listFormat != null ? listFormat : format )
					, "' style='display: none;'"
				);

				if( level == 1 && nullCodeMessageKey != null ) {
					out.print( "<a href='JavaScript:void(0);' onClick='JavaScript:setEventObject(event);"+ modified +"' tabIndex='0' code='' ucode='' "+ attribute +">" );
					out.print( msghandler.getMessage(nullCodeMessageKey) +"</a>" );
				}

				if( code1 == null ) code1 = "";
				if( code2 == null ) code2 = "";
				if( recordList != null && level-1 < recordList.size() ) {
					Map<String, Object> recordMap = recordList.get( level - 1 );

					out.print( separate );
					out.print( "<a href='JavaScript:void(0);' onClick='JavaScript:setEventObject(event);"+ modified +"'" );
					out.print( " tabIndex='"+ level +"' code='"+ code1 +"' ucode='"+ code2 +"' "+ attribute +">" );
					out.print( format.format(recordMap, msghandler) + suffix +"</a>" );
				} else {
					if( nextCodeMessageKey != null ) {
						out.print( separate );
						out.print( "<a href='JavaScript:void(0);' onClick='JavaScript:setEventObject(event);"+ modified +"'" );
						out.print( " tabIndex='"+ lastLevel +"' code='' ucode='"+ code1 +"' "+ attribute +">" );
						out.print( msghandler.getMessage(nextCodeMessageKey) + suffix +"</a>" );
					}
					break;
				}
			}
			out.print( "</span>" );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		} catch( java.sql.SQLException sqlEx ) {
			throw new JspException( sqlEx );
		}

		return EVAL_PAGE;
	}

	public void setClassId( String classId ) {
		this.classId = classId;
	}

	public void setClassName( String className ) {
		this.className = className;
	}

	public void setCode( Object code ) {
		if( code instanceof String )
			this.code = (String)code;
		else if( code instanceof Object[] && ((Object[])code).length > 0 && ((Object[])code)[0] instanceof String )
			this.code = (String)((Object[])code)[0];
	}

	public void setCondition( String conditionString ) {
		this.conditionMap = TagUtility.parseCondition( conditionString );
	}

	public void setFormat( String format ) {
		this.format = PatternRecordFormat.getInstance( format );
	}

	public void setId( String id ) {
		this.id = id;
	}

	public void setKey( String key ) {
		this.key = key;
	}

	public void setLastLevel( int lastLevel ) {
		this.lastLevel = lastLevel;
	}

	public void setListFormat( String listFormat ) {
		this.listFormat = PatternRecordFormat.getInstance( listFormat );
	}

	public void setModified( String modified ) {
		this.modified = modified;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public void setNextCodeMessageKey( String nextCodeMessageKey ) {
		this.nextCodeMessageKey = nextCodeMessageKey;
	}

	public void setNullCodeMessageKey( String nullCodeMessageKey ) {
		this.nullCodeMessageKey = nullCodeMessageKey;
	}

	public void setSeparate( String separate ) {
		this.separate = separate;
	}

	public void setStyleClass( String styleClass ) {
		this.styleClass = styleClass;
	}

	public void setSuffix( String suffix ) {
		this.suffix = suffix;
	}
}
