<%--
	File Name:	dpr_orderitem_tree.jsp
	Version:	2.2.14

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.14	신규 UI/UX 적용
	hankalam	2021/01/29		2.2.13	한국: 상품트리에서 display 되는 형식 변경
	hankalam	2020/10/30		2.2.12	itemName 에 encodeURIComponent 추가
	jbaek		2020/06/30		2.2.11	orderType parameter로 danger/nomral items filter 추가.
	jbaek		2019/06/30		2.2.10	StopItem, PackDeal, 브랜드별 필터 추가, 자주 시뮬되는 아이템 표시여부 조건 추가
	jbaek		2019/03/30		2.2.9	quickAddItem autocomplete 추가
	jbaek		2018/10/30		2.2.8	barCodeMultiItem, regularItemsList 기능 추가
	jbaek		2018/04/30		2.2.7	No refresh in SKU list 적용.
	jbaek		2017/06/30		2.2.6	upc/ean검색 기능 추가. itemConsumerEANCode
	hankalam	2017/05/31		2.2.5	같은 sold to, ship to 인 Order 상태가 Creating 일 때 중복 발주가 안되도록 메시지 출력 로직 추가
	hankalam	2017/02/28		2.2.4	Party 별 UOM 선택 기능 추가
	jbaek		2014/09/30		2.2.3	Product Hierarchy Level 기능 개발, Quick Add Item 사용가능 UOM 수정
	jbaek		2014/02/16		2.2.2	Plant SKU 제외 기능 개발
	lsinji		2009/05/31		2.2.1	Quick Add시에는 ItemTree가 보이지 않도록 설정
	guksm		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*,java.util.List,java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>
<%
	String requestType = request.getParameter( "rtype" );
	String organizationCode = (String)request.getParameter("organizationCode");
%>

<mtl:html errorPage="error.jsp">
<head>
<%@ include file="include_rbm_header.inc" %>
<%@ include file="include_dpr_itemtree.inc" %>
<style type="text/css">
span.details-added, input.details-added {
	background: #CCC;
}

#content_list_itemtree li.minus ul {
	display: block;
}

#content_list_itemtree li ul li.minus ul {
	display: block;
}

#content_list_itemtree li.plus ul {
	display: none;
}

#content_list_itemtree li ul li.plus ul {
	display: none;
}

input[type=checkbox] + label > span.is-stop-item-label, input[type=checkbox] + label > span.is-deal-item-label, input[type=checkbox] + label > span.is-close-item-label {
	color: #8F8F8F;
}

span.is-stop-item-label span.badge {
	margin-left: 10px;
	color: #FFFFFF;
	background-color: #D51900;
	border-radius: 15px;
	padding: 3px 7px;
	font-size: 12px;
	text-decoration: none;
}

span.is-deal-item-label span.badge {
	margin-left: 10px;
	color: #FFFFFF;
	background-color: #00B5E2;
	border-radius: 15px;
	padding: 3px 7px;
	font-size: 12px;
	text-decoration: none;
}

span.is-close-item-label span.badge {
	margin-left: 10px;
	color: #FFFFFF;
	background-color: #753BBD;
	border-radius: 15px;
	padding: 3px 7px;
	font-size: 12px;
	text-decoration: none;
}

span.is-stop-item-label {
	text-decoration: line-through;
	color: red;
}

span.is-deal-item-label {
	text-decoration: line-through;
	color: blue;
}

span.is-close-item-label {
	text-decoration: line-through;
	color: black;
}
</style>
<script type="text/javascript">
	window.FeatureEnabled = {
		barCodeMultiItems: '<%=com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "Feature;BarCodeMultiItems", false)%>'
	};

	function getStopItemTitle( stopStart, stopEnd ) {
		var titlePrefix = "<mtl:message encodeScript="true" key="MSG_STOPITEM_TITLE_PREFIX"/>";
		return titlePrefix + decodeURIComponent(stopStart) + " - " + decodeURIComponent(stopEnd);
	};

	function getDealItemTitle( dealStart, dealEnd ) {
		var titlePrefix = "<mtl:message encodeScript="true" key="MSG_PACKDEALITEM_TITLE_PREFIX"/>";
		return titlePrefix + ' ' + decodeURIComponent(dealStart) + " - " + decodeURIComponent(dealEnd);
	}

	function getCloseItemTitle( closeTime ) {
		var titlePrefix = "<mtl:message encodeScript="true" key="MSG_CLOSEITEM_TITLE_PREFIX"/>";
		return titlePrefix + ' ' + decodeURIComponent(closeTime);
	};

	function markCheckItemsAvailability() {
		$("input[data-is-stop-item=true], input[data-is-deal-item=true], input[data-is-close-item=true]").each(function(o,i) {
			var obj = $(this);
			obj.attr( "disabled", "disabled" );
			if( obj.attr("data-is-stop-item") === "true" ) {
				obj.next("label").children("span").addClass("is-stop-item-label")
					.attr("title", getStopItemTitle(obj.attr("data-stop-start-date"), obj.attr("data-stop-end-date")) );
			} else if( obj.attr("data-is-deal-item") === "true" ) {
				obj.next("label").children("span").addClass("is-deal-item-label")
					.attr("title", getDealItemTitle(obj.attr("data-deal-start-date"), obj.attr("data-deal-end-date")) );
			} else if( obj.attr("data-is-close-item") === "true" ) {
				obj.next("label").children("span").addClass("is-close-item-label")
					.attr("title", getCloseItemTitle(obj.attr("data-close-time")) );
			}
			return;
		});
	}

	$(document).ready(function(){
		if( parent.$("iframe.main-content") ) {
			$(".frame-content").innerHeight( parent.$(".main-content").contents().find( ".frame-content-wrap").innerHeight() );
		}

		markCheckItemsAvailability();
		var items = $("#tree-checkmenu input[name=checkItems]").map( function(o, i) {
			var obj = $(this);
			return {
				itemCode: obj.val().split(";")[0],
				label: obj.parent().find("label[for="+obj.prop("id")+"]").text(),
				stopStartDate: obj.attr("data-stop-start-date"),
				stopEndDate: obj.attr("data-stop-end-date"),
				isStopItem: obj.attr("data-is-stop-item"),
				dealStartDate: obj.attr("data-deal-start-date"),
				dealEndDate: obj.attr("data-deal-end-date"),
				isDealItem: obj.attr("data-is-deal-item"),
				isCloseItem: obj.attr("data-is-close-item"),
				closeTime: obj.attr("data-close-time"),
			};
		});

		$("input[name=quickItemCode]").autocomplete({
			minLength: 0,
			source: items.get(),
			focus: function( event,ui ) { return false; },
			select: function( event, ui ) { $("input[name=quickItemCode]").val( ui.item.itemCode ); return false; }
		}).autocomplete("instance")._renderItem=function( ul, item ) {
			var labelClass = "", labelText = "";
			if( item.isStopItem === "true" ) {
				labelClass = " class='is-stop-item-label' ";
				labelText = ("<br/>&nbsp;&nbsp;&nbsp;"+"<span>" + getStopItemTitle(item.stopStartDate, item.stopEndDate) + "</span>");
			} else if( item.isDealItem === "true" ) {
				labelClass = " class='is-deal-item-label' ";
				labelText = ("<br/>&nbsp;&nbsp;&nbsp;"+"<span>" + getDealItemTitle(item.dealStartDate, item.dealEndDate) + "</span>");
			} else if( item.isCloseItem === "true" ) {
				labelClass = " class='is-close-item-label' ";
				labelText = ("<br/>&nbsp;&nbsp;&nbsp;"+"<span>" + getCloseItemTitle(item.closeTime) + "</span>");
			}
			var disp = "<div>"
					+ "<span" + labelClass + ">"
					+ item.label
					+ "</span>"+ labelText + "</div>"
			return $("<li>").append( disp ).appendTo( ul );
		};
	});

	var checkmenu = new CheckTree( 'checkmenu' );

	function bodyLoad() {
		if( parent.$(".content-overlay .loading").is(":visible") ) {
			toggleLoadingParent( false );
		}

		if( String("<%=com.irt.dpr.Country.isFeature(organizationCode, "useRgSimItem")%>") === "true" ) {
			RegularItems.initValues();
		}

		var requestType = "<%= requestType %>";

		if( requestType == "search" ) {
			toggleTreeFunction( "tree_search" );
		} else if( requestType == "quick" ) {
			toggleTreeFunction( "quick_add" );
		}

		treeOpenAll();
	}

	function treeOpenAll() {
		$("#tree-checkmenu").find("li[name^=li-lvl].plus").click();
	}

	function selectItems() {
		var selectedValues = CheckBox.getValues( frmMain.checkItems );

		if( selectedValues == null || selectedValues.length == 0 ) {
			return;
		}

		if( parent && parent.window.main_content && typeof parent.window.main_content.getLocationURL != "undefined" ) {
			if( parent.window.main_content.document && parent.window.main_content.document.all.msg ) {
				parent.window.main_content.document.all.msg.innerHTML = "<mtl:message encodeScript="true" key="jsp.MSG_WAITING_ADDITEMSTOCART"/>";
			}

			frmMain.action = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder";
			frmMain.mode.value = "rgd";
			frmMain.url.value = replaceQueryValue( parent.window.main_content.getLocationURL(), "rtype", frmMain.rtype.value );
			if( window.parent.isContinueOrder )
				frmMain.url.value = replaceQueryValue( frmMain.url.value, "isContinueOrder", "Y" );

			<% if( htmlpage.getWindowType() != null ) { %>
				frmMain.wintype.value = "<%= htmlpage.getWindowType() %>";
			<% } %>
			<% if( htmlpage.getSystemMenu() != null ) { %>
				frmMain.menu.value = "<%= htmlpage.getSystemMenu() %>";
			<% } %>
			frmMain.locale.value = "<mtl:value id="request" key="locale"/>";

			if( parent.$("body.frame-content").length ) {
				toggleLoadingParent( true );
			}
			frmMain.submit();
		} else {
			return;
		}
	}

	function itemSearch( condType ) {
		var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=tree&type=ord";

		url = replaceQueryValue( url, "organizationCode", "<mtl:value id="request" key="organizationCode"/>" );
		url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="request" key="distributionChannelCode"/>" );
		url = replaceQueryValue( url, "partyCode", "<mtl:value id="request" key="partyCode"/>" );
		url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="request" key="shipPartyCode"/>" );
		url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
		url = replaceQueryValue( url, "orderType", "<mtl:value id="request" key="orderType"/>" );
		if( condType == "new" )
			url = replaceQueryValue( url, "newItemInd", "Y" );
		else if( condType == "prm" )
			url = replaceQueryValue( url, "promotionItemInd", "Y" );
		else
			url = replaceQueryValue( url, "searchType", "all" );

		url = replaceQueryValue( url, "btype", "ord" );

		if( frmMain.odrdlvGroup.value )
			url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );

		windowSelfOpen( url );
	}

	function itemSearchDetail() {
		var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=tree&type=ord&rtype=search";
		url = replaceQueryValue( url, "btype", "ord" );

		if( frmMain.itemConsumerEANCode.value ) {
			url = replaceQueryValue( url, "itemConsumerEANCode", frmMain.itemConsumerEANCode.value );
		}

		if( frmMain.itemCode.value ) {
			url = replaceQueryValue( url, "itemCode", encodeURIComponent(frmMain.itemCode.value) );
		} else {
			url = replaceQueryValue( url, "itemName", encodeURIComponent(frmMain.itemName.value) );
		}

		if( frmMain.odrdlvGroup.value )
			url = replaceQueryValue( url, "odrdlvGroup", frmMain.odrdlvGroup.value );

		url = replaceQueryValue( url, "organizationCode", "<mtl:value id="request" key="organizationCode"/>" );
		url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="request" key="distributionChannelCode"/>" );
		url = replaceQueryValue( url, "partyCode", "<mtl:value id="request" key="partyCode"/>" );
		url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="request" key="shipPartyCode"/>" );
		url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
		url = replaceQueryValue( url, "orderType", "<mtl:value id="request" key="orderType"/>" );

		document.all.msg.innerHTML = "<mtl:message encodeScript="true" key="jsp.include_rbm_header.MSG_SUBMIT"/>";
		windowSelfOpen( url );
	}

	function quickAddItemReq() {
		if( parent && parent.window.main_content && typeof parent.window.main_content.getLocationURL != "undefined" && typeof frmMain.quickItemCode != "undefined" && frmMain.quickItemCode.value ) {
			var url = parent.window.main_content.getLocationURL();

			url = replaceQueryValue( url, "mode", "rgd" );
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
			url = replaceQueryValue( url, "checkItems", encodeURIComponent(frmMain.quickItemCode.value) );
			url = replaceQueryValue( url, "soldPartyCode", "<mtl:value id="request" key="partyCode"/>" );
			url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="request" key="shipPartyCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="request" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="request" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "inputOrderQty", frmMain.inputQuickOrderQty.value );
		<% if( !com.irt.dpr.Country.isFeature(organizationCode, "useSuggestSalesUnitInput") ) { %>
			url = replaceQueryValue( url, "inputOrderUOM", frmMain.inputQuickOrderUOM.value );
		<% } %>
			url = replaceQueryValue( url, "rtype", "quick" );
			url = attachDefaultParameter( url );

			var backURL = parent.window.main_content.getLocationURL();
			backURL = replaceQueryValue( backURL, "rtype", "quick" );
			url = replaceQueryValue( url, "url", encodeURIComponent(backURL) );

			if( parent.window.main_content.document && parent.window.main_content.document.all.msg ) {
				parent.window.main_content.document.all.msg.innerHTML = "<mtl:message encodeScript="true" key="jsp.MSG_WAITING_ADDITEMSTOCART"/>";
			}

			if( parent.$("body.frame-content").length ) {
				toggleLoadingParent( true );
			}

			$(frmMain.quickItemCode).val( "" );

			window.open( url, "main_content" );
		} else {
			return;
		}
	}
</script>

<script type="text/javascript">
	function getItemCodeFromDistItem( combined ) {
		if( combined ) {
			return combined.split(";")[0];
		} else {
			return combined;
		}
	}

	function getDetailsItems() {
		if( parent && parent.window.main_content ) {
			return $(parent.frames["main_content"].document)
				.find("form[name=frmMain]").find(".list_content_xy_data_f* tr > td:nth-child(3)")
				.map(function(i,o){
					var itemCodeObj = $(this);
					if( itemCodeObj.find("select[name=value_itemCodeNew]").length > 0 ) {
						return itemCodeObj.find("[name=value_itemCodeNew]").first().val();
					} else {
						return itemCodeObj.find("[name=value_itemCode]").first().val();
					}
				});
		} else {
			return [];
		}
	}

	function treeParentsOpen( elmt ) {
		var treeLi = $(elmt).parent("li");

		treeLi.parents("li").removeClass("plus").addClass("minus");
		treeLi.parents("ul").filter(function(index){ return this.id !== "tree-checkmenu"; }).css("display","block");
	}

	function RegularItems() {}

	RegularItems.initValues = function( maxRank ) {
		var maxRank = maxRank || 10;

		/* big to small */
		function sortItemRankDescByItemCount(a,b){
			return parseInt( $(a).attr('data-sim-item-count'), 10) < parseInt( $(b).attr('data-sim-item-count'), 10)
			? 1 : -1;
		};

		/* small to big */
		function sortItemRankAscByItemCount(a,b){
			return parseInt( $(a).attr('data-sim-item-count'), 10) < parseInt( $(b).attr('data-sim-item-count'), 10)
			? -1 : 1;
		};


		var items = $("#tree-checkmenu")
			.find("input[name=checkItems][data-sim-item-count!=null]")
			.sort(sortItemRankDescByItemCount)
			.filter(function(index){
				if( index < maxRank ) {
					return true;
				}
				return false;
			})
			.map(function(index,object){
				var itemCode = $(this).val().split(";")[0];
				var itemName = $(this).siblings("label").find("span[name=checkItemsLabel]").text();
				var simItemCount = $(this).attr("data-sim-item-count");
				var ipt = $("<input>", {
					value: itemCode,
					type: 'checkbox',
					name: 'regularItems',
					id: 'regularItems_'+index,
					onclick: 'JavaScript: RegularItems.syncItem(this);'
				}).addClass( "small" )
					.attr( "data-tmp-value", itemCode )
					.attr( "data-item-name", itemName )
					.attr( "data-sim-item-count", simItemCount )
					.attr( "data-is-stop-item", $(this).attr("data-is-stop-item") )
					.attr( "data-stop-start-date", $(this).attr("data-stop-start-date") )
					.attr( "data-stop-end-date", $(this).attr("data-stop-end-date") )
					.attr( "data-is-deal-item", $(this).attr("data-is-deal-item") )
					.attr( "data-deal-start-date", $(this).attr("data-deal-start-date") )
					.attr( "data-deal-end-date", $(this).attr("data-deal-end-date") )
					.attr( "data-upc", $(this).attr("data-upc") )
					.attr( "data-searchtext", $(this).attr("data-searchtext") );


				var lbl = "<label for='regularItems_" + index + "' style='display: flex;'><span name='regularItemsLabel' title='"+ simItemCount + "-times" +"'>" + itemName + "</span></label>";
				return "<li data-position='"+ index +"'>" + ipt.get(0).outerHTML + lbl + "</li>";
			})
			.get();

		var poolSelector = "#regularItemsList #regularItemsPool";
		if( $(poolSelector).length == 0 ) {
			$("#regularItemsList").append( $("<ul id='regularItemsPool'>") );
		}
		$(poolSelector).html( items );

		function ieVersion() {
			var ua = window.navigator.userAgent;
			if( ua.indexOf("MSIE ") > -1 ) {
				if( ua.indexOf("Edge/") > -1 )
					return 12;
				else if( ua.indexOf("Trident/7.0") > -1 )
					return 11;
				else if( ua.indexOf("Trident/6.0") > -1 )
					return 10;
				else if( ua.indexOf("Trident/5.0") > -1 )
					return 9;
				else if( ua.indexOf("Trident/4.0") > -1 )
					return 8;
				else
					return 0;// maybe older version of IE.
			} else {
				return -1;// maybe not IE browser
			}
		}

		//IE 11 bug. to work around 'value' lost( currently, now checked prev ie or newer edge has bug or not... ).
		if( ieVersion() == 11 ) {
			$(poolSelector).each(function(index,object){
				$(object).val($(object).attr("data-tmp-value"));
				$(object).attr("data-tmp-value", "");
			});
		}

		if( items && items.length > 0 ) {
			RegularItems.syncFromDetails();
		} else {
			$("#regularItemsViewSize").css("display", "none");
			$("#regularItemsList").find("> a").css("color", "grey");
		}

		$("#regularItemsList").css("display","");

		markCheckItemsAvailability();
		return $("#regularItemsList").find("input[name=regularItems]:checked").map(function(index,object){ return $(this); });
	}

	RegularItems.toggleView = function() {
		var currVisible = $("#regularItemsPool").is(":visible");
		var hasItems = $("#regularItemsPool").find("input[name=regularItems]").length > 0;
		if( currVisible == true ) {
			$("#regularItemsPool").css("display", "none");
			if( hasItems ) {
				$("#regularItemsViewSize").css("display", "none");
			}
		} else {
			$("#regularItemsPool").css("display", "block");
			if( hasItems ) {
				$("#regularItemsViewSize").css("display", "inline-block");
			}
		}
	}

	RegularItems.syncItem = function( elmt ) {
		function check( elmt ) {
			var regularItem = $(elmt).val();

			var addedItems = getDetailsItems();

			if( $.inArray( regularItem, addedItems ) > -1 ) {
				elmt.checked = false;
				customPopup.alert( { "header" : "<mtl:message key="ERR_ALREADY_REGISTERED_ORDERITEM"/>" } );
			} else {
				$("#tree-checkmenu :input").filter(function(index) {
					return $(this).val().length >0 && this.value.split(";")[0] === regularItem;
				}).each( function(index, object){
					this.checked = true;
					$(this).siblings("label").find("span[name=checkItemsLabel]").addClass("details-added");
					treeParentsOpen(this);
				});
			}
		}

		function uncheck( elmt ) {
			var regularItem = $(elmt).val();

			$("#tree-checkmenu :input")
				.filter(function(index) {
					return $(this).val().length > 0 && this.value.split(";")[0] === regularItem;
				}).each(function(index,object){
					this.checked = false;
					$(this).siblings("label").find("span[name=checkItemsLabel]").removeClass("details-added");
					//$(this).siblings("span[name=checkItemsLabel]").removeClass("details-added");
					$(this).prop("checked", "");
					treeParentsOpen(this);
				});
		}

		if( $(elmt).prop("checked") === true ) {
			return check(elmt);
		} else {
			return uncheck(elmt);
		}
	}


	RegularItems.syncFromDetails = function() {
		var addedItems = getDetailsItems();
		var selectedValues = CheckBox.getValues( frmMain.checkItems ) || [];

		var thisDocument = (parent && parent.window.menu ) ? parent.frames["menu-content"].document : document;
		markCheckItemsAvailability();
		if( addedItems.length > 0 ) {
			$(thisDocument)
				.find("#regularItemsPool").find("input[name=regularItems]")
				.each(function(index,object){
					if( $.inArray( this.value, addedItems ) > -1 ) {
						this.checked = true;
						this.disabled = true;
						$(this).addClass("details-added");
					}
				});
		}
		if( selectedValues.length > 0 ) {
			$(thisDocument)
				.find("#regularItemsPool").find("input[name=regularItems]")
				.each(function(index,object){

					for( var i = 0; i < selectedValues.length; i++ ) {
						var itemCode = getItemCodeFromDistItem( selectedValues[i] );
						console.log( index + " "+ itemCode + " : "	 + this);
						if( itemCode == $(this).val() ) {
							this.checked = true;
							this.disabled = false;
						}
					}
				});
		}
	}

	function treeShowByBrand( el ) {
		if( el ) {
			var brandCode = Field.getValue(el);
			if( brandCode ) {
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-brand-code") != brandCode; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("brand-show").addClass("brand-hide"); });

				var foundCnt = 0;
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-brand-code") == brandCode; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("brand-hide").addClass("brand-show"); foundCnt++; });
				if( foundCnt == 0 ) {
					$("#tree-checkmenu :input")
						.each(function(i,o){ $(this).parent().closest("li").removeClass("brand-show").addClass("brand-hide"); });
				}

				$("#tree-checkmenu .brand-hide").hide();
				$("#tree-checkmenu .brand-show").show();
				treeOpenAll();
			} else {
				$("#tree-checkmenu .brand-hide").removeClass("brand-hide").show();
				$("#tree-checkmenu .brand-show").removeClass("brand-show");
			}
		}
	}

	function treeShowByFilter( el ) {
		if( el ) {
			var filterValue = Field.getValue( el );
			if( filterValue == "N" ) {
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-newitem") != "Y"; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });

				var foundCnt = 0;
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-newitem") == "Y"; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("item-hide").addClass("item-show"); foundCnt++; });
				if( foundCnt == 0 ) {
					$("#tree-checkmenu :input")
						.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });
				}

				$("#tree-checkmenu .item-hide").hide();
				$("#tree-checkmenu .item-show").show();
				treeOpenAll();
			} else if( filterValue == "P" ) {
				$("#tree-checkmenu :input")
				.filter(function(i){ return $(this).attr("data-promotion") != "Y"; })
				.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });

				var foundCnt = 0;
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-promotion") == "Y"; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("item-hide").addClass("item-show"); foundCnt++; });
				if( foundCnt == 0 ) {
					$("#tree-checkmenu :input")
						.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });
				}

				$("#tree-checkmenu .item-hide").hide();
				$("#tree-checkmenu .item-show").show();
				treeOpenAll();
			} else {
				$("#tree-checkmenu .item-hide").removeClass("item-hide").show();
				$("#tree-checkmenu .item-show").removeClass("item-show");
			}
		}
	}
</script>
</head>

<body class='content' style='padding-left: 8px; overflow: hidden;'>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_list.inc" %>
	<mtl:contentGroup groupId="tree" type="content" styleClass="frame-content">
		<mtl:form name="frmMain" target="main_content" onSubmit="JavaScript: return false;">
			<div class='top-wrap'>
				<h2><mtl:message key="jsp.dpr_item_tree.SUBTITLE_SKU_LIST"/></h2>
				<mtl:hidden id="request" key="rtype"/>
				<mtl:hidden id="request" key="organizationCode"/>
				<mtl:hidden id="request" key="distributionChannelCode"/>
				<mtl:hidden id="request" key="partyCode" name="soldPartyCode"/>
				<mtl:hidden id="request" key="shipPartyCode" name="shipPartyCode"/>
				<mtl:hidden id="request" key="orderKey"/>
				<input type="hidden" name="mode"/>
				<input type="hidden" name="url"/>
				<input type="hidden" name="locale"/>
				<input type="hidden" name="menu"/>
				<input type="hidden" name="wintype"/>
				<mtl:hidden id="request" key="odrdlvGroup"/>

				<span id="barCodeMultiItems" data-value='<%=request.getAttribute("barCodeMultiItems")%>'></span>

				<%
					List<Map> recordList = (List)pageContext.findAttribute( "records" );
					String type = property.getProperty( "type" );
					List organizations = (List)pageContext.findAttribute( "organizations" );
				%>
				<div id='tree_search' style='margin-bottom: 10px; display: none;'>
					<div class='search-table table-fixed'>
						<div class='row'>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'><mtl:message key="jsp.dpr_orderitem_tree.FIELD_MATERIAL"/></div>
								<div class='field'>
									<mtl:text id="request" key="itemCode"/>
								</div>
							</div>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'><mtl:message key="jsp.dpr_item_tree.FIELD_UPC_CODE"/></div>
								<div class='field'>
									<mtl:text id="request" key="itemConsumerEANCode"/>
								</div>
							</div>
						</div>
					</div>
					<div class='search-bottom'>
						<div class='table-cell search-button'>
							<mtl:button type="reset" onClick="JavaScript: searchReset(this);"/>
							<mtl:button type="button" icon="images/ico_close.png" onClick="JavaScript: toggleTreeFunction(\"tree_search\")" messageKey="jsp.BTN_CLOSE"/>
						</div>
					</div>
				</div>
				<div id='quick_add' style='margin-bottom: 10px; display: none;'>
					<div class='search-table table-fixed'>
						<div class='row'>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'><mtl:message key="FIELD_DPR_ITEM_MASTER_CODE"/></div>
								<div class='field'>
									<mtl:text id="request" key="quickItemCode"/>
								</div>
							</div>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'><mtl:message key="jsp.dpr_orderitem_tree.FIELD_INPUT_QTY"/></div>
								<div class='field'>
									<mtl:text id="request" key="inputQuickOrderQty" onKeyDown="if(event.keyCode==13) { quickAddItemReq() }"/>
								</div>
							</div>
						<% if( !com.irt.dpr.Country.isFeature(organizationCode, "useSuggestSalesUnitInput") ) { %>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'><mtl:message key="jsp.dpr_orderitem_tree.FIELD_INPUT_UOM"/></div>
								<div class='field'>
									<mtl:select id="request" key="inputQuickOrderUOM" searchable="false" codeValues="<%= property.getProperty(\"allowUOM\")%>"/>
								</div>
							</div>
						<% } %>
						</div>
					</div>
					<div class='search-bottom'>
						<div class='table-cell search-button'>
							<mtl:button type="reset" onClick="JavaScript: searchReset(this);"/>
							<mtl:button type="button" icon="images/ico_close.png" onClick="JavaScript: toggleTreeFunction(\"quick_add\")" messageKey="jsp.BTN_CLOSE"/>
							<mtl:button type="quickadd" onClick="JavaScript: quickAddItemReq();" icon="images/ico_add_white.png" styleClass="primary"/>
						</div>
					</div>
				</div>
				<script type="text/javascript">
					$(function() {
						var topHeight = $("#content_tree .top-wrap").outerHeight( true );
						var bottomHeight = $(".tree-bottom-button").outerHeight( true );
						$("#content_list_itemtreegroup").css( "height", "calc(100% - " + (topHeight + bottomHeight) + "px)" );
						$(document).on( "propertychange change keyup paste input", "#tree_search input[name=itemCode]", function(e) {
							if( e.keyCode !== 38 && e.keyCode !== 40 && e.keyCode !== 13 ) {
								treeOpenAll();
								var value = $(this).val().toLowerCase();
								$("#content_list_itemtree li.normal").filter( function() {
									var visible = ( $(this).find( "input" ).data( "searchtext" ) + "" ).toLowerCase().indexOf( value ) > -1;
									$(this).toggle( visible );
								});

								$("#regularItemsPool li").filter( function() {
									var visible = ( $(this).find( "input" ).data( "searchtext" ) + "" ).toLowerCase().indexOf( value ) > -1;
									$(this).toggle( visible );
								});
							}
						});

						$("#tree_search input[name=itemConsumerEANCode]").off( "keyup" ).on( "keyup", function(e) {
							if( e.keyCode !== 38 && e.keyCode !== 40 && e.keyCode !== 13 ) {
								treeOpenAll();
								var value = $(this).val().toLowerCase();
								$("#content_list_itemtree li.normal").filter( function() {
									var visible = ( $(this).find( "input" ).data( "upc" ) + "" ).toLowerCase().indexOf( value ) > -1;
									$(this).toggle( visible );
								});

								$("#regularItemsPool li").filter( function() {
									var visible = ( $(this).find( "input" ).data( "upc" ) + "" ).toLowerCase().indexOf( value ) > -1;
									$(this).toggle( visible );
								});
							}
						});
					});

					function toggleTreeFunction( id ) {
						if( "tree_search" === id ) {
							if( $("#tree_search").is(":visible") ) {
								$("#tree_search").hide();
								$("#tree_search input[type=text]").val( "" );
							} else {
								$("#tree_search").show();
							}
							$("#quick_add").hide();
						} else if( "quick_add" === id ) {
							if( $("#quick_add").is(":visible") ) {
								$("#quick_add").hide();
								$("#quick_add input[type=text]").val( "" );
							} else {
								$("#quick_add").show();
							}
							$("#tree_search").hide();
						}

						$("#content_list_itemtree li.normal").filter( function() {
							$(this).toggle( true );
						});

						$("#regularItemsPool li").filter( function() {
							$(this).toggle( true );
						});

						var topHeight = $("#content_tree .top-wrap").outerHeight( true );
						var bottomHeight = $(".tree-bottom-button").outerHeight( true );
						$("#content_list_itemtreegroup").css( "height", "calc(100% - " + (topHeight + bottomHeight) + "px)" );
					}

					function searchReset( obj ) {
						$(obj).closest( "#tree_search, #quick_add").find( "input[type=text]" ).val( "" );
						$("#content_list_itemtree li.normal").filter( function() {
							$(this).toggle( true );
						});

						$("#regularItemsPool li").filter( function() {
							$(this).toggle( true );
						});
					}
				</script>
				<div class='tree-menu table'>
					<div class='table-row'>
						<div class='table-cell align-left' style='vertical-align: middle;'>
							<mtl:button type="button" icon="images/ico_search.png" onClick="JavaScript: toggleTreeFunction(\"tree_search\");" messageKey="jsp.BTN_SEARCH"/>
							<mtl:button type="quickadd" icon="images/ico_add.png" onClick="JavaScript: toggleTreeFunction(\"quick_add\");"/>
						</div>
						<div class='table-cell align-right' style='vertical-align: middle;'>
							<mtl:select key="itemFilter" nullValueKey="jsp.MSG_SELECT_ALL_ITEMS" hasBlank="true" width="auto" searchable="false"
									prefixKey="jsp.dpr_item_tree.MSG_ITEM_FILTER_" codeValues="N,P" modified="treeShowByFilter(this);"/>
						</div>
					</div>
				</div>
			</div>

			<div id='content_list_itemtreegroup'>
				<div id='regularItemsList' style='display: none;'>
					<div class='table subtitle'>
						<div class='table-cell'><h3><mtl:message key="jsp.dpr_item_tree.SEARCH_REGULARITEM"/></h3></div>
						<div class='table-cell align-right'>
							<mtl:select key="regularItemCount" codeValues="10,20,30,40" width="auto" searchable="false" customOption="smallSelectmenuOptions"
									modified="RegularItems.initValues(encodeURIComponent(this.value));"/>
						</div>
					</div>
					<span id='regularItemsViewSize' style='display: inline-block;'>
					</span>
				</div>

				<div class='table subtitle'>
					<div class='table-cell'><h3><mtl:message key="jsp.dpr_item_tree.MSG_PRODUCT_LIST"/></h3></div>
					<div class='table-cell align-right'>
						<mtl:select key="brandCode" nullValueKey="MSG_PUB_SELECT@FIELD_DPR_BRAND_CODE"
								hasBlank="true" listId="brands" listCodeKey="brandCode" width="auto" searchable="false" customOption="smallSelectmenuOptions"
								listNameFormat="$S{[:brandCode;] $S{:brandName}}" modified="treeShowByBrand(this);"/>
					</div>
				</div>
				<ul id='content_list_itemtree'>
				<% if( recordList != null && recordList.size() > 0 && !"quick".equals(requestType) ) { %>
				<%
					String cateCode = null;
					String oldCateCode = null;

					String defaultCateCode = null;
					String parentCateCode = null;
					String oldParentCateCode = null;

					String childCateCode = null;
					String oldChildCateCode = null;

					String cateName = null;
					String childCateName = null;
					String parentCateName = null;

					int classCode = 0;
					int oldClassCode = 0;
					int defaultClassCode = 0;
					int parentClassCode = 0;
					int childClassCode = 0;

					int count = 0;
					int childCateCount = 0;
					int oldChildCateCount = 0;
					int parentCateCount = 0;

					boolean useStopItem = com.irt.dpr.Country.isFeature(organizationCode, "useStopItem");
					boolean usePackDeal = com.irt.dpr.Country.isFeature(organizationCode, "usePackDeal");
					boolean useCloseItem = com.irt.dpr.Country.isFeature(organizationCode, "useCloseItem");
					if( recordList != null && recordList.size() > 0 ) {
						out.println( "<ul id='tree-checkmenu' class='tree_content'>" );

						for( Map recordMap : recordList ) {
							String stopItemAttr = "";
							boolean isStopItem = false;
							boolean isDealItem = false;
							boolean isCloseItem = false;

							if( useStopItem ) {
								isStopItem = ("Y".equals(recordMap.get("isStopItem")));
								if( recordMap.get("isStopItem") != null ) {
									stopItemAttr += " data-is-stop-item='" + isStopItem  + "'";
									stopItemAttr += " data-stop-start-date='" + HtmlUtility.toHtmlString(recordMap.get("stopStartDate")) + "'";
									stopItemAttr += " data-stop-end-date='" + HtmlUtility.toHtmlString(recordMap.get("stopEndDate")) + "'";
								}
							}
							String dealItemAttr = "";
							if( usePackDeal ) {
								isDealItem = ("Y".equals(recordMap.get("isPackdealDate")));
								if( recordMap.get("isPackdealDate") != null ) {
									dealItemAttr += " data-is-deal-item='" + isDealItem  + "'";
									dealItemAttr += " data-deal-start-date='" + HtmlUtility.toHtmlString(recordMap.get("dealStartDate")) + "'";
									dealItemAttr += " data-deal-end-date='" + HtmlUtility.toHtmlString(recordMap.get("dealEndDate")) + "'";
								}
							}
							String closeItemAttr = "";
							if( useCloseItem ) {
								isCloseItem = ("Y".equals(recordMap.get("isCloseItem")));
								if( recordMap.get("isCloseItem") != null ) {
									closeItemAttr += " data-is-close-item='" + isCloseItem + "'";
									closeItemAttr += " data-close-time='" + HtmlUtility.toHtmlString(recordMap.get("ordCloseTime")) + "'";
								}
							}

							defaultCateCode = (String)recordMap.get( "defaultCateCode" );
							cateCode = (String)recordMap.get( "cateCode" );
							String defaultClassCodeStr = (String)recordMap.get( "defaultClassCode" );
							defaultClassCode = Integer.valueOf( defaultClassCodeStr );
							java.math.BigDecimal classCodeBD = (java.math.BigDecimal)recordMap.get( "currClassCode" );
							classCode = Integer.valueOf( classCodeBD.intValue() );

							if( classCode < 1 )
								classCode = 1;
							parentClassCode = classCode -1;
							if( parentClassCode < 1 )
								parentClassCode = 1;
							childClassCode = classCode +1;
							if( childClassCode > 5 )
								childClassCode = 6;

							cateCode = (String)recordMap.get("cateCode");
							cateName = (String)recordMap.get("N"+classCode);
							childCateCode = (String)recordMap.get("C"+childClassCode);
							childCateName = (String)recordMap.get("N"+childClassCode);

							parentCateCode = (String)recordMap.get("C"+parentClassCode);
							parentCateName = (String)recordMap.get("N"+parentClassCode);

							if( cateCode == null || cateCode.length() == 0 ) continue;

							if( count == 0 || (!cateCode.equals(oldCateCode)) ) {// new cateCode
								if( !childCateCode.equals( oldChildCateCode ) ) {
									if( oldCateCode != null ) {
										out.println( "</ul></li>" );

										if( childCateCode == null || childCateCode.length() == 0 ) continue;

										if( childCateCode != null && defaultClassCode == oldClassCode )
											out.println( "</ul></li>" );
									}
								}

								if( defaultClassCode == classCode ) {
									out.println( "<li id='show-"+ cateCode + "' name='li-lvl-1' title='"+ "default-lv1-"+ cateCode +"'>" );
									out.print( "<div class='cate-title'>" );
									if( cateName != null )
										out.print( cateName );
									else
										out.print( cateCode + " - Products" );
									//out.println( "<span id='count-"+ cateCode +"' name='checkItemsCount1' class='count'></span>" );
									out.println( "</div>" );
									out.println( "<ul id='tree-"+ cateCode +"'>" );

									out.println( "<li id='show-"+ childCateCode +"' name='li-lvl-2' title='"+ "default-lv2-"+ childCateCode +"'>" );
									out.print( "<div class='cate-sub-title'>" );
									if( childCateName != null ) {
										out.print( childCateName );
									} else {
										out.println( childCateCode + " - Products" );
									}
									out.println( "<div id='count-"+ childCateCode +"' name='checkItemsCount2' class='count'></div>" );
									out.println( "</div>" );
									out.println( "<ul id='tree-"+ childCateCode +"'>" );
								} else { // hierarchy condition
									out.println( "<li id='show-"+ childCateCode + "' name='li-lvl-2' title='"+ "cond-"+ childCateCode +"'>" );
									out.print( "<div class='cate-sub-title'>" );
									out.print( parentCateName + " - " );
									if( childCateName != null )
										out.print( childCateName );
									else
										out.print( childCateCode + " - Products" );
									out.println( "<div id='count-"+ childCateCode +"' name='checkItemsCount2' class='count'></div>" );
									out.println( "</div>" );
									out.println( "<ul id='tree-"+ childCateCode +"'>" );
								}
							} else { //new childCateCode
								if( !childCateCode.equals( oldChildCateCode ) ) {

									if( defaultClassCode == classCode ) {
										if( cateCode.equals(oldCateCode) ) {
											if( childCateCode != null )
												out.println( "</ul></li>" );
											out.println( "<li id='show-"+ childCateCode +"' name='li-lvl-2' title='default-lvl2-"+ childCateCode +"'>" );
											out.print( "<div class='cate-sub-title'>" );
											if( childCateName != null )
												out.print( childCateName );
											else
												out.print( childCateCode + " - Products" );
											out.println( "<div id='count-"+ childCateCode +"' name='checkItemsCount2' class='count'></div>" );
											out.println( "</div>" );
											out.println( "<ul id='tree-"+ childCateCode +"'>" );
										}
									} else { // hierarchy condition
										if( childCateCode != null )
											out.println( "</ul></li>" );
										out.println( "<li id='show-"+ childCateCode +"' name='li-lvl-2' title='cond-"+ childCateCode +"'>" );
										out.print( "<div class='cate-sub-title'>" );
										out.print( parentCateName + " - " );

										if( childCateName != null )
											out.print( childCateName );
										else
											out.print( childCateCode + " - Products" );
										out.println( "<div id='count-"+ childCateCode +"' name='checkItemsCount2' class='count'></div>" );
										out.println( "</div>" );
										out.println( "<ul id='tree-"+ childCateCode +"'>" );
									}
								}
							}

							com.irt.data.format.RecordFormat codePattern = com.irt.data.format.PatternRecordFormat.getInstance( "$f{pure(itemCode)}");
							com.irt.data.format.RecordFormat codePattern2 = com.irt.data.format.PatternRecordFormat.getInstance( "$f{pure(itemConsumerEANCode)}");
							String itemConsumerEANCode = (String)recordMap.get( "itemConsumerEANCode" );
							String displayItemName = ( recordMap.get("itemName") == null ? "" : (String)recordMap.get("itemName") );
							String newItemInd = (String)recordMap.get("newItemInd");
							String promotionItemInd = (String)recordMap.get("promotionItemInd");
							String existingOrderInd = (String)recordMap.get("existingOrderInd");
							StringBuffer sbuf = new StringBuffer();

							sbuf.append( "<li name='li-lvl-2' class='normal'>" );
							sbuf.append( "<input type='checkbox' name='checkItems' class='small' id='treeindex_"+ String.valueOf(count) +"'" )
							//.append( " data-regular-item-rank='" + recordMap.get("regularItemRank") + "'" )
							.append( " data-sim-item-count='" + HtmlUtility.toHtmlString(recordMap.get("simItemCount")) + "'" )
							.append( stopItemAttr )
							.append( dealItemAttr )
							.append( closeItemAttr )
							.append( " data-brand-code='" + HtmlUtility.toHtmlString(recordMap.get("brandCode")) + "'" )
							.append( " data-upc='" + itemConsumerEANCode + "'" )
							.append( " data-searchtext='" + codePattern.format(recordMap, msghandler) + " " + displayItemName + "'" );

							if( "Y".equals(newItemInd) ) {
								sbuf.append( " data-newitem='Y'" );
							}
							if( "Y".equals(promotionItemInd) ) {
								sbuf.append( " data-promotion='Y'" );
							}
							sbuf.append( " value='"+ recordMap.get("itemCode") +";"+ recordMap.get("distributionChannelCode") +"'" );

							if( "Y".equals(existingOrderInd) ) {
								sbuf.append( " checked disabled='disabled'" );
							} else if( isStopItem || isDealItem || isCloseItem ) {
								sbuf.append( " disabled='disabled'" );
							}
							sbuf.append( "/>" );

							sbuf.append( "<label for='treeindex_"+ String.valueOf(count) +"' style='display: flex;'>" );
							if( "Y".equals(newItemInd) )
								sbuf.append( "<div class='new-item'></div>" );
							if( "Y".equals(promotionItemInd) )
								sbuf.append( "<div class='promotion-item'></div>" );

							if( "Y".equals(existingOrderInd) )
								sbuf.append( "<span name='checkItemsLabel' class='details-added'>" );
							else
								sbuf.append( "<span name='checkItemsLabel'>" );

							String codeMsg;
							if( com.irt.dpr.Country.KOREA_ORGANIZATION.equals(organizationCode) ) {
								if( itemConsumerEANCode != null && itemConsumerEANCode.length() > 0 ) {
									itemConsumerEANCode = codePattern2.format( recordMap, msghandler );
									int length = itemConsumerEANCode.length();
									if( length > 6 ) {
										itemConsumerEANCode = itemConsumerEANCode.substring( 0, length - 6 ) + "<span style='font-weight: bold; color: #f06908;'>" + itemConsumerEANCode.substring( length - 6 ) + "</span>";
									}
								} else {
									itemConsumerEANCode = " - ";
								}

								String itemCode = codePattern.format( recordMap, msghandler );
								if( "79628314".equals(itemCode) ) {
									displayItemName = displayItemName.replace( "(처방용)Tylenol", "<span style='font-weight: bold; color: #FF0000;'>(처방용)Tylenol</span>" );
									displayItemName = displayItemName.replace( "(ETC) Tylenol", "<span style='font-weight: bold; color: #FF0000;'>(ETC) Tylenol</span>" );
								} else if( "79628321".equals(itemCode) ) {
									displayItemName = displayItemName.replace( "(처방용)어린이 타이레놀", "<span style='font-weight: bold; color: #FF0000;'>(처방용)어린이 타이레놀</span>" );
									displayItemName = displayItemName.replace( "(ETC) Children's Tylenol", "<span style='font-weight: bold; color: #FF0000;'>(ETC) Children's Tylenol</span>" );
								}
								codeMsg = "("+ itemConsumerEANCode + ") " + displayItemName + " ("+ codePattern.format(recordMap, msghandler) +")";
							} else {
								codeMsg = "("+ codePattern.format(recordMap, msghandler) +") "+ displayItemName;
							}
							sbuf.append( codeMsg );
/* 							if( isStopItem ) {
								sbuf.append( "<span class='badge'>" );
								sbuf.append( HtmlUtility.toHtmlString(msghandler.getMessage("jsp.dpr_orderitem_tree.BADGE_STOP")) );
								sbuf.append( "</span>" );
							}
							if( isDealItem ) {
								sbuf.append( "<span class='badge'>" );
								sbuf.append( HtmlUtility.toHtmlString(msghandler.getMessage("jsp.dpr_orderitem_tree.BADGE_PACKDEAL")) );
								sbuf.append( "</span>" );
							}
							if( isCloseItem ) {
								sbuf.append( "<span class='badge'>" );
								sbuf.append( HtmlUtility.toHtmlString(msghandler.getMessage("jsp.dpr_orderitem_tree.BADGE_CLOSE")) );
								sbuf.append( "</span>" );
							}
 */
							sbuf.append( "</span>" );
							sbuf.append( "</label>" );
							sbuf.append( "</li>" );
							out.println( sbuf.toString() );

							count++;
							oldCateCode = cateCode;
							oldChildCateCode = childCateCode;
							oldClassCode = classCode;
						}

						if( count > 0 )
							out.println( "</ul></li>" );

						out.println( "</ul>" );
					}
				}%>
				</ul>
			</div>
			<div class='tree-bottom-button table w100p'>
				<div class='table-cell align-right'>
					<mtl:button type="button" icon="images/ico_add_order_white.png" onClick="JavaScript: selectItems();"
							styleClass="primary" messageKey="jsp.BTN_ADD_TO_ORDER"/>
				</div>
			</div>
		</mtl:form>
	</mtl:contentGroup>
</body>
</mtl:html>
