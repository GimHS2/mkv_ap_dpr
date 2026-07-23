/*
 *	File Name:	QueryUtility.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/06/30		2.2.0c	org.slf4j 지원
 *	jbaek		2018/10/30		2.2.0c	Timestamp 지원
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										log4j 사용
 *	stghr12		2006/09/01		2.0.1	DateFormat 동기화 오류 수정
 *	stghr12		2006/02/28		2.0.0	create(QueryPrinter -> QueryUtility)
 *
**/

package com.irt.sql;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 */
public class QueryUtility {
	private final static int DEFAULT_MAX_LINESIZE		= 128;

	public static void printQuery( Logger logger, String message, QueryBuffer querybuf ) {
		printQuery( logger, message, querybuf.getQuery(), querybuf.getBindVariables(), DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( Logger logger, String message, QueryBuffer querybuf, int maximumLineSize ) {
		printQuery( logger, message, querybuf.getQuery(), querybuf.getBindVariables(), maximumLineSize );
	}

	public static void printQuery( Logger logger, String message, String query ) {
		printQuery( logger, message, query, null, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( Logger logger, String message, String query, Object[] bindVars ) {
		printQuery( logger, message, query, bindVars, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( Logger logger, String message, String query, Object[] bindVars, int maximumLineSize ) {
		java.io.StringWriter writer = new java.io.StringWriter();
		java.io.PrintWriter out = new java.io.PrintWriter( writer, true );

		out.print( message );
		out.println( ": " );
		printQuery( out, query, bindVars, maximumLineSize );
		try {
			throw new Exception();
		} catch( Exception ex ) {
			StackTraceElement[] stackTraceElements = ex.getStackTrace();
			if( stackTraceElements != null ) {
				for( int i = 2; i < stackTraceElements.length && i < 10; i++ )
					out.println( "\t> "+ stackTraceElements[i] );
				if( stackTraceElements.length >= 10 ) out.println( "\t> ..." );
			}
		}

		logger.debug( writer.toString() );
	}

	public static void printQuery( org.slf4j.Logger logger, String message, QueryBuffer querybuf ) {
		printQuery( logger, message, querybuf.getQuery(), querybuf.getBindVariables(), DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( org.slf4j.Logger logger, String message, QueryBuffer querybuf, int maximumLineSize ) {
		printQuery( logger, message, querybuf.getQuery(), querybuf.getBindVariables(), maximumLineSize );
	}

	public static void printQuery( org.slf4j.Logger logger, String message, String query ) {
		printQuery( logger, message, query, null, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( org.slf4j.Logger logger, String message, String query, Object[] bindVars ) {
		printQuery( logger, message, query, bindVars, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( org.slf4j.Logger logger, String message, String query, Object[] bindVars, int maximumLineSize ) {
		java.io.StringWriter writer = new java.io.StringWriter();
		java.io.PrintWriter out = new java.io.PrintWriter( writer, true );

		out.print( message );
		out.println( ": " );
		printQuery( out, query, bindVars, maximumLineSize );
		try {
			throw new Exception();
		} catch( Exception ex ) {
			StackTraceElement[] stackTraceElements = ex.getStackTrace();
			if( stackTraceElements != null ) {
				for( int i = 2; i < stackTraceElements.length && i < 10; i++ )
					out.println( "\t> "+ stackTraceElements[i] );
				if( stackTraceElements.length >= 10 ) out.println( "\t> ..." );
			}
		}

		logger.debug( writer.toString() );
	}

	public static void printQuery( PrintStream out, QueryBuffer querybuf ) {
		printQuery( new PrintWriter(out, true), querybuf.getQuery(), querybuf.getBindVariables(), DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( PrintStream out, QueryBuffer querybuf, int maximumLineSize ) {
		printQuery( new PrintWriter(out, true), querybuf.getQuery(), querybuf.getBindVariables(), maximumLineSize );
	}

	public static void printQuery( PrintStream out, String query ) {
		printQuery( new PrintWriter(out, true), query, null, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( PrintStream out, String query, Object[] bindVars ) {
		printQuery( new PrintWriter(out, true), query, bindVars, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( PrintStream out, String query, Object[] bindVars, int maximumLineSize ) {
		printQuery( new PrintWriter(out, true), query, bindVars, maximumLineSize );
	}

	public static void printQuery( PrintWriter out, QueryBuffer querybuf ) {
		printQuery( out, querybuf.getQuery(), querybuf.getBindVariables(), DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( PrintWriter out, QueryBuffer querybuf, int maximumLineSize ) {
		printQuery( out, querybuf.getQuery(), querybuf.getBindVariables(), maximumLineSize );
	}

	public static void printQuery( PrintWriter out, String query ) {
		printQuery( out, query, null, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( PrintWriter out, String query, Object[] bindVars ) {
		printQuery( out, query, bindVars, DEFAULT_MAX_LINESIZE );
	}

	public static void printQuery( PrintWriter out, String query, Object[] bindVars, int maximumLineSize ) {
		if( query == null )
			out.println( query );
		else
			Token.print( out, Token.makeQueryTokenList(query, bindVars), "", maximumLineSize );
	}

	/**
	 *
	 */
	static class Token {
		protected final static int TOKEN_NONE				= 'N';
		protected final static int TOKEN_BLANK			= ' ';
		protected final static int TOKEN_AND				= 'A';
		protected final static int TOKEN_OR				= 'R';
		protected final static int TOKEN_UNION			= 'U';
		protected final static int TOKEN_UNIONALL			= 'L';

		protected final static int STATUS_NONE			= 'N';
		protected final static int STATUS_SELECT			= 'S';
		protected final static int STATUS_FROM			= 'F';
		protected final static int STATUS_WHERE			= 'W';
		protected final static int STATUS_GROUPBY			= 'G';
		protected final static int STATUS_ORDERBY			= 'O';

		int status;
		String token;

		Token() {}

		Token( int status, String token ) {
			this.status = status;
			this.token = token;
		}

		static boolean isSyntaxToken( int status, String token ) {
			switch( status ) {
			case STATUS_NONE:
				return( "SELECT".equals(token) );
			case STATUS_SELECT:
				return( ",".equals(token) || "FROM".equals(token) );
			case STATUS_FROM:
				return( ",".equals(token) || "WHERE".equals(token) || "GROUP".equals(token) || "ORDER".equals(token) || "UNION".equals(token) );
			case STATUS_WHERE:
				return( "AND".equals(token) || "OR".equals(token) || "GROUP".equals(token) || "ORDER".equals(token) || "UNION".equals(token) );
			case STATUS_GROUPBY:
				return( ",".equals(token) || "UNION".equals(token) || "ORDER".equals(token) );
			case STATUS_ORDERBY:
				return( ",".equals(token) || "UNION".equals(token) || "GROUP".equals(token) );
			default:
				return false;
			}
		}

		static List<QueryUtility.Token> makeQueryTokenList( String query, Object[] bindVars ) {
			int deep = 0;
			int bindIdx = 0;
			int status = STATUS_NONE;
			DateFormat dateFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd" );
			DateFormat timestampFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );

			List<QueryUtility.Token> tokenList = new java.util.ArrayList<QueryUtility.Token>();
			StringBuffer savedToken = new StringBuffer();

			StringTokenizer tokenizer = new StringTokenizer( query, " \t\n()?,", true );
			Token qtoken = read( tokenizer );
			while( qtoken != null ) {
				if( qtoken.status == TOKEN_BLANK && savedToken.length() > 0 ) savedToken.append( " " );

				switch( qtoken.token.charAt(0) ) {
				case '(':
					deep++;
					savedToken.append( qtoken.token );
					break;
				case ')':
					deep--;
					savedToken.append( qtoken.token );
					break;
				case '?':
					if( bindVars == null )
						savedToken.append( qtoken.token );
					else {
						try {
							Object bindVar = bindVars[ bindIdx++ ];
							if( bindVar == null )
								savedToken.append( "NULL" );
							else if( bindVar instanceof Number )
								savedToken.append( bindVar.toString() );
							else if( bindVar instanceof java.util.Date ) {
								if( com.irt.data.Condition.isTimestampOfSql(bindVar) ) {
									savedToken.append( "TO_TIMESTAMP_TZ('"+ timestampFormat.format((java.util.Date)bindVar) +"', 'YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM')" );
								} else {
									savedToken.append( "TO_DATE('"+ dateFormat.format((java.util.Date)bindVar) +"', 'YYYY-MM-DD')" );
								}
							}
							else
								savedToken.append( "'"+ bindVar.toString() +"'" );
						} catch( ArrayIndexOutOfBoundsException arrEx ) {
							savedToken.append( qtoken.token );
						}
					}
					break;
				default:
					if( deep != 0 || !isSyntaxToken(status, qtoken.token) )
						savedToken.append( qtoken.token );
					else {
						if( "GROUP".equals(qtoken.token) || "ORDER".equals(qtoken.token) ) {
							Token nextToken = read( tokenizer );
							if( nextToken == null || nextToken.status != TOKEN_BLANK || !"BY".equals(nextToken.token) ) {
								savedToken.append( qtoken.token );
								qtoken = nextToken;
								continue;
							}
						} else if( "UNION".equals(qtoken.token) ) {
							Token nextToken = read( tokenizer );
							if( nextToken != null && nextToken.status == TOKEN_BLANK && "ALL".equals(nextToken.token) ) {
								qtoken.token = "UNION ALL";
								nextToken = read( tokenizer );
							}
							if( nextToken == null || nextToken.status != TOKEN_BLANK || !"SELECT".equals(nextToken.token) ) {
								savedToken.append( qtoken.token );
								qtoken = nextToken;
								continue;
							}
						}

						if( savedToken.length() > 0 ) {
							tokenList.add( new QueryUtility.Token(status, savedToken.toString()) );
							savedToken = new StringBuffer();
						}
						if( "UNION".equals(qtoken.token) ) {
							tokenList.add( new QueryUtility.Token(TOKEN_UNION, null) );
							tokenList.add( new QueryUtility.Token(status = STATUS_SELECT, null) );
						} else if( "UNION ALL".equals(qtoken.token) ) {
							tokenList.add( new QueryUtility.Token(TOKEN_UNIONALL, null) );
							tokenList.add( new QueryUtility.Token(status = STATUS_SELECT, null) );
						} else if( "SELECT".equals(qtoken.token) )
							tokenList.add( new QueryUtility.Token(status = STATUS_SELECT, null) );
						else if( "FROM".equals(qtoken.token) )
							tokenList.add( new QueryUtility.Token(status = STATUS_FROM, null) );
						else if( "WHERE".equals(qtoken.token) )
							tokenList.add( new QueryUtility.Token(status = STATUS_WHERE, null) );
						else if( "GROUP".equals(qtoken.token) )
							tokenList.add( new QueryUtility.Token(status = STATUS_GROUPBY, null) );
						else if( "ORDER".equals(qtoken.token) )
							tokenList.add( new QueryUtility.Token(status = STATUS_ORDERBY, null) );
						else if( "AND".equals(qtoken.token) )
							tokenList.add( new QueryUtility.Token(TOKEN_AND, null) );
						else if( "OR".equals(qtoken.token) )
							tokenList.add( new QueryUtility.Token(TOKEN_OR, null) );
					}
				}
				qtoken = read( tokenizer );
			}
			if( savedToken.length() > 0 ) tokenList.add( new QueryUtility.Token(status, savedToken.toString()) );

			return tokenList;
		}

		static void print( PrintWriter out, List<QueryUtility.Token> tokenList, String lineprefix, int maximumLineSize ) {
			int count = 0;
			String lineheader = "";
			StringBuffer linebuffer = new StringBuffer();
			for( QueryUtility.Token qtoken : tokenList ) {
				if( qtoken.token == null ) {
					if( linebuffer.length() > 0 ) {
						out.println( lineprefix + linebuffer.toString() );
						linebuffer = new StringBuffer();
					}

					switch( qtoken.status ) {
					case STATUS_SELECT:
						count = 0;
						linebuffer.append( "SELECT  " );
						lineheader = "        ";
						break;
					case STATUS_FROM:
						count = 0;
						linebuffer.append( "    FROM    " );
						lineheader = "            ";
						break;
					case STATUS_WHERE:
						count = 0;
						linebuffer.append( "    WHERE   " );
						lineheader = "";
						break;
					case STATUS_GROUPBY:
						count = 0;
						linebuffer.append( "    GROUP BY    " );
						lineheader = "                 ";
						break;
					case STATUS_ORDERBY:
						count = 0;
						linebuffer.append( "    ORDER BY    " );
						lineheader = "                 ";
						break;
					case TOKEN_AND:
						linebuffer.append( "        AND " );
						break;
					case TOKEN_OR:
						linebuffer.append( "         OR " );
						break;
					case TOKEN_UNION:
						out.println( lineprefix + "UNION" );
						break;
					case TOKEN_UNIONALL:
						out.println( lineprefix + "UNION ALL" );
						break;
					}
					continue;
				}

				switch( qtoken.status ) {
				case STATUS_NONE:
					if( linebuffer.length() > 0 ) {
						out.println( lineprefix + linebuffer.toString() );
						linebuffer = new StringBuffer();
					}
					out.println( lineprefix + qtoken.token );
					break;
				case STATUS_SELECT:
				case STATUS_FROM:
				case STATUS_GROUPBY:
				case STATUS_ORDERBY:
					if( count++ > 0 ) {
						if( linebuffer.length() > 0 ) {
							if( lineprefix.length() + linebuffer.length() + 2 + qtoken.token.length() > maximumLineSize ) {
								out.println( lineprefix + linebuffer.toString() );
								linebuffer = new StringBuffer( lineheader );
							}
						}
						linebuffer.append( ", " );
					}
					if( STATUS_FROM == qtoken.status ) {
						int idx0 = qtoken.token.indexOf( "(" );
						int idx1 = qtoken.token.indexOf( "SELECT", idx0 );
						int idx2 = qtoken.token.indexOf( "FROM", idx1 );
						int idx3 = qtoken.token.lastIndexOf( ")" );
						if( idx0 >= 0 && idx1 >= 0 && idx2 >= 0 && idx3 >= idx2 ) {
							if( idx0+1 == idx1 || (qtoken.token.charAt(idx0+1) == ' ' && idx0+2 == idx1) ) {
								linebuffer.append( "(" );
								out.println( lineprefix + linebuffer.toString() );

								String query = qtoken.token.substring( idx1, idx3 );
								print( out, makeQueryTokenList(query, null), lineprefix + "                ", maximumLineSize );

								linebuffer = new StringBuffer( qtoken.token.substring(idx3) );
								break;
							}
						}
					}
					linebuffer.append( qtoken.token );
					break;
				case STATUS_WHERE:
					linebuffer.append( qtoken.token );
					out.println( lineprefix + linebuffer.toString() );
					linebuffer = new StringBuffer();
					break;
				}
			}
			if( linebuffer.length() > 0 ) out.println( lineprefix + linebuffer.toString() );
		}

		static QueryUtility.Token read( StringTokenizer tokenizer ) {
			if( tokenizer.hasMoreElements() ) {
				int status = TOKEN_NONE;
				String token = tokenizer.nextToken();

				while( token.charAt(0) == ' ' || token.charAt(0) == '\t' || token.charAt(0) == '\n' ) {
					if( !tokenizer.hasMoreElements() ) return null;

					status = TOKEN_BLANK;
					token = tokenizer.nextToken();
				}
				return new QueryUtility.Token( status, token );
			}

			return null;
		}
	}
}
