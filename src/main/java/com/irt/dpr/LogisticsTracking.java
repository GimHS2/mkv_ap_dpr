/*
 *	File Name:	LogisticsTracking.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.0	create
 *
**/

package com.irt.dpr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import com.irt.json.Jsoner;
import com.irt.sql.SQLHandler;
import com.irt.util.MessageHandler;

/**
 *
 */
public class LogisticsTracking {//@formatter:on
	public final static String jsonUrl = "https://weika.weikayun.com/loadOrderForMobileMap";
	public final static String userRequestUrl = "https://weika.weikayun.com/mobileOrderTrackingDetail?company=JNJ&userId=JNJP&refNo=";
	SQLHandler handler;
	MessageHandler msghandler;
	Map<String, Object> jsonMap;

	public LogisticsTracking( SQLHandler handler ) {
		this.handler = handler;
		this.msghandler = handler.getMessageHandler();
	}

	public Map<String, Object> executeQuery( String deliveryNumber ) throws HttpClientException, IOException {
		CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
		HttpResponse response = null;
		try {
			httpClient.start();
			HttpPost post = new HttpPost( jsonUrl );
			List<NameValuePair> param = new java.util.ArrayList<NameValuePair>();
			param.add( new BasicNameValuePair("isNeedGpsInfo", "true") );
			param.add( new BasicNameValuePair("company", "JNJ") );
			param.add( new BasicNameValuePair("srRefNo", deliveryNumber) );
			param.add( new BasicNameValuePair("userIdForQualifyFilter", "JNJP") );
			post.setEntity( new UrlEncodedFormEntity(param, "utf-8") );

			Future<HttpResponse> future = httpClient.execute( post, null );
			response = future.get();
			response.addHeader( "Content-Type", "application/json; charset=utf-8" );
			int statusCode = response.getStatusLine().getStatusCode();
			if( statusCode == HttpStatus.SC_OK ) {
				HttpEntity respEntity = response.getEntity();
				if( respEntity != null ) {
					String content = EntityUtils.toString( respEntity );
					Jsoner json = Jsoner.getInstance();
					jsonMap = json.fromJson( content );
				} else {
					throw new HttpClientException( msghandler.getMessage("MSG_DPR_ERR_LOGISTICS_TRACKING_DATA") );
				}
			} else {
				throw new HttpClientException( msghandler.getMessage("MSG_DPR_ERR_LOGISTICS_TRACKING_SERVER", String.valueOf(statusCode)) );
			}

			return jsonMap;
		} catch( UnsupportedEncodingException unsupportedEx ) {
			throw new HttpClientException( unsupportedEx );
		} catch( InterruptedException interruptedEx ) {
			throw new HttpClientException( interruptedEx );
		} catch( ExecutionException execEx ) {
			throw new HttpClientException( execEx );
		} catch( ParseException parseEx ) {
			throw new HttpClientException( parseEx );
		} finally {
			if( httpClient != null ) try { httpClient.close(); } catch( IOException ioEx ) {}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getDataMapFromJson( String jsonString ) {
		Jsoner jsonBuilder = Jsoner.getInstance();
		return jsonBuilder.fromJson(jsonString, java.util.Map.class);
	}

	public class HttpClientException extends Exception {
		public HttpClientException( Throwable throwable ) {
			super( throwable );
		}

		public HttpClientException( String message ) {
			super( message );
		}

		public HttpClientException( String message, Throwable throwable ) {
			super( message, throwable );
		}
	}
}
