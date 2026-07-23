<%--
	File Name:	dpr_billingreport_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/06/30		2.2.1	useDangerousItem 항목 추가
	jbaek		2017/07/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr'%>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map"%>
<%@ taglib uri="/mtltaglib" prefix="mtl"%>
<%
	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map condition = (java.util.Map)request.getAttribute("condition");
	String organizationCode = (String)condition.get("organizationCode");
%>
<head>
<%@ include file="include_rbm_header.inc"%>
<%@ include file="include_pub_input.inc"%>
	<script type='text/javascript'>
		var linkmenu = null;
		function bodyLoad() {
			$(frmCond.partyCode).on("change", function(event){
				Select.setChained( frmCond.shipPartyCode, "shipParties", event );
			});
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,partyCode,shipPartyCode,customerOrderNumber,billVatNumber,billDate" );
		}

		function deleteAllReq() {
			resetSearchCond();

			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE_ALL_SEARCHED" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					Field.setValue(frmCond.isDeleteAll, 'Y');
					frmCond.submit();
				}
			});
		}

		function downloadReq() {
			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					url = replaceQueryValue( url, "orderType", "<mtl:value id="condition" key="orderType"/>" );
					windowOpen( url );
				}
			});
		}

		function downTemplateReq() {
			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					url = replaceQueryValue( url, "type", "dwntpl" );
					windowOpen( url );
				}
			});
		}

		function openExternalSiteLink( siteAddress ) {
			if( siteAddress ) {
				var url = "<%=systemConfig.getClassURL()%>/DPRSiteLink?mode=wcf&wintype=sub";
				url = replaceQueryValue( url, "locale", "<%=htmlpage.getLocale()%>" );
				url = replaceQueryValue( url, "requestURL", encodeURIComponent(siteAddress) );
				windowOpen( url, "sub-content" );
			}
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRBillingReport?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function releaseField( obj ) {
			var fieldName = obj.name;
			if( !obj.value ) {
				var prefix = "frmCond.condition";
				var nameObj = eval( prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length - 4) + "Name" );
				nameObj.value = "";
				Field.release( nameObj );
			}
		}

		Select.setChained = function setChained( chainTargetObj, reqObj, chainEventSource ) {
			var url = "<%=systemConfig.getClassURL()%>/DPRBillingReport?mode=codename";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );

			url = replaceQueryValue( url, "reqObj", reqObj );
			var fieldName = chainEventSource.target.name;
			var fieldValue = chainEventSource.target.value;
			if( fieldName ) {
				if( fieldValue ) {
					url = replaceQueryValue( url, fieldName, fieldValue );
					$.ajax({url: url,
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

<body class='content'>
	<%@ include file="include_pub_list.inc"%>
	<%@ include file="include_pub_calendar.inc"%>
	<%@ include file="include_rbm_bodyheader.inc"%>

	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list' />
		<input type='hidden' id='isDeleteAll' name='isDeleteAll'/>

		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc"%>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="compositeOrderNumber" descriptionKey="FIELD_DPR_ORDER_ORDERNUMBER"/></div>
						<div class='field'><mtl:text id="condition" key="compositeOrderNumber"/></div>
					</div>
					<div class='cell'>
					<% if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) { %>
						<div class='field-title'><mtl:title key="orderType" descriptionKey="FIELD_DPR_ORDER_ORDERTYPE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="orderType" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasBlank="true"
									prefixKey="DPR_ORDER_ORDERTYPE_" codeValues="NO,DA" searchable="false"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORDERTYPE"/>
						</div>
					<% } %>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="startOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_STARTDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="startOrderDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="endOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_ENDDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="endOrderDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"
									modified="readConditionReq(\"SHIP\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="shipPartyCode" descriptionKey="FIELD_DPR_SHIPPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
									listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>
						</div>
					</div>
				</div>
			</div>
			<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				$( function() {
					$("input[type=text].date").blur( function() {
						setTimeout( function() {
							if( Field.checkMultiMandatory([frmCond.startOrderDate, frmCond.endOrderDate], false) ) {
								Field.checkDateRange( frmCond.startOrderDate, frmCond.endOrderDate );
							}
						}, 500 );
					});
				});

				function checkSearchCond() {
					if( !Field.checkMultiMandatory([frmCond.startOrderDate, frmCond.endOrderDate], false) ) return false;
					if( !Field.checkDateRange(frmCond.startOrderDate, frmCond.endOrderDate) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRBillingReport?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );

				if( property.containsKey( "listmsg" ) )
					listwriter.print( out, property.getProperty( "listmsg" ) );
				else
					listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<% if( listwriter.containsData() ) { %> %>
						<mtl:button type="delete"/>
						<!-- 데이터 전체 삭제버튼 -->
						<%-- if( sessionMng.isSystemAdmin() ) { %>
							<mtl:button type="delete" onClick="JavaScript:deleteAllReq();" messageKey="jsp.BTN_DELETE_ALL"/>
						<% } --%>
					<% } %>
					<mtl:button type="upload"/>
					<mtl:button type="download" onClick="JavaScript:downTemplateReq()" messageKey="jsp.BTN_DOWNLOAD_TEMPLATE"/>
				<% } %>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>

		</mtl:contentGroup>
		<%@ include file="include_dpr_tail.inc"%>
	</mtl:form>
</body>
</mtl:html>
