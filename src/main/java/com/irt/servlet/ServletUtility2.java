/*
 *	File Name:	 ServletUtility2.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/04/30		2.2.0c	create
 *
**/

package com.irt.servlet;

import com.irt.data.DataLoader;
import com.irt.data.Record;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.json.Jsoner;
import com.irt.rbm.RBMSystem;
import com.irt.sql.SQLHandler;
import com.irt.util.FileType;
import com.irt.util.RBMWorkbook;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ServletUtility2 {//@formatter:on

	private static RequestDispatcher JSON_DISPATCHER = createJsonDispatcher();

	public static Map<String, Object> createErrorMap( Object name, String message ) {
		Map<String, Object> errorMap = new java.util.TreeMap<String, Object>();

		errorMap.put("name", name);
		errorMap.put("message", message);

		return errorMap;
	}

	protected static Map<String, Object> createErrorMap( Object name, Throwable throwable ) {
		return createErrorMap(name, throwable.getMessage());
	}

	public static RequestDispatcher createJsonDispatcher() {
		return new RequestDispatcher() {
			private void doJsonResponse( ServletRequest req, ServletResponse res, String... reqAttrKeys ) throws IOException {
				res.setContentType("application/json; charset=utf-8");
				res.setCharacterEncoding("UTF-8");
				java.io.PrintWriter pw = res.getWriter();
				// Map map = com.irt.data.Record.createMap("msg", pageConfig.getMessage());
				Map map = com.irt.data.Record.createMap("msg", null);
				for( String key : reqAttrKeys ) {
					Object obj = req.getAttribute(key);
					if( obj == null )
						continue;
					map.put(key, obj);
				}

				String jsonString = Jsoner.getInstance().toJson(map);

				pw.write(jsonString);
				pw.flush();
				pw.close();
			}

			@Override
			public void forward( ServletRequest request, ServletResponse response ) throws ServletException, IOException {
				String json = request.getParameter("json");
				String[] jsonAttrs;
				if( json != null && json.indexOf(";") > 0 )
					jsonAttrs = json.split(";");
				else
					jsonAttrs = new String[] { json };

				doJsonResponse(request, response, jsonAttrs);
			}

			@Override
			public void include( ServletRequest request, ServletResponse response ) throws ServletException, IOException {
			}
		};
	}

	public static String getFileType( ServletRequest request, SQLHandler handler, String mode, org.slf4j.Logger logger )
			throws ServletModelException, IOException {
		String fileType = null;
		String classMethodName = com.irt.util.TraceHelper.getClassMethodName(com.irt.util.TraceHelper.CURRENT);
		if( logger.isDebugEnabled() )
			logger.debug("TraceHelper.getClassMethodName::" + classMethodName);

		if( com.irt.rbm.RBMSystem.getSystemEnvBool("RBM", "EnvAutoReload;" + classMethodName, false) ) {
			try {
				com.irt.rbm.RBMSystem.reloadSystemEnv(handler);
			} catch( SQLException e ) {
				logger.debug(classMethodName + "::" + e);
			}
		}

		if( ServletModel.MODE_UPLOAD.equals(mode) || ServletModel.MODE_UPLOADINPUT.equals(mode) ) {
			if( !( request instanceof com.irt.servlet.MultipartHttpRequest ) )
				throw new ServletModelException(ServletModelException.INVALID_REQUEST);

			String inputFileName = null;
			java.io.File uploadFile;
			com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)request;
			if( ( uploadFile = req.getFile("file") ) == null )
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
			else
				inputFileName = req.getInputFileName("file");

			fileType = getUploadFileType(request, uploadFile, inputFileName);
		} else {// mode == download 및 그외
			fileType = request.getParameter("fileType");

			if( fileType == null || fileType.length() == 0 ) {
				String defaultFileType = com.irt.rbm.RBMSystem.getSystemEnv("RBM", "Default;DownFileType", com.irt.util.FileType.XLS);

				// SYSTEM_ENV에 정의된 filetype사용
				String callerClassMethodName = com.irt.util.TraceHelper.getClassMethodName(com.irt.util.TraceHelper.CALLER);
				String envFileType = com.irt.rbm.RBMSystem.getSystemEnv("RBM", "DownFileType;" + callerClassMethodName, null);
				if( envFileType != null && envFileType.length() > 0 ) {
					fileType = envFileType;
				} else {
					if( com.irt.util.FileType.XLS.equals(defaultFileType) || com.irt.util.FileType.XLX.equals(defaultFileType)
							|| com.irt.util.FileType.XLF.equals(defaultFileType) )
						fileType = defaultFileType;
					else// 현재 파일 다운로드 방식이 sheetWriter를 사용한 방식과 SSDataWriter방식이 혼용되어 있어 XLS 파일타입을 최종 default로 지정( CSV, TAB등은 default로 받아들일 수 없음 )
						fileType = com.irt.util.FileType.XLS;
				}
			}
		}
		return fileType;
	}

	public static RequestDispatcher getJsonDispatcherInstance() {
		return JSON_DISPATCHER;
	}

	/**
	 * override default "Dispatcher.forward" behavior to json.
	 *
	 * if parameter has "&json=some-req-attr-name"
	 *
	 * then return json when RequestDispatcher is returning to forward.
	 *
	 * "some-req-attr-name" should be set before forward to jsp.
	 *
	 */
	public static HttpServletRequestWrapper getJsonProcessor( final HttpServletRequest request ) {
		HttpServletRequestWrapper JSON_PROCESSOR = new HttpServletRequestWrapper(request) {
			@Override
			public RequestDispatcher getRequestDispatcher( String path ) {
				String json = request.getParameter("json");
				if( json != null ) {
					return JSON_DISPATCHER;
				} else {
					return request.getRequestDispatcher(path);
				}
			}
		};

		return JSON_PROCESSOR;
	}

	public static String getLogConfigFilePath( HttpServletRequest request ) {
		String dbConfigFilename = RBMSystem.getSystemEnv("SYS", "LogConfig;configFilePath");
		String initConfigFilename = request.getServletContext().getInitParameter("logConfigFilePath");

		return ( dbConfigFilename == null || dbConfigFilename.length() <= 0 ? initConfigFilename : dbConfigFilename );
	}

	public static char getMandatoryType( PageConfig htmlpage, boolean readonly, boolean mandatory ) {
		char mandatoryType;
		if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION )
			mandatoryType = HtmlPage.INPUT_INFORMATION;
		else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_READONLY )
			mandatoryType = HtmlPage.INPUT_READONLY;
		else {
			if( readonly )
				mandatoryType = HtmlPage.INPUT_READONLY;
			else if( mandatory )
				mandatoryType = HtmlPage.INPUT_MANDATORY;
			else
				mandatoryType = HtmlPage.INPUT_OPTIONAL;

		}
		return mandatoryType;
	}

	public static String getStyleClass( PageConfig htmlpage, boolean mandatory ) {
		String styleClass;
		if( mandatory )
			styleClass = htmlpage.getProperty().getProperty("jsp.styleClass.title.mandatory");
		else
			styleClass = htmlpage.getProperty().getProperty("jsp.styleClass.title.optional");
		return styleClass;
	}

	public static String getUploadFileType( ServletRequest request, java.io.File uploadFile, String uploadFileName )
			throws ServletModelException, IOException {
		String fileType = null;

		if( uploadFileName != null ) {// can we trust user?
			String extension = RBMWorkbook.getFileExtension(uploadFileName);
			if( extension != null ) {
				if( "xls".equalsIgnoreCase(extension) ) {
					fileType = FileType.XLS;
				} else if( "xlsx".equalsIgnoreCase(extension) ) {
					fileType = FileType.XLX;
				}
				// 그외 'csv' 및 'tsv' extension은 csv이면서 tab을 delimiter로 쓰는 경우가 있을 수 있어 detect하지 않음.
			}
		}

		if( uploadFile == null ) {
			com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)request;
			if( ( uploadFile = req.getFile("file") ) == null ) {
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
			}
		}

		if( fileType == null && uploadFile != null ) {// detect binary magic byte
			java.io.FileInputStream fis = null;
			try {
				fis = new java.io.FileInputStream(uploadFile);
				fileType = com.irt.util.RBMWorkbook.getFileType(fis);
			} catch( IOException ioEx ) {
			} finally {
				if( fis != null )
					fis.close();
			}
		}

		if( fileType != null ) { // binary 경우 처리
			// binary가 xlsx이고 file size가 클 경우 xlf Process 수행
			if( com.irt.util.FileType.XLX.equals(fileType) ) {
				int disabledXLFProcess = -1;
				int xlfProcessEnableSize = com.irt.rbm.RBMSystem.getSystemEnvInt("RBM", "Default;xlfProcessEnableSize", disabledXLFProcess);
				if( xlfProcessEnableSize != disabledXLFProcess && uploadFile.length() > xlfProcessEnableSize )
					fileType = com.irt.util.FileType.XLF;
			}

			// SYSTEM_ENV에 정의된 filetype사용. uploadFileType을 call하는 class. (eg. 서블렛이름.upload )
			String callerClassMethodName = com.irt.util.TraceHelper.getClassMethodName(com.irt.util.TraceHelper.CALLER);
			String envFileType = com.irt.rbm.RBMSystem.getSystemEnv("RBM", "UploadFileType;" + callerClassMethodName, null);
			if( envFileType != null && envFileType.length() > 0 )
				fileType = envFileType;

		} else { // text 경우 처리
			String paramFileType = request.getParameter("fileType");
			if( paramFileType != null && paramFileType.length() > 0 )
				fileType = paramFileType;
			else
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		}

		return fileType;
	}

	protected static String getUploadLogURL( ServletRequest req, PageConfig pageConfig, DataLoader dataLoader, String classURL,
			String uploadInputPath ) {
		String backURL;

		if( uploadInputPath == null )
			backURL = pageConfig.getBackURL();
		else
			backURL = makeRedirectURL(req, pageConfig, classURL + "/" + uploadInputPath, pageConfig.getBackURL());

		String redirectURL = classURL + "/RBMUploadLog?" + ServletModel.PARAM_MODE + "=" + ServletModel.MODE_LIST + "&logId="
				+ dataLoader.getLoaderLogId();
		if( dataLoader.getDataResult().getErrorCount() > 0 )
			redirectURL += "&status=ER";

		return makeRedirectURL(req, pageConfig, redirectURL, backURL);
	}

	public static boolean isJsonRequired( ServletRequest req ) {
		String jsonAttrs = req.getParameter("json");
		if( jsonAttrs != null )
			return true;
		return false;
	}

	public static boolean isMandatory( com.irt.data.AbstractFieldSet fieldSet, String key ) {
		boolean mandatory = false;
		if( fieldSet != null && key != null ) {
			com.irt.data.AbstractField field = fieldSet.getField(key);
			if( field != null ) {
				if( field instanceof com.irt.data.ValidableField )
					mandatory = ( !( (com.irt.data.ValidableField)field ).nullable() );
			}
		}

		return mandatory;
	}

	public static boolean jsonResponse( ServletRequest req, ServletResponse res, String message, boolean useNullAsEmpty, String... reqObjKeys )
			throws IOException {
		res.setContentType("application/json; charset=utf-8");
		res.setCharacterEncoding("UTF-8");
		java.io.PrintWriter pw = res.getWriter();
		Map<String, Object> map = com.irt.data.Record.createMap("msg", message);
		if( reqObjKeys == null ) {
			reqObjKeys = parseJsonRequestAttr(req);
		}
		if( reqObjKeys != null ) {
			for( int i = 0; i < reqObjKeys.length; i++ ) {
				Object obj = req.getAttribute(reqObjKeys[i]);
				;
				if( obj == null ) {
					if( useNullAsEmpty ) {
						obj = "";
					} else {
						continue;
					}
				}
				map.put(reqObjKeys[i], obj);
			}
		}

		String jsonString = Jsoner.getNewInstance().toJson(map);

		pw.write(jsonString);
		pw.flush();
		pw.close();

		return true;
	}

	/**
	 * @deprecated use {@link ServletUtility2#jsonResponse(ServletRequest, ServletResponse, String, boolean, String...)}
	 */
	@Deprecated
	public static boolean jsonResponse( ServletResponse res, String message, boolean useNullAsEmpty, String[] reqObjKeys, Object... reqObjs )
			throws IOException {
		res.setContentType("application/json; charset=utf-8");
		res.setCharacterEncoding("UTF-8");
		java.io.PrintWriter pw = res.getWriter();
		Map<String, Object> map = com.irt.data.Record.createMap("msg", message);
		for( int i = 0; i < reqObjs.length; i++ ) {
			Object obj = reqObjs[i];
			if( obj == null ) {
				if( useNullAsEmpty ) {
					obj = "";
				} else {
					continue;
				}
			}
			map.put(reqObjKeys[i], obj);
		}

		String jsonString = Jsoner.getInstance().toJson(map);

		pw.write(jsonString);
		pw.flush();
		pw.close();

		return true;
	}

	public static String makeRedirectURL( ServletRequest req, PageConfig pageConfig, String redirectURL, String backURL ) {
		redirectURL = HtmlUtility.replaceURLQuery(redirectURL, ServletModel.PARAM_LOCALE, req.getParameter("locale"));
		if( pageConfig.getWindowType() != null && pageConfig.getWindowType().length() > 0 ) {
			redirectURL = HtmlUtility.replaceURLQuery(redirectURL, ServletModel.PARAM_WINTYPE, pageConfig.getWindowType());
		}
		if( pageConfig.getSystemMenu() != null && pageConfig.getSystemMenu().length() > 0 ) {
			redirectURL = HtmlUtility.replaceURLQuery(redirectURL, ServletModel.PARAM_MENU, pageConfig.getSystemMenu());
		} else {
			String menu = req.getParameter(ServletModel.PARAM_MENU);
			redirectURL = HtmlUtility.replaceURLQuery(redirectURL, ServletModel.PARAM_MENU, req.getParameter(ServletModel.PARAM_MENU));
		}
		if( backURL != null )
			redirectURL = HtmlUtility.replaceURLQuery(redirectURL, ServletModel.PARAM_URL, java.net.URLEncoder.encode(backURL));

		return redirectURL;
	}

	/**
	 * use with care.
	 */
	public static String[] parseJsonRequestAttr( ServletRequest req ) {
		String jsonAttrs = req.getParameter("json");
		if( jsonAttrs != null )
			return jsonAttrs.split(",");
		return null;
	}

	public static String parseLanguage( String headerAcceptLanguage, String[] supportLangCodes ) {
		if( headerAcceptLanguage == null )
			return null;

		String langCode = null;
		Double sofar = 0.0;

		List<String> supportLangs = new ArrayList<String>(java.util.Arrays.asList(supportLangCodes == null ? new String[] {} : supportLangCodes));

		for( String str : headerAcceptLanguage.split(",") ) {
			String[] arr = str.trim().replace("-", "_").split(";");

			// Parse the locale
			Locale locale = null;
			String[] l = arr[0].split("_");
			switch( l.length ) {
			case 2:
				locale = new Locale(l[0], l[1]);
				break;
			case 3:
				locale = new Locale(l[0], l[1], l[2]);
				break;
			default:
				locale = new Locale(l[0]);
				break;
			}

			// Parse the q-value
			Double q = 1.0D;
			for( String s : arr ) {
				s = s.trim();
				if( s.startsWith("q=") ) {
					q = Double.parseDouble(s.substring(2).trim());
					break;
				}
			}
			// Print the Locale and associated q-value
			// System.out.println(q + " - " + arr[0] + "\t " + locale.getLanguage());
			if( sofar < q ) {
				if( ( supportLangs.isEmpty() || supportLangs.contains(locale.getLanguage()) ) ) {
					sofar = q;
					langCode = locale.getLanguage();
				}
			}
		}

		return langCode;
	}

	public static Map<String, Object> parsePageMeta( HttpServletRequest request ) {
		String requestURI = request.getRequestURI();
		String[] paths = requestURI.split("/");
		String name = null;
		if( paths != null && paths.length > 0 ) {
			name = paths[paths.length - 1];
		}

		Map<String, Object> map = Record.createMap("sysid", name.substring(0, 3).toLowerCase());
		map.put("name", name);
		map.put("SYSID", name.substring(0, 3).toLowerCase());
		map.put("daoid", name.substring(3).toLowerCase());
		map.put("DAOID", name.substring(3).toUpperCase());
		map.put("tabid", map.get("sysid") + "-" + map.get("daoid"));
		return map;
	}

}
