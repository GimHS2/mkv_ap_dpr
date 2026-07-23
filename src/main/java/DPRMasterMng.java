/*
 *	File Name:	DPRMasterMng.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_mastermng_list.jsp
 *		dpr_mastermng_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/02/28		2.2.3	initContext() : mngtype 타입별로 messageKey 설정
 *	dudwls3720	2024/02/29		2.2.2	download() : 컬럼리스트 이름 변경, tryWorkbookAutoSizeColumn() 삭제
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	jbaek		2020/03/30		2.2.0	info(): '_masterCodePart2' javascript에서 null/undefined값 체크 수정.
 *	jbaek		2019/07/30		2.2.0	create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataLoader.Validator;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.cols.ColumnList;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.Country;
import com.irt.dpr.Item;
import com.irt.dpr.LineProcessor;
import com.irt.dpr.MasterMng;
import com.irt.dpr.MasterMng.MngType;
import com.irt.dpr.ProductHierarchy;
import com.irt.dpr.TableProcDataLoader;
import com.irt.html.ColumnConfigureFile;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.json.SubmodeManager;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.TableDaoException;
import com.irt.rbm.TableDaoManager;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.HierarchyCodeField;
import com.irt.sql.SQLHandler;
import com.irt.util.DaoManager;
import com.irt.util.FileType;

@WebServlet( urlPatterns = { "/servlet/DPRMasterMng" } )
public class DPRMasterMng extends DPRServletModel {

	public final static String MODE_PUT = "put";

	private static final String MASTERTYPE_PARTY = DPRMaster.MASTERTYPE_PARTY;
	private static final String MASTERTYPE_ITEM = DPRMaster.MASTERTYPE_ITEM;

	private final static String HIERARCHY_PARAMETERKEY = "productHierarchyCode_";

	private final String MODE_COND_SETTING = "rtp";

	private SubmodeManager submoder = new SubmodeManager();

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return super.checkAuthorize(ctx, true);
	}

	Validator createAuthorizationValidator( final List<String> availCodes, final String targetFieldKey ) {
		return new Validator() {

			@Override
			public void close() {
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				if( availCodes != null && recordMap != null ) {
					String newRecordCode = (String)recordMap.get(targetFieldKey);

					if( !availCodes.contains(newRecordCode) ) {
						String message = handler.getMessageHandler().getMessage("ERR_UNAUTHORIZED_OPERATION_1", newRecordCode);
						throw new DataException("ERR_UNAUTHORIZED_OPERATION", message);
					}
				}
			}
		};
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws SQLException, ServletException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		ParameterMap parameterMap = new ParameterMap(ctx.req, true);
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		switch( mngtype ) {
		case MasterDesc:
			if( parameterMap.containsKey("masterType") )
				Condition.putConditionValueOnly(conditionMap, "masterType", parameterMap.get("masterType"));

			if( MODE_DOWNLOAD.equals(ctx.mode) ) {
				// some masterCode has ";" in database column value
				Object[] masterCode = Record.extractObjectArray(parameterMap, "masterCode");
				if( parameterMap.containsKey("_masterCodePart2") ) {
					Object[] p2 = Record.extractObjectArray(parameterMap, "_masterCodePart2");
					if( p2 != null && p2.length > 0 ) {
						if( masterCode.length == p2.length ) {
							for( int i = 0; i < masterCode.length; i++ ) {
								masterCode[i] = masterCode[i] + ";" + p2[i];
							}
						}
					}
				}
				Condition.putConditionValueOnly(conditionMap, "masterCode", masterCode);
			} else {
				if( parameterMap.containsKey("masterCode") ) {
					// Condition.putConditionValueOnly(conditionMap, "masterCode", parameterMap.get("masterCode"));
					String masterCode = parameterMap.getParameter("masterCode");
					Condition.putConditionValueOnly(conditionMap, "masterCode", getConditionValue(masterCode), Condition.CONDTYPE_CONTAINS);
				}
			}
			if( parameterMap.containsKey("masterCodePart1") )
				Condition.putConditionValueOnly(conditionMap, "masterCodePart1", parameterMap.get("masterCodePart1"));
			break;
		case ItemMaster:
			if( parameterMap.containsKey("itemType") )
				Condition.putConditionValueOnly(conditionMap, "itemType", parameterMap.get("itemType"));

			if( parameterMap.containsKey("baseUnitofMeasure") )
				Condition.putConditionValueOnly(conditionMap, "baseUnitofMeasure", parameterMap.get("baseUnitofMeasure"));
			if( parameterMap.containsKey("salesUnit") )
				Condition.putConditionValueOnly(conditionMap, "salesUnit", parameterMap.get("salesUnit"));
			break;
		case ItemMasterDesc:
			break;
		case ItemMasterSales:

			String productHierarchyCode = null;
			for( int i = 0; i < 6; i++ ) {
				String code = (String)parameterMap.get(HIERARCHY_PARAMETERKEY + ( i + 1 ));
				if( code != null )
					Condition.putConditionValueOnly(conditionMap, HIERARCHY_PARAMETERKEY + ( i + 1 ), productHierarchyCode = code);
				else
					break;
			}
			if( productHierarchyCode != null )
				Condition.putConditionValueOnly(conditionMap, "productCategoryCode", productHierarchyCode, Condition.CONDTYPE_STARTSWITH);
			if( !MODE_DOWNLOAD.equals(ctx.mode) )
				setProductHierarchyDivisionLevel(ctx, conditionMap);
			break;
		case ItemMasterUom:
			if( parameterMap.containsKey("uomCode") )
				Condition.putConditionValueOnly(conditionMap, "uomCode", parameterMap.get("uomCode"));
			break;
		case PartyFunction:
			break;
		case PartySales:
			break;
		}

		if( parameterMap.containsKey("languageCode") )
			Condition.putConditionValueOnly(conditionMap, "languageCode", parameterMap.get("languageCode"));
		else
			Condition.putConditionValueOnly(conditionMap, "languageCode", getDisplayLanguage(ctx));

		if( parameterMap.containsKey("mngtype") ) {
			Condition.putConditionValueOnly(conditionMap, "mngtype", parameterMap.get("mngtype"));
		}

		setAttributePartner(ctx, conditionMap, PARTNER_SHIP | PARTNER_SOLD);
		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly(conditionMap, "partyCode", parameterMap.get("partyCode"));

		if( parameterMap.containsKey("distributionChannelCode") )
			Condition.putConditionValueOnly(conditionMap, "distributionChannelCode", parameterMap.get("distributionChannelCode"));
		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly(conditionMap, "officeCode", parameterMap.get("officeCode"));
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly(conditionMap, "groupCode", parameterMap.get("groupCode"));

		conditionMap.put("countryCode", getUserCountryCode(ctx));
		conditionMap.put("divisionCode", getDivisionCode(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		// setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP | PARTYMASTER_REGION);

		if( parameterMap.containsKey("itemConsumerEANCode") )
			Condition.putConditionValueOnly(conditionMap, "itemConsumerEANCode", parameterMap.get("itemConsumerEANCode"));

		if( !MODE_DOWNLOAD.equals(ctx.mode) ) {
			String itemCode = getConditionValue((String)parameterMap.get("itemCode"));
			if( itemCode != null )
				Condition.putConditionValueOnly(conditionMap, "itemCode", itemCode, getConditionType(itemCode));
			String itemName = getConditionValue((String)parameterMap.get("conditionItemName"));
			if( itemCode != null && itemName == null ) {
				conditionMap.put("conditionItemName", new Item(ctx.handler).getName(itemCode, getDisplayLanguage(ctx)));
			}
			if( itemName != null ) {
				Condition.putConditionValueOnly(conditionMap, "itemName", getConditionValue(itemName), Condition.CONDTYPE_CONTAINS);
				conditionMap.put("conditionItemName", itemName);
			}
		} else {
			if( parameterMap.containsKey("itemCode") )
				Condition.putConditionValueOnly(conditionMap, "itemCode", parameterMap.get("itemCode"));
		}

		String[] allable = new String[] { "officeCode", "groupCode", "partyCode" };
		for( String key : allable ) {
			String all = (String)parameterMap.get(key + "_all");
			if( all != null && "0".equals(all) ) {
				conditionMap.put(key, "0");
			}
		}

		if( parameterMap.containsKey("displayLanguage") ) {
			conditionMap.put("displayLanguage", parameterMap.get("displayLanguage"));
		} else {
			conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
		}

		if( parameterMap.containsKey("pcateCode") ) {
			// Condition.putConditionValueOnly(conditionMap, "pcateCode", parameterMap.get("pcateCode"));

			String pcateCode = parameterMap.getParameter("pcateCode");
			Condition.putConditionValueOnly(conditionMap, "pcateCode", getConditionValue(pcateCode), Condition.CONDTYPE_CONTAINS);
		}
		if( parameterMap.containsKey("pcateName") ) {
			String pcateName = parameterMap.getParameter("pcateName");
			Condition.putConditionValueOnly(conditionMap, "pcateName", getConditionValue(pcateName), Condition.CONDTYPE_CONTAINS);
		}

		if( parameterMap.containsKey("masterName") ) {
			String masterName = parameterMap.getParameter("masterName");
			Condition.putConditionValueOnly(conditionMap, "masterName", getConditionValue(masterName), Condition.CONDTYPE_CONTAINS);
		}

		if( parameterMap.containsKey("partyName") ) {
			Condition.putConditionValueOnly(conditionMap, "partyName", getConditionValue(parameterMap.getParameter("partyName")),
					Condition.CONDTYPE_CONTAINS);
		}

		if( parameterMap.containsKey("linkPartyCode") )
			Condition.putConditionValueOnly(conditionMap, "linkPartyCode", parameterMap.get("linkPartyCode"));
		if( parameterMap.containsKey("linkPartyName") ) {
			Condition.putConditionValueOnly(conditionMap, "linkPartyName", getConditionValue(parameterMap.getParameter("linkPartyName")),
					Condition.CONDTYPE_CONTAINS);
		}

		return conditionMap;
	}

	private LineProcessor createLineProcessor( Context ctx, final Map<String, Object> lineDefaultMap ) {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		MasterMng mng = (MasterMng)ctx.extraObj;

		return mng.createLineProcessor(mngtype, lineDefaultMap);
	}

	private Validator createPartyLanguageCodeValidator( Context ctx ) {
		String locale = RBMSystem.getSystemEnv("SYS", "PartySupportLocale;" + ctx.sessionMng.getPartyId());
		if( locale != null ) {
			final String[] locales = locale.split(",");
			if( locales != null ) {
				List<String> availbleToManage = new ArrayList<String>(java.util.Arrays.asList(locales));

				if( availbleToManage.contains("en") ) {
					availbleToManage.remove("en");
					return createAuthorizationValidator(availbleToManage, "languageCode");
				}
			}
		}
		return null;
	}

	private com.irt.data.DataLoader.Validator createValidator( Context ctx ) {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));

		MasterMng mng = (MasterMng)ctx.extraObj;

		return mng.createValidator(mngtype);
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition(ctx);

		return super.doRequest(ctx, isPost);
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		Map<String, Object> conditionMap = createConditionMap(ctx);

		List<String> optionKeyList = new ArrayList<String>();
		optionKeyList.add(ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDistAllowUOM") )
			optionKeyList.add("useDistAllowUOM");

		com.irt.data.cols.ColumnList columnList = null;
		try {
			columnList = getColumnList(ctx, "DPRMasterMngHSSF." + mngtype.name() + "%DOWN", optionKeyList.toArray(new String[0]));
		} catch( ServletModelException maybeEx ) {
			if( ServletModelException.CANNOT_FIND_COLUMNLIST.equals(maybeEx.getErrorKey()) ) {
				if( columnList == null )
					columnList = getColumnList(ctx, "DPRMasterMng." + mngtype.name() + "%LIST", optionKeyList.toArray(new String[0]));
			} else {
				throw maybeEx;
			}
		}

		String filename = ctx.msghandler.getMessage("TITLE_DPR_MASTERMNG_") + "-" + mngtype.name();
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
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();
		String[] altKeys = db.getDao().getAlterableFieldKeys();

		if( MODE_DOWNLOAD.equals(ctx.mode) || MODE_UPLOAD.equals(ctx.mode) ) {
			return getDownUpColumnList(ctx, columnListName, altKeys, optionKeys);
		}

		return super.getColumnList(ctx, columnListName, optionKeys);
	}

	private String getConditionType( String value ) {
		if( value.indexOf("%") >= 0 )
			return Condition.CONDTYPE_LIKE;
		else if( value.indexOf("_") >= 0 )
			return Condition.CONDTYPE_LIKE;

		return Condition.CONDTYPE_EQUALS;
	}

	/**
	 * Wildcard Searching
	 * '*' : some characters matches. replace to "%" oracle wildcard.
	 * '?' : one charcter matches. replace to "_" oracle wildcard.
	 **/
	private String getConditionValue( String value ) {
		if( value == null )
			return null;
		List<String[]> list = new java.util.ArrayList();
		if( value.indexOf("*") >= 0 )
			list.add(new String[] { "*", "%" });
		if( value.indexOf("?") >= 0 )
			list.add(new String[] { "?", "_" });

		Object[] regex = new Object[list.size()];
		list.toArray(regex);

		if( regex.length > 0 ) {
			int cnt = 0;
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

	@Override
	protected String getFileType( Context ctx ) throws ServletModelException, IOException {
		String fileType = super.getFileType(ctx);

		if( !( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
				&& ( FileType.XLS.equals(fileType) || FileType.XLX.equals(fileType) ) ) {
			return FileType.XLF;
		}

		return fileType;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	@Override
	public boolean info( Context ctx, boolean inserting ) throws SQLException, IOException, ServletException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		ParameterMap paramMap = new ParameterMap(ctx.req);
		ctx.req.setAttribute("altKeys", db.getDao().getAlterableFieldKeys());

		switch( mngtype ) {
		case MasterDesc:
			// some masterCode has ";" in database column value
			String masterCodePart2 = paramMap.getParameter("_masterCodePart2");
			if( masterCodePart2 != null && masterCodePart2.length() > 0
					&& !masterCodePart2.equals("undefined") && !masterCodePart2.equals("null") ) {
				String masterCode = paramMap.getParameter("masterCode") + ";" + masterCodePart2;
				paramMap.put("masterCode", masterCode);
			}
			break;
		}

		// ready
		Map<String, Object> primaryMap;
		try {
			primaryMap = db.extractPrimary(paramMap, 0);
		} catch( TableDaoException taoEx ) {
			Map<String, Object> sourceMap = taoEx.getRecordMap();
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER, taoEx.getMessage());
		}

		primaryMap.put("displayLanguage", getDisplayLanguage(ctx));

		// process
		Map<String, Object> recordMap = db.getRecord(primaryMap);
		if( recordMap == null )
			throw new ServletModelException(ServletModelException.NO_RECORD_FOUND);
		ctx.req.setAttribute("record", recordMap);

		// forward
		if( inserting ) {
			ctx.req.setAttribute("fieldSet", db.getFieldSet(true));
			return registInput(ctx);
		} else {
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);
			ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRMasterMng.MNG"));
			ctx.req.setAttribute("fieldSet", db.getFieldSet(false));

			return forward(ctx, systemConfig.getJspPath() + "/dpr_mastermng_input.jsp");
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		MngType mngtype = MngType.fromValue(ctx.req.getParameter("mngtype"));

		if( mngtype == null ) {
			if( MODE_DEFAULT.equals(ctx.mode) || MODE_LIST.equals(ctx.mode) || MODE_LISTCOUNT.equals(ctx.mode) ) {
				mngtype = MngType.MasterDesc;
			} else {
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
			}
		}

		String masterType = null;
		switch( mngtype ) {
		case ItemMaster:
			masterType = MASTERTYPE_ITEM;
			break;
		case ItemMasterDesc:
			masterType = MASTERTYPE_ITEM;
			break;
		case ItemMasterSales:
			masterType = MASTERTYPE_ITEM;
			break;
		case ItemMasterUom:
			masterType = MASTERTYPE_ITEM;
			break;
		case PartyFunction:
			masterType = MASTERTYPE_PARTY;
			break;
		case PartySales:
			masterType = MASTERTYPE_PARTY;
			break;
		case MasterDesc:
			break;
		}
		PageConfig pageConfig = super.createPageConfig(ctx);

		ctx.pageConfig.setProperty("mastertype", masterType);
		ctx.pageConfig.setProperty("mngtype", mngtype.getValue());
		ctx.pageConfig.setProperty("mngtypeName", mngtype.name());
		ctx.req.setAttribute("mngtypes", MngType.namevalues(ctx.msghandler, "DPR_MASTERMNG_MNGTYPE_"));

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRMasterMng.INF");
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_SELECT.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItem.LST");
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRMasterMng.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) ) {
			pageConfig.setSystemPackageCode("DPR", "DPRMasterMng.MNG");
		} else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRMasterMng.MNG");
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRMasterMng.MNG");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRMasterMng.LST");
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) ) {
			// if( false ) {
			// Map<String, Object> paramMap = new ParameterMap(ctx.req);
			// String[] availCodes = new String[] {
			// MngType.ItemMasterDesc.value, MngType.MasterDesc.value, MngType.PartySales.value
			// };
			// Validator v = createAuthorizationValidator(java.util.Arrays.asList(availCodes), "mngtype");
			// try {
			// try {
			// v.validateLine(ctx.handler, paramMap);
			// } catch( SQLException sqlEx ) {
			// logger.error(sqlEx);
			// throw ctx.handler.createDataException(sqlEx.getMessage());
			// } finally {
			// v.close();
			// }
			// } catch( DataException dataEx ) {
			// throw new ServletModelException(ServletModelException.HAS_NOAUTH, dataEx.getMessage());
			// }
			// }
			pageConfig.setSystemPackageCode("DPR", "DPRMasterMng.MNG");
		} else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = DaoManager.getInstance(com.irt.dpr.Schema.class.getCanonicalName(), mngtype.name(), ctx.handler);
		ctx.extraObj = new MasterMng();

		pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_MASTERMNG_" + ctx.mode.toUpperCase()));

		String messageKey;
		if ( mngtype == MngType.PartySales || mngtype == MngType.PartyFunction ) {
			messageKey = "TITLE_DPR_CUSTOMER_MASTERMNG_";
		} else if ( mngtype == MngType.ItemMaster || mngtype == MngType.ItemMasterDesc || mngtype == MngType.ItemMasterUom || mngtype == MngType.ItemMasterSales ) {
			messageKey =  "TITLE_DPR_ITEM_MASTERMNG_";
		} else {
			messageKey =  "TITLE_DPR_MASTERMNG_";
		}
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_UPLOAD", messageKey );
	}

	@Override
	protected boolean list( Context ctx, boolean listing ) throws IOException, ServletException, SQLException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		Map<String, Object> conditionMap = createConditionMap(ctx);

		setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
		switch( mngtype ) {
		case ItemMasterUom:
			Map<String, Object> cond = new HashMap<String, Object>(conditionMap);
			cond.remove("uomCode");
			cond.remove("itemCode");
			cond.put(Condition.DISTINCT_CONDITIONKEY, "Y");
			String[] fieldKeys = new String[] { "uomCode", "uomName" };
			db.setSort(fieldKeys);
			List uoms = db.getRecords(cond, fieldKeys);
			ctx.req.setAttribute("uoms", uoms);
			break;
		case ItemMasterSales:
			setAttributePartyMaster(ctx, conditionMap,
					PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);

			if( !conditionMap.containsKey("distributionChannelCode") ) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
				conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );
			}
			break;
		case PartySales:
			setAttributePartyMaster(ctx, conditionMap,
					PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);
			break;
		case PartyFunction:
			setAttributePartyMaster(ctx, conditionMap,
					PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);
			break;
		}

		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDistAllowUOM") )
			optionKeyList.add("useDistAllowUOM");
		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRMasterMng." + mngtype.name() + "%LIST",
				optionKeyList.toArray(new String[0]));

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
		List recordList = db.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);

		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("condition", conditionMap);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRMasterMng.MNG"));

		boolean selecting = false;
		switch( mngtype ) {
		case ItemMasterSales:
			selecting = ( !listing ? true : false );
			break;
		}
		if( selecting )
			return forward(ctx, systemConfig.getJspPath() + "/dpr_itemmastersales_select.jsp");
		else
			return forward(ctx, systemConfig.getJspPath() + "/dpr_mastermng_list.jsp");
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", null) )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = db.getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
	}

	@Override
	public boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		switch( mngtype ) {
		case ProductCategory:
			HierarchyCodeField codeField = ProductHierarchy.getHierarchyField();
			ctx.req.setAttribute("codeField", codeField);
			break;
		}
		setAttributePartyMaster(ctx, createConditionMap(ctx), //
				PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_REGION);

		// process
		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.TreeMap<String, Object>();
			switch( mngtype ) {
			case ProductCategory:
				String code = ctx.req.getParameter("code");
				HierarchyCodeField codeField = ( ctx.req.getAttribute("codeField") == null
						? ProductHierarchy.getHierarchyField()
						: (HierarchyCodeField)ctx.req.getAttribute("codeField") );

				recordMap.put("code", code);
				recordMap.put("classCode", String.valueOf(codeField.getLevel(code) + 1));

				ctx.req.setAttribute("record", recordMap);
				ctx.req.setAttribute("fieldSet", db.getFieldSet(true));

				break;
			default:
				ctx.req.setAttribute("record", recordMap);
				ctx.req.setAttribute("fieldSet", db.getFieldSet(true));
				break;
			}
		}

		// forward
		return forward(ctx, systemConfig.getJspPath() + "/dpr_mastermng_input.jsp");
	}

	@Override
	protected boolean remove( Context ctx ) throws SQLException, IOException, ServletException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		// if( ctx.pageConfig.getBackURL() == null )
		// throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		ParameterMap paramMap = new ParameterMap(ctx.req);
		Map<String, Object> recordMap = new HashMap<String, Object>(paramMap);
		Map<String, Object[]> primaryKeyValues = null;
		try {
			primaryKeyValues = db.extractPrimaryKeyValues(paramMap);
		} catch( TableDaoException taoEx ) {
			Map sourceMap = taoEx.getRecordMap();
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER, taoEx.getMessage());
		}

		Map<String, Object> defaultMap = new HashMap<String, Object>();
		defaultMap.put("status", "00");
		defaultMap.put("updateDateTime", Calendar.getInstance().getTime());
		defaultMap.put("modifyUserId", ctx.sessionMng.getUniqId());
		defaultMap.put("updateUserId", ctx.sessionMng.getUniqId());
		defaultMap.put("divisionCode", getDivisionCode(ctx));
		defaultMap.put("organizationCode", getSavedOrganizationCode(ctx));
		defaultMap.put("countryCode", getUserCountryCode(ctx));

		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		switch( mngtype ) {
		case MasterDesc:
			break;
		case PartySales:
			break;
		case PartyFunction:
			break;
		}

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
				errorList.add(createErrorMap(primaryMap, dataEx));
			}
		}

		// set error or success
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute("errors", errorList);
		} else {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)));

			if( ctx.pageConfig.getBackURL() == null ) {
				String redirectURL = systemConfig.getClassURL() + "/DPRMasterMng"
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

	protected void setProductHierarchyDivisionLevel( Context ctx, Map<String, Object> parameterMap ) throws ServletModelException, SQLException {
		com.irt.dpr.ProductHierarchy db = new com.irt.dpr.ProductHierarchy(ctx.handler);

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>(parameterMap);
		conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");

		List<Map<String, Object>> recordList = db.getExistProductCategoris(conditionMap);

		ctx.req.setAttribute("categories", recordList);
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		boolean actionSuccess = false;
		String submode = this.submoder.endSubmodeAndDispose(ctx.pageConfig);

		Map<String, Object> defaultMap = new HashMap<String, Object>();
		defaultMap.put("status", "00");
		defaultMap.put("updateDateTime", Calendar.getInstance().getTime());
		defaultMap.put("modifyUserId", ctx.sessionMng.getUniqId());
		defaultMap.put("updateUserId", ctx.sessionMng.getUniqId());
		defaultMap.put("divisionCode", getDivisionCode(ctx));
		defaultMap.put("organizationCode", getSavedOrganizationCode(ctx));
		defaultMap.put("countryCode", getUserCountryCode(ctx));

		// 레코드 읽기
		ParameterMap paramMap = new ParameterMap(ctx.req);
		Map<String, Object> recordMap = new HashMap<String, Object>(paramMap);

		switch( mngtype ) {
		case MasterDesc:
			// some master code has ";"
			String masterCodePart2 = paramMap.getParameter("masterCodePart2");
			if( masterCodePart2 != null && masterCodePart2.length() > 0 ) {
				String masterCode = paramMap.getParameter("masterCode") + ";" + masterCodePart2;
				recordMap.put("masterCode", masterCode);
			}
			break;
		case PartySales:
			defaultMap.put("allowUOM", getDefaultUnitOfMeasure(ctx));
			Country country = new Country(ctx.handler);
			defaultMap.put("countryKey", country.getFieldValue(country.createPrimary(getUserCountryCode(ctx)), "countryKey"));
			break;
		case PartyFunction:
			Map<String, Object> condMap = new HashMap<String, Object>();
			condMap.put("partyCode", recordMap.get("partyCode"));
			condMap.put("linkType", recordMap.get("linkType"));
			condMap.put("countryCode", recordMap.get("countryCode"));
			condMap.put("divisionCode", recordMap.get("divisionCode"));
			condMap.put("distributionChannelCode", recordMap.get("distributionChannelCode"));
			condMap.put("organizationCode", recordMap.get("organizationCode"));

			String[] groupKeys = new String[] { "countryCode", "divisionCode", "organizationCode", "distributionChannelCode", "partyCode",
					"linkType" };
			String[] fieldKeys = new String[] { "nextDisplaySequence" };
			condMap.put(Condition.GROUPING_CONDITIONKEY, groupKeys);

			Map<String, Object> ptyLinkMap;
			List<Map<String, Object>> list = db.getRecords(condMap, fieldKeys);
			if( list != null && list.size() > 0 ) {
				defaultMap.put("displaySequence", list.get(0).get("nextDisplaySequence"));
			}
			break;
		}

		for( String key : defaultMap.keySet() ) {
			if( !paramMap.containsKey(key) )
				recordMap.put(key, defaultMap.get(key));
		}

		LineProcessor lineProc = createLineProcessor(ctx, defaultMap);
		Validator validator = createValidator(ctx);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			if( lineProc != null )
				lineProc.processLine(ctx.handler, recordMap);

			if( validator != null )
				validator.validateLine(ctx.handler, recordMap);
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			String message = ctx.handler.getMessageHandler().getMessage(dataEx.getMessage());
			ctx.pageConfig.setMessage(message);
			throw new ServletModelException(ServletModelException.INVALID_REQUEST, message);
		} finally {
			if( lineProc != null )
				lineProc.close();
			if( validator != null )
				validator.close();
		}

		ctx.req.setAttribute("record", paramMap);
		ctx.req.setAttribute("fieldSet", db.getFieldSet(inserting));

		if( !actionSuccess ) {
			try {
				if( inserting ) {
					db.regist(recordMap);
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REGIST_SUCCESS"));
				} else {
					// upsert
					if( !db.modify(recordMap) ) {
						if( MODE_PUT.equals(submode) ) {
							db.regist(recordMap);
							ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REGIST_SUCCESS"));
						} else {
							throw ctx.handler.createDataException(DataException.ERR_NO_RECORD_UPDATE);
						}
					} else {
						ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS"));
					}
				}

				// ctx.req.setAttribute("record", db.getRecord(recordMap, recordMap.keySet().toArray(new String[0])));
				// ctx.pageConfig.setManageAuth(true);
				// ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);

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
		}

		if( actionSuccess ) {
			switch( mngtype ) {
			case ProductCategory:
				HierarchyCodeField codeField = ProductHierarchy.getHierarchyField();
				ctx.req.setAttribute("codeField", codeField);

				ctx.req.setAttribute("record", recordMap);
				break;
			default:
				Map<String, Object> record_db = db.getRecord(recordMap);
				ctx.req.setAttribute("record", record_db);
				break;
			}

			ctx.pageConfig.setManageAuth(true);
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);
			return forward(ctx, systemConfig.getJspPath() + "/dpr_mastermng_input.jsp");
		} else {
			ctx.req.setAttribute("record", recordMap);
			ctx.req.setAttribute("fieldSet", db.getFieldSet(inserting));

			String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
			ctx.pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_MASTERMNG_" + mode.toUpperCase()));
			return registInput(ctx);
		}
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		MngType mngtype = MngType.fromValue(ctx.pageConfig.getProperty("mngtype"));
		UploadLog logDB = new UploadLog(ctx.handler);
		DaoManager dm = (DaoManager)ctx.db;
		TableDaoManager db = dm.getManager();

		Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
		int statementType = Record.UPDATE | Record.INSERT;

		List<String> optionKeyList = new ArrayList<String>();
		optionKeyList.add(ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDistAllowUOM") ) {
			optionKeyList.add("useDistAllowUOM");
		} else {
			switch( mngtype ) {
			case PartySales:
				defaultMap.put("allowUOM", getDefaultUnitOfMeasure(ctx));
				break;
			case PartyFunction:
				statementType = Record.INSERT;
				break;
			case ProductCategory:
				statementType = Record.INSERT;
			}
		}

		com.irt.data.cols.ColumnList columnList = null;
		try {
			columnList = getColumnList(ctx, "DPRMasterMng." + mngtype.name() + "%DOWN", optionKeyList.toArray(new String[0]));
		} catch( ServletModelException maybeEx ) {
			if( ServletModelException.CANNOT_FIND_COLUMNLIST.equals(maybeEx.getErrorKey()) ) {
				if( columnList == null )
					columnList = getColumnList(ctx, "DPRMasterMng." + mngtype.name() + "%LIST", optionKeyList.toArray(new String[0]));
			} else {
				throw maybeEx;
			}
		}

		String[] fieldKeyArray = columnList.getFieldKeyArray();
		List<String> updateFieldKeyList = new ArrayList<String>();
		String updateFieldKey = columnList.getProperty("updateFieldKeys");
		if( updateFieldKey != null && updateFieldKey.length() > 0 ) {
			String[] updateFieldKeys = updateFieldKey.split(",\\s?+");
			updateFieldKeyList.addAll(java.util.Arrays.asList(updateFieldKeys));
		} else {
			String[] altKeys = db.getDao().getAlterableFieldKeys();

			updateFieldKeyList.addAll(java.util.Arrays.asList(altKeys));
		}

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = createValidator(ctx);
		try {
			defaultMap.put("updateUserId", ctx.sessionMng.getUniqId());
			defaultMap.put("modifyUserId", ctx.sessionMng.getUniqId());
			defaultMap.put("countryCode", getUserCountryCode(ctx));
			defaultMap.put("divisionCode", getDivisionCode(ctx));
			defaultMap.put("organizationCode", getSavedOrganizationCode(ctx));
			defaultMap.put("status", "00");

			// loader = db.createDataLoader(fieldKeyArray, defaultMap, updateFieldKeyList.toArray(new String[0]), statementType);
			loader = new TableProcDataLoader(fieldKeyArray, defaultMap, ctx.handler, db.getDao().getTable(),
					updateFieldKeyList.toArray(new String[0]), statementType);
			( (TableProcDataLoader)loader ).setLineProcessor(createLineProcessor(ctx, defaultMap));

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put("systemCode", systemConfig.getSystemCode());

			String uploadType = db.getDao().getTable().getTableAlias();
			processRbmUploadLogUploadType(ctx, logDB, uploadType);
			resultMap.put("uploadType", uploadType);
			resultMap.put("userId", ctx.sessionMng.getUniqId());
			resultMap.put("headerInd", ( ctx.req.getParameter("headerInd") == null ? "Y" : "N" ));

			String pkFormat = "";
			if( columnList.getPrimaryFieldKeys() != null ) {
				for( String pk : columnList.getPrimaryFieldKeys() ) {
					pkFormat += "[${" + pk + "}]";
					pkFormat += " - ";
				}
			}
			pkFormat = pkFormat.replaceAll("\\ -\\ $", "");
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance(pkFormat);
			RecordFormat messageFormat = PatternRecordFormat.getInstance("%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}");

			validator = createValidator(ctx);

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

			// String uploadInputPath = "DPR" + mngtype.name() + "?" + PARAM_MODE + "=" + MODE_UPLOADINPUT;
			String redirectURL = RBMUploadLog.getUploadLogURL(ctx, dataLoader, systemConfig.getClassURL(), null);

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
		Map<String, Object> paramMap = new ParameterMap(ctx.req);

		if( paramMap.containsKey("hiddenKeys") ) {
			Object obj = paramMap.get("hiddenKeys");
			if( obj != null ) {
				if( obj instanceof String[] ) {
					ctx.req.setAttribute("hiddenKeys", obj);
				} else if( obj instanceof String ) {
					if( ( (String)obj ).contains(",") ) {
						ctx.req.setAttribute("hiddenKeys", ( (String)obj ).split(",\\s?+"));
					} else {
						ctx.req.setAttribute("hiddenKeys", new String[] { (String)obj });
					}
				} else {
					throw new ServletModelException(ServletModelException.INVALID_PARAMETER);
				}
			}
		}

		Map<String, Object> recordMap = createConditionMap(ctx);

		String organizationCode = Record.extractString(recordMap, "organizationCode");
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		if( recordMap.get("encoding") == null ) {
			recordMap.put("encoding", "UTF8");
		}

		recordMap.put("supportFileTypesCsv", "XLS");
		recordMap.put("defaultFileType", "XLS");

		ctx.req.setAttribute("record", recordMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp");
	}
}
