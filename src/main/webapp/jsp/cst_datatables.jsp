<%--
	File Name:	cst_datatables.jsp
	Version:	2.2.1c

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	dudwls3720	2024/07/31		2.2.1c	getRequestURL에 String을 Script에서 표시가능한 String이 되도록 변경
	stghr12		2019/01/30		2.2.0c	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8'%>
<%@ page import="com.irt.html.*"%>
<%@ taglib uri="/mtltaglib" prefix="mtl"%>
<%
	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
%>
<%
	com.irt.html.HtmlPage htmlpage = (com.irt.html.HtmlPage) pageContext.findAttribute("htmlpage");
	com.irt.servlet.SystemConfig systemConfig = (com.irt.servlet.SystemConfig) pageContext
			.findAttribute("systemConfig");

	if (systemConfig == null || htmlpage == null)
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	else {

		String jqdtConfigOverrideString = (String) request.getAttribute("jqdtConfigOverrideString");
		String jqdtConfigString = (String) request.getAttribute("jqdtConfigString");
		String jqdtConfigColumnDefsString = (String) request.getAttribute("jqdtConfigColumnDefsString");
		String fieldHeaders = (String) request.getAttribute("fieldHeaders");
		String cellEditConfigString = (String) request.getAttribute("cellEditConfigString");
		String jqdtJavascriptString = (String) request.getAttribute("jqdtJavascriptString");
%>

<html>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />
<meta http-equiv='Content-Style-Type' content='text/css' />
<meta http-equiv='Content-Script-Type' content='text/javascript' />
<title><%=systemConfig.getSystemName()%></title>
<base href='<%=systemConfig.getBaseURL(htmlpage.getLocale())%>' />
<%
	if (false) {
			String[] styleSheetNames = htmlpage.getStyleSheetNames();
			for (int i = 0; i < styleSheetNames.length; i++)
				out.println("<link rel='stylesheet' href='style/" + styleSheetNames[i] + "'/>");
		}
%>
<script type='text/javascript'>
		function bodyLoad() {
			<%if ("sub".equals(htmlpage.getWindowType())) {%>
				window.resizeTo( 600, document.body.scrollHeight );
				window.resizeBy( 0, document.body.scrollHeight - document.body.clientHeight );
			<%}%>
			self.focus();
		}
	</script>


<script type='text/javascript' src='script/utils.js'></script>
<script type='text/javascript' src='script/jquery_min.js'></script>
<script type='text/javascript' src='script/jquery-noty.js'></script>

<script type='text/javascript' src='script/jqdt/plugins/preact.min.js'></script>

<link href='script/jqdt/main/jquery.dataTables.min.css' rel='stylesheet'></link>
<link href='script/jqdt/plugins/buttons.dataTables.min.css' rel='stylesheet'/>
<link href='script/jqdt/plugins/rowGroup.dataTables.min.css'
	rel='stylesheet'></link>
<link href='script/jqdt/plugins/cellEdit.dataTables.css'
	rel='stylesheet'></link>
<link href='script/jqdt/plugins/responsive.dataTables.min.css'
	rel='stylesheet'></link>
<link href='script/jqdt/plugins/keyTable.dataTables.min.css'
	rel='stylesheet'></link>
<link href='script/jqdt/plugins/autoFill.dataTables.min.css'
	rel='stylesheet'></link>
<link href='script/jqdt/plugins/select.dataTables.min.css'
	rel='stylesheet'></link>

<script type='text/javascript'
	src='script/jqdt/main/jquery.dataTables.js'></script>
<!-- dataTables plugins -->
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.rowGroup.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.cellEdit.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.responsive.min.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.keyTable.min.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.autoFill.min.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.select.min.js'></script>

<script type='text/javascript' src='script/assets/js/moment.min.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.datetime.js'></script>

<script type='text/javascript' src='script/jqdt/plugins/jszip.min.js'></script>
<script type='text/javascript' src='script/jqdt/plugins/pdfmake.min.js'></script>
<script type='text/javascript' src='script/jqdt/plugins/vfs_fonts.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/buttons.html5.min.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/buttons.print.min.js'></script>
<script type='text/javascript'
	src='script/jqdt/plugins/dataTables.buttons.min.js'></script>

<script type='text/javascript'>
//
//Pipelining function for DataTables. To be used to the `ajax` option of DataTables
//
$.fn.dataTable.pipeline = function ( opts ) {
 // Configuration options
 var conf = $.extend( {
     pages: 5,     // number of pages to cache
     url: '',      // script url
     data: null,   // function or object with parameters to send to the server
                   // matching how `ajax.data` works in DataTables
     method: 'GET' // Ajax HTTP method
 }, opts );

 // Private variables for storing the cache
 var cacheLower = -1;
 var cacheUpper = null;
 var cacheLastRequest = null;
 var cacheLastJson = null;

 return function ( request, drawCallback, settings ) {
     var ajax          = false;
     var requestStart  = request.start;
     var drawStart     = request.start;
     var requestLength = request.length;
     var requestEnd    = requestStart + requestLength;

     if ( settings.clearCache ) {
         // API requested that the cache be cleared
         ajax = true;
         settings.clearCache = false;
     }
     else if ( cacheLower < 0 || requestStart < cacheLower || requestEnd > cacheUpper ) {
         // outside cached data - need to make a request
         ajax = true;
     }
     else if ( JSON.stringify( request.order )   !== JSON.stringify( cacheLastRequest.order ) ||
               JSON.stringify( request.columns ) !== JSON.stringify( cacheLastRequest.columns ) ||
               JSON.stringify( request.search )  !== JSON.stringify( cacheLastRequest.search )
     ) {
         // properties changed (ordering, columns, searching)
         ajax = true;
     }

     // Store the request for checking next time around
     cacheLastRequest = $.extend( true, {}, request );

     if ( ajax ) {
         // Need data from the server
         if ( requestStart < cacheLower ) {
             requestStart = requestStart - (requestLength*(conf.pages-1));

             if ( requestStart < 0 ) {
                 requestStart = 0;
             }
         }

         cacheLower = requestStart;
         cacheUpper = requestStart + (requestLength * conf.pages);

         request.start = requestStart;
         request.length = requestLength*conf.pages;

         // Provide the same `data` options as DataTables.
         if ( typeof conf.data === 'function' ) {
             // As a function it is executed with the data object as an arg
             // for manipulation. If an object is returned, it is used as the
             // data object to submit
             var d = conf.data( request );
             if ( d ) {
                 $.extend( request, d );
             }
         }
         else if ( $.isPlainObject( conf.data ) ) {
             // As an object, the data given extends the default
             $.extend( request, conf.data );
         }

         settings.jqXHR = $.ajax( {
             "type":     conf.method,
             "url":      conf.url,
             "data":     request,
             "dataType": "json",
             "cache":    false,
             "success":  function ( json ) {
                 cacheLastJson = $.extend(true, {}, json);

                 if ( cacheLower != drawStart ) {
                     json.data.splice( 0, drawStart-cacheLower );
                 }
                 if ( requestLength >= -1 ) {
                     json.data.splice( requestLength, json.data.length );
                 }

                 drawCallback( json );
             }
         } );
     }
     else {
         json = $.extend( true, {}, cacheLastJson );
         json.draw = request.draw; // Update the echo for each response
         json.data.splice( 0, requestStart-cacheLower );
         json.data.splice( requestLength, json.data.length );

         drawCallback(json);
     }
 }
};

//Register an API method that will empty the pipelined data, forcing an Ajax
//fetch on the next draw (i.e. `table.clearPipeline().draw()`)
$.fn.dataTable.Api.register( 'clearPipeline()', function () {
 return this.iterator( 'table', function ( settings ) {
     settings.clearCache = true;
 } );
} );
</script>

<script type='text/javascript'>

<%=request.getAttribute("jqdtJavascriptString")%>


var cellEditDefaults = { inputCss: 'jqdt-input-class', allowNulls: { errorClass: 'jqdt-error' }, confirmationButton :  { confirmCss: 'jqdt-confirm-class', cancelCss: 'jqdt-cancel-class' } };

function HtmlEx(){};
HtmlEx.parseErrorText = function( html ) {
	return jQuery(html).find("table.info_content td.error").text().trim();
}
HtmlEx.error = function( message ) {
	noty({text: message, type:"error", closeWith: ['click', 'button']});
}

HtmlEx.isDebug = function() {
	return ( <%="Y".equals(com.irt.rbm.RBMSystem.getSystemEnv("SYS", "Debug;javascript"))%> ? true : undefined);
}

function DataTableEx(){};

DataTableEx.addRow = function( record ) {
	$("#mytable").DataTable().row.add( record );
}

DataTableEx.deleteRowPromise = function( row ) {
	var self = this;
	self.dfd = $.Deferred();

	var dataSrc = getQueryValue(row.ajax.url(), "dataSrc");
	var passing = {dataSrc: dataSrc, data: row.data(), mode: "del"};

	var ret = $.ajax(DataTableEx.createAjaxMap(passing))
				.success(function(res) {
					var errText = HtmlEx.parseErrorText(res);
					if( errText ) {
						self.dfd.resolve(null);
						alert(errText);
					} else {
						self.dfd.resolve(res.responseJSON);
						console.log("createPutRequestMap" + ".success: ", res);
					}
				}).error(function(err){
					self.dfd.resolve(null);
					var errText = HtmlEx.parseErrorText(err['responseText']);
					if( errText ) {
						alert(errText);
					} else {
						//							alert(xhr.status + " " + );
						console.log("createPutRequestMap" + ".error: ", err);
					}
				});

	return self.dfd.promise();
};

	DataTableEx.registerDeleteButtonEvent = function() {
		var dt = $("#mytable").DataTable();

		$("#mytable tbody").on("click", "tr", function(){
			if( $(this).hasClass("selected") ) {
				$(this).removeClass("selected");
			} else {
				dt.$("tr.selected").removeClass("selected");
				$(this).addClass("selected");
			}
		});

		if( $(".dt-buttons .dt-button.delete-button").length <=0 ) {
			$(".dt-buttons").append( $("<a>").addClass("dt-button delete-button").attr("tabindex","0").attr("aria-controls","mytable")
											 .append($("<span>").text("Del Selected")) );
		}
		$(".dt-button.delete-button").click(function(){
			var row = dt.row(".selected");
			DataTableEx.deleteRowPromise(row).then(function(resolved){
				if( resolved ) {

					console.log("resolved: ", resolved);
					dt.row(".selected").remove().draw(false);
				}
			});
		});
	};

	DataTableEx.createAjaxMap = function( passing ) {
		var url = location.origin + location.pathname;
		url = replaceQueryValue(url, "mode", passing.mode);
		url = replaceQueryValue(url, "dataSrc", passing.dataSrc);

		return $.extend({data: passing.data} , {
			url: url,
			type : "POST",
			dataType : "json",
		});
	};

	var updatingStru;
	var updatingData;
	var updatingCell;
	var updatedStru;
	var updatedData;

	function groupColumnDrawFunction(groupColumn) {

		var groupColIdx = groupColumn;
		return function(settings) {
		var api = this.api();
		var rows = api.rows({ page : 'current' }).nodes();
		var last = null;

		var columnsCount = updatingStru.columns('').nodes().length;

		api.column(groupColIdx, { page : 'current' })
			.data().each(function(group, i) {
					if (last !== group) {
						$(rows).eq(i).before(
							'<tr class="group"><td colspan="' + columnsCount + '">' + group + '</td></tr>'
						);

						last = group;
					}
				});
		}
	}

	function cellEditCallback(updatedCell, updatedRow, oldValue) {
		updatingStru = updatedRow;
		updatingData = updatedRow.data();
		updatingCell = updatedCell;

		var newValue = updatedCell.cell(updatedCell.index().row, updatedCell.index().column).data();
		var dataSrc = getQueryValue(updatedRow.ajax.url(), "dataSrc");
		var passing = {dataSrc : dataSrc, data: updatedRow.data(), mode: "put"};


		var ret = $.ajax(DataTableEx.createAjaxMap(passing)).success(function(res) {

					var errText = jQuery(res['responseText']).find("td.error").text().trim();
					if( errText ) {
						alert( jQuery(err['responseText']).find("td.error").text().trim() );
						return false;
					} else {
						if( res['responseJSON'] !== undefined ) {
							var record = ret['responseJSON']['record'];

							if (record !== undefined) {
								console.log("....will reload only the row itself....");
							}
						}
					}
					updatedStru = res;
					updatedData = res.responseJSON;
					console.log("donedone;");

				}).error(function(err){
					console.log("err: ", err);
				});

		console.log('The new value for the cell is: ', updatedCell.data());
		console.log('The values for each cell in that row are: ', updatedRow.data());
	}

	function filterColumn( idx ) {
		$('#mytable').DataTable().column(idx).search(
				$('#col' + idx + '_filter').val(),
				$('#col' + idx + '_regex').prop('checked'))
			.draw();
	}


	$.fn.dataTable.ext.errMode = function ( settings, helpPage, message ) {
		console.log(message);
	};



	var jqdtConfig;
	var cellEditConfig;

	$(document)
			.ready(
					function() {
						$('#mytable tfoot th')
								.each(
										function(index) {
											var title = $(this).text();
											var hhhh = '<input type="text" placeholder="Search '+ title + '" id="col'+index+'_filter" data-column="' + index +'" class="column_filter"/>';
											hhhh += '<label for="col'+index+'_regex">regex</label>';
											hhhh += '<input type="checkbox" class="column_filter_regex" id="col'+index+'_regex"/>';
											$(this).html(hhhh);
										});

						var jqdtConfigOverride = <%=jqdtConfigOverrideString%>;

						jqdtConfig = <%=jqdtConfigString%>;
						jqdtConfigColumnDefs = <%=jqdtConfigColumnDefsString%>;

						jqdtConfig['columnDefs'] = $.merge(jqdtConfig['columnDefs'] || [],
								jqdtConfigColumnDefs['columnDefs']);

						if( jqdtConfigOverride ) {
							for( var key in jqdtConfigOverride ) {

								var val = jqdtConfigOverride[key];

								console.log( "overriden fr (" + key + ") : ", jqdtConfig[key] );
								var defaultVal = {};
								if( val instanceof Array ) {
									defaultVal = [];
									jqdtConfig[key] = $.merge( jqdtConfig[key] || defaultVal, val );
								} else {
									jqdtConfig[key] = $.merge( jqdtConfig[key] || defaultVal, val );
								}
								console.log( "overriden to (" + key + ") : ", jqdtConfig[key] );
							}
						}


						var t = $('#mytable').on('error.dt', function (e, settings, techNote, message) {
							console.log('An error has been reported by DataTables:', message);
							console.log(e );
						}).DataTable(jqdtConfig);


						t.columns().every(function() {
								var that = this;

								$('input.column_filter', this.footer())
									.on('keyup change', function() {
										if (that.search() !== this.value) {
											filterColumn($(this).attr( "data-column"));
										}
									});
							});

						cellEditConfig = $.extend(cellEditDefaults, {
							'onUpdate' : cellEditCallback
						}, <%=cellEditConfigString%>);

						t.MakeCellsEditable(cellEditConfig);

						DataTableEx.registerDeleteButtonEvent();
					});
</script>


</head>

<body onLoad='JavaScript:bodyLoad();'>
	<div id="structure">
		<div id="struct-new-row"></div>
	</div>
	<p id="msg"></p>
	<table id="mytable">
		<tfoot>
			<%=fieldHeaders%>
		</tfoot>
	</table>
</body>
</html>
<%
	}
%>
