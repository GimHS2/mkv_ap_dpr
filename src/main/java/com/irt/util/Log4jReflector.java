/*
 *	File Name:	Log4jReflector.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0	create
 *
**/

package com.irt.util;

import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.*;

/**
 * runtime시에 log4j 의 버전을 1.x 혹은 2.x를 결정할 수 있도록 하기 위한 java reflection class.
 * 실제 실행 환경의 lib에 log4j 1.x를 추가하면 log4j 1.x를 사용 할 수 있음.
 *
 * 기본 compile시의 라이브러리는 2.x로 하고 1.x는 사용하지 않도록 설정.
 * ( slf4j의 log4j-over-slf4j lib사용하여 기존 소스는 변경없이 compile시에는 문제가 없도록 함.
 * Log4jReflector를 구현한 이유는 RBMTools console logging 설정이 WriterAppender를 사용하는데
 * slf4j의 log4j-over-slf4j에는 WriterAppender는 구현되지 않았기 때문임.  )
 *
 */
public class Log4jReflector {

	public static void Main( String[] args ) {
		int errorCode = new Log4jReflector().setupRBMToolsConsoleLogger( "RBMTools", true, "[%d{HH:mm:ss}] %m%n" );
		if( errorCode == 0 )
			System.out.println( "RBMTools log4j1.x console logger init." );
		else
			System.out.println( "RBMTools log4j1.x console logger init failed with errorCode(" + errorCode + ")" );
	}

	Object createLevel( String levelName ) {
		Class<?> _level = null;
		try {
			_level = Class.forName( "org.apache.log4j.Level" );
		} catch ( ClassNotFoundException e ) {
			e.printStackTrace();
		}
		if( _level == null )
			return null;
		try {
			Method _toLevel = _level.getMethod( "toLevel", String.class );
			try {
				return _toLevel.invoke( null, levelName );
			} catch( IllegalAccessException e ) {
				e.printStackTrace();
			} catch( IllegalArgumentException e ) {
				e.printStackTrace();
			} catch( InvocationTargetException e ) {
				e.printStackTrace();
			}
		} catch( NoSuchMethodException e ) {
			e.printStackTrace();
		} catch( SecurityException e ) {
			e.printStackTrace();
		}
		return null;
	}


	Object createPatternLayout( String pattern ) {
		try {
			return newInstance( "org.apache.log4j.PatternLayout", pattern );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	Object createWriterAppender( Object patternLayout, OutputStream out ) {
		try {
			Class<?> _layout = getClassForName( "org.apache.log4j.Layout" );
			if( _layout == null )
				return null;
			Class<?> _out = getClassForName( "java.io.OutputStream" );
			return newInstance( "org.apache.log4j.WriterAppender", new Class<?>[] { _layout, _out }, patternLayout, out );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	Class<?> getClassForName( String className ) {
		Class<?> _class = null;
		try {
			_class = Class.forName( className );
		} catch( ClassNotFoundException e ) {
			e.printStackTrace();
		}
		return _class;
	}

	Object getRootLogger() {
		Class<?> _logger = null;
		try {
			_logger  = Class.forName( "org.apache.log4j.Logger" );
		} catch( ClassNotFoundException e ) {
			e.printStackTrace();
		}
		if( _logger == null )
			return null;
		try {
			Method _getRootLogger = _logger.getMethod( "getRootLogger" );
			try {
				return _getRootLogger.invoke( null );
			} catch( IllegalAccessException e ) {
				e.printStackTrace();
			} catch( IllegalArgumentException e ) {
				e.printStackTrace();
			} catch( InvocationTargetException e ) {
				e.printStackTrace();
			}
		} catch( NoSuchMethodException e ) {
			e.printStackTrace();
		} catch( SecurityException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public Object newInstance( String className, Object...args ) throws Exception {
		return newInstance( className, null, args );
	}

	public Object newInstance( String className, Class<?>[] types, Object... args ) throws Exception {
		Class<?> clazz = Class.forName( className );
		if( args == null || args.length == 0 ) {
			return clazz.newInstance();
		}

		List<Class<?>> argTypes = new ArrayList<Class<?>>();
		if( types == null ) {
			for(Object object : args) {
				argTypes.add(object.getClass());
			}
		} else {
			for( Class<?> cls : types ) {
				argTypes.add( cls );
			}
		}
		Constructor<?> explicitConstructor = clazz.getConstructor( argTypes.toArray(new Class[argTypes.size()]) );
		return explicitConstructor.newInstance( args );
	}

	Object setRootLoggerAddAppender( Object rootLoggerInstance, Object writerAppenderInstance ) {
		Class<?> _logger = rootLoggerInstance.getClass();
		Class<?> _appender = getClassForName( "org.apache.log4j.Appender" );
		if( _appender == null )
			return -1;
		try {
			Method _addAppender = _logger.getMethod( "addAppender", _appender );
			try {
				Object ret = _addAppender.invoke( rootLoggerInstance, writerAppenderInstance );
				return 0;
			} catch( IllegalAccessException e ) {
				e.printStackTrace();
			} catch( IllegalArgumentException e ) {
				e.printStackTrace();
			} catch( InvocationTargetException e ) {
				e.printStackTrace();
			}
		} catch( NoSuchMethodException e ) {
			e.printStackTrace();
		} catch( SecurityException e ) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 이 method는 아래의 log4j 1.x의 console logger를 실행시키는 코드임.
	 *
	 * WriterAppender appender = new WriterAppender( new PatternLayout("[%d{HH:mm:ss}] %m%n"), System.out );
	 * appender.setName( "RBMTools" );
	 * appender.setThreshold( debugging ? Level.DEBUG : Level.INFO );
	 * Logger.getRootLogger().addAppender( appender );
	 */
	public int setupRBMToolsConsoleLogger( String appenderName, boolean debugging, String pattern ) {
		Object patternLayout = createPatternLayout( pattern );
		if( patternLayout == null )
			return -10;
		Object writerAppender = createWriterAppender( patternLayout, System.out );
		if( writerAppender == null )
			return -20;
		Object level = null;
		if( debugging ) {
			level = createLevel( "DEBUG" );
		} else {
			level = createLevel( "INFO" );
		}
		if( level == null )
			return -30;
		writerAppender = setWriterAppenderConfigure( writerAppender, appenderName, level );
		if( writerAppender == null )
			return -40;
		Object rootLogger = getRootLogger();
		if( rootLogger == null )
			return -50;
		rootLogger = setRootLoggerAddAppender( rootLogger, writerAppender );
		if( rootLogger == null )
			return -60;
		return 0;
	}

	/*
	 * 이 method는 아래의 log4j 1.x의 PropertyConfigurator로 log4j를 설정 실행 코드임.
	 *
	 * PropertyConfigurator.configure( log4jproperties );
	 */
	public int setupPropertyConfigurator( String log4jFilename ) {
		Class<?> _propertyConfigurator = getClassForName( "org.apache.log4j.PropertyConfigurator" );
		try {
			Method _configure = _propertyConfigurator.getMethod( "configure", String.class );
			try {
				Object ret = _configure.invoke( null, log4jFilename );
				return 0;
			} catch( IllegalAccessException e ) {
				e.printStackTrace();
			} catch( IllegalArgumentException e ) {
				e.printStackTrace();
			} catch( InvocationTargetException e ) {
				e.printStackTrace();
			}
		} catch( NoSuchMethodException e ) {
			e.printStackTrace();
		} catch( SecurityException e ) {
			e.printStackTrace();
		}

		return -1;
	}

	Object setWriterAppenderConfigure( Object writerAppenderInstance, String appenderName, Object log4jLevel ) {
		Class<?> _writerAppender = writerAppenderInstance.getClass();
		Class<?> _priority = getClassForName( "org.apache.log4j.Priority" );
		try {
			Method _setName = _writerAppender.getMethod( "setName", String.class );
			Method _setThreshold = _writerAppender.getMethod( "setThreshold", _priority );
			try {
				_setName.invoke( writerAppenderInstance, appenderName );
				_setThreshold.invoke( writerAppenderInstance, log4jLevel );
				return writerAppenderInstance;
			} catch( IllegalAccessException e ) {
				e.printStackTrace();
			} catch( IllegalArgumentException e ) {
				e.printStackTrace();
			} catch( InvocationTargetException e ) {
				e.printStackTrace();
			}
		} catch( NoSuchMethodException e ) {
			e.printStackTrace();
		} catch( SecurityException e ) {
			e.printStackTrace();
		}
		return null;
	}
}
