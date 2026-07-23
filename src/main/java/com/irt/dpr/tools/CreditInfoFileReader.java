/*
 *	File Name:	CreditInfoFileReader.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	yjkdev21	2026/02/27		2.2.1	read() : 업데이트 건수 로그 추가
 *	jbaek		2023/07/27		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.data.AbstractField;
import com.irt.data.DataException;
import com.irt.data.DataReader;
import com.irt.data.DataResult;
import com.irt.data.FieldException;
import com.irt.data.ValidableField;
import com.irt.data.ValidableFieldSet;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.util.CSVReader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CreditInfoFileReader implements INFFileReader {

	private static int DEFAULT_MAXIMUM_CONSISTENT_ERROR = 1;

	// 888	JJKR	0000102816	0	0	0	KRW	2023-07-21 20:00:45.000	2023-07-21 22:09:22.000	2023-07-21 22:24:18.000
	//	Cl.	Customer	CCAr	Credit limit	Total receivables	Credit exposure	Curr.
	static String[] csvFieldKeys = new String[]{
		"dataClassCode"
		, "creditPartyCode"
		, "creditOrganizationLabel"
		, "creditLimit", "accountReceivable", "creditExposure", "creditCurrency"
	};

	ValidableFieldSet transferFieldSet = new ValidableFieldSet( new ValidableField[] {
			new ValidableField( false, "dataClassCode", "SAP_DATA_CLASS_CODE", AbstractField.TYPE_STRING ),
			new ValidableField( false, "creditPartyCode", "CREDIT_PARTY_CODE", AbstractField.TYPE_STRING ),
			new ValidableField( false, "creditOrganizationLabel", "CREDIT_ORGANIZATION_LABEL", AbstractField.TYPE_STRING ),
			new ValidableField( false, "creditLimit", "SAP_CREDIT_LIMIT", AbstractField.TYPE_DOUBLE ),
			new ValidableField( false, "accountReceivable", "SAP_ACCOUNT_RECEIVABLE", AbstractField.TYPE_DOUBLE ),
			new ValidableField( false, "creditExposure", "SAP_CREDIT_EXPOSURE", AbstractField.TYPE_DOUBLE ),
			new ValidableField( true, "creditCurrency", "SAP_CREDIT_CURRENCY", AbstractField.TYPE_STRING ),
			new ValidableField( false, "creditRefDateStr", "CREDIT_REFERENCE_DATETIME_STRING", AbstractField.TYPE_STRING ),
		} );


	protected Map<String, String[]> errorLineMap;
	PreparedStatement pstmt_ins, pstmt_upd;
	String messageId;
	String documentType;
	java.util.Date documentDate;
	String resultStatus;
	String resultMessage;
	Logger logger = null;

	private SQLHandler handler;
	private File infFile;
	private DataReader dataReader = null;
	private ZoneId infFileDateTimeZoneId = ZoneId.of( "Asia/Singapore" );
	DateTimeFormatter fileDateTimeFormatter = DateTimeFormatter.ofPattern( "yyyyMMdd_HHmmss" );

	public CreditInfoFileReader( SQLHandler handler, File file, Logger logger ) {
		this.handler = handler;
		this.infFile = file;
		this.logger = logger;
		this.errorLineMap = new HashMap<String, String[]>();
		this.documentDate = parseFileDateTime();
	}

	public void createDataReader() throws IOException {
		char delim = CSVReader.DEFAULT_DELIM;
		String encoding = "UTF-8";
		InputStream inputStream = new FileInputStream( infFile );
		InputStreamReader inputStreamReader = null;
		try {
			if( encoding != null )
				inputStreamReader = new java.io.InputStreamReader( inputStream, encoding );
			else
				inputStreamReader = new java.io.InputStreamReader( inputStream );
		} catch( java.io.UnsupportedEncodingException encodeEx ) {
			inputStreamReader = new java.io.InputStreamReader( inputStream );
		}
		this.dataReader = new CSVReader( inputStreamReader, delim, CSVReader.DEFAULT_ENCAP );
		this.dataReader.setTrim( true );
	}

	private void skipTitleRows() throws IOException {
		for( int i = 0; i < 1 && !dataReader.isEOF(); i++ ) {
			try {
				this.dataReader.readNext();
			} catch( DataException dataEx ) {}
		}
	}

	@Override
	public java.util.Date getDocumentDate() {
		return documentDate;
	}

	@Override
	public String getDocumentType() {
		return documentType;
	}

	public int getLineNumber() {
		return dataReader.getLineNumber();
	}

	public String[] getLines() {
		return dataReader.getLines();
	}

	public String getLineString() {
		return dataReader.getLineString();
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public String getResultMessage() {
		return resultMessage;
	}

	@Override
	public String getResultStatus() {
		return resultStatus;
	}

	String parseFileDateTimeString() {
		String filename = infFile.getName();
		String[] underValues = filename.split("_|\\.");
		List<String> backList = java.util.Arrays.asList(underValues);
		Collections.reverse(backList);
		String timeValue = backList.get(1);
		String dateValue = backList.get(2);
		return dateValue + "_" + timeValue;
	}

	Date parseFileDateTime() throws DateTimeParseException {
		String fileDateTimeStr = parseFileDateTimeString();
		LocalDateTime dateTime = java.time.LocalDateTime.parse( fileDateTimeStr, fileDateTimeFormatter );
		Instant instant = dateTime.atZone( infFileDateTimeZoneId ).toInstant();
		return Date.from(instant);
	}

	@Override
	public int read() throws INFFileException, IOException, SQLException {
		DataResult result = readData();

		logger.info( documentType + "("+ this.messageId +"): "+ result.getCount() +" lines read."
				 + "(ins: "+ result.getRegistCount() +", upg: "+result.getModifyCount() +", del: " + result.getDeleteCount() +", err: "+ result.getErrorCount() +", ign: " + result.getIgnoreCount() +")" );

		if( result.getErrorCount() > 0 ) {
			this.resultStatus = "ER";
			this.resultMessage = result.getCount() +" rows(error: "+ result.getErrorCount() +" rows) completed. ["+ getDocumentDate().toString() +"]";

			return -1 * result.getErrorCount();
		} else {
			this.resultStatus = "CP";
			this.resultMessage = result.getCount() +" rows completed. ["+ getDocumentDate().toString() +"]";

			return result.getCount();
		}
	}

	DataResult readData() throws IOException, SQLException {
		DataResult result = new DataResult();

		String upd_sql = "UPDATE DPR_INF_CREDIT_INFO"
			+ " SET DATA_CLASS_CD = ?, CREDIT_LIMIT = ?, ACCOUNT_RECEIVABLE = ?, CREDIT_EXPOSURE = ?, CREDIT_CURRENCY = ?"
			+ " , CREDIT_REF_DATE = TO_DATE(?, 'YYYYMMDD_HH24MISS'), UPGDATE = SYSDATE"
			+ " WHERE CREDIT_PARTYCD = ? AND CREDIT_ORGANIZATION_LABEL = ? AND CREDIT_REF_DATE < TO_DATE(?, 'YYYYMMDD_HH24MISS')";

		String ins_sql = "INSERT INTO DPR_INF_CREDIT_INFO"
			+ " ( CREDIT_PARTYCD, CREDIT_ORGANIZATION_LABEL, DATA_CLASS_CD"
			+ ", CREDIT_LIMIT, ACCOUNT_RECEIVABLE, CREDIT_EXPOSURE, CREDIT_CURRENCY"
			+ ", CREDIT_REF_DATE, REGDATE, UPGDATE )"
			+ " SELECT ?, ?, ?"
			+ ", ?, ?, ?, ?"
			+ ", TO_DATE(?, 'YYYYMMDD_HH24MISS'), SYSDATE, SYSDATE"
			+ " FROM DUAL"
			+ " WHERE NOT EXISTS( SELECT 1 FROM DPR_INF_CREDIT_INFO WHERE CREDIT_PARTYCD = ? AND CREDIT_ORGANIZATION_LABEL = ? )";

		try {
			createDataReader();
			skipTitleRows();

			pstmt_upd = handler.getConnection().prepareStatement( upd_sql );
			pstmt_ins = handler.getConnection().prepareStatement( ins_sql );

			Map<String, Object> recordMap = null;
			Object[] bindVars = null;
			int objIdx = 0;
			while( !dataReader.isEOF() ) {
				try {
					try {
						recordMap = readNext( csvFieldKeys );
						if( recordMap == null ) break;

						Map<String, Object> map = new HashMap<String, Object>();
						Object[] values = transferFieldSet.validate(recordMap);
						String[] keys = transferFieldSet.getFieldKeyArray();
						for( int i = 0; i < keys.length; i++ ) {
							map.put( keys[i], values[i] );
						}
						bindVars = new Object[ 9 ];
						objIdx = 0;
						bindVars[objIdx++] = map.get( "dataClassCode" );
						bindVars[objIdx++] = map.get( "creditLimit" );
						bindVars[objIdx++] = map.get( "accountReceivable" );
						bindVars[objIdx++] = map.get( "creditExposure" );
						bindVars[objIdx++] = map.get( "creditCurrency" );
						bindVars[objIdx++] = map.get( "creditRefDateStr" );
						bindVars[objIdx++] = map.get( "creditPartyCode" );
						bindVars[objIdx++] = map.get( "creditOrganizationLabel" );
						bindVars[objIdx++] = map.get( "creditRefDateStr" );
						SQLManager.bindVariables( pstmt_upd, bindVars );
						if( pstmt_upd.executeUpdate() > 0 ) {
							result.increaseModifyCount();
						} else {
							bindVars = new Object[ 10 ];
							objIdx = 0;
							bindVars[objIdx++] = map.get( "creditPartyCode" );
							bindVars[objIdx++] = map.get( "creditOrganizationLabel" );
							bindVars[objIdx++] = map.get( "dataClassCode" );
							bindVars[objIdx++] = map.get( "creditLimit" );
							bindVars[objIdx++] = map.get( "accountReceivable" );
							bindVars[objIdx++] = map.get( "creditExposure" );
							bindVars[objIdx++] = map.get( "creditCurrency" );
							bindVars[objIdx++] = map.get( "creditRefDateStr" );
							bindVars[objIdx++] = map.get( "creditPartyCode" );
							bindVars[objIdx++] = map.get( "creditOrganizationLabel" );
							SQLManager.bindVariables( pstmt_ins, bindVars );
							if( pstmt_ins.executeUpdate() > 0 ) {
								result.increaseRegistCount();
							} else {
								result.increaseIgnoreCount();
							}
						}
					} catch( SQLException sqlEx ) {
						String errorKey = "SQL;" + dataReader.getLineNumber();
						errorLineMap.put( errorKey
															, new String[] { String.valueOf(dataReader.getLineNumber()), String.valueOf(sqlEx.getErrorCode()), sqlEx.getMessage() } );

						throw new INFFileException( getLineNumber(), getLineString(), handler.createDataException(sqlEx) );
					} catch( FieldException fieldEx ) {
						String errorKey = "DATA;" +  dataReader.getLineNumber();

						errorLineMap.put( errorKey
															, new String[] { String.valueOf(dataReader.getLineNumber()), fieldEx.getErrorKey(), fieldEx.getMessage() } );
						logger.debug( "Error Field Value : "+ fieldEx.getErrorFieldValue() );
						logger.debug( "LineString:"+ getLineString() );

						throw new INFFileException( getLineNumber(), getLineString(), fieldEx );
					} catch( DataException dataEx ) {
						String errorKey = "DATA;" +  dataReader.getLineNumber();

						errorLineMap.put( errorKey
								, new String[] { String.valueOf(dataReader.getLineNumber()), dataEx.getErrorKey(), dataEx.getMessage() } );

						throw new INFFileException( getLineNumber(), getLineString(), dataEx );
					}
				} catch( INFFileException fileEx ) {
					result.increaseErrorCount();
					logger.log( fileEx.getLevel() == INFFileException.WARNING ? Level.WARN : Level.ERROR, infFile.getName() +": error", fileEx );

					if( result.getErrorCount() >= DEFAULT_MAXIMUM_CONSISTENT_ERROR )
						break;
				}
			}
			handler.commit();

			try {
				if( result.getModifyCount() > 0 || result.getRegistCount() > 0 ) {
					SQLManager.callStatement( handler, "BEGIN pkDPRMaster.pMergeCreditInfo(); END;" );
				}
			} catch( SQLException sqlEx ) {
				String errorKey = "SQL;" + dataReader.getLineNumber();
				errorLineMap.put( errorKey
													, new String[] { String.valueOf(dataReader.getLineNumber()), String.valueOf(sqlEx.getErrorCode()), sqlEx.getMessage() } );

				throw new INFFileException( getLineNumber(), getLineString(), handler.createDataException(sqlEx) );
			} catch( DataException dataEx ) {
				String errorKey = "DATA;" +  dataReader.getLineNumber();

				errorLineMap.put( errorKey
													, new String[] { String.valueOf(dataReader.getLineNumber()), dataEx.getErrorKey(), dataEx.getMessage() } );

				throw new INFFileException( getLineNumber(), getLineString(), dataEx );
			}
		} catch( INFFileException fileEx ) {
			result.increaseErrorCount();
			logger.log( fileEx.getLevel() == INFFileException.WARNING ? Level.WARN : Level.ERROR, infFile.getName() +": error", fileEx );
		} finally {
			try { pstmt_upd.close(); } catch( Exception ignored ) {}
			try { pstmt_ins.close(); } catch( Exception ignored ) {}
			try { handler.rollback(); } catch( Exception ignored ) {}
		}

		return result;
	}

	@Override
	public Map<String, Object> readNext( String[] keys ) throws DataException, IOException {
		Map<String, Object> recordMap = dataReader.readNext( keys );
		if( recordMap == null || recordMap.size() == 0 ) return null;

		recordMap.put( "creditRefDateStr", parseFileDateTimeString() );

		String creditPartyCode_s = (String)recordMap.get( "creditPartyCode" );
		recordMap.put( "creditPartyCode", creditPartyCode_s.replaceAll("^0+", "") );

		String creditLimit_s = (String)recordMap.get( "creditLimit" );
		if( creditLimit_s != null && creditLimit_s.endsWith("-") )
			recordMap.put( "creditLimit", "-" + creditLimit_s.replaceAll("-$", "") );
		String accountReceivable_s = (String)recordMap.get( "accountReceivable" );
		if( accountReceivable_s != null && accountReceivable_s.endsWith("-") )
			recordMap.put( "accountReceivable", "-" + accountReceivable_s.replaceAll("-$", "") );
		String creditExposure_s = (String)recordMap.get( "creditExposure" );
		if( creditExposure_s != null && creditExposure_s.endsWith("-") )
			recordMap.put( "creditExposure", "-" + creditExposure_s.replaceAll("-$", "") );

		return recordMap;
	}

	@Override
	public void updateMessageLog( String documentFileName ) throws DataException, SQLException {
		//ignored.
	}

	@Override
	public void updateMessageLog( String documentFileName, String previousMessageId ) throws DataException, SQLException {
		//ignored.
	}

}
