/*
 *	File Name:	Party.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getCheckDigit() -> calculateCheckDigit()
 *										getRecords(): appendMultiValue() 포함
 *										updateCompanyName() 추가
 *										PartyQuery 사용
 *										fieldKey 수정: "contactInfo" -> "contactList"
 *	stghr12		2007/10/31		2.1.2	getFieldValue( gln, fieldKey ) 추가
 *										PARTYROLE_SUPPLIER_DC, PARTYROLE_PLANT, PARTYROLE_CHILDSUPPLIER, PARTYROLE_PUBLIC 추가
 *										update(): 버그수정
 *										Party.FieldSet: extends com.irt.data.FieldSet -> extends com.irt.data.ValidableFieldSet
 *	stghr12		2007/04/30		2.1.1	validate(): GLN 검사 버그 수정
 *										PARTYROLE_* 추가
 *	stghr12		2006/12/01		2.1.0	ManipulableManager 변경사항 적용
 *										modifyAll(): modifyEach() 사용으로 수정
 *										registAll(): registEach() 사용으로 수정
 *										RBMDataManager.updateTable() 변경사항 적용
 *										Table.STATEMENT_* -> Record.*
 *	stghr12		2006/03/31		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
 **/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.rbm.RBMDataException;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class Party extends com.irt.rbm.ManipulableManagerImpl {
	public final static int INFO_PARTY					= 0x00000001;
	public final static int INFO_CONTACT				= 0x00000002;
	public final static int INFO_ALL					= 0x00000003;

	public final static String PARTYROLE_RETAIL			= "RTL";
	public final static String PARTYROLE_RETAIL_DC		= "RWH";
	public final static String PARTYROLE_STORE			= "STR";
	public final static String PARTYROLE_SUPPLIER		= "SUP";
	public final static String PARTYROLE_SUPPLIER_DC	= "SWH";
	public final static String PARTYROLE_PLANT			= "PLT";
	public final static String PARTYROLE_CHILDSUPPLIER	= "CSP";
	public final static String PARTYROLE_PUBLIC			= "PUB";

	private final static int IDX_CONTACT				= 0;
	private final static int IDX_PARTY					= 1;

	private final static Table[] tables = new Table[] {
			Schema.findTable( Schema.ECS_PARTY_CONTACT )
			, Schema.findTable( Schema.ECS_PARTY )
	};
	private final static QueryFactory[] factories = new QueryFactory[] {
			Schema.findQueryFactory( Schema.ECS_PARTY_CONTACT )
			, Schema.findQueryFactory( Schema.ECS_PARTY )
	};
	private final static QueryFactory factory = new QueryFactory( new PartyQuery(false) );

	public Party( SQLHandler handler ) {
		super( handler, tables[IDX_PARTY], factory );
	}

	Party( SQLHandler handler, QueryFactory factory ) {
		super( handler, tables[IDX_PARTY], factory );
	}

	public static char calculateCheckDigit( String prefix ) {
		int odd = 0, even = 0;

		for( int i = 0; i < prefix.length(); i+= 2 ) {
			if( prefix.charAt(i) < '0' || prefix.charAt(i) > '9' ) return ' ';
			odd += prefix.charAt(i) - '0';
		}
		for( int i = 1; i < prefix.length(); i+= 2 ) {
			if( prefix.charAt(i) < '0' || prefix.charAt(i) > '9' ) return ' ';
			even += prefix.charAt(i) - '0';
		}
		if( prefix.length() % 2 == 0 )
			return "0987654321".charAt( (odd + even * 3)%10 );
		else
			return "0987654321".charAt( (odd * 3 + even)%10 );
	}

	public static boolean checkGln( String gln ) {
		return ( calculateCheckDigit(gln+"0") == '0' );
	}

	public static Map<String, Object> createPrimary( String gln ) {
		return Record.createMap( "gln", gln );
	}

	public boolean delete( Map<String, Object> primaryMap, int infoType ) throws DataException, SQLException {
		if( (infoType & INFO_PARTY) > 0 ) return delete( primaryMap );

		int resultCnt = 0;
		Object[] primaryVars = Record.extractValues( primaryMap, new String[] { "gln" } );

		if( (infoType & INFO_CONTACT) > 0 ) {
			if( primaryMap.containsKey("contactName") ) {
				Object[] bindVars = new Object[] { primaryVars[0], Record.extractValue(primaryMap, "contactName") };
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_PARTY_CONTACT WHERE GLN = ? AND CONTACT_NAME = ?", bindVars );
			} else
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_PARTY_CONTACT WHERE GLN = ?", primaryVars );
		}

		return ( resultCnt > 0 );
	}

	@Override
	public AbstractFieldSet getFieldSet( boolean inserting ) {
		return Party.FieldSet.getInstance( inserting );
	}

	public Object getFieldValue( String gln, String fieldKey ) throws IllegalArgumentException, SQLException {
		return getFieldValue( createPrimary(gln), fieldKey );
	}

	@Override
	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException {
		return getRecord( primaryMap, INFO_ALL );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int infoType ) throws SQLException {
		Map<String, Object> recordMap = super.getRecord( primaryMap );
		if( recordMap == null ) return null;

		if( (infoType & INFO_CONTACT) > 0 ) {
			QueryBuffer querybuf = factories[IDX_CONTACT].setDataQuery( new QueryBuffer() );
			querybuf.appendCondition( "PCT.GLN = ?", Record.extractValue(recordMap, "gln") );
			recordMap.put( "contactList", SQLManager.getRecordList(handler, querybuf) );
		}

		return recordMap;
	}

	@Override
	public boolean modify( Map<String, Object> recordMap ) throws DataException, SQLException {
		return update( recordMap, INFO_ALL, false );
	}

	public boolean modify( Map<String, Object> recordMap, int infoType ) throws DataException, SQLException {
		return update( recordMap, infoType, false );
	}

	@Override
	public DataResult modifyAll( Collection<Map<String, Object>> records ) throws SQLException {
		return modifyEach( records );
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		return update( recordMap, INFO_ALL, true );
	}

	public boolean regist( Map<String, Object> recordMap, int infoType ) throws DataException, SQLException {
		return update( recordMap, infoType, true );
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}

	private boolean update( Map<String, Object> recordMap, int infoType, boolean inserting ) throws DataException, SQLException {
		Party.FieldSet.getInstance( inserting ).validate( handler, recordMap );
		Object[] primaryVars = Record.extractValues( recordMap, new String[] { "gln" } );

		// ECS_PARTY
		if( inserting )
			SQLManager.manageRecord( handler, tables[IDX_PARTY], recordMap, Record.INSERT );
		else if( (infoType & INFO_PARTY) > 0 )
			SQLManager.manageRecord( handler, tables[IDX_PARTY], recordMap, Record.UPDATE );

		String systemDateTime = (String)SQLManager.getObjectValue( handler,
				"SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM ECS_PARTY WHERE GLN = ?"
				, primaryVars );
		if( systemDateTime == null ) return false;
		Object[] bindVars = new Object[] { primaryVars[0], systemDateTime };

		// ECS_PARTY_CONTACT
		if( (infoType & INFO_CONTACT) > 0 ) {
			int statementType = inserting ? Record.INSERT : (Record.INSERT | Record.UPDATE);
			updateTable( tables[IDX_CONTACT], recordMap, new String[] { "gln", "updateUserId" }, "contactList", statementType );
			if( !inserting ) {
				String statement = "DELETE ECS_PARTY_CONTACT WHERE GLN = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
				SQLManager.executeStatement( handler, statement, bindVars );
			}
		}

		return true;
	}

	public int updateCompanyName( String parentGln, String companyName, String updateUserId ) throws DataException, SQLException {
		String statement = "UPDATE ECS_PARTY SET COMPANY_NAME = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE PARENT_GLN = ? AND COMPANY_NAME <> ?";

		ValidableField field_gln = (ValidableField)tables[IDX_PARTY].getField( "companyName" );
		try {
			field_gln.validate( companyName );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, null );
		}

		return SQLManager.executeStatement( handler, statement, companyName, updateUserId, parentGln, companyName );
	}

	/**
	 *
	 */
	static class FieldSet extends com.irt.data.ValidableFieldSet {
		private boolean inserting;
		private static FieldSet fieldSet_i, fieldSet_u;
		private ValidableField field_gln = (ValidableField)Schema.findTable(Schema.ECS_PARTY).getField("gln");

		private FieldSet( boolean inserting, Map<String, ValidableField> fieldMap ) {
			super( fieldMap );
			this.inserting = inserting;
		}

		private static void append( Map<String, ValidableField> fieldMap, Table table ) {
			for( ValidableField field : table.getFieldMap().values() ) {
				String fieldKey = field.getFieldKey();

				if( "gln".equals(fieldKey) ) continue;
				if( !field.nullable() ) field = new ValidableField( field, true, field.readonly() );
				fieldMap.put( fieldKey, field );
			}
		}

		public static FieldSet getInstance( boolean inserting ) {
			if( fieldSet_i == null ) initialize();
			return( inserting ? fieldSet_i : fieldSet_u );
		}

		private static synchronized void initialize() {
			if( fieldSet_i != null ) return;

			Map<String, ValidableField> fieldMap = new java.util.HashMap<String, ValidableField>();
			append( fieldMap, Schema.findTable(Schema.ECS_PARTY_CONTACT) );

			Map<String, ValidableField> fieldMap_i = fieldMap;
			Map<String, ValidableField> fieldMap_u = new java.util.HashMap<String, ValidableField>( fieldMap );

			Table table = Schema.findTable( Schema.ECS_PARTY );
			fieldMap_i.putAll( table.getFieldMap() );
			fieldMap_u.putAll( table.getValidableFieldSet(false).getFieldMap() );

			fieldSet_i = new FieldSet( true, fieldMap_i );
			fieldSet_u = new FieldSet( false, fieldMap_u );
		}

		public void validate( SQLHandler handler, Map recordMap ) throws DataException {
			if( inserting ) {
				try {
					String gln = (String)field_gln.validate( recordMap );
					Object glnType = Record.extractValue( recordMap, "glnType" );
					if( "GLN".equals(glnType) || "PRV".equals(glnType) ) {
						if( gln.length() != 13 ) throw new FieldException( FieldException.ERR_INVALID_LENGTH, field_gln, gln );
						if( !Party.checkGln(gln) ) throw handler.createDataException( RBMDataException.ERR_INVALID_GLN, recordMap );
					}
				} catch( FieldException fieldEx ) {
					throw handler.createDataException( fieldEx, recordMap );
				}
			}
		}
	}
}
