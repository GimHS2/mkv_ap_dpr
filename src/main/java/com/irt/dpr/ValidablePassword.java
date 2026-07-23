/*
 *	File Name:	ValidablePassword.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/10/30		2.2.1	validableCheckPassword(): HASH_ALGORITHM 에 따른 비밀번호 체크 방식으로 변경
 *	stghr12		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.sql.*;
import com.irt.data.DataException;
import com.irt.rbm.usr.UserUser;
import java.sql.SQLException;
import java.util.Map;


/**
 *
 */
public class ValidablePassword {
	public static boolean validableCheckPassword( SQLHandler handler, Map<String, Object> recordMap ) throws SQLException, DataException,InvalidPasswordException {
		String password = (String)recordMap.get( "password" );
		String partyId = (String)recordMap.get( "partyId" );
		String userId = (String)recordMap.get( "userId" );
		String userHashAlgorithm = (String)SQLManager.getObjectValue( handler,
				"SELECT HASH_ALGORITHM FROM USR_USER WHERE PARTYID = ? AND USER_ID = ?", partyId, userId );
		if( password == null ) return true;

		if( !com.irt.rbm.usr.UserUser.HASH_ALGORITHM_MD5.equals(userHashAlgorithm) && !com.irt.rbm.usr.UserUser.HASH_ALGORITHM_SHA2.equals(userHashAlgorithm) ) {
			userHashAlgorithm = com.irt.rbm.usr.UserUser.hashAlgorithm;
		}

		int bitmap = 0;
		for( int i = 0; i < password.length(); i++ ) {
			char ch = password.charAt(i);
			if( "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(ch) >= 0 )
				bitmap |= 0x01;
			else if( "abcdefghijklmnopqrstuvwxyz".indexOf(ch) >= 0 )
				bitmap |= 0x02;
			else if( "123456789".indexOf(ch) >= 0 )
				bitmap |= 0x04;
			else if( "{}[],.<>;:'\"?/|\\`~!@#$%^&*()_-+=".indexOf(ch) >= 0 )
				bitmap |= 0x08;
		}

		int complexity = 0;
		if( (bitmap & 0x01) > 0 ) complexity++;
		if( (bitmap & 0x02) > 0 ) complexity++;
		if( (bitmap & 0x04) > 0 ) complexity++;
		if( (bitmap & 0x08) > 0 ) complexity++;

		if( password.length() < 6 )
			throw new InvalidPasswordException( InvalidPasswordException.ERR_PASSWORD_MINIMUM_LENGTH );
		else if( password.length() < 8 ) {
			if( complexity < 3 )
				throw new InvalidPasswordException( InvalidPasswordException.ERR_PASSWORD_MINLENGTH_COMPLEXITY_6 );
		} else {
			if( complexity < 2 )
				throw new InvalidPasswordException( InvalidPasswordException.ERR_PASSWORD_MINLENGTH_COMPLEXITY_8 );
		}

		if( password.indexOf(userId) >= 0 || userId.indexOf(password) >= 0 )
			throw new InvalidPasswordException( InvalidPasswordException.ERR_PASSWORD_INCLUDING_USERINFO );

		String encryptedPassword = UserUser.getEncryptionPassword( password, userHashAlgorithm );
		// 암호 암호화
		int count = SQLManager.getInt( handler,
			"SELECT COUNT(*) FROM USR_USER_SECURITY "
				+ " WHERE PARTYID = ? AND USERID = ? AND ( PASSWORD1 = ? OR PASSWORD2 = ? OR PASSWORD3 = ? OR PASSWORD4 = ? OR PASSWORD5 = ? )"
				, partyId, userId, encryptedPassword, encryptedPassword, encryptedPassword, encryptedPassword, encryptedPassword );

		if( count > 0 )
			throw new InvalidPasswordException( InvalidPasswordException.ERR_PASSWORD_BEFORE_USED_PASSWORD );

		return ValidablePassword.updatePasswordHistory( handler, recordMap );
	}

	public static boolean updatePasswordHistory( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
		String password = (String)recordMap.get( "password" );
		String partyId = (String)recordMap.get( "partyId" );
		String userId = (String)recordMap.get( "userId" );

		int userExisting = SQLManager.getInt( handler,
			"SELECT COUNT(*) FROM USR_USER WHERE PARTYID = ? AND USER_ID = ?", partyId, userId );

		if( userExisting == 1 ) {
			String encryptedPassword = UserUser.getEncryptionPassword( password );

			int executeCount = SQLManager.executeStatement( handler,
				"UPDATE USR_USER_SECURITY"
					+ " SET PASSWORD5 = PASSWORD4, PASSWORD4 = PASSWORD3, PASSWORD3 = PASSWORD2, PASSWORD2 = PASSWORD1, PASSWORD1 = ?, UPGDATE = SYSDATE WHERE PARTYID = ? AND USERID = ?"
				, encryptedPassword, partyId, userId );

			int ret = 0;
			if( executeCount == 0 ) {
				ret = SQLManager.executeStatement( handler, "INSERT INTO USR_USER_SECURITY( PARTYID, USERID, PASSWORD1 ) VALUES( ?, ?, ? )", partyId, userId, password );
			}

			return ( executeCount > 0 || ret > 0 );
		}

		return true;
	}
}
