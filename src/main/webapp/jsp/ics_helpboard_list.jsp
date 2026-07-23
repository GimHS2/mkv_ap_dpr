<%--
	File Name:	ics_helpboard_list.jsp
	Version:	2.2.0c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/02/28		2.2.0c	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	String boardClassCode = property.getProperty( "boardClassCode" );
	String type = property.getProperty( "type" );
%>

<head>
	<%@ include file="include_ics_board_header.inc" %>
	<script type='text/javascript'>
		$(function() {
			syncFrameHeight();

			if( parent.$("iframe.main-content") ) {
				parent.$(".menu-content").contents().find( ".frame-content").innerHeight( $(".frame-content").innerHeight() );
			}
		});

		function syncFrameHeight() {
			if( $("div.list_content .list-function, div.frame-content .search-bottom").height() > 36 ) {
				$("div.list_content .list-function button, div.frame-content .search-bottom button").css( "margin-bottom", "10px" );
				$("div.list_content, div.frame-content").css( "padding-bottom", "15px" );
			}

			if( parent.$("iframe.menu-content") ) {
				var height = $(".frame-content-wrap").height();
				parent.$("iframe.menu-content").contents().find( ".frame-content").innerHeight( height );
				parent.$("div.content").height( height + 30 );
			}
		}

		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>";
			requestDelete( url, frmMain.listcheckbox, "boardNumber" );
		}

		function infoReq( boardNumber ) {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>&type=<%=type%>";
			requestInfo( url, frmMain.listcheckbox, "boardNumber", boardNumber, "main_content" );
		}

		function completedPostReq() {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>&type=list";
			url = getRequestMultiURL( url, "completed", frmMain.listcheckbox, "boardNumber" );

			if( !url ) return;

			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );

			customPopup.confirm( { "detail" : "<mtl:message key="MSG_ICS_HELPBOARD_CONFIRM_COMPLETED" encodeScript="true"/>" }, function(res) {
				if( res ) {
					if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
						submitPost( url );
					} else {
						if( parent.$("body.frame-content").length ) {
							toggleLoadingParent( true );
						} else {
							toggleLoading( true );
						}
						location.replace( url );
					}
				}
			});
		}
	</script>
</head>

<body class='content' style='padding-right: 8px; overflow: hidden;'>
	<div class='frame-content-wrap'>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_ics_board_bodyheader.inc" %>

		<script type='text/javascript'>
			$( function() {
				$("input[type=text].date").blur( function() {
					setTimeout( function() {
						Field.checkDateRange( frmCond.startDate, frmCond.endDate );
					}, 500 );
				});
			});

			function checkSearchCond() {
				if( !Field.checkDateRange(frmCond.startDate, frmCond.endDate) ) return false;
				disableBlankInput( frmCond, true );

				return submitInput();
			}

			function showAllSearchCond() {
				frmCond.headwordCode.value = "";
				frmCond.registUserUserId.value = "";
				frmCond.startDate.value = "";
				frmCond.endDate.value = "";
				frmCond.submit();
			}
		</script>

		<%@ include file="include_pub_input.inc" %>
		<%@ include file="include_pub_calendar.inc" %>

		<mtl:contentGroup groupId="condition" type="search" styleClass="frame-content">
			<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
				<mtl:contentGroup groupId="condition" type="search" styleClass="none-card">
					<%@ include file="include_rbm_listcond.inc" %>
					<div id='messagebar'></div>
					<div class='search-table'>
						<div class='row'>
							<div class='cell w120' style='padding-bottom: 15px;'>
								<div class='field-title'><mtl:message key="jsp.ics_helpboard_list.FIELD_MESSAGE_TYPE"/></div>
							</div>
							<div class='cell' style='padding-bottom: 15px;'>
								<div class="field-info">
									<mtl:select id="condition" key="headwordCode" condition="<%= \"boardClassCode=\"+ property.getProperty(\"boardClassCode\") %>"
											className="com.irt.ics.BoardHeadword" listCodeKey="headwordCode" listNameFormat="$H{ICS_HELP_BOARD_HEADWORD_@headwordName}"
											hasBlank="true" nullValueKey="jsp.ics_boardlist.VIEW_ALL" searchable="false"/>
								</div>
							</div>
							<div class='cell w120 align-right' style='padding-bottom: 15px;'>
								<div class='field-title'><mtl:message key="jsp.ics_helpboard_list.FIELD_SENDERID"/></div>
							</div>
							<div class='cell' style='padding-bottom: 15px;'>
								<div class='field-info'><mtl:text id="condition" key="registUserUserId"/></div>
							</div>
						</div>
					</div>
					<div class='search-table'>
						<div class='row'>
							<div class='cell w120' style='padding-bottom: 15px;'>
								<div class='field-title'><mtl:message key="jsp.ics_helpboard_list.FIELD_DATE"/></div>
							</div>
							<div class='cell' style='padding-bottom: 15px;'>
								<div class='field-info'>
									<mtl:date id="condition" key="startDate" style="width: calc(50% - 10px)"/> ~ <mtl:date id="condition" key="endDate" style="width: calc(50% - 10px)"/>
								</div>
							</div>
						</div>
					</div>
					<div class='search-bottom'>
						<div class='table-cell search-button'>
							<mtl:button type="reset" styleClass="seccondary-w135"/>
							<mtl:button type="search" styleClass="primary-w135"/>
						</div>
					</div>
				</mtl:contentGroup>
			</mtl:form>

			<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
				<mtl:hidden id="property" key="type"/>
				<mtl:contentGroup groupId="list" type="list" styleClass="none-card" style="min-height: 450px;">
					<div class='list-menu'>
						<mtl:select id="condition" key="completedInd" prefixKey="ICS_HELP_BOARD_COMPLETED_IND_" codeValues="Y,N"
								hasBlank="true" nullValueKey="jsp.ics_boardlist.VIEW_ALL"
								modified="JavaScript:listLink(this);" searchable="false" width="auto"/>
						<mtl:select id="condition" uniqId="headwordCode2" key="headwordCode" condition="<%= \"boardClassCode=\"+ property.getProperty(\"boardClassCode\") %>"
								className="com.irt.ics.BoardHeadword" listCodeKey="headwordCode" listNameFormat="$H{ICS_HELP_BOARD_HEADWORD_@headwordName}"
								hasBlank="true" nullValueKey="MSG_PUB_SELECT@jsp.ics_helpboard_list.FIELD_MESSAGE_TYPE"
								modified="JavaScript:listLink(this);" searchable="false" width="auto"/>
					</div>

					<%
						ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );

						if( sessionMng.isAuthorized("ICS", "ICSHelpBoard."+ boardClassCode +".DEL") )
							listwriter.setCheckboxType( ListWriter.CHECKBOXTYPE_CHECK );
						listwriter.setNumbering( false );
						listwriter.setImageBasePath( "images/board" );
						listwriter.setScrollHeight( 405 );
						listwriter.print( out );
					%>

					<div class='list-function'>
						<div class='button'>
							<mtl:button type="return"/>
						<% if( sessionMng.isAuthorized("ICS", "ICSHelpBoard."+ boardClassCode +".DEL") && listwriter.containsData() ) { %>
							<mtl:button type="board_completed" onClick="JavaScript:completedPostReq();" messageKey="jsp.BTN_COMPLETED"/>
							<mtl:button type="delete"/>
						<% } %>
							<mtl:button type="close_if"/>
						</div>
						<div id='list_page' class='page'>
							<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
						</div>
					</div>
				</mtl:contentGroup>
			</mtl:form>
		</mtl:contentGroup>
	</div>
</body>
</mtl:html>
