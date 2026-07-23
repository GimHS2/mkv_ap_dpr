<%--
	File Name:	dpr_orderrevise_input.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2023/07/27		2.2.3	발주 상품 체크
	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
	jbaek		2020/10/16		2.2.1	orderItem조회시 shipTo(및 plantCode)가 적용되는 getItemTreeList에서 정보를 가져오도록 변경
	jbaek		2020/06/30		2.2.0	create
	--%>

<%@page import="java.math.BigDecimal"%>
<%@page import="com.irt.tagext.TagUtility"%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr'%>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.*"%>
<%@ taglib uri="/mtltaglib" prefix="mtl"%>
<%
	response.setHeader( "CacheControl", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	String shortageType = property.getProperty( "shortageType" );
	Map headerMap = (Map)request.getAttribute("header");
	if( headerMap == null )
		headerMap = new HashMap();
%>
<head>
	<%@ include file="include_rbm_header.inc"%>
	<script type='text/javascript'>
		$(function() {
			$("#content_list").height( $("#content_input").height() );
			$(".wrapper").height( $("#content_input").outerHeight() + 30 );
		});

		function getOrderKey() {
			return "<mtl:value id="header" key="orderKey"/>";
		}

		function changeType() {
			var type = $("#reviseHelpType").val();
			$("input[name=value_orderQty]").each( function(index, item) {
				if( "ROM" == type ) {
					$(this).val( $("input[name=value_orgOrderQty]").eq(index).val() );
				} else if( "ROD" == type ) {
					if( $("input[name=value_itemCodeConfirmed]").eq(index).val() != "" ) {
						$(this).val( "0" );
					}
				}
			});
		}

		function checkOrderLimit( obj ) {
			var skipLowLimitCheck = <%=com.irt.dpr.Country.isFeature(htmlpage.getProperty().getProperty("savedOrgCd"), "useRevOrd")%>;
			if( Field.isArray(obj) ) {
				for(var i = 0; i < obj.length; i++) {
					if( String(frmMain.value_itemCode[i].value) === "" ) continue;
					if( frmMain.value_childLineNumber[i].value != 0 ) continue;
					if( frmMain.value_uom[i].value == "PC" && frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
						if( skipLowLimitCheck )
							return true;
						else
							customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
					} else if( frmMain.value_uom[i].value != "PC" && (frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %>
																   || frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %>) ) {
						if( frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %> ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
						} else if( frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
							if( skipLowLimitCheck )
								return true;
							else
								customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
						}
						frmMain.value_orderQty[i].select();
						frmMain.value_orderQty[i].focus();
						return false;
					}
				}
			} else if( String(frmMain.value_itemCode.value) === "" ) {
				//skip
			} else if( frmMain.value_orderQty && frmMain.value_childLineNumber.value == 0 ) {
				if( !skipLowLimitCheck && frmMain.value_uom.value == "PC" && frmMain.value_orderQty.value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
					customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
				} else if( frmMain.value_uom.value != "PC" && (frmMain.value_orderQty.value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %>
															|| frmMain.value_orderQty.value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %>) ) {
					if( frmMain.value_orderQty.value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %> ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
					} else if( frmMain.value_orderQty.value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
					}
					frmMain.value_orderQty.select();
					frmMain.value_orderQty.focus();
					return false;
				}
			}
			return true;
		}

		function checkDetailOrderValues() {
			var orderQtyObj;
			var childLineNumber;
			var detalStatus;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				orderQtyObj = frmMain.value_orderQty;
				childLineNumber = frmMain.value_childLineNumber;
				detailStatus = frmMain.value_detailStatus;
				itemCode = frmMain.value_itemCode;
			} else {
				orderQtyObj = new Array( frmMain.value_orderQty );
				childLineNumber = new Array( frmMain.value_childLineNumber );
				detailStatus = new Array( frmMain.value_detailStatus );
				itemCode = new Array( frmMain.value_itemCode );
			}
			for( var i = 0; i < orderQtyObj.length; i++ ) {
				if( String(itemCode[i].value) == "" )
					continue;
				if( childLineNumber[i].value != 0 )
					continue;
				if( orderQtyObj[i].value.length == 0 || (orderQtyObj[i].value.length > 0 && orderQtyObj[i].value.indexOf(' ') >= 0) ) {
					focusForm( frmMain, orderQtyObj[i] );
					customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_NUMBER"/>" } );
					return false;
				}
				if( !isFinite(orderQtyObj[i].value) || orderQtyObj[i].value < 0 ) {
					focusForm( frmMain, orderQtyObj[i] );
					return false;
				} else if( orderQtyObj[i].value.length > 0 && orderQtyObj[i].value.indexOf(' ') > 0 ) {
					focusForm( frmMain, orderQtyObj[i] );
					return false;
				}
			}

			return true;
		}

		function disableUnfilledLines( toDisable ) {
			var obj_itemCode = toArray( frmMain.value_itemCode );

			for( var i = 0; i < frmMain.listcheckbox.length; i++ ) {
				if( !obj_itemCode[i].value ) {
					frmMain.listcheckbox[i].disabled = true;
					$(frmMain.listcheckbox[i]).closest("tr").each(function(ii, oo){
						$(this).find("[name^='value_']").attr("disabled", "disabled");
					});
				}
			}
		}

		function enableUnfilledLines() {
			var obj_itemCode = toArray( frmMain.value_itemCode );

			for( var i = 0; i < frmMain.listcheckbox.length; i++ ) {
				if( !obj_itemCode[i].value ) {
					if( frmMain.listcheckbox[i].disabled == true ) {
						$(frmMain.listcheckbox[i]).closest("tr").each(function(ii,oo){
							$(this).find("[name^='value_']").removeAttr("disabled", "disabled");
						});
					}
				}
			}
		}

		function simulationWithUpdate() {
			//			if( resetUseSelectRdd ) {
			//				alert( "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" );
			//				return false;
			//			}

			var obj = frmMain.listcheckbox;

			if( typeof obj == "undefined" || !checkDetailOrderValues() || !checkOrderLimit(obj) )
				return;

			var messages = { "detail" : "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_SIMULATION" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					if( !frmMain.simulationValidation() )
						return;

					disableUnfilledLines();

					frmMain.mode.value = "wait";
					frmMain.type.value = "sim";
					/* frmMain.target = "_parent"; */

					frmMain.url.value = getLocationURL("url");

					var btn_simulation = document.getElementById( "btn_simulation" );
					if( btn_simulation ) btn_simulation.disabled = true;

					frmMain.submit();
				}
			});
		}

		function uploadReq() {
			//			if( resetUseSelectRdd ) {
			//				alert( "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" );
			//				return false;
			//			}

			//			if( !checkRegisteredOrderHeader() ) return;

			var url = "<%= systemConfig.getClassURL() %>/DPROrderRevise?mode=iup&uploadType=ORD";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "divisionCode", "<mtl:value id="header" key="divisionCode"/>" );

			if( frmMain.odrdlvGroup && frmMain.odrdlvGroup.value ) {
				url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );
			}

			windowOpen( url + "&wintype=sub", "clsMng" );
		}

		function downloadReq() {
			//			if( resetUseSelectRdd ) {
			//				alert( "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" );
			//				return false;
			//			}

			//			if( !checkRegisteredOrderHeader() ) return;

			var url =  "<%= systemConfig.getClassURL() %>/DPROrderRevise?mode=down&dwntype=item";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="header" key="shipPartyCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
			url = replaceQueryValue( url, "basisValue", "ITEM" );

			if( frmMain.odrdlvGroup && frmMain.odrdlvGroup.value ) {
				url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );
			}

			windowOpen( url );
		}
	</script>

	<script type='text/javascript' src='script/jquery-noty.js'></script>
	<script type='text/javascript'>
		function getAllowUomCodeArray(){
			var allowUomCsv = "<%=property.getProperty("allowUOM")%>";
			if( allowUomCsv ) {
				return allowUomCsv.split(",");
			}

			return [];
		}

		function queryItem( row ) {
			var resetItemLine = function( rr ) {
				$(frmMain).find("span[name=itemdesc]").get(rr).innerHTML = "";
				frmMain.value_itemCodeConfirmed[rr].value = "";
				frmMain.listcheckbox[rr].disabled = true;
				Select.removeOptions(frmMain.value_uom[rr]);
			}
			var enableItemLine = function( rr ) {
				frmMain.listcheckbox[rr].disabled = false;
			}
			if( String(frmMain.value_itemCode[row].value) == "" ) {
				resetItemLine( row );
				return;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPROrderRevise?mode=qryitm";
				url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
				url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="header" key="shipPartyCode"/>" );
				url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
				url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
				url = replaceQueryValue( url, "itemCode", frmMain.value_itemCode[row].value );
				url = replaceQueryValue( url, "type", "ord" );
				url = replaceQueryValue( url, "orderType", "<mtl:value id="header" key="orderType"/>" );
				url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
				url = replaceQueryValue( url, "displayLanguage", "<mtl:value id="property" key="dispLang"/>" );
				url = replaceQueryValue( url, "json", "records");

				var dataToSend = {};

				var ajaxOpts = {
					url : url,
					type : "GET",
					dataType : "json",
					data : dataToSend,
					success : function( data ) {
						if( data["msg"] )
							alertNoty(data["msg"], {modal:true})
//							noty({layout:'topLeft', type:'info', text: data["msg"]});
						if( data["records"] ) {
							var record = data.records[0];
							var obj_itemCode = toArray( frmMain.value_itemCode );
							var obj_itemCodeConfirmed = toArray( frmMain.value_itemCodeConfirmed );
							var obj_itemCodeNew = toArray( frmMain.value_itemCodeNew );
							var obj_uom = toArray( frmMain.value_uom );

							$(frmMain).find("span[name=itemdesc]").get(row).innerHTML = record.itemName;

							obj_itemCode[row].value = record.itemCode;
							obj_itemCodeConfirmed[row].value = record.itemCode;
							obj_itemCodeNew[row].value = record.itemCode;

							var allowUomCodes = getAllowUomCodeArray();
							var allowUOMOptions = $(record.uoms).map(function(i,o) {
								if( (","+allowUomCodes.join(",")).indexOf(","+o.uomCode) > -1 ) {
									return new Option(o.uomCode + " / " + o.packSize, o.uomCode);
								}
							});
							Select.removeOptions( obj_uom[row] );
							Select.addOptions( obj_uom[row], allowUOMOptions, true);
							enableItemLine( row );
						} else {
							resetItemLine( row );
						}
					},
					error : function( req, status, thrown ) {// only serious errors. controlled error
						// should go
						// success section.
						console.log(req);
						log4javascript.getLogger("client").error(req, thrown);
					}
				};
				$.ajax(ajaxOpts);
			}

			function reviseCommitReq() {
				if( !frmBoard.inputValidation() ||  !frmMain.reviseCommitValidation() )
					return;

				if( "ROM" === frmBoard.reviseHelpType.value
					&& "Y" !== String("<mtl:value id="request" key="isPlaceRevisible"/>") ) {
					customPopup.alert( { "header" : "<mtl:message key="ERR_ORDREV_SIM_MUST" encodeScript="true"/>" } );
					return;
				}

				var boardFormArray = $($($(frmBoard).serializeArray())
					.filter(function(i) { return this.value !== ""; }))
					.map(function(i,e) { return {name: "board_"+ this.name, value: this.value}; });
				boardFormArray.push({name: "board_headwordCode", value: frmBoard.reviseHelpType.value});
				var boardFormQueryParam = $.param(boardFormArray);

				var url =  "<%= systemConfig.getClassURL() %>/DPROrderRevise?mode=revcit";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "origOrderKey", "<mtl:value id="header" key="origOrderKey"/>" );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="header" key="shipPartyCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
			url = replaceQueryValue( url, "basisValue", "ITEM" );
			url = replaceQueryValue( url, "reviseHelpType", frmBoard.reviseHelpType.value );
			/* url = replaceQueryValue( url, "attachManageKey", atchMngKey ); */
			url = url + "&"+ boardFormQueryParam;

			/* var backURL = getQueryValue( getLocationURL(), "url" ); */
			var btn_confirm = document.getElementById( "btn_confirm" );
			if( btn_confirm ) btn_confirm.disabled = true;

			windowOpen( url, null, true );
		}

		function reviseCancelReq() {
			noty({ type:'info', text: "<mtl:message key="jsp.dpr_orderrevise_input.MSG_REVISECANCEL" encodeScript="true"/>" });

			var url =  "<%= systemConfig.getClassURL() %>/DPROrderRevise?mode=revccl";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "origOrderKey", "<mtl:value id="header" key="origOrderKey"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );

			var backURL = getQueryValue( getLocationURL(), "url" );
			if( backURL ) {
				url = replaceQueryValue( url, "url", backURL );
			}

			windowOpen( url, null, true );
		}

		function syncSubmitDetail() {
			//			removeOrderLineUnavailable();

			var url = "<%= systemConfig.getClassURL() %>/DPROrderRevise?mode=upd";

			frmMain.url.value = getLocationURL();

			var formArray = $(frmMain).serializeArray();

			var msg = document.getElementById( "msg" );

			var self = this;
			self.dfd = $.Deferred();

			$.ajax({
				url: url,
				data: formArray,
				dataType: 'html',
				method: 'POST',
				async: false,
				beforeSend: function() {
					if( msg ) msg.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_SIMULATING" encodeScript="true"/>";
				},
				success: function(data, status, xhr) {
					if( data ) {
						if( msg ) msg.innerHTML = "";

						var errMsg = $(xhr.responseText).find("table.info_content td.error").html();
						if( errMsg ) {
							noty({type: 'error', text: errMsg, timeout: 5000, closeWith:['button']});
							self.dfd.resolve(null);
						} else {
							self.dfd.resolve(true);
						}
					}
					return;
				},
				error: function(xhr, status, errorThrown) {
					var text = $(xhr.responseText);
					noty({text: text, closeWith:['button']});
				},
			});

			return self.dfd.promise();
		}

		function deleteReq() {
			//			if( resetUseSelectRdd ) {
			//				alert( "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" );
			//				return false;
			//			}

			var url = getRequestMultiURL( "<%= systemConfig.getClassURL() %>/DPROrderRevise", "rmd", frmMain.listcheckbox, "orderKey,lineNumber" );
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL("url")) );

			if( window.parent.isContinueOrder )
				url += "&isContinueOrder=Y";

			if( frmMain.odrdlvGroup && frmMain.odrdlvGroup.value ) {
				url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );
			}

			if( checkURLLength(url) ) {
				var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" };
				customPopup.confirm( messages, function(res) {
					if( res ) {
						disableUnfilledLines();
						url = replaceQueryValue( url, "prevUrl", encodeURIComponent(getQueryValue(getLocationURL(), "url")) );

						noty({ type:'info', text: "<mtl:message key="jsp.dpr_order_input.MSG_DELETING" encodeScript="true"/>" });
						syncSubmitDetail().then(function(resolved) {
							if( resolved == true ) {
								location.replace( url );
							}
							return false;
						});
					}
				});
			}
		}

		function bodyLoad() {
			if( frmMain.value_lineNumber ) {
				$(frmMain.value_itemCode).on('keydown', function(e) {
					if (e.which == 13) {
						e.preventDefault();
						$(this).trigger("blur");
					}
				});
				$(frmMain.listcheckbox).each(function(i,o) {
					if( $.isEmptyObject(frmMain.value_itemCode[i].value) ) {
						o.disabled = true;
					}
				});
			}

			<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
			syncField( frmBoard.email, 'frmBoard.email', true );
			var frmBoardContent = $("[name=frmBoard] textarea[name=content]").get(0);
			syncField( frmBoardContent, 'frmBoard.content', true );
			<% } %>
		}

		function shortageEliminateReq() {
			$( "#dialog-confirm" ).show();
			$( "#dialog-confirm" ).dialog({
				resizable: false,
				height: "auto",
				width: 400,
				modal: true,
				open: function() {
					$("#dialog-contents").html("<%= HtmlUtility.toScriptString(msghandler.getMessage("jsp.dpr_order_result.MSG_SHORTAGE_ELIMINATE") ) %>");
				},
				buttons: {
					"<%= HtmlUtility.toScriptString(msghandler.getMessage("jsp.dpr_order_result.DIALOG_BUTTON_YES") ) %>": function() {
						$( this ).dialog( "close" );
						var orderKey = "<mtl:value id="header" key="orderKey"/>";
						if( orderKey == null || orderKey == "" ) {
							customPopup.alert( { "header" : "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
							return;
						}

						disableUnfilledLines();

						if( Field.isArray(frmMain.value_lineNumber) ) {
							for(var i = 0; i < frmMain.value_lineNumber.length; i++ ) {
								if( frmMain.value_orderQty[i].value && frmMain.value_simulationOrderQty[i].value
								 && new Number(frmMain.value_orderQty[i].value) > new Number(frmMain.value_simulationOrderQty[i].value) ) {
									frmMain.value_orderQty[i].value = frmMain.value_simulationOrderQty[i].value;
								}
								if( frmMain.value_simulationOrderQty[i].value ) {
									frmMain.value_simulationOrderQty[i].disabled = false;
								}
							}
						} else {
							if( frmMain.value_orderQty.value && frmMain.value_simulationOrderQty.value
							 && new Number(frmMain.value_orderQty.value) > new Number(frmMain.value_simulationOrderQty.value) ) {
								frmMain.value_orderQty.value = frmMain.value_simulationOrderQty.value;
							}
						}

						frmMain.mode.value = "shrt";
						frmMain.type.value = "sim";
						frmMain.submit();
					},"<%= HtmlUtility.toScriptString(msghandler.getMessage("jsp.dpr_order_result.DIALOG_BUTTON_NO") ) %>": function() {
						$( this ).dialog( "close" );
					}
				}
			});
		}

		function syncField( el, key, forceUseSaved ) {
			if( window.sessionStorage ) {
				var val = Field.getValue( el );
				if( (val || String(val) == "" ) && !forceUseSaved ) {
					sessionStorage.setItem( key, val );
				} else {
					var savedVal = sessionStorage.getItem( key );
					if( (savedVal && String(savedVal)!="") ) {
						Field.setValue( el, savedVal );
					}
				}
			}
		}

		function alertNoty( message, opts ) {
			var opts = $.extend({
				layout:'topLeft',
				type:"warning",
				killer: true,
				template:'<div class="noty_message"><span class="noty_text" style="font-weight:bold;color:red;"></span><div class="noty_close"></div></div>'}
				, opts);
			noty($.extend(opts, {text: message}));
		}

	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc"%>
	<%@ include file="include_pub_input.inc"%>
	<%@ include file="include_rbm_bodyheader.inc"%>

	<% request.setAttribute("record", headerMap); %>
	<div class='wrapper' style='position: relative; min-width: 1300px;'>
		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
			<script>
				function toArray( obj ) {
					return ( Field.isArray(obj) ? obj : new Array(obj));
				}

				frmMain.simulationValidation = function() {
					var reviseHelpType = Field.getValue(frmBoard.reviseHelpType);
					if( "ROD" == reviseHelpType )
						customPopup.alert( { "header" : "<mtl:message key="ERR_ORDREV_CANNOT_ROD_STATUS"/>" } );
					var obj_itemCode = toArray( frmMain.value_itemCode );
					var obj_itemCodeConfirmed = toArray( frmMain.value_itemCodeConfirmed );

					var validated = true;
					for( var i = 0; i < frmMain.listcheckbox.length; i++ ) {
					//if( obj_itemCode[i].value ) {
					//	if( obj_itemCode[i].getAttribute("type") == "text"
					//		&& obj_itemCode[i].value !== obj_itemCodeConfirmed ) {
					//		validated = false;
					//		focusForm( frmMain, obj_itemCode[i] );
					//		alert("<mtl:message key="ERR_ORDREV_INVALID_ITEMCODE"/>");
					//		return;
					//	}
					//}
					}

					return validated;
				}

				frmMain.reviseCommitValidation = function() {
					var obj_itemCode = toArray( frmMain.value_itemCode );
					var obj_orderQty = toArray( frmMain.value_orderQty );
					var obj_revSimFinQty = toArray( frmMain.value_reviseSimFinalQty );
					var reviseHelpType = Field.getValue( frmBoard.reviseHelpType );

					var validated = true;
					for( var i = 0; i < frmMain.listcheckbox.length; i++ ) {
						if( obj_itemCode[i].value ) {
							var isSimedItem = (obj_revSimFinQty[i].value ? true : false);
							if( !isSimedItem && "ROM" == String(reviseHelpType) ) {
								validated = false;
								focusForm( frmMain, obj_orderQty[i] );
								customPopup.alert( { "header" : "<mtl:message key="ERR_ORDREV_SIM_MUST"/>" } );
								break;
							}
							if( !isSimedItem && !obj_orderQty[i].value ) {
								validated = false;
								focusForm( frmMain, obj_orderQty[i] );
								customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
								break;
							}
							if( "ROM" == String(reviseHelpType) ) {
								if( obj_orderQty[i].value && obj_revSimFinQty[i].value
									&& Number(obj_orderQty[i].value) != Number(obj_revSimFinQty[i].value) ) {
									validated = false;
									focusForm( frmMain, obj_orderQty[i] );
									customPopup.alert( { "header" : "<mtl:message key="ERR_ORDREV_ORDERQTY_CANNOT_EXCEED_SIMQTY"/>" } );
									break;
								}
							}
						}
					}

					return validated;
				}
			</script>

			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='type'/>
			<mtl:hidden id="header" key="orderKey"/>
			<mtl:hidden id="header" key="origOrderKey"/>
			<mtl:hidden id="header" key="organizationCode"/>
			<mtl:hidden id="header" key="reviseHbrdSeqId" />
			<mtl:hidden id="header" key="revHbrdContent" />
			<mtl:hidden id="header" key="distributionChannelCode"/>
			<mtl:hidden id="header" key="partyCode"/>

			<mtl:contentGroup groupId="list" type="list" style="float: left; width: calc(100% - 472px); min-width: 830px;">
				<%
					final java.util.Map<String, Object> final_headerMap = (Map<String, Object>)request.getAttribute("header");
					final String allowUOM = property.getProperty( "allowUOM" );

					com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage ) {
						private Object getUomPackSize( java.util.List<java.util.Map> uoms, String itemCode, String uomCode ) {
							Object uomPackSize = null;
							if( uoms != null ) {
								for( java.util.Map uomMap : uoms ) {
									if( itemCode.equals(uomMap.get("itemCode")) && uomCode.equals(uomMap.get("uomCode")) ) {
										uomPackSize = uomMap.get("packSize");
										break;
									}
								}
							}
							return uomPackSize;
						}

						public String getColumnValue( Column column, java.util.Map recordMap, int row, int col ) {
							if( "info".equals(request.getParameter("mode")) ) {
								return column.format( recordMap, msghandler );
							}
							String detailStatus = com.irt.data.Record.extractString( recordMap, "detailStatus" );
							String fieldKey = column.getFieldKey();
							StringBuffer sbuf = new StringBuffer();

							boolean isUserFillingLine = (recordMap.get("reviseBeforeCnfQty") == null ? true : false);

							if( "itemCode".equals(fieldKey) ) {

								sbuf
								.append( "<input type='hidden' name='value_lineNumber' value='"+ HtmlUtility.toHtmlString(recordMap.get("lineNumber")) +"'/>" )
								.append( "<input type='hidden' name='value_itemCodeConfirmed' value='"+ HtmlUtility.toHtmlString(recordMap.get("itemCodeConfirmed")) +"'/>" )
								.append( "<input type='hidden' name='value_itemRefInd' value='"+ HtmlUtility.toHtmlString(recordMap.get("itemRefInd")) +"'/>" )
								.append( "<input type='hidden' name='value_childLineNumber' value='"+ HtmlUtility.toHtmlString(recordMap.get("childLineNumber")) +"'/>" )
								.append( "<input type='hidden' name='uomPrice' value='"+ HtmlUtility.toHtmlString(recordMap.get("unitPrice")) +"'/>" )
								.append( "<input type='hidden' name='unitPrice' value='"+ HtmlUtility.toHtmlString(recordMap.get("unitPrice")) +"'/>" )
								.append( "<input type='hidden' name='packSize' value='"+ HtmlUtility.toHtmlString(recordMap.get("packSize")) +"'/>" )
								.append( "<input type='hidden' name='itemConsumerEANCode' value='"+ HtmlUtility.toHtmlString(recordMap.get("itemConsumerEANCode")) +"'/>" );

								String itemLineId = "item-ln-" + recordMap.get("lineNumber");
								sbuf.append( "<span id='"+ itemLineId +"' name='item-ln'>"
									+ "<input type='hidden' name='value_itemCodeNew' value='"+ HtmlUtility.toHtmlString(recordMap.get("itemCode")) +"'/>" );

								if( isUserFillingLine ) {
									sbuf.append( "<span>" );
									sbuf.append( "<input type='text' name='value_itemCode'" + " class='input-field small'" );
									sbuf.append( " onBlur='JavaScript:queryItem("+row+")'" );
									sbuf.append( " value='" + column.format(recordMap, msghandler)+ "'/>" );
									sbuf.append( "<i class='fa fa-arrow-circle-right' aria-hidden='true' onClick='JavaScript:queryItem("+row+");'></i>" );
									sbuf.append( "</span>" );
								} else {
									sbuf.append( "<input type='hidden' name='value_itemCode'" )
										.append( " value='"+ recordMap.get(fieldKey) +"'>" )
										.append( column.format(recordMap, msghandler) )
										.append( "</input>" );
								}
								sbuf.append( "</span>" );
							} else if( "itemNameConfirmed".equals(fieldKey) ) {
								sbuf.append( "<span id='itemdesc_"+row+"' name='itemdesc'>" );
								sbuf.append( column.format( recordMap, msghandler ) );
								sbuf.append( "</span>" );
							} else if( "uom".equals(fieldKey) ) {
								String[] allowUOMValues = allowUOM.split( "," );
								Object uom = recordMap.get( "uom" );
								java.util.List<java.util.Map> uoms = (java.util.List<java.util.Map>)recordMap.get("uoms");

								sbuf.append( "<select id='value_uom" + row + "' name='value_uom'>" );
								if( com.irt.dpr.Country.isFeature((String)final_headerMap.get("organizationCode"), "useSuggestSalesUnitInput") ) {
									Object uomPackSize = getUomPackSize( uoms, (String)recordMap.get("itemCode"), (String)uom );
									if( uomPackSize == null ) {// in uom table the uom in recordMap is not found.
										uom = "CSE";//default
										uomPackSize = getUomPackSize(uoms, (String)recordMap.get("itemCode"), "CSE");
									}
									sbuf.append( "<option value='"+ uom +"'"+ " selected" )
									.append( ">"+ uom +" / "+ HtmlUtility.toHtmlString(uomPackSize) + "</option>" );
								} else {
									boolean selected = false;
									for( int i = 0; i < allowUOMValues.length; i++ ) {
										if( !selected ) selected = allowUOMValues[i].equals( uom );
										Object uomPackSize = recordMap.get("packSize");
										boolean found = false;
										if( uoms != null ) {
											for( java.util.Map uomMap : uoms ) {
												if( recordMap.get("itemCode").equals(uomMap.get("itemCode")) && allowUOMValues[i].equals(uomMap.get("uomCode")) ) {
													uomPackSize = uomMap.get("packSize");
													found = true;
													break;
												}
											}
										}
										if( found ) {// in uom table the uom in recordMap is found.
											if( !isUserFillingLine ) {
												if( (allowUOMValues[i].equals(uom)) ) {
													sbuf.append( "<option value='"+ allowUOMValues[i] +"' "+ (allowUOMValues[i].equals(uom) ? "selected" : "") )
														.append( ">"+ allowUOMValues[i] +" / "+ HtmlUtility.toHtmlString(uomPackSize) + "</option>" );
													break;
												}
											} else {
												sbuf.append( "<option value='"+ allowUOMValues[i] +"' "+ (allowUOMValues[i].equals(uom) ? "selected" : "") )
													.append( ">"+ allowUOMValues[i] +" / "+ HtmlUtility.toHtmlString(uomPackSize) + "</option>" );
											}
										}
									}
								}
								sbuf.append( "</select>" );

								sbuf.append( "<script>" );
								sbuf.append( "	$( function() {" );
								sbuf.append( "		var options = $.extend( {}, smallSelectmenuOptions, { searchable: false } );" );
								sbuf.append( "		$(\"#value_uom" + row + "\").singleSelectmenu( options );" );
								sbuf.append( "	});" );
								sbuf.append( "</script>" );
							} else if( "simulationOrderQty".equals(fieldKey) ) {
								String itemRefInd = com.irt.data.Record.extractString( recordMap, "itemRefInd" );
								String orderQty = com.irt.data.Record.extractString( recordMap, "orderQty" );
								String simulationOrderQty = com.irt.data.Record.extractString( recordMap, "simulationOrderQty" );
								String title = column.getColumnHelp( recordMap, msghandler );
								String attr = (String)column.getColumnAttr();
								String htmlStr = "<td" + ( attr != null ? " " + attr : "" );

								if( orderQty != null && !orderQty.equals(simulationOrderQty)
									&& !com.irt.dpr.OrderDetail.ITEMREF_PIPO_ORIGINAL.equals(itemRefInd) )
								htmlStr += " bgcolor='#FF9934'";

								int intOrderQty = 0, intSimulationOrderQty = 0;
								if( orderQty != null && orderQty.length() > 0 )
								intOrderQty = Integer.parseInt( orderQty );
								if( simulationOrderQty != null && simulationOrderQty.length() > 0 )
								intSimulationOrderQty = Integer.parseInt( simulationOrderQty );

								int shortageQty = 0;
								String itemConsumerEANCodeCNF = (String)recordMap.get( "consumerEANCode" );
								if( itemConsumerEANCodeCNF == null ) {
									itemConsumerEANCodeCNF = (String)recordMap.get( "itemConsumerEANCodeCNF" );
								}

								if( intOrderQty != 0 && intOrderQty > intSimulationOrderQty ) {
									shortageQty = intOrderQty - intSimulationOrderQty;
									int pcQty = recordMap.get("formatPCQty") != null ? Integer.parseInt( recordMap.get("formatPCQty").toString() ): 0;
									int simulationPCQty = recordMap.get("formatSimulationPCQty") != null ? Integer.parseInt( recordMap.get("formatSimulationPCQty").toString() ): 0;
									int shortagePCQty = pcQty - simulationPCQty;
									if( shortageQty == intOrderQty ) {
										sbuf.append( "<input type='hidden' name='eliminate_lineNumber' value='"+ recordMap.get("lineNumber") +"'>" )
											.append( "<input type='hidden' name='eliminate_itemCode' value='"+ recordMap.get("itemCode") +"'>" )
											.append( "<input type='hidden' name='eliminate_itemCodeConfirmed' value='"+ recordMap.get("itemCodeConfirmed") +"'/>" )
											.append( "<input type='hidden' name='eliminate_itemConsumerEANCodeCNF' value='"+ itemConsumerEANCodeCNF +"'/>" )
											.append( "<input type='hidden' name='eliminate_itemName' value='"+ recordMap.get("itemNameConfirmed") +"'/>" )
											.append( "<input type='hidden' name='eliminate_shelfLife' value='"+ recordMap.get("shelfLife") +"'/>" )
											.append( "<input type='hidden' name='eliminate_qty' value='"+ shortageQty +"'>" )
											.append( "<input type='hidden' name='eliminate_uom' value='"+ recordMap.get("uom") +"'>" )
											.append( "<input type='hidden' name='eliminate_pcQty' value='"+ shortagePCQty +"'>" );
									} else {
										sbuf.append( "<input type='hidden' name='shortage_lineNumber' value='"+ recordMap.get("lineNumber") +"'>" )
											.append( "<input type='hidden' name='shortage_itemCode' value='"+ recordMap.get("itemCode") +"'>" )
											.append( "<input type='hidden' name='shortage_itemCodeConfirmed' value='"+ recordMap.get("itemCodeConfirmed") +"'/>" )
											.append( "<input type='hidden' name='shortage_itemConsumerEANCodeCNF' value='"+ itemConsumerEANCodeCNF +"'/>" )
											.append( "<input type='hidden' name='shortage_itemName' value='"+ recordMap.get("itemNameConfirmed") +"'/>" )
											.append( "<input type='hidden' name='shortage_shelfLife' value='"+ recordMap.get("shelfLife") +"'/>" )
											.append( "<input type='hidden' name='shortage_qty' value='"+ shortageQty +"'>" )
											.append( "<input type='hidden' name='shortage_uom' value='"+ recordMap.get("uom") +"'>" )
											.append( "<input type='hidden' name='shortage_pcQty' value='"+ shortagePCQty +"'>" );
									}
								}
								sbuf.append( column.format(recordMap, msghandler) );
							} else if( "reviseOrderQty".equals(fieldKey) ) {
								String status = com.irt.data.Record.extractString( final_headerMap, "status" );
								String reviseQty = com.irt.data.Record.extractString( recordMap, "orderQty" );
								String reviseSimInputQty = com.irt.data.Record.extractString( recordMap, "reviseSimInputQty" );
								String reviseSimFinalQty = com.irt.data.Record.extractString( recordMap, "reviseSimFinalQty" );
								String simulationOrderQty = com.irt.data.Record.extractString( recordMap, "simulationOrderQty" );

								sbuf.append( "<input type='hidden' name='value_reviseChangeIndex' value='"+HtmlUtility.toHtmlString(final_headerMap.get("reviseChangeIndex"))+"'/>" );
								sbuf.append( "<input type='hidden' name='value_reviseStatus' value='"+HtmlUtility.toHtmlString(final_headerMap.get("reviseStatus"))+"'/>" );
								sbuf.append( "<input type='hidden' name='value_orgOrderQty' value='"+ HtmlUtility.toHtmlString(reviseQty) +"'/>" );
								sbuf.append( "<input type='text' name='value_orderQty'" );
								sbuf.append( " class='input-field small'" );

								if( "DE".equals(detailStatus) ) {
									sbuf.append( " value='0'" );
								} else {
									sbuf.append( " value='"+ HtmlUtility.toHtmlString(reviseQty) +"'" );
								}
								sbuf.append( htmlpage.hasManageAuth() ? "" : "readonly='true'" );
								sbuf.append( "/>" );

								sbuf.append( "<input type='hidden' disabled='true' name='value_simulationOrderQty' value='"+ HtmlUtility.toHtmlString(recordMap.get("simulationOrderQty")) +"'>" );
								sbuf.append( "<input type='hidden' disabled='true' name='value_reviseSimFinalQty' value='"+ HtmlUtility.toHtmlString(recordMap.get("reviseSimFinalQty")) +"'>" );
							} else
							return column.format( recordMap, msghandler );

							return sbuf.toString();
						}
					};
					listwriter.setNumbering(false);
					listwriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
					<mtl:button type="return"/>
					<% if( !"info".equals(request.getParameter("mode")) ) { %>
						<mtl:button type="button" onClick="JavaScript: simulationWithUpdate();" icon="images/ico_submit_white.png"
									messageKey="jsp.BTN_SIMULATION" styleClass="primary"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>

		<mtl:form name="frmBoard">
			<mtl:contentGroup groupId="input" type="list" fieldSetId="boardFieldSet" mandatory="true" style="float: right; min-width: 452px; width: 452px;">
				<mtl:hidden id="record" key="boardType" defaultValue="C"/>
				<mtl:hidden id="record" key="boardOption" defaultValue="T"/>
				<mtl:hidden id="record" key="boardClassCode" defaultValue="<%=(\"HD.\"+headerMap.get(\"organizationCode\"))%>"/>
				<mtl:hidden id="record" key="tel" defaultValue=" " />
				<h2><mtl:message key="jsp.dpr_orderrevise_input.SUBTITLE_REVISE_REGIST"/></h2>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="reviseHelpType" descriptionKey="FIELD_DPR_ORDREV_HEADWORD" mandatory="true"/></div>
							<div class='field'>
								<mtl:select name="reviseHelpType" id="record" key="headwordCode" prefixKey="FIELD_DPR_ORDREV_HELPTYPE_" codeValues="ROM,ROD"
										mandatory="true" hasBlank="false" searchable="false" modified="changeType(this);"/>
							</div>
						</div>
					</div>
					<mtl:contains id="record" key="soldPartyCode">
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="reqUserCode" descriptionKey="FIELD_DPR_ORDREV_REQ_USER_CODE"/></div>
							<div class='field'><mtl:text id="record" key="soldPartyCode" name="reqUserCode" readonly="true"/></div>
						</div>
					</div>
					</mtl:contains>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderNumber" descriptionKey="FIELD_DPR_ORDREV_ORDERNUMBER"/></div>
							<div class='field'><mtl:text id="record" key="orderNumber" readonly="true"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="userName" descriptionKey="FIELD_DPR_ORDREV_USERNAME"/></div>
							<div class='field'><mtl:text id="record" key="userName" name="userName" defaultValue="<%=sessionMng.getUserName() %>"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="email" descriptionKey="FIELD_DPR_ORDREV_EMAIL"/></div>
							<div class='field'>
								<mtl:text id="record" key="email" defaultValue="<%=((com.irt.rbm.SessionMng)sessionMng).getProperty(\"userEmail\")%>"
										onBlur='JavaScript: syncField(this, "frmBoard.email");'/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="content" descriptionKey="FIELD_DPR_ORDREV_CONTENT"/></div>
							<div class='field'>
								<textarea name="content" onBlur='JavaScript: syncField(this, "frmBoard.content");'
										rows="3" <%=(htmlpage.getInputStatus()=='I'? "readonly=\"readonly\"":"")%>>
									<mtl:value id="record" key="revHbrdContent" encodeHTML="true"/>
								</textarea>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
					<% if( !"info".equals(request.getParameter("mode")) ) { %>
						<mtl:button type="button" onClick="JavaScript: reviseCancelReq();" messageKey="jsp.BTN_CANCEL"/>
						<% if( !"N".equals(request.getParameter("isPlaceRevisible")) ) { %>
							<mtl:button type="button" onClick="JavaScript: reviseCommitReq();" styleClass="primary" messageKey="jsp.BTN_SUBMIT" />
						<% } %>
					<% } %>
					</div>
				</div>
			</mtl:contentGroup>
			<script type="text/javascript">
				frmBoard.inputValidation = function() {
					<%= htmlpage.getValidationScript() %>
					if( String(frmBoard.content.value) == "" ) {
						frmBoard.content.value = "\t\t\t\t\n";
					}
					return true;
				}
			</script>
		</mtl:form>
	</div>

	<%@ include file="include_dpr_tail.inc"%>
</body>
</mtl:html>
