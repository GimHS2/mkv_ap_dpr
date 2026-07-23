/*
 *	File Name:	UserPartyFieldOption.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.1c	PartyLoginBlock 기능 추가
 *	stghr12		2008/03/31		2.2.1	getInstance(): 로직수정
 *										getMaxLength() 추가
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.FieldOptionSet;
import com.irt.rbm.RBMSystem;

/**
 *
 */
public class UserPartyFieldOption extends com.irt.data.FieldOptionSet {
	private UserPartyFieldOption( FieldOptionSet.Field[] fields ) {
		super( fields );
	}

	private static FieldOptionSet.Field createField( String userClass, boolean managableByPA, String fieldKey, Object defaultValue ) {
		String optionValue = RBMSystem.getSystemEnv( "USR", "PartyFieldOption;"+ fieldKey );

		if( managableByPA && UserUser.USERCLASS_PARTY_ADMIN.equals(optionValue) )
			return new FieldOptionSet.Field( fieldKey, true, true, defaultValue );
		else if( UserUser.USERCLASS_SYSTEM_ADMIN.equals(optionValue) )
			return new FieldOptionSet.Field( fieldKey, true, UserUser.USERCLASS_SYSTEM_ADMIN.equals(userClass), defaultValue );
		else if( (UserUser.USERCLASS_SYSTEM_ADMIN +"Only").equals(optionValue) ) {
			if( UserUser.USERCLASS_SYSTEM_ADMIN.equals(userClass) )
				return new FieldOptionSet.Field( fieldKey, true, true, defaultValue );
		}

		return new FieldOptionSet.Field( fieldKey, false, false, defaultValue );
	}

	public static UserPartyFieldOption getInstance( String userClass ) {
		FieldOptionSet.Field[] fields = new FieldOptionSet.Field[] {
			createField( userClass, false, "serviceDate", null )
			, createField( userClass, false, "serviceDate", null )
			, createField( userClass, false, "status", null )
			, createField( userClass, true, "blockIsoDate", null )
			, createField( userClass, true, "blockIsoDate", null )
			, createField( userClass, true, "mtnceIsoDate", null )
			, createField( userClass, true, "mtnceIsoDate", null )
			, createField( userClass, false, "blockTemplate", null )
			, createField( userClass, false, "mtnceTemplate", null )
			, createField( userClass, false, "partyGln", null )
			, createField( userClass, false, "chargeStartDate", null )
			, createField( userClass, false, "partyClass", RBMSystem.getSystemEnv("USR", "PartyFieldOption;partyClassDefault") )
			, createField( userClass, true, "partyRegistration", null )
			, createField( userClass, true, "telephone", null )
			, createField( userClass, true, "extraValue1", RBMSystem.getSystemEnv("USR", "PartyFieldOption;extraValue1Default") )
			, createField( userClass, true, "extraValue2", RBMSystem.getSystemEnv("USR", "PartyFieldOption;extraValue2Default") )
			, createField( userClass, true, "extraValue3", RBMSystem.getSystemEnv("USR", "PartyFieldOption;extraValue3Default") )
		};
		fields[0] = new FieldOptionSet.Field( "serviceStartDate", fields[0].using(), fields[0].hasManageAuth() );
		fields[1] = new FieldOptionSet.Field( "serviceEndDate", fields[1].using(), fields[1].hasManageAuth() );
		fields[2] = new FieldOptionSet.Field( "status", true, fields[2].hasManageAuth() );
		fields[3] = new FieldOptionSet.Field( "blockStartIsoDate", fields[3].using(), fields[3].hasManageAuth() );
		fields[4] = new FieldOptionSet.Field( "blockEndIsoDate", fields[4].using(), fields[4].hasManageAuth() );
		fields[5] = new FieldOptionSet.Field( "mtnceStartIsoDate", fields[5].using(), fields[5].hasManageAuth() );
		fields[6] = new FieldOptionSet.Field( "mtnceEndIsoDate", fields[6].using(), fields[6].hasManageAuth() );

		return new UserPartyFieldOption( fields );
	}

	public int getMaxLength( String fieldKey ) {
		if( "partyId".equals(fieldKey) )
			return RBMSystem.getSystemEnvInt( "USR", "PartyFieldOption;partyIdLength", 10 );
		else
			return -1;
	}
}
