
/*
 *	File Name:	DPRPackDealCfg.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/02/29		2.2.2	download() : 컬럼리스트 이름 변경, tryWorkbookAutoSizeColumn() 삭제
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	jbaek		2019/05/30		2.2.0	create
 *
**/

import com.irt.data.AbstractFieldSet;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataLoader.Validator;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.ManipulableManager;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.cols.ColumnList;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.LineProcessor;
import com.irt.dpr.PackDealCfg;
import com.irt.dpr.PackDealCfg.MngType;
import com.irt.dpr.PackDealCfgRlt;
import com.irt.html.ColumnConfigureFile;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.json.SubmodeManager;
import com.irt.rbm.TableDaoException;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@WebServlet( urlPatterns = { "/servlet/DPRPackDealCfg" } )
public class DPRPackDealCfg extends DPRServletModel {

	public final static String MODE_PUT = "put";

	private SubmodeManager submoder = new SubmodeManager();

	private Map<String, Object> createConditionMap( Context ctx ) throws SQLException, ServletException {
		ParameterMap parameterMap = new ParameterMap(ctx.req, true);
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("countryCode", getUserCountryCode(ctx));
		conditionMap.put("division", getDivisionCode(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));

		if( parameterMap.containsKey("distributionChannelCode") )
			Condition.putConditionValueOnly(conditionMap, "distributionChannelCode", parameterMap.get("distributionChannelCode"));
		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly(conditionMap, "officeCode", parameterMap.get("officeCode"));
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly(conditionMap, "groupCode", parameterMap.get("groupCode"));
		setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);

		if( !conditionMap.containsKey("distributionChannelCode") ) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
			conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );
		}

		setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly(conditionMap, "partyCode", parameterMap.get("partyCode"));

		if( parameterMap.containsKey("isPackdealDate") ) {
			Condition.putConditionValueOnly(conditionMap, "isPackdealDate", parameterMap.get("isPackdealDate"));
		}

		if( parameterMap.containsKey("dealCode") )
			Condition.putConditionValueOnly( conditionMap, "dealCode", parameterMap.get("dealCode"), Condition.CONDTYPE_CONTAINS );

		return conditionMap;
	}

	private Validator createValidator( Context ctx ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		PackDealCfg db = (PackDealCfg)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRPackDealHSSF.CFG%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		String filename = ctx.msghandler.getMessage("TITLE_DPR_PACKDEALCFG_");
		DataWriter out = createDataWriter(ctx, filename);

		try {
			ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
			db.write(out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE);
		} catch( SQLException sqlEx ) {
			out.println();
			out.print(sqlEx.getMessage());
			logger.error("internal Error", sqlEx);
		} finally {
			out.flush();
			out.close();
		}
		return true;
	}

	@Override
	protected ColumnList getColumnList( Context ctx, String columnListName, String... optionKeys ) throws ServletException {
		if( MODE_DOWNLOAD.equals(ctx.mode) || MODE_UPLOAD.equals(ctx.mode) ) {
			return getDownUpColumnList(ctx, columnListName, null, optionKeys);
		}

		return super.getColumnList(ctx, columnListName, optionKeys);
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	@Override
	public boolean info( Context ctx, boolean inserting ) throws SQLException, IOException, ServletException {
		PackDealCfg db = (PackDealCfg)ctx.db;

		Map<String, Object> paramMap = new ParameterMap(ctx.req);

		// ready
		Map<String, Object> primaryMap;
		try {
			primaryMap = db.extractPrimary(paramMap, 0);
		} catch( TableDaoException taoEx ) {
			Map<String, Object> sourceMap = taoEx.getRecordMap();
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER, taoEx.getMessage());
		}

		// process
		Map<String, Object> recordMap = db.getRecord(primaryMap);
		logger.debug(primaryMap + " : "+ recordMap);
		if( recordMap == null )
			throw new ServletModelException(ServletModelException.NO_RECORD_FOUND);

		ctx.req.setAttribute("record", recordMap);

		// forward
		if( inserting ) {
			ctx.req.setAttribute("fieldSet", db.getFieldSet(false));
			return registInput(ctx);
		} else {
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);
			ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRPackDealCfg.MNG"));
			ctx.req.setAttribute("fieldSet", db.getFieldSet(true));

			return forward(ctx, systemConfig.getJspPath() + "/dpr_packdealcfg_input.jsp");
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		ctx.pageConfig.setProperty("mngtype", "pdcfg");
		ctx.pageConfig.setProperty("mngtypeName", "PackDealCfg");

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfg.INF");
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfg.MNG");
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfg.MNG");
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfg.MNG_DEL");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfg.LST");
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfg.MNG");
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfg.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new PackDealCfg(ctx.handler);
		ctx.extraObj = new PackDealCfgRlt(ctx.handler);

		pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_PACKDEALCFG_" + ctx.mode.toUpperCase()));

		String messageKey = "TITLE_DPR_PACKDEALCFG_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_PACKDEALSETTING" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		PackDealCfg db = (PackDealCfg)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		setAttributePartyMaster(ctx, conditionMap,
				PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);

		setAttributePackDealCfg(ctx, conditionMap);
		switch( mngtype ) {
		case PackDealCfg:
			ctx.req.setAttribute("offices",
					getSelectListNaZeroValue(ctx, (List<Map<String, Object>>)ctx.req.getAttribute("offices"), "officeCode", "officeName"));
			ctx.req.setAttribute("groups",
					getSelectListNaZeroValue(ctx, (List<Map<String, Object>>)ctx.req.getAttribute("groups"), "groupCode", "groupName"));
			ctx.req.setAttribute("soldParties",
					getSelectListNaZeroValue(ctx, (List<Map<String, Object>>)ctx.req.getAttribute("soldParties"), "linkPartyCode", "linkPartyName"));
		}

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRPackDeal.CFG%LIST");

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
		List<Map<String, Object>> recordList = ( (QueryableManager)db ).getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);

		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("condition", conditionMap);

		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRPackDealCfg.MNG"));

		return

		forward(ctx, systemConfig.getJspPath() + "/dpr_packdealcfg_list.jsp");
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPackDeal.LST") )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = ( (QueryableManager)ctx.db ).getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
	}


	@Override
	public boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = createConditionMap(ctx);

		ctx.req.setAttribute("condition", conditionMap);

		// process
		if( ctx.req.getAttribute("record") == null ) {
			ctx.req.setAttribute("fieldSet", ( (ManipulableManager)ctx.db ).getFieldSet(true));

			Map<String, Object> record = new HashMap<String, Object>();
			if( conditionMap.containsKey("distributionChannelCode") ) {
				record.put("distributionChannelCode", conditionMap.get("distributionChannelCode"));
			} else {
				record.put("distributionChannelCode", getDistributionChannelCode(ctx));
			}

			ctx.req.setAttribute("record", record);
		}

		setAttributePartyMasterOnExisting(ctx, conditionMap,
				PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);

		// forward
		return forward(ctx, systemConfig.getJspPath() + "/dpr_packdealcfg_input.jsp");
	}

	@Override
	protected boolean remove( Context ctx ) throws SQLException, IOException, ServletException {
		PackDealCfg db = (PackDealCfg)ctx.db;

		// if( ctx.pageConfig.getBackURL() == null )
		// throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		Map<String, Object> paramMap = new ParameterMap(ctx.req);
		Map<String, Object[]> primaryKeyValues = null;
		try {
			primaryKeyValues = db.extractPrimaryKeyValues(paramMap);
		} catch( TableDaoException taoEx ) {
			Map sourceMap = taoEx.getRecordMap();
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER, taoEx.getMessage());
		}

		int count = 0;

		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		int keyslen = ( primaryKeyValues.values() == null ? 0 : primaryKeyValues.values().iterator().next().length );
		for( int i = 0; i < keyslen; i++ ) {
			try {
				primaryMap = db.extractPrimary(paramMap, i);
			} catch( TableDaoException taoEx ) {
				Map sourceMap = taoEx.getRecordMap();
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER, taoEx.getMessage());
			}
			try {
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				AbstractFieldSet fset = db.getFieldSet(true);
				StringBuffer sbuf = new StringBuffer();
				sbuf.append("[ ");
				for( String pk : primaryMap.keySet() ) {
					sbuf.append("${" + fset.getField(pk).getDescriptionKey() + "} (" + primaryMap.get(pk) + ")");
				}
				sbuf.append(" ]");
				errorList.add(createErrorMap(sbuf.toString(), dataEx));
			}
		}

		// set error or success
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute("errors", errorList);
		} else {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)));

			if( ctx.pageConfig.getBackURL() == null ) {
				String redirectURL = systemConfig.getClassURL() + "/DPRPackDealCfg"
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
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		Object distributionChannelCode = conditionMap.get( "distributionChannelCode" );
		if( distributionChannelCode == null )
			conditionMap.put( "distributionChannelCode", new String[] { getDistributionChannelCode(ctx) } );
		else if( distributionChannelCode instanceof String ){
			conditionMap.put( "distributionChannelCode", new String[] { (String)distributionChannelCode } );
		}

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}


	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		PackDealCfg db = (PackDealCfg)ctx.db;

		String submode = this.submoder.endSubmodeAndDispose(ctx.pageConfig);

		Map<String, Object> defaultMap = new HashMap<String, Object>();
		defaultMap.put("status", "00");
		defaultMap.put("divisionCode", getDivisionCode(ctx));
		defaultMap.put("organizationCode", getSavedOrganizationCode(ctx));
		defaultMap.put("updateUserId", ctx.sessionMng.getUniqId());
		defaultMap.put("modifyUserId", ctx.sessionMng.getUniqId());

		// 레코드 읽기
		Map<String, Object> recordMap = new ParameterMap(ctx.req);

		if( !recordMap.containsKey("status") )
			recordMap.put("status", defaultMap.get("status"));

		ctx.req.setAttribute("record", recordMap);
		ctx.req.setAttribute("fieldSet", db.getFieldSet(inserting));

		Map<String, Object> _recordMap = new HashMap<String, Object>();
		for( String fieldKey : recordMap.keySet() ) {
			_recordMap.put(fieldKey, recordMap.get(fieldKey));
		}
		_recordMap.put("updateDateTime", Calendar.getInstance().getTime());

		LineProcessor lineProc = db.createLineProcessor(defaultMap);
		Validator validator = db.createValidator(defaultMap);
		try {
			if( lineProc != null )
				_recordMap = lineProc.processLine(ctx.handler, _recordMap);

			if( validator != null )
				validator.validateLine(ctx.handler, _recordMap);
		} catch( DataException dataEx ) {
			String message = ctx.handler.getMessageHandler().getMessage(dataEx.getErrorKey());
			ctx.pageConfig.setMessage(message);
			throw new ServletModelException(ServletModelException.ERROR, dataEx.getMessage());
		} finally {
			if( lineProc != null )
				lineProc.close();
			if( validator != null )
				validator.close();
		}

		boolean actionSuccess = false;
		try {
			if( inserting ) {
				db.regist(_recordMap);
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REGIST_SUCCESS"));
			} else {
				// upsert
				if( !db.modify(_recordMap) ) {
					if( MODE_PUT.equals(submode) ) {
						db.regist(_recordMap);
						ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REGIST_SUCCESS"));
					} else {
						throw ctx.handler.createDataException(DataException.ERR_NO_RECORD_UPDATE);
					}
				} else {
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS"));
				}
			}

			ctx.req.setAttribute("record", db.getRecord(_recordMap, _recordMap.keySet().toArray(new String[0])));
			ctx.pageConfig.setManageAuth(true);
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);

			actionSuccess = true;
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage(dataEx.getMessage()));
			logger.info("error.", dataEx);
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage(sqlEx.getMessage()));
			logger.error("internal error.", sqlEx);
		}
		if( actionSuccess ) {
			ctx.req.setAttribute("record", db.getRecord(_recordMap));
			ctx.pageConfig.setManageAuth(true);
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);
			return forward(ctx, systemConfig.getJspPath() + "/dpr_packdealcfg_input.jsp");
		} else {
			ctx.req.setAttribute("record", recordMap);
			ctx.req.setAttribute("fieldSet", db.getFieldSet(inserting));

			String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
			ctx.pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_PACKDEALCFG_" + mode.toUpperCase()));
			return registInput(ctx);
		}
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog(ctx.handler);
		PackDealCfg db = (PackDealCfg)ctx.db;

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRPackDeal.CFG%LIST", ColumnConfigureFile.OPTIONKEY_DELETE_HTML);

		String[] fieldKeyArray = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put("updateUserId", ctx.sessionMng.getUniqId());
			defaultMap.put("countryCode", getUserCountryCode(ctx));
			defaultMap.put("divisionCode", getDivisionCode(ctx));
			defaultMap.put("organizationCode", getSavedOrganizationCode(ctx));
			defaultMap.put("status", "00");

			int statementType = Record.INSERT | Record.UPDATE;
			String uploadType = "PDCFG";

			List<String> updateFieldKeyList = new ArrayList<String>();
			String updateFieldKey = columnList.getProperty("updateFieldKeys");
			if( updateFieldKey != null ) {
				String[] updateFieldKeys = updateFieldKey.split(",\\s?+");
				updateFieldKeyList.addAll(java.util.Arrays.asList(updateFieldKeys));
			} else {
				updateFieldKeyList.addAll(java.util.Arrays.asList(new String[] { "dealStopInd", "dealStartDate", "dealEndDate" }));
			}

			loader = db.createDataLoader(fieldKeyArray, defaultMap, updateFieldKeyList.toArray(new String[0]), statementType);

			validator = createValidator(ctx);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put("systemCode", systemConfig.getSystemCode());

			processRbmUploadLogUploadType(ctx, logDB, uploadType);
			resultMap.put("uploadType", uploadType);
			resultMap.put("userId", ctx.sessionMng.getUniqId());
			resultMap.put("headerInd", ( ctx.req.getParameter("headerInd") == null ? "Y" : "N" ));

			RecordFormat messageFormat = PatternRecordFormat.getInstance("%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}");
			String pkFormat = "";
			for( String pk : columnList.getPrimaryFieldKeys() ) {
				pkFormat += "[${" + pk + "}]";
				pkFormat += " - ";
			}
			pkFormat = pkFormat.replaceAll("\\ -\\ $", "");
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance(pkFormat);

			loaderLogger = logDB.createLogger(resultMap, messageFormat, lineNameFormat);
			DataLoader dataLoader = createDataLoader(ctx, ctx.handler, loader, loaderLogger, validator, true);

			try {
				DataReader reader = dataLoader.getDataReader();
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList) && !reader.isEOF(); i++ ) {
					try {
						String[] maybeTitle = reader.readNext();
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

			String uploadInputPath = getServletName() + "?" + PARAM_MODE + "=" + MODE_UPLOADINPUT;
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

		ctx.req.setAttribute("record", recordMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp");
	}
}
