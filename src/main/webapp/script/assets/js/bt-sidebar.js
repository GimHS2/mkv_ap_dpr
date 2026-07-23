/*
	File Name:	bt-sidebar.js
	Version:	2.2.0

	Description:
		https://bootstrapious.com/p/bootstrap-sidebar#3-fixed-scrollable-sidebar-menu-with-a-content-overlay

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/03/30		2.2.0	create
*/

var sidebarOpen = function( partialMenu ) {
	var $ulnav = $("#sidebar ul.nav");
	if( partialMenu ) {
		var imgs = [].concat(partialMenu[0]|| []);
		// apply d-none first
		$ulnav.find("> li").addClass("partial-menu-hidden").addClass("d-none");
		$.each(imgs, function(i, img1) {
			var menuName = img1.name;
			var submenuLink = $("#sidebar").find("a[name='"+menuName+"']");
			console.log("sidebar pring menuName:"+ menuName, imgs);
			// show only partial menu
			submenuLink.parent("li").closest("li.partial-menu-hidden").removeClass("partial-menu-hidden").removeClass("d-none");
			$ulnav.attr("data-partial-menu", ($ulnav.attr("data-partial-menu")||"")+" "+menuName);
			submenuLink.parent("li").find("ul.collapse:not(.show)").addClass("show");
		});
	} else {
		if( $ulnav.attr("data-partial-menu") ) {//checking and processng if exists.
			var partialMenuNames = $ulnav.attr("data-patial-menu");
			if( partialMenuNames ) {
				$("#sidebar").find("ul[data-partial-menu] > li.partial-menu-hidden.d-none").removeClass("partial-menu-hidden").removeClass("d-none");
				$("#sidebar").find("ul[data-partial-menu]").attr("data-partial-menu", "");
			}
		}
	}

	//open sidebar
	$("#sidebar").addClass("active");
	// fade in the overlay
	$(".overlay").addClass("active");
	// close dropdowns
	$("#sidebar .collapse.in").toggleClass("in");

	if( $.noty ) {
		$.noty.closeAll();
	}
};

var sidebarClose = function() {
	// hide sidebar
	$("#sidebar").removeClass("active");
	// hide overlay
	$(".overlay").removeClass("active");
	//remove partialMenu
	if( $("#sidebar").find("ul[data-partial-menu]").length > 0 ) {
		var partialMenuName = $("#sidebar").find("ul[data-partial-menu]").val();
		if( partialMenuName ) {
			$("#sidebar").find("ul[data-partial-menu] > li.partial-menu-hidden.d-none").removeClass("d-none").removeClass("partial-menu-hidden");
			$("#sidebar").parent("ul").data("partialMenu", "");
		}
	}
};

var sidebarSetupDismiss = function() {
	if( $("#sidebar #dismiss").length > 0 ) {
	} else {
		var dismiss = $("<div id='dismiss'>").append(
			$("<i class='fa fa-arrow-right'></i>")
		);
		$("#sidebar").append(dismiss);
	}
};

var sidebarSetupHeader = function() {
	if( $("#sidebar .sidebar-header").length > 0 ) {
	} else {
		var header = $("<div class='sidebar-header'>");
		header.append( $("<h3 class='sidebar-heading'>&nbsp;</h3>") );
		$("#sidebar").append(header);
	}
};

var sidebarToggle = function( menuImgArgs ) {
	if( menuImgArgs ) {
		sidebarOpen( menuImgArgs );
	} else {
		if( $("#sidebar.active").length > 0 ) {
			sidebarClose();
		} else {
			sidebarOpen();
		}
	}
};

function createMenuEntry( data ) {
	var childs = data.childs;
	var submenuId = "sidebar_"+data.lvl+"_"+data.name;
	if( childs ) {// has childs
		var menuDoms = _.map(childs, function(child){
			if( child )
				return createMenuEntry(child);
			else
				return null;
		});
		var submenu = document.createElement("ul");
		submenu.setAttribute("data-menu-level", data.lvl);
		submenu.className += " list-unstyled collapse";
		if( data.classData.match(/\saria-expanded/) ) {
			submenu.className += " show";
		}
		submenu.id = submenuId;
		_.each(menuDoms, function(menu){
			if( menu )
				submenu.appendChild(menu);
		});
		var submenuEntry = document.createElement("li");
		submenuEntry.setAttribute("data-entry-level", data.lvl);
		var submenuLink = document.createElement("a");
		submenuLink.textContent = data.text;
		submenuLink.name = data.name;
		submenuLink.setAttribute("data-menu-id", submenuId);
		submenuLink.classList.toggle("submenu-link", true);
		submenuLink.setAttribute("data-href", data.href||"");

		if( data.href ) {// link to other page
			submenuLink.href = data.href;
			submenuLink.setAttribute("onclick", "JavaScript:return sidebarMenuClick(this);");
			submenuEntry.appendChild(submenuLink);
		} else {// intermediate menu
			submenuLink.setAttribute("onclick", "JavaScript:return sidebarMenuClick(this);");
			submenuLink.setAttribute("data-target", "#"+submenuId);
			submenuLink.setAttribute("data-toggle", "collapse");
			if( data.classData.match(/aria-expanded/) ) {
				submenuEntry.classList.toggle("active", true);
				submenuLink.setAttribute("aria-expanded", true);
			} else {
				submenuLink.setAttribute("aria-expanded", false);
			}
			submenuEntry.appendChild(submenuLink);
			submenuEntry.appendChild(submenu);
		}
		if( data.classData && data.classData.match(/-bt-mobile-none/) ) {
			return null;
		}
		return submenuEntry;
	}
	if( data.classData && data.classData.match(/-bt-mobile-none/) ) {
		return null;
	}
	var li = document.createElement("li");
	var leafmenuLink = document.createElement("a");
	leafmenuLink.href = data.href;
	leafmenuLink.name = data.name;
	leafmenuLink.setAttribute("data-menu-id", submenuId);
	leafmenuLink.textContent = data.text;
	leafmenuLink.setAttribute("onclick", "JavaScript:return sidebarMenuClick(this);");
	li.appendChild(leafmenuLink);
	return li;
};

var createMenuData = function( original_menu_el, savedMenuData ) {
	original_menu_el = original_menu_el || ( window.getTopMenuWindow ? getTopMenuWindow().document.querySelector("#menu") : null);
	var savedMenuActives = (savedMenuData ? savedMenuData.actives : undefined);
	var menuEntries = _.map($("img[level=1]", original_menu_el), function(img1){
		var lv1 = $(img1).parent().closest("li");
		var childs = _.map($("img[level=2]", lv1), function(img2){
			var link = $(img2).parent().closest("a")[0];
			return {lvl: img2.getAttribute("level"), el: img2, name: img2.getAttribute("name"), text: img2.getAttribute("alt"),
					classData: img2.getAttribute("data-css-class") || "",
					href: (link? link.getAttribute("href") : undefined), childs: undefined};
		});
		var menuId ="sub_"+1+"_"+img1.getAttribute("name");
		var cssClasses = (img1.getAttribute("data-css-class") || "") + (_.includes(savedMenuActives, menuId) ? " aria-expanded" : "");
		var link = $(img1).parent().closest("a")[0];
		return {lvl: img1.getAttribute("level"), el: img1, name: img1.getAttribute("name"), text: img1.getAttribute("alt"),
				classData: cssClasses,
				href: (link? link.getAttribute("href") : undefined), childs: childs};
	});
	return {actives: savedMenuActives, menuEntries: menuEntries};
};

var sidebarGetMenuData = function(original_menu_el) {
	return createMenuData( original_menu_el, store.get("menuData") );
};

var sidebarPromiseMenuElement = function( menuUrl ) {
	return new Promise(function(resolve, reject) {
		var topmenu = getTopMenuWindow();
		if( topmenu  ) {
			// console.log("[TRACE] try resolved topmenu from topdoc");
			var menuObj = topmenu.document.getElementById("menu");
			var checkExists = setInterval(function(){
				if( (menuObj ? menuObj : menuObj = topmenu.document.getElementById("menu")) ) {
					resolve( menuObj );
					clearInterval(checkExists);
				}
			});
		} else {
			var menu_el_in_menupage = document.querySelector(".menu_top #menu");
			if( menu_el_in_menupage ) {
				resolve(menu_el_in_menupage);
			} else {
				if( !menuUrl ) {
					console.log("[WARN] topmenu not found. Please supply menuUrl");
					return;
				}
				// console.log("[TRACE] try resolved topmenu from this document");
				var myiFrame = document.getElementById("myiFrame");
				if( myiFrame == null ) {
					myiFrame = document.createElement("iframe");
					myiFrame.setAttribute("id", 'myiFrame');
					myiFrame.setAttribute("seamless", '');
					myiFrame.style.display = "none";
					myiFrame.setAttribute("src", menuUrl);
					document.body.appendChild(myiFrame);
					myiFrame.addEventListener("load", function() {
						var el_in_iframe = myiFrame.contentWindow.document.getElementById("menu");
						return resolve(el_in_iframe);
					});
				} else {
					resolve(myiFrame.contentWindow.document.getElementById("menu"));
				}
			}
		}
	});
};

var sidebarMenuClick = function( evt ) {
	var link = evt.target ? evt.target : evt;
	if( link ) {
		var subMenuName = link.name;
		var topmenu = getTopMenuWindow();
		var $ulnav = $(link).parent().closest("ul.nav");
		var $li = $(link).parent("li");
		var sideMenuData = store.get("menuData");
		if( link.href ) {// link is leaf
			$li.addClass("active");
			if( sideMenuData.actives ) {
				_.each($ulnav.find("li.active > a"), function(activeLink) {
					if( activeLink.href && !(activeLink.getAttribute("data-menu-id") == link.getAttribute("data-menu-id")) ) {
						$(activeLink).parent().closest("li").removeClass("active");
					}
				});
			}
		} else {// link is intermediate
			$li.toggleClass("active");
		}
		if( topmenu ) {
			var imageObj = topmenu.document.querySelector("#menu img[name='"+subMenuName+"']");
			if( $ulnav.attr("data-partial-menu") ) {
				console.log("sidebar partialmenu clicked link:", link);
				topmenu.Menu.click(imageObj, subMenuName);
			} else {
				console.log("sidebar fullmenu clicked link:", link);
			}
			var navLinks= topmenu.document.querySelectorAll("#topMenuList .nav-link[name]");
			_.each(navLinks, function(navLink){
				if( navLink ) {
					if( subMenuName == navLink.getAttribute("name") ) {
						if( navLink.closest(".nav-item") ) {
							navLink.closest(".nav-item").classList.toggle("active", true);
						}
					} else {
						if( navLink.closest(".nav-item") ) {
							navLink.closest(".nav-item").classList.toggle("active", false);
						}
					}
				}
			});
		} else {
			console.log("sidebar clicked link:", link);
		}
		var acts = $ulnav.find("li.active > a").map(function(i,o){return o.getAttribute("data-menu-id");}).get();
		console.log("actives saving:", acts);
		sideMenuData.actives = acts;
		store.set("menuData", sideMenuData);
	}
	return true;
};

var sidebarSetupMenu = function( original_menu_el, to_el ) {
	var menuData = sidebarGetMenuData(original_menu_el);

	store.set("menuData", menuData);

	var menuWrapper = sidebarCreateMenuElement( menuData );

	if( to_el ) {
		menuWrapper.id = to_el.id;
		return $(to_el).replaceWith(menuWrapper)[0];
	} else {
		return $("#sidebar #sidebarMenu #sidebarMenuList").append(menuWrapper).find("ul")[0];
	}
	$("#sidebar ul.collapse").on("show.bs.collapse", function(){
		$(this).closest("[data-toggle=collapse]").addClass("hover");
	});
};

var getSiteMenuObjects = function() {
	var topmenu = getTopMenuWindow();

	var ret = [];
	if( topmenu ) {
		var siteLocaleObj = topmenu.Site.createSiteLocaleStruct();
		var savedOrgObj = topmenu.document.querySelector("[name=savedOrganizationCode]");
		var node;
		if( siteLocaleObj ) {
			node = siteLocaleObj.cloneNode(true) ;
			node.className += " -btx custom-select";
			ret.push( node );
		}
		if( savedOrgObj ) {
			node = savedOrgObj.cloneNode(true);
			node.classList.toggle("w-100", true);
			ret.push( node );
		}
	}
	return ret;
};

var sidebarCreateMenuElement = function( menuData ) {
	var menuDoms = _.map(menuData.menuEntries, function(data) {
		return createMenuEntry(data);
	});
	var menuWrapper = document.createElement("ul");
	menuWrapper.className = "list-unstyled components";
	menuWrapper.className += " nav navbar-nav";
	// menuWrapper.className += " nav navbar-nav navbar-right";
	var menuHeading = document.querySelector("#sidebar [name=menuHeading]");
	if( !menuHeading ) {
		menuHeading = document.createElement("div");
		menuHeading.name = "menuHeading";
		menuHeading.textContent = "&nbsp;";
		menuWrapper.appendChild( menuHeading );
	}
	if( menuHeading ) {
		var $siteObj = $("<span class='site-obj'>");
		var topObjs = getSiteMenuObjects();
		if( topObjs && topObjs.length > 0 ) {
			$siteObj.append( topObjs );
		}
		$(menuHeading).append( $siteObj );
	}

	_.each(menuDoms, function(menuLi_1){
		if( menuLi_1 )
			menuWrapper.appendChild( menuLi_1 );
	});
	// console.log("[TRACE] menu actives:"+ window.name, menuData.actives);
	_.each(menuData.actives, function(activeLinkMenuId) {
		var menuLink = menuWrapper.querySelector("[data-menu-id='"+activeLinkMenuId+"']");
		if( menuLink ) {
			var $li = $(menuLink).parent().closest("li").addClass("active");
			if( menuLink.getAttribute("data-toggle") == "collapse" ) {
				menuLink.setAttribute("aria-expanded", true);
				var tgtCollapse = menuWrapper.querySelector(menuLink.getAttribute("data-target"));
				if( tgtCollapse )
					tgtCollapse.classList.toggle("show", true);
			}
		}
	});
	return menuWrapper;
};

// if( false ) {
// 	sidebarPromiseMenuElement().then(function(menuObj){
// 		return sidebarSetupMenu(menuObj, document.querySelector("#topMenuList"));
// 	}).then(function(newMenuObj){
// 		var lis = newMenuObj.querySelectorAll("li.active");
// 		$(newMenuObj).find("a[aria-expanded=true]").attr("aria-expanded", false);
// 		console.log("2", newMenuObj);
// 	}).catch(function(err){
// 		console.log("error", err);
// 	});
// }

$(document).ready(function(){
	sidebarSetupDismiss();
	sidebarSetupHeader();

	$("#dismiss, .overlay").on("click", function(){
		sidebarClose();
	});
	$("#sidebarCollapse").on("click", function(){
		sidebarOpen();
	});
});
