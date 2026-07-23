<%--
	File Name:	dpr_util_tree.jsp
	Version:	2.0.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	guksm		2008/09/26		2.0.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*,java.util.List,java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<head>
	<style>
		ul.tree_content, ul.tree_content ul {
			list-style-type: none;
			padding: 0;
			margin: 0;
			font: 10px tahoma;
		}

		ul.tree_content li {
			background: no-repeat;
			margin: 0;
			padding: 0 0 0 16px;
			cursor: default;
		}

		ul.tree_content li.plus {
			background-image: url(images/ico_plus.gif);
			vertical-align: top;
		}

		ul.tree_content li.minus {
			background-image: url(images/ico_minus.gif);
			vertical-align: top;
		}

		ul.tree_content span.count {
			text-indent: 5pt;
			color: #999;
			font-style: italic;
		}
	</style>
	<script type='text/javascript'>
		function CheckTree(myName) {
			this.myName = myName;
			this.root = null;
			this.countAllLevels = false;
			this.checkFormat = '(%n% checked)';
			this.evtProcessed = navigator.userAgent.indexOf('Safari') > -1 ? 'safRtnVal' : 'returnValue';
			CheckTree.list[myName] = this;
		};
		CheckTree.list = [];


// Called onload, this sets up a reference to the 'root' node and hides sublevels.
		CheckTree.prototype.init = function() { with( this ) {
			if( !document.getElementById ) return;
			root = document.getElementById( 'tree-' + myName );
			if( root ) {
				var lists = root.getElementsByTagName( 'ul' );
				for( var ul = 0; ul < lists.length; ul++ ) {
// Hide all UL sublevels under the root node, and assign them a toggle/click methods.
					lists[ul].style.display = 'none';
					lists[ul].treeObj = this;
					lists[ul].setBoxStates = setBoxStates;

					var fn = new Function('e', 'this.setBoxStates(e)');
// Grr, workaronud another Safari bug.
					if( lists[ul].addEventListener && navigator.vendor != 'Apple Computer, Inc.' ) {
						lists[ul].addEventListener( 'click', fn, false );
					} else lists[ul].onclick = fn;
				}

// Now do a similar event capture setup for the 'root' node.
				root.treeObj = this;
				root.setBoxStates = setBoxStates;
				if( root.addEventListener && navigator.vendor != 'Apple Computer, Inc.' ) {
					root.addEventListener( 'click', new Function('e', myName + '.click(e)'), false );
				} else root.onclick = new Function( 'e', myName + '.click(e)' );
// Trigger a quick state update, to set the counters for each level.
				root.setBoxStates( {}, true, true );

// Now go through and assign plus/plus-last classes to the appropriate <LI>s.
				var nodes = root.getElementsByTagName( 'li' );
				for( var li = 0; li < nodes.length; li++ ) {
					if( nodes[li].id.match(/^show-/) ) {
						nodes[li].className = (nodes[li].className=='last' ? 'plus-last' : 'plus');
					}
				}
			}
		}};


// Called on click of the entire tree, this manages visibility of sublevels.
		CheckTree.prototype.click = function( e ) { with( this ) {
			e = e || window.event;
			var elm = e.srcElement || e.target;

// Has a checkbox been clicked, but not processed by a lower level onclick event?
// If so, one of the 'root' checkboxes must have been clicked.
// We must therefore trigger a manual 'downwards route' for that tree to update it.
			if( !e[evtProcessed] && elm.id && elm.id.match(/^check-(.*)/) ) {
				var tree = document.getElementById( 'tree-' + RegExp.$1 );
				if( tree ) tree.setBoxStates( e, true, false );
			}

			while( elm ) {
// Dont' do expand/collapses for clicks on checkboxes, or nested within menus.
				if( elm.tagName.match(/^(input|ul)/i) ) break;
// Show/hide the menu element that matches the source id="show-xxx" tag and quit.
				if( elm.id && elm.id.match(/^show-(.*)/) ) {
					var targ = document.getElementById('tree-' + RegExp.$1);
					if( targ.style ) {
						var col = (targ.style.display == 'none');
						targ.style.display = col ? 'block' : 'none';
// Swap the class of the <span> tag inside, maintaining "-last" state if applied.
						elm.className = elm.className.replace( col?'plus':'minus', col?'minus':'plus' );
					}
					break;
				}
// Otherwise, continue looping up the DOM tree, looking for a match.
				elm = elm.parentNode;
			}
		}};

	function setBoxStates( e, routingDown, countOnly ) { with ( this ) {
// Opera <7 fix... don't perform any actions in those browsers.
		if( !this.childNodes ) return;

		e = e || window.event;
		var elm = e.srcElement || e.target;

// Initial check: if the parent checkbox for a tree level has been clicked, trigger a
// pre-emptive downwards route within that tree, and set returnValue to true so that we
// don't repeat it or mess with any of the original checkbox's siblings.
		if( elm && elm.id && elm.id.match(/^check-(.*)/) && !routingDown && !e[treeObj.evtProcessed] ) {
			var refTree = document.getElementById( 'tree-' + RegExp.$1 );
			if( refTree ) {
				refTree.setBoxStates( e, true, countOnly );
				e[treeObj.evtProcessed] = true;
			}
		}

// Some counter and reference variables.
		var allChecked = true, boxCount = 0, subBoxes = null;
// Get the name of this branch and see if the source element has id="check-xxxx".
		var thisLevel = this.id.match( /^tree-(.*)/ )[1];
		var parBox = document.getElementById( 'check-' + thisLevel );

// Loop through all children of all list elements inside this UL tag.
		for( var li = 0; li < childNodes.length; li++ ) {
			for( var tag = 0; tag < childNodes[li].childNodes.length; tag++ ) {
				var child = childNodes[li].childNodes[tag];
				if( !child ) continue;
				if( child.tagName && child.type && child.tagName.match(/^input/i) && child.type.match(/^checkbox/i) ) {
// Set this box's state depending on its parent state, if we're routing downwards.
					if( routingDown && parBox && elm && elm.id && elm.id.match(/^check-/) && !countOnly )
						child.checked = parBox.checked;
    // Count the checked boxes directly under this level.
					allChecked &= child.checked;
					if( child.checked ) boxCount++;
				}
   // And route this event to sublevels, to update their nodes, during a downwards route.
				if( child.tagName && child.tagName.match(/^ul/i) && (!e[treeObj.evtProcessed] || routingDown) )
					child.setBoxStates( e, true, countOnly );
			}
		}

// Once we've routed the event to all sublevels, set the 'returnValue' to true, so that
// upper levels don't re-trigger a downwards route. This is a bit of a hack, admittedly :).
		if( !routingDown ) e[treeObj.evtProcessed] = true;

// Next, set the parent parBox state depending if all checkboxes in this menu are checked.
// Of course, we don't set its state if it's the source of the event!
		if( parBox && parBox != elm && !countOnly ) parBox.checked = allChecked;

// If "countAllLevels" is set, overwrite the previous one-level-only count.
		if( treeObj.countAllLevels ) {
			boxCount = 0;
			var subBoxes = this.getElementsByTagName( 'input' );
			for( var i = 0; i < subBoxes.length; i++ ) if ( subBoxes[i].checked ) boxCount++;
		}

// Either way, assign the counted value to the id="count-xxx" page element.
		var countElm = document.getElementById('count-' + thisLevel);
		if( countElm ) {
			while( countElm.firstChild ) countElm.removeChild( countElm.firstChild );
			if( boxCount ) countElm.appendChild( document.createTextNode(treeObj.checkFormat.replace('%n%', boxCount)) );
		}
	}};


// Calls the init() function of any active trees on page load, and backup previous onloads.
	var chtOldOL = window.onload;
	window.onload = function() {
		if( chtOldOL ) chtOldOL();
		for( var i in CheckTree.list ) CheckTree.list[i].init();
	};
</script>

<%@ include file="include_rbm_header.inc" %>
<script type="text/javascript">
	var checkmenu = new CheckTree('checkmenu');

	function selectItem() {
		var selectedValues = CheckBox.getValues( frmMain.checkItem );
		if( selectedValues == null ) {
alert( "notChooseItem" );

			return;
		}

		if( parent && parent.window.main ) {
			var url = parent.window.main.getLocationURL();
			if( url ) {
				if( selectedValues.length == 1 )
					url = replaceQueryValue( url, "itemCode", encodeURIComponent(selectedValues) );
				else {
alert( "execute something..." );
return;
				}

alert( url );
				parent.window.main.windowSelfOpen( url );
			} else {
alert( "isNotLocationURL" );
				return;
			}
		} else {
alert( "babo" );
				return;
		}
	}
</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<a href='JavaScript:selectItem();'><img src='images/btn_save.gif'></a>
	<hr>
	<%
		List<Map> recordList = (List)pageContext.findAttribute( "records" );
		String cateCode = null;

		out.println( "<ul id='tree-checkmenu' class='tree_content'>" );
		for( Map recordMap : recordList ) {
			if( cateCode == null || !cateCode.equals(recordMap.get("cateCode")) ) {
				if( cateCode != null )
					out.println( "</ul></li>" );

				cateCode = (String)recordMap.get( "cateCode" );
				out.println( "<li id='show-"+ cateCode +"'>" );
				out.println( "<input type='checkbox' id='check-"+ cateCode +"'>" );
				out.println( recordMap.get("cateName") );
				out.println( "<span id='count-"+ cateCode +"' class='count'></span>" );
				out.println( "<ul id='tree-"+ cateCode +"'>" );
			}

			out.println( "<li><input type='checkbox' name='checkItem' value='"+ recordMap.get("itemCode") +"'/>"+ recordMap.get("itemName") +"</li>" );
		}
		out.println( "</ul></li>" );
		out.println( "</ul>" );
	%>
	</mtl:form>
</body>
</mtl:html>
