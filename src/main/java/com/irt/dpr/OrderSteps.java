/*
 *	File Name:	OrderSteps.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/10/30		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.Date;
import com.irt.servlet.ServletModelException;
import com.irt.system.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public interface OrderSteps {

	public interface Rdd {

		Date getRddByCalc( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, Date defaultDateWhenSapNull )
				throws ServletModelException, SQLException, DataException;

		Date getRddByCalc( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, int defaultDragDaysWhenSapNull )
				throws ServletModelException, SQLException, DataException;

		Date getRddByChinaOrderClosingTime( Map<String, Object> headerMap, Date rddFromSap, TimeZone timeZone );

		Date getRddFromSap( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, Date defaultDateWhenSapNull ) throws DataException;

	}

	public interface Revise {

		Map<String, Object> manageHelpBoard( String uniqId, Map<String, Object> boardMap, File... attachFiles )
				throws IOException, SQLException, DataException;

		boolean manageHelpBoardAttaches( Map<String, Object> boardParamMap, int boardNumber, File... fileNames )
				throws DataException, IOException, SQLException;

		List<Map<String, Object>> reviseChangeDiffList( String orderKey, String displayLanguage ) throws DataException, SQLException;

		public boolean reviseCommit( String reviseOrderKey, String reviseHelpType, int helpBoardSeqId ) throws DataException, IOException, SQLException;

		public Map<String, Object> reviseCommitProcessMailContentMap( String orderKey, SessionManager sessionMng, String displayLanguage ) throws DataException, SQLException, IOException;

		javax.mail.Message reviseCommitSendMail( Map<String, Object> mailContentMap ) throws DataException, IOException, SQLException;

		int reviseDetailUpdate( String orderKey ) throws SQLException, DataException;

		boolean reviseSimulationEnd( String orderKey ) throws DataException, SQLException;

		boolean reviseSimulationStart( String orderKey, Date rddDate ) throws DataException, SQLException;

	}

}
