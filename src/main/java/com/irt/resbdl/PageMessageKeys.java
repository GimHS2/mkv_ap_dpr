/*
 *	File Name:	PageMessageKeys.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.resbdl;

import com.irt.custom.SystemConfig;
import com.irt.html.HtmlPage;
import com.irt.json.Jsoner;
import com.irt.rbm.RBMSystem;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class PageMessageKeys {

	/** {pageId : [messageKey:messageVal] } */
	static Map<String, List<Map.Entry<String, String>>> pageMessageKeyVals = java.util.Collections
			.synchronizedMap(new java.util.HashMap<String, List<Map.Entry<String, String>>>());

	static Map<String, List<String>> pageColumnKeys = java.util.Collections
			.synchronizedMap(new java.util.HashMap<String, List<String>>());

	static Map<String, List<String>> pageColumnListNames = java.util.Collections
			.synchronizedMap(new java.util.HashMap<String, List<String>>());

	static Map<String, List<String>> requestPages = java.util.Collections
			.synchronizedMap(new java.util.HashMap<String, List<String>>());

	public static Set<String> getAllPageIds() {
		synchronized( pageMessageKeyVals ) {
			return pageMessageKeyVals.keySet();
		}
	}

	public static List<String> getAllPageIdsByReq( String reqId ) {
		synchronized( requestPages ) {
			return requestPages.get(reqId) == null ? new ArrayList<String>() : requestPages.get(reqId);
		}
	}

	public static Set<String> getAllReqIds() {
		synchronized( requestPages ) {
			return requestPages.keySet();
		}
	}

	public static List<String> getColumnKeys( HtmlPage htmlpage ) {
		synchronized( pageColumnKeys ) {
			Map<String, Object> map = getPageAsDataMap(htmlpage);

			return getColumnKeys((String)map.get("pageId"));
		}
	}

	public static List<String> getColumnKeys( String pageId ) {
		synchronized( pageColumnKeys ) {

			return pageColumnKeys.get(pageId);
		}
	}

	public static List<String> getColumnListNames( HtmlPage htmlpage ) {
		synchronized( pageColumnListNames ) {
			Map<String, Object> map = getPageAsDataMap(htmlpage);

			return getColumnListNames((String)map.get("pageId"));
		}
	}

	public static List<String> getColumnListNames( String pageId ) {
		synchronized( pageColumnListNames ) {

			return pageColumnListNames.get(pageId);
		}
	}

	private static List<Integer> getListFromIntegerArray( int... ints ) {
		List<Integer> list = new ArrayList<Integer>();
		for( Integer i : ints ) {
			list.add(i);
		}

		return list;
	}

	/**
	 * check usePageEdit is enabled.
	 * 
	 * if enabled then save the value to page key;
	 */
	public static String getMessageAndProcess( HtmlPage htmlpage, String messageKey ) {
		String messageValue = htmlpage.getMessageHandler().getMessage( messageKey );
		
		if( RBMSystem.getSystemEnvBool("SYS", "DatabaseResource;usePageEdit", false) ) {
			putMessageKey(htmlpage, messageKey, messageValue );
		}
		
		return messageValue;
	}

	public static List<Map.Entry<String, String>> getMessageKeys( HtmlPage htmlpage ) {
		synchronized( pageMessageKeyVals ) {
			Map<String, Object> map = getPageAsDataMap(htmlpage);

			return getMessageKeys((String)map.get("pageId"));
		}
	}

	public static List<Map.Entry<String, String>> getMessageKeys( String pageId ) {
		synchronized( pageMessageKeyVals ) {

			return pageMessageKeyVals.get(pageId);
		}
	}

	public static String getMessageKeysJson( String pageId ) {
		List<Map.Entry<String, String>> messageKeys = getMessageKeys(pageId);

		Jsoner jsoner = new Jsoner();
		return jsoner.toJson(messageKeys);
	}

	public static Map<String, Object> getPageAsDataMap( HtmlPage page ) {
		Map<String, Object> dataMap = new HashMap<String, Object>();

		dataMap.put("backURL", page.getBackURL());
		dataMap.put("focus", page.getFocus());
		dataMap.put("formName", page.getFormName());
		dataMap.put("inputStatus", page.getInputStatus());
		dataMap.put("locale", page.getLocale().toString());
		// dataMap.put("supportedLangs", getListFromStringArray(page.getSupportedLangs()));
		dataMap.put("message", page.getMessage());
		dataMap.put("mode", page.getMode());
		dataMap.put("property", page.getProperty());
		dataMap.put("requestURL", page.getRequestURL());
		dataMap.put("styleSheetNames", java.util.Arrays.asList(page.getStyleSheetNames()));
		dataMap.put("systemMenu", page.getSystemMenu());
		dataMap.put("title", page.getTitle());
		// dataMap.put("userAgentString", page.getUserAgentString());

		// String uniq = getUniqFromSha1(dataMap.toString());
		String uniq = String.valueOf(dataMap.toString().hashCode());
		dataMap.put("pageId", uniq.toString());
		for( String key : dataMap.keySet() ) {
			// Logger.getRootLogger().warn(key + " : " + dataMap.get(key));
		}

		// unrelated to pageId
		dataMap.put("listIndexVariables", getListFromIntegerArray(page.getListIndexVariables()));

		return dataMap;
	}

	public static String getPageId( HtmlPage htmlpage ) {
		return (String)getPageAsDataMap(htmlpage).get("pageId");
	}

	public static Map<String, Object> getRequestAsDataMap( HttpServletRequest req ) {
		Map<String, Object> dataMap = new HashMap<String, Object>();

		if( req != null ) {
			Jsoner jsoner = new Jsoner();
			dataMap.put("parameterMap", jsoner.toJson(req.getParameterMap()));
			dataMap.put("requestURL", req.getRequestURL());

			String uniq = String.valueOf(dataMap.toString().hashCode());
			dataMap.put("reqId", uniq);
			return dataMap;

		}

		return null;
	}

	public static String getRequestId( HttpServletRequest req ) {
		return (String)getRequestAsDataMap(req).get("reqId");
	}

	public static void putColumnKey( HtmlPage htmlpage, String messageKey ) {
		synchronized( pageColumnKeys ) {
			Map<String, Object> map = getPageAsDataMap(htmlpage);

			putColumnKey((String)map.get("pageId"), messageKey);
		}
	}

	public static void putColumnKey( String pageId, String messageKey ) {
		synchronized( pageColumnKeys ) {
			List<String> messageKeys = pageColumnKeys.get(pageId);
			if( messageKeys == null )
				messageKeys = new ArrayList<String>();

			if( !messageKeys.contains(messageKey) ) {
				messageKeys.add(messageKey);
			}
			pageColumnKeys.put(pageId, messageKeys);
		}
	}

	public static void putColumnListName( HtmlPage htmlpage, String messageKey ) {
		synchronized( pageColumnListNames ) {
			Map<String, Object> map = getPageAsDataMap(htmlpage);

			putColumnListName((String)map.get("pageId"), messageKey);
		}
	}

	public static void putColumnListName( String pageId, String messageKey ) {
		synchronized( pageColumnListNames ) {
			List<String> messageKeys = pageColumnListNames.get(pageId);
			if( messageKeys == null )
				messageKeys = new ArrayList<String>();

			if( !messageKeys.contains(messageKey) ) {
				messageKeys.add(messageKey);
			}
			pageColumnListNames.put(pageId, messageKeys);
		}
	}

	public static void putMessageKey( HtmlPage htmlpage, String messageKey, String messageVal ) {
		synchronized( pageMessageKeyVals ) {
			Map<String, Object> map = getPageAsDataMap(htmlpage);

			putMessageKey((String)map.get("pageId"), messageKey, messageVal);
		}
	}

	public static void putMessageKey( String pageId, String messageKey, String messageVal ) {
		synchronized( pageMessageKeyVals ) {
			List<Map.Entry<String, String>> messageKeys = pageMessageKeyVals.get(pageId);
			if( messageKeys == null )
				messageKeys = new ArrayList<Map.Entry<String, String>>();

			if( !messageKeys.contains(messageKey) ) {
				messageKeys.add(new SimpleImmutableEntry(messageKey, messageVal));
			}
			pageMessageKeyVals.put(pageId, messageKeys);
		}
	}

	public static void putPage( HttpServletRequest req, String pageId ) {
		synchronized( requestPages ) {
			Map dataMap = getRequestAsDataMap(req);
			if( dataMap != null ) {
				String requestId = (String)dataMap.get("reqId");
				List<String> pages = requestPages.get(requestId);
				if( pages == null )
					pages = new ArrayList<String>();

				if( !pages.contains(pageId) ) {
					pages.add(pageId);
				}

				requestPages.put(requestId, pages);
			}
		}
	}

	private Map<String, Object> pageMap;

	private SystemConfig systemConfig;

	private HtmlPage htmlpage;

	public PageMessageKeys( SystemConfig systemConfig, HtmlPage htmlpage ) {
		this.systemConfig = systemConfig;
		this.htmlpage = htmlpage;
		this.pageMap = getPageAsDataMap(htmlpage);
	}

	private List<Integer> getListFromIntegerArray( Integer... ints ) {
		return java.util.Arrays.asList(ints);
	}

	private List<String> getListFromStringArray( String... strs ) {
		return java.util.Arrays.asList(strs);
	}

}
