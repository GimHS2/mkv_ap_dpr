<%--
	File Name:	dpr_party_cond.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/04/30		2.2.1	frmCond가 아니어도 사용 가능하도록 수정.
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
	Object offices = pageContext.findAttribute( "offices" );
	Object groups = pageContext.findAttribute( "groups" );
%>
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<script type='text/javascript' src='script/main.js'></script>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			<% if( offices != null ) { %>
				setInnerHTML( parent.document.all.search_office, document.all.search_office.innerHTML );
			<% } %>

			<% if( groups != null ) { %>
				setInnerHTML( parent.document.all.search_officeGroup, document.all.search_officeGroup.innerHTML );
			<% } %>

			if( parent.frmCond ) {
				var condArray = new Array();
				if( parent.frmCond.officeCode )
					condArray.push( parent.frmCond.officeCode );
				if( parent.frmCond.groupCode )
					condArray.push( parent.frmCond.groupCode );

				initConditionField( condArray, Field.Modified );
			}
		}

		function setInnerHTML( contentObj, html ) {
			if( contentObj ) contentObj.innerHTML = html;
		}
	</script>
</head>

<body onload="JavaScript:bodyLoad();">
	<mtl:form name="frmCond">
		<%@ include file="include_rbm_listcond.inc" %>
		<span id='search_office'>
			<mtl:select id="condition" key="officeCode" nullValueKey="MSG_COND_SALES_OFFICE"
					hasBlank="true" listId="offices" listCodeKey="code"
					listNameFormat="$S{[:code;$S{] :name}}"
					modified="JavaScript:Field.modified(this);changeGroupList(this.value);"/>
		</span>

		<span id='search_officeGroup'>
			<mtl:select id="condition" key="groupCode" nullValueKey="MSG_COND_SALES_GROUP"
					hasBlank="true" listId="groups" listCodeKey="code"
					listNameFormat="$S{[:code;$S{] :name}}"/>
		</span>

	</mtl:form>
</body>
</mtl:html>
