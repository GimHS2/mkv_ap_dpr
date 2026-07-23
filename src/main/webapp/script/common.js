/*
	File Name:	common.js
	Version:	2.2.7c(dpr)

	Description:

	Note:
		CheckBox
		DateWeek
		DialogBox
		Field
		FieldGroup
		LevelField
		Select
		Styles

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.7c	신규 UI/UX 적용
	jbaek		2018/12/30		2.2.7c	FlexListWriter function added.
	GimHS		2015/04/30		2.2.7	CrossBrowsing 적용: LevelField.disableLowerLevelFieldOfMultipleValue(), LevelField.getValue() 함수에서 JQuery의 멀티 select를 지원하도록 수정
	GimHS		2014/08/29		2.2.6	CrossBrowsing 적용:
										 -> option 태그의 defaultSelected 속성값을 바꾸면 selected 속성값이 같이 바뀌는 문제가 있어서 새로운 속성(defaultSelected2) 추가
										 -> text 태그의 defaultValue 속성값을 바꾸면 value 속성값이 같이 바뀌는 문제가 있어서 새로운 속성(defaultValue2) 추가
										 -> 태그에서 지원하지 않는 속성의 값을 가져올때나 넣을때 getAttribute(), setAttribute()를 사용하도록 수정
										 -> LevelField.focus(): Chrome에서 object.focus()가 동작하지 않아 window.setTimeout()을 이용하여 object.focus() 호출
	GimHS		2012/12/31		2.2.5	CrossBrowsing 적용: DialogBox.prototype.showAtPointer(): eventObj를 먼저 사용하도록 변경
	stghr12		2011/10/31		2.2.4	DialogBox에 siblingDialogBoxs 추가
	stghr12		2010/12/31		2.2.3	Field.getValue(): "checkbox" 지원
	stghr12		2010/07/31		2.2.2	Field.setCursor(); 추가
	stghr12		2010/01/31		2.2.1	Field.initFieldDefaultToNull(): "checkbox"는 무시함
										LevelField.getLastLevel() 추가
	stghr12		2009/10/31		2.2.0	create(main.js 복사)

*/

/***********************************************************************************************************************
	MULTIPLE_SELECT_MAXROWS
	MULTIPLE_SELECT_VALUE
***********************************************************************************************************************/
var MULTIPLE_SELECT_MAXROWS = 18;
var MULTIPLE_SELECT_VALUE = "__MULTIPLE_SELECT__";


/***********************************************************************************************************************
	CheckBox
		CheckBox.change( checkObj, checked );
		CheckBox.checkAll( checkObj );
		CheckBox.getObjectValues( checkObj, valueObj );
		CheckBox.getQueryValue( checkObj, name, ... );
		CheckBox.getValues( checkObj );
***********************************************************************************************************************/
function CheckBox() {}

CheckBox.change = function( checkObj, checked ) {
	if( checkObj == null )
		return;
	else if( checkObj[0] ) {
		for( var i = 0; i < checkObj.length; i++ ) {
			if( checkObj[i].disabled ) continue;
			checkObj[i].checked = checked;
			if( checkObj[i].onclick ) checkObj[i].onclick();
		}
	} else {
		if( checkObj.disabled ) return;
		checkObj.checked = checked;
		if( checkObj.onclick ) checkObj.onclick();
	}
}

CheckBox.checkAll = function( checkObj, titleCheckObj ) {
	if( checkObj == null )
		return;
	else if( checkObj[0] ) {
		for( var i = 0; i < checkObj.length; i++ ) {
			if( checkObj[i].disabled ) continue;
			if( !checkObj[i].checked ) {
				CheckBox.change( checkObj, true );
				if( titleCheckObj ) {
					$(titleCheckObj).removeClass( "check-part" ).addClass( "check-all" );
					var line = $(checkObj).closest( "tr" );
					line.addClass( "line-checked" );
				}
				return;
			}
		}
		CheckBox.change( checkObj, false );
		if( titleCheckObj ) {
			$(titleCheckObj).removeClass();
			var line = $(checkObj).closest( "tr" );
			line.removeClass( "line-checked" );
		}
	} else {
		CheckBox.change( checkObj, !checkObj.checked );
		if( titleCheckObj ) {
			var line = $(checkObj).closest( "tr" );
			if( checkObj.checked ) {
				$(titleCheckObj).removeClass( "check-part" ).addClass( "check-all" );
				line.addClass( "line-checked" );
			} else {
				$(titleCheckObj).removeClass();
				line.removeClass( "line-checked" );
			}
		}
	}
}

CheckBox.getObjectValues = function( checkObj, valueObj ) {
	var cnt = 0;
	var selected = new Array();

	if( checkObj == null || valueObj == null )
		return null;
	else if( checkObj[0] ) {
		for( var i = 0; i < checkObj.length; i++ ) {
			if( checkObj[i].checked )
				selected[cnt++] = valueObj[i].value;
		}
	} else if( checkObj.checked )
		selected[cnt++] = valueObj.value;

	return( cnt > 0 ? selected : null );
}

CheckBox.getQueryValue = function( checkObj, name ) {
	var selected = CheckBox.getValues( checkObj );
	if( selected ) {
		var query = "";
		var args = CheckBox.getQueryValue.arguments;
		if( args.length == 2 ) {
			for( var i = 0; i < selected.length; i++ )
				query += "&"+ name +"="+ encodeURIComponent(selected[i]);
		} else {
			names = new Array( args.length - 1 );
			for( var n = 1; n < args.length; n++ )
				names[n - 1] = args[n];

			for( var i = 0; i < selected.length; i++ ) {
				var values = selected[i].split(";");
				for( var n = 0; n < names.length; n++ )
					if( names[n] )
						query += "&"+ names[n] +"="+ encodeURIComponent(values[n]);
			}
		}
		return query.substring(1);
	}

	return null;
}

CheckBox.getValues = function( checkObj ) {
	return CheckBox.getObjectValues( checkObj, checkObj );
}


/***********************************************************************************************************************
	DateWeek
		DateWeek( weekvalue );
		DateWeek( date );
		DateWeek( year, week );
		DateWeek.getYearFirstDay( y );
		DateWeek.prototype.getDate();
		DateWeek.prototype.getWeek();
		DateWeek.prototype.getYear();
		DateWeek.prototype.toString();
***********************************************************************************************************************/
function DateWeek() {
	var args = DateWeek.arguments;

	var wdate;
	if( args.length == 0 ) {
		wdate = new Date();
		wdate = new Date( wdate.getFullYear(), wdate.getMonth(), wdate.getDate() );
	} else if( args.length == 1 ) {
		if( typeof args[0] == "string" ) {
			if( !args[0] || !isFinite(args[0]) || args[0].length < 6 || eval(args[0]) < 0 ) return null;
			wdate = DateWeek.getYearFirstDay( args[0].substring(0, 4) );
			wdate.setDate( wdate.getDate() + args[0].substring(4, 6) * 7 - 7 );
		} else
			wdate = new Date( args[0].getFullYear(), args[0].getMonth(), args[0].getDate() );
	} else if( args.length == 2 ) {
		if( !args[0] || !args[1] || !isFinite(args[0]) || !isFinite(args[1]) ) return null;
		wdate = DateWeek.getYearFirstDay( args[0] );
		wdate.setDate( wdate.getDate() + args[1] * 7 - 7 );
	} else
		return null;

	wdate.setDate( wdate.getDate() - ( wdate.getDay() == 0 ? 6 : wdate.getDay() - 1 ) );
	var y = 0, w = 0;
	for( y = wdate.getFullYear() + 1; w <= 0; y-- ) {
		w = ( wdate - DateWeek.getYearFirstDay(y) ) / ( 24 * 60 * 60 * 1000 ) / 7 + 1;
		if( w > 0 ) break;
	}

	this.date = wdate;
	this.year = y;
	this.week = w;
	this.weekstring = ( w < 10 ? y +"0"+ w : y +""+ w );
}

DateWeek.getYearFirstDay = function( y ) {
	var date = new Date( y, 0, 1 );

	if( date.getDay() == 0 )
		date.setDate( date.getDate() + 1 );
	else if( date.getDay() >= 5 )
		date.setDate( date.getDate() + 8 - date.getDay() );
	else
		date.setDate( date.getDate() + 1 - date.getDay() );

	return date;
}

DateWeek.prototype.getDate = function() {
	return this.date;
}

DateWeek.prototype.getWeek = function() {
	return this.week;
}

DateWeek.prototype.getYear = function() {
	return this.year;
}

DateWeek.prototype.toString = function() {
	return this.weekstring;
}


/***********************************************************************************************************************
	DialogBox
		DialogBox( className, siblingDialogBoxs );
		DialogBox.hideAll( siblingDialogBoxs );
		DialogBox.prototype.destory();
		DialogBox.prototype.getHeight();
		DialogBox.prototype.getWidth();
		DialogBox.prototype.hide( remove );
		DialogBox.prototype.moveTo( left, top );
		DialogBox.prototype.resize( width, height );
		DialogBox.prototype.setInnerHTML( innerHTML );
		DialogBox.prototype.show();
		DialogBox.prototype.showAtPointer();
***********************************************************************************************************************/
function DialogBox( className, siblingDialogBoxs ) {
	if( !DialogBox.doc ) {
		DialogBox.doc = document.createElement( "span" );
		document.body.insertBefore( DialogBox.doc, document.body.childNodes[0] );
	}

	this.appended = false;
	this.span = document.createElement( "span" );
	this.span.className = className;
	this.iframe = document.createElement( "iframe" );
	this.siblingDialogBoxs = siblingDialogBoxs;

	this.span.iframe = this.iframe;
	this.span.onresize = function() {
		this.iframe.style.width = this.offsetWidth;
		this.iframe.style.height = this.offsetHeight;
	};

	this.iframe.style.position = this.span.style.position = 'absolute';
	this.iframe.style.display = this.span.style.display = "none";
	this.iframe.style.zIndex = 2;
	this.span.style.zIndex = this.iframe.style.zIndex + 1;

	if( !DialogBox.dialogBoxs )
		DialogBox.dialogBoxs = new Array();
	DialogBox.dialogBoxs.push( this );
}

DialogBox.hideAll = function( siblingDialogBoxs ) {
	if( DialogBox.dialogBoxs ) {
		for( var i = 0; i < DialogBox.dialogBoxs.length; i++ ) {
			var dialogBox = DialogBox.dialogBoxs[i];

			if( siblingDialogBoxs ) {
				for( var j = 0; j < siblingDialogBoxs.length; j++ )
					if( dialogBox == siblingDialogBoxs[j] ) {
						dialogBox = null;
						break;
					}
			}
			if( dialogBox ) dialogBox.hide();
		}
	}
}

DialogBox.prototype.destory = function() {
	if( this.appended ) {
		document.body.removeChild( this.span );
		document.body.removeChild( this.iframe );
	}
	this.iframe = this.span = null;
}

DialogBox.prototype.getHeight = function() {
	return this.span.offsetHeight;
}

DialogBox.prototype.getWidth = function() {
	return this.span.offsetWidth;
}

DialogBox.prototype.hide = function( remove ) {
	if( this.appended ) {
		this.iframe.style.display = this.span.style.display = "none";
		if( remove ) {
			this.appended = false;
			DialogBox.doc.removeChild( this.span );
			DialogBox.doc.removeChild( this.iframe );
		}
	}
}

DialogBox.prototype.moveTo = function( left, top ) {
	this.iframe.style.left = this.span.style.left = left;
	this.iframe.style.top = this.span.style.top = top;
}

DialogBox.prototype.resize = function( width, height ) {
	if( width ) this.iframe.style.width = this.span.style.width = width;
	if( height ) this.iframe.style.height = this.span.style.height = height;
}

DialogBox.prototype.setInnerHTML = function( innerHTML ) {
	this.innerHTML = innerHTML;
	this.span.innerHTML = this.innerHTML;
}

DialogBox.prototype.show = function() {
	DialogBox.hideAll( this.siblingDialogBoxs );

	if( !this.appended ) {
		DialogBox.doc.appendChild( this.span );
		DialogBox.doc.appendChild( this.iframe );
		this.appended = true;
	}
	this.span.style.display = this.iframe.style.display = "";
	this.span.innerHTML = this.innerHTML;
}

DialogBox.prototype.showAtPointer = function() {
	var offsetX, offsetY;
	if( eventObj ) {
		offsetX = document.body.scrollLeft + eventObj.clientX;
		offsetY = document.body.scrollTop + eventObj.clientY;
	} else if( event ) {
		offsetX = document.body.scrollLeft + event.clientX;
		offsetY = document.body.scrollTop + event.clientY;
	} else {
		var newEventObj = document.createEventObject();
		offsetX = document.body.scrollLeft + newEventObj.clientX;
		offsetY = document.body.scrollTop + newEventObj.clientY;
	}
	this.show();

	if( offsetY >= document.body.clientHeight - this.span.clientHeight )
		offsetY -= this.span.clientHeight;
	if( offsetX >= document.body.clientWidth - this.span.clientWidth )
		offsetX -= this.span.clientWidth;

	this.moveTo( offsetX, offsetY );
}


/***********************************************************************************************************************
	Field
		Field.getValue( elementObj );
		Field.initFieldDefaultToNull( elementObjs, onchange );
		Field.isArray( elementObj );
		Field.isModified( elementObj );
		Field.setCursor( elementObj );
		Field.setDefaultValue( elementObj, value );
		Field.setValue( elementObj, value, callOnchange );
***********************************************************************************************************************/
function Field() {}

Field.getValue = function( elementObj ) {
	if( Field.isArray(elementObj) ) {
		switch( elementObj[0].type ) {
		case "radio":
			for( var e = 0; e < elementObj.length; e++ )
				if( elementObj[e].checked )
					return elementObj[e].value;
			break;
		case "checkbox":
			var value = new Array();

			for( var e = 0; e < elementObj.length; e++ )
				if( elementObj[e].checked )
					value.push( elementObj[e].value );

			if( value.length > 0 ) return value;
		}
	} else {
		switch( elementObj.type ) {
		case "radio":
			elementObj = eval( elementObj.form.name +"."+ elementObj.name );
			if( elementObj[0] ) return Field.getValue( elementObj );
		case "checkbox":
			if( elementObj.checked ) return elementObj.value;
			break;
		default:
			return elementObj.value;
		}
	}

	return "";
}

Field.initFieldDefaultToNull = function( elementObjs, onchange ) {
	for( var e = 0; e < elementObjs.length; e++ ) {
		if( elementObjs[e] ) {
			if( Field.isArray(elementObjs[e]) )
				Field.initFieldDefaultToNull( elementObjs[e], onchange );
			else {
				var defaultValue = "";
				if( elementObjs[e].type == "select-one" && elementObjs[e].options != null && elementObjs[e].options[0] != null )
					defaultValue = elementObjs[e].options[0].value;
				else if( elementObjs[e].type == "select-multiple" )
					defaultValue = null;
				else if( elementObjs[e].type == "checkbox" )
					continue;

				if( Field.setDefaultValue(elementObjs[e], defaultValue) && onchange )
					onchange( elementObjs[e] );
			}
		}
	}
}

Field.isArray = function( elementObj ) {
	return( elementObj[0] && elementObj.type != "select-one" && elementObj.type != "select-multiple" );
}

Field.isModified = function( elementObj ) {
	switch( elementObj.type ) {
	case "text":
	case "textarea":
	case "password":
	case "file":
	case "hidden":
		return ( (elementObj.getAttribute("defaultValue2") != null ? elementObj.getAttribute("defaultValue2") : "") != elementObj.value );
	case "select-one":
	case "select-multiple":
		for( var i = 0; i < elementObj.options.length; i++ ) {
			if( (elementObj.options[i].getAttribute("defaultSelected2") == "Y") != elementObj.options[i].selected ) {
				if( elementObj.type == "select-one" && i == 0 && elementObj.options[i].selected ) {
					for( ; i < elementObj.options.length; i++ )
						if( elementObj.options[i].getAttribute("defaultSelected2") == "Y" )
							return true;
					return false;
				}
				return true;
			}
		}
		return false;
	case "radio":
	case "checkbox":
		return ( elementObj.defaultChecked != elementObj.checked );
	default:
		return false;
	}
}

Field.setCursor = function( elementObj ) {
	if( elementObj.setSelectionRange ) {
		elementObj.focus();
		elementObj.setSelectionRange( elementObj.value.length, elementObj.value.length );
	} else if( elementObj.createTextRange ) {
		var range = elementObj.createTextRange();

		range.moveStart( 'character', elementObj.value.length );
		range.select();
	}
}

Field.setDefaultValue = function( elementObj, value ) {
	switch( elementObj.type ) {
	case "select-one":
	case "select-multiple":
		var idx = -1;

		for( var i = 0; i < elementObj.options.length; i++ ) {
			if( elementObj.options[i].getAttribute("defaultSelected2") == "Y" ) {
				if( idx < 0 )
					idx = i;
				else {
					idx = -2;
					break;
				}
			}
		}
		if( idx > -2 ) {
			if( idx < 0 ) idx = 0;
			if( elementObj.options[idx] && elementObj.options[idx].value == value )
				break;
		}

		idx = 0;
		for( var i = 0; i < elementObj.options.length; i++ ) {
			elementObj.options[i].setAttribute( "defaultSelected2", (elementObj.options[i].value == value ? "Y" : "") );
			if( elementObj.options[i].getAttribute("defaultSelected2") == "Y" )
				idx = 1;
		}
		if( elementObj.type == "select-one" && idx == 0 ) elementObj.options[0].setAttribute( "defaultSelected2", "Y" );

		return true;
	case "checkbox":
	case "radio":
		if( elementObj.defaultChecked != (elementObj.value == value) ) {
			elementObj.defaultChecked = ( elementObj.value == value );
			elementObj.defaultValue = value;
			return true;
		}
		break;
	case "text":
	case "textarea":
	case "password":
	case "hidden":
		if( elementObj.defaultValue != value ) {
			elementObj.setAttribute( "defaultValue2", value );
			return true;
		}
		break;
	}

	return false;
}

Field.setValue = function( elementObj, value, callOnchange ) {
	if( Field.isArray(elementObj) ) {
		if( elementObj[0].type == "radio" ) {
			for( var e = 0; e < elementObj.length; e++ )
				if( elementObj[e].value == value ) {
					elementObj[e].checked = true;
					if( callOnchange && elementObj[e].onclick ) elementObj[e].onclick();
					break;
				}
		}
	} else {
		switch( elementObj.type ) {
		case "checkbox":
		case "radio":
			if( elementObj.value == value ) {
				elementObj.checked = true;
				if( callOnchange && elementObj.onclick ) elementObj.onclick();
			}
			break;
		default:
			elementObj.value = value;
			if( callOnchange && elementObj.onchange ) elementObj.onchange();
		}
	}
}


/***********************************************************************************************************************
	FieldGroup
		FieldGroup( formObj );
		FieldGroup.clearContent( contentObj );
		FieldGroup.getFieldArray( FIELDs );
		FieldGroup.prototype.disableAllFields();
		FieldGroup.prototype.disableTempFields();
		FieldGroup.prototype.getFields();
		FieldGroup.prototype.getTempFields();
		FieldGroup.prototype.initFieldDefaultToNull( onchange );
		FieldGroup.prototype.resetContent();
		FieldGroup.prototype.saveContents( contentObj );
		FieldGroup.prototype.saveFields( fieldObjs );
		FieldGroup.prototype.saveFields( formName, fieldNames );
		FieldGroup.prototype.saveTempFields( fieldObjs );
		FieldGroup.prototype.saveTempFields( formName, fieldNames );
***********************************************************************************************************************/
function FieldGroup() {
	this.CONTENTs = new Array();
	this.FIELDs = new Array();
	this.TEMP_FIELDs = new Array();
}

FieldGroup.clearContent = function( contentObj ) {
	if( contentObj[0] ) {
		for( var c = 0; c < contentObj.length; c++ )
			contentObj[c].innerHTML = "";
	} else
		contentObj.innerHTML = "";
}

FieldGroup.getFieldArray = function( FIELDs ) {
	var elementObjs = new Array();

	for( var f = 0; f < FIELDs.length; f++ ) {
		var elementObj = FIELDs[f];
		if( typeof elementObj == "string" )
			elementObj = eval( elementObj );
		if( elementObj ) elementObjs.push( elementObj );
	}

	return elementObjs;
}

FieldGroup.prototype.disableAllFields = function() {
	this.disableTempFields();

	var elementObjs = FieldGroup.getFieldArray( this.FIELDs );
	for( var e = 0; e < elementObjs.length; e++ )
		elementObjs[e].disabled = true;
}

FieldGroup.prototype.disableTempFields = function() {
	var elementObjs = FieldGroup.getFieldArray( this.TEMP_FIELDs );
	for( var e = 0; e < elementObjs.length; e++ )
		elementObjs[e].disabled = true;
}

FieldGroup.prototype.getFields = function() {
	return FieldGroup.getFieldArray( this.FIELDs );
}

FieldGroup.prototype.getTempFields = function() {
	return FieldGroup.getFieldArray( this.TEMP_FIELDs );
}

FieldGroup.prototype.initFieldDefaultToNull = function( onchange ) {
	Field.initFieldDefaultToNull( FieldGroup.getFieldArray( this.FIELDs ), onchange );
	Field.initFieldDefaultToNull( FieldGroup.getFieldArray( this.TEMP_FIELDs ), onchange );
}

FieldGroup.prototype.resetContent = function() {
	for( var c = 0; c < this.CONTENTs.length; c++ )
		this.CONTENTs[c].innerHTML = this.CONTENTs[c].sourceHTML;
}

FieldGroup.prototype.saveContents = function( contentObj ) {
	if( contentObj[0] ) {
		for( var c = 0; c < contentObj.length; c++ ) {
			contentObj[c].sourceHTML = contentObj[c].innerHTML;
			this.CONTENTs.push( contentObj[c] );
		}
	} else {
		contentObj.sourceHTML = contentObj.innerHTML;
		this.CONTENTs.push( contentObj );
	}
}

FieldGroup.prototype.saveFields = function( arg1, arg2 ) {
	if( typeof arg2 == "undefined" ) {
		var fieldObjs = arg1;

		for( var f = 0; f < fieldObjs.length; f++ )
			this.FIELDs.push( fieldObjs[f] );
	} else {
		var formName = arg1;
		var fieldNames = arg2;

		for( var f = 0; f < fieldNames.length; f++ ) {
			if( eval(formName +"."+ fieldNames[f]) )
				this.FIELDs.push( formName +"."+ fieldNames[f] );
		}
	}
}

FieldGroup.prototype.saveTempFields = function( arg1, arg2 ) {
	if( typeof arg2 == "undefined" ) {
		var fieldObjs = arg1;

		for( var f = 0; f < fieldObjs.length; f++ )
			this.TEMP_FIELDs.push( fieldObjs[f] );
	} else {
		var formName = arg1;
		var fieldNames = arg2;

		for( var f = 0; f < fieldNames.length; f++ ) {
			if( eval(formName +"."+ fieldNames[f]) )
				this.TEMP_FIELDs.push( formName +"."+ fieldNames[f] );
		}
	}
}


/***********************************************************************************************************************
	LevelField
		LevelField.blur( selectObj_m );
		LevelField.disable( selectObj, disableAll );
		LevelField.disableLowerLevelFieldOfMultipleValue( formName, name );
		LevelField.extractLevel( selectObj );
		LevelField.extractName( selectObj );
		LevelField.focus( selectObj_s );
		LevelField.getContent( nameOrSelectObj, index );
		LevelField.getContents( nameOrSelectObj );
		LevelField.getElementNames( name );
		LevelField.getElements( formName, name );
		LevelField.getLastLevel( formName, name );
		LevelField.getTempElementNames( name );
		LevelField.getTempElements( formName, name );
		LevelField.getValue( selectObj, multiple );
		LevelField.modified( selectObj, onchange );
***********************************************************************************************************************/
function LevelField() {}

LevelField.blur = function( selectObj_m ) {
	var selectObj_s = eval( selectObj_m.form.name +"._"+ selectObj_m.name );

	selectObj_s.tabIndex = selectObj_s.getAttribute( "tabIndex_org" );
	selectObj_m.style.display = "none";

	var value = "";
	for( var i = 0; i < selectObj_m.options.length; i++ )
		if( selectObj_m.options[i].selected ) {
			if( selectObj_m.options[i].value == "" )
				selectObj_m.options[i].selected = false;
			else if( value ) {
				value = MULTIPLE_SELECT_VALUE;
				break;
			} else
				value = selectObj_m.options[i].value;
		}

	if( selectObj_s.value != value ) {
		selectObj_s.value = value;
		if( selectObj_s.onchange ) selectObj_s.onchange();
	} else {
		if( value == MULTIPLE_SELECT_VALUE && selectObj_s.onchange ) selectObj_s.onchange();
	}
	if( selectObj_m.onchange ) selectObj_m.onchange();
}

LevelField.disable = function( selectObj, disableAll ) {
	var formName = selectObj.form.name;
	var name = LevelField.extractName( selectObj );

	var selectObjs = LevelField.getTempElements( formName, name );
	if( selectObjs ) {
		for( var i = 0; i < selectObjs.length; i++ )
			selectObjs[i].disabled = true;
	}

	if( disableAll ) {
		var selectObjs = LevelField.getElements( formName, name );
		if( selectObjs ) {
			for( var i = 0; i < selectObjs.length; i++ )
				selectObjs[i].disabled = true;
		}
	}
}

LevelField.disableLowerLevelFieldOfMultipleValue = function( formName, name, isJQuery ) {
	var multiple = false;

	for( var i = 1; i <= 9; i++ ) {
		if( isJQuery ) {
			if( multiple ) {
				$("#"+ name + i).multipleSelect( "uncheckAll" );
				$("#"+ name + i).multipleSelect( "disable" );
			}

			var values = $("#"+ name + i).multipleSelect( "getSelects" );
			if( values.length > 1 ) multiple = true;
		} else {
			selectObj = eval( formName +"."+ name + i );
			if( selectObj ) {
				selectObj.disabled = multiple;
				multiple = ( multiple || ( selectObj.value == MULTIPLE_SELECT_VALUE ) );
			}

			selectObj = eval( formName +"._"+ name + i );
			if( selectObj ) {
				selectObj.disabled = multiple;
				multiple = ( multiple || ( selectObj.value == MULTIPLE_SELECT_VALUE ) );
			}
		}
	}
}

LevelField.extractLevel = function( selectObj ) {
	return ( selectObj.name.charAt( selectObj.name.length - 1 ) - '0' );
}

LevelField.extractName = function( selectObj ) {
	if( selectObj.name.charAt(0) == '_' )
		return selectObj.name.substring( 1, selectObj.name.length - 1 );
	else
		return selectObj.name.substring( 0, selectObj.name.length - 1 );
}

LevelField.focus = function( selectObj_s ) {
	var selectObj_m = eval( selectObj_s.form.name +"."+ selectObj_s.name.substring(1) );

	if( selectObj_m.options.length > 1 ) {
		selectObj_s.setAttribute( "tabIndex_org", selectObj_s.tabIndex );
		selectObj_s.tabIndex = -1;
		selectObj_m.style.display = "";
		selectObj_m.style.width = selectObj_s.offsetWidth;
		selectObj_m.style.zIndex = 2;
		selectObj_m.size = selectObj_m.options.length;
		if( selectObj_m.size > MULTIPLE_SELECT_MAXROWS )
			selectObj_m.size = MULTIPLE_SELECT_MAXROWS
		window.setTimeout( function() { selectObj_m.focus(); }, 0 );
	}
}

LevelField.getContent = function( nameOrSelectObj, level ) {
	if( typeof nameOrSelectObj == "object" )
		nameOrSelectObj = LevelField.extractName( nameOrSelectObj );

	return eval( "document.all.content_"+ nameOrSelectObj + level );
}

LevelField.getContents = function( nameOrSelectObj ) {
	if( typeof nameOrSelectObj == "object" )
		nameOrSelectObj = LevelField.extractName( nameOrSelectObj );

	var contentObjs = new Array();
	for( var level = 1; level <= 9; level++ )
		if( eval("document.all.content_"+ nameOrSelectObj + level) )
			contentObjs.push( eval("document.all.content_"+ nameOrSelectObj + level) );

	return contentObjs;
}

LevelField.getElementNames = function( name ) {
	var elementNames = new Array();

	for( var level = 1; level <= 9; level++ )
		elementNames.push( name + level );

	return elementNames;
}

LevelField.getElements = function( formName, name ) {
	var elementObjs = new Array();

	for( var level = 1; level <= 9; level++ )
		if( eval(formName +"."+ name + level) )
			elementObjs.push( eval(formName +"."+ name + level) );

	return elementObjs;
}

LevelField.getLastLevel = function( formName, name ) {
	var lastLevel = 0;

	for( var level = 1; level <= 9; level++ )
		if( eval(formName +"."+ name + level) )
			lastLevel = level;

	return lastLevel;
}

LevelField.getTempElementNames = function( name ) {
	var elementNames = new Array();

	for( var level = 1; level <= 9; level++ )
		elementNames.push( "_"+ name + level );

	return elementNames;
}

LevelField.getTempElements = function( formName, name ) {
	var elementObjs = new Array();

	for( var level = 1; level <= 9; level++ )
		if( eval(formName +"._"+ name + level) )
			elementObjs.push( eval(formName +"._"+ name + level) );

	return elementObjs;
}

LevelField.getValue = function( selectObj, multiple, isJQuery ) {
	var formName = selectObj.form.name;
	var name = LevelField.extractName( selectObj );

	var value = "";
	for( var level = 1; level <= 9; level++ ) {
		if( isJQuery ) {
			var values = $("#"+ name + level).multipleSelect( "getSelects" );
			if( values && values.length > 0 )
				value = values;
		} else {
			var selectObj = eval( formName +"._"+ name + level );
			if( !selectObj ) selectObj = eval( formName +"."+ name + level );
			if( selectObj && selectObj.value ) {
				if( selectObj.value == MULTIPLE_SELECT_VALUE ) {
					if( multiple ) {
						value = new Array();

						var selectObj = eval( formName +"."+ name + level );
						if( selectObj ) {
							for( var k = 0; k < selectObj.options.length; k++ )
								if( selectObj.options[k].selected )
									value.push( selectObj.options[k].value );
							}
					}
				} else
					value = selectObj.value;
			}
		}
	}

	return value;
}

LevelField.modified = function( selectObj, onchange ) {
	var formName = selectObj.form.name;
	var name = LevelField.extractName( selectObj );
	var changedLevel = LevelField.extractLevel( selectObj );

	if( onchange ) onchange( selectObj );

	var value = null;
	var multiple = ( selectObj.value == MULTIPLE_SELECT_VALUE );
	if( !multiple ) {
		value = selectObj.value;

		if( value ) {
			for( var level = changedLevel - 1; level >= 1; level-- ) {
				var selectObj_m = eval( formName +"."+ name + level );
				if( !selectObj_m ) break;

				var options = selectObj_m.options;
				for( var k = 0; k < options.length; k++ )
					options[k].selected = ( options[k].value && options[k].value == value.substring(0, options[k].value.length) );
				if( onchange ) onchange( selectObj_m );

				var selectObj_s = eval( formName +"._"+ name + level );
				if( selectObj_s ) {
					options = selectObj_s.options;

					for( var k = 0; k < options.length; k++ )
						if( options[k].value && options[k].value == value.substring(0, options[k].value.length) ) {
							selectObj_s.value = options[k].value;
							if( onchange ) onchange( selectObj_s );
							break;
						}
				}
			}
		} else {
			for( var level = changedLevel - 1; level >= 1; level-- ) {
				selectObj = eval( formName +"._"+ name + level );
				if( !selectObj ) selectObj = eval( formName +"."+ name + level );

				if( selectObj && selectObj.value ) {
					value = selectObj.value;
					break;
				}
			}
		}
	}

	var reloadLevel = 0;
	for( var level = changedLevel; selectObj; ) {
		var selectObj_s = eval( formName +"._"+ name + (++level) );
		if( selectObj_s ) {
			selectObj_s.value = "";
			selectObj_s.disabled = multiple;
			if( onchange ) onchange( selectObj_s );
		}

		var selectObj_m = selectObj = eval( formName +"."+ name + level );
		if( selectObj_m ) {
			for( var k = 0; k < selectObj_m.options.length; k++ )
				selectObj_m.options[k].selected = false;
			selectObj_m.disabled = multiple;
		}

		if( reloadLevel == 0 ) {
			var contentObj = eval( "document.all.content_"+ name + level );
			if( contentObj ) reloadLevel = level;
		}
	}

	return ( reloadLevel > 0 ? value : null );
}


/***********************************************************************************************************************
	Select
		Select.addOptions( selectObj, options, checkingValue );
		Select.move( selectObj_src, selectObj_det );
		Select.moveAll( selectObj_src, selectObj_det );
		Select.moveDown( selectObj );
		Select.moveToBottom( selectObj );
		Select.moveToTop( selectObj );
		Select.moveUp( selectObj );
		Select.removeOptions( selectObj, selected );
		Select.reverseOptions( selectObj );
		Select.selectAll( selectObj_m );
		Select.swap( selectObj, idx1, idx2 );
***********************************************************************************************************************/
function Select() {}

Select.addOptions = function( selectObj, options, checkingValue ) {
	var count = 0;

	for( var i = 0; i < options.length; i++ ) {
		var bool = true;

		if( checkingValue ) {
			for( var k = 0; k < selectObj.options.length; k++ )
				if( selectObj.options[k].value == options[i].value ) {
					bool = false;
					break;
				}
		}

		if( bool ) {
			count++;
			selectObj.options[selectObj.options.length] = options[i];
		}
	}

	return count;
}

Select.move = function( selectObj_src, selectObj_det ) {
	Select.addOptions( selectObj_det, Select.removeOptions(selectObj_src, true) );
}

Select.moveAll = function( selectObj_src, selectObj_det ) {
	Select.addOptions( selectObj_det, Select.removeOptions(selectObj_src) );
}

Select.moveDown = function( selectObj ) {
	for( var idx = selectObj.options.length - 2; idx >= 0; idx-- )
		if( selectObj.options[idx].selected && !selectObj.options[idx+1].selected )
			Select.swap( selectObj, idx, idx+1 );
}

Select.moveToBottom = function( selectObj ) {
	Select.addOptions( selectObj, Select.removeOptions(selectObj, true) );
}

Select.moveToTop = function( selectObj ) {
	Select.addOptions( selectObj, Select.removeOptions(selectObj, false) );
}

Select.moveUp = function( selectObj ) {
	for( var idx = 1; idx < selectObj.options.length; idx++ )
		if( selectObj.options[idx].selected && !selectObj.options[idx-1].selected )
			Select.swap( selectObj, idx, idx-1 );
}

Select.removeOptions = function( selectObj, selected ) {
	var options = new Array();

	for( var idx = 0; idx < selectObj.options.length; idx++ )
		if( typeof selected == "undefined" || selectObj.options[idx].selected == selected )
			options.push( selectObj.options[idx] );

	for( var idx = selectObj.options.length - 1; idx >= 0; idx-- )
		for( var i = 0; i < options.length; i++ )
			if( options[i] == selectObj.options[idx] ) {
				selectObj.options.remove( idx );
				break;
			}

	return options;
}

Select.reverseOptions = function( selectObj, selected ) {
	for( var i = selectObj.options.length - 1; i >= 0; i-- ) {
		var option = selectObj.options[i];
		selectObj.options.remove( i );
		selectObj.options[selectObj.options.length] = option;
	}
}

Select.selectAll = function( selectObj_m ) {
	for( var idx = 0; idx < selectObj_m.options.length; idx++ )
		selectObj_m.options[idx].selected = true;
}

Select.swap = function( selectObj, idx1, idx2 ) {
	var value = selectObj.options[idx1].value;
	var text = selectObj.options[idx1].text;
	var selected = selectObj.options[idx1].selected;
	var sortIndex = selectObj.options[idx1].sortIndex;

	selectObj.options[idx1].value = selectObj.options[idx2].value;
	selectObj.options[idx1].text = selectObj.options[idx2].text;
	selectObj.options[idx1].selected = selectObj.options[idx2].selected;
	selectObj.options[idx1].sortIndex = selectObj.options[idx2].sortIndex;

	selectObj.options[idx2].value = value;
	selectObj.options[idx2].text = text;
	selectObj.options[idx2].selected = selected;
	selectObj.options[idx2].sortIndex = sortIndex;
}


/***********************************************************************************************************************
	Styles
		Styles.appendClassName( obj, className )
		Styles.changeClassName( obj, oldClassName, newClassName )
		Styles.changeClassName2( obj, ... )
		Styles.changeDisplay( obj, display )
		Styles.containClassName( obj, className )
		Styles.getClassNames( className )
		Styles.removeClassName( obj, className )
***********************************************************************************************************************/
function Styles() {}

Styles.appendClassName = function( obj, className ) {
	if( obj.className ) {
		var classNames = Styles.getClassNames( className );
		for( var i = 0; i < classNames.length; i++ )
			if( !Styles.containClassName(obj, classNames[i]) )
				obj.className = obj.className + " " + classNames[i];
	} else
		obj.className = className;
}

Styles.changeClassName = function( obj, oldClassName, newClassName ) {
	var classNames0 = Styles.getClassNames( obj.className );

	if( classNames0 ) {
		var classNames1 = Styles.getClassNames( oldClassName );
		var classNames2 = Styles.getClassNames( newClassName );

		var removed = false;
		for( var i = 0; i < classNames0.length; i++ ) {
			for( var j = 0; classNames1 && j < classNames1.length; j++ )
				if( classNames0[i] == classNames1[j] ) {
					classNames0[i] = null;
					removed = true;
					break;
				}
			for( var j = 0; classNames2 && j < classNames2.length; j++ )
				if( classNames0[i] == classNames2[j] )
					classNames2[j] = null;
		}
		if( !removed ) return false;

		var newClassName = "";
		for( var i = 0; i < classNames0.length; i++ )
			if( classNames0[i] )
				newClassName += " " + classNames0[i];
		for( var i = 0; classNames2 && i < classNames2.length; i++ )
			if( classNames2[i] )
				newClassName += " " + classNames2[i];
		obj.className = ( newClassName ? newClassName.substring(1) : "" );
		return true;
	} else
		return false;
}

Styles.changeClassName2 = function( obj ) {
	var args = Styles.changeClassName2.arguments;

	for( var a = 1; a < args.length; a += 2 )
		if( Styles.changeClassName(obj, args[a], args[a+1]) )
			return true;

	return false;
}

Styles.changeDisplay = function( obj, display ) {
	if( obj[0] && obj.type != "select-one" && obj.type != "select-multiple" ) {
		for( var i = 0; i < obj.length; i++ )
			obj[i].style.display = ( display ? "" : "none" );
	} else
		obj.style.display = ( display ? "" : "none" );
}

Styles.containClassName = function( obj, className ) {
	if( obj.className && className ) {
		var idx1 = obj.className.indexOf( className );
		while( idx1 >= 0 ) {
			if( idx1 < 0 ) return false;
			if( idx1 == 0 || " \t".indexOf(obj.className.charAt(idx1-1)) >= 0 ) {
				var idx2 = idx1 + className.length;
				if( obj.className.length == idx2 || " \t".indexOf(obj.className.charAt(idx2)) >= 0 )
					return true;
			}
			idx1 = obj.className.indexOf( className, idx1+1 );
		}
	}

	return false;
}

Styles.getClassNames = function( className ) {
	if( className ) {
		var names0 = className.split( "\t" );
		if( names0.length == 1 )
			return names0[0].split( " " );
		else {
			var classNames = new Array();
			for( var i = 0, n = 0; i < names0.length; i++ ) {
				var names1 = names0[i].split( " " );
				for( var j = 0; j < names1.length; j++ )
					classNames[n++] = names1[j];
			}

			return classNames;
		}
	}
	return null;
}

Styles.removeClassName = function( obj, className ) {
	var classNames0 = Styles.getClassNames( obj.className );

	if( classNames0 ) {
		var classNames1 = Styles.getClassNames( className );
		for( var i = 0; i < classNames0.length; i++ )
			for( var j = 0; j < classNames1.length; j++ )
				if( classNames0[i] == classNames1[j] ) {
					classNames0[i] = null;
					break;
				}

		var newClassName = "";
		for( var i = 0; i < classNames0.length; i++ )
			if( classNames0[i] )
				newClassName += " " + classNames0[i];
		obj.className = ( newClassName ? newClassName.substring(1) : "" );
	}
}




/***********************************************************************************************************************
FlexListWriter
***********************************************************************************************************************/
function FlexListWriter() {}

FlexListWriter.adjustColumnWidth = function( colIndex, colWidth ) {
	var isDesc = $(".list_content_data colgroup").find("col:nth-child("+ (colIndex+1) +")" ).hasClass("description");
	if( !isDesc && colWidth > 0 ) {
//		var columnsWidth = FlexListWriter.getWidthForColumns();
		var fullWidth = FlexListWriter.getTableWidth();
		var colPer = colWidth/fullWidth*100+"%";
		$(".list_content_data colgroup").find("col:nth-child("+ (colIndex+1) +")" ).width(colPer);
		$(".list_content_header colgroup").find("col:nth-child("+ (colIndex+1) +")" ).width(colPer);
		$(".list_content_header tbody tr").find("th:nth-child("+ (colIndex+1) +")" ).css("word-break","break-word");
	}
}

FlexListWriter.getColumnWidthRate = function() {
	var tArray = new Array();
	var dArray = new Array();

	var titleLineWidth = Number($(".list_flex_data_all").attr("data-column-title-line-width"));
	var dataLineWidth = Number($(".list_flex_data_all").attr("data-column-data-line-max-width"));

	$(".list_flex_data span").each(function(idx,obj) {
		if( !($(obj).hasClass("numbering") || $(obj).hasClass("checkboxing")) ) {
			var colIndex = idx+1;

			var titleColWidth = Number($(obj).attr("data-column-title-width"));
			var dataColWidth = Number($(obj).attr("data-column-data-max-width"));

			tArray.push(titleColWidth/titleLineWidth);
			dArray.push(dataColWidth/dataLineWidth);
		}
	});

	return { dataColumnWidthRate: dArray, titleColumnWidthRate: tArray };
}

FlexListWriter.calcColumnWidth = function( tableWidth, rate ) {

}

FlexListWriter.getColumnWidthArray = function( tableWidth ) {
	if( !tableWidth ) {
		tableWidth = Number($("div.list_content").width());
	}
	if( tableWidth < 760 ) { tableWidth = 760; }// minimum width(with hidden area)

	var columnWidthRateArr = FlexListWriter.getColumnWidthRate().dataColumnWidthRate;
	var arr = new Array();
	var titleAdjustArray = FlexListWriter.getAdjustTitleArray();

	var dataLineWidth = Number($(".list_flex_data_all").attr("data-column-data-line-max-width"));

	$.each(columnWidthRateArr, function(idx, obj) {
		var isDesc = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")" ).hasClass("description");
		var width = 100;
		if( idx == 0 ) {
			width = FlexListWriter.getNumOrCheckboxWidth();
		} else if(idx > 0) {
			if( isDesc ) {
				width = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")").width();
			} else {
				if( titleAdjustArray[idx] > 0 ) {
					width = titleAdjustArray[idx] / dataLineWidth * tableWidth;
				} else {
					width = Number(obj) * tableWidth;
				}
			}
		}
		arr.push(width);
	});
	return arr;
}

FlexListWriter.getNumOrCheckboxWidth = function() {
	var chkboxWidth = $(".list_content_data tr:first td:first").find("[type=checkbox]").width();
	var toolbarWidth = $(".list_content_data tr:first td:first").find(".tabledit-toolbar button").width();

	if( !chkboxWidth )
		chkboxWidth = $(".list_content_data colgroup").find("col:nth-child(1)").width();

	return (chkboxWidth + toolbarWidth) * 3;
}

FlexListWriter.getTitleWidths = function() {
	var tArray = new Array();
	$(".list_flex_data span").each(function(idx,obj) {
		if( !($(obj).hasClass("numbering") || $(obj).hasClass("checkboxing")) ) {
			var colIndex = idx+1;

			var titleColWidth = Number($(obj).attr("data-column-title-width"));

			tArray.push(titleColWidth);
		}
	});
	return tArray;
}
FlexListWriter.getDataWidths = function() {
	var dArray = new Array();
	$(".list_flex_data span").each(function(idx,obj) {
		if( !($(obj).hasClass("numbering") || $(obj).hasClass("checkboxing")) ) {
			var colIndex = idx+1;

			var dataColWidth = Number($(obj).attr("data-column-data-max-width"));

			dArray.push(dataColWidth);
		}
	});
	return dArray;
}

FlexListWriter.getAdjustTitleArray = function() {
	var dArray = FlexListWriter.getDataWidths();
	var tArray = FlexListWriter.getTitleWidths();
	var needAdjustArray = new Array();
	$.each(dArray, function(idx,obj){
		var resizeWidth = -1;
		var dw = Number(obj);
		var tw = Number(tArray[idx]);
		if( idx > 0 ) {
			if( dw <= 3 && tw <= 5 ) {
				resizeWidth = tw+1;
			} else if( dw < 5 && (dw*3) <= tw ) {
				resizeWidth = tw/2;
			}
		}
		needAdjustArray.push(resizeWidth);
	});
	return needAdjustArray;
}


FlexListWriter.getTitleWidthArray = function( tableWidth ) {
	var columnWidthRateArr = FlexListWriter.getColumnWidthRate().titleColumnWidthRate;
	var arr = new Array();
	$.each(columnWidthRateArr, function(idx, obj) {
		var isDesc = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")" ).hasClass("description");
		if(idx > 0) {
			var width = 100;
			if( isDesc ) {
				width = $(".list_content_data colgroup").find("col:nth-child("+ (idx+1) +")").width();
			} else {
				var num = Number(obj);
				width = num * tableWidth;
			}

			arr.push(width);
		}
	});
	return arr;
}

FlexListWriter.getColumnWidthSum = function( tableWidth ) {
	var sum = 0;
	var cw = FlexListWriter.getColumnWidthArray(tableWidth);
	if( cw ) {
		for( var i = 0; i < cw.length; i++ ){
			sum += cw[i];
		}
	}
	return sum;
}

FlexListWriter.getTableWidth = function() {
	return Number($(".list_content_data").width());
}

FlexListWriter.getWidthForColumns = function() {
	var columnsWidth = FlexListWriter.getTableWidth() - FlexListWriter.getNumOrCheckboxWidth();
	return columnsWidth;
}

FlexListWriter.adjustColumnWidthAll = function( columnWidthRateArr ) {
$.each(FlexListWriter.getColumnWidthArray(), function(idx,obj){
	FlexListWriter.adjustColumnWidth(idx, Number(obj));
});
}



