<%--
	File Name:	include_cst_bootstrap_header.inc
	Version:	2.2.0c

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/03/30		2.2.0c	create
--%>

<!-- START bootstrap header -->
<meta name="viewport" content="width=device-width, initial-scale=1">

<script type="text/javascript" id="polyfill">
	!window.addEventListener&&function(e,t,n,r,i,s,o){e[r]=t[r]=n[r]=function(e,t){var n=this;o.unshift([n,e,t,function(e){e.currentTarget=n,e.preventDefault=function(){e.returnValue=!1},e.stopPropagation=function(){e.cancelBubble=!0},e.target=e.srcElement||n,t.call(n,e)}]),this.attachEvent("on"+e,o[0][3])},e[i]=t[i]=n[i]=function(e,t){for(var n=0,r;r=o[n];++n)if(r[0]==this&&r[1]==e&&r[2]==t)return this.detachEvent("on"+e,o.splice(n,1)[0][3])},e[s]=t[s]=n[s]=function(e){return this.fireEvent("on"+e.type,e)}}(Window.prototype,HTMLDocument.prototype,Element.prototype,"addEventListener","removeEventListener","dispatchEvent",[]);
	if (window.Element && !Element.prototype.closest) {
		Element.prototype.closest =
			function(s) {
				var matches = (this.document || this.ownerDocument).querySelectorAll(s), i, el = this;
				do {
					i = matches.length;
					while (--i >= 0 && matches.item(i) !== el) {};
				} while ((i < 0) && (el = el.parentElement));
				return el;
			};
	};
	//
	window.requestAnimationFrame = window.requestAnimationFrame
    || window.mozRequestAnimationFrame
    || window.webkitRequestAnimationFrame
    || window.msRequestAnimationFrame
    || function(f){return setTimeout(f, 1000/60)} // simulate calling code 60
	window.cancelAnimationFrame = window.cancelAnimationFrame
    || window.mozCancelAnimationFrame
    || function(requestID){clearTimeout(requestID)} //fall back
	//
	if (!String.prototype.trim) {
		String.prototype.trim = function () {
			return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
		};
	}
</script>
<% String __rand = ( (true && "dv".equals(systemConfig.getProperty("appEnvId"))) ? "?rand="+String.valueOf(new java.util.Date().getTime()) : "" ); %>

<%@ include file="include_cst_vconsole.inc" %>

<script type="text/javascript" src="script/jquery_min.js"></script>
<script type="text/javascript" src="script/assets/js/underscore.min.js"></script>
<script type="text/javascript">
	_.findDeep = function(obj, dotSeparatedKeys) {
		if (dotSeparatedKeys !== undefined && typeof dotSeparatedKeys !== 'string') return undefined;
		if (typeof obj !== 'undefined' && typeof dotSeparatedKeys === 'string') {
			// split on ".", "[", "]", "'", """ and filter out empty elements
			var splitRegex = /[.\[\]'"]/g; // eslint-disable-line no-useless-escape
			var pathArr = dotSeparatedKeys.split(splitRegex).filter(function(k) { return k !== ''; });
			// eslint-disable-next-line no-param-reassign, no-confusing-arrow
			obj = pathArr.reduce(function(o, key){ return (o && o[key] !== 'undefined' ? o[key] : undefined); }
							   , obj);
		}
		return obj;
	};
</script>

<!--style type="text/css" id="bts.navbar-icon-top">
	.navbar-icon-top .navbar-nav .nav-link > .fa {
		position: relative;
		width: 36px;
		font-size: 24px;
	}

	.navbar-icon-top .navbar-nav .nav-link > .fa > .badge {
		font-size: 0.75rem;
		position: absolute;
		right: 0;
		font-family: sans-serif;
	}

	.navbar-icon-top .navbar-nav .nav-link > .fa {
		top: 3px;
		line-height: 12px;
	}

	.navbar-icon-top .navbar-nav .nav-link > .fa > .badge {
		top: -10px;
	}

	@media (min-width: 576px) {
		.navbar-icon-top.navbar-expand-sm .navbar-nav .nav-link {
			text-align: center;
			display: table-cell;
			height: 70px;
			vertical-align: middle;
			padding-top: 0;
			padding-bottom: 0;
		}

		.navbar-icon-top.navbar-expand-sm .navbar-nav .nav-link > .fa {
			display: block;
			width: 48px;
			margin: 2px auto 4px auto;
			top: 0;
			line-height: 24px;
		}

		.navbar-icon-top.navbar-expand-sm .navbar-nav .nav-link > .fa > .badge {
			top: -7px;
		}
	}

	@media (min-width: 768px) {
		.navbar-icon-top.navbar-expand-md .navbar-nav .nav-link {
			text-align: center;
			display: table-cell;
			height: 70px;
			vertical-align: middle;
			padding-top: 0;
			padding-bottom: 0;
		}

		.navbar-icon-top.navbar-expand-md .navbar-nav .nav-link > .fa {
			display: block;
			width: 48px;
			margin: 2px auto 4px auto;
			top: 0;
			line-height: 24px;
		}

		.navbar-icon-top.navbar-expand-md .navbar-nav .nav-link > .fa > .badge {
			top: -7px;
		}
	}

	@media (min-width: 992px) {
		.navbar-icon-top.navbar-expand-lg .navbar-nav .nav-link {
			text-align: center;
			display: table-cell;
			height: 70px;
			vertical-align: middle;
			padding-top: 0;
			padding-bottom: 0;
		}

		.navbar-icon-top.navbar-expand-lg .navbar-nav .nav-link > .fa {
			display: block;
			width: 48px;
			margin: 2px auto 4px auto;
			top: 0;
			line-height: 24px;
		}

		.navbar-icon-top.navbar-expand-lg .navbar-nav .nav-link > .fa > .badge {
			top: -7px;
		}
	}

	@media (min-width: 1200px) {
		.navbar-icon-top.navbar-expand-xl .navbar-nav .nav-link {
			text-align: center;
			display: table-cell;
			height: 70px;
			vertical-align: middle;
			padding-top: 0;
			padding-bottom: 0;
		}

		.navbar-icon-top.navbar-expand-xl .navbar-nav .nav-link > .fa {
			display: block;
			width: 48px;
			margin: 2px auto 4px auto;
			top: 0;
			line-height: 24px;
		}

		.navbar-icon-top.navbar-expand-xl .navbar-nav .nav-link > .fa > .badge {
			top: -7px;
		}
	}
</style-->

<style type="text/css" id="bt.iphone-media-queries">
    /* iphone 3 */
    @media only screen and (min-device-width: 320px) and (max-device-height: 480px) and (-webkit-device-pixel-ratio: 1) { }

    /* iphone 4 */
    @media only screen and (min-device-width: 320px) and (max-device-height: 480px) and (-webkit-device-pixel-ratio: 2) { }

    /* iphone 5 */
    @media only screen and (min-device-width: 320px) and (max-device-height: 568px) and (-webkit-device-pixel-ratio: 2) { }

    /* iphone 6, 6s, 7, 8 */
    @media only screen and (min-device-width: 375px) and (max-device-height: 667px) and (-webkit-device-pixel-ratio: 2) { }

    /* iphone 6+, 6s+, 7+, 8+ */
    @media only screen and (min-device-width: 414px) and (max-device-height: 736px) and (-webkit-device-pixel-ratio: 3) { }

    /* iphone X */
    @media only screen and (min-device-width: 375px) and (max-device-height: 812px) and (-webkit-device-pixel-ratio: 3) { }

    /* iphone XR */
    @media only screen and (min-device-width : 414px) and (max-device-height : 896px) and (-webkit-device-pixel-ratio : 2) { }

   /* iphone XS */
    @media only screen and (min-device-width : 375px) and (max-device-height : 812px) and (-webkit-device-pixel-ratio : 3) { }

   /* iphone XS Max */
    @media only screen and (min-device-width : 414px) and (max-device-height : 896px) and (-webkit-device-pixel-ratio : 3) { }
</style>

<link rel="stylesheet" type="text/css" href="script/jqdt/main/bootstrap.dataTables.min.css"/>
<script type="text/javascript" src="script/jqdt/main/bootstrap.dataTables.min.js"></script>



<!--[if gte IE 9]><!-->
<link rel="stylesheet" href="script/assets/css/bootstrap.min.css">
<!--<![endif]-->

<!--[if IE 9]>
	<link href="script/assets/css/bootstrap-ie9.min.css" rel="stylesheet">
<![endif]-->
<!--[if lte IE 8]>
	<link href="script/assets/css/bootstrap-ie8.min.css" rel="stylesheet">
	<script src="script/assets/js/html5shiv.min.js"></script>
<![endif]-->

<!-- bootstrap4 supports IE10-11/IE Edge & modern browsers -->
<!--[if gte IE 9]><!-->
<script type="text/javascript" src="script/assets/js/popper.min.js"></script>
<script type="text/javascript" src="script/assets/js/bootstrap.min.js"></script>
<!--<![endif]-->
<!--[if IE 9]>
	<script src="script/assets/js/es5-shim.min.js"></script>
	<script type="text/javascript" src="script/assets/js/popper.min.js"></script>
	<script type="text/javascript" src="script/assets/js/bootstrap-ie9.min.js"></script>
<![endif]-->
<!--[if lte IE 8]>
	<script type="text/javascript" src="script/assets/js/es5-shim.min.js"></script>
	<script type="text/javascript" src="script/assets/js/bootstrap-ie8.min.js"></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.js"></script>
<![endif]-->

<!-- Promise polyfill for IE11 or other older browsers -->
<script type="text/javascript" src="script/assets/js/es6-promise.min.js"></script>
<script type="text/javascript" src="script/assets/js/es6-promise.auto.min.js"></script>

<script type="text/javascript" src="script/assets/js/fetch.js"></script>

<% if( htmlpage.getUserAgentString().indexOf("MSIE 8") < 0 ) { %>
<script type='text/javascript' src='script/assets/js/observers.js<%=__rand%>'></script>
<% } %>

<style type="text/css" id="bootstrap.custom">
</style>


<style type="text/css" id="bootstrap.media">
// Extra small devices (portrait phones, less than 576px)
// No media query for `xs` since this is the default in Bootstrap
	pre {
		border: 0;
		background-color: transparent;
	}
	pre.bootstrap {
		border: 1px solid #ccc;
		background-color: #f5f5f5;
	}
	table caption {
		caption-side: top;
	}
	.hidden {
		display:none;
	}
	#nav-more.hidden a.dropdown-toggle {
		display:none;
	}


// Extra Extra Small devices(portrait phones)
@media only screen and (max-width: 320px) {
	body {
		margin-left: 0px;
		margin-right: 0px;
	}
	nav#navbar-top .navbar-content {
		flex-direction: column!important;
	}
}

// Small devices (landscape phones, 576px and up)
@media (min-width: 576px) {  }

// Medium devices (tablets, 768px and up)
@media (min-width: 768px) {
	div.list_content table td.description {
		min-width: 100px;
		max-width: 200px;
	}
 }

// Large devices (desktops, 992px and up)
@media (min-width: 992px) {  }

// Extra large devices (large desktops, 1200px and up)
@media (min-width: 1200px) {  }

</style>

<script type="text/javascript" id="bootstrap.custom.android-stock-browser">
	// https://getbootstrap.com/docs/4.0/getting-started/browsers-devices/#android-stock-browser
	$(function () {
		var nua = navigator.userAgent
		var isAndroid = (nua.indexOf('Mozilla/5.0') > -1 && nua.indexOf('Android ') > -1 && nua.indexOf('AppleWebKit') > -1 && nua.indexOf('Chrome') === -1)
		if (isAndroid) {
			$('select.form-control').removeClass('form-control').css('width', '100%')
		}
	})
</script>

<script type="text/javascript">
	var _u = function(urlStr){
		var url;
		var chain = {
			replace: function( key, value ) {
				var newurl = replaceQueryValue( url, key, value);
				url = newurl;
				return this;
			},
			/** url string */
			toString: function() {
				return url;
			},
			/** multikey param data as js object */
			toQueryParams : function() {
				return (url
					  ? (url.split("?")[1] || url.split("?")[0])
					  : "")
					.split('&')
					.reduce(function(result, pair){
						pair = pair.split('=');
						var name = decodeURI(pair[0]);
						var value = decodeURI(pair[1]);
						if( name.length ) {
							if( result[name] !== undefined ) {
								if( !result[name].push ) {
									result[name] = [result[name]];
								}
								result[name].push(value || '');
							} else {
								result[name] = value || '';
							}
						}
						return (result);
					}, {});
			},
		};
		var setUrl = function( urlStr ) {
			if( url ) return url;
			return urlStr;
		};
		url = setUrl(urlStr);
		return chain;
	};

	function findDirectChild(/*child node*/c, /*parent node*/p){ //returns the closest element
		  while(c.parentNode!==p && (c=c.parentNode));
		  return c;
	}
	function btsViewSize( setToHtml ) {
		var getBtsViewSize = function() {
			var width = Math.max(
				document.documentElement.clientWidth,
				window.innerWidth || 0
			);
			if (width < 576) return 'xs';
			if (width < 768) return 'sm';
			if (width < 992) return 'md';
			if (width < 1200) return 'lg';
			return 'xl';
		};
		var btsWidth = getBtsViewSize();
		if( setToHtml == true ) {
			document.querySelector("html").setAttribute("data-bts-width", btsWidth);
			if( document.querySelector("#bts-width") ) {
				document.querySelector("#bts-width").textContent = btsWidth;
			}
		}
		return btsWidth;
	}
</script>


<script type="text/javascript" src="script/assets/js/store.modern.min.js"></script>
<script type="text/javascript" src="script/assets/js/store.plugin.expire.js"></script>
<script type="text/javascript" src="script/assets/js/cols.js<%=__rand%>"></script>
<!-- <script type="text/javascript" src="script/assets/js/cols.js"></script> -->
<script type="text/javascript" src="script/assets/js/btx.js<%=__rand%>"></script>
<script type="text/javascript" src="script/assets/js/bt-accd.js<%=__rand%>"></script>
<% if( false && "dv".equals(systemConfig.getProperty("appEnvId")) ) { %>
<script type="text/javascript" src="tail/dev.js<%=__rand%>"></script>
<% } %>

<link rel='stylesheet' href='script/assets/css/bt-sidebar.css' />
<script src="script/assets/js/bt-sidebar.js<%=__rand%>"></script>

<!-- Scrollbar Custom CSS -->
<!-- <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/malihu-custom-scrollbar-plugin/3.1.5/jquery.mCustomScrollbar.min.css"> -->
<!-- jQuery Custom Scroller CDN -->
<!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/malihu-custom-scrollbar-plugin/3.1.5/jquery.mCustomScrollbar.concat.min.js"></script> -->

<script type="text/javascript">
	function tabpaneTabSelectedObserved() {
		function workOnClassAdd( mutation ) {
			var tabSelected = mutation.target;
			if( tabSelected ) {
				if( window.ColList && ColList.colLists && ColList.colLists.length > 0 ) {
					var list_contents = tabSelected.closest(".content_group.tabpane").querySelectorAll("div.list_content");
					_.each(list_contents, function(list_content) {
						if( list_content.colList ) list_content.colList.fitRemoveWidth().fitWidth();
					});
				}
			}
		}
		function workOnClassRemove( mutation ) {
			// console.log("class removed");
		}
		window.classWatchers = window.classWatchers||[];
		_.each(document.querySelectorAll(".content_group .tabpane_tabrow span.tab"), function(tab, tab_i){
			var classWatcher = new ClassWatcher(tab, 'selected', workOnClassAdd, workOnClassRemove);
			window.classWatchers.push(classWatcher);
		});
	}
	var Site = {
		getSiteLocaleData: function() {
			return {
				supportLocale: "<%=htmlpage.getProperty().getProperty("partySupportLocale")%>",
				supportLocaleLabel: "<%=htmlpage.getProperty().getProperty("partySupportLocaleLabel")%>",
				siteLocale: "<%=htmlpage.getLocale()%>"// usually htmlpage.getLocale() is first locale user selected.
			};
		},
		createSiteLocaleStruct: function() {
			var opts = this.getSiteLocaleData();
			var sel = document.querySelector("select.site-locale-sel");
			if( sel == undefined ) {
				sel = document.createElement("select");
				sel.name = "site-locale";
				sel.classList = "site-locale-sel";
			}
			if( sel.querySelectorAll("option").length == 0 ) {
				var sl = opts.supportLocale.split(",");
				var sll = opts.supportLocaleLabel.split(",");
				for( var i = 0; i < sl.length; i++ ) {
					var selopt = document.createElement("option");
					selopt.value = sl[i];
					selopt.text = sll[i];
					sel.appendChild(selopt);
				}
				if( opts.siteLocale ) {
					var selopt = sel.querySelector("option[value='" + opts.siteLocale + "']");
					if( selopt != undefined ) {
						selopt.setAttribute("selected", true);
					}
				} else {
					alert('siteLocale null?');
				}
				sel.setAttribute("onchange", "JavaScript:Site.changeSiteLocale(this);");
			}
			return sel;
		},
		getSidebarWindow: function( startFr ) {
			startFr = startFr || window.top;
			var checker = function(fr) {
				return fr && fr.sidebarToggle && fr.name !== "topmenu";
			};
			var getSides = function( frames, checker ) {
				return _.map(frames, function(frame) {
					var chk = checker( frame );
					if( chk ) return {frame: frame};
					else return getSides(frame.frames, checker);
				});
			}
			var side = checker( startFr );
			if( side ) {
				return startFr;
			} else {
				var sides = getSides( startFr.frames, checker );
				if( sides && sides.length > 1 ) {
					console.log("[WARN] "+ "multiple sidebar window gathered.", sides);
				}
				return _.first(_.reject(_.pluck(sides,"frame"), _.isUndefined));
			}
		},
		getMainWindow: function() {
			if( parent && parent.main )
				return parent.main;
			if( parent && parent.parent && parent.parent.main )
				return parent.parent.main;
			if( parent && parent.parent && parent.parent.parent && parent.parent.parent.main )
				return parent.parent.parent.main;
			if( parent && parent.parent && parent.parent.parent && parent.parent.parent.parent && parent.parent.parent.parent.main )
				return parent.parent.parent.parent.main;
			else
				return undefined;
		},
		getTopMenuWindow: function() {
			if( parent && parent.topmenu )
				return parent.topmenu;
			if( parent && parent.parent && parent.parent.topmenu )
				return parent.parent.topmenu;
			if( parent && parent.parent && parent.parent.parent && parent.parent.parent.topmenu )
				return parent.parent.parent.topmenu;
			if( parent && parent.parent && parent.parent.parent && parent.parent.parent.parent && parent.parent.parent.parent.topmenu )
				return parent.parent.parent.parent.topmenu;
			else
				return undefined;
		},
		changeSiteLocale: function( evt ) {
			var tgt = ( evt.target ? evt.target : evt );
			var siteLocale;
			if( tgt instanceof HTMLElement ) {
				var idx = tgt.selectedIndex;
				var opt = tgt.options[idx];
				siteLocale = opt.value;
			} else {
				siteLocale = tgt;
			}
			if( siteLocale ) {
				var topmenu = this.getTopMenuWindow();
				if( topmenu ) {
					// reload with changeLang parameter to reload for the language
					var menuurl = replaceQueryValue(topmenu.getLocationURL(), "locale", siteLocale);
					menuurl = replaceQueryValue(menuurl, "chgLang", "true");
					menuurl = replaceQueryValue(menuurl, "mode", "menu");
					var imageObj = topmenu.Menu.selectedMenu[ topmenu.Menu.selectedMenu.length -1 ];
					if( imageObj ) {
						menuurl = replaceQueryValue(menuurl, "menuFocus", imageObj.name);
					}
					topmenu.location.href = menuurl;
					/* topmenu.windowSelfOpen( topurl ); */
				}
				var main = this.getMainWindow();
				if( main ) {
					// reload with changeLang parameter to reload for the language
					var mainurl = replaceQueryValue(main.getLocationURL(), "locale", siteLocale);
					main.location.href = mainurl;
				}
				//
				var top = window.top;
				if( topmenu && main ) {//framed
					if( window.history.pushState ) {//support history api( probably IE 10+ )
					} else {//old browser
					}
					var url = replaceQueryValue(top.location.href, "locale", siteLocale);
					top.location.href = url;
				} else {// single page
				}
			}
		},
		/* change site config by parameter */
		changeSiteMobile: function( evt ) {
			var tgt = ( evt.target ? evt.target : evt );
			var siteMobile;
			if( tgt instanceof HTMLElement ) {
				var idx = tgt.selectedIndex;
				var opt = tgt.options[idx];
				siteMobile = opt.value;
			} else {
				siteMobile = tgt;
			}
			if( siteMobile ) {
				var topmenu = this.getTopMenuWindow();
				if( topmenu ) {
					// reload with changeLang parameter to reload for the language
					var menuurl = replaceQueryValue(topmenu.getLocationURL(), "mobile", siteMobile);
					menuurl = replaceQueryValue(menuurl, "mode", "menu");
					var imageObj = topmenu.Menu.selectedMenu[ topmenu.Menu.selectedMenu.length -1 ];
					if( imageObj ) {
						menuurl = replaceQueryValue(menuurl, "menuFocus", imageObj.name);
					}
					topmenu.location.href = menuurl;
					/* topmenu.windowSelfOpen( topurl ); */
				}
				var main = this.getMainWindow();
				if( main ) {
					// reload with changeLang parameter to reload for the language
					var mainurl = replaceQueryValue(main.getLocationURL(), "mobile", siteMobile);
					main.location.href = mainurl;
				}
			}
		},
		logoutReq: function() {
			if( confirm("<mtl:message key="MSG_CONFIRM_LOGOUT" encodeScript="true"/>") ) {
				var url = "<%= systemConfig.getClassURL() %>/Login?mode=logout";
				url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
				window.open( url, "_top" );
			}
		},
	}

	function showPageMsgModal() {
		var msgObj = document.querySelector("pre#msg");
		var willShowModal = (msgObj && msgObj.textContent && !Util.isElementInScreenH( msgObj ));
		if( willShowModal ) {
			var modalBody = document.querySelector("#modal-page-msg .modal.modal-msg .modal-body");
			if( modalBody ) {
				modalBody.appendChild( msgObj.cloneNode(true) );
				$("#modal-page-msg .modal.modal-msg").modal('show');
			} else {
				var modalHtml = Bts.getModalTemplate({modal: {classData: "modal-msg"}});
				$("#modal-page-msg").html( modalHtml ).css("display","none");
				modalBody = document.querySelector("#modal-page-msg .modal.modal-msg .modal-body");
				modalBody.appendChild( msgObj.cloneNode(true) );
				//
				var $modalObj = $("#modal-page-msg .modal.modal-msg");
				$modalObj.on('hidden.bs.modal', function(evt){
					var tgt = evt.target;
					var pgMsgObj = tgt.closest("#modal-page-msg")
					if( pgMsgObj ) pgMsgObj.style.display = "none";
				}).on('show.bs.modal', function(evt){
					var tgt = evt.target;
					var pgMsgObj = tgt.closest("#modal-page-msg")
					if( pgMsgObj ) pgMsgObj.style.display = "";
					clearTimeout(tgt.getAttribute('data-hide-interval'));
					tgt.setAttribute('data-hide-interval', setTimeout(function(){
						$(tgt).modal('hide');
					}, 3000))
				}).modal('show');
			}
		}
	}
</script>

<script type="text/javascript" id="bootstrap.document.ready">
	if( !window.setOrganization ) {
		var topmenu = Site.getTopMenuWindow();
		if( topmenu ) {
			window.setOrganization = function( organizationCode ) {
				topmenu.setOrganization( organizationCode );
				var main = Site.getMainWindow();
				if( main ) {
					// reload with changeLang parameter to reload for the language
					var mainurl = replaceQueryValue(main.location.href, "organizationCode", organizationCode);
					main.location.href = mainurl;
				}
			};
		}
	}

	function changeViewPortHeader() {
		//TODO: delete below viewport hack and change viewport value in static/index.html as default value
		var viewMeta = window.top.document.querySelector("head meta[name=viewport]");
		var mobileViewMetaContent = "width=device-width,initial-scale=1.0";
		if( viewMeta ) {
			if( viewMeta.getAttribute("content") != mobileViewMetaContent ) {
				viewMeta.setAttribute("content", mobileViewMetaContent);
			}
		} else {
			var m = window.top.document.createElement("meta");
			m.setAttribute("name", "viewport");
			m.setAttribute("content", mobileViewMetaContent);
			window.top.document.querySelector("head").appendChild( m );
		}
	}

	function mobileReady() {
		changeViewPortHeader();
		//
		if( window.Bts ) {
			Bts.transformButtonPage();
			Bts.transformButtonContent();
		}
		//
		if( $("select[name=savedOrganizationCode]").length > 0 ) {
			$("#navbar-top [name=place_savedOrganizationCode]").prepend($("select[name=savedOrganizationCode]"));
		}
		if( $(window).width() < 320 ) {
			$("#navbar-top [name=place_savedOrganizationCode]").width("100px");
		}
		//
		// TODO: find better way. to make work a link in button
		$(document).find("button.btn a[href]").each(function(i,o){
			$(o).parent().on('click',function(evt){
				evt.preventDefault();
				var abtn = evt.target;
				location.href = $(o).attr("href");
			});
		});
	}


	<% if( htmlpage.isMobileProfile() ) { %>
	$(document).ready(function() {
		mobileReady();
	});

	window.addEventListener('resize', function(){
		if( ColList ) {
			_.each(ColList.colLists, function(colList){
				if( colList && !colList.dataTable ) colList.fitRemoveWidth().fitWidth();
				if( colList && colList.dataTable ) colList.fitRemoveWidth().fitWidth().fitDataTableRemoveWidth();
			});
		}
		console.log("[TRACE] window.resize:"+ " bts:"+btsViewSize()+ " w:"+window.innerWidth + " h:"+window.innerHeight );
	}, false);
		
	window.addEventListener('pageshow', function(){
		setupColList();
		if( (window.TabPane && window.TabPane.tabPanes && window.TabPane.tabPanes.length > 0) ) {
			tabpaneTabSelectedObserved();
		} else {
			if( window.Accd && document.querySelectorAll(".content_group").length > 1 )
				Accd.setupAccordion();
		}
		showPageMsgModal();
	}, false);
	<% } %>
</script><!-- bootstrap.document.ready -->
<!-- END bootstrap header -->
