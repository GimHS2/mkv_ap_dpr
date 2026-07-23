<%--
	File Name:	dpr_packdealorder_input.jsp
	Version:	2.2.4

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2026/03/31		2.2.4	Order Qty값이 Simulation Qty로 바뀌었을때 Input box가 주황색으로 표시 안되는 문제 수정
	dudwls3720	2025/12/31		2.2.3	FakeOrderCanonicalProcess 삭제에 따른 참조 코드 제거
	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
	jbaek		2019/10/04		2.2.1	서블렛에서 orderQty 와 simulationOrderQty 값 비교 체크. simQty default설정.
	jbaek		2019/09/30		2.2.1	jsp deleteZero param 삭제. placeOrder시에 체크하고 삭제하도록 변경.
	jbaek		2019/05/30		2.2.0	create
--%>

<%@page import="java.text.ParseException"%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding='utf-8' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*" %>
<%@ page import="com.irt.data.Record" %>
<%@ page import="com.irt.data.Date" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map headerMap = (java.util.Map)pageContext.findAttribute( "header" );
	String status = null;
	if( headerMap != null && headerMap.containsKey("status") )
		status = com.irt.data.Record.extractString( headerMap, "status" );

	String organizationCode = null;
	if( headerMap != null ) {
		organizationCode = (String)headerMap.get( "organizationCode" );
	}
	boolean useInputRDD = com.irt.dpr.Country.isFeature( organizationCode, "useInputRDD" );
	boolean resetUseSelectRdd = "Y".equals( property.getProperty("resetUseSelectRdd") );

	boolean isReOrder = (request.getParameter("reOrder") != null && "Y".equals(request.getParameter("reOrder")));

	String isPlantInd = property.getProperty( "isPlantInd" );
	String hasCreatingOrder = property.getProperty( "hasCreatingOrder" );
	String orderDate = Record.extractString( headerMap, "orderDate" );

	long simulationQtyTotal = 0l;

	java.util.List<java.util.Map<String,Object>> records = (java.util.List<java.util.Map<String,Object>>)request.getAttribute("details");
	if( records != null && records.size() > 0 ) {
		for( java.util.Map map : records ) {
			if( map != null && map.get("simulationOrderQty") != null )
				simulationQtyTotal += ( ((Number)map.get("simulationOrderQty")).longValue() );
		}
	}
%>
<head>
	<style type="text/css">
	span.is-stop-item-label {
		text-decoration: line-through;
		color: red;
	}

	span.is-close-item-label {
		text-decoration: line-through;
		color: black;
	}

	.is-moq-exceed {
		background-color: silver;
	}
	</style>
	<%@ include file="include_rbm_header.inc" %>
	<script type='text/javascript'>
		$(function() {
			var searchFold = $("input[name=search-fold]");
			if( !searchFold.val() || searchFold.val() == "N" ) {
				$("#content_order_header .search-table:not(:eq(1))").show();
				$("#content_order_header .search-bottom").show();
				$(this).removeClass( "arrow-down" ).addClass( "arrow-up" );
			} else {
				$("#content_order_header .search-table:not(:eq(1))").hide();
				$("#content_order_header .search-bottom").hide();
				$(this).removeClass( "arrow-up" ).addClass( "arrow-down" );
			}

			$(".accordion-button").click( function() {
				if( $(this).hasClass("arrow-up") ) {
					$("#content_order_header .search-table:not(:eq(1))").hide();
					$("#content_order_header .search-bottom").hide();
					$(this).removeClass( "arrow-up" ).addClass( "arrow-down" );
					searchFold.val( "Y" );
				} else {
					$("#content_order_header .search-table:not(:eq(1))").show();
					$("#content_order_header .search-bottom").show();
					$(this).removeClass( "arrow-down" ).addClass( "arrow-up" );
					searchFold.val( "N" );
				}
			});

			$("#content_order_header .search-table").find( ".field, .field-info" ).each( function(index, item) {
				if( $(item).text().trim() === "" ) {
					if( $(item).find("#orderNetAmount").length < 1 ) {
						$(item).text( "–" );
					}
				}
			});
		});

		var resetUseSelectRdd = <%= resetUseSelectRdd %>;

		window.FeatureEnabled = {
			barCodeMultiItems: '<%=com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "Feature;BarCodeMultiItems", false)%>',
			regularItemsList: '<%=com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "Feature;RegularItemsList", false)%>',
		};

		function setSelectFirstValue( selEl ) {
			if( selEl && !selEl.value ) {
				if( selEl.options ) {
					if( selEl.value == "" ) {
						if( selEl.options[0].value == "" && selEl.options.length >= 2 ) {// has nullValueKey
							Field.setValue( selEl, selEl.options[1].value, true );
							if( $(selEl).singleSelectmenu("instance") ) {
								$(selEl).singleSelectmenu( "refresh" );
							}
							return true;
						} else if( selEl.options[0].value != "" && selEl.options.length >= 1 ) {// no nullValueKey
							Field.setValue( selEl, selEl.options[0].value, true );
							if( $(selEl).singleSelectmenu("instance") ) {
								$(selEl).singleSelectmenu( "refresh" );
							}
							return true;
						}
					}
				}
			}
		}

		function setSelectSingleValue( selEl ) {
			if( selEl && !selEl.value ) {
				if( selEl.options ) {
					if( selEl.value == "" ) {
						if( selEl.options[0].value == "" && selEl.options.length == 2 ) {// has nullValueKey
							Field.setValue( selEl, selEl.options[1].value, true );
							if( $(selEl).singleSelectmenu("instance") ) {
								$(selEl).singleSelectmenu( "refresh" );
							}
							return true;
						} else if( selEl.options[0].value != "" && selEl.options.length == 1 ) {// no nullValueKey
							Field.setValue( selEl, selEl.options[0].value, true );
							if( $(selEl).singleSelectmenu("instance") ) {
								$(selEl).singleSelectmenu( "refresh" );
							}
							return true;
						}
					}
				}
			}
			return false;
		}

		function bodyLoad() {
			var mode = "<mtl:value id="request" key="mode"/>";
			var organizationCode = "<mtl:value id="header" key="organizationCode"/>";
			var orderKey = "<mtl:value id="header" key="orderKey"/>";

			<% if( "Y".equals(hasCreatingOrder) ) { %>
				if( confirm("<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_CREATING" encodeScript="true"/>") ) {
					var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder";
					url = attachDefaultParameter( url );
					url = replaceQueryValue( url, "vtype", "header" );
					url = replaceQueryValue( url, "status", "CG" );
					url = replaceQueryValue( url, "startOrderDate", "<mtl:value id="header" key="orderDate"/>" );
					windowOpen( url, "main" );
				} else {
					window.parent.isContinueOrder = true;
				}
			<% } %>

			var orderKey = getOrderKey();
			if( orderKey != null && orderKey != "" ) {
				toggleDisabled( true, [ frmHeader.organizationCode, frmHeader.distributionChannelCode
					, frmHeader.soldPartyCode, frmHeader.shipPartyCode, frmHeader.userRdd ] );
			}

			<% if( "Y".equals(isPlantInd) ) { %>
			customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_INVALID_ITEM"/>" } );
			<% } %>

			if( frmHeader.organizationCode && frmHeader.organizationCode.value
					&& frmHeader.distributionChannelCode && frmHeader.distributionChannelCode.value ) {
	 			if( setSelectSingleValue(frmHeader.soldPartyCode) && frmHeader.shipPartyCode.value == "" ) {
	 			//if( frmHeader.soldPartyCode && frmHeader.soldPartyCode.value && frmHeader.shipPartyCode.value == "" ) {
	 				if( orderKey == null || orderKey == "" ) {
						readConditionReq( "SHIP" );
					}
	 			}
		 		//setSelectSingleValue( frmHeader.shipPartyCode );
			}

			if( frmMain['value_lineNumber'] )
				calculateOrderNetAmount();

			$(frmMain.value_orderQty).on('keydown', function(e) {
				if (e.which == 13) {
					e.preventDefault();
					$(this).trigger("blur");
				}
			});
			$(frmMain.value_orderQty).each(function(i, o){
				if( String(frmMain.isFirstSim.value) === "Y" ) {
					o.value = "0";
				}

				checkOrderInputValue(i);
			});
			if( String(frmMain.isFirstSim.value) === "Y" ) {
				frmMain.isFirstSim.value = "";
			}

			markOrderLineAvailability();

			if( frmHeader.dealCode.value && frmHeader.orderKey.value ) {
				Field.lock( frmHeader.dealCode, false );
				toggleDisabled( true, frmHeader.dealCode );
			}

			<% if( "CD".equals(status) || (headerMap.get("orderKey") != null && status == null) ) { %>
				Field.lock( frmHeader.inDate, true );
				Field.lock( frmHeader.userRdd, true );
			<% } %>

			if( setSelectSingleValue( frmHeader.dealCode ) ) {
				changeDealCode( frmHeader.dealCode );
			}

			// execute if dealCode selected.
			if( frmHeader.soldPartyCode.value && frmHeader.shipPartyCode.value && frmHeader.dealCode.value ) {
				predefinedRdd();
			}
		}

		function checkOrderInputValue( row ) {
			var orderQtyObj, isStopItemObj, pdRmnMaxQtyObj, simQtyObj;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				orderQtyObj = frmMain.value_orderQty[row];
				simQtyObj = (frmMain['value_simulationOrderQty'] ? frmMain.value_simulationOrderQty[row] : null);
				isStopItemObj = frmMain.isStopItem[row];
				pdRmnMaxQtyObj = (frmMain['pdRmnMaxQty'] ? frmMain.pdRmnMaxQty[row] : null);
			} else {
				orderQtyObj = frmMain.value_orderQty;
				simQtyObj = (frmMain['value_simulationOrderQty'] ? frmMain.value_simulationOrderQty : null);
				isStopItemObj = frmMain.isStopItem;
				pdRmnMaxQtyObj = (frmMain['pdRmnMaxQty'] ? frmMain.pdRmnMaxQty : null);
			}

			if( !Field.checkNumberFormat(orderQtyObj, true, false, "<mtl:message key="FIELD_DPR_ORDERDTL_ORDERQTY"/>") ) {
				orderQtyObj.value = orderQtyObj.defaultValue;
				focusForm( frmMain, orderQtyObj );
				return false;
			} else if( orderQtyObj.value.length > 0 && orderQtyObj.value.indexOf(' ') >= 0 ) {
//				alert( "<mtl:message key="ERR_INVALID_NUMBER"/>" );
				orderQtyObj.value = $.trim( orderQtyObj.value );
				focusForm( frmMain, orderQtyObj );
			}
			if( orderQtyObj.value === "" ) {
				orderQtyObj.value = "0";
				return false;
			}

			if( pdRmnMaxQtyObj ) {
				var calcPackSize = getCalcPackSize( row );
				if( pdRmnMaxQtyObj.value ) {
					var uomRmnQty = parseInt( pdRmnMaxQtyObj.value / calcPackSize );
					$(orderQtyObj).attr("data-moq-rmn-max", uomRmnQty);

					$(pdRmnMaxQtyObj).parent().find("span.text_pdRmnMaxQty").text( uomRmnQty );

					if( uomRmnQty == 0 ) {
						orderQtyObj.value = 0;
						Field.lock( orderQtyObj );
						calculateOrderLine( row );
					} else if( orderQtyObj.value > uomRmnQty ) {
						orderQtyObj.value = uomRmnQty;
						$(orderQtyObj).addClass("alert").closest("td").addClass("is-moq-exceed");

						var itemCode = ( Field.isArray(frmMain.value_lineNumber) ? frmMain.value_itemCode[row] : frmMain.value_itemCode ).value;

						noty( {type:'warning', timeout: 5000, text:
						( "<mtl:message key="ERR_MAXORDQTY_EXCEED" encodeScript="true"/>" + "( " + itemCode + " : " + uomRmnQty + " )") } );

						return;
					}
				}
			}
			$(orderQtyObj).closest("td").removeClass("is-moq-exceed");

			if( isStopItemObj.value == true )
				orderQtyObj.value = 0;
		}

		function calculateTotalPrice( row ) {
			var singlePrice, packSize, orderQty;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				singlePrice = frmMain.singlePrice[row].value;
				packSize = frmMain.packSize[row].value;
				orderQty = frmMain.value_orderQty[row].value;
			} else {
				singlePrice = frmMain.singlePrice.value;
				packSize = frmMain.packSize.value;
				orderQty = frmMain.value_orderQty.value;
			}

			var totalPrice;
			if( isFinite(singlePrice) && isFinite(packSize) && isFinite(orderQty) )
				totalPrice = eval( singlePrice * packSize * orderQty );
			else
				totalPrice = 0;

			if( Field.isArray(document.all.totalPrice) )
				document.all.totalPrice[row].innerHTML = totalPrice;
			else
				document.all.totalPrice.innerHTML = totalPrice;
		}

		function checkRegisteredOrderHeader() {
			if( getOrderKey() == null || getOrderKey() == "" ) {
				customPopup.alert( { "header" :  "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
				return false;
			}

			return true;
		}

		function downloadReq() {
			if( !checkRegisteredOrderHeader() ) return;

			var url =  "<%= systemConfig.getClassURL() %>/DPRPackDealOrder?mode=down";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="header" key="shipPartyCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
			url = replaceQueryValue( url, "dealCode", "<mtl:value id="header" key="dealCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "basisValue", "ORD" );

			windowOpen( url );
		}

		function getOrderKey() {
			return "<mtl:value id="header" key="orderKey"/>";
		}

		function predefinedRdd() {
			if( frmHeader.soldPartyCode.value ) {
				var url = "<%= systemConfig.getClassURL() %>/DPRRddMng?mode=rdd";
				if( !Field.checkMandatory(frmHeader.organizationCode) ) return;
				if( !Field.checkMandatory(frmHeader.distributionChannelCode) ) return;
				if( !Field.checkMandatory(frmHeader.soldPartyCode) ) return;

				url = replaceQueryValue( url, "organizationCode", encodeURIComponent(frmHeader.organizationCode.value) );
				url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(frmHeader.distributionChannelCode.value) );
				url = replaceQueryValue( url, "partyCode", encodeURIComponent(frmHeader.soldPartyCode.value) );
				url = replaceQueryValue( url, "orderDate", encodeURIComponent(frmHeader.ordDate.value) );
				if( frmHeader.shipPartyCode.value != "" ) {
					url = replaceQueryValue( url, "shipPartyCode", encodeURIComponent(frmHeader.shipPartyCode.value) );
				}
				windowOpen( url, "clsName" );
			}
		}

		function setPredefinedRDD( options ) {
			var obj = frmHeader.userRdd;
			var disabled = false;
			if( options.length > 0 ) {
				obj.disabled = true;
				var selectIndex = 0;
				while( obj.options.length >= 1 )
					obj.remove( obj.options.length - 1 );

				var optionObj;
				if( obj.options.length < 1 ) {
					optionObj = document.createElement( "option" );
					obj.appendChild( optionObj );
				} else
					optionObj = obj.options[0];

				optionObj.value = "";
				optionObj.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_BLANK"/>";

				var dayOfWeeks = new Array( "<mtl:message key="jsp.dpr_order_input.MSG_DAYOFWEEK_1"/>"
						, "<mtl:message key="jsp.dpr_order_input.MSG_DAYOFWEEK_2"/>"
						, "<mtl:message key="jsp.dpr_order_input.MSG_DAYOFWEEK_3"/>"
						, "<mtl:message key="jsp.dpr_order_input.MSG_DAYOFWEEK_4"/>"
						, "<mtl:message key="jsp.dpr_order_input.MSG_DAYOFWEEK_5"/>"
						, "<mtl:message key="jsp.dpr_order_input.MSG_DAYOFWEEK_6"/>"
						, "<mtl:message key="jsp.dpr_order_input.MSG_DAYOFWEEK_7"/>" );

				var todayOfWeekIdx = new Date().getDay();
				var orderKey = getOrderKey();
				for( var i = 0; i < options.length; i++ ) {
					optionObj = document.createElement( "option" );
					var dayOfWeekIndex = new Date( options[i] ).getDay();

					optionObj.value = options[i];
					optionObj.innerHTML = options[i] + " " + dayOfWeeks[dayOfWeekIndex];
					obj.appendChild( optionObj );
					if( selectIndex < 1 ) {
						if( options.length == 1 ) {
							selectIndex = 1;
						} else if( orderKey != null && orderKey != "" && frmHeader.inDate.value == options[i] ) {
							selectIndex = i + 1;
						} else if( todayOfWeekIdx == 5 && options.length > 1 ) {
							selectIndex = options.length;
						}
					}
				}

				var orderKey = getOrderKey();
				if( orderKey != null && orderKey != "" ) {
					obj.selectedIndex = selectIndex;
					if( resetUseSelectRdd ) {
						obj.disabled = false;
						disabled = false;
					} else {
						obj.disabled = true;
						disabled = true;
					}
				} else {
					obj.disabled = false;
					disabled = false;
					if( options.length == 1 ) {
						obj.selectedIndex = selectIndex;
					} else if( todayOfWeekIdx == 5 && options.length > 1 ) {
						obj.selectedIndex = selectIndex;
					}
				}
			} else {
				while( obj.options.length >= 1 )
					obj.remove( obj.options.length - 1 );

				obj.disabled = true;
				disabled = true;
			}
			$(obj).singleSelectmenu( "refresh" );
			$(obj).singleSelectmenu( "option", "disabled", disabled );
		}

		function checkOrderInd( ordInd ) {
			orderInd = ordInd;
			if( ordInd == "N" ) {
				$("#sync").hide();
				$("#simulation").hide();
				$("#placeorder").hide();
				messageInit( "error", "<%= HtmlUtility.toScriptString( msghandler.getMessage("jsp.dpr_order_input.MSG_CANNOT_PLACE_ORDER_HOLIDAY") ) %>" );
			} else {
				$("#sync").show();
				$("#simulation").show();
				$("#placeorder").show();
			}
		}

		function readTradePartnersReq( type ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=stp";
			url = replaceQueryValue( url, "type", type );
			if( !Field.checkMandatory(frmHeader.organizationCode) ) return;
			if( !Field.checkMandatory(frmHeader.distributionChannelCode) ) return;
			url = replaceQueryValue( url, "organizationCode", encodeURIComponent(frmHeader.organizationCode.value) );
			url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(frmHeader.distributionChannelCode.value) );

			if( "sold" == type ) {
				frmHeader.soldPartyCode.childNodes[0].innerHTML = "<mtl:message key="jsp.MSG_LOADING"/>";
				frmHeader.soldPartyCode.selectedIndex = 0;
				frmHeader.soldPartyCode.disabled = true;
				frmHeader.shipPartyCode.childNodes[0].innerHTML = "<mtl:message key="jsp.MSG_LOADING"/>";
				frmHeader.shipPartyCode.selectedIndex = 0;
				frmHeader.shipPartyCode.disabled = true;

				windowOpen( url, "clsName" );
			} else if( "ship" == type ) {
				if( !Field.checkMandatory(frmHeader.soldPartyCode) ) return;
				url = replaceQueryValue( url, "soldPartyCode", encodeURIComponent(frmHeader.soldPartyCode.value) );

				frmHeader.shipPartyCode.childNodes[0].innerHTML = "<mtl:message key="jsp.MSG_LOADING"/>";
				frmHeader.shipPartyCode.selectedIndex = 0;
				frmHeader.shipPartyCode.disabled = true;

				windowOpen( url, "clsName" );
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

				setSelectSingleValue( obj );
			}
			obj.disabled = false;
		}

		function attributePartyCallback( type ) {
			setSelectSingleValue( frmHeader.soldPartyCode );
			setSelectSingleValue( frmHeader.shipPartyCode );

			if( "SHIP" == type ) {
				$("#dealCode").children( "option:not(:first)" ).remove();
				$("#dealCode").singleSelectmenu( "refresh" );
				changeDealCode( frmHeader.dealCode );
			}
		}

		function setTradePartners( type, soldOptions, shipOptions ) {
			if( "sold" == type )
				setTradePartner( type, frmHeader.soldPartyCode, soldOptions );
			else if( "ship" == type ) {
				setTradePartner( type, frmHeader.shipPartyCode, shipOptions );

				if( !frmHeader.dealCode.value )
					changeDealCode( frmHeader.dealCode );
			}
		}

		function syncSubmitDetail() {
			removeOrderLineUnavailable();

			var url = "<%= systemConfig.getClassURL() %>/DPRPackDealOrder?mode=upd";

			frmMain.url.value = getLocationURL("url");

			var formArray = $(frmMain).serializeArray();

			var msg = document.getElementById( "msg" );

			var self = this;
			self.dfd = $.Deferred();

			$.ajax({
				crossOrigin: true,
				url: url,
				data: formArray,
				dataType: 'html',
				method: 'POST',
				beforeSend: function() {
					if( msg ) msg.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_SIMULATING" encodeScript="true"/>";
				},
				success: function(data, status, xhr) {
					if( data ) {
						if( msg ) msg.innerHTML = "";

						var errMsg = $(xhr.responseText).find("table.info_content td.error").html();
						if( errMsg ) {
							noty({type: 'error', text: errMsg, timeout: 5000, closeWith:['button']});
							console.log( $(xhr.responseText).find("table.info_content").html() );
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
			})

			return self.dfd.promise();
		}


		function simulationWithUpdate( asking ) {
			var obj = frmMain.listcheckbox;

			if( typeof obj == "undefined" || !checkDetailOrderValues() || !checkOrderLimit(obj) )
				return;

			asking = (asking !== "undefined" ? asking : true);
			if( asking == true ) {
				if( !confirm( "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_SIMULATION" encodeScript="true"/>" ) )
					return;
			}

			syncSubmitDetail().then(function(resolved) {
				if( resolved == true ) {
					if( true ) {
						frmMain.mode.value = "wait";
						frmMain.type.value = "sim";
						//					frmMain.target = "_parent";

						var btn_simulation = document.getElementById( "btn_simulation" );
						if( btn_simulation ) btn_simulation.disabled = true;

						return frmMain.submit();
}
					return;
				} else {
					return false;
				}
			});
		}

		function creation() {
			calculateOrderNetAmount();

			var obj = frmMain.listcheckbox;
			if( typeof obj == "undefined" || !checkDetailOrderValues( true ) || !checkOrderLimit(obj) || !checkPackdealFulfilled() )
				return;

			if( <%= orderDate %> != <%= Date.getInstance(sessionMng.getTimeZone()) %> ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_ORDERDATE" encodeScript="true"/>" } );

				var url= "<%= systemConfig.getClassURL() %>/DPRPackDealOrder?mode=ior";
				url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey" />" );
				url = replaceQueryValue( url, "reOrder", "Y" );

				windowSelfOpen( url );
			} else {
				if( <%=simulationQtyTotal%> != 0 ) {
					if( !confirm( "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_CREATION" encodeScript="true"/>" ) ) {
						return;
					}
				} else {
					if( !confirm( "<mtl:message key="jsp.dpr_order_input.MSG_SIMULATION_QTY_CEHCK" encodeScript="true"/>" ) ) {
						return;
					}
				}

				syncSubmitDetail()
				.then(function(resolved) {
					if( resolved == true ) {
						var url = "<%= systemConfig.getClassURL() %>/DPRPackDealOrder?mode=wait";
						url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
						url = replaceQueryValue( url, "type", "cre" );

						var backURL = replaceQueryValue(getLocationURL("url"), "mode", "ior");
						var msg = document.getElementById( "msg" );

						if( msg ) msg.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_CREATING" encodeScript="true"/>";
						windowSelfOpen( url, backURL );
					} else {
						return false;
					}
				});
			}
		}

		function submitLineUpdate( lineNumber ) {
			var multiLineArray = $(frmMain).serializeArray();

			var formSingleLine = {};
			$.map(multiLineArray, function(n, i) { formSingleLine[n['name']] = n['value'];});
			formSingleLine["url"] = getLocationURL(); // backURL
			$("#item-ln-"+ lineNumber).parent("td").parent("tr").find("[name^='value']").each(function(index,object){
				formSingleLine[$(this).attr("name")] = $(this).val();
			});

			$.ajax({
				type:"POST",
				url: replaceQueryValue(getLocationURL(),"mode","upd"),
				data: formSingleLine,
				dataType: "html",
				success: function(result) {

					var ret_frmMain = $(result).filter(function(){ return $(this).attr("name") == "frmMain"; })
					var ret_tr = $(ret_frmMain).find("table.list_content_data td")
												.filter(function() {
													return $(this).find("input[name=value_lineNumber]").val() == lineNumber;
												}).closest("tr");

					if( ret_tr.length > 0 ) {
						var org_tr = $(document).find("form[name=frmMain] table.list_content_data td")
												.filter(function(){
													return $(this).find("input[name=value_lineNumber]").val() == lineNumber;
												}).closest("tr");

						var ret_barCode = $(ret_tr).find("input[name=itemConsumerEANCode]").val();
						var ret_spanItemLn = $(ret_tr).find("span[name=item-ln]");

						org_tr.replaceWith(ret_tr);
						loadBarCodeMultiItems(ret_barCode, ret_spanItemLn.get(0));

						var msg = $(result).find("#msg").html();
						if( msg ) {
							var org_itemCode = $(org_tr).find("input[name=value_itemCode]").val();
							var ret_itemCode = $(ret_tr).find("input[name=value_itemCode]").val();
							$(document).find("#msg").html(msg + "( "+ ret_barCode + " : " + org_itemCode + " -> " + ret_itemCode + " )" );
						}
					}
				},
				error: function(result) {
					customPopup.alert( { "header" : "<mtl:message key="ERR_INTERNAL_ERROR" encodeScript="true"/>" } );
				},
				complete: function(){
					reloadTree();
				}
			});
		}

		function totalCalculateTotalPrice() {
			if( Field.isArray(frmMain.value_lineNumber) ) {
				for( var i = 0; i < frmMain.value_lineNumber.length; i++ )
					calculateTotalPrice( i );
			} else {
				calculateTotalPrice();
			}
		}

		function partnerInfoReq( partyType, basePartyCode, partnerCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=info";

			if( !basePartyCode || basePartyCode == "" ) {
				if( partyType == "AG" )
					customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_SOLDPARTY" encodeScript="true"/>" } );
				else if( partyType == "WE" )
					customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_SOLDPARTY" encodeScript="true"/>" } );

				return;
			}

			if( !partnerCode || partnerCode == "" ) {
				if( partyType == "AG" )
					customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_SOLDPARTY" encodeScript="true"/>" } );
				else if( partyType == "WE" )
					customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_SHIPPARTY" encodeScript="true"/>" } );

				return;
			}

			url = replaceQueryValue( url, "partyCode", basePartyCode );
			url = replaceQueryValue( url, "organizationCode", frmHeader.organizationCode.value );
			url = replaceQueryValue( url, "displayLinkType", partyType );

			windowOpen( url + "&wintype=sub", "clsMng" );
		}

		function checkOrderLimit( obj ) {
			var orderQtyLowLimit = <%= com.irt.dpr.Order.PACKDEALORDER_LOW_LIMIT %>;

			if( Field.isArray(obj) ) {
				for(var i = 0; i < obj.length; i++) {
					if( frmMain.value_childLineNumber[i].value != 0 ) continue;
					if( frmMain.value_uom[i].value == "PC" && frmMain.value_orderQty[i].value < orderQtyLowLimit ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
					} else if( frmMain.value_uom[i].value != "PC" && (frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %>
							|| frmMain.value_orderQty[i].value < orderQtyLowLimit) ) {
						if( frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %> ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
						} else if( frmMain.value_orderQty[i].value < orderQtyLowLimit ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
						}
						frmMain.value_orderQty[i].select();
						frmMain.value_orderQty[i].focus();
						return false;
					}
				}
			} else if( frmMain.value_orderQty && frmMain.value_childLineNumber.value == 0 ) {
				if( frmMain.value_uom.value == "PC" && frmMain.value_orderQty.value < orderQtyLowLimit ) {
					customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
				} else if( frmMain.value_uom.value != "PC" && (frmMain.value_orderQty.value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %>
							|| frmMain.value_orderQty.value < orderQtyLowLimit) ) {
					if( frmMain.value_orderQty.value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %> ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
					} else if( frmMain.value_orderQty.value < orderQtyLowLimit ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
					}
					frmMain.value_orderQty.select();
					frmMain.value_orderQty.focus();
					return false;
				}
			}

			return true;
		}

		function uploadReq() {
			if( !checkRegisteredOrderHeader() ) return;

			var url = "<%= systemConfig.getClassURL() %>/DPRPackDealOrder?mode=iup&uploadType=ORD";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "divisionCode", "<mtl:value id="header" key="divisionCode"/>" );
			url = replaceQueryValue( url, "dealCode", "<mtl:value id="header" key="dealCode" encodeScript="true"/>" );

			windowOpen( url + "&wintype=sub", "clsMng" );
		}


		/**
			check if barcode has changed
			if same then return true;
			if not same then show alert and return false;

			after apply ajax this function is deprecated...
			should delete when commit to svn repository
		*/
		function checkBarCodeMultiItemsSame( obj ) {
			if( Field.isArray(obj) ) {
				for(var i = 0; i < obj.length; i++) {
					if( $(frmMain.value_itemCode[i]).val() === $(frmMain.value_itemCodeNew[i]).val() ) {
						continue;
					} else {
						customPopup.alert( { "header" : "<mtl:message key="ERR_BARCODE_CHANGED_SUGGEST_UPDATE" encodeScript="true"/>" } );
						frmMain.value_itemCodeNew[i].focus();
						return false;
					}
				}
			} else if( frmMain.value_itemCode.value && $(frmMain.value_itemCodeNew).val() ) {
				if( frmMain.value_itemCode.value === $(frmMain.value_itemCodeNew).val() ) {
					return true;
				}
			}

			return true;
		}

		/**
		   tree frame(named 'menu') is loaded slow sometimes,
		   so this function checks barcode data in tree frame constantly
		   and if found, refresh detail table in 'content' frame accordingly.
		*/
		function loadBarCodeMultiItemsToTable() {
			var count = 0;
			var intervalMillis = 500;
			var maxCount = 240;// 500 * 2 * 120  = 120sec
			var repeat  = setInterval(function(){
				count++;
				var found = false;

				if( parent.window.menu ) {
					var box = $(parent.frames["menu"].document).find("span#barCodeMultiItems");
					if( box.length > 0 ) {
						found = true;
					}
				}

				if( count > maxCount || found == true ) {
					clearInterval(repeat);
					reloadBarCodeMultiItemsToTable();
				}
			}, intervalMillis);
		}

		function loadBarCodeMultiItems( barCode, target ) {
			var itemCode = $(target).siblings("input[name=value_itemCode]").val();
			var itemCodeNew = $(target).siblings("input[name=value_itemCodeNew]").val();
			var barCode = $(target).siblings("input[name=itemConsumerEANCode]").val();
			var lineNumber = $(target).siblings("input[name=value_lineNumber]").val();

			if( barCodeMultiItems && barCodeMultiItems[barCode] ) {
				var items = barCodeMultiItems[barCode];

				var sel = $("<select/>");
				for( var i in items ) {
					var opt = $("<option/>").val(items[i]).text(items[i]);
					if( items[i] === itemCode ) {
						opt.attr("selected", "selected");
					}
					sel.append( opt.get(0).outerHTML );
				}
				sel.attr("onchange", "JavaScript:submitLineUpdate('"+ lineNumber +"');Field.modified(this);");
				sel.attr("name", "value_itemCodeNew");

				var selhtml = sel.get(0).outerHTML;
				if( target ) {
					$(target).html(selhtml);
					return selhtml;
				} else {
					return selhtml;
				}
			}
		}

		function reloadBarCodeMultiItemsToTable() {
			function reloadTableAndTree() {
				$("#content_list").find("table.list_content_data tr").each(function(index,object) {
					var barCode = $(this).find("input[name=itemConsumerEANCode]").val();
					var spanItemLn = $(this).find("span[name=item-ln]");

					if( window['barCodeMultiItems'] && barCodeMultiItems[barCode] ) {
						loadBarCodeMultiItems(barCode, spanItemLn.get(0));
					}
				});

				reloadTree();
			};

			if( parent.window.menu ) {
				var box = $(parent.frames["menu"].document).find("span#barCodeMultiItems");
				if( box.length > 0 && typeof box.attr("data-value") !== "undefined" ) {
					var jsonValue = $.parseJSON(box.attr("data-value"));
					if( !$.isPlainObject(jsonValue) ) {
						jsonValue = $.parseJSON(jsonValue);
					}
					if( jsonValue ) {
						window.barCodeMultiItems = jsonValue;
					} else {
						window.barCodeMultiItems = $.parseJSON("{}");
					}
				} else {
					window.barCodeMultiItems = $.parseJSON("{}");
				}
			} else {
				console.log("cannot find menu frame.");
			}

			reloadTableAndTree();
		}

		function getDetailsItems() {
			return $(parent.frames["content"].document)
				.find("form[name=frmMain]").find(".list_content_data* tr > td:nth-child(3)")
				.map(function(i,o){
					var itemObj = $(this);
					if( itemObj.find("select[name=value_itemCodeNew]").length > 0 ) {
						return itemObj.find("[name=value_itemCodeNew]").first().val();
					} else {
						return itemObj.find("[name=value_itemCode]").first().val();
					}
				});
		}

		function syncDetailsToRegularItems() {
			if( $(parent.frames["menu"].document).length > 0 ) {
				var addedItems = getDetailsItems();

				$(parent.frames["menu"].document)
					.find("#regularItemsPool").find("input")
					.each(function(index,object){
						if( $.inArray( this.value, addedItems ) > -1 ) {
							this.checked = true;
							this.disabled = true;
							$(this).addClass("details-added");
						} else {
							this.checked = "";
							this.disabled = "";
							$(this).removeClass("details-added");
						}
					});
			}
		}

		function reloadTree( reset ) {
			if( $(parent.frames["menu"].document).find("#content_list_itemtree").length > 0 ) {
				var inputtedItemCodes = $(parent.frames["content"].document)
					.find("table.list_content_data")
					.find("tr td > input[name=value_itemCode]")
					.map(function(index,object){return $(this).val();})
					.toArray();

				syncDetailsToRegularItems();

				$(parent.frames["menu"].document)
					.find("#content_list_itemtree li[name=li-lvl-1]")
					.find("li[name=li-lvl-2]").map(function(index,object) {
						var treeLi = $(this);

						if( reset ) {
							// reset all effects
							treeLi.parents("ul").css("display", "none");
							treeLi.parents("li.minus").removeClass("minus").addClass("plus");
							treeLi.find("span[name=checkItemsLabel]").removeClass("details-added");
							treeLi.find("span[name=checkItemsCount1]").html("");
							treeLi.find("span[name=checkItemsCount2]").html("");
						} else {
							// reset only selection effect on all items
							treeLi.find("span[name=checkItemsLabel]").removeClass("details-added");
							treeLi.find("span[name=checkItemsCount1]").html("");
							treeLi.find("span[name=checkItemsCount2]").html("");
						}

						return treeLi;
					}).filter(function(index){
						var itemCodeObj = $(this).find("input[name=checkItems]");
						if( itemCodeObj.length > 0 ) {
							var itemCode = itemCodeObj.val().split(";")[0];
							return inputtedItemCodes.indexOf(itemCode) > -1;
						}
						return false;
					}).each(function(index,object) {
						var treeLi = $(this);

						// apply all efects for checkedItems
						treeLi.find("> span[name=checkItemsLabel]").addClass("details-added");
						treeLi.find("input[name=checkItems]:checked").prop("checked","");
						treeLi.parents("li").removeClass("plus").addClass("minus");
						treeLi.parents("ul").css("display", "block");
					});
			}
		}

		function changeDealCode( selEl ) {
			Field.setValue(frmMain.dealCode, selEl.value);

			if( !Field.checkMandatory(frmHeader.organizationCode) ) return false;
			if( !Field.checkMandatory(frmHeader.distributionChannelCode) ) return false;
			if( !Field.checkMandatory(frmHeader.soldPartyCode) ) return false;

			var url = getLocationURL("url");
			url = replaceQueryValue( url, "organizationCode", encodeURIComponent(frmHeader.organizationCode.value) );
			url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(frmHeader.distributionChannelCode.value) );
			url = replaceQueryValue( url, "soldPartyCode", encodeURIComponent(frmHeader.soldPartyCode.value) );
			url = replaceQueryValue( url, "shipPartyCode", encodeURIComponent(frmHeader.shipPartyCode.value) );
			url = replaceQueryValue( url, "dealCode", encodeURIComponent(frmHeader.dealCode.value) );

			if( frmHeader.userRdd.disabled == false || frmHeader.userRdd.options.length > 1 ) {
				frmHeader.inDate.value = frmHeader.userRdd.value;
				if( frmHeader.inDate.value != "" ) {
					url = replaceQueryValue( url, "inDate", encodeURIComponent(frmHeader.inDate.value) );
				}
			}

			windowSelfOpen( url );
		}

		function getStopItemTitle( stopStart, stopEnd ) {
			var titlePrefix = "<mtl:message encodeScript="true" key="MSG_STOPITEM_TITLE_PREFIX"/>";
			return titlePrefix + decodeURIComponent(stopStart) + " - " + encodeURIComponent(stopEnd);
		};

		function getDealItemTitle( dealStart, dealEnd ) {
			var titlePrefix = "<mtl:message encodeScript="true" key="MSG_PACKDEALITEM_TITLE_PREFIX"/>";
			return titlePrefix + ' ' + decodeURIComponent(dealStart) + " - " + encodeURIComponent(dealEnd);
		}

		function getCloseItemTitle( closeTime ) {
			var titlePrefix = "<mtl:message encodeScript="true" key="MSG_CLOSEITEM_TITLE_PREFIX"/>";
			return titlePrefix + ' ' + decodeURIComponent(closeTime);
		};

		function markOrderLineAvailability() {
			$("[data-is-stop-item=true],[data-is-deal-item=true],[data-is-close-item=true]").each(function(o,i) {
				var obj = $(this);
				obj.attr("disabled", "disabled");
				if( obj.attr("data-is-stop-item") === "true" ) {
					obj.addClass("is-stop-item-label")
						.attr("title", getStopItemTitle($(this).attr("data-stop-start-date"), $(this).attr("data-stop-end-date")) );
				} else if( obj.attr("data-is-deal-item") === "true" ) {
					$(this).addClass("is-deal-item-label")
						.attr("title", getDealItemTitle($(this).attr("data-deal-start-date"), $(this).attr("data-deal-end-date")) );
				} else if( obj.attr("data-is-close-item") === "true" ) {
					$(this).addClass("is-close-item-label")
						.attr("title", getCloseItemTitle($(this).attr("data-close-time")) );
				}
				return;
			});
		}

		function removeOrderLineUnavailable() {
			$(frmMain.value_lineNumber).filter(function(i){ return this.value == "" || this.value == "null"; })
				.each(function(){ $(this).parent().closest("tr").remove(); });
		}

		function checkOrderLineZero() {
			$(frmMain.value_lineNumber)
				.each(function(i,o){
					if( frmMain.value_orderQty[i].value === "" || frmMain.value_orderQty[i].value === "0" ) {
						frmMain.listcheckbox[i].checked = true;
					}
				 });
		}

		function readConditionReq( type ) {
			$("#shipPartyCode").children( "option:not(:first)" ).remove();
			//$("#shipPartyCode").singleSelectmenu( "refresh" );
			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=stp";
			readPartyAttributeReq( url, type, frmHeader );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmHeader" fieldSetId="headerFieldSet" onSubmit="JavaScript: return checkHeaderInput();">
		<%@ include file="include_rbm_form.inc" %>
		<%@ include file="include_pub_input.inc" %>
		<%@ include file="include_pub_calendar.inc" %>
		<mtl:contentGroup groupId="order_header" type="search" style="position: relative;">
			<div id='messagebar'></div>
			<mtl:hidden id="header" key="orderKey"/>
			<mtl:hidden id="request" key="isContinueOrder"/>
			<input type='hidden' name='mode' value='uph'/>
			<input type='hidden' name='ordDate' value=<%= headerMap.get("orderDate")%>/>
			<mtl:hidden id="header" key="inDate" />
			<div class='accordion-button arrow-up' style='position: absolute; top: 25px; right: 20px;'></div>
			<h2><mtl:message key="jsp.dpr_order_input.SUBTITLE_INPUT_HEADER"/></h2>
			<div class='search-table table-fixed'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="orderDate" descriptionKey="FIELD_DPR_ORDER_ORDERDATE"/></div>
						<div class='field'>
							<mtl:value id="header" key="orderDate"/> <mtl:value id="header" key="orderDateDOW"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="inDate"/></div>
						<div class='field'>
							<mtl:select id="header" key="userRdd" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_USERRDD" searchable="false"
									listId="rddValues" listCodeKey="dateValue" listNameFormat="${dateValue}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="inDateDefault"/></div>
						<div class='field'>
							<mtl:value id="header" key="inDateDefault"/> <mtl:value id="header" key="inDateDefaultDOW"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode"/></div>
						<div class='field'>
							<mtl:select id="header" key="organizationCode" mandatory="true"  searchable="false"
									listId="organizations" listCodeKey="organizationCode"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SOLD\");"/>
						</div>
					</div>
				</div>
			</div>
			<div class='search-table table-fixed'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
						<div class='field'>
							<mtl:select id="header" key="distributionChannelCode" mandatory="true"  searchable="false"
									listId="distributionChannels" listCodeKey="distributionChannelCode"
									listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="soldPartyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE" mandatory="true"/></div>
						<div class='field'>
							<mtl:select id="header" key="soldPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
									modified="readConditionReq(\"SHIP\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="shipPartyCode" descriptionKey="FIELD_DPR_SHIPPARTY_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="header" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
									listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
									modified="predefinedRdd();"/>
						</div>
					</div>
					<div class='cell'></div>
				</div>
			</div>
			<div class='search-table table-fixed'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="dealCode" descriptionKey="FIELD_DPR_PACKDEAL" mandatory="true"/></div>
						<div class='field'>
							<mtl:select id="header" key="dealCode" nullValueKey="MSG_COND_DPR_PACKDEAL_CODE" hasBlank="true"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_PACKDEALCODE"
									listId="packdeals" listCodeKey="dealCode" listNameFormat="$f{pure(dealCode)}"
									modified="changeDealCode(this);" searchable="false"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="status"/></div>
						<div class='field'><mtl:valuef id="header" format="${DPR_ORDER_STATUS_@status}"/></div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
			</div>
		<mtl:contains id="header" key="dealCode">
			<mtl:hidden id="packdeal" key="isFulfilled"></mtl:hidden>
			<mtl:hidden id="packdeal" key="isPackdealDate"></mtl:hidden>
			<div id='content_packdeal' class='search-table table-fixed'>
			<% if( sessionMng.isSystemAdmin() ) { %>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="inDateSimulation"/></div>
						<div class='field-info'><mtl:value id="header" key="inDateSimulation"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="simulationKey"/></div>
						<div class='field-info'><mtl:value id="header" key="inDateSimulation"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="orderKey"/></div>
						<div class='field-info'><mtl:value id="header" key="orderKey"/></div>
					</div>
					<div class='cell'></div>
				</div>
			<% } %>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="targetTotalAmount" descriptionKey="FIELD_DPR_PACKDEAL_TARGETTOTAL_AMOUNT"/></div>
						<div class='field-info'>
							<mtl:hidden id="packdeal" key="targetTotalAmount"/>
							<mtl:valuef id="packdeal" key="targetTotalAmount" format="${targetTotalAmount#NF.CURRENCY}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="toleranceRate" descriptionKey="FIELD_DPR_PACKDEAL_TOLERANCE_RATE"/></div>
						<div class='field-info'>
							<span id="toleranceRate"><mtl:valuef id="packdeal" key="toleranceRate" format="${toleranceRate#NF.PERCENT;%}"/></span>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="orderNetAmount" descriptionKey="jsp.dpr_order_result.FIELD_ESTIMATED_NETAMOUNT"/></div>
						<div class='field-info'>
							<span id="orderNetAmount">
								<mtl:value id="packdeal" key="orderNetAmount"/>
							</span>
							<span id="fillRate"></span>
						</div>
					</div>
					<div class='cell'></div>
				</div>
			</div>
		</mtl:contains>
			<div class='search-bottom'>
			<% if( htmlpage.hasManageAuth() ) { %>
				<div class='table-cell search-button'>
				<% if( "WK".equals(status) || "ER".equals(status) || "SD".equals(status) || (headerMap.get("orderKey") == null && status == null) ) { %>
					<mtl:ifvalue id="packdeal" key="isPackdealDate" value="Y">
						<mtl:ifvalue id="property" key="hasInitPackdealItem" notValue="N">
						<% if( (headerMap.get("orderKey") == null && status == null) ) { %>
							<mtl:button id="sync" type="button" styleClass="primary" icon="images/ico_update_white.png" onClick="JavaScript: simulationPackdeal();" messageKey="jsp.BTN_ORDER_HEADER_SYNC"/>
						<% } else { %>
							<mtl:button id="sync" type="button" styleClass="primary" icon="images/ico_update_white.png" onClick="JavaScript: syncSubmitDetail();();" messageKey="jsp.BTN_ORDER_HEADER_SYNC"/>
						<% } %>
						</mtl:ifvalue>
					</mtl:ifvalue>
				<% } %>
					<mtl:ifvalue id="header" key="orderKey" value="">
						<mtl:button type="reset"/>
					</mtl:ifvalue>
				</div>
			<% } %>
			</div>
		</mtl:contentGroup>
	</mtl:form>

	<script type="text/javascript" id="applyOrderInputValue">
		var MONEY_DECIMAL = "<%=(("JNJAP_KR".equals(sessionMng.getPartyId()) || "JNJAP_VN".equals(sessionMng.getPartyId())) ? "0" : "2")%>";
		/*
		* formatMoney( 1234567.1234, 2, ".", ","); -> "1,234,567.12"
		*/
		function formatMoney(n, c, d, t) {
			var c = isNaN(c = Math.abs(c)) ? 2 : c,
			d = d == undefined ? "." : d,
			t = t == undefined ? "," : t,
			s = n < 0 ? "-" : "",
			i = String(parseInt(n = Math.abs(Number(n) || 0).toFixed(c))),
			j = (j = i.length) > 3 ? j % 3 : 0;

			return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
		};

		function getCalcPackSize( row ) {
			var packSize;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				packSize = frmMain.packSize[row].value;
			} else {
				packSize = frmMain.packSize.value;
			}

			var simPackSize;
			if( frmMain['simulationPackSize'] ) {
				if( Field.isArray(frmMain.value_lineNumber) ) {
					simPackSize = frmMain.simulationPackSize[row].value;
				} else {
					simPackSize = frmMain.simulationPackSize.value;
				}
			}
			var crePackSize;
			if( frmMain['confirmedPackSize'] ) {
				if( Field.isArray(frmMain.value_lineNumber) ) {
					crePackSize = frmMain.confirmedPackSize[row].value;
				} else {
					crePackSize = frmMain.confirmedPackSize.value;
				}
			}

			var calcPackSize;
			if( typeof crePackSize !== "undefined" ) {
				calcPackSize = crePackSize;
			} else {
				if( typeof simPackSize !== "undefined" ) {
					calcPackSize = simPackSize;
				} else {
					calcPackSize = packSize;
				}
			}
			return calcPackSize;
		}

		function calculateOrderLine( row ) {
			var packSize, orderQty, discountRate;
			var uomPrice, singlePrice;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				uomPrice = frmMain.uomPrice[row].value;
				packSize = frmMain.packSize[row].value;
				orderQty = frmMain.value_orderQty[row].value;
				discountRate = parseFloat(frmMain.discountRate[row].value);
			} else {
				uomPrice = frmMain.uomPrice.value;
				packSize = frmMain.packSize.value;
				orderQty = frmMain.value_orderQty.value;
				discountRate = parseFloat(frmMain.discountRate.value);
			}

			var calcPackSize = getCalcPackSize( row );

			if( Field.isArray(frmMain.value_lineNumber) ) {
				singlePrice = ( frmMain.singlePrice[row].value ? frmMain.singlePrice[row].value : uomPrice/calcPackSize );
			} else {
				singlePrice = ( frmMain.singlePrice.value ? frmMain.singlePrice.value : uomPrice/calcPackSize );
			}

			var totalPrice;
			if( isFinite(singlePrice) && isFinite(calcPackSize) && isFinite(orderQty) )
				totalPrice = eval( singlePrice * calcPackSize * orderQty );
			else
				totalPrice = 0;

			var finalPrice;
			if( totalPrice != 0 ) {
				/*
				if( isFinite(discountRate) )
					finalPrice = Math.floor( eval( totalPrice * (100-discountRate)/100 ) );
				else
				*/

				finalPrice = Math.floor( eval( totalPrice ) );
			} else
				finalPrice = 0;

			var orderFinalValueObj;
			var orderValueObj;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				orderValueObj = $(frmMain).find(".orderValue")[row];
				orderFinalValueObj = $(frmMain).find(".orderFinalValue")[row];
			} else {
				orderValueObj = $(frmMain).find(".orderValue")[0];
				orderFinalValueObj = $(frmMain).find(".orderFinalValue")[0];
			}

			$(orderValueObj).val(totalPrice).text( formatMoney(totalPrice, MONEY_DECIMAL) );
			$(orderFinalValueObj).val(finalPrice).text( formatMoney(finalPrice, MONEY_DECIMAL) );
		}

		function calculateOrderNetAmount() {
			if( Field.isArray(frmMain.value_lineNumber) ) {
				for( var i = 0; i < frmMain.value_lineNumber.length; i++ )
					calculateOrderLine( i );
			} else {
				calculateOrderLine();
			}
			var netAmt = 0;
			$(frmMain).find(".orderFinalValue").each( function(i, o) {
				netAmt += new Number($(this).val());
			});

			$("#content_packdeal").find("#orderNetAmount").val( netAmt ).text( formatMoney(netAmt, MONEY_DECIMAL) );

			var tgtTotal = $("#content_packdeal").find("[name=targetTotalAmount]").val();
			fillRate = eval( netAmt / tgtTotal * 100 );
			var tolRate = new Number( $("#content_packdeal").find("#toleranceRate").text().replace(/(\d+%)/, "") );

			var isFulfilled = isPackdealFulfilled( fillRate, tolRate );
			if( isFulfilled == true ) {
				$("#content_packdeal").find("[name=isFulfilled]").val( "Y" );
			} else {
				$("#content_packdeal").find("[name=isFulfilled]").val( "N" );
			}

			/* noty({text: ("tgtTotal: " + tgtTotal + " tolRate: "+ tolRate + " isFulfilled: "+ isFulfilled) , timeout: 3000}); */

			$("#content_packdeal").find("#fillRate").val(fillRate).text( "("+ parseFloat(fillRate).toFixed(2) + "%)" )
			.each(function(i,o){
				if( isFulfilled == false || isFulfilled == null ) {
					$(o).css("color", "red").css("font-weight", "bold");
				} else {
					$(o).css("color", "blue").css("font-weight", "bold");
				}
			});
			;
		}

		function simulationPackdeal( dealCode ) {
			function syncSubmitHeader() {

				if( !checkHeaderInput() ) {
					return;
				}

				frmHeader.url.value = getLocationURL();

				var url = "<%= systemConfig.getClassURL() %>/DPRPackDealOrder?mode=uph";

				var formArray = $(frmHeader).serializeArray();

				var self = this;
				self.dfd = $.Deferred();

				$.ajax({
					url: url,
					data: formArray,
					dataType: 'html',
					method: 'POST',
					success: function(data, status, xhr) {
						if( data ) {

							var errMsg = $(xhr.responseText).find("table.info_content td.error").html();
							if( errMsg ) {
								noty({type: 'error', text: errMsg, closeWith:['button']});
								self.dfd.resolve(null);
							} else {
								var orderKey = $(data).find("input[name=orderKey]");
								if( orderKey.length > 0 ) {
									orderKey =  orderKey[0];
									if( orderKey && orderKey.value ) {
										frmHeader.orderKey.value = orderKey.value;
										frmMain.orderKey.value = orderKey.value;
										self.dfd.resolve(true);
									} else {
										self.dfd.resolve(null);
									}
								} else {
									self.dfd.resolve(null);
								}
							}
						}
//						return;
					},
					error: function(xhr, status, errorThrown) {
					},
				})

				return self.dfd.promise();
			}

			if( dealCode )
				frmHeader.dealCode.value = dealCode;

			syncSubmitHeader().then(function(resolved){
				if( resolved == true ) {
					frmMain.dealCode.value = dealCode;
					return true;
				}
				return false;
			}).then(function(resolved) {
				if( resolved == true ) {
					$(frmMain.value_orderQty).each(function(i,o) { Field.release( o ); } );
					frmMain.isFirstSim.value = "Y";
					simulationWithUpdate( false );
				}
				$("form[name=frmHeader] #btn_submit").prop("disabled", false);
				return false;
			});
		}
	</script>

	<mtl:form name="frmMain" onSubmit="JavaScript: return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<%@ include file="include_pub_list.inc" %>
		<mtl:hidden id="header" key="orderKey"/>
		<mtl:hidden id="header" key="dealCode"/>
		<input type='hidden' name='mode' value='upd'/>
		<input type='hidden' name='type'/>
		<mtl:hidden id="request" key="isFirstSim"/>

		<mtl:contentGroup groupId="list" type="list">
			<h2><mtl:message key="jsp.dpr_order_input.SUBTITLE_ORDER_INFO_LIST"/></h2>

			<%
				final java.util.Map final_headerMap = headerMap;
				final String allowUOM = (property.getProperty( "allowUOM" ) == null
									? com.irt.rbm.RBMSystem.getSystemEnv("DPR","uom;"+(String)headerMap.get("organizationCode"), com.irt.dpr.Party.DEFAULT_UOM)
									: property.getProperty("allowUOM"));

				com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage ) {

					private int lineIndex = 0;

					public String getColumnValue( Column column, java.util.Map recordMap, int row, int col ) {
						String fieldKey = column.getFieldKey();
						StringBuffer sbuf = new StringBuffer();

						boolean isStopItem = ( "Y".equals(recordMap.get("isStopItem")) );
						boolean isCloseItem = ( "Y".equals(recordMap.get("isCloseItem")) );
						boolean isSslBase = ( "Y".equals(recordMap.get("isSslBase")) );
						boolean isSslOrder = ( "Y".equals(recordMap.get("isSslOrder")) );

						Object lineNumber = recordMap.get( "lineNumber" );
						if( lineNumber == null ) {
							Object displaySequence = recordMap.get("packdealDisplaySeq");
							if( displaySequence != null ) {
								if( displaySequence instanceof String ) {
									lineNumber = Integer.valueOf((String)displaySequence) * 10;;
								} else if( displaySequence instanceof Number ) {
									lineNumber = ((Number)displaySequence).intValue() * 10;
								}
							}

							if( lineNumber != null ) {
								if( isStopItem == true || isCloseItem == true || isSslBase == false || isSslOrder == false ) {
								} else {
									recordMap.put("lineNumber", (++lineIndex) * 10);
								}
							}
						}

						String status = com.irt.data.Record.extractString( final_headerMap, "status" );
						if( "itemCode".equals(fieldKey) ) {
							sbuf.append( "<input type='hidden' name='value_itemCode'" )
								.append( " value='"+ recordMap.get(fieldKey) +"'/>" )
								.append( "<input type='hidden' name='value_lineNumber' value='"+ recordMap.get("lineNumber") +"'/>" )
								.append( "<input type='hidden' name='value_itemCodeConfirmed' value='"+ recordMap.get("itemCodeConfirmed") +"'/>" )
								.append( "<input type='hidden' name='value_itemRefInd' value='"+ recordMap.get("itemRefInd") +"'/>" )
								.append( "<input type='hidden' name='value_childLineNumber' value='"+ (recordMap.get("childLineNumber") == null ? "0": recordMap.get("childLineNumber")) +"'/>" )
								.append( "<input type='hidden' name='discountRate' value='"+ recordMap.get("packdealDiscountRate") +"'/>" )
								.append( "<input type='hidden' name='packSize' value='"+ recordMap.get("packSize") +"'/>" )
								.append( "<input type='hidden' name='itemConsumerEANCode' value='"+ recordMap.get("itemConsumerEANCode") +"'/>" );

							if( recordMap.get("price") != null )
								sbuf.append( "<input type='hidden' name='value_price' value='"+ HtmlUtility.toHtmlString(recordMap.get("price"))+"'/>" );

							sbuf.append( "<input type='hidden' name='isStopItem' value='"+ HtmlUtility.toHtmlString(recordMap.get("isStopItem")) +"'/>" );

							if( "SD".equals(status) || "CD".equals(status) ) {
								String simQty = HtmlUtility.toHtmlString(recordMap.get("simulationOrderQty"));
								if( simQty == null || simQty.length() == 0 )
									simQty = "0";

								sbuf.append( "<input type='hidden' name='value_simulationUOM' value='"+ HtmlUtility.toHtmlString(recordMap.get("simulationUOM")) +"'/>" )
									.append( "<input type='hidden' name='value_simulationOrderQty' value='"+ simQty +"'/>" )
									.append( "<input type='hidden' name='value_simulationOrderValue' value='"+ HtmlUtility.toHtmlString(recordMap.get("simulationOrderValue")) +"'/>" );

								sbuf.append( "<input type='hidden' disabled='disabled' name='pdRmnDay' value='"+ HtmlUtility.toHtmlString(recordMap.get("pdRmnDay")) +"'/>" )
									.append( "<input type='hidden' disabled='disabled' name='pdRmnMonth' value='"+ HtmlUtility.toHtmlString(recordMap.get("pdRmnMonth")) +"'/>" );
							}

							if( recordMap.get("simulationPackSize") != null )
								sbuf.append( "<input type='hidden' name='simulationPackSize' value='"+ recordMap.get("simulationPackSize") +"'/>" );
							if( recordMap.get("confirmedPackSize") != null )
								sbuf.append( "<input type='hidden' name='confirmedPackSize' value='"+ recordMap.get("confirmedPackSize") +"'/>" );

							Object simUom = (recordMap.get("packdealSimUomFirst")==null
											? (recordMap.get("simulationUOM")==null ? "" : recordMap.get("simulationUOM"))
											: recordMap.get("packdealSimUomFirst"));
							Object uomPrice = (recordMap.get("packdealSimPriceFirst")==null
											? (recordMap.get("price")==null ? "" : recordMap.get("price"))
											: recordMap.get("packdealSimPriceFirst"));

							sbuf.append( "<input type='hidden' disabled='disabled' name='simPriceFirst' value='"+ HtmlUtility.toHtmlString(recordMap.get("packdealSimPriceFirst")) +"'/>" );
							sbuf.append( "<input type='hidden' disabled='disabled' name='simUomFirst' value='"+ HtmlUtility.toHtmlString(recordMap.get("packdealSimUomFirst")) +"'/>" );

							sbuf.append( "<input type='hidden' disabled='disabled' name='singlePrice' value='"+ HtmlUtility.toHtmlString(recordMap.get("unitPrice")) +"'/>" );
							sbuf.append( "<input type='hidden' disabled='disabled' name='uomPrice' value='"+ uomPrice +"'/>" );

							String itemLineId = "item-ln-" + recordMap.get("lineNumber");
							sbuf.append( "<span id='"+ itemLineId +"' name='item-ln'" )
								.append( " data-is-stop-item='"+ isStopItem +"'" )
								.append( " data-stop-start-date='"+ HtmlUtility.toHtmlString(recordMap.get("stopStartDate"))+"'" )
								.append( " data-stop-end-date='"+ HtmlUtility.toHtmlString(recordMap.get("stopEndDate"))+"'" )
								.append( " data-is-close-item='"+ isCloseItem +"'" )
								.append( " data-close-time='"+ HtmlUtility.toHtmlString(recordMap.get("ordCloseTime"))+"'" )
								.append( " data-is-ssl-base='"+ isSslBase +"'" )
								.append( " data-is-ssl-order='"+ isSslOrder +"'" );
							if( isStopItem )
								sbuf.append( " class='is-stop-item-label'" );
							if( isCloseItem )
								sbuf.append( " class='is-close-item-label'" );
							sbuf.append( ">" );
							sbuf.append( "<input type='hidden' name='value_itemCodeNew' value='"+ recordMap.get("itemCode") +"'/>"
									+ column.format(recordMap, msghandler) );
							sbuf.append( "</span>" );
						} else if( "uom".equals(fieldKey) ) {
							String[] allowUOMValues = allowUOM.split( "," );
							Object uom = recordMap.get( "uom" );
							java.util.List<java.util.Map> uoms = (java.util.List<java.util.Map>)recordMap.get("uoms");
							Object salesUnit = recordMap.get("salesUnit");

							sbuf.append( "<select id='value_uom" + row + "' name='value_uom'>" );
							if( salesUnit != null && ((String)salesUnit).length() > 0 && !"null".equals(salesUnit) ) {
								sbuf.append( "<option value='"+ salesUnit + "'" )
									.append( ">"+ salesUnit +" / "+ HtmlUtility.toHtmlString(recordMap.get("packSize")) +"</option>" );
							} else {
								uom = (uom == null ? allowUOMValues[0] : uom);
								boolean selected = false;
								for( int i = 0; i < allowUOMValues.length; i++ ) {
									if( !selected ) selected = allowUOMValues[i].equals( uom );
									Object uomPackSize = recordMap.get("packSize");
									if( uoms != null ) {
										for( java.util.Map uomMap : uoms ) {
											if( recordMap.get("itemCode").equals(uomMap.get("itemCode")) && allowUOMValues[i].equals(uomMap.get("uomCode")) ) {
												uomPackSize = uomMap.get("packSize");
												break;
											}
										}
									}
									sbuf.append( "<option value='"+ allowUOMValues[i] +"' "+ (allowUOMValues[i].equals(uom) ? "selected" : "") )
										.append( ">"+ allowUOMValues[i] +" / "+ HtmlUtility.toHtmlString(uomPackSize) + "</option>" );
								}
							}
							sbuf.append( "</select>" );
							sbuf.append( "<script>" );
							sbuf.append( "	$( function() {" );
							sbuf.append( "		var options = $.extend( {}, smallSelectmenuOptions, { searchable: false } );" );
							sbuf.append( "		$(\"#value_uom" + row + "\").singleSelectmenu( options );" );
							sbuf.append( "	});" );
							sbuf.append( "</script>" );
						} else if( "pdRmnMaxQty".equals(fieldKey) ) {
							sbuf.append( "<input type='hidden' disabled='disabled' name='pdRmnMaxQty' value='"+ HtmlUtility.toHtmlString(recordMap.get("pdRmnMaxQty")) +"'/>" );

							sbuf.append( "<span class='text_pdRmnMaxQty'" );
							sbuf.append( ">" + column.format(recordMap, msghandler) + "</span>" );
						} else if( "itemName".equals(fieldKey) ) {
							sbuf.append( "<span class='itemname'" );
							sbuf.append( ">" + column.format(recordMap, msghandler) + "</span>" );
						} else if( "infoUOM".equals(fieldKey) ) {
							Object uom = (recordMap.get(fieldKey) == null ? "" : recordMap.get(fieldKey) );

							sbuf.append( "<input type='hidden' name='"+fieldKey+"' value='"+ uom +"'/>" );
							sbuf.append( "<span>"+ uom +" / " + recordMap.get("confirmedPackSize") + "</span>" );
						} else if( "simulationUOM".equals(fieldKey) ) {
							Object uom = (recordMap.get(fieldKey) == null ? "" : recordMap.get(fieldKey) );

							sbuf.append( "<input type='hidden' name='"+fieldKey+"' value='"+ uom +"'/>" );
							sbuf.append( "<span>"+ uom +" / " + recordMap.get("simulationPackSize") + "</span>" );
						} else if( "salesUnit".equals(fieldKey) ) {
							sbuf.append( "<span class='salesUnit'>"+ column.format(recordMap, msghandler) +"</span>" );
						} else if( "orderQty".equals(fieldKey) ) {
							String orderQty = com.irt.data.Record.extractString( recordMap, "orderQty" );
							String simulationOrderQty = com.irt.data.Record.extractString( recordMap, "simulationOrderQty" );

							sbuf.append( "<input type='text' name='value_orderQty'" );
							if( !htmlpage.hasManageAuth() )
								sbuf.append( " class='input-field small readonly'" );
							else if( "SD".equals(status) && orderQty != null && orderQty.length() > 0 && !orderQty.equals(simulationOrderQty) )
								sbuf.append( " class='input-field small alert' style='background-color:#FF9934;'" );
							else if( isStopItem )
								sbuf.append( " class='input-field small readonly'" );
							else
								sbuf.append( " class='input-field small'" );

							String _orderQty = HtmlUtility.toHtmlString(recordMap.get(fieldKey));
							if( isStopItem )
								_orderQty = "0";
							else if( _orderQty == null || _orderQty.length() == 0 )
								_orderQty = "1";
							else if( "SD".equals(status) ) {
								Integer intOrderQty = (orderQty != null ? Integer.parseInt(orderQty) : null);
								Integer intSimQty = (simulationOrderQty != null ? Integer.parseInt(simulationOrderQty) : null);
								if( intOrderQty == null || (intSimQty != null && intOrderQty > intSimQty) )
									_orderQty = simulationOrderQty;
							}
							sbuf.append( " value='"+ HtmlUtility.toHtmlString(_orderQty) +"'" );

							boolean isDisabled = ( "CD".equals(status) || status == null );
							if( isDisabled )
								sbuf.append( " disabled='disabled'" );

							sbuf.append( " onBlur='JavaScript:checkOrderInputValue("+ row +"); JavaScript:calculateOrderLine("+row+");'" );
							sbuf.append( htmlpage.hasManageAuth() ? "" : "readonly='true'" );
							sbuf.append( " onChange='JavaScript:Field.modified(this); JavaScript:calculateOrderNetAmount();'/>" );

							sbuf.append( "<input type='hidden' name='value_tmp_orderQty' value='"+ HtmlUtility.toHtmlString(recordMap.get(fieldKey)) +"'/>" );

						} else if( "orderValue".equals(fieldKey) ) {
							sbuf.append( "<span class='orderValue'>"+ column.format(recordMap, msghandler) +"</span>" );
						} else if( "orderFinalValue".equals(fieldKey) ) {
							sbuf.append( "<span class='orderFinalValue'>"+ column.format(recordMap, msghandler) +"</span>" );
						} else
							return column.format( recordMap, msghandler );

						return sbuf.toString();
					}
			};
				listwriter.setNumbering(false);
				listwriter.setRecords( (java.util.List)pageContext.findAttribute("details") );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
				<% if( htmlpage.hasManageAuth() ) { %>
					<% if( listwriter.containsData() ) { %>
						<% if( "SD".equals(status) || "ER".equals(status) || "WK".equals(status) ) { %>
							<mtl:button type="reset" messageKey="jsp.BTN_RESET2"/>
							<mtl:button type="save" styleClass="secondary" icon="images/ico_save.png"/>
							<mtl:button id="simulation" type="button" onClick="JavaScript: simulationWithUpdate();" icon="images/ico_submit_white.png"
									messageKey="jsp.BTN_SIMULATION" styleClass="primary"/>
						<% } %>
						<% if( "SD".equals(status) && !"CD".equals(status) ) { %>
							<mtl:button id="placeorder" type="button" onClick="JavaScript: creation();" icon="images/ico_add_order_white.png"
									styleClass="primary" messageKey="jsp.BTN_PLACE_ORDER"/>
						<% } %>
					<% } %>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
			</div>

			<script type='text/javascript'>
				function checkHeaderInput() {
					<%= htmlpage.getValidationScript() %>
					frmHeader.url.value = getLocationURL();

					if( !frmHeader.userRdd.disabled && frmHeader.userRdd.options.length > 1 ) {
						frmHeader.inDate.value = frmHeader.userRdd.value;
					}

					var orderDate = DateField.convertValueToDate( "<mtl:value id="header" key="orderDate"/>", "ymd" );
					var inDate = DateField.convertValueToDate( frmHeader.inDate.value, "ymd" );
					var inDateDefault = DateField.convertValueToDate( "<mtl:value id="header" key="inDateDefault"/>", "ymd" );
					if( !inDate ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_INDATE_ABOUT_MANDATORY"/>" } );
						focusForm( frmHeader, frmHeader.inDate );
						return false;
					}
					if( orderDate >= inDate ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_INDATE_ABOUT_ORDERDATE"/>" } );
						focusForm( frmHeader, frmHeader.inDate );

						return false;
					} else if( inDateDefault != null && inDateDefault != "" && inDateDefault > inDate ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_INDATE_ABOUT_DEFAULTINDATE"/>" } );
						focusForm( frmHeader, frmHeader.inDate );

						return false;
					}

					var orderKey = getOrderKey();
					var status = "<mtl:value id="header" key="status"/>";
					if( (orderKey != null && orderKey != "") && (status == null || status == "") ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_ORDER_STATUS"/>" } );

						return false;
					} else if( "SD" == status ) {
// 						if( confirm("<mtl:message key="MSG_RETURN_WORKSHEET" encodeScript="true"/>") )
							frmHeader.dealCode.disabled = false;
							frmMain.dealCode.value = frmHeader.dealCode.value;
							return submitInput();
// 						else
// 							return false;
					} else if( "SG" == status || "CG" == status || "CD" == status ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_CANNOT_UPDATE"/>" } );

						return false;
					}

					return submitInput();
				}

				function isPackdealFulfilled( fillRate, tolRate, multiple ) {
					var tgtTotal = parseFloat( $("#content_packdeal").find("[name=targetTotalAmount]").val() );
					var netAmt = parseFloat( $("#content_packdeal").find("#orderNetAmount").val() );
					if( !fillRate ) {
						netAmt = 0;
						$(frmMain).find( ".orderFinalValue" ).each( function(i, o) {
							netAmt += new Number( $(this).val() );
						});
						fillRate = eval( netAmt / tgtTotal * 100 );
					}
					if( !tolRate ) {
						tolRate = parseFloat( $("#content_packdeal").find("#toleranceRate").text() );
					}

					if( multiple ) {
						var tolAmt = tgtTotal * (tolRate/100)* multiple;
						if( tgtTotal*multiple - tolAmt <= netAmt && tgtTotal*multiple + tolAmt >= netAmt )
							return true;
					} else {
						var ret = false;
						multiple = Math.floor(fillRate/100);
						if( multiple == 0 ) {
							if( isPackdealFulfilled( fillRate, tolRate, 1 ) ) {
								ret = true;
							} else {
								return null;
							}
						} else {
							if( isPackdealFulfilled( fillRate, tolRate, multiple ) ) {
								ret = true;
							} else if( isPackdealFulfilled( fillRate, tolRate, multiple+1 ) ) {
								ret = true;
							}
						}
						if( ret ) return ret;
					}

					return null;
				}

				function checkPackdealFulfilled( fillRate, tolRate ) {
					var isFulfilled = isPackdealFulfilled( fillRate, tolRate );
					if( ! isFulfilled ) {
						customPopup.alert( { "header" : "<mtl:valuef id='packdeal' format='%{ERR_INVALID_PACKDEALAMOUNT_TOLERANCE_1,${toleranceRate}}' />" } );
						return false;
					}

					return true;
				}

				function checkInput() {
					if( !frmMain.listcheckbox ) return false;

					removeOrderLineUnavailable();

					var obj = frmMain.listcheckbox;
					var url = getLocationURL();
					if( window.parent.isContinueOrder )
						url += "&isContinueOrder=Y";
					frmMain.url.value = url;

					var status = "<mtl:value id="header" key="status"/>";
					if( status == null || status == "" ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_ORDER_STATUS"/>" } );

						return false;
					} else if( "SG" == status || "CG" == status || "CD" == status ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_CANNOT_UPDATE"/>" } );

						return false;
					}

					if( checkDetailOrderValues() && checkOrderLimit(obj)  ) {
						return submitInput();
					} else
						return false;
				}

				function checkDetailOrderValues( checkSimQty ) {
					var orderQtyObj, simQtyObj;
					var childLineNumber;
					if( Field.isArray(frmMain.value_lineNumber) ) {
						orderQtyObj = frmMain.value_orderQty;
						simQtyObj = frmMain.value_simulationOrderQty;
						childLineNumber = frmMain.value_childLineNumber;
					} else {
						orderQtyObj = new Array( frmMain.value_orderQty );
						simQtyObj = new Array( frmMain.value_simulationOrderQty );
						childLineNumber = new Array( frmMain.value_childLineNumber );
					}
					for( var i = 0; i < orderQtyObj.length; i++ ) {
						if( childLineNumber[i].value != 0 ) continue;
						if( !isFinite(orderQtyObj[i].value) || orderQtyObj[i].value < 0 ) {
							focusForm( frmMain, orderQtyObj[i] );
							return false;
						} else if( orderQtyObj[i].value.length > 0 && orderQtyObj[i].value.indexOf(' ') > 0 ) {
							focusForm( frmMain, orderQtyObj[i] );
							return false;
						} else if( checkSimQty == true ) {
							if( orderQtyObj[i].value.length > 0 && simQtyObj[i] && simQtyObj[i].value.length > 0 ) {
								if( new Number(orderQtyObj[i].value) > new Number(simQtyObj[i].value) ) {
									customPopup.alert( { "header" : "<mtl:message key="ERR_PACKDEALORDER_INPUTQTY_GT_SIMULATIONQTY" encodeScript="true"/>" } );
									focusForm( frmMain, orderQtyObj[i] );
									return false;
								}
							}
						}
					}

					return true;
				}
			</script>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
