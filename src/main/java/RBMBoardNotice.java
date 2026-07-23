/*
 *	File Name:	RBMBoardNotice.java
 *	Version:	2.2.4(dpr)
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		rbm_boardnotice_info.jsp
 *		rbm_boardnotice_input.jsp
 *		rbm_boardnotice_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.4	신규 UI/UX 적용
 *	lsinji		2008/09/26		2.2.3	BOARDCLASSCODE_MAIN modify
 *	stghr12		2008/06/13		2.2.2	registInput(): 유통업체, 로케이션를 가져오지 않는 오류 수정
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										boardClassCode 처리방법 변경
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경, log4j 사용
 *										createConditionMap(): new ParameterMap(req) -> new ParameterMap(req, true), throws IOException 삭제
 *										listCount(): 저장된 conditionMap이 없을 경우 처리
 *										com.irt.data.Timestamp 사용
 *	stghr12		2007/04/30		2.1.0	com.irt.html.form.ColumnList -> com.irt.data.cols.ColumnList
 *										new DataException() -> ctx.handler.createDataException()
 *										Condition.putConditionValueOnly() 사용
 *										"useReadedUser"값에 따라, ColumnList Option 처리
 *	stghr12		2006/11/30		2.0.2	오타수정: "boradNumber" -> "boardNumber"
 *	stghr12		2006/10/14		2.0.1	remove()에서 getBackURL()이 없을 경우 NEEDED_PARAMETER 에러발생
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/11/22		1.0.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.Board;
import com.irt.rbm.rbm.BoardClass;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/RBMBoardNotice"})
public class RBMBoardNotice extends AbstractServletModel {
	public final static String BOARDCLASSCODE_MAIN		= "NO";
	public final static String NOTICEMANAGE				= "ALL";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "boardClassCode", ctx.extraObj );
		Condition.putConditionValueOnly( conditionMap, "userId", ctx.sessionMng.getUniqId() );

		if( !NOTICEMANAGE.equals( conditionMap.get("noticeManage") )) {
			com.irt.data.Date sysDate = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
			conditionMap.put( "noticeDate", sysDate );
		}

		return conditionMap;
		}

	private List<Map<String, Object>> getBoardClassCodeList( Context ctx ) throws SQLException {
		BoardClass db = new BoardClass( ctx.handler );

		Map<String, Object> conditionMap = ServletUtility.createMap( "boardClassCode", BOARDCLASSCODE_MAIN );
		List<Map<String, Object>> records = db.getRecords( conditionMap );

		conditionMap.put( "boardClassCode", BOARDCLASSCODE_MAIN + "." );
		conditionMap.put( "boardClassCode"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_STARTSWITH );
		List<Map<String, Object>> records_sub = db.getRecords( conditionMap );

		if( records_sub != null ) {
			if( records != null )
				records.addAll( records_sub );
			else
				records = records_sub;
		}
		if( records == null ) return null;

		for( java.util.Iterator<Map<String, Object>> iterator = records.iterator(); iterator.hasNext(); ) {
			String boardClassCode = (String)iterator.next().get( "boardClassCode" );

			if( !ctx.sessionMng.isAuthorized("RBM", "RBMBoard."+ boardClassCode +".LST") )
				iterator.remove();
		}

		return ( records.size() == 0 ? null : records );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "RBM" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// boardClassCode
		String boardClassCode = ctx.req.getParameter( "boardClassCode" );
		try {
			List<Map<String, Object>> boardClassCodeList = getBoardClassCodeList( ctx );

			if( boardClassCodeList != null ) {
				if( boardClassCode == null )
					boardClassCode = (String)boardClassCodeList.get(0).get( "boardClassCode" );
				if( boardClassCodeList.size() > 1 )
					ctx.req.setAttribute( "boardClasses", boardClassCodeList );
			}
		} catch( SQLException sqlEx ) {
			throw new ServletModelException( sqlEx );
		}
		pageConfig.setProperty( "boardClassCode", boardClassCode );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard."+ BOARDCLASSCODE_MAIN +".INF" );
		else if( MODE_LIST.equals(ctx.mode) || MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard."+ BOARDCLASSCODE_MAIN +".LST" );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard."+ BOARDCLASSCODE_MAIN +".MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard."+ BOARDCLASSCODE_MAIN +".MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard."+ BOARDCLASSCODE_MAIN +".MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new Board( ctx.handler );
		ctx.extraObj = boardClassCode;
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_RBM_BOARD_NOTICE_" + ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		String boardClassCode = (String)ctx.extraObj;

		// primaryKey 생성
		int boardNumber = 0;
		String userId = ctx.sessionMng.getUniqId();
		try {
			String boardNumberStr = ctx.req.getParameter( "boardNumber" );
			if( boardNumberStr == null || boardNumberStr.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			boardNumber = Integer.parseInt( boardNumberStr );
		} catch( NumberFormatException numEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}
		Map<String, Object> primaryMap = Board.createPrimary( boardClassCode, boardNumber, userId );

		// recordMap 읽기 & setAttribute
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap = setLocationInformation( ctx, recordMap ) );

		try {
			db.increaseViewCount( boardClassCode, boardNumber, userId );
		} catch( DataException dataEx ) {
			logger.error( "internal error.", dataEx );
		} catch( SQLException sqlEx ) {
			logger.error( "internal error.", sqlEx );
		}

		// forward
		boolean hasManageAuth = userId.equals( recordMap.get("registUserId") );
		if( inputting ) {
			if( !hasManageAuth ) return false;
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

			return registInput( ctx );
		} else {
			ctx.req.setAttribute( "prevRecord", db.getRecord( primaryMap, -1 ) );
			ctx.req.setAttribute( "nextRecord", db.getRecord( primaryMap, 1 ) );
			ctx.pageConfig.setManageAuth( hasManageAuth && ctx.sessionMng.isAuthorized("RBM", "RBMBoard."+ boardClassCode +".MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			String type = ctx.req.getParameter( "type" );
			if( "dashboard".equals(type) ) {
				return forward( ctx, systemConfig.getJspPath() + "/rbm_boardnotice_info2.jsp" );
			} else {
				return forward( ctx, systemConfig.getJspPath() + "/rbm_boardnotice_info.jsp" );
			}
		}
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		String boardClassCode = (String)ctx.extraObj;
		String noticeType = "%LIST";

		Map<String, Object> board = db.getBoardClass( boardClassCode );
		boolean usingReadedUser = ( board != null && "Y".equals(board.get("useReadedUser")) );

		// conditionMap, columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );
		if( NOTICEMANAGE.equals( conditionMap.get("noticeManage") ))
			noticeType = noticeType + "." + NOTICEMANAGE;
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "RBMBoard." + boardClassCode + noticeType, usingReadedUser ? "RD_Y" : "RD_N" );
		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys("extraValue"), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( recordList != null ) setLocationInformation( ctx, recordList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		return forward( ctx, systemConfig.getJspPath() + "/rbm_boardnotice_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("RBM", "RBMBoard."+ ctx.extraObj +".LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((Board)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> recordMap = (Map<String, Object>)ctx.req.getAttribute( "record" );

		if( recordMap == null ) {
			recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "boardClassCode", ctx.extraObj );
			recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
			recordMap.put( "registUserName", ctx.sessionMng.getUserName() );
			recordMap.put( "createDateTime", new com.irt.data.Timestamp( ctx.sessionMng.getTimeZone() ) );

			ctx.req.setAttribute( "record", recordMap );
			ctx.req.setAttribute( "fieldSet", ((Board)ctx.db).getFieldSet(true) );
		}

		return forward( ctx, systemConfig.getJspPath() + "/rbm_boardnotice_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		String boardClassCode = (String)ctx.extraObj;

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		int[] boardNumbers;
		String userId = ctx.sessionMng.getUniqId();
		try {
			String[] boardNumberStrs = ctx.req.getParameterValues( "boardNumber" );
			if( boardNumberStrs == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			boardNumbers = new int[ boardNumberStrs.length ];
			for( int i = 0; i < boardNumberStrs.length; i++ )
				boardNumbers[i] = Integer.parseInt( boardNumberStrs[i] );
		} catch( NumberFormatException numEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}
		Map<String, Object> primaryMap = Board.createPrimary( boardClassCode, 0, userId );
		boolean needAuthCheck = !ctx.sessionMng.isAuthorized("RBM", "RBMBoard."+ boardClassCode +".DEL");

		// 레코드 삭제
		int count = 0;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < boardNumbers.length; i++) {
			try {
				primaryMap.put( "boardNumber", new Integer(boardNumbers[i]) );
				if( needAuthCheck && !userId.equals( db.getRegistUserId(boardClassCode, boardNumbers[i]) ) )
					errorList.add( createErrorMap(primaryMap.get("boardNumber"), ctx.msghandler.getMessage(ServletModelException.HAS_NOAUTH)) );
				else if( db.delete( primaryMap ) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(primaryMap.get("boardNumber"), dataEx) );
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

	private Map<String, Object> setLocationInformation( Context ctx, Map<String, Object> recordMap ) throws SQLException {
		if( "NOTIS.PDA".equals(ctx.extraObj) ) {
			String gln = (String)recordMap.get( "extraValue" );
			if( gln != null ) {
				com.irt.rbm.ecs.Party db = new com.irt.rbm.ecs.Party( ctx.handler );

				Map partyMap = db.getRecord( com.irt.rbm.ecs.Party.createPrimary(gln), new String[] { "parentGln", "companyName", "locationName" } );
				if( partyMap != null ) {
					recordMap.put( "buyerCompanyName", partyMap.get("companyName") );
					if( partyMap.get("parentGln") != null ) {
						recordMap.put( "buyerGln", gln );
						recordMap.put( "parentBuyerGln", partyMap.get("parentGln") );
						recordMap.put( "buyerLocationName", partyMap.get("locationName") );
					} else
						recordMap.put( "parentBuyerGln", gln );
				}
			}
		}

		return recordMap;
	}

	private List<Map<String, Object>> setLocationInformation( Context ctx, List<Map<String, Object>> recordList ) throws SQLException {
		if( "NOTIS.PDA".equals(ctx.extraObj) ) {
			com.irt.rbm.ecs.Party db = new com.irt.rbm.ecs.Party( ctx.handler );

			for( Map<String, Object> recordMap : recordList ) {
				String gln = (String)recordMap.get( "extraValue" );
				if( gln == null ) continue;

				Map partyMap = db.getRecord( com.irt.rbm.ecs.Party.createPrimary(gln), new String[] { "parentGln", "companyName", "locationName" } );
				if( partyMap != null ) {
					recordMap.put( "buyerCompanyName", partyMap.get("companyName") );
					if( partyMap.get("parentGln") != null ) {
						recordMap.put( "buyerGln", gln );
						recordMap.put( "parentBuyerGln", partyMap.get("parentGln") );
						recordMap.put( "buyerLocationName", partyMap.get("locationName") );
					} else
						recordMap.put( "parentBuyerGln", gln );
				}
			}
		}

		return recordList;
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		String boardClassCode = (String)ctx.extraObj;

		// 레코드 읽기
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "boardClassCode", boardClassCode );
		recordMap.put( "userId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		try {
			int boardNumber;
			if( inserting ) {
				boardNumber = db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				boardNumber = db.modify( recordMap );
				if( boardNumber <= 0 )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			Map<String, Object> primaryMap = Board.createPrimary( boardClassCode, boardNumber, ctx.sessionMng.getUniqId() );
			ctx.req.setAttribute( "record", setLocationInformation(ctx, db.getRecord(primaryMap)) );
			ctx.pageConfig.setManageAuth( true );

			return forward( ctx, systemConfig.getJspPath() + "/rbm_boardnotice_info.jsp" );
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
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_RBM_BOARD_NOTICE_"+ mode.toUpperCase()) );
		recordMap.put( "registUserName", new com.irt.rbm.usr.UserUser(ctx.handler).getUserName((String)recordMap.get("userId")) );

		return registInput( ctx );
	}
}
