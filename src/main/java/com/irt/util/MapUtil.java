/*
 *	File Name:	MapUtil.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

/**
 * Generic Utility for Map.
 *
 * Usually for record map( Map<String, Object> )
 *
 * or record list( List<Map<String, Object>> )
 *
 */
public class MapUtil {

	/**
	 *
	 * @param target
	 * @param key
	 * @return collection has (the entry and the object is not null)
	 */
	public static <K, V> boolean allEntryIsNotNull( Collection<Map<K, V>> target, final K key ) {
		boolean allNotNull = true;
		for( Map<K, V> map : target ) {
			if( map.containsKey(key) ) {
				V obj = map.get(key);
				if( obj == null ) {
					allNotNull = false;
					break;
				}
			} else {
				allNotNull = false;
				break;
			}
		}
		return allNotNull;
	}

	/**
	 *
	 * @param target
	 * @param key
	 * @return collection has any ( the entry is null or entry value is null )
	 */
	public static <K, V> boolean anyEntryIsNull( Collection<Map<K, V>> target, final K key ) {
		boolean anyHasNull = false;
		for( Map<K, V> map : target ) {
			if( map.containsKey(key) ) {
				V obj = map.get(key);
				if( obj == null ) {
					anyHasNull = true;
					break;
				}
			} else {
				anyHasNull = true;
				break;
			}
		}
		return anyHasNull;
	}

	public static <T> boolean anyValueIsNull( Collection<T> target, final T key ) {
		boolean anyHasNull = false;
		for( T t : target ) {
			if( t == null ) {
				anyHasNull = true;
				break;
			}
		}
		return anyHasNull;
	}

	public static <K, V> boolean containAllKeys( Map<K, V> map, String... keys ) {
		for( String key : keys ) {
			if( !map.containsKey(key) )
				return false;
		}
		return true;
	}

	public static <K, V> boolean containAllKeysAndValueNotNull( Map<K, V> map, String... keys ) {
		for( String key : keys ) {
			if( !map.containsKey(key) )
				return false;
			if( map.get(key) == null )
				return false;
		}
		return true;
	}

	public static <K, V> List<V> extractValueList( List<Map<K, V>> target, final K key ) {
		List<V> result = new ArrayList<V>();
		for( Map<K, V> map : target ) {
			if( map == null )
				break;
			if( map.containsKey(key) ) {
				result.add(map.get(key));
			}
		}
		return result;
	}

	public static <K, V> Collection<V> extractValues( Collection<Map<K, V>> target, final K key ) {
		Collection<V> result = new ArrayList<V>();
		for( Map<K, V> map : target ) {
			if( map.containsKey(key) ) {
				result.add(map.get(key));
			}
		}
		return result;
	}

	/**
	 *
	 * @param target
	 *            : List of Map
	 * @param keynamesOfExtractKeyVal
	 *            : To convert from Map to Map.Entry. select two key names in Map
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static <String, V> Collection<Map.Entry<String, V>> getEntryPairList( Collection<Map<String, V>> target,
			final String[] keynamesOfExtractKeyVal ) {
		Collection<Map.Entry<String, V>> result = new ArrayList<Map.Entry<String, V>>();
		int keycnt = 0;
		if( keynamesOfExtractKeyVal != null && keynamesOfExtractKeyVal.length == 2 ) {
			for( Map<String, V> map : target ) {
				// Map.Entry<String, V> entryPair;
				V v1 = null;
				V v2 = null;
				for( String key : keynamesOfExtractKeyVal ) {
					if( keycnt == 0 ) {
						if( map.containsKey(key) ) {
							v1 = map.get(key);
						}
					} else {
						if( map.containsKey(key) ) {
							v2 = map.get(key);
						}
					}
					keycnt++;
				}
				if( v1 != null ) {
					result.add(new SimpleImmutableEntry<String, V>((String)v1, v2));
				}
				keycnt = 0;
			}
		}

		return result;
	}

	public static <K, V> Map<String, V> getPartialMap( Map<String, V> target, String... keys ) {
		return getPartialMap(target, keys, new TreeMap<String, V>());
	}

	public static <K, V> Map<String, V> getPartialMap( Map<String, V> target, String[] keys, Map<String, V> result ) {
		for( String k : target.keySet() ) {
			for( String key : keys ) {
				if( k.equals(key) ) {
					result.put(key, target.get(key));
				}
			}
		}
		return result;
	}

	public static <K, V> Collection<Map<K, V>> getRenamedKeyValues( Collection<Map<K, V>> target, final K key, final K renameKey ) {
		Collection<Map<K, V>> newResult = new ArrayList<Map<K, V>>();
		for( Map<K, V> map : target ) {
			if( map.containsKey(key) ) {
				Map<K, V> newMap = new TreeMap<K, V>();
				newMap.put(renameKey, map.get(key));
				newResult.add(newMap);
			}
		}
		return newResult;
	}

	public static <K, V> Collection<Map<K, V>> getRenamedKeyValues( Collection<Map<K, V>> target, final K[] keys, final K[] renameKeys ) {
		Collection<Map<K, V>> newResult = new ArrayList<Map<K, V>>();
		for( Map<K, V> map : target ) {
			Map<K, V> newRenamed = new TreeMap<K, V>();
			for( int k = 0; k < keys.length; k++ ) {
				K key = keys[k];
				K renameKey = renameKeys[k];
				if( map.containsKey(key) ) {
					newRenamed.put(renameKey, map.get(key));
				}
			}
			newResult.add(newRenamed);
		}
		return newResult;
	}

	/**
	 *
	 * @param target
	 * @param key
	 * @param collIndex:
	 *            0 base collection iternations
	 * @return : can return null if key is not exists in the target
	 */
	public static <K, V> V getValue( Collection<Map<K, V>> target, final K key, int collIndex ) {
		int cnt = 0;
		for( Map<K, V> map : target ) {
			if( cnt == collIndex ) {
				if( map != null ) {
					return map.get(key);
				} else {
					return null;
				}
			}
			cnt++;
		}
		return null;
	}

	public static List getValues( List<Map> target, String key ) {
		List result = new ArrayList();
		for( Map map : target ) {
			if( map.containsKey(key) ) {
				result.add(map.get(key));
			}
		}
		return result;
	}

	/**
	 * put k,v into the all list( usually for set default value )
	 *
	 * @param target
	 *            : usually List<Map<String, Object>>
	 * @param key
	 *            : put key value to the target
	 * @param value
	 *            : put key value to the target
	 * @return target
	 */
	public static <K, V> Collection<Map<K, V>> pupushConstantKeyValues( Collection<Map<K, V>> target, final K key, final V value ) {
		for( Map<K, V> map : target ) {
			map.put(key, value);
		}
		return target;
	}

	public static <String, Object> List<Map<String, Object>> pushConstantKeyValues( List<Map<String, Object>> target, final String key, final Object value ) {
		for( Map<String, Object> map : target ) {
			map.put(key, value);
		}
		return target;
	}

	public static <K, V> Collection<Map<K, V>> pushConstantKeyValues( Collection<Map<K, V>> target, final K[] keys, final V[] values ) {
		for( Map<K, V> map : target ) {
			for( int i = 0; i < keys.length; i++ ) {
				if( keys[i] != null ) {
					map.put(keys[i], values[i]);
				}
			}
		}
		return target;
	}

	/**
	 * put k,v into the list if the map has k
	 *
	 * @param target
	 * @param key
	 * @param value
	 * @return
	 */
	public static <K, V> Collection<Map<K, V>> pushConstantValuesIfHasKey( Collection<Map<K, V>> target, final K key, final V value ) {
		for( Map<K, V> map : target ) {
			if( map.containsKey(key) ) {
				map.put(key, value);
			}
		}
		return target;
	}

	// public static <String, V> Collection<Map<String, V>> pushConstantKeysAndValues( Collection<Map<String, V>> target, final String[] keys, final
	// String[] values ) {
	// for( Map<String, V> map : target ) {
	// for( int i=0; i < keys.length; i++ ) {
	// if( keys[i] != null ) {
	// map.put(keys[i], (V)values[i]);
	// }
	// }
	// }
	// return target;
	// }

	public static String toCsv( List<Object> strList ) {
		if( strList != null ) {
//			StringBuffer sbuf = new StringBuffer();
//			for( Object o : strList ) {
//				if( o != null ) {
//					sbuf.append(o);
//					sbuf.append(",");
//				}
//			}
//			sbuf.deleteCharAt(sbuf.length()-1);
//			return sbuf.toString();
			return strList.toString().replaceFirst("^\\[", "").replaceAll("\\]$", "");
		}
		return "";
	}

	/**
	 * dont use in production.
	 *
	 * @testing @experimental
	 *
	 * @param target
	 * @param key
	 * @param value
	 * @return
	 */
	public static <K, V> Collection<Map<K, V>> tPushObjectKeyValues(Collection<Map<K, V>> target, final K key, final V... values ) {
		if( target == null )
			return null;
		Iterator<Map<K,V>> it = target.iterator();
		for( int i=0; it.hasNext(); i++ ) {
			Map<K,V> map = it.next();
			map.put(key, values[i]);
		}
		return target;
	}

}
