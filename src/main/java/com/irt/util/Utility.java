/*
 *	File Name:	Utility.java
 *	Version:	2.2.6c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/02/29		2.2.6c	sendMail() : smtp.ssl.trust, smtp.starttls.enable, mail.smtp.ssl.protocols 속성 추가
 *	hankalam	2021/11/30		2.2.5c	convertListToJson(), convertMapToJson() 추가, sendMail(): 메일전송시 프로토콜을 찾지못하는 오류 수정
 *	hankalam	2020/09/29		2.2.5	sendMail(): id 가 null 일때 smtp.auth false 속성 추가
 *	GimHS		2018/06/29		2.2.4	sendMail() 추가
 *	jbaek		2018/10/30		2.2.2c	기능 삭제 정리. setJvmSystemEnv, getSystemEnv 추가
 *	jbaek		2017/02/28		2.2.2c	path관련 method추가.
 *	lsinji		2008/09/26		2.2.1	simpleConvertDateString() 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 */
public class Utility {

	@SuppressWarnings("rawtypes")
	public static Properties extractProperties( Properties properties, String prefix ) {
		int length = prefix.length();

		Properties prop = new java.util.Properties();
		for( java.util.Iterator iterator = properties.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String key = (String)entry.getKey();

			if( key.startsWith(prefix) ) {
				iterator.remove();
				prop.put(key.substring(length), entry.getValue());
			}
		}

		return ( prop.size() > 0 ? prop : null );
	}

	@SuppressWarnings("rawtypes")
	public static Map<String, Properties> extractPropertiesMap( Properties properties, String prefix ) {
		int length = prefix.length();

		Map<String, Properties> propertyMap = new java.util.HashMap<String, Properties>();
		for( java.util.Iterator iterator = properties.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String key = (String)entry.getKey();

			if( key.startsWith(prefix) ) {
				iterator.remove();

				key = key.substring(length);
				int index = key.indexOf('.');

				String name = key.substring(0, index);
				key = key.substring(index + 1);

				Properties options = propertyMap.get(name);
				if( options == null )
					propertyMap.put(name, options = new Properties());
				options.put(key, entry.getValue());
			}
		}

		return ( propertyMap.size() > 0 ? propertyMap : null );
	}

	@SuppressWarnings("unchecked")
	public static JSONArray convertListToJson( List<Map<String, Object>> list ) {
		JSONArray jsonArray = new JSONArray();
		for( Map<String, Object> map : list ) {
			jsonArray.add(convertMapToJson(map));
		}
		return jsonArray;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject convertMapToJson( Map<String, Object> map ) {
		JSONObject json = new JSONObject();
		for( Map.Entry<String, Object> entry : map.entrySet() ) {
			String key = entry.getKey();
			Object value = entry.getValue();
			json.put( key, value );
		}
		return json;
	}

	/**
	 * SMTP 서버를 이용하여 메일 전송
	 *
	 * @param sendMailMap
	 *            key : smtpHost, port, hostId, hostPw, fromAddress, toAddress, subject, contents
	 * @return 메일전송여부 true, false
	 */
	public static boolean sendMail( Map<String, Object> sendMailMap, org.apache.log4j.Logger logger ) {
		String host = (String)sendMailMap.get("host");
		String id = (String)sendMailMap.get("id");
		String pw = (String)sendMailMap.get("pw");
		String port = (String)sendMailMap.get("port");
		if( port == null )
			port = "25";

		Properties mailProperties = new Properties();
		mailProperties.put("mail.transport.protocol", "smtp");
		mailProperties.put("mail.smtp.port", port);
		boolean smtpAuth = id == null || id.length() == 0 ? false : true;
		mailProperties.put("mail.smtp.auth", smtpAuth);
		mailProperties.put( "mail.smtp.starttls.enable", true );
		mailProperties.put( "mail.smtp.ssl.trust", "smtp.kenvue.com" );
		mailProperties.put( "mail.smtp.ssl.protocols", "TLSv1.2" );

		Session session = Session.getDefaultInstance(mailProperties);
		// session.setDebug( true );

		Message message = new MimeMessage(session);
		try {
			String toAdd_str = "";
			InternetAddress[] toAdd;
			Object toAddObj = sendMailMap.get("toAddress");
			if( toAddObj instanceof String[] ) {
				String[] strs = (String[])toAddObj;
				toAdd = new InternetAddress[strs.length];
				for( int idx = 0; idx < strs.length; idx++ ) {
					toAdd[idx] = new InternetAddress(strs[idx]);
					toAdd_str += ";" + strs[idx];
				}
			} else {
				toAdd = new InternetAddress[] { new InternetAddress(toAddObj.toString()) };
				toAdd_str += ";" + toAddObj.toString();
			}

			message.setSubject(MimeUtility.encodeText((String)sendMailMap.get("subject"), "UTF-8", "B"));
			message.setFrom(new InternetAddress((String)sendMailMap.get("fromAddress")));
			message.setRecipients(Message.RecipientType.TO, toAdd);
			message.setContent(sendMailMap.get("contents"), "text/html; charset=utf-8");
			message.setSentDate(new java.util.Date());


			String protocol = "smtp";
			Provider p = session.getProvider( protocol );
			Transport transport = session.getTransport( p );
			transport.connect(host, id, pw);
			transport.sendMessage(message, message.getAllRecipients());

			logger.info("SendMail completed. (Title: " + sendMailMap.get("subject") + ", Recipient: " + toAdd_str.substring(1));

			return true;
		} catch( MessagingException msgEx ) {
			logger.error("SendMail error.", msgEx);
		} catch( UnsupportedEncodingException ueEx ) {
			logger.error("SendMail error.", ueEx);
		}

		return false;
	}

	public static String simpleConvertDateString( String dateValue ) {
		String DEFAULT_DATE_TOKEN = "-";
		String[] DATE_TOKENS = new String[] { ":", "-", "/" };

		if( dateValue == null )
			return null;
		else {
			switch( dateValue.length() ) {
			case 10:
				for( int n = 0; n < DATE_TOKENS.length; n++ ) {
					String[] dateValues = dateValue.split(DATE_TOKENS[n]);

					if( dateValues[0].length() == 2 )
						return dateValues[2] + DEFAULT_DATE_TOKEN + dateValues[1] + DEFAULT_DATE_TOKEN + dateValues[0];
					else if( dateValues[0].length() == 4 )
						return dateValues[0] + DEFAULT_DATE_TOKEN + dateValues[1] + DEFAULT_DATE_TOKEN + dateValues[2];
				}
			case 8:
				return dateValue.substring(0, 4) + DEFAULT_DATE_TOKEN + dateValue.substring(4, 6) + DEFAULT_DATE_TOKEN + dateValue.substring(6, 8);
			default:
				return null;
			}
		}
	}

	public static boolean isValidateFile( File file ) {
		try {
			if( file == null )
				return false;

			File canonicalFile = file.getCanonicalFile();
			String canonicalPath = canonicalFile.getPath();
			String absolutePath = file.getAbsolutePath();

			if( canonicalPath == null || absolutePath == null || !canonicalPath.equals(new File(absolutePath).getCanonicalPath())
					|| !isSafeFilePath(canonicalPath) || !isSafeFilePath(absolutePath) )
				return false;

			return true;
			// return ( canonicalFile.exists() && (canonicalFile.isFile() || canonicalFile.isDirectory()) );
		} catch( Exception ex ) {
			return false;
		}
	}

	public static boolean isSafeClassPath( String classPath ) {
		if( classPath == null )
			return false;

		String normalized = classPath.trim();
		if( normalized.length() == 0 )
			return false;

		return normalized.indexOf('\0') < 0
				&& normalized.indexOf("..") < 0
				&& normalized.indexOf('\\') < 0
				&& normalized.matches("^[A-Za-z0-9_-]{1,128}$");
	}

	public static boolean isSafeFilePath( String path ) {
		if( path == null )
			return false;

		String normalized = path.trim();
		if( normalized.length() == 0 )
			return false;

		return normalized.indexOf('\0') < 0
				&& normalized.indexOf("..") < 0;
	}

	public static boolean isSafeFileName( String fileName ) {
		if( fileName == null )
			return false;

		String normalized = fileName.trim();
		if( normalized.length() == 0 )
			return false;

		return normalized.indexOf('\0') < 0
				&& normalized.indexOf("..") < 0
				&& normalized.indexOf('/') < 0
				&& normalized.indexOf('\\') < 0
				&& normalized.matches("^[A-Za-z0-9_-]{1,128}$");
	}

	public static boolean isSafeFile( String path, String fileName ) {
		if( !isSafeFilePath(path) || !isSafeFileName(fileName) )
			return false;

		File baseDir, serverFile;
		try {
			baseDir = new File( path ).getCanonicalFile();
			serverFile = new File( baseDir, fileName ).getCanonicalFile();
			String basePath = baseDir.getPath();
			String targetPath = serverFile.getPath();
			if( !( targetPath.equals(basePath) || targetPath.startsWith(basePath + File.separator) || isValidateFile(serverFile) ) )
				return false;
		} catch( IOException ex ) {
			throw new IllegalArgumentException( ex );
		}

		return true;
	}

	public static String normalizeSortKey( String sortKey ) {
		if( sortKey == null ) return null;

		String normalized = sortKey.trim();
		if( normalized.length() == 0 ) return null;

		String[] keys = normalized.split( "#", 2 );
		if( !keys[0].matches("[A-Za-z0-9_]+") ) return null;

		if( keys.length < 2 ) return keys[0];

		if( "DESC".equalsIgnoreCase(keys[1]) )
			return keys[0] + "#DESC";
		else if( "ASC".equalsIgnoreCase(keys[1]) )
			return keys[0] + "#ASC";

		return null;
	}
}
