<%--
	File Name:	dpr_office_cond.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	song7981	2016/05/20		2.2.0	create
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
			<mtl:select id="condition" key="officeCode"
					nullValueKey="MSG_COND_SALES_OFFICE" hasBlank="true"
					listId="offices" listCodeKey="code"
					listNameFormat="$S{[:code;$S{] :name}}"/>
		</span>

	</mtl:form>
</body>
</mtl:html>
