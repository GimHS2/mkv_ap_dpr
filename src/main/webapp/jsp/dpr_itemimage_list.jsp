<%--
	File Name:	dpr_partylink_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
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
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteReq( countryCode, itemCode ) {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "countryCode,itemCode", countryCode, itemCode );
		}

		function linkMenuReq( countryCode, itemCode ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ITEMIMAGE_REGIST" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("REG");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "REG" )
				registReq( linkmenu.params[0], linkmenu.params[1] );
		}

		function registReq( countryCode, itemCode ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg";
			var query;
			if( countryCode && itemCode ) {
				url = replaceQueryValue( url, "countryCode", countryCode );
				url = replaceQueryValue( url, "itemCode", itemCode );

				windowOpen( url, "sub-content" );
			} else {
				var query = getRequestMultiURL( url, "ireg", frmMain.listcheckbox, "countryCode,itemCode" );
				if( query )
					windowOpen( query, "sub-content" );
			}
		}

		function selectLink( obj ) {
			if( !Field.checkMandatory(obj, true) ) return;

			listLink( obj );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div id='messagebar'></div>
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
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:message key="jsp.dpr_itemimage_list.MSG_IMAGE_REGISTER"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="imageRegistered" prefixKey="jsp.dpr_itemimage_list.MSG_IMAGE_REGISTER_"
								codeValues="Y,N" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_IMAGE_REGISTER"
								hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" searchable="false"/>
						</div>
					</div>
					<div class='cell'></div>
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
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;

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
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				if( property.containsKey("listmsg") )
					listwriter.print( out, property.getProperty("listmsg") );
				else
					listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<mtl:button type="delete"/>
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
