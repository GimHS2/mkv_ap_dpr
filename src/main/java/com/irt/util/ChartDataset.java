 /*
 *	File Name:	ChartDataset.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.util;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class ChartDataset {
	List<Map<String, Object>> axisXList, axisYList;
	String axisXKey, axisYKey;
	Map[][] dataMapMatrix;

	public ChartDataset( List<Map<String, Object>> axisXList, List<Map<String, Object>> axisYList, List<Map<String, Object>> dataList
						, String axisXKey, String axisYKey ) {
		Map<Object, Number> axisXMap = new java.util.HashMap<Object, Number>( axisXList.size() );
		for( int idx = 0; idx < axisXList.size(); idx++ ) {
			Map<String, Object> recordMap = axisXList.get( idx );
			axisXMap.put( recordMap.get(axisXKey), new Integer(idx) );
		}

		Map<Object, Number> axisYMap = new java.util.HashMap<Object, Number>( axisYList.size() );
		for( int idx = 0; idx < axisYList.size(); idx++ ) {
			Map<String, Object> recordMap = axisYList.get( idx );
			axisYMap.put( recordMap.get(axisYKey), new Integer(idx) );
		}

		Map[][] dataMapMatrix = new Map[ axisYList.size() ][ axisXList.size() ];
		for( Map<String, Object> recordMap : dataList ) {
			int idx_x = axisXMap.get(recordMap.get(axisXKey)).intValue();
			int idx_y = axisYMap.get(recordMap.get(axisYKey)).intValue();

			dataMapMatrix[idx_y][idx_x] = recordMap;
		}

		this.axisXList = axisXList;
		this.axisYList = axisYList;
		this.axisXKey = axisXKey;
		this.axisYKey = axisYKey;
		this.dataMapMatrix = dataMapMatrix;
	}

	public ChartDataset( List<Map<String, Object>> axisXList, List<Map<String, Object>> dataList, String axisXKey ) {
		Map<Object, Number> axisXMap = new java.util.HashMap<Object, Number>( axisXList.size() );
		for( int idx = 0; idx < axisXList.size(); idx++ ) {
			Map<String, Object> recordMap = axisXList.get( idx );
			axisXMap.put( recordMap.get(axisXKey), new Integer(idx) );
		}

		Map[][] dataMapMatrix = new Map[1][ axisXList.size() ];
		for( Map<String, Object> recordMap : dataList ) {
			int idx_x = axisXMap.get(recordMap.get(axisXKey)).intValue();
			dataMapMatrix[0][idx_x] = recordMap;
		}

		this.axisXList = axisXList;
		this.axisYList = null;
		this.axisXKey = axisXKey;
		this.axisYKey = null;
		this.dataMapMatrix = dataMapMatrix;
	}

	public int getAxisXCount() {
		return axisXList.size();
	}

	public String getAxisXKey() {
		return axisXKey;
	}

	public List getAxisXList() {
		return axisXList;
	}

	public int getAxisYCount() {
		return dataMapMatrix.length;
	}

	public String getAxisYKey() {
		return axisYKey;
	}

	public List getAxisYList() {
		return axisYList;
	}

	public Map getDataMap( int x, int y ) {
		return dataMapMatrix[y][x];
	}

	public Map[] getDataMapsX( int x ) {
		Map[] dataMaps = new Map[ dataMapMatrix.length ];
		for( int y = 0; y < dataMapMatrix.length; y++ )
			dataMaps[y] = dataMapMatrix[y][x];

		return dataMaps;
	}

	public Map[] getDataMapsY( int y ) {
		return dataMapMatrix[y];
	}
}
