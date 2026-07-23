<%--
	File Name:	dpr_item_tree.jsp
	Version:	2.0.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	guksm		2008/09/26		2.0.0	create
--%>
	<%
		String cateCode = null;

		if( recordList != null && recordList.size() > 0 ) {
			out.println( "<ul id='tree-checkmenu' class='tree_content'>" );

			int count = 0;
			for( Map recordMap : recordList ) {
				if( cateCode == null || !cateCode.equals(recordMap.get("cateCode")) ) {
					if( cateCode != null )
						out.println( "</ul></li>" );

					cateCode = (String)recordMap.get( "cateCode" );
					out.println( "<li id='show-"+ cateCode +"'>" );
/*
					if( !"itm".equals(type) )
						out.println( "<input type='checkbox' id='check-"+ cateCode +"'>" );
*/
					out.println( recordMap.get("cateName") );
					out.println( "<span id='count-"+ cateCode +"' class='count'></span>" );
					out.println( "<ul id='tree-"+ cateCode +"'>" );
				}

				if( "itm".equals(type) ) {
					StringBuffer sbuf = new StringBuffer();
					sbuf.append( "<li>" )
						.append( "<input type='radio' name='checkItem' id='treeindex_"+ String.valueOf(count) +"'" )
						.append( " value='"+ recordMap.get("itemCode") +"' onClick='JavaScript:selectItem(this.value);'/>" )
						.append( "<label for='treeindex_"+ String.valueOf(count) +"'>" )
						.append( recordMap.get("itemName") )
						.append( "</label>" )
						.append( "</li>" );

					out.println( sbuf.toString() );
				} else {
					StringBuffer sbuf = new StringBuffer();
					sbuf.append( "<li>" )
						.append( "<input type='checkbox' name='checkItem' id='treeindex_"+ String.valueOf(count) +"'" )
						.append( " value='"+ recordMap.get("itemCode") +"'/>" )
						.append( "<label for='treeindex_"+ String.valueOf(count) +"'>" )
						.append( "("+ recordMap.get("itemCode") +")"+ recordMap.get("itemName") )
						.append( "</label>" )
						.append( "</li>" );

					out.println( sbuf.toString() );
				}
				count++;
			}

			out.println( "</ul></li>" );
			out.println( "</ul>" );
		}
	%>
