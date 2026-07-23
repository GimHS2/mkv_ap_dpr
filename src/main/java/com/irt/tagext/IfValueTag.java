/*
 *	File Name:	IfValueTag.java
 *	Version:	2.1.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2017/02/28		2.1.1	notValue, notValues 추가
 *	stghr12		2007/10/31		2.1.0	create
 *
**/

package com.irt.tagext;

import java.util.Map;
import javax.servlet.jsp.JspException;

/**
 * Map에 Key의 값이 value와 같을 경우 body 출력.
 *
 * <ul type='square'>
 * <li>(M)id
 * <li>(M)key
 * </ul>
 * <ul type='square'>
 * <li>(O)value
 * </ul>
 * <ul type='square'>
 * <li>(O)valueList
 * </ul>
 * <ul type='square'>
 * <li>(O)notValue
 * </ul>
 * <ul type='square'>
 * <li>(O)notValueList
 * </ul>
 *
 */
public class IfValueTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	String id, key, value, notValue;
	String[] values, notValues;

	@Override
	public int doStartTag() throws JspException {
		Map map = TagUtility.getMap( pageContext, id );

		if( map != null ) {
			Object object = map.get( key );
			if( value != null || values != null ) {
				if( values == null ) {
					if( object == null ) {
						if( "".equals(value) ) return EVAL_BODY_INCLUDE;
					} else if( value.equals(object.toString()) )
						return EVAL_BODY_INCLUDE;
				} else{
					for( String value : values ) {
						if( object == null ) {
							if( "".equals(value) ) return EVAL_BODY_INCLUDE;
						} else if( value.equals(object.toString()) )
							return EVAL_BODY_INCLUDE;
					}
				}
			}

			if( notValue != null || notValues != null ) {
				if( notValues == null ) {
					if( object == null ) {
						if( !"".equals(notValue) ) return EVAL_BODY_INCLUDE;
					} else if( !notValue.equals(object.toString()) )
						return EVAL_BODY_INCLUDE;
				} else{
					for( String notValue : notValues ) {
						if( object == null ) {
							if( !"".equals(notValue) ) return EVAL_BODY_INCLUDE;
						} else if( notValue.equals(object.toString()) )
							return EVAL_BODY_INCLUDE;
					}
				}
			}
		}

		return SKIP_BODY;
	}

	@Override
	public void setId( String id ) {
		this.id = id;
	}

	public void setKey( String key ) {
		this.key = key;
	}

	public void setValue( String value ) {
		this.value = value;
	}

	public void setValueList( String valueList ) {
		this.values = valueList.split( "," );
	}

	public void setNotValue( String notValue ) {
		this.notValue = notValue;
	}

	public void setNotValueList( String notValueList ) {
		this.notValues = notValueList.split( "," );
	}
}
