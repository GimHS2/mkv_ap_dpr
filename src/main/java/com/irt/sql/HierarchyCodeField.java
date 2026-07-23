/*
 *	File Name:	HierarchyCodeField.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	kls1989		2014/03/31		2.2.3	getAuthorizedCodeValues(): 제외 권한 처리 오류 수정
 *	stghr12		2010/07/31		2.2.2	getAuthorizedCodeValues(), getLevel(Object) 추가
 *	stghr12		2009/10/31		2.2.1	getSharedUpperLevelCode() -> getCommonUpperLevelCode()
 *										getFullNameQueryableField() 추가
 *	stghr12		2008/03/31		2.2.0	getClassCodeField() 추가
 *	stghr12		2007/04/30		2.1.0	getLevelCode(), getLevelCodeQuery(classFieldName, codeFieldName), getSharedUpperLevelCode() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import com.irt.data.FieldException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class HierarchyCodeField extends Table.Field {
	public final static String NAME_SEPARATOR			= " - ";

	int[] codeLengths;
	Table.Field classCodeField;

	public HierarchyCodeField( Table.Field tfield, Table.Field classCodeField, int[] codeLengths ) {
		super( tfield );
		this.classCodeField = classCodeField;
		this.codeLengths = codeLengths;
	}

	public static String[] getAuthorizedCodeValues( String[] codes, String[] authorizedCodes ) {
		return getAuthorizedCodeValues( codes, authorizedCodes, null );
	}

	public static String[] getAuthorizedCodeValues( String[] codes, String[] authorizedCodes, String[] unauthorizedCodes ) {
		if( codes == null || codes.length == 0 ) return authorizedCodes;

		List<String> codeList = new java.util.ArrayList<String>( java.util.Arrays.asList(codes) );
		if( authorizedCodes != null ) {
			codeList = new java.util.ArrayList<String>();

			for( String code : codes ) {
				for( String authorizedCode : authorizedCodes ) {
					if( code.startsWith(authorizedCode) ) {
						codeList.add( code );
						break;
					} else if( authorizedCode.startsWith(code) )
						codeList.add( authorizedCode );
				}
			}
		}

		if( unauthorizedCodes != null ) {
			for( java.util.Iterator<String> iterator = codeList.iterator(); iterator.hasNext(); ) {
				String code = iterator.next();

				for( String unauthorizedCode : unauthorizedCodes )
					if( code.startsWith(unauthorizedCode) )
						iterator.remove();
				break;
			}
		}

		if( codeList.size() == 0 )
			return null;
		else
			return codeList.toArray( new String[codeList.size()] );
	}

	public Table.Field getClassCodeField() {
		return classCodeField;
	}

	public String getCommonUpperLevelCode( String ... codes ) {
		if( codes == null || codes.length == 0 ) return null;
		if( codes.length == 1 || codes[0] == null ) return codes[0];

		String code = codes[0];

		int length = code.length();
		for( int i = 1; i < codes.length && length > 0; i++ ) {
			int len = 0;

			if( codes[i] == null ) return null;
			while( len < length && len < codes[i].length() && code.charAt(len) == codes[i].charAt(len) ) len++;
			length = len;
		}
		if( length == 0 ) return null;

		for( int l = 0; l < codeLengths.length; l++ ) {
			if( codeLengths[l] == length )
				return code.substring( 0, length );
			else if( codeLengths[l] > length ) {
				if( l == 0 ) return null;
				return code.substring( 0, codeLengths[l-1] );
			}
		}

		return code.substring( 0, codeLengths[codeLengths.length - 1] );
	}

	public QueryableField getFullNameQueryableField( char dataType, String fieldKey, String fieldName, String descriptionKey ) {
		if( codeLengths == null )
			return new QueryableFieldImpl( dataType, fieldKey, table.alias +"."+ fieldName, descriptionKey );

		Joinable[] joinables = new Joinable[ codeLengths.length ];
		for( int level = 1; level <= codeLengths.length; level++ )
			joinables[level-1] = new JoinableImpl(
				table.alias + level
				, table.getTableName()
				, table.alias + level +"."+ getFieldName() +"(+) = SUBSTRB("+ table.alias +"."+ getFieldName() +", 1, "+ codeLengths[level-1] +")"
			);

		StringBuffer sbuf = new StringBuffer();
		String classCodeName = table.alias +"."+ classCodeField.getFieldName();
		for( int level = 1; level <= codeLengths.length; level++ ) {
			String nameFieldName = table.alias + level +"."+ fieldName;

			if( level == 1 )
				sbuf.append( nameFieldName );
			else
				sbuf.append( " || (CASE WHEN "+ classCodeName +" >= '"+ level +"' THEN '"+ NAME_SEPARATOR +"' || "+ nameFieldName +" END)" );
		}

		return new QueryableFieldImpl( dataType, fieldKey, sbuf.toString(), descriptionKey, new JoinableWrapper(joinables) );
	}

	public int getLastLevel() {
		try {
			return codeLengths.length;
		} catch( NullPointerException nullEx ) {
			return -1;
		}
	}

	public int getLength( int level ) {
		try {
			return codeLengths[level - 1];
		} catch( ArrayIndexOutOfBoundsException arrayEx ) {
			return -1;
		} catch( NullPointerException nullEx ) {
			return -1;
		}
	}

	public int getLength( Map recordMap ) {
		try {
			String classCode = (String)classCodeField.extractValue( recordMap );
			if( classCode != null && classCode.length() == 1 ) {
				try {
					return codeLengths[classCode.charAt(0) - '1'];
				} catch( ArrayIndexOutOfBoundsException arrayEx ) {
				} catch( NullPointerException nullEx ) {}
			}
		} catch( ClassCastException castEx ) {
		} catch( FieldException fieldEx ) {}

		return -1;
	}

	public int getLevel( String code ) {
		if( code == null || code.length() == 0 ) return 0;
		int length = code.length();

		try {
			for( int l = 0; l < codeLengths.length; l++ )
				if( codeLengths[l] >= length ) return (l+1);

			return codeLengths.length;
		} catch( NullPointerException nullEx ) {
			return -1;
		}
	}

	public int getLevel( Object code ) {
		if( code instanceof String )
			return getLevel( (String)code );
		else if( code instanceof String[] ) {
			String code_max = "";

			for( String code1 : (String[])code ) {
				if( code1 != null && code1.length() > code_max.length() )
					code_max = code1;
			}

			return getLevel( code_max );
		} else
			return 0;
	}

	public String getLevelCode( String code ) {
		return getLevelCode( code, getLevel(code) );
	}

	public String getLevelCode( String code, int level ) {
		if( --level < 0 ) return "";

		if( code.length() <= codeLengths[level] ) {
			try {
				return code.substring( level == 0 ? 0 : codeLengths[level-1] );
			} catch( IndexOutOfBoundsException idxEx ) {
				return "";
			}
		} else
			return code.substring( level == 0 ? 0 : codeLengths[level-1], codeLengths[level] );
	}

	public String getLevelCodeQuery() {
		return getLevelCodeQuery( table.alias +"."+ classCodeField.getFieldName(), table.alias +"."+ getFieldName() );
	}

	public String getLevelCodeQuery( String classFieldName, String codeFieldName ) {
		if( codeLengths == null ) return codeFieldName;

		StringBuffer sbuf = new StringBuffer();
		sbuf.append( "DECODE( " ).append( classFieldName ).append( ", '1', " ).append( codeFieldName );

		for( int l = 1; l < codeLengths.length; l++ ) {
			sbuf.append( ", '" ).append( l + 1 ).append( "'" )
				.append( ", SUBSTRB(" ).append( codeFieldName ).append( ", " ).append( codeLengths[l-1] + 1 ).append( ")" );
		}
		sbuf.append( ", " ).append( codeFieldName ).append( ")" );

		return sbuf.toString();
	}

	public String getUpperLevelCode( String code ) {
		int level = getLevel( code );
		if( level < 1 || codeLengths == null )
			return null;
		else if( level == 1 )
			return "";
		else
			return code.substring( 0, codeLengths[level - 2] );
	}

	public String getUpperLevelCode( String code, int level ) {
		if( code == null || level < 0 || codeLengths == null )
			return null;
		else if( level == 0 )
			return "";
		else if( level > getLevel(code) )
			return null;
		else
			return code.substring( 0, codeLengths[level - 1] );
	}

	public String[] getUpperLevelCodes( String code ) {
		return getUpperLevelCodes( code, false );
	}

	public String[] getUpperLevelCodes( String code, boolean containsSelf ) {
		int level = getLevel( code );
		if( level <= 0 || codeLengths == null ) return null;

		String[] codes;
		if( containsSelf ) {
			codes = new String[level];
			codes[--level] = code;
		} else
			codes = new String[--level];

		for( int l = 0; l < level; l++ )
			codes[l] = code.substring( 0, codeLengths[l] );

		return codes;
	}

	public Object validate( Map recordMap ) throws FieldException {
		String value = (String)super.validate( extractValue(recordMap), true );
		int codeLength = getLength( recordMap );
		if( codeLength > 0 && value != null && value.length() != codeLength )
			throw new FieldException( FieldException.ERR_INVALID_LENGTH, this, value );

		return value;
	}
}
