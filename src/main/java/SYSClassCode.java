/*
 *	File Name:	SYSClassCode.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_levelcode_name.jsp
 *		pub_levelcode_select.jsp
 *		pub_list_count.jsp
 *		sys_classcode_input.jsp
 *		sys_classcode_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.2	download() 추가
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										setAttribute("field") -> setAttribute("codeField")
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경, log4j 사용
 *										createConditionMap(): new ParameterMap(req) -> new ParameterMap(req, true), throws IOException 삭제
 *										listCount(): 저장된 conditionMap이 없을 경우 처리
 *	stghr12		2007/04/30		2.1.0	com.irt.html.form.ColumnList -> com.irt.data.cols.ColumnList
 *										new DataException() -> ctx.handler.createDataException()
 *										Condition.putConditionValueOnly() 사용
 *										ServletUtility.setSort() 사용
 *										remove(): java.util.HashMap -> java.util.TreeMap
 *										list(): setAttribute("codeDB") 추가
 *	stghr12		2006/10/14		2.0.1	remove()에서 getBackURL()이 없을 경우 NEEDED_PARAMETER 에러발생
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	stghr12		2002/04/15				ECSCode create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataWriter;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.sys.*;
import com.irt.servlet.*;
import com.irt.sql.HierarchyCodeField;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/SYSClassCode"})
public class SYSClassCode extends AbstractServletModel {
	public final static String PARAM_CODETYPE			= "type";
	public final static String TYPE_HS					= "hs";
	public final static String TYPE_ICATE				= "icate";
	public final static String TYPE_CATE				= "cate";
	public final static String TYPE_SPSC				= "spsc";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ClassCode db = (ClassCode)ctx.db;
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );
		HierarchyCodeField field = db.getCodeField();

		String name = conditionMap.removeParameter( "name.all" );
		if( name != null ) {
			Condition.putConditionValueOnly( conditionMap, "name", name );
			conditionMap.put( "name" + Condition.SUFFIX_TYPE, conditionMap.removeParameter("name.all"+ Condition.SUFFIX_TYPE) );
		} else {
			String code = conditionMap.removeParameter( "code" );
			String slcode = conditionMap.removeParameter( "slcode" );
			if( code == null && slcode != null )
				code = field.getUpperLevelCode( slcode );
			if( code != null )
				Condition.putConditionValueOnly( conditionMap, "code", code, Condition.CONDTYPE_STARTSWITH );

			ctx.pageConfig.setProperty( "upperCode", code == null ? "" : code );
			int level = ( code == null ? 1 : field.getLevel(code) + 1 );
			Condition.putConditionValueOnly( conditionMap, "classCode", String.valueOf(level) );

			if( level > 1 ) {
				String[] ucodes = field.getUpperLevelCodes( code, true );

				db.setSort( "code" );
				List recordList = db.getRecords( ServletUtility.createMap("code", ucodes), new String[] { "code", "name" } );
				if( recordList != null ) ctx.req.setAttribute( "uppers", recordList );
			}
		}

		return conditionMap;
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		ClassCode db = (ClassCode)ctx.db;

		DataWriter out = createDataWriter( ctx, "category" );
		try {
			ServletUtility.setSort( ctx.req, db, new String[] { "code" } );
			db.write( out, null, new String[] { "classCode", "code", "name" }, ClassCode.OPT_WRITING_TITLE | ClassCode.OPT_WRITING_VALUE_LITERALLY );
		} catch( SQLException sqlEx ) {
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error( "internal error.", sqlEx );
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		StandardCode db = (StandardCode)ctx.db;

		// primaryKey 생성
		String code = ctx.req.getParameter( "code" );
		if( code == null || code.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		Map<String, Object> primaryMap = StandardCode.createPrimary( code );

		// recordMap 읽기 & setAttribute
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		// forward
		if( inputting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("SYS", "SYSClassCode."+ ctx.extraObj +".MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/sys_classcode_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// create db instance
		String codeType = ctx.req.getParameter( PARAM_CODETYPE );
		if( codeType == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( TYPE_HS.equals(codeType) )
			ctx.db = new HSCode( ctx.handler );
		else if( TYPE_ICATE.equals(codeType) )
			ctx.db = new ICategoryCode( ctx.handler );
		else if( TYPE_SPSC.equals(codeType) )
			ctx.db = new SPSCCode( ctx.handler );
		else if( TYPE_CATE.equals(codeType) && CategoryCode.useCategoryCode() )
			ctx.db = new CategoryCode( ctx.handler );
		else
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		codeType = codeType.toUpperCase();

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSClassCode."+ codeType +".INF" );
		else if( MODE_SELECT.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSClassCode."+ codeType +".SEL" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSClassCode."+ codeType +".LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSClassCode."+ codeType +".MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSClassCode."+ codeType +".MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSClassCode."+ codeType +".MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSClassCode."+ codeType +".DWN" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		// setTitle & extraObject
		ctx.extraObj = codeType;
		ctx.req.setAttribute( "codeField", ((ClassCode)ctx.db).getCodeField() );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_SYS_"+ codeType +"CODE_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		StandardCode db = (StandardCode)ctx.db;

		// conditionMap & columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList;
		String classCode = (String)conditionMap.get( "classCode" );
		HierarchyCodeField field = ((ClassCode)db).getCodeField();
		if( classCode != null && field.getLastLevel() == Integer.parseInt(classCode) )
			columnList = getColumnList( ctx, "SYSClassCode."+ ctx.extraObj +"%LIST.LAST" );
		else
			columnList = getColumnList( ctx, "SYSClassCode."+ ctx.extraObj +"%LIST" );

		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		// setAttribute()
		ctx.req.setAttribute( "codeDB", db );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("SYS", "SYSClassCode."+ ctx.extraObj +".MNG") );
		ctx.pageConfig.setAuthority( "download", ctx.sessionMng.isAuthorized("SYS", "SYSClassCode."+ ctx.extraObj +".DWN") );

		return forward( ctx, systemConfig.getJspPath() + "/sys_classcode_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("SYS", "SYSClassCode."+ ctx.extraObj +".LST")
				&& !ctx.sessionMng.isAuthorized("SYS", "SYSClassCode."+ ctx.extraObj +".SEL") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((StandardCode)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean name( Context ctx ) throws IOException, ServletException, SQLException {
		String code = ctx.req.getParameter( "code" );
		if( code != null && code.length() > 0 ) {
			List recordList = ((StandardCode)ctx.db).getRecords( new ParameterMap(ctx.req), 0, 2 );
			if( recordList != null && recordList.size() == 1 ) ctx.req.setAttribute( "record", recordList.get(0) );
		}

		return forward( ctx, systemConfig.getJspPath() + "/pub_levelcode_name.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		if( ctx.req.getAttribute("record") == null ) {
			HierarchyCodeField field = ((ClassCode)ctx.db).getCodeField();
			String code = ctx.req.getParameter( "code" );

			Map<String, Object> recordMap = new java.util.TreeMap<String, Object>();
			recordMap.put( "code", code );
			recordMap.put( "classCode", String.valueOf(field.getLevel(code) + 1) );

			ctx.req.setAttribute( "record", recordMap );
			ctx.req.setAttribute( "fieldSet", ((StandardCode)ctx.db).getFieldSet(true) );
		}

		return forward( ctx, systemConfig.getJspPath() + "/sys_classcode_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		ClassCode db = (ClassCode)ctx.db;

		String[] codes = ctx.req.getParameterValues( "code" );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( codes == null || codes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < codes.length; i++) {
			try {
				count += db.delete( codes[i] );
				ctx.handler.commit();
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getName(codes[i]), dataEx) );
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	@Override
	protected boolean select( Context ctx ) throws IOException, ServletException, SQLException {
		StandardCode db = (StandardCode)ctx.db;

		// records 읽기
		ServletUtility.setSort( ctx.req, db, "code" );
		List recordList = db.getRecords( createConditionMap(ctx) );
		ctx.pageConfig.getListIndexVariables()[1] = -1;
		ctx.pageConfig.getListIndexVariables()[2] = ( recordList == null ? 0 : recordList.size() );

		// setAttribute() & forward
		ctx.req.setAttribute( "records", recordList );

		return forward( ctx, systemConfig.getJspPath() + "/pub_levelcode_select.jsp" );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		StandardCode db = (StandardCode)ctx.db;

		// 레코드 읽기
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		try {
			if( inserting ) {
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			ctx.req.setAttribute( "record", db.getRecord(recordMap) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/sys_classcode_input.jsp" );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_SYS_"+ ctx.extraObj +"CODE_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}
}
