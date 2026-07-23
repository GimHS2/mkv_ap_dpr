<%--
	File Name:	dpr_stopitemcfg_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/04/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
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

	String primaryKeysCsv = com.irt.util.StringUtil.strJoin(columnList.getPrimaryFieldKeys(), ",");
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
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

			if( $("input[name=itemName]").length ) {
				$("input[name=itemName]").autocomplete( {
					minLength: 2,
					source: codeItems,
					focus: function( event,ui ) { return false; },
					select: function( event, ui ) {
						$("input[name=itemName]").val( ui.item.itemName );
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

		function infoReq( organizationCode, distributionChannelCode, officeCode, groupCode, partyCode, itemCode ) {
			var values = organizationCode + ";" + distributionChannelCode + ";" + officeCode + ";" + groupCode + ";" + partyCode + ";" + itemCode;
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode", values );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,itemCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
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
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ITEM_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("INFO");' );
//				menu[1] = new Array( '<mtl:message key="jsp.LMENU_DPR_ITEMSTATUS_INPUT" encodeScript="true"/>'
//						, 'new', 'JavaScript:linkMenuReqClick("MODSTATUS");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "INFO" )
				infoReq( linkmenu.params[0] );
			else if( menu == "LOWER_LIST" )
				lowerItemReq( linkmenu.params[0] );
		}

		function lowerItemReq( itemCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=list";

			url = replaceQueryValue( url, "btype", "ord" );
			url = replaceQueryValue( url, "ltype", "itm" );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "itemCode", encodeURIComponent(itemCode) );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributonChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			windowSelfOpen( url, getLocationURL() );
		}

		function orderItemInfo( partyCode, itemCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=info";

			url = replaceQueryValue( url, "btype", "ord" );
			url = replaceQueryValue( url, "partyCode", partyCode );
			url = replaceQueryValue( url, "itemCode", itemCode );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			windowOpen( url + "&wintype=sub", "sub-content" );
		}

		function orderItemLinkMenuReq( partyCode, itemCode ) {
			var menu = new Array;

			menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ITEM_INFO" encodeScript="true"/>'
					, 'new', 'JavaScript:orderItemLinkMenuReqClick("INFO");' );

			linkmenu = createLinkMenu( menu );
			linkmenu.show();
			linkmenu.params = orderItemLinkMenuReq.arguments;
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

		function releaseField( obj ) {
			var fieldName = obj.name;
			if( !obj.value ) {
				var prefix = "frmCond.condition";
				var nameObj = eval( prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length - 4) + "Name" );
				nameObj.value = "";
				Field.release( nameObj );
			}
		}

		function setTradePartner( type, obj, options ) {
			if( options ) {
				obj.disabled = true;

				while( obj.options.length >= 1 )
					obj.remove( obj.options.length - 1 );

				var optionObj;
				if( obj.options.length < 1 ) {
					optionObj = document.createElement( "option" );
					obj.appendChild( optionObj );
				} else
					optionObj = obj.options[0];

				optionObj.value = "";
				if( "sold" == type )
					optionObj.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_SOLD_BLANK"/>";
				else if( "ship" == type )
					optionObj.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_SHIP_BLANK"/>";

				for( var i = 0; i < options.length; i++ ) {
					optionObj = document.createElement( "option" );
					optionObj.value = options[i][0];
					optionObj.innerHTML = options[i][1];
					obj.appendChild( optionObj );
				}
			}
			obj.disabled = false;
		}

		function setTradePartners( type, soldOptions, shipOptions ) {
			if( "sold" == type )
				setTradePartner( type, frmCond.partyCode, soldOptions );
			else if( "ship" == type )
				setTradePartner( type, frmCond.shipPartyCode, shipOptions );
		}

	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list'/>
		<mtl:hidden id="property" key="btype"/>
		<mtl:hidden id="property" key="mngtype"/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
						<div class='field'><mtl:text id="condition" key="itemName"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemConsumerEANCode" descriptionKey="jsp.FIELD_DPR_UPC_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemConsumerEANCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:message key="jsp.dpr_stopitemcfg_list.FIELD_DPR_STOPITEMCFG_STOPIND"/></div>
						<div class='field'>
							<mtl:select id="condition" key="isStopItem" prefixKey="PUB_WHETHER_" codeValues="Y,N" searchable="false"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_STOPITEM_IND"/>
						</div>
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
								out.print( " onclick='JavaScript: readConditionReq(\"SO;SG;SOLD\");'" );
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
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
						</div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
					<div class='cell'></div>
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

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );

				if( htmlpage.hasManageAuth() )
					listWriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_CHECK );
				if( property.containsKey("listmsg") )
					listWriter.print( out, property.getProperty("listmsg") );
				else
					listWriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( "StopItemCfg".equals(htmlpage.getProperty().getProperty("mngtypeName")) ) { %>
					<% if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="regist"/>
						<% if( listWriter.containsData() ) { %>
							<mtl:button type="modify"/>
							<mtl:button type="delete"/>
						<% } %>
						<mtl:button type="upload"/>
					<% } %>
				<% } %>
				<% if( listWriter.containsData() ) { %>
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
