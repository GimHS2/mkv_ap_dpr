/*
	File Name:	utils.js
	Version:	2.2.3

	Description:

	Note:
		toCurrencyFormat( value );

		getQueryValue( href, key );
		getQueryValues( href, key );
		removeQueryValue( href, keyPrefix );
		replaceClassName( href, className );
		replaceQueryValue( href, key, value );
		submitPost( url );

		getCookie( name );
		setCookie( name, value, expire );

		callByKeydown( functionObj );

		attachWindowEvent( eventId, functionObj );

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2016/08/31		2.2.3	attachWindowEvent(): event 추가시 window.addEventListener 이걸 먼저 사용하도록 수정
	stghr12		2010/07/31		2.2.2	toHtmlString(string) 추가
	stghr12		2010/05/31		2.2.1	submitPost(url) 추가
	stghr12		2009/10/31		2.2.0	create(main.js 복사)
*/

/***********************************************************************************************************************
	toCurrencyFormat( value );
***********************************************************************************************************************/
function toCurrencyFormat( value ) {
	value = value.toString();
	if( value.indexOf("e") >= 0 ) return value;

	var sign = "";
	if( value.charAt(0) == "-" ) {
		sign = "-";
		value = value.substring(1);
	}

	if( value.length <= 3 ) return sign + value;

	var idx = (value.length % 3);
	if( idx == 0 ) idx = 3;

	var string = value.substring( 0, idx );
	value = value.substring( idx );
	while( value.length > 0 ) {
		string += ","+ value.substring( 0, 3 );
		value = value.substring( 3 );
	}

	return sign + string;
}


/***********************************************************************************************************************
	toHtmlString( string );
***********************************************************************************************************************/
function toHtmlString( string ) {
	string = string.replace( /&/g, "&#38;" );
	string = string.replace( /</g, "&lt;" );
	string = string.replace( />/g, "&gt;" );

	return string;
}


/***********************************************************************************************************************
	getQueryValue( href, key );
	getQueryValues( href, key );
	removeQueryValue( href, keyPrefix );
	replaceClassName( href, className );
	replaceQueryValue( href, key, value );
	submitPost( url );
***********************************************************************************************************************/
function getQueryValue( href, key ) {
	var idx1 = href.indexOf( "?"+ key +"=" );
	if( idx1 < 0 ) idx1 = href.indexOf( "&"+ key +"=" );
	if( idx1 < 0 ) return null;

	idx1 += key.length + 2;
	var idx2 = href.indexOf( "&", idx1 );
	if( idx2 < 0 )
		return href.substring( idx1 );
	else
		return href.substring( idx1, idx2 );
}

function getQueryValues( href, key ) {
	var values = null;

	var idx1 = href.indexOf( "?"+ key +"=" );
	if( idx1 < 0 ) idx1 = href.indexOf( "&"+ key +"=" );
	while( idx1 >= 0 ) {
		if( values == null ) values = new Array();
		idx1 += key.length + 2;
		var idx2 = href.indexOf( "&", idx1 );
		if( idx2 < 0 ) {
			values[values.length] = href.substring( idx1 );
			return values;
		} else {
			values[values.length] = href.substring( idx1, idx2 );
			idx1 = href.indexOf( "&"+ key +"=", idx2 );
		}
	}

	return values;
}

function removeQueryValue( href, keyPrefix ) {
	var idx0 = href.indexOf( '?' );
	if( idx0 < 0 ) return href;

	var idx1 = href.indexOf( "?"+ keyPrefix, idx0 );
	if( idx1 < 0 ) idx1 = href.indexOf( "&"+ keyPrefix, idx0 );
	if( idx1 < 0 ) return href;

	var idx2 = href.indexOf( "&", idx1+1 );
	if( idx2 < 0 )
		href = href.substring( 0, idx1 );
	else {
		href = href.substring( 0, idx1+1 ) + href.substring( idx2+1 );

		idx1 = href.indexOf( "&"+ keyPrefix, idx1 );
		while( idx1 >= 0 ) {
			idx2 = href.indexOf( "&", idx1+1 );
			if( idx2 < 0 )
				return href.substring( 0, idx1 );
			else {
				href = href.substring( 0, idx1 ) + href.substring( idx2 );
				idx1 = href.indexOf( "&"+ keyPrefix, idx1 );
			}
		}
	}

	return href;
}

function replaceClassName( href, className ) {
	var idx1 = href.indexOf( '?' );
	if( idx1 < 0 ) {
		var idx0 = href.lastIndexOf( '/' );
		if( idx0 < 0 )
			return className;
		else
			return href.substring( 0, idx0+1 ) + className;
	} else {
		var idx0 = href.lastIndexOf( '/', idx1 );
		if( idx0 < 0 )
			return className + href.substring( idx1 );
		else
			return href.substring( 0, idx0+1 ) + className + href.substring( idx1 );
	}
}

function replaceQueryValue( href, key, value ) {
	var idx0 = href.indexOf( '?' );
	if( idx0 < 0 ) {
		if( value ) href = href +"?"+ key +"="+ value;
		return href;
	}

	var idx1 = href.indexOf( "?"+ key +"=", idx0 );
	if( idx1 < 0 ) idx1 = href.indexOf( "&"+ key +"=", idx0 );
	if( idx1 < 0 ) {
		if( value ) href = href +"&"+ key +"="+ value;
		return href;
	}

	var idx2 = href.indexOf( "&", idx1+1 );
	if( idx2 < 0 ) {
		if( value )
			href = href.substring( 0, ++idx1 ) + key +"="+ value;
		else
			href = href.substring( 0, idx1 );
	} else {
		if( value )
			href = href.substring( 0, ++idx1 ) + key +"="+ value + href.substring( idx2 );
		else
			href = href.substring( 0, idx1+1 ) + href.substring( idx2+1 );

		idx1 = href.indexOf( "&"+ key +"=", idx1 );
		while( idx1 >= 0 ) {
			idx2 = href.indexOf( "&", idx1+1 );
			if( idx2 < 0 )
				return href.substring( 0, idx1 );
			else {
				href = href.substring( 0, idx1 ) + href.substring( idx2 );
				idx1 = href.indexOf( "&"+ key +"=", idx1 );
			}
		}
	}

	return href;
}

function submitPost( url, target ) {
	var innerHTML;

	var className = url;
	var parameters = new Array();
	var idx = url.indexOf( '?' );
	if( idx >= 0 ) {
		className = url.substring( 0, idx );
		parameters = url.substring( idx+1 ).split("&");
	}

	innerHTML = "<form name='frmPOST' method='POST' action='"+ className +"' "+ ( target ? "target='"+ target +"'" : "" ) +">";
	for( var p = 0; p < parameters.length; p++ ) {
		var values = parameters[p].split("=");
		var value = ( values.length > 1 ? decodeURIComponent(values[1]) : "" );
		value = value.replace( /&/g, "&#38" );
		value = value.replace( /;/g, "&#59;" );
		value = value.replace( /&#38/g, "&#38;" );
		value = value.replace( /'/g, "&#39;" );
		value = value.replace( /"/g, "&#34;" );
		value = value.replace( /\\/g, "&#92;" );
		innerHTML += "<input type='hidden' name='"+ values[0] +"' value='"+ value +"'/>";
	}
	innerHTML += "</form>";

	if( !submitPost.span ) {
		submitPost.span = document.createElement( "span" );
		submitPost.span.style.display = "none";

		document.body.insertBefore( submitPost.span, document.body.childNodes[0] );
	}
	submitPost.span.innerHTML = innerHTML;

	frmPOST.submit();
}


/***********************************************************************************************************************
	getCookie( name );
	setCookie( name, value, expire );
***********************************************************************************************************************/
function getCookie( name ) {
	name = name + "=";

	if( document.cookie.length > 0 ) {
		var offset = document.cookie.indexOf( name );
		if( offset != -1 ) {
			offset += name.length;
			var end = document.cookie.indexOf( ";", offset );
			if( end == -1 ) end = document.cookie.length;
			return unescape( document.cookie.substring(offset, end) );
		}
	}
	return "";
}

function setCookie( name, value, expire ) {
	document.cookie = name + "=" + escape(value) + ( expire == null ? "" : "; expires=" + expire.toGMTString() );
}


/***********************************************************************************************************************
	callByKeydown( functionObj );
***********************************************************************************************************************/
function callByKeydown( functionObj ) {
	if( window.event.keyCode == 13 ) {
		window.event.returnValue = false;
		functionObj();
	}
}


/***********************************************************************************************************************
	attachWindowEvent( eventId, functionObj );
***********************************************************************************************************************/
function attachWindowEvent( eventId, functionObj ) {
	if ( typeof window.addEventListener != "undefined" )
		window.addEventListener( eventId, functionObj, false );
	else if ( typeof window.attachEvent != "undefined" )
		window.attachEvent( "on"+ eventId, functionObj );
	else {
		switch( eventId ) {
		case "load":
			if ( window.onload != null ) {
				var oldOnload = window.onload;
				window.onload = function( e ) {
					oldOnload( e );
					functionObj();
				};
			} else
				window.onload = functionObj;
			break;
		}
	}
}
