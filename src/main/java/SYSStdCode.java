/*
 *	File Name:	SYSStdCode.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_code_name.jsp
 *		pub_code_select.jsp
 *		pub_list_count.jsp
 *		sys_code_list.jsp
 *		sys_stdcode_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.2	download() 추가
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경, log4j 사용
 *										createConditionMap(): new ParameterMap(req) -> new ParameterMap(req, true), throws IOException 삭제
 *										listCount(): 저장된 conditionMap이 없을 경우 처리
 *	stghr12		2007/04/30		2.1.0	com.irt.html.form.ColumnList -> com.irt.data.cols.ColumnList
 *										new DataException() -> ctx.handler.createDataException()
 *										Condition.putConditionValueOnly() 사용
 *										ServletUtility.setSort() 사용
 *										remove(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/10/14		2.0.1	remove()에서 getBackURL()이 없을 경우 NEEDED_PARAMETER 에러발생
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	version 관리
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
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/SYSStdCode"})
public class SYSStdCode extends AbstractServletModel {
	public final static String PARAM_UNITCLASS			= "unitcls";

	public final static String PARAM_CODETYPE			= "type";
	public final static String TYPE_COUNTRY				= "country";
	public final static String TYPE_LANGUAGE			= "lang";
	public final static String TYPE_CURRENCY			= "curr";
	public final static String TYPE_UNIT				= "unit";
	public final static String TYPE_HANDLINGINFO		= "handinfo";
	public final static String TYPE_PKGTYPE				= "pkgtype";
	public final static String TYPE_PKGMATERIAL			= "pkgmtr";
	public final static String TYPE_WEATHER				= "weather";
	public final static String TYPE_BANK				= "bank";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		String unitClass = conditionMap.removeParameter( PARAM_UNITCLASS );
		if( unitClass != null )
			Condition.putConditionValueOnly( conditionMap, "classCode", unitClass );

		return conditionMap;
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		StandardCode db = (StandardCode)ctx.db;

		DataWriter out = createDataWriter( ctx, "category" );
		com.irt.data.cols.ColumnList columnList
				= getColumnList( ctx, "SYSStdCode."+ ctx.extraObj +"%LIST", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		try {
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, null, columnList, StandardCode.OPT_WRITING_TITLE );
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
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("SYS", "SYSStdCode."+ ctx.extraObj +".MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/sys_stdcode_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// create db instance
		String codeType = ctx.req.getParameter( PARAM_CODETYPE );
		if( codeType == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( TYPE_COUNTRY.equals(codeType) )
			ctx.db = new CountryCode( ctx.handler );
		else if( TYPE_LANGUAGE.equals(codeType) )
			ctx.db = new LanguageCode( ctx.handler );
		else if( TYPE_CURRENCY.equals(codeType) )
			ctx.db = new CurrencyCode( ctx.handler );
		else if( TYPE_UNIT.equals(codeType) )
			ctx.db = new UnitCode( ctx.handler );
		else if( TYPE_HANDLINGINFO.equals(codeType) )
			ctx.db = new HandlingInfoCode( ctx.handler );
		else if( TYPE_PKGTYPE.equals(codeType) )
			ctx.db = new PackageTypeCode( ctx.handler );
		else if( TYPE_PKGMATERIAL.equals(codeType) )
			ctx.db = new PackageMaterialCode( ctx.handler );
		else if( TYPE_WEATHER.equals(codeType) )
			ctx.db = new WeatherCode( ctx.handler );
		else if( TYPE_BANK.equals(codeType) )
			ctx.db = new BankCode( ctx.handler );
		else
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		codeType = codeType.toUpperCase();

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSStdCode."+ codeType +".INF" );
		else if( MODE_SELECT.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSStdCode."+ codeType +".SEL" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSStdCode."+ codeType +".LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSStdCode."+ codeType +".MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSStdCode."+ codeType +".MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSStdCode."+ codeType +".MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "SYS", "SYSStdCode."+ codeType +".DWN" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		// setTitle & extraObject
		ctx.extraObj = codeType;
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_SYS_"+ codeType +"CODE_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		StandardCode db = (StandardCode)ctx.db;

		// conditionMap & columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "SYSStdCode."+ ctx.extraObj +"%LIST" );

		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("SYS", "SYSStdCode."+ ctx.extraObj +".MNG") );
		ctx.pageConfig.setAuthority( "download", ctx.sessionMng.isAuthorized("SYS", "SYSStdCode."+ ctx.extraObj +".DWN") );

		return forward( ctx, systemConfig.getJspPath() + "/sys_code_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("SYS", "SYSStdCode."+ ctx.extraObj +".LST")
				&& !ctx.sessionMng.isAuthorized("SYS", "SYSStdCode."+ ctx.extraObj +".SEL") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((StandardCode)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean name( Context ctx ) throws IOException, ServletException, SQLException {
		String code = ctx.req.getParameter( "code" );
		if( code != null && code.length() > 0 ) {
			List recordList = ((StandardCode)ctx.db).getRecords( createConditionMap(ctx), 0, 2 );
			if( recordList != null && recordList.size() == 1 ) ctx.req.setAttribute( "record", recordList.get(0) );
		}

		return forward( ctx, systemConfig.getJspPath() + "/pub_code_name.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		if( ctx.req.getAttribute("record") == null )
			ctx.req.setAttribute( "fieldSet", ((StandardCode)ctx.db).getFieldSet(true) );

		return forward( ctx, systemConfig.getJspPath() + "/sys_stdcode_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		StandardCode db = (StandardCode)ctx.db;

		String[] codes = ctx.req.getParameterValues( "code" );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( codes == null || codes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < codes.length; i++) {
			try {
				primaryMap.put( "code", codes[i] );
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
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
		List recordList = db.getRecords( createConditionMap(ctx), new String[] { "code", "name" } );
		ctx.pageConfig.getListIndexVariables()[1] = -1;
		ctx.pageConfig.getListIndexVariables()[2] = ( recordList == null ? 0 : recordList.size() );

		// setAttribute() & forward
		ctx.req.setAttribute( "records", recordList );

		return forward( ctx, systemConfig.getJspPath() + "/pub_code_select.jsp" );
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

			return forward( ctx, systemConfig.getJspPath() + "/sys_stdcode_input.jsp" );
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
