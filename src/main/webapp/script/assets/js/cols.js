/*
	File Name:	cols.js
	Version:	2.2.0

	Description:

	Note:
  		ColList
		ColObj
		Col

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/03/30		2.2.0	create
*/

/*
   checkbox sort ordering ( checked or not checked )
*/
$.fn.dataTable.ext.order['dom-checkbox'] = function( settings, col ) {
	return this.api().column( col, {order:'index'} ).nodes().map( function ( td, i ) {
		return $('input', td).prop('checked') ? '1' : '0';
	} );
};


function ColList( div_list_content ) {
	this.element = div_list_content;
	this.dataTable = null;
	this.dataTableIndex = -1;
	this.tbody = this.element.querySelector("table.list_content_data tbody");
	this.thead = this.element.querySelector("table.list_content_header tbody");
	if( !this.tbody ) {
		this.tbody = this.element.querySelector("table.list_content_header tbody");
	}
	if( !this.thead ) {
		this.thead = this.element.querySelector("table.list_content_header thead");
	}
	if( !this.thead ) {
		console.log("[ERROR] thead mandatory:"+this.element.id, this.thead, this.tbody);
		return;
	}
	var tr_top = this.thead.querySelector("tr");
	if( !tr_top ) {
		console.log("[ERROR] tr_top first mandatory:"+this.element.id, this.thead);
		return;
	}
	this.colObj = new ColObj(tr_top);
	if( !this.colObj ) {
		console.log("[ERROR] colObj create failed:"+this.element.id, tr_top);
		return;
	}
	if( !this.colObj.cols ) {
		console.log("[ERROR] colObj cols list mandatory:"+this.element.id, this.colObj);
		return;
	}
	if( !this.thead ) {
		console.log("[ERROR] thead mandatory:"+this.element.id, this.element);
		return;
	}
	if( !this.tbody ) {
		console.log("[ERROR] tbody mandatory:"+this.element.id, this.element);
		return;
	}
	if( this.thead && this.thead.querySelectorAll("th").length > 0 ) {//wait
		var checkExists = setInterval(function(){
			if( (this.colObj && this.colObj.cols) ) {
				clearInterval(checkExists);
			}
		}, 100);
	}
	var colList = this;
	this.hasDataLine = function() {
		return !(colList.tbody.querySelector("td[data-has-data-line=false]"));
	};
	this.initSyncScrollY();
	this.element.colList = this;
	ColList.colLists[ColList.colLists.length] = this;
};
ColList.colLists = new Array;
ColList.findList = function( qlst_or_el ) {
	var lst = (_.isString(qlst_or_el)
			   ? document.querySelector( (qlst_or_el.match(/^#/) ? qlst_or_el : ("#"+qlst_or_el)) )
			   : qlst_or_el );
	return (lst
			? _.chain(ColList.colLists)
			.filter(function(colList) {
				return colList.element === lst;
			})
			.first().value()
			: null);
};
ColList._getFontClassName = function( fontSizePt ) {
	if( fontSizePt > 32 ) {
		return "font-zl";
	} else if( fontSizePt > 24 ) {
		return "font-xl";
	} else if( fontSizePt > 18 ) {
		return "font-lg";
	} else if( fontSizePt > 16 ) {
		return "font-md";
	} else if( fontSizePt > 13 ) {
		return "font-sm";
	} else if( fontSizePt > 10 ) {
		return "font-xs";
	} else {
		return "font-zs";
	}
};
ColList._getFontClassSize = function( fontClassName ) {
	var fontSizePt = 8;
	switch( fontClassName ) {
	case "font-zl":
		fontSizePt = 32;
	case "font-xl":
		fontSizePt = 24;
	case "font-lg":
		fontSizePt = 18;
	case "font-md":
		fontSizePt = 16;
	case "font-sm":
		fontSizePt = 13;
	case "font-xs":
		fontSizePt = 10;
	case "font-zs":
		fontSizePt = 8;
	}
	return fontSizePt;
};
ColList._getOtherHeight = function( el ) {
	var $obj = $(el);
	var heights = $.merge( $obj.prevAll(), $obj.nextAll() )
		.map(function(i,o){ return( Util.getOffsetHeight(this) > 0 ? $(this).height() : 0 ); });
	return _.reduce(heights, function(memo,v){return memo += v;}, 0);
};


ColList.prototype.getTrFirst = function() {
	return this.tbody.querySelector("tr");
};
ColList.prototype.getTdWidths = function() {
	return _.map($(this.tbody.querySelector("tr")).find("td"), function(td){
		return Util.getOffsetWidth(td);
	});
};
ColList.prototype.getThWidths = function() {
	return _.map(this.colObj.cols, function(col){
		return Util.getOffsetWidth(col.element);
	});
};
ColList.prototype.getTbodyColgroupWidths = function() {
	return _.map($(this.tbody.parentElement.querySelector("colgroup")).find("col"), function(colel){
		return ( colel && colel.style.display == "none" ? 0 : parseInt($(colel).css("width")) );
	});
};
ColList.prototype.getTheadColgroupWidths = function() {
	return _.map($(this.thead.parentElement.querySelector("colgroup")).find("col"), function(colel){
		return ( colel && colel.style.display == "none" ? 0 : parseInt($(colel).css("width")) );
	});
};
ColList.prototype.setFontClass = function( fontClass ) {
	var prevFontClass;
	_.each(this.element.classList, function(className){
		if( className.match(/^font-(zs|xs|sm|md|lg|xl|zl)$/) ) {
			prevFontClass = className;
		}
	});
	// console.log(this.element.id+"[TRACE] prevClasses:", this.element.className);
	if( fontClass === null ) {
		this.element.classList.toggle(prevFontClass, false);
		return prevFontClass;
	} else if( fontClass === undefined ) {
		fontClass = this.calcFontClass();
	}
	if( prevFontClass !== fontClass ) {
		_.each(this.element.classList, function(className){
			if( className && className.match(/^font-(zs|xs|sm|md|lg|xl|zl)$/) ) {
				this.element.classList.toggle(className, false);
			}
		}, this);
		this.element.classList.toggle(fontClass, true);
	}
	// console.log(this.element.id+"[TRACE] currClasses:", this.element.className);
	return prevFontClass;
};
ColList.prototype.getWidths = function() {
	var ret = {colel: this.thead.parentElement.querySelectorAll("colgroup col"),
			   thW: this.getThWidths(),
			   tdW: this.getTdWidths(),
			   thcW: this.getTheadColgroupWidths(),
			   tbcW: this.getTbodyColgroupWidths(),
			   theadW: this.thead.offsetWidth,
			   tbodyW: this.tbody.offsetWidth,
			  };
	var sum = {};
	_.each(ret, function(v,k){
		sum[k] = _.reduce(v,function(memo,w){ return memo+parseInt(w);},0);
	});
	return _.extend(ret, {sum: sum});
};
ColList.prototype.focusLeft = function() {
	var tbodyScroll = this.tbody.closest("div[class*=list_content_][class*=_scroll]");
	if( tbodyScroll ) {
		tbodyScroll.scrollLeft = 0;
	}
};
ColList.prototype.focusRight = function() {
	var tbodyScroll = this.tbody.closest("div[class*=list_content_][class*=_scroll]");
	if( tbodyScroll ) {
		tbodyScroll.scrollLeft = tbodyScroll.scrollWidth - tbodyScroll.offsetWidth;
	}
};
ColList.prototype.fitRemoveWidth = function() {
	//remove fit check data
	this.element.setAttribute("data-win-client-w", "");
	return this;
};
ColList.prototype.fitWidth = function( fontClass ) {
	if( !this.element || !this.thead || !this.tbody || !$(this.tbody).is(":visible") )  {
		console.log("[WARN]"+ this.element.id +" fitWidth() canceled due to not able to fit width.");
		return;
	}
	var dataWinClient = this.element.getAttribute("data-win-client-w");
	if( !fontClass && dataWinClient == (window.innerWidth||window.document.clientWidth) ) {
		console.log("[WARN]"+ this.element.id +" fitWidth() canceled due to already set");
		return;
	}
	var tdFirst = this.tbody.querySelector("tr td");
	var trFirst = tdFirst && tdFirst.parentElement;
	var theadColgroup = this.thead.parentElement.querySelector("colgroup");
	var tbodyColgroup = this.tbody.parentElement.querySelector("colgroup");
	//
	var colList = this;
	colList.element.classList.toggle("ing-width", true);
	colList.thead.parentElement.style.tableLayout = "auto";
	if( colList.thead !== colList.tbody )
		colList.tbody.parentElement.style.tableLayout = "auto";
	fontClass = fontClass || colList.calcFontClass();
	var fontSize = ColList._getFontClassSize( fontClass );
	colList.setFontClass( fontClass );
	var tableWidth = Util.getOffsetWidth(this.tbody.parentElement);
	var wasTheadW = this.thead.offsetWidth;
	var wasTbodyW = this.tbody.offsetWidth;
	var wasTdWidths = [];
	var wasThWidths = [];
	var wasTbodyW2;
	var fontSpec;
	var widths = [];
	var colDefWidths = [];
	var thMinWidths = [];
	var descColIndex;
	_.each(colList.colObj.cols, function(col, i){
		var td = (colList.hasDataLine() == true ? trFirst.children[i] : col.element);
		var tdW = Util.getOffsetWidth(td);
		var thW = Util.getOffsetWidth(col.element);
		wasTdWidths.push(tdW);
		wasThWidths.push(thW);
		if( !fontSpec ) {
			fontSpec = Util.getElementFontSpec(col.element);
		}
		var thMinWidth = Math.round(col.calcMinWidth(fontSpec))||0;
		var colDefWidth = parseInt(col.element.getAttribute("width")||0)/11*fontSize;
		var width = _.max([_.max([tdW, colDefWidth]), thMinWidth]);
		if( _.includes(col.element.classList, "description") ) {
			descColIndex = col.index;
		}
		widths.push(width);
		colDefWidths.push(colDefWidth);
		thMinWidths.push(thMinWidth);
		col.element.setAttribute("data-col-min-w", thMinWidth);
		col.element.setAttribute("data-col-def-w", colDefWidth);
		col.element.setAttribute("data-col-td-w", tdW);

		if( colList.colObj.cols.length-1 == i ) {
			wasTbodyW2 = colList.tbody.offsetWidth;
		}
	}, colList);
	//before rendering
	var sumColW = _.reduce(widths,function(memo,w){return memo+w;},0);//if nowrap line width
	colList.element.setAttribute("data-was-sum-col-w", sumColW);
	colList.element.setAttribute("data-was-win-client-w", window.innerHeight||window.document.clientHeight);
	colList.element.setAttribute("data-was-me-scroll-w", colList.element.scrollWidth);
	colList.element.setAttribute("data-was-tbody-scroll-w", colList.tbody.scrollWidth);
	colList.element.setAttribute("data-was-thead-scroll-w", colList.thead.scrollWidth);
	colList.element.setAttribute("data-was-tbody-offset-w", Util.getOffsetWidth(colList.tbody));
	colList.element.setAttribute("data-was-tbody-offset-h", Util.getOffsetHeight(colList.tbody));
	//
	if( !_.isEmpty(colDefWidths) && !_.isEmpty(widths) ) {
		var needsListWrap = colList.element.scrollWidth <= sumColW;
		// console.log(colList.element.id+"[TRACE] fontSpec:"+ fontSpec + " ofsW:"+colList.element.offsetWidth+ " sclW:"+colList.element.scrollWidth + " sumColW:"+sumColW);
		_.each(colList.colObj.cols, function(col, i){
			var width = widths[i];
			var thMinWidth = thMinWidths[i];
			if( needsListWrap && colDefWidths[i] && width > colDefWidths[i] ) {//needs wrap
				width = colDefWidths[i];
				var tds = colList.getTdArray(col.getFieldKey());
				_.each(tds, function(td){
					td.classList.toggle("col-text-wrap", true);
				});
			}
			col.element.setAttribute("data-col-w", width);
			col.updateMinWidth(thMinWidth, colList).updateWidth(Math.round(width), colList);
			if( col.isDispNone() ) {
				theadColgroup.children[i].style.display = "none";
				if( theadColgroup !== tbodyColgroup ) {
					tbodyColgroup.children[i].style.display = "none";
				}
			}
		});
		if( needsListWrap && descColIndex ) {
			var descCol = colList.colObj.cols[descColIndex];
			if( descCol ) {
				var currSumW = _.reduce(widths,function(memo,w, i){ return memo + (i == descColIndex?0:w);},0);
				widths[descColIndex] = sumColW - currSumW;
				descCol.element.setAttribute("data-col-w", widths[descColIndex]);
				descCol.updateWidth(Math.round(widths[descColIndex]));
			}
		}
	}
	if( colList.hasDataLine() == false ) {
		var lstmsg = colList.tbody.querySelector("td[data-has-data-line]");
		if( lstmsg ) {
			lstmsg.setAttribute("colspan", _.filter(tbodyColgroup.children, function(colel){
				return colel.style.display !== "none";
			}).length);
		}
	}
	//invoke fixed render
	colList.thead.parentElement.style.tableLayout = "fixed";
	if( colList.thead !== colList.tbody )
		colList.tbody.parentElement.style.tableLayout = "fixed";
	colList.element.classList.toggle("ing-width", false);
	// scroll
	var scrolls = this.getScrolls();
	if( scrolls && scrolls.tbodyScroll && scrolls.theadScroll ) {
		if( scrolls.theadScroll && scrolls.theadScroll.style.overflowX != "hidden" ) {
			scrolls.theadScroll.style.overflowX = "hidden";
			console.log(this.element.id+"[TRACE] overflow theadScrollX now :"+ scrolls.theadScroll.style.overflowX);
		}
		if( scrolls.theadScroll.style.overflowY != scrolls.tbodyScroll.style.overflowY ) {
			scrolls.theadScroll.style.overflowY = scrolls.tbodyScroll.style.overflowY;
		}
	}
	//
	colList.element.setAttribute("data-win-client-w", dataWinClient = window.innerHeight||window.document.clientHeight);
	colList.element.setAttribute("data-me-scroll-w", colList.element.scrollWidth);
	colList.element.setAttribute("data-tbody-scroll-w", colList.tbody.scrollWidth);
	colList.element.setAttribute("data-thead-scroll-w", colList.thead.scrollWidth);
	colList.element.setAttribute("data-tbody-offset-w", Util.getOffsetWidth(colList.tbody));
	colList.element.setAttribute("data-tbody-offset-h", Util.getOffsetHeight(colList.tbody));
	// invoke fitHeightToWindow
	if( colList.tbody.parentElement.closest("div[class*=list_content_][class*=_scroll]") ) {
		var tbodyScroll = colList.tbody.parentElement.closest("div[class*=list_content_][class*=_scroll]");
		if( tbodyScroll ) {
			// var bottomOffset = $(".footer").offset().top;
			// var scrollHeight = tbodyScroll.scrollHeight;
			// var offsetHeight = tbodyScroll.offsetHeight;
			// var otherHeight = colList.getTbodyOtherH();
			// var availHeight = colList.getAvailHeight();
			// var accdOffsets = $(".content_group_accd,.footer").map(function(i,o){return $(o).offset();});
			// console.log(colList.element.id+" [TRACE] 1 avl:"+availHeight+" scl:"+scrollHeight+" ofs:"+offsetHeight+" oth:"+otherHeight+" btm:"+bottomOffset, accdOffsets);
			tbodyScroll.style.height = "";
			if( tbodyScroll.ondblclick ) {
				tbodyScroll.ondblclick();
				// console.log(colList.element.id+"[TRACE] ondblclick: ", colList.getScrolls().tbodyScroll);
			} else {
				fitHeightToWindow( tbodyScroll );
				// console.log(colList.element.id+"[TRACE] fithToWin: ", colList.getScrolls().tbodyScroll);
			}
			// bottomOffset = $(".footer").offset().top;
			// scrollHeight = tbodyScroll.scrollHeight;
			// offsetHeight = tbodyScroll.offsetHeight;
			// accdOffsets = $(".content_group_accd,.footer").map(function(i,o){return $(o).offset();});
			// console.log(colList.element.id+" [TRACE] 2 avl:"+availHeight+" scl:"+scrollHeight+" ofs:"+offsetHeight+" oth:"+otherHeight+" btm:"+bottomOffset, accdOffsets);
		} else {
		}
	}
	//
	//
	// console.log("setupColWidth(): colgroup col list:"+this.element.id,
	// 			_.extend(this.getWidths(), {
	// 						 wasThW: wasThWidths,
	// 						 wasTdW: wasTdWidths,
	// 						 wasTheadW: wasTheadW,
	// 						 wasTbodyW: wasTbodyW,
	// 						 wasTbodyW2: wasTbodyW2,
	// 						 widths: widths,
	// 						 thMinWidths: thMinWidths,
	// 					 }));

	return this;
};
ColList.prototype.getTbody = function() {
	var tbody = this.element.querySelector("table.list_content_data tbody");
	if( !tbody )
		tbody = this.element.querySelector("table.list_content_header tbody");
	return tbody;
};
ColList.prototype.removeServerSideHeaderSort = function() {
	var colList = this;
	if( ! colList.colObj ) return;
	_.each(colList.colObj.cols, function(col) {
		var onClick = col.element.getAttribute("onClick");
		if( onClick ) {
			onClick = onClick.replace(/listSort\(\".*\"\)/, "");
			onClick = onClick.replace(/^JavaScript:;$/, "");
			if( onClick )
				col.element.setAttribute("onClick", onClick);
			else
				col.element.removeAttribute("onClick");
		}
		col.element.classList.toggle("header_sort", false);
		var sortImgs = col.element.querySelectorAll("img[alt=sort_none],img[alt=sort_asc],img[sort_desc]");
		_.each(sortImgs, function(img){
			col.element.removeChild(img);
		});
	});
};
ColList.prototype.initDataTableCustomFilters = function() {
	var colList = this;
	if( ! colList ) return;
	var filters = colList.element.querySelectorAll( ".data_table_custom_filter");
	if( !filters ) return;
	_.each(filters, function(filter) {
		var nameAsFieldKey = filter.getAttribute("name");
		var theCol = _.find(colList.colObj.cols, function(col){
			return col.data.xColk == nameAsFieldKey;
		});
		if( theCol ) {
			$(filter).on("change", function() {
				if( filter instanceof HTMLSelectElement ) {
					var search = [];
					_.each(filter.selectedOptions, function(option){
						search.push(option.value);
						search.push(option.text);
					});
					// column().search( input [, regex[ , smart[ , caseInsen ]]] )
					colList.dataTable.table().columns( theCol.index ).search( search.join('|'), true, false ).draw();
				} else {
					colList.dataTable.table().columns( theCol.index ).search( Field.getValue(filter) ).draw();
				}
			});
		}
	});
};
ColList.prototype.initSyncScrollY = function() {
	var theadScroll = this.thead.parentElement.closest("div[class*=list_content_][class*=_scroll],div.dataTables_scrollHeadInner,div.dataTables_scrollHead");
	var tbodyScroll = this.tbody.parentElement.closest("div[class*=list_content_][class*=_scroll],div.dataTables_scrollBody");
	var isTheadSync = false;
	var isTbodySync = false;
	if( this.thead.parentElement !== this.tbody.parentElement ) {
		theadScroll.onscroll = function() {
			if( !isTheadSync ) {
				isTbodySync = true;
				tbodyScroll.scrollLeft = this.scrollLeft;
			}
			isTheadSync = false;
		};
		tbodyScroll.onscroll = function() {
			if( !isTbodySync ) {
				isTheadSync = true;
				theadScroll.scrollLeft = this.scrollLeft;
			}
			isTbodySync = false;
		};
	}
};
ColList.prototype.getCheckboxCol = function() {
	var checkboxCol = _.find(this.colObj.cols, function(col){
		return col.data.checkboxName;
	});
	return checkboxCol;
};
ColList.prototype.getCheckboxName = function() {
	var checkboxCol = this.getCheckboxCol();
	if( checkboxCol )
		return checkboxCol.data.checkboxName;
	return "";
};
ColList.prototype.getCheckboxPrimaryKeys = function() {
	var checkboxCol = this.getCheckboxCol();
	if( checkboxCol )
		return checkboxCol.data.checkboxPrimaryKeys;// as semicolon seperated value
	return "";
};
ColList.prototype.getCheckboxTdTemplate = function( rowindex, primaryValues ) {
	var modelObj = {"model": {"checkboxName": this.getCheckboxName(),
							  "primaryValues": primaryValues || "",
							  "rowindex": rowindex}};
	if( !modelObj || !modelObj.model || _.isUndefined(rowindex) ) {
		throw new Error("model is mandatory");
	}
	if( !this.checkboxTdTemplate ) {
		this.checkboxTdTemplate = _.template(
			"<input type='checkbox' name='<%=model.checkboxName%>' id='<%=model.checkboxName%>_<%=model.rowindex%>' onclick='JavaScript:listClick(this);' value='<%=model.primaryValues%>'>");
	}
	return this.checkboxTdTemplate(modelObj);
};
ColList.prototype.getHideableCols = function() {
	return _.filter((this ? this.colObj.cols : null), function(col) {
		return _.includes(col.element.classList, "d-none");
	});
};
ColList.prototype.calcFontClass = function() {
	var listWidth = Util.getOffsetWidth(this.element);
	var someFontEl = this.tbody.querySelector("td") || this.element;
	var trFirst;
	if( someFontEl !== this.element ) {
		trFirst = this.tbody.querySelector("td").closest("tr");
	}
	var fontFullWidth = Util.getOffsetWidth(trFirst)+10||0; //some more offset for fontFullWidth
	var currFontSize = parseInt($(someFontEl).css("font-size"));
	var newFontSize = currFontSize;
	if( listWidth < fontFullWidth ) {
		newFontSize = currFontSize * listWidth/fontFullWidth;
	} else {
		newFontSize = currFontSize+0.01;// to get correct value
	}
	console.log(this.element.id+"[TRACE]"+" w:"+ listWidth + " full:"+ fontFullWidth + " currFont:"+currFontSize + " newFont:"+newFontSize, someFontEl);
	return ColList._getFontClassName(newFontSize);
};
ColList.prototype.getScrolls = function() {
	return {
		"theadScroll": this.thead.parentElement.closest("div[class*=list_content_][class*=_scroll],div[class*=dataTables_scrollHead]"),
		"tbodyScroll": this.tbody.parentElement.closest("div[class*=list_content_][class*=_scroll],div[class*=dataTables_scrollBody]"),
	};
};

ColList.prototype.toDataTable = function( options ) {
	var hd_tbl = this.element.querySelector("table.list_content_header");
    if( !hd_tbl ) return;
	var htmlLang = document.querySelector("html").getAttribute("data-lang")||"";
	var i18nUrl = ( (!htmlLang || htmlLang=="en") ? "" : "script/jqdt/i18n/"+htmlLang+".json");
	var DEFAULT_OPTIONS = {
		"language": {"url": i18nUrl},
		"lengthMenu": [[10,25,50,100,-1],[10,25,50,100,"All"]],
		"pageLength": 10,
		"scrollX": true,
		"scrollY": "auto",
		"scrollCollapse": true,
		"saveState": true,
		"dom": "<'row'<'col-6 col-md-4'l><'col-6 col-md-4'f>><'row'<'col-sm-12'tr>><'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
	};
	//
	var opts = _.extend({}, DEFAULT_OPTIONS, options);
	var hd_div = this.element.querySelector("div.list_content_header_scroll,div.list_content_header");
	if( this.thead.parentElement === this.tbody.parentElement ) {
		if( this.thead !== this.tbody.parentElement.querySelector("thead") ) {
			this.thead = this.tbody.parentElement.querySelector("thead");
		}
	}
	var colList = this;
	colList.removeServerSideHeaderSort();
	this.dataTable = $(hd_tbl).DataTable(opts);
	if( !this.dataTable ) {
		console.log("[ERROR] failed to create dataTable");
	}
	var checkExists = setInterval(function(){
		if( colList.dataTable.table().header() != colList.dataTable.table().body() ) {
			var dt_top_row = colList.element.querySelector(".dataTables_wrapper .row");
			if( dt_top_row ) {
				colList.doDataTable();
				clearInterval(checkExists);
			}
		}
	}, 100);
	//
	return this.dataTable;
};
ColList.prototype.fitDataTableRemoveWidth = function() {
	var $dtScroll = $(this.element).find(".dataTables_scroll");
	var dts = this.element.querySelectorAll(".dataTable");
	var colList = this;
	if( $dtScroll.length > 0 && dts && dts.length > 0 ) {
		_.each(dts, function(dt){
			var $dt = $(dt);
			$dt.css("width", "");
			var $dtScroll = $dt.closest(".dataTables_scrollHead,.dataTables_scrollBody");
			$dtScroll.find(".dataTables_scrollHeadInner").css("width", "");
			$dtScroll.css("width", "");
		});
	}
	return this;
};
ColList.prototype.doDataTable = function() {
	if( !this.dataTable ) {
		console.log("[WARN] dataTable needs to be setup by toDatatable() function.");
		return;
	}
	$(".dataTables_filter input[type=search].form-control", this.element).addClass("w-75");
	var list_function = this.element.querySelector("table.list_content_function");
	if( list_function ) {
		var dt_top_row = this.element.querySelector(".dataTables_wrapper .row");
		if( dt_top_row ) {
			var dt_top_right_buttons = document.createElement("div");
			dt_top_right_buttons.classList.toggle("-btx-dt-button", true);
			dt_top_right_buttons.classList.toggle("col-sm-12", true);
			dt_top_right_buttons.classList.toggle("col-md-4", true);
			dt_top_right_buttons.setAttribute("role", "group");
			dt_top_right_buttons.appendChild(list_function);
			dt_top_row.appendChild(dt_top_right_buttons);
		} else {
			console.log(this.element.id+"[WARN] cannot found dataTables_wrapper .row");
		}
	} else {
		console.log(this.element.id+"[WARN] cannot found list_content_function");
	}
	this.initDataTableCustomFilters();
	if( this.dataTable.table().header().parentElement ) {
		var tbodyColgroup = this.dataTable.table().body().parentElement.querySelector("colgroup").cloneNode(true);
		if( tbodyColgroup ) {
			$(this.dataTable.table().header().parentElement).append( tbodyColgroup );
		} else {
			console.log(this.element.id+"[WARN] cannot find tbody colgroup");
		}
	} else {
		console.log(this.element.id+"[WARN] cannot find table().header().parentElement");
	}
	//
	var dtPad = 32;
	var widths = [];
	var tbodyColgroupCols = this.tbody.parentElement.querySelectorAll("colgroup col");
	if( tbodyColgroupCols.length == this.colObj.cols.length ) {
		_.each(this.colObj.cols, function(col, i) {
			if( col ) {
				var th = col.element;
				var colel = tbodyColgroupCols[i];
				if( colel.style.width ) {
					var w = parseInt(colel.style.width||0) + dtPad;
					colel.style.width = w;
					widths.push( w );
				}
			}
			col.setDispNoneToTd();
		});
		var theadColgroupCols = this.dataTable.table().header().parentElement.querySelectorAll("colgroup col");
		if( theadColgroupCols ) {
			this.colObj.syncWidths( tbodyColgroupCols, theadColgroupCols );
		} else {
			console.log("[WARN] theadColgroupCols not found");
		}
	} else {
		console.log("[WARN] tbodyColgroupCols not equals colObj.cols:", tbodyColgroupCols, this.colObj.cols);
	}
	this.initSyncScrollY();
	this.fitRemoveWidth().fitWidth().fitDataTableRemoveWidth();
};


ColList.prototype.getCol = function( fieldKey, fieldKeyN ) {
	fieldKeyN = fieldKeyN || 0;
	if( !_.isString(fieldKey) )
		throw new Error("fieldKey is mandatory as string.");
	var fieldKeyCols = _.filter(this.colObj.cols, function(col) {
		return col.getFieldKey() == fieldKey;
	});
	return fieldKeyCols ? fieldKeyCols[fieldKeyN] : {};

};
ColList.prototype.getTdArray = function( fieldKey, fieldKeyN ) {
	var theCol = this.getCol( fieldKey, fieldKeyN );
	if( theCol && !_.isEmpty(theCol) && this.hasDataLine() == true ) {
		return _.map(this.tbody.children, function(tr) {
			return tr.querySelectorAll("td")[theCol.index];
		});
	}
	return [];
};

ColList.prototype.isDataTable = function() {
	var hd_tbl = this.element.querySelector("table.list_content_header");
	if( ! hd_tbl ) return;
	return ( this.dataTable || _.includes(hd_tbl.classList, "dataTable"));
};
ColList.prototype.getDataTableOptions = function() {
	if( ! this.dataTable ) return {};
	return this.dataTable.settings()[0];
};
ColList.prototype.destroyDataTable = function() {
	if( ! this.dataTable ) return;
	return this.dataTable.fnDestroy();
};


function setupColList( list_content ) {
	if( list_content ) {
		if( !list_content.colList ) {
			var colList = new ColList(list_content);
			if( colList ) colList.fitWidth();
		}
	} else {
		var lists = document.querySelectorAll("div.list_content");
		_.each(lists, function(list){
			setupColList(list);
		});
	}
};


function ColObj( tr_top ) {
	if( !tr_top instanceof HTMLTableRowElement )
		throw new Error("tr is mandatory");
	var element = tr_top;
	this.element = element;
	this.rowIndex = element.rowIndex;
	var coli = 0;
	this.cols = [];
	var makedCols = this.makeCols();
	this.groupedCols = makedCols.gCols;
	this.cols = makedCols.cols;
	//
	this.colObservers = [];
	if( window.ComputedStyleObserver ) {
		this.colObservers.push(
			new ComputedStyleObserver(function(entries){
				entries.forEach(function(entry){
					var th = entry.target;
					var thCol = th.col;
					var groupParent = th.col.groupParent;
					var willHide = entry.value === "none";
					if( thCol ) {
						thCol.setDispNone( willHide );
					}
					// console.log(thCol.data.xColk+"[TRACE] Property "+ entry.property+" changed from "+entry.previousValue+" to "+entry.value);
				});
			}, ['display'])
		);
	}
	var colObj = this;
	// console.log(this.element.closest("div.list_content").id+" [TRACE] colObs:", this.colObservers);
	_.each(this.cols, function(myCol){
		_.each(colObj.colObservers, function(colObser){
			colObser.observe(myCol.element);// observer
		});
		myCol.setDispNone();// init
	});
	//
	return this;
};
ColObj._setDispNoneToGroup = function( groupParent, willHide ) {
	if( groupParent && groupParent.groupCols ) {
		var lst = groupParent.element.closest("div.list_content");
		var groupCols = groupParent.groupCols;
		var hiddenCols = _.filter(groupCols, function(col){
			return col && getComputedStyle(col.element, null).display === "none";
		});
		var prevColspan = parseInt(groupParent.element.getAttribute("colspan")) || groupCols.length;
		var nextColspan = groupCols.length - (hiddenCols ? hiddenCols.length : 0);
		console.log(lst.id+"[TRACE]"+" pv:"+prevColspan+" nv:"+nextColspan+" gp:", groupParent);
		groupParent.element.setAttribute("colspan", nextColspan);
		if( nextColspan > 0 && _.includes(groupParent.element.classList, "d-none") ) {
			groupParent.element.classList.toggle("d-none", false);
		} else if( nextColspan === 0 && !_.includes(groupParent.element.classList, "d-none") ) {
			groupParent.element.classList.toggle("d-none", true);
		}

	}
};
ColObj.prototype.getWidths = function( els ) {
	return _.map(els, function(el){
		if( el ) {
			if( el instanceof Col ) {
				return $(el.element).width();
			} else {
				return ( el.style.display == "none" ? 0 : $(el).width());
			}
		}
		return -1;
	});
};
ColObj.prototype.removeWidths = function( els ) {
	return _.map(els, function(el){
		if( el ) {
			if( el instanceof Col ) {
				return $(el.element).css("width","");
			} else {
				return $(el).css("width","");
			}
		}
		return -1;
	});
};
ColObj.prototype.setWidths = function( els, callback ) {
	return _.map(els, function(el, i){
		if( el ) {
			if( el instanceof Col ) {
				el = el.element;
			}
		}
		if( el ) {
			var w = callback(el, i);
			if( w )
				el.style.width = w;
		}
		return el;
	});
};
ColObj.prototype.syncWidths = function( fromArr, tgtArr ) {
	var fromWidths = this.getWidths( fromArr );
	this.setWidths( tgtArr, function(el, i){
		return fromWidths[i];
	});
};
ColObj.prototype.makeCols = function() {
	var tr_top = this.element;
	var lst = this.element.closest("div.list_content");
	var colgroup = tr_top.parentElement.closest("table").querySelector("colgroup");
	var ths = tr_top.parentElement.querySelectorAll("th");
	var currGkey;
	var stayIndex;
	var deferIndex = 0;
	var calced = _.reduce(ths, function(memo, th, i){
		var col;
		var colk = th.getAttribute("data-x-colk");
		var gcolk = th.getAttribute("data-x-colgk") ;
		if( colk ) {
			col = new Col(th, tr_top);
			if( gcolk ) {
				memo.gkeyToCindexes[gcolk] = memo.gkeyToCindexes[gcolk]||[];
				memo.gkeyToCindexes[gcolk].push( memo.cols.length );
				var gcol = memo.gkeyToGcol[gcolk];
				gcol.groupCols = gcol.groupCols || [];
				col.groupParent = memo.gkeyToGcol[gcolk];
				gcol.groupCols.push( col );
				col.groupParent = gcol;
				stayIndex = (stayIndex ? stayIndex : gcol.element.cellIndex);
				col.index = stayIndex++;
			} else {
				col.index = deferIndex++;
			}
			col.element.setAttribute("data-x-coli", col.index);
			memo.cols.splice(col.index, 0, col);
		} else if( gcolk ) {
			col = new Col(th, tr_top);
			if( currGkey !== gcolk ) {
				deferIndex = deferIndex + parseInt(col.data.xColgs);
			}
			currGkey = gcolk;
			if( col ) {
				col.index = memo.gCols.length;
				memo.gkeyToGcol[gcolk] = col;
				memo.gCols.push( col );
			}
		} else {
			console.log(lst.id+"[WARN] "+ th.textContent + " : something wrong cannot determine col th:", th, colgroup );
			col = new Col(th, tr_top);
			col.index = deferIndex++;
			col.element.setAttribute("data-x-coli", col.index);
			col.element.setAttribute("data-x-colk", colgroup.children[col.index].getAttribute("data-col-key"));
			memo.cols.splice(col.index, 0, col);
		}
		return memo;
	}, {cols:[], gCols:[], gkeyToGcol:{}, gkeyToCindexes:{}, coli:0});
	return calced;
};
ColObj.prototype.findGroupedCol = function( groupKey ) {
	if( _.isEmpty(this.groupedCols) )
		throw new Error("cg empty. cannot find");
	if( _.isEmpty(this.groupedCols) )
		return null;
	return _.chain(this.groupedCols)
		.filter(function(cg){
			var gk = cg.data.xColgk;
			return _.isEqual(gk, groupKey);
		})
		.first()
		.value();
};



function Col( th, parent ) {
	var element = th;
	this.element = element;
	this.element.col = this;
	this.level = element.rowSpan || 1;
	this.index = null;
	this.data = this.element.dataset;
	this.groupParent = null;
	this.groupCols = null;
	//
	return this;
};
/* updateSTyle across thead and tbody */
Col._updateStyle = function( col, styleName, styleValue, colList ) {
	styleValue = styleValue ||"";
	colList = colList || (col.element.closest("div.list_content") && col.element.closest("div.list_content").colList);
	if( colList ) {
		var theadColgroupCols = colList.thead.parentElement.querySelectorAll("colgroup col");
		var tbodyColgroupCols = colList.tbody.parentElement.querySelectorAll("colgroup col");
		if( theadColgroupCols ) {
			col.element.style[styleName] = styleValue;
			if( theadColgroupCols[col.index] ) {
				theadColgroupCols[col.index].style[styleName]= styleValue;
			} else {
				console.log(colList.element.id+"[WARN]"+" thead colel not found", theadColgroupCols);
			}
			if( colList.thead.parentElement !== colList.tbody.parentElement ) {
				if( tbodyColgroupCols[col.index] ) {
					tbodyColgroupCols[col.index].style[styleName]= styleValue;
				} else {
					console.log(colList.element.id+"[WARN]"+" tbody colel not found", tbodyColgroupCols);
				}
			}
		}

		// var maybeTbodyHiddenHead = colList.tbody.parentElement.querySelector("thead");
	}
};


Col.prototype.setDispNone = function( willHide ) {
	willHide = willHide || this.isDispNone();
	var lst = this.element.closest("div.list_content");
	var colList = lst && lst.colList;
	if( willHide ) {
		if( colList ) {
			$(colList.thead.parentElement).find("colgroup col").eq(this.index).css("display", "none").css("width", "");
			if( colList.tbody.parentElement && colList.thead.parentElement !== colList.tbody.parentElement )
				$(colList.tbody.parentElement).find("colgroup col").eq(this.index).css("display", "none").css("width", "");
		}
	} else {
		if( colList ) {
			$(colList.thead.parentElement).find("colgroup col").eq(this.index).css("display", "").css("width", "auto");
			if( colList.tbody.parentElement && colList.thead.parentElement !== colList.tbody.parentElement )
				$(colList.tbody.parentElement).find("colgroup col").eq(this.index).css("display", "").css("width", "auto");
		}
	}
	if( this.hasDispNoneBase() ) {
		if( colList && colList.dataTable )
			colList.dataTable.columns(this.index).visible(!willHide);
		if( this.groupParent )
			ColObj._setDispNoneToGroup( this.groupParent, willHide );
	}
};
Col.prototype.setDispNoneToTd = function() {
	var dispNoneClass = this.getDispNoneClass();
	var lst = this.element.closest("div.list_content");
	if( dispNoneClass ) {
		var colList = lst && lst.colList;
		if( colList ) {
			var tds = colList.getTdArray( this.getFieldKey() );
			if( tds && tds.length > 0 ) {
				_.each(tds, function(td){
					td.classList.toggle(dispNoneClass, true);
				});
			}
		}
	}
};
Col.prototype.toString = function() {
	return 'l:' +this.level
		+ " i:" + this.index
		+ " p:"+ (this.groupParent ? this.groupParent.data.xColgk : this.groupParent )
		+ " data:" + JSON.stringify(this.data);
};
Col.prototype.data = function() {
	return JSON.stringify(this.data);
};
Col.prototype.isGroupedCol = function() {
	return this.groupCols && this.groupCols.length > 0;
};
Col.prototype.getDispNoneClass = function() {
	if( _.includes(this.element.classList, "d-none") ) {
		var classMatches = /d-(sm|md|lg|xl)-table-cell/.exec(this.element.className);
		if( classMatches && classMatches.index > 1 ) {
			return "d-none" + (" "+classMatches[0]||"");
		}
		return "d-none";
	}
	return "";
};
Col.prototype.isDispNone = function() {
	var isHidden = false;
	if( this.hasDispNoneBase() ) {
		isHidden = true;
		if( this.element.className.match(/d-(sm|md|lg|xl)-table-cell/)
			&& getComputedStyle( this.element, null ).display !== "none" ) {
			isHidden = false;
		}
	}
	return isHidden;
};
Col.prototype.hasDispNoneBase = function() {
	var hasHidden = false;
	if( _.includes(this.element.classList, "d-none") ) {
		hasHidden = true;
	}
	return hasHidden;
};
Col.prototype.focus = function() {
	var col = this;
	col.element.scrollIntoView();
};
Col.prototype.calcMinWidth = function( fontSpec ) {
	var getMaxWord = function( sentence ) {
		return _.max((sentence ? sentence.trim().split(/\s/) : undefined), function(str){
			return str.length;
		});
	};
	var getMinWidth = function( col, fontSpec ) {
		fontSpec = fontSpec || Util.getElementFontSpec( col.element );
		var maxWord = getMaxWord( col.element.textContent );
		if( maxWord && maxWord.length > 0 ) {
			var textW = Util.getTextWidth(fontSpec, maxWord);
			var borderW = + parseFloat($(col.element).css("border-left-width"))
				+ parseFloat($(col.element).css("border-right-width"));
			// console.log("col min width:"+ col.data.xColk + " textW: " + textW + " borderW: "+borderW);
			return (textW + borderW);
		} else {
			return undefined;
		}
	};
	return getMinWidth( this, (fontSpec || Util.getElementFontSpec(this.element)));
};
Col.prototype.updateWidth = function( width, colList ) {
	if( parseInt(this.element.style.width) != width )
		Col._updateStyle( this, "width", width, colList );
	return this;
};
Col.prototype.updateMinWidth = function( minWidth, colList ) {
	if( parseInt(this.element.style.minWidth) != minWidth )
		Col._updateStyle( this, "minWidth", minWidth, colList );
	return this;
};


/*
  example:
 $(document).on('ready', function(){
 		setupColUsrWidthObserver();
 });
 */
function setupColUsrWidthObserver() {
	var thUserWidthObserver = new MutationObserver(function(mutations){
		mutations.forEach(function(mutation){
			if( mutation.type == "attributes" && mutation.attributeName == "data-col-usr-w" ) {
				var newValue = mutation.target.getAttribute("data-col-usr-w");
				var mycol = mutation.target.col;
				var lst = mutation.target.closest("div.list_content");
				if( !_.includes(lst.classList,"ing-width") ) {
					mycol.updateWidth( newValue );
				}
				// console.log("[TRACE]"+"type:"+mutation.type + " value:"+newValue +"col+mutation->",mycol, mutation);
			}
		});
	});
	var config = { attributes: true };
	document.querySelectorAll("table.list_content_header th").forEach(function(th){
		thUserWidthObserver.observe(th, config);
	});
}


Col.prototype.getFieldKey = function() {
	return (this.data.xColk ? this.data.xColk.split(/\./)[0] : this.data.xColk);
};
