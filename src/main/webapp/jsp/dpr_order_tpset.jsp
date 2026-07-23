<%--
	File Name:	dpr_order_tpset.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/03/31		2.2.1	CrossBrowsing Àû¿ë
	guksm		2008/09/26		2.2.0	create
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
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<script type='text/javascript'>
		var soldOptions = new Array();
		var shipOptions = new Array();

		<%
			String type = property.getProperty( "type" );

			com.irt.data.format.RecordFormat codePattern = com.irt.data.format.PatternRecordFormat.getInstance( "$f{pure(linkPartyCode)}" );
			Collection soldRecords = (List)pageContext.findAttribute( "soldParties" );
			Collection shipRecords = (List)pageContext.findAttribute( "shipParties" );
			int count;

			if( soldRecords != null && soldRecords.size() > 0 ) {
				count = 0;
				for( java.util.Iterator iterator = soldRecords.iterator(); iterator.hasNext(); ) {
					Map map = (Map)iterator.next();

					out.println( "soldOptions["+ count +"] = new Array();" );
					out.println( "soldOptions["+ count +"][0] = \""+ map.get("linkPartyCode") +"\";" );
					out.print( "soldOptions["+ count +"][1] = \"["+ HtmlUtility.toScriptString(codePattern.format(map, msghandler)) +"] " );
					out.println( HtmlUtility.toScriptString(map.get("linkPartyName")) +"\";" );

					count++;
				}
			} else if( shipRecords != null && shipRecords.size() > 0 ) {
				count = 0;
				for( java.util.Iterator iterator = shipRecords.iterator(); iterator.hasNext(); ) {
					Map map = (Map)iterator.next();

					out.println( "shipOptions["+ count +"] = new Array();" );
					out.println( "shipOptions["+ count +"][0] = \""+ map.get("linkPartyCode") +"\";" );
					out.print( "shipOptions["+ count +"][1] = \"["+ HtmlUtility.toScriptString(codePattern.format(map, msghandler)) +"] " );
					out.println( HtmlUtility.toScriptString(map.get("linkPartyName")) +"\";" );

					count++;
				}
			}
		%>
		var windowObj = window.opener;
		if( !windowObj )
			windowObj = window.parent;

		if( windowObj.setTradePartners )
			windowObj.setTradePartners( "<%= type %>", soldOptions, shipOptions );
	</script>
</head>

<body></body>
</mtl:html>
