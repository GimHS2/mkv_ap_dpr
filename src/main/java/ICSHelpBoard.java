/*
 *	File Name:	ICSHelpBoard.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		ics_helpboard_info.jsp
 *		ics_helpboard_input.jsp
 *		ics_helpboard_list.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2c	신규 UI/UX 적용
 *	hankalam	2019/07/31		2.2.1c	오류 수정.
 *	jbaek		2019/07/30		2.2.1c	sendMail() 기능 추가.
 *	hankalam	2017/02/28		2.2.0c	create
 *
**/

import com.irt.data.AbstractFieldSet;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.dpr.tools.OrderCanonicalProcess;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.ics.Board;
import com.irt.ics.BoardAttach;
import com.irt.ics.BoardClass;
import com.irt.ics.HelpBoard;
import com.irt.rbm.RBMSystem;
import com.irt.servlet.MultipartHttpRequest;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.sql.SQLManager;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/ICSHelpBoard" } )
public class ICSHelpBoard extends ICSBoardServlet {//@formatter:off
	public final String BOARDCLASS_PREFIX			= "HD.";
	public static final String MODE_COMPLETED_POST		= "completed";
	public static final String MODE_FRAME				= "frm";
	public static final String MODE_BLANK				= "blank";

	protected Map<String, Object> createConditionMap( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req, true );

		if( !conditionMap.containsKey("boardClassCode") ) {
			String boardClassCode = (String)ctx.extraObj;
			conditionMap.put( "boardClassCode", boardClassCode );
		}
		conditionMap.put( "newDays", new Integer(2) );

		String startDate = Record.extractString( conditionMap, "startDate" );
		String endDate = Record.extractString( conditionMap, "endDate" );
		try {
			if( startDate != null && startDate.length() > 0 ) {
				conditionMap.put( "updateDateTime" + Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startDate) );
			}
			if( endDate != null && endDate.length() > 0 ) {
				conditionMap.put( "updateDateTime" + Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endDate).getDate(1) );
			} else {
				conditionMap.put( "updateDateTime" + Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).getDate(1) );
			}
			conditionMap.put( "updateDateTime"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MIN );
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_FRAME.equals(ctx.mode) ) return frame( ctx );
		if( MODE_BLANK.equals(ctx.mode) ) return infoBlank( ctx );
		if( MODE_COMPLETED_POST.equals(ctx.mode) ) return updateCompletedPost( ctx );
		return super.doRequest( ctx, isPost );
	}

	protected boolean frame( Context ctx ) throws IOException, ServletException, SQLException {
		setPath( ctx, "jsp.MENU_HOME", "TITLE_ICS_BOARD_FRM" );
		ctx.pageConfig.setProperty( "type", "helpboard" );
		return forward( ctx, systemConfig.getJspPath() + "/ics_board_main.jsp" );
	}

	private Map<String, Object> getDefaultBoardRecord( Context ctx ) throws SQLException {
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();

		recordMap.put( "attachManageKey", BoardAttach.makeAttachManageKey( (String)ctx.extraObj, ctx.sessionMng.getUniqId() ) );
		recordMap.put( "boardClassCode", ctx.extraObj );
		recordMap.put( "boardOption", Board.BOARDOPTION_TEXT );
		recordMap.put( "boardType", Board.BOARDTYPE_NORMAL );
		recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
		recordMap.put( "userName", ctx.sessionMng.getUserName() );
		recordMap.put( "registUserPartyName", ctx.sessionMng.getPartyName() );
		recordMap.put( "registUserUserId", ctx.sessionMng.getUserId() );
		recordMap.put( "createDateTime", com.irt.data.Date.getInstance(ctx.handler.getTimeZone()) );

		return recordMap;
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// boardClassCode
		String boardClassCode = BOARDCLASS_PREFIX + ctx.sessionMng.getExtraValue();

		if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		pageConfig.setProperty( "boardClassCode", boardClassCode );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_FRAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSHelpBoard."+ boardClassCode +".LST" );
		else if( MODE_INFO.equals(ctx.mode) || MODE_BLANK.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSHelpBoard."+ boardClassCode +".INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSHelpBoard."+ boardClassCode +".LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSHelpBoard."+ boardClassCode +".REG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSHelpBoard."+ boardClassCode +".REG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSHelpBoard."+ boardClassCode +".MNG" );
		else if( MODE_COMPLETED_POST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSHelpBoard."+ boardClassCode +".MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String boardClassName = boardClassCode;
		try {
			boardClassName = (new BoardClass(ctx.handler)).getName( boardClassCode );
		} catch( SQLException sqlEx ) {}

		ctx.db = new HelpBoard( ctx.handler );
		ctx.extraObj = boardClassCode;
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_ICS_HELP_BOARD_"+ ctx.mode.toUpperCase(), boardClassName) );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		HelpBoard db = (HelpBoard)ctx.db;

		String boardClassCode = (String)ctx.extraObj;
		int boardNumber = getNumberParameter(ctx, "boardNumber", true );

		Map<String, Object> primaryMap = HelpBoard.createPrimary( boardClassCode, boardNumber );
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
			if( recordMap.get("attachManageKey") != null ) {
				ctx.req.setAttribute( "attaches", attachDB.getRecords(conditionMap, fieldKeys) );
			}
		}

		boolean isOwnerPosts = ctx.sessionMng.getUniqId().equals( recordMap.get("registUserId") );
		if( inputting ) {
			if( !isOwnerPosts ) return false;

			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
			return forward( ctx, systemConfig.getJspPath() + "/ics_helpboard_input.jsp" );
		} else {
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".MNG") );
			return forward( ctx, systemConfig.getJspPath() + "/ics_helpboard_info.jsp" );
		}
	}

	protected boolean infoBlank( Context ctx ) throws IOException, ServletException, SQLException {
		return forward( ctx, systemConfig.getJspPath() + "/ics_helpboard_info.jsp" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		HelpBoard db = (HelpBoard)ctx.db;
		String boardClassCode = (String)ctx.extraObj;

		Map<String, Object> boardClassMap = db.getBoardClass( boardClassCode );
		boolean usingReadedUser = ( boardClassMap != null && "Y".equals(boardClassMap.get("useReadedUser")) );

		// conditionMap, columnList 읽기
		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList;
		columnList = getColumnList( ctx, "ICSHelpBoard%LIST", usingReadedUser ? "RD_Y" : "RD_N" );

		// records 읽기 & conditionMap 저장
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		return forward( ctx, systemConfig.getJspPath() + "/ics_helpboard_list.jsp" );
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
		ctx.pageConfig.getListIndexVariables()[2] = ((HelpBoard)ctx.db).getRecordCount( conditionMap );

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
		Map<String, Object> recordMap = getDefaultBoardRecord( ctx );
		if( recordMap != null ) {
			recordMap.putAll( new com.irt.rbm.usr.UserUser(ctx.handler).getRecord((String)recordMap.get("registUserId"), new String[]{ "email", "tel" }) );
		}

		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", ((HelpBoard)ctx.db).getFieldSet(true) );

		return forward( ctx, systemConfig.getJspPath() + "/ics_helpboard_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		HelpBoard db = (HelpBoard)ctx.db;
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
		boolean needAuthCheck = !ctx.sessionMng.isAuthorized("ICS", "ICSHelpBoard."+ boardClassCode +".DEL");

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
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		HelpBoard db = (HelpBoard)ctx.db;

		String boardClassCode = (String)ctx.extraObj;
		String attachManageKey = ctx.req.getParameter( "attachManageKey" );
		if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );

		// 레코드 읽기
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );
		boolean willSendMail = false;
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
				if( !ctx.sessionMng.getUniqId().equals(db.getFieldValue(recordMap, "registUserId")) )
					return false;

				boardNumber = db.modify( recordMap );
				if( boardNumber > 0 && attachManageKey != null ) {
					manageAttaches( ctx, attachManageKey, boardClassCode, boardNumber );
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
				} else
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
			}

			willSendMail = RBMSystem.getSystemEnvBool("DPR", "Feature;useHDSendMail", false);
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		} finally {
			if( willSendMail )
				sendMail(ctx);
		}

		return forward( ctx, systemConfig.getJspPath() + "/ics_helpboard_input.jsp" );
	}

	protected boolean updateCompletedPost( Context ctx ) throws IOException, ServletException, SQLException {
		HelpBoard db = (HelpBoard)ctx.db;
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

		Map<String, Object> recordMap = HelpBoard.createPrimary( boardClassCode, 0 );

		int count = 0;
		List<Object> errorList = new java.util.ArrayList<Object>();
		for( int i = 0; i < boardNumbers.length; i++ ) {
			recordMap.put( "boardNumber", new Integer(boardNumbers[i]) );
			try {
				Map<String, Object> originalMap = db.getRecord( recordMap, new String[] { "completedInd" } );
				if( originalMap != null ) {
					String completedInd = (String) originalMap.get( "completedInd" );
					if( "Y".equals(completedInd) ) {
						completedInd = "N";
					} else {
						completedInd = "Y";
					}
					recordMap.put( "completedInd", completedInd );
					db.modifyCompletedPost(recordMap);
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
			String type = ctx.req.getParameter( "type" );
			if( "ordrevise".equals(type) || "list".equals(type) ) {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS", String.valueOf(count)) );
				return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
			} else {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS", String.valueOf(count)) );
				ctx.pageConfig.setProperty( "completed", "Y" );
				return info( ctx, false );
			}
		}
	}

	private boolean sendMail( Context ctx ) throws SQLException {
		Map recordMap = (Map)ctx.req.getAttribute("record");
		AbstractFieldSet fieldSet = (AbstractFieldSet)ctx.req.getAttribute("fieldSet");

		String registUserId = (String)recordMap.get("registUserId");
		String userName = (String)recordMap.get("userName");


		String query = "SELECT DISTINCT UNiQID, EMAIL"
		+ " FROM USR_GROUP_AUTH UGA, USR_USER USR, SYS_SERVGRP_PKG SPKG"
		+ " WHERE UGA.SERVGRPCD = SPKG.SERVGRPCD"
		+ " AND UGA.GROUPID = USR.GROUPID"
		+ " AND UGA.PARTYID = USR.PARTYID"
		+ " AND UGA.PARTYID = ?"
		+ " AND USR.EMAIL IS NOT NULL"
		+ " AND USR.USERCLASS = 'PA'"
		+ " AND SPKG.PKGCD LIKE 'ICSHelpBoard.HD.%.MNG'";

		boolean isTestingEmail = RBMSystem.getSystemEnvBool("DPR", "Feature;testingEmail", false);
		if( isTestingEmail ) {
			query += " AND USR.USERCLASS = 'SA'";
		}

		List<Map<String, Object>> receiveEmails = SQLManager.getRecordList( ctx.handler, query, new Object[]{ctx.sessionMng.getPartyId()} );
		if( receiveEmails == null || receiveEmails.size() == 0 )
			return false;

		String[] toAddress = java.util.Arrays.asList(Record.extractObjectArray(receiveEmails, "EMAIL")).toArray(new String[0]);

		String subject = ctx.msghandler.getMessage("MSG_ICS_HELPBOARD_EMAILSUBJECT_TITLE", systemConfig.getDomain(), userName, registUserId);

		List<String> fieldKeyList = java.util.Arrays.asList(new String[]{ "orderNumber", "registUserId", "userName", "tel", "email", "content" });
		String contents = "";
		for( String key : fieldSet.getFieldKeyArray() ) {
			if( fieldKeyList.contains(key) ) {
				contents += ctx.msghandler.getMessage(fieldSet.getField(key).getDescriptionKey()) + " : "
							+ fieldSet.getField(key).format(recordMap, ctx.msghandler) + "<br/>";
			}
		}
		Map systemInfo = OrderCanonicalProcess.getSystemInfo(getSystemConfig());
		Properties mailProps = getMailProperty(systemInfo);
		String fromAddress= (String)systemInfo.get("smtp.from");

		return sendMail(mailProps, fromAddress, toAddress, subject, contents);
	}

	private Properties getMailProperty(Map systemInfo) {
		Properties props = new Properties();

		props.put("mail.smtp.host", systemInfo.get("smtp.host") );

		return props;
	}

	private Address[] toAddress( String[] toAddObj ) throws AddressException {
		InternetAddress[] toAdd;
		String[] strs = toAddObj;
		toAdd = new InternetAddress[strs.length];
		for( int idx = 0; idx < strs.length; idx++ ) {
			toAdd[idx] = new InternetAddress(strs[idx]);
		}

		return toAdd;
	}

	private boolean sendMail( Properties mailProps, String fromAddress, String[] toAddress, String subject, String htmlContents ) {
//		Properties property = getMailProperty();
		Session session = Session.getDefaultInstance( mailProps );

		MimeBodyPart mbp = new MimeBodyPart();
		try {
			mbp.setContent( htmlContents, "text/html; charset=utf-8" );
		} catch( MessagingException messageEx ) {
			logger.error( messageEx );
			return false;
		}

		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setFrom( new InternetAddress(fromAddress) );
			msg.setRecipients(Message.RecipientType.TO, toAddress(toAddress));
			msg.setSubject(subject, "utf-8");

			InternetAddress[] recipients = (InternetAddress[])msg.getRecipients( Message.RecipientType.TO );
			if( recipients != null && recipients.length > 0 ) {
				Multipart mp = new MimeMultipart();
				mp.addBodyPart( mbp );
				msg.setContent( mp );

				msg.setSentDate( Calendar.getInstance().getTime() );
				Transport.send(msg);
				logger.info("sendMail success. (Title: "+ subject +") (To: "+java.util.Arrays.asList(recipients).toString()+")");
				return true;
			}
		} catch( MessagingException messageEx ) {
			logger.error( messageEx );
			return false;
		}

		return false;
	}

}
