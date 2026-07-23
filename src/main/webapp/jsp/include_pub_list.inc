<%--
	File Name:	include_pub_list.inc
	Version:	2.2.4

	Description:
		<body>에 추가

		window.onLoad시에 initList() 호출추가(attachWindowEvent)
		include
			include_pub_index.inc
		property.getProperty()
			"conditionKey"
		request.getParameter()
			"filterType"
			"filterValue"
		script
			changeShowCount( maxRows );
			filterReq();
			getLocationListURL( ... );
			getRequestMultiURL( url, mode, checkObj, name );
			indexMove( skipRows );
			initList();
			listClick( checkObj );
			listLink( selectObj, name );
			listSort( sortKey );
			requestDelete( url, query );
			requestDelete( url, checkObj, name );
			requestDownload( url, checkObj, name );
			requestInfo( url, checkObj, name, value, self );
			requestModify( url, checkObj, name, value, self );
			requestOne( url, checkObj, name, value, self );
			tabLink( selectObj, tabname, name );
		script(using)
			attachDefaultParameter();
			attachWindowEvent();
			checkURLLength();
			getLocationURL();
			replaceQueryValue();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/10/31		2.2.4	requestDownload() 추가
	jbaek		2014/08/30		2.2.3	requestDelete에 submitPost추가
	lsinji		2009/01/02		2.2.2	iframe.src를 blank.html로 설정
	guksm		2008/09/02		2.2.1	tabLink() 추가(focustab=${tabname} 을 url에 넣어서 request) -> 그외는 listLink 와 같음
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										getRequestMultiURL() 추가
										filterValueInput() 삭제: callByKeydown(filterReq)으로 대체
										requestDelete(): attachDefaultParameter() 사용
	stghr12		2007/04/30		2.1.1	requestDelete(): 로직 변경
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
										getLocationListURL() 추가
										filterValueInput(): 엔터가 눌렸을 때, window.event.returnValue = false 처리 추가
										initList(): allrows가 0보다 크더라도, condionKey가 있으면 listCount(mode=cnt)를 요청하도록 수정
										listLink(): encodeURIComponent 사용
										requestDelete(): URL길이 체크 추가
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/11		1.0.0	create
--%>

<script type='text/javascript'>

	$(function() {
		$("table.list_content_data td.line-check input[type=checkbox]").change( function() {
			var checkBoxSize = $("table.list_content_data td.line-check input[type=checkbox]").length;
			var checkedSize = $("table.list_content_data td.line-check input[type=checkbox]:checked").length;
			var titleCheckBox = $(this).closest( "div.list_content" ).find( "table.list_content_header th div[name=title-checkbox]" );
			titleCheckBox.removeClass();

			if( checkBoxSize == checkedSize ) {
				titleCheckBox.addClass( "check-all" );
			} else if( checkedSize > 0 ) {
				titleCheckBox.addClass( "check-part" );
			}

			var line = $(this).closest( "tbody.multi-line" );
			if( line.length < 1 ) {
				line = $(this).closest( "tr" );
			}
			if( $(this).is(":checked") ) {
				line.addClass( "line-checked" );
			} else {
				line.removeClass( "line-checked" );
			}
		});

		if( $("div.list_content .list-function").height() > 36 ) {
			$("div.list_content .list-function button").css( "margin-bottom", "10px" );
			$("div.list_content").css( "padding-bottom", "15px" );
		}
	});

	function changeShowCount( maxRows ) {
		var url = getLocationURL();
		if( typeof maxRows == "object" ) {
			url = replaceQueryValue( url, "focus", maxRows.name );
			maxRows = maxRows.value;
		}
		url = replaceQueryValue( url, "max", maxRows );
		var skipRows = <%= htmlpage.getListIndexVariables()[0] %>;
		if( skipRows%maxRows != 0 )
			url = replaceQueryValue( url, "skip", null );

		location.replace( url, "_self" );
	}

	function filterReq() {
		var url = replaceQueryValue( getLocationURL(), "skip", null );
		url = replaceQueryValue( url, "filterType", encodeURIComponent(frmMain.filterType.value) );
		url = replaceQueryValue( url, "filterValue", encodeURIComponent(frmMain.filterValue.value) );
		url = replaceQueryValue( url, "focus", "filterValue" );

		location.replace( url, "_self" );
	}

	function getLocationListURL() {
		var url = getLocationURL( "filterType", "filterValue", "sort", "url" );

		var args = getLocationListURL.arguments;
		if( args != null ) {
			for( var i = 0; i < args.length; i++ )
				url = replaceQueryValue( url, args[i], null );
		}

		return url;
	}

	function getRequestMultiURL( url, mode, checkObj, name ) {
		var query;

		var names = name.split( "," );
		if( names.length == 1 )
			query = CheckBox.getQueryValue( checkObj, name );
		else {
			query = "";

			var selectedValues = CheckBox.getValues( checkObj );
			if( selectedValues != null ) {
				for( var v = 0; v < selectedValues.length; v++ ) {
					var values = selectedValues[v].split( ";", names.length );
					for( var i = 0; i < names.length; i++ )
						if( names[i] && names[i] != 'null' )
							query += "&"+ names[i] +"="+ encodeURIComponent( values[i] );
				}
				if( query ) query = query.substring( 1 );
			}
		}

		if( !query ) {
			var messages = { "header" : "<mtl:message key="MSG_CHOOSE_ONE_OR_MORE" encodeScript="true"/>" };
			customPopup.alert( messages );
			return null;
		} else
			return attachDefaultParameter( replaceQueryValue(url, "mode", mode) +"&"+ query, false );
	}

	function indexMove( skipRows ) {
		var url = replaceQueryValue( getLocationURL(), "skip", skipRows );
		windowOpen( url, "_self" );
	}

	function initList() {
		if( <%= htmlpage.getListIndexVariables()[2] %> >= 0 ) {
			if( document.all.list_page ) document.all.list_page.innerHTML = document.all._list_page_.innerHTML;
		}
		<% if( property.getProperty("conditionKey") != null ) { %>
			var url = getLocationURL();
			if( url == null ) {
				url = attachDefaultParameter( "<%= htmlpage.getRequestURL() %>" );
			}
			url = replaceQueryValue( url, "mode", "cnt" );
			url = replaceQueryValue( url, "condkey", "<%= property.getProperty("conditionKey") %>" );
			window.open( url, "subwin_count" );
		<% } %>

		if( $("form[name=frmMain]").length && frmMain.filterValue ) {
			var filterType = "<%= HtmlUtility.toScriptString(request.getParameter("filterType")) %>";
			if( filterType )
				frmMain.filterType.value = filterType;
			frmMain.filterValue.value = "<%= HtmlUtility.toScriptString(request.getParameter("filterValue")) %>";
		}
	}
	attachWindowEvent( "load", initList );

	function listClick( checkObj ) {
		if( listClick.previous ) {
			var tr_obj = listClick.previous.parentNode.parentNode;
			if( tr_obj.original_className )
				tr_obj.original_className = ( listClick.previous.checked ? 'selected' : 'list_content' );
			else
				tr_obj.className = ( listClick.previous.checked ? 'selected' : 'list_content' );
		}

		var tr_obj = checkObj.parentNode.parentNode;
		if( tr_obj.original_className )
			tr_obj.original_className = ( checkObj.checked ? 'selected' : 'list_content' );
		else
			tr_obj.className = ( checkObj.checked ? 'selected' : 'list_content' );
		if( checkObj.type == 'radio' ) listClick.previous = checkObj;
	}

	function listLink( selectObj, name ) {
		var url = replaceQueryValue( getLocationURL(), name ? name : selectObj.name, encodeURIComponent(selectObj.value) );

		var args = listLink.arguments;
		if( args.length > 2 )
			for( var i = 2; i < args.length; i++ )
				url = replaceQueryValue( url, args[i], null );
		url = replaceQueryValue( url, "skip", null );
		url = replaceQueryValue( url, "focus", selectObj.name );

		windowOpen( url, "_self" );
	}

	function listSort( sortKey ) {
		toggleLoading( true );
		location.replace( replaceQueryValue(getLocationURL(), "sort", sortKey), "_self" );
	}

	function requestDelete( url, queryOrCheckObj, name ) {
		var query;
		if( requestDelete.arguments.length == 2 ) {
			url = replaceQueryValue(url, "mode", "del") +"&"+ queryOrCheckObj;
			url = attachDefaultParameter( url, false );
		} else {
			url = getRequestMultiURL( url, "del", queryOrCheckObj, name );
			if( !url ) return;
		}

		url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
		var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" };
		customPopup.confirm( messages, function(res) {
			if( res ) {
				if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
					submitPost( url );
				} else {
					windowSelfOpen( url );
				}
			}
		});
	}

	var downUrl, downClassName;
	function requestDownload( url, checkObj, name, anchorObj, mode ) {
		if( !mode ) mode = "down";

		if( anchorObj ) {
			if( !downClassName ) {
				downClassName = anchorObj.className;
				downUrl = anchorObj.href;
			} else {
				anchorObj.className = downClassName;
				anchorObj.href = downUrl;
			}
		}

		url = replaceQueryValue( url, "fileType", getQueryValue( location.href, "fileType" ) );

		if( checkObj && name ) {
			var query = CheckBox.getQueryValue( checkObj, name );
			if( query ) {
				if( anchorObj ) anchorObj.className = "";
				var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD_SELECTED_ONLY" encodeScript="true"/>" };
				customPopup.confirm( messages, function(res) {
					if( res ) {
						if( name.indexOf(",") > 0 )
							url = getRequestMultiURL( url, mode, checkObj, name );
						else
							url = replaceQueryValue( url, "mode", mode ) +"&"+ query;

						if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> )
							submitPost( url );
						else
							windowOpen( url );
					}
				});
				return;
			}
		}

		var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
		customPopup.confirm( messages, function(res) {
			if( res ) {
				var downurl;
				if( checkObj ) downurl = getLocationURL( "url" );
				else downurl = url;
				downurl = replaceQueryValue( downurl, "mode", mode );
				downurl = replaceQueryValue( downurl, "fileType", getQueryValue( url, "fileType" ) );
				if( anchorObj )
					anchorObj.href = downurl;
				else
					windowOpen( downurl );
			} else {
				if( anchorObj ) {
					anchorObj.className = "";
				}
			}
		});
	}

	function requestInfo( url, checkObj, name, value, winname ) {
		requestOne( replaceQueryValue(url, "mode", "info"), checkObj, name, value, winname );
	}

	function requestModify( url, checkObj, name, value, winname ) {
		requestOne( replaceQueryValue(url, "mode", "imod"), checkObj, name, value, winname );
	}

	function requestOne( url, checkObj, name, value, winname ) {
		if( !value ) {
			var selectedValues = CheckBox.getValues( checkObj );
			if( selectedValues == null || selectedValues.length > 1 ) {
				var messages = { "header" : "<mtl:message key="MSG_CHOOSE_ONLY_ONE" encodeScript="true"/>" };
				customPopup.alert( messages );
				return;
			}
			value = selectedValues[0];
		}
		var names = name.split( "," );
		if( names.length == 1 )
			url = replaceQueryValue( url, name, encodeURIComponent(value) );
		else {
			var values = value.split( ";" );
			for( var i = 0; i < names.length; i++ )
				if( names[i] && names[i] != 'null' )
					url = replaceQueryValue( url, names[i], encodeURIComponent(values[i]) );
		}

		if( winname == "_self" )
			windowSelfOpen( url, getLocationURL() );
		else if( winname && winname.length  > 0 )
			windowOpen( replaceQueryValue(url, "wintype", "sub"), winname );
		else
			windowOpen( replaceQueryValue(url, "wintype", "sub"), "sub-content" );
	}

	function tabLink( selectObj, tabname, name ) {
		var url = replaceQueryValue( getLocationURL(), name ? name : selectObj.name, encodeURIComponent(selectObj.value) );

		var args = tabLink.arguments;
		if( args.length > 3 )
			for( var i = 3; i < args.length; i++ )
				url = replaceQueryValue( url, args[i], null );
		url = replaceQueryValue( url, "skip", null );
		url = replaceQueryValue( url, "focus", selectObj.name );
		url = replaceQueryValue( url, "focustab", tabname );

		window.open( url, "_self" );
	}

	function getScrollBarWidth () {
		var inner = document.createElement('p');
		inner.style.width = "100%";
		inner.style.height = "200px";

		var outer = document.createElement('div');
		outer.style.position = "absolute";
		outer.style.top = "0px";
		outer.style.left = "0px";
		outer.style.visibility = "hidden";
		outer.style.width = "200px";
		outer.style.height = "150px";
		outer.style.overflow = "hidden";
		outer.appendChild (inner);

		document.body.appendChild (outer);
		var w1 = inner.offsetWidth;
		outer.style.overflow = 'scroll';
		var w2 = inner.offsetWidth;
		if (w1 == w2) w2 = outer.clientWidth;

		document.body.removeChild (outer);

		return (w1 - w2);
	}
</script>

<% if( htmlpage.getListIndexVariables()[2] >= 0 ) { %>
	<%@ include file="include_pub_index.inc" %>
<% } %>
<% if( property.getProperty("conditionKey") != null ) { %>
	<iframe name='subwin_count' style='display: none' src='<%= systemConfig.getProperty("baseURL") %>blank.html'></iframe>
<% } %>
