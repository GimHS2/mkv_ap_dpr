<%--
	File Name:	pub_common_name.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2011/06/30		2.2.1	pageEncoding="euc-kr" Ãß°¡
	stghr12		2009/10/31		2.2.0	create
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
<head>
	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		var value = "";

		<%
			String fieldValue = property.getProperty( "field" );
			if( fieldValue == null ) {
				com.irt.sql.HierarchyCodeField codeField = (com.irt.sql.HierarchyCodeField)pageContext.findAttribute( "codeField" );
				if( codeField == null || codeField.getLastLevel() == 1 )
					fieldValue = "code=code, name=name";
				else {
					String nameClass = request.getParameter( com.irt.servlet.ServletModel.PARAM_SELECT_NAMECLASS );
					if( "L".equals(nameClass) )
						fieldValue = "code=, classCode=, lowerLevelCount=, levelCode=code, name=name";
					else
						fieldValue = "code=code, classCode=, lowerLevelCount=, levelCode=, name=name";
				}
			}

			String[] fields = fieldValue.split( "," );
			StringBuffer params = new StringBuffer();
			for( int f = 0; f < fields.length; f++ ) {
				String[] values = fields[f].split( "=" );
				if( f > 0 ) {
					out.println( "value += ';';" );
					params.append( ", " );
				}
				params.append( values.length < 2 || values[1].trim().length() == 0 ? "null" : "'"+ values[1].trim() +"'" );
		%>
				value += "<mtl:value id="record" key="<%= values[0].trim() %>" encodeScript="true"/>";
		<% } %>

		<% if( htmlpage.getMessage() != null ) { %>
			alert( "<%= HtmlUtility.toScriptString(htmlpage.getMessage()) %>" );
		<% } %>
		Select.setElementNames( <%= params.toString() %> );
		Select.setValue( new Array(value) );
	</script>
</head>

<body></body>
</mtl:html>
