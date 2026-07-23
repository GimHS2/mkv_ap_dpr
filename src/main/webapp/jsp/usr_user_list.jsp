<%--
	File Name:	usr_user_list.jsp
	Version:	2.2.5

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/11/29		2.2.5	multi-sold-to 기능 추가
	jbaek		2015/04/30		2.2.4	LinkMenu가 1개일 경우 해당 기능으로 바로 이동.
	jbaek		2014/03/31		2.2.3	Cross Browsing 적용
	lsinji		2008/09/26		2.2.2	DPR Page tail (legal notice) add
										partnerAuthListReq() 추가
	stghr12		2008/03/31		2.2.1	table.list_content_top: absolute외 width 추가
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										pageindex style 변경 및 "Loading ..." message 처리
										filterValueInput() -> callByKeydown(filterReq)
										거래처별권한 Link 추가
										FIELD_USR_USERID -> FIELD_USERID
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
										com.irt.html.ListWriter 적용
										linkmenu생성을 bodyLoad()에서 linkMenu()로 이동. LMENU_ 메시지 변경
										사용하지 않는 변수 삭제
										groupId 선택 추가
	stghr12		2006/10/14		2.0.1	'var linkmenu;' 추가
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/21		1.0.0	create
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
	boolean multiSoldTo = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", (sessionMng.getPartyId()+";multiSoldTo"), "JNJAP_CN".equals(sessionMng.getPartyId()) );
	boolean usingTPAuth = com.irt.rbm.RBMSystem.getSystemEnvBool( "USR", "User;UsingTPAuth", false );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;
		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "partyId", getQueryValue(location.href, "partyId") );
			requestDelete( url, CheckBox.getQueryValue(frmMain.listcheckbox, null, "userId") );
		}

		function infoReq( uniqId ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=info";
			requestOne( url, frmMain.listcheckbox, "uniqId,null", uniqId, "_self" );
		}

		function linkMenuReq( uniqId, partyId, userId ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_USR_USER_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("UINFO");' );
				<% if( usingTPAuth && sessionMng.isAuthorized("ECS", "ECSTPAuth.LST.TP") ) { %>
					menu[1] = new Array( '<mtl:message key="jsp.LMENU_ECS_TPAUTH_LIST" encodeScript="true"/>'
							, 'self', 'JavaScript:linkMenuReqClick("TPAUTH");' );
				<% } %>

				linkmenu = createLinkMenu( menu );
			}

			var linkDefaultMenu = "UINFO";
			if( linkmenu.menuListArray[0].length == 1 ) {
				linkmenu.params = linkMenuReq.arguments;
				linkMenuReqClick(linkDefaultMenu);
			} else {
				linkmenu.show();
				linkmenu.params = linkMenuReq.arguments;
			}
		}

		function linkMenuReqClick( menu ) {
			if( menu == "UINFO" )
				infoReq( linkmenu.params[0] );
			else if( menu == "TPAUTH" )
				partnerAuthListReq( linkmenu.params[0], linkmenu.params[1], linkmenu.params[2] );
		}

		function modifyReq( uniqId ) {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "uniqId,null", uniqId, "_self" );
		}

		function partnerAuthListReq( uniqId, partyId, userId ) {
			var requestPartyId = "<%= sessionMng.getPartyId() %>";
			var requestUserId = "<%= sessionMng.getUserId() %>";
			var requestUniqId = "<%= sessionMng.getUniqId() %>";

			if( typeof uniqId == "undefined" || !uniqId ) {
				var selected = CheckBox.getValues( frmMain.listcheckbox );
				if( selected && selected[0] ) {
					uniqId = selected[0].split(";")[0];
				}
			}

			if( (requestUniqId == uniqId) || (requestPartyId == partyId && requestUserId == userId) ) {
				customPopup.alert( { "header" : "<mtl:message key="ERR_WRONG_SELECT" encodeScript="true"/>" } );
				return;
			}

			requestOne( "<%= systemConfig.getClassURL() %>/DPRPartyAuth?mode=list", frmMain.listcheckbox, "uniqId,userId"
					, null, "_self" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "partyId", getQueryValue(location.href, "partyId") );
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			windowSelfOpen( url );
		}

		function downloadReq(  ) {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "uniqId,userId" );
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/USRUser?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}
	</script>

	<style type='text/css'>
		@media only screen and (max-width: 1200px) {
			.description { width: 80px; }
		}
	</style>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:hidden key="ptype" defaultValue="excl" />
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div id='messagebar'></div>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="userId" descriptionKey="FIELD_USERID"/></div>
						<div class='field'><mtl:text id="condition" key="userId"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="userName" descriptionKey="FIELD_USR_USERNAME"/></div>
						<div class='field'><mtl:text id="condition" key="userName"/></div>
					</div>
				<% if( multiSoldTo ) { %>
					<div class='cell'>
						<div class='field-title'><mtl:title key="soldPartyCodes" descriptionKey="MSG_FIELD_DPR_SOLD_PARTYCODE"/></div>
						<div class='field'><mtl:text id="condition" key="soldPartyCodes"/></div>
					</div>
					<div class='cell'>
					</div>
				<% } else { %>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="groupId" descriptionKey="FIELD_USR_GROUPNAME"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="groupId" listId="groups" listCodeKey="groupId" listNameFormat="$H{groupName}"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									searchable="false"/>
						</div>
					</div>
					<div class='cell'>
					<% if( htmlpage.hasManageAuth() ) { %>
						<div class='field-title'><mtl:title key="status" descriptionKey="FIELD_STATUS"/></div>
						<div class='field'>
							<mtl:select id="condition" key="status" prefixKey="USR_USER_STATUS_" codeValues="00,PW,LK,99"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_STATUS"
									searchable="false"/>
						</div>
					<% } %>
					</div>
				<% } %>
				</div>
			<% if( multiSoldTo ) { %>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="groupId" descriptionKey="FIELD_USR_GROUPNAME"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="groupId" listId="groups" listCodeKey="groupId" listNameFormat="$H{groupName}"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									searchable="false"/>
						</div>
					</div>
					<div class='cell'>
					<% if( htmlpage.hasManageAuth() ) { %>
						<div class='field-title'><mtl:title key="status" descriptionKey="FIELD_STATUS"/></div>
						<div class='field'>
							<mtl:select id="condition" key="status" prefixKey="USR_USER_STATUS_" codeValues="00,PW,LK,99"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_STATUS"
									searchable="false"/>
						</div>
					<% } %>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
			<% } %>
			</div>
		<% if( multiSoldTo ) { %>
			<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
		<% } %>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
 				function checkSearchCond() {
					disableBlankInput( frmCond, true );
					return submitInput();
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contains id="record_pty" copyId="_record">
			<mtl:contentGroup groupId="partyinfo" type="content" descriptionKey="jsp.GRP_USR_PARTY_INFO">
				<%@ include file="include_usr_party_info.inc" %>
			</mtl:contentGroup>
		</mtl:contains>

		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				if( htmlpage.hasManageAuth() )
					listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_CHECK );
				else if( usingTPAuth && sessionMng.isAuthorized("ECS", "ECSTPAuth.LST.TP") )
					listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_CHECK );
				else
					listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NUMBER );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
				<% } %>
				<% if( listwriter.containsData() && sessionMng.isAuthorized("DPR", "DPRPartyAuth.MNG") ) { %>
					<mtl:button type="button" onClick="JavaScript: partnerAuthListReq();" messageKey="jsp.BTN_TRADEPARTNER_AUTH"/>
				<% } %>
				<% if( listwriter.containsData() && sessionMng.isAuthorized("DPR", "DPRPartyAuth.MNG") && multiSoldTo ) { %>
					<mtl:button type="download"/>
					<mtl:button type="upload"/>
				<% } %>
					<mtl:button type="close_if" styleClass="btn_list"/>
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
