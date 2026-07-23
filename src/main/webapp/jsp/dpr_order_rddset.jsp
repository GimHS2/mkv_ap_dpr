<%--
	File Name:	dpr_order_rddset.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/06/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Object rddValues = pageContext.findAttribute( "rddValues" );

	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
%>
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<script type='text/javascript'>
		var rddOptions = new Array();

		<%
			Collection rddRecords = (List)pageContext.findAttribute( "rddValues" );
			int count;

			if( rddRecords != null && rddRecords.size() > 0 ) {
				count = 0;
				for( java.util.Iterator iterator = rddRecords.iterator(); iterator.hasNext(); ) {
					Map map = (Map)iterator.next();

					out.println( "rddOptions["+ count +"] = \""+ map.get("dateValue") +"\";" );
					count++;
				}
			}
		%>
		var windowObj = window.opener;
		if( !windowObj )
			windowObj = window.parent;

		if( windowObj.setPredefinedRDD )
			windowObj.setPredefinedRDD( rddOptions );

		var orderInd = "<mtl:value id="property" key="orderInd" />";
		windowObj.checkOrderInd( orderInd );

	</script>
</head>

<body></body>
</mtl:html>
