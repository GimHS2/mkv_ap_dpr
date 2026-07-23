/*
 *	File Name:	SelectInputTag.java
 *	Version:	2.2.16c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/07/31		2.2.16c	doEndTag() : HtmlUtility.toHtmlString으로 XSS 공격 방지 기능 추가
 *	hankalam	2021/11/30		2.2.15c	신규 UI/UX 적용
 *	soma		2020/06/30		2.2.15	nullValueKey를 가져올때 toHtmlString() 사용하지 않도록 변경
 *	GimHS		2020/03/31		2.2.14	v2.2.10 내용 삭제
 *	GimHS		2019/08/30		2.2.13	종료 태그(</>) 이후에 줄바꿈문자 출력
 *	GimHS		2019/07/31		2.2.12	multipleSelect의 width 속성 삭제(jsp에서 style로 처리)
 *	GimHS		2017/06/30		2.2.11	multi select 버튼의 maxHeight를 100->200 변경
 *	GimHS		2016/08/31		2.2.10	value값을 HtmlUtility.toHtmlString()으로 변환
 *	GimHS		2015/04/30		2.2.9	CrossBrowsing 적용: v2.2.8 오류 수정
 *	GimHS		2014/08/29		2.2.8	CrossBrowsing 적용: multiple 이고, 최신 Browser 혹은 Mobile인 경우 jQuery를 이용하여 Select 화면 표시
 *	yjcha		2011/08/31		2.2.7	NullPointerException 오류 수정
 *	stghr12		2010/05/31		2.2.6	prefix/codeValues(1), listId(2), classId/className(3)를 동시에 표시할 수 있도록 수정
 *	stghr12		2010/03/31		2.2.5	multiple 지원
 *	stghr12		2009/12/31		2.2.4	condition 추가
 *	stghr12		2009/10/31		2.2.3	TagUtility 사용
 *										nameFormat, nameKey, listNameKey 삭제
 *										classId, className, listExtraFormat 추가
 *	stghr12		2008/11/28		2.2.2	getDescription(): nameFormat처리 후 HtmlUtility.toHtmlString() 하는 부분 제거
 *	stghr12		2008/03/31		2.2.1	PatternRecordFormat -> RecordFormat
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.1	listCodeFormat 추가
 *										getDescription(): INPUT_INFORMATION에서만 format사용
 *	stghr12		2006/12/01		2.1.0	ignoreNotListedValue, listNameFormat, nameFormat 추가
 *										doEndTag():
 *											READONLY일 때도 extraValue 추가
 *											prefixKey가 null일 경우 처리
 *										getDescription():
 *											format 사용, listId가 null일 때에도 nameKey 사용
 *											prefixKey가 null일 경우 처리
 *										getOptionValues():
 *											codeValues의 값이 빈문자일 때 msghandler.getMessageValue(prefixKey)를 우선 사용
 *											codeValues의 값이 ALL일 때 msghandler.getMessageValue(prefixKey + "ALL")를 우선 사용
 *											prefixKey가 null일 경우 처리
 *	stghr12		2006/11/30		2.0.2	value, description를 잘못 처리하는 오류 수정
 *	stghr12		2006/09/15		2.0.1	TAG안에 value에 대해서 HtmlUtility.toScriptString() -> HtmlUtility.toHtmlString()
 *	stghr12		2006/02/28		2.0.0	version up(SelectInputTag, CodeSelectInputTag, ListSelectInputTag로 분리)
 *	stghr12		2004/02/29		1.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.format.PatternRecordFormat;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="select"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)defaultValue
 * <li>(O)format
 * <li>(O)hasBlank
 * <li>(O)hasPlaceholder
 * <li>(O)id
 * <li>(O)ignoreNotListedValue
 * <li>(M)key
 * <li>(O)mandatory
 * <li>(O)modified
 * <li>(O)multiple
 * <li>(O)name
 * <li>(O)nullValue | nullValueKey
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)placeholder
 * <li>(O)readonly
 * <li>(O)searchable
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * <li>(O)uniqId
 * <li>(O)width
 * </ul>
 * <ul type='square'>
 * <li>(O)codeValues
 * <li>(O)prefixKey
 * </ul>
 * <ul type='square'>
 * <li>(O)classId
 * <li>(O)className
 * <li>(O)condition
 * <li>(O)listCodeFormat
 * <li>(O)listCodeKey
 * <li>(O)listExtraFormat
 * <li>(O)listExtraKey
 * <li>(M)listId
 * <li>(O)listNameFormat
 * </ul>
 */
public class SelectInputTag extends InputTag {
	String uniqId;
	String classId, className;
	boolean hasBlank, ignoreNotListedValue = false, hasPlaceholder = false, searchable = true;
	boolean multiple = false;
	boolean customSelect = true;
	String nullValue, nullValueKey, placeholder;
	String width, customOption;
	Map<String, Object> conditionMap;

	String[] codeValues;
	String prefixKey;

	String listId;
	String listCodeKey = "code";
	String listExtraKey = null;
	com.irt.data.format.RecordFormat listCodeFormat, listNameFormat, listExtraFormat;

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			Object value = getValue();
			String strvalue = null;

			if( value != null ) {
				Object object = value;
				if( object instanceof Object[] )
					object = ((Object[])object)[0];
				if( object != null ) strvalue = object.toString();
			}
			if( !onlyInput && mandatoryType == INPUT_INFORMATION ) {
				if( format != null )
					out.print( getFormatValue() );
				else {
					String[][] optionValues = getOptionValues( strvalue, true, false );
					if( optionValues != null ) out.print( optionValues[0][1] );
				}
			} else {
				String name = getName();
				String uniqId = this.uniqId;
				if( uniqId == null ) {
					uniqId = name;
				}
				String attribute = getAttribute( HtmlPage.INPUT_SELECT );

				if( mandatoryType == INPUT_READONLY ) {
					String[][] optionValues = getOptionValues( strvalue, true, ignoreNotListedValue );
					if( customSelect ) {
						String attribute_s = "";
						if( this.mandatory ) {
							attribute_s = " class='mandatory'";
						}
						out.print(  "<select id='"+ uniqId +"' name='"+ name +"'" + attribute_s + ">" );
						if( utility.printOptions(out, strvalue, optionValues) == 0 ) {
							if( strvalue != null && !ignoreNotListedValue ) {
								strvalue = HtmlUtility.toHtmlString( strvalue );
								out.print( "<option value='"+ strvalue +"' selected>"+ getOptionValues(strvalue, false, false)[0][1] +"</option>" );
							}
						}
						out.println( "</select>" );

						printSelectScript( out );
					} else {
						utility.printSelect( out, name, strvalue, optionValues, attribute );
					}
				} else {
					String[][] optionValues = getOptionValues();

					if( multiple ) {
						String nullValue = null;
						if( hasBlank ) {
							nullValue = optionValues[0][1];
							if( optionValues.length == 1 )
								optionValues = null;
							else {
								String[][] optionValues_new = new String[optionValues.length - 1][3];
								System.arraycopy( optionValues, 1, optionValues_new, 0, optionValues_new.length );
								optionValues = optionValues_new;
							}
						}

						if( htmlpage.isOldBrowserIE() ) {
							String onFocus = htmlpage.getProperty().getProperty( "jsp.script.multisel.focus" );
							String onBlur = htmlpage.getProperty().getProperty( "jsp.script.multisel.blur" );
							if( onFocus == null ) throw new IllegalArgumentException( "cannot find 'jsp.script.multisel.focus'." );
							if( onBlur == null ) throw new IllegalArgumentException( "cannot find 'jsp.script.multisel.blur'." );

							String attribute_s = utility.getInputAttribute( HtmlPage.INPUT_SELECT, mandatoryType, modified, style, styleId, styleClass );
							attribute_s += " onFocus='"+ onFocus +"'";

							String attribute_m = utility.getInputAttribute(
								HtmlPage.INPUT_SELECT, mandatoryType, null
								, "position: absolute; display: none;"+ (style == null ? "" : style), styleId, styleClass
							);
							attribute_m += " onBlur='"+ onBlur +"' multiple";

							utility.printSelectMultiple( out, name, value, optionValues, nullValue, attribute_s, attribute_m );
						} else {
							String attribute_s = utility.getInputAttribute( HtmlPage.INPUT_SELECT, mandatoryType, null, style, styleId, styleClass );
							String attribute_m = modified;

							out.print( "<select id='"+ uniqId +"' name='"+ name +"' "+ attribute_s +" multiple>" );
							if( utility.printOptions(out, value, optionValues) == 0 ) {
								if( value != null && !ignoreNotListedValue ) {
									Object[] values = (Object[])( value instanceof Object[] ? value : null );
									for( int i = 0; values != null && i < values.length; i++ ) {
										String tmp = HtmlUtility.toHtmlString( (String)values[i] );
										out.print( "<option value='"+ tmp +"' selected>"+ getOptionValues(tmp, false, false)[0][1] +"</option>" );
									}
								}
							}
							out.println( "</select>" );

							out.print( "<script>" );
							out.print( "	$(function() {" );
							out.print( "		$('#"+ uniqId +"').multipleSelect({" );
							if( nullValue != null )
								out.print( "			placeholder: \""+ nullValue +"\"" );
							out.print( "			, maxHeight: 200" );
							out.print( "			, filter: true" );
							if( attribute_m != null )
								out.print( "	, onClose: function() {  var obj = frmCond."+ name +"; "+ attribute_m.replace("this", "obj") +" }" );
							out.print( "		});" );
							out.print( "	});" );
							out.println( "</script>" );
						}
					} else {
						String attribute_s = "";
						if( customSelect ) {
							if( this.mandatory ) {
								attribute_s = " class='mandatory'";
							}
						} else {
							attribute_s = attribute;
						}
						out.print(  "<select id='"+ uniqId +"' name='"+ name +"'" + attribute_s + ">" );
						if( utility.printOptions(out, strvalue, optionValues) == 0 ) {
							if( strvalue != null && !ignoreNotListedValue ) {
								strvalue = HtmlUtility.toHtmlString( strvalue );
								out.print( "<option value='"+ strvalue +"' selected>"+ getOptionValues(strvalue, false, false)[0][1] +"</option>" );
							}
						}
						out.println( "</select>" );
						if( customSelect ) {
							printSelectScript( out );
						}
						if( htmlpage.getContentGroup().autoValidation )
							utility.putValidationScript( field, mandatoryType, htmlpage.getFormName() +"."+ name );
					}
				}
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		} catch( java.sql.SQLException sqlEx ) {
			throw new JspException( sqlEx );
		}

		return EVAL_PAGE;
	}

	String[][] getOptionValues() throws JspException, java.sql.SQLException {
		String nullValue = null;
		if( hasBlank ) {
			if( this.nullValue != null )
				nullValue = this.nullValue;
			else if( nullValueKey != null )
				nullValue = msghandler.getMessage(nullValueKey);
			else
				nullValue = "";
		}

		String[][][] optionValues = new String[3][][];

		if( this.codeValues != null ) {
			String prefixKey = ( this.prefixKey == null && field != null ? field.getPrefixKey() : this.prefixKey );
			optionValues[0] = utility.makeOptionValues( prefixKey, this.codeValues, nullValue );
		}

		if( listId != null ) {
			Collection<? extends Map> optionList = (Collection<? extends Map>)pageContext.findAttribute( listId );
			if( listNameFormat == null ) throw new IllegalArgumentException( "listNameFormat cannot be null." );

			optionValues[1] = utility.makeOptionValues(
				optionList
				, listCodeFormat != null ? listCodeFormat : listCodeKey
				, listNameFormat
				, listExtraFormat != null ? listExtraFormat : listExtraKey
				, nullValue
			);
		}

		if( classId != null || className != null ) {
			com.irt.data.QueryableManager db = TagUtility.getQueryableManager( pageContext, classId, className );
			if( db == null ) return null;

			Set<String> fieldKeySet = new java.util.HashSet<String>();
			if( listCodeFormat != null )
				listCodeFormat.addFieldKeyToSet( fieldKeySet );
			else
				fieldKeySet.add( listCodeKey );

			com.irt.data.format.RecordFormat listNameFormat = this.listNameFormat;
			if( listNameFormat != null )
				listNameFormat.addFieldKeyToSet( fieldKeySet );
			else {
				String formatValue = htmlpage.getProperty().getProperty( "Select."+ db.getClass().getCanonicalName() +".format" );
				if( formatValue == null )
					throw new IllegalArgumentException( "cannot find 'Select."+ db.getClass().getCanonicalName() +".format'." );
				listNameFormat = PatternRecordFormat.getInstance( formatValue );
			}
			listNameFormat.addFieldKeyToSet( fieldKeySet );

			if( listExtraFormat != null )
				listExtraFormat.addFieldKeyToSet( fieldKeySet );
			else
				fieldKeySet.add( listExtraKey );

			String sortKeyValue = htmlpage.getProperty().getProperty( "Select."+ db.getClass().getCanonicalName() +".sort" );
			if( sortKeyValue != null ) db.setSort( sortKeyValue.split(",") );

			String[] fieldKeys = fieldKeySet.toArray( new String[fieldKeySet.size()] );

			Collection<? extends Map> optionList = db.getRecords( conditionMap, fieldKeys );

			optionValues[2] = utility.makeOptionValues(
				optionList
				, listCodeFormat != null ? listCodeFormat : listCodeKey
				, listNameFormat
				, listExtraFormat != null ? listExtraFormat : listExtraKey
				, nullValue
			);
		}

		for( int i = 1; i < optionValues.length; i++ ) {
			if( optionValues[0] == null )
				optionValues[0] = optionValues[i];
			else if( optionValues[i] != null && optionValues[i].length > (nullValue == null ? 0 : 1) ) {
				int startIndex = ( nullValue == null ? 0 : 1 );
				String[][] optionValues_tmp = optionValues[0];

				optionValues_tmp = new String[ optionValues[0].length + optionValues[i].length - startIndex ][];
				System.arraycopy( optionValues[0], 0, optionValues_tmp, 0, optionValues[0].length );
				System.arraycopy( optionValues[i], startIndex, optionValues_tmp, optionValues[0].length, optionValues[i].length - startIndex );

				optionValues[0] = optionValues_tmp;
			}
		}

		if( optionValues[0] == null && field != null && field.getPrefixKey() != null && field.getValidValues() != null )
			return utility.makeOptionValues( field.getPrefixKey(), field.getValidValues(), nullValue );
		else
			return optionValues[0];
	}

	String[][] getOptionValues( Object value, boolean usingList, boolean ignoreNotListedValue ) throws JspException, java.sql.SQLException {
		String[][] optionValues = new String[1][3];

		if( value == null ) {
			optionValues[0][0] = "";
			if( nullValue != null )
				optionValues[0][1] = nullValue;
			else if( nullValueKey != null )
				optionValues[0][1] = msghandler.getMessage( nullValueKey );
			else
				optionValues[0][1] = "";

			return optionValues;
		}

		while( usingList ) {
			if( classId == null && className == null && listId == null && !ignoreNotListedValue ) break;

			String[][] optionValues_tmp = getOptionValues();
			if( optionValues_tmp != null ) {
				for( int i = 0; i < optionValues_tmp.length; i++ )
					if( optionValues_tmp[i][0].equals(value) ) {
						optionValues[0][0] = optionValues_tmp[i][0];
						optionValues[0][1] = optionValues_tmp[i][1];
						if( optionValues_tmp[i].length > 2 )
							optionValues[0][2] = optionValues_tmp[i][2];

						return optionValues;
					}
			}
			break;
		}

		if( !ignoreNotListedValue ) {
			optionValues[0][0] = value.toString();
			if( classId != null || className != null || listId != null )
				optionValues[0][1] = value.toString();
				if( format != null )
					optionValues[0][1] = getFormatValue();
			else {
				String prefixKey = ( this.prefixKey == null && field != null ? field.getPrefixKey() : this.prefixKey );
				optionValues[0][1] = ( prefixKey == null ? value.toString() : msghandler.getMessage( prefixKey + value ) );
			}

			return optionValues;
		}

		return null;
	}

	private void printSelectScript( JspWriter out ) throws IOException {
		String name = getName();
		String uniqId = this.uniqId;
		if( uniqId == null ) {
			uniqId = name;
		}

		String customOptions = "selectmenuOptions";
		if( this.customOption != null ) {
			customOptions = this.customOption;
		}
		String extendOptions = "";
		if( this.hasPlaceholder ) {
			String message;
			if( this.placeholder != null && this.placeholder.length() > 0 ) {
				message = msghandler.getMessage( this.placeholder );
			} else {
				message = msghandler.getMessage( "jsp.MSG_SELECT_PLACEHOLDER" );
			}
			extendOptions += ", placeholder: \"" + message + "\"";
		}
		if( this.searchable ) {
			extendOptions += ", searchable: \"" + this.searchable + "\"";
		}
		out.print( "<script>" );
		out.print( "	$( function() {" );
		if( extendOptions.length() > 0 ) {
			out.print( "		var options = $.extend( {}, " + customOptions + ", { " + extendOptions.substring(2) + " } );" );
		} else {
			out.print( "		var options = " + customOptions + ";" );
		}
		out.print( "		$('#"+ uniqId +"').singleSelectmenu( options );" );
		if( this.modified != null ) {
			out.print( "		$('#"+ uniqId +"').on( 'singleselectmenuselect', function(event, ui) {" );
			out.print( "			" + modified );
			out.print( "		});" );
		}
		if( this.width != null ) {
			out.print( "		$('#"+ uniqId +"').singleSelectmenu( \"option\", \"width\", \"" + this.width + "\" );" );
		}
		out.print( "	});" );
		out.println( "</script>" );
	}

	public void setClassId( String classId ) {
		this.classId = classId;
	}

	public void setClassName( String className ) {
		this.className = className;
	}

	public void setCodeValues( String codeValues ) {
		this.codeValues = codeValues.split( "," );
	}

	public void setCustomSelect( boolean customSelect ) {
		this.customSelect = customSelect;
	}

	public void setCondition( String conditionString ) {
		this.conditionMap = TagUtility.parseCondition( conditionString );
	}

	public void setCustomOption( String customOption ) {
		this.customOption = customOption;
	}

	public void setHasBlank( boolean hasBlank ) {
		this.hasBlank = hasBlank;
	}

	public void setHasPlaceholder( boolean hasPlaceholder ) {
		this.hasPlaceholder = hasPlaceholder;
	}

	public void setIgnoreNotListedValue( boolean ignoreNotListedValue ) {
		this.ignoreNotListedValue = ignoreNotListedValue;
	}

	public void setListCodeFormat( String listCodeFormat ) {
		this.listCodeFormat = PatternRecordFormat.getInstance( listCodeFormat );
	}

	public void setListCodeKey( String listCodeKey ) {
		this.listCodeKey = listCodeKey;
	}

	public void setListExtraFormat( String listExtraFormat ) {
		this.listExtraFormat = PatternRecordFormat.getInstance( listExtraFormat );
	}

	public void setListExtraKey( String listExtraKey ) {
		this.listExtraKey = listExtraKey;
	}

	public void setListId( String listId ) {
		this.listId = listId;
	}

	public void setListNameFormat( String listNameFormat ) {
		this.listNameFormat = PatternRecordFormat.getInstance( listNameFormat );
	}

	public void setMultiple( boolean multiple ) {
		this.multiple = multiple;
	}

	public void setNullValue( String nullValue ) {
		this.nullValue = nullValue;
	}

	public void setNullValueKey( String nullValueKey ) {
		this.nullValueKey = nullValueKey;
	}

	public void setPrefixKey( String prefixKey ) {
		this.prefixKey = prefixKey;
	}

	public void setPlaceholder( String placeholder ) {
		this.placeholder = placeholder;
	}

	public void setSearchable( boolean searchable ) {
		this.searchable = searchable;
	}

	public void setUniqId( String uniqId ) {
		this.uniqId = uniqId;
	}

	public void setWidth( String width ) {
		this.width = width;
	}
}
