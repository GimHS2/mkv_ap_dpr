/*
 *	File Name:	MailTransport.java
 *	Version:	2.2.10(dpr)
 *
 *	Description:
 *
 *	Note:
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2026/03/31		2.2.10	getMailProperty(): mail.smtp.ssl.trust 옵션을 smtp.kenvue.com -> * 변경
 *	dudwls3720	2025/04/30		2.2.9	getMailProperty() : 속성 값 문자열 형식으로 수정
 *	flexyduck	2025/01/31		2.2.8	getReceiveAddress() : 사용자 이메일에 세미클론같은 특수문자를 쉼표(,)로 구분되도록 변경
 *	dudwls3720	2024/08/30		2.2.7	getReceiveAddress() : 사용자 이메일에 연속적인 콤마나 세미클론 들어올 경우 콤마로 구분되도록 변경
 *	dudwls3720	2024/02/29		2.2.6	getMailProperty()에서 메일 옵션 추가
 *	hankalam	2019/07/31		2.2.5	Freegoods Order Mail 기능 추가
 *	jbaek		2014/12/31		2.2.4	RDDFailure Email 기능: 국가별 메일 전송 기능.
 *	lsinji		2009/12/13		2.2.3	메일 수신자가 있는 경우에만 메일 전송
 *	lsinji		2009/08/14		2.2.2	ERROR_TEMPLATE 오타 처리 및 getMailContext()에서 Reader NullPointerException() 처리
 *	lsinji		2009/04/20		2.2.1	getReceiveAddress()에서 email NullPointerException 처리
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.irt.sql.SQLHandler;

/**
 *
 */
public class MailTransport {
	Map<String, Object> systemInfo;
	Map<String, Object> infoMap;
	String defaultSenderEmail = "ghan8@kenvue.com";
	SQLHandler handler;
	Logger logger;

	private final int BUFFER_SIZE						= 1024;
	private final String ERROR_TEMPLATE					= "MailErrorTemplate.html";
	private final String RDDFAILURE_TEMPLATE					= "MailRDDFailureTemplate.html";
	private final String INFO_TEMPLATE					= "MailInfoTemplate.html";
	private final String FREEGOODS_ORDER_TEMPLATE		= "MailFreegoodsTemplate.html";

	private final String MAILCONTENT_NULLSTRING			= "";

	private final String OPEN_TOKEN						= "%{";
	private final String CLOSE_TOKEN					= "}%";


	public final static String SENDMAILTYPE_INFO			= "IN";
	public final static String SENDMAILTYPE_FREEGOOD_ORDER	= "FO";
	public final static String SENDMAILTYPE_ERROR			= "ER";
	public final static String SENDMAILTYPE_RDDFAILURE		= "RF";

	public MailTransport( SQLHandler handler, Logger logger, Map<String, Object>infoMap ) {
		this.handler = handler;
		try {
			this.systemInfo = OrderCanonicalProcess.getSystemInfo();
		} catch( IllegalArgumentException ex ) {
			this.systemInfo = new java.util.HashMap<String, Object>();
		}

		this.logger = logger;
		this.infoMap = infoMap;
	}

	public MailTransport( SQLHandler handler, Logger logger, Map<String, Object>infoMap, Map<String,Object> systemInfoMap ) {
		this.handler = handler;
		if( systemInfoMap != null ) {
			this.systemInfo = systemInfoMap;
		} else {
			try {
				this.systemInfo = OrderCanonicalProcess.getSystemInfo();
			} catch( IllegalArgumentException ex ) {
				this.systemInfo = new java.util.HashMap<String, Object>();
			}
		}
		this.logger = logger;
		this.infoMap = infoMap;
	}

	public void send( String type ) {
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		MimeBodyPart mbp = new MimeBodyPart();
		try {
			mbp.setContent( getMailContext(infoMap, type), "text/html; charset=utf-8" );
		} catch( MessagingException messageEx ) {
			logger.error( messageEx );
		}

		if( SENDMAILTYPE_INFO.equals(type) )
			sendInfoMail( mbp, msghandler );
		else if( SENDMAILTYPE_RDDFAILURE.equals(type) )
			sendRDDFailureMail( mbp, msghandler );
		else if( SENDMAILTYPE_INFO.equals(type) )
			sendErrorMail( mbp, msghandler );
		else if( SENDMAILTYPE_FREEGOOD_ORDER.equals(type) ) {
			sendFreegoodsOrderMail( mbp, msghandler );
		}
	}

	private void sendErrorMail( MimeBodyPart mbp, com.irt.util.MessageHandler msghandler ) {
		sendMail( msghandler.getMessage("MSG_SMTP_ORDER_ERR_SUBJECT") +" [ "+systemInfo.get("systemName")+" ]", mbp, SENDMAILTYPE_ERROR );
	}

	private void sendRDDFailureMail( MimeBodyPart mbp, com.irt.util.MessageHandler msghandler ) {
		sendMail( "RDD Failure Email" +" [ "+systemInfo.get("systemName")+" ]", mbp, SENDMAILTYPE_RDDFAILURE );
	}

	private void sendInfoMail( MimeBodyPart mbp, com.irt.util.MessageHandler msghandler ) {
		sendMail( msghandler.getMessage("MSG_SMTP_ORDER_INFO_SUBJECT") +" [ "+systemInfo.get("systemName")+" ]", mbp, SENDMAILTYPE_INFO );
	}

	private void sendFreegoodsOrderMail( MimeBodyPart mbp, com.irt.util.MessageHandler msghandler ) {
		String subject = msghandler.getMessage( "MSG_DPR_FREEGOODS_ORDER" ) + " : " + infoMap.get( "soldPartyCode" )
					+ " " + infoMap.get( "soldPartyName" ) +" [ "+systemInfo.get("systemName")+" ]";
		sendMail( subject, mbp, SENDMAILTYPE_INFO );
	}

	private void sendMail( String subject, MimeBodyPart mbp, String type ) {
		Properties property = getMailProperty();
		Session session = Session.getDefaultInstance( property );

		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setFrom( getSenderAddress(type) );
			msg.setRecipients(Message.RecipientType.TO,
					getReceiveAddress((String)infoMap.get("soldPartyCode"), (String)infoMap.get("organizationCode"), type) );
			msg.setSubject(subject, "utf-8");

			InternetAddress[] recipients = (InternetAddress[])msg.getRecipients( Message.RecipientType.TO );
			if( recipients != null && recipients.length > 0 ) {
				Multipart mp = new MimeMultipart();
				mp.addBodyPart( mbp );
				msg.setContent( mp );

				if( SENDMAILTYPE_RDDFAILURE.equals(type) )
					msg.setSentDate( new java.util.Date() );
				else
					msg.setSentDate( getCurrentDate() );

				Transport.send(msg);
			}
		} catch( MessagingException messageEx ) {
			logger.error( messageEx );
		}
	}

	private String getMailContext( Map<String, Object> contentMap, String type ) {
		java.io.InputStream in;
		if( "ER".equals(type) )
			in = MailTransport.class.getClassLoader().getResourceAsStream( "com/irt/dpr/tools/" + ERROR_TEMPLATE );
		else if( "RF".equals(type) )
			in = MailTransport.class.getClassLoader().getResourceAsStream( "com/irt/dpr/tools/" + RDDFAILURE_TEMPLATE );
		else if( "IN".equals(type) )
			in = MailTransport.class.getClassLoader().getResourceAsStream( "com/irt/dpr/tools/" + INFO_TEMPLATE );
		else if( SENDMAILTYPE_FREEGOOD_ORDER.equals(type) )
			in = MailTransport.class.getClassLoader().getResourceAsStream("com/irt/dpr/tools/" + FREEGOODS_ORDER_TEMPLATE);
		else
			throw new IllegalStateException("Unsupported MailContext type: "+ type);

		java.io.BufferedReader reader = null;
		try {
			reader = new java.io.BufferedReader( new java.io.InputStreamReader(in) );
		} catch( Exception ex ) {
			logger.error( ex );
		}

		if( reader == null )
			return MAILCONTENT_NULLSTRING;

		StringBuffer strbuf = new StringBuffer();
		char[] buf = new char[BUFFER_SIZE];
		int ret = 0;
		try {
			while( (ret = reader.read(buf, 0, BUFFER_SIZE)) > 0 )
				strbuf.append( buf, 0, ret );
		} catch( IOException ioEx ) {
			logger.error( ioEx );
		}

		String html = null;
		try {
			int position = 0;
			int destination = -1;
			int index = 0;
			html = strbuf.toString();
			while( (position = html.indexOf(OPEN_TOKEN)) > 0 ) {
				destination = html.indexOf( CLOSE_TOKEN, position );
				try {
					String key = html.substring( position + OPEN_TOKEN.length(), destination );
					String value = com.irt.dpr.util.BasicData.getStringValue( contentMap.get(key) );
					if( value == null ) value = "";

					html = html.replace( OPEN_TOKEN + key + CLOSE_TOKEN, value.toString() );
				} catch( java.lang.ArrayIndexOutOfBoundsException boundEx ) {
					logger.error( boundEx );
					break;
				}
			}
		} finally {
			try { reader.close(); } catch( Exception ex ) {}
			try { in.close(); } catch( Exception ex ) {}
		}

		return html.toString();
	}

	private com.irt.data.Date getCurrentDate() {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		TimeZone zone = TimeZone.getDefault();
		try {
			pstmt = handler.getConnection().prepareStatement( "SELECT TIMEZONE FROM USR_PARTY WHERE GLN = ?" );
			pstmt.setString( 1, (String)infoMap.get("countryCode") );

			rset = pstmt.executeQuery();
			if( rset.next() ) {
				String timeString = rset.getString( 1 );

				if( timeString != null )
					zone = TimeZone.getTimeZone( timeString );
			}
		} catch( SQLException sqlEx ) {
			logger.error( sqlEx );
		} finally {
			try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
			try { if( rset != null ) rset.close(); } catch( Exception ex ) {}
		}

		return com.irt.data.Date.getInstance( zone );
	}

	private InternetAddress getSenderAddress( String type ) {
		try {
			String smtpFrom = null;
			if( SENDMAILTYPE_RDDFAILURE.equals( type ) )
				smtpFrom = (String)systemInfo.get( "RDDFailure.smtp.from" );
			else
				smtpFrom = (String)systemInfo.get("smtp.from");

			if( smtpFrom != null )
				return new InternetAddress( smtpFrom );
			else
				return new InternetAddress( defaultSenderEmail );
		} catch( AddressException addressEx ) {
			try {
				return new InternetAddress( defaultSenderEmail );
			} catch( Exception ex ) {
				logger.error( ex );
			}
		}

		return null;
	}

	private Properties getMailProperty() {
		Properties props = new Properties();
		props.put("mail.smtp.host", systemInfo.get("smtp.host") );
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", "25");
		props.put("mail.smtp.auth", "false");
		props.put( "mail.smtp.starttls.enable", "true" );
		props.put( "mail.smtp.ssl.trust", "*" );
		props.put( "mail.smtp.ssl.protocols", "TLSv1.2" );

		return props;
	}

	private InternetAddress[] getReceiveAddress( String partyCode, String organizationCode, String type ) {
		if( SENDMAILTYPE_RDDFAILURE.equals( type ) ) {
			String RDDFailureDL = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "RDDFailureDL;"+ organizationCode );
			if( RDDFailureDL == null || RDDFailureDL.length() == 0 ) {
				String to = (String) systemInfo.get( "smtp.to" );
				if( to != null )
					RDDFailureDL = to;
				else
					RDDFailureDL = (String) systemInfo.get( "smtp.from" );
			}


			InternetAddress[] address = null;
			try {
				address = InternetAddress.parse( RDDFailureDL );
			} catch( AddressException addressEx ) {
				logger.error( addressEx );
			}
			return address;
		} else {
			String divisionCode = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Default;division" );
			String distributionChannelCode = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Default;distributionChannel" );

			List<InternetAddress> addList = new java.util.ArrayList<InternetAddress> ();
			PreparedStatement pstmt = null;
			ResultSet rset = null;
			try {
				pstmt = handler.getConnection().prepareStatement(
						"SELECT USR.EMAIL "
								+ " FROM DPR_PARTY_AUTH PAUT, USR_USER USR, USR_GROUP UGP"
								+ " WHERE PAUT.UNIQID = USR.UNIQID"
								+ " AND UGP.PARTYID = USR.PARTYID"
								+ " AND UGP.GROUP_ID = USR.GROUPID"
								+ " AND UGP.GROUP_CLASS IN ( 'MR', 'OR', 'BA' )"
								+ " AND USR.EMAIL IS NOT NULL"
								+ " AND PAUT.ORGANIZATIONCD = ?"
								+ " AND PAUT.DIVISIONCD = ?"
								+ " AND PAUT.DIST_CHANNELCD = ?"
								+ " AND PAUT.PARTYCD = ?"
								+ " AND USR.STATUS = '00'"
						);

				pstmt.setString( 1, organizationCode );
				pstmt.setString( 2, divisionCode );
				pstmt.setString( 3, distributionChannelCode );
				pstmt.setString( 4, partyCode );

				rset = pstmt.executeQuery();
				while( rset.next() ) {
					try {
						String email = rset.getString(1);
						if( email != null && email.length() > 0 ) {
							email = email.trim().replaceAll("\\s+", ",").replaceAll(";", ",").replaceAll(",+", ",").replaceAll("，", ",").replaceAll("；",",").replaceAll("^,", "").replaceAll(",$", "");

							InternetAddress[] userAddress = InternetAddress.parse( email );

							if( userAddress != null && userAddress.length > 0 )
								for( int i = 0; i < userAddress.length; i++ )
									addList.add( userAddress[i] );
						}
					} catch( AddressException addressEx ) {
						logger.error( addressEx );
					}
				}
			} catch( SQLException sqlEx ) {
				logger.error( "Can't not read e-mail address", sqlEx );
			} finally {
				try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
				try { if( rset != null ) rset.close(); } catch( Exception ex ) {}
			}

			InternetAddress[] address = new InternetAddress[ addList.size() ];
			addList.toArray( address );

			return address;
		}
	}
}
