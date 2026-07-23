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

	boolean inserting = "reg".equals(htmlpage.getMode()) || "ireg".equals(htmlpage.getMode());
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			changePatternType();

			windowResizeTo( 700 );
			resetForm( frmMain );
			focusForm( frmMain );
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

		openCalendar.click = function( date ) {
			DateField.setDate( openCalendar.elementName, date, true );
			DateField.modifiedByName( openCalendar.elementName );
			changePatternValue();
		}

		function readDistributionChannelReq( organizationCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyOper?mode=rtp";

			if( !organizationCode || typeof organizationCode == "undefined" ) {
				if( !Field.checkMandatory(frmMain.organizationCode) ) {
					frmMain.distributionChannelCode.disabled = true;
					return;
				}
			}

			frmMain.distributionChannelCode.disabled = false;
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", organizationCode );
			url = replaceQueryValue( url, "divisionCode", "<mtl:value id="condition" key="divisionCode"/>" );
			url = replaceQueryValue( url, "ctype", "input" );

			if( typeof frmMain.organizationCode == "undefined" )
				frmMain.organizationCode.disabled = true;
			if( typeof frmMain.distributionChannelCode == "undefined" )
				frmMain.distributionChannelCode.disabled = true;
			if( typeof frmMain.partyCode != "undefined" )
				frmMain.partyCode.disabled = false;

			windowOpen( url, "clsName" );
		}

		function readOfficeGroupReq( organizationCode, distributionChannelCode, officeCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyOper?mode=rtp";

			if( !organizationCode || typeof organizationCode == "undefined" ) {
				if( typeof frmMain.organizationCode != "undefined" ) {
					if( !Field.checkMandatory(frmMain.organizationCode) ) {
						frmMain.distributionChannelCode.disabled = true;
						return;
					}

					organizationCode = frmMain.organizationCode.value;
				}
			}

			if( !distributionChannelCode || typeof distributionChannelCode == "undefined" ) {
				if( typeof frmMain.distributionChannelCode != "undefined" ) {
					if( !Field.checkMandatory(frmMain.distributionChannelCode) ) {
						return;
					}

					distributionChannelCode = frmMain.distributionChannelCode.value;
				}
			}

			if( !officeCode || typeof officeCode == "undefined" ) {
				if( typeof frmMain.officeCode != "undefined" ) {
					officeCode = frmMain.officeCode.value;
				}
			}

			url = replaceQueryValue( url, "countryCode", "<mtl:value id="record" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", organizationCode );
			url = replaceQueryValue( url, "distributionChannelCode", distributionChannelCode );
			url = replaceQueryValue( url, "officeCode", officeCode );
			url = replaceQueryValue( url, "ctype", "input" );


			windowOpen( url, "clsName" );
		}

		function readTradePartnersReq() {
			return;
		}
	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>

		<mtl:contentGroup groupId="input" type="content">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject' width='25%'><mtl:title key="name" mandatory="true"/></td>
				<td class='content2'><mtl:text id="record" key="name" mandatory="true" styleClass="length_50"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='subject' width='25%'><mtl:title key="organizationCode"/></td>
				<td class='content2'>
					<mtl:select id="record" key="organizationCode" nullValueKey="MSG_COND_SALES_ORGANIZATION"
							hasBlank="true" listId="organizations" listCodeKey="organizationCode" mandatory="true"
							listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"
							modified="JavaScript:readDistributionChannelReq(this.value); JavaScript:Field.modified(this);"/>
				</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='subject' width='25%'><mtl:title key="distributionChannelCode"/></td>
				<td class='content2'>
					<span id='search_distribution'>
						<mtl:select id="record" key="distributionChannelCode"
								nullValueKey="MSG_COND_DISTRIBUTION_CHANNEL" mandatory="true"
								hasBlank="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
								listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"
								modified="JavaScript:readOfficeGroupReq(null, this.value, null);JavaScript:Field.modified(this);"/>
					</span>
				</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr><td class='subject' width='25%'><mtl:title key="officeCode"/></td>
					<td class='content2'>
						<span id='search_office'>
							<mtl:select id="record" key="officeCode"
									nullValueKey="FIELD_DPR_PARTY_SALESOFFICE_CODE" mandatory="false"
									hasBlank="true" listId="offices" listCodeKey="officeCode"
									listNameFormat="$S{[:officeCode;$S{] :officeName}}"
									modified="JavaScript:readOfficeGroupReq(null, null, this.value); JavaScript:Field.modified(this);"/>
						</span>
					</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr><td class='subject' width='25%'><mtl:title key="groupCode"/></td>
					<td class='content2'>
						<span id='search_group'>
							<mtl:select id="record" key="groupCode"
									nullValueKey="FIELD_DPR_PARTY_SALESGROUP_CODE" mandatory="false"
									hasBlank="true" listId="groups" listCodeKey="groupCode"
									listNameFormat="$S{[:groupCode;$S{] :groupName}}"
									modified="JavaScript: JavaScript:Field.modified(this);" />
						</span>
					</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject' width='25%'><mtl:title key="patternType" name="pattern" descriptionKey="FIELD_SCHEDULE_PATTERN"/></td>
				<td class='content2'>
					<mtl:hidden id="record" key="patternType" name="pattern"/>
					<mtl:select id="record" key="patternType" codeValues="0,1,2,3,4,5,6" format="${[:DPR_PARTYOPER_PTN_TYPE_@patternType;]}"
							modified="JavaScript:Field.modified(this);changePatternType();"/>

					<span id='pattern_0' style='display: none;'>
						<mtl:text id="record" key="patternDate" onBlur="JavaScript:Field.formatDate(this);"
								styleClass="length_date_ymd" modified="JavaScript:Field.modified(this);changePatternValue();"/>
						<mtl:ibutton type="calendar" key="patternDate" href="JavaScript:openCalendar(\"frmMain.patternDate[0]\");" styleClass="tbtn"/>
						<input type='hidden' name='patternIndex' value='0'/>
					</span>
					<span id='pattern_1' style='display: none;'>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN1_MSG1"/>
						<mtl:select id="record" key="patternIndex"
								codeValues="1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31"
								modified="JavaScript:Field.modified(this);changePatternValue();"/>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN1_MSG2"/>
						<input type='hidden' name='patternDate' value='1900-01-01'/>
					</span>
					<span id='pattern_2' style='display: none;'>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN2_MSG1"/>
						<mtl:select id="record" key="patternIndex1" mandatory="true" readonly="<%= !inserting %>" codeValues="1,2,3,4,5"
								modified="JavaScript:Field.modified(this);changePatternValue();"/><mtl:message key="jsp.dpr_partyoper_input.MSG_NTH"/>
						<mtl:select id="record" key="patternIndex2" mandatory="true" readonly="<%= !inserting %>"
								prefixKey="PUB_DAY_OF_WEEK_" codeValues="1,2,3,4,5,6,7"
								modified="JavaScript:Field.modified(this);changePatternValue();"/>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN2_MSG2"/>
						<input type='hidden' name='patternIndex' value=''/>
						<input type='hidden' name='patternDate' value='1900-01-01'/>
					</span>
					<span id='pattern_3' style='display: none;'>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN3_MSG1"/>
						<mtl:text id="record" key="patternDate" onBlur="JavaScript:Field.formatDate(this);"
								styleClass="length_date_ymd" modified="JavaScript:Field.modified(this);changePatternValue();"/>
						<mtl:ibutton type="calendar" key="patternDate" href="JavaScript:openCalendar(\"frmMain.patternDate[3]\");" styleClass="tbtn"/>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN3_MSG2"/>
						<mtl:text id="record" key="patternIndex" styleClass="length_3" maxlength="2"
								modified="JavaScript:Field.modified(this);changePatternValue();"/>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN3_MSG3"/>
					</span>
					<span id='pattern_4' style='display: none;'>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN4_MSG1"/>
						<mtl:select id="record" key="patternIndex" prefixKey="PUB_DAY_OF_WEEK_" codeValues="1,2,3,4,5,6,7"
								modified="JavaScript:Field.modified(this);changePatternValue();"/>
						<mtl:message key="jsp.dpr_partyoper_input.PATTERN4_MSG2"/>
						<input type='hidden' name='patternDate' value='1900-01-01'/>
					</span>
					<span id='pattern_5' style='display: none;'>
						<input type='hidden' name='patternIndex' value='0'/>
						<input type='hidden' name='patternDate' value='1900-01-01'/>
					</span>
					<span id='pattern_6' style='display: none;'>
						<mtl:text id="record" key="patternDate" onBlur="JavaScript:Field.formatDate(this);"
								styleClass="length_date_ymd" modified="JavaScript:Field.modified(this);changePatternValue();"/>
						<mtl:ibutton type="calendar" key="patternDate" href="JavaScript:openCalendar(\"frmMain.patternDate[6]\");" styleClass="tbtn"/>
						<input type='hidden' name='patternIndex' value='6'/>
					</span>
				</td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject' width='25%'><mtl:title key="orderInd"/></td>
				<td class='content2'><mtl:select id="record" key="orderInd" codeValues="Y,N" defaultValue="N"/></td>
			</tr>
			</table>
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject' width='25%'><mtl:title key="delvInd"/></td>
				<td class='content2'><mtl:select id="record" key="delvInd" codeValues="Y,N" defaultValue="N"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
				<mtl:button type="submit" styleClass="btn_page"/>
				<mtl:button type="reset" styleClass="btn_page"/>
			<% } else if( htmlpage.hasManageAuth() ) { %>
				<% if( "reg".equals(htmlpage.getMode()) ) { %>
					<mtl:button type="regist" styleClass="btn_page"/>
				<% } %>
			<% } %>
			<mtl:button type="close_if" styleClass="btn_page"/>
		</td></tr>
		</table>

		<script type='text/javascript'>
			function checkInput() {
				if( !Field.checkMandatory(frmMain.name) ) return false;
				if( !Field.checkMandatory(frmMain.patternType) ) return false;

				var idx = frmMain.patternType.value;
				frmMain.patternIndex[2].value = frmMain.patternIndex1.value +""+ frmMain.patternIndex2.value;

				if( !Field.checkMandatory(frmMain.patternDate[idx], document.all.title_pattern.innerHTML) ) return false;
				if( !Field.checkDateFormat(frmMain.patternDate[idx]) ) return false;

				if( !Field.checkMandatory(frmMain.patternIndex[idx], document.all.title_pattern.innerHTML) ) return false;
				if( !Field.checkNumberFormat(frmMain.patternIndex[idx]) ) return false;
				if( !Field.checkNumberRange(frmMain.patternIndex[idx], false, 0, 99, 0x03) ) return false;

				for( var i = 0; i <= 5; i++ ) {
					if( i == idx ) continue;
					frmMain.patternDate[i].disabled = true;
					frmMain.patternIndex[i].disabled = true;
				}

				return submitInput();
			}

			function resetInput() {
				frmMain.reset();
				changePatternType();
				resetForm( frmMain );
			}
		</script>
	</mtl:form>
</body>
</mtl:html>
