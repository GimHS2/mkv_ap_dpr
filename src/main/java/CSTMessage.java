/*
 *	File Name:	CSTMessage.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0	create
 *
**/

import com.irt.custom.SystemEx;
import com.irt.data.DataException;
import com.irt.data.DataResult;
import com.irt.data.ManipulableManager;
import com.irt.data.Record;
import com.irt.data.cols.ColumnResourceBundle;
import com.irt.html.DatabaseColumnResource;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.json.JqdtConfigHelper;
import com.irt.json.Jsoner;
import com.irt.json.SubmodeManager;
import com.irt.rbm.RBMSystem;
import com.irt.resbdl.DatabaseMessageResource;
import com.irt.resbdl.DatabaseResourceRepository;
import com.irt.resbdl.DatabaseResourceRepositoryImpl;
import com.irt.resbdl.MessageResourceFileWriter;
import com.irt.resbdl.PageMessageKeys;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.Table;
import com.irt.util.BundleUtil;
import com.irt.util.DaoManager;
import com.irt.util.FileUtil;
import com.irt.util.IPredicate;
import com.irt.util.MessageHandler;
import com.irt.util.Predicate;
import com.irt.util.StringUtil;
import com.irt.util.Utility2;
import com.irt.util.cst.ReflectUtil;
import com.irt.util.cst.SchemaTableFinder;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * 3 versions.
 *
 * 1. original properties file
 * 2. install to database( only missing 'resourceKey' are installed )
 * 3. backed up properties file( 'messageResourceLocation' )
 *
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/CSTMessage" } )
public class CSTMessage extends AbstractServletModel {//@formatter:on

	/** like rest api 'put' method full record processing( update or insert ) */
	public final static String MODE_PUT = "put";

	public final static String MODE_LISTCOLRES = "listcolres";

	public static final String MODE_LISTMSGRES = "listmsgres";

	public static final String MODE_REQMSGS = "reqmsgs";

	public static final String MODE_PAGEKEYS = "pagekeys";

	public static final String MODE_LISTENVDATA = "listenvdata";
	public static final String MODE_LISTSYSENV = "listsysenv";
	public static final String MODE_INST_MSGRES = "instmsgres";

	public static final String MODE_INST_IMGSRC = "instimgsrc";

	public static final String MODE_BACKUP_MSGRES = "bckmsgres";

	public static final String RESOURCE_VALUE_BLANK = "";

	private static Map<String, Entry<String, String>> LISTDATASRC_MAPPING = new HashMap<String, Entry<String, String>>();

	static {
		LISTDATASRC_MAPPING.put("pkg",
				new AbstractMap.SimpleEntry(com.irt.rbm.sys.Schema.class.getCanonicalName(), com.irt.rbm.sys.Schema.SYS_SYSTEM_PACKAGE));
		LISTDATASRC_MAPPING.put("svg",
				new AbstractMap.SimpleEntry(com.irt.rbm.sys.Schema.class.getCanonicalName(), com.irt.rbm.sys.Schema.SYS_SERVICEGRP));
		LISTDATASRC_MAPPING.put("svgl",
				new AbstractMap.SimpleEntry(com.irt.rbm.sys.Schema.class.getCanonicalName(), com.irt.rbm.sys.Schema.SYS_SERVICEGRP_LINK));
		LISTDATASRC_MAPPING.put("svgpkg",
				new AbstractMap.SimpleEntry(com.irt.rbm.sys.Schema.class.getCanonicalName(), com.irt.rbm.sys.Schema.SYS_SERVICEGRP_PACKAGE));
		LISTDATASRC_MAPPING.put("imt",
				new AbstractMap.SimpleEntry(com.irt.dpr.Schema.class.getCanonicalName(), com.irt.dpr.Schema.DPR_ITEM_MASTER));
	}

	private static boolean DEFAULT_JSON_NULL_AS_EMPTY = true;

	private static String getPageTitlePrefix( String servletName, String initParamPageTitlePrefix ) {
		String pageTitlePrefix = "TITLE_";
		Matcher m = Pattern.compile("(^[A-Z]{3})(.*)$").matcher(servletName);
		if( m.find() ) {
			if( m.group(1) != null ) {
				pageTitlePrefix += m.group(1).toUpperCase();
			}
			if( m.group(2) != null ) {
				pageTitlePrefix += "_" + m.group(2).toUpperCase() + "_";
			}
		}
		return initParamPageTitlePrefix == null ? pageTitlePrefix : initParamPageTitlePrefix;
	}

	private SubmodeManager submoder = new SubmodeManager();

	private InstallStrategy DEFAULT_INST_STRATEGY = InstallStrategy.INST_ONLY_MISSING;

	Pattern SUBKEY_FIND_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

	String[] COLRES_viewFieldKeys = {
			"poolName", "columnKey", "poolLocale", "columnTitle", "columnType", "columnParentKey",
			"columnDataPattern", "columnHelpPattern", "columnDataCellAttr", "columnAttr", "columnSortable",
			"columnLinkKeys", "linkAuthSystemCode", "linkAuthPackageCode", "linkCondClass", "linkCondFieldKey", "linkCondValue",
			"updateDateTime", "partyId", "createDateTime"
	};

	String[] COLRES_SystemAdminOnlyKeys = { "columnSortable", "columnDataPattern", "columnHelpPattern", "columnDataCellAttr", "columnAttr",
			"columnLinkKeys", "linkAuthSystemCode", "linkAuthPackageCode", "linkCondClass", "linkCondFieldKey", "linkCondValue",
	};

	String[] MSGRES_viewFieldKeys = {
			"resourceKey", "resourceLocale", "resourceValue",
			"updateDateTime", "resourceBaseName", "createDateTime"
	};

	private boolean bckmsgres( Context ctx ) throws ServletModelException, IOException {
		String systemCode = ctx.req.getParameter("systemCode");
		if( systemCode == null || systemCode.length() <= 0 )
			systemCode = "RBM";

		String[] targetLocales = ctx.req.getParameterValues("targetLocale");
		if( targetLocales == null || targetLocales.length <= 0 )
			targetLocales = new String[] { "en" };

		String isAllLocale = ctx.req.getParameter("isAllLocale");
		if( "Y".equals(isAllLocale) ) {
			String supportLocale = RBMSystem.getSystemEnv("DPR", "Default;SupportLocale");
			if( supportLocale != null ) {
				targetLocales = supportLocale.split(",");
			}
		}

		String isMailReport = ctx.req.getParameter("isMailReport");

		Map<String, Object> record = Record.createMap("reqmap", ctx.req.getParameterMap());
		if( targetLocales != null ) {
			File msgbckDir = FileUtil.getCreatedDir(systemConfig.getTemporaryDirectory(), "message-backup");

			String[] _sourceNames = null;

			for( String targetLocale : targetLocales ) {
				Locale _targetLocale = new Locale(targetLocale);
				MessageHandler msghandler = systemConfig.getMessageHandler(_targetLocale);
				if( _sourceNames == null ) {
					_sourceNames = BundleUtil.getBundleBaseNamesUnsafely(msghandler);
				}

				MessageResourceFileWriter fw = new MessageResourceFileWriter(msgbckDir, _targetLocale);
				fw.writeMessageBundles(msghandler);
			}

			File outputDir = FileUtil.getCreatedDir(systemConfig.getTemporaryDirectory(), "message-backup-zipfile");
			String zipFilename = "message-backup-" + new com.irt.data.Timestamp().getIsoLocal().replaceAll(":", ".");
			FileUtil.zipDir(outputDir, msgbckDir, zipFilename, MessageResourceFileWriter.REGEX_HIDDEN_PROPERTIES_FILE);
			File zipFile = new File(outputDir, zipFilename + ".zip");
			record.put("isFileCreated", zipFile.exists());
			ctx.req.setAttribute("record", record);

			if( "Y".equals(isMailReport) ) {
				Map<String, Object> sendMailMap = Record.createMap("host",
						RBMSystem.getSystemEnv("DPR", "CSTMessage#bckmsgres;host", "mail.irt.co.kr"));
				// sendMailMap.put("id", RBMSystem.getSystemEnv("DPR", "CSTMessage#bckmsgres;id", "jbaek"));
				// sendMailMap.put("pw", RBMSystem.getSystemEnv("DPR", "CSTMessage#bckmsgres;pw"));
				sendMailMap.put("port", RBMSystem.getSystemEnv("DPR", "CSTMessage#bckmsgres;port"));
				sendMailMap.put("toAddress", RBMSystem.getSystemEnv("DPR", "CSTMessage#bckmsgres;toAddress", "jbaek@irt.co.kr").split(","));
				sendMailMap.put("fromAddress", RBMSystem.getSystemEnv("DPR", "CSTMessage#bckmsgres;fromAddress", "jbaek@irt.co.kr"));
				sendMailMap.put("subject", zipFilename);
				sendMailMap.put("contents", "first sourceNames: " + java.util.Arrays.asList(_sourceNames) + "<br>" + record);

				Message _message = null;
				Date sent;
				try {
					try {
						_message = Utility2.sendMail(sendMailMap, logger, new File(outputDir, zipFilename + ".zip"));
					} catch( DataException dataEx ) {
						logger.error("error.", dataEx);
					}

					sent = _message.getSentDate();
					if( sent != null ) {
						record.put("sentDate", sent);
						ctx.req.setAttribute("record", record);
					}
					return jsonResponse(ctx, "record");
				} catch( MessagingException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
				}
			}
		}

		ctx.res.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return jsonResponse(ctx, "record");
	}

	public String createAjaxUrl( HttpServletRequest req, String mode, String dataSrc ) {
		return createJqdtConfigAjaxUrl(req, mode, dataSrc, null);
	}

	public String createJqdtConfigAjaxUrl( HttpServletRequest req, String mode, String dataSrc, String[] reqIds ) {
		String requestedUrl = req.getRequestURI();

		String[] targetLocales = req.getParameterValues("targetLocale");
		if( targetLocales == null || targetLocales.length <= 0 ) {
			targetLocales = new String[] { new Locale("en").getLanguage() };
		}
		String urlParams = StringUtil.strJoin("&targetLocale", "=", targetLocales);

		urlParams += "&mode=" + mode;
		urlParams += "&dataSrc=" + dataSrc;
		if( reqIds != null ) {
			for( String id : reqIds ) {
				if( id != null && id.length() > 0 ) {
					urlParams += "&reqId=" + id;
				} else {
					urlParams += "&reqId=";
				}
			}
		}

		final String url = requestedUrl + urlParams.replaceFirst("&", "?");

		return url;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_LISTMSGRES.equals(ctx.mode) )
			return listmsgres(ctx);
		else if( MODE_LISTCOLRES.equals(ctx.mode) )
			return listcolres(ctx);
		else if( MODE_LISTSYSENV.equals(ctx.mode) )
			return listsysenv(ctx);
		// else if( MODE_LISTSVG.equals(ctx.mode) )
		// return listsvg(ctx);
		// else if( MODE_LISTSVGL.equals(ctx.mode) )
		// return listsvgl(ctx);
		// else if( MODE_LISTSVGPKG.equals(ctx.mode) )
		// return listsvgpkg(ctx);
		else if( MODE_LISTENVDATA.equals(ctx.mode) )
			return listenvdata(ctx);
		else if( MODE_PAGEKEYS.equals(ctx.mode) )
			return listpagemsgs(ctx);
		else if( MODE_INST_MSGRES.equals(ctx.mode) )
			return instmsgres(ctx);
		else if( MODE_INST_IMGSRC.equals(ctx.mode) )
			return instimgsrc(ctx);
		else if( MODE_BACKUP_MSGRES.equals(ctx.mode) )
			return bckmsgres(ctx);
		else if( MODE_PUT.equals(ctx.mode) ) {
			submoder.startSubmode(ctx.pageConfig, MODE_PUT);
			return modify(ctx);
		}

		if( LISTDATASRC_MAPPING.keySet().contains(ctx.mode.replaceFirst("list", "")) ) {
			return listing(ctx, ctx.mode, ctx.mode.replaceFirst("list", ""));
		}

		return super.doRequest(ctx, isPost);
	}

	/**
	 * TODO: remove reflection.
	 */
	private String[] getBundleBaseNamesUnsafely( MessageHandler msghandler ) {
		String[] bundleBaseNames = null;
		try {
			if( msghandler instanceof com.irt.util.MessageBundle ) {
				bundleBaseNames = (String[])ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "bundleBaseNames");
			} else {
				MessageHandler mm = (MessageHandler)ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "msghandler");
				bundleBaseNames = (String[])ReflectUtil.getDeclaredFieldObject(mm.getClass(), mm, "bundleBaseNames");
			}
		} catch( IllegalArgumentException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IllegalAccessException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bundleBaseNames;
	}

	/**
	 * TODO: remove reflection.
	 */
	private ResourceBundle[] getBundlesUnsafely( MessageHandler msghandler ) {
		ResourceBundle[] bundles = null;
		try {
			if( msghandler instanceof com.irt.util.MessageBundle ) {
				bundles = (ResourceBundle[])ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "bundles");
			} else {
				MessageHandler mm = (MessageHandler)ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "msghandler");
				bundles = (ResourceBundle[])ReflectUtil.getDeclaredFieldObject(mm.getClass(), mm, "bundles");
			}
		} catch( IllegalArgumentException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IllegalAccessException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bundles;
	}

	/** TODO: find parent messagekey ( inside value has ${} notation ) to resolve that */
	private List<Map<String, Object>> getCurrentMessageResource( SystemConfig systemConfig, boolean uniq, Locale... locales )
			throws ServletModelException {
		List<String> processedBundleNames = new ArrayList<String>();

		Set<Map<String, Object>> props = new HashSet<Map<String, Object>>();
		for( int lo = 0; lo < locales.length; lo++ ) {
			MessageHandler mh = systemConfig.getMessageHandler(locales[lo]);

			ResourceBundle[] bundles = getBundlesUnsafely(mh);
			String[] bundleBaseNames = getBundleBaseNamesUnsafely(mh);

			if( bundles.length != bundleBaseNames.length ) {
				throw new ServletModelException(ServletModelException.INTERNAL_ERROR);
			}

			String localeString = locales[lo].getLanguage();

			if( bundles != null ) {
				for( int i = 0; i < bundles.length; i++ ) {
					// String baseName = bundles[i].getBaseBundleName();//jdk1.8
					// String localeString = bundles[i].getLocale().toString();

					String baseName = DatabaseResourceRepositoryImpl.BundleNaming.getPureBaseName(bundleBaseNames[i]);
					String bundleName = DatabaseResourceRepositoryImpl.BundleNaming.toBundleName(baseName, localeString);

					if( !uniq || !processedBundleNames.contains(bundleName) ) {
						Enumeration<String> keys = bundles[i].getKeys();
						while( keys.hasMoreElements() ) {
							String key = keys.nextElement();

							if( key != null && key.length() > 0 ) {

								Map<String, Object> map = new HashMap<String, Object>();
								map.put("resourceBaseName", baseName);
								map.put("resourceLocale", localeString);
								map.put("resourceKey", key);
								try {
									Object value = bundles[i].getObject(key);
									map.put("resourceValue", value);
								} catch( java.util.MissingResourceException missing ) {
									// probably null value for 'resourceValue'
									// map.put("resourceValue", RESOURCE_VALUE_BLANK);
									map.put("resourceValue", null);
								}
								props.add(map);
							}
						}
						processedBundleNames.add(bundleName);
					}
				}
			}
		}

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.addAll(props);

		return list;
	}

	private List<String> getPageColumnListNames( String[] reqIds ) {
		List<String> msgkeys = new ArrayList<String>();
		if( reqIds != null ) {
			List<String> pageIds = new ArrayList<String>();
			for( String id : reqIds ) {
				if( id != null )
					pageIds.addAll(PageMessageKeys.getAllPageIdsByReq(id));
			}
			for( String pageId : pageIds ) {
				List list = PageMessageKeys.getColumnListNames(pageId);
				if( list != null )
					msgkeys.addAll(list);
			}
		}

		return msgkeys;
	}

	private List<Map.Entry<String, String>> getPageMessageKeys( String[] reqIds ) {

		List<String> pageIds = new ArrayList<String>();
		// List<String> pageIds = new ArrayList<String>();

		for( String id : reqIds ) {
			pageIds.addAll(PageMessageKeys.getAllPageIdsByReq(id));
		}

		// Set<String> allCapturedPageIds = PageMessageKeys.getAllPageIds();
		List<Map.Entry<String, String>> msgkeys = new ArrayList<Map.Entry<String, String>>();
		if( pageIds != null ) {
			for( String pageId : pageIds ) {
				List<Map.Entry<String, String>> msgkeylist = PageMessageKeys.getMessageKeys(pageId);
				for( Map.Entry<String, String> entry : msgkeylist ) {
					String value = entry.getValue();
					List<String> subkeys = new ArrayList<String>();
					// if( value != null && value.contains("\\${") ) {
					if( value != null ) {
						if( entry.getKey().equals("MSG_COND_SALES_ORGANIZATION") ) {
							System.out.println();
						}
						Matcher match = SUBKEY_FIND_PATTERN.matcher(value);
						if( match.find() ) {
							for( int i = 0; i < match.groupCount(); i++ ) {
								subkeys.add(match.group(i));
							}
						}
					}
					for( String subkey : subkeys ) {
						msgkeys.add(new SimpleEntry<String, String>(subkey, null));
					}
				}

				msgkeys.addAll(msgkeylist);
			}
		}

		return msgkeys;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("SYS");
	}

	private List<Map<String, Object>> getUniqueList( List<Map<String, Object>> currView, List<Map<String, Object>> dbView,
			String[] commonKeys ) {
		List<Map<String, Object>> toRemoves = new ArrayList<Map<String, Object>>();

		if( dbView != null && dbView.size() > 0 ) {
			for( Map<String, Object> dbmesg : dbView ) {
				Object[] o1 = Record.extractValues(dbmesg, commonKeys);
				Set<Object> set1 = new HashSet<Object>(java.util.Arrays.asList(o1));

				for( Map<String, Object> currmesg : currView ) {
					Object[] o2 = Record.extractValues(currmesg, commonKeys);
					Set<Object> set2 = new HashSet<Object>(java.util.Arrays.asList(o2));

					if( set1.equals(set2) ) {
						toRemoves.add(currmesg);
					}
				}
			}

			for( Map<String, Object> common : toRemoves ) {
				currView.remove(common);
			}

			currView.addAll(dbView);
		}

		return currView;
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		ManipulableManager db = resolveDbManager(ctx);

		if( inputting ) {
		} else {
			Map<String, Object> paramMap = new ParameterMap(ctx.req);
			ctx.req.setAttribute("record", db.getRecord(paramMap));
		}

		return jsonResponse(ctx, "record");
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( pageConfig.getProperty("pageTitlePrefix") == null ) {
			pageConfig.setProperty("pageTitlePrefix", getPageTitlePrefix(getServletName(), getInitParameter("pageTitlePrefix")));
		}

		ctx.db = new DatabaseMessageResource(ctx.handler);
		ctx.extraObj = new DatabaseColumnResource(ctx.handler);

		// setSystemPackageCode, partyId
		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_PUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode(null, null);
		else if( MODE_PAGEKEYS.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_LISTSYSENV.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_LISTENVDATA.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_LISTMSGRES.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_LISTCOLRES.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_INST_MSGRES.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_INST_IMGSRC.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_BACKUP_MSGRES.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
		else {
			if( LISTDATASRC_MAPPING.keySet().contains(ctx.mode.replaceFirst("list", "")) ) {
				pageConfig.setSystemPackageCode("CST", "CSTMessage.MNG");
			} else {
				throw new ServletModelException(ServletModelException.INVALID_MODE);
			}
		}

		pageConfig.setTitle(ctx.msghandler.getMessage(pageConfig.getProperty("pageTitlePrefix") + ctx.mode.toUpperCase()));
	}

	private Map<String, Object> initJqdtContext( Context ctx, String[] viewFieldKeys ) {
		JqdtConfigHelper helper = new JqdtConfigHelper();

		Map<String, Object> jqdtConfig = new HashMap<String, Object>(JqdtConfigHelper.Default.getSaneDefault());
		jqdtConfig.put("columns", helper.getColumns(viewFieldKeys, viewFieldKeys));
		jqdtConfig.put("responsive", helper.getPluginResponsive());

		// String[] downButtons = new String[] { "csv", "excel", "pdf" };
		// String[] downButtons = new String[] { "copy", "csv", "${tsvButtonString}", "excel" };
		String[] downButtons = new String[] { "copy", "csv", "excel" };
		jqdtConfig = helper.getConfigMap$Buttons(jqdtConfig, downButtons);

		jqdtConfig = helper.getConfigMap$Select(jqdtConfig);

		// because autoFill is confusing, disabled the feature.
		// jqdtConfig.put("autoFill", helper.getPluginKeyTable());
		// String autoFillButtonString = JqdtConfigHelper.Functions.getAutoFillToggleButton("Enable AutoFill", "Disable AutoFill");

		String jqdtJavascriptString = ""
				+ com.irt.json.JqdtConfigHelper.Javascript.getRefreshButtonOnGlobalFunction()
				+ ";"
				+ com.irt.json.JqdtConfigHelper.Javascript.getReloadButtonOnGlobalFunction()
				+ ";"
				+ com.irt.json.JqdtConfigHelper.Javascript.getColumnTitleGetterFunction()
				+ ";";
		ctx.req.setAttribute("jqdtJavascriptString", jqdtJavascriptString);

		return jqdtConfig;
	}

	private boolean instimgsrc( Context ctx ) {
		String[] imgsrcs = ctx.req.getParameterValues("img");
		String[] imglocales = ctx.req.getParameterValues("imgLocale");
		String[] imgnames = ctx.req.getParameterValues("name");

		String modifyUserId = ctx.sessionMng.getUniqId();
		DatabaseMessageResource db = (DatabaseMessageResource)ctx.db;

		for( int i = 0; i < imgsrcs.length; i++ ) {
			Map<String, Object> toInsert = Record.createMap("resourceKey", imgsrcs[i]);
			toInsert.put("resourceBaseName", "mesg.SystemImageResource");
			toInsert.put("resourceLocale", ( ( imglocales == null || imglocales[i] == null ) ? ctx.locale.getLanguage() : imglocales[i] ));
			toInsert.put("resourceValue", ( ( imgnames == null || imgnames[i] == null ) ? imgsrcs[i] : imgnames[i] ));
			toInsert.put("modifyUserId", modifyUserId);
			toInsert.put("updateDateTime", Calendar.getInstance().getTime());

			try {
				// Map<String, Object> record = db.getRecord(toInsert);
				// if( imgnames == null || imgnames[i] == null ) {
				//
				// } else {
				// if( record.containsKey("resourceValue") && imgnames[i].equals(record.get("resourceValue")) ) {
				// toInsert.put("resourceValue", ( ( imgnames == null || imgnames[i] == null ) ? imgsrcs[i] : imgnames[i] ));
				// } else {
				//
				// }
				// }

				if( !db.modify(toInsert) ) {
					db.regist(toInsert);
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REGIST_SUCCESS"));
				} else {
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS"));
				}
			} catch( DataException e ) {
				e.printStackTrace();
			} catch( SQLException e ) {
				e.printStackTrace();
			}
		}

		return true;
	}

	/**
	 *
	 * CSTMessage?mode=instmsgres&fromLocale=en&toLocale=vi&systemCode=ICS
	 *
	 */
	private boolean instmsgres( Context ctx ) throws SQLException, ServletException, IOException {
		DatabaseMessageResource db = (DatabaseMessageResource)ctx.db;

		String fromLocale = ctx.req.getParameter("fromLocale");
		String toLocale = ctx.req.getParameter("toLocale");

		String modifyUserId = ctx.sessionMng.getUniqId();

		if( fromLocale == null || fromLocale.length() <= 0 ) {
			throw new ServletModelException(ServletModelException.INTERNAL_ERROR, "mandatory");
		}
		if( toLocale == null || toLocale.length() <= 0 ) {
			throw new ServletModelException(ServletModelException.INTERNAL_ERROR, "mandatory");
		}

		String systemCode = ctx.req.getParameter("systemCode");
		SystemConfig systemConfig = null;
		try {
			if( systemCode != null && systemCode.length() > 0 ) {
				systemConfig = com.irt.custom.SystemConfig.getInstance(systemCode);
			}
		} finally {
			if( systemConfig == null ) {
				systemConfig = getSystemConfig();
			}
		}

		// maynot up-to-date resource( since db resource updating may take time by 'ttl' )
		List<Map<String, Object>> fromLocaleCurrView = getCurrentMessageResource(systemConfig, true, new Locale(fromLocale));

		List<Map<String, Object>> toLocaleCurrView = getCurrentMessageResource(systemConfig, true, new Locale(toLocale));

		Map<String, Object> conditionMap = Record.createMap("resourceLocale", toLocale);
		List<Map<String, Object>> toLocaleDbView = db.getRecords(conditionMap);

		List<Integer> toLocaleInCurrView = new ArrayList<Integer>();// index of fromLocaleCurrView
		List<Integer> toLocaleInDbView = new ArrayList<Integer>();// index of fromLocaleCurrView

		String[] compareKeys = new String[] { "resourceKey", "resourceBaseName" };

		int idx = 0;
		for( Map<String, Object> currmap : fromLocaleCurrView ) {
			Object[] part1 = Record.extractValues(currmap, compareKeys);
			Set<Object> set1 = new HashSet<Object>(java.util.Arrays.asList(part1));

			if( toLocaleDbView != null ) {
				for( Map<String, Object> inmap : toLocaleDbView ) {
					Object[] part2 = Record.extractValues(inmap, compareKeys);
					Set<Object> set2 = new HashSet<Object>(java.util.Arrays.asList(part2));

					boolean isSamePrimaryKeys = set1.equals(set2);

					if( isSamePrimaryKeys ) {
						// Object value1 = currmap.get("resourceValue");
						// Object value2 = inmap.get("resourceValue");

						toLocaleInDbView.add(idx);
					}
				}
			}
			idx++;
		}

		DataResult result = new DataResult();
		for( int i = 0; i < fromLocaleCurrView.size(); i++ ) {
			if( !toLocaleInDbView.contains(i) ) {
				// only not in db and not in curr view ( not really value is existing... )
				Map<String, Object> toInsert = fromLocaleCurrView.get(i);

				if( toInsert.get("resourceValue") != null ) {
					toInsert.put("resourceLocale", toLocale);
					toInsert.put("modifyUserId", modifyUserId);
					toInsert.put("updateDateTime", Calendar.getInstance().getTime());

					try {
						db.regist(toInsert);
						result.increaseRegistCount();
					} catch( DataException dataEx ) {
						ctx.handler.rollback();
						ctx.pageConfig.setMessage(dataEx.getMessage());
						logger.info("error." + toInsert, dataEx);
						result.appendError(dataEx);
					} catch( SQLException sqlEx ) {
						ctx.handler.rollback();
						ctx.pageConfig.setMessage(sqlEx.getMessage());
						logger.error("internal error.", sqlEx);
					}
				}
			}
		}

		ctx.req.setAttribute("result", result);
		return jsonResponse(ctx, "insertCount", "result");
	}

	@Override
	protected boolean jsonResponse( Context ctx, String... reqAttrKeys ) throws IOException {
		return jsonResponse(ctx, DEFAULT_JSON_NULL_AS_EMPTY, reqAttrKeys);
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		String[] targetLocaleStrings = ctx.req.getParameterValues("targetLocale");
		if( targetLocaleStrings == null ) {
			String currentLocaleString = DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(ctx.locale);
			targetLocaleStrings = new String[] { currentLocaleString };
		}

		final String DEFAULT_SYSTEM_LANG = "en";

		List<Locale> anotherLocales = new ArrayList<Locale>();
		for( int i = 0; i < targetLocaleStrings.length; i++ ) {
			if( targetLocaleStrings[i] != null && targetLocaleStrings[i].length() > 0 ) {
				if( !DEFAULT_SYSTEM_LANG.equals(targetLocaleStrings[i]) ) {
					anotherLocales.add(new Locale(targetLocaleStrings[i]));
				}
			}
		}

		String[] reqIds = ctx.req.getParameterValues("reqId");
		String dataSrc = ctx.req.getParameter("dataSrc");

		String systemCode = ctx.req.getParameter("systemCode");
		SystemConfig systemConfig = null;
		try {
			if( systemCode != null && systemCode.length() > 0 ) {
				systemConfig = com.irt.custom.SystemConfig.getInstance(systemCode);
			}
		} finally {
			if( systemConfig == null ) {
				systemConfig = getSystemConfig();
			}
		}

		if( "msgres".equals(dataSrc) ) {
			DatabaseMessageResource db = (DatabaseMessageResource)ctx.db;
			List<Map<String, Object>> currentView = getCurrentMessageResource(systemConfig, true, anotherLocales.toArray(new Locale[0]));
			if( java.util.Arrays.asList(targetLocaleStrings).contains(DEFAULT_SYSTEM_LANG) ) {
				List<Map<String, Object>> defaultLangView = getCurrentMessageResource(systemConfig, true, new Locale(DEFAULT_SYSTEM_LANG));
				currentView.addAll(defaultLangView);
			}

			Map<String, Object> cmap = new TreeMap<String, Object>();
			cmap.put("resourceLocale", targetLocaleStrings);
			String[] sortKeys = new String[] { "updateDateTime", "resourceBaseName", "resourceKey", "resourceLocale" };
			ServletUtility.setSort(ctx.req, db, sortKeys);
			List<Map<String, Object>> fromDbResMsg = db.getRecords(cmap, MSGRES_viewFieldKeys);

			ctx.req.setAttribute("dbmsgres", fromDbResMsg);
			if( fromDbResMsg != null ) {

				// remove common map and add from dbmap
				// TODO: find better way without below remove and add operation...
				final String[] commonKeys = new String[] { "resourceBaseName", "resourceLocale", "resourceKey", "resourceValue" };
				List<Map<String, Object>> toRemoves = new ArrayList<Map<String, Object>>();
				for( Map<String, Object> dbmesg : fromDbResMsg ) {
					Object[] o1 = Record.extractValues(dbmesg, commonKeys);
					Set<Object> set1 = new HashSet<Object>(java.util.Arrays.asList(o1));

					for( Map<String, Object> currmesg : currentView ) {
						Object[] o2 = Record.extractValues(currmesg, commonKeys);
						Set<Object> set2 = new HashSet<Object>(java.util.Arrays.asList(o2));

						if( set1.equals(set2) ) {
							toRemoves.add(currmesg);
						}
					}
				}
				for( Map<String, Object> common : toRemoves ) {
					currentView.remove(common);
				}

				for( Map<String, Object> map : fromDbResMsg ) {
					// take care blank entry ( json does not like blank entries.( no-key is not good)
					if( map.get("resourceValue") == null ) {
						map.put("resourceValue", RESOURCE_VALUE_BLANK);
					}
					currentView.add(map);
				}
			}

			if( reqIds != null ) {
				final List<Map.Entry<String, String>> msgkeys = getPageMessageKeys(reqIds);

				IPredicate<Map<String, Object>> pred = new IPredicate<Map<String, Object>>() {
					@Override
					public boolean apply( Map<String, Object> type ) {
						String key = (String)type.get("resourceKey");
						for( Map.Entry<String, String> ent : msgkeys ) {
							if( ent.getKey().equals(key) ) {
								return true;
							}
						}

						return false;
					}
				};

				ctx.req.setAttribute("msgres", Predicate.filter(currentView, pred));
			} else {
				ctx.req.setAttribute("msgres", currentView);
			}

		} else if( "colres".equals(dataSrc) ) {
			DatabaseColumnResource db = (DatabaseColumnResource)ctx.extraObj;

			String partyId = ctx.sessionMng.getPartyId();

			ColumnResourceBundle bundle = getColumnResourceBundle(ctx);

			List<Map<String, Object>> dbView = new ArrayList<Map<String, Object>>();
			for( String localeString : targetLocaleStrings ) {
				List<Map<String, Object>> dbViewByLocale = db.getDatabaseColumnResourceMessages(partyId, new Locale(localeString));
				if( dbViewByLocale != null && dbViewByLocale.size() > 0 )
					dbView.addAll(dbViewByLocale);
			}
			ctx.req.setAttribute("dbcolres", dbView);

			final String[] commonKeys = new String[] { "poolName", "poolLocale", "columnType", "columnKey", "partyId" };
			List<Map<String, Object>> currentView = new ArrayList<Map<String, Object>>();
			final List<String> columnListNames = getPageColumnListNames(reqIds);
			if( columnListNames != null && columnListNames.size() > 0 ) {
				for( String columnListName : columnListNames ) {
					for( String targetLocaleString : targetLocaleStrings ) {
						List<Map<String, Object>> byLocale = db.getCurrentColumnResource(bundle, partyId, columnListName,
								new Locale(targetLocaleString));
						if( byLocale != null && byLocale.size() > 0 ) {
							currentView.addAll(byLocale);
						}
					}
				}
			}

			List<Map<String, Object>> theUniqView = null;
			if( dbView.size() > 0 ) {
				theUniqView = getUniqueList(currentView, dbView, commonKeys);
			} else {
				theUniqView = currentView;
			}

			if( reqIds != null ) {

				IPredicate<Map<String, Object>> pred = new IPredicate<Map<String, Object>>() {

					@Override
					public boolean apply( Map<String, Object> type ) {
						if( type == null )
							throw new NullPointerException();

						String poolName = (String)type.get("poolName");
						if( poolName == null )
							return false;

						for( String cl : columnListNames ) {
							String _poolName = cl.split("%")[0];
							if( poolName.equals(_poolName) ) {
								return true;
							}
						}

						return false;
					}

				};

				if( theUniqView != null ) {
					ctx.req.setAttribute("colres", Predicate.filter(theUniqView, pred));
				} else {
					ctx.req.setAttribute("colres", null);
				}
			} else {
				ctx.req.setAttribute("colres", theUniqView);
			}
		} else {
			throw new IllegalStateException();
		}

		return jsonResponse(ctx, "db" + dataSrc, dataSrc);
	}

	private boolean listcolres( Context ctx ) throws ServletException, IOException {
		Table table = SchemaTableFinder.create()
				.withSchemaClassName(DatabaseResourceRepository.CST_RESOURCE_SCHEMA_CLASS)
				.withSchemaTableKey(DatabaseResourceRepository.CST_COLUMN_RESOURCE)
				.getTable();

		String[] viewFieldKeys = COLRES_viewFieldKeys;

		JqdtConfigHelper helper = new JqdtConfigHelper();
		Jsoner jsoner = new Jsoner();
		jsoner.setPrettyPrinting();

		Map<String, Object> jqdtConfig = initJqdtContext(ctx, viewFieldKeys);
		jqdtConfig = helper.getConfigMap$RowGroup(jqdtConfig, viewFieldKeys, "poolName");

		String[] reqIds = ctx.req.getParameterValues("reqId");
		String dataSrc = "colres";
		String url = createJqdtConfigAjaxUrl(ctx.req, "list", dataSrc, reqIds);
		url += "&locale=" + ctx.locale;// passing ctxlocale
		jqdtConfig = helper.getConfigMap$Ajax(jqdtConfig, url, dataSrc);

		String[] sortKeys = new String[] { "poolName", "columnKey", "poolLocale" };
		jqdtConfig = helper.getConfigMap$Sort(jqdtConfig, viewFieldKeys, sortKeys, null);
		if( !ctx.sessionMng.isSystemAdmin() ) {
			jqdtConfig = helper.getConfigMap$HiddenColumns(jqdtConfig, viewFieldKeys, COLRES_SystemAdminOnlyKeys);
		}

		String[] viewReadonly = new String[] { "updateDateTime", "columnParentKey" };
		Map<String, Object> cellEditConfigMap = JqdtConfigHelper.getCellEditConfig(viewFieldKeys, table, viewReadonly);

		Map<String, Object> allowNulls = (Map<String, Object>)cellEditConfigMap.get("allowNulls");
		if( allowNulls != null ) {
			Object allowNullColumnIndex = allowNulls.get("columns");
			if( allowNullColumnIndex != null ) {
				jqdtConfig = helper.getConfigMap$ColumnDefsDefaultContent(jqdtConfig, (List<Integer>)allowNullColumnIndex, "");
			}
		}

		logger.debug("jqdtConfig: " + jqdtConfig);
		ctx.req.setAttribute("jqdtConfigString", jsoner.toJson(jqdtConfig));

		String cellEditConfigString = jsoner.toJson(cellEditConfigMap);

		ctx.req.setAttribute("cellEditConfigString", cellEditConfigString);

		String jqdtConfigColumnDefsString = JqdtConfigHelper.Javascript.getConfigColumnDefs(viewFieldKeys);
		ctx.req.setAttribute("jqdtConfigColumnDefsString", jqdtConfigColumnDefsString);
		ctx.req.setAttribute("fieldHeaders", helper.getListOfWrapString(viewFieldKeys, "<th>", "</th>"));

		return forward(ctx, systemConfig.getJspPath() + "/cst_datatables.jsp");
	}

	private boolean listenvdata( Context ctx ) throws IOException, ServletException {
		return false;
	}

	private boolean listing( Context ctx, String pname_mode, String pname_dataSrc ) throws IOException, ServletException, SQLException {

		Entry<String, String> schemaTableEntry = LISTDATASRC_MAPPING.get(pname_dataSrc);

		DaoManager dao = new DaoManager(schemaTableEntry, ctx.handler);
		//
		// * json is requested from servlet( jsp )
		//
		String dataSrc = ctx.req.getParameter("dataSrc");
		if( dataSrc != null ) {
			if( pname_dataSrc.equals(dataSrc) ) {
				String _start = ctx.req.getParameter("start");// datatable's pipeline option "start"
				String _length = ctx.req.getParameter("length");// datatable's pipeline option "length"
				int start = 0;
				if( _start != null ) {
					try {
						start = Integer.parseInt(_start);
					} catch( NumberFormatException nfEx ) {
					}
				}
				int length = -1;
				if( _length != null ) {
					try {
						length = Integer.parseInt(_length);
					} catch( NumberFormatException nfEx ) {
					}
				}

				Logger.getRootLogger().debug("listing(" + dataSrc + ")" + " start: " + start + " length: " + length);

				int MAX_ANONYMOUS_LISTING_SIZE = 1000;
				if( length == -1 ) {
					int allCount = dao.getManager().getRecordCount(null);
					if( allCount >= MAX_ANONYMOUS_LISTING_SIZE ) {
						String message = "Your request is too large with current maximum(" + MAX_ANONYMOUS_LISTING_SIZE + ")";
						ctx.pageConfig.setMessage(message);
						ctx.res.setStatus(500);
						return jsonResponse(ctx, pname_dataSrc);
						// throw new ServletException(ServletModelException.INVALID_REQUEST,
						// new Throwable(message));
					}
				}

				List<Map<String, Object>> envlist = dao.getManager().getRecords(null, start, length);
				ctx.req.setAttribute(pname_dataSrc, envlist);
				return jsonResponse(ctx, pname_dataSrc);
			} else {
				throw new ServletException(ServletModelException.INVALID_PARAMETER);
			}
		}

		//
		// * servlet
		//
		Table table = dao.getManager().getDao().getTable();
		String[] pkFieldKeys = dao.getManager().getDao().getPrimaryFieldKeys();
		List<String> readonlyFieldKeys = dao.getManager().getDao().getReadonlyFieldKeyList();

		List<String> _viewFieldKeys = new ArrayList<String>();
		List<String> alterableFieldKeys = dao.getManager().getDao().getAlterableFieldKeyList();

		_viewFieldKeys.addAll(alterableFieldKeys);
		_viewFieldKeys.addAll(readonlyFieldKeys);
		_viewFieldKeys.addAll(0, java.util.Arrays.asList(pkFieldKeys == null ? new String[] {} : pkFieldKeys));

		String[] viewFieldKeys = _viewFieldKeys.toArray(new String[0]);

		JqdtConfigHelper helper = new JqdtConfigHelper();
		Jsoner jsoner = new Jsoner();
		jsoner.setPrettyPrinting();

		Map<String, Object> jqdtConfig = initJqdtContext(ctx, viewFieldKeys);
		// jqdtConfig = helper.getConfigMap$RowGroup(jqdtConfig, viewFieldKeys, "poolName");

		String url = createJqdtConfigAjaxUrl(ctx.req, pname_mode, pname_dataSrc, null);
		url += "&locale=" + ctx.locale;// passing ctxlocale

		jqdtConfig = helper.getConfigMap$Ajax(jqdtConfig, url, pname_dataSrc, "GET");

		// http://www.tothenew.com/blog/using-pipeline-in-datatables/
		String jqdtConfigOverrideString = "{"
				+ "processing:" + "true" + ","
				+ "serverSide:" + "true" + ","
				+ "ajax:" + "$.fn.dataTable.pipeline({"
				+ "			url: " + "'" + url + "'" + ","
				+ "			pages: " + " 5 " + ","
				+ "			dataSrc: " + "'" + HtmlUtility.toHtmlString(pname_dataSrc) + "'" + ","
				+ "			error: function(xhr, error, code){ "
				+ "					HtmlEx.error(error);"
				+ "					console.log(error);"
				+ "				}" + ","
				+ "		})" + ","
				+ "lengthMenu: ["
				+ "			[10, 25, 50, 100, -1],"
				+ "			[10, 25, 50, 100, \"All\"]"
				+ "		]" + ","
				// + "pagingType: " + "'" + "full_numbers" + "'" + ","
				+ "}";
		ctx.req.setAttribute("jqdtConfigOverrideString", jqdtConfigOverrideString);

		// String[] sortKeys = new String[] { "systemCode", "envName", "envKey" };
		jqdtConfig = helper.getConfigMap$Sort(jqdtConfig, viewFieldKeys, pkFieldKeys, null);

		// if( !ctx.sessionMng.isSystemAdmin() ) {
		// jqdtConfig = helper.getConfigMap$HiddenColumns(jqdtConfig, viewFieldKeys, COLRES_SystemAdminOnlyKeys);
		// }

		String[] alterKeys = new String[] {};

		Map<String, Object> cellEditConfigMap = JqdtConfigHelper.getCellEditConfig(viewFieldKeys, table, viewFieldKeys, alterKeys);

		Map<String, Object> allowNulls = (Map<String, Object>)cellEditConfigMap.get("allowNulls");
		if( allowNulls != null ) {
			Object allowNullColumnIndex = allowNulls.get("columns");
			if( allowNullColumnIndex != null ) {
				jqdtConfig = helper.getConfigMap$ColumnDefsDefaultContent(jqdtConfig, (List<Integer>)allowNullColumnIndex, "");
			}
		}

		logger.debug("jqdtConfig: " + jqdtConfig);
		ctx.req.setAttribute("jqdtConfigString", jsoner.toJson(jqdtConfig));

		String cellEditConfigString = jsoner.toJson(cellEditConfigMap);

		ctx.req.setAttribute("cellEditConfigString", cellEditConfigString);

		String jqdtConfigColumnDefsString = JqdtConfigHelper.Javascript.getConfigColumnDefs(viewFieldKeys);
		ctx.req.setAttribute("jqdtConfigColumnDefsString", jqdtConfigColumnDefsString);
		ctx.req.setAttribute("fieldHeaders", helper.getListOfWrapString(viewFieldKeys, "<th>", "</th>"));

		return forward(ctx, systemConfig.getJspPath() + "/cst_datatables.jsp");
	}

	protected boolean listmsgres( Context ctx ) throws IOException, ServletException {
		Table table = SchemaTableFinder.create()
				.withSchemaClassName(DatabaseResourceRepository.CST_RESOURCE_SCHEMA_CLASS)
				.withSchemaTableKey(DatabaseResourceRepository.CST_MESSAGE_RESOURCE)
				.getTable();
		String[] viewFieldKeys = MSGRES_viewFieldKeys;

		initJqdtContext(ctx, viewFieldKeys);

		JqdtConfigHelper helper = new JqdtConfigHelper();
		Jsoner jsoner = new Jsoner();
		jsoner.setPrettyPrinting();

		Map<String, Object> jqdtConfig = initJqdtContext(ctx, viewFieldKeys);
		jqdtConfig = helper.getConfigMap$RowGroup(jqdtConfig, viewFieldKeys, "resourceKey");

		String[] reqIds = ctx.req.getParameterValues("reqId");
		String dataSrc = "msgres";

		String url = createJqdtConfigAjaxUrl(ctx.req, "list", dataSrc, reqIds);
		jqdtConfig = helper.getConfigMap$Ajax(jqdtConfig, url, dataSrc);

		ctx.req.setAttribute("jqdtConfigString", jsoner.toJson(jqdtConfig));

		Map<String, Object> cellEditConfigMap = JqdtConfigHelper.getCellEditConfig(viewFieldKeys, table);
		String cellEditConfigString = jsoner.toJson(cellEditConfigMap);
		ctx.req.setAttribute("cellEditConfigString", cellEditConfigString);

		String jqdtConfigColumnDefsString = JqdtConfigHelper.Javascript.getConfigColumnDefs(viewFieldKeys);
		ctx.req.setAttribute("jqdtConfigColumnDefsString", jqdtConfigColumnDefsString);
		ctx.req.setAttribute("fieldHeaders", helper.getListOfWrapString(viewFieldKeys, "<th>", "</th>"));

		return forward(ctx, systemConfig.getJspPath() + "/cst_datatables.jsp");
	}

	private boolean listpagemsgs( Context ctx ) throws IOException {
		String[] pageIds = ctx.req.getParameterValues("pageId");
		if( pageIds == null ) {
			String[] reqId = ctx.req.getParameterValues("reqId");
			List<String> pages = new ArrayList<String>();
			for( String id : reqId ) {
				pages.addAll(PageMessageKeys.getAllPageIdsByReq(id));
			}

			if( pages != null )
				pageIds = pages.toArray(new String[0]);
		}

		// Set<String> allCapturedPageIds = PageMessageKeys.getAllPageIds();
		List<Map.Entry<String, String>> pageMessages = new ArrayList<Map.Entry<String, String>>();
		if( pageIds != null ) {
			for( String pageId : pageIds ) {
				pageMessages.addAll(PageMessageKeys.getMessageKeys(pageId));
			}
		}

		ctx.req.setAttribute("pagekeys", pageMessages);
		return jsonResponse(ctx, "pagekeys");
	}

	private boolean listsysenv( Context ctx ) throws IOException, ServletException {
		//
		// * json is requested from servlet( jsp )
		//
		String dataSrc = ctx.req.getParameter("dataSrc");
		if( dataSrc != null ) {
			if( "sysenv".equals(dataSrc) ) {
				try {
					List<Map<String, Object>> envlist = SystemEx.getSysEnvRecords(ctx.handler);
					ctx.req.setAttribute("sysenv", envlist);
					return jsonResponse(ctx, "sysenv");
				} catch( SQLException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				throw new ServletException(ServletModelException.INVALID_PARAMETER);
			}
		}

		//
		// * servlet
		//
		Table table = SchemaTableFinder.create()
				.withSchemaClassName("com.irt.rbm.sys.SystemEnvironment.ThisSchema")
				.withSchemaTableKey(com.irt.rbm.sys.Schema.SYS_SYSTEM_ENVIRONMENT)
				.getTable();

		String[] viewFieldKeys = { "systemCode", "envName", "envKey", "envVal", "status", "updateDateTime" };

		JqdtConfigHelper helper = new JqdtConfigHelper();
		Jsoner jsoner = new Jsoner();
		jsoner.setPrettyPrinting();

		Map<String, Object> jqdtConfig = initJqdtContext(ctx, viewFieldKeys);
		// jqdtConfig = helper.getConfigMap$RowGroup(jqdtConfig, viewFieldKeys, "poolName");

		String _dataSrc = "sysenv";
		String url = createJqdtConfigAjaxUrl(ctx.req, "listsysenv", _dataSrc, null);
		url += "&locale=" + ctx.locale;// passing ctxlocale

		jqdtConfig = helper.getConfigMap$Ajax(jqdtConfig, url, _dataSrc);

		String[] sortKeys = new String[] { "systemCode", "envName", "envKey" };
		jqdtConfig = helper.getConfigMap$Sort(jqdtConfig, viewFieldKeys, sortKeys, null);

		// if( !ctx.sessionMng.isSystemAdmin() ) {
		// jqdtConfig = helper.getConfigMap$HiddenColumns(jqdtConfig, viewFieldKeys, COLRES_SystemAdminOnlyKeys);
		// }

		String[] alterKeys = new String[] { "envVal" };
		String[] viewReadonly = new String[] { "status", "updateDateTime" };
		Map<String, Object> cellEditConfigMap = JqdtConfigHelper.getCellEditConfig(viewFieldKeys, table, viewReadonly, alterKeys);

		Map<String, Object> allowNulls = (Map<String, Object>)cellEditConfigMap.get("allowNulls");
		if( allowNulls != null ) {
			Object allowNullColumnIndex = allowNulls.get("columns");
			if( allowNullColumnIndex != null ) {
				jqdtConfig = helper.getConfigMap$ColumnDefsDefaultContent(jqdtConfig, (List<Integer>)allowNullColumnIndex, "");
			}
		}

		logger.debug("jqdtConfig: " + jqdtConfig);
		ctx.req.setAttribute("jqdtConfigString", jsoner.toJson(jqdtConfig));

		String cellEditConfigString = jsoner.toJson(cellEditConfigMap);

		ctx.req.setAttribute("cellEditConfigString", cellEditConfigString);

		String jqdtConfigColumnDefsString = JqdtConfigHelper.Javascript.getConfigColumnDefs(viewFieldKeys);
		ctx.req.setAttribute("jqdtConfigColumnDefsString", jqdtConfigColumnDefsString);
		ctx.req.setAttribute("fieldHeaders", helper.getListOfWrapString(viewFieldKeys, "<th>", "</th>"));

		return forward(ctx, systemConfig.getJspPath() + "/cst_datatables.jsp");
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		ManipulableManager db = resolveDbManager(ctx);

		Map<String, Object> paramMap = new ParameterMap(ctx.req);

		try {
			db.delete(paramMap);
			ctx.req.setAttribute("record", paramMap);
			return jsonResponse(ctx, "record");
		} catch( UnsupportedOperationException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage(dataEx.getMessage()));
			logger.error("internal error.", dataEx);
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage(dataEx.getMessage()));
			logger.error("internal error.", dataEx);
		}

		setError(ctx);
		return jsonResponse(ctx, "error");
	}

	private ManipulableManager resolveDbManager( Context ctx ) throws ServletModelException {
		String dataSrc = (String)ctx.req.getParameter("dataSrc");
		if( dataSrc == null || dataSrc.length() <= 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		ManipulableManager db = null;
		if( "colres".equals(dataSrc) ) {
			db = (DatabaseColumnResource)ctx.extraObj;
		} else if( "msgres".equals(dataSrc) ) {
			db = (DatabaseMessageResource)ctx.db;
		} else if( "sysenv".equals(dataSrc) ) {
			db = new com.irt.rbm.sys.SystemEnvironment(ctx.handler);
		} else {
			throw new ServletModelException(ServletModelException.INVALID_PARAMETER);
		}

		return db;
	}

	private void setError( Context ctx ) {
		Map<String, Object> error = new HashMap<String, Object>();
		error.put("record", ctx.req.getAttribute("record"));
		ctx.req.setAttribute("error", error);
		ctx.res.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ManipulableManager db = resolveDbManager(ctx);

		// String[] defaultFieldKeys = { "status", "createDateTime", "updateDateTime" };
		String submode = this.submoder.endSubmodeAndDispose(ctx.pageConfig);

		// 레코드 읽기
		Map<String, Object> recordMap = new ParameterMap(ctx.req);
		if( "msgres".equals(ctx.req.getParameter("dataSrc")) && "mesg.PageGuide".equals(recordMap.get("resourceName")) ) {
			String value = (String)recordMap.get("resourceValue");
			recordMap.put("resourceValue", HtmlUtility.cleanXSS(value));
		}

		recordMap.put("modifyUserId", ctx.sessionMng.getUniqId());

		ctx.req.setAttribute("record", recordMap);
		ctx.req.setAttribute("fieldSet", db.getFieldSet(inserting));

		List<String> fieldKeys = new ArrayList<String>();
		for( String key : recordMap.keySet() ) {
			// currently cannot handle array structure. so should ignore.(usually "createDateTime" or "updateDateTime" has extra array structure)
			boolean hasJsonArrayThatCannotHandle = key.contains("[");
			if( !hasJsonArrayThatCannotHandle ) {
				fieldKeys.add(key);
			}
		}

		Map<String, Object> _recordMap = new HashMap<String, Object>();
		for( String fieldKey : fieldKeys ) {
			_recordMap.put(fieldKey, recordMap.get(fieldKey));
		}
		_recordMap.put("updateDateTime", Calendar.getInstance().getTime());

		try {
			if( inserting ) {
				db.regist(_recordMap);
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REGIST_SUCCESS"));
			} else {
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

			// String[] fieldKeys = com.irt.util.Arrays.append(recordMap.keySet().toArray(new String[0]), defaultFieldKeys);
			// String[] fieldKeys = recordMap.keySet().toArray(new String[0]);

			ctx.req.setAttribute("record", db.getRecord(_recordMap, fieldKeys.toArray(new String[0])));
			ctx.pageConfig.setManageAuth(true);
			ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);

			return jsonResponse(ctx, "record");
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage(dataEx.getMessage()));
			if( "Y".equals(ctx.req.getParameter("debugDATA")) ) {
				logger.info("error. record: " + ctx.req.getAttribute("record"), dataEx);
			} else {
				logger.info("error.", dataEx);
			}
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage(sqlEx.getMessage()));
			logger.error("internal error.", sqlEx);
		}

		String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
		ctx.pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_CST_MESSAGE_" + mode.toUpperCase()));

		setError(ctx);
		return jsonResponse(ctx, "error");
	}

	public static enum InstallStrategy {
		INST_ONLY_MISSING;
	}

}
