/*
 *	File Name:	DataLoaderValidators.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.dpr.util;

import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.cols.Column;
import com.irt.sql.SQLHandler;
import com.irt.util.StringUtil;

import java.sql.SQLException;
import java.util.Map;

/**
 * defined {@link com.irt.data.DataLoader.Validator}
 */
public class DataLoaderValidators {

	/**
	 * check ScientificNotation
	 *
	 *	if columnResource's columnAttr value has " type='number' "
	 * <pre>
	 * ....
	 * 	columnName		column Display value	Y	width='100' type='number'
	 * ...
	 * </pre>
	 *
	 *
	 * @param columns
	 * @return
	 */
	public static DataLoader.Validator getScientificNotationValidator( final Column[] columns ) {
		DataLoader.Validator validator = new DataLoader.Validator() {

			void throwOnSientificNotation( SQLHandler handler, Map<String, Object> recordMap, Column column ) throws DataException {
				if( recordMap.containsKey(column.getFieldKey()) ) {
					Object value = recordMap.get(column.getFieldKey());
					if( StringUtil.isScientificNotation((String)value) ) {
						String errMessage = handler.getMessageHandler().getMessage("ERR_INVALID_VALUE", //
								(String)column.getColumnTitle(), (String)value);
						throw handler.createDataException("ERR_INVALID_VALUE", errMessage);
					}
				}
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				for( Column column : columns ) {
					String type = StringUtil.extractAttrValue((String)column.getColumnAttr(), "type");
					if( "number".equals(type) ) {
						throwOnSientificNotation(handler, recordMap, column);
					}
				}
			}

			@Override
			public void close() {
			}
		};
		return validator;
	}

}
