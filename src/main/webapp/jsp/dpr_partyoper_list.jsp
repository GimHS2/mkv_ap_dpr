<%--
	File Name:	dpr_partyoper_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/06/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<script type='text/javascript'>

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,patternType,patternIndex,patternDate" );
		}

		function infoReq() {
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,patternType,patternIndex,patternDate" );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,patternType,patternIndex,patternDate" );
		}

		function registReq() {
			windowOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub", "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div id='messagebar'></div>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/>
						</div>
						<div class='field'>
						<%
							Object _distChannelCodes = condition.get( "distributionChannelCode" );
							String[] condDistributionChannelCodes;
							if( _distChannelCodes instanceof Object[] ) {
								condDistributionChannelCodes = new String[ ((Object[])_distChannelCodes).length ];
								int i = 0;
								for( Object o : (Object[])_distChannelCodes ) {
									condDistributionChannelCodes[i++] = (String)o;
								}
							} else {
								condDistributionChannelCodes = new String[1];
								condDistributionChannelCodes[0] = (String)_distChannelCodes;
							}
							for( Map<String, Object> channelMap : distributionChannels ) {
								String code = (String)channelMap.get( "distributionChannelCode" );
								String name = (String)channelMap.get( "distributionChannelName" );
								out.print( "<input type='checkbox' id='distributionChannelCode_" + code + "'" );
								out.print( " name='distributionChannelCode' class='mandatory' value='" + code + "'");
								out.print( " onclick='JavaScript: readConditionReq(\"SO;SG\");'" );
								for( String channelCode : condDistributionChannelCodes ) {
									if( code.equals(channelCode) ) {
										out.print( " checked " );
									}
								}
								out.print( ">" );
								out.print( "<label for='distributionChannelCode_" + code + "'>" );
								out.print( "<span>" + name + "</span>" );
								out.print( "</label>" );
							}
						%>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_PARTY_SALESOFFICE_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
									listId="offices" listCodeKey="officeCode"
									listNameFormat="$S{[:officeCode;$S{] :officeName}}"
									modified="readConditionReq(\"SG\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="groupCode" descriptionKey="FIELD_DPR_PARTY_SALESGROUP_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									listId="groups" listCodeKey="groupCode" listNameFormat="$S{[:groupCode;$S{] :groupName}}"/>
						</div>
					</div>
				</div>
			</div>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;
					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRStopItemCfg?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<div class='list-menu'>
				<mtl:select id="condition" key="availableInd" prefixKey="jsp.dpr_partyoper_list.MSG_AVAILABLEIND_" codeValues="Y,N" width="auto" searchable="false"
							hasBlank="true" nullValueKey="jsp.dpr_partyoper_list.MSG_AVAILABLEIND_SELECT" modified="JavaScript:listLink(this);"/>
			</div>

			<%
			ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
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
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>
		</mtl:contentGroup>
	</mtl:form>
</body>
</mtl:html>
