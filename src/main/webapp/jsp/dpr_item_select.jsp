<%--
	File Name:	dpr_item_select.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.0	create
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
	List<Map<String, Object>> categories = (List<Map<String, Object>>)pageContext.findAttribute( "categories" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_select.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var categories = new Array();
		function bodyLoad() {
			if( !Select.selectName ) Select.setSelectName( "item" );

			windowResizeTo( 800 );
			Select.setElementNames( "code", "name" );
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

		function changeGroupList( value ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=cond&datatype=SG";
			if( value == "" )
				value = "null";
			else
				url = replaceQueryValue( url, "officeCode", encodeURIComponent(value) );

			windowOpen( url, "clsName" );
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

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:hidden id="request" key="mode"/>
		<mtl:hidden id="property" key="btype"/>
		<mtl:hidden id="request" key="organizationCode"/>
		<mtl:hidden id="request" key="distributionChannelCode"/>
		<mtl:hidden id="request" key="officeCode"/>
		<mtl:hidden id="request" key="groupCode"/>
		<mtl:hidden id="request" key="partyCode"/>
		<mtl:hidden id="request" key="oitmHierIndex"/>

		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<table class='search_content' cellspacing='0' cellpadding='0'>
				<tr><td>
					<table class='search_line_content' cellspacing='0' cellpadding='0'>
					<tr>
						<td class='search_subject' width='80'><mtl:message key="FIELD_DPR_ITEM_CODE"/></td>
						<td class='search_content'><mtl:text id="condition" key="itemCode" styleClass="length_15"
								modified="JavaScript:Field.modified(this); JavaScript:releaseField(this);"/></td>

						<td class='search_subject' width='50'><mtl:message key="FIELD_DPR_ITEM_MASTER_NAME"/></td>
						<td class='search_content'><mtl:text id="condition" key="conditionItemName" styleClass="length_20"/></td>

						<td class='search_subject' width='50'><mtl:message key="jsp.FIELD_DPR_UPC_CODE"/></td>
						<td class='search_content'><mtl:text id="condition" key="itemConsumerEANCode" styleClass="length_15"/></td>
					</tr>
					</table>

				</td><td class='search_button'>
					<mtl:button type="submit" imageSrc="images/btn_search.gif" styleClass="btn_list"/>
					<mtl:button type="reset" href="JavaScript:resetSearchCond();" styleClass="btn_list"/>
				</td></tr>
			</table>

			<script type='text/javaScript'>
				var btype = frmCond.btype.value;

				function checkSearchCond() {
					if( frmCond.itemCode.value )
						frmCond.conditionItemName.disabled = "true";

					if( frmCond.organizationCode && !Field.checkMandatory(frmCond.organizationCode) ) return false;

					if( !frmCond.oitmHierIndex.value || !(frmCond.oitmHierIndex.value == "1") )
						if( frmCond.distributionChannelCode && !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;

//					if( frmCond.btype.value != "ordall" )
//						frmCond.btype.value = ( (frmCond.partyCode&&frmCond.partyCode.value) || (frmCond.officeCode&&frmCond.officeCode.value) || (frmCond.groupCode&&frmCond.groupCode.value) ? "ord" : "itm" );
				}

				function initSearchCond() {
					if( frmCond.itemCode.value )
						Field.lock( frmCond.conditionItemName );
					if( frmCond.conditionItemName && "<mtl:value id="condition" key="itemName" encodeScript="true"/>" )
						frmCond.conditionItemName.value = "<mtl:value id="condition" key="itemName" encodeScript="true"/>";
				}
				attachWindowEvent( "load", initSearchCond );

				function resetSearchCond() {
					frmCond.reset();
					resetForm( frmCond );
//					changeProductHierarchy( 1, eval("frmCond.productHierarchyCode_1") );
				}

				function readDistributionChannelReq( organizationCode ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=rtp&ctype=dc";

					if( !organizationCode || typeof organizationCode == "undefined" ) {
						if( !Field.checkMandatory(frmCond.organizationCode) ) {
							frmCond.distributionChannelCode.disabled = true;
							if( "ordall" == btype ) {
								frmCond.partyCode.value = "";
								frmCond.shipPartyCode.disabled = true;
							} else {
								frmCond.partyCode.disabled = true;
							}

							return;
						}
					}

					url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
					url = replaceQueryValue( url, "organizationCode", organizationCode );
					url = replaceQueryValue( url, "divisionCode", "<mtl:value id="condition" key="division"/>" );
					url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );

					if( typeof frmCond.organizationCode == "undefined" )
						frmCond.organizationCode.disabled = true;
					if( typeof frmCond.distributionChannelCode == "undefined" )
						frmCond.distributionChannelCode.disabled = true;
					if( "ordall" == btype ) {
						frmCond.partyCode.value = "";
						frmCond.shipPartyCode.disabled = true;
					} else {
						frmCond.partyCode.disabled = true;
					}

					windowOpen( url, "clsName" );
				}

				function readOfficeGroupReq( organizationCode, distributionChannelCode, officeCode ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=rtp&ctype=sg";

					if( !organizationCode || typeof organizationCode == "undefined" ) {
						if( typeof frmCond.organizationCode != "undefined" ) {
							if( !Field.checkMandatory(frmCond.organizationCode) ) {
								frmCond.distributionChannelCode.disabled = true;
								if( "ordall" == btype ) {
									frmCond.partyCode.value = "";
									frmCond.shipPartyCode.disabled = true;
								} else {
									frmCond.partyCode.disabled = true;
								}
								return;
							}

							organizationCode = frmCond.organizationCode.value;
						}
					}

					if( typeof frmCond.distributionChannelCode != "undefined" ) {
						if( !Field.checkMandatory(frmCond.distributionChannelCode) ) {
							if( "ordall" == btype ) {
								frmCond.partyCode.value = "";
								frmCond.shipPartyCode.disabled = true;
							} else {
								frmCond.partyCode.disabled = true;
							}
							return;
						}

						distributionChannelCode = frmCond.distributionChannelCode.value;
					}

					if( !officeCode || typeof officeCode == "undefined" ) {
						if( typeof frmCond.officeCode != "undefined" ) {
							officeCode = frmCond.officeCode.value;
						}
					}

					url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
					url = replaceQueryValue( url, "organizationCode", organizationCode );
					url = replaceQueryValue( url, "distributionChannelCode", distributionChannelCode );
					url = replaceQueryValue( url, "officeCode", officeCode );
					url = replaceQueryValue( url, "divisionCode", "<mtl:value id="condition" key="division"/>" );
					url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );

					if( typeof frmCond.organizationCode == "undefined" )
						frmCond.organizationCode.disabled = true;
					if( typeof frmCond.distributionChannelCode == "undefined" )
						frmCond.distributionChannelCode.disabled = true;
					if( "ordall" == btype ) {
						frmCond.partyCode.value = "";
						frmCond.shipPartyCode.disabled = true;
					} else {
						frmCond.partyCode.disabled = true;
					}

					windowOpen( url, "clsName" );
				}


				function readTradePartnerReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=stp";
					url = replaceQueryValue( url, "type", type );
					if( !Field.checkMandatory(frmCond.organizationCode) ) return;
					if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return;
					url = replaceQueryValue( url, "organizationCode", encodeURIComponent(frmCond.organizationCode.value) );
					url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(frmCond.distributionChannelCode.value) );

					if( "sold" == type ) {
						url = replaceQueryValue( url, "shipPartyCode", encodeURIComponent(frmCond.shipPartyCode.value) );
						//frmCond.partyCode.childNodes[0].innerHTML = "<mtl:message key="jsp.MSG_LOADING"/>";
						//frmCond.partyCode.selectedIndex = 0;
						//frmCond.partyCode.disabled = true;
						//frmCond.shipPartyCode.childNodes[0].innerHTML = "<mtl:message key="jsp.MSG_LOADING"/>";
						//frmCond.shipPartyCode.selectedIndex = 0;
						//frmCond.shipPartyCode.disabled = true;

						windowOpen( url, "clsName" );
					} else if( "ordall" == btype && "ship" == type ) {
						//if( !Field.checkMandatory(frmCond.partyCode) ) return;
						url = replaceQueryValue( url, "partyCode", encodeURIComponent(frmCond.partyCode.value) );

						frmCond.shipPartyCode.childNodes[0].innerHTML = "<mtl:message key="jsp.MSG_LOADING"/>";
						frmCond.shipPartyCode.selectedIndex = 0;
						//frmCond.shipPartyCode.disabled = true;

						windowOpen( url, "clsName" );
					}
				}

				function readTradePartnersReq( organizationCode, distributionChannelCode ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=rtp&ctype=sold";

					if( !organizationCode || typeof organizationCode == "undefined" ) {
						if( typeof frmCond.organizationCode != "undefined" ) {
							if( !Field.checkMandatory(frmCond.organizationCode) ) {
								frmCond.distributionChannelCode.disabled = true;
								if( "ordall" == btype ) {
									frmCond.partyCode.value = "";
									frmCond.shipPartyCode.disabled = true;
								} else {
									frmCond.partyCode.disabled = true;
								}
								return;
							}

							organizationCode = frmCond.organizationCode.value;
						}
					}

					if( !distributionChannelCode || typeof distributionChannelCode == "undefined" ) {
						if( typeof frmCond.distributionChannelCode != "undefined" ) {
							if( !Field.checkMandatory(frmCond.distributionChannelCode) ) {
								if( "ordall" == btype ) {
									frmCond.partyCode.value = "";
									frmCond.shipPartyCode.disabled = true;
								} else {
									frmCond.partyCode.disabled = true;
								}
								return;
							}

							distributionChannelCode = frmCond.distributionChannelCode.value;
						}
					}

					url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
					url = replaceQueryValue( url, "organizationCode", organizationCode );
					url = replaceQueryValue( url, "officeCode", frmCond.officeCode.value );
					url = replaceQueryValue( url, "groupCode", frmCond.groupCode.value );
					url = replaceQueryValue( url, "distributionChannelCode", distributionChannelCode );
					url = replaceQueryValue( url, "divisionCode", "<mtl:value id="condition" key="division"/>" );
					url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );

					if( typeof frmCond.organizationCode == "undefined" )
						frmCond.organizationCode.disabled = true;
					if( typeof frmCond.distributionChannelCode == "undefined" )
						frmCond.distributionChannelCode.disabled = true;
					if( "ordall" == btype ) {
						frmCond.partyCode.value = "";
						frmCond.shipPartyCode.disabled = true;
					} else {
						frmCond.partyCode.disabled = true;
					}
					windowOpen( url, "clsName" );
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
//			else if( menu == "MODSTATUS" )
//				modifyMaterialStatusReq( linkmenu.params[0] );
			else if( menu == "LOWER_LIST" )
				lowerItemReq( linkmenu.params[0] );
		}

		function orderItemLinkMenuReq( partyCode, itemCode ) {
			var menu = new Array;

			menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ITEM_INFO" encodeScript="true"/>'
					, 'new', 'JavaScript:orderItemLinkMenuReqClick("INFO");' );

			linkmenu = createLinkMenu( menu );
			linkmenu.show();
			linkmenu.params = orderItemLinkMenuReq.arguments;
		}

		function orderItemLinkMenuReqClick( menu ) {
			if( menu == "INFO" )
				orderItemInfo( linkmenu.params[0], linkmenu.params[1] );
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
			</script>
		</mtl:contentGroup>
	</mtl:form>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px'>
			<tr><td class='list_content_top' align='left'>
			</td></tr>
			</table>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'></span>
			</td></tr>
			</table>

			<%
				ListWriter listwriter = new com.irt.custom.SelectListWriter( request, htmlpage );
				listwriter.setScrollHeight( 288 );
				listwriter.setPrimaryKeys( new String[] { "itemCode", "itemName" } );
				listwriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<mtl:button type="select" styleClass="btn_list"/>
			<% if( "chk".equals(request.getParameter("attr")) ) { %>
				<mtl:button type="reset" href="JavaScript:Select.reset(frmMain.listcheckbox);" styleClass="btn_list"/>
			<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' align='left' nowrap>
					<select name='filterType'>
						<option value='itemCode' selected><mtl:message key="FIELD_DPR_ITEM_MASTER_CODE"/></option>
						<option value='itemName' selected><mtl:message key="FIELD_DPR_ITEM_MASTER_NAME"/></option>
					</select>

					<input type='text' name='filterValue' class='length_20' onKeyDown='JavaScript:callByKeydown(filterReq);'>
					<a href='JavaScript:filterReq();'><img src='images/lbtn_filter.gif' class='tbtn'></a>
				</td>

				<td class='list_content_bottom' id='index_lst' align='center' width='100%' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
				<td class='list_content_bottom' id='index_btn' align='right' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>
		</mtl:contentGroup>
	</mtl:form>
</body>
</mtl:html>
