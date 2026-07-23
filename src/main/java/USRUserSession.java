/*
 *	File Name:	USRUserSession.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		pub_list_count.jsp
 *		usr_usersession_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										createConditionMap(): new ParameterMap(req) -> new ParameterMap(req, true), throws IOException 삭제
 *										listCount(): 저장된 conditionMap이 없을 경우 처리
 *	stghr12		2007/04/30		2.1.0	com.irt.html.form.ColumnList -> com.irt.data.cols.ColumnList
 *										Condition.putConditionValueOnly() 사용
 *										ServletUtility.getSortKeys() 사용
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2005/01/11		1.0.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.rbm.usr.UserSession;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/USRUserSession"})
public class USRUserSession extends AbstractServletModel {
	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "status", "00" );
		if( !ctx.sessionMng.isSystemAdmin() )
			Condition.putConditionValueOnly( conditionMap, "partyId", ctx.sessionMng.getPartyId() );

		return conditionMap;
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", "USRUserSession.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "USR", null );
		else{
			throw new ServletModelException( ServletModelException.INVALID_MODE );
		}

		ctx.db = new UserSession( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_USR_USERSESSION_"+ ctx.mode.toUpperCase()) );
		setPath( ctx, "jsp.MENU_USER", "jsp.SUBMENU_USERSESSION" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		UserSession db = (UserSession)ctx.db;

		// conditionMap & columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList;
		if( conditionMap.get("partyId") != null )
			columnList = getColumnList( ctx, "USRUserSession%LIST" );
		else
			columnList = getColumnList( ctx, "USRUserSession%LIST.ALL" );

		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		db.setSort( ServletUtility.getSortKeys(ctx.req, columnList.getSortKeys()) );
		List recordList = db.getCurrentUsers( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		return forward( ctx, systemConfig.getJspPath() + "/usr_usersession_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("USR", "USRUserSession.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((UserSession)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}
}
