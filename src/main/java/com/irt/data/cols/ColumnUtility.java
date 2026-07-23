/*
 *	File Name:	ColumnUtility.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.2	writeTitle(out, columnList, msghandler) -> writeTitle(out, columnList, msghandler, ...)
 *	stghr12		2008/03/31		2.2.1	getTitleRowCount() 추가
 *										writeTitle(out, columnList) -> writeTitle(out, columnList, msghandler)
 *	stghr12		2007/11/30		2.2.0	mergeColumnList() 추가
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.data.cols;

import com.irt.data.DataWriter;
import com.irt.util.MessageHandler;
import java.io.IOException;

/**
 *
 */
public class ColumnUtility {
	/**
	 * coumnList 앞뒤에 각각 columnList1, columnList2를 연결한다.
	 */
	public static ColumnList mergeColumnList( ColumnList columnList, ColumnList columnList1, ColumnList columnList2 ) {
		MergedColumnList mergedColumnList;

		if( columnList instanceof MergedColumnList )
			mergedColumnList = (MergedColumnList)((MergedColumnList)columnList).clone();
		else
			mergedColumnList = new MergedColumnList( columnList );

		if( columnList1 != null ) mergedColumnList.addAtFirst( columnList1 );
		if( columnList2 != null ) mergedColumnList.addAtLast( columnList2 );

		return mergedColumnList;
	}

	public static int getTitleRowCount( ColumnList columnList ) {
		Column[] columns = columnList.getColumns();

		for( int c = 0; c < columns.length; c++ ) {
			if( columns[c].getColumnGroup() != null )
				return 2;
		}

		return 1;
	}

	public static void writeTitle( DataWriter out, ColumnList columnList, MessageHandler msghandler, String ... titles ) throws IOException {
		Column[] columns = columnList.getColumns();

		int columnCount = ( titles == null ? 0 : titles.length );
		ColumnGroup columnGroup = null;
		for( int c = 0; c < columns.length; c++ ) {
			if( columns[c].getColumnGroup() == columnGroup )
				columnCount += columns[c].getColumnSize();
			else {
				if( columnGroup != null )
					out.print( columnGroup.getGroupTitle(null, msghandler), columnCount );
				else if( columnCount > 0 )
					out.printNull( columnCount );

				columnGroup = columns[c].getColumnGroup();
				columnCount = columns[c].getColumnSize();
			}
		}
		if( columnGroup != null ) {
			out.print( columnGroup.getGroupTitle(null, msghandler), columnCount );
			out.println();
		} else if( columnCount < columnList.getColumnSize() + (titles == null ? 0 : titles.length) ) {
			out.printNull( columnCount );
			out.println();
		}

		if( titles != null ) {
			for( int c = 0; c < titles.length; c++ )
				out.print( titles[c] );
		}
		for( int c = 0; c < columns.length; c++ ) out.print( columns[c].getColumnTitle(null, msghandler), columns[c].getColumnSize() );
		out.println();
	}
}
