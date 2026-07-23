<%--
	File Name:	dpr_itemprice_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.1	conditionItemName조건 수정.
	song7981	2016/05/20		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;
		function bodyLoad(){
			windowResizeTo( 800 );
		}

		function downloadReq() {
			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					windowOpen( url );
				}
			});
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,itemCode");
		}

		function deleteAllReq() {
			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE_ALL" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "del" );
					url = replaceQueryValue( url, "isdeleteAll", "Y" );
					windowOpen( url );
				}
			});
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItemMasterExtra?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "clsMng" );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,itemCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>")
			windowOpen( url, "clsMng" );
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list'/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<table class='search_content' cellspacing='0' cellpadding='0'>
				<tr><td>
				<table class='search_line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='search_subject' width='80'><mtl:message key="FIELD_DPR_ITEM_CODE"/></td>
					<td class='search_content'><mtl:text id="condition" key="itemCode" styleClass="length_15"/></td>

					<td class='search_subject'><mtl:message key="FIELD_DPR_ITEM_MASTER_NAME"/></td>
					<td class='search_content'><mtl:text id="condition" key="itemName" styleClass="length_30"/></td>
				</tr>
				</table>

				<table class='search_line_content' cellspacing='0' cellpadding='0'>
					<tr>
						<td class='search_subject' width='80'><mtl:message key="jsp.MSG_CONDITION"/></td>
						<td class='search_content'>
							<mtl:select id="condition" key="organizationCode" nullValueKey="MSG_COND_SALES_ORGANIZATION"
									hasBlank="true" listId="organizations" listCodeKey="organizationCode" mandatory="true"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"
									modified="JavaScript:JavaScript:Field.modified(this);"/>

						</td>
					</tr>
					</table>
				</td><td class='search_button'>
					<mtl:button type="submit" imageSrc="images/btn_search.gif" styleClass="btn_list"/>
					<mtl:button type="reset" href="JavaScript:resetSearchCond();" styleClass="btn_list"/>
				</td></tr>
			</table>

			<script type='text/javaScript'>
				function checkSearchCond() {
					if( frmCond.itemCode.value )
						frmCond.itemName.disabled = "true";

					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;

					return submitInput();
				}

				function initSearchCond() {
					if( frmCond.itemCode.value )
						Field.lock( frmCond.itemName );

					initConditionField( new Array( frmCond.organizationCode, frmCond.itemCode, frmCond.itemName ), Field.modified );
				}
				attachWindowEvent( "load", initSearchCond );

				function resetSearchCond() {
					frmCond.reset();
					resetForm( frmCond );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'><mtl:message key="jsp.SHOWCOUNT"/> <mtl:showcount modified="JavaScript:changeShowCount(this);"/></span>
			</td></tr>
			</table>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<mtl:button type="download" styleClass="btn_page"/>
				<% if( htmlpage.hasManageAuth() && sessionMng.isCountryAdmin() || sessionMng.isPartyAdmin() || sessionMng.isSystemAdmin() ) { %>
					<mtl:button type="upload" styleClass="btn_list"/>
					<mtl:button type="delete" styleClass="btn_list"/>
					<mtl:button type="modify" styleClass="btn_page" />
					<mtl:button type="regist" styleClass="btn_page" />
				<% } %>
				<!-- 데이터 전체 삭제버튼 -->
				<% if( sessionMng.isSystemAdmin()) { %>
					<mtl:button type="deleteAll" href="JavaScript:deleteAllReq();" imageSrc="images/btn_delAll.gif" styleClass="btn_list"/>
				<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' id='index_lst' align='center' width='100%' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
				<td class='list_content_bottom' id='index_btn' align='right' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>

		</mtl:contentGroup>
		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>


