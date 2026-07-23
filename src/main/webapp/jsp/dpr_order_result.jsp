<%--
	File Name:	dpr_order_result.jsp
	Version:	2.2.16

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.16	신규 UI/UX 적용
	hankalam	2020/06/30		2.2.15	위험 or 일반 상품 타입의 발주일 때 반대타입의 상품이 있을 경우 발주 안되도록 수정
	hankalam	2019/07/31		2.2.14	Freegoods 항목 표시
	jbaek		2019/07/30		2.2.13	itemPrice 칼럼 표시. uom에 입수표시.
	jbaek		2018/10/30		2.2.12	isChinaCountry() 삭제
	hankalam	2017/08/31		2.2.11	Simulation Result 화면에서 수량 수정 및 재고 부족분 제거 기능 추가
	hankalam	2017/05/31		2.2.10	같은 sold to, ship to 인 Order 상태가 Creating 일 때 중복 발주가 안되도록 메시지 출력 로직 추가
	hankalam	2017/02/28		2.2.9	Simulation 시 PlantSKU 에 존재하는 상품이 있을 경우 modify 버튼만 뜨도록 수정
	song7981	2016/06/03		2.2.8	modify, creation 시 현재 날짜가 아니면 OrderInput으로 이동하도록 수정
	song7981	2015/09/30		2.2.7	토탈 값 보여주는 기준을 orderValue -> confirmedOrderValue로 변경.
	jbaek		2014/09/30		2.2.6	SimulationResult 다운로드 기능 개발
	jbaek		2013/04/30		2.2.5	Sales Mov 관리
	jbaek		2013/01/30		2.2.4	PIPO 기능  개발
	jbaek		2012/08/30		2.2.3	duplicate request patch: 오더 상태가 CG일때 리프레쉬, CD일때 orderEnquiry되도록 변경
	jbaek		2012/07/30		2.2.2	regulate minOrderTotal 기능 추가
	lsinji		2009/06/30		2.2.1	simulationOrderQty가 null일 때 NullPointerException
	lsinji		2008/09/26		2.0.0	create
--%>

<%@page import="java.text.ParseException"%>
<%@ page import="com.irt.data.Record" %>
<%@ page import="com.irt.data.Date" %>
<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.Map, java.util.List, java.lang.Integer " %>
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

	java.util.Map sumMap = (java.util.Map)pageContext.findAttribute( "summary" );
	String isPlaceOrderable = null;
	if( sumMap != null && sumMap.containsKey("isPlaceOrderable") )
		isPlaceOrderable = com.irt.data.Record.extractString( sumMap, "isPlaceOrderable" );

	long simulationQtyTotal = 0l;

	List<Map<String,Object>> records = (List<Map<String,Object>>)request.getAttribute("records");
	if( records != null && records.size() > 0 ) {
		for( Map map : records ) {
			if( map != null && map.get("simulationOrderQty") != null )
				simulationQtyTotal += ( ((Number)map.get("simulationOrderQty")).longValue() );
		}
	}

	String orderDate = Record.extractString( headerMap, "orderDate" );
	String invalidItem = property.getProperty( "invalidItem" );
	String shortageType = property.getProperty( "shortageType" );
	String hasCreatingOrder = property.getProperty( "hasCreatingOrder" );
	String listType = property.getProperty( "listType" );
	String useSelectRdd = property.getProperty( "useSelectRdd" );
	boolean hasFreegoods = "Y".equals(property.getProperty("hasFreegoods") );
	boolean hasMostWay = "Y".equals(property.getProperty("hasMostWay") );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<link rel='stylesheet' href='style/jquery-ui.css'/>
 	<script type='text/javascript' src='script/jquery-ui.js'></script>
	<script type='text/javascript'>
		function bodyLoad() {
			<% if( "Y".equals(hasCreatingOrder) ) { %>
				if( confirm("<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_CREATING" encodeScript="true"/>") ) {
					var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder";
					url = attachDefaultParameter( url );
					url = replaceQueryValue( url, "vtype", "header" );
					url = replaceQueryValue( url, "status", "CG" );
					url = replaceQueryValue( url, "startOrderDate", "<mtl:value id="header" key="orderDate"/>" );
					windowOpen( url, "main" );
				}
			<% } %>

			<% if( records == null || records.size() < 1 ) { %>
				var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=frm";
				url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );

				windowSelfOpen( url );
			<% } %>

			<%-- if( com.irt.dpr.Order.SIMULATION_SHORTAGE_ELIMINATE.equals(shortageType) || com.irt.dpr.Order.SIMULATION_SHORTAGE_REQUEST.equals(shortageType) ) { %>
					frmMain.mode.value = "slist";
					var frm = document.frmMain;
					window.open("about:blank", "subwin_util" );
					frm.action = "<%= htmlpage.getRequestURL() %>" + "?wintype=sub";
					frm.target = "subwin_util";
					frm.submit();

					frm.target = "";
					frm.action = "<%= htmlpage.getRequestURL() %>";
					frm.mode.value = "";
			<% } --%>

			<%-- if( com.irt.dpr.Country.isFeature((String)headerMap.get("organizationCode"), "useFreegoods") && hasFreegoods ) { %>
				frmMain.mode.value = "flist";
				var frm = document.frmMain;
				window.open("about:blank", "subwin_util2" );
				frm.action = "<%= htmlpage.getRequestURL() %>" + "?wintype=sub";
				frm.target = "subwin_util2";
				frm.submit();

				frm.target = "";
				frm.action = "<%= htmlpage.getRequestURL() %>";
				frm.mode.value = "";
			<% } --%>

			<% if( "Y".equals(property.getProperty("shortageFreegoods")) ) { %>
				customPopup.alert( { "header": "<mtl:message key="jsp.dpr_order_result.MSG_SHORTAGE_FREEGOODS" encodeScript="true"/>" } );
			<% } %>
		}

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=frm";
			if( <%= orderDate %> != <%= Date.getInstance(sessionMng.getTimeZone()) %> ) {
				customPopup.alert( { "header": "<mtl:message key="jsp.dpr_order_input.MSG_ORDERDATE" encodeScript="true"/>" } );

				url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey" />" );
				url = replaceQueryValue( url, "reOrder", "Y" );
			} else {
				url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
			}

			windowSelfOpen( url );
		}

		function saveTemplateReq() {
			var orderKey = "<mtl:value id="header" key="orderKey"/>";
			if( orderKey == null || orderKey == "" ) {
				customPopup.alert( { "header": "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
				return false;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPROrderTemplate?mode=ireg";
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
			url = replaceQueryValue( url, "wintype", "sub" );

			windowOpen( url, "sub-content" );
		}

		function modifyQtyReq() {
			var orderKey = "<mtl:value id="header" key="orderKey"/>";
			if( orderKey == null || orderKey == "" ) {
				customPopup.alert( { "header": "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
				return;
			}
			var shortageType = "<%= shortageType %>";

 			if( "<%= com.irt.dpr.Order.SIMULATION_SHORTAGE_ELIMINATE %>" == shortageType ) {
 				customPopup.alert( { "header": "<%= HtmlUtility.toScriptString( msghandler.getMessage("jsp.dpr_order_result.MSG_MODIFY_QTY") ) %>" } );
				return;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=simr";
			url = replaceQueryValue( url, "orderKey", orderKey );
			url = replaceQueryValue( url, "ltype", "input" );

			windowSelfOpen( url );
		}

		function shortageDownloadReq() {
			var url =  "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=sdown";
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );

			windowOpen( url );
		}

		function freegoodsDownloadReq() {
			var url =  "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=fdown";
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );

			windowOpen( url );
		}

		function shortageEliminateReq() {
			customPopup.dialog( { "detail": "<mtl:message key="jsp.dpr_order_result.MSG_SHORTAGE_ELIMINATE" encodeScript="true"/>" }, function(res) {
				if( res ) {
					var orderKey = "<mtl:value id="header" key="orderKey"/>";
					if( orderKey == null || orderKey == "" ) {
						customPopup.alert( { "header": "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_NOTREGIST_ORDERHEADER") ) %>" } );
						return;
					}
					frmMain.mode.value = "shrt";
					frmMain.type.value = "sim";
					frmMain.submit();
				}
			});
		}

		function creation() {
			if( <%= orderDate %> != <%= Date.getInstance(sessionMng.getTimeZone()) %> ) {
				customPopup.alert( { "header": "<mtl:message key="jsp.dpr_order_input.MSG_ORDERDATE" encodeScript="true"/>" } );

				var url= "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=frm";
				url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey" />" );
				url = replaceQueryValue( url, "reOrder", "Y" );

				windowSelfOpen( url );
			} else {
				<% if( com.irt.dpr.Country.KOREA_ORGANIZATION.equals((String)headerMap.get("organizationCode")) ) { %>
					customPopup.alert( { "detail": "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_GAPPRICE" encodeScript="true"/>" }, function(res) {
						if( <%=simulationQtyTotal%> != 0 ) {
							customPopup.confirm( { "detail": "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_CREATION" encodeScript="true"/>" }, function(res) {
								if( res ) {
									creationReq();
								}
							});
						} else {
							customPopup.dialog( { "detail": "<mtl:message key="jsp.dpr_order_input.MSG_SIMULATION_QTY_CEHCK" encodeScript="true"/>" }, function(res) {
								if( res ) {
									creationReq();
								}
							});
						}
					});
				<% } else { %>
					if( <%=simulationQtyTotal%> != 0 ) {
						customPopup.confirm( { "detail": "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_CREATION" encodeScript="true"/>" }, function(res) {
							if( res ) {
								creationReq();
							}
						});
					} else {
						customPopup.dialog( { "detail": "<mtl:message key="jsp.dpr_order_input.MSG_SIMULATION_QTY_CEHCK" encodeScript="true"/>" }, function(res) {
							if( res ) {
								creationReq();
							}
						});
					}
				<% } %>
			}
		}

		function creationReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=wait";
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
			url = replaceQueryValue( url, "type", "cre" );

		<% if( com.irt.dpr.Country.isFeature((String)headerMap.get("organizationCode"), "useFreegoods") && hasFreegoods ) { %>
			for(var i=0; i < frmMain.freegoodsOrderWay.length; i++){
				if( frmMain.freegoodsOrderWay[i].checked )
					url = replaceQueryValue( url, "freegoodsOrderWay", frmMain.freegoodsOrderWay[i].value );
			}

		<% } %>

			var locationURL = replaceQueryValue(getLocationURL(), "mode", "simr");
			var msg = document.getElementById( "msg" );

		<% if( com.irt.dpr.Country.isFeature((String)headerMap.get("organizationCode"), "useProductRequire")
				&& (com.irt.dpr.Order.SIMULATION_SHORTAGE_ELIMINATE.equals(shortageType) || com.irt.dpr.Order.SIMULATION_SHORTAGE_REQUEST.equals(shortageType)) ) { %>

			customPopup.dialog( { "detail": "<mtl:message key="jsp.dpr_order_result.MSG_SHORTAGE_REQUEST" encodeScript="true"/>" }, function(res) {
				if( res ) {
					url = replaceQueryValue( url, "rtype", "reg" );
					windowSelfOpen( url, locationURL );
				} else {
					windowSelfOpen( url, locationURL );
				}
			});
		<% } else { %>
			if( msg ) msg.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_CREATING" encodeScript="true"/>";
			windowSelfOpen( url, locationURL );
		<% } %>
		}

		function orderEnquiry() {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=info";
			url = replaceQueryValue( url, "orderKey", encodeURIComponent("<mtl:value id="header" key="orderKey"/>") );
			url = replaceQueryValue( url, "status", encodeURIComponent("<mtl:value id="header" key="status"/>") );

			var msg = document.getElementById( "msg" );
			if( msg ) msg.innerHTML = "<mtl:message key="jsp.include_rbm_header.MSG_SUBMIT" encodeScript="true"/>";

			windowSelfOpen( url );
		}

		function downloadReq() {
			var url =  "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=down&dwntype=simr";
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
			url = replaceQueryValue( url, "ftype", "<mtl:value id="request" key="ftype"/>" );

			windowOpen( url );
		}

		function simulationWithUpdate() {
			var obj = frmMain.value_itemCode;
			if( typeof obj == "undefined" || !checkDetailOrderValues() || !checkOrderLimit(obj) )
				return;

			customPopup.confirm( { "detail": "<mtl:message key="jsp.dpr_order_input.MSG_CONFIRM_SIMULATION" encodeScript="true"/>" }, function(res) {
				if( res ) {
					frmMain.mode.value = "wait";
					frmMain.type.value = "sim";

					var btn_simulation = document.getElementById( "btn_simulation" );
					if( btn_simulation ) btn_simulation.disabled = true;

					frmMain.submit();
				}
			});
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

		function checkOrderLimit( obj ) {
 			if( Field.isArray(obj) ) {
 				for(var i = 0; i < obj.length; i++) {
					if( frmMain.value_childLineNumber[i].value != 0 ) continue;
					if( (frmMain.value_tmp_orderQty[i].value != frmMain.value_simulationOrderQty[i].value
									&& frmMain.value_orderQty[i].value > frmMain.value_simulationOrderQty[i].value)
							|| frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
						if( frmMain.value_tmp_orderQty[i].value != frmMain.value_simulationOrderQty[i].value
									&& frmMain.value_orderQty[i].value > frmMain.value_simulationOrderQty[i].value ) {
							customPopup.alert( { "header": "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT_THAN_SIMUALTION_QTY"/> " + frmMain.value_simulationOrderQty[i].value } );
						} else if( frmMain.value_orderQty[i].value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
							customPopup.alert( { "header": "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
						}
						frmMain.value_orderQty[i].select();
						frmMain.value_orderQty[i].focus();
						return false;
					} else if( frmMain.value_uom[i].value != "PC" && frmMain.value_orderQty[i].value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %> ) {
						customPopup.alert( { "header": "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
						frmMain.value_orderQty[i].select();
						frmMain.value_orderQty[i].focus();
						return false;
					}
				}
			} else if( frmMain.value_orderQty && frmMain.value_childLineNumber.value == 0 ) {
				if( (frmMain.value_tmp_orderQty[i].value != frmMain.value_simulationOrderQty[i].value
								&& frmMain.value_orderQty[i].value > frmMain.value_simulationOrderQty[i].value)
						|| frmMain.value_orderQty.value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
					 if( frmMain.value_tmp_orderQty[i].value != frmMain.value_simulationOrderQty[i].value
								&& frmMain.value_orderQty[i].value > frmMain.value_simulationOrderQty[i].value ) {
						customPopup.alert( { "header": "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT_THAN_SIMUALTION_QTY"/> " + frmMain.value_simulationOrderQty[i].value } );
					} else if( frmMain.value_orderQty.value < <%= com.irt.dpr.Order.ORDER_LOW_LIMIT %> ) {
						customPopup.alert( { "header": "<mtl:message key="ERR_ORD_ORDER_LOW_LIMIT"/>" } );
					}
					frmMain.value_orderQty.select();
					frmMain.value_orderQty.focus();
					return false;
				} else if( frmMain.value_orderQty.value > <%= com.irt.dpr.Order.ORDER_HIGH_LIMIT %> ) {
					customPopup.alert( { "header": "<mtl:message key="ERR_ORD_ORDER_HIGH_LIMIT"/>" } );
					frmMain.value_orderQty.select();
					frmMain.value_orderQty.focus();
					return false;
				}
			}
			return true;
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_list.inc" %>
	<iframe name='subwin_util2' style='display: none' src='<%= systemConfig.getProperty("baseURL") %>blank.html'></iframe>
	<mtl:form name="frmMain" fieldSetId="headerFieldSet" onSubmit="JavaScript: return false;">
		<%@ include file="include_rbm_form.inc" %>
		<mtl:contentGroup groupId="info" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<%@ include file="include_dpr_order_header.inc" %>

			<div id='messagebar'></div>
			<mtl:hidden id="header" key="orderKey"/>
			<input type='hidden' name='type'/>
			<input type='hidden' name='rtype'/>
			<input type='hidden' name='mode'/>
			<mtl:contentGroup groupId="list" type="list" styleClass="list_content none-card">
				<div class='list-menu align-right'>
				<% if( headerMap != null && "Y".equals(headerMap.get("pipoItemExist")) ) { %>
					<span style='position: relative; top: 15px; color: #0B8BAA; font-weight: bold; float: left'><mtl:message key="jsp.MSG_PIPO_EXIST"/></span>
				<% } %>
					<mtl:select id="request" key="ftype" prefixKey="jsp.dpr_order_input.MSG_FORMATTYPE_" codeValues="PC,DZ"
							customOption="smallSelectmenuOptions" modified="tabLink(this, 1, null, \"sort\");" width="auto" searchable="false"/>
				</div>

				<%
					ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage ) {

						public String getColumnValue( Column column, java.util.Map recordMap, int row, int col ) {
							String fieldKey = column.getFieldKey();
							StringBuffer sbuf = new StringBuffer();
							if( "uom".equals(fieldKey) && recordMap.get("packSize") != null ) {
								sbuf.append( "<span>"+ HtmlUtility.toHtmlString(recordMap.get(fieldKey))
										+" / " + HtmlUtility.toHtmlString(recordMap.get("packSize"))
										+ "</span>" );
							} else if( "simulationUOM".equals(fieldKey) && recordMap.get("simulationPackSize") != null ) {
								sbuf.append( "<span>"+ HtmlUtility.toHtmlString(recordMap.get(fieldKey))
										+" / " + HtmlUtility.toHtmlString(recordMap.get("simulationPackSize")) + "</span>" );
							} else if( "itemPrice".equals(fieldKey)
									&& com.irt.dpr.Country.isFeature((String)recordMap.get("organizationCode"), "useManualItemPrice")
									&& com.irt.dpr.Country.isFeature((String)recordMap.get("organizationCode"), "useSinglePrice") ) {
								Object itemPrice = recordMap.get("itemPrice");
								Object uomPrice = recordMap.get("price");
								Object simulationPackSize = recordMap.get("simulationPackSize");
								java.math.BigDecimal _itemPrice = null;
								Double singlePrice = null;

								if( itemPrice != null && uomPrice != null ) {
									if( itemPrice instanceof String ) {
										_itemPrice = new java.math.BigDecimal((String)itemPrice);
									} else if( simulationPackSize instanceof java.math.BigDecimal ) {
										_itemPrice = (java.math.BigDecimal)itemPrice;
									}
									java.math.BigDecimal _uomPrice = null;
									if( uomPrice instanceof String ) {
										_uomPrice = new java.math.BigDecimal((String)uomPrice);
									} else if( uomPrice instanceof java.math.BigDecimal ) {
										_uomPrice = (java.math.BigDecimal)uomPrice;
									}
									java.math.BigDecimal _simulationPackSize = null;
									if( simulationPackSize instanceof String ) {
										_simulationPackSize = new java.math.BigDecimal((String)simulationPackSize);
									} else if( simulationPackSize instanceof java.math.BigDecimal ) {
										_simulationPackSize = (java.math.BigDecimal)simulationPackSize;
									}
									singlePrice = (_uomPrice != null && _simulationPackSize != null ? _uomPrice.doubleValue() / _simulationPackSize.doubleValue() : null);
								}

								if( _itemPrice != null && _itemPrice.doubleValue() != singlePrice ) {
									sbuf.append( "<span style='color:red;'>"+ HtmlUtility.toHtmlString(column.format(recordMap, msghandler)) + "</span>" );
								} else {
									sbuf.append( "<span>"+ HtmlUtility.toHtmlString(column.format(recordMap, msghandler)) + "</span>" );
								}
							} else
								return column.format( recordMap, msghandler );

							return sbuf.toString();
						}

						public void printDataCell( JspWriter out, Column column, Map recordMap, int row, int col ) throws java.io.IOException {

							String fieldKey = column.getFieldKey();
							if( "simulationOrderQty".equals(fieldKey) ) {
								String itemRefInd = com.irt.data.Record.extractString( recordMap, "itemRefInd" );
								String orderQty = com.irt.data.Record.extractString( recordMap, "orderQty" );
								String simulationOrderQty = com.irt.data.Record.extractString( recordMap, "simulationOrderQty" );
								String title = column.getColumnHelp( recordMap, msghandler );
								String attr = (String)column.getColumnAttr();
								String htmlStr = "<td" + ( attr != null ? " " + attr : "" );
								StringBuffer sbuf = new StringBuffer();

								if( orderQty != null && !orderQty.equals(simulationOrderQty)
									&& !com.irt.dpr.OrderDetail.ITEMREF_PIPO_ORIGINAL.equals(itemRefInd) ) {
									htmlStr += " style='background-color: #FFA700'";
								}

								out.print( htmlStr );
								if( title == null )
									out.print( ">" );
								else
									out.print( " title='"+ title +"'>" );

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
		 									.append( "<input type='hidden' name='shortage_pcQty' value='"+ shortagePCQty +"'>" )

			 								.append( "<input type='hidden' name='value_lineNumber' value='"+ recordMap.get("lineNumber") +"'>" )
											.append( "<input type='hidden' name='value_itemCode' value='"+ recordMap.get("itemCode") +"'>" )
											.append( "<input type='hidden' name='value_itemCodeConfirmed' value='"+ recordMap.get("itemCodeConfirmed") +"'/>" )
											.append( "<input type='hidden' name='value_childLineNumber' value='"+ recordMap.get("childLineNumber") +"'/>" )
											.append( "<input type='hidden' name='value_uom' value='"+ recordMap.get("uom") +"'>" )
											.append( "<input type='hidden' name='value_orderQty' value='"+ recordMap.get("simulationOrderQty") +"'/>" )
											.append( "<input type='hidden' name='value_simulationOrderQty' value='"+ recordMap.get("simulationOrderQty") +"'>" );
									}
								}

								out.print( sbuf.toString() );
								out.print( getColumnValue(column, recordMap, row, col) );
								out.println( "</td>" );

								return;
							} else if( "editQty".endsWith(fieldKey) ) {
								String title = column.getColumnHelp( recordMap, msghandler );
								String htmlStr = "<td";
								StringBuffer sbuf = new StringBuffer();

								out.print( htmlStr );
								if( title == null )
									out.print( ">" );
								else
									out.print( " title='"+ title +"'>" );

								String orderQtyValue = HtmlUtility.toHtmlString( recordMap.get("orderQty") );
								String simulationOrderQtyValue = HtmlUtility.toHtmlString( recordMap.get("simulationOrderQty") );

								int orderQty = 0, simulationOrderQty = 0;
								if( orderQtyValue != null && orderQtyValue.length() > 0 )
									orderQty = Integer.parseInt( orderQtyValue );
								if( simulationOrderQtyValue != null && simulationOrderQtyValue.length() > 0 )
									simulationOrderQty = Integer.parseInt( simulationOrderQtyValue );

								String value = String.valueOf( orderQty > simulationOrderQty ? simulationOrderQty : orderQty );

								sbuf.append( "<input type='hidden' name='value_lineNumber' value='"+ recordMap.get("lineNumber") +"'>" )
									.append( "<input type='hidden' name='value_itemCode' value='"+ recordMap.get("itemCode") +"'>" )
									.append( "<input type='hidden' name='value_itemCodeConfirmed' value='"+ recordMap.get("itemCodeConfirmed") +"'/>" )
									.append( "<input type='hidden' name='value_childLineNumber' value='"+ recordMap.get("childLineNumber") +"'/>" )
									.append( "<input type='hidden' name='value_uom' value='"+ recordMap.get("uom") +"'>" )
									.append( "<input type='hidden' name='value_tmp_orderQty' class='content_o length_7' value='"+ orderQty +"'/>" )
									.append( "<input type='hidden' name='value_simulationOrderQty' value='"+ recordMap.get("simulationOrderQty") +"'>" )
									.append( "<input type='text' name='value_orderQty' class='content_o length_7' value='"+ value +"'/>" );
								out.print( sbuf.toString() );
								out.println( "</td>" );

								return;
							}

							super.printDataCell( out, column, recordMap, row, col );
						}

					};
					listWriter.setCheckboxType( com.irt.custom.ListWriter.CHECKBOXTYPE_NONE );
					listWriter.setNumbering( false );
					listWriter.print( out );
				%>

				<div class='info-table table-fixed' style='width: 913px; margin-top: 20px;'>
					<div class='row'>
						<div class='cell field-title'><mtl:message key="FIELD_DPR_ORDER_ORDERVALUE"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${inputtedOrderValue#NF.CURRENCY,N/A}"/></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_order_result.FIELD_ESTIMATED_NETAMOUNT"/></div>
						<div class='cell field-info'>
							<mtl:valuef id="summary" format="$f{decode(isCurrInt,Y,${confirmedOrderValue#NF.INTEGER,N/A},${confirmedOrderValue#NF.CURRENCY,N/A})}"/>
						</div>
					</div>
					<div class='row'>
						<div class='cell field-title'><mtl:message key="FIELD_DPR_ORDER_ORDER_TAX"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${inputtedOrderTax#NF.CURRENCY,N/A}"/></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_order_result.FIELD_ESTIMATED_TAX"/></div>
						<div class='cell field-info'>
							<mtl:valuef id="summary" format="$f{decode(isCurrInt,Y,${confirmedOrderTax#NF.INTEGER,N/A},${confirmedOrderTax#NF.CURRENCY,N/A})}"/>
						</div>
					</div>
					<div class='row'>
						<div class='cell field-title'><mtl:message key="FIELD_DPR_ORDER_ORDERDISCOUNT"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${inputtedOrderDiscount#NF.CURRENCY,N/A}"/></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_order_result.FIELD_ESTIMATED_DAMAGEDDISCOUNT"/></div>
						<div class='cell field-info'>
							<mtl:valuef id="summary" format="$f{decode(isCurrInt,Y,${confirmedOrderDiscount#NF.INTEGER,N/A},${confirmedOrderDiscount#NF.CURRENCY,N/A})}"/>
						</div>
					</div>
					<div class='row'>
						<div class='cell field-title'><mtl:message key="FIELD_DPR_ORDER_ORDERTOTAL"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${inputtedOrderTotal#NF.CURRENCY,N/A}"/></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_order_result.FIELD_ESTIMATED_TOTAL"/></div>
						<div class='cell field-info'>
							<mtl:valuef id="summary" format="$f{decode(isCurrInt,Y,${confirmedOrderTotal#NF.INTEGER,N/A},${confirmedOrderTotal#NF.CURRENCY,N/A})}"/>
						</div>
					</div>
				</div>
			<% if( com.irt.dpr.Country.isFeature((String)headerMap.get("organizationCode"), "useFreegoods") && hasFreegoods
						&& "SD".equals(status) && "Y".equals(isPlaceOrderable) && !"Y".equals(invalidItem) && !"input".equals(listType) ) { %>
				<div class='info-table table-fixed'>
					<div class='row'>
						<div class='cell field-title' style='width: 228px;'><mtl:message key="jsp.dpr_order_result.FIELD_FREEGOODS_ORDER_WAY"/></div>
						<div class='cell field-info'>
							<input type='radio' name='freegoodsOrderWay' id='freegoodsOrderWay_1' value='1' checked="checked">
							<label for='freegoodsOrderWay_1'><span><mtl:message key="jsp.dpr_order_result.MSG_MOST_FREEGOODS"/></span></label>
						<% if( hasMostWay ) { %>
							<input type='radio' name='freegoodsOrderWay' id='freegoodsOrderWay_2' value='2'>
							<label for='freegoodsOrderWay_2'><span><mtl:message key="jsp.dpr_order_result.MSG_MOST_NORMAL"/></span></label>
						<% } %>
							<input type='radio' name='freegoodsOrderWay' id='freegoodsOrderWay_3' value='3'>
							<label for='freegoodsOrderWay_3'><span><mtl:message key="jsp.dpr_order_result.MSG_ONLY_NORMAL"/></span></label>
						</div>
					</div>
				</div>
			<% } %>

				<div class='list-function' style='margin-top: 0;'>
					<div class='button'>
					<% if( listWriter.containsData() ) { %>
						<% if( !"Y".equals(invalidItem) ) { %>
							<mtl:button type="button" onClick="JavaScript:saveTemplateReq();" messageKey="jsp.BTN_SAVE_TEMPLATE" />
						<% } %>
						<% if( "CG".equals(status) ) { %>
							<mtl:button type="enquiry" onClick="JavaScript:location.replace(getLocationURL());" messageKey="jsp.BTN_ORDER_STATUS"/>
						<% } else if( "CD".equals(status) ) { %>
							<mtl:button type="enquiry" onClick="JavaScript:orderEnquiry();" messageKey="jsp.BTN_ORDER_STATUS"/>
						<% } else { %>
							<mtl:button type="modify" messageKey="jsp.BTN_EDIT_ORDER" />
							<% if( "SD".equals(status) &&  "Y".equals(isPlaceOrderable) && !"Y".equals(invalidItem) ) { %>
								<% if( com.irt.dpr.Order.SIMULATION_SHORTAGE_ELIMINATE.equals(shortageType) ) { %>
									<mtl:button type="delete" onClick="JavaScript:shortageEliminateReq();" messageKey="jsp.BTN_REMOVE_SHORTAGE"/>
								<% } %>
							<% } %>
						<% } %>
						<% if( "SD".equals(status) && "Y".equals(isPlaceOrderable) && !"Y".equals(invalidItem) && !"input".equals(listType) ) { %>
							<mtl:button type="button" onClick="JavaScript:creation();" icon="images/ico_add_order_white.png"
									styleClass="primary" messageKey="jsp.BTN_PLACE_ORDER"/>
						<% } %>
						<mtl:button type="download" styleClass="primary" icon="images/ico_download_white.png" messageKey="jsp.BTN_ORDER_DETAIL_DOWNLOAD"/>
						<% if( "input".equals(listType) ) { %>
							<mtl:button type="button" onClick="JavaScript:simulationWithUpdate();" icon="images/ico_submit_white.png"
									messageKey="jsp.BTN_SIMULATION" styleClass="primary"/>
						<% } %>
					<% } %>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:contentGroup>

		<mtl:contains id="freegoodsList" >
			<mtl:contentGroup groupId="freegoods" type="list">
				<h2><mtl:message key="jsp.dpr_order_result.SUBTITLE_FREEGOODS_LIST"/></h2>
				<%
					com.irt.data.cols.ColumnList freegoodsColumnList = (com.irt.data.cols.ColumnList)request.getAttribute( "freegoodsColumnList" );
					ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage, freegoodsColumnList, "freegoodsList" );
					listWriter.setCheckboxTypeAndNumbering( com.irt.custom.ListWriter.CHECKBOXTYPE_NUMBER );
					listWriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="download" onClick="JavaScript: freegoodsDownloadReq()"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:contains>

		<mtl:contains id="shortageList" >
			<mtl:contentGroup groupId="shorage" type="list">
				<h2><mtl:message key="jsp.dpr_order_result.SUBTITLE_SHORTAGE_LIST"/></h2>
				<%
					com.irt.data.cols.ColumnList shortageColumnList = (com.irt.data.cols.ColumnList)request.getAttribute( "shortageColumnList" );
					ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage, shortageColumnList, "shortageList" );
					listWriter.setCheckboxTypeAndNumbering( com.irt.custom.ListWriter.CHECKBOXTYPE_NUMBER );
					listWriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="download" onClick="JavaScript: shortageDownloadReq()"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:contains>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
