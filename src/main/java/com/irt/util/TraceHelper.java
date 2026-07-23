/*
 *	File Name:	TraceHelper.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/09/30		2.2.0c	create
 **/

package com.irt.util;

import java.lang.reflect.Method;

public class TraceHelper {
	// save it static to have it available on every call
	private static Method m;
	private static final String LINE_SEPARATOR = "\n";

	public static int CURRENT = 0;
	public static int CALLER = 1;
	public static char CLASSMETHOD_SEPERATOR = '.';

	static {
		try {
			try {
				m = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);// old jdk way( before jdk9 )
				if( m != null )
					m.setAccessible(true);
			} catch ( NoSuchMethodException ignore ) {
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * @param depth
	 *            : {@link #CURRENT}=0 or {@link #CALLER}=1 or deep level
	 * @return : {@link Class}
	 */
	public static Class<?> getClass( final int depth ) {
		try {
			StackTraceElement element = (m != null ?
					(StackTraceElement)m.invoke(new Throwable(), depth + 1)
					: new Throwable().getStackTrace()[depth+1]);
			return element.getClass();
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param depth
	 *            : {@link #CURRENT}=0 or {@link #CALLER}=1 or deep level
	 * @return : classMethodName eg. "com.irt.util.TraceHelper.getClassMethodName"
	 */
	public static String getClassMethodName( final int depth ) {
		try {
			StackTraceElement element = (m != null ?
					(StackTraceElement)m.invoke(new Throwable(), depth + 1)
					: new Throwable().getStackTrace()[depth+1]);
			return element.getClassName() + CLASSMETHOD_SEPERATOR + element.getMethodName();
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param depth
	 *            : {@link #CURRENT}=0 or {@link #CALLER}=1 or deep level
	 * @return : className
	 */
	public static String getClassName( final int depth ) {
		try {
			StackTraceElement element = (m != null ?
					(StackTraceElement)m.invoke(new Throwable(), depth + 1)
					: new Throwable().getStackTrace()[depth+1]);
			return element.getClassName();
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param depth
	 *            : {@link #CURRENT}=0 or {@link #CALLER}=1 or deep level
	 * @return : methodName
	 */
	public static String getMethodName( final int depth ) {
		try {
			StackTraceElement element = (m != null ?
					(StackTraceElement)m.invoke(new Throwable(), depth + 1)
					: new Throwable().getStackTrace()[depth+1]);
			return element.getMethodName();
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	public static String formatStackTrace( StackTraceElement[] stackTrace ) {
		StringBuilder buffer = new StringBuilder();
		for( StackTraceElement element : stackTrace ) {
			buffer.append(element).append(LINE_SEPARATOR);
		}
		return buffer.toString();
	}

	public static String formatCurrentStacktrace() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		return formatStackTrace(stackTrace);
	}

	public static StackTraceElement[] getCurrentStacktrace() {
		return Thread.currentThread().getStackTrace();
	}

}