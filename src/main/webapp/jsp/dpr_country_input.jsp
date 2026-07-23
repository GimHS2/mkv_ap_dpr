<%--
	File Name:	dpr_country_input.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/06/30		2.2.3	Revise Order Feature.
	jbaek		2019/11/30		2.2.2	Multiple Country Manager 기능 추가
	jbaek		2014/03/31		2.2.1	CrossBrowsing 적용
	lsinji		2008/09/26		2.2.0	create
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
		java.util.Map condition = (java.util.Map)pageContext.findAttribute( "condition" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		var windowWidth = 850;

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountry?mode=imod";

			windowSelfOpen( url +"&countryCode="+ encodeURIComponent("<mtl:value id="record" key="countryCode"/>") );
		}

		function setCond() {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountryCond?mode=list">
			replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );

			windowsOpen( url+"&wintype=sub", "clsMng" );
		}

		function selectUserReq() {
			selectUserUser( "authUser", "Q", null );
		}

		function selectManagerReq( loopCnt ) {
			selectUserUser( "authUser"+loopCnt, "Q", null );
		}

		function setAuthUser( selectedValues ) {
			if( selectedValues ) {
				var values = selectedValues[0].split(";");

				frmMain.authUniqId.value = values[0];
				frmMain.authUserName.value = values[1];
			}
		}

		function infoManager() {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountryAuthorize?mode=list">
			replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );

			windowsOpen( url+"&wintype=sub", "clsMng" );
		}

	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='countryManager'>
			<input type='hidden' name='languageCode' value='en'/>
			<mtl:hidden id="record" key="countryCode"/>

			<mtl:contentGroup groupId="country" type="content" descriptionKey="jsp.GRP_DPR_COUNTRY_INFO">
				<div class='group-wrap'>
					<h3 class='gorup-title' style='margin-top: 0;'><mtl:message key="jsp.GRP_DPR_ITEM_BASE_INFO"/></h3>
					<div class='info-table table-fixed'>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:title key="partyId" descriptionKey="MSG_USR_PARTY_PARTYID"/></div>
								<div class='field-info'>
								<% if( "ireg".equals(htmlpage.getMode()) ) { %>
									<mtl:text id="record" key="partyId" mandatory="true"/>
								<% } else { %>
									<mtl:value id="record" key="partyId"/>
									<mtl:hidden id="record" key="partyId"/>
								<% } %>
								</div>
							</div>
							<div class='cell'>
								<div class='field-title'><mtl:title key="status"/></div>
								<div class='field-info'><mtl:select id="record" key="status" prefixKey="PUB_STATUS_" codeValues="00,99" searchable="false"/></div>
							</div>
						</div>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:title key="countryName"/></div>
								<div class='field-info'><mtl:text id="record" key="countryName"/></div>
							</div>
							<div class='cell'>
								<div class='field-title'><mtl:title key="countryKey"/></div>
								<div class='field-info'>
								<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
									<mtl:valuef id="record" format="$S{[:countryKey;$S{] :countryISOName}}"/>
								<% } else { %>
									<mtl:select id="record" key="countryKey" name="countryKey" listId="countries" listCodeKey="code"
											listNameFormat="$S{[:code;$S{] :name}}" searchable="false"/>
								<% } %>
								</div>
							</div>
						</div>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:title key="timeZone" descriptionKey="FIELD_TIMEZONE"/></div>
								<div class='field-info'>
								<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
									<mtl:valuef id="record" key="timeZoneName"/>
								<% } else { %>
									<mtl:select id="record" key="timeZone" name="timeZone" listId="timezones"
											listCodeKey="code" listNameFormat="$S{name}" searchable="false"/>
								<% } %>
								</div>
							</div>
							<div class='cell'>
								<div class='field-title'><mtl:title key="currencyCode"/></div>
								<div class='field-info'>
								<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
									<mtl:valuef id="record" format="$S{[:currencySymbol;$S{] :currencyName}}"/>
								<% } else { %>
									<mtl:select id="record" key="currencyCode" name="currencyCode" listId="currencies" listCodeKey="code"
											listNameFormat="$S{[:symbol;$S{] :name}}" searchable="false"/>
								<% } %>
								</div>
							</div>
						</div>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:title key="contactName"/></div>
								<div class='field-info'><mtl:text id="record" key="contactName"/></div>
							</div>
							<div class='cell'>
								<div class='field-title'><mtl:title key="contactTelephone"/></div>
								<div class='field-info'>
									<mtl:text id="record" key="contactTelephone"/>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class='group-line'></div>

				<div class='group-wrap'>
					<h3 class='gorup-title'><mtl:message key="jsp.GRP_DPR_COUNTRY_AUTH_INFO"/></h3>
					<div class='info-table table-fixed'>
					<%
						int rows = com.irt.rbm.RBMSystem.getSystemEnvInt( "DPR", "CountryAuth;ManagerRows", 3 );

						java.util.Map record = (java.util.Map)pageContext.findAttribute( "record" );
						if( record != null && record.get("countryAuthList") != null )
							pageContext.setAttribute( "countryAuthList", record.get("countryAuthList"), PageContext.PAGE_SCOPE );
						else
							pageContext.setAttribute( "countryAuthList", new java.util.ArrayList(java.util.Arrays.asList(new Object[] {new java.util.TreeMap() }))
									, PageContext.PAGE_SCOPE );

					%>
					<mtl:loop id="countryAuthList" loopId="loop" loopIndex="index" rows="<%= rows %>">
					<%
						int loopCnt = (Integer)pageContext.getAttribute("index")+1;
					%>

						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:message key="FIELD_DPR_COUNTRYAUTH_AUTHUSERNAME"/></div>
								<div class='field-info'>
									<mtl:text id="loop" key="authUserName" name='<%=( "authUser"+(String.valueOf(loopCnt)+"Name") ) %>' />
									<mtl:hidden id="loop" key="authUniqId" name='<%=( "authUser"+(String.valueOf(loopCnt)+"Id") ) %>'/>
									<input type='hidden' name='authUniqId'/>
								</div>
							</div>
						</div>
					</mtl:loop>

					<% if( com.irt.dpr.Country.isFeature(sessionMng.getExtraValue(), "useRevOrd") ) { %>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:message key="jsp.FIELD_DPR_PARTY_ORDREV_MAX_LIMIT"/></div>
								<div class='field-info'>
									<mtl:text id="record" key="partyRevOrdMaxLimit"/>
								</div>
							</div>
						</div>
					</div>
					<div class='info-table table-fixed'>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:message key="jsp.FIELD_DPR_PARTY_ORDREV_SEND_EMAIL"/></div>
								<div class='field-info'>
									<mtl:text id="record" key="partyRevOrdSndEmail"/>
								</div>
							</div>
							<div class='cell'>
								<div class='field-title'><mtl:message key="jsp.FIELD_DPR_PARTY_ORDREV_RECEIVE_EMAILS"/></div>
								<div class='field-info'>
									<mtl:text id="record" key="partyRevOrdRcvEmails"/>
								</div>
							</div>
						</div>
					</div>
				<% } %>
				</div>
				<div class='group-line'></div>

				<div class='group-wrap'>
					<h3 class='gorup-title'><mtl:message key="jsp.GRP_DPR_COUNTRY_ENV"/></h3>
					<div class='info-table table-fixed'>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:title key="defaultHierarchyLevel"/></div>
								<div class='field-info'>
									<mtl:select id="record" key="defaultHierarchyLevel" prefixKey="jsp.dpr_country_input.HIERARCHY_LEVEL_"
											codeValues="1,2,3,4,5,6" searchable="false"/>
								</div>
							</div>
							<div class='cell'>
								<div class='field-title'><mtl:title key="hierarchyCondition"/></div>
								<div class='field-info'>
									<mtl:text id="record" key="hierarchyCondition"/>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class='list-function'>
					<div class='button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
					<% } else if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="modify"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					<% if( "ireg".equals(htmlpage.getMode()) ) { %>
						if( !Field.checkMandatory(frmMain.partyId) ) return false;
					<% } %>

					if( !checkHierarchyCondition( frmMain.hierarchyCondition ) ) {
						var messages = { "header" : "<mtl:message key="jsp.dpr_country_input.MSG_HIERARCHY_CONDITION_INVALID" encodeScript="true"/>" };
						customPopup.alert( messages );
						frmMain.hierarchyCondition.focus();

						return false;
					}

					$("#content_auth").find("[name^=authUser][name$=Id]")
						.each(function(i, o){
							if( $.isArray(frmMain.authUniqId) ) {
								frmMain.authUniqId[i].value = this.value || "";
							} else {
								frmMain.authUniqId.value = this.value || "";
							}
						});

					return submitInput();
				}

				function checkHierarchyCondition( condition ) {
					if( !condition || condition == "" || typeof condition == "undefined" ) return false;

					if( condition.value == null || condition.value == "" )
						return true;

					var values = condition.value.split( ";" );
					var size = values.length;
					for( var i = 0; i < size; i++ ) {
						var validation = true;

						var len = values[i].length;
						if( len != null && len != 1 && len != 2 && len != 6 && len != 10 && len != 14 && len != 18 )
							return false;

						for( var j = i + 1; j < size; j++ ) {
							if( values[i].indexOf(values[j]) >= 0 || values[j].indexOf(values[i]) >= 0 ) {
								return false;
							}
						}
					}

					return true;
				}

			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
