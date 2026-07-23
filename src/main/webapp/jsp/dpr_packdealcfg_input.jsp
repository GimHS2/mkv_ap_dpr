<%--
	File Name:	dpr_packdealcfg_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/04/30		2.2.1	packdeal tolrate 100허용하여 제한없이 발주하도록 설정.
	jbaek		2019/05/30		2.2.0	create
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
	<%@ include file="include_pub_input.inc" %>

	<%
	boolean imod = "imod".equals(htmlpage.getMode());
	%>

	<script type='text/javascript'>
		function bodyLoad() {
				windowResizeTo( 600 );
				resetForm( frmMain );
				focusForm( frmMain );
		}

		function changeGroupList( value ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=cond&datatype=SG";
			if( value == "" )
				value = "null";
			else
				url = replaceQueryValue( url, "officeCode", encodeURIComponent(value) );

			windowOpen( url, "clsName" );
		}

		function selectDPRPartyReq( naming ) {
			var query = "";
			if( frmMain.distributionChannelCode && frmMain.distributionChannelCode.value ) {
				query += ( "&distributionChannelCode="+ frmMain.distributionChannelCode.value );
			}
			if( frmMain.officeCode && frmMain.officeCode.value && frmMain.officeCode.value != "0" ) {
				query += ( "&officeCode="+ frmMain.officeCode.value );
			}
			if( frmMain.groupCode && frmMain.groupCode.value && frmMain.groupCode.value != "0" ) {
				query += ( "&groupCode="+ frmMain.groupCode.value );
			}
			var value;
			if( naming ) {
				query += ( "&partyCode="+ frmMain.partyCode.value );
			}
			_selectDPRParty( "party", "D", ""+query, null, null, naming );
		}

		function _selectDPRItem( slname, namecls, attr, value, winname , naming) {
			var url = classURL +"DPRItem?slname="+ slname;
			url += ( namecls ? "&namecls="+ namecls : "" );
			url += ( attr ? "&attr="+ attr : "" );

			if( frmMain.partyCode && frmMain.partyCode.value && frmMain.partyCode.value != "0" ) {
				url += ( "&btype=ord" );
				url += ( "&partyCode="+ frmMain.partyCode.value );
			} else {
				url += ( "&btype=itm" );
			}

			var query = "";
			if( frmMain.distributionChannelCode && frmMain.distributionChannelCode.value ) {
				query += ( "distributionChannelCode="+ frmMain.distributionChannelCode.value );
			}
			url += "&"+ query;

			if( value && naming )
				url += "&code=" + value;

			if( naming )
				windowOpen( url +"&mode=name", winname ? winname : "clsName" );
			else
				windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

			return false;

		}

		function selectDPRItemReq( naming ) {
			if( naming ) {
				return _selectDPRItem( "item", "D", "", null, null, naming );
			} else {
				_selectDPRItem( "item", "D", "", null, null, naming );
			}
		}

// 		function changeDealStopDate() {
// 			if( frmMain.packdealStopInd.value == "Y" ) {
<%-- 				if( !frmMain.dealApplyStopDate.value || "<mtl:value id="record" key="dealApplyStopDate"/>" < "<%=com.irt.data.Date.getInstance(sessionMng.getTimeZone())%>" ) { --%>
<%-- 					frmMain.dealApplyStopDate.value = "<%=com.irt.data.Date.getInstance(sessionMng.getTimeZone())%>"; --%>
// 				}
// 			} else {
// 				frmMain.dealApplyStopDate.value = "";
// 			}
// 		}

	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_pub_calendar.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>

			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
								<mtl:hidden id="record" key="organizationCode"></mtl:hidden>
								<mtl:hidden id="record" key="organizationName"></mtl:hidden>
							<% } else { %>
								<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD\");"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
								<mtl:hidden id="record" key="distributionChannelCode"/>
							<% } else { %>
								<mtl:select id="record" key="distributionChannelCode" mandatory="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO;SG;SOLD\");"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="officeCode" format="$S{[:officeCode;]$S{ :officeName}}"/>
								<mtl:hidden id="record" key="officeCode"/>
							<% } else { %>
								<mtl:select id="record" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
										listId="offices" listCodeKey="code" listNameFormat="$S{[:code;$S{] :name}}"
										modified="readConditionReq(\"SG;SOLD\");" mandatory="false"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="groupCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="groupCode" format="$S{[:groupCode;]$S{ :groupName}}"/>
								<mtl:hidden id="record" key="groupCode"/>
							<% } else { %>
								<mtl:select id="record" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
										listId="groups" listCodeKey="code" listNameFormat="$S{[:code;$S{] :name}}"
										modified="readConditionReq(\"SOLD\");" mandatory="false"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="partyCode" format="$S{[:partyCode;]$S{ :partyName}}"/>
								<mtl:hidden id="record" key="partyCode"/>
							<% } else { %>
								<mtl:select id="record" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="false"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="dealCode"/></div>
							<div class='field'><mtl:text id="record" key="dealCode"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="targetTotalAmount"/></div>
							<div class='field'><mtl:text id="record" key="targetTotalAmount"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="toleranceRate"/></div>
							<div class='field'><mtl:text id="record" key="toleranceRate"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="dealStopInd" descriptionKey="jsp.dpr_packdeal_input.FIELD_DPR_PACKDEAL_DEALSTOPIND"/></div>
							<div class='field'>
								<mtl:select id="record" key="dealStopInd" prefixKey="PUB_WHETHER_" codeValues="Y,N" defaultValue="N" searchable="false"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="dealStartDate"/></div>
							<div class='field'>
								<mtl:date id="record" key="dealStartDate"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="dealEndDate"/></div>
							<div class='field'>
								<mtl:date id="record" key="dealEndDate"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
					<% } else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
						<mtl:button type="modify"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				$( function() {
					$("input[type=text].date").blur( function() {
						setTimeout( function() {
							if( Field.checkMultiMandatory([frmMain.dealStartDate, frmMain.dealEndDate], false) ) {
								Field.checkDateRange( frmMain.dealStartDate, frmMain.dealEndDate );
							}
						}, 500 );
					});
				});

				function checkInput() {
					<%= htmlpage.getValidationScript() %>

					if( !Field.checkNumberRange( frmMain.targetTotalAmount, false, -1 ) ) return false;
					if( !Field.checkNumberRange( frmMain.toleranceRate, false, -1, 101 ) ) return false;

					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRPackDealCfg?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>

