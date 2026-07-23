<%--
	File Name:	dpr_orderreport_list.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.3	신규 UI/UX 적용
	hankalam	2020/06/30		2.2.2	orderType 항목 추가
	jbaek		2018/09/30		2.2.1	search 버튼을 기본값으로 simple search가 적용되도록 변경.
	jbaek		2017/07/30		2.2.0	create
--%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8'%>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map"%>
<%@ taglib uri="/mtltaglib" prefix="mtl"%>
<%
	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
%>
<%
	java.util.Map<String, Object> condition = (java.util.Map<String, Object>)request.getAttribute("condition");
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
	String organizationCode = (String)condition.get("organizationCode");
	List<Map<String, Object>> items = (List<Map<String, Object>>)pageContext.findAttribute( "items" );
	org.json.simple.JSONArray itemArray = null;
	if( items != null ) {
		itemArray = com.irt.util.Utility.convertListToJson( items );
	}
%>
<mtl:html errorPage="error.jsp">
<head>

<%@ include file="include_rbm_header.inc"%>

	<!-- <link rel='stylesheet' type='text/css' href='style/buttons.css'> -->

	<style tyle='text/css'>
		span.info-msg {
			color: blue;
			font-size: small;
			font-weight: bold;
		}
	</style>

	<script type='text/javascript' id='flexlistwriter'>
		/***********************************************************************************************************************
		FlexListWriter
		***********************************************************************************************************************/
		function FlexListWriter() {}

		FlexListWriter.adjustColumnWidth = function( colIndex, colWidth ) {
			var isDesc = $(".list_content_data colgroup").find("col:nth-child("+ (colIndex+1) +")" ).hasClass("description");
			if( !isDesc && colWidth > 0 ) {
				$(".list_content_data colgroup").find("col:nth-child("+ (colIndex+1) +")" ).width(colWidth);
				$(".list_content_header colgroup").find("col:nth-child("+ (colIndex+1) +")" ).width(colWidth);
				$(".list_content_header tbody tr").find("th:nth-child("+ (colIndex+1) +")" ).css("word-break","break-word");
			}
		}

		FlexListWriter.getColumnWidthRate = function() {
			var tArray = new Array();
			var dArray = new Array();

			var titleLineWidth = Number($(".list_flex_data_all").attr("data-column-title-line-width"));
			var dataLineWidth = Number($(".list_flex_data_all").attr("data-column-data-line-max-width"));

			$(".list_flex_data span").each(function(idx,obj) {
				if( !($(obj).hasClass("numbering") || $(obj).hasClass("checkboxing")) ) {
					var colIndex = idx+1;

					var titleColWidth = Number($(obj).attr("data-column-title-width"));
					var dataColWidth = Number($(obj).attr("data-column-data-max-width"));

					tArray.push(titleColWidth/titleLineWidth);
					dArray.push(dataColWidth/dataLineWidth);
				}
			});

			return { dataColumnWidthRate: dArray, titleColumnWidthRate: tArray };
		}

		FlexListWriter.recalcColumnWidth = function( tableWidth, rate ) {

		}

		FlexListWriter.getColumnWidthSum = function( tableWidth ) {
			var sum = 0;
			var cw = FlexListWriter.getColumnWidthArray(tableWidth);
			if( cw ) {
				for( var i =0; i<cw.length; i++ ){
					sum += cw[i];
				}
			}
			return sum;
		}

		FlexListWriter.getColumnWidthArray = function( tableWidth ) {
			if( !tableWidth ) {
				tableWidth = Number($("div.list_content").width());
			}
			if( tableWidth < 760 ) { tableWidth = 760; }// minimum table width

			var columnWidthRateArr = FlexListWriter.getColumnWidthRate().dataColumnWidthRate;
			var arr = new Array();
			var titleAdjustArray = FlexListWriter.getAdjustTitleArray();

			var dataLineWidth = Number($(".list_flex_data_all").attr("data-column-data-line-max-width"));

			$.each(columnWidthRateArr, function(idx, obj) {
				var isDesc = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")" ).hasClass("description");
				var width = 100;//minium column width
				if( idx == 0 ) {
					var numOrChkboxWidth = $(".list_content_data colgroup").find("col:nth-child(1)").width();
					width = numOrChkboxWidth;
				} else if( idx > 0 ) {
					if( isDesc ) {
						width = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")").width();
					} else {
						if( titleAdjustArray[idx] > 0 ) {
							width = titleAdjustArray[idx] / dataLineWidth * tableWidth;
						} else {
							width = Number(obj) * tableWidth;
						}
					}
				}
				arr.push(width);
			});
			return arr;
		}

		FlexListWriter.getTitleWidths = function() {
			var tArray = new Array();
			$(".list_flex_data span").each(function(idx,obj) {
				if( !($(obj).hasClass("numbering") || $(obj).hasClass("checkboxing")) ) {
					var colIndex = idx+1;

					var titleColWidth = Number($(obj).attr("data-column-title-width"));

					tArray.push(titleColWidth);
				}
			});
			return tArray;
		}
		FlexListWriter.getDataWidths = function() {
			var dArray = new Array();
			$(".list_flex_data span").each(function(idx,obj) {
				if( !($(obj).hasClass("numbering") || $(obj).hasClass("checkboxing")) ) {
					var colIndex = idx+1;

					var dataColWidth = Number($(obj).attr("data-column-data-max-width"));

					dArray.push(dataColWidth);
				}
			});
			return dArray;
		}

		FlexListWriter.getAdjustTitleArray = function() {
			var dArray = FlexListWriter.getDataWidths();
			var tArray = FlexListWriter.getTitleWidths();
			var needAdjustArray = new Array();
			$.each(dArray, function(idx,obj){
				var resizeWidth = -1;
				var dw = Number(obj);
				var tw = Number(tArray[idx]);
				if( idx > 0 ) {
					if( dw <= 3 && tw <= 5 ) {// when data width is too small
						resizeWidth = tw+1;
					} else if( dw < 5 && (dw*3) <= tw ) {
						resizeWidth = tw/2;
					}
				}
				needAdjustArray.push(resizeWidth);
			});
			return needAdjustArray;
		}


		FlexListWriter.getTitleWidthArray = function( tableWidth ) {
			var columnWidthRateArr = FlexListWriter.getColumnWidthRate().titleColumnWidthRate;
			var arr = new Array();
			$.each(columnWidthRateArr, function(idx, obj) {
				var isDesc = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")" ).hasClass("description");
				if(idx > 0) {
					var width = 100;
					if( isDesc ) {
						width = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")").width();
					} else {
						var num = Number(obj);
						width = num * tableWidth;
					}

					arr.push(width);
				}
			});
			return arr;
		}

		FlexListWriter.getWidthForColumns = function() {
			var numOrChkboxWidth = $(".list_content_data colgroup").find("col:nth-child(1)").width();
			var tableWidth = Number($(".list_content_data").width()) - numOrChkboxWidth;//
			return tableWidth;
		}

		FlexListWriter.adjustColumnWidthAll = function( columnWidthRateArr ) {
			$.each(FlexListWriter.getColumnWidthArray(), function(idx,obj){
				FlexListWriter.adjustColumnWidth(idx, Number(obj));
			});
		}
	</script>

	<%@ include file="include_pub_input.inc"%>
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

			$("input[name=itemCodeConfirmed]").autocomplete( {
				minLength: 2,
				source: codeItems,
				focus: function( event,ui ) { return false; },
				select: function( event, ui ) {
					$("input[name=itemCodeConfirmed]").val( ui.item.itemCode );
					return false;
				},
			}).autocomplete("instance")._renderItem=function( ul, item ) {
				var disp = "<div>("+ item.itemCode + ") " + item.itemName+ "</div>"
				return $("<li>").append( disp ).appendTo( ul );
			};

			$("input[name=consumerEANCode]").autocomplete( {
				minLength: 2,
				source: upcItems,
				focus: function( event,ui ) { return false; },
				select: function( event, ui ) {
					$("input[name=consumerEANCode]").val( ui.item.itemConsumerEANCode );
					return false;
				},
			}).autocomplete("instance")._renderItem=function( ul, item ) {
				var disp ="<div>("+ item.itemCode + ") " + item.itemName+ " (" + item.itemConsumerEANCode + ")" + "</div>"
				return $("<li>").append( disp ).appendTo( ul );
			};
		});

		function bodyLoad() {
			$(frmCond.partyCode).on("change", function(event){
				Select.setChained( frmCond.shipPartyCode, "shipParties", event );
			});

			var pollTimeoutMi = 5;// 5 minutes
			var pollTimeout = pollTimeoutMi * 60 * 1000;// 300000 ms
		}

		function deleteAllReq() {
			resetSearchCond();

			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE_ALL" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					Field.setValue(frmCond.isDeleteAll, 'Y');
					frmCond.submit();
				}
			});
		}

		function downloadReq( type ) {
			if( type == "DPR" ) {
				var url = replaceQueryValue( getLocationURL("url"), "mode", "idown" );
				url = replaceQueryValue( url, "wintype", "sub" );
				windowOpen( url, "sub-content" );
			}else {
				var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
				customPopup.confirm( messages, function(res) {
					if( res ) {
						var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
						url = replaceQueryValue( url, "fileType", "XLX" );
						windowOpen( url );
					}
				});
			}
		}

		function openExternalSiteLink( siteAddress ) {
			if( siteAddress ) {
				var url = "<%=systemConfig.getClassURL()%>/DPRSiteLink?mode=wcf";
				url = replaceQueryValue( url, "locale", "<%=htmlpage.getLocale()%>" );
				url = replaceQueryValue( url, "requestURL", encodeURIComponent(siteAddress) );
				windowOpen( url + "&wintype=sub", "sub-content" );
			}
		}

		function uploadReq() {
			var url = "<%=systemConfig.getClassURL()%>/DPROrderReport?mode=iup&wintype=sub";
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

		/**
		chainTargetObj should be <select/>
		chainEventSource can be an object that has single instances of "name" and "value"
		*/
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

	function downonlyReq() {
		var url = "<%=systemConfig.getClassURL()%>/DPROrderReport";

		var formDataArray = $(frmCond).serializeArray().filter(function(obj){
			return obj.value !== '';
		});
		var qparam = $.param(formDataArray);
		url = url + "?" + qparam;

		url = replaceQueryValue( url, "mode", "down");

		windowSelfOpen(url, getLocationURL("url"));
	}

	function listonlyReq() {
		var url = "<%=systemConfig.getClassURL()%>/DPROrderReport";

		var formDataArray = $(frmCond).serializeArray().filter(function(obj){
			return obj.value !== '';
		});
		var qparam = $.param(formDataArray);
		url = url + "?" + qparam;

		url = replaceQueryValue( url, "mode", "listonly");

		windowSelfOpen(url, getLocationURL("url"));
	}

	function listSimpleReq() {
		var url = "<%=systemConfig.getClassURL()%>/DPROrderReport";

		var formDataArray = $(frmCond).serializeArray().filter(function(obj){
			return obj.value !== '';
		});
		var qparam = $.param(formDataArray);
		url = url + "?" + qparam;

		url = replaceQueryValue( url, "mode", "list");
		url = replaceQueryValue( url, "submode", "simple");

		windowSelfOpen(url, getLocationURL("url"));
	}

	</script>
</head>
<body class='content'>
	<%@ include file="include_pub_list.inc"%>
	<%@ include file="include_pub_calendar.inc"%>
	<%@ include file="include_rbm_bodyheader.inc"%>

	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list' />
		<input type='hidden' id='isDeleteAll' name='isDeleteAll' />
	<% if( !com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDetailCondition") ) { %>
		<mtl:hidden id="condition" key="organizationCode" />
	<% } %>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc"%>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="compositeOrderNumber" descriptionKey="FIELD_DPR_ORDER_ORDERNUMBER"/></div>
						<div class='field'><mtl:text id="condition" key="compositeOrderNumber"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCodeConfirmed" descriptionKey="FIELD_DPR_ORDERDTL_ITEMCODE_CONFIRMED"/></div>
						<div class='field'><mtl:text id="condition" key="itemCodeConfirmed"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="consumerEANCode" descriptionKey="FIELD_DPR_ORDERDTL_UPC_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="consumerEANCode"/></div>
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
				<% if( com.irt.dpr.Country.isFeature((String)((java.util.Map)request.getAttribute("condition")).get("organizationCode"), "usePackDeal") ) { %>
					<div class='cell'>
						<div class='field-title'><mtl:title key="dealCode" descriptionKey="FIELD_DPR_PACKDEAL"/></div>
						<div class='field field-multiple'>
							<mtl:text id="condition" key="dealCode" placeholder="jsp.MSG_TEXT_PLACEHOLDER_PACKDEALCODE"
									style="width: calc(100% - 142px);"/>
							<mtl:select id="condition" key="packdealInd" nullValueKey="FIELD_DPR_PACKDEAL_DEALIND" hasBlank="true"
									prefixKey="PUB_WHETHER_" codeValues="Y,N" listNameFormat="$f{pure(dealCode)}"
									searchable="false" width="135"/>
						</div>
					</div>
				<% } %>
				<% if( !com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDetailCondition") ) { %>
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
				<% } %>
				</div>
			<% if( com.irt.dpr.Country.isFeature( organizationCode, "useDetailCondition") ) { %>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
						<% if( !"OR".equals(sessionMng.getGroupClass()) ) { %>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD;SHIP\");"/>
						<% } else { %>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SOLD;SHIP\");"/>
						<% } %>
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
								if( !"OR".equals(sessionMng.getGroupClass()) ) {
									out.print( " onclick='JavaScript: readConditionReq(\"SO;SG;SOLD;SHIP\");'" );
								} else {
									out.print( " onclick='JavaScript: readConditionReq(\"SOLD;SHIP\");'" );
								}
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
				<% if( !"OR".equals(sessionMng.getGroupClass()) ) { %>
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
				<% } %>
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
			<% } %>
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
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( frmCond.distributionChannelCode ) {
						if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;
					}

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPROrderReport?mode=rtp";
					readPartyAttributeReq( url, type );
				}

				function generateUUID() {
					var d = new Date().getTime();
					var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
							/[xy]/g, function(c) {
								var r = (d + Math.random() * 16) % 16 | 0;
								d = Math.floor(d / 16);
								return (c == 'x' ? r : (r & 0x3 | 0x8))
										.toString(16);
							});
					return uuid;
				}

				<%if (sessionMng.isSystemAdmin()) {%>
					var fakepoll = function() {
						var pollTimeout = 3000;// overall polling timeout
						console.log( 'pollTimeout: '+ pollTimeout.toString() );
						var initData = {"action": "poll", "pollState": "init", "pollTimeout": pollTimeout.toString(), "isFake": "Y"};
						poll(initData);
					}
				<%}%>

				function Polling() {};
				Polling.pollStates = new Array( "init", "continue", "pollTimeouted", "done", "alert", "error" );
				Polling.showWaiting = function() {
					$('#loadingmessage').show();
				}
				Polling.hideWaiting = function() {
					$('#loadingmessage').hide();
				}
				Polling.logError = function(data) {
					var message = data.message;
					$('#message').append(message);
				}
				Polling.showLogError = function() {
					$('#message').show();
				}

				/**
				generate uniqid and set uniqid
				*/
				Polling.getUniqId = function() {
					var uniqId = Math.random().toString(36).substring(2)
						+ (new Date()).getTime().toString(36);
					Polling.uniqId = uniqId;
					return uniqId;
				}

				Polling.lockElement = function(element) {
					Polling.element = element;
					if( element ) {
						element.disabled = true;
					}
				}

				Polling.checkSearchCond = function() {
					if( !Field.checkDateFormat(frmCond.startOrderDate) ) return false;
					if( !Field.checkDateFormat(frmCond.endOrderDate) ) return false;

					if( Field.getValue(frmCond.startOrderDate).length > 0 && Field.getValue(frmCond.endOrderDate).length > 0
							&& frmCond.startOrderDate.value > frmCond.endOrderDate.value ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_DATESCOPE" encodeScript="true"/>" } );

						return false;
					}

					// auto trim(remove whitespace) for text fields
					var textFields = new Array(
							frmCond.compositeOrderNumber, frmCond.itemCodeConfirmed
							, frmCond.consumerEANCode, frmCond.partyCode
					);
					for( var i=0; i < textFields.length; i++ ) {
						if( textFields[i].value ) {
							textFields[i].value = textFields[i].value.trim();
						}
					}

//					disableBlankInput( frmCond, true );

					return true;
				}

				Polling.unlockElement = function(element) {
					if( element ) {
						element.disabled = false;
					} else {
						if( Polling.element ) {
							Polling.unlockElement(Polling.element);
						}
					}
				}

				var pollDoneCallback = function(data) {
					var url = "<%=systemConfig.getClassURL()%>/DPROrderReport";
					var formDataArray = $(frmCond).serializeArray();
					var qparam = $.param(formDataArray);
					url = url + "?" + qparam;

					windowSelfOpen(url, getLocationURL("url"));
				}

				var pollExtra;
				var pollCallback = function(data, status) {
					Polling.hideWaiting();
					if( data ) {
						if( data.extra ) {
							Polling.extra = data.extra;
						}
						if( data.pollState == 'done' ) {
							pollDoneCallback(data);
						} else if( data.pollState == 'pollTimeouted' ) {// controlled timeout
							customPopup.alert( { "header" : data.message } );
						} else if( data.pollState == 'alert' ) {// controlled exception
							customPopup.alert( { "header" : data.message } );
						} else if( data.pollState == 'error' ) {// serious error
							Polling.logError(data);
							customPopup.alert( { "header" : "<mtl:message key="ERR_INTERNAL_ERROR" encodeScript="true"/>" } );
						} else {//other unmanaged state
							console.log("polling other state::");
							console.log(data);

							Polling.extra = data;
							Polling.unlockElement()
							if( data.getResponseHeader ) {
								if( data.getResponseHeader('content-type') ) {
									if( data.getResponseHeader('content-type').indexOf('text/html') >= 0 ) {
										$("#loadingmessage").replaceWith($(data.responseText).html());
										$("#loadingmessage").show();
									}
								}
							}

							customPopup.alert( { "header" : "<mtl:message key="ERR_INTERNAL_ERROR" encodeScript="true"/>" } );
							//usually session timeout or disconnection, so invoke new reload from server
							window.location.reload(true);// true: reload from server; false: reload in client
							return false;
						}
					} else {
						customPopup.alert( { "header" : "<mtl:message key="ERR_INTERNAL_ERROR" encodeScript="true"/>" } );
						//usually session timeout or disconnection, so invoke new reload from server
						window.location.reload(true);// true: reload from server; false: reload in client
						return false;
					}
					Polling.unlockElement()
					return true;
				}

				var pollOutput;
				function poll(ctlData) {
					var url = "<%=systemConfig.getClassURL()%>/DPROrderReport";
					var _formDataArray = $(frmCond).serializeArray();
					var formDataArray = _formDataArray.filter(function(obj){
						return obj.value !== '';
					});

					var debugSQL = getQueryValue(getLocationURL(), 'debugSQL');
					<% if( sessionMng.isSystemAdmin() ) { %>
						debugSQL = "Y";
					<% } %>
					if( debugSQL ) {
						formDataArray.push({name: 'debugSQL', value: debugSQL });
					}
					console.log( formDataArray );

					if( ctlData ) {
						$.each(ctlData, function(key, value){
							formDataArray.push({name: key, value: value});
						});
					} else {
						return false;
					}

//					var clientTimeout = 2000;// maybe small like 2000 is riggh value if server is good
					var clientTimeout = 10000;// maybe large like 10000 is good if server is slow.
					var pollInterval = 9000;// poll call interval from client to server

					Polling.showWaiting();
					$.ajax({
						url: url,
						type: "GET",
						data: formDataArray,
						dataType: "json",
						success: function(data) {
							if( data ) {
								if( data['pollState'] == 'continue' || data['pollState'] == 'init' ) {
									console.log("polling success "+ data['pollState'] +" from server");
									console.log(data);
									if( pollOutput == 'undefined' ) {
										pollOutput = data.message;
									}

									var pollData = {"action":"poll", "pollState": "continue"};
									if( data.extra ) {
										pollData = $.extend(pollData, {"extra": data.extra});
									}
									if( data.pollStarted ) {
										pollData = $.extend(pollData, {"pollStarted": data.pollStarted.toString()});
									}
									if( data.pollTimeout ) {
										pollData = $.extend(pollData, {"pollTimeout": data.pollTimeout.toString()});
									} else if( ctlData.pollTimeout ) {
										pollData = $.extend(pollData, {"pollTimeout": ctlData.pollTimeout.toString()});
									}
									if( data.pollId ) {
										pollData = $.extend(pollData, {"pollId": data.pollId});
									}
									setTimeout(function() {
										poll(pollData);
									}, pollInterval);
								} else {
									return pollCallback(data);
								}
							} else {
								var pollData = {"action":"poll", "pollState": "init"};
								if( ctlData ) {
									if( ctlData.pollTimeout ) {
										pollData = $.extend(pollData, {"pollTimeout": ctlData.pollTimeout.toString()});
									}
									if( ctlData.pollId ) {
										pollData = $.extend(pollData, {"pollId": ctlData.pollId.toString()});
									}
								}
								setTimeout(function() {
									poll(pollData);
								}, pollInterval);
							}
						},
						error: function(data, errorThrown) {
							if( errorThrown == 'timeout' ) {// clientside $.ajax.timeout ignore and continue

								var pollData = $.extend(ctlData, {"action":"poll", "pollState": "continue"});
								if( data.pollStarted ) {
									pollData = $.extend(pollData, {"pollStarted": data.pollStarted.toString()});
								}
								if( data.pollTimeout ) {
									pollData = $.extend(pollData, {"pollTimeout": data.pollTimeout.toString()});
								} else if( ctlData.pollTimeout ) {
									pollData = $.extend(pollData, {"pollTimeout": ctlData.pollTimeout.toString()});
								}
								if( data.pollId ) {
									pollData = $.extend(pollData, {"pollId": data.pollId});
								}

								setTimeout(function() {
									poll(pollData);
								}, pollInterval);
								console.log("polling error timeout on client"+ " is ignored and continue polling.");
								console.log(data);
							} else {// some error from client or server
								return pollCallback(data);
							}
						},
						timeout: clientTimeout
					});
				}

			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter(request, htmlpage);
				listwriter.setUseFlexDataLine( true );
				listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NUMBER );

				if( property.containsKey("listmsg") )
					listwriter.print(out, property.getProperty("listmsg"));
				else
					listwriter.print(out);
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="download" onClick="JavaScript:downloadReq(\"DPR\");" messageKey="jsp.BTN_DOWNLOAD_ALL"/>
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
