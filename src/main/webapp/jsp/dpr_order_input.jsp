<%--
	File Name:	dpr_order_input.jsp
	Version:	2.2.19

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2023/07/27		2.2.19  Credit Info 기능 개발, salesUnit default값 적용
	jbaek		2023/04/28		2.2.18	orderQtyValue 에 focus 시에 값이 0인 경우에 빈값으로 변경
	hankalam	2021/11/30		2.2.17	신규 UI/UX 적용
	hankalam	2021/04/30		2.2.16	usePredefinedRDD 환경변수가 Y일 경우에만 userRDD 적용되도록 변경
	hankalam	2021/01/29		2.2.15	Max 수량이 0인 경우 메시지 표시 추가
	jbaek		2020/03/30		2.2.14	오류수정.
	hankalam	2020/06/30		2.2.14	헤더에 오더타입 선택가능하도록 수정
	hankalam	2019/10/30		2.2.13	한국 경우: inDate를 Predefined RDD 우선 적용.
	jbaek		2019/06/28		2.2.13	OrderClose 적용. salesUnit 적용.
	hankalam	2019/06/28		2.2.13	Predefined RDD 적용
	jbaek		2019/05/30		2.2.13	soldparty, shipparty select box에 값이 1개일시 자동으로 선택하도록 변경.
	jbaek		2018/10/30		2.2.12	isChinaCountry() 삭제
	jbaek		2018/04/30		2.2.11	barCodeMultiItem, regularItemsList 기능 추가
	jbaek		2018/04/30		2.2.11	No refresh in SKU list 적용
	jbaek		2018/03/31		2.2.10	reOrder할때  inDate업데이트하여 sim시에 새 ordDate에 맞는 inDate(RDD)가 사용되도록 수정
	hankalam	2017/09/31		2.2.9	UOM이 PC일 경우 MAX LIMIT 수량 제한 없앰.
	hankalam	2017/05/31		2.2.8	같은 sold to, ship to 인 Order 상태가 Creating 일 때 중복 발주가 안되도록 메시지 출력 로직 추가
	hankalam	2017/02/28		2.2.7	Party 별 UOM 선택 기능 추가
										Plant Exclusion 적용
	hankalam	2016/06/03		2.2.6	Upload Input 매뉴얼 버튼 추가
	jbaek		2014/02/16		2.2.5	Plant SKU 제외 기능 개발
	jbaek		2013/01/30		2.2.4	PIPO 기능 개발
	jbaek		2011/11/30		2.2.3	inDate를 사용자 입력가능 여부 설정( ChinaCountry는 사용자 입력불가 )
	lsinji		2010/07/09		2.2.2	JJC, SJJ의 uom를 CSE로 설정
	lsinji		2009/12/11		2.2.1	orderQty에 공백이 있는 경우 alter.
	guksm		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='utf-8' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map<String, Object> headerMap = (java.util.Map<String, Object>)pageContext.findAttribute( "header" );
	String status = null;
	if( headerMap != null && headerMap.containsKey("status") )
		status = com.irt.data.Record.extractString( headerMap, "status" );

	String organizationCode = null;
	if( headerMap != null ) {
		organizationCode = (String)headerMap.get( "organizationCode" );
	}
	boolean useInputRDD = com.irt.dpr.Country.isFeature( organizationCode, "useInputRDD" );
	boolean usePredefinedRdd = com.irt.dpr.Country.isFeature( organizationCode, "usePredefinedRDD" );
	boolean isReOrder = (request.getParameter("reOrder") != null && "Y".equals(request.getParameter("reOrder")));

	String invalidItem = property.getProperty( "invalidItem" );
	String hasCreatingOrder = property.getProperty( "hasCreatingOrder" );
	boolean resetUseSelectRdd = "Y".equals( property.getProperty("resetUseSelectRdd") );
%>
<head>
<%@ include file="include_rbm_header.inc" %>
	<style type="text/css">
		span.is-stop-item-label {
			text-decoration: line-through;
			color: red;
		}

		.is-moq-exceed {
			background-color: silver;
		}

		.ui-selectmenu-button.selectmenu-small {
			width: 100%;
			border: 1px solid #C6C6C6;
			border-radius: 4px;
			background-color: #FFFFFF;
			line-height: 26px;
			text-align: left;
			white-space: nowrap;
			padding: 0px 5px;
			-webkit-box-sizing: border-box;
			-moz-box-sizing: border-box;
			box-sizing: border-box;
		}

		input[type=text].input-field.readonly {
			background-color: #E6E6E6;
			color: #63666A;
		}

		.wrap-credit-status.list_content_data_scroll {
			border-bottom: 0px solid #C6C6C6
		}
		.wrap-credit-status tr.list_content {
			border: 1px solid #D8D8D8;
		}
		.wrap-credit-status .field-title {
			margin-bottom: 1px;
		}
		.wrap-credit-status .field {
			line-height: 24px;
		}
	</style>
	<script type='text/javascript'>
		$(function() {
			$(".itemNameTooltip").tooltip({
				show: false,
				hide: false
			});

			syncFrameHeight();

			$("#content_order_header .search-table").find( ".field" ).each( function(index, item) {
				if( $(item).children().length < 1 && $(item).text().trim() === "" ) {
					$(item).text( "–" );
				}
			});

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
				syncFrameHeight();
			});

			$("#upload-info .circle-close").click( function() {
				$(this).closest( "#upload-info" ).hide();
				syncFrameHeight();
			});

			$(window).resize( function () {
				syncFrameHeight();
			});
		});

		function syncFrameHeight() {
			if( $("div.list_content .list-function, div.frame-content .search-bottom").height() > 36 ) {
				$("div.list_content .list-function button, div.frame-content .search-bottom button").css( "margin-bottom", "10px" );
				$("div.list_content, div.frame-content").css( "padding-bottom", "15px" );
			}

			if( parent.$("iframe.menu-content") ) {
				var height = $(".frame-content-wrap").height();
				parent.$("iframe.menu-content").contents().find( ".frame-content").innerHeight( height );
				parent.$("div.content").height( height + 30 );
			}
		}

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

		function getAllowUOM() {
			return "<%=property.getProperty("allowUOM")%>";
		}

		var orderInd = "Y";
		var resetUseSelectRdd = <%= resetUseSelectRdd %>;

		window.FeatureEnabled = {
			barCodeMultiItems: '<%=com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "Feature;BarCodeMultiItems", false)%>',
			regularItemsList: '<%=com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "Feature;RegularItemsList", false)%>',
		};

		function setSelectSingleValue( selEl ) {
			if( selEl && !selEl.value ) {
				if( selEl.options ) {
					if( selEl.value == "" ) {
						if( selEl.options[0].value == "" && selEl.options.length == 2 ) {// has nullValueKey
							Field.setValue( selEl, selEl.options[1].value, true );
							if( $(selEl).singleSelectmenu("instance") ) {
								$(selEl).singleSelectmenu( "refresh" );
							}
						} else if( selEl.options[0].value != "" && selEl.options.length == 1 ) {// no nullValueKey
							Field.setValue( selEl, selEl.options[0].value, true );
							if( $(selEl).singleSelectmenu("instance") ) {
								$(selEl).singleSelectmenu( "refresh" );
							}
						}
					}
				}
			}
		}

		function bodyLoad() {
			resetForm( frmHeader );

			var mode = "<mtl:value id="request" key="mode"/>";
			var organizationCode = "<mtl:value id="header" key="organizationCode"/>";
			var orderKey = "<mtl:value id="header" key="orderKey"/>";

			if( organizationCode != null && organizationCode != "" && orderKey != null && orderKey != "" ) {
				if( parent.frames["menu_content"] && $(parent.frames["menu_content"].document).find("#content_list_itemtree").length == 0 ) {
					reloadItemTreeMenu();
				} else {
					if( parent.$(".content-overlay .loading").is(":visible") ) {
						toggleLoadingParent( false );
					}
				}
				loadBarCodeMultiItemsToTable();
			}

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
				<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") ) { %>
				predefinedRdd();
			<% } %>
				toggleDisabled( true, [ frmHeader.organizationCode, frmHeader.distributionChannelCode
										, frmHeader.soldPartyCode, frmHeader.shipPartyCode, frmHeader.userRdd ] );
			<% if( com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDangerousItem") ) { %>
				toggleDisabled( true, frmHeader.orderType );
				<% } %>
			}

			$(frmMain.value_orderQty).each(function(i){ checkOrderInputValue(i); })

			<% if( "Y".equals(invalidItem) ) { %>
				customPopup.alert( { "header" :"<mtl:message key="jsp.dpr_order_input.MSG_INVALID_ITEM"/>" } );
			<% } %>

			if( frmHeader.organizationCode && frmHeader.organizationCode.value
					&& frmHeader.distributionChannelCode && frmHeader.distributionChannelCode.value ) {
				setSelectSingleValue( frmHeader.soldPartyCode );
				if( (orderKey == null || orderKey == "") && frmHeader.soldPartyCode.value != "" ) {
					readConditionReq( "SHIP" );
				}
			}

			if( resetUseSelectRdd ) {
				toggleDisabled( false, frmHeader.userRdd );
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
			}
		}

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

		function checkOrderInputValue( row ) {
			var orderQtyObj, rgRmnMaxQtyObj;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				orderQtyObj = frmMain.value_orderQty[row];
				rgRmnMaxQtyObj = (frmMain['rgRmnMaxQty'] ? frmMain.rgRmnMaxQty[row] : null);
			} else {
				orderQtyObj = frmMain.value_orderQty;
				rgRmnMaxQtyObj = (frmMain['rgRmnMaxQty'] ? frmMain.rgRmnMaxQty : null);
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

			if( rgRmnMaxQtyObj ) {
				var calcPackSize = getCalcPackSize( row );
				if( rgRmnMaxQtyObj.value ) {
					var uomRmnQty = parseInt( rgRmnMaxQtyObj.value / calcPackSize );
					$(orderQtyObj).attr("data-moq-rmn-max", uomRmnQty);

					$(rgRmnMaxQtyObj).parent().find("span.text_rgRmnMaxQty").text( uomRmnQty );

					var itemCode = ( Field.isArray(frmMain.value_lineNumber) ? frmMain.value_itemCode[row] : frmMain.value_itemCode ).value;
					if( uomRmnQty == 0 ) {
						orderQtyObj.value = 0;
						Field.lock( orderQtyObj );
						$(orderQtyObj).removeAttr( "onBlur" );
						$(orderQtyObj).attr( "onfocus", "JavaScript:checkOrderInputValue("+ row +"); JavaScript:calculateOrderLine("+row+")" );
						noty( {type:'warning', timeout: 8000, text:
							("<mtl:message key="ERR_ZEROMOQ" encodeScript="true"/>"
									+ " ( " + itemCode + " )") } );
					} else if( orderQtyObj.value > uomRmnQty ) {
						orderQtyObj.value = uomRmnQty;
						$(orderQtyObj).addClass("alert").closest("td").addClass("is-moq-exceed");

						var itemCode = ( Field.isArray(frmMain.value_lineNumber) ? frmMain.value_itemCode[row] : frmMain.value_itemCode ).value;

						noty( {type:'warning', timeout: 5000, text:
						("<mtl:message key="ERR_MAXORDQTY_EXCEED" encodeScript="true"/>"
								+ "( " + itemCode + " : " + uomRmnQty + ' ' + "<mtl:message key="jsp.MSG_UOM_QTY" encodeScript="true"/>" + " )") } );

						return;
					}
				}
			}
			$(orderQtyObj).closest("td").removeClass("is-moq-exceed");
		}

		function calculateOrderLine( row ) {
			var packSize, orderQty, discountRate;
			var uomPrice, singlePrice;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				uomPrice = frmMain.uomPrice[row].value;
				packSize = frmMain.packSize[row].value;
				orderQty = frmMain.value_orderQty[row].value;
				discountRate = (frmMain['discountRate'] ? parseFloat(frmMain.discountRate[row].value) : 0);
			} else {
				uomPrice = frmMain.uomPrice.value;
				packSize = frmMain.packSize.value;
				orderQty = frmMain.value_orderQty.value;
				discountRate = (frmMain['discountRate'] ? parseFloat(frmMain.discountRate.value) : 0);
			}

			var calcPackSize = getCalcPackSize( row );

			if( Field.isArray(frmMain.value_lineNumber) ) {
				singlePrice = ( (frmMain['singlePrice'] && frmMain.singlePrice[row].value) ? frmMain.singlePrice[row].value : uomPrice/calcPackSize );
			} else {
				singlePrice = ( (frmMain['singlePrice'] && frmMain.singlePrice.value) ? frmMain.singlePrice.value : uomPrice/calcPackSize );
			}

			var totalPrice;
			if( isFinite(singlePrice) && isFinite(calcPackSize) && isFinite(orderQty) )
				totalPrice = eval( singlePrice * calcPackSize * orderQty );
			else
				totalPrice = 0;

			var finalPrice;
			if( totalPrice != 0 ) {
				if( isFinite(discountRate) )
					finalPrice = Math.floor( eval( totalPrice * (100-discountRate)/100 ) );
				else
					finalPrice = Math.floor( eval( totalPrice ) );
			} else
				finalPrice = 0;

			var orderFinalValueObj;
			var orderValueObj;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				orderValueObj = $(frmMain).find("span.orderValue")[row];
				orderFinalValueObj = $(frmMain).find("[name=orderFinalValue")[row];
			} else {
				orderValueObj = $(frmMain).find("span.orderValue")[0];
				orderFinalValueObj = $(frmMain).find("[name=orderFinalValue]")[0];
			}

			$(orderValueObj).val(totalPrice).text( formatMoney(totalPrice, MONEY_DECIMAL) );
			$(orderFinalValueObj).val(finalPrice).text( formatMoney(finalPrice, MONEY_DECIMAL) );
		}

		function calculateTotalPrice( row ) {
			var unitPrice, packSize, orderQty;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				unitPrice = frmMain.unitPrice[row].value;
				packSize = frmMain.packSize[row].value;
				orderQty = frmMain.value_orderQty[row].value;
			} else {
				unitPrice = frmMain.unitPrice.value;
				packSize = frmMain.packSize.value;
				orderQty = frmMain.value_orderQty.value;
			}

			var totalPrice;
			if( isFinite(unitPrice) && isFinite(packSize) && isFinite(orderQty) )
				totalPrice = eval( unitPrice * packSize * orderQty );
			else
				totalPrice = 0;

			if( Field.isArray(document.all.totalPrice) )
				document.all.totalPrice[row].innerHTML = totalPrice;
			else
				document.all.totalPrice.innerHTML = totalPrice;
		}

		function creation() {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			customPopup.confirm( { "detail" : "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_CREATION" encodeScript="true"/>" }, function(res) {
				if( res ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=wait";
					url = replaceQueryValue( url, "type", "cre" );
					url = replaceQueryValue( url, "orderKey", encodeURIComponent(getOrderKey()) );

					var msg = document.getElementById( "msg" );
					if( msg ) msg.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_CREATING" encodeScript="true"/>";

					var btn_creation = document.getElementById( "btn_ordercreate" );
					if( btn_creation ) btn_creation.disabled = true;

					windowSelfOpen( url );
				}
			});
		}

		function checkRegisteredOrderHeader() {
			if( getOrderKey() == null || getOrderKey() == "" ) {
				customPopup.alert( { "header" : "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
				return false;
			}

			return true;
		}

		function deleteReq() {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			var url = getRequestMultiURL( "<%= systemConfig.getClassURL() %>/DPRPlaceOrder", "rmd", frmMain.listcheckbox, "orderKey,lineNumber" );
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL("url")) );

			if( window.parent.isContinueOrder )
				url += "&isContinueOrder=Y";

			if( frmMain.odrdlvGroup && frmMain.odrdlvGroup.value ) {
				url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );
			}

			if( checkURLLength(url) && confirm( "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" ) ) {

				noty({ type:'info', text: "<mtl:message key="jsp.dpr_order_input.MSG_DELETING" encodeScript="true"/>" });
				syncSubmitDetail().then(function(resolved) {
					if(resolved == true ) {
						windowOpen( url );
					}
					return false;
				});
			}
		}

		function downloadReq() {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			if( !checkRegisteredOrderHeader() ) return;

			var url =  "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=down&dwntype=item";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="header" key="shipPartyCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
			url = replaceQueryValue( url, "basisValue", "ITEM" );
		<% if( com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDangerousItem") ) { %>
			url = replaceQueryValue( url, "orderType", "<mtl:value id="header" key="orderType"/>" );
		<% } %>
			if( frmMain.odrdlvGroup && frmMain.odrdlvGroup.value ) {
				url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );
			}

			windowOpen( url );
		}

		function focusedOrderInputValue( row ) {
			var orderQtyObj;
			if( Field.isArray(frmMain.value_lineNumber) ) {
				orderQtyObj = frmMain.value_orderQty[row];
			} else {
				orderQtyObj = frmMain.value_orderQty;
			}
			if( orderQtyObj.value == 0 ) {
				orderQtyObj.value = ""
			}
		}

		function getOrderKey() {
			return "<mtl:value id="header" key="orderKey"/>";
		}

		function loadTemplate( templateKey ) {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			if( !checkRegisteredOrderHeader() ) return;
			if( templateKey == null || templateKey == "" ) return;

			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=ltp";
			url = replaceQueryValue( url, "orderKey", encodeURIComponent(getOrderKey()) );
			url = replaceQueryValue( url, "templateKey", encodeURIComponent(templateKey) );
			if( window.parent.isContinueOrder )
				url += "&isContinueOrder=Y";

			windowSelfOpen( url, getLocationURL() );
		}

		function loadTemplateReq() {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			if( getOrderKey() == null || getOrderKey() == "" ) {
				customPopup.alert( { "header" : "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
				return;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPROrderTemplate?mode=list";
			//url = replaceQueryValue( url, "wintype", "sub" );
			windowOpen( url, "sub-content" );
		}

		function readConditionReq( type ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=stp";
			readPartyAttributeReq( url, type, frmHeader );
		}

	<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") ) { %>
		function predefinedRdd() {
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
			windowOpen( url, "subwin_rdd" );
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
				for( var i = 0; i < options.length; i++ ) {
					optionObj = document.createElement( "option" );
					var dayOfWeekIndex = new Date( options[i] ).getDay();

					optionObj.value = options[i];
					optionObj.innerHTML = options[i] + " " + dayOfWeeks[dayOfWeekIndex];
					obj.appendChild( optionObj );
					if( selectIndex < 1 ) {
						if( options.length == 1 ) {
							selectIndex = 1;
						} else if( frmHeader.inDate.value == options[i] ) {
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
	<% } %>

		function checkOrderInd( ordInd ) {
			orderInd = ordInd;
			if( ordInd == "N" ) {
				customPopup.alert( { "header" : "<%= HtmlUtility.toScriptString( msghandler.getMessage("jsp.dpr_order_input.MSG_CANNOT_PLACE_ORDER_HOLIDAY") ) %>" } );
			}
		}

		function saveTemplateReq() {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			var obj = frmMain.listcheckbox;
			if( getOrderKey() == null || getOrderKey() == "" ) {
				customPopup.alert( { "header" : "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
				return;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPROrderTemplate?mode=ireg";
			url = replaceQueryValue( url, "orderKey", encodeURIComponent(getOrderKey()) );
			url = replaceQueryValue( url, "wintype", "sub" );

			if ( checkOrderLimit( obj ) )
				windowOpen( url, "sub-content" );
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
		}

		function setTradePartners( type, soldOptions, shipOptions ) {
			if( "sold" == type )
				setTradePartner( type, frmHeader.soldPartyCode, soldOptions );
			else if( "ship" == type )
				setTradePartner( type, frmHeader.shipPartyCode, shipOptions );
		}

		function simulationWithUpdate() {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			var obj = frmMain.listcheckbox;

			if( typeof obj == "undefined" || !checkDetailOrderValues() || !checkOrderLimit(obj) )
				return;

			customPopup.confirm( { "detail" : "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_SIMULATION" encodeScript="true"/>" }, function(res) {
				if( res ) {
					frmMain.mode.value = "wait";
					frmMain.type.value = "sim";
					frmMain.target = "_parent";

					var btn_simulation = document.getElementById( "btn_simulation" );
					if( btn_simulation ) btn_simulation.disabled = true;

					frmMain.submit();
				}
			});
		}

		function submitLineUpdate( lineNumber ) {
			if( !checkOrderLimit(frmMain.listcheckbox) )
				return;

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
				success: function(data, status, xhr) {
					var result = xhr.responseText;
					var errMsg = $(result).find("table.info_content td.error").html();
					if( errMsg ) {
						noty({type: 'error', text: errMsg, timeout: 5000, closeWith:['button']});
						return;
					}

					var ret_frmMain = $(result).filter(function(){ return $(this).attr("name") == "frmMain"; })
					var ret_tr = $(ret_frmMain).find("table.list_content_xy_data_f td")
												.filter(function() {
													return $(this).find("input[name=value_lineNumber]").val() == lineNumber;
												}).closest("tr");

					if( ret_tr.length > 0 ) {
						var org_tr = $(document).find("form[name=frmMain] table.list_content_xy_data_f td")
												.filter(function(){
													return $(this).find("input[name=value_lineNumber]").val() == lineNumber;
												}).closest("tr");

						var ret_barCode = $(ret_tr).find("input[name=itemConsumerEANCode]").val();
						var ret_spanItemLn = $(ret_tr).find("span.item-ln");

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
			url = replaceQueryValue( url, "distributionChannelCode", frmHeader.distributionChannelCode.value );
			url = replaceQueryValue( url, "displayLinkType", partyType );

			windowOpen( url + "&wintype=sub", "clsMng" );
		}

		function checkOrderLimit( obj ) {
			if( Field.isArray(obj) ) {
				for(var i = 0; i < obj.length; i++) {
					if( frmMain.value_childLineNumber[i].value != 0 ) continue;
					if( frmMain.value_uom[i].value == "PC" && frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
					} else if( frmMain.value_uom[i].value != "PC" && (frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %>
							|| frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %>) ) {
						if( frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %> ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
						} else if( frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
						}
						frmMain.value_orderQty[i].select();
						frmMain.value_orderQty[i].focus();
						return false;
					}
				}
			} else if( frmMain.value_orderQty && frmMain.value_childLineNumber.value == 0 ) {
				if( frmMain.value_uom.value == "PC" && frmMain.value_orderQty.value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
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

		function uploadReq() {
			if( resetUseSelectRdd ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_order_input.MSG_USER_RDD_UPDATE" encodeScript="true"/>" } );
				return false;
			}

			if( !checkRegisteredOrderHeader() ) return;

			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=iup&uploadType=ORD";
			url = replaceQueryValue( url, "orderKey", getOrderKey() );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "divisionCode", "<mtl:value id="header" key="divisionCode"/>" );

			if( frmMain.odrdlvGroup && frmMain.odrdlvGroup.value ) {
				url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );
			}

			url = replaceQueryValue( url, "wintype", "sub" );

			windowOpen( url, "sub-content" );
		}

		function uploadInputManualReq() {
			var url = "<%= systemConfig.getBaseURL(htmlpage.getLocale())%>uploadManual/upload_input_manual.html";
			window.open( url, "window"
					, "toolbar=no, status=no, location=no, directories=no, menubar=no, scrollbars=yes, "
						+ " width=1030, height=700, top=0, left=0" );
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

				if( parent.window.menu_content ) {
					var box = $(parent.frames["menu_content"].document).find("span#barCodeMultiItems");
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

			if( barCodeMultiItems[barCode] ) {
				var items = barCodeMultiItems[barCode];

				var sel = $("<select/>");
				for( var i in items ) {
					if( parent["menu_content"] ) {
						var treeItem = $(parent["menu_content"].document).find("#tree-checkmenu").find("[name=checkItems]")
							.filter(function(i) {
								var obj = $(this);
								var itmcd = obj.val().split(";")[0];
								if( obj.val().length > 0 && itmcd && items[i] ) {
									return itmcd.trim() === items[i].trim();
								}
								return false;
							});
						if( treeItem.length > 0 ) {
							if( treeItem.attr("data-is-deal-item") == "true" ) {
								continue;
							}
							if( treeItem.attr("data-is-stop-item") == "true" ) {
								continue;
							}
							if( treeItem.attr("data-is-close-item") == "true" ) {
								continue;
							}
						}
					}
					var opt = $("<option/>").val(items[i]).text(items[i]);
					if( items[i] === itemCode ) {
						opt.attr("selected", "selected");
					}
					sel.append( opt.get(0).outerHTML );
				}
				sel.attr("onchange", "JavaScript: submitLineUpdate('"+ lineNumber +"');");
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
				$("#content_list").find("table.list_content_xy_data_f tr").each(function(index,object) {
					var barCode = $(this).find("input[name=itemConsumerEANCode]").val();
					var spanItemLn = $(this).find("span.item-ln");

					if( window['barCodeMultiItems'] ) {
						if( barCodeMultiItems[barCode] ) {
							loadBarCodeMultiItems(barCode, spanItemLn.get(0));
						}
					}
				});

				if( $("select[name=value_itemCodeNew]").length ) {
					var options = $.extend( {}, smallSelectmenuOptions, { searchable: false } );
					$("select[name=value_itemCodeNew]").singleSelectmenu( options );
				}

				reloadTree();
			};

			if( parent.window.menu_content ) {
				var box = $(parent.frames["menu_content"].document).find("span#barCodeMultiItems");
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
			return $(parent.frames["main_content"].document)
				.find("form[name=frmMain]").find(".list_content_xy_data_f* tr > td:nth-child(3)")
				.map(function(i,o){
					var itemObj = $(this);
					if( itemObj.find("select[name=value_itemCodeNew]").length > 0 ) {
						return itemObj.find("[name=value_itemCodeNew]").first().val();
					} else {
						return itemObj.find("[name=value_itemCode]").first().val();
					}
				});
		}

		function syncSubmitDetail() {
//			removeOrderLineUnavailable();

			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=upd";

			frmMain.url.value = getLocationURL("url");

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
			})

			return self.dfd.promise();
		}

		function syncDetailsToRegularItems() {
			if( parent.frames["menu_content"] && $(parent.frames["menu_content"].document).length > 0 ) {
				var addedItems = getDetailsItems();

				$(parent.frames["menu_content"].document)
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

		function changeOrderDeliveryGroup( el, value ) {
			if( window.parent.frames["menu_content"] ) {
				if( el ) {
					if( value ) {//system called.
						el.value = value;
					} else {//user selected.
						if( !confirm("<mtl:message key="jsp.dpr_order_input.MSG_ODRDLVGRP_CHANGE" encodeScript="true"/>") ) {
							return false;
						} else {
							if( frmMain.listcheckbox ) {
								CheckBox.checkAll( frmMain.listcheckbox );
								var url = getRequestMultiURL( "<%= systemConfig.getClassURL() %>/DPRPlaceOrder", "rmd", frmMain.listcheckbox, "orderKey,lineNumber" );
								url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
								location.replace( url );
							}
						}
					}

					if( window.parent.frames["menu_content"] && window.parent.frames["menu_content"].document.all.odrdlvGroup ) {
						window.parent.frames["menu_content"].document.all.odrdlvGroup.value = el.value;
					}
					reloadItemTreeMenu();
				}
			}
		};

		function getOrderDeliveryGroup() {
			return $(parent.frames["menu_content"].document).find("input[name=odrdlvGroup]").val();
		};

		function reloadItemTreeMenu() {
			// replace item tree
			var url = "<%= systemConfig.getClassURL() %>/DPRItem";
			if( parent.window.menu_content && parent.window.menu_content.getLocationURL )
				url = parent.window.menu_content.getLocationURL();

			url = attachDefaultParameter( url );
			url = replaceQueryValue( url, "mode", "tree" );
			url = replaceQueryValue( url, "type", "ord" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="header" key="organizationCode" />" );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
			url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="header" key="shipPartyCode"/>" );
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="header" key="orderKey" />" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="header" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "orderType", "<mtl:value id="header" key="orderType" />" );
			var requestType = "<mtl:value id="request" key="rtype"/>";
			if( requestType != null && requestType != "" )
				url = replaceQueryValue( url, "rtype", requestType );
			if( frmMain.odrdlvGroup && frmMain.odrdlvGroup.value )
				url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );

			window.open( url, "menu_content" );
		}

		function reloadTree( reset ) {
			if( parent.frames["menu_content"] && $(parent.frames["menu_content"].document).find("#content_list_itemtree").length > 0 ) {
				var inputtedItemCodes = $(parent.frames["main_content"].document)
					.find("table.list_content_xy_data_f")
					.find("tr td input[name=value_itemCode]")
					.map(function(index,object){return $(this).val();})
					.toArray();

				syncDetailsToRegularItems();

				$(parent.frames["menu_content"].document)
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
							$(this).find( "input[name=checkItems]" ).prop( "checked", "" ).removeAttr( "disabled" );
						} else {
							// reset only selection effect on all items
							treeLi.find("span[name=checkItemsLabel]").removeClass("details-added");
							treeLi.find("span[name=checkItemsCount1]").html("");
							treeLi.find("span[name=checkItemsCount2]").html("");
							$(this).find( "input[name=checkItems]" ).prop( "checked", "" ).removeAttr( "disabled" );
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
						$(this).find( "> input[name=checkItems]" ).prop( "checked", "checked" ).attr( "disabled", "disabled" );
						treeLi.find("> label span[name=checkItemsLabel]").addClass("details-added");
						treeLi.parents("li").removeClass("plus").addClass("minus");
						treeLi.parents("ul").css("display", "block");
					});

				parent.frames["menu_content"].markCheckItemsAvailability();
			}
		}

	</script>
</head>

<body class='content' style='padding-right: 8px; overflow: hidden;'>
	<%@ include file="include_rbm_frame_bodyheader.inc" %>
	<iframe name='subwin_rdd' style='display: none' src='<%= systemConfig.getProperty("baseURL") %>blank.html'></iframe>
	<div class='frame-content-wrap'>
		<mtl:form name="frmHeader" fieldSetId="headerFieldSet" onSubmit="JavaScript: return checkHeaderInput();">
			<%@ include file="include_rbm_form.inc" %>
			<%@ include file="include_pub_input.inc" %>
			<%@ include file="include_pub_calendar.inc" %>
			<mtl:hidden id="header" key="orderKey"/>
			<mtl:hidden id="request" key="isContinueOrder"/>
			<input type='hidden' name='mode' value='uph'/>
			<input type='hidden' name='ordDate' value="<%=headerMap.get("orderDate")%>" />
			<mtl:contentGroup groupId="order_header" type="content" styleClass="frame-content" descriptionKey="jsp.dpr_order_input.GRP_ORDER_HEADER">
				<div id='messagebar'></div>
				<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
				<div class='accordion-button arrow-up' style='position: absolute; top: 25px; right: 20px;'></div>

				<h2><mtl:message key="jsp.dpr_order_input.SUBTITLE_INPUT_HEADER"/></h2>

				<div class='search-table table-fixed'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderDate" descriptionKey="FIELD_DPR_ORDER_ORDERDATE"/></div>
							<div class='field'>
							<% if( !useInputRDD ) { %>
								<mtl:hidden id="header" key="inDate" />
							<% } %>
								<mtl:value id="header" key="orderDate"/> <mtl:value id="header" key="orderDateDOW"/>
							</div>
						</div>
						<% if( useInputRDD || usePredefinedRdd ) { %>
						<div class='cell'>
							<div class='field-title'><mtl:title key="inDate"/></div>
							<div class='field'>
							<% if( useInputRDD ) { %>
								<mtl:date id="header" key="inDate"/>
							<% } else { %>
								<mtl:select id="header" key="userRdd" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_USERRDD" searchable="false"
										listId="rddValues" listCodeKey="dateValue" listNameFormat="${dateValue}"/>
							<% } %>
							</div>
						</div>
						<% } %>
						<div class='cell'>
						<% if( "SG".equals(status) || "SD".equals(status) ) { %>
							<% if( isReOrder ) { %>
								<div class='field-title'><mtl:title key="inDateDefault"/></div>
								<div class='field'>
									<mtl:value id="header" key="inDateDefault"/> <mtl:value id="header" key="inDateDefaultDOW"/>
								</div>
							<% } else { %>
								<div class='field-title'><mtl:title key="inDateSimulation"/></div>
								<div class='field'>
									<mtl:value id="header" key="inDateSimulation"/> <mtl:value id="header" key="inDateSimulationDOW"/>
								</div>
							<% } %>
						<% } else if( "CG".equals(status) || "CD".equals(status) ) { %>
							<div class='field-title'><mtl:title key="inDateConfirm"/></div>
							<div class='field'>
								<mtl:value id="header" key="inDateConfirm"/> <mtl:value id="header" key="inDateConfirmDOW"/>
							</div>
						<% } else { %>
							<div class='field-title'><mtl:title key="inDateDefault"/></div>
							<div class='field'>
								<mtl:value id="header" key="inDateDefault"/> <mtl:value id="header" key="inDateDefaultDOW"/>
							</div>
						<% } %>
						</div>
					</div>

					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
								<mtl:select id="header" key="organizationCode" mandatory="true" searchable="false"
										listId="organizations" listCodeKey="organizationCode"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SOLD;SHIP\");"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
							<div class='field'>
								<mtl:select id="header" key="distributionChannelCode" mandatory="true"  searchable="false"
										listId="distributionChannels" listCodeKey="distributionChannelCode"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SOLD;SHIP\");"/>
							</div>
						</div>
					<% if( com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDangerousItem") ) { %>
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderType"/></div>
							<div class='field'>
								<mtl:select id="header" key="orderType" searchable="false"  prefixKey="DPR_ORDER_ORDERTYPE_" codeValues="NO,DA" defaultValue="NO" />
							</div>
						</div>
					<% } %>
					</div>
				</div>

				<div class='search-table table-fixed'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'>
								<mtl:title key="soldPartyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE" mandatory="true"/>
							</div>
							<div class='field'>
								<mtl:select id="header" key="soldPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
										modified="readConditionReq(\"SHIP\");"/>
							</div>
						</div>
						<div class='cell' style='width: 15px; padding-left: 5px;'>
							<div class='field-title' style='height: 16px; color: #FFFFFF;'>-</div>
							<div class='field-info'>
								<a href="JavaScript: partnerInfoReq('AG', $(frmHeader.soldPartyCode).val(), $(frmHeader.soldPartyCode).val());" class='party-info'></a>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'>
								<mtl:title key="shipPartyCode" descriptionKey="FIELD_DPR_SHIPPARTY_CODE" mandatory="true"/>
							</div>
							<div class='field'>
							<% if( usePredefinedRdd ) { %>
								<mtl:select id="header" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
										listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
										modified="predefinedRdd();"/>
							<% } else { %>
								<mtl:select id="header" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
										listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>
							<% } %>
							</div>
						</div>
						<div class='cell' style='width: 15px; padding-left: 5px;'>
							<div class='field-title' style='height: 16px; color: #FFFFFF;'>-</div>
							<div class='field-info'>
								<a href="JavaScript: partnerInfoReq('WE', $(frmHeader.soldPartyCode).val(), $(frmHeader.shipPartyCode).val());" class='party-info'></a>
							</div>
						</div>
					<% if( !com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") ) { %>
						<div class='cell'>
							<div class='cell'>
								<div class='field-title'><mtl:title key="status"/></div>
								<div class='field'><mtl:valuef id="header" format="${DPR_ORDER_STATUS_@status}"/></div>
							</div>
						</div>
					<% } %>
					</div>
				</div>

				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") ) { %>
				<div class='search-table table-fixed'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:message key="jsp.dpr_order_input.FIELD_CUSTOMER_ORDER_NUMBER"/></div>
							<div class='field'>
								<mtl:text id="header" key="customerOrderNumber" placeholder="MSG_DPR_ORDER_NOTCONATINS_CHARACTER"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:message key="jsp.dpr_order_input.FIELD_ORDER_NUMBER"/></div>
							<div class='field'><mtl:value id="header" key="orderNumber"/></div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="status"/></div>
							<div class='field'><mtl:valuef id="header" format="${DPR_ORDER_STATUS_@status}"/></div>
						</div>
					</div>
				</div>
				<% } %>

				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useCreditStatus") ) { %>
				<mtl:contains id="creditStatusList">
					<div class='search-table table-fixed' style='margin-top:-25px;padding-bottom:2px;'>
						<div class='content' style='min-height:50px;'>
							<mtl:contentGroup groupId="creditStatus" type="info" style="width: 100%; min-width: 400px; margin-top: 1px; margin-bottom: 1px; padding-top: 2px; padding-bottom: 2px;">
								<div class='wrap-credit-status list_content_data_scroll' style='height:53px; overflow-y:auto;'>
									<mtl:loop id="creditStatusList" loopId="loop" loopIndex="index">

									<% String creditRiskRedStyle = ""; %>
									<mtl:ifvalue id="loop" key="creditRiskInd" value="Y">
										<% creditRiskRedStyle = "color:red;font-weight:bold;"; %>
									</mtl:ifvalue>

										<table class='list_content_data' cellspacing="0" cellpadding="0" rules="rows">
											<tbody>
												<tr class='list_content'>
													<td class='w45 align-left'>
														<div class='line-cell ' style='width:40px;height:40px;'>
															<img id='creditStatusImg' src="images/credit_status.png" style="padding-right:0px; max-width:100%;max-height:100%;">
														</div>
													</td>
													<td>
														<div class='field-title' style='white-space:normal;'>
															<span id="title_creditPartyCode">
																<mtl:valuef id="loop" format="${creditPartyCode} ${creditPartyName}"/>
															</span>
														</div>
													</td>
													<td class='align-center'>
														<div class='field-title'>
															<span id="title_creditLimit">
																<mtl:message key="FIELD_DPR_PARTY_CREDIT_CREDIT_LIMIT"/>
															</span>
														</div>
														<div class='field'>
															<span>
																<mtl:valuef id="loop" format="$f{decode(creditCurrency,KRW,${creditLimitCrcy#NF.INTEGER},${creditLimit#NF.FLOAT2})}"/>
																<sub style='font-size:11px;'><mtl:valuef id="loop" format="$S{creditCurrency}"/></sub>
															</span>
														</div>
													</td>
													<td class='align-center'>
														<div class='field-title'>
															<span id="title_creditExposure">
																<mtl:message key="FIELD_DPR_PARTY_CREDIT_CREDIT_EXPOSURE"/>
															</span>
														</div>
														<div class='field' style='<%= creditRiskRedStyle %>'>
															<span><mtl:valuef id="loop" format="$f{decode(creditCurrency,KRW,${creditExposureCrcy#NF.INTEGER},${creditExposure#NF.FLOAT2})}"/>
																<sub style='font-size:11px;'><mtl:valuef id="loop" format="$S{creditCurrency}"/></sub>
															</span>
														</div>
													</td>
													<td class='align-center'>
														<div class='field-title'>
															<span id="title_accountReceivable"> <mtl:message key="FIELD_DPR_PARTY_CREDIT_ACCOUNT_RECEIVABLE"/> </span>
														</div>
														<div class='field'>
															<span><mtl:valuef id="loop" format="$f{decode(creditCurrency,KRW,${accountReceivableCrcy#NF.INTEGER},${accountReceivable#NF.FLOAT2})}"/>
																<sub style='font-size:11px;'><mtl:valuef id="loop" format="$S{creditCurrency}"/></sub>
															</span>
														</div>
													</td>
												</tr>
											</tbody>
										</table>

									</mtl:loop>
								</div><!--scroll-->
							</mtl:contentGroup>
						</div>
					</div>
				</mtl:contains>
				<% } %>

				<div class='search-bottom'>
				<% if( htmlpage.hasManageAuth() ) { %>
					<div class='table-cell search-button'>
						<mtl:ifvalue id="header" key="orderKey" value="">
							<mtl:button type="reset"/>
						</mtl:ifvalue>
					<% if( "WK".equals(status) || "ER".equals(status) || "SD".equals(status) ) { %>
						<mtl:button type="download" messageKey="jsp.BTN_DOWNLOAD_ORDER_PRODUCT_LIST"/>
						<mtl:button type="upload" messageKey="jsp.BTN_UPLOAD_ORDER_LIST"/>
					<% } %>
					<% if( "JNJAP_KR".equals(sessionMng.getPartyId()) ) { %>
						<mtl:button type="submit" icon="images/ico_update_white.png" messageKey="jsp.BTN_ORDER_HEADER_SYNC"/>
					<% } else { %>
						<mtl:button type="submit" icon="images/ico_update_white.png" messageKey="jsp.BTN_ORDER_HEADER_UPDATE"/>
					<% } %>

					</div>
				<% } %>
				</div>

			<% if( com.irt.dpr.Country.isFeature( organizationCode, "useUploadManual" ) ) { %>
				<div id="upload-info" class="messagebar success" style="display: table; margin-top: 20px; margin-bottom: 0">
					<div class="msgbox table-cell">
						<a class="circle-close success"></a>
						<mtl:message key="jsp.dpr_order_input.MSG_ORDER_UPLOAD_GUIDE" encodeHTML="true"/>
						<mtl:button type="s_button" onClick="JavaScript: uploadInputManualReq();" messageKey="jsp.BTN_LEARN_MORE" style="margin-left: 20px;"/>
					</div>
				</div>
			<% } %>
			</mtl:contentGroup>
		</mtl:form>

		<mtl:form name="frmMain" onSubmit="JavaScript: return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<%@ include file="include_pub_list.inc" %>
			<mtl:hidden id="header" key="orderKey"/>
			<mtl:hidden id="header" key="organizationCode"/>
			<mtl:hidden id="header" key="distributionChannelCode"/>
			<mtl:hidden id="header" key="partyCode"/>
			<input type='hidden' name='mode' value='upd'/>
			<input type='hidden' name='type'/>

			<mtl:contentGroup groupId="list" type="list" style="min-height: 450px;">
				<h2><mtl:message key="jsp.dpr_order_input.SUBTITLE_ORDER_INFO_LIST"/></h2>
				<div class='list-menu' style='text-align: right;'>
					<%-- <span style='font-size: 12px; font-weight: bold; margin-right: 8px;'><mtl:message key="jsp.SHOWCOUNT"/></span><mtl:showcount modified="JavaScript:changeShowCount(this);" customOption="smallSelectmenuOptions"/> --%>
				<% if( htmlpage.getProperty().getProperty("odrdlvGroup") != null &&  htmlpage.getProperty().getProperty("odrdlvGroup").length() > 0 ) { %>
					<mtl:select id="property" key="odrdlvGroup" prefixKey="MSG_ODRDLVGROUP_" mandatory="true" codeValues="RG,SP"
							modified="changeOrderDeliveryGroup(this);" customOption="smallSelectmenuOptions" width="auto"/>
					<mtl:valuef id="property" key="dlvrySpBrands" />
				<% } %>
					<mtl:select id="request" key="ftype" prefixKey="jsp.dpr_order_input.MSG_FORMATTYPE_"
							codeValues="PC,DZ" modified="listLink(this, null, \"sort\");" searchable="false" customOption="smallSelectmenuOptions" width="auto"/>

				</div>

				<%
					final java.util.Map<String, Object> final_headerMap = headerMap;
					final String allowUOM = property.getProperty( "allowUOM" );

					//com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage ) {
					ListWriter listwriter = new com.irt.custom.ListWriterXY( request, htmlpage, "list" ) {

						private Object getUomPackSize( java.util.List<java.util.Map<String, Object>> uoms, String itemCode, String uomCode ) {
							Object uomPackSize = null;
							if( uoms != null ) {
								for( java.util.Map<String, Object> uomMap : uoms ) {
									if( itemCode.equals(uomMap.get("itemCode")) && uomCode.equals(uomMap.get("uomCode")) ) {
										uomPackSize = uomMap.get("packSize");
										break;
									}
								}
							}
							return uomPackSize;
						}

						public String getColumnValue( Column column, java.util.Map recordMap, int row, int col ) {
							String fieldKey = column.getFieldKey();
							StringBuffer sbuf = new StringBuffer();

							if( "itemName".equals(fieldKey) ) {
								sbuf.append( "<div class='itemNameTooltip' style='font-size: 11px;'>");
								sbuf.append( HtmlUtility.toHtmlString(recordMap.get("itemName")) );
								sbuf.append( "</div>" );
							}
							else if( "itemCode".equals(fieldKey) ) {
								sbuf.append( "<input type='hidden' name='value_itemCode'" )
									.append( " value='"+ HtmlUtility.toHtmlString(recordMap.get(fieldKey)) +"'/>" )
									.append( "<input type='hidden' name='value_lineNumber' value='"+ HtmlUtility.toHtmlString(recordMap.get("lineNumber")) +"'/>" )
									.append( "<input type='hidden' name='value_itemCodeConfirmed' value='"+ HtmlUtility.toHtmlString(recordMap.get("itemCodeConfirmed")) +"'/>" )
									.append( "<input type='hidden' name='value_itemRefInd' value='"+ HtmlUtility.toHtmlString(recordMap.get("itemRefInd")) +"'/>" )
									.append( "<input type='hidden' name='value_childLineNumber' value='"+ HtmlUtility.toHtmlString(recordMap.get("childLineNumber")) +"'/>" )
									.append( "<input type='hidden' name='uomPrice' value='"+ HtmlUtility.toHtmlString(recordMap.get("price")) +"'/>" )
									.append( "<input type='hidden' name='unitPrice' value='"+ HtmlUtility.toHtmlString(recordMap.get("price")) +"'/>" )
									.append( "<input type='hidden' name='packSize' value='"+ HtmlUtility.toHtmlString(recordMap.get("packSize")) +"'/>" )
									.append( "<input type='hidden' name='itemConsumerEANCode' value='"+ HtmlUtility.toHtmlString(recordMap.get("itemConsumerEANCode")) +"'/>" );

									String itemLineId = "item-ln-" + recordMap.get("lineNumber");
									sbuf.append( "<span id='"+ itemLineId +"' class='item-ln'>"
										+ "<input type='hidden' name='value_itemCodeNew' value='"+ recordMap.get("itemCode") +"'/>"
										+ column.format(recordMap, msghandler));
							} else if( "uom".equals(fieldKey) ) {
								String[] allowUOMValues = allowUOM.split( "," );
								Object uom = recordMap.get( "uom" );
								java.util.List<java.util.Map<String, Object>> uoms = (java.util.List<java.util.Map<String, Object>>)recordMap.get("uoms");

								sbuf.append( "<select id='value_uom" + row + "' name='value_uom'>" );
								if( com.irt.dpr.Country.isFeature((String)final_headerMap.get("organizationCode"), "useSuggestSalesUnitInput") ) {
									if( uom == null || ((String)uom).length() == 0 ) {
										uom = "CSE";// system default
									}
									Object uomPackSize = getUomPackSize( uoms, (String)recordMap.get("itemCode"), (String)uom );
									if( uomPackSize == null ) {// in uom table the uom in recordMap is not found.
										uomPackSize = getUomPackSize(uoms, (String)recordMap.get("itemCode"), "PC");// last default
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
											for( java.util.Map<String, Object> uomMap : uoms ) {
												if( recordMap.get("itemCode").equals(uomMap.get("itemCode")) && allowUOMValues[i].equals(uomMap.get("uomCode")) ) {
													uomPackSize = uomMap.get("packSize");
													found = true;
													break;
												}
											}
										}
										if( found ) {// in uom table the uom in recordMap is found.
											sbuf.append( "<option value='"+ allowUOMValues[i] +"' "+ (allowUOMValues[i].equals(uom) ? "selected" : "") )
												.append( ">"+ allowUOMValues[i] +" / "+ HtmlUtility.toHtmlString(uomPackSize) + "</option>" );
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
							} else if( "rgRmnMaxQty".equals(fieldKey) ) {
								String zeroValueStyle = "";
								if( "0".equals(column.format(recordMap, msghandler)) ) {
									zeroValueStyle = " style='color: red;'";
								}
								sbuf.append( "<input type='hidden' disabled='disabled' name='rgRmnMaxQty' value='"+ HtmlUtility.toHtmlString(recordMap.get("rgRmnMaxQty")) +"'/>" );

								sbuf.append( "<span class='text_rgRmnMaxQty'" + zeroValueStyle );
								sbuf.append( ">" + column.format(recordMap, msghandler) + "</span>" );
							} else if( "orderQty".equals(fieldKey) ) {
								String status = com.irt.data.Record.extractString( final_headerMap, "status" );
								String orderQty = com.irt.data.Record.extractString( recordMap, "orderQty" );
								String simulationOrderQty = com.irt.data.Record.extractString( recordMap, "simulationOrderQty" );

								sbuf.append( "<input type='text' name='value_orderQty'" )
									.append( " value='"+ HtmlUtility.toHtmlString(recordMap.get(fieldKey)) +"'" )
									.append( " title='"+ HtmlUtility.toHtmlString(recordMap.get("itemName"))+"'" );
								if( !htmlpage.hasManageAuth() )
									sbuf.append( " class='input-field small readonly'" );
								else if( "SD".equals(status) && orderQty != null && orderQty.length() > 0 && !orderQty.equals(simulationOrderQty) )
									sbuf.append( " class='input-field small alert'" );
								else
									sbuf.append( " class='input-field small'" );

								sbuf.append( " onFocus='JavaScript: focusedOrderInputValue("+row+");'" );
								sbuf.append( " onBlur='JavaScript: checkOrderInputValue("+ row +"); JavaScript: calculateOrderLine("+row+");'" );
								sbuf.append( htmlpage.hasManageAuth() ? "" : "readonly='true'/>" );
							} else if( "orderValue".equals(fieldKey) ) {
								sbuf.append( "<span class='orderValue'>"+ column.format(recordMap, msghandler) +"</span>" );
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
						<mtl:button type="return" style="letter-spacing: 1; margin: 0;"/>
					<% if( htmlpage.hasManageAuth() ) { %>
						<% if( listwriter.containsData() ) { %>
							<mtl:button type="reset" messageKey="jsp.BTN_ORDER_INPUT_RESET" style="letter-spacing: 1; margin: 0;"/>
							<mtl:button type="delete" style="letter-spacing: 1; margin: 0;"/>
						<% } %>
							<mtl:ifvalue id="header" key="orderKey" notValue="">
								<mtl:button type="button" onClick="JavaScript: loadTemplateReq();" icon="images/ico_load.png"
										messageKey="jsp.BTN_LOAD_TEMPLATE" style="letter-spacing: 1; margin: 0;"/>
							</mtl:ifvalue>
						<% if( listwriter.containsData() ) { %>
							<mtl:button type="button" onClick="JavaScript: saveTemplateReq();" icon="images/ico_save.png"
									messageKey="jsp.BTN_SAVE_TEMPLATE" style="letter-spacing: 1; margin: 0;"/>
							<mtl:button type="save" icon="images/ico_save.png" styleClass="btn btn-secondary" style="letter-spacing: 1;margin: 0;"/>
							<mtl:button type="button" onClick="JavaScript: simulationWithUpdate();" icon="images/ico_submit_white.png"
									messageKey="jsp.BTN_SIMULATION" styleClass="primary" style="letter-spacing: 1; margin: 0;"/>
						<% } %>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				<% if( listwriter.containsData() ) { %>
					<div class='page' style='width: 65px;'>
					<% int[] _idxVars = htmlpage.getListIndexVariables(); %>
						<span class='page-idx' style='font-weight: bold;'><%= msghandler.getMessage( "jsp.SHOWCOUNT_SELECT", String.valueOf(_idxVars[2]) ) %></span>
					</div>
				<% } %>
				</div>

				<script type='text/javascript'>
					function initTooltip() {
						$(".itemNameTooltip").each( function(index, item) {
							if( this.offsetWidth < this.scrollWidth ) {
								$(this).attr( 'title', $(this).text() );
							}
						});
					}
					attachWindowEvent( "load", initTooltip );

					function checkHeaderInput() {
						if( !checkMandatoryInForm(frmHeader) ) return false;

						<%= htmlpage.getValidationScript() %>
						frmHeader.url.value = getLocationURL();
					<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") ) { %>
						if( frmHeader.userRdd.disabled == false || frmHeader.userRdd.options.length >= 1 ) {
							frmHeader.inDate.value = frmHeader.userRdd.value;
						}
					<% } %>
						if( orderInd == "N" ) {
							customPopup.alert( { "header" : "<%= HtmlUtility.toScriptString( msghandler.getMessage("jsp.dpr_order_input.MSG_CANNOT_PLACE_ORDER_HOLIDAY") ) %>" } );
							return false;
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
							<% if( !"JNJAP_KR".equals(sessionMng.getPartyId()) ) {// ignroe inDateDefault date check %>
								customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_INDATE_ABOUT_DEFAULTINDATE"/>" } );
							focusForm( frmHeader, frmHeader.inDate );

							return false;
							<% } %>
						}

						var orderKey = getOrderKey();
						var status = "<mtl:value id="header" key="status"/>";
						if( (orderKey != null && orderKey != "") && (status == null || status == "") ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_ORDER_STATUS"/>" } );

							return false;
						} else if( "SD" == status ) {
							if( confirm("<mtl:message key="MSG_RETURN_WORKSHEET" encodeScript="true"/>") )
								return submitInput();
							else
								return false;
						} else if( "SG" == status || "CG" == status || "CD" == status ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_CANNOT_UPDATE"/>" } );

							return false;
						}
						return submitInput();
					}

					function checkInput() {
						if( !frmMain.listcheckbox ) return false;
						if( !frmHeader.inDate.value ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_INDATE_ABOUT_MANDATORY"/>" } );

							return false;
						}

						var obj = frmMain.listcheckbox;
						var url = getLocationURL();
						if( window.parent.isContinueOrder )
							url += "&isContinueOrder=Y";
						frmMain.url.value = url;

						var status = "<mtl:value id="header" key="status"/>";
						if( status == null || status == "" ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_ORDER_STATUS"/>" } );

							return false;
						} else if( "SD" == status ) {
							if( confirm("<mtl:message key="MSG_RETURN_WORKSHEET" encodeScript="true"/>") )
								return submitInput();
							else
								return false;
						} else if( "SG" == status || "CG" == status || "CD" == status ) {
							customPopup.alert( { "header" : "<mtl:message key="ERR_CANNOT_UPDATE"/>" } );

							return false;
						}

						if( checkDetailOrderValues() && checkOrderLimit(obj) )
							return submitInput();
						else
							return false;

					}

					function checkDetailOrderValues() {
						var orderQtyObj;
						var childLineNumber;
						if( Field.isArray(frmMain.value_lineNumber) ) {
							orderQtyObj = frmMain.value_orderQty;
							childLineNumber = frmMain.value_childLineNumber;
						} else {
							orderQtyObj = new Array( frmMain.value_orderQty );
							childLineNumber = new Array( frmMain.value_childLineNumber );
						}
						for( var i = 0; i < orderQtyObj.length; i++ ) {
							if( childLineNumber.value != 0 ) continue;
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
				</script>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>
