/*
 *	File Name:	SYSPostalCode.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		pub_list_count.jsp
 *		sys_post_select.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										createConditionMap(): throws IOException 삭제
 *										listCount(): 저장된 conditionMap이 없을 경우 처리
 *										countryCode 추가
 *	stghr12		2006/12/01		2.1.0	ServletUtility.setSort() 사용
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/04		1.0.0	version 관리
 *	stghr12		2002/04/15				create
 *
**/

import com.irt.rbm.sys.PostalCode;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/SYSPostalCode"})
public class SYSPostalCode extends AbstractServletModel {
	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		String countryCode = ctx.req.getParameter( "countryCode" );
		if( countryCode == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String code = ctx.req.getParameter( "code" );
		String street = ctx.req.getParameter( "street" );
		String postalCode = ctx.req.getParameter( "postalCode" );

		conditionMap.put( "countryCode", countryCode );
		if( code != null ) {
			conditionMap.put( "code", code );
			conditionMap.put( "code" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_STARTSWITH );
		}
		if( postalCode != null ) ctx.pageConfig.setProperty( "postalCode", postalCode );
		if( street != null ) {
			conditionMap.put( "street", street );
			conditionMap.put( "street" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_CONTAINS );
			ctx.pageConfig.setProperty( "street", street );
		}

		return conditionMap;
	}

	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) ctx.pageConfig.setMode( ctx.mode = MODE_SELECT );
		if( MODE_SELECT.equals(ctx.mode) )
			ctx.pageConfig.setSystemPackageCode( "SYS", "SYSPostalCode.SEL" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			ctx.pageConfig.setSystemPackageCode( "SYS", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		// create db instance & setTitle
		ctx.db = new PostalCode( ctx.handler );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_SYS_POSTALCODE_SEL") );
	}

	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("SYS", "SYSPostalCode.SEL") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((PostalCode)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	protected boolean select( Context ctx ) throws IOException, ServletException, SQLException {
		PostalCode db = (PostalCode)ctx.db;

		// conditionMap
		Map<String, Object> conditionMap = createConditionMap( ctx );
		if( conditionMap.size() > 1 ) {		// countryCode외
			// records 읽기 & conditionMap 저장
			int[] idxVars = ctx.pageConfig.getListIndexVariables();
			ServletUtility.setSort( ctx.req, db );
			List recordList = db.getRecords( conditionMap, idxVars[0], idxVars[1] );
			String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

			// setAttribute()
			ctx.req.setAttribute( "records", recordList );
			if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		} else
			ctx.pageConfig.getListIndexVariables()[2] = 0;

		return forward( ctx, systemConfig.getJspPath() + "/sys_post_select.jsp" );
	}
}
