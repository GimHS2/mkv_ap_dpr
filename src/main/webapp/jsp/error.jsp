<%--
	File Name:	error.jsp
	Version:	2.2.2c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2024/08/28		2.2.2c	에러 라인이 긴 경우 접혀서 보이도록 적용, data-error-type 표시
	hankalam	2021/11/30		2.2.2c	신규 UI/UX 적용
	hankalam	2020/07/31		2.2.2	타이틀 스타일 적용
	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
										table.full_size -> width='100%'
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
	stghr12		2007/04/30		2.1.1	windowType "inner" 처리
	stghr12		2006/12/01		2.1.0	ex_errors 추가, sytle 변경
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/02/29		1.0.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>
<%
	com.irt.html.HtmlPage htmlpage = (com.irt.html.HtmlPage)pageContext.findAttribute( "htmlpage" );
	com.irt.servlet.SystemConfig systemConfig = (com.irt.servlet.SystemConfig)pageContext.findAttribute( "systemConfig" );
	java.util.Collection ex_errors = (java.util.Collection)pageContext.findAttribute( "ex_errors" );

	if( systemConfig == null || htmlpage == null )
		response.sendError( HttpServletResponse.SC_NOT_FOUND );
	else {
%>

<html>
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= systemConfig.getSystemName() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
	<% {
		String[] styleSheetNames = htmlpage.getStyleSheetNames();
		for( int i = 0; i < styleSheetNames.length; i++ )
			out.println( "<link rel='stylesheet' href='style/"+ styleSheetNames[i] +"'/>" );
	} %>
	<link rel='stylesheet' href='style/menu_portal.css'/>
	<link rel='stylesheet' href='style/dpr_common.css'/>
	<script type='text/javascript' src='script/jquery.min.js'></script>
	<style type='text/css'><!--
		li {
			border-bottom: 1px dotted #BBBBBB;
			margin-bottom: 4px; padding-top: 5px; padding-bottom: 0px;
		}
		td.error {
			font-size: 12px; font-weight: bold; color: #B90000;
			background: #F0F0F0 url(images/error_bg.gif) fixed no-repeat left bottom;
			min-height: 130px;
		}
		ul {
			vertical-align: middle;
			margin-left: 80px; margin-right: 20px; padding-top: 5px;
		}
	//--></style>
	<script type='text/javascript'>
		$(function() {
			$(".content-overlay").css( "display", "flex" );
			$(".popup").show();

			if( parent.$(".content-overlay .loading").length && parent.$(".content-overlay .loading").is(":visible") ) {
				toggleLoadingParent( false );
			}
		});

		function bodyLoad() {
			<% if( "sub".equals(htmlpage.getWindowType()) ) { %>
				window.resizeTo( 600, document.body.scrollHeight );
				window.resizeBy( 0, document.body.scrollHeight - document.body.clientHeight );
			<% } %>

			self.focus();
		}

		function windowClose( reload ) {
			if( self.opener && !self.opener.closed ) {
				if( reload ) {
					if( window.name == "sub-content" ) {
						toggleLoadingParent( true );
					}
					self.opener.location.reload();
				}
				self.opener.focus();
			}
			if( window.name == "sub-content" ) {
				if( !reload ) {
					parent.$(".content-overlay").fadeOut();
				}
				parent.$(".sub-content-wrap").hide();
				parent.$(".sub-content-wrap").css( "width", "" );
				//parent.$("#sub-content").attr( "src", "<%= systemConfig.getProperty("baseURL") %>blank.html" );
			} else {
				self.close();
			}
		}

		function toggleLoadingParent( visible, message ) {
			if( parent.$(".content-overlay").length ) {
				if( visible ) {
					if( !message ) {
						message = "<mtl:message key="jsp.MSG_LOADING"/>"
					}
					parent.$(".content-overlay .loading").show();
					parent.$(".content-overlay .loading .msg").empty().append( message );
					parent.$(".content-overlay").fadeIn( 150 ).css( "display", "flex" );
				} else {
					parent.$(".content-overlay .loading").hide();
					parent.$(".content-overlay").fadeOut();
				}
			}
		}
	</script>
</head>

<body class='content' data-error-type='isError'>
	<form name='frmMain'>
		<div class='content-overlay'>
			<mtl:contains id="queryStorage">
				<div style='display: none'>
					<span id='_debug_query'>
						<%
							Throwable throwable = (Throwable)pageContext.findAttribute( "throwable" );
							if( throwable != null ) {
								out.print( "<b>Exception:</b><hr><pre>" );
								throwable.printStackTrace( new java.io.PrintWriter(out) );
								out.print( "</pre><hr><br><br>" );
							}
						%>
						<%@ include file="include_pub_query.inc" %>
					</span>
					<a href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>query.html?name=_debug_query'
							target='winDebug'><img src='images/lbtn_sql.gif'></a>
				</div>
			</mtl:contains>
			<div class='popup alert'>
				<div class='header-msg w100p' style='position: relative;'>
					<img src='images/error.png' style='margin-right: 30px;'><span style='position: relative; font-size: 30px; top: -20px;'><mtl:message key="jsp.error.TITLE"/></span>
				</div>
				<style>
					.error-msgs li {
						white-space: normal; /* Allow wrapping to show full text initially */
						overflow: visible; /* Show all text initially */
						max-width: none; /* No width restriction initially */
						transition: all 0.3s; /* Smooth transition for expanding and collapsing */
					}
					.error-msgs .collapsed {
						white-space: nowrap; /* Prevent text from wrapping */
						overflow: hidden; /* Hide overflowed text */
						text-overflow: ellipsis; /* Show ellipsis for overflowed text */
						max-width: 200px; /* Adjust width as needed */
					}
				</style>
				<div class='msg error-msgs'>
					<%
						if( htmlpage.getMessage() != null )
							out.println( "<li>"+ HtmlUtility.toHtmlString(htmlpage.getMessage()) +"</li>" );
						if( ex_errors != null ) {
							for( java.util.Iterator iterator = ex_errors.iterator(); iterator.hasNext(); ) {
								com.irt.data.DataException dataEx = (com.irt.data.DataException)iterator.next();

								out.print( "<li>" );
								if( dataEx.getLineNumber() >= 0 )
									out.print( "Line "+ dataEx.getLineNumber() +" : " );
								out.print( HtmlUtility.toHtmlString(dataEx.getMessage()) );
								out.println( "</li>" );
							}
						}
					%>

					<mtl:loop id="errors" loopId="loop" loopIndex="index">
						<li class='error-line'><mtl:value id="loop" key="name"/> -&gt; <mtl:value id="loop" key="message"/></li>
					</mtl:loop>
				</div>
				<span style='float: right'>
				<% if( htmlpage.getBackURL() != null ) { %>
					<mtl:button type="return" styleClass='secondary'/>
				<% } else if( !"inner".equals(htmlpage.getWindowType()) ) { %>
					<mtl:button type="return" onClick="JavaScript:history.go(-1);"/>
				<% } %>
					<mtl:button type="close_if"/>
				</span>
				<script>
					$(document).ready(function() {
						// Apply the collapsed class to items with long text.
						$('li.error-line').each(function() {
							if ($(this).text().length > 2000) {
								$(this).addClass('collapsed').addClass('expandable');
							}
						});
						$("li.error-line.expandable").click(function() {
							$(this).toggleClass('collapsed');
						});
					});
				</script>
			</div>
		</div>

	</form>
</body>
</html>
<% } %>
