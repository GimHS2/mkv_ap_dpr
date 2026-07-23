<%--
	File Name:	dpr_country_condition_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2012/07/30		2.2.1	최소 발주 가능 금액 설정 기능 추가(multiUpdate)
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
<%
	java.util.Map<String, Object> condition = (java.util.Map<String, Object>)pageContext.findAttribute( "condition" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<script type='text/javascript'>
		var windowWidth = 1000;

		$(function() {
			var options = $.extend( {}, selectmenuOptions, { width: "auto", position: { my : "left bottom", at: "left top" } } );
			$("select[name=filterType]").singleSelectmenu( options );
		});

		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=del";
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );

			requestDelete( url, frmMain.listcheckbox, "organizationCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>";
			url = getRequestMultiURL( url, "reg", frmMain.listcheckbox, "organizationCode" );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );

			if( !checkURLLength(url) ) return false;

			windowSelfOpen( url, getLocationURL() );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" onSubmit="JavaScript: return checkInput();">
			<input type='hidden' name='mode' value='mmod'>
			<%@ include file="include_rbm_form.inc" %>

			<mtl:contentGroup groupId="list" type="list" styleClass="list_content none-card">
				<div class='list-menu'>
					<mtl:select id="condition" key="conditionInd" prefixKey="jsp.DPR_COUNTRY_COND_STATUS_" codeValues="00,99"
							modified="listLink(this);" width="auto" searchable="false"/>
				</div>
				<%
					ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage ) {

						public String getColumnValue( com.irt.data.cols.Column column, java.util.Map recordMap, int row, int col ) {
							String fieldKey = column.getFieldKey();
							StringBuffer sbuf = new StringBuffer();

							String st = HtmlUtility.toHtmlString( recordMap.get("conditionInd"));
							if( "minOrderTotal".equals(fieldKey) && "00".equals(st) ) {
								String value = HtmlUtility.toHtmlString( recordMap.get(fieldKey));
								return  "\n<input type='text' name='value_"+ fieldKey + "' value='"+ value + "' style='text-align: right' size='9' />";
							}

							if( "conditionInd".equals(column.getFieldKey()) ) {
								String status = (String)recordMap.get( column.getFieldKey() );
								String message = msghandler.getMessage( "jsp.DPR_COUNTRY_COND_STATUS_" + status );

								return (message != null ? message : super.getColumnValue(column, recordMap, row, col) );
							}
							return super.getColumnValue( column, recordMap, row, col );
						}

						public void printDataLine( JspWriter out, java.util.Map recordMap, int row ) throws java.io.IOException {
							out.println( "<input type='hidden' name='value_countryCode' value='"+ recordMap.get("countryCode") +"'>" );
							out.println( "<input type='hidden' name='value_organizationCode' value='"+ recordMap.get("organizationCode") +"'>" );
							out.println( "<input type='hidden' name='value_conditionInd' value='"+ recordMap.get("conditionInd") +"'>" );
							super.printDataLine( out, recordMap, row );
						}
					};
					listWriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="return"/>
					<% if( htmlpage.hasManageAuth() ) { %>
						<% if( listWriter.containsData() && "00".equals((String)condition.get("conditionInd")) ) { %>
							<mtl:button type="delete"/>
						<% } else if( listWriter.containsData() && "99".equals((String)condition.get("conditionInd")) ) { %>
							<mtl:button type="regist"/>
						<% } %>
						<mtl:button type="save"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
					<div id='list_page' class='page'>
						<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
					</div>
				</div>
				<div class='list-function'>
					<div class='button'>
						<select name='filterType'>
							<option value='organizationCode'><mtl:message key="FIELD_DPR_SALESORGANIZATION_CODE"/></option>
							<option value='organizationName'><mtl:message key="FIELD_DPR_SALES_ORGANIZATION_NAME"/></option>
						</select>
						<input type='text' name='filterValue' class='input-field' style='width: 200px' onKeyDown='JavaScript:callByKeydown(filterReq);'>
						<mtl:button type="search" styleClass="btn btn-secondary" icon="images/ico_search.png" onClick="JavaScript:filterReq();"/>
					</div>
				</div>
			</mtl:contentGroup>
			<script type='text/javascript'>
				function checkInput() {
					frmMain.url.value = getLocationURL();
					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>

