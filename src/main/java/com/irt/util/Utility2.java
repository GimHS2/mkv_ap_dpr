/*
 *	File Name:	Utility2.java
 *	Version:	2.2.4c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	flexyduck	2025/01/31		2.2.4c	sendMail() : 사용자 이메일에 세미클론같은 특수문자를 쉼표(,)로 구분되도록 변경
 *	dudwls3720	2024/08/21		2.2.3c	sendMail() : 사용자 이메일에 연속적인 콤마나 세미클론 들어올 경우 콤마로 구분되도록 변경
 *	dudwls3720	2024/07/31		2.2.2c	sendMail() : smtpAuth가 false일 경우 ID,PW 인증 없이 메일 서버 연결하도록 변경
 *	dudwls3720	2024/02/29		2.2.1c	sendMail() : smtp.ssl.trust, smtp.starttls.enable, mail.smtp.ssl.protocols 속성 추가
 *	jbaek		2020/06/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.irt.data.DataException;
import com.irt.data.Field;
import com.irt.data.FieldException;
import com.irt.data.ValidableField;
import com.irt.data.ValidableFieldSet;
import com.irt.rbm.RBMSystem;

public class Utility2 {//@formatter:on

	/**
	 * init log4j by file name 'log4j.properties' or 'log4j2.xml'
	 *
	 * @param log4jFilenamePath: full file path
	 */
	public static void configureLog4j2Engine( String log4jFilenamePath ) {
		if( log4jFilenamePath == null ) {
			final LoggerContext loggerContext = (LoggerContext)LogManager.getContext(false);
			final Configuration loggerContextConfiguration = loggerContext.getConfiguration();
			final PatternLayout layout = PatternLayout.createDefaultLayout();
			final Appender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
			appender.start();
			loggerContextConfiguration.addAppender(appender);
			for( LoggerConfig lc : loggerContextConfiguration.getLoggers().values() ) {
				lc.addAppender(appender, Level.DEBUG, null);
			}
			loggerContext.updateLoggers();
			return;
		}

		String log4jFilename = LocationUtil.basename(log4jFilenamePath, File.separator);
		if( "log4j2.xml".equals(log4jFilename) ) {
			LoggerContext logCtx = (LoggerContext)org.apache.logging.log4j.LogManager.getContext(false);

			File log4jFile = new File(log4jFilenamePath);
			if( log4jFile != null )
				logCtx.setConfigLocation(log4jFile.toURI());
		} else if( "log4j.properties".equals(log4jFilename) ) {
			System.out.println("[ERROR] log4j2 does not support legacy log4j 1.x based log4j.properties file.('" + log4jFilename + "')");
			System.err.println("[ERROR] log4j2 does not support legacy log4j 1.x based log4j.properties file.('" + log4jFilename + "')");
		}
	}

	public static String fileGetName( String filepath ) {
		if( filepath == null || filepath.length() == 0 )
			return "";
		filepath = filepath.replaceAll("[/\\\\]+", "/");
		int len = filepath.length(), upCount = 0;
		while( len > 0 ) {
			// remove trailing separator
			if( filepath.charAt(len - 1) == '/' ) {
				len--;
				if( len == 0 )
					return "";
			}
			int lastInd = filepath.lastIndexOf('/', len - 1);
			String fileName = filepath.substring(lastInd + 1, len);
			if( fileName.equals(".") ) {
				len--;
			} else if( fileName.equals("..") ) {
				len -= 2;
				upCount++;
			} else {
				if( upCount == 0 )
					return fileName;
				upCount--;
				len -= fileName.length();
			}
		}
		return "";
	}

	public static String[] fromCsv( String csv ) {
		if( csv != null ) {
//			StringBuffer sbuf = new StringBuffer();
//			for( Object o : strList ) {
//				if( o != null ) {
//					sbuf.append(o);
//					sbuf.append(",");
//				}
//			}
//			sbuf.deleteCharAt(sbuf.length()-1);
//			return sbuf.toString();
			return csv.split(",\\s+?");
		}
		return new String[0];
	}

	public static Map<String, InputStream> getAttachments( BodyPart part ) throws Exception {
		// List<InputStream> result = new ArrayList<InputStream>();
		Map<String, InputStream> result = new HashMap<String, InputStream>();
		Object content = part.getContent();
		if( content instanceof InputStream || content instanceof String ) {
			if( Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || ( part.getFileName() != null && part.getFileName().length() > 0 ) ) {
				String fileName = part.getFileName();
				result.put(fileName, part.getInputStream());
				return result;
			} else {
				return new HashMap<String, InputStream>();
			}
		}

		if( content instanceof Multipart ) {
			Multipart multipart = (Multipart)content;
			for( int i = 0; i < multipart.getCount(); i++ ) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				for( String key : getAttachments(bodyPart).keySet() ) {
					result.putAll(getAttachments(bodyPart));
				}
			}
		}
		return result;
	}

	public static Map<String, InputStream> getAttachments( Message message ) throws Exception {
		Object content = message.getContent();
		if( content instanceof String )
			return null;

		if( content instanceof Multipart ) {
			Multipart multipart = (Multipart)content;
			Map<String, InputStream> result = new HashMap<String, InputStream>();

			for( int i = 0; i < multipart.getCount(); i++ ) {
				result.putAll(getAttachments(multipart.getBodyPart(i)));
			}
			return result;

		}
		return null;
	}

	/**
	 * @param systemCode
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 * @param key
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 * @return property from JVM System or null.
	 */
	public static String getJvmSystemEnv( String systemCode, String key ) {
		return getJvmSystemEnv(systemCode, key, null);
	}

	/**
	 * @param systemCode
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 * @param key
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 * @param defaltValue
	 * @return property from JVM System or defaultValue
	 */
	public static String getJvmSystemEnv( String systemCode, String key, String defaultValue ) {
		if( key != null && key.contains(";") ) {
			return System.getProperty(systemCode + "." + key.replace(";", "."));
		}
		return defaultValue;
	}

	/**
	 * This method gets the network name of the machine we are running on.
	 * Returns "UNKNOWN_LOCALHOST" in the unlikely case where the host name
	 * cannot be found.
	 *
	 * @return String the name of the local host
	 */
	public static String getLocalHostname() {
		try {
			final InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostName();
		} catch( final UnknownHostException uhe ) {
			try {
				final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while( interfaces.hasMoreElements() ) {
					final NetworkInterface nic = interfaces.nextElement();
					final Enumeration<InetAddress> addresses = nic.getInetAddresses();
					while( addresses.hasMoreElements() ) {
						final InetAddress address = addresses.nextElement();
						if( !address.isLoopbackAddress() ) {
							final String hostname = address.getHostName();
							if( hostname != null ) {
								return hostname;
							}
						}
					}
				}
			} catch( final SocketException se ) {
				return "UNKNOWN_LOCALHOST";
			}
			return "UNKNOWN_LOCALHOST";
		}
	}

	public static String getLocalHostname( String defaultHostname ) {
		return ( "UNKNOWN_LOCALHOST".equals(getLocalHostname()) ? defaultHostname : getLocalHostname() );
	}

	public static <T> List<T> listIntersect( Collection<T> list1, Collection<T> list2 ) {
		List<T> result = new ArrayList<T>();
		Set<T> set2 = new HashSet<T>(list2);
		for( T t1 : list1 ) {
			if( set2.contains(t1) ) {
				result.add(t1);
			}
		}
		return result;
	}

	public static <T> List<T> listSubtract( Collection<T> list1, Collection<T> list2 ) {
		List<T> result = new ArrayList<T>();
		Set<T> set2 = new HashSet<T>(list2);
		for( T t1 : list1 ) {
			if( !set2.contains(t1) ) {
				result.add(t1);
			}
		}
		return result;
	}

	public static Map<String, List<String>> parseUrlQuery( URL url ) throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		final String[] pairs = url.getQuery().split("&");
		for( String pair : pairs ) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if( !query_pairs.containsKey(key) ) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
	}

	public static Message sendMail( Map<String, Object> sendMailMap, org.apache.log4j.Logger logger, File... attachments ) throws DataException {
		MimeBodyPart[] attachBodyParts = new MimeBodyPart[attachments.length];
		String[] attachNames = (String[])sendMailMap.get("attachNames");
		for( int i = 0; i < attachments.length; i++ ) {
			File attachFile = attachments[i];

			if( attachFile != null && attachFile.exists() ) {
				MimeBodyPart attachment = new MimeBodyPart();
				DataSource source = new FileDataSource(attachFile);
				String attachName = null;
				if( attachNames != null && attachNames[i] != null ) {
					attachName = attachNames[i];
				} else {
					attachName = attachFile.getName();
				}

				try {
					attachment.setFileName(attachName);
					attachment.setDataHandler(new DataHandler(source));
					attachBodyParts[i] = attachment;
				} catch( MessagingException msgEx ) {
					logger.error("SendMail attach error.(" + attachFile.getAbsolutePath() + ")", msgEx);
				}
			}
		}

		return sendMail(sendMailMap, logger, attachBodyParts);
	}

	public static Message sendMail( Map<String, Object> sendMailMap, org.apache.log4j.Logger logger, MimeBodyPart... attachments )
			throws DataException {
		ValidableFieldSet sendMailFieldSet = new ValidableFieldSet(new ValidableField[] {
				new ValidableField(false, "host", "SMTP_HOST", ValidableField.TYPE_STRING),
				new ValidableField(false, "subject", "MAIL_SUBJECT", ValidableField.TYPE_STRING),
				new ValidableField(false, "content", "MAIL_CONTENT", ValidableField.TYPE_STRING),
				new ValidableField(false, "fromAddress", "MAIL_FROM_ADDRESS", ValidableField.TYPE_STRING),
				new ValidableField(false, "toAddress", "MAIL_TO_ADDRESS_CSV", ValidableField.TYPE_STRING),
				new ValidableField(true, "ccAddress", "MAIL_CC_ADDRESS_CSV", ValidableField.TYPE_STRING),
				new ValidableField(true, "bccAddress", "MAIL_BCC_ADDRESS_CSV", ValidableField.TYPE_STRING),
				new ValidableField(true, "port", "SMTP_PORT", ValidableField.TYPE_STRING),
				new ValidableField(true, "id", "SMTP_USERID", ValidableField.TYPE_STRING),
				new ValidableField(true, "pw", "SMTP_PASSWORD", ValidableField.TYPE_STRING),
				new ValidableField(true, "smtpDebug", "SMTP_DEBUG_TF", "", "true,false"),
				new ValidableField(true, "mail.smtp.auth", "SMTP_AUTH_TF", "", "true,false"),
		});

		try {
			sendMailFieldSet.validate(sendMailMap);
		} catch( FieldException fEx ) {
			throw new DataException(fEx.getErrorKey(), fEx.getMessage() + "(" + fEx.getErrorField().getFieldKey() + ")", fEx);
		}

		String host = (String)sendMailMap.get("host");
		String id = (String)sendMailMap.get("id");
		String pw = (String)sendMailMap.get("pw");
		String port = (String)sendMailMap.get("port");
		if( port == null )
			port = "25";

		Properties mailProperties = new Properties();
		mailProperties.put("mail.transport.protocol", "smtp");
		mailProperties.put("mail.transport.protocol.rfc822", "smtp");
		mailProperties.put("mail.smtp.port", port);
		String smtpAuth = ( sendMailMap.get("mail.smtp.auth") == null
				? ( ( id == null || id.length() == 0 ) ? "false" : "true" )
				: (String)sendMailMap.get("mail.smtp.auth") );
		mailProperties.put("mail.smtp.auth", smtpAuth);
		mailProperties.put( "mail.smtp.starttls.enable", true );
		mailProperties.put( "mail.smtp.ssl.trust", "smtp.kenvue.com" );
		mailProperties.put( "mail.smtp.ssl.protocols", "TLSv1.2" );

		///** {@link javax.mail.Session#getDefaultInstance} */
		//String[] runtimeMailPropKeys = {"mail.store.protocol", "mail.transport.protocol", "mail.host", "mail.user", "mail.from"};
		Session session = Session.getDefaultInstance(mailProperties);
		if( !session.getDebug() && RBMSystem.getSystemEnvBool("SYS", "DebugOption;smtpDebug", false) ) {
			session.setDebug(true);
		}
		if( sendMailMap.get("smtpDebug") != null
				&& ( "true".equalsIgnoreCase((String)sendMailMap.get("smtpDebug")) ) ) {
			// session.setDebugOut(out);
			session.setDebug(true);// if debugOut is not set then it prints to System.out( usually tomcat catalina.out ).
		}

		Message message = new MimeMessage(session);
		Transport transport = null;
		try {
			String toAdd_csv = (String)sendMailMap.get("toAddress");
			if( toAdd_csv != null && toAdd_csv.length() > 0 )
				toAdd_csv = toAdd_csv.trim().replaceAll("\\s+", ",").replaceAll(";", ",").replaceAll(",+", ",").replaceAll("，", ",").replaceAll("；",",").replaceAll("^,", "").replaceAll(",$", "");
			String[] toAdd_strs = toAdd_csv.split(",\\s?");
			InternetAddress[] toAdd = new InternetAddress[toAdd_strs.length];
			for( int idx = 0; idx < toAdd_strs.length; idx++ )
				toAdd[idx] = new InternetAddress(toAdd_strs[idx]);
			message.setRecipients(Message.RecipientType.TO, toAdd);
			String ccAdd_csv = (String)sendMailMap.get("ccAddress");
			if( ccAdd_csv != null ) {
				String[] ccAdd_strs = ccAdd_csv.split(",\\s?");
				InternetAddress[] ccAdd = new InternetAddress[ccAdd_strs.length];
				for( int idx = 0; idx < ccAdd_strs.length; idx++ )
					ccAdd[idx] = new InternetAddress(ccAdd_strs[idx]);
				message.setRecipients(Message.RecipientType.CC, ccAdd);
			}
			String bccAdd_csv = (String)sendMailMap.get("bccAddress");
			if( bccAdd_csv != null ) {
				String[] bccAdd_strs = bccAdd_csv.split(",\\s?");
				InternetAddress[] bccAdd = new InternetAddress[bccAdd_strs.length];
				for( int idx = 0; idx < bccAdd_strs.length; idx++ )
					bccAdd[idx] = new InternetAddress(bccAdd_strs[idx]);
				message.setRecipients(Message.RecipientType.BCC, bccAdd);
			}
			message.setSubject(MimeUtility.encodeText((String)sendMailMap.get("subject"), "UTF-8", "B"));
			message.setFrom(new InternetAddress((String)sendMailMap.get("fromAddress")));

			transport = session.getTransport(mailProperties.getProperty("mail.transport.protocol"));
			if( Boolean.parseBoolean(smtpAuth) ) {
				transport.connect(host, id, pw);
			} else {
				transport.connect(host, "", "");
			}

			Multipart multipart = new MimeMultipart();
			MimeBodyPart mailContentHtmlBodyPart = new MimeBodyPart();
			mailContentHtmlBodyPart.setContent(sendMailMap.get("content"), "text/html; charset=utf-8");
			multipart.addBodyPart(mailContentHtmlBodyPart);
			for( MimeBodyPart attach : attachments ) {
				if( attach != null )
					multipart.addBodyPart(attach);
			}
			message.setContent(multipart);

			try {
				message.setSentDate(new java.util.Date());
				transport.sendMessage(message, message.getAllRecipients());
			} catch( MessagingException msgEx ) {
				message.setSentDate(null);
				throw msgEx;
			}

			logger.info("SendMail completed. (Title: " + sendMailMap.get("subject")
				+ ", Recipient: " + toAdd_csv + ", From: "+ sendMailMap.get("fromAddress") + ")");

			return message;
		} catch( MessagingException msgEx ) {
			sendMailMap.remove("pw");
			logger.error("SendMail error. sendMailMap:" + sendMailMap, msgEx);
		} catch( UnsupportedEncodingException ueEx ) {
			sendMailMap.remove("pw");
			logger.error("SendMail error. sendMailMap:" + sendMailMap, ueEx);
		} finally {
			try {
				if( transport != null )
					transport.close();
			} catch( MessagingException msgEx ) {
				sendMailMap.remove("pw");
				logger.error("SendMail transport close error. sendMailMap:" + sendMailMap, msgEx);
			}
		}

		return message;
	}

	/**
	 * @param systemCode
	 *            : same param as 'RBMSystem.setSystemEnv( systemCode, key );
	 * @param key
	 *            : same param as 'RBMSystem.setSystemEnv( systemCode, key );
	 * @param value
	 *            : value will be applied only to JVM( not RBMSystem )
	 * @return beforeValue
	 */
	public static String setJvmSystemEnv( String systemCode, String key, String value ) {
		String beforeValue = null;
		if( key != null && key.contains(";") ) {
			String jvmkey = systemCode + "." + key.replace(";", ".");
			beforeValue = System.getProperty(jvmkey);
			System.setProperty(jvmkey, value);
			return beforeValue;
		}

		return beforeValue;
	}

	public static Map<String, List<String>> splitQuery( URL url ) throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		final String[] pairs = url.getQuery().split("&");
		for( String pair : pairs ) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if( !query_pairs.containsKey(key) ) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
	}

	public static String toCsv( List<Object> strList ) {
		if( strList != null ) {
//			StringBuffer sbuf = new StringBuffer();
//			for( Object o : strList ) {
//				if( o != null ) {
//					sbuf.append(o);
//					sbuf.append(",");
//				}
//			}
//			sbuf.deleteCharAt(sbuf.length()-1);
//			return sbuf.toString();
			return strList.toString().replaceFirst("^\\[", "").replaceAll("\\]$", "");
		}
		return "";
	}

	public static boolean validateEmailCsv( String emails ) {
		try {
			InternetAddress[] addrs = javax.mail.internet.InternetAddress.parse(emails, true);
			if( addrs != null ) {
				for( InternetAddress addr : addrs )
					addr.validate();
			}
			return true;
		} catch( AddressException addrEx ) {
			return false;
		}
	}

	public static class DataField {
		public static com.irt.data.Date toDate( Object object ) throws DataException {
			if( object == null || ( object instanceof String && ( (String)object ).length() == 0 ) )
				throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL,
						com.irt.data.Record.createMap("stacktrace", TraceHelper.formatCurrentStacktrace()));

			try {
				return (com.irt.data.Date)new Field(com.irt.sql.Table.Field.TYPE_DATE, object.toString())
						.convertObject(object);
			} catch( FieldException fdEx ) {
				throw new DataException(fdEx.getErrorKey(), fdEx.getMessage(), fdEx);
			}
		}

		public static Integer toInteger( Object object ) throws DataException {
			if( object == null || ( object instanceof String && ( (String)object ).length() == 0 ) )
				throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL,
						com.irt.data.Record.createMap("stacktrace", TraceHelper.formatCurrentStacktrace()));

			try {
				Object converted = new Field(com.irt.sql.Table.Field.TYPE_INTEGER, object.toString())
						.convertObject(object);
				if( converted instanceof java.math.BigDecimal ) {
					return ( (java.math.BigDecimal)converted ).intValue();
				} else {
					return (Integer)converted;
				}
			} catch( FieldException fdEx ) {
				throw new DataException(fdEx.getErrorKey(), fdEx.getMessage(), fdEx);
			}
		}

		public static Integer toInteger( Map<String, Object> map, String key ) throws DataException {
			if( map == null ) {
				throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL,
						com.irt.data.Record.createMap("stacktrace", TraceHelper.formatCurrentStacktrace()));
			}

			Object object = map.get(key);
			if( object == null || ( object instanceof String && ( (String)object ).length() == 0 ) )
				throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL
						+ " key: " + key + " map: " + map, map);

			try {
				Object converted = new Field(com.irt.sql.Table.Field.TYPE_INTEGER, object.toString())
						.convertObject(object);
				if( converted instanceof java.math.BigDecimal ) {
					return ( (java.math.BigDecimal)converted ).intValue();
				} else {
					return (Integer)converted;
				}
			} catch( FieldException fdEx ) {
				throw new DataException(fdEx.getErrorKey(), fdEx.getMessage(), fdEx);
			}
		}

		public static Integer toInteger( Map<String, Object> map, String key, Integer defaultValue ) throws DataException {
			if( map == null ) {
				throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL,
						com.irt.data.Record.createMap("stacktrace", TraceHelper.formatCurrentStacktrace()));
			}

			Object object = map.get(key);
			if( object == null || ( object instanceof String && ( (String)object ).length() == 0 ) )
				return defaultValue;

			try {
				Object converted = new Field(com.irt.sql.Table.Field.TYPE_INTEGER, object.toString())
						.convertObject(object);
				if( converted instanceof java.math.BigDecimal ) {
					return ( (java.math.BigDecimal)converted ).intValue();
				} else {
					return (Integer)converted;
				}
			} catch( FieldException fdEx ) {
				throw new DataException(fdEx.getErrorKey(), fdEx.getMessage(), fdEx);
			}
		}

		public static Integer toInteger( Object object, Object defaultValue ) throws DataException {
			if( defaultValue == null && ( object == null || ( object instanceof String && ( (String)object ).length() == 0 ) ) )
				throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL,
						com.irt.data.Record.createMap("stacktrace", TraceHelper.formatCurrentStacktrace()));

			try {
				Object converted = new Field(com.irt.sql.Table.Field.TYPE_INTEGER, ( object == null ? defaultValue : object ).toString())
						.convertObject(( object == null ? defaultValue : object ));
				if( converted instanceof java.math.BigDecimal ) {
					return ( (java.math.BigDecimal)converted ).intValue();
				} else {
					return (Integer)converted;
				}
			} catch( FieldException fdEx ) {
				throw new DataException(fdEx.getErrorKey(), fdEx.getMessage(), fdEx);
			}
		}
	}

}
