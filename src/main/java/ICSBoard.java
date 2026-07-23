/*
 *	File Name:	ICSBoard.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		ics_board_info.jsp
 *		ics_board_input.jsp
 *		ics_board_list.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2c	신규 UI/UX 적용
 *	jbaek		2017/07/30		2.2.1c	menuhtml 적용
 *	jbaek		2016/08/31		2.2.1c	fieldSet 적용
 *	hankalam	2015/10/30		2.2.1c	웹취약성 수정. headwordCode 파라미터가 있을 때 위험문자 검사 추가.
 *	yjcha		2010/10/08		2.2.1c	팝업 공지사항 기능 추가, noticeDate noticeManage 기능 추가
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

import com.irt.cst.MenuCode;
import com.irt.data.DataException;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.ics.Board;
import com.irt.ics.BoardAttach;
import com.irt.ics.BoardClass;
import com.irt.ics.BoardComment;
import com.irt.ics.html.CommentListWriter;
import com.irt.servlet.MultipartHttpRequest;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/ICSBoard"})
public class ICSBoard extends ICSBoardServlet {
	public static final String MODE_FRAME				= "frm";
	public static final String MODE_COMMENT				= "comm";
	public final static String MODE_FAQ					= "faq";
	public final static String MODE_FAQ_LIST			= "faqlist";
	public final static String MODE_FAQMNG				= "faqmng";
	public static final String MODE_IMPORTANT_POST		= "important";
	public static final String MODE_BLANK				= "blank";
	public final static String NOTICEMANAGE				= "ALL";

	protected Map<String, Object> createConditionMap( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req, true );

		conditionMap.put( "newDays", new Integer(2) );
		if( conditionMap.containsKey( "boardClassCode" ) ) {
			if( !NOTICEMANAGE.equals( conditionMap.get("noticeManage") )) {
				com.irt.data.Date sysDate = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
				conditionMap.put( "noticeDate", sysDate );
			}
		}
		return conditionMap;
	}

	protected boolean comment( Context ctx ) throws IOException, ServletException, SQLException {
		BoardComment db = new BoardComment( ctx.handler );

		String exeuteType = ctx.req.getParameter( "type" );
		String boardClassCode = (String)ctx.extraObj;
		if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( !isAuthorized(ctx, boardClassCode, "MNG_CMT") )
			return false;

		int boardNumber = getNumberParameter( ctx, "boardNumber", true );
		int commentNumber = getNumberParameter( ctx, "commentNumber", false );
		int originalCommentNumber = getNumberParameter( ctx, "originalCommentNumber", false );

		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
		recordMap.put( "boardClassCode", boardClassCode );
		recordMap.put( "boardNumber", new Integer(boardNumber) );
		recordMap.put( "commentNumber", new Integer(commentNumber) );
		recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
		recordMap.put( "content", ctx.req.getParameter("content") );
		if( "reply".equals(exeuteType) ) {
			if( originalCommentNumber < 0 )
				throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
			recordMap.put( "originalCommentNumber", new Integer(originalCommentNumber) );
		}

		try {
			if( "regist".equals(exeuteType) || "reply".equals(exeuteType) )
				db.regist( recordMap );
			else {
				Map<String, Object> originalMap = db.getRecord( BoardComment.createPrimary(boardClassCode, boardNumber, commentNumber) );
				if( originalMap == null )
					throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
				else if( commentNumber < 0 )
					throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
				else if( !ctx.sessionMng.getUniqId().equals(originalMap.get("registUserId")) )
					throw new ServletModelException( ServletModelException.HAS_NOAUTH );

				if( "modify".equals(exeuteType) ) {
					if( !db.modify(recordMap, new String[] { "content" }) )
						throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				} else if( "delete".equals(exeuteType) )
					db.delete( originalMap );
				else
					throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );

			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		}

		CommentListWriter commentWriter = new CommentListWriter( ctx.msghandler, ctx.sessionMng );
		commentWriter.setEditCommentAuth( true );
		commentWriter.setPrintingUnderLine( true );
		commentWriter.setRecords( db.getRecords(Board.createPrimary(boardClassCode, boardNumber), CommentListWriter.getFieldKeys()) );

		ctx.res.setContentType( "text/html; charset=utf-8" );
		ctx.res.setHeader( "Cache-Control", "no-cache" );
		commentWriter.print( ctx.res.getWriter() );

		return true;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_FRAME.equals(ctx.mode) ) return frame( ctx );
		if( MODE_FAQ_LIST.equals(ctx.mode) ) return faqlist( ctx );
		if( MODE_COMMENT.equals(ctx.mode) ) return comment( ctx );
		if( MODE_IMPORTANT_POST.equals(ctx.mode) ) return updateImportantPost( ctx );
		if( MODE_FAQMNG.equals(ctx.mode) ) return faqManage( ctx );
		if( MODE_BLANK.equals(ctx.mode) ) return infoBlank( ctx );
		return super.doRequest( ctx, isPost );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// boardClassCode
		String boardClassCode = ctx.req.getParameter( "boardClassCode" );
		if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		pageConfig.setProperty( "boardClassCode", boardClassCode );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_FRAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".INF" );
		else if( MODE_INFO.equals(ctx.mode) || MODE_BLANK.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".INF" );
		else if( MODE_FAQ.equals(ctx.mode) || MODE_FAQ_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".INF" );
		else if( MODE_FAQMNG.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG" );
		else if( MODE_COMMENT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG_CMT" );
		else if( MODE_IMPORTANT_POST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String boardClassName = boardClassCode;
		try {
			boardClassName = (new BoardClass(ctx.handler)).getName( boardClassCode );
		} catch( SQLException sqlEx ) {}

		ctx.db = new Board( ctx.handler );
		ctx.extraObj = boardClassCode;
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_ICS_BOARD_"+ ctx.mode.toUpperCase(), boardClassName) );
	}

	protected boolean frame( Context ctx ) throws IOException, ServletException, SQLException {
		String type = ctx.req.getParameter( "type" );
		if( "faq".equals(type) ) {
			String boardClassCode = (String)ctx.extraObj;
			setPath( ctx, "jsp.MENU_FAQ" );
			ctx.pageConfig.setProperty( "type", type );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_ICS_BOARD_FAQ") );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".MNG") );
		} else if( "faqmng".equals(type) ) {
			setPath( ctx, "jsp.MENU_FAQ" );
			ctx.pageConfig.setProperty( "type", type );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_ICS_BOARD_FAQMNG_") );
		} else {
			setPath( ctx, "jsp.MENU_HOME", "TITLE_ICS_BOARD_FRM" );
		}
		return forward( ctx, systemConfig.getJspPath() + "/ics_board_main.jsp" );
	}

	private Map<String, Object> getDefaultBoardRecord( Context ctx ) throws SQLException {
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();

		recordMap.put( "attachManageKey", BoardAttach.makeAttachManageKey( (String)ctx.extraObj, ctx.sessionMng.getUniqId() ) );
		recordMap.put( "boardClassCode", ctx.extraObj );
		recordMap.put( "boardOption", Board.BOARDOPTION_HTML );
		recordMap.put( "boardType", Board.BOARDTYPE_NORMAL );
		recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
		recordMap.put( "registUserName", ctx.sessionMng.getUserName() );
		recordMap.put( "registUserPartyName", ctx.sessionMng.getPartyName() );
		recordMap.put( "registUserUserId", ctx.sessionMng.getUserId() );
		recordMap.put( "createDateTime", com.irt.data.Date.getInstance(ctx.handler.getTimeZone()) );

		return recordMap;
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		String rtype = ctx.req.getParameter( "rtype" );
		String boardClassCode = (String)ctx.extraObj;
		String noticeInd = ctx.req.getParameter("noticeInd");
		int boardNumber = getNumberParameter(ctx, "boardNumber", true );

		Map<String, Object> primaryMap = Board.createPrimary( boardClassCode, boardNumber );
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		try {
			db.increaseViewCount( boardClassCode, boardNumber, ctx.sessionMng.getUniqId() );
		} catch( DataException dataEx ) {
			logger.error( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			logger.error( "internal error.", sqlEx );
		}

		{
			BoardAttach attachDB = new BoardAttach( ctx.handler );
			String[] fieldKeys = new String[] { "attachManageKey", "attachNumber", "fileName", "fileSize" };

			Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
			conditionMap.put( "attachManageKey", recordMap.get("attachManageKey") );
			conditionMap.put( "fileType", BoardAttach.ATTACHTYPE_FILE );
			conditionMap.put( "status", "00" );

			ctx.req.setAttribute( "attaches", attachDB.getRecords(conditionMap, fieldKeys) );
			ctx.req.setAttribute( "comments", (new BoardComment(ctx.handler)).getRecords( primaryMap, CommentListWriter.getFieldKeys() ) );
		}

		boolean isOwnerPosts = ctx.sessionMng.getUniqId().equals( recordMap.get("registUserId") ) || ctx.sessionMng.isSystemAdmin();
		if( inputting ) {
			if( !isOwnerPosts ) return false;

			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
			return forward( ctx, systemConfig.getJspPath() + "/ics_board_input.jsp" );
		} else if ("Y".equals(noticeInd)) {
			return forward( ctx, systemConfig.getJspPath() + "/ics_board_popup.jsp" );
		} else {
			ctx.pageConfig.setManageAuth( isOwnerPosts && ctx.sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".MNG") );

			if( "faq".equals(rtype) ) {
				return forward( ctx, systemConfig.getJspPath() + "/ics_faq_info.jsp" );
			} else {
				return forward( ctx, systemConfig.getJspPath() + "/ics_board_info.jsp" );
			}
		}
	}

	protected boolean infoBlank( Context ctx ) throws IOException, ServletException, SQLException {
		return forward( ctx, systemConfig.getJspPath() + "/ics_board_info.jsp" );
	}

	protected boolean faqManage( Context ctx ) throws IOException, ServletException, SQLException {
		String boardClassCode = ctx.pageConfig.getProperty("boardClassCode");
		Board db = (Board)ctx.db;

		String headwordCode = ctx.req.getParameter( "headwordCode" );
		if( headwordCode != null ) {
			if( headwordCode.indexOf("<") >= 0 || headwordCode.indexOf(">") >= 0
					|| headwordCode.indexOf("+") >= 0 || headwordCode.indexOf(";") >= 0 )
				throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		String type = "NAME";
		if( ctx.req.getParameter("type") != null )
			type = ctx.req.getParameter("type");

		ctx.pageConfig.setProperty("type", type);

		Map<String, Object> boardClassMap = db.getBoardClass( boardClassCode );
		boolean usingReadedUser = ( boardClassMap != null && "Y".equals(boardClassMap.get("useReadedUser")) );

		// conditionMap, columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );

		String noticeType = "%LIST";
		if( NOTICEMANAGE.equals( conditionMap.get("noticeManage") ))
			noticeType = noticeType + "." + NOTICEMANAGE;

		com.irt.data.cols.ColumnList columnList;
		if( type.equals("NONAME") ) {
			columnList = getColumnList( ctx, "ICSBoard.NONAME"+noticeType, usingReadedUser ? "RD_Y" : "RD_N" );
		} else{
			columnList = getColumnList( ctx, "ICSBoard"+noticeType, usingReadedUser ? "RD_Y" : "RD_N" );
		}

		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setPath( ctx, "jsp.MENU_FAQ" );

		return forward( ctx, systemConfig.getJspPath() + "/ics_board_list.jsp" );
	}

	protected boolean faqlist( Context ctx ) throws IOException, ServletException, SQLException {
		String boardClassCode = ctx.pageConfig.getProperty("boardClassCode");
		String menuId = "ICSBoard." + boardClassCode;
		String menuLocale = ctx.locale.getLanguage();// ISO lang code, en or zh or th

		MenuCode menu = new MenuCode(ctx.handler);
		String[] availableLocales = menu.getAvaialbleLocales( menuId );

		if( availableLocales != null ) {
			boolean existLocale = false;
			for( String locale : availableLocales ) {
				if( locale.equals(menuLocale) ) {
					existLocale = true;
				}
			}

			if( !existLocale ) {
				menuLocale = availableLocales[0];
			}
		}

		String menuhtml = "";// empty string to check if is empty in js
		try {
			menuhtml = menu.getMenuHtml(menuId, menuLocale);
			ctx.req.setAttribute("menuhtml", menuhtml);
		} catch( SQLException e ) {
			throw new ServletModelException(e.getMessage());
		}

		setPath( ctx, "jsp.MENU_FAQ" );

		return forward( ctx, systemConfig.getJspPath() + "/ics_faq_list.jsp" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		String boardClassCode = (String)ctx.extraObj;

		String headwordCode = ctx.req.getParameter( "headwordCode" );
		if( headwordCode != null ) {
			if( headwordCode.indexOf("<") >= 0 || headwordCode.indexOf(">") >= 0
					|| headwordCode.indexOf("+") >= 0 || headwordCode.indexOf(";") >= 0 )
				throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		String type = "NAME";
		if( ctx.req.getParameter("type") != null )
			type = ctx.req.getParameter("type");

		ctx.pageConfig.setProperty("type", type);

		Map<String, Object> boardClassMap = db.getBoardClass( boardClassCode );
		boolean usingReadedUser = ( boardClassMap != null && "Y".equals(boardClassMap.get("useReadedUser")) );

		// conditionMap, columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );

		String noticeType = "%LIST";
		if( NOTICEMANAGE.equals( conditionMap.get("noticeManage") ))
			noticeType = noticeType + "." + NOTICEMANAGE;

		com.irt.data.cols.ColumnList columnList;
		if( type.equals("NONAME") ) {
			columnList = getColumnList( ctx, "ICSBoard.NONAME"+noticeType, usingReadedUser ? "RD_Y" : "RD_N" );
		} else{
			columnList = getColumnList( ctx, "ICSBoard"+noticeType, usingReadedUser ? "RD_Y" : "RD_N" );
		}

		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		if( "faqmng".equals(ctx.req.getParameter("type")) ) {
			setPath( ctx, "jsp.MENU_FAQ" );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_ICS_BOARD_FAQMNG_" + ctx.mode.toUpperCase()) );
		} else {
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("jsp.ics_board_list.SUBTITLE_BOARD") );
		}

		if( MODE_FAQ.equals(ctx.mode) ) {
			return forward( ctx, systemConfig.getJspPath() + "/ics_faq_list.jsp" );
		} else {
			return forward( ctx, systemConfig.getJspPath() + "/ics_board_list.jsp" );
		}
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("ICS", "ICSBoard."+ ctx.extraObj +".LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((Board)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	private boolean manageAttaches( Context ctx, String attachManageKey, String boardClassCode, int boardNumber )
						throws DataException, IOException, ServletModelException, SQLException {
		BoardAttach db = new BoardAttach( ctx.handler );

		MultipartHttpRequest req = (MultipartHttpRequest)ctx.req;

		String attachNumber = ctx.req.getParameter( "attachNumbers" );
		if( attachNumber != null && attachNumber.length() > 0 )
			db.cleanNoUsedAttaches( attachManageKey, attachNumber.split(",") );
		else
			db.cleanNoUsedAttaches( attachManageKey );

		String[] paramNames = req.getFileNames();
		int[] attachNumbers = new int[ paramNames == null ? 0 : paramNames.length ];
		if( paramNames != null && paramNames.length > 0 ) {
			Map<String, Object> attachMap = new java.util.HashMap<String, Object>();

			attachMap.put( "attachManageKey", attachManageKey );
			attachMap.put( "boardClassCode", boardClassCode );
			attachMap.put( "boardNumber", Integer.valueOf(boardNumber) );
			attachMap.put( "filePath", ATTACHFILE_PATH );
			attachMap.put( "fileType", BoardAttach.ATTACHTYPE_FILE );

			for( int i = 0; i < paramNames.length; i++ ) {
				String paramName = paramNames[i];
				File file = req.getFile( paramName );

				while( true ) {
					try {
						attachNumbers[i] = db.getNextAttachNumberByManageKey( attachManageKey );

						attachMap.put( "attachNumber", Integer.valueOf(attachNumbers[i]) );
						attachMap.put( "contentType", req.getFileContentType(paramName) );
						attachMap.put( "fileName", req.getInputFileName(paramName) );
						attachMap.put( "fileSize", Long.valueOf(file.length() / 1024L) );
						attachMap.put( "serverFileName", BoardAttach.getServerFileName(attachManageKey, attachNumbers[i]) );

						db.regist( attachMap, file );
						break;
					} catch( DataException dataEx ) {
						if( !DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) )
							throw dataEx;
					}
				}
			}
		}

		return true;
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		String type = "NAME";
		if( ctx.req.getParameter("type") != null )
			type = ctx.req.getParameter("type");

		ctx.pageConfig.setProperty("type", type);

		String boardClassCode = (String)ctx.extraObj;
		int originalBoardNumber = getNumberParameter( ctx, "originalBoardNumber", false );

		Map<String, Object> recordMap = getDefaultBoardRecord( ctx );
		if( originalBoardNumber >= 0 ) {
			Map<String, Object> originalBoardMap = ((Board)ctx.db).getRecord( Board.createPrimary(boardClassCode, originalBoardNumber) );

			recordMap.put( "originalBoardNumber", Integer.valueOf(originalBoardNumber) );
			recordMap.put( "title", ctx.msghandler.getMessage("MSG_ICS_BOARD_REPLY_TITLE", (String)originalBoardMap.get("title")) );
			recordMap.put( "boardType", Board.BOARDTYPE_REPLY );
		}
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", ((Board)ctx.db).getFieldSet(true) );

		return forward( ctx, systemConfig.getJspPath() + "/ics_board_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		BoardAttach attachDB = new BoardAttach( ctx.handler );
		String boardClassCode = (String)ctx.extraObj;

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		int[] boardNumbers;
		try {
			String[] boardNumberStrings = ctx.req.getParameterValues( "boardNumber" );
			if( boardNumberStrings == null || boardNumberStrings.length == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			boardNumbers = new int[ boardNumberStrings.length ];
			for( int i = 0; i < boardNumberStrings.length; i++ )
				boardNumbers[i] = Integer.parseInt( boardNumberStrings[i] );
		} catch( NumberFormatException numberEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		// 레코드 삭제
		Map<String, Object> primaryMap = Board.createPrimary( boardClassCode, 0 );
		boolean needAuthCheck = !ctx.sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".DEL");

		int count = 0;
		List<Object> errorList = new java.util.ArrayList<Object>();
		for( int i = 0; i < boardNumbers.length; i++ ) {
			primaryMap.put( "boardNumber", new Integer(boardNumbers[i]) );

			try {
				Map<String, Object> originalMap = db.getRecord( primaryMap, new String[] { "attachManageKey", "replyCount", "registUserId" } );
				if( originalMap != null ) {
					if( needAuthCheck && !ctx.sessionMng.getUniqId().equals(originalMap.get("registUserId")) )
						errorList.add( createErrorMap(primaryMap.get("boardNumber"), ctx.msghandler.getMessage(ServletModelException.HAS_NOAUTH)) );
					else if( ((Number)originalMap.get("replyCount")).intValue() > 0 )
						errorList.add( createErrorMap(boardNumbers[i], ctx.msghandler.getMessage("ERR_ICS_BOARD_CANNOT_DELETE_HAS_REPLY_POSTS")) );
					else if( db.delete(primaryMap) ) {
						attachDB.delete( (String)originalMap.get("attachManageKey"), ATTACHFILE_PATH );

						count++;
					}
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(primaryMap.get("boardNumber"), dataEx) );
			}
		}

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
			String url = HtmlUtility.replaceURLQuery( ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx) );
			url = HtmlUtility.replaceURLQuery( url, "removeComplete", "Y" );
			return sendRedirect( ctx, url );
		}
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		BoardAttach attachDB = new BoardAttach( ctx.handler );

		String boardClassCode = (String)ctx.extraObj;
		String attachManageKey = ctx.req.getParameter( "attachManageKey" );
		if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );

		// 레코드 읽기
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );
		try {
			int boardNumber;
			if( inserting ) {
				boardNumber = db.regist( recordMap );
				if( boardNumber > 0 && attachManageKey != null ) {
					manageAttaches( ctx, attachManageKey, boardClassCode, boardNumber );
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_ICS_BOARD_REGIST_SUCCESS") );
				} else
					throw ctx.handler.createDataException( DataException.ERR_CANNOT_INSERT );
			} else {
				if( !ctx.sessionMng.getUniqId().equals(db.getFieldValue(recordMap, "registUserId")) && !ctx.sessionMng.isSystemAdmin() )
					return false;

				boardNumber = db.modify( recordMap );
				if( boardNumber > 0 && attachManageKey != null ) {
					manageAttaches( ctx, attachManageKey, boardClassCode, boardNumber );
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
				} else
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
			}

			String redirectURL = "ICSBoard?"+ PARAM_MODE +"="+ MODE_INFO;
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "boardClassCode", boardClassCode );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "boardNumber", String.valueOf(boardNumber) );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "updateComplete", "Y" );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx) );

			return sendRedirect( ctx, makeRedirectURL(ctx, redirectURL, ctx.pageConfig.getBackURL()) );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		}

		return forward( ctx, systemConfig.getJspPath() + "/ics_board_input.jsp" );
	}

	protected boolean updateImportantPost( Context ctx ) throws IOException, ServletException, SQLException {
		Board db = (Board)ctx.db;
		String boardClassCode = (String)ctx.extraObj;

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		int[] boardNumbers;
		try {
			String[] boardNumberStrings = ctx.req.getParameterValues( "boardNumber" );
			if( boardNumberStrings == null || boardNumberStrings.length == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			boardNumbers = new int[ boardNumberStrings.length ];
			for( int i = 0; i < boardNumberStrings.length; i++ )
				boardNumbers[i] = Integer.parseInt( boardNumberStrings[i] );
		} catch( NumberFormatException numberEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		Map<String, Object> recordMap = Board.createPrimary( boardClassCode, 0 );

		int count = 0;
		List<Object> errorList = new java.util.ArrayList<Object>();
		for( int i = 0; i < boardNumbers.length; i++ ) {
			recordMap.put( "boardNumber", new Integer(boardNumbers[i]) );
			try {
				Map<String, Object> originalMap = db.getRecord( recordMap, new String[] { "boardType" } );
				if( originalMap != null ) {
					String boardType = (String) originalMap.get( "boardType" );
					if( Board.BOARDTYPE_NORMAL.equals(boardType) ) {
						boardType = Board.BOARDTYPE_KNOWLEDGE;
					} else if( Board.BOARDTYPE_KNOWLEDGE.equals(boardType) ) {
						boardType = Board.BOARDTYPE_NORMAL;
					} else if( Board.BOARDTYPE_REPLY.equals(boardType) ) {
						throw ctx.handler.createDataException( "ERR_CANNOT_UPDATE_IMPORTANTPOST" );
					}
					recordMap.put( "boardType", boardType );
					db.modifyImportantPost(recordMap);
					count++;

					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(recordMap.get("boardNumber"), dataEx) );
			}
		}

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}
}
