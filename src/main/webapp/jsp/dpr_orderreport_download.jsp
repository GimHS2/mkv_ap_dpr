<%--
	File Name:	dpr_orderreport_download.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2018/07/31		2.2.0	create
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
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function downloadReq() {
			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					url = replaceQueryValue( url, "fileType", "XLX" );
					windowOpen( url );
				}
			});
		}

		Select.setChained = function setChained( chainTargetObj, reqObj, chainEventSource ) {
			var url = "<%=systemConfig.getClassURL()%>/DPROrderReport?mode=codename";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );

			url = replaceQueryValue( url, "reqObj", reqObj );
			var fieldName = chainEventSource.target.name;
			var fieldValue = chainEventSource.target.value;
			if( fieldName ) {
				if( fieldValue ) {
					url = replaceQueryValue( url, fieldName, fieldValue );
					$.ajax({
						type: "GET",
						url: url,
						error: function(result) {
							console.log( result );
						},
						success: function(result) {
							Select.setCodeNames( chainTargetObj, result, true );
						}
					});
				}
			}
		}

		/**
		* codenames: list of {code: name} javascript object
		* remove existing and add code as selectObj.value and name as selectObj.innerHTML
		*/
		Select.setCodeNames = function( selectObj, codenames, checkingValue ) {
			Select.removeCodeNames( selectObj, true );
			return Select.addCodeNames( selectObj, codenames, checkingValue );
		}

		/**
		* @param selecttObj
		* @param hasBlank : selectObj is allow hasBlank( nullValueKey )
		*/
		Select.removeCodeNames = function( selectObj, hasBlank ) {
			// remove all except nullvalue(usually description of selectObj)
			$(selectObj).find('option').each(function(idx, obj){
				if( hasBlank ) {
					if( $(obj).val().length > 0 ) {
						$(obj).remove();
					}
				} else {
					$(obj).remove();
				}
			});
		}

		Select.addCodeNames = function( selectObj, codenames, checkingValue ) {
			var count = 0;

			for( var i = 0; i < codenames.length; i++ ) {
				var bool = true;

				if( checkingValue ) {
					for( var k = 0; k < selectObj.options.length; k++ )
						if( selectObj.options[k].value == codenames[i].code ) {
							bool = false;
							break;
						}
				}

				if( bool ) {
					count++;

					optionObj = document.createElement( "option" );
					optionObj.innerHTML = '['+ codenames[i].code +'] '+ codenames[i].name;
					optionObj.value = codenames[i].code;

					selectObj.options[selectObj.options.length] = optionObj;
				}
			}

			return count;
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<%@ include file="include_pub_calendar.inc"%>

		<mtl:form name="frmMain" method="get" target="subwin_download" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:contentGroup groupId="download" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="startOrderDate" descriptionKey="jsp.FIELD_DPR_STARTDATE"/></div>
							<div class='field'>
								<mtl:date id="record" key="startOrderDate"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="endOrderDate" descriptionKey="jsp.FIELD_DPR_ENDDATE"/></div>
							<div class='field'>
								<mtl:date id="record" key="endOrderDate"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="compositeOrderNumber" descriptionKey="MSG_COND_ORDERNUMBER"/></div>
							<div class='field'>
								<mtl:text id="condition" key="compositeOrderNumber"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
							<div class='field'>
								<mtl:select id="record" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"
										modified="readConditionReq(\"SHIP\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="shipPartyCode" descriptionKey="FIELD_DPR_SHIPPARTY_CODE"/></div>
							<div class='field'>
								<mtl:select id="record" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
										listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="submit" messageKey="jsp.BTN_DOWNLOAD"/>
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
							if( Field.checkMultiMandatory([frmMain.startOrderDate, frmMain.endOrderDate], false) ) {
								Field.checkDateRange( frmMain.startOrderDate, frmMain.endOrderDate );
							}
						}, 500 );
					});
				});

				function checkInput() {
					if( !Field.checkMultiMandatory([frmMain.startOrderDate, frmMain.endOrderDate], false) ) return false;
					if( !Field.checkDateRange(frmMain.startOrderDate, frmMain.endOrderDate) ) return false;
					return true;
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPROrderReport?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
