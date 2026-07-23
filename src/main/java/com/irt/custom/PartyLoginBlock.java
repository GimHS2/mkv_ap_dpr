/*
 *	File Name:	PartyLoginBlock.java
 *	Version:	2.2.1c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1c	신규 UI/UX 적용
 *	jbaek		2018/12/30		2.2.0c	create
 *
**/
package com.irt.custom;

import com.irt.data.Condition;
import com.irt.data.Week;
import com.irt.rbm.usr.UserParty;
import com.irt.rbm.usr.UserUser;
import com.irt.sql.SQLHandler;
import com.irt.util.StringUtil;
import com.irt.util.cst.DateTimeUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class PartyLoginBlock {//@formatter:on

	Logger logger = Logger.getLogger(PartyLoginBlock.class);

	private static final String DEFAULT_TEMPLATE_LOGIN_BLOCK_KEY = "MSG_TEMPLATE_LOGIN_BLOCK";
	private static final String DEFAULT_TEMPLATE_MTNCE_BLOCK_KEY = "MSG_TEMPLATE_MTNCE_BLOCK";
	private static final String PLACE_PREFIX = "{{";
	private static final String PLACE_SUFFIX = "}}";

	public PartyLoginBlock() {
	}

	public boolean isWithinBlockFrame( String startIsoDate, String endIsoDate, long nowAt ) {
		if( startIsoDate == null || endIsoDate == null )
			return false;

		try {
			java.util.Date blockStart = DateTimeUtil.parseISODate(startIsoDate);
			java.util.Date blockEnd = DateTimeUtil.parseISODate(endIsoDate);

			if( nowAt > blockStart.getTime()
					&& nowAt < blockEnd.getTime() ) {
				return true;
			}
		} catch( ParseException ignored ) {
			logger.error(ignored);
		}

		return false;
	}

	private boolean isWithinBlockFrame( Date startDateCvtFromIso, Date endDateCvtFromIso, long nowAt ) {
		if( nowAt > startDateCvtFromIso.getTime()
				&& nowAt < endDateCvtFromIso.getTime() ) {
			return true;
		}

		return false;
	}

	private Date getNoticeStartDate( Date blockStartDateCvtFromIso ) {
		com.irt.data.Week noticeStartWeek = new Week(blockStartDateCvtFromIso);
		noticeStartWeek.addWeeks(-2);
		return noticeStartWeek.getFirstday().getTime();
	}

	private boolean isWithinNoticeFrame( Date blockStartDateCvtFromIso, Date blockEndDateCvtFromIso, long nowAt ) {
		if( nowAt > getNoticeStartDate(blockStartDateCvtFromIso).getTime()
				&& nowAt < blockEndDateCvtFromIso.getTime() ) {
			return true;
		}

		return false;
	}

	public boolean shouldBlockNormalUser( SQLHandler handler, final String partyId, final String userClass ) throws SQLException {
		return shouldBlockNormalUserAt(handler, partyId, userClass, Calendar.getInstance().getTimeInMillis());
	}

	public boolean shouldBlockNormalUserAt( SQLHandler handler, final String partyId, final String userClass, long nowAt ) throws SQLException {
		boolean shouldBlock = false;

		if( !UserUser.USERCLASS_USER.equals(userClass) ) {
			return false;
		}

		Map<String, Object> record = getBlockInfoMap(handler, partyId);
		if( record != null ) {
			String blockStartIsoDate = (String)record.get("blockStartIsoDate");
			String blockEndIsoDate = (String)record.get("blockEndIsoDate");
			boolean loginBlock = isWithinBlockFrame(blockStartIsoDate, blockEndIsoDate, nowAt);

			String mtnceStartIsoDate = (String)record.get("mtnceStartIsoDate");
			String mtnceEndIsoDate = (String)record.get("mtnceEndIsoDate");
			boolean mtnceBlock = isWithinBlockFrame(mtnceStartIsoDate, mtnceEndIsoDate, nowAt);

			if( loginBlock || mtnceBlock ) {
				shouldBlock = true;
			}
		}

		return shouldBlock;
	}

	public Map<String, Object> determinePartyBlockAt( SQLHandler handler, final String partyId, long nowAt ) throws SQLException {
		Map<String, Object> ret = new HashMap<String, Object>();

		Map<String, Object> record = getBlockInfoMap(handler, partyId);
		if( record != null ) {
			String blockStartIsoDate = (String)record.get("blockStartIsoDate");
			String blockEndIsoDate = (String)record.get("blockEndIsoDate");
			boolean loginBlock = isWithinBlockFrame(blockStartIsoDate, blockEndIsoDate, nowAt);

			String mtnceStartIsoDate = (String)record.get("mtnceStartIsoDate");
			String mtnceEndIsoDate = (String)record.get("mtnceEndIsoDate");
			boolean mtnceBlock = isWithinBlockFrame(mtnceStartIsoDate, mtnceEndIsoDate, nowAt);

			ret.put("blockStartIsoDate", blockStartIsoDate);
			ret.put("blockEndIsoDate", blockEndIsoDate);
			ret.put("mtnceStartIsoDate", mtnceStartIsoDate);
			ret.put("mtnceEndIsoDate", mtnceEndIsoDate);
			ret.put("timestamp", nowAt);
			ret.put("isLoginBlock", loginBlock);
			ret.put("isMtnceBlock", mtnceBlock);
		}

		return ret;
	}

	public Map<String, Object> getBlockInfoMap( SQLHandler handler, final String partyId ) throws SQLException {
		UserParty party = new UserParty(handler);
		try {
			Map<String, Object> record = party.getRecord(new TreeMap<String, Object>() {
				{
					put("partyId", partyId);
				}
			}, new String[] {
					"partyName", "timeZone",
					"blockStartIsoDate", "blockEndIsoDate",
					"mtnceStartIsoDate", "mtnceEndIsoDate",
					"blockTemplate", "mtnceTemplate"
			});

			if( record != null ) {
				return record;
			} else {
				return new HashMap<String, Object>();
			}
		} finally {
			party = null;
		}
	}

	public List<Map<String, Object>> getBlockInfoList( SQLHandler handler ) throws SQLException {
		UserParty party = new UserParty(handler);
		try {
			List<Map<String, Object>> records = party.getRecords(new TreeMap<String, Object>() {
				{
					put("blockCondCount" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_NOTEQUALS);
					put("blockCondCount", 0);
				}
			}, new String[] {
					"partyId", "partyName", "timeZone",
					"blockStartIsoDate", "blockEndIsoDate",
					"mtnceStartIsoDate", "mtnceEndIsoDate",
					"blockTemplate", "mtnceTemplateTitle", "mtnceTemplate"
			});

			if( records != null ) {
				return records;
			} else {
				return new ArrayList<Map<String, Object>>();
			}
		} finally {
			party = null;
		}
	}

	private Date getZonedDate( Date date, TimeZone zone ) {
		Calendar cal = Calendar.getInstance(zone);
		cal.setTime(date);
		return cal.getTime();
	}

	public List<Map<String, Object>> getNoticeListAt( SQLHandler handler, TimeZone sessTimeZone, long nowAt ) throws SQLException {
		List<Map<String, Object>> list = new java.util.ArrayList<Map<String, Object>>();

		String sdfPattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(sdfPattern);
		List<Map<String, Object>> infoList = getBlockInfoList(handler);
		if( infoList != null ) {
			for( Map<String, Object> info : infoList ) {
				try {
					TimeZone zone = null;
					String zoneString = (String)info.get("timeZone");
					if( zoneString != null ) {
						zone = TimeZone.getTimeZone(zoneString);
					}
					if( zone == null )
						zone = sessTimeZone;
					sdf.setTimeZone(zone);

					if( info.get("blockStartIsoDate") != null ) {
						Date blockStartDate = DateTimeUtil.parseISODate((String)info.get("blockStartIsoDate"));
						Date blockEndDate = DateTimeUtil.parseISODate((String)info.get("blockEndIsoDate"));

						if( isWithinNoticeFrame(blockStartDate, blockEndDate, nowAt) ) {
							String contentTemplate = (String)info.get("blockTemplate");
							String defaultTemplate = null;
							try {
								defaultTemplate = handler.getMessageHandler().getMessageValue(DEFAULT_TEMPLATE_LOGIN_BLOCK_KEY);
							} catch( MissingResourceException missedKey ) {
								defaultTemplate = "{{partyName}} System Maintenance From {{blockStartIsoDate}} To {{blockEndIsoDate}} !!!";
							}
							if( contentTemplate == null || contentTemplate.length() == 0 ) {
								contentTemplate = defaultTemplate;
							}

							Map<String, Object> newinfo = new HashMap<String, Object>();
							newinfo.putAll(info);
							newinfo.put("blockTimeZone", sdf.getTimeZone());
							newinfo.put("blockStartDate", sdf.format(blockStartDate));
							newinfo.put("blockEndDate", sdf.format(blockEndDate));
							newinfo.put("blockStartIsoDate", sdf.format(blockStartDate));
							newinfo.put("blockEndIsoDate", sdf.format(blockEndDate) + " (" + sdf.getTimeZone().getID() + ")");

							final String content = StringUtil.evalPlaceholder(contentTemplate, newinfo, PLACE_PREFIX, PLACE_SUFFIX);
							Map<String, Object> noticeMap = new TreeMap<String, Object>();
							noticeMap.put("noticeStartDate", info.get("blockStartIsoDate"));
							noticeMap.put("noticeEndDate", info.get("blockEndIsoDate"));
							noticeMap.put("maintenanceStart", newinfo.get("blockStartDate"));
							noticeMap.put("maintenanceEnd", newinfo.get("blockEndDate"));
							noticeMap.put("maintenanceTimeZone", sdf.getTimeZone().getID());
							noticeMap.put("title", info.get("mtnceTemplateTitle"));
							noticeMap.put("content", content);
							noticeMap.put("partyId", info.get("partyId") );
							list.add(noticeMap);
						}
					}

					if( info.get("mtnceStartIsoDate") != null ) {
						Date mtnceStartDate = DateTimeUtil.parseISODate((String)info.get("mtnceStartIsoDate"));
						Date mtnceEndDate = DateTimeUtil.parseISODate((String)info.get("mtnceEndIsoDate"));

						if( isWithinNoticeFrame(mtnceStartDate, mtnceEndDate, nowAt) ) {
							String contentTemplate = (String)info.get("mtnceTemplate");
							String defaultTemplate = null;
							try {
								defaultTemplate = handler.getMessageHandler().getMessageValue(DEFAULT_TEMPLATE_MTNCE_BLOCK_KEY);
							} catch( MissingResourceException missedKey ) {
								defaultTemplate = "{{partyName}} System Maintenance From {{mtnceStartIsoDate}} To {{mtnceEndIsoDate}} !!!";
							}
							if( contentTemplate == null || contentTemplate.length() == 0 ) {
								contentTemplate = defaultTemplate;
							}

							Map<String, Object> newinfo = new HashMap<String, Object>();
							newinfo.putAll(info);
							newinfo.put("mtnceTimeZone", sdf.getTimeZone());
							newinfo.put("mtnceStartDate", sdf.format(mtnceStartDate));
							newinfo.put("mtnceEndDate", sdf.format(mtnceEndDate));
							newinfo.put("mtnceStartIsoDate", sdf.format(mtnceStartDate) + "(" + sdf.getTimeZone().getID() + ")");
							newinfo.put("mtnceEndIsoDate", sdf.format(mtnceEndDate) + "(" + sdf.getTimeZone().getID() + ")");

							final String content = StringUtil.evalPlaceholder(contentTemplate, newinfo, PLACE_PREFIX, PLACE_SUFFIX);
							Map<String, Object> noticeMap = new TreeMap<String, Object>();
							noticeMap.put("noticeStartDate", info.get("mtnceStartIsoDate"));
							noticeMap.put("noticeEndDate", info.get("mtnceEndIsoDate"));
							noticeMap.put("maintenanceStart", newinfo.get("mtnceStartDate"));
							noticeMap.put("maintenanceEnd", newinfo.get("mtnceEndDate"));
							noticeMap.put("maintenanceTimeZone", sdf.getTimeZone().getID());
							noticeMap.put("content", content);
							list.add(noticeMap);
						}
					}
				} catch( ParseException parseEx ) {
					logger.error(parseEx);
				}
			}
		}
		return list;
	}

}
