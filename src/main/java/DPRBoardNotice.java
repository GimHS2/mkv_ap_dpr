/*
 *	File Name:	DPRBoardNotice.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_boardnotice_info.jsp
 *		dpr_boardnotice_input.jsp
 *		rbm_boardnotice_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	hankalam	2017/02/28		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.Board;
import com.irt.rbm.rbm.BoardClass;
import com.irt.rbm.usr.UserParty;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;

@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRBoardNotice"})
public class DPRBoardNotice extends AbstractServletModel {
	public final static String BOARDCLASSCODE			= "SYSTEM";
	public final static String NOTICEMANAGE				= "ALL";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "boardClassCode", BOARDCLASSCODE );
		Condition.putConditionValueOnly( conditionMap, "userId", ctx.sessionMng.getUniqId() );

		if( !NOTICEMANAGE.equals( conditionMap.get("noticeManage") )) {
			com.irt.data.Date sysDate = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
			conditionMap.put( "noticeDate", sysDate );
		}

		return conditionMap;
		}

	private List<Map<String, Object>> getBoardClassCodeList( Context ctx ) throws SQLException {
		BoardClass db = new BoardClass( ctx.handler );

		Map<String, Object> conditionMap = ServletUtility.createMap( "boardClassCode", BOARDCLASSCODE );
		List<Map<String, Object>> records = db.getRecords( conditionMap );

		conditionMap.put( "boardClassCode", BOARDCLASSCODE);
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
		String boardClassCode = BOARDCLASSCODE;
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
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard.NO.INF" );
		else if( MODE_LIST.equals(ctx.mode) || MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard.NO.LST" );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard.NO.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard.NO.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMBoard.NO.MNG" );
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
			if( boardNumberStr == null || boardNumberStr.length() == 0 ) {
				boardNumberStr = ctx.pageConfig.getProperty( "boardNumber" );
			}
			if( boardNumberStr == null || boardNumberStr.length() == 0 ) {
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			}
			boardNumber = Integer.parseInt( boardNumberStr );
		} catch( NumberFormatException numEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}
		Map<String, Object> primaryMap = Board.createPrimary( boardClassCode, boardNumber, userId );

		// recordMap 읽기 & setAttribute
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		{
			String[] titles = ( (String)recordMap.get("title") ).split( "\\|\\|" );

			if( titles.length == 1 && !titles[0].matches("\\[[a-z]+\\].+") ) {
				recordMap.put( "title_en", titles[0] );
			}
			for( String title : titles ) {
				String locale = null;
				String titleValue = null;
				if( title.matches("^\\[[a-z]+\\].+") ) {
					locale = title.substring( title.indexOf("[") + 1, title.indexOf("]") );
					titleValue = title.replace( "[" + locale + "]", "" );
				}
				if( locale != null ) {
					recordMap.put( "title_" + locale, titleValue );
				}
			}
			recordMap.remove( "title" );

			String[] contents = ( (String)recordMap.get("content") ).split( "\\|\\|" );

			if( contents.length == 1 && !contents[0].matches("\\[[a-z]+\\].+") ) {
				recordMap.put( "content_en", contents[0] );
			}
			for( String content : contents ) {
				String locale = null;
				String contentValue = null;
				if( content.substring(0, 4).matches("^\\[[a-z]+\\].*") ) {
					locale = content.substring( content.indexOf("[") + 1, content.indexOf("]") );
					contentValue = content.replace( "[" + locale + "]", "" );
				}
				if( locale != null ) {
					recordMap.put( "content_" + locale, contentValue );
				}
			}
			recordMap.remove( "content" );
		}

		String type = ctx.req.getParameter( "type" );
		String extraValue = (String)recordMap.get( "extraValue" );
		if( extraValue != null && extraValue.contains( "maintenanceStart" )
				&&  extraValue.contains( "maintenanceEnd" ) &&  extraValue.contains( "maintenanceTimeZone" ) ) {
			UserParty partyDB = new UserParty( ctx.handler );
			String timezoneStr = (String) partyDB.getFieldValue( Record.createMap("partyId", ctx.sessionMng.getPartyId()), "timeZone" );
			String sdfPattern = "yyyy-MM-dd HH:mm";

			String[] kvs = extraValue.split( ";" );
			TimeZone timezone;
			if( !"dashboard".equals(type) ) {
				String[] kv = kvs[0].split( "=" );
				timezoneStr = kv[1];
			}

			timezone = TimeZone.getTimeZone( timezoneStr );

			for (int i = 0; i < kvs.length; i++) {
				String[] kv = kvs[i].split( "=" );

				if( "maintenanceStart".equals(kv[0]) || "maintenanceEnd".equals(kv[0]) ) {

					String[] time = kv[1].split( " " );
					recordMap.put( kv[0], time[0] );
					recordMap.put( kv[0] + "Time", time[1] );

					try {
						SimpleDateFormat sdf = new SimpleDateFormat( sdfPattern );
						java.util.Date date = sdf.parse( time[0] + " " + time[1] );
						sdf.setTimeZone( timezone );
						recordMap.put( kv[0] + "DateTime", sdf.format(date) );
					} catch( ParseException e ) {}
				}

				if( "maintenanceTimeZone".equals(kv[0]) ) {
					kv[0] = "timeZone";
					if( "dashboard".equals(type) ) {
						recordMap.put( kv[0], timezone.getDisplayName(false, TimeZone.SHORT) );
					} else {
						recordMap.put( kv[0], kv[1] );
					}
				}
			}
		}
		ctx.req.setAttribute( "record", recordMap = setLocationInformation( ctx, recordMap ) );

		try {
			db.increaseViewCount( boardClassCode, boardNumber, userId );
		} catch( DataException dataEx ) {
			logger.error( "internal error.", dataEx );
		} catch( SQLException sqlEx ) {
			logger.error( "internal error.", sqlEx );
		}

		// forward
		boolean hasManageAuth = userId.equals( recordMap.get("registUserId") ) || ctx.sessionMng.isSystemAdmin();
		if( inputting ) {
			if( !hasManageAuth ) return false;
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

			return registInput( ctx );
		} else {
			ctx.req.setAttribute( "prevRecord", db.getRecord( primaryMap, -1 ) );
			ctx.req.setAttribute( "nextRecord", db.getRecord( primaryMap, 1 ) );
			ctx.pageConfig.setManageAuth( hasManageAuth && ctx.sessionMng.isAuthorized("RBM", "RBMBoard."+ boardClassCode +".MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );


			if( "dashboard".equals(type) ) {
				recordMap = db.getLocaleRecord( recordMap, ctx.locale );
				String content = (String)recordMap.get( "content" );
				content = com.irt.util.StringUtil.evalPlaceholder( content, recordMap, "{{", "}}" );
				recordMap.put( "content", content );
				ctx.req.setAttribute( "record", recordMap );
				return forward( ctx, systemConfig.getJspPath() + "/dpr_boardnotice_info2.jsp" );
			} else {
				return forward( ctx, systemConfig.getJspPath() + "/dpr_boardnotice_info.jsp" );
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
		@SuppressWarnings("unchecked")
		Map<String, Object> recordMap = (Map<String, Object>)ctx.req.getAttribute( "record" );

		if( recordMap == null ) {
			recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "boardClassCode", ctx.extraObj );
			recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
			recordMap.put( "registUserName", ctx.sessionMng.getUserName() );
			recordMap.put( "createDateTime", new com.irt.data.Timestamp( ctx.sessionMng.getTimeZone() ) );

			ctx.req.setAttribute( "record", recordMap );
			ctx.req.setAttribute( "fieldSet", ((Board)ctx.db).getFieldSet(true) );
		} else {
			java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
			java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
			java.util.Date startDate = (java.util.Date)recordMap.get( "noticeStartDateTime" );
			if( startDate != null ) {
				String noticeStartDate = dateFormat.format( startDate );
				String noticeStartTime = timeFormat.format( startDate );
				recordMap.put( "noticeStartDate", noticeStartDate );
				recordMap.put( "noticeStartTime", noticeStartTime );
			}

			java.util.Date endDate = (java.util.Date)recordMap.get( "noticeEndDateTime" );
			if( endDate != null ) {
				String noticeEndtDate = dateFormat.format( endDate );
				String noticeEndTime = timeFormat.format( endDate );
				recordMap.put( "noticeEndtDate", noticeEndtDate );
				recordMap.put( "noticeEndTime", noticeEndTime );
			}
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_boardnotice_input.jsp" );
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

				Map<String, Object> partyMap = db.getRecord( com.irt.rbm.ecs.Party.createPrimary(gln), new String[] { "parentGln", "companyName", "locationName" } );
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

				Map<String, Object> partyMap = db.getRecord( com.irt.rbm.ecs.Party.createPrimary(gln), new String[] { "parentGln", "companyName", "locationName" } );
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

		String[] supportLocales = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;SupportLocale", "en,zh,th,vi,ko").split( "," );
		{
			String title = null;
			for( String locale : supportLocales ) {
				String localeTitle = (String) recordMap.get( "title_" + locale );
				if( localeTitle != null ) {
					if( title == null ) {
						title = "[" + locale + "]" + localeTitle;
					} else {
						title += "||[" + locale + "]" + localeTitle;
					}
					recordMap.remove( "title_" + locale );
				}
			}
			if( title != null ) {
				recordMap.put( "title", title );
			}
		}

		String noticeStartDate = (String)recordMap.get( "noticeStartDate" );
		String maintenanceStart = (String)recordMap.get( "maintenanceStart" );
		String noticeEndDate = (String)recordMap.get( "noticeEndDate" );
		String maintenanceEnd = (String)recordMap.get( "maintenanceEnd" );

		if( recordMap.containsKey("noticeStartTime") ) {
			noticeStartDate += " " + recordMap.get( "noticeStartTime" );
			java.util.Date startDate;
			try {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
				startDate = sdf.parse( noticeStartDate );
			} catch (ParseException e) {
				throw new ServletException( ctx.handler.getMessageHandler().getMessage("ERR_INVALID_DATE", noticeStartDate) );
			}
			recordMap.remove( "noticeStartTime" );
			recordMap.remove( "noticeStartDate" );
			recordMap.put( "noticeStartDateTime", startDate );
		}

		if( recordMap.containsKey("noticeEndTime") ) {
			noticeEndDate += " " + recordMap.get( "noticeEndTime" );
			java.util.Date endDate;
			try {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
				endDate = sdf.parse( noticeEndDate );
			} catch (ParseException e) {
				throw new ServletException( ctx.handler.getMessageHandler().getMessage("ERR_INVALID_DATE", noticeEndDate) );
			}

			recordMap.remove( "noticeEndTime" );
			recordMap.remove( "noticeEndDate" );
			recordMap.put( "noticeEndDateTime", endDate );
		}

		if( recordMap.containsKey("maintenanceStartTime") ) {
			maintenanceStart += " " + recordMap.get( "maintenanceStartTime" );
			try {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
				sdf.parse( maintenanceStart );
			} catch (ParseException e) {
				throw new ServletException( ctx.handler.getMessageHandler().getMessage("ERR_INVALID_DATE", maintenanceStart) );
			}

			recordMap.remove( "maintenanceStartTime" );
			recordMap.remove( "maintenanceStart" );
		}

		if( recordMap.containsKey("maintenanceEndTime") ) {
			maintenanceEnd += " " + recordMap.get( "maintenanceEndTime" );
			try {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
				sdf.parse( maintenanceEnd );
			} catch (ParseException e) {
				throw new ServletException( ctx.handler.getMessageHandler().getMessage("ERR_INVALID_DATE", maintenanceEnd) );
			}

			recordMap.remove( "maintenanceEndTime" );
			recordMap.remove( "maintenanceEnd" );
		}

		String extraValue = "maintenanceTimeZone=/TIMEZONE/;maintenanceStart=/STARTDATE/;maintenanceEnd=/ENDDATE/";
		extraValue = extraValue.replace( "/TIMEZONE/", (String)recordMap.get("timeZone") )
				.replace( "/STARTDATE/", maintenanceStart )
				.replace( "/ENDDATE/", maintenanceEnd );

		{
			String content = null;
			for( String locale : supportLocales ) {
				String localeTitle = (String) recordMap.get( "content_" + locale );
				if( localeTitle != null ) {
					if( content == null ) {
						content = "[" + locale + "]" + localeTitle;
					} else {
						content += "||[" + locale + "]" + localeTitle;
					}
					recordMap.remove( "content_" + locale );
				}
			}
			if( content != null ) {
				recordMap.put( "content", content );
			}
		}

		recordMap.put( "extraValue", extraValue );
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
			//ctx.req.setAttribute( "record", setLocationInformation(ctx, db.getRecord(primaryMap)) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setProperty( "boardNumber", String.valueOf(boardNumber) );
			return info( ctx, false );
			//return forward( ctx, systemConfig.getJspPath() + "/dpr_boardnotice_info.jsp" );
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
