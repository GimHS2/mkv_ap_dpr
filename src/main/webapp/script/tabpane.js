/*
	File Name:	tabpane.js
	Version:	2.2.0

	Description:
		style
			.dynamic-tab-pane-control
			.hover
			.selected
			.*_tabrow
			.tab-page
			.tab-row
	Note:
		TabPane( element, useCookie );
			TabPane.regularExpression
			TabPane.tabPanes
			TabPane.getCookie( name );
			TabPane.setCookie( name, value, days );
			TabPane.prototype.addTabPage( element );
			TabPane.prototype.getSelectedIndex();
			TabPane.prototype.setSelectedIndex( index );
		TabPage( element, tabPane, nIndex );
			TabPage.regularExpression
			TabPage.prototype.tabOver( tabpage );
			TabPage.prototype.tabOut( tabpage );
			TabPage.prototype.dispose();
			TabPage.prototype.hide();
			TabPage.prototype.hideTab();
			TabPage.prototype.select();
			TabPage.prototype.show();
			TabPage.prototype.showTab();
		setupAllTabs();
		disposeAllTabs();
		hasSupport();

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	TabPane에 onclicktab 변수 추가. setSelectedIndex에서 활용
										TabPage.prototype.hideTab(), TabPage.prototype.showTab() 추가
	stghr12		2007/04/30		2.1.0	version 관리
*/

/***********************************************************************************************************************
	TabPane
***********************************************************************************************************************/
function TabPane( element, useCookie ) {
	if ( !hasSupport() || element == null ) return;

	this.element = element;
	this.element.tabPane = this;
	this.pages = [];
	this.selectedIndex = null;
	this.useCookie = ( useCookie != null ? useCookie : false );
	this.onclicktab = null;

	// add tab row
	this.tabRow = document.createElement( "div" );
	if( this.element.className )
		this.tabRow.className = this.element.className +"_tabrow";
	else
		this.tabRow.className = this.element.className +"tab-row";
	element.insertBefore( this.tabRow, this.element.firstChild );

	// add class name tag to class name
	this.element.className = "dynamic-tab-pane-control " + this.element.className;

	var tabIndex = 0;
	if ( this.useCookie ) {
		tabIndex = Number( TabPane.getCookie( "webfxtab_" + this.element.id ) );
		if( isNaN(tabIndex) )
			tabIndex = 0;
	}
	this.selectedIndex = tabIndex;

	// loop through child nodes and add them
	var nodes = element.childNodes;
	for( var i = 0; i < nodes.length; i++ )
		if( nodes[i].nodeType == 1 && TabPage.regularExpression.test(nodes[i].id) )
			this.addTabPage( nodes[i] );

	TabPane.tabPanes[TabPane.tabPanes.length] = this;
}
TabPane.regularExpression = /tabpane_/;
TabPane.tabPanes = new Array;

TabPane.getCookie = function( name ) {
	var regular = new RegExp( "(\;|^)[^;]*(" + name + ")\=([^;]*)(;|$)" );
	var values = regular.exec( document.cookie );

	return( values != null ? values[3] : null );
};

TabPane.setCookie = function( name, value, days ) {
	var expires = "";

	if( days ) {
		var d = new Date();
		d.setTime( d.getTime() + days * 24 * 60 * 60 * 1000 );
		expires = "; expires=" + d.toGMTString();
	}
	document.cookie = name + "=" + value + expires + "; path=/";
};

TabPane.prototype.addTabPage = function( element ) {
	if( !hasSupport() ) return;

	if( element.tabPage == this ) return element.tabPage;

	var n = this.pages.length;
	var tabpage = this.pages[n] = new TabPage( element, this, n );

	tabpage.tabPane = this;

	// move the tab out of the box
	this.tabRow.appendChild( tabpage.tab );
	if( n == this.selectedIndex )
		tabpage.show();
	else
		tabpage.hide();

	return tabpage;
};

TabPane.prototype.dispose = function() {
	this.element.tabPane = null;
	this.element = null;
	this.tabRow = null;

	for (var i = 0; i < this.pages.length; i++) {
		this.pages[i].dispose();
		this.pages[i] = null;
	}
	this.pages = null;
};

TabPane.prototype.getSelectedIndex = function() {
	return this.selectedIndex;
};

TabPane.prototype.setSelectedIndex = function( index ) {
	if( this.selectedIndex != index ) {
		if( !this.pages[index] ) return;
		if( this.selectedIndex != null && this.pages[ this.selectedIndex ] != null )
			this.pages[ this.selectedIndex ].hide();

		this.pages[ this.selectedIndex = index ].show();

		if( this.useCookie )
			TabPane.setCookie( "webfxtab_" + this.element.id, index );
		if( tabPane.onclicktab ) tabPane.onclicktab();
	}
};


/***********************************************************************************************************************
	TabPage
***********************************************************************************************************************/
function TabPage( element, tabPane, nIndex ) {
	if ( !hasSupport() || element == null ) return;

	this.element = element;
	this.element.tabPage = this;
	this.index = nIndex;

	if( !this.element.className )
		this.element.className = "tab-page";

	var nodes = element.childNodes;
	for( var i = 0; i < nodes.length; i++ ) {
		if( nodes[i].nodeType == 1 ) {
			this.tab = nodes[i];
			break;
		}
	}

	var anchor = document.createElement( "A" );
	this.anchor = anchor;
	anchor.href = "#";
	anchor.onclick = function() { return false; };

	while( this.tab.hasChildNodes() )
		anchor.appendChild( this.tab.firstChild );
	this.tab.appendChild( anchor );

	// hook up events, using DOM0
	var oThis = this;
	this.tab.onclick = function() { oThis.select(); };
	this.tab.onmouseover = function() { oThis.tabOver(); };
	this.tab.onmouseout = function() { oThis.tabOut(); };
	this.tab.onkeyup = function() { oThis.keyUp(); };
}
TabPage.regularExpression = /tabpage_/;

TabPage.prototype.keyUp = function() {
	if( event.keyCode == 33 || event.keyCode == 37 ) {
		this.tabPane.setSelectedIndex( this.index - 1 );
		this.tabPane.pages[this.tabPane.getSelectedIndex()].anchor.focus();
	} else if( event.keyCode == 34 || event.keyCode == 39 ) {
		this.tabPane.setSelectedIndex( this.index + 1 );
		this.tabPane.pages[this.tabPane.getSelectedIndex()].anchor.focus();
	}
};

TabPage.prototype.tabOver = function() {
	if( this.index != this.tabPane.getSelectedIndex() )
		this.tab.className = this.tab.className + " hover";
};

TabPage.prototype.tabOut = function() {
	this.tab.className = this.tab.className.replace( / hover/g, "");
};

TabPage.prototype.dispose = function() {
	this.anchor.onclick = null;
	this.anchor = null;
	this.element.tabPage = null;
	this.tab.onclick = null;
	this.tab.onmouseover = null;
	this.tab.onmouseout = null;
	this.tab = null;
	this.tabPane = null;
	this.element = null;
};

TabPage.prototype.hide = function() {
	this.tab.className = this.tab.className.replace( / selected/g, "");
	this.element.style.display = "none";
};

TabPage.prototype.hideTab = function() {
	this.tab.style.display = "none";
}

TabPage.prototype.select = function() {
	this.tabPane.setSelectedIndex( this.index );
};

TabPage.prototype.show = function() {
	this.tab.className = this.tab.className.replace( / hover/g, "");
	this.tab.className = this.tab.className + " selected";
	this.element.style.display = "";
};

TabPage.prototype.showTab = function() {
	this.tab.style.display = "";
}


/***********************************************************************************************************************
	setupAllTabs();
	disposeAllTabs();
	hasSupport();
***********************************************************************************************************************/
function disposeAllTabs() {
	var tabPanes = TabPane.tabPanes;
	for( var i = tabPanes.length - 1; i >= 0; i-- ) {
		tabPanes[i].dispose();
		tabPanes[i] = null;
	}
}

function hasSupport() {
	if (typeof hasSupport.support != "undefined")
		return hasSupport.support;

	var ie55 = /msie 5\.[56789]/i.test( navigator.userAgent );
	hasSupport.support = ( typeof document.implementation != "undefined" && document.implementation.hasFeature( "html", "1.0" ) || ie55 )

	// IE55 has a serious DOM1 bug... Patch it!
	if ( ie55 ) {
		document._getElementsByTagName = document.getElementsByTagName;
		document.getElementsByTagName = function( sTagName ) {
			if( sTagName == "*" )
				return document.all;
			else
				return document._getElementsByTagName( sTagName );
		};
	}

	return hasSupport.support;
}

function setupAllTabs() {
	if( !hasSupport() ) return;

	var elements = document.getElementsByTagName( "*" );
	for ( var i = 0; i < elements.length; i++ ) {
		var element = elements[i]
		var elementId = element.id;

		if ( elementId == "" ) continue;
		if( TabPane.regularExpression.test(elementId) ) {
			if( !element.tabPane ) new TabPane( element, true );
		} else if( TabPage.regularExpression.test(elementId) ) {
			if( !element.tabPage && tabPaneRe.test(element.parentNode.id) )
				element.parentNode.tabPane.addTabPage( element );
		}
	}
}


/***********************************************************************************************************************
	attachEvent
************************************************************************************************************************
if ( typeof window.addEventListener != "undefined" )
	window.addEventListener( "load", setupAllTabs, false );
else if ( typeof window.attachEvent != "undefined" ) {
	window.attachEvent( "onload", setupAllTabs );
	window.attachEvent( "onunload", disposeAllTabs );
} else {
	if ( window.onload != null ) {
		var oldOnload = window.onload;
		window.onload = function( e ) {
			oldOnload( e );
			setupAllTabs();
		};
	} else
		window.onload = setupAllTabs;
}
***********************************************************************************************************************/
