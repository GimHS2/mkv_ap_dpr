/*
 *	File Name:	ServiceGroupPackage.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	createPrimary(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/02/28		2.0.0	version up(ServiceGroupPkg -> ServiceGroupPackage)
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	kdmcom		2002/12/20				create
 *
**/

package com.irt.rbm.sys;

import com.irt.sql.*;
import java.util.Map;

/**
 *
 */
public class ServiceGroupPackage extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.SYS_SERVICEGRP_PACKAGE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_SERVICEGRP_PACKAGE );

	public ServiceGroupPackage( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String serviceGroupCode, String systemCode, String packageCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "serviceGroupCode", serviceGroupCode );
		primaryMap.put( "systemCode", systemCode );
		primaryMap.put( "packageCode", packageCode );

		return primaryMap;
	}
}
