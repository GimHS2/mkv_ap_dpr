<%--
	File Name:	dpr_order_list.jsp
	Version:	2.2.9

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/04/30		2.2.9	freegoods 발주도 같이 다운받아지도록 download 로직 변경
	hankalam	2020/12/31		2.2.8	logistics tracking 기능 추가
	hankalam	2020/07/31		2.2.7	freeGoodsOrderMapping(): mapping type 추가
	jbaek		2020/06/30		2.2.6	Revise Order Feature.
	hankalam	2019/07/31		2.2.5	freegoods 항목 표시
	jbaek		2019/07/30		2.2.4	downHeader, sync unsync다운 추가.
	jbaek		2018/03/31		2.2.3	multiDownload시 status조건 삭제
	jbaek		2017/02/28		2.2.2	orderStatus multi download
	jbaek		2015/04/30		2.2.1	LinkMenu가 1개일 경우 링크 클릭으로 이동
	guksm		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> conditionMap = (Map<String, Object>)pageContext.findAttribute( "condition" );
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
	String organizationCode = (String)conditionMap.get( "organizationCode" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		$(function() {
			var date = new Date();
			var year = date.getFullYear().toString();
			var month = (date.getMonth() + 1).toString();
			var day = date.getDate().toString();
			var today = year + "-" + ( month[1] ? month : "0" + month ) + "-" + ( day[1] ? day : "0" + day );
			if( $(frmCond.startOrderDate).val() == "" ) {
				$(frmCond.startOrderDate).val( today );
			}
			if( $(frmCond.endOrderDate).val() == "" ) {
				$(frmCond.endOrderDate).val( today );
			}
		});

		var linkmenu = null;

		function billingDetailReq( billingNumber, orderNumber ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRBillingDetail?mode=list&rtype=S";
			url = replaceQueryValue( url, "billingNumber", billingNumber );
			url = replaceQueryValue( url, "orderNumber", orderNumber );

			windowOpen( url + "&wintype=sub", "sub-content" );
		}

		function freeGoodsOrderMapping( orderKey, status ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ifgm&type=map";
			url = replaceQueryValue( url, "orderKey", orderKey );
			url = replaceQueryValue( url, "status", status );

			windowOpen( url +"&wintype=sub", "sub-content" );
		}

		function freeGoodsOrderCreate( orderKey, status ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ifgm&type=create";
			url = replaceQueryValue( url, "orderKey", orderKey );
			url = replaceQueryValue( url, "status", status );

			windowOpen( url +"&wintype=sub", "sub-content" );
		}

		function infoReq( orderKey, status ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=info";
			url = replaceQueryValue( url, "orderKey", orderKey );
			url = replaceQueryValue( url, "status", status );

			windowSelfOpen( url, getLocationURL() );
		}

		function modifyReq( orderKey, status ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder";
			url = replaceQueryValue( url, "orderKey", orderKey );
			if( status == "SD" ) {
				url = replaceQueryValue( url, "mode", "simr" );
			} else {
				url = replaceQueryValue( url, "mode", "frm" );
			}

			windowSelfOpen( url, getLocationURL() );
		}

		function linkHeaderInfoReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=info";

			url = replaceQueryValue( url, "infoType", linkmenu.params[0] );
			url = replaceQueryValue( url, "orderNumber", linkmenu.params[1] );
			url = replaceQueryValue( url, "status", "CD" );

			windowSelfOpen( url, getLocationURL() );
		}

		function linkHeaderInfoMenuReq( type, orderNumber ) {
			var menu = new Array;

			menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_INFO" encodeScript="true"/>'
				, 'self', 'JavaScript:linkHeaderInfoReq();' );

			linkmenu = createLinkMenu( menu );

			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkHeaderMenuReq( orderKey, status, freegoodsOrderInd, childOrderKey ) {
			var cnt = 0;
			var menu = new Array;

			menu[cnt++] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_INFO" encodeScript="true"/>'
					, 'self', 'JavaScript:linkHeaderMenuClick("INFO");' );
			if( "Y" != freegoodsOrderInd && (status == "WK" || status == "ER" || status == "SD") ) {
				menu[cnt++] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_MOD" encodeScript="true"/>'
						, 'self', 'JavaScript:linkHeaderMenuClick("MOD");' );
			} else if( <%= sessionMng.isAuthorized("DPR", "DPROrder.MAP") %> && "Y" == freegoodsOrderInd && status == "ER" && false ) {
				menu[cnt++] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_FREEGOOD_MOD" encodeScript="true"/>'
						, 'self', 'JavaScript:linkHeaderMenuClick("FGM");' );
			} else if( <%= sessionMng.isAuthorized("DPR", "DPROrder.MAP") %> && "Y" != freegoodsOrderInd && status == "CD" && childOrderKey == "" && false) {
				menu[cnt++] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_FREEGOOD_CREATE" encodeScript="true"/>'
						, 'self', 'JavaScript:linkHeaderMenuClick("FGC");' );
			}
			linkmenu = createLinkMenu( menu );

			var linkDefaultMenu = "INFO";
			if( linkmenu.menuListArray[0].length == 1 ) {
				linkmenu.params = linkHeaderMenuReq.arguments;
				linkHeaderMenuClick( linkDefaultMenu );
			} else {
				linkmenu.show();
				linkmenu.params = linkHeaderMenuReq.arguments;
			}
		}

		function linkHeaderMenuClick( menu ) {
			if( menu == "INFO" )
				infoReq( linkmenu.params[0], linkmenu.params[1] );
			else if( menu == "MOD" )
				modifyReq( linkmenu.params[0], linkmenu.params[1] );
			else if( menu == "FGM" )
				freeGoodsOrderMapping( linkmenu.params[0], linkmenu.params[1] );
			else if( menu == "FGC" )
				freeGoodsOrderCreate( linkmenu.params[0], linkmenu.params[1] );
		}

		function downloadReq() {
			requestOne( "<%= systemConfig.getClassURL() %>/DPROrderDownload?mode=down", frmMain.listcheckbox, "orderKey,orderNumber", null, true );
		}

		CheckBox.uncheckAll = function( checkObj ) {
			if( checkObj == null )
				return;
			else if( checkObj[0] ) {
				for( var i = 0; i < checkObj.length; i++ ) {
					if( checkObj[i].disabled ) continue;
					if( checkObj[i].checked ) {
						CheckBox.change( checkObj, false );
						return;
					}
				}
				CheckBox.change( checkObj, false );
			} else
				CheckBox.change( checkObj, false );
		}

		function downloadHeaderReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=hdrdwn";
			//url = replaceQueryValue( url, "startOrderDate", frmCond.startOrderDate.value );
			//url = replaceQueryValue( url, "endOrderDate", frmCond.endOrderDate.value );
			var query = CheckBox.getValues( frmMain.listcheckbox );

			if( url ) {
				var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
				customPopup.confirm( messages, function(res) {
					if( res ) {
						var uncheck = false;
						if( !query ) {
							CheckBox.checkAll( frmMain.listcheckbox );
							uncheck = true;
						}
						url = getMultiURL( url, "hdrdwn", frmMain.listcheckbox, "orderKey,orderNumber" );
						if( uncheck ) {
							CheckBox.uncheckAll( frmMain.listcheckbox );
						}
						if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
							submitPost( url );
						} else {
							windowOpen( url );
						}
					}
				});
			}
		}

		function getMultiURL( url, mode, checkObj, name ) {
			var query = "";
			var names = name.split( "," );
			var selectedValues = CheckBox.getValues( checkObj );
			if( selectedValues != null ) {
				for( var v = 0; v < selectedValues.length; v++ ) {
					var values = selectedValues[v].split( ";" );
					var length = values.length > 2 ? 2 : 1;
					for( var y = 0; y < length; y++ ) {
						var plus = y * length;
						for( var i = 0; i < names.length; i++ )
							if( names[i] && names[i] != 'null' )
								query += "&"+ names[i] +"="+ encodeURIComponent( values[i + plus] );
					}
				}
				if( query ) query = query.substring( 1 );
			}

			if( !query ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_ONE_OR_MORE" encodeScript="true"/>" } );
				return null;
			} else
				return attachDefaultParameter( replaceQueryValue(url, "mode", mode) +"&"+ query, false );
		}

		function multiDownloadUnsyncReq( obj ) {
			var url = getLocationURL( "url" );
			url = attachDefaultParameter( url, false );
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			url = replaceQueryValue( url, "dntype", "unsync");

			var enqiryTypeText = frmCond.status.options[frmCond.status.selectedIndex].text;
			var query = CheckBox.getValues( frmMain.listcheckbox );

			if( url ) {
				var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
				customPopup.confirm( messages, function(res) {
					if( res ) {
						var uncheck = false;
						if( !query ) {
							CheckBox.checkAll( frmMain.listcheckbox );
							uncheck = true;
						}
						url = getMultiURL( url, "mdown", frmMain.listcheckbox, "orderKey,orderNumber" );
						if( uncheck ) {
							CheckBox.uncheckAll( frmMain.listcheckbox );
						}
						if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
							submitPost( url );
						} else {
							windowOpen( url );
						}
					}
				});
			}
		}

		function multiDownloadSyncReq( obj ) {
			var url = getLocationURL( "url" );
			url = attachDefaultParameter( url, false );
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			url = replaceQueryValue( url, "dntype", "sync");

			<%
				int downMaxLimit = com.irt.rbm.RBMSystem.getSystemEnvInt( "DPR", "DownMaxLimit;DPREnquiryOrder.multiDownload", 30);
				String msgArgsKey = "";
			%>
			var enqiryTypeText = frmCond.status.options[frmCond.status.selectedIndex].text;
			var query = CheckBox.getValues( frmMain.listcheckbox );
			var uncheck = false;
			if( !query ) {
				uncheck = true;
				CheckBox.checkAll( frmMain.listcheckbox );
				query = CheckBox.getValues( frmMain.listcheckbox );
			}

			if( query.length > <%= downMaxLimit %> ) {
				if( uncheck ) {
					CheckBox.uncheckAll( frmMain.listcheckbox );
				}
				<% msgArgsKey = "MSG_CONFIRM_DOWNLOAD_RESTRICTED@" + String.valueOf(downMaxLimit); %>
				customPopup.alert( { "header" : "<mtl:message key="<%= msgArgsKey %>" encodeScript="true"/>" } );
				return;
			} else {
				url = getMultiURL( url, "mdown", frmMain.listcheckbox, "orderKey,orderNumber" );
				if( uncheck ) {
					CheckBox.uncheckAll( frmMain.listcheckbox );
				}
				if( url ) {
					var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
					customPopup.confirm( messages, function(res) {
						if( res ) {
							if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
								submitPost( url );
							} else {
								windowOpen( url );
							}
						}
					});
				}
			}
		}
	<% if( com.irt.dpr.Country.isFeature(organizationCode, "useLogisticsQuery") ) { %>
		function logisticsQueryReq( deliveryNumber ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=loqi";
			url = replaceQueryValue( url, "deliveryNumber", deliveryNumber );
			windowOpen( url +"&wintype=sub", "clsMng" );
		}

		$( function() {
			$( ".tooltip" ).tooltip( {
				position: { my: "left-15 bottom-15", at: "right center" }
			}).css( "cursor", "pointer" );
		});
	<% } %>
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_dpr_order_cond.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ColumnList freegoodsColumnList = (ColumnList)pageContext.findAttribute( "columnList" );
				ColumnList billingColumnList = (ColumnList)pageContext.findAttribute( "billingColumnList" );
				ColumnList deliveryColumnList = (ColumnList)pageContext.findAttribute( "deliveryColumnList" );
				ColumnList creditMemosColumnList = (ColumnList)pageContext.findAttribute( "creditMemosColumnList" );
				ColumnList debitMemosColumnList = (ColumnList)pageContext.findAttribute( "debitMemosColumnList" );
				com.irt.custom.MultiListWriter listwriter = new com.irt.custom.MultiListWriter( request, htmlpage ) {
					public void printHeaderCell( JspWriter out, com.irt.data.cols.Column column, int rowspan ) throws java.io.IOException {
						if( com.irt.dpr.Country.isFeature(organizationCode, "useLogisticsQuery") ) {
							StringBuffer attributeBuffer = new StringBuffer();
							String columnAttr = (String)column.getColumnAttr();
							if( columnAttr == null ) columnAttr = "";

							if( rowspan > 1 )
								attributeBuffer.append( " rowspan='"+ rowspan +"'" );

							String fieldKey = column.getFieldKey();
							String helpMessage = msghandler.getMessage( "jsp.dpr_order_list.MSG_LOGISTICS_TRACKING" );
							if( "deliveryNumber2".equals(fieldKey) ) {
								out.print( "<th"+ attributeBuffer.toString() +" "+ columnAttr + ">" );
								out.print( "<span><div class='field-title'>" + column.getColumnTitle(null, msghandler) + "</div>" );
								out.print( "<div class='icon tooltip'> " );
								out.print( "<img src='images/ico_info_small.png' title='" + helpMessage + "'></div>" );
								out.print( "</div></span>" );
								out.print( "</th>" );
								return;
							}
						}
						super.printHeaderCell(out, column, rowspan);
					}
				};

				if( sessionMng.isAuthorized("DPR", "DPROrder.DWN") )
					listwriter.setCheckboxTypeAndNumbering( com.irt.custom.ListWriter.CHECKBOXTYPE_CHECK );
				listwriter.setSortable( false );
				listwriter.setAppendNames( new String[] { "freegoodsOrderList", "billingList", "deliveryList", "creditMemosList", "debitMemosList" } );
				listwriter.setAppendColumnLists( new ColumnList[] { freegoodsColumnList, billingColumnList, deliveryColumnList, creditMemosColumnList, debitMemosColumnList } );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listwriter.containsData() && sessionMng.isAuthorized("DPR", "DPROrder.DWN") ) { %>
					<mtl:button type="download" onClick="JavaScript:downloadHeaderReq();" messageKey="jsp.BTN_DOWNLOAD_HEADER"/>
					<%-- <mtl:button type="download" onClick="JavaScript:multiDownloadUnsyncReq(this);"/> --%>
					<mtl:button type="download" onClick="JavaScript:multiDownloadSyncReq(this);" messageKey="jsp.BTN_DOWNLOAD_SAP"/>
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
