<%--
	File Name:	include_ics_board_header.inc
	Version:	2.2.8c

	Description:
		window.onLoad시에 initFocus() 호출추가(attachWindowEvent)
		script
			classURL
			attachDefaultParameter( url, attachBackURL );
			checkURLLength( url, noalerting );
			fitHeightToWindow( listObj, minimumHeight );
			getLocationURL( ... );
			initFocus();
			listMouseOut( obj );
			listMouseOver( obj, className1, className2 );
			submitInput( url, isPost );
			windowClose( reload );
			windowMoveToCenter();
			windowOpen( url, winname, isPostAvailable );
			windowReplace( url, isPostAvailable );
			windowResizeTo( width, height );
			windowSelfOpen( url, backURL, isPostAvailable );
		script(using)
			attachWindowEvent();
			bodyLoad();						// initFocus()시 bodyLoad가 정의되어 있으면 호출
			replaceQueryValue();
			submitPost();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2017/09/30		2.2.8c	FAQ 페이지 적용: fitHeightToWindow기능 jquery의 "originalEvent"있을 경우 skip하여 jquery이벤트와의 충돌을 막음.
	GimHS		2016/08/31		2.2.8	windowResizeTo(): Chrome에서 창이 계속 작아지는 문제 수정
	jbaek		2016/08/31		2.2.8	medium-editor frontend css 추가
	GimHS		2015/10/30		2.2.7	페이지 이동할 때 location.replace() -> location.href 변경(location.replace()는 주소 history가 기록되지 않음)
	GimHS		2015/07/31		2.2.6	loadingReq(): modal 방식으로 변경, submitInput(): submit 전에 Loading modal 창 표시, modal용 style 추가
										jQuery용 javascript 파일 추가
	GimHS		2015/04/30		2.2.5	loadingReq() 추가, windowOpen() 호출시 Loading 화면이 나오도록 수정(download 기능일때는 제외)
	GimHS		2014/08/29		2.2.4	CrossBrowsing 적용:
										  -> fitHeightToWindow(): listObj.offsetHeight가 0인 경우 listObj.style.height를 minimumHeightfh로 변경
										  -> windowResizeTo(): Chrome에서는 resizeBy() 함수를 지원하지 않아 resizeTo()로 변경
										  -> setEventObject() 추가
										  -> fitHeightToWindow(): 일반 리스트와 분석 리스트의 높이 style을 다르게 적용
	GimHS		2012/12/31		2.2.3	CrossBrowsing 적용: windowResizeTo(width, height) : height 계산시, clientHeight->offsetHeight 로 변경
	stghr12		2010/12/31		2.2.2	fitHeightToWindow(): 무한루프 문제로 height 계산에서 2값이 반복될 경우 작은 값으로 설정되도록 처리
	stghr12		2010/07/31		2.2.1	정리
	lsinji		2009/10/25		2.2.0	create
--%>

	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
<!-- 	<link rel='stylesheet' href='style/dpr_common.css'/>
	<link rel='stylesheet' href='style/dpr_footer.css'>
	<link rel='stylesheet' href='style/board.css'/>
	<link rel='stylesheet' href='style/jquery-ui.css'/> -->
	<link rel='stylesheet' href='style/jquery-ui.css'/>
	<link rel='stylesheet' href='style/dpr_common.css'/>
	<link rel='stylesheet' href='style/dpr_footer.css'>
	<script type='text/javascript' src='script/common.js'></script>
	<script type='text/javascript' src='script/utils.js'></script>
	<script type='text/javascript' src='script/jquery.min.js'></script>
	<script type='text/javascript' src='script/jquery.cookie.js'></script>
	<script type='text/javascript' src='script/jquery-ui.js'></script>
	<script type='text/javascript' src='script/jquery-noty.js'></script>

	<% if( htmlpage.getInputStatus() != HtmlPage.INPUT_MANDATORY ) { %>
		<link rel='stylesheet' href='script/mediumeditor/css/medium-editor-insert-plugin-frontend.ics.min.css'>
	<% } %>

	<script type='text/javascript'>
		var classURL = "<%= systemConfig.getClassURL() %>/";
		var eventObj;

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

			alert : function( messages ){
				if( parent.$("body.frame-content").length ) {
					button = parent.$("div.popup.alert button[name=confirm]");
				} else {
					button = $("div.popup.alert button[name=confirm]");
				}

				button.on( "click", function() {
					$(this).unbind( "click" );
					customPopup.close( "alert" );
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

		$( function() {
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
			var messageInit = function() {
				var msgObj = $("#messagebar");
				if( msgObj.length ) {
					msgObj.addClass( "messagebar" );
					var msgbox = $("<div class='msgbox table-cell'>");
					msgbox.append( "<a class='circle-close'>" );
					msgObj.append( msgbox );
				}
				<%
					String msg = HtmlUtility.toScriptString( htmlpage.getMessage() );
					if( msg != null ) {
						msg = msg.replaceAll( "\t|\n", "" ).trim();
					}
				%>
				var message = "<%= msg %>";
				var messageClass = "<%= messageClass %>";
				if( message && message != "" && msgObj.length ) {
					msgObj.css( "display", "table" );
					msgObj.addClass( messageClass );
					msgObj.children().append( message );
					msgObj.find( ".circle-close" ).addClass( messageClass );
				}
			}
			$("#messagebar").on( "click", "div.msgbox .circle-close", function() {
				$(this).closest( ".messagebar" ).fadeOut();
			});
			messageInit();

		<% if( (htmlpage.getWindowType() != null && "sub".equals(htmlpage.getWindowType().split("@")[0])) ) { %>
			$(".content-overlay").css( {"box-shadow": "none", "-moz-box-shadow": "none", "-webkit-box-shadow": "none"} );
		<% } else { %>
			if( parent.$("iframe#content").length ) {
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
					if( $(".content-overlay .loading").is(":visible") ) {
						$(".content-overlay .loading").hide();
					}
					$(".sub-content-wrap").show();
					subContentResize();
					//$("body").css( "overflow", "hidden" );
				});
			}

			$(window).resize( function () {
				if( $(".sub-content-wrap").is(":visible") ) {
					subContentResize();
				}
			});
		});

		function subContentResize() {
			var frameWindow = $("#sub-content")[0].contentWindow;
			if( $("#sub-content").contents().find(".content-wrap").length < 1 ) {
				return;
			}
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
				var frameDocHeight = $("#sub-content").contents().find( ".content-wrap" )[0].scrollHeight + 60;
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

		function attachDefaultParameter( url, attachBackURL ) {
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

		function checkURLLength( url, noalerting ) {
			if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
				if( !noalerting )
					customPopup.alert( { "header" : "<mtl:message key="MSG_TOO_LONG_URL" encodeScript="true"/>" } );
				return false;
			} else
				return true;
		}

		function fitHeightToWindow( listObj, minimumHeight, analyzing ) {
			if( typeof listObj == "undefined" || !minimumHeight ) {
				var elements = document.getElementsByTagName( "div" );

				for( var e = 0; e < elements.length; e++ ) {
					// div.onResize 대신 onDblClick으로 변경(div.onResize 함수 지원 안함)
					if( elements[e].ondblclick )
						elements[e].ondblclick();
				}
			} else {
				if( listObj.hasOwnProperty("originalEvent") ) return;// if invoked by event object simply return.

				var height = document.body.clientHeight + listObj.offsetHeight - document.body.scrollHeight;
				if( height < minimumHeight ) height = minimumHeight;

				if( fitHeightToWindow.height2 == height && fitHeightToWindow.height2 > fitHeightToWindow.height1 ) {
					fitHeightToWindow.height1 = fitHeightToWindow.height2 = "";
					return;
				}
				if( listObj.scrollHeight > height ) {
					listObj.style.overflowY = "scroll";
					listObj.style.height = height;
				} else if( analyzing ) {
					listObj.style.overflowY = "scroll";
					listObj.style.height = "";
				} else {
					listObj.style.overflowY = "scroll";
					if( listObj.offsetHeight < 1 )
						listObj.style.height = minimumHeight;
					else {
						/*
						//TODO: below chrome specific code may be outdated.
							For Chrome 59, below code is outdated.
						if( navigator.userAgent.indexOf("Chrome") > 0 )
							listObj.style.height = listObj.offsetHeight - 17;
						else
							listObj.style.height = listObj.offsetHeight;
						*/
						listObj.style.height = listObj.offsetHeight;
					}
				}
				fitHeightToWindow.height2 = fitHeightToWindow.height1;
				fitHeightToWindow.height1 = height;
			}
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
			<% if( com.irt.data.Condition.isConditionTrue(property.getProperty("jsp.public.focusOnLoad")) ) { %>
				self.focus();
			<% } %>
			if( window.bodyLoad ) bodyLoad();

			<% if( request.getParameter("focustab") != null ) { %>
				var element = document.getElementById( "tabpage_<%= request.getParameter("focustab") %>" );
				if( element && element.tabPage )
					element.tabPage.select();
			<% } %>

			<% if( request.getParameter("focus") != null ) { %>
				var object = frmMain.<%= request.getParameter("focus") %>;
				if( object ) {
					if( object.select ) object.select();
					if( object.focus ) object.focus();
				}
			<% } %>
			fitHeightToWindow();
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

		function loadingReq( srcUrl, winname ) {
			<% if( htmlpage.isOldBrowserIE() ) { %>
				var url = replaceQueryValue( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>loading.html", "url", encodeURIComponent(srcUrl) );
			<% } else { %>
				$("body").addClass("loading");
				var url = srcUrl;
			<% } %>

			if( winname )
				window.open( url, winname );
			else
				location.href = url;
		}

		function setEventObject( event ) {
			eventObj = event;
		}

		function submitInput( mode ) {

			if( window.name != "sub-content" && parent.$("body.frame-content").length ) {
				toggleLoadingParent( true );
			} else {
				toggleLoading( true );
			}

			if( mode && (mode == "down" || mode == "mdown" || mode == "tdown") ) {
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
			if( self.opener && !self.opener.closed ) {
				if( reload ) {
					if( window.name == "sub-content" ) {
						toggleLoadingParent( true );
					}
					self.opener.location.reload();
				}
				self.opener.focus();
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
			var left = ( screen.availWidth - document.body.clientWidth ) / 2;
			var top = ( screen.availHeight - document.body.clientHeight ) / 2;
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
				parent.$(".sub-content-wrap").hide();
				if( mode && (mode == "down" || mode == "mdown" || mode == "tdown" || mode == "hdrdwn") ) {
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
				location.href = url;
			else if( isPostAvailable )
				submitPost( url );
		}

		function windowResizeTo( width, height ) {
			<% if( htmlpage.getWindowType() != null && "sub".equals(htmlpage.getWindowType().split("@")[0]) ) { %>
				var resizeWidth, resizeHeight;
				if( width ) {
					resizeWidth = ( width ? width : document.body.offsetWidth );
					if( screen.availWidth - window.screenLeft < resizeWidth )
						window.moveTo( ( screen.availWidth - resizeWidth ) / 2, window.screenTop );
				}

				// 두번 실행해야 정상적으로 작동함!
				resizeHeight = ( height ? height : document.body.offsetHeight + 95 );
				window.resizeTo( resizeWidth, resizeHeight );

				resizeHeight = ( height ? height : document.body.offsetHeight + 95 );
				if( screen.availHeight - window.screenTop < resizeHeight ) {
					var screenTop = ( screen.availHeight - resizeHeight ) / 2;
					window.moveTo( window.screenLeft, screenTop < 0 ? 0 : screenTop );
				}

				if( resizeHeight > screen.availHeight ) resizeHeight = screen.availHeight * 0.85;
				window.resizeTo( resizeWidth, resizeHeight );
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

	<style type='text/css'>
		.modal {
			display:	none;
			position:	fixed;
			z-index:	1000;
			top:		0;
			left:		0;
			height:		100%;
			width:		100%;
			background:	rgba( 255, 255, 255, .8 )
						url('images/loading.gif')
						50% 50%
						no-repeat;
		}

		body.loading {
			overflow: hidden;
		}

		body.loading .modal {
			display: block;
		}
	</style>
