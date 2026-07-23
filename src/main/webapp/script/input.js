/*
	File Name:	input.js
	Version:	2.2.1

	Description:
		script
			disableBlankInput( formObj, condition );
			focusForm( formObj, elementObj );
			initTabPaneForm( ... );
			initTabPaneForm1( ... );
			initTabPaneForm_setParentTabPage( tabPanes );
			resetForm( formObj );
			resetTabPaneForm( tabPane, formObj );

			checkGln( gln )
			checkGtin( gtin )

			TabPageEx( tabPage )
				TabPageEx.elementTags
				TabPageEx.changeCheckParentTab( tabPage, node, bool )
				TabPageEx.disableChild( tabPage, node )
				TabPageEx.enableChild( tabPage, node )
				TabPageEx.prototype.changeMandatory( mandatory, registed )
				TabPageEx.prototype.focus()
				TabPageEx.prototype.modified( registed )
				TabPageEx.prototype.reset( resetAll )
			Field
				Field.readonlyStyle
				Field.optionalStyles
				Field.mandatoryStyles
				Field.changeMandatory( elementObj, mandatory )
				Field.focus( elementObj )
				Field.formatDate( elementObj )
				Field.getSubjectObject( elementObj )
				Field.isMandatory( elementObj )
				Field.isReadonly( elementObj )
				Field.lock( elementObj, disabled )
				Field.modified( elementObj )
				Field.release( elementObj )
			DateField
				DateField.convertDateToValue( date, type );
				DateField.convertValueToDate( value, type );
				DateField.getDateValue( elementName, type );
				DateField.getElementName( elementName );
				DateField.getElements( elementName );
				DateField.getSubjectObjectByName( elementName );
				DateField.getValueByName( elementName );
				DateField.modifiedByName( elementName );
				DateField.setDate( elementName, date, dateonly, callOnchange );
				DateField.setValue( elementName, value, dateonly, callOnchange );
			TimeField
				TimeField.changed( elementObj );
				TimeField.checkValue( value );
				TimeField.getElements( elementName );
				TimeField.getSubjectObject( elementObj );
				TimeField.getValue( elementObj );
				TimeField.getValueByName( elementName );
				TimeField.modified( elementObj );
				TimeField.setValue( elementName, value, callOnchange );
		script(using)
			Field
			Styles
			TabPage
		style
			.readonly
			.content_o
			.content_omod
			.optional_u
			.optional
			.content_m
			.content_mmod
			.mandatory_u
			.mandatory

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2008/05/31		2.2.1	focusForm(), resetForm(): input type="file" 처리
	stghr12		2007/11/30		2.2.0	resetForm(): radio 처리방식 변경
										DateField.setDate(), DateField.setValue(): callOnchange 추가
										TimeField.setValue() 추가
	stghr12		2007/04/30		2.1.0	version 관리
*/


/***********************************************************************************************************************
	disableBlankInput( formObj, condition );
	focusForm( formObj, elementObj );
	initTabPaneForm( ... );
	initTabPaneForm1( ... );
	initTabPaneForm_setParentTabPage( tabPanes );
	resetForm( formObj );
	resetTabPaneForm( tabPane, formObj );
***********************************************************************************************************************/
function disableBlankInput( formObj, condition ) {
	for( var i = 0; i < formObj.elements.length; i++ ) {
		var elementObj = formObj.elements[i];

		switch( elementObj.type ) {
		case 'text':
		case 'hidden':
		case 'select-one':
			if( elementObj.value != "" ) {
				if( elementObj.value != "ALL" || elementObj.type != 'select-one' )
					continue;
			}

			elementObj.disabled = true;
			if( !condition ) continue;

			elementObj = eval( formObj.name +"."+ elementObj.name +"_type" );
			if( elementObj ) {
				if( Field.isArray(elementObj) ) {
					for( var j = 0; j < elementObj.length; j++ )
							elementObj[j].disabled = true;
				} else
					elementObj.disabled = true;
			}
		}
	}
}

function focusForm( formObj, elementObj) {
	if( elementObj && Field.isArray(elementObj) ) elementObj = elementObj[0];
	if( elementObj && !elementObj.disabled && elementObj.focus && elementObj.type != "hidden" ) {
		if( !Styles.containClassName(elementObj, Field.readonlyStyle) ) {
			Field.focus( elementObj );
			if( elementObj.select ) elementObj.select();
			return;
		}
	}
	if( !formObj ) return;

	for( var i = 0; i < formObj.elements.length; i++ ) {
		elementObj = formObj.elements[i];
		if( elementObj.disabled ) continue;

		switch( elementObj.type ) {
		case "text":
		case "textarea":
		case "password":
		case "file":
		case "select-one":
			if( !Styles.containClassName(elementObj, Field.readonlyStyle) ) {
				Field.focus( elementObj );
				if( elementObj.select ) elementObj.select();
				return;
			}
		}
	}
}

function initTabPaneForm() {
	var tabPanes = initTabPaneForm.arguments;

	initTabPaneForm_setParentTabPage( tabPanes );

	for( var i = 0; i < tabPanes.length; i++ ) {
		for( var p = 0; p < tabPanes[i].pages.length; p++ )
			new TabPageEx( tabPanes[i].pages[p] );
	}
}

function initTabPaneForm1() {
	initTabPaneForm_setParentTabPage( initTabPaneForm1.arguments );
}

function initTabPaneForm_setParentTabPage( tabPanes ) {
	for( var i = 0; i < tabPanes.length; i++ ) {
		var pages = tabPanes[i].pages;
		var tags = new Array( "INPUT", "SELECT", "TEXTAREA" );

		for( var p = 0; p < pages.length; p++ ) {
			for( var t = 0; t < tags.length; t++ ) {
				var elements = pages[p].element.getElementsByTagName( tags[t] );
				if( !elements ) continue;

				for( var e = 0; e < elements.length; e++ ) {
					if( elements[e].parentTabPage ) {
						if( elements[e].parentTabPage = pages[p] )
							continue;
						else if( elements[e].parentTabPage == pages[p].parentTabPage )
							elements[e].parentTabPage = pages[p];
						else if( pages[p] != elements[e].parentTabPage.parentTabPage ) {
							if( pages[p].element.contains(elements[e].parentTabPage.element) )
								elements[e].parentTabPage.parentTabPage = pages[p];
							else {
								pages[p].parentTabPage = elements[e].parentTabPage;
								elements[e].parentTabPage = pages[p];
							}
						}
					} else
						elements[e].parentTabPage = pages[p];
				}
			}
		}
	}
}

function resetForm( formObj ) {
	var mStyles = Field.mandatoryStyles;
	var oStyles = Field.optionalStyles;

	var array = new Array();
	for( var e = 0; e < formObj.elements.length; e++ ) {
		var elementObj = formObj.elements[e];
		var subjectObj = Field.getSubjectObject( elementObj );

		switch( elementObj.type ) {
		case "checkbox":
			if( subjectObj )
				Styles.changeClassName2( subjectObj, mStyles[2], mStyles[3], oStyles[2], oStyles[3] );
			break;
		case "radio":
			if( subjectObj ) array.push( elementObj );
			break;
		case "hidden":
		case "text":
		case "textarea":
		case "password":
		case "file":
		case "select-one":
			Styles.changeClassName2( elementObj, mStyles[1], mStyles[0], oStyles[1], oStyles[0] );

			if( subjectObj ) {
				if( elementObj.value == "" )
					Styles.changeClassName2( subjectObj, mStyles[3], mStyles[2], oStyles[3], oStyles[2] );
				else
					Styles.changeClassName2( subjectObj, mStyles[2], mStyles[3], oStyles[2], oStyles[3] );
			}
		}
	}

	var name = null;
	var narray = new Array();
	var elarray = null;
	while( array.length > 0 ) {
		var elementObj = array.pop();
		if( elementObj.name != name ) {
			narray.push( elarray = new Array() );
			name = elementObj.name;
		}
		elarray.push( elementObj );
	}

	while( narray.length > 0 ) {
		elarray = narray.pop();

		var value = "";
		for( var e = 0; e < elarray.length; e++ )
			if( elarray[e].checked )
				value = elarray[e].value;

		var subjectObj = Field.getSubjectObject( elarray[0] );
		if( value == "" )
			Styles.changeClassName2( subjectObj, mStyles[3], mStyles[2], oStyles[3], oStyles[2] );
		else
			Styles.changeClassName2( subjectObj, mStyles[2], mStyles[3], oStyles[2], oStyles[3] );
	}
}

function resetTabPaneForm( tabPane, formObj ) {
	resetForm( formObj );
	for( var p = 0; p < tabPane.pages.length; p++ )
		tabPane.pages[p].reset( true );
}


/***********************************************************************************************************************
	checkGln( gln )
	checkGtin( gtin )
***********************************************************************************************************************/
function checkGln( gln ) {
	var odd = 0;
	var even = 0;

	if( gln.length != 13 ) return false;
	for( var i = 0; i < gln.length; i+= 2 ) {
		if( gln.charAt(i) < '0' || gln.charAt(i) > '9' ) return false;
		odd += gln.charAt(i) - '0';
	}
	for( var i = 1; i < gln.length; i+= 2 ) {
		if( gln.charAt(i) < '0' || gln.charAt(i) > '9' ) return false;
		even += gln.charAt(i) - '0';
	}
	if( gln.length % 2 == 0 )
		return ( (odd * 3 + even)%10 == 0 );
	else
		return ( (odd + even * 3)%10 == 0 );
}

function checkGtin( gtin ) {
	var odd = 0;
	var even = 0;

	for( var i = 0; i < gtin.length; i+= 2 ) {
		if( gtin.charAt(i) < '0' || gtin.charAt(i) > '9' ) return false;
		odd += gtin.charAt(i) - '0';
	}
	for( var i = 1; i < gtin.length; i+= 2 ) {
		if( gtin.charAt(i) < '0' || gtin.charAt(i) > '9' ) return false;
		even += gtin.charAt(i) - '0';
	}
	if( gtin.length % 2 == 0 )
		return ( (odd * 3 + even)%10 == 0 );
	else
		return ( (odd + even * 3)%10 == 0 );
}


/***********************************************************************************************************************
	TabPageEx( tabPage )
***********************************************************************************************************************/
function TabPageEx( tabPage ) {
	this.tabPage = tabPage;
	this.tabTitle = tabPage.anchor.firstChild;

	if( this.tabTitle.nodeType != 1 ) {
		this.tabTitle = document.createElement( "DIV" );
		while( tabPage.anchor.hasChildNodes() )
			this.tabTitle.appendChild( tabPage.anchor.firstChild );
		tabPage.anchor.appendChild( this.tabTitle );
	}

	this.childFields = new Array;
	this.childTabs = new Array;
	for( var t = 0; t < TabPageEx.elementTags.length; t++ ) {
		var elements = tabPage.element.getElementsByTagName( TabPageEx.elementTags[t] );
		for( var e = 0; e < elements.length; e++ ) {
			if( elements[e].parentTabPage == tabPage ) {
				this.childFields[this.childFields.length] = elements[e];
				elements[e].checkParentTab = true;
			} else {
				for( var page = elements[e].parentTabPage; page; page = page.parentTabPage ) {
					if( page.parentTabPage == tabPage ) {
						this.childTabs[this.childTabs.length] = page;
						page.checkParentTab = true;
						break;
					}
				}
			}
		}
	}

	var oThis = this;
	tabPage.changeMandatory = function( mandatory, registed ) { oThis.changeMandatory(mandatory, registed); };
	tabPage.focus = function() { oThis.focus(); };
	tabPage.modified = function( registed ) { oThis.modified(registed); };
	tabPage.reset = function() { oThis.reset(false); };
}

TabPageEx.elementTags = new Array( "INPUT", "SELECT", "TEXTAREA" );

TabPageEx.changeCheckParentTab = function( tabPage, node, bool ) {
	if( node[0] ) {
		for( var i = 0; i < node.length; i++ )
			TabPageEx.changeCheckParentTab( tabPage, node[i], bool );
		return null;
	}

	if( !tabPage ) {
		for( var element = node.parentNode; element; element = element.parentNode )
			if( element.tabPage ) {
				tabPage = element.tabPage;
				break;
			}
	}

	var changed = false;
	if( node.parentTabPage == tabPage ) {
		changed = ( node.checkParentTab != bool );
		node.checkParentTab = bool;
	} else if( node.getElementsByTagName ) {
		var changed = false;
		for( var t = 0; t < TabPageEx.elementTags.length; t++ ) {
			var elements = node.getElementsByTagName( TabPageEx.elementTags[t] );
			for( var e = 0; e < elements.length; e++ ) {
				if( elements[e].parentTabPage == tabPage && elements[e].checkParentTab != bool ) {
					changed = true;
					elements[e].checkParentTab = bool;
				}
			}
		}
	}

	return ( changed ? tabPage : null );
}

TabPageEx.disableChild = function( tabPage, node ) {
	Styles.changeDisplay( node, false );
	return TabPageEx.changeCheckParentTab( tabPage, node, false );
}

TabPageEx.enableChild = function( tabPage, node ) {
	Styles.changeDisplay( node, true );
	return TabPageEx.changeCheckParentTab( tabPage, node, true );
}

TabPageEx.prototype.changeMandatory = function( mandatory, registed ) {
	if( mandatory ) {
		if( this.tabPage.mandatory ) {
			if( !registed && this.tabPage.registed ) {
				this.tabPage.registed = false;
				Styles.changeClassName( this.tabTitle, Field.mandatoryStyles[3], Field.mandatoryStyles[2] );
			}
		} else {
			this.tabPage.registed = registed;
			Styles.appendClassName( this.tabTitle, this.tabPage.registed ? Field.mandatoryStyles[3] : Field.mandatoryStyles[2] );
			if( this.tabPage.checkParentTab )
				parentTabPage.changeMandatory( true, registed );
		}
	} else
		this.reset();
}

TabPageEx.prototype.focus = function() {
	if( this.tabPage.parentTabPage )
		this.tabPage.parentTabPage.focus();
	this.tabPage.select();
}

TabPageEx.prototype.modified = function( registed ) {
	if( !registed ) {
		if( this.tabPage.registed ) {
			this.tabPage.registed = false;
			Styles.changeClassName( this.tabTitle, Field.mandatoryStyles[3], Field.mandatoryStyles[2] );
			if( this.tabPage.checkParentTab )
				this.tabPage.parentTabPage.modified( registed );
		}
	} else
		this.reset();
}

TabPageEx.prototype.reset = function( resetAll ) {
	var tabPage = this.tabPage;

	tabPage.mandatory = false;
	tabPage.registed = true;
	for( var p = 0; p < this.childTabs.length; p++ ) {
		if( resetAll ) this.childTabs[p].reset();
		if( !this.childTabs[p].checkParentTab ) continue;
		if( this.childTabs[p].mandatory ) {
			tabPage.mandatory = true;
			if( !this.childTabs[p].registed ) tabPage.registed = false;
		}
	}
	if( !tabPage.mandatory || tabPage.registed ) {
		for( var e = 0; e < this.childFields.length; e++ ) {
			if( !this.childFields[e].checkParentTab ) continue;
			if( Field.isMandatory(this.childFields[e]) ) {
				tabPage.mandatory = true;
				if( this.childFields[e].type != "checkbox" && Field.getValue(this.childFields[e]) == "" ) {
					tabPage.registed = false;
					break;
				}
			}
		}
	}

	Styles.removeClassName( this.tabTitle, Field.mandatoryStyles[2] +" "+ Field.mandatoryStyles[3] );
	if( tabPage.mandatory )
		Styles.appendClassName( this.tabTitle, tabPage.registed ? Field.mandatoryStyles[3] : Field.mandatoryStyles[2] );
	if( this.checkParentTab ) this.parentTabPage.reset();
}


/***********************************************************************************************************************
	Field
***********************************************************************************************************************/
Field.readonlyStyle = "readonly";
Field.optionalStyles = new Array( "content_o", "content_omod", "optional_u", "optional" );
Field.mandatoryStyles = new Array( "content_m", "content_mmod", "mandatory_u", "mandatory" );

Field.changeMandatory = function( elementObj, mandatory ) {
	if( Field.isArray(elementObj) ) {
		switch( elementObj.type ) {
		case "radio":
			if( elementObj[e].checkParentTab )
				elementObj[e].parentTabPage.changeMandatory( mandatory, Field.getValue(elementObj) != "" );
			break;
		case "checkbox":
			if( elementObj[e].checkParentTab )
				elementObj[e].parentTabPage.changeMandatory( mandatory, true );
			break;
		default:
			for( var e = 0; e < elementObj.length; e++ )
				Field.changeMandatory( elementObj[e] );
			return;
		}
	} else {
		if( Field.isMandatory(elementObj) == mandatory ) return;
		if( mandatory )
			elementObj.className = elementObj.className.replace( /content_o/, "content_m" );
		else
			elementObj.className = elementObj.className.replace( /content_m/, "content_o" );
		if( elementObj.checkParentTab )
			elementObj.parentTabPage.changeMandatory( mandatory, Field.getValue(elementObj) != "" );
	}

	var subjectObj = Field.getSubjectObject( elementObj );
	if( subjectObj ) {
		if( mandatory )
			subjectObj.className = subjectObj.className.replace( /optional/, "mandatory" );
		else
			subjectObj.className = subjectObj.className.replace( /mandatory/, "optional" );
	}
}

Field.focus = function( elementObj ) {
	if( Field.isArray(elementObj) ) elementObj = elementObj[0];
	if( elementObj.parentTabPage ) elementObj.parentTabPage.focus();
	if( elementObj.type != "hidden" && elementObj.focus && elementObj.style.display != "none" ) elementObj.focus();
}

Field.formatDate = function( elementObj ) {
	if( elementObj.value.length == 8 ) {
		var datevalue = elementObj.value.substring( 0, 4 ) +"-"+ elementObj.value.substring( 4, 6 ) +"-"+ elementObj.value.substring( 6, 8 );
		if( DateField.convertValueToDate(datevalue) ) {
			elementObj.value = datevalue;
			Field.modified( elementObj );
		}
	}
}

Field.getSubjectObject = function( elementObj ) {
	return document.getElementById( "title_"+ (Field.isArray(elementObj) ? elementObj[0].name : elementObj.name) );
}

Field.isMandatory = function( elementObj ) {
	return ( elementObj.className.indexOf("content_m") >= 0 );
}

Field.isReadonly = function( elementObj ) {
	return ( elementObj.className.indexOf("readonly") >= 0 );
}

Field.lock = function( elementObj, disabled ) {
	if( Field.isArray(elementObj) ) {
		for( var e = 0; e < elementObj.length; e++ )
			Field.lock( elementObj[e], disabled );
	} else {
		elementObj.className = elementObj.className.replace( /_*content_o/, "_content_o" );
		elementObj.className = elementObj.className.replace( /_*content_m/, "_content_m" );

		switch( elementObj.type ) {
		case "text":
		case "textarea":
		case "password":
		case "file":
			elementObj.readOnly = true;
		case "hidden":
			if( disabled ) elementObj.disabled = true;
			Styles.appendClassName( elementObj, Field.readonlyStyle );
			break;
		case "select-one":
			Styles.appendClassName( elementObj, Field.readonlyStyle );
		case "checkbox":
		case "radio":
			elementObj.disabled = true;
		}
	}
}

Field.modified = function( elementObj ) {
	var mStyles = Field.mandatoryStyles;
	var oStyles = Field.optionalStyles;

	if( Field.isArray(elementObj) && elementObj[0].type == "radio" )
		elementObj = elementObj[0];

	switch( elementObj.type ) {
	case "radio":
		var subjectObj = Field.getSubjectObject( elementObj );
		if( subjectObj && elementObj.checked )
			Styles.changeClassName2( subjectObj, mStyles[2], mStyles[3], oStyles[2], oStyles[3] );
		if( elementObj.checkParentTab && Field.isMandatory(elementObj) )
			elementObj.parentTabPage.modified( true );
	case "checkbox":
		return;
	}

	if( Field.isModified(elementObj) )
		Styles.changeClassName2( elementObj, mStyles[0], mStyles[1], oStyles[0], oStyles[1] );
	else
		Styles.changeClassName2( elementObj, mStyles[1], mStyles[0], oStyles[1], oStyles[0] );

	var subjectObj = Field.getSubjectObject( elementObj );
	if( subjectObj ) {
		if( elementObj.value == "" )
			Styles.changeClassName2( subjectObj, mStyles[3], mStyles[2], oStyles[3], oStyles[2] );
		else
			Styles.changeClassName2( subjectObj, mStyles[2], mStyles[3], oStyles[2], oStyles[3] );
	}

	if( elementObj.checkParentTab && Field.isMandatory(elementObj) )
		elementObj.parentTabPage.modified( elementObj.value != "" );
}

Field.release = function( elementObj ) {
	if( Field.isArray(elementObj) ) {
		for( var e = 0; e < elementObj.length; e++ )
			Field.release( elementObj[e] );
	} else {
		elementObj.className = elementObj.className.replace( /_+content_o/, "content_o" );
		elementObj.className = elementObj.className.replace( /_+content_m/, "content_m" );

		switch( elementObj.type ) {
		case "text":
		case "textarea":
		case "password":
		case "file":
			elementObj.readOnly = false;
		case "hidden":
			Styles.removeClassName( elementObj, Field.readonlyStyle );
			elementObj.disabled = false;
			break;
		case "select-one":
			Styles.removeClassName( elementObj, Field.readonlyStyle );
		case "checkbox":
		case "radio":
			elementObj.disabled = false;
			break;
		}
	}
}


/***********************************************************************************************************************
	DateField
***********************************************************************************************************************/
function DateField() {}

DateField.convertDateToValue = function( date, type ) {
	if( date == null ) return null;

	var y = date.getFullYear();
	var m = date.getMonth() + 1;
	var d = date.getDate();
	if( y < 10 ) y = "000" + y;
	else if( y < 100 ) y = "00" + y;
	else if( y < 1000 ) y = "0" + y;
	if( m < 10 ) m = "0" + m;
	if( d < 10 ) d = "0" + d;

	var value = y +"-"+ m +"-"+ d;
	if( !type || type == "ymd" ) return value;

	var h = date.getHours();
	var mi = date.getMinutes();
	var s = date.getSeconds();
	if( h < 10 ) h = "0" + h;
	if( mi < 10 ) mi = "0" + mi;
	if( s < 10 ) s = "0" + s;

	if( type == "ymdh" )
		return value +" "+ h;
	else if( type == "ymdhi" )
		return value +" "+ h +":"+ mi;
	else if( type == "ymdhis" )
		return value +" "+ h +":"+ mi +":"+ s;
	else if( type == "hi" )
		return mi +":"+ s;
	else
		return value;
}

DateField.convertValueToDate = function( value, type ) {
	var y, m, d, h, mi, s, length;

	if( value == null ) return null;
	if( !type ) {
		length = value.length;
		if( length != 10 && length != 13 && length != 16 && length != 19 )
			return null;
	} else if( type == "ymd" )
		length = 10;
	else if( type == "ymdh" )
		length = 13;
	else if( type == "ymdhi" )
		length = 16;
	else if( type == "ymdhis" )
		length = 19;
	else if( type == "hi" ) {
		if( value.length != 5 || value.charAt(2) != ":" ) return null;
		if( !isFinite(value.substring(0, 2)) || !isFinite(value.substring(3, 5)) ) return null;

		h = eval( value.substring(0, 2) );
		mi = eval( value.substring(3, 5) );
		if( h < 0 || h > 23 || mi < 0 || mi > 59 ) return null;

		var date = new Date();
		date.setHours( h );
		date.setMinutes( mi );
		return date;
	}

	if( value.length != length || value.charAt(4) != "-" || value.charAt(7) != "-" ) return null;
	if( !isFinite(value.substring(0, 4)) || !isFinite(value.substring(5, 7)) || !isFinite(value.substring(8, 10)) )
		return null;

	y = eval( value.substring(0, 4) );
	m = eval( value.substring(5, 7) );
	d = eval( value.substring(8, 10) );
	if( y <= 0 || m <= 0 || m > 12 || d <= 0 || d > 31 ) return null;

	h = mi = s = -1;
	if( length > 10 ) {
		if( value.charAt(10) != " " || !isFinite(value.substring(11, 13)) ) return null;
		h = eval( value.substring(11, 13) );
		if( h < 0 || h > 23 ) return null;
	}
	if( length > 13 ) {
		if( value.charAt(13) != ":" || !isFinite(value.substring(14, 16)) ) return null;
		mi = eval( value.substring(14, 16) );
		if( mi < 0 || mi > 59 ) return null;
	}
	if( length > 16 ) {
		if( value.charAt(16) != ":" || !isFinite(value.substring(17, 19)) ) return null;
		s = eval( value.substring(17, 19) );
		if( s < 0 || s > 59 ) return null;
	}

	var date = new Date( y, m - 1, d );
	if( date.getMonth() != (m-1) ) return null;

	if( h >= 0 ) date.setHours( h );
	if( mi >= 0 ) date.setMinutes( mi );
	if( s >= 0 ) date.setSeconds( s );

	return date;
}

DateField.getDateValue = function( elementName, type ) {
	return DateField.convertValueToDate( DateField.getValueByName(elementName), type );
}

DateField.getElementName = function( elementName ) {
	var idx = elementName.indexOf( "[" );
	if( idx > 0 ) elementName = elementName.substring( 0, idx );
	idx = elementName.indexOf( "." );
	if( idx > 0 ) elementName = elementName.substring( idx + 1 );

	return elementName;
}

DateField.getElements = function( elementName ) {
	var elementName_idx = "";
	var elementObj;
	var elementCnt = 0;
	var elementObjs = new Array();

	// setting elementName, elementName_idx
	var idx = elementName.indexOf("[");
	if( idx > 0 ) {
		elementName_idx = elementName.substring( idx );
		elementName = elementName.substring( 0, idx );
	}
	if( elementName.indexOf(".") < 0 ) elementName = "frmMain."+ elementName;

	elementObj = eval( elementName );
	if( elementObj ) {
		if( elementName_idx ) {
			if( elementName_idx != "[0]" || Field.isArray(elementObj) )
				elementObj = eval( elementName + elementName_idx );
		}
		if( elementObj ) elementObjs[elementCnt++] = elementObj;

		return ( elementCnt == 0 ? null : elementObjs );
	}

	var check_h = false;
	elementObj = eval( elementName +"_ymd" );
	if( elementObj ) {
		check_h = true;
		if( elementName_idx ) {
			if( elementName_idx != "[0]" || Field.isArray(elementObj) )
				elementObj = eval( elementName +"_ymd"+ elementName_idx );
		}
		if( elementObj ) elementObjs[elementCnt++] = elementObj;
	} else {
		elementObj = eval( elementName +"_y" );
		if( elementObj && elementName_idx ) {
			if( elementName_idx != "[0]" || Field.isArray(elementObj) )
				elementObj = eval( elementName +"_y"+ elementName_idx );
			else
				elementName_idx = null;
		}
		if( elementObj ) {
			elementObjs[elementCnt++] = elementObj;

			elementObj = eval( elementName +"_m" );
			if( elementObj && elementName_idx ) elementObj = eval( elementName +"_m"+ elementName_idx );
			if( elementObj ) {
				elementObjs[elementCnt++] = elementObj;

				elementObj = eval( elementName +"_d" );
				if( elementObj && elementName_idx ) elementObj = eval( elementName +"_d"+ elementName_idx );
				if( elementObj ) {
					check_h = true;
					elementObjs[elementCnt++] = elementObj;
				}
			}
		}
	}

	if( check_h ) {
		elementObj = eval( elementName +"_h" );
		if( elementObj && elementName_idx ) elementObj = eval( elementName +"_h"+ elementName_idx );
		if( elementObj ) {
			elementObjs[elementCnt++] = elementObj;

			elementObj = eval( elementName +"_mi" );
			if( elementObj && elementName_idx ) elementObj = eval( elementName +"_mi"+ elementName_idx );
			if( elementObj ) elementObjs[elementCnt++] = elementObj;
		}
	}

	return ( elementCnt == 0 ? null : elementObjs );
}

DateField.getSubjectObjectByName = function( elementName ) {
	return document.getElementById( "title_"+ DateField.getElementName(elementName) );
}

DateField.getValueByName = function( elementName ) {
	var elementObjs = DateField.getElements( elementName );
	if( elementObjs ) {
		elementName = DateField.getElementName( elementName );

		var dateValue = "";
		for( var e = 0; e < elementObjs.length; e++ ) {
			value = elementObjs[e].value;
			if( elementObjs[e].name == elementName )
				dateValue = value;
			else if( elementObjs[e].name == elementName +"_ymd" )
				dateValue = value;
			else if( elementObjs[e].name == elementName +"_y" )
				dateValue = ("0000"+ value).substring( value.length() );
			else if( elementObjs[e].name == elementName +"_m" )
				dateValue = dateValue +"-"+ ("00"+ value).substring( value.length() );
			else if( elementObjs[e].name == elementName +"_d" )
				dateValue = dateValue +"-"+ ("00"+ value).substring( value.length() );
			else if( elementObjs[e].name == elementName +"_h" )
				dateValue = dateValue +" "+ ("00"+ value).substring( value.length() );
			else if( elementObjs[e].name == elementName +"_mi" )
				dateValue = dateValue +":"+ ("00"+ value).substring( value.length() );
		}

		return dateValue;
	}

	return null;
}

DateField.modifiedByName = function( elementName ) {
	var mStyles = Field.mandatoryStyles;
	var oStyles = Field.optionalStyles;

	var elementObjs = DateField.getElements( elementName );
	if( elementObjs ) {
		var changed = false;
		for( var e = 0; e < elementObjs.length; e++ ) {
			if( Field.isModified(elementObjs[e]) ) {
				changed = true;
				break;
			}
		}

		if( changed ) {
			for( var e = 0; e < elementObjs.length; e++ )
				Styles.changeClassName2( elementObjs[e], mStyles[0], mStyles[1], oStyles[0], oStyles[1] );
		} else {
			for( var e = 0; e < elementObjs.length; e++ )
				Styles.changeClassName2( elementObjs[e], mStyles[1], mStyles[0], oStyles[1], oStyles[0] );
		}

		var value = DateField.getDateValue( elementName );
		var subjectObj = DateField.getSubjectObjectByName( elementName );
		if( subjectObj ) {
			if( value )
				Styles.changeClassName2( subjectObj, mStyles[2], mStyles[3], oStyles[2], oStyles[3] );
			else
				Styles.changeClassName2( subjectObj, mStyles[3], mStyles[2], oStyles[3], oStyles[2] );
		}

		if( elementObjs[0].checkParentTab && Field.isMandatory(elementObjs[0]) )
			elementObjs[0].parentTabPage.modified( value ? true : false );
	}
}

DateField.setDate = function( elementName, date, dateonly, callOnchange ) {
	DateField.setValue( elementName, DateField.convertDateToValue(date, "ymdhis"), dateonly, callOnchange );
}

DateField.setValue = function( elementName, value, dateonly, callOnchange ) {
	if( !value ) value = "";

	var elementObjs = DateField.getElements( elementName );
	if( elementObjs ) {
		elementName = DateField.getElementName( elementName );

		for( var e = 0; e < elementObjs.length; e++ ) {
			if( elementObjs[e].name == elementName )
				elementObjs[e].value = value.substring( 0, 10 );
			else if( elementObjs[e].name == elementName +"_ymd" )
				elementObjs[e].value = value.substring( 0, 10 );
			else if( elementObjs[e].name == elementName +"_y" )
				elementObjs[e].value = value.substring( 0, 4 );
			else if( elementObjs[e].name == elementName +"_m" )
				elementObjs[e].value = value.substring( 5, 7 );
			else if( elementObjs[e].name == elementName +"_d" )
				elementObjs[e].value = value.substring( 8, 10 );
			else if( !dateonly ) {
				if( elementObjs[e].name == elementName +"_h" )
					elementObjs[e].value = value.substring( 11, 13 );
				else if( elementObjs[e].name == elementName +"_mi" )
					elementObjs[e].value = value.substring( 14, 16 );
				else if( elementObjs[e].name == elementName +"_s" )
					elementObjs[e].value = value.substring( 17, 19 );
			}
			if( callOnchange && elementObjs[e].onchange ) elementObjs[e].onchange();
		}
	}
}


/***********************************************************************************************************************
	TimeField
***********************************************************************************************************************/
function TimeField() {}

TimeField.changed = function( elementObj ) {
	var elementName = elementObj.name;
	var elementName_suffix = elementName.substring( elementName.length - 2 );
	if( elementName_suffix == "_h" || elementName_suffix == "_m" )
		elementName = elementName.substring( 0, elementName.length - 2 );
	else
		return;

	var elementObj_h = null;
	var elementObj_m = null;
	var elementObj_value = eval( elementObj.form.name +"."+ elementName );

	if( !elementObj_value )
		return;
	else if( Field.isArray(elementObj_value) ) {
		elementObj_m = eval( elementObj.form.name +"."+ elementName +"_m" );
		elementObj_h = eval( elementObj.form.name +"."+ elementName +"_h" );
		if( elementName_suffix == "_h" ) {
			for( var i = 0; i < elementObj_h.length; i++ ) {
				if( elementObj_h[i] == elementObj ) {
					elementObj_m = elementObj_m[i];
					elementObj_value = elementObj_value[i];
					break;
				}
			}
			elementObj_h = elementObj;
		} else {
			for( var i = 0; i < elementObj_m.length; i++ ) {
				if( elementObj_m[i] == elementObj ) {
					elementObj_h = elementObj_h[i];
					elementObj_value = elementObj_value[i];
					break;
				}
			}
			elementObj_m = elementObj;
		}
	} else {
		if( elementName_suffix == "_h" ) {
			elementObj_h = elementObj;
			elementObj_m = eval( elementObj.form.name +"."+ elementName +"_m" );
		} else {
			elementObj_h = eval( elementObj.form.name +"."+ elementName +"_h" );
			elementObj_m = elementObj;
		}
	}

	if( elementObj_h.value && elementObj_m.value )
		elementObj_value.value = elementObj_h.value + ":" + elementObj_m.value;
	else
		elementObj_value.value = "";
	if( elementObj_value.onchange ) elementObj_value.onchange();
}

TimeField.checkValue = function( value ) {
	if( value == "" ) return true;
	if( value.length != 5 || value.charAt(2) != ':' ) return false;
	if( value.charAt(0) < '0' || value.charAt(0) > '2' ) return false;
	if( value.charAt(1) < '0' || value.charAt(1) > '9' ) return false;
	if( value.charAt(3) < '0' || value.charAt(3) > '5' ) return false;
	if( value.charAt(4) < '0' || value.charAt(4) > '9' ) return false;
	if( value.charAt(0) == '2' && value.charAt(1) > '3' ) return false;

	return true;
}

TimeField.getElements = function( elementName ) {
	var elementName_idx = "";

	// setting elementName, elementName_idx
	var idx = elementName.indexOf("[");
	if( idx > 0 ) {
		elementName_idx = elementName.substring( idx );
		elementName = elementName.substring( 0, idx );
	}
	if( elementName.indexOf(".") < 0 ) elementName = "frmMain."+ elementName;

	// find _h, _m
	var elementObj_h = eval( elementName +"_h" );
	if( elementObj_h && elementName_idx ) {
		if( elementName_idx != "[0]" || Field.isArray(elementObj_h) )
			elementObj_h = eval( elementName +"_h"+ elementName_idx );
		else
			elementName_idx = null;
	}
	if( elementObj_h ) {
		var elementObj_m = eval( elementName +"_m" );
		if( elementObj_m && elementName_idx ) elementObj_m = eval( elementName +"_m"+ elementName_idx );
		if( elementObj_m ) return new Array( elementObj_h, elementObj_m );
	}

	var elementObj = eval( elementName );
	if( elementObj && elementName_idx ) elementObj = eval( elementName + elementName_idx );
	if( !elementObj ) return null;

	return new Array( elementObj );
}

TimeField.getSubjectObject = function( elementObj ) {
	var elementName = elementObj.name;
	var elementName_suffix = elementName.substring( elementName.length - 2 );
	if( elementName_suffix == "_h" || elementName_suffix == "_m" )
		elementName = elementName.substring( 0, elementName.length - 2 );

	return document.getElementById( "title_"+ elementName );
}

TimeField.getValue = function( elementObj ) {
	if( !elementObj.value ) return null;

	var elementName = elementObj.name;
	var elementName_suffix = elementName.substring( elementName.length - 2 );
	if( elementName_suffix == "_h" ) {
		elementName = elementName.substring( 0, elementName.length - 2 );

		var elementObj_m = eval( elementObj.form.name +"."+ elementName +"_m" );
		if( Field.isArray(elementObj_m) ) {
			var elementObj_h = eval( elementObj.form.name +"."+ elementName +"_h" );
			for( var i = 0; i < elementObj_h.length; i++ ) {
				if( elementObj_h[i] == elementObj ) {
					elementObj_m = elementObj_m[i];
					break;
				}
			}
		}
		if( !elementObj_m.value ) return "";

		return elementObj.value + ":" + elementObj_m.value;
	} else if( elementName_suffix == "_m" ) {
		elementName = elementName.substring( 0, elementName.length - 2 );

		var elementObj_h = eval( elementObj.form.name +"."+ elementName +"_h" );
		if( Field.isArray(elementObj_h) ) {
			var elementObj_m = eval( elementObj.form.name +"."+ elementName +"_m" );
			for( var i = 0; i < elementObj_m.length; i++ ) {
				if( elementObj_m[i] == elementObj ) {
					elementObj_h = elementObj_h[i];
					break;
				}
			}
		}
		if( !elementObj_h.value ) return "";

		return elementObj_h.value + ":" + elementObj.value;
	} else
		return elementObj.value;
}

TimeField.getValueByName = function( elementName ) {
	var elementObjs = TimeField.getElements( elementName );

	if( elementObjs == null )
		return "";
	else if( elementObjs.length == 1 )
		return elementObjs[0].value;
	else {
		if( elementObjs[0].value == "" || elementObjs[1].value == "" )
			return "";
		else
			return elementObjs[0].value +":"+ elementObjs[1].value;
	}
}

TimeField.modified = function( elementObj ) {
	var mStyles = Field.mandatoryStyles;
	var oStyles = Field.optionalStyles;

	if( Field.isModified(elementObj) )
		Styles.changeClassName2( elementObj, mStyles[0], mStyles[1], oStyles[0], oStyles[1] );
	else
		Styles.changeClassName2( elementObj, mStyles[1], mStyles[0], oStyles[1], oStyles[0] );

	var value = TimeField.getValue( elementObj );
	var subjectObj = TimeField.getSubjectObject( elementObj );
	if( subjectObj ) {
		if( value )
			Styles.changeClassName2( subjectObj, mStyles[2], mStyles[3], oStyles[2], oStyles[3] );
		else
			Styles.changeClassName2( subjectObj, mStyles[3], mStyles[2], oStyles[3], oStyles[2] );
	}

	if( elementObj.checkParentTab && Field.isMandatory(elementObj) )
		elementObj.parentTabPage.modified( value ? true : false );
}

TimeField.setValue = function( elementName, value, callOnchange ) {
	if( !value ) value = "";

	var elementObjs = TimeField.getElements( elementName );
	if( elementObjs ) {
		var idx = elementName.indexOf( "[" );
		if( idx > 0 ) elementName = elementName.substring( 0, idx );
		idx = elementName.indexOf( "." );
		if( idx > 0 ) elementName = elementName.substring( idx + 1 );

		for( var e = 0; e < elementObjs.length; e++ ) {
			if( elementObjs[e].name == elementName )
				elementObjs[e].value = value;
			else if( elementObjs[e].name == elementName +"_h" )
				elementObjs[e].value = value.substring( 0, 2 );
			else if( elementObjs[e].name == elementName +"_m" )
				elementObjs[e].value = value.substring( 3, 5 );
			if( callOnchange && elementObjs[e].onchange ) elementObjs[e].onchange();
		}
	}
}
