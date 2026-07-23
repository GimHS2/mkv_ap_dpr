<%--
	File Name:	usr_usersession_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.1	DPR Page tail (legal notice) add
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										pageindex style 변경 및 "Loading ..." message 처리
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
										com.irt.html.ListWriter 적용
										linkmenu생성을 bodyLoad()에서 linkMenu()로 이동. LMENU_ 메시지 변경
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2005/01/11		1.0.0	create
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
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function linkMenuReq( partyId, uniqId ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_USR_USER_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("UINFO");' );

				linkmenu = createLinkMenu( menu );
			}

			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "UINFO" ) {
				var url = "<%= systemConfig.getClassURL() %>/USRUser?mode=info&wintype=sub";
				url = replaceQueryValue( url, "uniqid", linkmenu.params[0] );
				url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
				windowSelfOpen( url );
			}
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
