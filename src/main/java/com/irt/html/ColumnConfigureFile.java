/*
 *	File Name:	ColumnConfigureFile.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/01/30		2.2.2c	AbstractServletModel에서 ctx.mode가 "down"으로 시작시 OPTIONKEY_DELETE_HTML이 기본으로 적용
 *	jbaek		2018/08/30		2.2.2c	Reloadable추가. columnTitle만 변경 가능하도록 추가
 *	stghr12		2008/05/31		2.2.2	COMMAND_PROPERTYVALUE 설정방식 변경.
 *	stghr12		2008/03/31		2.2.1	COMMAND_OPTIONSET 추가, BOOL_CHECK_DUPLICATE -> ignoreDuplicated
 *										GRP, COL에서 {title} -> {titlePattern}
 *										SFX.{suffixKey} {suffixPattern} 추가
 *										ColumnListTemplate 사용: columnList를 사용하는 시점에서 생성하도록 로직변경
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										*.conf -> *.properties
 *										getColumnResourceBundle() 읽는 순서 변경: Locale -> Default Locale -> No Locale
 *										com.irt.data.cols 변경사항 적용: columnListType 추가
 *	stghr12		2007/10/31		2.1.3	IllegalArgumentException message 변경: invalid -> illegal
 *										CONDITION_CONTAINS 추가
 *										COMMAND_PARENTCOLUMNPOOL 추가
 *										COMMAND_INCLUDEKEYS: GRP추가시 GRP에 속한 COL도 함께 추가하도록 수정
 *										COMMAND_INCLUDEFILE: locale 사용
 *	stghr12		2007/07/31		2.1.2	MessageHandler 추가: columnName에 PatternRecordFormat 지원
 *										COMMAND_PROPERTYVALUE 추가, loadColumnList()에서 COMMAND_PROPERTYVALUE 처리 로직 추가
 *	stghr12		2007/04/30		2.1.1	OPTIONKEY_DELETE_HTML 추가
 *										columnPoolType 지원(HTML/HSSF)
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.Configure -> com.irt.html.ColumnConfigureFile
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnGroup;
import com.irt.data.cols.ColumnPool;
import com.irt.data.cols.ColumnResourceBundle;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.html.*;
import com.irt.util.Arrays;
import com.irt.util.HSSF;
import com.irt.util.MessageHandler;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class ColumnConfigureFile {
	final static String COMMAND_PARENTRESOURCE			= "%parentResourceBundle(";
	final static String COMMAND_INCLUDEFILE				= "%includeResourceFile(";
	final static String COMMAND_OPTIONSET				= "%optionSet(";

	final static String COMMAND_PARENTCOLUMNPOOL		= "%parentColumnPool(";
	final static String COMMAND_COLUMNLISTTYPEKEYS		= "%columnListTypeKeys(";
	final static String COMMAND_DEFAULTSORTKEYS			= "%defaultSortKeys(";
	final static String COMMAND_INCLUDEKEYS				= "%includeKeys(";
	final static String COMMAND_HIDDENKEYS				= "%hiddenKeys(";
	final static String COMMAND_PRIMARYKEYS				= "%primaryKeys(";
	final static String COMMAND_PROPERTYVALUE			= "%propertyValue(";

	private final static char BLOCK_COLUMNLIST			= 'L';
	private final static char BLOCK_COLUMNPOOL			= 'P';
	private final static char BLOCK_FIELDKEYARRAY		= 'F';

	private final static char COLUMNPOOLTYPE_HSSF		= 'X';
	private final static char COLUMNPOOLTYPE_HTML		= 'H';

	public final static String OPTIONKEY_DELETE_HTML	= "delH";
	public final static String OPTIONKEY_NODEL_HTML	= "nodelH";
	public final static String COLUMNPOOL_HSSF_STRING	= ".HSSF";

	static Map<String, ColumnResourceBundle> resourceMap
			= java.util.Collections.synchronizedMap( new java.util.HashMap<String, ColumnResourceBundle>() );
	static ColumnResourceBundle blankResourceBundle		= new ColumnResourceBundleImpl( null );

	ColumnResourceBundleImpl columnResourceBundleImpl;
	ColumnConfigureFile.LineReader reader;
	MessageHandler msghandler;
	Locale locale;
	ClassLoader loader;

	boolean ignoreDuplicated = false;

	private ColumnConfigureFile() {}

	public static ColumnResourceBundle getColumnResourceBundle( String baseName, MessageHandler msghandler ) throws IOException {
		return getColumnResourceBundle( null, baseName, msghandler, Locale.getDefault(), ClassLoader.getSystemClassLoader() );
	}

	public static ColumnResourceBundle getColumnResourceBundle( String baseName, MessageHandler msghandler, Locale locale ) throws IOException {
		return getColumnResourceBundle( null, baseName, msghandler, locale, ClassLoader.getSystemClassLoader() );
	}

	public static ColumnResourceBundle getColumnResourceBundle( String baseName, MessageHandler msghandler, Locale locale, ClassLoader loader )
						throws IOException {
		return getColumnResourceBundle( null, baseName, msghandler, locale, loader );
	}

	public static ColumnResourceBundle getColumnResourceBundle( ColumnResourceBundle parent, String baseName, MessageHandler msghandler )
						throws IOException {
		return getColumnResourceBundle( parent, baseName, msghandler, Locale.getDefault(), ClassLoader.getSystemClassLoader() );
	}

	public static ColumnResourceBundle getColumnResourceBundle( ColumnResourceBundle parent, String baseName, MessageHandler msghandler
						, Locale locale ) throws IOException {
		return getColumnResourceBundle( parent, baseName, msghandler, locale, ClassLoader.getSystemClassLoader() );
	}

	public static ColumnResourceBundle getColumnResourceBundle( ColumnResourceBundle parent, String baseName, MessageHandler msghandler
						, Locale locale, ClassLoader loader ) throws IOException {
		Locale[] locales = new Locale[3];
		locales[0] = locale;
		locales[1] = Locale.getDefault();
		locales[ locale.equals(locales[1]) ? 1 : 2 ] = null;
		baseName = baseName.replace( '.', '/' );

		String resourceName = null;
		ColumnResourceBundle resourceBundle = null;
		Stack<String> stack = new Stack<String>();

		for( int i = 0; true; i++ ) {
			if( locales[i] == null ) {
				if( (resourceBundle = resourceMap.get(resourceName = baseName)) != null ) break;
				if( (resourceBundle = load(parent, resourceName, msghandler, locale, loader)) != null ) break;
				stack.push( resourceName );
				break;
			}
			String language = locales[i].getLanguage();
			String country = locales[i].getCountry();
			String variant = locales[i].getVariant();
			if( language != null && language.length() > 0 ) {
				if( country != null && country.length() > 0 ) {
					if( variant != null && variant.length() > 0 ) {
						if( (resourceBundle = resourceMap.get(resourceName = baseName +"_"+ language +"_"+ country +"_"+ variant)) != null ) break;
						if( (resourceBundle = load(parent, resourceName, msghandler, locale, loader)) != null ) break;
						stack.push( resourceName );
					}
					if( (resourceBundle = resourceMap.get(resourceName = baseName +"_"+ language +"_"+ country)) != null ) break;
					if( (resourceBundle = load(parent, resourceName, msghandler, locale, loader)) != null ) break;
					stack.push( resourceName );
				}
				if( (resourceBundle = resourceMap.get(resourceName = baseName +"_"+ language)) != null ) break;
				if( (resourceBundle = load(parent, resourceName, msghandler, locale, loader)) != null ) break;
				stack.push( resourceName );
			}
		}
		if( resourceBundle == null ) resourceBundle = blankResourceBundle;

		synchronized( resourceMap ) {
			ColumnResourceBundle sourceBundle = resourceMap.get( resourceName );
			if( sourceBundle == null )
				resourceMap.put( resourceName, resourceBundle );
			else
				resourceBundle = sourceBundle;

			while( !stack.empty() ) {
				String key = stack.pop();
				if( !resourceMap.containsKey(key) )
					resourceMap.put( key, resourceBundle );
			}
		}

		return ( resourceBundle == blankResourceBundle ? null : resourceBundle );
	}

	private static ColumnResourceBundle load( ColumnResourceBundle parent, String resourceName, MessageHandler msghandler
						, Locale locale, ClassLoader loader ) throws IOException {
		InputStream inputStream = loader.getResourceAsStream( resourceName = resourceName +".properties" );
		if( inputStream == null ) return null;

		ColumnConfigureFile configure = new ColumnConfigureFile();
		try {
			configure.columnResourceBundleImpl = new ColumnResourceBundleImpl( parent );
			configure.reader = new ColumnConfigureFile.LineReader( resourceName, inputStream );
			configure.msghandler = msghandler;
			configure.locale = locale;
			configure.loader = loader;
			return configure.load();
		} finally {
			try { configure.reader.close(); } catch( Exception ex ) {}
			try { inputStream.close(); } catch( Exception ex ) {}
		}
	}

	private ColumnResourceBundle load() throws IOException {
		ColumnConfigureFile.Line line = reader.readLine();
		if( line != null && line.buffer != null ) {
			String buffer = line.buffer.trim();

			if( buffer.startsWith(COMMAND_PARENTRESOURCE) && buffer.endsWith(")") ) {
				if( columnResourceBundleImpl.parent != null )
					throw line.throwIOException( "already exist parent ColumnResourceBundle" );

				String baseName = buffer.substring( buffer.indexOf('(') + 1, buffer.lastIndexOf(')') ).trim();
				columnResourceBundleImpl.parent = getColumnResourceBundle( null, baseName, msghandler, locale, loader );
				if( columnResourceBundleImpl.parent == null )
					throw line.throwIOException( "cannot load configure file '"+ baseName +"'" );
			} else
				reader.rollback( line );
		}

		while( loadBlock() );

		return columnResourceBundleImpl;
	}

	private boolean loadBlock() throws IOException {
		ColumnConfigureFile.Line line = null;

		// read Block Header
		ColumnConfigureFile.Line hline = null;
		while( hline == null && (line = reader.readLine()) != null ) {
			if( line.buffer == null ) continue;

			String buffer = line.buffer;
			switch( buffer.charAt(0) ) {
			case '\t':
			case ' ':
				throw line.throwIOException( "illegal block header" );
			case '%':
				buffer = buffer.trim();
				if( buffer.startsWith(COMMAND_INCLUDEFILE) && buffer.endsWith(")") ) {
					String baseName = buffer.substring( buffer.indexOf('(') + 1, buffer.lastIndexOf(')') ).trim().replace( '.', '/' );
					String resourceName = null;
					InputStream inputStream = null;

					Locale[] locales = new Locale[3];
					locales[0] = locale;
					locales[1] = Locale.getDefault();
					locales[ locale.equals(locales[1]) ? 1 : 2 ] = null;
					for( int i = 0; true; i++ ) {
						if( locales[i] == null ) {
							inputStream = loader.getResourceAsStream( resourceName = baseName +".properties" );
							break;
						}
						String language = locales[i].getLanguage();
						String country = locales[i].getCountry();
						String variant = locales[i].getVariant();
						if( language != null && language.length() > 0 ) {
							if( country != null && country.length() > 0 ) {
								if( variant != null && variant.length() > 0 ) {
									resourceName = baseName +"_"+ language +"_"+ country +"_"+ variant +".properties";
									inputStream = loader.getResourceAsStream( resourceName );
									if( inputStream != null ) break;
								}
								inputStream = loader.getResourceAsStream( resourceName = baseName +"_"+ language +"_"+ country +".properties" );
								if( inputStream != null ) break;
							}
							inputStream = loader.getResourceAsStream( resourceName = baseName +"_"+ language +".properties" );
							if( inputStream != null ) break;
						}
					}
					if( inputStream == null )
						throw line.throwIOException( "cannot load configure file '"+ resourceName +"'" );
					reader.push( resourceName, inputStream );
					break;
				} else if( buffer.startsWith(COMMAND_OPTIONSET) && buffer.endsWith(")") ) {
					String[] options = buffer.substring( buffer.indexOf('(') + 1, buffer.lastIndexOf(')') ).split( "=", 2 );
					if( options.length != 2 )
						throw line.throwIOException( "illegal option" );

					String optionKey = options[0].trim();
					String optionValue = options[1].trim();
					if( optionKey.equals("ignoreDuplicated") )
						this.ignoreDuplicated = Boolean.valueOf( optionValue ).booleanValue();
					break;
				} else if( !buffer.startsWith("%(") )
					throw line.throwIOException( "illegal command" );
			default:
				if( buffer.indexOf(':') <= 0 )
					throw line.throwIOException( "illegal block header" );

				String[] strings = buffer.split( ":", 2 );
				if( strings.length > 1 && strings[1].length() > 0 ) {
					if( strings[1].charAt(0) == '=' && strings[1].length() > 1 ) {
						String sourceKey = strings[1].substring(1).trim();
						if( !columnResourceBundleImpl.makeAlias(strings[0].trim(), sourceKey) )
							throw line.throwIOException( "illegal key '"+ sourceKey +"'" );
						continue;
					} else
						reader.rollback( new ColumnConfigureFile.Line(line, "\t"+ strings[1]) );
				}
				hline = new ColumnConfigureFile.Line( line, strings[0].trim() );
			}
		}
		if( hline == null ) return false;


		// read Block Line
		List<ColumnConfigureFile.Line> lineList = new java.util.ArrayList<ColumnConfigureFile.Line>();
		{
			while( (line = reader.readLine()) != null ) {
				String buffer = line.buffer;

				if( buffer == null )
					break;
				else if( buffer.charAt(0) == ' ' || buffer.charAt(0) == '\t' ) {
					line.buffer = buffer.trim();
					lineList.add( line );
				} else {
					reader.rollback( line );
					break;
				}
			}
			if( lineList.size() == 0 ) throw line.throwIOException( "block line needed" );

			// save Variable

			if( hline.buffer.startsWith("%(") && hline.buffer.endsWith(")") ) {
				columnResourceBundleImpl.putObject( hline.buffer, loadVariable(hline, lineList) );
				return loadBlock();
			}
		}


		// tokenizer Block Line;
		int blockType;
		List<ColumnConfigureFile.Line> tokenList = new java.util.ArrayList<ColumnConfigureFile.Line>();
		if( hline.buffer.endsWith("[]") )
			blockType = BLOCK_FIELDKEYARRAY;
		else if( hline.buffer.indexOf('%') > 0 )
			blockType = BLOCK_COLUMNLIST;
		else
			blockType = BLOCK_COLUMNPOOL;
		{
			java.util.Iterator<ColumnConfigureFile.Line> m_iterator = lineList.iterator();
			java.util.Iterator v_iterator = null;
			StringTokenizer m_tokenizer = null;
			StringTokenizer v_tokenizer = null;

			String command = null;
			boolean isCommandLine = true;
			boolean tokenizing = true;

			do {
				// read Next Token
				String buffer = null;		// token이 Line의 시작일 경우 not null
				String token = null;
				do {
					if( v_iterator != null ) {
						if( tokenizing && v_tokenizer != null && v_tokenizer.hasMoreTokens() )
							token = v_tokenizer.nextToken();
						else if( v_iterator.hasNext() ) {
							token = buffer = (String)v_iterator.next();
							if( tokenizing ) {
								v_tokenizer = new StringTokenizer( token, " ,\t" );
								token = v_tokenizer.nextToken();
							}
						} else
							v_iterator = null;
						if( token != null ) break;
					}

					if( tokenizing && m_tokenizer != null && m_tokenizer.hasMoreTokens() )
						token = m_tokenizer.nextToken();
					else if( m_iterator.hasNext() ) {
						line = m_iterator.next();
						token = buffer = line.buffer;
						if( tokenizing ) {
							m_tokenizer = new StringTokenizer( line.buffer, " ,\t" );
							token = m_tokenizer.nextToken();
						}
					}
				} while( false );
				if( token == null ) break;

				// parse Token
				if( token.startsWith("%(") && token.endsWith(")") && v_iterator == null ) {
					List list = (List)columnResourceBundleImpl.getObject( token );
					if( list == null ) throw line.throwIOException( "cannot find variable '"+ token +"'" );
					v_iterator = list.iterator();
					continue;
				}

				while( isCommandLine ) {
					if( token.charAt(0) == '%' ) {
						if( command != null ) throw line.throwIOException( "unclosed command '"+ command +"'" );

						int idx = token.indexOf( '(' );
						if( idx < 0 ) throw line.throwIOException( "illegal command" );

						tokenList.add( new ColumnConfigureFile.Line(line, command = token.substring(0, ++idx)) );
						if( COMMAND_COLUMNLISTTYPEKEYS.equals(command) || COMMAND_PROPERTYVALUE.equals(command) ) {
							if( buffer == null || !buffer.endsWith(")") ) throw line.throwIOException( "illegal command line for '"+ command +"'" );

							int sidx = command.length();
							int eidx = buffer.length() - 1;
							tokenList.add( new ColumnConfigureFile.Line(line, buffer.substring(sidx, eidx)) );
							tokenList.add( new ColumnConfigureFile.Line(line, ")") );
							if( v_iterator != null )
								v_tokenizer = null;
							else
								m_tokenizer = null;
							command = null;
							break;
						} else {
							if( token.length() <= idx ) break;
							token = token.substring( idx );
						}
					}
					if( command != null ) {
						if( token.endsWith(")") ) {
							command = null;
							if( token.length() > 1 )
								tokenList.add( new ColumnConfigureFile.Line(line, token.substring(token.length() - 1)) );
							tokenList.add( new ColumnConfigureFile.Line(line, ")") );
						} else
							tokenList.add( new ColumnConfigureFile.Line(line, token) );
					} else {
						isCommandLine = false;
						if( blockType == BLOCK_COLUMNPOOL ) {
							if( buffer == null ) throw line.throwIOException( "illegal ColumnPool line" );
							token = buffer;
							tokenizing = false;
						}
					}

					break;
				}
				if( !isCommandLine ) tokenList.add( new ColumnConfigureFile.Line(line, token) );
			} while( true );
		}

		// parse token
		switch( blockType ) {
		case BLOCK_COLUMNLIST:
			columnResourceBundleImpl.putColumnList( loadColumnList(hline, tokenList) );
			return true;
		case BLOCK_COLUMNPOOL:
			columnResourceBundleImpl.putColumnPool( loadColumnPool(hline, tokenList) );
			return true;
		case BLOCK_FIELDKEYARRAY:
			columnResourceBundleImpl.putFieldKeyArray( loadFieldKeyArray(hline, tokenList) );
			return true;
		}

		return true;
	}

	private ColumnListTemplate loadColumnList( ColumnConfigureFile.Line hline, List<ColumnConfigureFile.Line> tokenList ) throws IOException {
		String columnListName = hline.buffer;
		ColumnPool columnPool = columnResourceBundleImpl.getColumnPool( columnListName.split("%", 2)[0] );
		if( columnPool == null )
			throw hline.throwIOException( "cannot find columnPool '"+ columnListName.split("%", 2)[0] +"'" );
		else if( columnResourceBundleImpl.containsKey(columnListName) && !ignoreDuplicated )
			throw hline.throwIOException( "duplicate columnList '"+ columnListName +"'" );

		List<String> propertyValueList = null;
		List<String[]> columnListTypeList = null;
		String defaultColumnListTypeKey = null;
		String[] keys = null, sortKeys = null, primaryFieldKeys = null, hiddenFieldKeys = null;
		for( java.util.Iterator<ColumnConfigureFile.Line> iterator = tokenList.iterator(); iterator.hasNext(); ) {
			ColumnConfigureFile.Line line = iterator.next();

			if( line.buffer.charAt(0) == '%' ) {
				String command = line.buffer;
				List<String> list = new java.util.ArrayList<String>();

				while( iterator.hasNext() ) {
					line = iterator.next();
					if( ")".equals(line.buffer) ) break;
					list.add( line.buffer );
				}
				if( list.size() > 0 ) {
					if( COMMAND_DEFAULTSORTKEYS.equals(command) )
						sortKeys = list.toArray( new String[list.size()] );
					else if( COMMAND_HIDDENKEYS.equals(command) )
						hiddenFieldKeys = list.toArray( new String[list.size()] );
					else if( COMMAND_PRIMARYKEYS.equals(command) )
						primaryFieldKeys = list.toArray( new String[list.size()] );
					else if( COMMAND_PROPERTYVALUE.equals(command) ) {
						String[] values = list.get(0).split( "=", 2 );

						if( propertyValueList == null ) propertyValueList = new java.util.ArrayList<String>();
						propertyValueList.add( values.length > 1 ? values[0].trim() +"="+ values[1].trim() : values[0].trim() +"" );
					} else if( COMMAND_COLUMNLISTTYPEKEYS.equals(command) ) {
						String[] columnListTypeValues = list.get(0).split( "," );
						if( columnListTypeValues.length > 0 ) {
							boolean validable = false;
							String[] splitValues = columnListTypeValues[0].trim().split( ":" );

							if( splitValues.length == 1 ) defaultColumnListTypeKey = splitValues[0].trim();
							columnListTypeList = new java.util.ArrayList<String[]>();

							for( int i = (defaultColumnListTypeKey == null ? 0 : 1); i < columnListTypeValues.length; i++ ) {
								splitValues = columnListTypeValues[i].trim().split( ":" );
								if( splitValues.length != 2 )
									throw line.throwIOException( "illegal columnListType format '"+ columnListTypeValues[i] +"'" );

								String key = splitValues[0].trim();
								if( key.equals(defaultColumnListTypeKey) ) validable = true;
								columnListTypeList.add( new String[] { key, splitValues[1].trim() } );
							}
							if( defaultColumnListTypeKey == null )
								defaultColumnListTypeKey = columnListTypeList.get(0)[0];
							else if( !validable )
								throw line.throwIOException( "cannot find default columnListType '"+ defaultColumnListTypeKey +"'" );
						}
					} else
						throw line.throwIOException( "illegal command '"+ command +"'" );
				}
			} else {
				List<String> list = new java.util.ArrayList<String>();
				list.add( line.buffer );
				while( iterator.hasNext() )
					list.add( iterator.next().buffer );
				keys = list.toArray( new String[list.size()] );
			}
		}

		try {
			columnPool.createColumnList( columnListName, defaultColumnListTypeKey, keys, primaryFieldKeys, hiddenFieldKeys, sortKeys );

			String[] propertyValues = null;
			if( propertyValueList != null ) propertyValues = propertyValueList.toArray( new String[propertyValueList.size()] );

			return new ColumnListTemplate(
				columnListName.split("%", 2)[0], columnListName, keys, primaryFieldKeys, hiddenFieldKeys, sortKeys
				, propertyValues, defaultColumnListTypeKey, columnListTypeList
			);
		} catch( IllegalArgumentException argumentEx ) {
			throw hline.throwIOException( argumentEx.getMessage() );
		}
	}

	private ColumnPool loadColumnPool( ColumnConfigureFile.Line hline, List<ColumnConfigureFile.Line> tokenList ) throws IOException {
		String columnPoolName = hline.buffer;
		if( columnResourceBundleImpl.containsKey(columnPoolName) && !ignoreDuplicated )
			throw hline.throwIOException( "duplicate columnPool '"+ columnPoolName +"'" );

		ColumnPool columnPool = columnResourceBundleImpl.getColumnPool( columnPoolName );
		ColumnPoolImpl columnPoolImpl;
		if( columnPool == null )
			columnPoolImpl = new ColumnPoolImpl( columnPoolName );
		else if( columnResourceBundleImpl.containsKey(columnPoolName) && columnPool instanceof ColumnPoolImpl )
			columnPoolImpl = (ColumnPoolImpl)columnPool;
		else
			columnPoolImpl = new ColumnPoolImpl( columnPoolName, columnPool );

		int tokencnt = 0;
		for( java.util.Iterator<ColumnConfigureFile.Line> iterator = tokenList.iterator(); iterator.hasNext(); tokencnt++ ) {
			ColumnConfigureFile.Line line = iterator.next();

			if( line.buffer.charAt(0) == '%' ) {
				String command = line.buffer;

				if( COMMAND_PARENTCOLUMNPOOL.equals(command) ) {
					if( tokencnt > 0 )
						throw line.throwIOException( "command '"+ command +"' must be at first" );
					else if( columnPoolImpl.getParent() != null )
						throw line.throwIOException( "already exist parent ColumnPool" );

					while( iterator.hasNext() ) {
						line = iterator.next();
						if( ")".equals(line.buffer) ) break;
						if( columnPoolImpl.getParent() != null )
							throw line.throwIOException( "illegal command '"+ command +"' format" );

						columnPool = columnResourceBundleImpl.getColumnPool( line.buffer );
						if( columnPool == null )
							throw line.throwIOException( "cannot find columnPool '"+ line.buffer +"'" );
						columnPoolImpl = new ColumnPoolImpl( columnPoolName, columnPool );
					}
				} else if( COMMAND_INCLUDEKEYS.equals(command) ) {
					String includeColumnPoolName = null;
					ColumnPool includeColumnPool = null;
					ColumnPool publicColumnPool = columnResourceBundleImpl.getColumnPool( "PUBLIC" );

					while( iterator.hasNext() ) {
						line = iterator.next();
						if( ")".equals(line.buffer) ) break;

						String[] keys = line.buffer.split( "#", 2 );
						if( keys.length > 1 ) {
							if( !keys[0].equals(includeColumnPoolName) ) {
								includeColumnPool = columnResourceBundleImpl.getColumnPool( includeColumnPoolName = keys[0] );
								if( includeColumnPool == null )
									throw line.throwIOException( "cannot find columnPool '"+ keys[0] +"'" );
							}
							keys[0] = keys[1];
						} else {
							includeColumnPoolName = null;
							includeColumnPool = publicColumnPool;
							if( includeColumnPool == null )
								throw line.throwIOException( "cannot find columnPool 'PUBLIC'" );
						}

						if( keys[0].startsWith( "LNK.") ) {
							HyperLink link = null;
							if( includeColumnPool instanceof ColumnPoolImpl )
								link = ((ColumnPoolImpl)includeColumnPool).getHyperLink( keys[0].substring(4) );
							if( link == null )
								throw line.throwIOException( "cannot find hyperLink '"+ keys[0].substring(4) +"'" );
							columnPoolImpl.putHyperLink( link instanceof HyperLinkImpl ? new HyperLinkImpl((HyperLinkImpl)link) : link );
						} else if( keys[0].startsWith( "GRP.") ) {
							ColumnGroup columnGroup = includeColumnPool.getColumnGroup( keys[0].substring(4) );
							if( columnGroup == null )
								throw line.throwIOException( "cannot find columnGroup '"+ keys[0].substring(4) +"'" );
							else if( columnGroup instanceof ColumnGroupImpl )
								columnPoolImpl.putColumnGroup( new ColumnGroupImpl((ColumnGroupImpl)columnGroup) );
							else
								columnPoolImpl.putColumnGroup( columnGroup );

							String[] columnKeys = columnGroup.getColumnKeys();
							if( columnKeys != null ) {
								for( int i = 0; i < columnKeys.length; i++ ) {
									Map<String, Column> columnMap = includeColumnPool.getColumnFamily( columnKeys[i] );
									if( columnMap == null ) continue;
									for( Column column : columnMap.values() )
										columnPoolImpl.putColumn( column instanceof ColumnImpl ? new ColumnImpl((ColumnImpl)column) : column );
								}
							}
						} else {
							if( keys[0].startsWith( "COL.") ) keys[0] = keys[0].substring(4);
							Map<String, Column> columnMap = includeColumnPool.getColumnFamily( keys[0] );
							if( columnMap == null )
								throw line.throwIOException( "cannot find columnFamily '"+ keys[0] +"'" );
							for( Column column : columnMap.values() )
								columnPoolImpl.putColumn( column instanceof ColumnImpl ? new ColumnImpl((ColumnImpl)column) : column );
						}
					}
				} else {
					List<String> list = new java.util.ArrayList<String>();

					while( iterator.hasNext() ) {
						line = iterator.next();
						if( ")".equals(line.buffer) ) break;
						list.add( line.buffer );
					}
					if( list.size() > 0 ) {
						if( COMMAND_DEFAULTSORTKEYS.equals(command) )
							columnPoolImpl.sortKeys = list.toArray( new String[list.size()] );
						else if( COMMAND_PRIMARYKEYS.equals(command) )
							columnPoolImpl.primaryFieldKeys = list.toArray( new String[list.size()] );
						else
							throw line.throwIOException( "illegal command '"+ command +"'" );
					}
				}
			} else {
				char columnPoolType;
				if( columnPoolName.indexOf(COLUMNPOOL_HSSF_STRING) > 0 )
					columnPoolType = COLUMNPOOLTYPE_HSSF;
				else
					columnPoolType = COLUMNPOOLTYPE_HTML;

				loadColumnPoolLine( columnPoolImpl, line, columnPoolType );
				while( iterator.hasNext() )
					loadColumnPoolLine( columnPoolImpl, iterator.next(), columnPoolType );
			}
		}

		return columnPoolImpl;
	}

	/**
	 *
	 * <ul type='square'>
	 * <li>GRP.{columnGroupKey} {titlePattern} [{columnAttr}]
	 * <li>GRP.{columnGroupKey}%COL {columnKey} [{titlePattern}] [{columnAttr}]
	 * <li>LNK.{linkKey} {hrefPattern} [{titlePattern}]
	 * <li>LNK.{linkKey}%AUTH {systemCode} {packageCode}
	 * <li>LNK.{linkKey}%COND NOT_NULL|NOT_ZERO|EQUALS|CONTAINS {fieldKey} [{value1}[,{value2}[,...]]]
	 * <li>LNK.{linkKey}%TITLE {titlePattern}
	 * <li>SFX.{suffixKey} {suffixPattern}
	 * <li>[COL.]{columnKey} {titlePattern} {sortable} [{columnAttr}] [{dataPattern}]
	 * <li>[COL.]{columnKey}%ATTR {columnAttr}
	 * <li>[COL.]{columnKey}%DATA {dataPattern}
	 * <li>[COL.]{columnKey}%HELP {helpPattern}
	 * </ul>
	 */
	private void loadColumnPoolLine( ColumnPoolImpl columnPoolImpl, ColumnConfigureFile.Line line, char columnPoolType ) throws IOException {
		String[] tokens;

		List<String> tokenList = new java.util.ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer( line.buffer, "\t" );
		while( tokenizer.hasMoreTokens() )
			tokenList.add( tokenizer.nextToken() );
		tokens = tokenList.toArray( new String[tokenList.size()] );

		String[] keys = tokens[0].split( "%", 2 );
		String key = keys[0];
		String option = ( keys.length > 1 ? keys[1] : null );
		if( key.startsWith("GRP.") ) {
			key = key.substring(4);
			if( option == null ) {
				// GRP.{columnGroupKey} {titlePattern} [{columnAttr}]
				if( tokens.length == 2 )
					columnPoolImpl.putColumnGroup( new ColumnGroupImpl(key, parseValue(tokens[1])) );
				else if( tokens.length == 3 )
					columnPoolImpl.putColumnGroup( new ColumnGroupImpl(key, parseValue(tokens[1]), tokens[2]) );
				else
					throw line.throwIOException( "illegal ColumnGroup line" );
			} else if( "COL".equals(option) ) {
				// GRP.{columnGroupKey}%COL {columnKey} [{titlePattern}] [{columnAttr}]
				ColumnGroupImpl columnGroup = columnPoolImpl.makeColumnGroup( key );
				if( columnGroup == null )
					throw line.throwIOException( "cannot find columnGroup '"+ key +"'" );
				if( tokens.length == 2 )
					columnGroup.appendColumnKey( tokens[1] );
				else if( tokens.length == 3 )
					columnGroup.appendColumnKey( tokens[1], parseValue(tokens[2]) );
				else if( tokens.length == 4 )
					columnGroup.appendColumnKey( tokens[1], parseValue(tokens[2]), tokens[3] );
				else
					throw line.throwIOException( "illegal ColumnGroup%COL line" );
			} else
				throw line.throwIOException( "illegal ColumnGroup% line" );
		} else if( key.startsWith("LNK.") ) {
			key = key.substring(4);
			if( option == null ) {
				// LNK.{linkKey} {hrefPattern} [{titlePattern}]
				RecordFormat href = null, help = null;
				switch( tokens.length ) {
				case 3:
					help = PatternRecordFormat.getInstance( tokens[2] );
				case 2:
					href = PatternRecordFormat.getInstance( tokens[1] );
					break;
				default:
					throw line.throwIOException( "illegal HyperLink line" );
				}
				columnPoolImpl.putHyperLink( new HyperLinkImpl(key, href, help) );
			} else {
				// LNK.{linkKey}%AUTH {systemCode} {packageCode}
				// LNK.{linkKey}%TITLE {titlePattern}
				// LNK.{linkKey}%COND NOT_NULL|NOT_ZERO|EQUALS|CONTAINS {fieldKey} [{value1}[,{value2}[,...]]]
				HyperLinkImpl hyperLinkImpl = columnPoolImpl.makeHyperLink( key );
				if( hyperLinkImpl == null )
					throw line.throwIOException( "cannot find hyperLink '"+ key +"'" );

				if( "AUTH".equals(option) && tokens.length == 3 )
					hyperLinkImpl.setSystemPackage( tokens[1], tokens[2] );
				else if( "TITLE".equals(option) && tokens.length == 2 )
					hyperLinkImpl.help = PatternRecordFormat.getInstance( tokens[1] );
				else if( "COND".equals(option) && tokens.length >= 3 ) {
					if( "NOT_NULL".equals(tokens[1]) && tokens.length == 3 )
						hyperLinkImpl.setCondition( HyperLinkImpl.CONDITION_NOT_NULL, tokens[2], null );
					else if( "NOT_ZERO".equals(tokens[1]) && tokens.length == 3 )
						hyperLinkImpl.setCondition( HyperLinkImpl.CONDITION_NOT_ZERO, tokens[2], null );
					else if( "EQUALS".equals(tokens[1]) && tokens.length == 4 )
						hyperLinkImpl.setCondition( HyperLinkImpl.CONDITION_EQUALS, tokens[2], tokens[3] );
					else if( "CONTAINS".equals(tokens[1]) && tokens.length == 4 )
						hyperLinkImpl.setCondition( HyperLinkImpl.CONDITION_CONTAINS, tokens[2], tokens[3] );
					else
						throw line.throwIOException( "illegal HyperLink%COND line" );
				} else
					throw line.throwIOException( "illegal HyperLink% line" );
			}
		} else if( key.startsWith("SFX.") ) {
			// SFX.{suffixKey} {suffixPattern}
			if( tokens.length != 2 ) throw line.throwIOException( "illegal ColumnSuffix line" );
			columnPoolImpl.putColumnSuffix( key.substring(4), PatternRecordFormat.getInstance(tokens[1]) );
		} else {
			if( key.startsWith("COL.") ) key = key.substring(4);
			if( option == null ) {
				// [COL.]{columnKey} {titlePattern} {sortable} [{columnAttr}] [{dataPattern}]
				RecordFormat data = null;
				ColumnImpl columnImpl;
				String columnAttr = null;
				switch( tokens.length ) {
				case 5:
					data = PatternRecordFormat.getInstance( tokens[4] );
				case 4:
					if( tokens[3].startsWith("%(") && tokens[3].endsWith(")") && tokens[3].split("[ ,\t]", 2).length == 1 ) {
						List list = (List)columnResourceBundleImpl.getObject( tokens[3] );
						if( list == null ) throw line.throwIOException( "cannot find variable '"+ tokens[3] +"'" );
						if( list.size() > 1 )
							throw line.throwIOException( "illegal variable '"+ tokens[3] +"'" );
						columnAttr = (String)list.get(0);
					} else
						columnAttr = ( "-".equals(tokens[3]) || "_".equals(tokens[3]) ? null : tokens[3] );
				case 3:
					if( columnPoolType == COLUMNPOOLTYPE_HSSF )
						columnImpl = new ColumnImpl( key, parseValue(tokens[1]), HSSF.getColumnStyle(columnAttr)
								, data, null, "Y".equals(tokens[2]), false );
					else
						columnImpl = new ColumnImpl( key, parseValue(tokens[1]), columnAttr, data, null, "Y".equals(tokens[2]) );
					columnPoolImpl.putColumn( columnImpl );
					break;
				default:
					throw line.throwIOException( "illegal Column line" );
				}
			} else {
				// [COL.]{columnKey}%TITLE {columnTitle}
				// [COL.]{columnKey}%ATTR {columnAttr}
				// [COL.]{columnKey}%DATA {dataPattern}
				// [COL.]{columnKey}%HELP {helpPattern}
				ColumnImpl columnImpl = columnPoolImpl.makeColumn( key );
				if( columnImpl == null )
					throw line.throwIOException( "cannot find column '"+ key +"'" );

				if( "TITLE".equals(option) && tokens.length == 2 )
					columnImpl.columnName = tokens[1];
				else if( "ATTR".equals(option) && tokens.length == 2 ) {
					if( columnPoolType == COLUMNPOOLTYPE_HSSF )
						columnImpl.columnAttr = HSSF.getColumnStyle( tokens[1] );
					else
						columnImpl.columnAttr = tokens[1];
				} else if( "CELL".equals(option) && tokens.length == 2 )
					columnImpl.dataCellAttr = tokens[1];
				else if( "DATA".equals(option) && tokens.length == 2 )
					columnImpl.columnValue = PatternRecordFormat.getInstance( tokens[1] );
				else if( "HELP".equals(option) && tokens.length == 2 )
					columnImpl.columnHelp = PatternRecordFormat.getInstance( tokens[1] );
				else
					throw line.throwIOException( "illegal Column% line" );
			}
		}
	}

	private FieldKeyArray loadFieldKeyArray( ColumnConfigureFile.Line hline, List<ColumnConfigureFile.Line> tokenList ) throws IOException {
		String fieldKeyArrayName = hline.buffer;
		if( columnResourceBundleImpl.containsKey(fieldKeyArrayName) && !ignoreDuplicated )
			throw hline.throwIOException( "duplicate fieldKeyArray '"+ fieldKeyArrayName +"'" );

		List<String> list = new java.util.ArrayList<String>();
		for( ColumnConfigureFile.Line line : tokenList )
			list.add( line.buffer );

		String[] keys = list.toArray( new String[list.size()] );
		String[] fieldKeys = null;
		String[] optionKeys = null;
		for( int k = 0; k < keys.length; k++ ) {
			String[] splitKeys = keys[k].split("/");

			if( splitKeys.length > 1 ) {
				if( fieldKeys == null ) {
					fieldKeys = Arrays.clone( keys );
					optionKeys = new String[ keys.length ];
				}
				fieldKeys[k] = splitKeys[0];
				optionKeys[k] = splitKeys[1];
			} else if( fieldKeys != null )
				fieldKeys[k] = splitKeys[0];
		}

		return new FieldKeyArray( fieldKeyArrayName, (fieldKeys == null ? keys : fieldKeys), optionKeys );
	}

	private Object loadVariable( ColumnConfigureFile.Line hline, List<ColumnConfigureFile.Line> lineList ) throws IOException {
		List<String> stringList = new java.util.ArrayList<String>();

		for( ColumnConfigureFile.Line line : lineList ) {
			String buffer = line.buffer;

			StringBuffer sbuf = null;
			int idx0 = 0;
			int idx1 = buffer.indexOf( "%(" );
			while( idx1 >= 0 ) {
				int idx2 = buffer.indexOf( ")", idx1 );
				if( idx2 < 0 ) break;

				String varkey = buffer.substring( idx1, ++idx2 );
				if( varkey.split("[ ,\t]", 2).length == 1 ) {
					if( sbuf == null ) sbuf = new StringBuffer();
					sbuf.append( buffer.substring(idx0, idx1) );

					List list = (List)columnResourceBundleImpl.getObject( varkey );
					if( list == null ) throw line.throwIOException( "cannot find variable '"+ varkey +"'" );

					sbuf.append( (String)list.get(0) );
					if( list.size() > 1 ) {
						if( sbuf.length() > 0 ) stringList.add( sbuf.toString() );
						for( int i = 1; i < list.size(); i++ )
							stringList.add( (String)list.get(i) );
						sbuf = null;
					}
					idx0 = idx2;
				}
				idx1 = buffer.indexOf( "%(", idx2 );
			}
			if( idx0 == 0 )
				stringList.add( buffer );
			else if( sbuf != null ) {
				sbuf.append( buffer.substring(idx0) );
				if( sbuf.length() > 0 ) stringList.add( sbuf.toString() );
			} else if( idx0 < buffer.length() - 1 )
				stringList.add( buffer.substring(idx0) );
		}

		return stringList;
	}

	private Object parseValue( String token ) {
		RecordFormat format = PatternRecordFormat.getInstance( token );
		if( format instanceof com.irt.data.format.ConstantRecordFormat )
			return ((com.irt.data.format.ConstantRecordFormat)format).getString();

		return format;
	}

	/**
	 *
	 */
	static class Line {
		int linenum;
		String filename;
		String buffer;

		Line( int linenum, String filename, String buffer ) {
			this.linenum = linenum;
			this.filename = filename;
			this.buffer = buffer;
		}

		Line( Line line, String buffer ) {
			this( line.linenum, line.filename, buffer );
		}

		IOException throwIOException( String message ) throws IOException {
			throw new IOException( message +": line "+ linenum +" in '"+ filename +"': '"+ buffer +"'" );
		}

		public String toString() {
			return linenum +" : '"+ buffer +"'";
		}
	}

	/**
	 *
	 */
	static class LineReader {
		java.util.Stack<Object[]> stack;
		int linenum;
		String filename;
		ColumnConfigureFile.Line savedLine;
		BufferedReader reader;

		LineReader( String filename, InputStream inputStream ) throws IOException {
			this.stack = new java.util.Stack<Object[]>();
			this.linenum = 0;
			this.filename = filename;
			this.savedLine = null;
			this.reader = new BufferedReader( new java.io.InputStreamReader(inputStream, "ISO-8859-1") );
		}

		void close() {
			try { reader.close(); } catch( IOException ioEx ) {}
			while( !stack.empty() ) {
				pop();
				try { reader.close(); } catch( IOException ioEx ) {}
			}
		}

		ColumnConfigureFile.Line readLine() throws IOException {
			if( savedLine != null ) {
				try {
					return savedLine;
				} finally {
					savedLine = null;
				}
			}

			linenum++;
			String buffer = reader.readLine();
			if( buffer == null ) {
				if( stack.empty() )
					return null;
				else {
					try {
						return new ColumnConfigureFile.Line( linenum, filename, buffer );
					} finally {
						try { reader.close(); } catch( IOException ioEx ) {}
						pop();
					}
				}
			}

			int idx = 0;
			try {
				while( buffer.charAt(idx) == '\t' || buffer.charAt(idx) == ' ' )
					idx++;
				if( buffer.charAt(idx) == '#' ) return readLine();
			} catch( StringIndexOutOfBoundsException idxEx ) {
				return readLine();
			}

			idx = buffer.indexOf( '\\' );
			if( idx > 0 ) {
				StringBuffer sbuf = new StringBuffer( buffer.length() );
				sbuf.append( buffer.substring(0, idx) );
				do {
					char c = buffer.charAt( idx++ );
					if( c == '\\' ) {
						if( idx == buffer.length() ) {
							idx = 0;
							linenum++;
							buffer = reader.readLine();
							if( buffer == null ) break;
							sbuf.ensureCapacity( buffer.length() + sbuf.length() );
						}
						c = buffer.charAt( idx++ );
						switch( c ) {
						case 'n':
							sbuf.append( '\n' );
							break;
						case 't':
							sbuf.append( '\t' );
							break;
						case 'r':
							sbuf.append( '\r' );
							break;
						case 'u':
							if( idx + 4 <= buffer.length() ) {
								c = (char)Integer.parseInt( buffer.substring( idx, idx + 4), 16 );
								sbuf.append( c );
								idx += 4;
							}
							break;
						default:
							sbuf.append( c );
							break;
						}
					} else
						sbuf.append( c );
				} while( idx < buffer.length() );
				buffer = sbuf.toString();
			}

			return new ColumnConfigureFile.Line( linenum, filename, buffer );
		}

		void pop() {
			Object[] objects = stack.pop();
			this.linenum = ((Integer)objects[0]).intValue();
			this.filename = (String)objects[1];
			this.reader = (BufferedReader)objects[2];
		}

		void push( String filename, InputStream inputStream ) throws IOException {
			stack.push( new Object[] { new Integer(linenum), filename, reader } );
			this.linenum = 0;
			this.filename = filename;
			this.reader = new BufferedReader( new java.io.InputStreamReader(inputStream, "ISO-8859-1") );
		}

		void rollback( ColumnConfigureFile.Line line ) {
			String buffer = line.buffer;
			try {
				int idx = 0;
				while( buffer.charAt(idx) == '\t' || buffer.charAt(idx) == ' ' )
					idx++;
				if( buffer.charAt(idx) != '#' ) this.savedLine = line;
			} catch( StringIndexOutOfBoundsException idxEx ) {}
		}
	}

	public static class Reloadable {
		public static boolean clear() {
			synchronized( resourceMap ) {
				resourceMap.clear();
			}
			return true;
		}

		public static boolean clear( String resourceName ) {
			synchronized( resourceMap ) {
				if( resourceMap.containsKey(resourceName) ) {
					resourceMap.remove(resourceName);
				};
			}
			return true;
		}
	}
}
