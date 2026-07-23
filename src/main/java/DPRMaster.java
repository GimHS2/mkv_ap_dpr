/*
 *	File Name:	DPRMaster.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.PartyMaster;
import com.irt.servlet.*;
import com.irt.util.Utility;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRMaster"})
public class DPRMaster extends DPRServletModel {
	protected static final String MASTERTYPE_PARTY				= "P";
	protected static final String MASTERTYPE_ITEM				= "I";

	private final int MASTERIDX_PARTY					= 0;
	private final int MASTERIDX_ITEM					= 1;

	private final String DATATYPE_CUSTOMERGROUP			= "CG";
	private final String DATATYPE_DIVISION				= "DI";
	private final String DATATYPE_REGION				= "RG";
	private final String DATATYPE_DISTRIBUTION_CHANNEL	= "DC";
	private final String DATATYPE_SALES_DISTRICT		= "SD";
	private final String DATATYPE_SALES_ORGANIZATION	= "SO";
	private final String DATATYPE_SALESOFFICE			= "SF";
	private final String DATATYPE_SALESGROUP			= "SG";
	private final String DATATYPE_BASEPRODUCT			= "BP";
	private final String DATATYPE_MEGABRAND				= "MB";
	private final String DATATYPE_BRAND					= "BR";
	private final String DATATYPE_VARIANT				= "VA";
	private final String DATATYPE_PUTUP					= "PU";
	private final String DATATYPE_PRODUCT_CATEGORY		= "PC";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		String types[] = getTypes( ctx );
		String masterType = types[MASTERIDX_PARTY];
		String dataType = types[MASTERIDX_ITEM];

		Map fieldMap = (Map)ctx.extraObj;

		String[] dataTypes;
		if( dataType != null && dataType.indexOf(";") > 0 )
			dataTypes = dataType.split(";");
		else
			dataTypes = new String[] { dataType };

//		if( !conditionMap.containsKey("displayLanguage") )
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		for( int i = 0; i < dataTypes.length; i++ ) {
			if( conditionMap.containsKey( "exclusiveCode") ) {
				String[] fieldKey = (String [])fieldMap.get( dataTypes[i] );
				Object exclusiveCode  = conditionMap.get( "exclusiveCode" );

				conditionMap.put( fieldKey[0], exclusiveCode );
				conditionMap.put( fieldKey[0] + com.irt.data.Condition. SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_NOTEQUALS );
			}

			if( conditionMap.containsKey("code") ) {
				String[] fieldKey = (String [])fieldMap.get( dataTypes[i] );
				Object code  = conditionMap.get( "code" );

				conditionMap.put( fieldKey[0], code );
			}

			if( conditionMap.containsKey("name") ) {
				String[] fieldKey = (String [])fieldMap.get( dataTypes[i] );
				String name  = (String)conditionMap.get( "name" );

				conditionMap.put( fieldKey[1], name );
			}
		}

		return conditionMap;
	}

	private String[] getTypes( Context ctx ) throws ServletModelException {
		String[] types = new String[2];

		String nameClass = ctx.req.getParameter( "namecls" );
		if( nameClass != null ) {
			if( nameClass.length() == 4 ) {
				types = nameClass.split( "," );
			} else {
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			}
		} else {
			types[MASTERIDX_PARTY] = ctx.req.getParameter( "mastertype" );
			types[MASTERIDX_ITEM] = ctx.req.getParameter( "datatype" );
		}

		return types;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_COND.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_SELECT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		Map<String, String[]> fieldMap = new java.util.HashMap<String, String[]> ();
		fieldMap.put( DATATYPE_CUSTOMERGROUP, new String[] { "customerGroupCode", "customerGroupName" } );
		fieldMap.put( DATATYPE_DIVISION, new String[] { "divisionCode", "divisionName" } );
		fieldMap.put( DATATYPE_REGION, new String[] { "regionCode", "regionName" } );
		fieldMap.put( DATATYPE_DISTRIBUTION_CHANNEL, new String[] { "channelCode", "channelName" } );
		fieldMap.put( DATATYPE_SALES_DISTRICT, new String[] { "districtCode", "districtName" } );
		fieldMap.put( DATATYPE_SALES_ORGANIZATION, new String[] { "organizationCode", "organizationName" } );
		fieldMap.put( DATATYPE_SALESOFFICE, new String[] { "officeCode", "officeName" } );
		fieldMap.put( DATATYPE_SALESGROUP, new String[] { "groupCode", "groupName" } );

		String[] fieldKey = new String[] { "code", "name"  };
		fieldMap.put( DATATYPE_BASEPRODUCT, fieldKey );
		fieldMap.put( DATATYPE_MEGABRAND, fieldKey );
		fieldMap.put( DATATYPE_BRAND, fieldKey );
		fieldMap.put( DATATYPE_VARIANT, fieldKey );
		fieldMap.put( DATATYPE_PUTUP, fieldKey );
		fieldMap.put( DATATYPE_PRODUCT_CATEGORY, fieldKey );

		ctx.extraObj = fieldMap;

		String types[] = getTypes( ctx );
		String masterType = types[MASTERIDX_PARTY];
		String dataType = types[MASTERIDX_ITEM];

		if( masterType == null || masterType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( dataType == null || dataType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( MASTERTYPE_PARTY.equals(masterType) ) {
			int idx = 0;
			if( DATATYPE_CUSTOMERGROUP.equals(dataType) )
				idx = PartyMaster.IDX_CUSTOMER_GROUP;
			else if( DATATYPE_DIVISION.equals(dataType) )
				idx = PartyMaster.IDX_DIVISION;
			else if( DATATYPE_REGION.equals(dataType) )
				idx = PartyMaster.IDX_REGION;
			else if( DATATYPE_DISTRIBUTION_CHANNEL.equals(dataType) )
				idx = PartyMaster.IDX_DISTRIBUTIONCHANNEL;
			else if( DATATYPE_SALES_DISTRICT.equals(dataType) )
				idx = PartyMaster.IDX_SALES_DISTRICT;
			else if( DATATYPE_SALES_ORGANIZATION.equals(dataType) )
				idx = PartyMaster.IDX_SALES_ORGANIZATION;
			else if( DATATYPE_SALESOFFICE.equals(dataType) )
				idx = PartyMaster.IDX_SALES_OFFICE;
			else if( DATATYPE_SALESGROUP.equals(dataType) )
				idx = PartyMaster.IDX_SALES_GROUP;

			ctx.db = new PartyMaster( ctx.handler, idx );
			pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PARTYMASTER_"+ ctx.mode.toUpperCase()) );
		} else {
//			ctx.db = new ItemMaster( ctx.handler,  );
			pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ITEMMASTER_"+ ctx.mode.toUpperCase()) );
		}
	}

	@Override
	protected boolean name( Context ctx ) throws ServletException, SQLException, IOException {
		Map<String, Object> conditionMap = createConditionMap( ctx );

		String types[] = getTypes( ctx );
		String masterType = types[MASTERIDX_PARTY];
		String dataType = types[MASTERIDX_ITEM];

		Map<String, String[]> fieldMap = (Map<String,String[]>)ctx.extraObj;
		String[] fieldKeys = fieldMap.get( dataType );
		if( fieldKeys == null || fieldKeys.length == 0 )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		String listName = ctx.req.getParameter( "listname" );
		if( MASTERTYPE_PARTY.equals(masterType) )
			ctx.req.setAttribute( listName, ((PartyMaster)ctx.db).getRecords( conditionMap, fieldKeys, 0, -1 ) );

		String jspName = ctx.req.getParameter( "jspname" );
		if( jspName == null || jspName.length() == 0 || !Utility.isSafeFileName(jspName) )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		return forward( ctx, systemConfig.getJspPath() +"/"+ jspName +".jsp" );
	}

	@Override
	protected boolean select( Context ctx ) throws ServletException, SQLException, IOException {
		Map<String, Object> conditionMap = createConditionMap( ctx );

		String types[] = getTypes( ctx );
		String masterType = types[MASTERIDX_PARTY];
		String dataType = types[MASTERIDX_ITEM];
		String listName = ctx.req.getParameter( "listname" );

		String[] dataTypes;
		if( dataType != null && dataType.indexOf(";") > 0 )
			dataTypes = dataType.split(";");
		else
			dataTypes = new String[] { dataType };

		String[] listNames;
		if( listName != null && listName.indexOf(";") > 0 )
			listNames = listName.split(";");
		else
			listNames = new String[] { listName };

		if( dataTypes.length != listNames.length )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		Map<String, String[]> fieldMap = (Map<String,String[]>)ctx.extraObj;
		for( int i = 0; i < dataTypes.length; i++ ) {
			conditionMap.put( "dataType", dataTypes[i] );

			String[] fieldKeys = fieldMap.get( dataTypes[i] );
			if( fieldKeys == null || fieldKeys.length == 0 )
				throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
			if( MASTERTYPE_PARTY.equals(masterType) ) {
				PartyMaster db = new PartyMaster( ctx.handler, getDataIndex(dataTypes[i]) );

				ctx.req.setAttribute( listNames[i], db.getRecords( conditionMap, fieldKeys, 0, -1 ) );
			}
		}

		String jspName = ctx.req.getParameter( "jspname" );
		if( jspName == null || jspName.length() == 0 || !Utility.isSafeFileName(jspName) )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		return forward( ctx, systemConfig.getJspPath() +"/" + (jspName != null ? jspName + ".jsp" : "dpr_master_select.jsp") );
	}

	private int getDataIndex( String dataType ) {
		int idx = 0;
		if( DATATYPE_CUSTOMERGROUP.equals(dataType) )
			idx = PartyMaster.IDX_CUSTOMER_GROUP;
		else if( DATATYPE_DIVISION.equals(dataType) )
			idx = PartyMaster.IDX_DIVISION;
		else if( DATATYPE_REGION.equals(dataType) )
			idx = PartyMaster.IDX_REGION;
		else if( DATATYPE_DISTRIBUTION_CHANNEL.equals(dataType) )
			idx = PartyMaster.IDX_DISTRIBUTIONCHANNEL;
		else if( DATATYPE_SALES_DISTRICT.equals(dataType) )
			idx = PartyMaster.IDX_SALES_DISTRICT;
		else if( DATATYPE_SALES_ORGANIZATION.equals(dataType) )
			idx = PartyMaster.IDX_SALES_ORGANIZATION;
		else if( DATATYPE_SALESOFFICE.equals(dataType) )
			idx = PartyMaster.IDX_SALES_OFFICE;
		else if( DATATYPE_SALESGROUP.equals(dataType) )
			idx = PartyMaster.IDX_SALES_GROUP;
		else
			idx = PartyMaster.IDX_CUSTOMER_GROUP;

		return idx;
	}
}
