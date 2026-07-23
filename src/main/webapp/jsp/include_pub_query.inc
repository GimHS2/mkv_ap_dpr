<%--
	File Name:	include_pub_query.inc
	Version:	2.2.0

	Description:
		<body>¿¡ ?ß°¡

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.0	쿼리내용 toHtmlString 으로 변환하여 출력하도록 수정
	stghr12		2007/04/30		2.1.0	version up(not changed)
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2005/11/11		1.0.0	create
--%>

<% {
	com.irt.sql.QueryStorage storage = (com.irt.sql.QueryStorage)pageContext.findAttribute( "queryStorage" );

	if( storage != null && storage.getSavedQuerySize() > 0 ) out.println( "<pre>" );
	for( int q = 0; storage != null && q < storage.getSavedQuerySize(); q++ ) {
		com.irt.sql.QueryBuffer query = storage.getSavedQueryBuffer(q);
		String methods[] = storage.getSavedQueryCallerTrace(q);
		long elapsedTime = storage.getSavedQueryElapsedTime(q);

		if( methods != null && methods.length > 1 )
			out.println( "<div align='center'><b>Query["+ q +"]: "+ HtmlUtility.toHtmlString(methods[1]) +"</b></div>" );
		else
			out.println( "<div align='center'><b>Query["+ q +"]</b></div>" );
		out.println( "<hr>" );
		out.println( elapsedTime + " ms" );
		out.println( "<hr>" );

		if( methods != null ) {
			for( int m = 1; m < 6 && m < methods.length; m++ )
				out.println( HtmlUtility.toHtmlString(methods[m]) );
			out.println( "<hr>" );
		}

		if( query instanceof com.irt.sql.ConditionQueryBuffer ) {
			com.irt.sql.ConditionQueryBuffer condquery = (com.irt.sql.ConditionQueryBuffer)query;
			if( condquery.getConditionMap() != null ) {
				out.print( HtmlUtility.toHtmlString(condquery.getConditionMap().toString()) );
				out.println( "<hr>" );
			}
		}
		
		java.io.StringWriter writer = new java.io.StringWriter();
		com.irt.sql.QueryUtility.printQuery( new java.io.PrintWriter(writer), query.getQuery()
			, query.getBindVariables() );
		
		out.println( HtmlUtility.toHtmlString(writer.toString()) );

		out.println( "<hr>" );
		out.println( "<p>" );
		out.println( "" );
	}
	if( storage != null && storage.getSavedQuerySize() > 0 ) out.println( "</pre>" );
} %>
