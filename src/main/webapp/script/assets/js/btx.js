/*
	File Name:	btx.js
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/03/30		2.2.0	create
*/

function Bts() {}

/* deviceWidth: device's actual width
   viewportWidth: your web page's viewable space width
*/
Bts.getDeviceWidthName = function() {
	if(window.innerWidth < 769){
		// Extra Small Device(768 and under)
		return "xs";
	} else if(window.innerWidth < 992){
		// Small Device(768~991)
		return "sm";
	} else if(window.innerWidth < 1200){
		// Medium Device(992~1199)
		return "md";
	} else {
		// Large Device(1200 and upper)
		return "lg";
	}
};

Bts.getNativeResolution = function() {
	//devicePixelRatio: how many of the device's screen actual pixels should be used to draw a single CSS pixel;
	// for retina display
	return {x: (window.screen.width * window.devicePixelRatio), y: (window.screen.height * window.devicePixelRatio)};
};

Bts.getEffectiveDeviceWidth = function() {
	var deviceWidth = window.orientation == 0 ? window.screen.width : window.screen.height;
	// iOS returns available pixels, Android returns pixels / pixel ratio
	// http://www.quirksmode.org/blog/archives/2012/07/more_about_devi.html
	if (navigator.userAgent.indexOf('Android') >= 0 && window.devicePixelRatio) {
		deviceWidth = deviceWidth / window.devicePixelRatio;
	}
	return deviceWidth;
};

Bts.determineOverflowX = function(content, container) {
	var containerMetrics = container.getBoundingClientRect();
	var containerMetricsRight = Math.floor(containerMetrics.right);
	var containerMetricsLeft = Math.floor(containerMetrics.left);
	var contentMetrics = content.getBoundingClientRect();
	var contentMetricsRight = Math.floor(contentMetrics.right);
	var contentMetricsLeft = Math.floor(contentMetrics.left);
	if (containerMetricsLeft > contentMetricsLeft && containerMetricsRight < contentMetricsRight) {
		return "both";
	} else if (contentMetricsLeft < containerMetricsLeft) {
		return "left";
	} else if (contentMetricsRight > containerMetricsRight) {
		return "right";
	} else {
		return "none";
	}
};

Bts.moveScreenToTop = function( gotoTopButtonElement ) {
	if( !gotoTopButtonElement )
		throw new Error("element manadatory.");

	var mybutton = gotoTopButtonElement;

	// When the user scrolls down 20px from the top of the document, show the button
	window.onscroll = function() {scrollFunction()};

	function scrollFunction() {
		if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
			mybutton.style.display = "block";
		} else {
			mybutton.style.display = "none";
		}
	}

	// When the user clicks on the button, scroll to the top of the document
	function topFunction() {
		document.body.scrollTop = 0;
		document.documentElement.scrollTop = 0;
	}
};

/**
   Bts.contentGroupTdWidth0( "content", "header" );
*/
Bts.transformContentGroup = function( contentType, contentId ) {
	var useBtWrapHack = true;
	var processOfContent = function( contentType, contentId ) {
		var q_content_group = '[id='+contentType+'_'+contentId+']';
		var content_group;
		if( contentType instanceof HTMLElement ) {
			content_group = contentType;
		} else {
			content_group = document.querySelector(q_content_group);
		}
		if( !content_group ) return;
		var btn_content = content_group.querySelector(".btn_content");
		if( btn_content ) {
			var btn_group = btn_content.querySelector(".btn-group");
		}

		content_group.classList.toggle("-btx", true);
		var prevTrChildCount;
		var trs = content_group.querySelectorAll("table.line_content tr td table.line_content tr");
		if( !trs || trs.length == 0 )
			trs = content_group.querySelectorAll("table.line_content tr");
		_.each(trs||[], function(tr) {
			tr.classList.toggle("-btx", true);
			tr.classList.toggle("row", true);
			tr.classList.toggle("mx-0", true);
			var trChildCount = tr.children.length;
			var $tmp_td = $("<td>");
			$tmp_td.addClass("-btx btx-td-wrap container-fluid mx-0 px-0");
			var tmp_row = document.createElement("div");
			tmp_row.className = "-btx -btx-row row mx-0";
			_.each(tr.children, function(td) {
				td.classList.toggle("-btx", true);
				td.classList.toggle("col", true);
				var hadSubject = false;
				var hadContent = false;
				_.each(td.classList, function( clsName ){
					if( clsName && !_.isEqual(clsName, "subject0") && clsName.match(/^subject/) ) {
						td.classList.toggle(clsName, false);
						td.setAttribute("data-original-subject-suffix", clsName.replace(/^subject/,""));
						if( hadSubject == false )
							hadSubject = true;
					}
					if( clsName && !_.isEqual(clsName, "content0") && clsName.match(/^content/) ) {
						td.classList.toggle(clsName, false);
						td.setAttribute("data-original-content-suffix", clsName.replace(/^content/,""));
						td.setAttribute("data-content-column-length", clsName.replace(/^content[0-9]$/,""));
						if( hadContent == false )
							hadContent = true;
					}
				});
				if( hadSubject == true ) {
					td.classList.toggle("subject0", true);
				}
				if( hadContent == true )
					td.classList.toggle("content0", true);
				//
				var tareas = td.querySelectorAll("textarea");
				if( tareas ) {
					_.each(tareas, function(tarea){
						_.each(tarea.classList, function( clsName ) {
							if( clsName && clsName.match(/^length_/) ) {
								tarea.classList.toggle(clsName, false);
								tarea.classList.toggle("form-control", true);
							}
						});
					});
				}
				if( useBtWrapHack ) {
					if( _.includes(td.classList, "subject0") ) {
						td.className += " -btx -btx-col d-flex align-items-center border-bottom";

						if( trChildCount > 3 && trChildCount < 5) {
							td.className += " col-4 col-md-2 pl-0 pl-md-1";
						} else {
							td.className += " col-4 col-md-2 pl-0 pl-md-1";
						}
					}
					if( $(td).prev("td").hasClass("subject0") && _.includes(td.classList, "content0") ) {
						td.className += " -btx -btx-col d-flex align-items-center border-bottom";
						if( trChildCount > 3 && trChildCount < 5) {
							td.className += " col-8 col-md-4 pr-0 pr-md-1";
						} else {
							td.className += " col-8 col-md-4 pr-0 pr-md-1";
						}
						_.each(td.children, function(el){
							if( el instanceof HTMLInputElement && _.includes(el.classList, "form-control") ) {
								if( el.className.match(/(^|\ )length\w+/) ) {
									var grps = /((^|\ )length\w+)/.exec(el.className);
									var lenCls = grps[1];
									if( lenCls ) {
										lenCls = lenCls.replace(/^\s+|\s+$/gm, "");
										el.setAttribute("data-original-classes", lenCls);
										el.classList.toggle(lenCls, false);
									}
									el.className += " -btx";
								}
							}
						});
					}
					var $div = $("<div>");
					$div.addClass( td.className ).addClass("justify-content-between");
					$div.attr("style", td.getAttribute("style") );
					$div.html( td.innerHTML );
					$div.appendTo( tmp_row );
				}
			});
			if( useBtWrapHack ) {
				while( tr && tr.firstChild ) {
					tr.removeChild( tr.firstChild );
				}
				$tmp_td.append( tmp_row );
				$(tr).append( $tmp_td );
			}
			prevTrChildCount = trChildCount;
		});
	};

	switch( contentType ) {
	case 'fieldset':
	case 'content':
		processOfContent(contentType, contentId);
		break;
	case 'list':
		Bts.transformListContent(contentType, contentId);
		break;
	case 'search':
		Bts.transformSearchContent(contentType, contentId);
		break;
	case 'tabpane':
		break;
	}
};

Bts.transformListContent = function(contentType, contentId )  {
	var q_content = '#content_'+contentId;
	var content;
	if( contentType instanceof HTMLElement ) {
		content = contentType;
	} else {
		content = document.querySelector(q_content);
	}
	if( !content ) return;

	function addClassList(el, classArr) {
		_.each(classArr, function(className) {
			el.classList.toggle(className, true);
		});
	}
	function isChildOf(/*child node*/c, /*parent node*/p){ //returns boolean
		while((c=c.parentNode)&&c!==p);
		return !!c;
	}
	function findDirectChild(/*child node*/c, /*parent node*/p){ //returns boolean
		while(c.parentNode!==p && (c=c.parentNode));
		return c;
	}

	Bts.transformListContentTop( content );
	Bts.transformListContentFunction( content );
	Bts.transformListContentBottom( content );
	Bts.transformContentGroupToCardBody( content );
};

Bts.transformListContentFunction = function( list_content ) {
	function findDirectChildOfNonRelativePosition(/*child node*/c, /*parent node*/p){ //returns boolean
		while(c.parentNode!==p && c.parentNode.tagName != 'FORM' && (c=c.parentNode));
		return c;
	}
	var list_header = list_content.querySelector("table.list_content_header");
	var list_starting_el;
	if( list_header ) {
		list_starting_el = findDirectChildOfNonRelativePosition(list_header, list_content);
	}
	var list_functions = list_content.querySelectorAll("table.list_content_function");
	_.each(list_functions, function(btnList){
		var btn_tds = btnList.querySelectorAll("tr td");
		var div = document.createElement("div");
		_.each(btn_tds, function(td) {
			while( td.childNodes.length > 0 ) {
				div.appendChild(td.childNodes[0]);
			}
			div.className += " -btx d-flex flex-wrap btn-group justify-content-end align-items-center";
		});
		if( div.childNodes.length == 1 && div.childNodes[0].nodeType == Node.TEXT_NODE )
			div.className += " empty";
		if( btn_tds && btn_tds.length > 0 ) {
			btn_tds[0].appendChild(div);
		}
	});
};

Bts.moveListContentFunctionToTop = function( list_content_function, whereToInsert ) {
	$(list_content_function).insertBefore(whereToInsert);
};

Bts.transformSearchContent = function( contentType, contentId ) {
	var q_content = '#content_'+contentId;
	var content = document.querySelector(q_content);
	if( !content ) return;

	var table_contents = content.querySelectorAll("table.search_content");
	console.log("table_contents:", table_contents);
	_.each(table_contents, function(table_content) {
		var search_line_contents = table_content.querySelectorAll("tr td table.search_line_content");

		console.log("search_line_contents:", search_line_contents);
		var tr_new = document.createElement("tr");
		var td_search_buttons = table_content.querySelectorAll("td.search_button");
		_.each(td_search_buttons, function(td){
			td.classList.toggle("align-bottom", true);
			var div = document.createElement("div");
			div.className += " -btx btn-group d-flex flex-row";
			while(td.children.length > 0 ) {
				td.children[0].classList.toggle("flex-fill", true);
				div.append(td.children[0]);
			}
			td.appendChild(div);
			tr_new.appendChild(td);
		});

		_.each(search_line_contents, function(table) {
			var trs = table.querySelectorAll("tr");
			_.each(trs, function(tr){
				tr.classList.toggle("-btx", true);
				tr.classList.toggle("row", true);
				tr.classList.toggle("mx-0", true);
				_.each(tr.children, function(td){
					var $span = $("<span class='-btx d-flex input-group'>");
					while( td.childNodes.length > 0 ) {
						if( td.childNodes[0] instanceof HTMLSelectElement ) {
							var $spanIn = $("<span class='-btx py-0 px-1 p-sm-1'>");
							$spanIn.append( td.childNodes[0] );
							$span.append( $spanIn );
						} else {
							if( td.childNodes[0] instanceof HTMLSpanElement ) {
								td.childNodes[0].className += " -btx py-0 px-1 p-sm-1";
							}
							$span.append( td.childNodes[0] );
						}
					}
					$(td).append($span);
				});
				var tdCount = tr.children.length;
				var subjects = tr.querySelectorAll("td.search_subject");
				_.each(subjects, function(td) {
					td.className += " -btx col-4 d-flex align-items-center";
					if( tdCount > 3 ) {
						td.classList.toggle("col-md-2", true);
						td.classList.toggle("pr-md-0", true);
					} else {
						td.classList.toggle("col-md-2", true);
						td.classList.toggle("pr-md-0", true);
					}
					_.each(td.querySelectorAll("span"), function(td_span){
						if( td_span.childNodes.length == 1 && td_span.childNodes[0].nodeType == Node.TEXT_NODE ) {
							td_span.className += " textonly";
						}
					});
				});
				var contents = tr.querySelectorAll("td.search_content");
				_.each(contents, function(td, td_i) {
					td.classList.toggle("-btx", true);
					if( subjects.length == 1 && contents.length == 2 ) {
						if( td_i == 0 ) {
							td.className += " col-8 col-md-8 pl-md-0 d-flex flex-column flex-sm-row";
						} else {
							while(td.childNodes.length > 0){
								contents[0].appendChild(td.childNodes[0]);
							}
						}
					} else {
						td.classList.toggle("col-8", true);
						if( tdCount > 3 ) {
							td.className += " col-md-4 pl-md-0";
						} else {
							td.className += " col-md-10 pl-md-0";
						}
						td.className += " d-flex flex-column flex-sm-row";
					}
				});
			});
			table.querySelector("tbody").appendChild(tr_new);
		});
	});
	Bts.transformContentGroupToCardBody( content );
};

Bts.transformButtonPage = function() {
	var btn_pages = document.querySelectorAll("table.btn_page");
	_.each(btn_pages, function(btnPage){
		if( !_.includes(btnPage.classList, "-btx") ) {
			var tds = btnPage.querySelectorAll("tr td");
			if( tds && tds.length > 0 ) {
				var div = tds[0].querySelector(".-btx-btn-page");
				if( div ) {
					return;
				}
				if( !div ) {
					div = document.createElement("div");
					div.className += "-btx -btx-btn-page btn-group d-flex flex-wrap";
				}
				_.each(tds, function(td) {
					while( td.childNodes.length > 0 ) {
						div.appendChild(td.childNodes[0]);
					}
				});
				var showCnt = _.reduce(div.childNodes, function(memo, child){
					if( child && ( !_.includes(child.classList, "d-none")
								   || child.style.display != "none" ) ) {
						if( !(child.nodeType == Node.TEXT_NODE && child.textContent.trim() == "") ) {
							memo = memo +1;
						}
					}
					return memo;
				},0);
				if( showCnt == 0 )
					div.className += " empty";
				tds[0].appendChild(div);
			}
		}
		btnPage.classList.toggle("bg-white", true);
	});
};

Bts.transformButtonContent = function() {
	var btn_contents = document.querySelectorAll("table.btn_content");
	_.each(btn_contents, function(btnPage){
		if( !_.includes(btnPage.classList, "-btx") ) {
			var tds = btnPage.querySelectorAll("tr td");
			if( tds && tds.length > 0 ) {
				var div = document.createElement("div");
				div.className += "-btx btn-group d-flex flex-wrap";
				_.each(tds, function(td) {
					while( td.childNodes.length > 0 ) {
						div.appendChild(td.childNodes[0]);
					}
				});
				tds[0].appendChild(div);
			}
		}
	});
};

Bts.transformContentGroupToCardBody = function( list_content ) {
	$(list_content).children().wrapAll("<div class='-btx -btx-list-content card-body px-0 py-1'>");
};

Bts.transformListContentBottom = function( list_content ) {
	var list_bottoms = list_content.querySelectorAll("table.list_content_bottom");
	_.each(list_bottoms, function(list_bottom){
		var trs = list_bottom.querySelectorAll("tr");
		_.each(trs, function(tr){
			tr.className += " -btx d-flex flex-column flex-sm-row align-items-center justify-content-between";
			var bottoms = list_bottom.querySelectorAll("td.list_content_bottom");
			_.each(bottoms, function(bottom){
				bottom.className += " -btx p-1 flex-fill";
			});
		});
	});
};

Bts.transformListContentTop = function( list_content ) {
	var topWrap = list_content.querySelector(".-btx-list-content-top");
	if( topWrap ) {
		// console.log(list_content.id+"[TRACE] transformListContentTop already exists.", topWrap);
		return;
	}
	//
	var $list_tops = $("table.list_content_top", list_content);
	$list_tops.css("position", "relative");
	var $spans = $list_tops.wrap("<span class='-btx p-1 flex-fill'>").parent();
	$spans.wrapAll("<div class='-btx -btx-list-content-top d-flex flex-wrap'>");
	//
	_.each($list_tops, function(list_top, top_i){
		var tds = list_top.querySelectorAll("td");
		// console.log(list_content.id+"[TRACE] trancontop: tds:", tds);
		if( tds && tds.length > 0 ) {
			var span = document.createElement("span");
			span.className += " -btx d-flex flex-column flex-sm-row flex-wrap align-middle";
			_.each(tds, function(td){
				if( "left" == td.getAttribute("align") ) {
					span.className += " justify-content-start";
				} else if( "right" == td.getAttribute("align") ) {
					span.className += " justify-content-end";
				} else if( "center" == td.getAttribute("align") ) {
					span.className += " justify-content-center";
				}
				_.each(td.childNodes, function(child){
					if( child instanceof HTMLSelectElement ) {
						var $toAdd = $(child).wrap("<span class='-btx p-0'>").parent();
						span.appendChild($toAdd[0]);
					} else if( child instanceof HTMLSpanElement ) {
						child.className += " -btx p-0";
						// child.classList.toggle("flex-fill", true);// flex-fill may not need in content top
						span.appendChild(child);
					}
				});
			});
			tds[0].appendChild(span);
		}
	});
	return;
};

Bts.getModalTemplate = function( model ) {
	function randomIntFromInterval(min, max) { // min and max included
		return Math.floor(Math.random() * (max - min + 1) + min);
	}
	var DEFAULT_MODEL = {modal: {id: "modal_"+randomIntFromInterval(2000, 3000),
								 title: "",
								 label: {
									 close: ""
								 },
								 classData: ""
								}
						};
	model = model||{};
	model.modal = _.extend(DEFAULT_MODEL.modal, (model && model.modal));
	return _.template(
		"<div class='modal fade <%=modal.classData%>' id='<%=modal.id%>' tabindex='-1' role='dialog' aria-hidden='true'>"
			+ " <div class='modal-dialog modal-dialog-centered' role='document'>"
			+ "	 <div class='modal-content'>"
			+ "	  <div class='modal-header'>"
			+ "	   <h5 class='modal-title' id='<%=modal.id%>_title'><%=modal.title%></h5>"
			+ "	   <button type='button' class='close' data-dismiss='modal' aria-label='<%=modal.label.close%>'>"
			+ "		<span aria-hidden='true'>&times;</span>"
			+ "	   </button>"
			+ "	  </div>"
			+ "	  <div class='modal-body' id='<%=modal.id%>_body'>"
			+ "	  </div>"
		// + "	 <div class='modal-footer'>"
		// + "	  <button type='button' class='btn btn-secondary' data-dismiss='modal'><%=modal.label.close%></button>"
		// + "	  <button type='button' class='btn btn-primary' data-dismiss='modal'><%=modal.label.save%></button>"
		// + "	 </div>"
			+ "	 </div>"
			+ " </div>"
			+ "</div>"
	)(model);
};

function transformAllContentGroup() {
	$(".content_group").each(function(){
		function getContentType( el, contentTypeRefer ) {
			var contentTypeClass = _.find( el.classList, function(className){
				return className.indexOf( "_"+ contentTypeRefer ) >= 0;
			});
			return ( contentTypeClass ? contentTypeClass.split("_")[0] : undefined);
		};

		Bts.transformContentGroup( this );
	});
};
