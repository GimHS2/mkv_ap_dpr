/*
 *	File Name:	RBMTools.java
 *	Version:	2.2.8c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.8c	log4j2 적용( log4j-*.jar 대체하는 log4j-over-slf4j 적용하여 tools console logger 재구현 )
 *	jbaek		2019/04/30		2.2.8c	JndiUtil 사용 옵션 추가
 *	jbaek		2017/09/30		2.2.8c	SQLManager.getDBTimeZone()  rset.getTimestamp(int, Calendar)
 *	stghr12		2011/09/30		2.2.8	help 명령어 추가
 *										schedule 명령어에서 scheduleCode가 잘못된 경우 처리
 *	stghr12		2011/06/30		2.2.7	InterruptedException 처리
 *										ProcessRunner.getUsingDaemon() 사용
 *										!command 지원
 *	stghr12		2011/03/31		2.2.6	ScheduleProcessHandler 사용
 *	stghr12		2011/02/28		2.2.5	executeFile() 추가, RBMToolsDaemon부분 제거
 *	stghr12		2010/08/31		2.2.4	ToolsCommandExecuter 지원
 *	stghr12		2010/05/31		2.2.3	ProcessRunnerExaminer 로직 추가
 *	stghr12		2009/05/20		2.2.2	execute(): "passwd" encoding 기능 추가
 *										"defaultSystem" configure 사용
 *	stghr12		2009/01/31		2.2.1	execute(): throws IOException 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.data.DataException;
import com.irt.rbm.RBMSystem;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.system.SystemConfig;
import com.irt.util.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import org.apache.log4j.*;

/**
 *
 */
public class RBMTools {
	protected Configure configure;
	protected ToolsCommandExecuter commandExecuter;
	protected Map<String, ToolsCommandDescriptor> commandDescriptorMap;
	protected List<ToolsCommandDescriptor> commandDescriptorList;
	protected ScheduleProcessHandler scheduleProcessHandler;

	public void destroy() {}

	public void execute( String[] commands ) throws IllegalArgumentException, IOException, SQLException {
		if( "help".equals(commands[0]) || "h".equals(commands[0]) ) {
			if( commands.length > 1 ) {
				if( commandDescriptorMap == null ) {
					commandDescriptorMap = new java.util.HashMap<String, ToolsCommandDescriptor>();

					Stack<Object[]> stack = new Stack<Object[]>();

					stack.push( new Object[] { "", this.commandDescriptorList } );
					while( !stack.empty() ) {
						Object[] objects = stack.pop();
						List<ToolsCommandDescriptor> commandDescriptorList = (List<ToolsCommandDescriptor>)objects[1];

						for( ToolsCommandDescriptor commandDescriptor : commandDescriptorList ) {
							for( String alias : commandDescriptor.getCommandAliases() ) {
								commandDescriptorMap.put( objects[0] + alias, commandDescriptor );
								if( commandDescriptor.getSubCommandDescriptors() != null )
									stack.push( new Object[] { objects[0] + alias +" ", commandDescriptor.getSubCommandDescriptors() } );
							}
						}
					}
				}

				String command = commands[1];

				for( int c = 2; c < commands.length; c++ )
					command += " "+ commands[c];

				ToolsCommandDescriptor commandDescriptor = commandDescriptorMap.get( command );
				if( commandDescriptor == null )
					throw new IllegalArgumentException( "illegal command '"+ command +"'." );

				printCommandDescriptor( commandDescriptor, true );
			} else {
				for( ToolsCommandDescriptor commandDescriptor : commandDescriptorList )
					printCommandDescriptor( commandDescriptor, false );
			}
		} else if( "schedule".equals(commands[0]) || "sch".equals(commands[0]) ) {
			if( commands.length != 2 && commands.length != 3 ) throw new IllegalArgumentException( "illegal command." );

			SystemConfig systemConfig = configure.getSystemConfig( configure.getProperty("defaultSystem", "RBM") );
			SQLHandler handler = systemConfig.createSQLHandler( systemConfig.getMessageHandler() );

			Process process = null;
			String scheduleCode, scheduleName, extraValue;
			try {
				scheduleCode = commands[1];
				if( scheduleCode.length() < 10 ) scheduleCode = (scheduleCode +"          ").substring( 0, 10 );
				scheduleName = (String)SQLManager.getObjectValue( handler, "SELECT SCH_NAME FROM RBM_SCHEDULE WHERE SCH_CD = ?", scheduleCode );
				extraValue = ( commands.length > 2 ? commands[2] : null );

				process = scheduleProcessHandler.getInstance( handler, scheduleCode, scheduleName, extraValue );
				if( process == null )
					throw new IllegalArgumentException( "illegal schedule '"+ scheduleCode +"'." );

				process.execute();
			} catch( InterruptedException interruptEx ) {
			} catch( ProcessException processEx ) {
				( processEx.getCause() != null ? processEx.getCause() : processEx ).printStackTrace( System.out );
			} finally {
				try { handler.close(); } catch( Exception ignored ) {}
				try { if( process != null ) process.close(); } catch( Exception ignored ) {}
			}
		} else if( "run".equals(commands[0]) ) {
			if( commands.length < 2 ) throw new IllegalArgumentException( "illegal command." );

			String runnerName = commands[1];
			ProcessRunner runner = configure.getProcessRunner( runnerName );
			if( runner == null ) throw new IllegalArgumentException( "illegal runner '"+ runnerName +"'." );

			if( commands.length == 2 ) {
				try {
					runner.execute();
				} catch( InterruptedException interruptEx ) {}
			} else if( commands.length > 2 ) {
				String[] args = new String[commands.length - 2];
				System.arraycopy( commands, 2, args, 0, args.length );
				runner.execute( args );
			}
		} else if( "passwd".equals(commands[0]) ) {
			if( commands.length < 2 || commands.length > 4 )
				throw new IllegalArgumentException( "illegal command." );

			SystemConfig systemConfig = configure.getSystemConfig( configure.getProperty("defaultSystem", "RBM") );
			SQLHandler handler = systemConfig.createSQLHandler( systemConfig.getMessageHandler() );
			try {
				com.irt.rbm.usr.UserUser db = new com.irt.rbm.usr.UserUser( handler );

				int count = 0;
				if( commands.length == 2 )
					count = db.encodePassword( commands[1] );
				else if( commands.length == 3 )
					count = db.encodePassword( commands[1], commands[2] );
				else if( commands.length == 4 )
					count = db.encodePassword( commands[1], commands[2], commands[3] );
				handler.commit();

				Logger.getLogger( "com.irt.rbm.tools.RBMTools" ).info( count +" rows updated." );
			} catch( DataException dataEx ) {
				dataEx.printStackTrace( System.out );
			} finally {
				try { handler.close(); } catch( Exception ignored ) {}
			}
		} else if( commandExecuter != null )
			commandExecuter.execute( configure, commands );
		else
			throw new IllegalArgumentException( "illegal command." );
	}

	public void executeFile( String filename ) throws IllegalArgumentException, IOException, SQLException {
		int lineNumber = 0;
		java.io.InputStreamReader inputStreamReader = new java.io.FileReader( filename );
		try {
			java.io.BufferedReader reader = new java.io.BufferedReader( inputStreamReader );

			while( true ) {
				String line = reader.readLine().trim();
				if( line == null ) break;

				lineNumber++;
				if( line.length() > 0 ) {
					try {
						execute( line.split(" ") );
					} catch( IllegalArgumentException argEx ) {
						System.out.println( "line "+ lineNumber +" <"+ line +">: "+ argEx.getMessage() );
						return;
					}
				}
			}
		} finally {
			try { inputStreamReader.close(); } catch( Exception ignored ) {}
		}
	}

	public static void executeTools( RBMTools tools, String[] args ) throws Exception {
		String command = null;
		String configureFileName = null;
		boolean debugging = false;
		boolean silentMode = false;
		try {
			for( int idx = 0; idx < args.length; idx++ ) {
				if( "-conf".equals(args[idx]) ) {
					try {
						configureFileName = args[++idx];
					} catch( ArrayIndexOutOfBoundsException arrEx ) {
						throw new IllegalArgumentException( "configure file name cannot be null." );
					}
				} else if( "-cmd".equals(args[idx]) ) {
					try {
						tools.commandExecuter = (ToolsCommandExecuter)RBMTools.class.getClassLoader().loadClass(args[++idx]).newInstance();
					} catch( ArrayIndexOutOfBoundsException arrEx ) {
						throw new IllegalArgumentException( "configure file name cannot be null." );
					} catch( ClassNotFoundException classEx ) {
						throw new IllegalArgumentException( classEx );
					} catch( IllegalAccessException accessEx ) {
						throw new IllegalArgumentException( accessEx );
					} catch( InstantiationException instantiationEx ) {
						throw new IllegalArgumentException( instantiationEx );
					}
				} else if( "-c".equals(args[idx]) ) {
					try {
						command = args[++idx];
					} catch( ArrayIndexOutOfBoundsException arrEx ) {
						throw new IllegalArgumentException( "configure file name cannot be null." );
					}
				} else if( "-debug".equals(args[idx]) )
					debugging = true;
				else if( "-h".equals(args[idx]) ) {
					tools.printUsage();
					return;
				} else if( "-s".equals(args[idx]) )
					silentMode = true;
				else if( args[idx].startsWith("@") && args[idx].length() > 1 && idx+1 == args.length )
					command = args[idx];
				else
					throw new IllegalArgumentException( "illegal argument '"+ args[idx] +"'." );
			}
		} catch( IllegalArgumentException argEx ) {
			System.err.println( "com.irt.rbm.tools.RBMTools : "+ argEx.getMessage() );
			tools.printUsage();
			throw argEx;
		}

		// init()
		configureFileName = tools.init( configureFileName );
		{
//			WriterAppender appender = new WriterAppender( new PatternLayout("[%d{HH:mm:ss}] %m%n"), System.out );
//			appender.setThreshold( debugging ? Level.DEBUG : Level.INFO );
//			Logger.getRootLogger().addAppender( appender );
			int errorCode = new Log4jReflector().setupRBMToolsConsoleLogger("RBMTools", debugging, "[%d{HH:mm:ss}] %m%n");
			if( errorCode != 0 ) {
				org.apache.logging.log4j.core.appender.ConsoleAppender console = org.apache.logging.log4j.core.appender.ConsoleAppender.newBuilder()
						.setTarget(org.apache.logging.log4j.core.appender.ConsoleAppender.Target.SYSTEM_OUT)
						.setLayout(org.apache.logging.log4j.core.layout.PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss}] %m%n").build())
						.setFilter(org.apache.logging.log4j.core.filter.ThresholdFilter.createFilter(
								(debugging ? org.apache.logging.log4j.Level.DEBUG : org.apache.logging.log4j.Level.INFO)
								, org.apache.logging.log4j.core.Filter.Result.ACCEPT, org.apache.logging.log4j.core.Filter.Result.NEUTRAL))
						.setName("RBMTools")
						.build();
				console.start();
				( (org.apache.logging.log4j.core.Logger)org.apache.logging.log4j.LogManager.getRootLogger() ).addAppender(console);
			}
		}

		// printLogo()
		if( !silentMode ) {
			tools.printLogo();
			System.out.println( "configure file '"+ configureFileName +"' read." );
		}

		// prompt()
		if( command == null )
			tools.prompt();
		else if( command.startsWith("@") )
			tools.executeFile( command.substring(1) );
		else
			tools.execute( command.split(" ") );

		// destroy()
		tools.destroy();
	}

	private static List<ToolsCommandDescriptor> getCommandDescriptors( MessageHandler msghandler, ToolsCommandExecuter commandExecuter ) {
		ToolsCommandDescriptor[] commandDescriptors = new ToolsCommandDescriptor[] {
			  ToolsCommandDescriptor.createDescriptor( msghandler, "!", "!", "COMMAND_DESC_1" )
			, ToolsCommandDescriptor.createDescriptor( msghandler, "@", "@", "COMMAND_DESC_2" )
			, ToolsCommandDescriptor.createDescriptor( msghandler, "help", new String[] { "h" }, "help | h [COMMAND]", "COMMAND_DESC_HELP" )
			, ToolsCommandDescriptor.createDescriptor( msghandler, "passwd", "passwd PARTYID [USERNAME [PASSWORD]]", "COMMAND_DESC_PASSWD" )
			, ToolsCommandDescriptor.createDescriptor( msghandler, "quit", new String[] { "q" }, "quit | q", "COMMAND_DESC_QUIT" )
			, ToolsCommandDescriptor.createDescriptor( msghandler, "run", "run RUNNERNAME [PARAMS]", "COMMAND_DESC_RUN" )
			, ToolsCommandDescriptor.createDescriptor( msghandler, "schedule", new String[] { "sch" }, "schedule | sch [SCHCD]", "COMMAND_DESC_SCHEDULE" )
		};

		List<ToolsCommandDescriptor> commandDescriptorList = null;
		if( commandExecuter != null ) {
			commandDescriptorList = commandExecuter.getCommandDescriptors( msghandler );
			if( commandDescriptorList != null )
				commandDescriptorList = new java.util.ArrayList<ToolsCommandDescriptor>( commandDescriptorList );
		}

		if( commandDescriptorList == null )
			return java.util.Arrays.asList( commandDescriptors );

		Set<String> commandSet = new java.util.HashSet<String>();
		for( ToolsCommandDescriptor commandDescriptor : commandDescriptorList )
			commandSet.add( commandDescriptor.getCommand() );

		for( ToolsCommandDescriptor commandDescriptor : commandDescriptors ) {
			if( !commandSet.contains(commandDescriptor.getCommand()) )
				commandDescriptorList.add( commandDescriptor );
		}
		java.util.Collections.sort( commandDescriptorList );

		return commandDescriptorList;
	}

	public String getName() {
		return "RBMTools";
	}

	public String init() throws Exception {
		return init( (String)null );
	}

	public String init( String configureFileName ) throws Exception {
		// configure File
		java.io.File file;
		if( configureFileName != null )
			file = new java.io.File( configureFileName );
		else {
			file = new java.io.File( configureFileName = "../conf/tools.conf" );
			if( !file.exists() )
				file = new java.io.File( configureFileName = "tools.conf" );
		}

		Properties properties = new Properties();
		java.io.InputStream inputStream = new java.io.FileInputStream( file );
		try {
			properties.load( inputStream );
			init( properties );
		} finally {
			try { inputStream.close(); } catch( Exception ignored ) {}
		}

		return file.getAbsolutePath();
	}

	public void init( Properties properties ) throws Exception {
		// configure.load()
		this.configure = new Configure();
		if( "Y".equals(System.getProperty("useJndiUtil")) || Boolean.parseBoolean(System.getProperty("useJndiUtil")) ) {
			JndiUtil.setupDataSourceInitialContext(properties);
		}
		this.configure.load( properties );

		// initialize log4j
		String log4jproperties = configure.getProperty( "log4j.properties" );
		if( log4jproperties != null ) {
			if( log4jproperties.endsWith(".xml") )
				Utility2.configureLog4j2Engine( log4jproperties );
			else
				new Log4jReflector().setupPropertyConfigurator( log4jproperties );
		}

		// com.irt.rbm.RBMSystem.initSystemEnv()
		SQLHandler handler = null;
		SystemConfig systemConfig = configure.getSystemConfig( configure.getProperty("defaultSystem", "RBM") );
		if( systemConfig == null ) throw new IOException( configure.getProperty("defaultSystem", "RBM") +" SystemConfig cannot be found." );
		try {
			com.irt.rbm.RBMSystem.initSystemEnv( handler = systemConfig.createSQLHandler( systemConfig.getMessageHandler() ) );
		} finally {
			try { handler.close(); } catch( Exception ignored ) {}
		}
		// set DBTimeZone for SQLManager.getDBTimeZone()
		String dbTimeZone = RBMSystem.getSystemEnv("SYS", "TimeZone;DBTimeZone");
		if( dbTimeZone != null && dbTimeZone.length() > 0 ) {
			System.setProperty("DBTimeZone", dbTimeZone);
		}

		// commandExecuter
		if( this.commandExecuter == null ) {
			String commandExecuter = configure.getProperty( "tools.commandExecuter" );
			if( commandExecuter != null )
				this.commandExecuter = (ToolsCommandExecuter)RBMTools.class.getClassLoader().loadClass(commandExecuter).newInstance();
		}
		this.commandDescriptorList = getCommandDescriptors( systemConfig.getMessageHandler(), this.commandExecuter );

		// scheduleProcessHandler
		String scheduleProcessHandler = configure.getProperty( "tools.scheduleProcessHandler" );
		if( scheduleProcessHandler != null )
			this.scheduleProcessHandler = (ScheduleProcessHandler)RBMTools.class.getClassLoader().loadClass(scheduleProcessHandler).newInstance();
		else
			this.scheduleProcessHandler = new ScheduleRunner( systemConfig );
	}

	public static void main( String[] args ) throws Exception {
		executeTools( new RBMTools(), args );
	}

	protected void printCommandDescriptor( ToolsCommandDescriptor commandDescriptor, boolean printingDetail ) {
		printCommandDescriptor( commandDescriptor, printingDetail, 0 );
	}

	protected void printCommandDescriptor( ToolsCommandDescriptor commandDescriptor, boolean printingDetail, int level ) {
		String format1, format2;

		format2 = "";
		for( int i = 0; i < level; i++ )
			format2 = format2 + "  ";
		format1 = format2 +"%s\n";
		format2 = format2 +"%-"+ (60 - level*2) +"s%s\n";

		if( commandDescriptor.getExplanation() == null )
			System.out.format( format1, commandDescriptor.getInstruction() );
		else
			System.out.format( format2, commandDescriptor.getInstruction(), commandDescriptor.getExplanation() );

		if( commandDescriptor.getSubCommandDescriptors() != null ) {
			for( ToolsCommandDescriptor subCommandDescriptor : commandDescriptor.getSubCommandDescriptors() )
				printCommandDescriptor( subCommandDescriptor, false, level+1 );
		}

		if( commandDescriptor.getDetailExplanations() != null && printingDetail ) {
			if( commandDescriptor.getSubCommandDescriptors() != null ) System.out.println();
			for( String detail : commandDescriptor.getDetailExplanations() )
				System.out.format( format1, detail );
		}
	}

	public void printLogo() {
		System.out.println();
		System.out.println( getName() +": version 2.2.0 created by stghr12" );
		System.out.println( "Copyright (c) 2003-2010, iRT Corporation. All rights reserved." );
		System.out.println();
	}

	public void printUsage() {
		System.out.println();
		System.out.println( "Usage: "+ getClass().getName() +" -conf <file> | -cmd <class> | -debug | -h | -s | -c <command> @<file>" );
		System.out.println();
	}

	public void prompt() {
		try {
			java.io.BufferedReader reader = new java.io.BufferedReader( new java.io.InputStreamReader(System.in) );

			System.out.println();
			while( true ) {
				System.out.print( getName() +"> " );

				String line = reader.readLine().trim();
				if( line.length() > 0 ) {
					if( "quit".equals(line) || "q".equals(line) ) break;

					try {
						if( line.startsWith("@") )
							executeFile( line.substring(1) );
						else if( line.startsWith("!") ) {
							java.lang.Process process = Runtime.getRuntime().exec( line.substring(1) );

							java.io.InputStream inputStream = process.getInputStream();
							try {
								int length;
								byte[] bytes = new byte[1024];

								while( (length = inputStream.read(bytes)) > 0 )
									System.out.write( bytes, 0, length );
							} finally {
								inputStream.close();
							}

							process.waitFor();
						} else
							execute( line.split(" ") );
					} catch( IllegalArgumentException argEx ) {
						System.out.println( line +" : "+ argEx.getMessage() );
					} catch( Exception ex ) {
						ex.printStackTrace( System.out );
					}
				}
			}
		} catch( IOException ioEx ) {
			ioEx.printStackTrace( System.err );
		}
	}

	public void start() {
		for( ProcessRunner runner : configure.getProcessRunners() )
			if( runner.getUsingDaemon() )
				runner.start();
	}

	public void stop() {
		for( ProcessRunner runner : configure.getProcessRunners() ) {
			if( runner instanceof com.irt.rbm.tools.ProcessRunnerExaminer )
				runner.interrupt();
		}

		for( ProcessRunner runner : configure.getProcessRunners() ) {
			if( !(runner instanceof com.irt.rbm.tools.ProcessRunnerExaminer) )
				runner.interrupt();
		}

		for( ProcessRunner runner : configure.getProcessRunners() ) {
			try {
				runner.join();
			} catch( InterruptedException interruptEx ) {}
		}
	}
}
