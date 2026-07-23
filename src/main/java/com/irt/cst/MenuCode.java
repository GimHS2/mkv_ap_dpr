/*
 *	File Name:	MenuCode.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	en 이 기본 locale 이 되도록 수정
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.cst;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataResult;
import com.irt.data.Record;
import com.irt.dpr.util.CondPred;
import com.irt.rbm.TableAccessor;
import com.irt.rbm.TableDaoException;
import com.irt.rbm.TableDao;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.util.MapUtil;
import com.irt.util.StringUtil;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MenuCode extends com.irt.rbm.ManipulableManagerImpl implements TableAccessor {

	private final static Table table = com.irt.cst.Schema.findTable(com.irt.cst.Schema.CST_MENU);
	private final static QueryFactory factory = com.irt.cst.Schema.findQueryFactory(com.irt.cst.Schema.CST_MENU);

	private final static String HRCY_LEVEL_DIVIDER = "#";

	private final static int HRCY_LEVEL_DIVIDER_ARRIDX = 0;

	private final static String HRCY_SEQ_DIVIDER = "[";

	private final static int HRCY_SEQ_DIVIDER_ARRIDX = 1;

	public static String createMenuMessageKey( String menuId, String menuHrcyCode ) {
		return menuId + menuHrcyCode;
	}

	public static String getCurrentHrcyCode( int menuClass, int menuClassSeq ) {
		return HRCY_LEVEL_DIVIDER + menuClass//
				+ HRCY_SEQ_DIVIDER + menuClassSeq;
	}

	/**
	 * @param map
	 * @return : classSymbol eg. ( #1[2 first class and second seq within the same class )
	 */
	public static String getCurrentMenuHrcy( Map map ) {
		int menuClass = (Integer)map.get("menuClass");
		int menuClassSeq = (Integer)map.get("menuClassSeq");
		return HRCY_LEVEL_DIVIDER + menuClass//
				+ HRCY_SEQ_DIVIDER + menuClassSeq;
	}

	public static String getHrcyCode( String[] ArrOfHrcy ) {
		return StringUtil.strJoin(ArrOfHrcy, HRCY_LEVEL_DIVIDER);
	}

	private static String[] getHrcyCodes( String menuHrcyCode ) {
		String[] ArrOfHrcy = menuHrcyCode.split(HRCY_LEVEL_DIVIDER);
		return ArrOfHrcy;
	}

	/**
	 * @param menuHrcyCode
	 * @return : lastClass
	 */
	public static int getHrcyLevel( String menuHrcyCode ) {
		return getHrcyLevel(menuHrcyCode, -1);
	}

	public static int getHrcyLevel( String menuHrcyCode, int level ) {
		String[] levels = getHrcyCodes(menuHrcyCode);
		if( level == -1 ) {
			level = levels.length - 1;
		}
		if( level > levels.length - 1 )
			return -1;

		String current = levels[level];

		String[] classAndSeq = getHrcyLevelAndSeq(current);

		String str = null;
		int num = -1;
		if( classAndSeq != null ) {
			str = classAndSeq[HRCY_LEVEL_DIVIDER_ARRIDX];
			num = Integer.parseInt(str);
		}
		return num;
	}

	public static String[] getHrcyLevelAndSeq( String hrcyCode ) {
		String[] classAndSeq = hrcyCode.split(Pattern.quote(HRCY_SEQ_DIVIDER));
		return classAndSeq;
	}

	public static int getHrcySeq( String menuHrcyCode, int level ) {
		String[] levels = getHrcyCodes(menuHrcyCode);
		if( level == -1 ) {
			level = levels.length - 1;
		}
		if( level > levels.length - 1 )
			return -1;

		String current = levels[level];
		String[] classAndSeq = getHrcyLevelAndSeq(current);

		String str = null;
		int num = -1;
		if( classAndSeq != null ) {
			str = classAndSeq[HRCY_SEQ_DIVIDER_ARRIDX];
			num = Integer.parseInt(str);
		}
		return num;
	}

	public static int getLevel( String menuHrcyCode ) {
		if( menuHrcyCode == null || menuHrcyCode.length() == 0 ) {
			return -1;
		}

		String[] levels = menuHrcyCode.split(HRCY_LEVEL_DIVIDER);
		if( levels != null && levels.length > 0 ) {
			return levels.length - 1;
		} else {
			return -1;
		}
	}

	public static String getMenuIdForHtml( Map map ) {
		String menuId = (String)map.get("menuId");
		return getMenuIdForHtml(menuId);
	}

	/**
	 * @param menuId
	 *            : usually pattern like "ICSBoard.FQ.CN"
	 * @return replaces "." to "_" for better html id attribute
	 */
	public static String getMenuIdForHtml( String menuId ) {
		String menuIdForHtml = menuId.replaceAll("\\.", "_");
		return menuIdForHtml;
	}

	/**
	 * @param menuIdForHtml
	 *            : string from {@link #getMenuIdForHtml(String)}
	 * @return replaces "_" to "." for original menuId
	 */
	public static String getMenuIdFromHtml( String menuIdForHtml ) {
		String menuId = menuIdForHtml.replaceAll("\\_", ".");
		return menuId;
	}

	public static String getParentMenuHrcy( Map map ) {
		String menuHrcyCode = (String)map.get("menuHrcy");
		String[] ArrOfHrcy = getHrcyCodes(menuHrcyCode);
		String[] parentLevels = Arrays.copyOf(ArrOfHrcy, ArrOfHrcy.length - 1);
		String parentMenuHrcyCode = getHrcyCode(parentLevels);
		return parentMenuHrcyCode;
	}

	public static String getPartySupportLocaleCsv( String partyId, String defaultSupportLocale ) {
		String supportLocale = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;" + partyId, defaultSupportLocale);
		return supportLocale;
	}

	public static String getPartySupportLocaleLabelCsv( String partyId, String defaultSupportLocaleLabel ) {
		String supportLocaleLabel = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocaleLabel;" + partyId, defaultSupportLocaleLabel);
		return supportLocaleLabel;
	}

	TableDao tao;

	public MenuCode( SQLHandler handler ) {
		this(handler, table, factory);
		tao = new TableDao(table, handler.getMessageHandler());
	}

	private MenuCode( SQLHandler handler, Table table, QueryFactory factory ) {
		super(handler, table, factory);
	}

	/**
	 * TODO: This may not be used... check and delete this method...
	 */
	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		Map conditionMap = Record.createMap("menuId", primaryMap.get("menuId"));
		conditionMap.put("displayLanguage", "en");
		Condition.putConditionValueOnly(conditionMap, "menuHrcyCode", primaryMap.get("menuHrcyCode"), Condition.CONDTYPE_STARTSWITH);

		String[] fieldKeys = getPrimaryFieldKeys();
		fieldKeys = com.irt.util.Arrays.append(fieldKeys, "menuMessageKey");
		fieldKeys = com.irt.util.Arrays.append(fieldKeys, "menuMessage");
		List<Map<String, Object>> childs = getRecords(conditionMap, fieldKeys);

		if( childs != null ) {
			if( childs.size() == 1 ) {
				Map<String, Object> p1 = extractPrimary(primaryMap, 0);
				Map<String, Object> p2 = extractPrimary(childs.get(0), 0);
				boolean equals = false;
				if( p1.get("menuHrcyCode").equals(p2.get("menuHrcyCode")) //
						&& p1.get("menuId").equals(p2.get("menuId")) ) {
					equals = true;
				}
				if( equals ) {
					return SQLManager.manageRecord(handler, table, primaryMap, Record.DELETE);
				} else {
					throw new DataException(DataException.ERR_CHILD_RECORD_FOUND, DataException.ERR_CHILD_RECORD_FOUND, childs.get(0));
				}
			} else {
				throw new DataException(DataException.ERR_CHILD_RECORD_FOUND, DataException.ERR_CHILD_RECORD_FOUND, childs.get(0));
			}
		}

		return SQLManager.manageRecord(handler, table, primaryMap, Record.DELETE);
	}

	/**
	 * delete all records for {menuId and menuLocale} and insert supplied records
	 *
	 * @param records
	 * @return
	 * @throws SQLException
	 */
	public DataResult deleteInsert( List<Map<String, Object>> records ) throws SQLException {
		String menuId = null;
		String menuLocale = null;
		if( records != null && records.size() > 0 ) {
			menuId = (String)records.get(0).get("menuId");
			menuLocale = (String)records.get(0).get("menuLocale");
		}
		if( menuId == null || menuId.length() == 0 || menuLocale == null || menuLocale.length() == 0 ) {
			return null;
		} else {
			PreparedStatement pstmt = handler.getConnection().prepareStatement("DELETE CST_MENU WHERE MENU_ID = ? AND MENU_LOCALE = ?");
			Object[] bindVars = new Object[] { menuId, menuLocale };
			SQLManager.bindVariables(pstmt, bindVars);
			int ret = pstmt.executeUpdate();
			return SQLManager.manageRecordAll(handler, table, records, Record.REGIST );

		}
	}

	@Override
	public Map<String, Object> extractPrimary( Map<String, Object> sourceMap, int valueIndex ) throws TableDaoException {
		return tao.extractPrimary(sourceMap, valueIndex);
	}

	@Override
	public Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> sourceMap ) throws TableDaoException {
		return tao.extractPrimaryKeyValues(sourceMap);
	}

	public String[] getAvaialbleLocales( String menuId ) throws SQLException {
		Map<String, Object> conditionMap = Record.createMap("menuId", menuId);
		String[] fieldKeys = new String[] { "menuLocale" };
		CondPred.putDistinct(conditionMap);

		List<Map<String, Object>> localeList = getRecords(conditionMap, fieldKeys);
		List<Object> locales = (List<Object>)MapUtil.extractValues(localeList, "menuLocale");
		return locales.toArray(new String[locales.size()]);
	}

	@Override
	public String[] getBindFieldKeys( int statementType ) {
		return tao.getBindFieldKeys(statementType);
	}

	public String getMenuHtml( String menuId, String menuLocale ) throws SQLException {
		Map<String, Object> conditionMap = Record.createMap("menuId", menuId);

		if( menuLocale.indexOf(",") > -1 ) {
			String[] locales = menuLocale.split(",");
			conditionMap.put("menuLocales", locales);
		} else {
			conditionMap.put("menuLocale", menuLocale);
		}


		String[] fieldKeys = getFieldSet(false).getFieldKeyArray();

		List<Map<String, Object>> records = getRecords(conditionMap, fieldKeys);
		if( records == null || records.size() <= 0 ) {
			return null;
		} else {
			return MenuHtml.Converter.getMenuHtml(records);
		}
	}

	@Override
	public String[] getPrimaryFieldKeys() {
		return tao.getPrimaryFieldKeys();
	}

	@Override
	public String[] getReadonlyFieldKeys() {
		return tao.getReadonlyFieldKeys();
	}

	private boolean isNullOrEmpty( String s ) {
		return null == s || s.length() <= 0;
	}

	private Integer toInteger( Object bigDecObj ) {
		return ( (BigDecimal)bigDecObj ).intValueExact();
	}
}
