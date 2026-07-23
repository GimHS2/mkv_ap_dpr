/*
	File Name:	menuhtml.js
	Version:	2.2.0c(dpr)

	Description:

	Note:
		debugVariable( variable, varName );
		getJson( data );
		isJson( item );
		fireCustomEvent( eventName, element, data );
		notyConfirm( msg );
		checkSameOrigin( url, strictCheck );
		isExternal( url );

		Alink()
			Alink.rewriteLinkStartsWith( parentSelector, aStartWith, bStartWith );
			Alink.getLinksNotStartsWith(parentSelector, aStartWith);
			Alink.getLinks(parentSelector);
		Ajaxy()
			Ajaxy.isSessionErr( html );
		Assert()
			Assert.getBoolean( strOrTfOrNum );
		String()
			String.prototype.format();
			String.prototype.formatUnicorn = String.prototype.formatUnicorn || function();
			String.prototype.toSnakeCase();
			String.prototype.toDashCase();
		jQuery.fn()
			jQuery.fn.datainfo( data );
			jQuery.fn.setdatainfo( data );
			jQuery.fn.outerHTML( s );
		Noty()
			Noty.info( msg, timeSec );
			Noty.debugConfirm( msg );
			Noty.debugInfo( msg, jsobj );
			Noty.confirm( msg );

		MenuHtml()
			MenuHtml.setMenuLinkActive( $li );
			MenuHtml.addIconBox( $li );
			MenuHtml.boxDelMenu( icon );
			MenuHtml.boxEditMenu( icon );
			MenuHtml.boxGrowMenu( icon );
			MenuHtml.boxLinkMenu( icon );
			MenuHtml.boxPlusMenu( icon );
			MenuHtml.delMenu( li );
			MenuHtml.editMenu( li );
			MenuHtml.growMenu( li );
			MenuHtml.linkMenu( li );
			MenuHtml.plusMenu( li );
			MenuHtml.updateMenuLevel( li );
			MenuHtml.updateTextLink( li );
			MenuHtml.getNotyEditingBar();
			MenuHtml.getNotyEditingButtons();

		MenuMng()
			MenuMng.hideMenuHtml();
			MenuMng.showMenuHtml();
			MenuMng.setMenu( wrapper, menuhtml );
			MenuMng.getMenuLocaleLabel( menuOptions, locale );
			MenuMng.setSavedStateTo( editor, toState );
			MenuMng.getSavedState( editor );
			MenuMng.getOptions();
			MenuMng.getDefaultOpts();
			MenuMng.getInitMenuOption( selector );
			MenuMng.initMenuOption( selector, options );
			MenuMng.getContentPromise( url, innerSelector );
			MenuMng.loadContent( url, target, innerSelector );
			MenuMng.getMenuHtml();
			MenuMng.saveMenu( icon );
			MenuMng.createMenuRoot( $wrapper, id, locale, menuMessage );
			MenuMng.copyMenuRoot( editor, root );
			MenuMng.replaceMenu( $wrapper, id, locale );
			MenuMng.refreshMenu( icon );
			MenuMng.refreshPage();
			MenuMng.isEditMode();
			MenuMng.editModeToggle( button );
			MenuMng.editModeToggleTo( state, button );
			MenuMng.editModeInit( opts );
			MenuMng.editModeShowLinkContent(href);
			MenuMng.editModeDisable();
			MenuMng.editModeDestroy();
			MenuMng.calcMenuHrcy( li );
			MenuMng.getParentMenuHrcy( li );
			MenuMng.getNewHrcy( parentMenuHrcy, menuLevel, menuSeq );
			MenuMng.getMenuSeq( li );
			MenuMng.mngMenuSeq( li );
			MenuMng.getMenuId( li );
			MenuMng.mngMenuId( li );
			MenuMng.getMenuLevel( li );
			MenuMng.mngMenuLevel( li );
			MenuMng.getMenuHrcy( li );
			MenuMng.mngMenuHrcy( li );
			MenuMng.getMenuMessageKey( li );
			MenuMng.mngMenuMessageKey( li );
			MenuMng.getMenuLocale( li );
			MenuMng.mngMenuLocale( li );
			MenuMng.getMenuHref( li );
			MenuMng.mngMenuHref( li );
			MenuMng.getMenuMessage( li );
			MenuMng.getMenuTopMessage( ul );
			MenuMng.mngAllMenuLevels( menutop );
			MenuMng.mngAllMenuSeqs( menutop );
			MenuMng.calcMenuLevel( li );
			MenuMng.calcMenuSeq( li );
			MenuMng.mngAllMenuClasses( menutop );
			MenuMng.mngMenuTop( topul, data );
			MenuMng.mngMenuDetail( li );
			MenuMng.mngMenuSerialize( topul, selector );
			MenuMng.isValidMenuHref( url );
			MenuMng.validateMenuHref( li );
			MenuMng.validateMenuMessage( li );

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2017/09/30		2.2.0	create
*/

/***********************************************************************************************************************
	MenuHtml Helper Function
***********************************************************************************************************************/
JSSTR_VOID_FUNCTION = "JavaScript:void(0)";

function debugVariable( variable, varName ) {
	var varNameOutput;

	varName = varName || '';
	varNameOutput = varName ? varName + ':' : '';

	console.warn(varNameOutput, variable, ' (' + (typeof variable) + ')');
};

/**
 * below is JSON polyfill for old browsers.
 * JSON is supported ie 11 and + and most modern browsers.
 */
if( !window.JSON ) {
	window.JSON = {
		parse : function( sJSON ) {
			return eval('(' + sJSON + ')');
		},
		stringify : (function() {
			var toString = Object.prototype.toString;
			var isArray = Array.isArray || function( a ) {
				return toString.call(a) === '[object Array]';
			};
			var escMap = {
				'"' : '\\"',
				'\\' : '\\\\',
				'\b' : '\\b',
				'\f' : '\\f',
				'\n' : '\\n',
				'\r' : '\\r',
				'\t' : '\\t'
			};
			var escFunc = function( m ) {
				return escMap[m] || '\\u' + (m.charCodeAt(0) + 0x10000).toString(16).substr(1);
			};
			var escRE = /[\\"\u0000-\u001F\u2028\u2029]/g;
			return function stringify( value ) {
				if( value == null ) {
					return 'null';
				} else if( typeof value === 'number' ) {
					return isFinite(value) ? value.toString() : 'null';
				} else if( typeof value === 'boolean' ) {
					return value.toString();
				} else if( typeof value === 'object' ) {
					if( typeof value.toJSON === 'function' ) {
						return stringify(value.toJSON());
					} else if( isArray(value) ) {
						var res = '[';
						for( var i = 0; i < value.length; i++ )
							res += (i ? ', ' : '') + stringify(value[i]);
						return res + ']';
					} else if( toString.call(value) === '[object Object]' ) {
						var tmp = [];
						for( var k in value ) {
							if( value.hasOwnProperty(k) )
								tmp.push(stringify(k) + ': ' + stringify(value[k]));
						}
						return '{' + tmp.join(', ') + '}';
					}
				}
				return '"' + value.toString().replace(escRE, escFunc) + '"';
			};
		})()
	};
}

function getJson( data ) {
	try {
		return JSON.parse(data);
	} catch( e ) {
		return data;
	}
}
function isJson( item ) {
	item = typeof item !== "string" ? JSON.stringify(item) : item;

	try {
		item = JSON.parse(item);
	} catch( e ) {
		return false;
	}

	if( typeof item === "object" && item !== null ) {
		return true;
	}

	return false;
}

function fireCustomEvent( eventName, element, data ) {
	'use strict';
	var event;
	data = data || {};
	if( document.createEvent ) {
		event = document.ConstructEvent("HTMLEvents");
		event.initEvent(eventName, true, true);
	} else {
		event = document.createEventObject();
		event.eventType = eventName;
	}

	event.eventName = eventName;
	event = $.extend(event, data);

	if( document.createEvent ) {
		element.dispatchEvent(event);
	} else {
		element.fireEvent("on" + event.eventType, event);
	}
}

function notyConfirm( msg ) {
	var self = this;
	self.dfd = $.Deferred();
	var n = noty({
		text : msg,
		type : 'confirm',
		modal : true,
		buttons : [ {
			addClass : 'btn btn-primary',
			text : 'Ok',
			onClick : function( $noty ) {
				$noty.close();
				self.dfd.resolve(true);
			}
		}, {
			addClass : 'btn btn-danger',
			text : 'Cancel',
			onClick : function( $noty ) {
				$noty.close();
				self.dfd.resolve(false);
			}
		} ]
	});
	return self.dfd.promise();
}

function checkSameOrigin( url, strictCheck ) {
	if( strictCheck == true ) {
		if( !(/^(\/\/|http:|https:).*/.test(url)) ) {
			return false;
		}
	}
	// test that a given url is a same-origin URL
	// url could be relative or scheme relative or absolute
	var host = window.document.location.host; // host + port
	var protocol = window.document.location.protocol;
	var srOrigin = '//' + host;
	var origin = protocol + srOrigin;
	// Allow absolute or scheme relative URLs to same origin
	return (url === origin || url.slice(0, origin.length + 1) === origin + '/')
			|| (url === srOrigin || url.slice(0, srOrigin.length + 1) === srOrigin + '/') ||
			// or any other URL that isn't scheme relative or absolute i.e
			// relative.
			!(/^(\/\/|http:|https:).*/.test(url));
};

/**
 * check url is the same as current domain
 *
 * @param {}
 *            url
 * @returns {}
 */
function isExternal( url ) {
	if( isAbsoluteUrl(url) ) {
		hostname = new RegExp(location.host);
		return !hostname.test(url);
	} else {
		return false;
	}
};

function isAbsoluteUrl( url ) {
	return (url.indexOf('://') > 0 || url.indexOf('//') === 0);
}


/***********************************************************************************************************************
	Alink
***********************************************************************************************************************/
function Alink(){};

/**
 *
 * @param {string} parentSelector
 * @param {string} aStartWith : original domain start with
 * @param {string} bStartWith : after domain start with
 * @returns {jQuery}
 */
Alink.rewriteLinkStartsWith = function( parentSelector, aStartWith, bStartWith ) {
	var ret = [];
	$(parentSelector + " a[href^='"+ aStartWith +"']").each(function(idx,obj) {
		$obj = $(obj);
		var href = $obj.attr("href");
		$obj.attr("href", href.replace(aStartWith, bStartWith));
		ret.push($obj);
	});
	return ret;
};

/**
 * @param {string} parentSelector
 * @param {string} aStartWith : same origin
 * @returns {jQuery}
 */
Alink.getLinksNotStartsWith = function(parentSelector, aStartWith) {
	return $(parentSelector + " a[href]:not([href^='"+ aStartWith + "'])");
};

/**
 * @param {string} parentSelector
 * @returns {jQuery}
 */
Alink.getLinks = function(parentSelector) {
	return $(parentSelector + " a");
};


/***********************************************************************************************************************
	Ajaxy
***********************************************************************************************************************/
function Ajaxy() {};
/**
 * to check response is session error html
 */
Ajaxy.isSessionErr = function( html ) {
	var sessionerrSelector = "input[name='sessionerr']";
	var sessionerr = ($(html).find(sessionerrSelector).val() == "sessionerr");
	if( true == sessionerr ) {
		return true;
	} else {
		return undefined;
	}
}


/***********************************************************************************************************************
	Assert
***********************************************************************************************************************/
function Assert() {};

/**
 * javascript boolean is tricky.
 *
 * (true == "true") -> false
 *
 * (true == "false") -> false
 */
Assert.getBoolean = function( strOrTfOrNum ) {
	return (strOrTfOrNum && typeof strOrTfOrNum == 'string') //
	? (strOrTfOrNum.toLowerCase() == 'true' || strOrTfOrNum == '1') //
	: (strOrTfOrNum == true);
}



/***********************************************************************************************************************
	String
***********************************************************************************************************************/
/**
 * var s = 'Hello %s The magic number is %d.'; s.format('world!', 12); // Hello
 * World! The magic number is 12.
 */
String.prototype.format = function() {
	var a = this, b;
	for( b in arguments ) {
		a = a.replace(/%[a-z]/, arguments[b]);
	}
	return a; // Make chainable
};

/**
 * "Hello, {name}, are you feeling {adjective}?.formatUnicorn({name: "Jane",
 * adjective: "OK"});" // yields "Hello, Jane, are you feeling OK?" However,
 * "a{0}bcd{1}ef".formatUnicorn("foo", "bar"); // yields "aFOObcdBARef"
 */
String.prototype.formatUnicorn = String.prototype.formatUnicorn || function() {
	"use strict";
	var str = this.toString();
	if( arguments.length ) {
		var t = typeof arguments[0];
		var key;
		var args = ("string" === t || "number" === t) ? Array.prototype.slice.call(arguments) : arguments[0];

		for( key in args ) {
			str = str.replace(new RegExp("\\{" + key + "\\}", "gi"), args[key]);
		}
	}

	return str;
};

/**
 * snake_is_like_this
 */
String.prototype.toSnakeCase = function() {
	var upperChars = this.match(/([A-Z])/g);
	if( !upperChars ) {
		return this;
	}

	var str = this.toString();
	for( var i = 0, n = upperChars.length; i < n; i++ ) {
		str = str.replace(new RegExp(upperChars[i]), '_' + upperChars[i].toLowerCase());
	}

	if( str.slice(0, 1) === '_' ) {
		str = str.slice(1);
	}

	return str;
};

/**
 * dash-is-like-this
 */
String.prototype.toDashCase = function() {
	var myStr = this.toString();
	return myStr.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
};


/***********************************************************************************************************************
	jQuery function
***********************************************************************************************************************/
$.fn.removeClassStartsWith = function( filter ) {
	$(this).removeClass(function( index, className ) {
		return (className.match(new RegExp("\\S*" + filter + "\\S*", 'g')) || []).join(' ')
	});
	return this;
};


jQuery.fn.extend({
	datainfo : function( data ) {
		if( data == undefined ) {// getter
			var data = {};

			[].forEach.call(this.get(0).attributes, function( attr ) {
				if( /^data-/.test(attr.name) ) {
					var camelCaseName = attr.name.substr(5).replace(/-(.)/g, function( $0, $1 ) {
						return $1.toUpperCase();
					});
					data[camelCaseName] = attr.value;
				}
			});
			return data;
		} else {// setter
			var isObj = function( obj ) {
				if( obj !== null && typeof obj === 'object' )
					return true;
				else
					return false;
			};
			var that = this.get(0);
			if( data !== undefined ) {
				if( isObj(data) && !Array.isArray(data) ) {
					$.each(data, function( k, v ) {
						var key = k.toDashCase();
						var val = isObj(v) ? JSON.stringify(v) : v;
						that.setAttribute("data-" + key, val);

					});
				}
			}
			return that;// chain
		}
	}
});

jQuery.fn.setdatainfo = function( data ) {
	var isObj = function( obj ) {
		if( obj !== null && typeof obj === 'object' )
			return true;
		else
			return false;
	};
	var that = this.get(0);
	if( data !== undefined ) {
		if( isObj(data) && !Array.isArray(data) ) {
			$.each(data, function( k, v ) {
				var key = k.toDashCase();
				var val = isObj(v) ? JSON.stringify(v) : v;
				that.setAttribute("data-" + key, val);
			});
		}
	}
	return that;
};

jQuery.fn.outerHTML = function(s) {
    return s
        ? this.before(s).remove()
        : jQuery("<p>").append(this.eq(0).clone()).html();
};


/***********************************************************************************************************************
	Noty
***********************************************************************************************************************/
function Noty() {};

Noty.info = function( msg, timeSec ) {
	if( timeSec == undefined ) {
		timeSec = 5;
	}
	var opts = {
		text : msg,
		timeout : (timeSec * 1000),
		type: 'information'
	};
	if( window.Noty.options["maxVisible"] ) {
		opts["maxVisible"] = window.Noty.options["maxVisible"];
	}
	noty(opts);
};

/**
 * developer's click confirm needed to proceed...
 */
Noty.debugConfirm = function( msg ) {
	if( window.Noty.options["debug"] == true ) {
		Noty.confirm(msg).then(function( tf ) {
			if( true == tf ) {
				Noty.info(msg + " promised as " + tf);
			} else {
				Noty.info(msg + " promised as " + tf);
			}
		});
	}
}

/**
 * info closes after certain time
 */
Noty.debugInfo = function( msg, jsobj ) {
	if( window.Noty.options["debug"] == true ) {
		if( jsobj != undefined ) {
			try {
				var jsonStr = JSON.stringify(jsobj);
				Noty.info(msg + " :: " + jsonStr, 2);
			} catch( e ) {
				Noty.info(msg + " :: " + jsobj, 2);
			}
		} else {
			Noty.info(msg, 2);
		}
	}
}

/**
 * @return promise
 */
Noty.confirm = function( msg ) {
	var self = this;
	self.dfd = $.Deferred();
	var n = noty({
		text : msg,
		type : 'confirm',
		modal : true,
		buttons : [ {
			addClass : 'btn btn-primary',
			text : 'Ok',
			onClick : function( $noty ) {
				$noty.close();
				self.dfd.resolve(true);
			}
		}, {
			addClass : 'btn btn-danger',
			text : 'Cancel',
			onClick : function( $noty ) {
				$noty.close();
				self.dfd.resolve(false);
			}
		} ]
	});
	return self.dfd.promise();
}


/***********************************************************************************************************************
	MenuHtml
***********************************************************************************************************************/
function MenuHtml() {};

MenuHtml.setMenuLinkActive = function( $li ) {
	$(".menu-wrapper").find("a.menu-link, a.menu-top-link").removeClass("active");
	var $alink = $li.find("> a.menu-link");
	$alink.addClass("active");
}

MenuHtml.addIconBox = function( $li ) {
	var istop = $li.is("ul") && $li.hasClass("menu-top");

	$li.find("span.ctl-icon-box").remove();

	var boxstr = '';
	boxstr = boxstr + '<span class="ctl-icon-box">';
	boxstr = boxstr + '<i class="fa fa-pencil fa-edit" title="edit label" onclick="JavaScript:MenuHtml.boxEditMenu(this);"></i>';
	boxstr = boxstr + '<i class="fa fa-link" title="add link" onclick="JavaScript:MenuHtml.boxLinkMenu(this);"></i>';
	if( false == istop ) {
		boxstr = boxstr + '<i class="fa fa-trash" title="delete item" onclick="JavaScript:MenuHtml.boxDelMenu(this);"></i>';
		boxstr = boxstr + '<i class="fa fa-arrow-down fa-grow" title="add below" onclick="JavaScript:MenuHtml.boxGrowMenu(this);"></i>';
	}
	boxstr = boxstr + '<i class="fa fa-plus" title="add child" onclick="JavaScript:MenuHtml.boxPlusMenu(this);"></i>' + '</span>';

	if( false == istop ) {
		if( $li.find("i.fa-arrows").length == 0 ) {
			$li.prepend('<i class="fa fa-arrows" title="drag me"></i>');
		}
		if( $li.find("ul").length > 0 ) {
			$li.find("ul").first().before(boxstr);
		} else {
			$li.append(boxstr);
		}
	} else {// menu-top is ul
		if( $li.find("li").length > 0 ) {
			$li.find("li").first().before(boxstr);
		} else {
			$li.append(boxstr);
		}
	}

	return $li;
};

/***********************************************************************************************************************
	MenuHtml - function to modify
***********************************************************************************************************************/
MenuHtml.boxDelMenu = function( icon ) {
	var uli = $(icon).parent().closest("[data-menu-level]");
	MenuHtml.delMenu(uli);
};

MenuHtml.boxEditMenu = function( icon ) {
	var uli = $(icon).parent().closest("[data-menu-level]");
	MenuHtml.editMenu(uli);
};

MenuHtml.boxGrowMenu = function( icon ) {
	var uli = $(icon).parent().closest("[data-menu-level]");
	MenuHtml.growMenu(uli);
};

MenuHtml.boxLinkMenu = function( icon ) {
	var uli = $(icon).parent().closest("[data-menu-level]");
	MenuHtml.linkMenu(uli);
};

MenuHtml.boxPlusMenu = function( icon ) {
	var uli = $(icon).parent().closest("[data-menu-level]");
	MenuHtml.plusMenu(uli);
};

MenuHtml.delMenu = function( li ) {
	if( !li.is("li") ) {
		alert("Internal Error occurred: " + li.html());
	}
	if( confirm("Would you like to delete ?") ) {
		li.remove();
	}
	return;
};

MenuHtml.editMenu = function( li ) {
	var o_className = "noty-editing";
	var btn_classNamePrefix = "btn-" + o_className;
	var currFuncName = "Function.MenuHtml.editMenu";
	var menuAttr = li.attr("data-menu-message") || "";
	var text = $("<div></div>").addClass("noty-editing-wrapper").attr("data-curr-func-name", currFuncName);
	var elmt = $("<span></span>");
	elmt.addClass("noty-editing-label").text("Label: ");
	text.append(elmt);
	elmt = $("<input></input>");
	elmt.addClass(o_className).attr("type", "text");
	text.append(elmt);

	var n = noty({
		text : $('<div class="temp-to-get-html">').append(text.clone()).html(),
		type : 'confirm',
		closeWith : [ 'button' ],
		callback : {
			onShow : function() {
				{// close existing
					var existing = $("." + o_className);
					if( existing.length > 0 ) {
						existing.each(function( idx, obj ) {
							var noty_buttons = $(obj).parents(".noty_bar").find(".noty_buttons");
							if( idx < existing.length - 1 ) {
								noty_buttons.find("." + btn_classNamePrefix + "-cancel").first().click();
							} else if( idx == existing.length - 1 ) {
								$(obj).on("keydown", function( event ) {
									var code = event.keyCode || event.which;
									if( code == 13 ) {
										noty_buttons.find("." + btn_classNamePrefix + "-ok").first().click();
									}
								});
							}
						});
					}
				}
			},
			afterShow : function() {
				$("." + o_className).val(menuAttr);
				$("." + o_className).focus();
			}
		},
		buttons : [ {
			addClass : 'btn btn-primary ' + (btn_classNamePrefix + "-ok"),
			text : 'Ok',
			onClick : function( $noty ) {
				// this = button element
				// $noty = $noty element
				var newtext = $noty.$message.find("." + o_className).val();
				li.attr("data-menu-message", newtext);
				MenuHtml.updateTextLink(li);

				var isValid = MenuMng.validateMenuMessage(li);
				if( true == isValid ) {
					li.removeClass("is-not-valid");
				}

				$noty.close();
				// noty({
				// text : 'You clicked "Ok" button',
				// type : 'success'
				// });
			}
		}, {
			addClass : 'btn btn-danger ' + (btn_classNamePrefix + "-cancel"),
			text : 'Cancel',
			onClick : function( $noty ) {
				$noty.close();
				// noty({
				// text : 'You clicked "Cancel" button',
				// type : 'error'
				// });
			}
		} ]
	});
};

MenuHtml.growMenu = function( li ) {
	if( !li.is("li") ) {
		alert("Internal Error occurred: " + li.html());
	}
	var lvl = li.attr("data-menu-level");
	if( lvl == undefined )
		lvl = 0;
	lvl = Number(lvl);

	if( lvl == 0 ) {
		alert("Internal Error occurred: " + li.html());
	}
	var parentIsTop = li.parent().hasClass("menu-top");
	if( parentIsTop ) {
		lvl = 1;
	}
	var newli = $('<li></li>');
	newli.attr("data-menu-level", lvl);
	newli.append("<ul></ul>")
	MenuHtml.updateTextLink(newli);
	MenuHtml.addIconBox(newli);
	li.parent().append(newli);
	MenuHtml.updateMenuLevel(newli);// after appended to dom

	return li;
};

MenuHtml.linkMenu = function( li ) {
	var o_className = "noty-editing";
	var btn_classNamePrefix = "btn-" + o_className;
	var currFuncName = "Function.MenuHtml.linkMenu";

	var menuAttr = li.attr("data-menu-href") || li.find("> a.menu-link").first().attr("href") || "";
	var text = $("<div></div>").addClass("noty-editing-wrapper").attr("data-curr-func-name", currFuncName);
	var elmt = $("<span></span>");
	elmt.addClass("noty-editing-label").text("URL: ");
	text.append(elmt);

	elmt = $("<input></input>");
	elmt.addClass(o_className).attr("type", "text");
	text.append(elmt);
	var texthtml = $('<div class="temp-to-get-html">').append(text.clone());

	var n = noty({
		text : texthtml.html(),
		type : 'confirm',
		closeWith : [ 'button' ],
		callback : {
			onShow : function() {
				{// close existing
					var existing = $("." + o_className);
					if( existing.length > 0 ) {
						existing.each(function( idx, obj ) {
							var noty_buttons = $(obj).parents(".noty_bar").find(".noty_buttons");
							if( idx < existing.length - 1 ) {
								noty_buttons.find("." + btn_classNamePrefix + "-cancel").first().click();
							} else if( idx == existing.length - 1 ) {
								$(obj).on("keydown", function( event ) {
									var code = event.keyCode || event.which;
									if( code == 13 ) {
										noty_buttons.find("." + btn_classNamePrefix + "-ok").first().click();
									}
								});
							}
						});
					}
				}
			},
			afterShow : function() {
				if( menuAttr == JSSTR_VOID_FUNCTION ) {
					$("." + o_className).val("");
				} else {
					$("." + o_className).val(menuAttr);
				}
				$("." + o_className).focus();
			}
		},
		buttons : [ {
			addClass : 'btn btn-primary ' + (btn_classNamePrefix + "-ok"),
			text : 'Ok',
			onClick : function( $noty ) {
				// this = button element
				// $noty = $noty element
				var newtext = $noty.$message.find('.' + o_className).val();
				var isext = isExternal(newtext);

				if( newtext == undefined || newtext == "" ) {
					li.removeAttr("data-menu-href");
					li.removeAttr("data-menu-href-is-external");
					MenuHtml.updateTextLink(li);
				} else {
					li.attr("data-menu-href", newtext);
					li.attr("data-menu-href-is-external", isext);
					MenuHtml.updateTextLink(li);

					var isValid = MenuMng.validateMenuHref(li);
					if( true == isValid ) {
						li.removeClass("is-not-valid");
					}
				}

				$noty.close();
			}
		}, {
			addClass : 'btn btn-danger ' + (btn_classNamePrefix + "-cancel"),
			text : 'Cancel',
			onClick : function( $noty ) {
				$noty.close();
			}
		} ]
	});
};

MenuHtml.plusMenu = function( li ) {
	var lvl = li.attr("data-menu-level");
	if( lvl == undefined )
		lvl = 0;
	lvl = Number(lvl);
	lvl = (lvl + 1);

	if( li.is("ul") && li.hasClass("menu-top") ) {// is actually ul
		if( li.is("ul") ) {
			var newli = $('<li data-menu-level="' + lvl + '"></li>');
			MenuHtml.updateTextLink(newli);
			MenuHtml.addIconBox(newli);

			li.append(newli);
		}
	} else {
		var newul = $('<ul class="menu-level-' + lvl + '"</ul>');
		var newli = $('<li data-menu-level="' + lvl + '"></li>');
		MenuHtml.updateTextLink(newli);
		MenuHtml.addIconBox(newli);

		newli.append("<ul></ul>");
		newul.append(newli);
		li.append(newul);
	}
	return li;
};

MenuHtml.updateMenuLevel = function( li ) {
	var pli = li.parent().parents("li").first();
	var plvl = 0;
	if( pli.length > 0 ) {
		var _plvl = pli.attr("data-menu-level");

		if( _plvl !== undefined ) {
			plvl = Number(_plvl);
		}
	}

	var lvl = plvl + 1;
	li.attr("data-menu-level", lvl);

	return li;
};

/**
 * update link with link based on data attr
 */
MenuHtml.updateTextLink = function( li ) {
	var text = li.attr("data-menu-message");
	var href = li.attr("data-menu-href");

	if( li.is("ul") && li.hasClass("menu-top") ) {
		var top_alink = li.find("> a.menu-link").first();
		if( top_alink.length == 0 ) {
			top_alink = $('<a></a>');
			top_alink.addClass("menu-top-link");
			top_alink.addClass("menu-link");
			li.prepend(top_alink);
		}

		if( text ) {
			top_alink.text(text);
			var locale = li.parent().closest("ul.menu-top").attr("data-menu-locale") || "";
			top_alink.attr("title", "menu title for language: " + locale);
		}

		if( href ) {
			var isValid = MenuMng.isValidMenuHref(href);
			if( isValid == false ) {
				return false;
			}
			href = $.trim(href);
			if( href == null || href == '' ) {
				top_alink.addClass("has-not-menu-link");
				top_alink.attr("href", "JavaScript:void(0)");
				top_alink.attr("data-has-link", false);
				li.attr("data-has-link", false);
			} else {
				top_alink.attr("href", href);
				li.attr("data-has-link", true);
			}
		} else {
			top_alink.addClass("has-not-menu-link");
			top_alink.attr("href", "JavaScript:void(0)");
			top_alink.attr("data-has-link", false);
			li.attr("data-has-link", false);
		}
	} else {
		var new_alink = $('<a class="menu-link"></a>');

		if( text == undefined ) {
			new_alink.text("......");
			new_alink.addClass("has-not-menu-message");
			li.removeAttr("data-menu-message");
		} else {
			new_alink.text(text);
			new_alink.removeClass("has-not-menu-message");
			li.attr("data-menu-message", text);
		}

		if( href ) {
			href = $.trim(href);
			if( href == null || href == '' ) {
				new_alink.addClass("has-not-menu-link");
				new_alink.attr("href", JSSTR_VOID_FUNCTION);
				new_alink.attr("data-has-link", "false");
				li.attr("data-has-link", false);
			} else {
				new_alink.attr("href", href);
				li.attr("data-has-link", true);
			}
		} else {
			new_alink.addClass("has-not-menu-link");
			new_alink.attr("href", "JavaScript:void(0)");
			new_alink.attr("data-has-link", "false");
			li.attr("data-has-link", false);
		}

		new_alink.on("click", function( event ) {
			if( li.closest(".menu-top.menu-editing").length > 0 ) {
				event.preventDefault();
				return false;
			} else {
				return true;
			}
		});

		var alink = li.find("a.menu-link").first();
		if( alink.length > 0 ) {
			alink.replaceWith(new_alink);
		} else {
			li.prepend(new_alink);
		}
	}

	return li;
};

/**
 * @return current ".noty_bar" element that has '.noty-editing';
 */
MenuHtml.getNotyEditingBar = function() {
	var noty_bar = $(".noty-editing").closest(".noty_bar");
	return noty_bar;
}
MenuHtml.getNotyEditingButtons = function() {
	var noty_bar = MenuHtml.getNotyEditingBar();
	return noty_bar.find(".noty_buttons");
}


/***********************************************************************************************************************
	MenuMng
***********************************************************************************************************************/
function MenuMng( options ) {
	var tempDefaults = {
		menuId : "TEMP:DEFAULT"
	};

	this.selector = options["selector"] || ".menu-wrapper";
	this.$wrapper = $(this.selector);

	var defaultOption_Api = {
		/** menu url mandatory. where to get the menuhtml ? */
		url : "",
		/** menu-locale-sel select box value */
		supportLocale : "en",
		/** menu-locale-sel select box label */
		supportLocaleLabel : "English",
		/** uniq id for the menu */
		menuId : "",
		/** locale for menu */
		menuLocale : "en",
		/** menu top message */
		menuMessage : "Menu Top Title",
		/** menu-wrapper selector ( vs content-wrapper ) */
		selector : this.$wrapper.selector,
		/** user has auth? */
		canEditMode : false
	};
	this.options = $.extend(defaultOption_Api, options);

	MenuMng.initMenuOption(this.$wrapper, this.options);
	if( !Assert.getBoolean(this.options.canEditMode) ) {
		MenuMng.getMenuHtml();
	}

	return this;
};

MenuMng.hideMenuHtml = function() {
	$(".menuhtml").addClass("menuhtml-hide");
}

MenuMng.showMenuHtml = function() {
	$(".menuhtml").removeClass("menuhtml-hide");
}

MenuMng.setMenu = function( wrapper, menuhtml ) {
	var $menuhtml = wrapper.find(".menuhtml");
	if( $menuhtml.length == 0 ) {
		wrapper.append(menuhtml);
	} else {
		$menuhtml.replaceWith(menuhtml);
	}
}

MenuMng.getMenuLocaleLabel = function( menuOptions, locale ) {
	var mngdata = MenuMng.getInitMenuOption();

	var labelIndex = -1;
	var locales = menuOptions.supportLocale.split(",");
	for( var i = 0; i < locales.length; i++ ) {
		if( locale == locales[i] ) {
			labelIndex = i;
		}
	}
	var label = menuOptions.supportLocaleLabel.split(",")[labelIndex];
	return label;
}

MenuMng.setSavedStateTo = function( editor, toState ) {
	if( toState == "saved" ) {
		editor.removeClass("menuhtml-unsaved").addClass("menuhtml-saved");
	} else if( toState == "unsaved" ) {
		editor.removeClass("menuhtml-saved").addClass("menuhtml-unsaved");
	}
	return editor;
}

MenuMng.getSavedState = function( editor ) {
	if( editor.hasClass("menuhtml-unsaved") ) {
		return "unsaved";
	} else if( editor.hasClass("menuhtml-saved") ) {
		return "saved";
	}
	return undefined;
}


/***********************************************************************************************************************
	MenuMng - menu options
***********************************************************************************************************************/
MenuMng.getOptions = function() {
	return window["MenuMng"]["options"]
};

MenuMng.getDefaultOpts = function() {
	return {
		menuId : "NEW.ING",
		menuLocale : "en",
		menuMessage : "Menu Title"
	}
}

MenuMng.getInitMenuOption = function( selector ) {
	if( selector == undefined ) {
		selector = $(".menu-wrapper");
	}
	var $opts = selector.find(".menu-mng-options");
	if( $opts.length == 0 ) {
		return {};
	} else {
		return $opts.datainfo();
	}
}

/**
 * setup default options on parent element reuse that option for toggle init or
 * destroy cycle. default: $(".menu-mng-options")
 */
MenuMng.initMenuOption = function( selector, options ) {
	selector = $(selector);

	var $toggle_box = selector.find(".ctl-edit-toggle-box");
	if( Assert.getBoolean(options.canEditMode) ) {
		if( $toggle_box.length == 0 ) {
			$toggle_box = $('<span class="ctl-edit-toggle-box"></span>');
			var btn = $('<button class="pure-button button-small"></button>');
			btn.attr("onclick", "JavaScript:MenuMng.editModeToggle(this);");
			btn.attr("name", "btn-menu-edit-toggle");
			btn.text("Edit Menu");
			btn.append($('<i title="edit menu" class="fa fa-toggle-off fa-toggle"></i>'));
			$toggle_box.append(btn);
			selector.append($toggle_box);
		}
	}

	var $opts = selector.find(".menu-mng-options");
	if( $opts.length == 0 ) {
		$opts = $("<div class='menu-mng-options' style='display:none;'></div>");
		selector.append($opts);
	}
	$opts.setdatainfo(options);

	return selector;
};


/***********************************************************************************************************************
	MenuMng - menu manage
***********************************************************************************************************************/
MenuMng.getContentPromise = function( url, innerSelector ) {
	var promised = function() {
		var self = this;
		self.dfd = $.Deferred();
		$.get(url, function(content){
			var ret = $(content).filter(".content-wrapper");
			if( innerSelector != undefined ) {
				self.dfd.resolve( ret.find(innerSelector).outerHTML() );
			} else {
				self.dfd.resolve( ret.outerHTML() );
			}
		});
		return self.dfd.promise();
	}
	return promised();
}

MenuMng.loadContent = function( url, target, innerSelector ) {
	url = replaceQueryValue( url, "locale", MenuMng.getInitMenuOption().menuLocale );

	MenuMng.getContentPromise(url, innerSelector)
		.then(function(getContentPromiseHtml){
			$(target).html(getContentPromiseHtml);
		});
}

MenuMng.getMenuHtml = function() {
	var mngdata = MenuMng.getInitMenuOption();

	if( mngdata == undefined || mngdata["url"] == undefined ) {
		alert("Internal Error occurred: " + "url is required.");
	}

	var menuhtml;
	var self = this;
	self.dfd = $.Deferred();

	var item = {
		menuId : mngdata.menuId,
		menuLocale : mngdata.menuLocale
	};

	if( item ) {// when validation success
		var dataToSend = $.extend({}, {
			mode : "menu"
		}, item);// map object

		// var response;
		var ajaxOpts = {
			url : mngdata.url,
			type : "GET",
			dataType : "json",
			data : dataToSend,
			success : function( data ) {

				// response = data;
				if( data["msg"] ) {
					// var text = "Save Success";
					var text = data.msg;
					var n = noty({
						text : text,
						layout : 'topLeft',
						timeout : 2000
					// timeout: ms or boolean
					});
				}
				if( data["menuhtml"] ) {
					menuhtml = data.menuhtml;
					MenuMng.setMenu($(".menu-wrapper"), menuhtml);
					self.dfd.resolve(data.menuhtml);
				} else {
					self.dfd.resolve(false);
				}
			},
			error : function( data ) {// only serious errors. controlled error
				// should
				// go success section.
				var text = "Save Error: " + data.responseText;
				var n = noty({
					text : text,
					layout : 'topLeft',
					timeout : 2000
				// timeout: ms or boolean
				});
			}
		};
		var promise = $.ajax(ajaxOpts);
	}
	return self.dfd.promise();
};

MenuMng.saveMenu = function( icon ) {
	var mngdata = MenuMng.getInitMenuOption();

	if( mngdata == undefined || mngdata["url"] == undefined ) {
		alert("Internal Error occurred: " + "url is required.");
	}

	var editor = $(".menu-edit-wrapper").first();
	var menutop = editor.find(".menu-top").first();
	var menutop_data = menutop.datainfo();
	if( menutop_data.menuHref == JSSTR_VOID_FUNCTION ) {
		alert("Internal Error occurred: " + JSON.stringify(menutop_data));
	}

	var locale = menutop_data.menuLocale;
	var label = MenuMng.getMenuLocaleLabel(mngdata, locale);
	var msg = "Do you want to save menu for " + label + "?";

	var item = MenuMng.mngMenuSerialize(menutop, "li");
	if( item == undefined || false == item ) {
		return false;
	}

	var saveAction = function( url, dataToSend ) {
		var self = this;
		self.dfd = $.Deferred();

		// var response;
		var ajaxOpts = {
			url : url,
			type : "POST",
			dataType : "json",
			data : dataToSend,
			success : function( data ) {
				if( data["msg"] ) {
					if( data.msg == "success" ) {
						self.dfd.resolve(true);
					}
					// var text = "Save Success";
					var text = data.msg;
					var n = noty({
						text : text,
						layout : 'topLeft',
						timeout : 2000
					});
				}
			},
			error : function( data ) {// only serious errors.
				// controlled error
				// should
				// go success section.
				console.log(data);
				var text = "Save Error: " + data.responseText;
				var n = noty({
					text : text,
					layout : 'topLeft',
					timeout : 2000
				// timeout: ms or boolean
				});
			}
		};
		var response = $.ajax(ajaxOpts);
		return self.dfd.promise();
	};

	notyConfirm(msg).then(function( state ) {
		if( state == true ) {
			if( item == undefined || false == item ) {
				return false;
			}
			var dataToSend = {
				mode : "mmod",
				menuId : menutop_data.menuId,
				menuLocale : menutop_data.menuLocale,
				data : JSON.stringify(item)
			};// map object

			var isSaveSuccess = saveAction(mngdata.url, dataToSend).then(function( state ) {
				if( state == true ) {
					MenuMng.setSavedStateTo(editor, "saved");
					MenuMng.refreshMenu();
				}
				return state;
			});
			return isSaveSuccess;
		} else {
			return false;
		}
	});
};

MenuMng.createMenuRoot = function( $wrapper, id, locale, menuMessage ) {
	var editing = $wrapper.find(".menuhtml-editing").first();
	var editingfound = editing.length;
	if( editing.length == 0 ) {
		editing = $('<div id="menu-creator" class="menuhtml-editing"></div>');
	}
	var menu_top = $wrapper.find(".menuhtml-editing .menu-top").first();
	var menu_topfound = menu_top.length;
	if( menu_top.length == 0 ) {
		menu_top = $('<ul id="menu-creator" class="menu-top"></ul>');
	}

	menu_top.addClass("menu-editing");
	menu_top.addClass("menu-top");

	menu_top_message = menu_top.find(".menu-top-link").first();
	if( !menu_top_message.length ) {
		menu_top.append($('<a class="menu-top-link menu-link">' + menuMessage + '</a>'));
	}

	MenuHtml.addIconBox(menu_top);

	var gdata = {
		"menuId" : id,
		"menuLocale" : locale,
		"menuMessage" : menuMessage
	};
	MenuMng.mngMenuTop(menu_top, gdata);

	var group = menu_top.sortable({
		group: 'menu-wrapper',
		nested: true,
		tolerance: -6,
		handle : 'i.fa-arrows',
		onDrop : function( $item, container, _super ) {
			MenuHtml.updateMenuLevel($item);
			_super($item, container);// _super is the copy of $item for
			// temporary
			// moving
		}
	});

	if(menu_topfound == 0 ) {
		editing.append(menu_top);
	} else {
		editing.find(".menu-top").first().replaceWith(menu_top);
	}
	if( editingfound == 0 ) {
		$wrapper.append(editing);
	} else {
		$wrapper.find(".menuhtml-editing").first().replaceWith(editing);
	}
	return $wrapper;
};

MenuMng.copyMenuRoot = function( editor, root ) {
	var existing = editor.find(".menuhtml-editing");
	if( existing.length > 0 ) {

	} else {
		root.clone().appendTo(editor);// copy root to editor
	}
	// replace .menuhtml to .menuhtml-editing
	editor.find(".menuhtml").removeClass("menuhtml").addClass("menuhtml-editing");
	MenuMng.setSavedStateTo(editor, "unsaved");

	editor.find("ul[data-menu-id]").first().addClass("menu-editing");
	var menu_top = editor.find(".menu-top");

	var group = menu_top.sortable({
		group: 'menu-wrapper',
		nested: true,
		tolerance: -6,
		handle : 'i.fa-arrows',
		onDrop : function( $item, container, _super ) {
			MenuHtml.updateMenuLevel($item);
			_super($item, container);// _super is the copy of $item for
			// temporary
			// moving
		}
	});

	MenuHtml.addIconBox(menu_top);
	menu_top.find("li").each(function( idx, obj ) {
		MenuHtml.addIconBox($(obj));
	});
	MenuMng.mngMenuSerialize(menu_top);

	// inject no click event to menuhtml-editing
	$(".menuhtml-editing a.menu-link").on("click", function( event ) {
		event.preventDefault();
		return false;
	});

	return editor;
};

/**
 * get menu from server and replace on html
 *
 * @param {}
 *            $wrapper
 * @param {}
 *            id
 * @param {}
 *            locale
 * @returns {String : menuhtml} menuhtml
 */
MenuMng.replaceMenu = function( $wrapper, id, locale ) {
	if( MenuMng.url == undefined ) {
		alert("Internal Error occurred.");
	}

	var dataToSend = {
		mode : "menu",
		menuId : id,
		menuLocale : locale
	};

	var menuhtml;
	var ajaxOpts = {
		url : MenuMng.url,
		type : "GET",
		dataType : "json",
		data : dataToSend,
		success : function( data ) {
			response = data;
			if( data["menuhtml"] ) {
				menuhtml = $wrapper.children(".menuhtml").first();
				if( menuhtml ) {// existing
					menuhtml.replaceWith(data.menuhtml);
				} else {
					$wrapper.append(data.menuhtml);
				}
			}
		},
		error : function( data ) {// only serious errors. controlled error
			// should go
			// success section.
			console.log(data);
		}
	};
	$.ajax(ajaxOpts);

	return menuhtml;
};

/**
 * refresh icon clicked. call MenuMng.replaceMenu
 *
 * @param {}
 *            icon
 */
MenuMng.refreshMenu = function( icon ) {
	var parent = $(icon).closest(".menu-wrapper");
	var menuhtml = parent.find(".menuhtml");

	if( menuhtml.length > 0 ) {
		var menu_top = menuhtml.find("ul.menu-top").first();
		var data = menu_top.datainfo();
		MenuMng.replaceMenu(parent, data.menuId, data.menuLocale);
	}
};

MenuMng.refreshPage = function() {
	location.reload(true);
};

/***********************************************************************************************************************
	MenuMng - editMode
***********************************************************************************************************************/
MenuMng.isEditMode = function() {
	var isOn = $(".menu-wrapper ctl-edit-toggle-box").find("i.fa-toggle").hasClass("fa-toggle-on");
	return isOn;
}

MenuMng.editModeToggle = function( button ) {
	var icon = $(button).find("i.fa-toggle");
	if( icon.hasClass("fa-toggle-off") ) {
		MenuMng.editModeToggleTo("init", button);
	} else if( $(icon).hasClass("fa-toggle-on") ) {
		MenuMng.editModeToggleTo("destroy", button);
	}
	return button;
};

MenuMng.editModeToggleTo = function( state, button ) {
	if( button == undefined ) {
		button = $(".menu-wrapper .ctl-edit-toggle-box button:has(i.fa-toggle)");
	}
	var icon = $(button).find("i.fa-toggle");
	if( state == "init" ) {
		$(icon).removeClass("fa-toggle-off");
		$(icon).addClass("fa-toggle-on");
		$(icon).addClass("menu-editing");
		MenuMng.editModeInit();
	} else if( state == "destroy" ) {
		$(icon).removeClass("fa-toggle-on");
		$(icon).addClass("fa-toggle-off");
		$(icon).removeClass("menu-editing");
		// MenuMng.editModeDestroy();
		MenuMng.editModeDisable();
	}
}


/**
 * first init with opions; and options are save to ".menu-mng-options"; if
 * invoked without parameter then get options from saved attributes.
 *
 */
MenuMng.editModeInit = function( opts ) {
	var wrapper = opts && $(opts['wrapper']) || $(".menu-wrapper");
	if( opts == undefined ) {
		var $opts = wrapper.find(".menu-mng-options");
		if( $opts.length == 0 ) {
			alert("opts.length == 0, not allowed");
			return false;
		} else {
			opts = $opts.datainfo();
		}
	}
	if( $.isEmptyObject(opts) ) {
		alert("Internal Error orccurred:: opts.length == 0, not allowed");
		return false;
	}

	MenuMng.initMenuOption(wrapper, opts);

	if( !wrapper ) {
		Noty.debugInfo("editModeInit wrapper false");
		return false;
	}

	var ctl_toggle_box = wrapper.find(".ctl-edit-toggle-box");
	if( ctl_toggle_box.length == 0 ) {
		ctl_toggle_box = $("<div class='ctl-edit-toggle-box'></div>");
		wrapper.append(ctl_toggle_box);
	}
	var ctl_box = ctl_toggle_box.find(".ctl-edit-box");
	if( ctl_box.length == 0 ) {
		ctl_box = $("<div class='ctl-edit-box pure-button-group' role='group'></div>");
		ctl_toggle_box.append(ctl_box);
	}

	var buttonLabel = {
		save : "Save",
		refreshPage : "Refresh Page"
	};
	var btn = $("<button>").addClass("pure-button button-xsmall");
	if( ctl_box.find("button i.fa-save").length == 0 ) {
		btn = btn.clone();
		btn.text(buttonLabel.save);
		btn.attr("name", "ctl-btn-save");
		btn.attr("onclick", "JavaScript:MenuMng.saveMenu(this);");
		btn.append('<i title="save" class="fa fa-floppy-o fa-save"></i>');
		ctl_box.append(btn);
	}

	if( ctl_box.find("button i.fa-refresh-page").length == 0 ) {
		btn = btn.clone();
		btn.text(buttonLabel.refreshPage);
		btn.attr("name", "ctl-btn-refresh");
		btn.attr("onclick", "JavaScript:MenuMng.refreshPage(this);");
		btn.append('<i title="refresh page" class="fa fa-refresh fa-refresh-page"></i>');
		ctl_box.append(btn);
	}

	// decide reuse current html or get from server

	var editor = wrapper.find(".menu-edit-wrapper");
	var menuhtml = $(wrapper.selector).find(".menuhtml").first();

	var editorWasExists;
	if( editor.length == 0 ) {
		editor = $('<div class="menu-edit-wrapper"></div>');
		wrapper.append(editor);
	} else {
		editorWasExists = true;
	}
	editor.show();
	ctl_box.show();

	// editing
	if( menuhtml.find("[data-menu-id]").length > 0 ) {
		MenuMng.copyMenuRoot(editor, menuhtml);
	} else {// for testing
		var _opts = MenuMng.getInitMenuOption();
		MenuMng.createMenuRoot(editor, _opts.menuId, _opts.menuLocale, _opts.menuMessage);
	}

	if( Assert.getBoolean(opts.canEditMode) ) {
		MenuMng.hideMenuHtml();
	}

	// hide
	menuhtml.addClass("menuhtml-hide");
	return this;
};


MenuMng.editModeShowLinkContent = function(href) {
	var currContentWidthCss = "width:"+ $(".content-wrapper").css("width")+";";
	var currDocumentHeightCss = "height:"+ $("body").height()+";";
	var customStyle = currContentWidthCss + currDocumentHeightCss + "float:right;overflow-x:hidden;overflow-y:scroll;";
	var disableClickCss = "pointer-events:none;";
	var templateStr = '<div class="noty_message_custom noty_message_menu_link_content" style="'+customStyle+'"><span class="noty_text" style="'+disableClickCss+'"></span><div class="noty_close"></div></div>';
	var killAllAndShow = true;

	var closeLinkContent = function() {
		$(".noty_bar .noty_message_menu_link_content").closest(".noty_bar").parent().remove();
	}
	var registerCloseNotyOnMenuWrapper = function() {
		$(".menu-wrapper").on("click", function(event){
			closeLinkContent();
			$(this).off(event);//for one time event
		});
		$(".menu-wrapper [name='btn-menu-edit-toggle']").on("click", function(event){
			closeLinkContent();
			$(this).off(event);// for one time event
		});
	}

	var hideContentWrapperTitle = function( response, status, xhr ) {
		debugger;
		if( status == "success" ) {
			console.log($(this));
			if( $("#content-title").length == 0 ) {
				alert("not");
			} else {
				alsert("ok");
			}
			$("#content-title").hide();
		} else {
			alert("not success");
		}
	}

	noty({
		layout: 'top',
		killer: killAllAndShow,
		template: templateStr,
		callback:{
			onShow: function(){
				MenuMng.loadContent(href, $(".noty_message_custom .noty_text"), "form[name='frmMain']");

				$(".noty_bar").parent().css({ width: $(".noty_message_custom").width(), float: 'left'});
				var bodyW = $("body").width();
				var scrollbarWPer = 2;
				var leftPercent = (( (bodyW - $(".content-wrapper").width())/bodyW )*100 + scrollbarWPer) + '%';
				$(".noty_bar").parent().parent().css({left: leftPercent })
			},
			afterShow: function() {
				registerCloseNotyOnMenuWrapper();
			}
		}
	});
};

MenuMng.editModeDisable = function() {
	var editor = $(".menu-wrapper .menu-edit-wrapper");
	var menuhtml = $(".menu-wrapper .menuhtml").first();
	menuhtml.removeClass("menuhtml-hide");

	// move 'editor' to 'menuhtml'
	var item = MenuMng.mngMenuSerialize(editor.find(".menu-top").first());

	var savedState = MenuMng.getSavedState(editor);
	if( savedState == "unsaved" ) {
		// copy from editor to menuhtml . not implemented yet.
		var menuhtml_editing = editor.find(".menuhtml-editing").first();

		// replacing and change class
		$(".menu-wrapper").find(".menuhtml").replaceWith(menuhtml_editing);
		$(".menu-wrapper .menuhtml-editing").removeClass("menuhtml-editing").removeClass("menuhtml-hide").addClass("menuhtml");

		$(".menuhtml a.menu-link").on("click", function(event){
			var $li = $(event.target).parent();
			MenuHtml.setMenuLinkActive($li);
			MenuMng.editModeShowLinkContent($(event.target).attr("href"));
		});

	} else if( savedState == "saved" ) {
		MenuMng.getMenuHtml().then(function( state ) {
			if( false != state ) {
				Noty.debugInfo("MenuMng.getMenuHtml() get and replace success");
			}
		});
	}

	var ctl_box = $(".menu-wrapper .ctl-edit-toggle-box .ctl-edit-box");
	if( false != item ) {
		editor.hide();
		ctl_box.hide();
	}
};

MenuMng.editModeDestroy = function() {
	var editor = $(".menu-wrapper .menu-edit-wrapper");
	var menuhtml = $(".menu-wrapper .menuhtml").first();
	menuhtml.removeClass("menuhtml-hide");

	var ctl_box = $(".menu-wrapper").find(".ctl-edit-box");
	if( ctl_box.length == 0 ) {
		alert("?");
	} else {
		ctl_box.remove();
	}

	editor.remove();
	MenuMng.getMenuHtml().then(function( state ) {

		var injectLoadLinkEvent = function() {
			$(".menuhtml a.menu-link").on("click", function( event ) {
				$obj = $(this);
				var canInvokeCustomEvent = ($obj.attr("href") != "JavaScript:void(0)");
				if( true == canInvokeCustomEvent ) {
					event.preventDefault();// will do custom action

					var contentSelector = $obj.attr("href") + " .content-wrapper > *";
					$(".content-wrapper").load(contentSelector, function( response, status, xhr ) {
						if( status == "success" ) {
							if( Ajaxy.isSessionErr(response) ) {
								location.href = getLocationURL();
							}
						} else if( status == "error" ) {
							Noty.debugInfo("Loading error:: " + $obj.attr("href"));
						}
					});
				}
			});
		}
		injectLoadLinkEvent();
	});
};


/***********************************************************************************************************************
	MenuMng - manage hierarchy
***********************************************************************************************************************/
MenuMng.calcMenuHrcy = function( li ) {
	var lvl = MenuMng.getMenuLevel(li);
	var seq = MenuMng.getMenuSeq(li);
	var hrcy = MenuMng.getNewHrcy(MenuMng.getParentMenuHrcy(li), lvl, seq);
	return hrcy;
}

MenuMng.getParentMenuHrcy = function( li ) {
	var lvl = MenuMng.getMenuLevel(li);
	if( lvl == 1 ) {// lvl == 1 -> direct child of menu-top
		return "#0[0";
	} else if( lvl > 1 ) {
		var parent = li.parent().closest("li[data-menu-level]");
		var phrcy = MenuMng.getMenuHrcy(parent);
		return phrcy;
	}
	return "";
}

MenuMng.getNewHrcy = function( parentMenuHrcy, menuLevel, menuSeq ) {
	return parentMenuHrcy + "#" + menuLevel + "[" + menuSeq;
};

MenuMng.getMenuSeq = function( li ) {
	var nth = li.index();
	if( $(li).parent().hasClass("menu-top") ) {
		nth = nth - 1;
	}
	return nth;
};

MenuMng.mngMenuSeq = function( li ) {
	var nth = MenuMng.getMenuSeq(li);
	li.attr("data-menu-seq", nth);
	return nth;
};

MenuMng.getMenuId = function( li ) {
	return li.parents("ul.menu-top").first().attr("data-menu-id");
};

MenuMng.mngMenuId = function( li ) {
	li.attr("data-menu-id", MenuMng.getMenuId(li));
	return li;
};

MenuMng.getMenuLevel = function( li ) {
	var lvl = li.attr("data-menu-level");
	if( lvl == undefined ) {
		lvl = Number(0);
	} else {
		lvl = Number(lvl);
	}

	return lvl;
};

MenuMng.mngMenuLevel = function( li ) {
	var lvl = MenuMng.getMenuLevel(li);
	li.attr("data-menu-level", lvl);
	return li;
};

/**
 * always calculate with data-menu-level and data-menu-seq
 */
MenuMng.getMenuHrcy = function( li ) {
	var hrcy = li.attr("data-menu-hrcy");
	var lvl = MenuMng.getMenuLevel(li);
	var seq = MenuMng.getMenuSeq(li);
	if( li.parent().hasClass("menu-top") ) {
		hrcy = MenuMng.getNewHrcy("#0[0", lvl, seq);
	} else {
		var phrcy = MenuMng.getMenuHrcy(li.parent().closest("li"));
		hrcy = MenuMng.getNewHrcy(phrcy, lvl, seq);
	}

	return hrcy;
};

MenuMng.mngMenuHrcy = function( li ) {
	var hrcy = MenuMng.getMenuHrcy(li);
	li.attr("data-menu-hrcy", hrcy);
	return li;
};

MenuMng.getMenuMessageKey = function( li ) {
	var id = MenuMng.getMenuId(li);
	var hrcy = MenuMng.getMenuHrcy(li);
	return String(id + hrcy);
};

MenuMng.mngMenuMessageKey = function( li ) {
	var msgkey = MenuMng.getMenuMessageKey(li);
	li.attr("data-menu-message-key", msgkey);
	return li;
};

MenuMng.getMenuLocale = function( li ) {
	return li.parents("ul.menu-top").first().attr("data-menu-locale");
};

MenuMng.mngMenuLocale = function( li ) {
	var locale = MenuMng.getMenuLocale(li);
	li.attr("data-menu-locale", locale);
	return li;
};

MenuMng.getMenuHref = function( li ) {
	var href = li.find("> a.menu-link").attr("href");
	return href;
};

MenuMng.mngMenuHref = function( li ) {
	var href = MenuMng.getMenuHref(li);
	li.attr("data-menu-href", href);
	return li;
};

MenuMng.getMenuMessage = function( li ) {
	if( li.is("ul") && li.hasClass("menu-top") ) {
		return li.find("> a.menu-link").first().text();
	} else {
		return li.find("> a.menu-link").first().text();
	}
};

MenuMng.getMenuTopMessage = function( ul ) {
	if( ul.find("a.menu-top-link").length > 0 ) {
		return ul.find("a.menu-top-link").first().text();
	} else {
		return "";
	}
};

MenuMng.mngAllMenuLevels = function( menutop ) {
	menutop.find("li").each(function( idx, obj ) {
		var $obj = $(obj);
		var currLevel = $obj.attr("data-menu-level");
		var calcLevel = MenuMng.calcMenuLevel($obj);
		if( currLevel != calcLevel ) {
			var msg = "before: <br/>" + JSON.stringify($obj.datainfo());
			$obj.attr("data-menu-level", calcLevel);
			msg += "after: <br/>" + JSON.stringify($obj.datainfo());
			msg += "---- data-menu-level:: " + $obj.attr("data-menu-level");
			Noty.debugInfo(msg);
		}
	});
}

MenuMng.mngAllMenuSeqs = function( menutop ) {
	menutop.find("li").each(function( idx, obj ) {
		var $obj = $(obj);
		var currSeq = $obj.attr("data-menu-seq");
		var calcSeq = MenuMng.calcMenuSeq($obj);
		if( currSeq != calcSeq ) {
			var msg = "before: <br/>" + JSON.stringify($obj.datainfo());
			$obj.attr("data-menu-seq", calcSeq);
			msg += "after: <br/>" + JSON.stringify($obj.datainfo());
			msg += "---- data-menu-seq:: " + $obj.attr("data-menu-seq");
			Noty.debugInfo(msg);
		}
	});
}

MenuMng.calcMenuLevel = function( li ) {
	if( li.is("ul") && li.hasClass("menu-top") ) {
		return 0;
	} else {
		return (li.parents("li[data-menu-level]").length + 1);
	}
}

MenuMng.calcMenuSeq = function( li ) {
	var nth = li.index();
	if( $(li).parent().hasClass("menu-top") ) {// top ul has one span for the
		nth = nth - 1;
	}
	return nth;
}

MenuMng.mngAllMenuClasses = function( menutop ) {
	// level class
	menutop.removeClassStartsWith("menu-level-");
	menutop.addClass("menu-level-0");
	menutop.find("li").each(function( idx, obj ) {
		var $obj = $(obj);
		var p_ul = $obj.parent();
		var c_ul = $obj.find("> ul").first();
		c_ul.removeClassStartsWith("menu-level-");
		var cls = MenuMng.calcMenuLevel($obj);
		c_ul.addClass("menu-level-" + String(cls));
	});
}


/***********************************************************************************************************************
	MenuMng - serialize
***********************************************************************************************************************/
/**
 * @param data
 *            eg. {menuLocale: "en", menuId: "TEST:ING", menuMessage: "Test Top" }
 */
MenuMng.mngMenuTop = function( topul, data ) {
	// try resolve menuHref, menuMessage
	var alink = topul.find("> a.menu-top-link");
	if( alink.length > 0 ) {
		if( alink.attr("href") != undefined ) {
			if( alink.attr("href") != JSSTR_VOID_FUNCTION ) {
				data.menuHref = alink.attr("href");
				data.hasLink = true;
				topul.attr("data-menu-href", data.menuHref);
				topul.attr("data-has-link", data.hasLink);
			}
		}
		if( alink.text() != undefined || alink.text() != "" ) {
			data.menuMessage = alink.text();
			topul.attr("data-menu-message", data.menuMessage);
		}
	}

	// menuId menuLocale menuMessage required
	var requiredKeys = [ "menuId", "menuLocale", "menuMessage" ];
	for( var ridx in requiredKeys ) {
		var rkey = requiredKeys[ridx];
		if( data.hasOwnProperty(rkey) ) {
			var rval = data[rkey];
			if( rval ) {
				topul.attr("data-" + rkey.toDashCase(), rval);
			} else {// invalid val
				alert("val of key required::" + rkey);
				return false;
			}
		} else {// invalid keyval
			alert("required keyval::" + rkey);
			return false;
		}
	}

	// default for top
	topul.attr("data-menu-hrcy", "#0[0");
	topul.attr("data-menu-level", "0");
	topul.attr("data-menu-seq", "0");
	topul.attr("data-menu-message-key", data["menuId"] + "#0[0");

	return topul;
};

MenuMng.mngMenuDetail = function( li ) {
	MenuMng.mngMenuId(li);
	MenuMng.mngMenuLocale(li);
	MenuMng.mngMenuLevel(li);
	MenuMng.mngMenuSeq(li);
	MenuMng.mngMenuHrcy(li);
	MenuMng.mngMenuMessageKey(li);
	MenuMng.mngMenuHref(li);
	return li;
};

/**
 *
 * @param {}
 *            topul : ul.menutop
 * @param {}
 *            selector : selector for "li" object
 * @returns {}
 */
MenuMng.mngMenuSerialize = function( topul, selector ) {
	var item = [];

	var data = topul.datainfo();

	MenuMng.mngMenuTop(topul, data);

	var isValid = MenuMng.validateMenuHref(topul);
	if( true != isValid ) {
		topul.find("> .ctl-icon-box > i.fa-link").first().trigger("click");
		noty({
			text : 'Please insert required Top Link.',
			timeout : 5000
			// ms or boolean
		});
		return false;
	}
	item.push(topul.datainfo());

	// level and seq should be calculated from menu-top to leaf
	// with level and then seq
	MenuMng.mngAllMenuLevels(topul);
	MenuMng.mngAllMenuSeqs(topul);
	MenuMng.mngAllMenuClasses(topul);

	topul.find(selector).each(function( idx, obj ) {
		var li = MenuMng.mngMenuDetail($(obj));
		if( li ) {
			item.push(li.datainfo());
			return true;
		} else {
			return false;
		}
	});

	var isValid = true;
	topul.find(selector).each(function( idx, obj ) {
		if( true == isValid ) {
			var li = MenuMng.mngMenuDetail($(obj));
			if( li ) {
				if( !MenuMng.validateMenuMessage(li) ) {
					isValid = false;
					if( false == isValid ) {
						li.addClass("is-not-valid");
						li.find("> .ctl-icon-box > i.fa-edit").first().trigger("click");
						noty({
							text : 'Please insert required label.',
							timeout : 5000
						// ms or boolean
						});
					}
				} else if( !MenuMng.validateMenuHref(li) ) {
					isValid = false;
					if( false == isValid ) {
						li.addClass("is-not-valid");
						li.find("> .ctl-icon-box > i.fa-link").first().trigger("click");
						noty({
							text : 'Please insert required link or valid link.',
							timeout : 5000
						// ms or boolean
						});
					}
				}
			}
			if( isValid == false ) {
				return false;
			}
		}
	});
	if( true == isValid ) {
		return item;
	} else {
		return false;
	}
};


/***********************************************************************************************************************
	MenuMng - validation
***********************************************************************************************************************/

/**
 * ( for ul.menu-top do not use this function as menu-top require valid same
 * origin url )
 *
 * valid: JSSTR_VOID_FUNCTION or domain url
 *
 * invalid: external url
 *
 * @return Boolean
 *
 */
MenuMng.isValidMenuHref = function( url ) {
	var isValid;
	if( JSSTR_VOID_FUNCTION == url ) {
		isValid = true;
	} else {
		if( isAbsoluteUrl(url) ) {
			isValid = checkSameOrigin(url, true);
		} else {
			isValid = true;
		}
	}
	return isValid;
}

MenuMng.validateMenuHref = function( li ) {
	var href;
	if( li.is("ul") && li.hasClass("menu-top") ) {
		href = MenuMng.getMenuHref(li);
		isValid = checkSameOrigin(href, true);
	} else {
		href = MenuMng.getMenuHref(li);
	}

	return MenuMng.isValidMenuHref(href);
}

MenuMng.validateMenuMessage = function( li ) {
	var msg = li.attr("data-menu-message");
	if( msg == undefined ) {
		return false;
	}

	return true;
};
