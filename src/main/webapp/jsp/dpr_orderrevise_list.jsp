<%--
	File Name:	dpr_orderrevise_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/06/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.Map, java.util.List, java.lang.Integer " %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>

<%
	String boardClassCode = "HD."+htmlpage.getProperty().getProperty("savedOrgCd");
%>
	<script type='text/javascript'>
		var linkmenu = null;

		function completedPostReq() {
			var url = "<%= systemConfig.getClassURL() %>/ICSHelpBoard?boardClassCode=<%= boardClassCode %>";

			url = getRequestMultiURL( url, "completed", frmMain.listcheckbox, "parentOrderNumber,boardNumber,reviseChangeIndex,reviseStatus" );

			if( !url ) return;

			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			url = replaceQueryValue( url, "type", "ordrevise" );

			customPopup.confirm( { "detail" : "<mtl:message key="MSG_DPR_ORDREV_CONFIRM_COMPLETED" encodeScript="true"/>" }, function(res) {
				if( res ) {
					windowOpen( url );
				}
			});
		}

		function downloadReq( type ) {
			customPopup.confirm( { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" }, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					url = replaceQueryValue( url, "fileType", "XLX" );
					windowOpen( url );
				}
			});
		}

		function toArray( obj ) {
			return ( obj ? (Field.isArray(obj) ? obj : new Array(obj)) : new Array() );
		}

		function bodyLoad() {
			var prevKey;
			var obj_reviseStatus = toArray(frmMain.listcheckbox_reviseStatus);
			$(frmMain.listcheckbox).each(function(i,o) {
				if( String(o.value) == String(prevKey) ) {
					o.disabled = true;
				} else {
					if( Field.getValue(obj_reviseStatus[i]) == "CQ" )
						o.disabled = false;
					else {
						o.disabled = true;
					}
					prevKey = o.value;
				}
			});
		}


	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div id='messagebar'></div>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="soldPartyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:text id="condition" key="soldPartyCode"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="revOrderNumber" descriptionKey="FIELD_DPR_ORDER_ORDERNUMBER"/></div>
						<div class='field'><mtl:text id="condition" key="compositeOrderNumber" name="revOrderNumber"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="reviseHelpType" descriptionKey="FIELD_DPR_ORDREV_HELPTYPE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="reviseHelpType" prefixKey="FIELD_DPR_ORDREV_HELPTYPE_" codeValues="ROM,ROD"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_REVISEHELPTYPE"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="reviseStatus" descriptionKey="FIELD_DPR_ORDREV_REVSTATUS"/></div>
						<div class='field'>
							<mtl:select id="condition" key="reviseStatus" prefixKey="FIELD_DPR_ORDREV_REVSTATUS_" codeValues="CQ,CP"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_REVISESTATUS"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="startRevBaseDate" descriptionKey="jsp.FIELD_DPR_ORDER_REVISE_STARTDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="startRevBaseDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="endRevBaseDate" descriptionKey="jsp.FIELD_DPR_ORDER_REVISE_ENDDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="endRevBaseDate"/></div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
			</div>
			<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				$( function() {
					$("input[type=text].date").blur( function() {
						setTimeout( function() {
							if( Field.checkMultiMandatory([frmCond.startRevBaseDate, frmCond.endRevBaseDate], false) ) {
								Field.checkDateRange( frmCond.startRevBaseDate, frmCond.endRevBaseDate );
							}
						}, 500 );
					});
				});

				function checkSearchCond() {
					if( !Field.checkMultiMandatory([frmCond.startRevBaseDate, frmCond.endRevBaseDate], false) ) return false;
					if( !Field.checkDateRange(frmCond.startRevBaseDate, frmCond.endRevBaseDate) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage ) {

					public String getColumnValue( Column column, java.util.Map recordMap, int row, int col ) {
						String fieldKey = column.getFieldKey();
						if( "reviseHbrdContent".equals(fieldKey) ) {
							return com.irt.dpr.OrderRevise.getSaferHelpBoardContent(recordMap);
						} else {
							return column.format( recordMap, msghandler );
						}
					}

				};

				if( htmlpage.hasManageAuth() )
					listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_CHECK );
				else
					listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NONE );

				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="download"/>
					<% if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="button" onClick="JavaScript: completedPostReq();" messageKey="jsp.BTN_COMPLETE"/>
					<% } %>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>

		</mtl:contentGroup>
		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
