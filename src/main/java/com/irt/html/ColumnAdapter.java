/*
 *	File Name:	ColumnAdapter.java
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

package com.irt.html;

import com.irt.data.Record;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnGroup;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.util.StringUtil;
import com.irt.util.cst.ReflectUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ColumnAdapter {
	public static final String TAB = "\t";
	public static final String NEWLINE = "\n";

	private Comparator<Map<String, Object>> COLUMN_SORT_COMPARATOR = new Comparator<Map<String, Object>>() {

		@Override
		public int compare( Map<String, Object> o1, Map<String, Object> o2 ) {
			int columnType = getColumnType(o1, o2);
			if( columnType == 0 ) {

				int columnKey = getColumnKey(o1, o2);
				if( columnKey == 0 ) {

					int columnParentKey = getColumnParentKey(o1, o2);
					if( columnParentKey == 0 ) {
						return 0;
					} else {
						return columnParentKey;
					}
				} else {
					return columnKey;
				}
			} else {
				return columnType;
			}
		}

		private int getColumnKey( Map<String, Object> o1, Map<String, Object> o2 ) {
			String key1 = (String)o1.get("columnKey");
			if( key1 == null )
				throw new NullPointerException("'columnKey' is mandatory: " + o1);
			String key2 = (String)o2.get("columnKey");
			if( key2 == null )
				throw new NullPointerException("'columnKey' is mandatory: " + o2);

			return key1.compareTo(key2);
		}

		private int getColumnParentKey( Map<String, Object> o1, Map<String, Object> o2 ) {
			int columnParentKey = 0;
			String cpkey1 = (String)o1.get("columnParentKey");
			String cpkey2 = (String)o2.get("columnParentKey");
			if( cpkey1 == null ) {
				columnParentKey = 1;
			} else if( cpkey2 == null ) {
				columnParentKey = -1;
			} else if( cpkey1 != null && cpkey2 != null ) {
				if( cpkey1.equals(cpkey2) ) {
					columnParentKey = 0;
				} else {
					columnParentKey = cpkey1.compareTo(cpkey2);
				}
			}
			return columnParentKey;
		}

		private int getColumnType( Map<String, Object> o1, Map<String, Object> o2 ) {
			int columnType = 0;
			String ctype1 = (String)o1.get("columnType");
			String ctype2 = (String)o2.get("columnType");
			if( ctype1 == null ) {
				columnType = -1;
			} else if( ctype2 == null ) {
				columnType = 1;
			} else if( ctype1 != null && ctype2 != null ) {
				if( ctype1.equals(ctype2) ) {
					columnType = 0;
				} else {
					if( "COL".equals(ctype1) ) {
						columnType = -1;
					} else if( "COL".equals(ctype2) ) {
						columnType = 1;
					} else if( "GRP".equals(ctype1) ) {
						columnType = -1;
					} else if( "GRP".equals(ctype2) ) {
						columnType = 1;
					} else if( "LNK".equals(ctype1) ) {
						columnType = -1;
					} else if( "LNK".equals(ctype2) ) {
						columnType = 1;
					} else if( "SFX".equals(ctype1) ) {
						columnType = -1;
					} else if( "SFX".equals(ctype2) ) {
						columnType = 1;
					}
				}
			}
			return columnType;
		}
	};

	private boolean canWriteFullLine( Map<String, Object> map, String[] mandKeys ) {
		boolean hasFullLine = true;
		for( String key : mandKeys ) {
			if( map.get(key) == null ) {
				hasFullLine = false;
				break;
			}
		}
		return hasFullLine;
	}

	public Comparator<Map<String, Object>> getColumnSortComparator() {
		return COLUMN_SORT_COMPARATOR;
	}

	private String getColumnTypeString( Object obj ) {
		if( obj == null )
			return null;

		if( obj instanceof ColumnImpl ) {
			boolean isColumnGroupSubColumn = ( ( (ColumnImpl)obj ).getColumnGroup() != null );
			if( isColumnGroupSubColumn ) {
				return ColumnAdapter.ColumnType.ColumnGroup.toString();
			} else {
				return ColumnAdapter.ColumnType.Column.toString();
			}
		} else if( obj instanceof ColumnGroupImpl ) {
			return ColumnAdapter.ColumnType.ColumnGroup.toString();
		} else if( obj instanceof HyperLinkImpl ) {
			return ColumnAdapter.ColumnType.HyperLink.toString();
		} else {
			throw new IllegalArgumentException("undefined ColumnType for '" + obj.getClass().getCanonicalName() + "'");
		}
	}

	private Map<String, Object> getHyperLinkMap( HyperLinkImpl hyperLink, char linkingType ) {
		HyperLinkImpl link = (HyperLinkImpl)hyperLink;
		Map map = Record.createMap("linkKey", link.key);
		map.put("linkType", linkingType);
		map.put("columnType", getColumnTypeString(hyperLink));
		try {
			map.put("linkHrefPattern", getPatternUnsafely(link.href));
			map.put("linkHelpPattern", getPatternUnsafely(link.help));
		} catch( IllegalArgumentException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IllegalAccessException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		map.put("linkAuthSystemCode", link.systemCode);
		map.put("linkAuthPackageCode", link.packageCode);
		map.put("linkCondClass", getLinkCondClassString(link.conditionClass));
		map.put("linkCondFieldKey", link.conditionFieldKey);
		map.put("linkCondValue", link.conditionValue);

		return map;
	}

	public String getLinkCondClassString( int conditionClass ) {
		return HyperLinkConditionClass.fromConditionClass(conditionClass).toString();
	}

	private String getPatternUnsafely( RecordFormat format ) throws IllegalArgumentException, IllegalAccessException {
		if( format == null )
			return null;

		Map<String, PatternRecordFormat> formatMap = (Map)ReflectUtil.getDeclaredFieldObject(PatternRecordFormat.class, null, "formatMap");
		for( PatternRecordFormat prf : formatMap.values() ) {
			if( format.equals(prf.getInstance(prf.getPattern())) ) {
				return prf.getPattern();
			}
		}

		return null;
	}

	private String getTabed( Map<String, Object> map, String key ) {
		return getTabed(map, key, null);
	}

	private String getTabed( Map<String, Object> map, String key, String suffix ) {
		return "" + ( getUTFText((String)map.get(key), suffix) ) + TAB;
	}

	private String getTabed( Map<String, Object> map, String key, String suffix, String prefix ) {
		return "" + ( getUTFText((String)map.get(key), suffix) ) + TAB;
	}

	/**
	 * should be pure text( no tab, no new line )
	 */
	private String getUTFText( String key, String sfx ) {
		return getUTFText(key, sfx, null);
	}

	private String getUTFText( String key, String sfx, String pfx ) {
		return StringUtil.getUnicodeEncoded(( key == null ? "" : key )
				+ ( sfx == null ? "" : sfx )
				+ ( pfx == null ? "" : pfx ));
	}

	private Column toColumnObject( Map<String, Object> map ) {
		throw new UnsupportedOperationException("not impl yet.");
	}

	public List<Map<String, Object>> getDbColumnSuffixRecords( ColumnPoolImpl columnPool, String partyId, Locale locale ) {
		List records = new ArrayList();
		Map<String, RecordFormat> columnSuffixMap = columnPool.columnSuffixMap;
		if( columnSuffixMap == null ) {
			if( columnPool.parent != null && columnPool.parent instanceof ColumnPoolImpl ) {
				List fromParent = getDbColumnSuffixRecords((ColumnPoolImpl)columnPool.parent, partyId, locale);
				records.addAll(fromParent);
			}
		} else {
			for( String suffixKey : columnSuffixMap.keySet() ) {
				Map map = Record.createMap("columnKey", suffixKey);
				map.put("columnType", ColumnType.ColumnSuffix.toString());
				try {
					map.put("columnDataPattern", getPatternUnsafely(columnSuffixMap.get(suffixKey)));
				} catch( IllegalArgumentException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch( IllegalAccessException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				map.put("poolName", columnPool.getName());
				map.put("poolLocale", locale.getLanguage());
				map.put("partyId", partyId);
				records.add(map);
			}
		}

		return records;
	}

	public Map<String, Object> toDbRecord( Column _column, String partyId, String poolLocale, String poolName ) {
		Map<String, Object> map = null;

		if( true ) {
			if( _column.getKey().contains("\\+") ) {
				System.out.println("xx");
			}
		}

		if( _column instanceof ColumnImpl ) {
			ColumnImpl column = (ColumnImpl)_column;
			map = Record.createMap("columnKey", column.getKey());
			map.put("columnType", getColumnTypeString(column));
			map.put("columnTitle", column.getColumnTitle());
			map.put("columnAttr", column.getColumnAttr());
			map.put("columnSortable", ( column.sortable() ? "Y" : "N" ));
			map.put("columnDataCellAttr", column.getDataCellAttr());
			try {
				map.put("columnDataPattern", getPatternUnsafely(column.columnValue));
				map.put("columnHelpPattern", getPatternUnsafely(column.columnHelp));
			} catch( IllegalArgumentException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch( IllegalAccessException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			map.put("columnParentKey", ( column.getColumnGroup() == null ) ? null : column.getColumnGroup().getKey());
			map.put("poolName", poolName);
			map.put("poolLocale", poolLocale);
			map.put("partyId", partyId);
			return map;
		} else if( _column instanceof ColumnGroup ) {
			ColumnGroup columnGroup = (ColumnGroup)_column;
			map = Record.createMap("columnKey", columnGroup.getKey());
			map.put("columnTitle", columnGroup.getGroupTitle());
			map.put("columnAttr", columnGroup.getGroupAttr());
			map.put("columnType", getColumnTypeString(columnGroup));
			map.put("columnParentKey", null);
			map.put("poolName", poolName);
			map.put("poolLocale", poolLocale);
			map.put("partyId", partyId);
			return map;
		} else if( _column instanceof LinkedColumn ) {
			LinkedColumn linkedColumn = (LinkedColumn)_column;
			HyperLink _headerLink = linkedColumn.getHeaderLink();
			HyperLink _columnLink = linkedColumn.getColumnLink();

			map = toDbRecord(linkedColumn.getSourceColumn(), partyId, poolLocale, poolName);

			List<String> columnLinkKeys = new ArrayList<String>();

			if( _headerLink != null && _headerLink instanceof HyperLinkImpl ) {
				Map<String, Object> linkMap = toDbRecordFromHyperLink(map, (HyperLinkImpl)_headerLink, 'H');

				map.put("headerLink", linkMap);
				map.put("columnLinkHeaderKey", linkMap.get("linkKey"));
				columnLinkKeys.add((String)linkMap.get("linkKey"));
			} else if( _columnLink != null && _columnLink instanceof HyperLinkImpl ) {
				Map<String, Object> linkMap = toDbRecordFromHyperLink(map, (HyperLinkImpl)_columnLink, 'C');

				map.put("columnLink", linkMap);
				map.put("columnLinkColumnKey", (String)linkMap.get("linkKey"));
				columnLinkKeys.add((String)linkMap.get("linkKey"));
			} else {
				throw new UnsupportedOperationException();
			}

			if( columnLinkKeys.size() > 0 ) {
				map.put("columnLinkKeys", columnLinkKeys);
			}

			return map;
		} else {
			throw new UnsupportedOperationException();
		}

		// return map;
	}

	private Map<String, Object> toDbRecordFromHyperLink( Map<String, Object> sourceColumn, HyperLinkImpl hyperLink, char linkType ) {
		Map<String, Object> linkMap = getHyperLinkMap((HyperLinkImpl)hyperLink, linkType);
		linkMap.put("columnTitle", linkMap.get("linkHelpPattern"));
		linkMap.put("columnDataPattern", linkMap.get("linkHrefPattern"));
		linkMap.put("columnKey", linkMap.get("linkKey"));
		linkMap.put("poolName", sourceColumn.get("poolName"));
		linkMap.put("poolLocale", sourceColumn.get("poolLocale"));
		linkMap.put("partyId", sourceColumn.get("partyId"));

		return linkMap;
	}

	public String toFileColumnListLine( Map<String, Object> map ) {
		if( map.get("columnKey") == null )
			throw new NullPointerException();

		ColumnAdapter.ColumnType columnType = ColumnAdapter.ColumnType.fromTypeName((String)map.get("columnType"));
		String toLine = null;

		switch( columnType ) {
		case ColumnList:

			break;
		default:
			throw new UnsupportedOperationException("columnType is mandatory.");
		}

		return toLine;
	}

	public String toFileColumnLine( Map<String, Object> map ) {
		if( map.get("columnKey") == null )
			throw new NullPointerException();

		ColumnAdapter.ColumnType columnType = ColumnAdapter.ColumnType.fromTypeName((String)map.get("columnType"));

		String toLine = "";
		switch( columnType ) {
		case Column:
			if( canWriteFullLine(map, new String[] { "columnKey", "columnTitle", "columnSortable" }) ) {
				toLine = TAB
						+ getTabed(map, "columnKey")
						+ getTabed(map, "columnTitle")
						+ getTabed(map, "columnSortable")
						+ getTabed(map, "columnAttr")
						+ getTabed(map, "columnDataPattern")
						+ "";
				if( map.get("columnHelp") != null ) {
					toLine += getTabed(map, "columnKey", "%HELP") + getTabed(map, "columnHelpPattern");
				}
			} else {
				int count = 0;
				for( String key : map.keySet() ) {
					if( "columnTitle".equals(key) ) {
						toLine += TAB
								+ getTabed(map, "columnKey", "%TITLE", "COL.") + getTabed(map, key)
								+ NEWLINE;
						count++;
					} else if( "columnAttr".equals(key) ) {
						toLine += TAB
								+ getTabed(map, "columnKey", "%ATTR", "COL.") + getTabed(map, key)
								+ NEWLINE;
						count++;
					} else if( "columnDataPattern".equals(key) ) {
						toLine += TAB
								+ getTabed(map, "columnKey", "%DATA", "COL.") + getTabed(map, key)
								+ NEWLINE;
						count++;
					} else if( "columnDataCellAttr".equals(key) ) {
						toLine += TAB
								+ getTabed(map, "columnKey", "%CELL", "COL.") + getTabed(map, key)
								+ NEWLINE;
						count++;
					} else if( "columnHelpPattern".equals(key) ) {
						toLine += TAB
								+ getTabed(map, "columnKey", "%HELP", "COL.") + getTabed(map, key)
								+ NEWLINE;
						count++;
					}
				}
				if( count == 0 )
					toLine = "";
			}
			break;
		case ColumnGroup:
			if( map.get("columnParentKey") == null ) {
				toLine = TAB + "GRP."
						+ getTabed(map, "columnKey")
						+ getTabed(map, "columnTitle")
						+ getTabed(map, "columnAttr")
						+ "";
			} else {
				toLine = TAB + "GRP."
						+ getTabed(map, "columnParentKey", "%COL")
						+ getTabed(map, "columnKey")
						+ getTabed(map, "columnTitle")
						+ getTabed(map, "columnAttr")
						+ "";
			}
			break;
		case ColumnSuffix:
			toLine = TAB + "SFX."
					+ getTabed(map, "columnKey")
					+ getTabed(map, "columnDataPattern")
					+ "";
			break;
		case HyperLink:
			if( canWriteFullLine(map, new String[] { "columnKey", "linkHrefPattern" }) ) {
				toLine = TAB + "LNK."
						+ getTabed(map, "columnKey", "%TITLE")
						+ getTabed(map, "columnDataPattern")// linkHrefPattern
						+ getTabed(map, "columnTitle")// linkHelpPattern
						+ "";
			} else {
				int count = 0;
				List<String> alreadyWritten = new ArrayList<String>();
				for( String key : map.keySet() ) {
					if( "columnTitle".equals(key) ) {
						toLine += TAB + "LNK."
								+ getTabed(map, "columnKey", "%TITLE") + getTabed(map, key)
								+ NEWLINE;
						count++;
					} else if( !"linkCondClass".equals(key) && key.startsWith("linkCond") && map.get(key) != null ) {
						if( alreadyWritten.contains("linkCond") ) {
							continue;
						} else {
							toLine += TAB + "LNK."
									+ getTabed(map, "columnKey", "%COND")
									+ getTabed(map, "linkCondClass")
									+ getTabed(map, "linkCondFieldKey")
									+ getTabed(map, "linkCondValue")
									+ NEWLINE;
							alreadyWritten.add("linkCond");
							count++;
						}
					} else if( key.startsWith("linkAuth") && map.get(key) != null ) {
						if( alreadyWritten.contains("linkAuth") ) {
							continue;
						} else {
							toLine += TAB + "LNK."
									+ getTabed(map, "columnKey", "%AUTH")
									+ getTabed(map, "linkAuthSystemCode")
									+ getTabed(map, "linkAuthPackageCode")
									+ NEWLINE;
							alreadyWritten.add("linkCond");
							count++;
						}
					}
				}
				if( count == 0 )
					toLine = "";
			}
			break;
		default:
			throw new UnsupportedOperationException("columnType is mandatory.");
		}
		return toLine;
	}

	public enum ColumnType {
		Column( "COL" ), ColumnGroup( "GRP" ), HyperLink( "LNK" ), ColumnSuffix( "SFX" ),

		HyperLinkToColumn( "LNKC" ), HyperLinkToHeader( "LNKH" ),

		ColumnList( "CLST" );

		public static ColumnType fromTypeName( String typeName ) {
			for( ColumnType type : values() ) {
				if( type.toString().equals(typeName) ) {
					return type;
				}
			}
			return null;
		}

		private final String typeName;

		ColumnType( final String typeName ) {
			this.typeName = typeName;
		}

		public String toString() {
			return typeName;
		}
	}

	public enum HyperLinkConditionClass {
		NOT_NULL( HyperLinkImpl.CONDITION_NOT_NULL ),

		NOT_ZERO( HyperLinkImpl.CONDITION_NOT_ZERO ),

		EQUALS( HyperLinkImpl.CONDITION_EQUALS ),

		CONTAINS( HyperLinkImpl.CONDITION_CONTAINS ),

		NONE( HyperLinkImpl.CONDITION_NONE );

		public static HyperLinkConditionClass fromConditionClass( final int conditionClass ) {
			for( HyperLinkConditionClass type : values() ) {
				if( conditionClass == type.conditionClass ) {
					return type;
				}
			}
			return null;
		}

		private final int conditionClass;

		HyperLinkConditionClass( final int conditionClass ) {
			this.conditionClass = conditionClass;
		}

		public String toString() {
			return name();
		}
	}

}
