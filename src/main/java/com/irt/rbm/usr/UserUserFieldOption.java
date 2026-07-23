/*
 *	File Name:	UserUserFieldOption.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	getInstance(): 로직수정
 *										getMaxLength() 추가
 *	stghr12		2007/11/30		2.2.0	재작성( com.irt.data.FieldOptionSet 사용 )
 *	stghr12		2007/10/31		2.1.1	makeRecordMap() 오류 수정
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.FieldOptionSet;
import com.irt.data.Record;
import com.irt.rbm.RBMSystem;
import java.util.Map;

/**
 *
 */
public class UserUserFieldOption extends com.irt.data.FieldOptionSet {
	String userClass;

	private UserUserFieldOption( String userClass, FieldOptionSet.Field[] fields ) {
		super( fields );
		this.userClass = userClass;
	}

	public void applyOptionToRecordMap( Map<String, Object> recordMap, Map<String, Object> originalMap ) {
		if( hasManageAuth("userClass") ) {
			String userClass_new = Record.extractString( recordMap, "userClass" );
			if( !UserUser.USERCLASS_SYSTEM_ADMIN.equals(userClass) ) {
				if( UserUser.USERCLASS_SYSTEM_ADMIN.equals(userClass_new) )
					recordMap.remove( "userClass" );
				else if( !UserUser.USERCLASS_PARTY_ADMIN.equals(userClass) && UserUser.USERCLASS_PARTY_ADMIN.equals(userClass_new) )
					recordMap.remove( "userClass" );
			}
		}

		super.applyOptionToRecordMap( recordMap, originalMap );
	}

	private static FieldOptionSet.Field createField( String userClass, String fieldKey, Object defaultValue ) {
		String optionValue = RBMSystem.getSystemEnv( "USR", "UserFieldOption;"+ fieldKey );
		if( optionValue == null || "NONE".equals(optionValue) )
			optionValue = "NN";
		else if( optionValue.startsWith(UserUser.USERCLASS_USER) )
			optionValue = "YY";
		else if( optionValue.startsWith(UserUser.USERCLASS_PARTY_ADMIN) ) {
			if( UserUser.USERCLASS_PARTY_ADMIN.equals(userClass) || UserUser.USERCLASS_SYSTEM_ADMIN.equals(userClass) )
				optionValue = "YY";
			else if( optionValue.endsWith("Only") )
				optionValue = "NN";
			else
				optionValue = "YN";
		} else if( optionValue.startsWith(UserUser.USERCLASS_SYSTEM_ADMIN) ) {
			if( UserUser.USERCLASS_SYSTEM_ADMIN.equals(userClass) )
				optionValue = "YY";
			else if( optionValue.endsWith("Only") )
				optionValue = "NN";
			else
				optionValue = "YN";
		} else
			optionValue = "NN";

		return new FieldOptionSet.Field( fieldKey, optionValue.charAt(0) == 'Y', optionValue.charAt(1) == 'Y', defaultValue );
	}

	public static UserUserFieldOption getInstance( String userClass ) {
		FieldOptionSet.Field[] fields = new FieldOptionSet.Field[] {
			createField( userClass, "serviceDate", null )
			, createField( userClass, "serviceDate", null )
			, createField( userClass, "userClass", null )
			, createField( userClass, "status", null )
			, createField( userClass, "userGln", null )
			, createField( userClass, "groupId", null )
			, createField( userClass, "availAccessCount", RBMSystem.getSystemEnv("USR", "UserFieldOption;availAccessCountDefault", "1") )
			, createField( userClass, "extraValue1", RBMSystem.getSystemEnv("USR", "UserFieldOption;extraValue1Default") )
			, createField( userClass, "extraValue2", RBMSystem.getSystemEnv("USR", "UserFieldOption;extraValue2Default") )
			, createField( userClass, "extraValue3", RBMSystem.getSystemEnv("USR", "UserFieldOption;extraValue3Default") )
		};
		fields[0] = new FieldOptionSet.Field( "serviceStartDate", fields[0].using(), fields[0].hasManageAuth() );
		fields[1] = new FieldOptionSet.Field( "serviceEndDate", fields[1].using(), fields[1].hasManageAuth() );
		fields[2] = new FieldOptionSet.Field( "userClass", true, fields[2].hasManageAuth(), UserUser.USERCLASS_USER );
		fields[3] = new FieldOptionSet.Field( "status", true, fields[3].hasManageAuth(), "PW" );

		return new UserUserFieldOption( userClass, fields );
	}

	public int getMaxLength( String fieldKey ) {
		if( "userId".equals(fieldKey) )
			return RBMSystem.getSystemEnvInt( "USR", "UserFieldOption;userIdLength", 10 );
		else
			return -1;
	}

	public String getVaildAccessCountValues() {
		return RBMSystem.getSystemEnv( "USR", "UserFieldOption;availAccessCountValues", "1,3,5,0" );
	}

	public String getValidUserClassValues() {
		if( UserUser.USERCLASS_SYSTEM_ADMIN.equals(userClass) )
			return UserUser.USERCLASS_USER +","+ UserUser.USERCLASS_PARTY_ADMIN +","+ UserUser.USERCLASS_SYSTEM_ADMIN;
		else if( UserUser.USERCLASS_PARTY_ADMIN.equals(userClass) )
			return UserUser.USERCLASS_USER +","+ UserUser.USERCLASS_PARTY_ADMIN;
		else
			return UserUser.USERCLASS_USER;
	}
}
