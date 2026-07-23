/*
 *	File Name:	AbstractFieldWrapper.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	RecordFormat 변경사항 적용: format( recordMap, msghandler, stringBuffer ) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getValidDateFormat() 삭제
 *	stghr12		2007/10/31		2.1.1	AbstractFieldWrapper(): 생성자 오류 수정
 *	stghr12		2006/12/01		2.1.0	addFieldKeyToSet(), format() 추가
 *										dataType을 int형에서 char형으로 변경
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class AbstractFieldWrapper implements AbstractField {
	AbstractField field;

	public AbstractFieldWrapper( AbstractField field ) {
		this.field = field;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		field.addFieldKeyToSet( fieldKeySet );
	}

	public String format( Map recordMap, com.irt.util.MessageHandler msghandler ) {
		return field.format( recordMap, msghandler );
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return field.format( recordMap, msghandler, stringBuffer );
	}

	public char getDataType() {
		return field.getDataType();
	}

	public String getDescriptionKey() {
		return field.getDescriptionKey();
	}

	public String getFieldKey() {
		return field.getFieldKey();
	}

	public String getPrefixKey() {
		return field.getPrefixKey();
	}

	public AbstractField getSourceField() {
		return field;
	}
}
