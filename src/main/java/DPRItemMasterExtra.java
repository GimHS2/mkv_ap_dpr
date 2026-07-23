
/*
 *	File Name:	DPRItemMasterExtra.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		pub_upload_input.jsp
 *		dpr_itemextra_input.jsp
 *		dpr_itemextra_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.1	isChinaCountry() 삭제
 *	jbaek		2018/04/30		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.CountryCondition;
import com.irt.dpr.Item;
import com.irt.dpr.ItemMasterExtra;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/*
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/DPRItemMasterExtra" } )
public class DPRItemMasterExtra extends DPRServletModel {

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletModelException, SQLException {
		ParameterMap conditionMap = new ParameterMap(ctx.req, true);

		setDefaultParameter(ctx, conditionMap);
		Condition.putConditionValueOnly(conditionMap, "countryCode", getUserCountryCode(ctx));
		Condition.putConditionValueOnly(conditionMap, "displayLanguage", getDisplayLanguage(ctx));

		String itemCode = getConditionValue(conditionMap.getParameter("itemCode"));
		if( itemCode != null )
			Condition.putConditionValueOnly(conditionMap, "itemCode", itemCode, getConditionType(itemCode));

		String itemName = getConditionValue(conditionMap.getParameter("itemName"));
		if( itemCode != null && itemName == null ) {
			conditionMap.put("itemName", new Item(ctx.handler).getName(itemCode, getDisplayLanguage(ctx)));
		}
		if( itemName != null ) {
			Condition.putConditionValueOnly(conditionMap, "itemName", getConditionValue(itemName), Condition.CONDTYPE_CONTAINS);
		}

		return conditionMap;
	}

	private String getConditionType( String value ) {
		if( value.indexOf("%") >= 0 )
			return Condition.CONDTYPE_LIKE;
		else if( value.indexOf("_") >= 0 )
			return Condition.CONDTYPE_LIKE;

		return Condition.CONDTYPE_EQUALS;
	}

	private String getConditionValue( String value ) {
		if( value == null )
			return null;
		List<String[]> list = new java.util.ArrayList<String[]>();
		if( value.indexOf("*") >= 0 )
			list.add(new String[] { "*", "%" });
		if( value.indexOf("?") >= 0 )
			list.add(new String[] { "?", "_" });

		Object[] regex = new Object[list.size()];
		list.toArray(regex);

		if( regex.length > 0 ) {
			int position = -1;
			for( int i = 0; i < regex.length; i++ ) {
				String[] str = (String[])regex[i];

				while( ( position = value.indexOf(str[0]) ) >= 0 ) {
					if( position == 0 )
						value = str[1] + value.substring(1);
					else if( position > 0 )
						value = value.substring(0, position) + str[1] + value.substring(position + 1);
				}
			}
		}

		return value;
	}

	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize(ctx, true);
	}

	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemMasterExtra.INF");
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemMasterExtra.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemMasterExtra.MNG");
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemMasterExtra.MNG");
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemMasterExtra.MNG");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemMasterExtra.MNG");
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemMasterExtra.MNG");
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new ItemMasterExtra(ctx.handler);
		pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_ITEMMASTEREXTRA_", ctx.mode.toUpperCase()));
	}

	protected boolean download( Context ctx ) throws ServletException, IOException, SQLException {
		ItemMasterExtra db = (ItemMasterExtra)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRItemMasterExtra%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);

		DataWriter out = createDataWriter(ctx, "ItemMasterExtra" + "_" + getSavedOrganizationCode(ctx));

		try {
			ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
			db.write(out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE);
		} catch( SQLException sqlEx ) {
			out.println();
			out.print(sqlEx.getMessage());
			logger.error("Internal Error", sqlEx);
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		ItemMasterExtra db = (ItemMasterExtra)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRItemMasterExtra%LIST");

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
		List<Map<String, Object>> recordList = db.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("condition", conditionMap);
		ctx.req.setAttribute("columnList", columnList);

		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRItemMasterExtra.MNG"));

		return forward(ctx, systemConfig.getJspPath() + "/dpr_itemextra_list.jsp");
	}

	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRItemMasterExtra.LST") )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = ( (ItemMasterExtra)ctx.db ).getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
	}

	protected boolean upload( Context ctx ) throws ServletException, SQLException, IOException {
		UploadLog logDB = new UploadLog(ctx.handler);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRItemMasterExtra%DOWN");
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put("organizationCode", getSavedOrganizationCode(ctx));
			defaultMap.put("countryCode", getUserCountryCode(ctx));
			defaultMap.put("divisionCode", getDivisionCode(ctx));
			defaultMap.put("updateUserId", ctx.sessionMng.getUniqId());

			ItemMasterExtra db = (ItemMasterExtra)ctx.db;

			String[] updateFieldKeys = { "itemExtraCate", "itemExtraDesc", "itemExtraAbbrev", "itemExtraSpec", "uomNameLocal", "updateUserId" };
			loader = db.createDataLoader(fieldKeys, defaultMap, updateFieldKeys, Record.INSERT_OR_UPDATE_OR_DELETE);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put("systemCode", "DPR");
			resultMap.put("uploadType", "IMTEXT");
			resultMap.put("userId", ctx.sessionMng.getUniqId());

			RecordFormat messageFormat = PatternRecordFormat.getInstance("%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}");
			RecordFormat lineNameFormat = PatternRecordFormat
				.getInstance("[${itemCode}] - [${itemExtraCate}] - [${itemExtraDesc}] - [${itemExtraAbbrev}] - [${itemExtraSpec}] - [${uomNameLocal}]");

			loaderLogger = logDB.createLogger(resultMap, messageFormat, lineNameFormat);
			DataLoader dataLoader = createDataLoader(ctx, ctx.handler, loader, loaderLogger, validator, true);

			try {
				DataReader reader = dataLoader.getDataReader();
				// System.out.println("reader = " + reader.toString());
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList) && !reader.isEOF(); i++ ) {
					// System.out.println("count = " + com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList));
					try {
						reader.readNext();
					} catch( DataException dataEx ) {
					}
					// if( reader.getLineString() != null && out != null )
					// System.out.println("lineNumber = " + reader.getLineNumber());
				}
				reader.setTrim(true);
				dataLoader.execute();
				ctx.pageConfig.setMessage((String)loaderLogger.getResultMap().get("message"));
			} finally {
				dataLoader.close(false);
			}
			loader = null;

			String uploadInputPath = "DPRItemMasterExtra?" + PARAM_MODE + "=" + MODE_UPLOADINPUT;
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

	protected boolean uploadInput( Context ctx ) throws SQLException, IOException, ServletException {
		Map<String, Object> recordMap = new ParameterMap(ctx.req);
		recordMap = createConditionMap(ctx);

		String organizationCode = Record.extractString(recordMap, "organizationCode");
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		recordMap.put("encoding", com.irt.dpr.Country.getDefault(organizationCode, "encoding"));

		ctx.req.setAttribute("record", recordMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp");
	}

	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ItemMasterExtra db = (ItemMasterExtra)ctx.db;

		Map<String, Object> recordMap = createConditionMap(ctx);
		recordMap.put("updateUserId", ctx.sessionMng.getUniqId());
		ctx.req.setAttribute("record", recordMap);
		ctx.req.setAttribute("fieldSet", db.getFieldSet(inserting));

		try {
			if( inserting ) {
				db.regist(recordMap);
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REGIST_SUCCESS"));
			} else {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException(DataException.ERR_NO_RECORD_UPDATE);
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS"));
			}

			ctx.req.setAttribute("record", db.getRecord(recordMap));
			ctx.pageConfig.setManageAuth(true);
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);

			return forward(ctx, systemConfig.getJspPath() + "/dpr_itemextra_input.jsp");
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(dataEx.getMessage());
			logger.info("error.", dataEx);
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(sqlEx.getMessage());
			logger.error("internal error.", sqlEx);
		}

		String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
		ctx.pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_ITEMMASTEREXTRA_" + mode.toUpperCase()));

		return registInput(ctx);
	}

	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		String organizationCode = ctx.req.getParameter("organizationCode");
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition(ctx.handler);
			Map<String, Object> ccndMap = ccnd.getRecord(CountryCondition.createPrimary(ctx.sessionMng.getGln(), organizationCode));

			recordMap.put("organizationCode", ccndMap.get("organizationCode"));
			recordMap.put("organizationName", ccndMap.get("organizationName"));

			ctx.req.setAttribute("record", recordMap);
		}

		return forward(ctx, systemConfig.getJspPath() + "/dpr_itemextra_input.jsp");
	}

	protected boolean remove( Context ctx ) throws SQLException, IOException, ServletException {
		ItemMasterExtra db = (ItemMasterExtra)ctx.db;

		String[] organizationCodes = ctx.req.getParameterValues("organizationCode");
		String[] itemCodes = ctx.req.getParameterValues("itemCode");

		String del = ctx.req.getParameter("isdeleteAll");
		String organizationCode_all = getSavedOrganizationCode(ctx);

		if( del == null ) {
			if( ctx.pageConfig.getBackURL() == null )
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
			else if( organizationCodes == null || organizationCodes.length == 0 )
				throw new ServletModelException(ServletModelException.INVALID_PARAMETER);
			else if( itemCodes == null || itemCodes.length == 0 )
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		}

		int count = 0;

		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		if( del != null ) {
			try {
				if( del != null )
					db.deleteAll(); // Data delete All
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add(createErrorMap(db.getFieldValue(primaryMap, "title"), dataEx));
			}
		}

		if( del == null ) {
			for( int i = 0; i < itemCodes.length; i++ ) {
				primaryMap = ItemMasterExtra.createPrimary(organizationCodes[i], itemCodes[i]);
				try {
					if( db.delete(primaryMap) ) {
						count++;
						ctx.handler.commit();
					}
				} catch( DataException dataEx ) {
					ctx.handler.rollback();
					errorList.add(createErrorMap(db.getFieldValue(primaryMap, "title"), dataEx));
				}
			}
		}

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute("errors", errorList);
			return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
		} else {
			if( del != null ) {
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_ALL_SUCCESS"));
			} else {
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)));
			}

			if( ctx.pageConfig.getBackURL() == null ) {
				String redirectURL = systemConfig.getClassURL() + "/DPRItemMasterExtra?menu=portal"
						+ "&locale=" + getDisplayLanguage(ctx)
						+ "&organizationCode=" + getSavedOrganizationCode(ctx);
				return sendRedirect(ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)));
			}

			return sendRedirect(ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)));
		}
	}

	public boolean info( Context ctx, boolean inserting ) throws SQLException, IOException, ServletException {
		ItemMasterExtra db = (ItemMasterExtra)ctx.db;

		String organizationCode = ctx.req.getParameter("organizationCode");
		String itemCode = ctx.req.getParameter("itemCode");

		Map<String, Object> primaryMap = ItemMasterExtra.createPrimary(organizationCode, itemCode);

		Map<String, Object> recordMap = db.getRecord(primaryMap);
		if( recordMap == null )
			throw new ServletModelException(ServletModelException.NO_RECORD_FOUND);
		ctx.req.setAttribute("record", recordMap);

		// forward
		if( inserting ) {
			ctx.req.setAttribute("fieldSet", db.getFieldSet(false));
			return registInput(ctx);
		} else {
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);
			ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRItemMasterExtra.MNG"));
			ctx.req.setAttribute("fieldSet", db.getFieldSet(true));

			return forward(ctx, systemConfig.getJspPath() + "/dpr_itemextra_input.jsp");
		}
	}
}
