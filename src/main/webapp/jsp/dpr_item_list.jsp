<%--
	File Name:	dpr_item_list.jsp
	Version:	2.2.4

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.5	delete 기능 추가
	jbaek		2019/07/30		2.2.4	conditionItemName 조건 수정.
	hankalam	2017/02/28		2.2.3	ordall Type(Selling SKU vs Plant SKU) 조건 추가
	hankalam	2016/09/30		2.2.2	initSearchCond(): html 출력부분 toStringString() 추가
	jbaek		2013/11/30		2.2.1	Material Status Auto Update 기능 개발
	lsinji		2008/09/26		2.2.0	create
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
		var categories = new Array();
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

		function changeProductHierarchy( level, obj ) {
			var options = $.extend( {}, selectmenuOptions, { width: "auto" } );
			var fieldName = obj.name;
			var pos = fieldName.indexOf( "_" );
			var prefixKey = fieldName.substring( 0, pos );

			for( var i = level + 1; i <= 6; i++ ) {
				var hierarchyObj = $("select[name=" + prefixKey + "_" + i)[0];
				initProductHierarchy( hierarchyObj );
				Styles.changeDisplay( hierarchyObj, "" );
			}

			var value = obj.value;
			if( !$(obj).singleSelectmenu("instance") ) {
				$(obj).singleSelectmenu( options );
				$(obj).on( 'singleselectmenuselect', function(event, ui) { changeProductHierarchy(level, obj); } );
			} else {
				$(obj).singleSelectmenu( "refresh" );
			}
			if( !obj.value ) return;

			var lowerObj = $("select[name=" + prefixKey + "_" + (level + 1))[0];
			makeProductHierachy( level + 1, obj.value, null, lowerObj );
			if( typeof lowerObj != "undefined" ) {
				if( !$(lowerObj).singleSelectmenu("instance") ) {
					$(lowerObj).singleSelectmenu( options );
					$(lowerObj).on( 'singleselectmenuselect', function(event, ui) { changeProductHierarchy(level + 1, lowerObj); } );
				} else {
					$(lowerObj).singleSelectmenu( "refresh" );
				}
//				Styles.changeDisplay( lowerObj, "Y" );
			}
		}

	<% if( "ordall".equals((String)condition.get("btype")) ) { %>
		function deleteReq() {
			var btype = "<mtl:value id="property" key="btype"/>";
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );
			requestDelete( url, frmMain.listcheckbox, "partyCode,shipPartyCode,itemCode" );
		}
	<% } %>

		function downloadReq( anchorObj ) {
			var btype = "<mtl:value id="property" key="btype"/>";
			var url = getLocationURL( "url" );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			if( btype == "ordall" ) {
				url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
				requestDownload( url, frmMain.listcheckbox, "partyCode,shipPartyCode,itemCode", anchorObj );
			} else {
				url = replaceQueryValue( url, "mode", "idown" );
				windowOpen( url + "&wintype=sub", "sub-content" );
			}
		}

		function modifyMaterialStatusReq( itemCode ) {
			var selected = CheckBox.getValues( frmMain.listcheckbox );
			if( selected == null || selected.length > 1 ) {
				var messages = { "header" : "<mtl:message key="MSG_CHOOSE_ONLY_ONE" encodeScript="true"/>" };
				customPopup.alert( messages );
					return;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=imodstatus";

			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributonChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );

			requestOne( url, frmMain.listcheckbox, "itemCode", itemCode, false );
		}

		function getCodeLevel( code ) {
			if( code.length() == 1 )
				return 1;
			else if( code.length() == 2 )
				return 2;
			else if( code.length() == 6 )
				return 3;
			else if( code.length() == 10 )
				return 4;
			else if( code.length() == 14 )
				return 5;
			else if( code.length() == 18 )
				return 6;
		}

		function initProductHierarchy( obj ) {
			for( var i = 1; i < obj.length; i++ ) {
				obj.remove( i );
			}
			if( $(obj).singleSelectmenu("instance") ) {
				$(obj).singleSelectmenu( "destroy" );
			}
			//Styles.changeDisplay( obj, "" );
			//Field.modified( obj );
		}

		function infoReq( itemCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=info&wintype=sub";

			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "itemCode", encodeURIComponent(itemCode) );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributonChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );

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

		function makeProductHierachy( level, parentHierarchyCode, hierarchyCode, obj ) {
			if( !obj || typeof obj == "undefined" ) {
				obj = eval( "frmCond.productHierarchyCode_" + level );
				if( !obj || typeof obj == "undefined" ) return;
			}

			var idx = 0;
			while( true ) {
				if( categories == null || idx >= categories.length ) break;

				var category = categories[ idx++ ];
				var classCode = category[0];
				if( classCode == level ) {
					var _parentHierarchyCode = category[1];
					if( classCode == 1 || parentHierarchyCode == _parentHierarchyCode ) {
						var element = document.createElement( "OPTION" );
						element.value = category[2];
						element.text = category[3];
						if( hierarchyCode == category[2] )
							element.selected = "true";

						obj.add( element );
					}
				} else if( classCode > level ) {
					break;
				}
			}
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

		function orderItemLinkMenuReqClick( menu ) {
			if( menu == "INFO" )
				orderItemInfo( linkmenu.params[0], linkmenu.params[1] );
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
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list'/>
		<mtl:hidden id="property" key="btype"/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div id='messagebar'></div>
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
					<mtl:ifvalue id="property" key="btype" value="ordall">
						<div class='field-title'><mtl:title key="plantInd" descriptionKey="jsp.dpr_item_list.FIELD_DPR_ORDERITEM_PLANTIND"/></div>
						<div class='field'>
							<mtl:select id="condition" key="plantInd" prefixKey="jsp.dpr_item_list.FIELD_DPR_ORDERITEM_PLANTIND_" codeValues="Y,N"
									hasBlank="true" nullValueKey="jsp.dpr_item_list.FIELD_DPR_ORDERITEM_PLANTIND_" />
						</div>
					</mtl:ifvalue>
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
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD;SHIP\");"/>
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
									listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
									modified="readConditionReq(\"SG;SOLD;SHIP\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="groupCode" descriptionKey="FIELD_DPR_PARTY_SALESGROUP_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									listId="groups" listCodeKey="groupCode" listNameFormat="$S{[:groupCode;$S{] :groupName}}"
									modified="readConditionReq(\"SOLD;SHIP\");"/>
						</div>
					</div>
				</div>

				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
						<mtl:ifvalue id="property" key="btype" notValue="ordall">
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
						</mtl:ifvalue>
						<mtl:ifvalue id="property" key="btype" value="ordall">
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"
									modified="readConditionReq(\"SHIP\");"/>
						</mtl:ifvalue>
						</div>
					</div>
					<div class='cell'>
						<mtl:ifvalue id="property" key="btype" value="ordall">
						<div class='field-title'><mtl:title key="shipPartyCode" descriptionKey="FIELD_DPR_SHIPPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
									listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>
						</div>
						</mtl:ifvalue>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
			</div>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="categoryCode" descriptionKey="FIELD_DPR_PRODUCT_CATEGORY_NAME"/></div>
						<div class='field'>
							<select name='productHierarchyCode_1' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 1, this );'>
							<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL1"/></option>
							</select>
							<select name='productHierarchyCode_2' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 2, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL2"/></option>
							</select>
							<select name='productHierarchyCode_3' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 3, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL3"/></option>
							</select>
							<select name='productHierarchyCode_4' class='content_o '
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 4, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL4"/></option>
							</select>
							<select name='productHierarchyCode_5' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 5, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL5"/></option>
							</select>
							<select name='productHierarchyCode_6' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 6, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL6"/></option>
							</select>
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
				var btype = frmCond.btype.value;

				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;

					if( frmCond.btype.value != "ordall" )
						frmCond.btype.value = ( frmCond.partyCode.value || frmCond.officeCode.value || frmCond.groupCode ? "ord" : "itm" );

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=rtp";
					url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
					readPartyAttributeReq( url, type );
				}

				function initSearchCond() {
					var parameterHierarchyCodes = new Array();
					<%
						int idx = 0;
						if( categories != null ) {
							for( Map<String, Object> map : categories ) {
								String classCode = (String)map.get( "classCode" );
								String code = (String)map.get( "code" );
								String parentCode = (String)map.get( "parentCode" );
								String name = (String)map.get( "name" );

								out.print( "categories["+ idx++ + "] = new Array(\"" + classCode + "\", \"" + parentCode + "\""
										+ ", \"" + code + "\", \"" + name + "\" );" );

								out.println();
							}
						}

						idx = 0;
						for( int i = 0; i < 6; i++ ) {
							String key = "productHierarchyCode_" + (i+1);
							if( condition.containsKey(key) )
								out.print( "parameterHierarchyCodes[" + i + "] = \"" + HtmlUtility.toScriptString(condition.get(key)) + "\";" );
							else
								out.print( "parameterHierarchyCodes[" + i + "] = \"\";" );

							out.println();
						}
					%>

					var parentHierarchyCode = null;
					for( var i = 0; i < 6; i++ ) {
						var level = i + 1;

						var hierarchyObj = $("select[name=productHierarchyCode_" + level)[0];
						if( i == 0 ) {
							makeProductHierachy( level, parentHierarchyCode, parameterHierarchyCodes[i], hierarchyObj );
							changeProductHierarchy( level, hierarchyObj );
						} else if( parameterHierarchyCodes[i] ) {
							for( var j = 0; j < hierarchyObj.length; j++ ) {
								if( hierarchyObj[j].value == parameterHierarchyCodes[i] ) {
									hierarchyObj[j].selected = "true";
									hierarchyObj.value = hierarchyObj[j].value;

									break;
								}
							}

							changeProductHierarchy( level, hierarchyObj );
						}

						parentHierarchyCode = parameterHierarchyCodes[i];
					}
				}
				attachWindowEvent( "load", initSearchCond );
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				if( property.containsKey("listmsg") )
					listwriter.print( out, property.getProperty("listmsg") );
				else
					listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="download"/>
					<% if( htmlpage.hasManageAuth() ) { %>
						<% if( "itm".equals((String)condition.get("btype")) ) { %>
							<mtl:button type="button" onClick="JavaScript: modifyMaterialStatusReq();" messageKey="jsp.BTN_SETTING"/>
						<% } else { %>
							<mtl:button type="delete"/>
						<% } %>
					<% } %>
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
