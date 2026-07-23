<%--
	File Name:	dpr_partyoper_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/06/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	if( recordMap != null ) {
		String patternType = (String)recordMap.get( "patternType" );
		if( "2".equals(patternType) ) {
			String patternIndex = recordMap.get("patternIndex").toString();
			if( patternIndex.length() == 2 ) {
				recordMap.put( "patternIndex1", patternIndex.charAt(0) );
				recordMap.put( "patternIndex2", patternIndex.charAt(1) );
			}
		}
	}

	boolean imod = "imod".equals(htmlpage.getMode());
%>
<head>
	<style type="text/css">
		div.error-text {
			line-height: 25px;
			position: absolute;
			font-size: 12px;
			color: #B41601;
			margin-left: 5px;
		}
	</style>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			changePatternType();
		}

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyOper?mode=imod";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "officeCode", "<mtl:value id="record" key="officeCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "groupCode", "<mtl:value id="record" key="groupCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "patternType", "<mtl:value id="record" key="patternType" encodeScript="true"/>" );
			url = replaceQueryValue( url, "patternIndex", "<mtl:value id="record" key="patternIndex" encodeScript="true"/>" );
			url = replaceQueryValue( url, "patternDate", "<mtl:value id="record" key="patternDate" encodeScript="true"/>" );

			windowSelfOpen( url );
		}

		function changePatternType( patternType ) {
			if( frmMain.patternType )
				patternType = frmMain.patternType.value;
			else
				patternType = "<mtl:value id="record" key="patternType"/>";

			Styles.changeDisplay( document.all.pattern_0, patternType == "0" );
			Styles.changeDisplay( document.all.pattern_1, patternType == "1" );
			Styles.changeDisplay( document.all.pattern_2, patternType == "2" );
			Styles.changeDisplay( document.all.pattern_3, patternType == "3" );
			Styles.changeDisplay( document.all.pattern_4, patternType == "4" );
			Styles.changeDisplay( document.all.pattern_5, patternType == "5" );
			Styles.changeDisplay( document.all.pattern_6, patternType == "6" );

			if( frmMain.patternType ) changePatternValue();
		}

		function changePatternValue() {
			var idx = frmMain.patternType.value;

			frmMain.patternIndex[2].value = frmMain.patternIndex1.value +""+ frmMain.patternIndex2.value;
			if( frmMain.patternDate[idx].value != "" && frmMain.patternDate[idx].value )
				frmMain.pattern.value = "Y";
			else
				frmMain.pattern.value = "";
			Field.modified( frmMain.pattern );
		}

		function registReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg&retailGln=<mtl:value id="record" key="retailGln"/>" );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:contentGroup groupId="input" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="name" mandatory="true"/></div>
							<div class='field'><mtl:text id="record" key="name" mandatory="true"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
								<mtl:hidden id="record" key="organizationCode"/>
							<% } else { %>
								<mtl:select id="record" key="organizationCode" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG\");"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
								<mtl:hidden id="record" key="distributionChannelCode"/>
							<% } else { %>
								<mtl:select id="record" key="distributionChannelCode" mandatory="false" listId="distributionChannels" listCodeKey="distributionChannelCode"
										hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO;SG\");"/>
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
										modified="readConditionReq(\"SG\");" mandatory="false"/>
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
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP" mandatory="false"
										listId="groups" listCodeKey="code" listNameFormat="$S{[:code;$S{] :name}}"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="patternType" descriptionKey="FIELD_SCHEDULE_PATTERN"/></div>
							<div class='field'>
								<mtl:hidden id="record" key="patternType" name="pattern"/>
								<mtl:select id="record" key="patternType" codeValues="0,1,2,3,4,5,6" format="${DPR_PARTYOPER_PTN_TYPE_@patternType}"
										modified="JavaScript:changePatternType();" searchable="false" width="auto"/>

								<span id='pattern_0' style='display: none;'>
									<mtl:date id="record" key="patternDate" style="width: 200px;" modified="JavaScript:changePatternValue();"/>
									<input type='hidden' name='patternIndex' value='0'/>
								</span>
								<span id='pattern_1' style='display: none;'>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN1_MSG1"/>
									<mtl:select id="record" key="patternIndex"
											codeValues="1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31"
											modified="JavaScript:changePatternValue();" searchable="false" width="auto"/>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN1_MSG2"/>
									<input type='hidden' name='patternDate' value='1900-01-01'/>
								</span>
								<span id='pattern_2' style='display: none;'>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN2_MSG1"/>
									<mtl:select id="record" key="patternIndex1" codeValues="1,2,3,4,5" searchable="false" width="auto"
											modified="JavaScript:changePatternValue();"/><mtl:message key="jsp.dpr_partyoper_input.MSG_NTH"/>
									<mtl:select id="record" key="patternIndex2" searchable="false" width="auto"
											prefixKey="PUB_DAY_OF_WEEK_" codeValues="1,2,3,4,5,6,7"
											modified="JavaScript:changePatternValue();"/>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN2_MSG2"/>
									<input type='hidden' name='patternIndex' value=''/>
									<input type='hidden' name='patternDate' value='1900-01-01'/>
								</span>
								<span id='pattern_3' style='display: none;'>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN3_MSG1"/>
									<mtl:date id="record" key="patternDate" style="width: 150px;" modified="JavaScript:changePatternValue();"/>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN3_MSG2"/>
									<mtl:text id="record" key="patternIndex" maxlength="2" style="width: 40px;" modified="JavaScript:changePatternValue();"/>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN3_MSG3"/>
								</span>
								<span id='pattern_4' style='display: none;'>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN4_MSG1"/>
									<mtl:select id="record" uniqId="patternIndex3" key="patternIndex" prefixKey="PUB_DAY_OF_WEEK_" codeValues="1,2,3,4,5,6,7"
											modified="JavaScript:changePatternValue();" searchable="false" width="auto"/>
									<mtl:message key="jsp.dpr_partyoper_input.PATTERN4_MSG2"/>
									<input type='hidden' name='patternDate' value='1900-01-01'/>
								</span>
								<span id='pattern_5' style='display: none;'>
									<input type='hidden' name='patternIndex' value='0'/>
									<input type='hidden' name='patternDate' value='1900-01-01'/>
								</span>
								<span id='pattern_6' style='display: none;'>
									<mtl:date id="record" key="patternDate" style="width: 200px;" modified="JavaScript:changePatternValue();"/>
									<input type='hidden' name='patternIndex' value='6'/>
								</span>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderInd"/></div>
							<div class='field'>
								<mtl:select id="record" key="orderInd" codeValues="Y,N" defaultValue="N" searchable="false"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="delvInd"/></div>
							<div class='field'>
								<mtl:select id="record" key="delvInd" codeValues="Y,N" defaultValue="N" searchable="false"/>
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
				function checkInput() {
					if( !Field.checkMandatory(frmMain.organizationCode) ) return false;
					if( !Field.checkMandatory(frmMain.name) ) return false;
					if( !Field.checkMandatory(frmMain.patternType) ) return false;

					var idx = frmMain.patternType.value;
					frmMain.patternIndex[2].value = frmMain.patternIndex1.value +""+ frmMain.patternIndex2.value;

					if( !Field.checkMandatory(frmMain.patternDate[idx]) ) return false;
					if( !Field.checkNumberRange(frmMain.patternIndex[idx], false, 0, 99, 0x03) ) return false;

					for( var i = 0; i <= 5; i++ ) {
						if( i == idx ) continue;
						frmMain.patternDate[i].disabled = true;
						frmMain.patternIndex[i].disabled = true;
					}

					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRPartyOper?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
