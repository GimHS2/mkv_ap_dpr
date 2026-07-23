<%--
	File Name:	pub_list_count.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
										listwriter.setSummaryMap() 추가
	stghr12		2006/12/01		2.1.0	summary 처리
										include_rbm_header.inc 제거
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
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
	<script type='text/javascript'>
		function bodyLoad() {
			var windowObj = window.parent;
			if( windowObj ) {
				if( windowObj.document.all.list_page ) windowObj.document.all.list_page.innerHTML = document.all._list_page_.innerHTML;

				var det = windowObj.document.all.summary_data;
				var src = document.all.summary_data;
				if( src && det ) {
					if( src.length ) {
						for( var i = 0; i < src.length; i++ )
							det[i].innerHTML = src[i].innerHTML;
					} else
						det.innerHTML = src.innerHTML;
				}
			}
		}
	</script>
</head>

<body onLoad='JavaScript:bodyLoad();'>
	<%@ include file="include_pub_index.inc" %>
	<%
		Object summaryColumnList = pageContext.findAttribute( "summaryColumnList" );
		if( summaryColumnList != null ) {
			ListWriter listwriter = null;

			if( summaryColumnList instanceof com.irt.data.cols.ColumnList )
				listwriter = new com.irt.custom.ListWriter( request, htmlpage, (com.irt.data.cols.ColumnList)summaryColumnList );
			else if( summaryColumnList instanceof com.irt.data.cols.ColumnList[] )
				listwriter = new com.irt.custom.ListWriter( request, htmlpage, ((com.irt.data.cols.ColumnList[])summaryColumnList)[0] );
			if( listwriter != null ) {
				out.println( "<table cellspacing='0' cellpadding='0'>" );
				listwriter.setSummaryMap( (java.util.Map)pageContext.findAttribute("summary") );
				listwriter.printSummaryLine( out );
				out.println( "</table>" );
			}
		}
	%>

<% if( pageContext.findAttribute("queryStorage") != null ) { %>
	<span id='_debug_query' style='display: none'><%@ include file="include_pub_query.inc" %></span>

	<script type='text/javascript'>
		if( parent && parent.DebugQuery && parent.DebugQuery.append )
			parent.DebugQuery.append( document.all._debug_query.innerHTML, "COUNT" );
	</script>
<% } %>
</body>
</mtl:html>
