<%--
	File Name:	ics_board_list.jsp
	Version:	2.2.5c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2017/09/30		2.2.5c	FAQ 페이지 적용: menuhtml 적용. 기존 modify,info,regist 기능에 selfOpen 옵션 추가
	hankalam	2017/02/28		2.2.4c	Important Post 기능 추가
	GimHS		2015/05/29		2.2.4	CrossBrowsing 적용: 스타일 수정
	GimHS		2012/12/31		2.2.3	CrossBrowsing 적용
										  -> 표시건수 select 박스가 아래로 내려가는 문제 해결
										  -> 리스트 좌측 상단에 있는 검색 Select 박스 일렬로 안나오는 문제 해결
										  -> background 스타일을 속성별로 분리(통합해서 사용하면 타브라우저에서 동작 안함)
	stghr12		2011/06/30		2.2.2	pageEncoding="euc-kr" 추가
	stghr12		2010/07/31		2.2.1	정리
	lsinji		2009/10/25		2.2.0	create
--%>

<%@page import="com.irt.rbm.SessionMng"%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	String boardClassCode = property.getProperty( "boardClassCode" );
	String type = property.getProperty( "type" );
%>

<head>
	<%@ include file="include_ics_board_header.inc" %>
	<%@ include file="include_pub_menuhtml_bodyheader.inc"%>
	<script type='text/javascript' id='page_fadein'>
		/** page fade in block start */
		function Page(){};
		document.write('<style>body .content-wrapper { visibility: hidden; } body .content-wrapper.is-hide { visibility: hidden; } body .content-wrapper.is-show { visibility: visible; }</style>');
		jQuery(document).ready(function(){
			Page.delay();
		});
		Page.delay = function() {
			var secs = 1000;
			setTimeout('Page.initFadeIn()', secs);
		}
		Page.initFadeIn = function() {
			jQuery("body .content-wrapper").css("visibility","visible");
			jQuery("body .content-wrapper").css("display","none");
			jQuery("body .content-wrapper").fadeIn(1200);
		}
		/** page fade in block end */
	</script>
<% if( "faq".equals(htmlpage.getMode()) || "faqmng".equals(htmlpage.getMode()) ) { %>
	<script type='text/javascript' id='faq_page'>
		<%
			String defaultSupportLocale = "en";
			String defaultSupportLocaleLabel = "English";
			if( "JNJAP_CN".equals(sessionMng.getPartyId()) ) {
				defaultSupportLocale += ",zh";
				defaultSupportLocaleLabel += ",Simplified-Chinese";
			} else if( "JNJAP_TH".equals(sessionMng.getPartyId()) ) {
				defaultSupportLocale += ",th";
				defaultSupportLocaleLabel += ",Thailand";
			}
			String menuId = "ICSBoard." + boardClassCode;
			String supportLocale = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;"+ menuId, defaultSupportLocale);
			String supportLocaleLabel = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocaleLabel;"+ menuId, defaultSupportLocaleLabel);
		%>
		<% if( sessionMng.isSystemAdmin() ) { %>
			window.Noty.options = { debug: true, maxVisible : 1000 };
		<% } else { %>
			window.Noty.options = { debug: false };
		<% } %>

		window.MenuMng.options = $.extend({}, window.MenuMng.options, {});
		MenuMng.pushOption = function(key, val) {
			var opts = window.MenuMng.options;
			opts[key] = val;

			window.MenuMng.options = $.extend(window.MenuMng.options, opts);
			return MenuMng.options;
		};

		var registerMenuLinkLoadableClasses = function(docWatcherSelector) {
			$(docWatcherSelector).each(function(idx,obj){// obj is alink
				$obj = $(obj);
				if( $obj.attr("href") != "JavaScript:void(0)" ) {
					var href = $obj.attr("href");
					if( !isAbsoluteUrl(href) ) {

						href = getClassUrlLink( href );
						$obj.attr("href", href);
					}

					if( !$obj.hasClass("loadlink") ) {
						$obj.addClass("loadlink");
					}
				}
			});
		};

		function getClassUrlLink( relUrl ) {
			return "<%=systemConfig.getClassURL()%>" + relUrl;
		}

		function replaceRelativeUrlToClassUrl( menuLinkSelector ) {
			$(menuLinkSelector).each(function(idx,obj) {
				var $obj = $(this);
				if( $obj.attr("href") != "JavaScript:void(0)" ) {
					if( !isAbsoluteUrl($obj.attr("href")) ) {
						var href = getClassUrlLink($obj.attr("href"));
						$obj.attr("href", href);
					}
				}
			});
		}

		var registerMenuLinkLoadable = function(docWatchSelector) {
			$(document).on("click", docWatchSelector, function(event){
				var $obj = $(this);
				var canInvokeCustomEvent = ($obj.attr("href") != "JavaScript:void(0)");
				if( true == canInvokeCustomEvent ) {
					var $li = $obj.parent();
					MenuHtml.setMenuLinkActive($li);

					event.preventDefault();// will do custom action

					var href = $obj.attr("href");
					MenuMng.loadContent(href, $(".content-wrapper"), "form[name='frmMain']");
				}
			});
		};

		var watchOnCheckbox = function() {
			var selector = ".content-wrapper tr td input[name=listcheckbox].menu-href-inputable";
			var removeCheckboxHrefInputableClasses = function() {
				$(".content-wrapper tr td input[name=listcheckbox]").removeClass("menu-href-inputable")
			};
			$(document).on("click", selector, function(event){
				var handleCheckboxClick = function(event){
					event.preventDefault();// prevent checkbox click's original event
					var noty_bar = MenuHtml.getNotyEditingBar();
					var noty_ed_wrap = noty_bar.find("[data-curr-func-name]");
					if( noty_ed_wrap.length > 0 ) {
						if( noty_ed_wrap.attr("data-curr-func-name") == "Function.MenuHtml.linkMenu" ) {
							var url = getBoardInfoUrl(event.target);
							if( url ) {
								var relUrl = url.replace("<%=systemConfig.getClassURL()%>", "");
								var noty_editing = noty_ed_wrap.find(".noty-editing");
								noty_editing.val(relUrl);
								var noty_buttons = MenuHtml.getNotyEditingButtons();
								var ok_button = noty_buttons.find(".btn-noty-editing-ok");
								if( ok_button.length > 0 ) {
									ok_button.click();
								}
							}
						}
					}
				};

				handleCheckboxClick(event);
				removeCheckboxHrefInputableClasses();
			});
		};
		var watchOnLinkIcon = function() {
			var selector = ".menu-wrapper .menuhtml-editing i.fa-link";
			var registerCheckboxHrefInputableClasses = function() {
				$(".content-wrapper tr td input[name=listcheckbox]").addClass("menu-href-inputable")
			};
			$(document).on("click", selector, function(event){
				registerCheckboxHrefInputableClasses();
			});
		};
		var registerLinkIconInvokeMenuHrefInputable = function() {
			watchOnLinkIcon();// watch on link icon
			watchOnCheckbox();// watch on listcheckbox and do input
		};

		function registerEditModeShowList() {
			$("[name=btn-menu-edit-toggle]").on("click", function(event){
				var goingEditMode = $(event.target).find(".fa-toggle").hasClass("fa-toggle-off");
				if( true == goingEditMode ) {
					var url = getLocationURL();
					url = replaceQueryValue( url, "mode", "faq" );
					MenuMng.loadContent(url, $(".content-wrapper"), "form[name='frmMain']");
				}
				$("#menu-creator").toggle("hide");
			});
		}

		var bodyLoadForFaqBoard = function(options) {
			var menuurl = "<%=systemConfig.getClassURL()%>/CSTMenu";
			var defaultOptions = {
				url: menuurl,
				supportLocale: "<%=supportLocale%>",
				supportLocaleLabel: "<%=supportLocaleLabel%>",
				menuId: "<%=menuId%>",
				menuLocale: "<%=htmlpage.getLocale().getLanguage()%>",
				menuMessage: "Home",
				canEditMode: false
			};
			var opts = $.extend(defaultOptions, options);

			var mng = new MenuMng(opts);
			MenuMng.getMenuHtml().then(function(state) {
				replaceRelativeUrlToClassUrl(".menuhtml a.menu-link");

				if( Assert.getBoolean(opts.canEditMode) ) {// admin user
					$(".content-wrapper").addClass("is-edit-mode-content");
					$(".menuhtml-editing a.menu-link").on("click", function(event){
						console.log(event.target + " preventDefault();");
						event.preventDefault();
						return false;
					});

					registerMenuLinkLoadableClasses(".menuhtml-editing a.menu-link:not(has-not-menu-link)");
					registerMenuLinkLoadable(".menu-wrapper a.menu-link:not(has-not-menu-link)");
					registerLinkIconInvokeMenuHrefInputable();

					MenuMng.editModeToggleTo("init");
					registerEditModeShowList();
				} else {// normal user
					var topurl = $(".menuhtml .menu-top > a.menu-top-link").attr("href");
					if( topurl ) {
						MenuMng.loadContent(topurl, $(".content-wrapper"), "form[name='frmMain']");

						registerMenuLinkLoadableClasses(".menuhtml a.menu-link:not(has-not-menu-link)");
						registerMenuLinkLoadable(".menuhtml a.menu-link:not(has-not-menu-link)");
					} else {
						customPopup.alert( { "header" : "<%=msghandler.getMessage("MSG_ICS_BOARD_FAQPAGE_NOT_READY")%>" } );
						windowOpen( "<%= systemConfig.getClassURL() %>/Menu?type=dpr", getLocationURL() );
					}
				}
			});
		};

		function getBoardInfoUrl( elmt ) {
			var boardNumber = elmt.value;
			var url = "<%=htmlpage.getRequestURL()%>?boardClassCode=<%=boardClassCode%>&type=<%=type%>";
			url = replaceQueryValue(url, "mode", "info")
			url = replaceQueryValue(url, "boardNumber", boardNumber);
		//	url = replaceQueryValue(url, "locale", );// not include locale. assume page is for the faq menu
			return url;
		}

		<%
		if( "faq".equals(htmlpage.getMode()) ) { %>
			$(document).ready(function(){
				bodyLoadForFaqBoard({ canEditMode: "<%=sessionMng.isAuthorized("DPR", "CSTMenu.MNG."+ menuId)%>" });
			});
		<% } %>
	</script>
<% } %>
	<script type='text/javascript'>

		$(function() {
			syncFrameHeight();

			$(window).resize( function () {
				syncFrameHeight();
			});

			if( parent.$("iframe.main-content") ) {
				parent.$(".menu-content").contents().find( ".frame-content").innerHeight( $(".frame-content").innerHeight() );
			}
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

		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>";
			requestDelete( url, frmMain.listcheckbox, "boardNumber" );
		}

		function headwordReq() {
			windowOpen( "<%= systemConfig.getClassURL() %>/ICSBoardHeadword?mode=list&wintype=sub&boardClassCode=<%= boardClassCode %>", "sub-content" );
		}

		function modifyReq( boardNumber ) {
			var selfOpen = ("faq" == "<%= htmlpage.getMode() %>") ? true : undefined;
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>&type=<%=type%>";
			requestModify( url, frmMain.listcheckbox, "boardNumber", boardNumber, "main_content" );
		}

		function infoReq( boardNumber ) {
			var selfOpen = ("faq" == "<%= htmlpage.getMode() %>") ? true : undefined;
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>&type=<%=type%>";
			requestInfo( url, frmMain.listcheckbox, "boardNumber", boardNumber, "main_content" );
		}

		function registReq() {
			var selfOpen = ("faq" == "<%= htmlpage.getMode() %>") ? true : undefined;
			if( true == selfOpen ) {
				windowSelfOpen( "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>&mode=ireg&type=<%=type%>", getLocationURL() );
			} else {
				windowOpen( "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>&mode=ireg&type=<%=type%>&wintype=sub", "main_content" );
			}
		}

		function importantPostReq() {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>";
			url = getRequestMultiURL( url, "important", frmMain.listcheckbox, "boardNumber" );

			if( !url ) return;

			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );

			customPopup.confirm( { "detail": "<mtl:message key="MSG_ICS_BOARD_CONFIRM_IMPORTANT_POST" encodeScript="true"/>" }, function(res) {
				if( res ) {
					if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
						submitPost( url );
					} else {
						location.replace( url );
					}
				}
			});
		}
	</script>
</head>

<body class='content' style='padding-right: 8px; overflow: hidden;'>
	<div class='frame-content-wrap'>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_ics_board_bodyheader.inc" %>

		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
			<mtl:hidden id="property" key="type"/>
			<mtl:contentGroup groupId="list" type="list" styleClass="frame-content" style="min-height: 600px;">
				<h2><mtl:message key="jsp.ics_board_list.SUBTITLE_BOARD"/></h2>

				<div class='list-menu'>
				<% if( sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".DEL") ) { %>
	<%-- 				<mtl:select id="request" key="noticeManage" prefixKey="jsp.ics_boardlist.VIEW_" codeValues="ALL,OG"
							searchable="false" width="auto" hasBlank="true" nullValueKey="jsp.ics_boardlist.VIEW_" modified="JavaScript:listLink(this);"/> --%>
					<mtl:select id="condition" key="headwordCode" condition="<%= \"boardClassCode=\"+ property.getProperty(\"boardClassCode\") %>"
							className="com.irt.ics.BoardHeadword" listCodeKey="headwordCode" listNameFormat="$H{headwordName}"
							hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_ICS_BOARD_HEADWORD" modified="JavaScript:listLink(this);"
							searchable="false" width="auto"/>
					<% if( sessionMng.isAuthorized("ICS", "ICSBoardHeadword.MNG") ) { %>
						<mtl:button type="button" onClick="JavaScript: headwordReq();" messageKey="jsp.BTN_HEADWORD_EDIT"/>
					<%} %>
				<% } %>
				</div>

				<%
					ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );

					if( sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".DEL") )
						listwriter.setCheckboxType( ListWriter.CHECKBOXTYPE_CHECK );
					listwriter.setNumbering( false );
					listwriter.setImageBasePath( "images/board" );
					listwriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="return" styleClass="btn-small btn-secondary" style="padding: 0 10px; margin: 0;"/>
					<% if( sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".MNG") ) { %>
						<mtl:button type="regist" styleClass="btn-small btn-secondary" style="padding: 0 10px; margin: 0;"/>
						<% if( listwriter.containsData() ) { %>
							<mtl:button type="s_button" onClick="JavaScript: modifyReq();" messageKey="jsp.BTN_MODIFY"
									styleClass="btn-small btn-secondary" style="padding: 0 10px; margin: 0;"/>
						<% } %>
					<% } %>
					<% if( sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".DEL") && listwriter.containsData() ) { %>
						<mtl:button type="s_button" onClick="JavaScript: deleteReq();" messageKey="jsp.BTN_DELETE"
								styleClass="btn-small btn-secondary" style="padding: 0 10px; margin: 0;"/>
					<% } %>
					<% if( sessionMng.isAuthorized("ICS", "ICSBoard."+ boardClassCode +".MNG") && listwriter.containsData() ) { %>
						<mtl:button type="s_button" onClick="JavaScript: importantPostReq();" messageKey="jsp.BTN_IMPORTANT"
								styleClass="btn-small btn-secondary" style="padding: 0 10px; margin: 0;"/>
					<% } %>
					</div>
					<div id='list_page' class='page'>
						<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>
