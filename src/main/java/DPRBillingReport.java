/*
 *	File Name:	DPRBillingReport.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		pub_upload_input.jsp
 *		dpr_itemean_input.jsp
 *		dpr_itemean_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.4	신규 UI/UX 적용
 *	hankalam	2020/06/30		2.2.3	list(): columnList 옵션키 useDangerousItem 추가
 *	jbaek		2019/11/30		2.2.2	PartyAuth 권한 처리
 *	hankalam	2019/06/28		2.2.1	useCustomerPONumber 옵션 적용
 *	jbaek		2017/09/30		2.2.0	create
 *
**/

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataResult;
import com.irt.data.DataWriter;
import com.irt.data.Record;
import com.irt.data.cols.Column;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.BillingReport;
import com.irt.dpr.util.CondPred;
import com.irt.html.HtmlUtility;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.TableDaoException;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;
import com.irt.util.FileType;
import com.irt.util.RBMWorkbook;
import com.irt.util.RBMWorkbookPicture;
import com.irt.util.SheetWriter;
import com.irt.util.StringUtil;

/*
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/DPRBillingReport" } )
public class DPRBillingReport extends DPRServletModel {
	protected final static String PARAM_TYPE					= "type";
	protected final static String TYPE_SOLD						= "sold";
	protected final static String TYPE_SHIP						= "ship";
	protected final static String TYPE_DOWNTEMPLATE = "dwntpl";

	private void addSheetImageForBillingLinkExternalSite( Sheet sheet, int colIdx, int rowIdx, String imageInClasspath, String siteAddress )
			throws IOException, URISyntaxException {
		XSSFDrawing drawing = (XSSFDrawing)sheet.createDrawingPatriarch();
		int dx1 = 10;
		int dy1 = dx1;
		int dx2 = 110;
		int dy2 = dx2;
		int col1 = colIdx;
		int row1 = rowIdx;
		int col2 = col1;
		int row2 = row1;

		int pictureIndex = RBMWorkbookPicture.addPictureData(sheet.getWorkbook(), this.getClass().getClassLoader(), imageInClasspath);
		XSSFClientAnchor anchor = drawing.createAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2);
		XSSFPicture pict = (XSSFPicture)RBMWorkbookPicture.createPicture(sheet, anchor, pictureIndex, true);
		URI target = new URI(siteAddress);
		RBMWorkbookPicture.addHyperlinkByImage((XSSFSheet)sheet, target, TargetMode.EXTERNAL, pict);
	}

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize(ctx, true);
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletModelException, SQLException {
		ParameterMap conditionMap = new ParameterMap(ctx.req, true);

		setDefaultParameter(ctx, conditionMap);
		if( !com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			Condition.clearCondition(conditionMap, "distributionChannelCode");// user may not in default distribution channel
		}

		Condition.putConditionValueOnly(conditionMap, "countryCode", getUserCountryCode(ctx));
		Condition.putConditionValueOnly(conditionMap, "displayLanguage", getDisplayLanguage(ctx));

		CondPred.putValueIfNoKey(conditionMap, "endOrderDate", com.irt.data.Date.getInstance(ctx.handler.getTimeZone()));
		String startOrderDate = Record.extractString(conditionMap, "startOrderDate");
		String endOrderDate = Record.extractString(conditionMap, "endOrderDate");
		try {
			com.irt.data.Date today = com.irt.data.Date.getInstance();
			if( endOrderDate != null && endOrderDate.length() > 0 ) {
				if( startOrderDate == null || startOrderDate.length() <= 0 ) {
					CondPred.putIsEquals(conditionMap, "orderDate", com.irt.data.Date.getInstance(endOrderDate));
				} else {// has startOrderDate
					conditionMap.put("orderDate" + Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startOrderDate));
					conditionMap.put("orderDate" + Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endOrderDate));
					conditionMap.put("orderDate" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MAX);
				}
			}
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException(ServletModelException.INVALID_PARAMETER);
		}

		// determine user's sold party code
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isAdminUser() || ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL") ) {
			// admin can search all distributors
		} else if( !conditionMap.containsKey("partyCode") ) {
			List<Map<String, Object>> userDistributors = getUserDistributorCodes(ctx);
			if( userDistributors != null ) {
				if( userDistributors.size() <= 0 ) {// user is no map to dist
					conditionMap.put("partyCode", "");
				} else if( userDistributors.size() == 1 ) {// user is map to single dist
					Map<String, Object> map = userDistributors.get(0);
					conditionMap.put("partyCode", map.get("partyCode"));
				} else {// user is map to multiple dist
					conditionMap.put("partyCode", "");
				}
			} else {// user is map to no dist
				conditionMap.put("partyCode", "");
			}
		}

		Map<String, Object> masterConditionMap = new java.util.HashMap<String, Object>(conditionMap);
		setAttributePartner(ctx, masterConditionMap, PARTNER_SHIP);
		masterConditionMap.remove("partyCode");
		masterConditionMap.remove("soldPartyCode");
		setAttributePartner(ctx, masterConditionMap, PARTNER_SOLD);

		return conditionMap;
	}

	@Override
	protected boolean download( Context ctx ) throws ServletException, IOException, SQLException {
		BillingReport db = (BillingReport)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		if( !conditionMap.containsKey("organizationCode") )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		String optionKey = null;
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useCustomerPONumber") ) {
			optionKey = "PO";
		}

		String type = ctx.req.getParameter("type");
		if( TYPE_DOWNTEMPLATE.equals(type) ) {
			com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRBillingReport%DOWN.TPL", optionKey );
			String filenamePattern = columnList.getProperty("filename");
			String filename = PatternRecordFormat.getInstance(filenamePattern)//
					.format(conditionMap, ctx.msghandler);

			DataWriter out = createTextDataWriter(ctx, filename);

			try {
				for( int i = 0; i < columnList.getColumnCount(); i++ ) {
					out.print(columnList.getColumn(i).getColumnTitle(null, ctx.msghandler));
				}
				out.println();
			} finally {
				out.flush();
				out.close();
			}
		} else {
			com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRBillingReport%DOWN", optionKey );
			String filenamePattern = columnList.getProperty("filename");
			String filename = PatternRecordFormat.getInstance(filenamePattern)//
					.format(conditionMap, ctx.msghandler);
			int[] idxVars = ctx.pageConfig.getListIndexVariables();
			ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
			List<Map<String, Object>> recordList = db.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);

			Workbook workbook = RBMWorkbook.createWorkbook(FileType.XLX);
			SheetWriter writer = new SheetWriter(ctx.handler, recordList, workbook);

			writer.setColumnList(columnList);
			try {
				writer.write(filename, null);
			} catch( DataException dataEx ) {
				logger.error(dataEx.getErrorKey(), dataEx);
				throw new ServletModelException(dataEx.getErrorKey(), dataEx.getMessage());
			}

			Sheet sheet = writer.getSheet();
			int colCount = columnList.getColumnCount();
			String organizationCode = (String)conditionMap.get("organizationCode");
			if( organizationCode == null || organizationCode.length() == 0 || !organizationCode.matches("^[A-Za-z0-9_-]+$") ) {
				throw new ServletModelException(ServletModelException.INVALID_REQUEST);
			}
			String imageInClasspath = "com/irt/dpr/BillingLinkExternalSite-" + organizationCode + ".png";
			String siteAddress = RBMSystem.getSystemEnv("DPR", "BillingLinkExternalSite;" + organizationCode);

			try {
				addSheetImageForBillingLinkExternalSite(writer.getSheet(), colCount, 1, imageInClasspath, siteAddress);
			} catch( URISyntaxException uriEx ) {
				logger.error("Internal Error", uriEx);
			}

			// auto size column
			for( int colNum = 0; colNum < colCount; colNum++ ) {
				sheet.autoSizeColumn(colNum, false);
			}

			writer.writeResponse(ctx.res, filename);
		}

		return true;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	@Override
	public List<Map<String, Object>> getUserDistributorCodes( Context ctx ) throws SQLException, ServletModelException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put("divisionCode", getDivisionCode(ctx));
		// conditionMap.put( "distributionChannelCode", getDistributionChannelCode( ctx ) );
		conditionMap.put("uniqId", ctx.sessionMng.getUniqId());
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		if( !ctx.sessionMng.isSystemAdmin() ) {
			conditionMap.put("authIndicator", "Y");
			conditionMap.put("partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE);
			conditionMap.put("baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD);
		}
		conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");

		List<Map<String, Object>> recordList = new com.irt.dpr.PartyAuth(ctx.handler).getRecords(conditionMap,
				new String[] { "partyCode", "partyName" });
		return recordList;
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);

		if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRBillingReport.DWN");
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRBillingReport.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRBillingReport.MNG");
		else if( MODE_REMOVE.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRBillingReport.MNG" );
		else if( MODE_CODENAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRBillingReport.LST");
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new BillingReport(ctx.handler);

		String messageKey = "TITLE_DPR_BILLINGREPORT_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_ORDER", "jsp.SUBMENU_BILLINGREPORT" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		BillingReport db = (BillingReport)ctx.db;

		// ready
		Map<String, Object> conditionMap = createConditionMap(ctx);

		String reqPartyCode = ctx.req.getParameter("partyCode");
		boolean userHasSoldParty = false;
		if( reqPartyCode != null && reqPartyCode.length() > 0 ) {
			String partyCode = null;
			List<Map<String, Object>> userSoldParties = (List<Map<String, Object>>)ctx.req.getAttribute("soldParties");
			if( userSoldParties != null && userSoldParties.size() > 0 ) {
				for( Map<String, Object> map : userSoldParties ) {
					partyCode = (String)map.get("linkPartyCode");
					if( !userHasSoldParty && reqPartyCode.equals(partyCode) ) {
						userHasSoldParty = true;
						break;
					}
				}
			}
			if( com.irt.system.SessionManager.USERCLASS_USER.equals(ctx.sessionMng.getUserClass()) && !userHasSoldParty ) {
				String message = ctx.msghandler.getMessage(ServletModelException.HAS_NOAUTH, reqPartyCode);
				throw new ServletModelException(ServletModelException.HAS_NOAUTH, message);
			}
		}

		String organizationCode = getSavedOrganizationCode( ctx );
		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") )
			optionKeyList.add( "PO" );
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
			optionKeyList.add( "OT" );
		}

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRBillingReport%LIST", optionKeyList.toArray(new String[optionKeyList.size()]) );
		boolean isDeleteAll = "Y".equals(conditionMap.get("isDeleteAll"));
		if( isDeleteAll ) {
			List<Map<String, Object>> recordList = db.getRecords(conditionMap, columnList.getFieldKeys());
			if( recordList != null && recordList.size() > 0 ) {
				try {
					DataResult dataResult = db.deleteAll(recordList);
					if( dataResult.getErrorCount() > 0 ) {
						ctx.req.setAttribute("errors", dataResult.getErrors());
						return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
					} else {
						ctx.handler.commit();
						ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_ALL_SUCCESS", //
								String.valueOf(dataResult.getDeleteCount())));
					}
				} catch( SQLException sqlEx ) {
					ctx.handler.rollback();
					try {
						throw ctx.handler.createDataException(DataException.ERR_NO_RECORD_DELETE);
					} catch( DataException dataEx ) {
						ctx.pageConfig.setMessage(dataEx.getMessage());
						logger.info("error.", dataEx);
					}
				}
			}
		}

		// process
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
		List<Map<String, Object>> recordList = db.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);

		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap,
				recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		ctx.req.setAttribute("condition", conditionMap);
		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("records", recordList);

		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRBillingReport.MNG"));

		// forward
		return forward(ctx, systemConfig.getJspPath() + "/dpr_billingreport_list.jsp");
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRBillingReport.LST") )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = ( (BillingReport)ctx.db ).getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, SQLException, ServletException {
		BillingReport db = (BillingReport)ctx.db;

		// requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox,
		// "organizationCode,partyCode,shipPartyCode,customerOrderNumber,billVatNumber,billDate" );

		Map<String, Object> paramMap = new ParameterMap(ctx.req);
		String[] organizationCodes = ctx.req.getParameterValues("organizationCode");
		String[] partyCodes = ctx.req.getParameterValues("partyCode");
		String[] billShipPartyCodes = ctx.req.getParameterValues("billShipPartyCode");
		String[] customerOrderNumbers = ctx.req.getParameterValues("customerOrderNumber");
		String[] billVatNumbers = ctx.req.getParameterValues("billVatNumber");
		String[] billDates = ctx.req.getParameterValues("billDate");

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( organizationCodes == null || organizationCodes.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( partyCodes == null || partyCodes.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( customerOrderNumbers == null || customerOrderNumbers.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( billVatNumbers == null || billVatNumbers.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( billDates == null || billDates.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < organizationCodes.length; i++ ) {
			try {
				primaryMap = db.extractPrimary(paramMap, i);
			} catch( TableDaoException taoEx ) {
				Map sourceMap = taoEx.getRecordMap();
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER, taoEx.getMessage());
			}
			try {
				String billShipPartyCode = null;
				if( billShipPartyCodes != null && billShipPartyCodes.length> 0) {
					billShipPartyCode = billShipPartyCodes[i];
				}
				if( db.deleteWith(primaryMap, billShipPartyCode) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add(createErrorMap(db.getFieldValue(primaryMap, "title"), dataEx));
			}
		}

		// set error or success
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute("errors", errorList);
		} else {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)));

			if( ctx.pageConfig.getBackURL() == null ) {
				String redirectURL = systemConfig.getClassURL() + "/DPRBillingReport"
						+ "?menu=portal"
						+ "&locale=" + getDisplayLanguage(ctx)
						+ "&organizationCode=" + getSavedOrganizationCode(ctx);
				ctx.pageConfig.setBackURL(redirectURL);
			}
		}

		// returns
		if( ctx.req.getAttribute("errors") != null ) {
			return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
		} else {
			return sendRedirect(ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)));
		}
	}

	@Override
	protected boolean upload( Context ctx ) throws ServletException, SQLException, IOException {
		UploadLog logDB = new UploadLog(ctx.handler);

		String optionKey = null;
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useCustomerPONumber") ) {
			optionKey = "PO";
		}

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRBillingReport%DOWN", optionKey );

		String[] updateFieldKeys = columnList.getProperty("updateFieldKeys").split(",\\s?+");
		if( updateFieldKeys == null ) {
			logger.error(ServletModelException.INTERNAL_ERROR + ": updateFieldKeys cannot be null.");
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		}
		for( int i = 0; i < updateFieldKeys.length; i++ ) {
			if( updateFieldKeys[i] != null )
				updateFieldKeys[i] = updateFieldKeys[i].trim();
		}

		// columnList.getcolumn
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;

		final Column[] columns = columnList.getColumns();
		DataLoader.Validator validator = new DataLoader.Validator() {

			@Override
			public void close() {
			}

			void throwOnSientificNotation( SQLHandler handler, Map<String, Object> recordMap, Column column ) throws DataException {
				if( recordMap.containsKey(column.getFieldKey()) ) {
					Object value = recordMap.get(column.getFieldKey());
					if( StringUtil.isScientificNotation((String)value) ) {
						String errMessage = handler.getMessageHandler().getMessage("ERR_INVALID_VALUE", //
								(String)column.getColumnTitle(), (String)value);
						throw handler.createDataException("ERR_INVALID_VALUE", errMessage);
					}
				}
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				for( Column column : columns ) {
					String type = StringUtil.extractAttrValue((String)column.getColumnAttr(), "type");
					if( "number".equals(type) ) {
						throwOnSientificNotation(handler, recordMap, column);
					}
				}
			}
		};
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put("organizationCode", getSavedOrganizationCode(ctx));
			defaultMap.put("updateUserId", ctx.sessionMng.getUniqId());

			BillingReport db = (BillingReport)ctx.db;

			// String[] updateFieldKeys = { "billPostNumber", "updateUserId" };
			loader = db.createDataLoader(fieldKeys, defaultMap, updateFieldKeys, Record.INSERT_OR_UPDATE_OR_DELETE);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put("systemCode", "DPR");
			resultMap.put("uploadType", "BILRPT");
			resultMap.put("userId", ctx.sessionMng.getUniqId());

			RecordFormat messageFormat = PatternRecordFormat.getInstance("%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}");
			StringBuilder sb = new StringBuilder();
			String delimStr = " - ";
			for( String key : fieldKeys ) {
				sb.append("[");
				sb.append("${" + key + "}");
				sb.append("]");
				sb.append(delimStr);
			}
			sb.delete(sb.length() - delimStr.length(), sb.length());
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance(sb.toString());

			loaderLogger = logDB.createLogger(resultMap, messageFormat, lineNameFormat);
			DataLoader dataLoader = createDataLoader(ctx, ctx.handler, loader, loaderLogger, validator, true);

			try {
				DataReader reader = dataLoader.getDataReader();
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList) && !reader.isEOF(); i++ ) {
					try {
						reader.readNext();
					} catch( DataException dataEx ) {
					}
					if( reader.getLineString() != null && out != null )
						out.println(reader.getLineString());
				}
				reader.setTrim(true);
				dataLoader.execute();
				ctx.pageConfig.setMessage((String)loaderLogger.getResultMap().get("message"));
			} finally {
				dataLoader.close(false);
			}
			loader = null;

			String uploadInputPath = "DPRBillingReport" + "?" + PARAM_MODE + "=" + MODE_UPLOADINPUT;
			String redirectURL = RBMUploadLog.getUploadLogURL(ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath);

			return sendRedirect(ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)));
		} finally {
			try {
				if( loader != null )
					loader.close();
			} catch( Exception ignored ) {
			}
		}
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws SQLException, IOException, ServletException {
		Map<String, Object> recordMap = new ParameterMap(ctx.req);
		recordMap = createConditionMap(ctx);

		String organizationCode = Record.extractString(recordMap, "organizationCode");
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		recordMap.put("encoding", "UTF8");
		ctx.req.setAttribute("record", recordMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp");
	}

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> paramMap = new ParameterMap( ctx.req );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( paramMap );
		setDefaultParameterMultiDist( ctx, conditionMap );

		if( !com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			com.irt.data.Condition.clearCondition(conditionMap, "distributionChannelCode");
		}

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}
}
