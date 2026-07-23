
function TableEx() {};

TableEx.getHeaderSelector = function( tblOrTdSelector ) {
	var re = /(.*\.list_content_)(data)(.*)/;
	var ret = tblOrTdSelector.replace(re, "$1header$3");
	var groups = tblOrTdSelector.match(re);
	if( groups && groups[3] ) {
		var rere = /(.*)(td:nth-of)(.*)/;
		var gg = groups[3].match(rere);
		if( gg && gg.length > 0 ) {
			ret = groups[1] + 'header' + groups[3].replace(rere, "$1th:nth-of$3");
		}
	}

	return ret;
};

TableEx.getHeaderNames = function( tblOrTdSelector ) {
	var selec = TableEx.getHeaderSelector(tblOrTdSelector);

	return jQuery(selec).map(function(i,o){
		if( o && o.tagName == "TABLE" ) {
			return $(o).find("th").map(function(j,v){
				var val = (v ? $(v).text().trim() : undefined);
				if( j == 0 && !val ) {
					val = $(v).find("span").attr("name");
				}
				return val;
			}).get();
		} else {
			var val = (o ? $(o).text().trim() : undefined);
			if( i == 0 && !val ) {
				val = $(o).find("span").attr("name");
			}
			return val;
		}
	}).get();
};
