<%--
	File Name:	include_rbm_header.inc
	Version:	2.2.5c(dpr)

	Description:
		window.onLoad시에 initFocus() 호출추가(attachWindowEvent)
		script
			classURL
			attachDefaultParameter( url, attachBackURL );
			checkURLLength( url );
			fitHeightToWindow( listObj, minimumHeight );
			getLocationURL( ... );
			initFocus();
			listMouseOut( obj );
			listMouseOver( obj, className1, className2 );
			submitInput();
			windowClose( reload );
			windowMoveToCenter();
			windowOpen( url, winname, attr );
			windowReplace( url, isPostAvailable );
			windowResizeTo( width, height );
			windowSelfOpen( url, backURL );
		script(using)
			attachWindowEvent();
			bodyLoad();						// initFocus()시 bodyLoad가 정의되어 있으면 호출
			replaceQueryValue();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.5c	신규 UI/UX 적용
	jbaek		2019/01/30		2.2.5c	include font-awesome.css, jquery-noty.js, jquery-ui.min, jquery.fileDownload
	jbaek		2018/03/31		2.2.5c	fitHeightToWindow(): chrome에서 리스트가 1레코드일 경우 너무 작게 나오는 문제 수정
	hankalam	2015/10/30		2.2.5	웹 취약성 수정. initFocus() : focus 값 pageConfig 에서 갖고오도록 수정
	jbaek		2015/04/30		2.2.4	Cross Browsing 적용 :
										 -> fitHeightToWindow(): Chrome이고, scrollHeight가 minimumHeight보다 작을 경우 listObj의 height를 scrollHeight값 사용.
										 -> windowReplace(): 추가
										 -> $.fn.textWidth(): jquery. text의 수평 사이즈 계산시 사용.
										 -> resizeDescCol(): jquery. list의 description 칼럼의 사이즈를 지정하여 description칼럼만 많은 공간을 차지하지 않도록 변경.
										 -> syncListHorizontalScroll(): jquery. header의 수평 스크롤과 data의 수평 스크롤이 연동되도록 변경.
										 -> resizeList(): jquery. (dpr) .footer와 list사이의 공간을 계산하여 , 동으로 브라우저 사이즈에 list가 맞추어지도록 변경.
										 -> doReadyWork(): jquery. reload시에 적용이 필요한 스크립트 내용.
	jbaek		2014/09/30		2.2.3	Cross Browsing 적용 : fitHeightToWindow(), windowResizeTo()
	stghr12		2008/05/31		2.2.2	initFocus(): RBMSystem의 환경변수("RBM", "WebService;FocusAtLoad")를 사용해서 self.focus() 처리
	stghr12		2008/03/31		2.2.1	fitHeightToWindow() 추가
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
										attachDefaultParameter(): "locale" 추가
										checkURLLength() 추가
										initFocus(): "focustab" 처리 추가, "focus 처리"/"bodyLoad()" 순서 변경
										windowClose(): self.opener.closed 검사 추가
										windowOpen(): "clsInfo" 삭제, "locale" 추가
										windowResizeTo(): 동작방식 변경
	stghr12		2007/10/31		2.1.2	getLocationURL(): "savedkey" 추가
	stghr12		2007/04/30		2.1.1	listMouseOut(), listMouseOver() 수정
										windowMoveToCenter() 추가
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
										HtmlPage.MAX_URL_LENGTH 사용
	stghr12		2006/10/21		2.0.1	windowOpen()에서 url이 너무 길 경우 에러 후 return하도록 오류 수정
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/03		1.0.0	create
--%>

	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<!--
	<meta http-equiv="Content-Security-Policy" content="default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';;">
-->
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
	<% {
		String[] styleSheetNames = htmlpage.getStyleSheetNames();
		for( int i = 0; i < styleSheetNames.length; i++ )
			out.println( "<link rel='stylesheet' href='style/"+ styleSheetNames[i] +"'/>" );
	} %>

<style type="text/css">
pre {
	border: 0;
	background-color: transparent;
}

pre.bootstrap {
	border: 1px solid #ccc;
	background-color: #f5f5f5;
}

.modal .btn{cursor: pointer;border: 1px solid #999999;text-align: center;border-radius: 5px;outline: none;font-weight: 500;}
.dimLayer{display: block;width: 100%;background-color: rgba(0, 0, 0, 0.3);position: fixed;left: 0;top: 0px;margin: 0;padding: 0;z-index: 9998;}
.modal{width: 600px;height: 252px;border-radius: 10px;padding: 80px 24px;box-sizing: border-box;text-align: center;}
.modal-section{background: #ffffff;box-sizing: border-box;display: none;position: absolute;top: 50%;left: 50%;-webkit-transform: translate(-50%, -50%);-ms-transform: translate(-50%, -50%);-moz-transform: translate(-50%, -50%);-o-transform: translate(-50%, -50%);transform: translate(-50%, -50%);display: none;z-index: 9999;}
.menu_msg{font-size: 21px;font-weight: 500;}
.enroll_box p{padding-bottom: 56px;}
.gray_btn {width: 90px;background: #ffffff;color: #999999;height: 36px;line-height: 36px;transition: 0.5s;font-size: 17px;}
.pink_btn {width: 90px;background: #ed197a;color: #fff;height: 36px;line-height: 36px;transition: 0.5s;font-size: 17px;border: none;}

</style>
	<%-- <link rel='stylesheet' href='<%= systemConfig.getClassURL()+"/../" %>html/fonts/font-awesome.min.css'/> --%>
	<link rel='stylesheet' href='style/jquery-ui.css'/>
	<link rel='stylesheet' href='style/dpr_common.css'/>
	<link rel='stylesheet' href='style/dpr_footer.css'>
	<script type='text/javascript' src='script/common.js'></script>
	<script type='text/javascript' src='script/utils.js'></script>
	<script type='text/javascript' src='script/link.js'></script>
	<script type='text/javascript' src='script/jquery.min.js'></script>
	<script type='text/javascript' src='script/jquery-noty.js'></script>
	<script type='text/javascript' src='script/jquery.cookie.js'></script>
	<script type='text/javascript' src='script/jquery-ui.js'></script>

	<script type='text/javascript'>
		var windowOnload=window.onload||function(){};window.onload=function(){windowOnload();};
		var classURL = "<%= systemConfig.getClassURL() %>/";
		var eventObj;
		var agent = navigator.userAgent.toLowerCase();
		var ieBrowser = false;
		if ( (navigator.appName == 'Netscape' && agent.indexOf('trident') != -1) || (agent.indexOf("msie") != -1)) {
			ieBrowser = true;
		}

		var customPopup = {
			dialog : function( messages, callback ) {
				var yesButton, noButton;
				if( parent.$("body.frame-content").length ) {
					yesButton = parent.$("div.popup.dialog button[name=confirm]");
					noButton = parent.$("div.popup.dialog button[name=cancel]");
				} else {
					yesButton = $("div.popup.dialog button[name=confirm]");
					noButton = $("div.popup.dialog button[name=cancel]");
				}

				yesButton.on( "click", function() {
					$(this).unbind( "click" );
					noButton.unbind( "click" );
					customPopup.close( "dialog" );
					callback( true );
				});

				noButton.on( "click", function() {
					$(this).unbind( "click" );
					yesButton.unbind( "click" );
					customPopup.close( "dialog" );
					callback( false );
				});

				this.open( "dialog", messages );
			},

			confirm : function( messages, callback ) {
				var confirmButton, cancelButton;
				if( parent.$("body.frame-content").length ) {
					confirmButton = parent.$("div.popup.confirm button[name=confirm]");
					cancelButton = parent.$("div.popup.confirm button[name=cancel]");
				} else {
					confirmButton = $("div.popup.confirm button[name=confirm]");
					cancelButton = $("div.popup.confirm button[name=cancel]");
				}

				confirmButton.on( "click", function() {
					$(this).unbind( "click" );
					cancelButton.unbind( "click" );
					customPopup.close( "confirm" );
					callback( true );
				});

				cancelButton.on( "click", function() {
					$(this).unbind( "click" );
					confirmButton.unbind( "click" );
					customPopup.close( "confirm" );
					callback( false );
				});

				this.open( "confirm", messages );
			},

			alert : function( messages, callback ){
				if( parent.$("body.frame-content").length ) {
					button = parent.$("div.popup.alert button[name=confirm]");
				} else {
					button = $("div.popup.alert button[name=confirm]");
				}

				button.on( "click", function() {
					$(this).unbind( "click" );
					customPopup.close( "alert" );
					if( typeof callback === "function" ) {
						callback();
					}
				});

				this.open( "alert", messages );
			},

			open : function( type, messages ) {
				if( parent.$("body.frame-content").length ) {
					togglePopupParent( true, type, messages );
				} else {
					togglePopup( true, type, messages );
				}
			},

			close : function( type ) {
				if( parent.$("body.frame-content").length ) {
					togglePopupParent( false, type );
				} else {
					togglePopup( false, type );
				}
			}
		};

		$.widget( "custom.singleSelectmenu", $.ui.selectmenu, {
			_appendTo: function() {
				var element;
				if( window.name == "sub-content" ) {
					element = this.element.parent();
				} else {
					element = this.options.appendTo;
				}

				if ( element ) {
					element = element.jquery || element.nodeType ?
						$( element ) :
						this.document.find( element ).eq( 0 );
				}

				if ( !element || !element[ 0 ] ) {
					element = this.element.closest( ".ui-front, dialog" );
				}

				if ( !element.length ) {
					element = this.document[ 0 ].body;
				}

				return element;
			},
			_renderButtonItem: function( item ) {
				var buttonItem = $( "<span>" );
				if( item.index == 0 && item.value == "" ) {
					if( this.options.placeholder ) {
						this._setText( buttonItem, this.options.placeholder );
					} else {
						this._setText( buttonItem, item.label );
					}
					this._addClass( buttonItem, "placeholder" );
				} else {
					this._setText( buttonItem, item.label );
				}
				this._addClass( buttonItem, "ui-selectmenu-text" );

				return buttonItem;
			},
			_renderMenu: function( ul, items ) {
				var that = this;
				$.each( items, function(index, item) {
					that._renderItemData( ul, item );
				});

				if( !$(ul).closest("div.selectmenu-menu-wrapper").length ) {
					$(ul).wrap( $("<div/>").addClass("selectmenu-menu-wrapper") );

					if( this.options.searchable ) {
						var menuWrap = $(ul).parent();
						var search = $("<div/>").addClass( "selectmenu-menu-search" );
						search.append( "<input type='text' placeholder='<mtl:message key="jsp.MSG_TEXT_PLACEHOLDER_SEARCH"/>'class='selectmenu-search-input'>" );
						menuWrap.prepend( search );

						var searchInput = menuWrap.find( ".selectmenu-search-input" );

						searchInput.off( "keydown" ).on( "keydown", function(e) {
							switch ( e.keyCode ) {
							case 38:	// UP
								that._move( "prev", event );
								break;
							case 40:	// DOWN
								that._move( "next", event );
								break;
							case 13:	// ENTER
								that._selectFocusedItem( event );
								break;
							}
						}).off( "keyup" ).on( "keyup", function(e) {
							if( e.keyCode !== 38 && e.keyCode !== 40 && e.keyCode !== 13 ) {
								var value = $(this).val().toLowerCase();
								$(ul).children( "li" ).filter( function() {
									var visible = $(this).text().toLowerCase().indexOf( value ) > -1;
									$(this).toggle( visible );
								});
							}
						});
					}
				}

				if( items[0] && items[0].value == "" ) {
					$(ul).children( "li" ).first().find( "div" ).addClass( "not-choose" );
				}
			},
			_move: function( direction, event ) {
				var item, next,
					filter = ".ui-menu-item";

				if ( this.isOpen ) {
					//item = this.menuItems.filter( ":visible" ).eq( this.focusIndex ).parent( "li" );
					item = this.menuItems.eq( this.focusIndex ).parent( "li" );
				} else {
					item = this.menuItems.eq( this.element[ 0 ].selectedIndex ).parent( "li" );
					filter += ":not(.ui-state-disabled)";
				}

				if ( direction === "first" || direction === "last" ) {
					next = item[ direction === "first" ? "prevAll" : "nextAll" ]( filter ).eq( -1 );
				} else {
					next = item[ direction + "All" ]( filter + ":visible" ).eq( 0 );
				}

				if ( next.length ) {
					this.menuInstance.focus( event, next );
				}
			},
			_select: function( item, event ) {
				var oldSelectedItem = this._getSelectedItem();
				this._super( item, event);
				var selectedItem = this._getSelectedItem();

				oldSelectedItem.children().removeClass( "selected" );
				var hasError = this.element.hasClass("mandatory") && item.index == 0 && item.value == "";
				toggleFieldErrorMessage( this.button, hasError, "<mtl:message key="jsp.MSG_ERR_INPUT_SELECT_MANDATORY"/>" );
				if( !hasError ) {
					selectedItem.children().addClass( "selected" );
				}
			},
			open: function( event ) {
				if( this.element[0].options.length > 0 ) {
					this._super( event );
					this.button.find( ".arrow-down").removeClass( "arrow-down" ).addClass( "arrow-up" );
					var search = this.menuWrap.find( "input.selectmenu-search-input" );
					search.val( "" );
					search.focus();
				}
			},
			close: function( event ) {
				this._super( event );
				this.button.find( ".arrow-up").removeClass( "arrow-up" ).addClass( "arrow-down" );
				this.menu.children( "li" ).filter( function() {
					$(this).toggle( true );
				});
			}
		});

		var selectmenuOptions = {
			icons: { button: "arrow-down" },
			classes: {
				"ui-selectmenu-button": "selectmenu",
				"ui-selectmenu-open": "selectmenu-open"
			}
		};

		var smallSelectmenuOptions = {
			icons: { button: "arrow-down" },
			classes: {
				"ui-selectmenu-button": "selectmenu-small",
				"ui-selectmenu-open": "selectmenu-small-open"
			}
		};

		var messageInit = function( messageClass, message ) {
			var msgObj = $("#messagebar");
			if( msgObj.length ) {
				if( $("#messagebar").hasClass("messagebar") ) {
					msgObj.find("div").remove();
				}
				msgObj.addClass( "messagebar" );
				var msgbox = $("<div class='msgbox table-cell'>");
				msgbox.append( "<a class='circle-close'>" );
				msgObj.append( msgbox );
			}

			if( message && message != "" && msgObj.length ) {
				msgObj.css( "display", "table" );
				msgObj.addClass( messageClass );
				msgObj.children().append( message );
				msgObj.find( ".circle-close" ).removeClass().addClass( "circle-close" ).addClass( messageClass );
			}
			if( window.name == "sub-content" ) {
				parent.subContentResize();
			}
		}

		$( function() {
			if( typeof parent.jQuery == "undefined" ) {
				var script = parent.document.createElement( "script" );
				script.type = "text/javascript";
				script.src = "<%= systemConfig.getBaseURL( htmlpage.getLocale() ) %>script/jquery.min.js";
				parent.document.getElementsByTagName( "head" )[0].appendChild( script );
			}

			<% char pageResult = htmlpage.getResultLevel();
				String messageClass;

				switch( pageResult ) {
					case HtmlPage.PAGE_RESULT_WARNING:
						messageClass = "warning";
						break;
					case HtmlPage.PAGE_RESULT_ERROR:
						messageClass = "error";
						break;
					case HtmlPage.PAGE_RESULT_NULL:
					case HtmlPage.PAGE_RESULT_SUCCESS:
					default:
						messageClass = "success";
				}
			%>

			$("#messagebar").on( "click", "div.msgbox .circle-close", function() {
				$(this).closest( ".messagebar" ).fadeOut();
				if( window.name == "sub-content" ) {
					setTimeout( function() {
						parent.subContentResize();
					}, 500 );
				}
			});

			<%
				String msg = HtmlUtility.toScriptString( htmlpage.getMessage() );
				if( msg != null ) {
					msg = msg.replaceAll( "\t|\n", "" ).trim();
				}
			%>
			console.log("####<%= messageClass %>=<%= msg %>");
			messageInit( "<%= messageClass %>", "<%= msg %>" );

		<% if( (htmlpage.getWindowType() != null && "sub".equals(htmlpage.getWindowType().split("@")[0])) ) { %>
			$(".content-overlay").css( {"box-shadow": "none", "-moz-box-shadow": "none", "-webkit-box-shadow": "none"} );
			$(".popup-overlay").css( {"box-shadow": "none", "-moz-box-shadow": "none", "-webkit-box-shadow": "none"} );
		<% } else { %>
			//if( parent.$("iframe#content").length ) {
			if( parent.document.getElementById("content") ) {
				$("div.list_content, div.search_content, div.tabpane").each( function(index, item) {
					var minWidth = $(this).css( "min-width" );
					if( minWidth ) {
						if( minWidth == "0px" || minWidth == "none" ) {
							minWidth = 910;
						}
					} else {
						minWidth = 910;
					}
					$(this).css( "min-width", minWidth );
				});
			}
		<% } %>

			if( parent.name != 'content' && $(".sub-content-wrap").length ) {
				$(".sub-content-wrap").append( "<iframe id='sub-content' name='sub-content'></iframe>" );

				$("#sub-content").bind( "load", function () {
					if( frames["sub-content"].document.location.host != "" ) {
						if( $(".content-overlay .loading").is(":visible") ) {
							$(".content-overlay .loading").hide();
						}
						$(".sub-content-wrap").show();
						subContentResize();
					}

					//$("body").css( "overflow", "hidden" );
				});
			}

			$(window).resize( function () {
				if( $(".sub-content-wrap").is(":visible") ) {
					subContentResize( true );
				}
			});
		});

		function subContentResize( windowResize ) {
			var timer = ieBrowser && !windowResize ? 30 : 0;
			if( $("#sub-content").length < 1 ) {
				return;
			}

			var frameWindow = $("#sub-content")[0].contentWindow;
			if( $("#sub-content").contents().find(".content-wrap").length < 1 ) {
				return;
			}

			setTimeout( function() {
				var fixWidth = frameWindow.windowWidth;
				var fixHeight = frameWindow.windowHeight;
				if( !fixHeight ) {
					var contentMaxHeight = $(".content-overlay").height() - 70;
					var minHeight = $(".sub-content-wrap").css( "min-height" );
					minHeight = minHeight.replace( "px", "" );
					if( contentMaxHeight <= minHeight) {
						return;
					}
					$(".sub-content-wrap").height( minHeight );
					var frameDocHeight = $("#sub-content").contents().find( ".content-wrap" )[0].scrollHeight + 65;
					//var frameDocHeight = frameWindow.document.getElementById("content-wrap").scrollHeight + 60;

					if( contentMaxHeight > frameDocHeight ) {
						$(".sub-content-wrap").height( frameDocHeight );
					} else {
						$(".sub-content-wrap").height( contentMaxHeight );
					}
				} else {
					$(".sub-content-wrap").height( fixHeight );
				}
				if( fixWidth ) {
					$(".sub-content-wrap").width( fixWidth );
				}
			}, timer );
		}

		function toggleFieldError( fieldObj, errorMessage ) {
			if( $(fieldObj).length ) {
				var lastIndex = $(fieldObj).length - 1;
				var type = $(fieldObj[lastIndex]).attr( "type" );
				var hasError = false;
				var message;
				if( errorMessage ) {
					message = errorMessage;
				}
				if( type == "checkbox" || type == "radio" ) {
					var count = 0;
					$(fieldObj).each( function(index, item) {
						if( $(this).is(":checked") ) {
							count++;
						}
					});
					hasError = count < 1;
					if( !message ) {
						message = "<mtl:message key="jsp.MSG_ERR_INPUT_CHECK_MANDATORY"/>";
					}
				} else {
					hasError = $(fieldObj).val().length < 1;
					if( !message ) {
						message = "<mtl:message key="jsp.MSG_ERR_INPUT_TEXT_MANDATORY"/>";
					}
				}

				toggleFieldErrorMessage( fieldObj, hasError, message );
				return !hasError;
			}
			return true;
		}

		function toggleFieldErrorMessage( fieldObj, visible, errorMessage ) {
			var parent = $(fieldObj).parent();
			if( visible ) {
				if( parent.children("div.error-text").length < 1 ) {
					var errorObj = $("<div/>");
					errorObj.addClass( "error-text" );
					errorObj.text( errorMessage );

					parent.append( errorObj );
					$(fieldObj).addClass( "error" );
				}

				if( $("div.search-bottom .accordion-button").length
						&& $("div.search-bottom .accordion-button").hasClass("arrow-down") ) {
					$("div.search-bottom .accordion-button").trigger( "click" );
				}

				var errorObj = parent.children( "div.error-text" );
				var count = 0;
				var highlight = setInterval( function() {
					if( count < 4 ) {
						errorObj.toggle();
						count++;
					} else {
						clearInterval( highlight );
					}
				}, 300);

			} else {
				$(fieldObj).removeClass("error");
				if (parent.children("div.error-text").length) {
					parent.children("div.error-text").remove();
				}
			}
		}

		function toggleDisabled( disabled, elements ) {
			$(elements).each( function(index, item) {
				if( $(item).singleSelectmenu("instance") ) {
					$(item).singleSelectmenu( "option", "disabled", disabled );
				} else {
					if( disabled ) {
						$(item).attr( "disabled", "disabled" );
					} else {
						$(item).removeAttr( "disabled" );
					}
				}
			})
		}

		function attachDefaultParameter(url, attachBackURL) {
			<% if( htmlpage.getWindowType() != null ) { %>
				url = replaceQueryValue( url, "wintype", "<%= htmlpage.getWindowType() %>" );
			<% } %>
			<% if( htmlpage.getSystemMenu() != null ) { %>
				url = replaceQueryValue( url, "menu", "<%= htmlpage.getSystemMenu() %>" );
			<% } %>
			url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
			url = replaceQueryValue( url, "debugSQL", "<mtl:value id="request" key="debugSQL"/>" );
			<% if( htmlpage.getBackURL() != null ) { %>
				if( attachBackURL )
					url = replaceQueryValue( url, "url", encodeURIComponent("<%= htmlpage.getBackURL() %>") );
			<% } %>

			return url;
		}

		function checkURLLength( url ) {
			if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_TOO_LONG_URL" encodeScript="true"/>" } );
				return false;
			} else
				return true;
		}

		function fitHeightToWindow( listObj, minimumHeight, analyzing ) {
			if( typeof listObj == "undefined" || !minimumHeight ) {
				var elements = document.getElementsByTagName( "div" );

				for( var e = 0; e < elements.length; e++ ) {
					// div.onResize onDblClick(div.onResize)
					if( elements[e].ondblclick )
						elements[e].ondblclick();
				}
			} else {
				var clientHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
				var height = clientHeight + listObj.offsetHeight - document.body.scrollHeight;
				if( height < minimumHeight ) height = minimumHeight;

				if( fitHeightToWindow.height2 == height && fitHeightToWindow.height2 > fitHeightToWindow.height1 ) {
					fitHeightToWindow.height1 = fitHeightToWindow.height2 = "";
					return;
				}
				if( listObj.scrollHeight > height ) {
					listObj.style.overflowY = "scroll";
					listObj.style.height = getValidPixel(height);
				} else if( analyzing ) {
					listObj.style.overflowY = "scroll";
					listObj.style.height = "";
				} else {
					listObj.style.overflowY = "scroll";
					if( listObj.offsetHeight < 1 )
						listObj.style.height = getValidPixel(minimumHeight);
					else {
						if( navigator.userAgent.indexOf("Chrome") > 0 ) {
							if( listObj.scrollHeight < minimumHeight ) {
								if( height < minimumHeight )
									listObj.style.height = getValidPixel(listObj.scrollHeight);
							} else {
								listObj.style.height = getValidPixel(listObj.offsetHeight );
							}
						} else
							listObj.style.height = getValidPixel(listObj.offsetHeight);
						}
					}
				}
				fitHeightToWindow.height2 = fitHeightToWindow.height1;
				fitHeightToWindow.height1 = height;
		}

		function getLocationURL() {
			<% if( "GET".equals(request.getMethod()) ) { %>
				var url = replaceQueryValue( location.href, "all", null );
				url = replaceQueryValue( url, "focus", null );
				url = replaceQueryValue( url, "condkey", null );
				url = replaceQueryValue( url, "msgkey", null );
				url = replaceQueryValue( url, "savedkey", null );

				var args = getLocationURL.arguments;
				if( args != null ) {
					for( var i = 0; i < args.length; i++ )
						url = replaceQueryValue( url, args[i], null );
				}

				return url;
			<% } else { %>
				return null;
			<% } %>
		}

		function initFocus() {
			<% if( com.irt.rbm.RBMSystem.getSystemEnvBool("RBM", "WebService;FocusAtLoad", true) ) { %>
				self.focus();
			<% } %>
			if( window.bodyLoad ) bodyLoad();

			<% if( request.getParameter("focustab") != null ) { %>
				var element = document.getElementById( "tabpage_<%= request.getParameter("focustab") %>" );
				if( element && element.tabPage )
					element.tabPage.select();
			<% } %>

			<% if( htmlpage.getFocus() != null ) { %>
				var object = frmMain.<%= com.irt.html.HtmlUtility.toScriptString( htmlpage.getFocus() ) %>;
				if( object ) {
					if( object.select ) object.select();
					if( object.focus ) object.focus();
				}
			<% } %>
			//fitHeightToWindow();
		}
		attachWindowEvent( "load", initFocus );

		function listMouseOut( obj ) {
			obj.className = obj.original_className;
			obj.original_className = null;
		}

		function listMouseOver( obj, className1, className2 ) {
			obj.original_className = obj.className;
			if( className1 ) {
				if( className2 )
					Styles.changeClassName( obj, className1, className2 );
				else
					obj.className = className1;
			} else
				obj.className = 'mouseover';
		}

		function setEventObject( event ) {
			eventObj = event;
		}

		function submitInput( mode ) {
/* 			if( parent.$("body.frame-content").length ) {
				parent.$(".sub-content-wrap").hide();
				toggleLoadingParent( true );
			} else {
				toggleLoading( true );
			}
 */
			if( mode && (mode == "down" || mode == "mdown" || mode == "tdown") ) {
				if( parent.$("body.frame-content").length ) {
					parent.$(".sub-content-wrap").hide();
					downlosdProgress( true );
				} else {
					downlosdProgress();
				}
			} else if( parent.$("body.frame-content").length ) {
				parent.$(".sub-content-wrap").hide();
				toggleLoadingParent( true );
			} else {
				toggleLoading( true );
			}

			return true;
		}

		function toggleLoading( visible, message ) {
			if( $(".content-overlay").length ) {
				if( visible ) {
					if( !message ) {
						message = "<mtl:message key="jsp.MSG_LOADING"/>"
					}
					$(".content-overlay .loading").show();
					$(".content-overlay .loading .msg").empty().append( message );
					$(".content-overlay").fadeIn( 150 ).css( "display", "flex" );
				} else {
					$(".content-overlay .loading").hide();
					$(".content-overlay").fadeOut();
				}
			}
		}

		function toggleLoadingParent( visible, message ) {
			if( parent.$(".content-overlay").length ) {
				if( visible ) {
					if( !message ) {
						message = "<mtl:message key="jsp.MSG_LOADING"/>"
					}
					parent.$(".content-overlay .loading").show();
					parent.$(".content-overlay .loading .msg").empty().append( message );
					parent.$(".content-overlay").fadeIn( 150 ).css( "display", "flex" );
				} else {
					parent.$(".content-overlay .loading").hide();
					parent.$(".content-overlay").fadeOut();
				}
			}
		}

		function togglePopup( visible, type, messages ) {
			if( $(".popup-overlay").length ) {
				var popup = $(".popup."+ type);
				var header = popup.find( ".header-msg" );
				var detail = popup.find( ".msg" );

				if( visible ) {
					if( messages["header"] ) {
						header.text( messages["header"] );
					} else {
						header.hide();
					}
					if( messages["detail"] ) {
						detail.text( messages["detail"] );
					} else {
						detail.hide();
					}

					popup.show();
					$(".popup-overlay").fadeIn( 150 ).css( "display", "flex" );
				} else {
					$(".popup-overlay").fadeOut( 150 );
					popup.hide();
				}
			}
		}

		function togglePopupParent( visible, type, messages ) {
			if( parent.$(".popup-overlay").length ) {
				var popup = parent.$(".popup."+ type);
				var header = popup.find( ".header-msg" );
				var detail = popup.find( ".msg" );

				if( visible ) {
					if( messages["header"] ) {
						header.text( messages["header"] );
					} else {
						header.hide();
					}
					if( messages["detail"] ) {
						detail.text( messages["detail"] );
					} else {
						detail.hide();
					}

					popup.show();
					parent.$(".popup-overlay").fadeIn( 150 ).css( "display", "flex" );
				} else {
					parent.$(".popup-overlay").fadeOut( 150 );
					popup.hide();
				}
			}
		}

		function windowClose( reload ) {
			if( (self.opener && !self.opener.closed) || window.name == "sub-content" ) {
				if( reload ) {
					if( window.name == "sub-content" ) {
						toggleLoadingParent( true );
						parent.location.reload();
					} else {
						self.opener.location.reload();
					}
				}
				if( self.opener ) {
					self.opener.focus();
				}
			}
			if( window.name == "sub-content" ) {
				if( !reload ) {
					parent.$(".content-overlay").fadeOut();
				}
				parent.$(".sub-content-wrap").hide();
				parent.$(".sub-content-wrap").css( "width", "" );
				//parent.$("#sub-content").attr( "src", "<%= systemConfig.getProperty("baseURL") %>blank.html" );
			} else {
				self.close();
			}
		}

		function windowMoveToCenter() {
			var clientWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
			var clientHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
			var left = ( screen.availWidth - clientWidth ) / 2;
			var top = ( screen.availHeight - clientHeight ) / 2;
			if( left < 0 ) left = 0;
			if( top < 0 ) top = 0;

			window.moveTo( left, top );
		}

		function windowOpen( url, winname ) {
			attribute = "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=yes, scrollbars=yes, width=50, height=50";
			if( !url ) return;

			<% if( htmlpage.getSystemMenu() != null ) { %>
				url = replaceQueryValue( url, "menu", "<%= htmlpage.getSystemMenu() %>" );
			<% } %>
			url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
			if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_TOO_LONG_URL" encodeScript="true"/>" } );
				return;
			}

			if( winname == null ) winname = "_self";
			if( winname.indexOf("cls") == 0 ) {
				if( winname == "clsMng" )
					winname = "winMng";
				else if( winname == "clsSel" )
					winname = "winSel";
				else if( winname == "clsName" )
					winname = "subwin_util";

				if( winname != null ) {
					if( self.name.indexOf(winname) == 0 )
						winname = self.name + "0";
					return window.open( url, winname, attribute );
				}
			} else if( winname.indexOf("win") == 0 || winname.indexOf("subwin") == 0 )
				return window.open( url, winname, attribute );
			else {
				var mode = getQueryValue( url, "mode" );
				if( parent.$("body.frame-content").length ) {
					parent.$(".sub-content-wrap").hide();
				}

				if( mode && (mode == "down" || mode == "mdown" || mode == "tdown" || mode == "hdrdwn" || mode=="sdown" || mode=="fdown") ) {
					if( parent.$("body.frame-content").length ) {
						downlosdProgress( true );
					} else {
						downlosdProgress();
					}
				} else if( parent.$("body.frame-content").length ) {
					toggleLoadingParent( true );
				} else {
					toggleLoading( true );
				}
				if( parent.$("body.frame-content").length && winname == "sub-content" ) {
					url = replaceQueryValue( url, "wintype", "sub" );
					return parent.window.open( url, winname );
				} else if( winname == "sub-content" ) {
					url = replaceQueryValue( url, "wintype", "sub" );
				}

				return window.open( url, winname );
			}
		}

		function windowReplace( url, backURL, isPostAvailable ) {
			if( backURL ) {
				url = attachDefaultParameter( url, false );
				url = replaceQueryValue( url, "url", encodeURIComponent(backURL) );
			} else
				url = attachDefaultParameter( url, true );

			if( checkURLLength(url, isPostAvailable) )
				location.replace( url );
			else if( isPostAvailable )
				submitPost( url );
		}

		function windowResizeTo( width, height ) {
			<% if( htmlpage.getWindowType() != null && "sub".equals(htmlpage.getWindowType().split("@")[0]) ) { %>
				var clientWidth = window.innerWidth || document.body.clientWidth;
				var clientHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
//				var clientHeight = window.innerHeight || document.body.clientHeight || document.documentElement.clientHeight;
//				var clientHeight = getDocHeight();
				if( navigator.userAgent.indexOf("Chrome") > 0 ) {
					if( height ) height = height + 70;

					var resizeWidth = ( width ? width : document.body.scrollWidth );
					var resizeHeight = ( height ? height : document.body.scrollHeight );
					if( resizeHeight > screen.availHeight ) resizeHeight = screen.availHeight * 0.85;
					window.resizeTo( resizeWidth, resizeHeight );
				} else {
					var resizeWidth, resizeHeight;
					if( width ) {
						resizeWidth = ( width ? width : document.body.offsetWidth );
						if( screen.availWidth - window.screenLeft < resizeWidth )
							window.moveTo( ( screen.availWidth - resizeWidth ) / 2, window.screenTop );
						window.resizeBy( resizeWidth - clientWidth, 0 );
					}

					resizeHeight = ( height ? height : document.body.offsetHeight );
					if( screen.availHeight - window.screenTop < resizeHeight ) {
						var screenTop = ( screen.availHeight - resizeHeight ) / 2;
						window.moveTo( window.screenLeft, screenTop < 0 ? 0 : screenTop );
					}

					resizeHeight = ( height ? height : document.body.scrollHeight );
					if( resizeHeight > screen.availHeight ) resizeHeight = screen.availHeight * 0.85;
					window.resizeBy( 0, resizeHeight - clientHeight );
				}
			<% } %>
		}

		function windowSelfOpen( url, backURL ) {
			if( backURL ) {
				url = attachDefaultParameter( url, false );
				url = replaceQueryValue( url, "url", encodeURIComponent(backURL) );
			} else
				url = attachDefaultParameter( url, true );

			windowOpen( url );
		}

		function downlosdProgress( hasParent ) {
			if( hasParent ) {
				toggleLoadingParent( true, "<mtl:message key="jsp.MSG_DOWNLOADING"/>" );
			} else {
				toggleLoading( true, "<mtl:message key="jsp.MSG_DOWNLOADING"/>" );
			}

			var downloadTimer = setInterval( function() {
				var fileDownload = $.cookie( "fileDownload" );
				if( fileDownload && fileDownload == "true" ) {
					$.removeCookie( "fileDownload", { path: "/" } );
					if( hasParent ) {
						toggleLoadingParent( false );
					} else {
						toggleLoading( false );
					}
					clearInterval( downloadTimer );
				}
			}, 2000 );
		}
</script>

<script type="text/javascript">
	$.fn.textWidth = function( text, font ) {
		if( !$.fn.textWidth.fakeEl ) $.fn.textWidth.fakeEl = $('<span>').hide().appendTo(document.body);
		$.fn.textWidth.fakeEl.text( $.trim(text || this.val() || this.text()) ).css( 'font', font || this.css('font') );
		return $.fn.textWidth.fakeEl.width();
	};

	function resizeDescCol() {
		$(".list_content_data").each(function(){
			var headerCols = $(this).parent().parent().find("table[class^=list_content_header]").find("col");
			var descMaxTextWidth = 0;
			var descMaxCheckCount = 20;
			$(this).parent().parent().find("tbody>tr>td.description").each(function(index){
				if( index > descMaxCheckCount ) return false;
				if( $(this).textWidth() > descMaxTextWidth ) descMaxTextWidth = $(this).textWidth();
			});
			var tableWidth = $(this).width();
			$(this).find("col").each(function(){
				var colIndex = $(this).index();
				var headerCol = headerCols.eq( colIndex );
				var headerTH = headerCol.parent().parent().find("tbody>tr>th").eq(colIndex);

				// get first line td at the same index
				var firstDataTD = $(this).parent().parent().find("tbody>tr>td").eq(colIndex);
				var width_firstDataTD = firstDataTD.width();

				if( firstDataTD.hasClass("description") ) {
					var descCurrWidth = width_firstDataTD;
					if( descCurrWidth <= 0 ) return;

					var descHeaderTextWidth = parseInt( headerTH.textWidth() ) || 0;
					var descResizedWidth = (width_firstDataTD > 0 ? width_firstDataTD: descHeaderTextWidth); //set default
					if( descHeaderTextWidth > descMaxTextWidth ) {
						descResizedWidth = descHeaderTextWidth;
					} else {
						descResizedWidth = descMaxTextWidth;
					}
					var widthPer = descResizedWidth/tableWidth;
					$(this).attr("width", widthPer+"%");
					headerCol.attr("width", widthPer+"%");
				}
			})
		})
	}

	function resizeList( $divList ) {
		var footerBottom = 0;
		if( $('.footer').position() )
			footerBottom = $('.footer').position().top + $('.footer').outerHeight(true);
		var footerSize = $(document).height() - footerBottom;
		var $listContentData = $divList.find('[class^="list_content_data"]');
		var listContentDataHeight = $listContentData.height();
		var suggestListHeight = 220;//default list size
		if( footerSize > 0 )
			suggestListHeight = listContentDataHeight + footerSize;

		if( $listContentData.length > 0 ) {
			//fitHeightToWindow( $listContentData.get( 0 ), suggestListHeight )
		}
	}

	function doReadyWork(){
		$('table.search_content').each(function(){
			var search_contents = $(this).find('tr td.search_content');
			search_contents.each(function(){
				// this is needed when td has 'input' and 'a' then they needs to be aligned horizontally.
				$(this).css('white-space','nowrap');
			});

			var search_subjects = $(this).find('tr td.search_subject');
			var wMaxFirstElement = 0;
			search_subjects.each(function(){
				// break-word css ???
				$(this).css('word-break','keep-all');
				$(this).css('word-wrap','break-word');
				if( $(this).index() === 0 || $(this).parent().find('td:first-child') ) {
					if( $(this).textWidth() > wMaxFirstElement )
						wMaxFirstElement = $(this).textWidth();
					$(this).addClass('ui-search-subject-first-td');
				}
			});

			if( wMaxFirstElement > 0 ) {
				$('.ui-search-subject-first-td').each(function(){
					if( !$(this).attr('width') )
					$(this).attr('width', wMaxFirstElement);
				})
			}
		});

		// break-word css
		$('table.info_content').each(function(){
			var subjects = $(this).find('tr td.subject');
			subjects.each(function(){
				$(this).css('word-break','keep-all');
				$(this).css('word-wrap','break-word');
			});
		});
	}

	function syncListHorizontalScroll( $listHeader, $listData ) {
		$listHeader.on('scroll',function(){ $listData.scrollLeft($(this).scrollLeft()); });
		$listData.on('scroll',function(){ $listHeader.scrollLeft($(this).scrollLeft()); });
	}
</script>

<% String s_useJsEvent = pageContext.getServletContext().getInitParameter("useJsEvent");
	if( Boolean.parseBoolean(s_useJsEvent) ) { %>
	<%@ include file="include_cst_event_header.inc" %>
<% } %>
<script type='text/javascript'>
/* 	var resizeTimeout;
	$(window).on(function() {
		doReadyWork();
//		resizeDescCol();

		resizeTimeout = setTimeout(function() {
		var listObj = $("div[class^='list_content']");
		listObj.each(function(){
			syncListHorizontalScroll( $(this).find('div[class^=list_content_header]')
					, $(this).find('div[class^=list_content_data]') );
			resizeList($(this));
		})
		}, 500);
	});

	$(function(){
		$(window).bind('resize',function(){
			clearTimeout(resizeTimeout);
			doReadyWork();

			resizeTimeout = setTimeout(function() {
			var listObj = $("div[class^='list_content']");
			listObj.each(function(){
				syncListHorizontalScroll( $(this).find('div[class^=list_content_header]')
					, $(this).find('div[class^=list_content_data]') );
				resizeList($(this));
			})
			}, 500);
		});
	}); */
</script>
