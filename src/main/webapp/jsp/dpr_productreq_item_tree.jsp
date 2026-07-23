<%--
	File Name:	dpr_productreq_item_tree.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.1	displayItemName XSS 취약성 수정
	hankalam	2017/12/29		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*,java.util.List,java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>
<%
	String requestType = request.getParameter( "rtype" );
%>

<mtl:html errorPage="error.jsp">
<head>
<%@ include file="include_rbm_header.inc" %>
<%@ include file="include_dpr_itemtree.inc" %>
<script type="text/javascript">
	function bodyLoad() {

		setInnerHTML( parent.document.all.tree, document.all.tree.innerHTML );
		checkmenu = new CheckTree( 'checkmenu' );
		checkmenu.init();
	}

	function setInnerHTML( contentObj, html ) {
		if( contentObj ) contentObj.innerHTML = html;
	}
</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>

	<span id="tree">
	<mtl:form name="frmTree" target="content" onSubmit="JavaScript: return false;">
		<mtl:hidden id="request" key="rtype"/>
		<mtl:hidden id="request" key="organizationCode"/>
		<mtl:hidden id="request" key="distributionChannelCode"/>
		<mtl:hidden id="request" key="partyCode" name="partyCode"/>
		<mtl:hidden id="request" key="shipPartyCode" name="shipPartyCode"/>
		<mtl:hidden id="request" key="orderKey"/>
		<input type="hidden" name="mode"/>
		<input type="hidden" name="url"/>
		<input type="hidden" name="locale"/>
		<input type="hidden" name="menu"/>
		<input type="hidden" name="wintype"/>

		<%
			List<Map> recordList = (List)pageContext.findAttribute( "records" );
			String type = property.getProperty( "type" );
			List organizations = (List)pageContext.findAttribute( "organizations" );
		%>
		<ul id='content_cond_search_item' class='search_item'>
			<div style='margin: 0; padding: 0;'>
				<li><div><mtl:message key="FIELD_DPR_ITEM_MASTER_CODE"/></div>
					<mtl:text id="request" key="itemCode" styleClass="length_15"/>
					<a href='JavaScript:treeReq(frmTree.itemCode);'><img src='images/tbtn_search_frame.gif' align='absmiddle'></a>
				</li>
				<li><div><mtl:message key="FIELD_DPR_ITEM_MASTER_NAME"/></div>
					<mtl:text id="request" key="itemName" styleClass="length_15"/>
					<a href='JavaScript:treeReq(frmTree.itemName);'><img src='images/tbtn_search_frame.gif' align='absmiddle'></a>
				</li>
				<li><div><mtl:message key="jsp.dpr_orderitem_tree.FIELD_UPC_CODE"/></div>
					<mtl:text id="request" key="itemConsumerEANCode" styleClass="length_15"/>
					<a href='JavaScript:treeReq(frmTree.itemConsumerEANCode);'><img src='images/tbtn_search_frame.gif' align='absmiddle'></a>
				</li>
			</div>
		</ul>
		<pre id='msg'><mtl:contains id="property" key="treeMsg"><%= property.getProperty("treeMsg") %></mtl:contains></pre>

	<ul id='content_list_itemtree'>
	<% if( recordList != null && recordList.size() > 0 && !"quick".equals(requestType) ) { %>
	<%

		String cateCode = null;
		String oldCateCode = null;

		String defaultCateCode = null;
		String parentCateCode = null;
		String oldParentCateCode = null;

		String childCateCode = null;
		String oldChildCateCode = null;

		String cateName = null;
		String childCateName = null;
		String parentCateName = null;

		int classCode = 0;
		int oldClassCode = 0;
		int defaultClassCode = 0;
		int parentClassCode = 0;
		int childClassCode = 0;

		int count = 0;
		int childCateCount = 0;
		int oldChildCateCount = 0;
		int parentCateCount = 0;

		if( recordList != null && recordList.size() > 0 ) {
			out.println( "<ul id='tree-checkmenu' class='tree_content'>" );

			for( Map recordMap : recordList ) {
				defaultCateCode = (String)recordMap.get( "defaultCateCode" );
				cateCode = (String)recordMap.get( "cateCode" );
				String defaultClassCodeStr = (String)recordMap.get( "defaultClassCode" );
				defaultClassCode = Integer.valueOf( defaultClassCodeStr );
				java.math.BigDecimal classCodeBD = (java.math.BigDecimal)recordMap.get( "currClassCode" );
				classCode = Integer.valueOf( classCodeBD.intValue() );

				if( classCode < 1 )
					classCode = 1;
				parentClassCode = classCode -1;
				if( parentClassCode < 1 )
					parentClassCode = 1;
				childClassCode = classCode +1;
				if( childClassCode > 5 )
					childClassCode = 6;

				cateCode = (String)recordMap.get("cateCode");
				cateName = (String)recordMap.get("N"+classCode);
				childCateCode = (String)recordMap.get("C"+childClassCode);
				childCateName = (String)recordMap.get("N"+childClassCode);

				parentCateCode = (String)recordMap.get("C"+parentClassCode);
				parentCateName = (String)recordMap.get("N"+parentClassCode);

				if( cateCode == null || cateCode.length() == 0 ) continue;

				if( count == 0 || (!cateCode.equals(oldCateCode)) ) {// new cateCode
					if( !childCateCode.equals( oldChildCateCode ) ) {
						if( oldCateCode != null ) {
							out.println( "</ul></li>" );

							if( childCateCode == null || childCateCode.length() == 0 ) continue;

							if( childCateCode != null && defaultClassCode == oldClassCode )
								out.println( "</ul></li>" );
						}
					}

					if( defaultClassCode == classCode ) {
						out.println( "<li id='show-"+ cateCode + "' title='"+ "default-lv1-"+ cateCode +"'>" );

						if( cateName != null )
							out.println( cateName );
						else
							out.println( cateCode + " - Products" );
						out.println( "<span id='count-"+ cateCode +"' class='count'></span>" );
						out.println( "<ul id='tree-"+ cateCode +"'>" );

						out.println( "<li id='show-"+ childCateCode +"' title='"+ "default-lv2-"+ childCateCode +"'>" );

						if( childCateName != null )
							out.println( childCateName );
						else
							out.println( childCateCode + " - Products" );
						out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );
						out.println( "<ul id='tree-"+ childCateCode +"'>" );
					} else { // hierarchy condition
						out.println( "<li id='show-"+ childCateCode + "' title='"+ "cond-"+ childCateCode +"'>" );
						out.println( parentCateName + " - " );
						if( childCateName != null )
							out.println( childCateName );
						else
							out.println( childCateCode + " - Products" );
						out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );

						out.println( "<ul id='tree-"+ childCateCode +"'>" );
					}
				} else { //new childCateCode
					if( !childCateCode.equals( oldChildCateCode ) ) {

						if( defaultClassCode == classCode ) {
							if( cateCode.equals(oldCateCode) ) {
								if( childCateCode != null )
									out.println( "</ul></li>" );
								out.println( "<li id='show-"+ childCateCode +"' title='default-lvl2-"+ childCateCode +"'>" );

								if( childCateName != null )
									out.println( childCateName );
								else
									out.println( childCateCode + " - Products" );
								out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );
								out.println( "<ul id='tree-"+ childCateCode +"'>" );
							}
						} else { // hierarchy condition
							if( childCateCode != null )
								out.println( "</ul></li>" );
							out.println( "<li id='show-"+ childCateCode +"' title='cond-"+ childCateCode +"'>" );
							out.println( parentCateName + " - " );

							if( childCateName != null )
								out.println( childCateName );
							else
								out.println( childCateCode + " - Products" );
							out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );
							out.println( "<ul id='tree-"+ childCateCode +"'>" );
						}
					}
				}

				com.irt.data.format.RecordFormat codePattern = com.irt.data.format.PatternRecordFormat.getInstance( "$f{pure(itemCode)}");
				String displayItemName = ( recordMap.get("itemName") == null ? "" : (String)recordMap.get("itemName") );
				String newItemInd = (String)recordMap.get("newItemInd");
				String promotionItemInd = (String)recordMap.get("promotionItemInd");
				String existingOrderInd = (String)recordMap.get("existingOrderInd");
				StringBuffer sbuf = new StringBuffer();

				sbuf.append( "<li class='normal'>" );
				sbuf.append( "<div class='ico_info'>" );
				if( "Y".equals(newItemInd) )
					sbuf.append( "<img src='images/ico_newitem.gif' class='ico_newitem'/>" );
				if( "Y".equals(promotionItemInd) )
					sbuf.append( "<img src='images/ico_promo.gif' class='ico_promotion'/>" );
				sbuf.append( "</div>" );

				sbuf.append( "<input type='checkbox' name='checkItems' id='treeindex_"+ String.valueOf(count) +"'" )
				.append( " value='"+ recordMap.get("itemCode") + ";" + recordMap.get("itemConsumerEANCode") +"' onClick='JavaScript: selectItem(this, \"" 
					+ com.irt.html.HtmlUtility.toHtmlString(displayItemName) + "\");'/>" );

				if( "Y".equals(existingOrderInd) )
					sbuf.append( "<span style='background:#CCC'>" );

				sbuf.append( "<label for='treeindex_"+ String.valueOf(count) +"'>" )
				.append( "("+ codePattern.format(recordMap, msghandler) +")"+ displayItemName )
				.append( "</label>" );

				if( "Y".equals(existingOrderInd) )
					sbuf.append( "</span>" );

				sbuf.append( "</li>" );

				out.println( sbuf.toString() );

				count++;
				oldCateCode = cateCode;
				oldChildCateCode = childCateCode;
				oldClassCode = classCode;
			}

			if( count > 0 )
				out.println( "</ul></li>" );


			out.println( "</ul>" );
		}


	}%>
	</ul>
	</mtl:form>
	</span>
</body>
</mtl:html>
