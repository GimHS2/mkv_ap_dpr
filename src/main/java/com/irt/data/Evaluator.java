/*
 *	File Name:	Evaluator.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.0	create
 *
**/

package com.irt.data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 */
public class Evaluator {
	private final static char NUMBER					= 'N';
	private final static char BOOLEAN					= 'B';

	private final static char OPERAND_PLUS						= '+';
	private final static char OPERAND_MINUS						= '-';
	private final static char OPERAND_MULIPLIER					= '*';
	private final static char OPERAND_DIVIDER					= '/';
	private final static char OPERAND_EQUALS					= '=';
	private final static char OPERAND_NOTEQUALS					= '!';
	private final static char OPERAND_GREATER_THAN				= '>';
	private final static char OPERAND_GREATER_THAN_OR_EQUALS	= '.';
	private final static char OPERAND_LESS_THAN					= '<';
	private final static char OPERAND_LESS_THAN_OR_EQUALS		= ',';
	private final static char OPERAND_LOGICAL_AND				= '&';
	private final static char OPERAND_LOGICAL_OR				= '|';

	private Object[] tokens;
	private char formulaType;

	public Evaluator( String formula ) throws IllegalArgumentException {
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer( formula, " \t\n()+-/*=!<>&|", true );

		int depth = 0;
		char previousChar = ' ';
		boolean isMinusValue = false;
		boolean expectedOperand = true;
		List tokenList = new java.util.ArrayList();
		while( tokenizer.hasMoreElements() ) {
			String token = tokenizer.nextToken();

			if( "=!<>&|".indexOf(token.charAt(0)) >= 0 ) {
				if( expectedOperand ) throw new IllegalArgumentException( token +" is not operand!" );

				if( previousChar == ' ' )
					previousChar = token.charAt(0);
				else {
					switch( token.charAt(0) ) {
					case '=':
						if( previousChar == '>' )
							tokenList.add( new Character(OPERAND_GREATER_THAN_OR_EQUALS) );
						else if( previousChar == '<' )
							tokenList.add( new Character(OPERAND_LESS_THAN_OR_EQUALS) );
						else if( previousChar == '!' || previousChar == '=' )
							tokenList.add( new Character(previousChar) );
						else
							throw new IllegalArgumentException( previousChar + token +" is illegal operand!" );
						break;
					case '&':
					case '|':
						if( previousChar == token.charAt(0) )
							tokenList.add( new Character(previousChar) );
						else
							throw new IllegalArgumentException( previousChar + token +" is illegal operand!" );
						break;
					default:
						throw new IllegalArgumentException( previousChar + token +" is illegal operand!" );
					}
					expectedOperand = true;
					previousChar = ' ';
				}
				continue;
			}

			if( previousChar != ' ' ) {
				switch( previousChar ) {
				case '>':
				case '<':
					tokenList.add( new Character(previousChar) );
					expectedOperand = true;
					previousChar = ' ';
					break;
				default:
					throw new IllegalArgumentException( previousChar + " is illegal operand!" );
				}
			}

			switch( token.charAt(0) ) {
			case ' ':
			case '\t':
			case '\n':
				continue;
			case '-':
				if( expectedOperand ) {
					if( isMinusValue )
						throw new IllegalArgumentException( "illegal formula!" );
					isMinusValue = true;
					break;
				}
			case '+':
			case '*':
			case '/':
				if( expectedOperand )
					throw new IllegalArgumentException( token +" is not operand!" );
				expectedOperand = true;
				tokenList.add( new Character(token.charAt(0)) );
				break;
			case '(':
				depth++;
				if( !expectedOperand )
					new IllegalArgumentException( token +" is not operator!" );
				if( isMinusValue ) {
					tokenList.add( new Double(-1) );
					tokenList.add( new Character('*') );
					isMinusValue = false;
				}
				tokenList.add( new Character(token.charAt(0)) );
				break;
			case ')':
				if( --depth < 0 )
					throw new IllegalArgumentException( "illegal formula!" );
				else if( expectedOperand )
					new IllegalArgumentException( token +" is not operand!" );
				tokenList.add( new Character(token.charAt(0)) );
				break;
			default:
				if( !expectedOperand )
					new IllegalArgumentException( token +" is not operator!" );

				try {
					double doubleValue = Double.parseDouble( token );
					if( isMinusValue ) doubleValue *= -1;
					tokenList.add( new Double(doubleValue) );
				} catch( NumberFormatException numberEx ) {
					if( isMinusValue ) {
						tokenList.add( new Double(-1) );
						tokenList.add( new Character('*') );
					}
					tokenList.add( token );
				}
				isMinusValue = false;
				expectedOperand = false;
			}
		}

		Stack stack = new Stack();
		List paramList = new java.util.ArrayList();
		for( Object token : tokenList ) {
			if( token instanceof String || token instanceof Number )
				paramList.add( token );
			else {
				char operand = ((Character)token).charValue();
				String priorOperand = "";

				switch( operand ) {
				case OPERAND_MULIPLIER:
				case OPERAND_DIVIDER:
					priorOperand = "*/";
					break;
				case OPERAND_PLUS:
				case OPERAND_MINUS:
					priorOperand = "*/+-";
					break;
				case OPERAND_EQUALS:
				case OPERAND_NOTEQUALS:
				case OPERAND_GREATER_THAN:
				case OPERAND_GREATER_THAN_OR_EQUALS:
				case OPERAND_LESS_THAN:
				case OPERAND_LESS_THAN_OR_EQUALS:
					priorOperand = "*/+-=!<,>.";
					break;
				case OPERAND_LOGICAL_AND:
				case OPERAND_LOGICAL_OR:
				case ')':
					priorOperand = "*/+-=!<,>.&|";
					break;
				}

				while( !stack.empty() ) {
					Character ch = (Character)stack.pop();
					if( priorOperand.indexOf(ch.charValue()) < 0 ) {
						stack.push( ch );
						break;
					}
					paramList.add( ch );
				}
				if( operand == ')' )
					stack.pop();
				else
					stack.push( token );
			}
		}
		while( !stack.empty() ) paramList.add( stack.pop() );

		stack = new Stack();
		for( Object token : paramList ) {
			if( token instanceof Number || token instanceof String )
				stack.push( new Character(NUMBER) );
			else {
				char type2 = ((Character)stack.pop()).charValue();
				char type1 = ((Character)stack.pop()).charValue();

				switch( ((Character)token).charValue() ) {
				case OPERAND_PLUS:
				case OPERAND_MINUS:
				case OPERAND_MULIPLIER:
				case OPERAND_DIVIDER:
					if( !(type1 == NUMBER && type2 == NUMBER) )
						throw new IllegalArgumentException( "illegal formula!" );
					stack.push( new Character(NUMBER) );
					break;
				case OPERAND_EQUALS:
				case OPERAND_NOTEQUALS:
				case OPERAND_GREATER_THAN:
				case OPERAND_GREATER_THAN_OR_EQUALS:
				case OPERAND_LESS_THAN:
				case OPERAND_LESS_THAN_OR_EQUALS:
					if( !(type1 == NUMBER && type2 == NUMBER) )
						throw new IllegalArgumentException( "illegal formula!" );
					stack.push( new Character(BOOLEAN) );
					break;
				case OPERAND_LOGICAL_AND:
				case OPERAND_LOGICAL_OR:
					if( !(type1 == BOOLEAN && type2 == BOOLEAN) )
						throw new IllegalArgumentException( "illegal formula!" );
					stack.push( new Character(BOOLEAN) );
					break;
				}
			}
		}

		this.formulaType = ((Character)stack.pop()).charValue();
		this.tokens = paramList.toArray();
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		for( int t = 0; t < tokens.length; t++ )
			if( tokens[t] instanceof String )
				fieldKeySet.add( (String)tokens[t] );
	}

	public Object evaluate( Map recordMap ) {
		if( recordMap == null ) return null;

		Stack stack = new Stack();
		for( int t = 0; t < tokens.length; t++ ) {
			if( tokens[t] instanceof Number )
				stack.push( tokens[t] );
			else if( tokens[t] instanceof String ) {
				String token = (String)tokens[t];
				do {
					String[] keys = token.split( "\\?", 2 );
					token = null;

					Object value = recordMap.get( keys[0] );
					if( value == null ) {
						if( keys.length == 1 ) return null;

						try {
							stack.push( Double.valueOf(keys[1]) );
						} catch( NumberFormatException numEx ) {
							token = keys[1];
						}
					} else if( value instanceof Number )
						stack.push( value );
					else if( value instanceof String )
						stack.push( Double.valueOf((String)value) );
					else
						return null;
				} while( token != null );
			} else {
				Object value2 = stack.pop();
				Object value1 = stack.pop();

				switch( ((Character)tokens[t]).charValue() ) {
				case '+':
					stack.push( new Double(((Number)value1).doubleValue() + ((Number)value2).doubleValue()) );
					break;
				case '-':
					stack.push( new Double(((Number)value1).doubleValue() - ((Number)value2).doubleValue()) );
					break;
				case '*':
					stack.push( new Double(((Number)value1).doubleValue() * ((Number)value2).doubleValue()) );
					break;
				case '/':
					stack.push( new Double(((Number)value1).doubleValue() / ((Number)value2).doubleValue()) );
					break;
				case '=':
					stack.push( new Boolean(((Number)value1).doubleValue() == ((Number)value2).doubleValue()) );
					break;
				case '!':
					stack.push( new Boolean(((Number)value1).doubleValue() != ((Number)value2).doubleValue()) );
					break;
				case '<':
					stack.push( new Boolean(((Number)value1).doubleValue() < ((Number)value2).doubleValue()) );
					break;
				case ',':	// <=
					stack.push( new Boolean(((Number)value1).doubleValue() <= ((Number)value2).doubleValue()) );
					break;
				case '>':
					stack.push( new Boolean(((Number)value1).doubleValue() > ((Number)value2).doubleValue()) );
					break;
				case '.':	// >=
					stack.push( new Boolean(((Number)value1).doubleValue() >= ((Number)value2).doubleValue()) );
					break;
				case '|':
					stack.push( new Boolean(((Boolean)value1).booleanValue() || ((Boolean)value2).booleanValue()) );
					break;
				case '&':
					stack.push( new Boolean(((Boolean)value1).booleanValue() && ((Boolean)value2).booleanValue()) );
					break;
				}
			}
		}

		return stack.pop();
	}

	public boolean isBooleanType() {
		return ( formulaType == BOOLEAN );
	}
}
