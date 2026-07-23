/*
 *	File Name:	OrderCanonicalProcessTools.java
 *	Version:	2.2.1(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.1	org.slf4j.MDC 적용
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.dpr.OrderProcessException;
import com.irt.system.SystemConfig;
import com.irt.sql.SQLHandler;
import java.util.Map;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.*;


public class OrderCanonicalProcessTools extends com.irt.rbm.tools.RBMTools {
	private final static OrderCanonicalProcessTools tools = new OrderCanonicalProcessTools();

	SystemConfig rbm_systemConfig, apmasq_systemConfig;
	Map<String, Object> infoMap;

	@Override
	public void execute( String[] commands ) throws IllegalArgumentException, SQLException {
		if( commands[0] != null && "h".equals(commands[0]) ) {
			printOrderProcessUsage();

			return;
		}

		Logger logger = Logger.getRootLogger();
		SQLHandler rbm_handler = null;
		try {
			rbm_handler = rbm_systemConfig.createSQLHandler( rbm_systemConfig.getMessageHandler() );
			OrderCanonicalProcess create_process = new OrderCanonicalProcess( rbm_handler, null, commands[0] );
			create_process.setConnectionInfo( infoMap );

			if( OrderCanonicalProcess.ORDER_IF_STATUSLIST.equals(commands[0]) ) {
				try {
					com.irt.data.Date startDate = com.irt.data.Date.getInstance( commands[1] );
					create_process.setParameter( "startDate", startDate );
					if( commands[2] != null && commands[2].length() > 0 ) {
						com.irt.data.Date endDate = com.irt.data.Date.getInstance( commands[2] );
						endDate = endDate.getDate(1);
						create_process.setParameter( "endDate", commands[2] );
					}

					create_process.setParameter( "organizationCode", commands[3] );
					create_process.setParameter( "soldPartyCode", commands[4] );
					create_process.setParameter( "updateUserId", commands[5] );
					create_process.setParameter( "countryCode", commands[6] );

					create_process.execute();
				} catch( java.text.ParseException parseEx ) {
					throw new IllegalArgumentException ( "OrderProcess["+ commands[0] + "] : Parameter is not valid:"
							+ commands[1] + "," + commands[2] );
				}
			} else if( OrderCanonicalProcess.ORDER_IF_STATUS.equals(commands[0]) ) {
				create_process.setParameter( "orderNumber", commands[1] );
				create_process.setParameter( "soldPartyCode", commands[2] );
				create_process.setParameter( "uniqId", commands[3] );
				create_process.setParameter( "countryCode", commands[4] );

				create_process.execute();
			} else if( OrderCanonicalProcess.ORDER_IF_RDD.equals(commands[0]) ) {
				/* portalUser */
/* WORKING:
				com.irt.data.Date orderDate = null;
				try {
					orderDate = com.irt.data.Date.getInstance( commands[1] );
				} catch( java.text.ParseException parseEx ) {
					if( commands[1] == null ) {
						orderDate = com.irt.data.Date.getInstance();
					} else
						throw new IllegalArgumentException ( "OrderProcess["+ commands[0] + "] : ORDDATE Parameter is not valid:" + commands[1] );
				}
				create_process.setParameter( "orderDate", orderDate );
				create_process.setParameter( "soldToParty", commands[2] );
				create_process.setParameter( "shipToParty", commands[3] );
				create_process.setParameter( "organizationCode", commands[4] );
				create_process.setParameter( "distributionChannelCode"
						, (commands[5] == null || commands[5].length() == 0 ? "11" : commands[5]) );
				create_process.setParameter( "divisionCode"
						, (commands[6] == null || commands[6].length() == 0 ? "10" : commands[6]) );
*/
				create_process.setParameter( "orderKey", commands[1] );
				create_process.setParameter( "countryCode", commands[2] );

				create_process.execute();
			} else {
				if( commands[1] == null || commands[1].length() == 0 )
					throw new IllegalArgumentException ( "OrderProcess["+ commands[0] + "] : Mandatory parameter is null: orderKey" );

				create_process.setParameter( "orderKey", commands[1] );
				create_process.setParameter( "countryCode", commands[2] );

				create_process.execute();
			}
		} catch( OrderProcessException orderEx ) {
			logger.error( "Can't running Order Process", orderEx );
		} finally {
			if( rbm_handler != null ) rbm_handler.close();
			org.slf4j.MDC.remove("uniqId");
		}
	}

	@Override
	public String getName() {
		return "WMTools";
	}

	@Override
	public void init( Properties properties ) throws Exception {
		super.init( properties );

		rbm_systemConfig = configure.getSystemConfig( "RBM" );
		if( rbm_systemConfig == null ) throw new java.io.IOException( "RBM SystemConfig can't be found." );

		com.irt.dpr.tools.Configure local_configure = new com.irt.dpr.tools.Configure( configure );

		infoMap = local_configure.getSystemInfo();
		if( infoMap == null || infoMap.size() == 0 ) throw new java.io.IOException( "WM Connection can't be found." );
	}

	public void printOrderProcessUsage() {
		System.out.println( "Order Simulation Usage : " + OrderCanonicalProcess.ORDER_IF_SIMULATION + " ORDER_KEY | COUNTRYCD " );
		System.out.println( "Order Creation Usage : " + OrderCanonicalProcess.ORDER_IF_CREATION + " ORDER_KEY | COUNTRYCD " );
		System.out.println( "Order Status Usage : " + OrderCanonicalProcess.ORDER_IF_STATUS + " ORDER_NUMBER | SoldPartyCode | UserUniqID | COUNTRYCD" );
		System.out.println( "Order Status List Usage : " + OrderCanonicalProcess.ORDER_IF_STATUSLIST
				+ " START_DATE(YYYYMMDD) | [END_DATE(YYYYMMDD)] | organizationCode | soldPartyCode | UserUniqID | COUNTRYCD" );
		System.out.println( "Order RDD Usage : " + OrderCanonicalProcess.ORDER_IF_RDD + " portalUser |  SHIP-TO | "
				+ " SALES_ORG | DIST_CH | DIVISION | COUNTRYCD" );
	}

	public static void main( String[] args ) throws Exception {
		if( args.length > 0 && "stop".equals(args[0]) )
			tools.stop();
		else
			executeTools( tools, args );
	}
}
