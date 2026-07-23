
// Create Base64 Object
var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/++[++^A-Za-z0-9+/=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}



function isUserAction(event) {
	var typ = event.type;
	var arr = ["click", "dblclick", "keydown", "keyup", "keypress", "blur", "select", "submit", "change", "mouseover"];
	for( var i=0; i< arr.length; i++ ) {
		if( arr[i].indexOf(typ) > -1 ) {
			return true;
		}
	}

	return false;
}

function getJSessionId(){
    var jsId = document.cookie.match(/JSESSIONID=[^;]+/);
    if(jsId != null) {
        if (jsId instanceof Array)
            jsId = jsId[0].substring(11);
        else
            jsId = jsId.substring(11);
    }
    return jsId;
}

function getHashCode(s){
	return s.split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);
}



// function createPouchDB( dbname ) {
// var pouch = new PouchDB(dbname, {adapter: 'websql'});// websql is perform better
// if (!pouch.adapter) { // websql not supported by this browser
//   pouch = new PouchDB(dbname);
// }
// return pouch;
// };

var schemaVer = "v1";
var mondbname = "evm"+schemaVer;
var remoteDBhost = "192.168.0.83";
var remoteDBport = 15984;
var mondb = new PouchDB(mondbname);
try {
	var remoteDB = new PouchDB('http://'+remoteDBhost+':'+remoteDBport+'/'+mondbname);
	remoteDB.info().then(function(info) {
		mondb.replicate.to(remoteDB).on('complete', function(){
			//yay, we're done!
		}).on('error', function(err){
			// something went wrong!
			console.log(err);
		});
	});
} catch(ex_ignored) {}


window.unique_id_counter = 0 ;
var uniqid = function(){
    var id ;
    while(true){
        window.unique_id_counter++ ;
        var pageId = 'cst';
        id = 'uid_'+ pageId +'_' + window.unique_id_counter ;
        if(!document.getElementById(id)){
            /*you can remove the loop and getElementById check if you 
              are sure that noone use your prefix and ids with this 
              prefix are only generated with this function.*/
            return id ;
        }
    }
}

function getElementByXpath(path) {
  return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
}

function getCssClassList( element ) {
	return ((element && element.className) ? element.className.split(/\s+/) : "");
}

function hasIdRegexp( element, regex ) {
	var hasClass = false;
	return $(element).attr("id") ? $(element).attr("id").match(regex) : false;
}

function hasCssRegexp( element, regex ) {
	var hasClass = false;
	var classList = $(element).attr("class") ? $(element).attr("class").split(" ") : "";
	for( var i=0; i< classList.length; i++ ) {
		if( classList[i].match(regex) ) {
			hasClass = true;
			break;
		}
	}

	return hasClass;
}

function toContextValue( i, el ) {
	var o = $(el);
	if( o.length > 0 ) {
		if( el.tagName == 'SELECT' ) {
			var opt = o.find("option:selected");
			return opt.text();
		} else if( el.tagName == 'OPTION' ) {
		} else if( el.tagName == 'TBODY' ) {
//			return o.children().length > 0 ? o.children().map(toContextValue).get().join("\t").trim() : "";
			return o.children().map(toContextValue).get().join("\t").trim();
		} else if( el.tagName == 'TABLE' ) {
			return o.find("tbody").children().map(toContextValue).get().join("\t").trim();
		} else {
			return o.text();
		}
	}
	return "";
}

function getContextValues( element, ctxValue, ctxName ) {
	var ctxContainer;
	var ctxType;
	var ctxTitle;
	var obj = $(element);

	if( obj.length > 0 ) {
		if( element.nodeName == "TD" ) {
			ctxType = 'TableCell';
			ctxName = ctxName || "Row("+(obj.parent().parent().children().index(obj.parent())+1)+")";
			ctxValue = ctxValue
				|| ( obj.find("select, table, tbody").length > 0
					 ? obj.children().map(toContextValue).get().join("\t").trim()
					 : (obj.text() || obj.val()));

			if( obj.closest("table").length>0 ) {
				ctxContainer = obj.closest("table")[0].className;
				if( ctxContainer.indexOf("list_content") > -1 ) {
					var tb = obj.closest("table");
					if( tb.length > 0 ) {
						var colidx = (obj.index()+1);
						ctxTitle = TableEx.getHeaderNames( "table"+"."+ ctxContainer + " td:nth-of-type("+colidx+")" );
						if( ctxTitle ) {
							ctxTitle = ctxTitle[0];
						}
					}
				}
				if( !ctxContainer ) {
					ctxContainer = obj[0].className;
				}
			}
		}


		// list_content or search_line_content ( label and value relationship )
		if( hasCssRegexp(element, /^content/) || hasCssRegexp(element, /^subject/) || hasIdRegexp(element, /^title_/) ) {
			ctxContainer = obj.closest("table")[0].className;
			if( ctxContainer.indexOf("line_content") > -1 ) {
				var td = obj.closest("td");
				var nm;
				if( hasCssRegexp(element, /^subject/) ) {
					ctxType = 'Subject';
					ctxTitle = obj.text().trim();
					nm = obj.attr("id") ? obj.attr("id").replace(/^title_/) : undefined;
					if( nm ) {
						ctxValue = ctxValue || obj.closest("table").find("[name="+nm+"]").val();
					}
				} else if( hasCssRegexp(element, /^content/) ) {
					ctxType = 'Content';
					nm = obj.attr("name");
					if( nm ) {
						ctxTitle = obj.closest("table").find("#title_"+nm).text().trim();
						if( !ctxTitle ) ctxTitle = nm;
						ctxValue = ctxValue || obj.val();
					}
					if( obj[0].tagName == 'SELECT' ) {
						ctxName = obj.find("option:selected").text();
					} else {
						ctxName = obj.text();
					}
				} else if( hasIdRegexp(element, /^title_/) ) {
					ctxType = 'Subject';
					ctxTitle = obj.text().trim();
					nm = obj.attr("id") ? obj.attr("id").replace(/^title_/, "") : undefined;
					if( nm ) {
						ctxValue = ctxValue || obj.closest("table").find("[name="+nm+"]").val();
					}
				}
			}
		}

		var pobj = (obj.parent() && obj.parent().closest("td").length>0 ? obj.parent().closest("td") : undefined );
		if( pobj && hasCssRegexp(pobj[0], /^list_content/) ) {
			ctxContainer = obj.closest("table")[0].className;

			if( obj.parent() && obj.parent()[0].tagName == 'SPAN' ) {
				ctxTitle = obj.parent().attr("class") || obj.parent().attr("id");
			} else if( element && element.tagName == 'SPAN' ) {
				ctxTitle = obj.attr("class") || obj.attr("id");
			}

			ctxValue = ctxValue || obj.val();
			if( obj && obj[0].tagName == 'SELECT' ) {
				ctxName = obj.find("option:selected").text();
			} else {
				ctxName = obj.text();
			}
		}

		if( obj.parent().length>0 && obj.parent()[0].tagName == 'TD' && obj[0].tagName == 'INPUT' && obj.attr("name") == "listcheckbox" && (obj.attr("type") == 'checkbox' || obj.attr("type") == 'radio') ) {

			var checked = obj.prop("checked") == true ? "checked" : "unchecked";
			var labels = obj.parent().closest("tr").find("label[for="+ obj.attr("id")+"]").map(function(i,o){
				return $(o).text();
			}).get().join(";");
			labels = checked + '\t' + labels;
			var line = obj.parent().closest("tr").text().trim();
			line = checked + "\n" + line;
			var tdobj = obj.parent()[0];
			return getContextValues( tdobj, labels, obj.attr("id") );
		}

		if( obj[0].tagName == 'IMG' ) {
			if( obj.parent() && obj.parent()[0].tagName == 'A' ) {
				return getContextValues(obj.parent());
			}
		}

		if( obj[0].tagName == 'INPUT' && obj.attr("type") == 'image' ) {
			ctxType = 'Submit';
			ctxName = obj.text() || obj.attr("title") || obj.attr("id");
			ctxValue = ctxValue || obj.attr("href");
			if( obj.parent().closest("table").length>0 ) {
				ctxContainer = obj.closest("table")[0].className || obj.closest("table")[0].id;
				if( !ctxContainer ) {
					console.log("not %%% ctxContainer. element: " + element);
				}
			}
		}

		if( obj[0].tagName == 'A' ) {
			ctxType = 'Link';
			ctxName = obj.text() || obj.attr("title") || obj.find("> img").attr("id");
			if( !ctxName && obj.parent().prop("tagName") == 'SPAN' ) {
				pobj = obj.parent();
				ctxName = pobj.attr("class") || pobj.attr("id") || pobj.attr("name") || pobj.attr("title");
			}
			ctxValue = ctxValue || obj.attr("href");
			if( !ctxContainer && obj.parent().closest("table").length>0 ) {
				ctxContainer = obj.closest("table")[0].className || obj.closest("table")[0].id;
			}
			if( !ctxContainer && obj.parent().closest("div").length > 0 ) {
				ctxContainer = obj.closest("div")[0].className || obj.closest("div")[0].id;
			}
		}
	}

	return {
		"ctxContainer": ctxContainer,
		"ctxType": ctxType,
		"ctxTitle":ctxTitle,
		"ctxName":ctxName,
		"ctxValue": ctxValue,
	};
}



var sessId = getCookie("chlrhdmldlstod");
var uqUserId = window.uqUserId || Base64.encode(sessId);

function dbSaveEventHandler(event) {

	if( !isUserAction(event) ) {
		return false;
	}

	var xp = getElementXpath(event.toElement);
//    console.log("my custom handler: ", event.type, event);
//    console.log("my custom handler: ", "xpath: ", xp);

	var markup = document.documentElement.innerHTML;
	var timestamp = Date.now();
	var uqprefix = uniqid();
	var saveId = (uqprefix + '.' + uqUserId+ '.' + timestamp);
	try {
		var eventTarget = $(event.target)[0].outerHTML;
	} catch(e) {
		console.log(e, event.target);
	}
	var eventContext = getContextValues(event.target);
	var el = getElementByXpath(xp);
		console.log("uqUserId: "+ uqUserId + " elxp: ", el, event.type, eventContext);

		mondb.put({
			_id: saveId,
			uqPrefix: uqprefix,
			uqUserId: uqUserId,
			uqTimestamp: timestamp,
			userSessionId: encodeURIComponent(sessId),
			eventType: event.type,
			eventTarget: eventTarget,
			eventContext: eventContext,
			xpath: xp,
			url: location.href,
			html: markup,
		}).then(function(res) {
//			console.log("res: ", res);
			if( res ) {
				var useImageCapture = false;
				if( false ) {
					if( event.type.indexOf('click') > -1 ) {
						console.log( "ee: ", event.type );
					}
					if( event.type.indexOf('mouseover') > -1 ) {
						console.log( "ee: ", event.type );
					}
				}
			if( useImageCapture == true ) {
				html2canvas(document.body).then(function(canvas) {
					// Generate the base64 representation of the canvas
					var base64image = canvas.toDataURL("image/png");

					// Split the base64 string in data and contentType
					var block = base64image.split(";");
					// Get the content type
					var mimeType = block[0].split(":")[1];// In this case "image/png"
					// get the real base64 content of the file
					var realData = block[1].split(",")[1];// For example:  iVBORw0KGgouqw23....

					// Convert b64 to blob and store it into a variable (with real base64 as value)
					var canvasBlob = b64toBlob(realData, mimeType);

					mondb.putAttachment(res.id, (res.id+'.png'), res.rev, canvasBlob, 'image/png').then(function(result){
//						console.log("saveid: "+ saveId + " saved: ", result);
						mondb.get(res.id).then(function(doc){
						});
					});
				});
			}
		}
		}).then(function(err) {
			if( err ) {
				console.log("err: ", err);
			}
		});
}

// Usage: startMonitorEvents(window, eventHandler);
//EventMon.startMonitorEvents(window, EventMon.consoleLogEventHandler);
EventMon.startMonitorEvents(window, dbSaveEventHandler);
