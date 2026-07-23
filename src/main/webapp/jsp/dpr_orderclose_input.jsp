<%--
	File Name:	dpr_orderclose_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/06/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );

	String hour = "";
	for(int i = 0; i < 24; i++ ) {
		if( i < 10 )
		hour += "0" + i + ",";
		else
		hour += i + ",";
	}

	String minutes = "";
	for( int i = 0; i <= 59; i++ ) {
		if( i < 10 )
		minutes += "0" + i + ",";
		else
		minutes += i + ",";
	}


%>
<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<%
	boolean imod = "imod".equals(htmlpage.getMode());
	%>

	<script type='text/javascript'>
		var windowHeight = 600;

		function bodyLoad() {
			changeTimeValue( TimeField.getValue(frmMain.ordCloseTime) );
			$("#hour").singleSelectmenu( "refresh" );
			$("#minutes").singleSelectmenu( "refresh" );
		}

		function changeTimeValue( timeValue ) {
			if( timeValue ) {
				if( TimeField.checkValue( timeValue ) ) {
					var hm = timeValue.split(":");
					if( frmMain.hour && frmMain.minutes ) {
						frmMain.hour.value = hm[0];
						frmMain.minutes.value = hm[1];
					}
				}
			}

			if( frmMain.hour && frmMain.minutes ) {
				frmMain.ordCloseTime.value = frmMain.hour.value + ":" + frmMain.minutes.value;
				Field.modified( frmMain.ordCloseTime );
			}
		}
	</script>
	<style type="text/css">
		.ui-selectmenu-menu.selectmenu-open .selectmenu-menu-wrapper ul.ui-menu {
			text-align: left;
			padding-bottom: 10px;
			max-height: 200px;
			overflow-y: auto;
		}
	</style>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
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
								<mtl:hidden id="record" key="organizationCode"/>
							<% } else { %>
								<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="brandCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="brandCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_BRAND"
										hasBlank="true" listId="brands" listCodeKey="brandCode" listNameFormat="$S{[:brandCode;] $S{:brandName}}"
										format="$S{[:brandCode;] $S{:brandName}}"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="ordCloseTime" mandatory="true"/></div>
							<div class='field'>
								<mtl:hidden id="record" key="ordCloseTimeZone" />
							<% if( HtmlPage.INPUT_INFORMATION == htmlpage.getInputStatus() ) { %>
								<mtl:text id="record" key="ordCloseTime"/>
							<% } else { %>
								<mtl:select id="record" key="hour" codeValues="<%= hour %>" modified="JavaScript:changeTimeValue();"
										width="auto" searchable="false"/>
								:
								<mtl:select id="record" key="minutes" codeValues="<%= minutes %>" modified="JavaScript:changeTimeValue();"
										width="auto" searchable="false"/>
								<mtl:hidden id="record" key="ordCloseTime"/>
							<% } %>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="close_if"/>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="reset"/>
						<mtl:button type="save"/>
					<% } else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
						<mtl:button type="modify"/>
					<% } %>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>

					if( !TimeField.checkValue( frmMain.ordCloseTime.value ) ) {
						customPopup.alert( { "header": "<mtl:message key="ERR_INVALID_TIME"/>"+ " - " + encodeURIComponent(frmMain.ordCloseTime.value) } );
						return false;
					}

					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>

