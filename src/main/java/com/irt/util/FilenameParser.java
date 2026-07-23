/*
 *	File Name:	FilenameParser.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steps to use "FilenameParser":
 * 
 * <pre>
 * 1. Implement "FilenameParser"
 * 2. Identify file name pattern
 * 3. Define tools.conf
 * 4. Use filenameParser object
 * </pre>
 * 
 * 
 * 1. Implement under "com.irt.rbm.tools.ProcessRunner" or "com.irt.rbm.tools.Process" :
 * 
 * <pre>
 * public SomeProcess implements com.irt.rbm.tools.ProcessRunner {
 * 
 * private FilenameParser filenameParser = new FilenameParser();
 * 
 * public void setFilenameParser( FilenameParser parser ) {
 * 		this.filenameParser = parser;
 * }
 * 
 * public FilenameParser getFilenameParser() {
 * 		return this.filenameParser;
 * }
 * 
 * ....
 * 
 * }
 * </pre>
 * 
 * 
 * 
 * 2. Identify filename regex pattern(Regular Expression):
 * 
 * <pre>
 * For Example,
 * IF File names are like
 * 	=> "Inventory_20181003_2345.csv"
 * 	=> "Inventory_20181005_2345.csv"
 * 
 * THEN Identify change part is date and time.
 * 	=> "20181003_2345"
 * 	=> "20181005_2345"
 * 
 * SO REGEX PATTERN for Inventory file's date and time is
 * 	=> "^Inventory_(.*)_(.*).csv$"
 * 
 * </pre>
 * 
 * 
 * 
 * 3. Define tools.conf :
 * 
 * <pre>
 * ....
 * ProcessRunner.somerunner.FilenameParser.parsedKeys = datepart;timepart;
 * ProcessRunner.somerunner.FilenameParser.regex = ^Inventory_(.*)_(.*).csv$
 * ....
 * </pre>
 * 
 * 
 * 
 * 4. Use filenameParser.parse( filename ) usually in "FileProcessRunner" like:
 * 
 * <pre>
 * 
 * String filename = file.getName();
 * 
 * Map<String, Object> defaultMap = getFilenameParser().parse(filename);
 * 
 * String datepart = defaultMap.get("datepart");
 * String timepart = defaultMap.get("timepart");
 * 
 * com.irt.data.Date date = com.irt.data.Date.getInstance(datepart);
 * </pre>
 *
 */
public class FilenameParser implements RegexMapParser {//@formatter:on
	public static final String DEFAULT_EVENTTIME_KEY = "eventTime";

	private String filename;
	private String regex;
	private String parsedKeys;
	private String[] parsedKeyArray;

	private String[] parsedValueArray;

	public FilenameParser() {
	}

	public FilenameParser( String regex, String parsedKeys ) {
		this.regex = regex;
		setParsedKeys(parsedKeys);
	}

	public String[] getParsedKeyArray() {
		if( parsedKeyArray == null ) {
			setParsedKeys(this.parsedKeys);
		}
		return parsedKeyArray;
	}

	public Map<String, Object> getParsedMap() {
		Map<String, Object> map = new java.util.TreeMap<String, Object>();
		for( int i = 0; i < getParsedKeyArray().length; i++ ) {
			map.put(getParsedKeyArray()[i], getParsedValueArray()[i]);
		}
		return map;
	}

	public String[] getParsedValueArray() {
		if( parsedValueArray != null )
			return parsedValueArray;
		else
			parsedValueArray = new String[parsedKeyArray.length];

		if( getSource() == null ) {
			throw new IllegalStateException("parse source is not defined.");
		}

		return this.parsedValueArray = getParsedValueArray(getSource());
	}

	public String[] getParsedValueArray( String source ) {
		if( source == null ) {
			throw new IllegalStateException("parse source is not defined.");
		}

		String[] parsedValueArr = new String[parsedKeyArray.length];

		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(source);
		if( match.find() ) {
			int groupCount = match.groupCount();
			if( groupCount != parsedKeyArray.length ) {
				throw new IllegalStateException("parsedKey is not the same as groupCount: " + toString());
			} else {
				for( int i = 0; i < groupCount; i++ ) {
					String value = match.group(i + 1);
					if( value != null ) {
						parsedValueArr[i] = value;
					} else {
						parsedValueArr[i] = "";
					}
				}
			}
		}

		return parsedValueArr;
	}

	public String getRegex() {
		return regex;
	}

	public String getSource() {
		return filename;
	}

	/**
	 * all param supplied and filename also supplied?
	 */
	public boolean isParseable() {
		if( !isParseReady() ) {
			return false;
		} else {
			if( filename == null ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check all param supplied and only filename left?
	 */
	public boolean isParseReady() {
		boolean isValid = true;
		if( parsedKeyArray == null || parsedKeyArray.length == 0 ) {
			isValid = false;
		} else if( parsedValueArray == null ) {
			isValid = false;
		} else if( regex == null ) {
			isValid = false;
		}
		return isValid;
	}

	public boolean matched( String source ) {
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(source);
		return match.find();
	}

	public Map<String, Object> parse( String filename ) {
		Map<String, Object> map = new java.util.TreeMap<String, Object>();
		String[] parsedValueArr = getParsedValueArray(filename);
		for( int i = 0; i < getParsedKeyArray().length; i++ ) {
			map.put(getParsedKeyArray()[i], parsedValueArr[i]);
		}
		return map;
	}

	public void setParsedKeys( String parsedKeys ) {
		if( parsedKeys != null && parsedKeys.length() > 0 ) {
			this.parsedKeys = parsedKeys;
			String temp = parsedKeys.trim();
			if( temp.endsWith(";") )
				temp = temp.substring(0, temp.length() - 1);

			setParsedKeys(temp.split(";"));
		}
	}

	private void setParsedKeys( String[] parsedKeyArray ) {
		if( parsedKeyArray != null && parsedKeyArray.length > 0 ) {
			this.parsedKeyArray = parsedKeyArray;
			this.parsedValueArray = new String[parsedKeyArray.length];
		}
	}

	public void setRegex( String regex ) {
		this.regex = regex;
	}

	public void setSource( String filename ) {
		this.filename = filename;
	}

	public String toString() {
		return "FilenameParser{" + " regex: \"" + regex
				+ "\" parsedKeys: " + ( parsedKeyArray == null ? parsedKeyArray : java.util.Arrays.asList(parsedKeyArray) )
				+ " filename: " + filename + "}";
	}
}
