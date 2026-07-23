<%--
	File Name:	dpr_moqitemcfg_list.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2026/03/31		2.2.2	basisValue 필드 삭제
	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
	jbaek		2019/05/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8' %>
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
	com.irt.data.cols.ColumnList columnList = (com.irt.data.cols.ColumnList)pageContext.findAttribute("columnList");

	String dateValueType = (String)condition.get("dateValueType");
	if( dateValueType == null )
		dateValueType = "M";

	String primaryKeysCsv = com.irt.util.StringUtil.strJoin(columnList.getPrimaryFieldKeys(), ",");
// 	List<Map<String, Object>> categories = (List<Map<String, Object>>)pageContext.findAttribute( "categories" );
	List<Map<String, Object>> items = (List<Map<String, Object>>)pageContext.findAttribute( "items" );
	org.json.simple.JSONArray itemArray = null;
	if( items != null ) {
		itemArray = com.irt.util.Utility.convertListToJson( items );
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;
		$(function() {
			changeDateValueType( frmCond.dateValueType, "<%= dateValueType%>" );

			var items = <%= itemArray %>;
			var codeItems = $.map( items, function(item) {
				return {
					label: item.itemCode + "|" + item.itemName,
					itemCode: item.itemCode,
					itemName: item.itemName
				};
			});

			var upcItems = $.map( items, function(item) {
				return {
					label: item.itemConsumerEANCode + "|" + item.itemName,
					itemCode: item.itemCode,
					itemName: item.itemName,
					itemConsumerEANCode: item.itemConsumerEANCode
				};
			});

			if( $("input[name=itemCode]").length ) {
				$("input[name=itemCode]").autocomplete( {
					minLength: 2,
					source: codeItems,
					focus: function( event,ui ) { return false; },
					select: function( event, ui ) {
						$("input[name=itemCode]").val( ui.item.itemCode );
						return false;
					},
				}).autocomplete("instance")._renderItem=function( ul, item ) {
					var disp = "<div>("+ item.itemCode + ") " + item.itemName+ "</div>"
					return $("<li>").append( disp ).appendTo( ul );
				};
			}

			if( $("input[name=conditionItemName]").length ) {
				$("input[name=conditionItemName]").autocomplete( {
					minLength: 2,
					source: codeItems,
					focus: function( event,ui ) { return false; },
					select: function( event, ui ) {
						$("input[name=conditionItemName]").val( ui.item.itemName );
						return false;
					},
				}).autocomplete("instance")._renderItem=function( ul, item ) {
					var disp = "<div>("+ item.itemCode + ") " + item.itemName+ "</div>"
					return $("<li>").append( disp ).appendTo( ul );
				};
			}

			if( $("input[name=itemConsumerEANCode]").length ) {
				$("input[name=itemConsumerEANCode]").autocomplete( {
					minLength: 2,
					source: upcItems,
					focus: function( event,ui ) { return false; },
					select: function( event, ui ) {
						$("input[name=itemConsumerEANCode]").val( ui.item.itemConsumerEANCode );
						return false;
					},
				}).autocomplete("instance")._renderItem=function( ul, item ) {
					var disp ="<div>("+ item.itemCode + ") " + item.itemName+ " (" + item.itemConsumerEANCode + ")" + "</div>"
					return $("<li>").append( disp ).appendTo( ul );
				};
			}
		});

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode" );
		}

		function infoReq( organizationCode, distributionChannelCode, officeCode, groupCode, partyCode, dealCode ) {
			var values = organizationCode + ";" + distributionChannelCode + ";" + officeCode + ";" + groupCode + ";" + partyCode + ";" + dealCode;
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode", values );
		}

		function modifyReq( organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode,dateValue,dateValueType ) {
			if( itemCode ) {
				var values = organizationCode+";"+(distributionChannelCode?distributionChannelCode:"0")+";"+(officeCode?officeCode:"0")+";"+(groupCode?groupCode:"0")+";"+(partyCode?partyCode:"0")+";"+itemCode;
				var url = "<%= systemConfig.getClassURL() %>/DPRMoqItemCfg";
				if( dateValueType )
					url = replaceQueryValue( url, "dateValueType", dateValueType );
				requestModify( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode", values );

				return;
			}

			var url = getLocationURL("url");
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function downloadReq() {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode" );
		}

		function uploadReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function linkMenuReq( itemCode ) {
			if( !linkmenu ) {
				var idx = 0;
				var menu = new Array;

				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_DPR_MOQITEMCFG_MOD" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("CFG_MOD");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "CFG_MOD" )
				linkMoqItemConfigModifyReq( linkmenu.params );
// 			else if( menu == "CFGRLT_LIST" )
// 				linkPackdealCfgRltListReq( linkmenu.params[0] );
// 			else if( menu == "CFG_LIST" )
// 				linkPackdealCfgListReq( linkmenu.params[0] );
		}

		function linkMoqItemConfigModifyReq( p ) {
			modifyReq( p[0],p[1],p[2],p[3],p[4],p[5],p[6],p[7] );
		}

		function partyInfoReq( partyCode, linkType ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=info";

			url = replaceQueryValue( url, "partyCode", partyCode );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "displayLinkType", linkType );

			windowOpen( url + "&wintype=sub", "sub-content" );
		}

		function partyLinkMenuReq( partyCode, linkType ) {
			var menu = new Array;

			menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_PARTY_INFO" encodeScript="true"/>'
					, 'new', 'JavaScript:partyLinkMenuReqClick("INFO");' );

			linkmenu = createLinkMenu( menu );
			linkmenu.show();
			linkmenu.params = partyLinkMenuReq.arguments;
		}

		function partyLinkMenuReqClick( menu ) {
			if( menu == "INFO" )
				partyInfoReq( linkmenu.params[0], linkmenu.params[1] );
		}

		function changeDateValueType( el, type ) {
			if( el ) {
				if( type )
					el.value = type;

				if( el.value == "D" ) {
					toggleDisabled( true, frmCond.dateValueMonth );
					$("#dateValueM").hide();
					toggleDisabled( false, frmCond.dateValue );
					$(frmCond.dateValue).show();
				} else if ( el.value == "M") {
					toggleDisabled( true, frmCond.dateValue );
					$(frmCond.dateValue).hide();
					toggleDisabled( false, frmCond.dateValueMonth );
					$("#dateValueM").show();
				}
			}
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list'/>
		<mtl:hidden id="property" key="btype"/>
		<mtl:hidden id="property" key="mngtype"/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<mtl:ifvalue id="property" key="mngtype" value="moqrlt">
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="dateValueType" descriptionKey="FIELD_DATEVALUE_TYPE" mandatory="true"/></div>
						<div class='field'>
							<mtl:select id="condition" key="dateValueType" prefixKey="DATEVALUE_TYPE_" codeValues="M,D"
									modified="changeDateValueType(this);" mandatory="true"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="dateValue" descriptionKey="FIELD_DATEVALUE" mandatory="true"/></div>
						<div class='field'>
							<span id='dateValueM'>
								<mtl:select id="condition" key="dateValueMonth" mandatory="true"
										listId="periods" listCodeKey="dateValue" listNameFormat="$S{[:dateValue;$S{] :startDate};${:endDate}}"
										defaultValue="<%=htmlpage.getProperty().getProperty(\"currUniMonth\")%>"/>
							</span>
							<mtl:date id="condition" key="dateValue"/>
						</div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
				</mtl:ifvalue>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="brandCode" descriptionKey="FIELD_DPR_BRAND_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="brandCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
								hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_BRANDCODE"
								hasBlank="true" listId="brands" listCodeKey="brandCode" listNameFormat="$S{[:brandCode;] $S{:brandName}}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="conditionItemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
						<div class='field'><mtl:text id="condition" key="conditionItemName"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemConsumerEANCode" descriptionKey="jsp.FIELD_DPR_UPC_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemConsumerEANCode"/></div>
					</div>
				</div>

				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="distributionChannelCode" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="distributionChannels" listCodeKey="distributionChannelCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
									listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO;SG;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_PARTY_SALESOFFICE_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
									listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
									modified="readConditionReq(\"SG;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="groupCode" descriptionKey="FIELD_DPR_PARTY_SALESGROUP_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									listId="groups" listCodeKey="groupCode" listNameFormat="$S{[:groupCode;$S{] :groupName}}"
									modified="readConditionReq(\"SOLD\");"/>
						</div>
					</div>
				</div>

				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"
									modified="readConditionReq();"/>
						</div>
					</div>

				<% if( sessionMng.isSystemAdmin() ) { %>
					<div class='cell'>
						<div class='field-title'><mtl:title key="startOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_STARTDATE"/></div>
						<div class='field'><mtl:date id="condition" key="startOrderDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="endOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_ENDDATE"/></div>
						<div class='field'><mtl:date id="condition" key="endOrderDate"/></div>
					</div>
				<% } else { %>
					<div class='cell'></div>
					<div class='cell'></div>
				<% } %>
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
				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRMoqItemCfg?mode=rtp";
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

				if( htmlpage.hasManageAuth() )
					listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_CHECK );

				if( property.containsKey("listmsg") )
					listwriter.print( out, property.getProperty("listmsg") );
				else
					listwriter.print( out );

			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:ifvalue id="property" key="mngtype" value="moqcfg">
						<mtl:button type="regist"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
						<mtl:button type="upload"/>
					</mtl:ifvalue>
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
		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
