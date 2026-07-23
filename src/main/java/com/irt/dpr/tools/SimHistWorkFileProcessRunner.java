/*
 *	File Name:	SimHistWorkFileProcessRunner.java
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

package com.irt.dpr.tools;

import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.rbm.tools.FileWatchRunner;
import com.irt.rbm.tools.ProcessException;
import com.irt.rbm.tools.RBMTools;
import com.irt.rbm.tools.RBMToolsDaemon;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.system.SystemConfig;
import com.irt.util.FileUtil;
import com.irt.util.TableProp;
import com.irt.util.cst.ReflectUtil;

import java.io.File;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

public class SimHistWorkFileProcessRunner extends FileWatchRunner {//@formatter:on
	static final String NEWLINE = "\n";

	static final String systemContent = ""
			+ NEWLINE + "	systemName								=	" + SimHistWorkFileProcessRunner.class.getSimpleName() + " Example System"
			+ NEWLINE + "	messageSource							=	com.irt.rbm.mesg.RBMMessages"
			+ NEWLINE + "	tempDirectory							=	${user.dir}/tools/tmp"
			// + NEWLINE + " log4j.properties = ${user.dir}/ools/conf/log4j.properties.org"
			+ NEWLINE + "	DataSource.dpr.driverClassName			=	oracle.jdbc.driver.OracleDriver"
			+ NEWLINE + "	DataSource.dpr.url						=	jdbc:oracle:thin:@192.168.0.15:1521/RBM2"
			+ NEWLINE + "	DataSource.dpr.username					=	dpradmin"
			+ NEWLINE + "	DataSource.dpr.password					=	dpradmin"
			+ NEWLINE + "	DataSource.dpr.maxActive				=	20"
			+ NEWLINE + "	DataSource.dpr.maxWait					=	10000"
			+ NEWLINE + "	RBMSystem.SQLHandler					=	com.irt.rbm.RBMDataHandler"
			+ NEWLINE + "	RBMSystem.dataSource					=	dpr"
			+ NEWLINE + "";

	// if backupDirectory not defined then file is not deleted, and the file stays at the original place.
	// if backupDirectory is defined then file is moved to backup directory
	// + NEWLINE + " ProcessRunner.wk.backupDirectory = /tmp/"
	// + NEWLINE + " ProcessRunner.wk.errorDirectory = /tmp/"
	static final String runnerContent = ""

			+ NEWLINE + "	ProcessRunner.wk2.className				=	" + SimHistWorkFileProcessRunner.class.getCanonicalName()
			+ NEWLINE + "	ProcessRunner.wk2.system					=	RBM"
			+ NEWLINE + "	ProcessRunner.wk2.directory				=	/proc"

			+ NEWLINE + " ProcessRunner.wk2.FilenameParser.parsedKeys = transferType;orderKey;eventTime"
			+ NEWLINE + " ProcessRunner.wk2.FilenameParser.regex = ^(SRES)_(.*)_(.*).xml$"

			+ NEWLINE + "	ProcessRunner.wk2.TableProp.schemaClassName	=	com.irt.dpr.Schema"
			+ NEWLINE + "	ProcessRunner.wk2.TableProp.schemaTableKey	=	SimulationHistory"

			+ NEWLINE + "	ProcessRunner.wk2.maxThreadCount			=	5"
//			+ NEWLINE + "	ProcessRunner.wk2.checkMinute			=	1"
//			+ NEWLINE + "	ProcessRunner.wk2.minimumCheckMillis		=	150"
			+ NEWLINE + "	ProcessRunner.wk2.verboseLevel			=	5"

			+ NEWLINE + "	ProcessRunner.wk.className				=	" + SimHistWorkFileProcessRunner.class.getCanonicalName()
			+ NEWLINE + "	ProcessRunner.wk.system					=	RBM"
			+ NEWLINE + "	ProcessRunner.wk.directory				=	/tmp"

			+ NEWLINE + " ProcessRunner.wk.FilenameParser.parsedKeys = transferType;orderKey;eventTime"
			+ NEWLINE + " ProcessRunner.wk.FilenameParser.regex = ^(SRES)_(.*)_(.*).xml$"

			+ NEWLINE + "	ProcessRunner.wk.TableProp.schemaClassName	=	com.irt.dpr.Schema"
			+ NEWLINE + "	ProcessRunner.wk.TableProp.schemaTableKey	=	SimulationHistory"
			+ NEWLINE + "	ProcessRunner.wk.checkMinute			=	1"

			+ NEWLINE + "	ProcessRunner.wk.maxThreadCount			=	5"
//			+ NEWLINE + "	ProcessRunner.wk.checkMinute			=	1"
//			+ NEWLINE + "	ProcessRunner.wk.minimumCheckMillis		=	150"
			+ NEWLINE + "	ProcessRunner.wk.verboseLevel			=	5"

			+ NEWLINE + "";

	public static void main( String[] args ) throws Exception {
		List<String> argList = new ArrayList<String>(java.util.Arrays.asList(args));
		if( !argList.contains("-daemon") ) {
			argList.add("-daemon");
		}

		String template = systemContent + runnerContent;
		String content = PatternRecordFormat.getInstance(template)
				.format(Record.createMap("user.dir", System.getProperty("user.dir")), null);

		BasicConfigurator.configure();// console logger

		RBMTools t = new RBMTools();
		Properties properties = new Properties();

		properties.load(new StringReader(content));

		String tempDirectoryPath = properties.getProperty("tempDirectory");
		File parent = new File(tempDirectoryPath).getParentFile();
		File tempDirectory = FileUtil.getCreatedDir("tmp", parent);
		if( !tempDirectory.exists() )
			throw new IllegalStateException("'tempDirectory' is mandatory.");

		t.init(properties);

		if( argList.contains("-daemon") ) {
			RBMToolsDaemon d = new RBMToolsDaemon();

			ReflectUtil.setDeclaredFieldObject(RBMToolsDaemon.class, d, "tools", t);

			d.start();
		} else {
			t.execute(new String[] {
					"run", "wk"
			});
		}
	}

	private static final int DEFAULT_HIST_KEEP_DAYS = 90;
	private static final int DEFAULT_HIST_DELETE_ROWS_MAX = 10000;
	@SuppressWarnings("unused")
	private static final int DEFAULT_HIST_DELETE_INTERVAL_MINUTE = 5;

	public SimHistWorkFileProcessRunner( SystemConfig systemConfig, File directory ) {
		super(systemConfig, directory);
	}

	/** to insert production data without error in dev environment */
	private String fakeOrderKey;
	private int histKeepDays = DEFAULT_HIST_KEEP_DAYS;
	private Date lastDelExecDate;
	private Table table;
	private TableProp tableProp = new TableProp();

	public void setFakeOrderKey( String fakeOrderKey ) {
		this.fakeOrderKey = fakeOrderKey;
	}

	public void setHistKeepDays( int histKeepDays ) {
		if( histKeepDays <= 0 ) {
			throw new IllegalStateException("histKeepDays should be greater than 0.");
		}
		this.histKeepDays = histKeepDays;
	}

	public void setTableProp( TableProp tableProp ) {
		this.tableProp = tableProp;
	}

	public TableProp getTableProp() {
		return tableProp;
	}

	private int deleteOldHistory( SQLHandler handler, int histKeepDays, int maxDelRows ) throws SQLException {
		String sql = null;
		Object[] bindVars = null;
		PreparedStatement pstmt = null;
		try {

			String query = "SELECT COUNT(*) CNT FROM DPR_SIMULATION_HIST SIMHST"
					+ " WHERE SIMHST.SIM_OUT_DATETIME < TRUNC(SYSDATE - ?)"
					+ " AND SIMHST.REGDATE < SYSDATE - INTERVAL '5' MINUTE"
					+ " AND ROWNUM <= ?";

			bindVars = new Object[] { histKeepDays, maxDelRows };
			Integer cnt = -1;

			try {
				cnt = SQLManager.getInt(handler, query, bindVars);
			} catch( SQLException sqlEx ) {
				getLogger().error(getDescription() + " bindVars: " + java.util.Arrays.asList(bindVars) + " sql: \n" + query, sqlEx);
				throw sqlEx;
			}
			if( cnt > 0 ) {
				sql = "DELETE FROM DPR_SIMULATION_HIST SIMHST"
						+ " WHERE ROWID IN ("
						+ " SELECT ROWID FROM DPR_SIMULATION_HIST SIMHST"
						+ " WHERE SIMHST.SIM_OUT_DATETIME < TRUNC(SYSDATE - ?)"
						+ " AND SIMHST.REGDATE < SYSDATE - INTERVAL '5' MINUTE"
						+ " AND ROWNUM <= ?"
						+ ")";
				pstmt = handler.getConnection().prepareStatement(sql);
				SQLManager.bindVariables(pstmt, bindVars);
				int ret = pstmt.executeUpdate();
				if( ret > 0 ) {
					this.lastDelExecDate = new Date();
					handler.commit();
				}
				return ret;
			} else {
				return -1;
			}
		} catch( SQLException sqlEx ) {
			getLogger().error(getDescription() + " bindVars: " + java.util.Arrays.asList(bindVars) + " sql: \n" + sql, sqlEx);
			throw sqlEx;
		} finally {
			try {
				handler.rollback();
			} catch( SQLException ignored ) {
			}
			try {
				pstmt.close();
			} catch( Exception ignored ) {
			}
		}
	}

	private boolean isDeletedToday() {
		if( lastDelExecDate == null )
			return false;

		// is executed within one day ?
		return lastDelExecDate.before(com.irt.data.Date.getInstance().getDate(-1));
	}

	@Override
	public com.irt.rbm.tools.Process getProcessInstance( SQLHandler handler, File workfile ) throws ProcessException {
		int deletedRows = -1;
		if( !isDeletedToday() ) {
			try {
				deletedRows = deleteOldHistory(handler, histKeepDays, DEFAULT_HIST_DELETE_ROWS_MAX);
			} catch( SQLException sqlEx ) {
				throw new ProcessException(sqlEx);
			}
		}

		if( !workfile.exists() ) {
			getLogger().warn(getDescription() + " workfile not exist. : " + workfile.getAbsolutePath());
			throw new ProcessException(getDescription() + " workfile not exists. :" + workfile.getAbsolutePath());
		} else if( !tableProp.isValid() ) {
			getLogger().error(getDescription() + " tableProp is mandatory. : " + tableProp);
			throw new ProcessException(getDescription() + " tableProp is mandatory.");
		}

		if( table == null ) {
			table = tableProp.getInstance();
			if( table == null )
				throw new ProcessException(getDescription() + " table is mandatory. : " + tableProp);
		}
		if( !getFilenameParser().isParseReady() ) {
			throw new ProcessException(getDescription() + " filenameParser is mandatory. : " + getFilenameParser());
		}

		SimHistWorkProcess proc = new SimHistWorkProcess(handler, workfile, table);
		proc.setFilenameParser(getFilenameParser());

		if( fakeOrderKey != null ) {
			proc.setFakeOrderKey(fakeOrderKey);
		}

		getLogger().trace(getDescription() + " histKeepDays: " + histKeepDays + " lastDelExecDate: " + lastDelExecDate
				+ " deletedRows:" + deletedRows);

		return proc;
	}
}
