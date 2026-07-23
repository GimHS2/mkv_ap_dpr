/*
 *	File Name:	UserUser.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/03/30		2.2.2c	regex오류 수정
 *	jbaek		2020/12/31		2.2.2c	multi-sold-to 기능 추가
 *	hankalam	2020/09/28		2.2.2c	encodePassword(): HASH_ALGORITHM 추가
 *										getEncryptionPassword(): HASH_ALGORITHM 에 따른 비밀번호 암호화 처리
 *										checkAvailableUser(), existId(), getUserEmail(), getUserName(), getUserId() 추가
 *										checkValidUser() 수정
 *										비밀번호 HASH 알고리즘 MD5, SHA-256 환경변수로 적용되도록 변경.
 *	lsinji		2008/09/26		2.2.1	comparePassword(), getEncryptionPassword() 추가.
										checkPassword()에서 comparePassword() 호출 하도록 변경
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										SQLManager 변경사항 적용: fieldMap 삭제
 *	stghr12		2007/10/31		2.1.1	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2007/04/30		2.1.0	createPrimary(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/13		1.0.0	version 관리
 *	crystal		2002/10/01				create
 *
 **/

package com.irt.rbm.usr;

import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.Record;
import com.irt.dpr.PartyAuth;
import com.irt.rbm.RBMSystem;
import com.irt.sql.*;
import com.irt.util.MapUtil;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 */
public class UserUser extends com.irt.rbm.ManipulableManagerImpl {
	public final static String USERCLASS_SYSTEM_ADMIN	= "SA";
	public final static String USERCLASS_PARTY_ADMIN	= "PA";
	public final static String USERCLASS_USER			= "UR";

	public final static String USERSTATUS_NORMAL		= "00";

	public enum USERSTATUS {
		ACTIVE("00"),
		LOCKED("LK"),
		INACTIVE("99"),
		EXPIRED("PW");

		private final String code;
		USERSTATUS(final String text) {
			this.code = text;
		}
		@Override
		public String toString() {
			return code;
		}

		public String code() {
			return code;
		}

		public static String[] names() {
			USERSTATUS[] states = values();
			String[] names = new String[states.length];

			for (int i = 0; i < states.length; i++) {
				names[i] = states[i].name();
			}

			return names;
		}
	}

	public final static String HASH_ALGORITHM_MD5		= "MD5";
	public final static String HASH_ALGORITHM_SHA2		= "SHA-256";

	private final static Table table = Schema.findTable( Schema.USER_USER );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.USER_USER );

	public static String hashAlgorithm;

	static {
		if( hashAlgorithm == null ) {
			hashAlgorithm = RBMSystem.getSystemEnv("USR", "SecurityPolicy;PasswordHashAlgorithm"
					, HASH_ALGORITHM_SHA2 );

			if( !HASH_ALGORITHM_MD5.equals(hashAlgorithm) && !HASH_ALGORITHM_SHA2.equals(hashAlgorithm) ) {
				hashAlgorithm = HASH_ALGORITHM_SHA2;
			}
		}
	}

	public UserUser( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public boolean checkAvailableUser( String partyId, String userId ) throws SQLException {
		String querybuf = "SELECT COUNT(*) FROM vwUSR_USER WHERE SERV_START_IND = 'Y' AND SERV_END_IND = 'N' AND PARTYID = ? AND USERID = ?";
		return SQLManager.getInt( handler, querybuf, partyId, userId ) > 0;
	}

	public boolean checkPassword( String partyId, String userId, String password ) throws SQLException {
		return( password != null && UserUser.comparePassword(password, getFieldValueByName(partyId, userId, "PASSWORD")) );
	}

	public boolean checkValidUser( String partyId, String userId ) throws SQLException {
		PreparedStatement pstmt = handler.getConnection().prepareStatement(
				"SELECT SERV_START, SERV_END FROM vwUSR_USER WHERE PARTYID = ? AND USERID = ? AND STATUS = '00'"
				);
		ResultSet rset = null;
		try {
			pstmt.setString( 1, partyId );
			pstmt.setString( 2, userId );
			rset = pstmt.executeQuery();

			return( rset.next() && rset.getInt(1) == 1 && rset.getInt(2) == 0 );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static boolean comparePassword( String inputPassword, String dbPassword ) {
		String password = getEncryptionPassword( inputPassword );
		if( password == null ) return false;

		return password.equals( dbPassword );
	}

	public static boolean comparePassword( String inputPassword, String dbPassword, String hashAlgorithm ) {
		String password = getEncryptionPassword( inputPassword, hashAlgorithm );
		if( password == null ) return false;

		return password.equals( dbPassword );
	}

	public int encodePassword( String partyId ) throws DataException, SQLException {
		List<Map<String, Object>> recordList = SQLManager.getRecordList( handler,
				"SELECT USER_ID \"userId\", PASSWORD \"password\" FROM USR_USER WHERE PARTYID = ?"
				, new Object[] { partyId } );
		if( recordList == null ) return 0;

		int count = 0;

		for( Map<String, Object> recordMap : recordList )
			count += encodePassword( partyId, (String)recordMap.get("userId"), (String)recordMap.get("password") );

		return count;
	}

	public int encodePassword( String partyId, String userId ) throws DataException, SQLException {
		String password = (String)SQLManager.getObjectValue( handler,
				"SELECT PASSWORD FROM USR_USER WHERE PARTYID = ? AND USER_ID = ?"
				, partyId, userId );
		if( password == null ) return 0;

		return encodePassword( partyId, userId, password );
	}

	public int encodePassword( String partyId, String userId, String password ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler,
				"UPDATE USR_USER SET PASSWORD = ?, HASH_ALGORITHM = ?, UPGDATE = SYSDATE WHERE PARTYID = ? AND USER_ID = ?"
				, getEncryptionPassword(password), hashAlgorithm, partyId, userId );
	}

	public boolean existId( String partyId, String userId ) throws SQLException {
		return ( getFieldValueByName(partyId, userId, "USERID") != null );
	}

	public static String getEncryptionPassword( String password ) {
		return getEncryptionPassword( password, hashAlgorithm );
	}

	public static String getEncryptionPassword( String password, String hashAlgorithm ) {
		if( password == null ) return null;
		if( hashAlgorithm == null || hashAlgorithm.trim().length() < 1 ) {
			hashAlgorithm = UserUser.hashAlgorithm;
		}

		try {
			byte[] passwordBytes = password.getBytes( "UTF-8" );
			java.security.MessageDigest md = java.security.MessageDigest.getInstance( hashAlgorithm );
			byte[] digest = md.digest( passwordBytes );

			StringBuffer buf = new StringBuffer();
			for( byte b : digest ) {
				String hexString = Integer.toHexString(b & 0xff);
				buf.append( (hexString.length() < 2 ? "0" + hexString : hexString) );
			}

			return buf.toString();
		} catch( java.security.NoSuchAlgorithmException alEx ) {}
		catch( java.io.UnsupportedEncodingException enEx ) {}

		return null;
	}

	public static Map<String, Object> createPrimary( String partyId, String userId ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "partyId", partyId );
		primaryMap.put( "userId", userId );

		return primaryMap;
	}

	public boolean delete( String uniqId ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler, "DELETE USR_USER WHERE UNIQID = ?", uniqId ) > 0 );
	}

	public boolean existId( String uniqId ) throws SQLException {
		return( getPartyId(uniqId) != null );
	}

	public Object getFieldValue( String uniqId, String fieldKey ) throws SQLException {
		QueryBuffer querybuf = new QueryBuffer();

		if( !factory.appendData(querybuf, fieldKey) )
			throw new IllegalArgumentException( "illegal fieldKey '"+ fieldKey +"'" );
		querybuf.appendCondition( table.getTableAlias() +".UNIQID = ?", uniqId );

		return SQLManager.getObjectValue( handler, querybuf );
	}

	private String getFieldValueByName( String uniqId, String fieldName ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT "+ fieldName +" FROM vwUSR_USER WHERE UNIQID = ?", uniqId );
	}

	private String getFieldValueByName( String partyId, String userId, String fieldName ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler,
				"SELECT "+ fieldName +" FROM vwUSR_USER WHERE PARTYID = ? AND USERID = ?"
				, partyId, userId );
	}

	public String getPartyId( String uniqId ) throws SQLException {
		return getFieldValueByName( uniqId, "PARTYID" );
	}

	public String getPartyAdminUserId( String partyId ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler,
				"SELECT USERID FROM vwUSR_USER WHERE PARTYID = ? AND USERCLASS = 'PA' AND STATUS = '00' AND ROWNUM < 2"
				, partyId );
	}

	public String[] getPartyAdminUserIds( String partyId ) throws SQLException {
		return SQLManager.getStringValues( handler,
				"SELECT USERID FROM vwUSR_USER WHERE PARTYID = ? AND USERCLASS = 'PA' AND STATUS = '00'"
				, partyId );
	}

	public Map<String, Object> getRecord( String uniqId ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( Record.createMap("uniqId", uniqId) );
		return SQLManager.getRecordMap( handler, null, factory.setQuery(querybuf) );
	}

	public Map<String, Object> getRecord( String uniqId, String[] fieldKeys ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( Record.createMap("uniqId", uniqId) );
		return SQLManager.getRecordMap( handler, null, factory.setQuery(querybuf, fieldKeys) );
	}

	public int getRecordCount( String partyId ) throws SQLException {
		return getRecordCount( Record.createMap("partyId", partyId) );
	}

	public List<Map<String, Object>> getRecords( String partyId ) throws SQLException {
		return getRecords( Record.createMap("partyId", partyId) );
	}

	public List<Map<String, Object>> getRecords( String partyId, String[] fieldKeys ) throws SQLException {
		return getRecords( Record.createMap("partyId", partyId), fieldKeys );
	}

	public String getUniqId( String partyId, String userId ) throws SQLException {
		return getFieldValueByName( partyId, userId, "UNIQID" );
	}

	public String getUserEmail( String partyId, String userId ) throws SQLException {
		return getFieldValueByName( partyId, userId, "EMAIL" );
	}

	public String getUserGln( String uniqId ) throws SQLException {
		return getFieldValueByName( uniqId, "GLN" );
	}

	public String getUserGln( String partyId, String userId ) throws SQLException {
		return getFieldValueByName( partyId, userId, "GLN" );
	}

	public String getUserId( String partyId, String name, String email ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT USER_ID FROM USR_USER WHERE PARTYID = ? AND USER_NAME = ? AND EMAIL = ?"
				, partyId, name, email );
	}

	public String getUserName( String uniqId ) throws SQLException {
		return getFieldValueByName( uniqId, "USERNAME" );
	}

	public String getUserName( String partyId, String userId ) throws SQLException {
		return getFieldValueByName( partyId, userId, "USERNAME" );
	}

	public String getUserStatus( String partyId, String userId ) throws SQLException {
		return getFieldValueByName( partyId, userId, "STATUS" );
	}

	public boolean updateStatus( String uniqId, String status ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler,
				"UPDATE USR_USER SET STATUS = ?, UPGDATE = SYSDATE WHERE UNIQID = ?"
				, status, uniqId ) > 0 );
	}

	public boolean updateStatus( String partyId, String userId, String status ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler,
				"UPDATE USR_USER SET STATUS = ?, UPGDATE = SYSDATE WHERE PARTYID = ? AND USER_ID = ?"
				, status, partyId, userId ) > 0 );
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException, UnsupportedOperationException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType ) {
			// uniqId -> map
			private Map<String, Map<String, Object>> uniqIdMaps = new java.util.HashMap<String, Map<String, Object>>();

			private String getUniqId( String userId ) {
				String partyId = (String)defaultMap.get("partyId");
				return userId + "@" + partyId;
			}

			private Map<String, Object> createResultMap( String executeType, String status, String message ) {
				Map<String, Object> resultMap = new java.util.TreeMap<String, Object>();
				resultMap.put( "executeType", executeType);
				resultMap.put( "status", status );
				resultMap.put( "message", message );
				return resultMap;
			}

			@Override
			public Map<String, Object> processLine(SQLHandler handler, Map<String, Object> recordMap)
					throws DataException, SQLException {
				String uniqId = getUniqId( (String)recordMap.get("userId") );
				recordMap.put( "uniqId", uniqId );
				if( uniqIdMaps.get(uniqId) == null ) {
					String theStatus = (String)recordMap.get("status");
					String a = USERSTATUS.INACTIVE.name();
					if( USERSTATUS.ACTIVE.name().equalsIgnoreCase(theStatus) || USERSTATUS.ACTIVE.code().equals(theStatus) ) {
						recordMap.put("status", USERSTATUS.ACTIVE.code());
					} else if ( USERSTATUS.INACTIVE.name().equalsIgnoreCase(theStatus) || USERSTATUS.INACTIVE.code().equals(theStatus) ) {
						recordMap.put("status", USERSTATUS.INACTIVE.code());
					} else if ( USERSTATUS.EXPIRED.name().equalsIgnoreCase(theStatus) || USERSTATUS.EXPIRED.code().equals(theStatus) ) {
						recordMap.put("status", USERSTATUS.EXPIRED.code());
					} else if ( USERSTATUS.LOCKED.name().equalsIgnoreCase(theStatus) || USERSTATUS.LOCKED.code().equals(theStatus) ) {
						recordMap.put("status", USERSTATUS.LOCKED.code());
					} else {
						throw new DataException( DataException.ERR_INVALID_VALUE
								, handler.getMessageHandler().getMessage(DataException.ERR_INVALID_VALUE, theStatus) );
					}
//					String theEmail = (String)recordMap.get("email");
//					if( theEmail != null && !Utility2.validateEmailCsv(theEmail) ) {
//						throw new DataException( DataException.ERR_INVALID_VALUE
//								, handler.getMessageHandler().getMessage(DataException.ERR_INVALID_VALUE, theEmail) );
//					}

					Map<String, Object> processed = super.processLine(handler, recordMap);
					uniqIdMaps.put(uniqId, processed);
					return processed;
				} else {
					if( defaultMap != null ) recordMap.putAll( defaultMap );
					recordMap.put("status", uniqIdMaps.get(uniqId).get("status"));// to ignore other lines for the user table
					recordMap.put("email", uniqIdMaps.get(uniqId).get("email"));// to ignore other lines for the user table
					return super.processLine(handler, recordMap);
				}
			}

			@Override
			public Map<String, Object> loadLine(SQLHandler handler, Map<String, Object> recordMap)
					throws DataException, SQLException {
				Map<String, Object> uniqIdMap = uniqIdMaps.get(recordMap.get("uniqId"));
				if( uniqIdMap == null )
					throw new IllegalStateException("cannot find uniqidMap.");

				String employeeId = (String)recordMap.get("employeeId");
				if( employeeId != null && employeeId.length() > 0 ) {
					List<Map<String, Object>> employeeSoldCodeList = (List<Map<String, Object>>) uniqIdMap.get("employeeSoldCodeList");
					if( employeeSoldCodeList == null )
						employeeSoldCodeList = new java.util.ArrayList<>();
					Map<String, Object> _recordMap = new java.util.TreeMap<>();
					_recordMap.put("soldPartyCode", recordMap.get("soldPartyCode"));
					_recordMap.put("employeeId", recordMap.get("employeeId"));
					if( recordMap.get("soldPartyOrgCode") == null )
						_recordMap.put("organizationCode", "ORGALL");
					else
						_recordMap.put("organizationCode", recordMap.get("soldPartyOrgCode"));

					employeeSoldCodeList.add( _recordMap );
					uniqIdMap.put("employeeSoldCodeList", employeeSoldCodeList);
				} else {
					List<Map<String, Object>> partySoldCodeList = (List<Map<String, Object>>) uniqIdMap.get("partySoldCodeList");
					if( partySoldCodeList == null )
						partySoldCodeList = new java.util.ArrayList<>();
					Map<String, Object> _recordMap = new java.util.TreeMap<>();
					_recordMap.put("soldPartyCode", recordMap.get("soldPartyCode"));
					if( recordMap.get("soldPartyOrgCode") == null )
						_recordMap.put("organizationCode", "ORGALL");
					else
						_recordMap.put("organizationCode", recordMap.get("soldPartyOrgCode"));

					partySoldCodeList.add( _recordMap );
					uniqIdMap.put("partySoldCodeList", partySoldCodeList);
				}

				return super.loadLine(handler, recordMap);
			}

			@Override
			public void complete(SQLHandler handler) throws SQLException {
				PartyAuth authDB = new PartyAuth( handler );
				com.irt.dpr.UserMultiEmployee multiempDB = new com.irt.dpr.UserMultiEmployee( handler );

				Map<String, Object> firstMap = null;
				java.util.Iterator<Entry<String, Map<String, Object>>> it = uniqIdMaps.entrySet().iterator();
				if( it.hasNext() )
					firstMap = it.next().getValue();

				String updateUserId = (String)firstMap.get("updateUserId");
				String divisionCode = (String)firstMap.get("divisionCode");
				String countryCode = (String)firstMap.get("countryCode");
				String organizationCodes = (String)firstMap.get("organizationCodes");
				String[] organizationCodeArr = null;
				if( organizationCodes != null && organizationCodes.contains(";") ) {
					organizationCodeArr = organizationCodes.split("\\s?;\\s?");
				} else {
					throw new IllegalStateException(
							handler.getMessageHandler().getMessage(DataException.ERR_INVALID_VALUE,
									String.format("%s: (%s)", "organizationCodes", organizationCodes)) );
				}

				for( String uniqId : uniqIdMaps.keySet() ) {
					Map<String, Object> userMap = uniqIdMaps.get(uniqId);
					Connection conn = handler.getConnection();
					PreparedStatement pstmt_delEmployee = null, pstmt_delPartyAuth = null;
					try {
						pstmt_delEmployee = conn.prepareStatement(
								"DELETE DPR_USER_MULTIEMP WHERE UNIQID = ?"
						);
						pstmt_delEmployee.setString( 1, uniqId );
						pstmt_delEmployee.executeUpdate();
						pstmt_delEmployee.close();

						pstmt_delEmployee = conn.prepareStatement(
								"DELETE DPR_USER_EMPLOYEE WHERE UNIQID = ?"
						);
						pstmt_delEmployee.setString( 1, uniqId );
						pstmt_delEmployee.executeUpdate();

						pstmt_delPartyAuth =conn.prepareStatement(
								"DELETE DPR_PARTY_AUTH WHERE UNIQID = ?"
						);
						pstmt_delPartyAuth.setString( 1, uniqId );
						pstmt_delPartyAuth.executeUpdate();
					} finally {
						try { if( pstmt_delEmployee != null ) pstmt_delEmployee.close(); } catch( Exception ignored ) {}
						try { if( pstmt_delPartyAuth != null ) pstmt_delPartyAuth.close(); } catch( Exception ignored ) {}
					}

					Map<String, Object> authConditionMap = new java.util.HashMap<>();
					authConditionMap.put( "divisionCode", divisionCode );
					authConditionMap.put( "countryCode", countryCode );
					authConditionMap.put( "uniqId", uniqId );
					if( !MapUtil.containAllKeysAndValueNotNull(authConditionMap, new String[] {"divisionCode", "countryCode", "uniqId"}) )
						throw new IllegalStateException( handler.getMessageHandler().getMessage(DataException.ERR_INVALID_VALUE,
									String.format("%s: (%s)", "authConditionMap", authConditionMap)) );

					List<Map<String, Object>> employeeSoldCodeList = (List<Map<String, Object>>) userMap.get("employeeSoldCodeList");
					if( employeeSoldCodeList != null && employeeSoldCodeList.size() > 0 ) {
						List<Map<String, Object>> employeeList = new java.util.ArrayList<Map<String, Object>>();

						Map<String, List<String>> orgEmpList = employeeSoldCodeList.stream().collect(
								Collectors.groupingBy(map -> (String)(map.get("organizationCode")),
										Collectors.mapping(map -> (String)(map.get("employeeId")),
												Collectors.toList())));

						List<String> defaultEmpList = orgEmpList.get("ORGALL");
						for( String organizationCode : organizationCodeArr ) {
							List<String> empList = orgEmpList.get(organizationCode);
							if( empList == null )
								empList = new java.util.ArrayList<>();
							if( defaultEmpList != null )
								empList.addAll( defaultEmpList );

							if( empList != null && empList.size() > 0 ) {
								String employeeIdCsv = String.join(";", empList.stream().distinct()
										.collect(Collectors.toList()));

								Map<String, Object> _record = new java.util.HashMap<String, Object> ();
								_record.put( "uniqId", uniqId );
								_record.put( "organizationCode", organizationCode );
								_record.put( "employeeId", employeeIdCsv );
								_record.put( "countryCode", countryCode );
								_record.put( "updateUserId", updateUserId );

								if( !MapUtil.containAllKeysAndValueNotNull(_record
										, new String[] {"uniqId", "organizationCode", "employeeId", "countryCode"}) )
									throw new IllegalStateException(
											handler.getMessageHandler().getMessage(DataException.ERR_INVALID_VALUE,
													String.format("%s: (%s)", "_record from employeeSoldCodeList", _record)) );

								employeeList.add( _record );
							}
						}
						if( employeeList != null && employeeList.size() > 0 ) {
							try {
								multiempDB.update( employeeList, uniqId, false );
								authDB.updateWithEmployeeIdCsv( employeeList, authConditionMap );
							} catch ( DataException dataEx ) {
								throw new SQLException( dataEx );
							}
						}
					}

					List<Map<String, Object>> partySoldCodeList = (List<Map<String, Object>>) userMap.get("partySoldCodeList");
					if( partySoldCodeList != null && partySoldCodeList.size() > 0 ) {
						List<Map<String, Object>> employeeList = new java.util.ArrayList<Map<String, Object>>();

						Map<String, List<String>> orgPtyList = partySoldCodeList.stream().collect(
								Collectors.groupingBy(map -> (String)(map.get("organizationCode")),
										Collectors.mapping(map -> (String)(map.get("soldPartyCode")),
												Collectors.toList())));

						List<String> defaultPtyList = orgPtyList.get("ORGALL");
						for( String organizationCode : organizationCodeArr ) {
							List<String> ptyList = orgPtyList.get(organizationCode);
							if( ptyList == null )
								ptyList = new java.util.ArrayList<>();
							if( defaultPtyList != null )
								ptyList.addAll( defaultPtyList );

							if( ptyList != null && ptyList.size() > 0 ) {
								String ptyCodeCsv = String.join(";", ptyList.stream().distinct()
										.collect(Collectors.toList()));

								Map<String, Object> _record = new java.util.HashMap<String, Object> ();
								_record.put( "uniqId", uniqId );
								_record.put( "organizationCode", organizationCode );
								_record.put( "employeeId", ptyCodeCsv );
								_record.put( "countryCode", countryCode );
								_record.put( "updateUserId", updateUserId );

								if( !MapUtil.containAllKeysAndValueNotNull(_record, new String[] {"uniqId", "organizationCode", "employeeId", "countryCode"}) )
									throw new IllegalStateException(
											handler.getMessageHandler().getMessage(DataException.ERR_INVALID_VALUE,
													String.format("%s: (%s)", "_record from partySoldCodeList", _record)) );

								employeeList.add( _record );
							}
						}

						if( employeeList != null && employeeList.size() > 0 ) {
							try {
								authDB.updateWithEmployeeIdCsv( employeeList, authConditionMap );
							} catch ( DataException dataEx ) {
								throw new SQLException( dataEx );
							}
						}
					}
				}
				super.complete(handler);
			}
		};
	}
}
