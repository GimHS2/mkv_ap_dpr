<%--
	File Name:	dpr_party_select.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/04/30		2.2.1	partyName -> customerName 으로 변경
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										filterValueInput() -> callByKeydown(filterReq)
										listwriter.setMaximumRows(h) -> listwriter.setScrollHeight(h*24)
										listwriter.setPrintingHeader() 사용
	stghr12		2006/12/01		2.1.0	com.irt.html.ListWriter, com.irt.html.ColumnListFactory 적용
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/03		1.0.0	create
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
	java.util.Map conditionMap = (java.util.Map)pageContext.findAttribute( "condition" );
	boolean useDivision = (com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;division") == null);
	boolean useDistributionChannel = (com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;distributionChannel") == null);
	boolean readOnly = com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(sessionMng.getGroupClass());
	String organizationCode = (String)conditionMap.get("organizationCode");
%>

<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );
			Select.setElementNames( "code", "name" );
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
					<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
					<span id='list_showcount'></span>
			</td></tr>
			</table>

			<%
				ListWriter listwriter = new com.irt.custom.SelectListWriter( request, htmlpage );
				listwriter.setScrollHeight( 288 );
				listwriter.setPrimaryKeys( new String[] { "partyCode", "customerName" } );
				listwriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<mtl:button type="select" styleClass="btn_list"/>
			<% if( "chk".equals(request.getParameter("attr")) ) { %>
				<mtl:button type="reset" href="JavaScript:Select.reset(frmMain.listcheckbox);" styleClass="btn_list"/>
			<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>

			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' id='index_lst' align='center' width='100%' style="margin-left:30px;" nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
				<td class='list_content_bottom' id='index_btn' align='right' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>
		</mtl:contentGroup>
	</mtl:form>
</body>
</mtl:html>
