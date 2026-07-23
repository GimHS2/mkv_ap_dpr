<%--
	File Name:	dpr_main_page.jsp
	Version:	2.2.7

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	dudwls3720	2024/12/31		2.2.7	ORDER 메뉴의 소메뉴 Product Requirements의 권한을 주면 ORDER 메뉴 보이도록 수정
	hankalam	2021/11/30		2.2.6	신규 UI/UX 적용
	hankalam	2017/02/28		2.2.5	noticeListReq(): 호출 서블릿 ICSBoard -> DPRBoard 로 수정
	song7981	2016/06/21		2.2.4	siteLink 함수에 PARAM 추가
	hankalam	2015/10/30		2.2.3	locale 을 파라미터가 아닌 PageConfig 에서 갖고오도록 수정
	jbaek		2014/12/14		2.2.2	notice 팝업창 resize가능하도록 변경
	jbaek		2014/08/13		2.2.1	Document Management 기능 개발
	lsinji		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>
<mtl:html errorPage="error.jsp">
<%
	String organizationCode = sessionMng.getExtraValue();
	if( organizationCode == null ) organizationCode = "";
	String partyId = sessionMng.getPartyId();
	if( partyId == null ) partyId = "";

	String[] supportLocales = property.getProperty( "partySupportLocale" ).split("\\,");

	String debugSQLValue = request.getParameter( "debugSQL" );
	String lang = htmlpage.getLocale().getLanguage();
	String PARAM = ( lang == null || lang.length() == 0 ? "&menu=portal" : "&menu=portal&locale="+ lang );
	if( debugSQLValue != null && debugSQLValue.length() > 0 ) {
		PARAM += "&debugSQL=" + debugSQLValue;
	}

	String menuKey = (String)request.getParameter( "menuKey" );
	String submenuUrl = (String)request.getParameter( "suburl" );
	if( menuKey == null ) menuKey = "home";
	if( submenuUrl == null ) {
		submenuUrl = "";
	} else {
		submenuUrl = HtmlUtility.replaceURLQuery( submenuUrl, "locale", lang );
		if( debugSQLValue != null && debugSQLValue.length() > 0 ) {
			submenuUrl = HtmlUtility.replaceURLQuery( submenuUrl, "debugSQL", debugSQLValue );
		}
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<link rel='stylesheet' href='style/dpr_menu.css'>
	<script type='text/javascript'>
		var menuKey = "<%= menuKey %>";

		$( function() {
			var clickMenuKey = menuKey;
			var submenuUrl = "<%= submenuUrl %>";

			$("#menubar > div").each( function(index, item) {
				var menuName = $(item).attr( "class" );
				if( $("ul.sub-menu." + menuName).length ) {
					if( !$("ul.sub-menu." + menuName).find("li").length ) {
						$(item).hide();
					}
				}
			});

			$(".content-overlay .loading").show();
			$(".content-overlay").fadeIn( 150 ).css( "display", "flex" );
			if( submenuUrl.length > 0 ) {
				//document.content.location.href = submenuUrl;
				$("#content").attr( "src", submenuUrl );
			} else {
				$("#content").attr( "src", "<%= systemConfig.getClassURL() %>/DPRDashboard?mode=list<%= PARAM %>");
				//document.content.location.href = "<%= systemConfig.getClassURL() %>/DPRDashboard?mode=list<%= PARAM %>";
			}

			if( menuKey.length ) {
				menuMouseOver( menuKey );
			}

			$("#content").bind( "load", function () {
				$(".content-overlay").fadeOut();
				$(".content-overlay .loading").hide();
			});

			$.widget( "custom.localeSelectmenu", $.ui.selectmenu, {
				_renderButtonItem: function( item ) {
					var buttonItem = $( "<span>", {
						"class": "ui-selectmenu-text"
					})
					this._setText( buttonItem, item.label );
					buttonItem.css( "background-color", item.value )
					buttonItem.prepend( "<img src='images/ico_locale.png'>");
					return buttonItem;
				},
				_renderMenu: function( ul, items ) {
					var that = this;
					$.each( items, function(index, item) {
						that._renderItemData( ul, item );
					});
					var menuWrap = $("<div/>").addClass( "selectmenu-menu-wrapper" );
					$(ul).wrap( menuWrap );
				},
				open: function( event ) {
					this._super( event );
					this.button.find( ".arrow-down").removeClass( "arrow-down" ).addClass( "arrow-up" );
				},
				close: function( event ) {
					this._super( event );
					this.button.find( ".arrow-up").removeClass( "arrow-up" ).addClass( "arrow-down" );
				}
			});

			$(document).click( function(e) {
				var targetParent = $(e.target).parent();
				if( $(e.target).closest("#menubar")[0] == $("#menubar")[0] || $(e.target).closest("div.sub-menu")[0] == $("div.sub-menu")[0]
						|| e.target == $("div.menu-over-bg")[0] || e.target == $("div.menu-over")[0] ) {

					return false;
				} else {
					clickMenuKey = menuKey;
					menuMouseOver( clickMenuKey );
					submenuClose();
				}
			});

			$("select[name=locale]").localeSelectmenu( selectmenuOptions ).localeSelectmenu({ width: 150 } );
			$("select[name=locale]").on( "localeselectmenuselect", function( event, ui ) {
				changeLocale( ui.item.value );
			});

			$("div.menu-locale .ui-selectmenu-button").focusout( function() {
				$("select[name=locale]").localeSelectmenu( "close" );
			});

			$("#menubar > div").mouseenter( function() {
				var menuName = $(this).attr( "class" );
				menuMouseOver( menuName );
			}).click( function() {
				var menuName = $(this).attr( "class" );
				var targetMenu = $( "ul." + menuName );
				var othersMenu = targetMenu.siblings();
				menuMouseOver( menuName );
				clickMenuKey = menuName;

				if( othersMenu.is(":visible") ) {
					othersMenu.hide();
				} else if( targetMenu.length < 1 ) {
					$(".sub-menu ul").slideUp();
				}

				var url = $(this).data( "url" );
				if( url ) {
					url = "<%= systemConfig.getClassURL() %>/" + url;
					url += "<%= PARAM %>";
					$("#content").attr( "src", url );
					//document.content.location.href = url;
					$(".content-overlay .loading").show();
					$(".content-overlay").fadeIn( 150 ).css( "display", "flex" );
				} else {
					if( targetMenu.length > 0 && !targetMenu.is(":visible") ) {
						var menuLeft = $(this).offset().left;
						var submenuWidth = targetMenu.width();
						menuLeft = menuLeft - (submenuWidth / 2) + ($(this).width() / 2);
						targetMenu.css( "padding-left", menuLeft );
						targetMenu.slideDown();
						$(".content-overlay").show();
					}
				}
			});

			$("#menubar").mouseleave( function() {
				menuMouseOver( clickMenuKey );
			});

			$("ul.sub-menu > li > a").click( function() {
				menuKey = clickMenuKey;
				var url = "<%= systemConfig.getClassURL() %>/";
				url += $(this).data( "url" );
				url += "<%= PARAM %>";
				//document.content.location.href = url;
				$("#content").attr( "src", url );
				$(".content-overlay .loading").show();
				$(".content-overlay").fadeIn( 150 ).css( "display", "flex" );

				setTimeout( function() {
					submenuClose();
				}, 300 );
			});

			$("div.logout").mouseenter( function() {
				var txtLogout = $(this).find( "div" );
				txtLogout.removeClass().addClass( "hover" );
				$(this).mousedown( function() {
					txtLogout.addClass( "active" );
				}).mouseup( function() {
					txtLogout.removeClass( "active" );
				})
			}).mouseleave( function() {
				$(this).find( "div" ).removeClass();
			}).click( function() {
				$(".popup-overlay > .popup.confirm button[name=confirm]").click( function() {
					var url = "<%= systemConfig.getClassURL() %>/Login?mode=logout";
					url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
					window.open( url, "_top" );
				});

				$(".popup-overlay > .popup.confirm button[name=cancel]").click( function() {
					$(".popup-overlay").fadeOut();
					$(".popup-overlay > .popup.confirm" ).hide();
				});
				showPopup( "confirm", "<mtl:message key="MSG_CONFIRM_LOGOUT" encodeScript="true"/>" );
			});

			var selectLocale = $("select[name=locale]").val();
			var locale = "<%= lang %>";
			if( selectLocale != locale ) {
				$("select[name=locale]").val( locale );
				$("select[name=locale]").localeSelectmenu( "refresh" );
			}

			function submenuClose() {
				$("div.sub-menu ul").slideUp();
				if( !$(".content-overlay .loading").is(":visible") ) {
					$(".content-overlay").fadeOut();
				}
			}
		});

		function menuMouseOver( menuName ) {
			var baseLeft = $("#menubar > div.home").position().left;
			var targetMenu = $( "#menubar > div." + menuName );
			var menuOver = $( "div.menu-over" );
			menuOver.css( "left", targetMenu.position().left - baseLeft + (targetMenu.width() / 2) + 30 );
		}

		function changeLocale( language ) {
			if( language && language != "<%= lang %>" ) {
				var url = replaceQueryValue(getLocationURL(), "locale", language);
				url = replaceQueryValue(url, "menuKey", menuKey);
				//url = replaceQueryValue(url, "suburl", encodeURIComponent(document.content.location.href) );
				url = replaceQueryValue(url, "suburl", encodeURIComponent($("#content").attr("src")) );
				location.href = url;
			}
		}

		function showPopup( method, message ) {
			var popupObj;
			if( method === "confirm" ) {
				popupObj = $(".popup-overlay > .popup.confirm");
			} else if( method === "alert" ) {
				popupObj = $(".popup-overlay > .popup.alert");
			} else {
				return;
			}

			popupObj.siblings().hide();
			popupObj.find( "p.header-msg" ).text( message );
			popupObj.show();
			$(".popup-overlay").fadeIn().css( "display", "flex" );
		}
	</script>
</head>

<body class='main'>
	<div class='header'>
		<div class='header-sep header-logo'></div>
		<div class='header-sep'>
			<div id='menubar' class='menu-wrap'>
				<div data-url='DPRDashboard?mode=list' class='home'><img src='images/ico_nav_home.png'><mtl:message key="jsp.MENU_HOME"/></div>
				<div class='material'><img src='images/ico_nav_material.png'><mtl:message key="jsp.MENU_MATERIAL"/></div>
			<% if( sessionMng.isAuthorized("DPR","DPRParty.LST") || (!("JNJAP_KR".equals(sessionMng.getPartyId())) && (sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin()))
					|| (sessionMng.isAuthorized("DPR","DPRPartyUOM.MNG") && com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM"))
					|| (com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") && sessionMng.isAuthorized("DPR","DPRRddMng.LST"))
					|| (com.irt.dpr.Country.isFeature(organizationCode, "usePartyOper") && sessionMng.isAuthorized("DPR","DPRPartyOper.LST"))
					|| sessionMng.isAuthorized("DPR","DPROrderClose.LST") ) { %>
				<div class='partner'><img src='images/ico_nav_partner.png'><mtl:message key="jsp.MENU_PARTNER"/></div>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR", "DPROrder.DWN") || sessionMng.isAuthorized("DPR","DPROrder.LST")
					|| sessionMng.isAuthorized("DPR","DPROrder.INF") || sessionMng.isAuthorized("DPR","DPROrder.MNG") || sessionMng.isAuthorized("DPR","DPROrderReport")
					|| sessionMng.isAuthorized("DPR","DPRMoqItemCfg.LST") || sessionMng.isAuthorized("DPR","DPROrderRevise.MNG") || sessionMng.isAuthorized("DPR","DPRProductRequire.LST") ) { %>
				<div class='order'><img src='images/ico_nav_order.png'><mtl:message key="jsp.MENU_ORDER"/></div>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR", "DPRItemMasterExtra.MNG")
					|| sessionMng.isAuthorized("DPR","DPRUpload.SSL") || sessionMng.isAuthorized("DPR","DPRUpload.PCD")
					|| sessionMng.isAuthorized("DPR","DPRMasterMng.MNG") ) { %>
				<div class='upload'><img src='images/ico_nav_upload.png'><mtl:message key="jsp.MENU_UPLOAD"/></div>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR","DPRUpload.LST") ) { %>
				<div class='log'><img src='images/ico_nav_log.png'><mtl:message key="jsp.MENU_UPLOADLOG"/></div>
			<% } %>
				<div class='user'><img src='images/ico_nav_user.png'><mtl:message key="jsp.MENU_USER"/></div>
			<%
				String faqBoardCode = "FQ";
				String countryKey = "";
				countryKey = com.irt.dpr.Country.getCountryKeyFromPartyId(sessionMng.getPartyId());
				if( countryKey != null && countryKey.length() > 0 ) {
					faqBoardCode += "." + countryKey;
				}
			%>
			<% if( false && sessionMng.isAuthorized("ICS", "ICSBoard." + faqBoardCode + ".INF") ) { %>
				<div data-url='ICSBoard?mode=frm&type=faq&boardClassCode=<%=faqBoardCode%>' class='faq'><img src='images/ico_nav_faq.png'><mtl:message key="jsp.MENU_FAQ"/></div>
			<% } %>

			<%
				String packageCode = "ICSHelpBoard.HD";
				if( organizationCode != null ) {
					packageCode += "." + organizationCode;
				}
			%>
			<% if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && sessionMng.isAuthorized("ICS", packageCode + ".MNG") ) { %>
				<div data-url='DPRHelpBoard?mode=frm' class='inbox'><img src='images/ico_nav_message.png'><mtl:message key="jsp.MENU_INBOX"/></div>
			<% } else if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && sessionMng.isAuthorized("ICS", packageCode + ".REG") ) { %>
				<div data-url='DPRHelpBoard?mode=ireg' class='messageboard'><img src='images/ico_nav_message.png'><mtl:message key="jsp.MENU_MESSAGEBOARD"/></div>
			<% } %>

			<% if( sessionMng.isSystemAdmin() ) { %>
				<div data-url='DPRBoardNotice?mode=list' class='notice'><img src='images/ico_nav_log.png'><mtl:message key="jsp.MENU_NOTICE"/></div>
			<% } %>
			</div>
			<div class='menu-over-bg'><div class='menu-over'></div></div>
		</div>
		<div class='header-sep'>
			<div class='menu-right'>
				<div class='logout'><div><mtl:message key="jsp.BTN_LOGOUT"/></div></div>
				<div class='menu-locale'>
					<select name='locale' class='locale'>
					<% for( String localeString : supportLocales ) {
						if( localeString == null ) continue;
						String languageMessageKey = "MSG_LANGUAGE_BYLANGCD_" + localeString.toUpperCase();
					%>
						<option value='<%=localeString%>' <%=(localeString.equals(lang) ? "selected" : "")%>>
							<mtl:message key='<%=languageMessageKey%>' />
						</option>
					<% } %>
					</select>
				</div>
			</div>
		</div>
	</div>

	<div class='sub-menu'>
		<ul class="sub-menu material">
	<% if( sessionMng.isAuthorized("DPR","DPRStockQuery.LST") || !"JNJAP_CN_DIST".equals(sessionMng.getGroupId()) || sessionMng.isSystemAdmin() ) { %>
		<% if( (sessionMng.isSystemAdmin() || !"JNJAP_CN_DIST".equals(sessionMng.getGroupId())) && sessionMng.isAuthorized("DPR", "DPRItem.INF") ) { %>
			<li><a data-url='DPRItem?mode=frm'><mtl:message key="jsp.SUBMENU_MATERIAL"/></a></li>
		<% } %>
		<% if( (sessionMng.isSystemAdmin() || !"JNJAP_CN_DIST".equals(sessionMng.getGroupId())) && sessionMng.isAuthorized("DPR", "DPRItemImage.LST") ) { %>
			<li><a data-url='DPRItemImage?mode=list'><mtl:message key="jsp.SUBMENU_MATERIALIMAGE"/></a></li>
		<% } %>
		<% if( (sessionMng.isSystemAdmin() || !"JNJAP_CN_DIST".equals(sessionMng.getGroupId())) && sessionMng.isAuthorized("DPR", "DPRItem.LST") ) { %>
			<li><a data-url='DPRItem?mode=list&btype=itm'><mtl:message key="jsp.SUBMENU_SELLINGSKU"/></a></li>
		<% } %>
		<% if( (sessionMng.isSystemAdmin() || !"JNJAP_CN_DIST".equals(sessionMng.getGroupId())) && sessionMng.isAuthorized("DPR", "DPRPlantItem.LST") ) { %>
			<li><a data-url='DPRPlantItem?mode=list'><mtl:message key="jsp.SUBMENU_PLANTSKU"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPRStockQuery.LST") ) { %>
			<li><a data-url='DPRStockQuery?mode=list'><mtl:message key="jsp.SUBMENU_STOCKQUERY"/></a></li>
		<% } %>
		<% if( sessionMng.isSystemAdmin() || !"JNJAP_CN_DIST".equals(sessionMng.getGroupId()) && sessionMng.isAuthorized("DPR", "DPRItem.WithPlant.LST") ) { %>
			<li><a data-url='DPRItem?mode=list&vtype=party&btype=ordall&distributionChannelCode=11'><mtl:message key="jsp.SUBMENU_SKUSTATUS"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") && sessionMng.isAuthorized("DPR","DPRDangerousItem.LST") ) { %>
			<li><a data-url='DPRDangerousItem?mode=list&dangerousInd=Y'><mtl:message key="jsp.SUBMENU_DANGEROUSMATERIAL"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePlantRcv") && sessionMng.isAuthorized("DPR","DPRPlantRecovery.LST") ) { %>
			<li><a data-url='DPRPlantRecovery?mode=list'><mtl:message key="jsp.SUBMENU_RECOVERYCOMMENT"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR","DPRItemPrice.LST") ) { %>
			<li><a data-url='DPRItemPrice?mode=list'><mtl:message key="jsp.SUBMENU_MATERIALPRICE"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR","DPRItemEAN.LST") ) { %>
			<li><a data-url='DPRItemEAN?mode=list'><mtl:message key="jsp.SUBMENU_MATERIALUPC"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "useStopItem") && sessionMng.isAuthorized("DPR","DPRStopItemCfg.LST") ) { %>
			<li><a data-url='DPRStopItemCfg?mode=list'><mtl:message key="jsp.SUBMENU_STOPITEMSETTING"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "useStopItem") && sessionMng.isAuthorized("DPR","DPRStopItem.LST") ) { %>
			<li><a data-url='DPRStopItem?mode=list'><mtl:message key="jsp.SUBMENU_STOPITEM"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePackDeal") && sessionMng.isAuthorized("DPR","DPRPackDealCfg.LST") ) { %>
			<li><a data-url='DPRPackDealCfg?mode=list'><mtl:message key="jsp.SUBMENU_PACKDEALSETTING"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR","DPRFreeGoods.MNG") ) { %>
			<li><a data-url='DPRFreeGoods?mode=list'><mtl:message key="jsp.SUBMENU_FREEQUOTA"/></a></li>
		<% } %>
	<% } %>
		</ul>

	<% if( sessionMng.isAuthorized("DPR","DPRParty.LST") || (!("JNJAP_KR".equals(sessionMng.getPartyId())) && (sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin()))
			|| (sessionMng.isAuthorized("DPR","DPRPartyUOM.MNG") && com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM"))
			|| (com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") && sessionMng.isAuthorized("DPR","DPRRddMng.LST"))
			|| (com.irt.dpr.Country.isFeature(organizationCode, "usePartyOper") && sessionMng.isAuthorized("DPR","DPRPartyOper.LST"))
			|| sessionMng.isAuthorized("DPR","DPROrderClose.LST") ) { %>
		<ul class="sub-menu partner">
		<% if( sessionMng.isAuthorized("DPR","DPRParty.LST") ) { %>
			<li><a data-url='DPRParty?mode=list'><mtl:message key="jsp.SUBMENU_PARTNER"/></a></li>
		<% } %>
		<% if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && (sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin()) ) { %>
			<li><a data-url='DPRCountry?mode=list'><mtl:message key="jsp.SUBMENU_COUNTRY"/></a></li>
		<% } %>
		<% if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && (sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin()) ) { %>
			<li><a data-url='DPRSalesMov?mode=list'><mtl:message key="jsp.SUBMENU_SALESMOV"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR","DPRPartyUOM.MNG") && com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") ) { %>
			<li><a data-url='DPRPartyUOM?mode=list'><mtl:message key="jsp.SUBMENU_UOMSETTING"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") && sessionMng.isAuthorized("DPR","DPRRddMng.LST") ) { %>
			<li><a data-url='DPRRddMng?mode=list'><mtl:message key="jsp.SUBMENU_RDDSETTING"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePartyOper") && sessionMng.isAuthorized("DPR","DPRPartyOper.LST") ) { %>
			<li><a data-url='DPRPartyOper?mode=list'><mtl:message key="jsp.SUBMENU_PARTYOPER"/></a></li>
		<% } %>
		<% if( "JNJAP_KR".equals(sessionMng.getPartyId()) && sessionMng.isAuthorized("DPR","DPROrderClose.LST") ) { %>
			<li><a data-url='DPROrderClose?mode=list'><mtl:message key="jsp.SUBMENU_ORDERCLOSING"/></a></li>
		<% } %>
		</ul>
	<% } %>

	<% if( sessionMng.isAuthorized("DPR", "DPROrder.DWN") || sessionMng.isAuthorized("DPR","DPROrder.LST")
			|| sessionMng.isAuthorized("DPR","DPROrder.INF") || sessionMng.isAuthorized("DPR","DPROrder.MNG") || sessionMng.isAuthorized("DPR","DPROrderReport")
			|| sessionMng.isAuthorized("DPR","DPRMoqItemCfg.LST") || sessionMng.isAuthorized("DPR","DPROrderRevise.MNG") || sessionMng.isAuthorized("DPR","DPRProductRequire.LST") ) { %>
		<ul class="sub-menu order">
		<% if( sessionMng.isAuthorized("DPR", "DPROrder.LST") ) { %>
			<li><a data-url='DPREnquiryOrder?mode=list&status=CD&recentCount=10'><mtl:message key="jsp.SUBMENU_ORDERSTATUS"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPROrder.MNG") && com.irt.data.Condition.isConditionTrue(property.getProperty("isOrderInputAuth")) ) { %>
			<li><a data-url='DPRPlaceOrder?mode=frm'><mtl:message key="jsp.SUBMENU_ORDER"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePackDeal") && sessionMng.isAuthorized("DPR", "DPROrder.MNG") && com.irt.data.Condition.isConditionTrue(property.getProperty("isOrderInputAuth")) ) { %>
			<li><a data-url='DPRPackDealOrder?mode=ior'><mtl:message key="jsp.SUBMENU_PACKDEAL_ORDER"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPROrderReport.LST") ) { %>
			<li><a data-url='DPROrderReport?mode=list'><mtl:message key="jsp.SUBMENU_ORDERREPORT"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPRBillingReport.LST") ) { %>
			<li><a data-url='DPRBillingReport?mode=list'><mtl:message key="jsp.SUBMENU_BILLINGREPORT"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPROrderRevise.MNG") ) { %>
			<li><a data-url='DPROrderRevise?mode=list'><mtl:message key="jsp.SUBMENU_ORDERREVISE"/></a></li>
		<% } %>
		<% if( com.irt.dpr.Country.isFeature(sessionMng.getExtraValue(), "useProductRequire") &&
					sessionMng.isAuthorized("DPR", "DPRProductRequire.MNG") && com.irt.data.Condition.isConditionTrue(property.getProperty("isOrderInputAuth")) ) { %>
			<li><a data-url='DPRProductRequire?mode=list'><mtl:message key="jsp.SUBMENU_PRODUCTREQUIRE"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR","DPRMoqItemCfg.LST") ) { %>
			<li><a data-url='DPRMoqItemCfg?mode=list'><mtl:message key="jsp.SUBMENU_MOQITEM_CONFIG"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR","DPRMoqItem.LST") ) { %>
			<li><a data-url='DPRMoqItem?mode=list'><mtl:message key="jsp.SUBMENU_MOQITEM"/></a></li>
		<% } %>
		</ul>
	<% } %>

	<% boolean showDeprecatedFunction = false;
		if( sessionMng.isAuthorized("DPR", "DPRItemMasterExtra.MNG")
			|| sessionMng.isAuthorized("DPR","DPRUpload.SSL") || sessionMng.isAuthorized("DPR","DPRUpload.PCD")
			|| sessionMng.isAuthorized("DPR","DPRMasterMng.MNG") ) { %>
		<ul class="sub-menu upload">
		<% if( sessionMng.isAuthorized("DPR","DPRMasterMng.MNG") ) { %>
			<li><a data-url='DPRMasterMng?mode=list&mngtype=ptysales'><mtl:message key="jsp.SUBMENU_CUSTOMERMASTER"/></a></li>
			<li><a data-url='DPRMasterMng?mode=list&mngtype=itmsales'><mtl:message key="jsp.SUBMENU_PRODUCTMASTER"/></a></li>
		<% } %>
		<% if( false && com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "OrdExtInfoMng;"+ organizationCode, false) ) { %>
			<li><a data-url='DPRItemMasterExtra?mode=list'><mtl:message key="jsp.SUBMENU_EXTRAMASTER"/></a></li>
		<% } %>

		<% if( sessionMng.isAuthorized("DPR","DPRUpload.SSL") ) { %>
			<li><a data-url='DPRUpload?mode=iup&uploadType=SSL'><mtl:message key="jsp.SUBMENU_SSL"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPRUpload.PCD") ) { %>
			<li><a data-url='DPRUpload?mode=iup&uploadType=PCD'><mtl:message key="jsp.SUBMENU_PCD"/></a></li>
		<% } %>
<%-- 	<% if( sessionMng.isAuthorized("DPR", "DPRUpload.INV") ) { %>
			<li><a data-url='DPRUpload?mode=iup&uploadType=INV'><mtl:message key="jsp.SUBMENU_INV"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPRUpload.SEO") ) { %>
			<li><a data-url='DPRUpload?mode=iup&uploadType=SEO'><mtl:message key="jsp.SUBMENU_SEO"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPRUpload.CMT") ) { %>
			<li><a data-url='DPRUpload?mode=iup&uploadType=CMT'><mtl:message key="jsp.SUBMENU_CMT"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPRUpload.SKM") ) { %>
			<li><a data-url='DPRUpload?mode=iup&uploadType=SKM'><mtl:message key="jsp.SUBMENU_SKM"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("DPR", "DPRUpload.CIM") ) { %>
			<li><a data-url='DPRUpload?mode=iup&uploadType=CIM'><mtl:message key="jsp.SUBMENU_CIM"/></a></li>
		<% } %>
--%>
		</ul>
	<% } %>

	<% if( sessionMng.isAuthorized("DPR","DPRUpload.LST") ) { %>
		<ul class="sub-menu log">
			<li><a data-url='DPRUpload?mode=list'><mtl:message key="jsp.SUBMENU_DPR_UPLOADLOG"/></a></li>
		<% if( "JNJAP_KR".equals(sessionMng.getPartyId()) ) { %>
			<li><a data-url='RBMUploadLog?mode=list'><mtl:message key="jsp.SUBMENU_RBM_UPLOADLOG"/></a></li>
		<% } %>
		</ul>
	<% } %>

		<ul class="sub-menu user">
			<li><a data-url='USRUser?mode=info'><mtl:message key="jsp.SUBMENU_USERINFO"/></a></li>
		<%
			if( sessionMng.isAuthorized("USR","USRUser.LST") ) {
				String param = "";
				if( com.irt.dpr.Country.KOREA_ORGANIZATION.equals(organizationCode) ) {
					param = "&max=30";
				}
		%>
			<li><a data-url='USRUser?mode=list<%=param%>'><mtl:message key="jsp.SUBMENU_USERLIST"/></a></li>
		<% } %>
		<% if( sessionMng.isAuthorized("USR","USRUserSession.LST") ) { %>
			<li><a data-url='USRUserSession?mode=list'><mtl:message key="jsp.SUBMENU_USERSESSION"/></a></li>
		<% } %>
		</ul>
	</div>

	<iframe id='content' name='content'>
	</iframe>

	<div class='content-overlay'>
		<div class='loading'>
			<img src="images/loading.gif">
			<p class='msg'><mtl:message key="jsp.MSG_LOADING"/></p>
		</div>
	</div>

	<div class='popup-overlay'>
		<div class='popup confirm'>
			<p class='header-msg'></p>
			<span style='float: right'>
				<mtl:button type="confirm" name="confirm"/>
				<mtl:button type="cancel" name="cancel"/>
			</span>
		</div>
		<div class='popup alert'>
			<p class='header-msg'></p>
			<span style='float: right'>
				<mtl:button type="confirm" name="confirm"/>
			</span>
		</div>
	</div>
</body>
</mtl:html>
