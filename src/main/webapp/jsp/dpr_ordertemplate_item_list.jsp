<%--
	File Name:	dpr_order_template_itemp_list.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/09/29		2.2.2	UOM이 PC일 경우 MAX LIMIT 수량 제한 없앰.
	jbaek		2013/01/30		2.2.1	PIPO 기능 개발
	lsinji		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">


<%
	java.util.Map conditionMap = (java.util.Map)pageContext.findAttribute( "condition" );
	String templateKey = (String)conditionMap.get( "templateKey" );
%>

<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function bodyLoad(){
			windowResizeTo( 800 );
			resetForm( frmMain );
			focusForm( frmMain, frmMain.title );
		}

		function registReq( templateKey ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountry?mode=ireg";
			if( countryCode )
				url += "&countryCode=" + encodeURIComponent(countryCode);

			windowOpen( url +"&wintype=sub", "clsMng" );
		}

		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=del";
			url = replaceQueryValue( url, "templateKey", "<mtl:value id="condition" key="templateKey"/>" );

			requestDelete( url, frmMain.listcheckbox, "lineNumber" );
		}

		function linkMenuReq( templateKey ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_TEMPLATE_ITEM_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("INFO");' );
				menu[1] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_TEMPLATE_ITEM_MODIFY" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("MODIFY");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}


		function linkMenuReqClick( menu ) {
			if( menu == "INFO" )
				infoReq( linkmenu.params[0] );
			else if( menu == "MODIFY" )
				modifyReq( linkmenu.params[0] );
		}

	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>
			<mtl:contentGroup groupId="info" type="content" descriptionKey="jsp.GRP_DPR_TEMPLATE_INFO">
				<table class='line_content' cellspacing='0' cellpadding='0' >
					<td class='subject' ><mtl:message key="FIELD_DPR_TEMPLATE_NAME"/>
					<td class='content3'><mtl:value id="templateInfo" key="templateName"/>
					<td class='subject' align='center'><mtl:message key="jsp.dpr_ordertemplate.FIELD_DPR_TEMPLATE_REGISTERED"/>
					<td class='content3'><mtl:value id="templateInfo" key="createDateTime"/>
					<td class='subject'>
					<td class='content3'>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
					<td class='subject'><mtl:message key="FIELD_DPR_TEMPLATE_USERID"/>
					<td class='content3'><mtl:valuef id="templateInfo" key="manageUserId"/>
					<td class='subject'><mtl:message key="jsp.dpr_ordertemplate.FIELD_DPR_TEMPLATE_DISTRIBUTOR"/>
					<td class='content3'><mtl:valuef id="templateInfo" key="viewPartyCode"/>
					<td class='subject'><mtl:message key="jsp.dpr_ordertemplate.FIELD_DPR_TEMPLATE_PUBLIC"/>
					<td class='content3'><mtl:value id="templateInfo" key="publicInd"/>
				</table>

			</mtl:contentGroup>

		<mtl:form name="frmMain" onSubmit="JavaScript: return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='mode' value='mmod'/>
			<mtl:hidden id="request" key="templateKey"/>
			<input type='hidden' name='URL' value="<%= htmlpage.getRequestURL() %>" />

			<mtl:contentGroup groupId="list" type="list" styleClass="list_content none-card">
				<%
					com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage ) {
						public String getColumnValue( Column column, Map recordMap, int row, int col ) {
						String fieldKey = column.getFieldKey();
							if( "orderQty".equals(fieldKey) ) {
								StringBuffer sbuf = new StringBuffer();
								if( recordMap.containsKey("itemCode") ) {
									sbuf.append( "\n<input type='hidden' name='value_itemCode'" )
										.append( " value='"+ HtmlUtility.toScriptString(recordMap.get("itemCode")) +"'/>" );
								}
								if( recordMap.containsKey("itemCodeConfirmed") ) {
									sbuf.append( "\n<input type='hidden' name='value_itemCodeConfirmed'" )
										.append( " value='"+ HtmlUtility.toScriptString(recordMap.get("itemCodeConfirmed")) +"'/>" );
								}
								if( recordMap.containsKey("childLineNumber") ) {
									sbuf.append( "\n<input type='hidden' name='value_childLineNumber'" )
										.append( " value='"+ HtmlUtility.toScriptString(recordMap.get("childLineNumber")) +"'/>" );
								}
								if( recordMap.containsKey("itemRefInd") ) {
									sbuf.append( "\n<input type='hidden' name='value_itemRefInd'" )
										.append( " value='"+ HtmlUtility.toScriptString(recordMap.get("itemRefInd")) +"'/>" );
								}
								if( recordMap.containsKey("lineNumber") ) {
									sbuf.append( "\n<input type='hidden' name='value_lineNumber'" )
										.append( " value='"+ HtmlUtility.toScriptString(recordMap.get("lineNumber")) +"'/>" );
								}
								if( recordMap.containsKey("uom") ) {
									sbuf.append( "\n<input type='hidden' name='value_uom'" )
										.append( " value='"+ HtmlUtility.toScriptString(recordMap.get("uom")) +"'/>" );
								}
								sbuf.append( "\n<input type='text' name='value_orderQty' class='input-field' maxlength='10'" )
								.append( " value='"+ HtmlUtility.toScriptString(recordMap.get("orderQty")) +"'/>" );

								sbuf.append( "\n<input type='hidden' name='value_templateKey' maxlength='10'" )
									.append( " value='"+ HtmlUtility.toScriptString(request.getParameter("templateKey")) +"'" );
								return sbuf.toString();
							} else
								return column.format( recordMap, msghandler );
						}
					};
					listwriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="return"/>
					<% if( htmlpage.hasManageAuth() && listwriter.containsData() ) { %>
						<mtl:button type="submit"/>
						<mtl:button type="delete"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
					<div id='list_page' class='page'>
						<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					if( !frmMain.listcheckbox ) return false;
					var obj = frmMain.listcheckbox;
					frmMain.url.value = getLocationURL();

					var title_orderQty = "<mtl:message key="FIELD_DPR_TEMPLATEDTL_ORDERQTY" encodeScript="true"/>";

					if( Field.isArray(frmMain.listcheckbox) ) {
						for( var i = 0; i < frmMain.listcheckbox.length; i++ ) {
							if( !Field.checkNumberFormat(frmMain.value_orderQty[i], true, false, title_orderQty) ) return false;
							else if( !Field.checkNumberRange(frmMain.value_orderQty[i], false, 0.00, 10000.00, 0x03, title_orderQty) ) return false;

							frmMain.value_orderQty.disabled = false;
						}
					}
					else{
						if( !Field.checkNumberFormat(frmMain.value_orderQty[i], true, false, title_orderQty) ) return false;
						else if( !Field.checkNumberRange(frmMain.value_orderQty[i], false, 0.00, 10000.00, 0x03, title_orderQty) ) return false;

						frmMain.value_orderQty.disabled = false;
					}

					if( Field.isArray(obj) ) {
						for(var i = 0; i < obj.length; i++ ){
							if( frmMain.value_childLineNumber[i].value != 0 ) continue;
							if( frmMain.value_uom[i].value != "PC" && frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %>) {
								customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
								frmMain.value_orderQty[i].select();
								frmMain.value_orderQty[i].focus();
								return false;
							}
						}
					}
					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
