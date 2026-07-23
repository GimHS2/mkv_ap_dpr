<%--
	File Name:	dpr_item_download.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.1	encoding추가, distributionChannelCode선택 가능하도록 변경.
	lsinji		2009/01/09		2.2.0	create
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
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:hidden id="record" key="sslType" defaultValue="D"/>
			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'>
								<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
							</div>
							<div class='field'>
								<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
										nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'>
								<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/>
							</div>
							<div class='field'>
							<%
								Object _distChannelCodes = condition.get( "distributionChannelCode" );
								String[] condDistributionChannelCodes;
								if( _distChannelCodes instanceof String[] ) {
									condDistributionChannelCodes = (String[])_distChannelCodes;
								} else {
									condDistributionChannelCodes = new String[1];
									condDistributionChannelCodes[0] = (String)_distChannelCodes;
								}
								for( Map<String, Object> channelMap : distributionChannels ) {
									String code = (String)channelMap.get( "distributionChannelCode" );
									String name = (String)channelMap.get( "distributionChannelName" );
									out.print( "<input type='checkbox' id='distributionChannelCode_" + code + "'" );
									out.print( " name='distributionChannelCode' class='mandatory' value='" + code + "'");
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
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="downloadType" mandatory="true" descriptionKey="jsp.MSG_DOWNLOAD_TYPE"/></div>
							<div class='field'>
								<mtl:select id="condition" key="downloadType" prefixKey="jsp.MSG_DOWNLOAD_TYPE_" codeValues="SSL" searchable="false"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="fileType" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILETYPE"/></div>
							<div class='field'>
								<mtl:radio id="record" key="fileType" mandatory="true" prefixKey="PUB_FILEFORMAT_" codeValues="CSV,TAB" defaultValue="CSV"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="encoding" mandatory="true" descriptionKey="FIELD_ENCODING"/></div>
							<div class='field'>
								<mtl:select id="record" key="encoding" mandatory="true" prefixKey="PUB_ENCODING_" codeValues="UTF8,UTF16,EUC-KR,GB18030" searchable="false"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="close_if"/>
						<mtl:button type="return"/>
						<mtl:button type="submit" icon="images/ico_download_white.png" messageKey="jsp.BTN_DOWNLOAD"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>

					if( !Field.checkMandatory(frmCond.organizationCode) ) return;
					if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return;

					return submitInput( $("input[name=mode]").val() );
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=rtp";
					url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
