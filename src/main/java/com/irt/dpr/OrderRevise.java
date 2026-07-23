/*
 *	File Name:	OrderRevise.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.2	Sheet.autoSizeColumn 사용시 멈추는 현상 수정.
 *	jbaek		2023/03/30		2.2.1	RddOrderSteps에 RDD관련 로직 이동
 *	jbaek		2020/06/30		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.data.Date;
import com.irt.data.Record;
import com.irt.data.cols.ColumnList;
import com.irt.dpr.tools.Configure;
import com.irt.dpr.util.BasicData;
import com.irt.dpr.util.CondPred;
import com.irt.ics.BoardAttach;
import com.irt.ics.HelpBoard;
import com.irt.rbm.RBMSystem;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.system.SessionManager;
import com.irt.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletException;

import org.apache.poi.ss.usermodel.Workbook;

public class OrderRevise extends Order implements OrderSteps.Revise, OrderSteps.Rdd {
	public static final int DEFAULT_MAX_LIMIT_MODIFICATION = 5;
	public static final int DEFAULT_MAX_REVISE_ADD_PER_DAY = 5;
	public static final String MARKER_REVISE_TABLE = "<!--START_REVISE_TABLE_CONTENT-->";

	public static final String REVISE_STATUS_CQ		= "CQ";
	public static final String REVISE_STATUS_CP		= "CP";

	private final String ORDERREVISE_TEMPLATE		= "MailReviseOrderTemplate.html";

	private SystemConfig systemConfig;
	private RddOrderSteps rddOrderSteps;

	public OrderRevise( SQLHandler handler, SystemConfig systemConfig ) {
		super(handler, systemConfig);
		this.systemConfig = systemConfig;
		this.rddOrderSteps = new RddOrderSteps( handler, systemConfig );
	}

	public static String getSaferHelpBoardContent( Map recordMap ) {
		String rawAsHtml_reviseHbrdContent = (String)recordMap.get("reviseHbrdContent");

		if( rawAsHtml_reviseHbrdContent != null && rawAsHtml_reviseHbrdContent.length() > 0 ) {
			int endIdx = rawAsHtml_reviseHbrdContent.length();
			int delIdx = rawAsHtml_reviseHbrdContent.indexOf(com.irt.dpr.OrderRevise.MARKER_REVISE_TABLE);
			if( delIdx >= 0 ) {
				endIdx = delIdx;
				return rawAsHtml_reviseHbrdContent.substring(0, endIdx);
			} else {
				if( "ROD".equals(recordMap.get("reviseHelpType")) )
					return rawAsHtml_reviseHbrdContent;
				//				String danger_removed_html = rawAsHtml_reviseHbrdContent.replaceAll("(?i)<td[^>]*>", " ").replaceAll("\\s+", " ").trim();
				//				return danger_removed_html.substring(0, 100);
				// 				StringBuffer cleaned = new org.htmlcleaner.HtmlCleaner().clean(rawAsHtml_reviseHbrdContent).getText();
				// 				if( cleaned != null && cleaned.length() > 0 )
				// 					return cleaned.toString().substring(0, 100);
			}
		}
		return "";
	}

	public String createReviseOrder( String origOrderKey ) throws SQLException {
		CallableStatement cstmt = handler.getConnection().prepareCall("{ ? = call pkDPROrdRev.fGetOrdRevSim(?) }");
		cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);
		cstmt.setString(2, origOrderKey);
		cstmt.executeUpdate();
		return cstmt.getString(1);
	}

	public Map<String, Object> getMailContentMap( Map<String, Object> reqMap ) throws DataException, SQLException, IOException {
		String reviseOrderKey = (String)reqMap.get("orderKey");
		if( reviseOrderKey == null )
			throw new DataException(DataException.ERR_CANNOT_NULL,
					handler.getMessageHandler().getMessage("ERR_CANNOT_NULL", "orderKey"));
		String displayLanguage = (String)reqMap.get("displayLanguage");
		if( displayLanguage == null || displayLanguage.length() == 0 )
			throw new DataException(DataException.ERR_CANNOT_NULL,
					handler.getMessageHandler().getMessage("ERR_CANNOT_NULL", "displayLanguage"));

		String templateFilePath = "com/irt/dpr/tools/" + ORDERREVISE_TEMPLATE;
		URL tpl = getClass().getClassLoader().getResource(templateFilePath);
		if( tpl == null ) {
			throw new IOException(handler.getMessageHandler().getMessage("ERR_CANNOT_NULL", templateFilePath));
		}

		MessageHandler msghandler = handler.getMessageHandler();
		ValidableFieldSet content_vset = new ValidableFieldSet(new ValidableField[] {
				new ValidableField(false, "parentOrderNumber", "DPR_ORDREV_ORDERNUMBER", Field.TYPE_STRING),
				new ValidableField(false, "orderKey", "DPR_ORDER_KEY", Field.TYPE_STRING),
				new ValidableField(false, "soldPartyCode", "DPR_ORDREV_REQ_USER_CODE", Field.TYPE_STRING),
				new ValidableField(false, "uniqId", "USR_UNIQID", Field.TYPE_STRING),
				new ValidableField(false, "userName", "DPR_ORDREV_USERNAME", Field.TYPE_STRING),
				new ValidableField(false, "countryLanguageCode", "", Field.TYPE_STRING),
				new ValidableField(false, "organizationCode", "DPR_ORDER_ORGANIZATIONCODE", Field.TYPE_STRING),
				new ValidableField(false, "revHbrdClassCode", "DPR_ORDREV_HELPBOARD_CLASSCODE", Field.TYPE_STRING),
				new ValidableField(false, "reviseHbrdSeqId", "DPR_ORDREV_HELPBOARD_SEQID", Field.TYPE_INTEGER),
				new ValidableField(false, "reviseHelpType", "DPR_ORDREV_HEADWORD", Field.TYPE_STRING),
				new ValidableField(false, "revHbrdEmail", "DPR_ORDREV_EMAIL", Field.TYPE_STRING),
				new ValidableField(false, "revHbrdContent", "DPR_ORDREV_CONTENT", Field.TYPE_STRING),
				new ValidableField(true, "reviseServerAttachPath", "", Field.TYPE_STRING),
				new ValidableField(true, "reviseFileName", "", Field.TYPE_STRING),
				new ValidableField(true, "reviseServerFileName", "", Field.TYPE_STRING)
		});

		Map<String, Object> pmap = new HashMap<String, Object>();
		pmap.put("orderKey", reqMap.get("orderKey"));
		pmap.put("useRevOrd", "Y");
		pmap.put("displayLanguage", displayLanguage);
		Map<String, Object> contentMap = this.getRecord(pmap, content_vset.getFieldKeyArray());
		contentMap.put("MSG_DPR_ORDREV_EMAIL_STARTWORD", msghandler.getMessage("MSG_DPR_ORDREV_EMAIL_STARTWORD"));
		contentMap.put("MSG_DPR_ORDREV_EMAIL_STARTMESSAGE", msghandler.getMessage("MSG_DPR_ORDREV_EMAIL_STARTMESSAGE"));
		contentMap.put("reviseHelpTypeDesc", msghandler.getMessage("FIELD_DPR_ORDREV_HELPTYPE_" + contentMap.get("reviseHelpType")));
		contentMap.put("reviseHelpTypeTitle", msghandler.getMessage(( "MSG_DPR_ORDREV_HELPTYPE_TITLE_" + (String)contentMap.get("reviseHelpType") )));
		contentMap.put("uniqId", reqMap.get("uniqId"));
		contentMap.put("userName", reqMap.get("userName"));

		try {
			content_vset.validate(contentMap);
		} catch( FieldException fdEx ) {
			String message = "field error: " + "(" + fdEx.getErrorField().getFieldKey() + " : " + fdEx.getErrorField().getDescriptionKey() + ")";
			message += " " + contentMap;
			logger.error(message, fdEx);
			throw new DataException(DataException.ERR_ERROR, message, fdEx);
		}

		List<Map<String, Object>> changedList = this.reviseChangeDiffList(reviseOrderKey, displayLanguage);
		Map<String, Object> labelMap = null;
		if( changedList == null || changedList.size() == 0 ) {
			if( "ROM".equals(contentMap.get("reviseHelpType")) ) {
				throw new DataException("ERR_ORDREV_NOT_CHANGED", "ERR_ORDREV_NOT_CHANGED");
			}
		} else {
			MapUtil.pushConstantKeyValues(changedList, "reviseHelpType", contentMap.get("reviseHelpType"));
			labelMap = (Map<String, Object>)changedList.get(0).get("_columnLabels_");
			contentMap.put("_columnLabels_", labelMap);
			contentMap.put("_columnKeys_", changedList.get(0).get("_columnKeys_"));
			contentMap.put("_detailLines_", changedList);
			if( "ROM".equals(contentMap.get("reviseHelpType")) ) {
				contentMap.put("detailLinesHtml", createDetailLinesHtml(changedList, labelMap));
			}
		}

		String[] headerLabelKeys = new String[] { "soldPartyCode", "userName", "parentOrderNumber", "reviseHelpType", "revHbrdEmail", "revHbrdContent" };
		for( String key : headerLabelKeys ) {
			Field field = (Field)content_vset.getField(key);
			String msg = handler.getMessageHandler().getMessage(field.getDescriptionKey());
			if( contentMap.get(key) == null ) {
				throw new DataException(DataException.ERR_CANNOT_NULL
						, msghandler.getMessage("ERR_CANNOT_NULL"
								, msghandler.getMessage(field.getDescriptionKey()))
						);
			}
			contentMap.put(field.getDescriptionKey(), msg);
		}

		String html = BasicData.evalPlaceholder(tpl, contentMap, logger, "%{", "}%");
		contentMap.put("content", html);

		return contentMap;
	}

	public File createReviseOrderFile( Map<String, Object> conditionMap, ColumnList columnList, String orderKey, File tempDirectory )
			throws ServletException, IOException {

		String serverFileDirStr = com.irt.custom.SystemConfig.getInstance("ICS").getProperty("attachPath");
		java.io.File attachFileDir = new java.io.File(serverFileDirStr);
		if( !attachFileDir.exists() ) {
			attachFileDir.mkdir();
		}

		OrderDetail detailDB = new OrderDetail(handler);

		java.io.File tempFile = File.createTempFile(orderKey, ".tmp", tempDirectory);
		java.io.FileOutputStream outStrm = new java.io.FileOutputStream(tempFile);

		DataWriter out = RBMWorkbook.getDataWriter(outStrm, FileType.XLS, "");
		try {
			detailDB.write(out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE);
		} catch( SQLException sqlEx ) {
			out.println();
			out.print(sqlEx.getMessage());
			logger.error("internal error.", sqlEx);
		} finally {
			out.flush();
			out.close();
		}

		return tempFile;
	}

	private File createDetailLinesExcel( List<Map<String, Object>> recordList, Map<String, Object> labelsMap ) throws DataException {
		File tempDirectory = this.systemConfig.getTemporaryDirectory();
		File tempRevOrdDir = null;
		boolean exists = tempDirectory.exists();
		if( tempDirectory == null || !tempDirectory.exists() ) {
			throw new DataException(DataException.ERR_CANNOT_NULL, handler.getMessageHandler().getMessage("ERR_CANNOT_NULL", "tempDirectory"));
		}
		tempRevOrdDir = new File(tempDirectory, "revord");
		if( !tempRevOrdDir.exists() )
			tempRevOrdDir.mkdir();
		if( recordList == null || recordList.size() == 0 )
			return null;
		String reviseHelpType = (String)recordList.get(0).get("reviseHelpType");
		if( reviseHelpType == null )
			reviseHelpType = "RO";
		String name = ( recordList != null && recordList.size() > 0
				? reviseHelpType + "_" + (String)recordList.get(0).get("soldPartyCode") + "_" + (String)recordList.get(0).get("parentOrderNumber")
						: "temp" );
		File file = new File(tempRevOrdDir, name + "_" + System.currentTimeMillis() + ".xlsx");
		logger.debug( "Revise Order creating file: "+ file );
		java.io.FileOutputStream out;
		boolean success = false;
		try {
			out = new java.io.FileOutputStream(file);
			Workbook workbook = RBMWorkbook.createWorkbook(FileType.XLX);
			SSDataWriter dw = new SSDataWriter(out, workbook);
			String[] titleKeys = new String[] { "parentOrderNumber", "lineNumber", "itemCodeConfirmed", "itemNameConfirmed", "reviseBeforeCnfQty",
					"reviseSimFinalQty", "reviseAfterCnfUom" };
			Integer[] titleSizes = new Integer[] { 100, 100, 100, 200, 100, 100, 100 };
			if( recordList != null && recordList.size() > 0 ) {
				dw.setDataType(SSDataWriter.TITLE);
				for( int i = 0; i < titleKeys.length; i++ ) {
					dw.print(labelsMap.get(titleKeys[i]));
				}
				dw.println();
				dw.setDataType(SSDataWriter.DATA);
				for( Map<String, Object> map : recordList ) {
					for( int i = 0; i < titleKeys.length; i++ ) {
						dw.print(map.get(titleKeys[i]));
					}
					dw.println();
				}
			}

			org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
			for( int i = 0; i < titleSizes.length; i++ ) {
				sheet.setColumnWidth( i, titleSizes[i]*50 );
			}
			dw.flush();
			dw.close();
			success = true;
		} catch( FileNotFoundException fnfEx ) {
			logger.error("error:" + file.getName(), fnfEx);
		} catch( IOException ioEx ) {
			logger.error("error:" + file.getName(), ioEx);
		}
		return ( success ? file : null );
	}

	private String createDetailLinesHtml( List<Map<String, Object>> recordList, Map<String, Object> labelsMap ) {
		String tableStyle = "border:1px solid black; border-collapse:collapse;";
		String thStyle = "background-color:#EFEFEF; border-bottom:1px solid E06308;";
		thStyle += "border-top:0px solid; border-left:0px solid; border-right:1px solid #C1C1C1";
		String centerStyle = "text-align:center;";
		String rightStyle = "text-align:right;";
		String tdStyle = "border-top:1px solid #FFFFFF; border-bottom:1px solid #C1C1C1; border-left:0px solid; border-right:1px solid #C1C1C1;";
		String trStyle = "border-left: 1px solid #000000; border-right:1px solid #000000";

		String theadTemplate = ""
				+ "<tr>" + "\n"
				+ "<th style=\"" + thStyle + "\">%{parentOrderNumber}%</th>" + "\t"
				+ "<th style=\"" + thStyle + "\">%{lineNumber}%</th>" + "\t"
				+ "<th style=\"" + thStyle + "\">%{itemCodeConfirmed}%</th>" + "\t"
				+ "<th style=\"" + thStyle + "\">%{itemNameConfirmed}%</th>" + "\t"
				+ "<th style=\"" + thStyle + " " + rightStyle + "\">%{reviseBeforeCnfQty}%</th>" + "\t"
				+ "<th style=\"" + thStyle + " " + rightStyle + "\">%{reviseSimFinalQty}%</th>" + "\t"
				+ "<th style=\"" + thStyle + " " + rightStyle + "\">%{reviseAfterCnfUom}%</th>" + "\t"
				+ "</tr>" + "\n"
				+ "";
		String tlineTemplate = ""
				+ "<tr style=\"" + trStyle + "\">" + "\n"
				+ "<td style=\"" + tdStyle + "\">%{parentOrderNumber}%</td>" + "\t"
				+ "<td style=\"" + tdStyle + "\">%{lineNumber}%</td>" + "\t"
				+ "<td style=\"" + tdStyle + "\">%{itemCodeConfirmed}%</td>" + "\t"
				+ "<td style=\"" + tdStyle + "\">%{itemNameConfirmed}%</td>" + "\t"
				+ "<td align=\"right\" style=\"" + tdStyle + " " + rightStyle + "\">%{reviseBeforeCnfQty}%</td>" + "\t"
				+ "<td align=\"right\" style=\"" + tdStyle + " " + rightStyle + "\">%{reviseSimFinalQty}%</td>" + "\t"
				+ "<td align=\"center\" style=\"" + tdStyle + " " + centerStyle + "\">%{reviseAfterCnfUom}%</td>" + "\t"
				+ "\n" + "</tr>"
				+ "";
		StringBuffer lineBuffers = new StringBuffer();
		if( recordList == null ) {
			logger.debug("list is null: ");
		} else {
			for( Map<String, Object> recordMap : recordList ) {
				if( recordMap.get("reviseBeforeCnfQty") == null )
					recordMap.put("reviseBeforeCnfQty", 0);
				if( recordMap.get("reviseBeforeCnfUom") == null )
					recordMap.put("reviseBeforeCnfUom", "");
				lineBuffers.append(StringUtil.evalPlaceholder(tlineTemplate, recordMap, "%{", "}%"));
			}
		}
		String detailLinesHtml = "<table style=\"" + tableStyle + "\">" + "<thead>"
				+ StringUtil.evalPlaceholder(theadTemplate, labelsMap, "%{", "}%")
				+ "</thead>" + "\n"
				+ "<tbody>" + lineBuffers.toString() + "</tbody>" + "\n"
				+ "</table>";
		return detailLinesHtml;
	}

	File getHelpBoardAttachServerFile( String attachManageKey, int attachNumber ) {
		return new File(com.irt.custom.SystemConfig.getInstance("ICS").getProperty("attachPath"),
				BoardAttach.getServerFileName(attachManageKey, attachNumber));
	}

	// protected Map<String, Object> getMailContentMap( String orderKey ) throws DataException, SQLException, IOException {
	// return createMailContentMap(Record.createMap("orderKey", orderKey));
	// }

	@Override
	public Date getRddByCalc( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, Date defaultDateWhenSapNull )
			throws ServletModelException, SQLException, DataException {
		return rddOrderSteps.getRddByCalc( headerMap, uniqId, timeZone, defaultDateWhenSapNull );
	}

	@Override
	public Date getRddByCalc( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, int defaultDragDays )
			throws ServletModelException, SQLException, DataException {
		return rddOrderSteps.getRddByCalc( headerMap, uniqId, timeZone, defaultDragDays );
	}

	@Override
	public Date getRddByChinaOrderClosingTime( Map<String, Object> headerMap, com.irt.data.Date rddFromSap, TimeZone timeZone ) {
		return rddOrderSteps.getRddByChinaOrderClosingTime( headerMap, rddFromSap, timeZone );
	}

	@Override
	public Date getRddFromSap( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, Date defaultDateWhenSapNull ) throws DataException {
		return rddOrderSteps.getRddFromSap( headerMap, uniqId, timeZone, defaultDateWhenSapNull );
	}

	protected List<Object> getReceiveEmailAddresses( String orderKey ) throws DataException, SQLException {
		String query = "" + "SELECT USR.EMAIL" +
				"  FROM DPR_ORDER ORD, DPR_PARTY_AUTH PAUT" +
				", USR_USER USR, USR_GROUP UGP" +
				" WHERE ORD.PARTYCD = PAUT.PARTYCD" +
				"   AND ORD.ORGANIZATIONCD = PAUT.ORGANIZATIONCD" +
				"   AND ORD.DIST_CHANNELCD = PAUT.DIST_CHANNELCD" +
				"   AND ORD.DIVISIONCD = PAUT.DIVISIONCD" +
				"   AND ORD.ORDER_KEY = ?" +
				"	AND PAUT.UNIQID = USR.UNIQID" +
				"   AND UGP.PARTYID = USR.PARTYID" +
				"   AND UGP.GROUP_ID = USR.GROUPID" +
				"   AND USR.EMAIL IS NOT NULL" +
				"";

		boolean isTestingEmail = RBMSystem.getSystemEnvBool("DPR", "Feature;testRevOrdSendMail", false);
		if( isTestingEmail ) {
			query += " AND USR.USERCLASS = 'SA'";
		} else {
			query += " AND UGP.GROUP_CLASS IN ('MR', 'OR', 'BA')";
		}

		List<Map<String, Object>> receiveEmails = SQLManager.getRecordList(handler, query, new Object[] { orderKey });
		if( receiveEmails == null || receiveEmails.size() == 0 ) {
			receiveEmails = new ArrayList<Map<String, Object>>();
		}

		List<Object> emailList = MapUtil.extractValueList(receiveEmails, "EMAIL");
		String userPartyId = (String)getFieldValue(Record.createMap("orderKey", orderKey), "userPartyId");
		String extraEmailCsv = RBMSystem.getSystemEnv("DPR", userPartyId + ";" + "partyRevOrdRcvEmails");
		if( extraEmailCsv != null && extraEmailCsv.length() > 0 ) {
			String[] extraEmails = extraEmailCsv.split(",");
			if( extraEmails != null ) {
				for( String email : extraEmails ) {
					if( !emailList.contains(email) )
						emailList.add(email);
				}
			}
		}

		if( emailList == null || emailList.size() == 0 ) {
			logger.error("OrderRevise Receive Emails not configured. Please configure it to send email.(failed to send email)");
		} else {
			logger.debug("OrderRevise Receive Emails configured.: " + emailList);
		}

		return emailList;
	}

	@Override
	public java.util.Map<java.lang.String, java.lang.Object> manageHelpBoard( String uniqId,
			java.util.Map<java.lang.String, java.lang.Object> boardMap,
			java.io.File... attachFiles ) throws java.io.IOException, java.sql.SQLException, DataException {

		HelpBoard helpDB = new HelpBoard(handler);

		String attachManageKey = (String)boardMap.get("attachManageKey");
		String boardClassCode = (String)boardMap.get("boardClassCode");

		String reviseHelpType = (String)boardMap.get("reviseHelpType");
		if( reviseHelpType == null )
			reviseHelpType = "ROM";

		String headwordCode = (String)com.irt.sql.SQLManager.getObjectValue(handler,
				"SELECT HEADWORD_CD FROM ICS_BOARD_HEADWORD WHERE HEADWORD_DESC = ? AND HEADWORD_CD LIKE ? || '%'",
				new Object[] { reviseHelpType, boardMap.get("boardClassCode") });
		if( headwordCode == null )
			throw new DataException(DataException.ERR_CANNOT_NULL, "headwordCode");

		boardMap.put("headwordCode", headwordCode);
		boardMap.put("headwordName", reviseHelpType);

		boolean inserting = ( boardMap.get("boardNumber") == null );
		int boardNumber = ( inserting ? -1 : Integer.parseInt((String)boardMap.get("boardNumber")) );

		if( inserting ) {
			if( attachManageKey == null ) {
				boardMap.put("attachManageKey", attachManageKey = BoardAttach.makeAttachManageKey(boardClassCode, uniqId));
			}

			boardNumber = helpDB.regist(boardMap);
			// if( boardNumber > 0 && attachManageKey != null ) {
			// if( !"ROD".equals(reviseHelpType) && attachFiles != null ) {
			// this.manageHelpBoardAttaches(boardMap, boardNumber, attachFiles);
			// }
			// } else {
			// // throw ctx.handler.createDataException(DataException.ERR_CANNOT_INSERT);
			// }
		} else {
			if( boardNumber > 0 && attachManageKey == null )
				boardMap.put("attachManageKey", attachManageKey = BoardAttach.makeAttachManageKey(boardClassCode, uniqId));
			// if( boardNumber > 0 && attachManageKey != null ) {
			// if( !"ROD".equals(reviseHelpType) && attachFiles != null ) {
			// this.manageHelpBoardAttaches(boardMap, boardNumber, attachFiles);
			// }
			// } else {
			// // throw ctx.handler.createDataException(DataException.ERR_NO_RECORD_UPDATE);
			// }
			// int updCnt = helpDB.modify(boardMap);
		}

		if( boardNumber > 0 )
			boardMap.put("boardNumber", String.valueOf(boardNumber));

		return boardMap;
	}

	@Override
	public boolean manageHelpBoardAttaches( java.util.Map<java.lang.String, java.lang.Object> boardParamMap, int boardNumber,
			java.io.File... attachFiles ) throws com.irt.data.DataException, java.io.IOException, java.sql.SQLException {
		BoardAttach db = new BoardAttach(handler);

		String attachManageKey = (String)boardParamMap.get("attachManageKey");
		String boardClassCode = (String)boardParamMap.get("boardClassCode");

		File[] paramNames = attachFiles;

		int[] attachNumbers = new int[paramNames == null ? 0 : paramNames.length];
		if( paramNames != null && paramNames.length > 0 ) {
			Map<String, Object> attachMap = new java.util.HashMap<String, Object>();

			attachMap.put("attachManageKey", attachManageKey);
			attachMap.put("boardClassCode", boardClassCode);
			attachMap.put("boardNumber", Integer.valueOf(boardNumber));
			attachMap.put("filePath", com.irt.custom.SystemConfig.getInstance("ICS").getProperty("attachPath"));
			attachMap.put("fileType", BoardAttach.ATTACHTYPE_FILE);

			for( int i = 0; i < paramNames.length; i++ ) {
				File file = paramNames[i];

				while( true ) {
					try {
						attachNumbers[i] = db.getNextAttachNumberByManageKey(attachManageKey);

						attachMap.put("attachNumber", Integer.valueOf(attachNumbers[i]));
						attachMap.put("contentType", RBMWorkbook.getResponseContentType(FileType.XLS));

						if( file != null ) {
							attachMap.put("fileSize", Long.valueOf(file.length() / 1024L));
							attachMap.put("serverFileName",
									BoardAttach.getServerFileName(attachManageKey, attachNumbers[i]));

							String reviseHelpType = (String)boardParamMap.get("reviseHelpType");

							String userFileName = reviseHelpType + "_" + boardParamMap.get("parentOrderNumber")
							+ "_" + com.irt.data.Date.getInstance(handler.getTimeZone()) + ".xls";
							attachMap.put("fileName", userFileName);
							if( "ROM".equals(reviseHelpType) ) {
								// db.saveAttachFileToServer((String)attachMap.get("filePath"), file, userFileName);
								db.regist(attachMap, file);
							} else if( "ROD".equals(reviseHelpType) ) {
								db.saveAttachFileToServer((String)attachMap.get("filePath"), file, userFileName);
							}

							file.delete();
						}
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
	public List<Map<String, Object>> reviseChangeDiffList( String reviseOrderKey, String displayLanguage ) throws DataException, SQLException {
		OrderDetail odtl = new OrderDetail(handler);

		Map<String, Object> conditionMap = com.irt.data.Record.createMap("orderKey", reviseOrderKey);
		conditionMap.put(Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);
		conditionMap.put("useRevOrd", "Y");
		conditionMap.put("displayLanguage", displayLanguage);
		CondPred.putIsEquals(conditionMap, "revChgedInd", "Y");

		MessageHandler msghandler = handler.getMessageHandler();

		ValidableFieldSet diff_vset = new ValidableFieldSet(new ValidableField[] {
				new ValidableField(false, "parentOrderNumber", "DPR_ORDREV_MAIL_ORDERNUMBER", ValidableField.TYPE_STRING),
				new ValidableField(false, "orderKey", "DPR_ORDERKEY", ValidableField.TYPE_STRING),
				new ValidableField(false, "lineNumber", "DPR_ORDERDTL_LINENUMBER", ValidableField.TYPE_INTEGER),
				new ValidableField(false, "itemCode", "DPR_ITEM_CODE", ValidableField.TYPE_STRING),
				new ValidableField(false, "itemCodeConfirmed", "DPR_ITEM_CODE", ValidableField.TYPE_STRING),
				new ValidableField(false, "itemName", "DPR_ITEM_MASTER_NAME", ValidableField.TYPE_STRING),
				new ValidableField(false, "itemNameConfirmed", "DPR_ITEM_MASTER_NAME", ValidableField.TYPE_STRING),
				new ValidableField(true, "reviseSimFinalQty", "DPR_ORDERDTL_REVSIM_FINQTY", ValidableField.TYPE_INTEGER),
				new ValidableField(false, "reviseAfterCnfQty", "DPR_ORDERDTL_REVAF_CNFQTY", ValidableField.TYPE_INTEGER),
				new ValidableField(false, "reviseAfterCnfUom", "DPR_ORDERDTL_REVAF_CNFUOM", ValidableField.TYPE_STRING),
				new ValidableField(true, "reviseBeforeCnfQty", "DPR_ORDERDTL_REVBF_CNFQTY", ValidableField.TYPE_INTEGER),
				new ValidableField(true, "reviseBeforeCnfUom", "DPR_ORDERDTL_REVBF_CNFUOM", ValidableField.TYPE_STRING),
				new ValidableField(false, "revChgedInd", "DPR_ORDERDTL_REV_CHANGED_IND", ValidableField.TYPE_STRING),
				new ValidableField(false, "soldPartyCode", "FIELD_DPR_ORDREV_USERNAME", ValidableField.TYPE_STRING)
		});

		Map<String, Object> labelMap = new HashMap<String, Object>();
		for( String k : diff_vset.getFieldKeyArray() ) {
			AbstractField d = diff_vset.getField(k);
			labelMap.put(k, msghandler.getMessage(d.getDescriptionKey()));
		}

		List<Map<String, Object>> reviseList = odtl.getRecords(conditionMap, diff_vset.getFieldKeyArray());
		if( reviseList != null && reviseList.size() > 0 ) {
			Map<String, Object> recordMap = reviseList.get(0);
			try {
				diff_vset.validate(recordMap);
			} catch( FieldException fEx ) {
				throw new DataException(fEx.getErrorKey()
						, msghandler.getMessage(fEx.getMessage()
								, msghandler.getMessage(fEx.getErrorField().getDescriptionKey()))
						, fEx
						, recordMap);
			}
			reviseList = MapUtil.pushConstantKeyValues(reviseList, "_columnLabels_", labelMap);
		}

		return reviseList;
	}

	@Override
	public boolean reviseCommit( String reviseOrderKey, String reviseHelpType, int helpBoardSeqId ) throws DataException, IOException, SQLException {
		if( "ROD".equals(reviseHelpType) ) {// set null to distinguish completely new record and old record.
			com.irt.sql.SQLManager.executeStatement(handler, "UPDATE DPR_ORDER_DTL SET REVSIM_FINQTY = NULL WHERE ORDERKEY = ?", new Object[] { reviseOrderKey });
		}
		return updateParentRevStatus(reviseOrderKey, "CQ", helpBoardSeqId)
				&& ( com.irt.sql.SQLManager.executeStatement(handler, "UPDATE ICS_HELP_BOARD HBRD SET HBRD.ORDERKEY = ?"
						+ " WHERE HBRD.ORDER_NUMBER = (SELECT REVP.ORDER_NUMBER"
						+ " FROM DPR_ORDER REVP, DPR_ORDER ORD"
						+ " WHERE REVP.ORDER_KEY = ORD.PARENT_ORDERKEY AND ORD.ORDER_KEY = ?)"
						+ " AND HBRD.SEQID = ?", new Object[] { reviseOrderKey, reviseOrderKey, helpBoardSeqId }) > 0 )
				&& ( com.irt.sql.SQLManager.executeStatement(handler
						, "UPDATE DPR_ORDER ORD SET REV_STATUS = 'CQ', REV_HDSEQID = ? WHERE ORDER_KEY = ?"
						, new Object[] { helpBoardSeqId, reviseOrderKey }) > 0 );
	}

	private boolean updateParentRevStatus( String reviseOrderKey, String reviseStatus, int helpBoardSeqId ) throws DataException, SQLException {
		return ( SQLManager.executeStatement(handler, "UPDATE DPR_ORDER ORD SET REV_STATUS = ?, REV_HDSEQID = ?"
				+ " WHERE ORDER_KEY = (SELECT PARENT_ORDERKEY FROM DPR_ORDER WHERE ORDER_KEY = ?)",
				new Object[] { reviseStatus, helpBoardSeqId, reviseOrderKey }) > 0 );
	}

	@Override
	public Map<String, Object> reviseCommitProcessMailContentMap( String reviseOrderKey, SessionManager sessionMng, String displayLanguage )
			throws DataException, SQLException, IOException {
		MessageHandler msghandler = handler.getMessageHandler();

		Map<String, Object> contentMap = null;
		try {
			Map<String, Object> reqMap = Record.createMap("orderKey", reviseOrderKey);
			reqMap.put("uniqId", sessionMng.getUniqId());
			reqMap.put("userName", sessionMng.getUserName());
			reqMap.put("displayLanguage", displayLanguage);
			contentMap = getMailContentMap(reqMap);
		} catch( IOException ioEx ) {
			throw new DataException(DataException.ERR_ERROR, ioEx.getMessage(), ioEx);
		}

		String hbrdClassCode = (String)contentMap.get("revHbrdClassCode");
		Integer hbrdNumber = Utility2.DataField.toInteger(contentMap.get("reviseHbrdSeqId"));
		String boardOption = com.irt.ics.Board.BOARDOPTION_HTML;
		String content = (String)contentMap.get("revHbrdContent");
		String detailLinesHtml = (String)contentMap.get("detailLinesHtml");

		if( "ROM".equals(contentMap.get("reviseHelpType")) )
			content += MARKER_REVISE_TABLE + "<br></br><br></br>" + detailLinesHtml;

		String updateSql = "UPDATE ICS_HELP_BOARD SET BOARD_OPTION = ?, CONTENT = ?"
				+ " WHERE BOARDCLASSCD = ? AND SEQID = ?";
		if( SQLManager.executeStatement(handler, updateSql, new Object[] { boardOption, content, hbrdClassCode, hbrdNumber }) <= 0 ) {
			throw new DataException(DataException.ERR_CANNOT_UPDATE,
					msghandler.getMessage("ERR_CANNOT_UPDATE") + " orderNumber: " + contentMap.get("parentOrderNumber"), contentMap);
		}

		return contentMap;
	}

	@Override
	public javax.mail.Message reviseCommitSendMail( Map<String, Object> contentMap ) throws DataException, IOException, SQLException {
		MessageHandler msghandler = handler.getMessageHandler();

		String reviseOrderKey = (String)contentMap.get("orderKey");

		Map<String, Object> smtpMap = Configure.getSmtpMap(systemConfig.getProperty("toolsConfig"));

		Map<String, Object> sendMailMap = new HashMap<String, Object>();
		sendMailMap.put("id", smtpMap.get("smtp.userid"));
		sendMailMap.put("pw", smtpMap.get("smtp.password"));
		sendMailMap.put("host", smtpMap.get("smtp.host"));
		sendMailMap.put("smtpDebug", smtpMap.get("smtp.smtpDebug"));

		String subject = msghandler.getMessage("MSG_DPR_ORDREV_EMAILSUBJECT_TITLE", (String)contentMap.get("soldPartyCode"),
				(String)contentMap.get("reviseHelpTypeTitle"), (String)contentMap.get("parentOrderNumber")) + " [" + systemConfig.getSystemName()
				+ "]";

		String userPartyId = (String)getFieldValue(Record.createMap("orderKey", reviseOrderKey), "userPartyId");
		String partyRevOrdSndEmail = RBMSystem.getSystemEnv("DPR", userPartyId + "partyRevOrdSndEmail");
		sendMailMap.put("fromAddress", ( ( partyRevOrdSndEmail != null && partyRevOrdSndEmail.length() > 0 )
				? partyRevOrdSndEmail
						: smtpMap.get("smtp.from") ));

		List<Object> toAddress = getReceiveEmailAddresses(reviseOrderKey);
		if( toAddress == null )
			toAddress = new ArrayList<Object>();
		if( contentMap.get("revHbrdEmail") != null && !toAddress.contains(contentMap.get("revHbrdEmail")) )
			toAddress.add(contentMap.get("revHbrdEmail"));
		if( toAddress == null || toAddress.size() == 0 ) {
			logger.warn("OrderRevise Receive Emails not configured. sending email using fromAddress:" + contentMap.get("fromAddress"));
			toAddress.add(contentMap.get("fromAddress"));
		}

		sendMailMap.put("toAddress", Utility2.toCsv(toAddress));
		sendMailMap.put("subject", subject);
		sendMailMap.put("content", contentMap.get("content"));

		File serverFile = null;
		// String reviseServerAttachPath = (String)contentMap.get("reviseServerAttachPath");
		// if( contentMap.get("reviseServerFileName") != null && ( (String)contentMap.get("reviseServerFileName") ).length() > 0 ) {
		// serverFile = new File(reviseServerAttachPath, (String)contentMap.get("reviseServerFileName"));
		// if( "ROM".equals(contentMap.get("reviseHelpType")) && ( serverFile == null || !serverFile.exists() ) ) {
		// throw new DataException(DataException.ERR_CANNOT_NULL,
		// msghandler.getMessage("ERR_CANNOT_NULL", (String)contentMap.get("reviseServerFileName")),
		// contentMap);
		// }
		// }
		if( "ROM".equals(contentMap.get("reviseHelpType"))) {
			List<Map<String, Object>> detailLines = (List<Map<String, Object>>)contentMap.get("_detailLines_");
			Map<String, Object> labelsMap = (Map<String, Object>)contentMap.get("_columnLabels_");
			serverFile = createDetailLinesExcel(detailLines, labelsMap);
			if( serverFile != null ) {
				serverFile.deleteOnExit();
				logger.trace( "OrderRevise temp file created: " + serverFile.getCanonicalPath() + " size: " + serverFile.length() );
			}
		}

		String userFileName = (String)contentMap.get("reviseFileName");
		sendMailMap.put("attachNames", new String[] { userFileName });

		try {
			return com.irt.util.Utility2.sendMail(sendMailMap, logger, serverFile);
		} finally {
			if( serverFile != null ) {
				serverFile.delete();
				logger.trace( "OrderRevise temp file delete executed: " + serverFile + " isExists: " +  new File(serverFile.getCanonicalPath()).exists() );
			}
		}
	}

	public int reviseDetailQtyRemove( java.lang.String reviseOrderKey, String[] lineNumbers )
			throws java.sql.SQLException, com.irt.data.DataException {
		if( lineNumbers == null )
			return -1;

		int cnt = 0;
		if( lineNumbers != null ) {
			for( String lineNumber : lineNumbers ) {
				String statement = ""
						+ "UPDATE DPR_ORDER_DTL ODTL"
						+ "	SET ORDERQTY = 0, SIMULATION_ORDERQTY = 0, STATUS = 'DE', REVSIM_IPTQTY = 0, REVSIM_FINQTY = 0"
						+ " WHERE ORDERKEY = ? AND LINE_NO = ?"
						+ "";

				cnt += SQLManager.executeStatement(handler, statement, new Object[] { reviseOrderKey, lineNumber });
			}
		}
		return cnt;
	}

	@Override
	public int reviseDetailUpdate( java.lang.String reviseOrderKey ) throws java.sql.SQLException, com.irt.data.DataException {
		String detailSql = ""
				+ "MERGE INTO DPR_ORDER_DTL ODTL"
				+ "  USING ("
				+ "	SELECT ORD.REV_STATUS, ? ORDERKEY, ORD.ORDER_KEY PARENT_ORDERKEY, OIND.LINE_NO, OIND.CONFIRMED_ORDERQTY, OIND.ORDERQTY, OIND.UOM"
				+ "	  FROM DPR_ORDER_INFO_DTL OIND, DPR_ORDER ORD"
				+ "	 WHERE ORD.ORDER_NUMBER = OIND.ORDER_NUMBER"
				+ "	   AND ORD.ORDER_KEY = (SELECT PARENT_ORDERKEY FROM DPR_ORDER WHERE ORDER_KEY = ?)"
				+ "  ) OIND"
				+ "  ON (ODTL.ORDERKEY = OIND.ORDERKEY"
				+ "	  AND ODTL.LINE_NO = OIND.LINE_NO)"
				+ "  WHEN MATCHED THEN UPDATE"
				+ "  SET"
				+ " ODTL.REVBF_CNFQTY = NVL(ODTL.REVBF_CNFQTY,DECODE(ODTL.STATUS,'DE',0,NVL(OIND.CONFIRMED_ORDERQTY,0)))"
				+ "  , ODTL.REVBF_CNFUOM = NVL(ODTL.REVBF_CNFUOM,DECODE(ODTL.STATUS,'DE',NULL,OIND.UOM))"
				+ "";

		int cnt = com.irt.sql.SQLManager.executeStatement(handler, detailSql, new Object[] { reviseOrderKey, reviseOrderKey });

		return 1;
	}

	@Override
	public boolean reviseSimulationEnd( java.lang.String reviseOrderKey ) throws com.irt.data.DataException, java.sql.SQLException {
		try {
			if( com.irt.sql.SQLManager.executeStatement(handler, "UPDATE DPR_ORDER_DTL"
					+ " SET REVSIM_IPTQTY = ORDERQTY"
					+ " , ORDERQTY"
					+ "		= (CASE WHEN ORDERQTY > (NVL(SIMULATION_ORDERQTY,0)+NVL(REVBF_CNFQTY,0))"
					+ "				THEN NVL(REVBF_CNFQTY,0) + NVL(SIMULATION_ORDERQTY,0)"
					+ "				ELSE ORDERQTY"
					+ "				END)"
					+ " , REVSIM_FINQTY"
					+ "		= (CASE WHEN ORDERQTY > (NVL(SIMULATION_ORDERQTY,0)+NVL(REVBF_CNFQTY,0))"
					+ "				THEN NVL(REVBF_CNFQTY,0) + NVL(SIMULATION_ORDERQTY,0)"
					+ "				ELSE ORDERQTY"
					+ "				END)"
					+ " WHERE ORDERKEY = ?", new Object[] { reviseOrderKey }) > 0 ) {
				return true;
			}
		} catch( DataException dataEx ) {
			logger.error("error.", dataEx);
			throw dataEx;
		}
		return false;
	}

	@Override
	public boolean reviseSimulationStart( java.lang.String reviseOrderKey, Date rddDate ) throws com.irt.data.DataException, java.sql.SQLException {
		return ( com.irt.sql.SQLManager.executeStatement( handler, "UPDATE DPR_ORDER ORD SET INDATE = ? WHERE ORDER_KEY = ?"
				, new Object[] { rddDate, reviseOrderKey } )> 0);
	}

}
