<%--
	File Name:	dpr_rddmng_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/06/28		2.2.0	create.
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.List, java.util.Map" %>
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
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "vtype", "<mtl:value id="property" key="vtype"/>" );

			<mtl:ifvalue id="property" key="vtype" value="TRG">
				requestDelete( url, frmMain.listcheckbox, "trgKey,organizationCode,distributionChannelCode,officeCode" );
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="vtype" value="IND">
				requestDelete( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,partyCode,shipPartyCode" );
			</mtl:ifvalue>
		}

		function downloadReq() {
			customPopup.confirm( { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" }, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					url = replaceQueryValue( url, "vtype", "<mtl:value id="property" key="vtype"/>" );
					url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
					windowOpen( url );
				}
			});
		}

		function modifyReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=imod&wintype=sub";
			url = replaceQueryValue( url, "vtype", "<mtl:value id="property" key="vtype"/>" );
			<mtl:ifvalue id="property" key="vtype" value="TRG">
				requestModify( url, frmMain.listcheckbox, "trgKey,organizationCode,distributionChannelCode,officeCode" );
			</mtl:ifvalue>
			<mtl:ifvalue id="property" key="vtype" value="IND">
				requestModify( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,partyCode,shipPartyCode" );
			</mtl:ifvalue>

		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>")
			url = replaceQueryValue( url, "vtype", "<mtl:value id="property" key="vtype"/>" );
			windowOpen( url, "sub-content" );
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRRddMng?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "vtype", "<mtl:value id="property" key="vtype"/>" );
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:hidden id="property" key="vtype" />
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"SO\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_PARTY_SALESOFFICE_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
									listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"/>
						</div>
					</div>
					<div class='cell'></div>
				</div>
			</div>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				function checkSearchCond() {
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRRddMng?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:hidden id="property" key="vtype" />
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage ) {
					@Override
					public void printDataCell( JspWriter out, Column column, Map recordMap, int row, int col ) throws java.io.IOException {
						String fieldKey = column.getFieldKey();
						if( "dayOfWeek".equals(fieldKey) ) {
							String title = column.getColumnHelp( recordMap, msghandler );
							String attr = (String)column.getColumnAttr();
							String htmlStr = "<td" + ( attr != null ? " " + attr : "" );
							out.print( htmlStr );
							if( title == null )
								out.print( ">" );
							else
								out.print( " title='"+ title +"'>" );

							String dayOfWeeks = getColumnValue( column, recordMap, row, col );
							String value = "";
							if( dayOfWeeks != null && dayOfWeeks.length() > 0 ) {
								for( String dayOfWeek : dayOfWeeks.split(",") ) {
									value += ", " + msghandler.getMessage( "DPR_RDD_MNG_DAYOFWEEK_SHORT_" + dayOfWeek );
								}
							}
							if( value.indexOf(",") > -1 ) {
								value = value.substring( 2 );
							}
							out.print( value );
							out.println( "</td>" );

							return;
						} else if( "allowDays".equals(fieldKey) ) {
							String title = column.getColumnHelp( recordMap, msghandler );
							String attr = (String)column.getColumnAttr();
							String htmlStr = "<td" + ( attr != null ? " " + attr : "" );
							out.print( htmlStr );
							if( title == null )
								out.print( ">" );
							else
								out.print( " title='"+ title +"'>" );

							String value = getColumnValue( column, recordMap, row, col );
							value = value.replace( ",", ", " );
							out.print( value );
							out.println( "</td>" );

							return;
						}
						super.printDataCell( out, column, recordMap, row, col );
					}
				};

				listWriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<% if( listWriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
					<mtl:button type="upload"/>
				<% } %>
				<% if( listWriter.containsData() ) { %>
					<mtl:button type="download"/>
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
