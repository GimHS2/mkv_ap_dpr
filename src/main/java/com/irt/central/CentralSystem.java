/*
 *	File Name:	CentralSystem.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	guksm		2008/09/26		2.2.0	create
 *
**/

package com.irt.central;

import java.sql.*;

/**
 *
 */
public class CentralSystem {
	public static String CONN			= "jdbc:oracle:thin:@10.35.34.14:2117:APMASQ";
	public static String ID				= "IRTSYACT";
	public static String PW				= "Tac0_B3ll";

	public CentralSystem() {}

	public static Connection openConnection() throws SQLException {
		DriverManager.registerDriver( new oracle.jdbc.driver.OracleDriver() );
		Connection conn = DriverManager.getConnection( CONN, ID, PW );
		if( conn != null )
			conn.setAutoCommit( false );

		return conn;
	}
}
