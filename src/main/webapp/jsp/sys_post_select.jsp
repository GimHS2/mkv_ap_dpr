<%--
	File Name:	sys_post_select.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										pageindex style 변경 및 "Loading ..." message 처리
										listwriter.setMaximumRows(h) -> listwriter.setScrollHeight(h*24)
										windowResizeTo( 500 ); -> windowResizeTo( 600 );
										countryCode 추가, 오류수정
	stghr12		2006/12/01		2.1.0	com.irt.html.ListWriter, com.irt.html.ColumnListFactory 적용
	stghr12		2006/11/30		2.0.1	html내에서 encodeScript()하는 부분 제거
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/20		1.0.0	create
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
	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		var type;

		function bodyLoad() {
			windowResizeTo( 600 );
			if( !Select.selectName ) Select.setSelectName( "postCode" );

			if( frmMain.street.value ) {
				frmMain.street.select();
				frmMain.street.focus();
			} else if( frmMain.postalCode.value ) {
				frmMain.postalCode.select();
				frmMain.postalCode.focus();
			} else
				frmMain.street.focus();
			frmMain.street.focus();
		}

		function checkInput() {
			if( type == "street" ) {
				if( !Field.checkMandatory(frmMain.street) ) return false;
				frmMain.postalCode.value = "";
			} else if( type == "postalCode" ) {
				if( !Field.checkMandatory(frmMain.postalCode) ) return false;
				frmMain.code.value = "";
				frmMain.street.value = "";

				var strs = frmMain.postalCode.value.split( "-" );
				for( var i = 0; i < strs.length; i++ )
					frmMain.code.value += strs[i];
			} else
				return false;

			return true;
		}

		function setType( value ) {
			type = value;
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" method="get" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<input type='hidden' name='mode' value='sel'>
		<input type='hidden' name='code' value=''>
		<mtl:hidden id='request' key='slname'/>
		<mtl:hidden id='request' key='countryCode'/>

		<table class='info_content' id='content_condition' cellspacing='0' cellpadding='0'>
		<tr>
			<td rowspan='2' class='subject0' width='30%'><span id='title_street'><mtl:message key="jsp.sys_post_select.STREET"/></span>
					<mtl:message key="jsp.sys_post_select.SEARCH"/></td>
			<td class='content0'>
				<input type='text' name='street' value='<mtl:value id="property" key="street"/>' class='length_20'
						onFocus='setType("street");'/>
				<input type='image' src='images/tbtn_search.gif' onFocus='JavaScript:setType("street");' class='tbtn'>
			</td>
		</tr>
		<tr><td class='content0'><span class='content_info'><mtl:message key="jsp.sys_post_select.MSG_ENTER_STREET"/></span></td></tr>
		<tr>
			<td rowspan='2' class='subject0' width='30%'><span id='title_street'><mtl:message key="jsp.sys_post_select.POSTALCODE"/></span>
					<mtl:message key="jsp.sys_post_select.SEARCH"/></td>
			<td class='content0'>
				<input type='text' name='postalCode' value='<mtl:value id="property" key="postalCode"/>' class='length_10'
						onFocus='setType("postalCode");'/>
				<input type='image' src='images/tbtn_search.gif' onFocus='JavaScript:setType("postalCode");' class='tbtn'>
			</td>
		</tr>
		<tr><td class='content0'><span class='content_info'><mtl:message key="jsp.sys_post_select.MSG_ENTER_POSTALCODE"/></span></td></tr>
		</table>

		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'></span>
			</td></tr>
			</table>

			<%
				ColumnListFactory factory = new ColumnListFactory( "columnList" );

				String pattern = "(${postalCode}) ${state} ${city} ${street} ${address} "
						+ "${addressNumberFrom,${~ :addressNumberTo}; ~ ${addressNumberTo}}";
				factory.appendColumn( "name", "&nbsp;", "class='description'", pattern );
				factory.setPrimaryFieldKeys( new String[] { "code", "state", "city", "street", "address" } );

				ListWriter listwriter = new com.irt.custom.SelectListWriter( request, htmlpage, factory.getColumnList() ) {
					public void printHeaderLine( JspWriter out ) throws java.io.IOException {
						out.println( "<tr class='header_one'><th colspan='2'>"+ msghandler.getMessage("jsp.sys_post_select.ADDRESS") +"</th></tr>" );
					}
				};
				listwriter.setScrollHeight( 240 );
				listwriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="select" styleClass="btn_list"/>
				<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' id='index_lst' align='center' width='100%' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>
		</mtl:contentGroup>
	</mtl:form>
</body>
</mtl:html>
