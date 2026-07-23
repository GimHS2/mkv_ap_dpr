/*
 *	File Name:	JqdtConfigHelper.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.json;

import com.google.gson.Gson;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.sql.Table;
import com.irt.sql.Table.Field;
import com.irt.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class JqdtConfigHelper {

	public static String MOMENTJS_MILLIS_TO_DATETIMEOFBROWSER = "jQuery.fn.dataTable.render.moment(\'x\', \'YYYY-MM-DD HH:mm:ss\')";

	public static List<Map<String, Object>> getColumns( String[] fieldKeys, String[] fieldTitles ) {
		List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();

		for( int i = 0; i < fieldKeys.length; i++ ) {
			String fieldKey = fieldKeys[i];
			Map<String, Object> column = new HashMap<String, Object>();

			column.put("name", fieldKey);

			if( fieldKey.endsWith("DateTime") ) {
				column.put("data", fieldKey + ".millis");// Gson json formatt for java.sql.Timestamp(com.irt.data.Timestamp)
			} else {
				column.put("data", fieldKey);
			}

			if( fieldTitles != null && fieldTitles[i] != null ) {
				column.put("title", fieldTitles[i]);
			}

			columns.add(column);
		}

		return columns;
	}

	public Map<String, Object> getConfigMap$Buttons( Map<String, Object> jqdtConfig, String[] buttons ) {
		Map<String, Object> map = Record.createMap("tsvButtonString", Button.TSV_BUTTON_STRING);

		List<String> existing = new ArrayList<String>((List<String>)jqdtConfig.get("buttons"));
		for( String btn : buttons ) {
			String evaluated = PatternRecordFormat.getInstance(btn).format(map, null);
			// String jsEscaped = HtmlUtility.toScriptString(evaluated);
			String jsEscaped = "" + evaluated + "";
			if( !existing.contains(jsEscaped) ) {
				existing.add(jsEscaped);
			}
		}
		jqdtConfig.put("buttons", existing);

		return jqdtConfig;
	}

	// public Map<String[], String> getCustomConfigMap( String[] keyspec, String jsString ) {
	//
	// }

	public Map<String, Object> getConfigMap$Select( Map<String, Object> jqdtConfig ) {
		List<Map<String, Object>> columnDefs = getColumnDefs(jqdtConfig);

		Map columnDef = Record.createMap("orderable", false);
		columnDef.put("className", "select-checkbox");
		columnDef.put("targets", "0");

		columnDefs.add(columnDef);

		Map select = Record.createMap("style", "os");
		select.put("selector", "td:first-child");

		jqdtConfig.put("select", select);

		return jqdtConfig;
	}

	public Map<String, Object> getConfigMap$HiddenColumns( Map<String, Object> jqdtConfig, String[] viewFieldKeys, String... hiddenColumnKeys ) {
		int i = 0;
		List<Integer> targets = new ArrayList<Integer>();
		List<String> hiddenColumns = new ArrayList<String>(java.util.Arrays.asList(hiddenColumnKeys));
		for( String s : viewFieldKeys ) {
			if( hiddenColumns.contains(s) ) {
				targets.add(i);
			}
			i++;
		}

		Map<String, Object> map = Record.createMap("targets", targets);
		map.put("visible", false);

		List<Map<String, Object>> columnDefs = getColumnDefs(jqdtConfig);

		if( targets.size() > 0 ) {
			columnDefs.add(map);
			jqdtConfig.put("columnDefs", columnDefs);
		}

		return jqdtConfig;
	}

	private List<Map<String, Object>> getColumnDefs( Map<String, Object> jqdtConfig ) {
		List<Map<String, Object>> columnDefs = (List<Map<String, Object>>)jqdtConfig.get("columnDefs");
		if( columnDefs == null )
			columnDefs = new ArrayList<Map<String, Object>>();

		return columnDefs;
	}

	public Map<String, Object> getConfigMap$Sort( Map<String, Object> jqdtConfig, String[] viewFieldKeys, String[] sortKeys,
			String[] sortWays ) {
		int i = 0;
		int j = 0;
		List<Integer> targets = new ArrayList<Integer>();
		List<Integer[]> orderData = new ArrayList<Integer[]>();
		List<String> sortColumns = new ArrayList<String>(java.util.Arrays.asList(sortKeys));
		List<Map<String, Object>> columnDefs = new ArrayList<Map<String, Object>>();
		for( String s : viewFieldKeys ) {
			Map<String, Object> columnDef = new TreeMap<String, Object>();
			if( sortColumns.contains(s) ) {
				targets.add(i);
				j = 0;
				Integer sort = 1;// asc
				for( String col : sortColumns ) {
					String sortWay = sortWays == null ? "asc" : sortWays[j];
					if( "desc".equals(sortWay) ) {
						sort = 0;
					}
					j++;
				}
				orderData.add(new Integer[] { i, sort });

				columnDef.put("targets", new ArrayList<Integer>(java.util.Arrays.asList(new Integer[] { i })));
				columnDef.put("orderData", new ArrayList<Integer>(java.util.Arrays.asList(new Integer[] { i, sort })));
				columnDefs.add(columnDef);
			}
			i++;
		}

		List<Map<String, Object>> _columnDefs = getColumnDefs(jqdtConfig);

		_columnDefs.addAll(columnDefs);

		jqdtConfig.put("columnDefs", _columnDefs);

		return jqdtConfig;
	}

	public Map<String, Object> getConfigMap$RowGroup( Map<String, Object> jqdtConfig, String[] viewFieldKeys, String rowGroupColumnName ) {
		int i = 0;
		int columnIndex = -1;// find first columnIndex by the fieldkeyname
		for( String s : viewFieldKeys ) {
			if( rowGroupColumnName.equals(s) ) {
				columnIndex = i;
			}
			i++;
		}
		jqdtConfig.put("rowGroup", getRowGroup(rowGroupColumnName));
		String[] in = new String[] { String.valueOf(columnIndex), "asc" };
		List<List> out = new ArrayList<List>();
		out.add(java.util.Arrays.asList(in));
		jqdtConfig.put("order", out);

		return jqdtConfig;
	}

	/**
	 * @deprecated not working...
	 */
	public Map<String, Object> getConfigMap$RowGroups( Map<String, Object> jqdtConfig, String[] viewFieldKeys, String... rowGroupColumnNames ) {
		int i = 0;
		List<Integer> rowGroupColumnIndex = new ArrayList<Integer>();
		List<String> rowGroupColumns = new ArrayList<String>(java.util.Arrays.asList(rowGroupColumnNames));
		for( String s : viewFieldKeys ) {
			if( rowGroupColumns.contains(s) ) {
				rowGroupColumnIndex.add(i);
			}
			i++;
		}
		Map<String, List<Integer>> map = new TreeMap<String, List<Integer>>();
		map.put("dataSrc", rowGroupColumnIndex);
		jqdtConfig.put("rowGroup", map);

		String[] in = new String[] { new Gson().toJson(rowGroupColumnIndex), "asc" };
		List<List> out = new ArrayList<List>();
		out.add(java.util.Arrays.asList(in));
		jqdtConfig.put("order", out);

		return jqdtConfig;
	}

	private Locale DEFAULT_LOCALE = new Locale("en");

	/**
	 * like excel key navigation. ( not good with 'cellEditPlugin', so better to disable )
	 */
	public Map<String, Object> getConfigMap$KeyTablePlugin( Map<String, Object> jqdtConfig, boolean enable ) {

		jqdtConfig.put("keys", enable);

		return jqdtConfig;
	}

	public Map<String, Object> getConfigMap$Ajax( Map<String, Object> jqdtConfig, String url, String dataSrc ) {
		return getConfigMap$Ajax(jqdtConfig, url, dataSrc, "GET");
	}

	public Map<String, Object> getConfigMap$Ajax( Map<String, Object> jqdtConfig, String url, String dataSrc, String reqType ) {
		Map<String, Object> map = Record.createMap("url", url);
		map.put("dataSrc", dataSrc);

		map.put("type", reqType);

		jqdtConfig.put("ajax", map);

		return jqdtConfig;
	}

	/**
	 * datatables 'dom' property
	 * 
	 *
	 */
	public static class Dom {
		/** B: button */
		public final static String DEFAULT_DOM_OPTION_LAYOUT = "Bfrltip";

		private final static char OPT_LAYOUT_RecordLengthMenu = 'l';
		private final static char OPT_LAYOUT_FilterInput = 'f';
		private final static char OPT_LAYOUT_TheTable = 't';
		private final static char OPT_LAYOUT_Info = 'i';
		private final static char OPT_LAYOUT_Pagenation = 'p';
		private final static char OPT_LAYOUT_Processing = 'r';

		private final static char OPT_LAYOUT_JqueryUI_Header = 'H';
		private final static char OPT_LAYOUT_JqueryUI_Footer = 'F';
	}

	/**
	 * ajax render option
	 */
	public static class Render {

	}

	public static class Default {
		public static Map<String, Object> getSaneDefault() {
			Map<String, Object> jqdtConfig = new HashMap<String, Object>();
			jqdtConfig.put("buttons", getButtonsDefault());
			jqdtConfig.put("dom", JqdtConfigHelper.Dom.DEFAULT_DOM_OPTION_LAYOUT);
			jqdtConfig.put("deferRender", true);

			return jqdtConfig;
		}
	}

	public static class Button {
		final String[] DEFAULT_BUTTONS = new String[] {
				"copy", "csv", "excel"

		};
		final static String TSV_BUTTON_STRING = "{extend:'csvHtml5', fieldSeparator: '\\t', extension: '.tsv'}";
	}

	/**
	 * Usage:
	 * 
	 * <pre>
	 * String fieldHeaders = getListOfWrap(fieldTitles, "<th>", "</th>");
	 * </pre>
	 */
	public static String getListOfWrapString( String[] arr, String wrapStart, String wrapEnd ) {
		String fieldHeaders = wrapStart;
		fieldHeaders += StringUtil.strJoin(arr, wrapEnd + wrapStart);
		fieldHeaders += wrapEnd;

		return fieldHeaders;
	}

	public static Map<String, Object> getCellEditConfig( String[] fieldKeys, Table table ) {
		return getCellEditConfig(fieldKeys, table, null);
	}

	public static Map<String, Object> getCellEditConfig( String[] fieldKeys, Table table, String[] viewReadonly ) {
		return getCellEditConfig(fieldKeys, table, viewReadonly, null);
	}

	public static Map<String, Object> getCellEditConfig( String[] fieldKeys, Table table, String[] viewReadonly, String[] alterKeys ) {
		List<Integer> allowNullsColumns = new ArrayList<Integer>();

		Map<String, Object> cellEdit = new HashMap<String, Object>();

		List<Integer> cellEditColumns = new ArrayList<Integer>();

		List<Map<String, Object>> inputTypes = new ArrayList<Map<String, Object>>();

		// List<Map.Entry<String, String>> inputTypesOptions = new ArrayList<Map.Entry<String, String>>();

		int fieldKeyIndex = 0;
		for( String fieldKey : fieldKeys ) {
			Table.Field fd = (Field)table.getField(fieldKey);

			if( true ) {
				if( "envVal".equals(fieldKey) ) {
					System.out.println("x");
				}
			}

			if( fd == null ) {
				if( new ArrayList<String>(java.util.Arrays.asList(alterKeys)).contains(fieldKey) ) {
					cellEditColumns.add(fieldKeyIndex);
				} else {
					Logger.getRootLogger().warn("Null found for fieldKey: " + fieldKey
							+ " fieldKeys: " + java.util.Arrays.asList(fieldKeys)
							+ " table: " + table.getTableName());
				}
			} else {
				if( fd.alterable() ) {
					if( viewReadonly == null || !new ArrayList<String>(java.util.Arrays.asList(viewReadonly)).contains(fieldKey) ) {
						cellEditColumns.add(fieldKeyIndex);
					}

					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("column", fieldKeyIndex);
					map.put("type", DataTableCellEditInputType.TEXT);
					map.put("options", null);
					inputTypes.add(map);
				}

				if( fd.nullable() ) {
					allowNullsColumns.add(fieldKeyIndex);
				}
			}

			fieldKeyIndex++;
		}
		Map<String, Object> allowNulls = new HashMap<String, Object>();
		allowNulls.put("columns", allowNullsColumns);

		cellEdit.put("allowNulls", allowNulls);
		cellEdit.put("columns", cellEditColumns);
		cellEdit.put("inputTypes", inputTypes);

		return cellEdit;
	}

	public Map<String, Object> createAjaxConfigMap( javax.servlet.http.HttpServletRequest req, String dataSrc, String... extraParams ) {
		String requestedUrl = req.getRequestURI();

		String urlParams = "";
		for( String paramKey : extraParams ) {
			String[] paramValues = req.getParameterValues(paramKey);
			urlParams += StringUtil.strJoin("&" + paramKey, "=", paramValues);
		}

		final String url = requestedUrl + urlParams.replaceFirst("&", "?");

		Map<String, Object> ajax = new HashMap<String, Object>();
		ajax.put("dataSrc", dataSrc);
		ajax.put("url", url);

		return ajax;
	}

	/**
	 * <pre>
	 * columnDefs: [
	 * {targets: [0, 1], defaultContent: ""}
	 * ]
	 * </pre>
	 */
	public Map<String, Object> getConfigMap$ColumnDefsDefaultContent( Map<String, Object> configMap, List<Integer> allowNullsColumnIndex,
			String defaultContent ) {
		List<Map<String, Object>> columnDefs = (List<Map<String, Object>>)configMap.get("columnDefs");
		if( columnDefs == null ) {
			columnDefs = new ArrayList<Map<String, Object>>();
		}

		Map<String, Object> map = Record.createMap("targets", allowNullsColumnIndex);
		map.put("defaultContent", defaultContent);

		if( !columnDefs.contains(map) ) {
			columnDefs.add(map);
		}

		configMap.put("columnDefs", columnDefs);

		return configMap;
	}

	/**
	 * 
	 * 'Javascript function string'.( Don't serialize(jsonize) javascript )
	 *
	 */
	public static class Javascript {
		/**
		 * Clear existing and reload.
		 * 
		 * - init before using 'DataTable'
		 * 
		 * <pre>
		 * var table = $("#example").DataTable({
		 * , buttons: ["refresh", "reload"]
		 * });
		 * </pre>
		 */
		public static String getRefreshButtonOnGlobalFunction() {
			String refreshButtonFunction = "jQuery.fn.dataTable.ext.buttons.refresh = {"
					+ " text: 'Refresh', "
					+ " action: function( e, dt, node, config ) {"
					+ "  dt.clear().draw();"
					+ "  dt.ajax.reload();"
					+ " }"
					+ "};";

			return refreshButtonFunction;
		}

		/**
		 * Clear existing and reload.
		 * 
		 * - init before using 'DataTable'
		 * 
		 * <pre>
		 * var table = $("#example").DataTable({
		 * , buttons: ["refresh", "reload"]
		 * });
		 * </pre>
		 */
		public static String getRemoveButtonOnGlobalFunction() {
			String refreshButtonFunction = "jQuery.fn.dataTable.ext.buttons.remove = {"
					+ " text: 'Remove', "
					+ " action: function( e, dt, node, config ) {"
					+ "  deleteRow( node );"
					+ " }"
					+ "};";

			return refreshButtonFunction;
		}

		/**
		 * Usage:
		 * 
		 * <pre>
		 * // Read the title text of column index 3
		 * var table = $('#example').DataTable();
		 * table.column( 3 ).title();
		 * </pre>
		 */
		public static String getColumnTitleGetterFunction() {
			String str = "jQuery.fn.dataTable.Api.register( 'column().title()', function () {"
					+ " var colheader = this.header();"
					+ " return jQuery(colheader).text().trim();"
					+ " } );";
			return str;
		}

		/**
		 * Only reload without clear previous content.
		 * 
		 * - init before using 'DataTable'
		 * 
		 * <pre>
		 * var table = $("#example").DataTable({
		 * , buttons: ["refresh", "reload"]
		 * });
		 * </pre>
		 */
		public static String getReloadButtonOnGlobalFunction() {
			String refreshButtonFunction = "jQuery.fn.dataTable.ext.buttons.reload = {"
					+ " text: 'Reload', "
					+ " action: function( e, dt, node, config ) {"
					+ "  dt.ajax.reload();"
					+ " }"
					+ "};";

			return refreshButtonFunction;
		}

		public static String getConfigColumnDefs( String[] fieldKeys ) {
			String jqdtConfigColumnDefsString = "";
			jqdtConfigColumnDefsString += "{columnDefs: [";
			jqdtConfigColumnDefsString += " { targets: " + getColumnDefsMomentable(fieldKeys).toString() + ", ";
			jqdtConfigColumnDefsString += " render: " + MOMENTJS_MILLIS_TO_DATETIMEOFBROWSER + "}";
			jqdtConfigColumnDefsString += "]}";

			return jqdtConfigColumnDefsString;
		}

		public static String getRowGroupFunctionDrawCallback( int colspanOfRow ) {
			String sbuf = "function drawCallback( settings ) {"
					+ " var api = this.api();"
					+ " var rows = api.rows( {page:'current'} ).nodes();"
					+ " var last = null;"
					+ " "
					+ " api.column(groupColumn, {page:'current'} ).data().each( function(group, i) {"
					+ "  if( last !== group ) {"
					+ "   $(rows).eq( i ).before("
					+ "    '<tr class=\"group\"><td colspan=\"" + colspanOfRow + "\">'+group+'</td></tr>'"
					+ "   );"
					+ "   last = group;"
					+ "  }"
					+ " } );"
					+ "}";

			return sbuf;
		}

		public static String getAutoFillToggleButton( String enableButtonText, String disableButtonText ) {
			String sbuf = "{buttons: [{"
					+ "text:'" + enableButtonText + "', "
					+ "action: function(e, dt) {"
					+ "  if( dt.autoFill().enabled() ) {"
					+ "   this.autoFill().disable();"
					+ "   this.text( '" + enableButtonText + "' );"
					+ "  } else {"
					+ "   this.autoFill().enable();"
					+ "   this.text( '" + disableButtonText + "' );"
					+ "  }"
					+ " }"
					+ "}]}";
			return sbuf;
		}

	}

	/**
	 * usually column naming( = fieldKey )
	 */
	public Map<String, Object> getRowGroup( String dataSrc ) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("dataSrc", dataSrc);
		return map;
	}

	public static List<String> getButtonsDefault() {
		return java.util.Arrays.asList(new String[] { "refresh", "reload" });
	}

	/**
	 * 
	 * This is only used for CellEdit's "inputTypes" property.
	 */
	public enum DataTableCellEditInputType {
		TEXT( "text" );
		// LIST( "list" ); if needed then implement!!
		// DATEPICKER("datepicker"); if needed then download datepicker and load!!

		private final String value;

		private DataTableCellEditInputType( String value ) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	public boolean getPluginResponsive() {
		return true;
	}

	public Map<String, Object> getPluginSelect() {
		Map<String, Object> map = Record.createMap("style", "multi");

		return map;
	}

	public Map<String, Object> getPluginAutoFill() {
		Map<String, Object> map = Record.createMap("enable", false);
		map.put("dom", Dom.DEFAULT_DOM_OPTION_LAYOUT);

		return map;
	}

	public static List<Integer> getColumnDefsMomentable( String[] fieldKeys ) {

		List<Integer> dateTimeRenderTargets = new ArrayList<Integer>();

		int idx = 0;
		Map columnDef = new HashMap();
		for( String fieldKey : fieldKeys ) {
			if( fieldKey.endsWith("DateTime") ) {
				dateTimeRenderTargets.add(idx);
			}

			idx++;
		}
		return dateTimeRenderTargets;
	}

	public List<Map<String, Object>> getColumns( String[] fieldKeys ) {
		return getColumns(fieldKeys, null);
	}

}
