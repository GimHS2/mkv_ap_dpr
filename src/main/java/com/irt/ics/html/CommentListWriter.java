/*
 *	File Name:	CommentListWriter.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	kls1989		2014/02/28		2.2.	무기명 게시판 처리
 *	GimHS		2012/12/31		2.2.2	CrossBrowsing 적용: 'cursor: hand;' -> 'cursor: pointer;' 스타일 변경
 *	stghr12		2010/07/31		2.2.1	version up
 *	bvox		2009/11/02		2.2.0	create
 *
**/

package com.irt.ics.html;

import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.system.SessionManager;
import com.irt.util.MessageHandler;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class CommentListWriter {
	MessageHandler msghandler;
	SessionManager sessionMng;

	int replyLevelLimit;
	boolean hasEditAuth;
	boolean printingUnderLine;
	Collection records;

	String imageBasePath = "images/board";
	String type = "NAME";
	
	public CommentListWriter( MessageHandler msghandler, SessionManager sessionMng ) {
		this.msghandler = msghandler;
		this.sessionMng = sessionMng;

		this.replyLevelLimit = 1;
		this.hasEditAuth = false;
		this.printingUnderLine = false;
		this.records = null;
	}

	public CommentListWriter( HttpServletRequest request, HtmlPage htmlpage ) {
		this( htmlpage.getMessageHandler(), (SessionManager)request.getAttribute("sessionMng") );
		setRecords( (Collection)request.getAttribute("records") );
		if(request.getParameter("type")!=null)
			type = request.getParameter("type");
	}

	public static String[] getFieldKeys() {
		return new String[] {
			"commentNumber", "commentGroupLevel", "commentReplyCount", "content"
			, "registUserId", "registUserName", "registUserUserId", "status", "updateDateTime"
		};
	}

	public void print( java.io.Writer writer ) throws IOException {
		java.io.PrintWriter out;
		if( writer instanceof java.io.PrintWriter )
			out = (java.io.PrintWriter)writer;
		else
			out = new java.io.PrintWriter( writer );

		if( printingUnderLine )
			out.print("<table width='100%'><tr height='4'><td background='"+ imageBasePath +"/b_dot1.gif'></td></tr></table>");

		if( records == null || records.size() == 0 ) {
			if( hasEditAuth ) {
				out.println( "<div><table width='100%'><tr><td style='padding: 25px'>" );
				out.println( "<table width='100%' cellspacing='0' cellpadding='0' bgcolor='#f7f7f7'><tr><td style='padding: 15px'>" );
				out.println(
					"<textarea id='newContent' rows='5' wrap='physical' style='width: 85%'></textarea>&nbsp&nbsp&nbsp"
					+ "<img src='"+ imageBasePath +"/btn_comment.gif' onClick='JavaScript:updateComment(\"regist\", 0);' style='cursor: pointer;'>"
				);
				out.println( "</td></tr></table>" );
				out.println( "</td></tr></table></div>" );
			}
		} else {
			int row = 0;

			out.println( "<table width='100%' cellspacing='0' cellpadding='0'><tr><td width='100%' align='center' style='padding: 15px'>" );
			out.println( "<div><table bgcolor='#f7f7f7' width='100%' cellspacing='0' cellpadding='0' border='0'>" );
			out.println( "<tr><td>" );
			for( java.util.Iterator iterator = records.iterator(); iterator.hasNext(); row++ ) {
				Map recordMap = (Map)iterator.next();
				printDataLine( out, recordMap, row );
			}
			out.println( "</td></tr>" );
			if( hasEditAuth ) {
				out.println( "<tr><td class='comment'>" );
				out.println(
					"<textarea id='newContent' rows='5' wrap='physical' style='width: 85%'></textarea>&nbsp&nbsp&nbsp"
					+ "<img src='"+ imageBasePath +"/btn_comment.gif' onClick='JavaScript:updateComment(\"regist\", 0);' style='cursor: pointer;'>"
				);
				out.println( "</td></tr>" );
			}
			out.println( "</table></div>" );
			out.println( "</td></tr></table>" );
		}
	}

	public void printDataLine( java.io.Writer writer, Map recordMap, int row ) throws IOException {
		java.io.PrintWriter out;
		if( writer instanceof java.io.PrintWriter )
			out = (java.io.PrintWriter)writer;
		else
			out = new java.io.PrintWriter( writer );

		Number commentNumber = (Number)recordMap.get( "commentNumber" );
		Number commentGroupLevel = (Number)recordMap.get( "commentGroupLevel" );
		int replyLevel = ( commentGroupLevel == null ? 0 : commentGroupLevel.intValue() );

		if( "00".equals(recordMap.get("status")) && replyLevel <= replyLevelLimit ) {
			out.println( "<div id='commentArea_"+ commentNumber +"'>" );
			out.println( "<table width='100%'>" );
			out.println( "<tr>" );
			out.print( "<td class='comment' style='padding-left: "+ (replyLevel * 18 + 10) +"px;'>" );
			if( replyLevel > 0 )
				out.print( "<img src='"+ imageBasePath +"/ico_reply.gif'>&nbsp" );
			if(!type.equals("NONAME"))
				out.print( "&nbsp<b>"+ recordMap.get("registUserName") +" ("+ recordMap.get("registUserUserId") +")" );
			out.print( "</b>&nbsp&nbsp"+ recordMap.get("updateDateTime") );
			out.println( "</td>" );

			if( hasEditAuth ) {
				out.println( "<td align='right'><font color='#FC744A'>" );
				if( replyLevel < replyLevelLimit ) {
					out.print( "<a id='comm_fc_reply_"+ commentNumber +"' onClick='JavaScript:replyComment(this);' style='cursor: pointer'>" );
					out.print( "<span id='comm_fcmsg_reply_"+ commentNumber +"'>" );
					out.println( msghandler.getMessage("MSG_ICS_BOARD_COMMENT_FC_REPLY") +"</span></a>" );
					out.println( "<img src='"+ imageBasePath +"/ico_separate.gif' align='absmiddle'> ");
				}

				if( sessionMng != null && sessionMng.getUniqId().equals(recordMap.get("registUserId")) ) {
					out.print( "<a id='comm_fc_modify_"+ commentNumber +"' onClick = 'JavaScript:modifyComment(this);' style='cursor: pointer'>" );
					out.print( "<span id='comm_fcmsg_modify_"+ commentNumber +"'>" );
					out.println( msghandler.getMessage("MSG_ICS_BOARD_COMMENT_FC_MODIFY") +"</span></a>" );

					out.println( "<img src='"+ imageBasePath +"/ico_separate.gif' align='absmiddle'> ");
					out.println( "<a id='comm_fc_delete_"+ commentNumber +"' onClick='JavaScript:deleteComment(this);' style='cursor: pointer'>" );
					out.println( msghandler.getMessage("MSG_ICS_BOARD_COMMENT_FC_DELETE") +"</a>" );
				}
				out.println( "</font></td>" );
			}
			out.println( "</tr>" );

			out.print( "<tr><td class='commentcontent' colspan='2'>" );
			out.print( "<span id='comm_content_area_"+ commentNumber +"'>" );
			out.print( "<p id='comm_content_"+ commentNumber +"' style='display: inline; padding-left: "+ (replyLevel * 20) +"px;'>" );
			out.print( HtmlUtility.toHtmlString(recordMap.get("content")) +"</p>");
			out.print( "</span>" );
			out.println( "</td></tr>" );

			out.print( "<tr><td class='commentcontent align='center' colspan='2'>" );
			out.print( "<div id='comm_reply_area_"+ commentNumber +"'></div>");
			out.println( "</td></tr>" );

			out.println( "<tr height='1'><td background='"+ imageBasePath +"/b_dot3.gif' colspan='2'></td></tr>" );
			out.println( "</table></div>" );
		} else if( replyLevel < replyLevelLimit
				&& recordMap.get("commentReplyCount") != null && ((Number)recordMap.get("commentReplyCount")).intValue() > 0 ) {
			out.println( "<div id='commentArea_"+ commentNumber +"'><table width='100%'>" );
			out.println( "<tr height='50'><td align='center' colspan='2'>"+ msghandler.getMessage("MSG_ICS_BOARD_COMMENT_DELETED") +"</td></tr>" );
			out.println( "<tr height='1'><td background='"+ imageBasePath +"/b_dot3.gif'></td></tr>" );
			out.println( "</table></div>" );
		}
	}

	public void setEditCommentAuth( boolean hasEditAuth ) {
		this.hasEditAuth = hasEditAuth;
	}

	public void setImageBasePath( String imageBasePath ) {
		this.imageBasePath = imageBasePath;
	}

	public void setPrintingUnderLine( boolean printingUnderLine ) {
		this.printingUnderLine = printingUnderLine;
	}

	public void setRecords( Collection records ) {
		this.records = records;
	}

	public void setReplyLevelLimit( int replyLevelLimit ) {
		this.replyLevelLimit = replyLevelLimit;
	}
}
