/*
	File Name:	linkmenu.js
	Version:	2.2.0

	Description:
		menuList�� ��������
			menuList = new Array;
			menuList[i] = new Array( "����" );
			menuList[i] = new Array( "����", "LINK����", "JavaScript: SCRIPT" );
			menuList[i] = new Array( "����", "LINK����", URL" );
			menuList[i] = new Array( "����", "LINK����", URL", "TARGET" );
			menuList[i] = new Array( "����", "LINK����", sub_menuList );
			menuList[i] = new Array( LinkMenu.MENU_SPACER );
			// LINK����: 'self'; �ڱ��ڽ��� URL����, 'new'; ��â ����, 'sub'; ����޴�

		script
			LinkMenu( menuList )
				LinkMenu.MENU_SPACER
				LinkMenu.activeMenu
				LinkMenu.timeOutId
				LinkMenu.timeOutMS
				LinkMenu.active(menuIdx, idx, relativeY);
				LinkMenu.click(menuIdx, idx);
				LinkMenu.deactive(menuIdx, idx);
				LinkMenu.hide();
				LinkMenu.prototype.active(menuIdx, idx, relativeY);
				LinkMenu.prototype.click(menuIdx, idx);
				LinkMenu.prototype.deactive(menuIdx, idx);
				LinkMenu.prototype.hide(menuIdx);
				LinkMenu.prototype.print(menuIdx);
				LinkMenu.prototype.show();
				LinkMenu.prototype.showSub(menuIdx, menuList, relativeY);
		script(using)
			DialogBox
		style
			span.linkmenu
			td.linkmenu_class
			td.linkmenu_title
			td.linkmenu_arrow

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/04/30		2.2.0	version ����
*/

/***********************************************************************************************************************
	LinkMenu
***********************************************************************************************************************/
function LinkMenu( menuList ) {
	this.image_arrow = new Image;
	this.image_arrowhl = new Image;
	this.image_spacer = new Image;
	this.image_spacerb = new Image;
	this.image_new = new Image;

	this.color_bg = "#FFFFFF";
	this.color_bg_hl = "#47598F";
	this.color_fg = "black";
	this.color_fg_hl = "white";

	this.width = 150;
	this.height_one = 19;
	this.height_head = 2;
	this.height_tail = 2;
	this.menuStyle = "style='border-width: 1px; border-style: solid; border-color: #FFF #808080 #808080 #FFF;'";

	this.menuListArray = new Array;
	this.menuListArray[0] = menuList;
	this.dialogBoxArray = new Array;
	this.onLeft = false;
}

LinkMenu.MENU_SPACER = "#SPACER#";
LinkMenu.activeMenu = null;
LinkMenu.timeOutId = null;
LinkMenu.timeOutMS = 3000;

LinkMenu.active = function( menuIdx, idx, relativeY ) {
	if( LinkMenu.activeMenu ) LinkMenu.activeMenu.active( menuIdx, idx, relativeY );
}

LinkMenu.click = function( menuIdx, idx ) {
	if( LinkMenu.activeMenu ) LinkMenu.activeMenu.click( menuIdx, idx );
}

LinkMenu.deactive = function( menuIdx, idx ) {
	if( LinkMenu.activeMenu ) LinkMenu.activeMenu.deactive( menuIdx, idx );
}

LinkMenu.hide = function() {
	if( LinkMenu.activeMenu ) LinkMenu.activeMenu.hide(0);
}

LinkMenu.prototype.active = function( menuIdx, idx, relativeY ) {
	this.hide( menuIdx+1 );
	var menu = this.menuListArray[menuIdx][idx];
	if( menu.length > 1 ) {
		var obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx );
		obj.style.backgroundColor = this.color_bg_hl;
		obj.style.color = this.color_fg_hl;

		obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx +"_class" );
		if( obj ) {
			obj.style.backgroundColor = this.color_bg_hl;
			obj.style.color = this.color_fg_hl;
		}

		if( menu[1] == "sub" ) {
			obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx +"_arr" );
			obj.style.backgroundColor = this.color_bg_hl;
			obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx +"_arrimg" );
			obj.src = this.image_arrowhl.src;
			this.showSub( menuIdx+1, menu[2], relativeY );
		}
	}

	if( LinkMenu.timeOutId ) {
		window.clearTimeout( LinkMenu.timeOutId );
		document.onmousedown = null;
		document.onmousewheel = null;
		LinkMenu.timeOutId = null;
	}
}

LinkMenu.prototype.click = function( menuIdx, idx ) {
	this.deactive( menuIdx, idx );
	var menu = this.menuListArray[menuIdx][idx];
	if( menu.length > 3 )
		window.open( menu[2], menu[3] );
	else if( menu.length > 2 )
		window.open( menu[2], "_self" );

	this.hide(0);
}

LinkMenu.prototype.deactive = function( menuIdx, idx ) {
	var menu = this.menuListArray[menuIdx][idx];
	if( menu.length > 1 ) {
		var obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx );
		obj.style.backgroundColor = this.color_bg;
		obj.style.color = this.color_fg;

		obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx +"_class" );
		if( obj ) {
			obj.style.backgroundColor = this.color_bg;
			obj.style.color = this.color_fg;
		}

		if( menu[1] == "sub" ) {
			obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx +"_arr" );
			obj.style.backgroundColor = this.color_bg;
			obj = document.getElementById( "_linkmenu_"+ menuIdx +"_"+ idx +"_arrimg" );
			obj.src = this.image_arrow.src;
		}
	}

	if( !LinkMenu.timeOutId ) {
		LinkMenu.timeOutId = window.setTimeout( LinkMenu.hide, LinkMenu.timeOutMS );
		document.onmousedown = LinkMenu.hide;
		document.onmousewheel = LinkMenu.hide;
	}
}

LinkMenu.prototype.hide = function( menuIdx ) {
	if( menuIdx == 0 ) {
		this.dialogBoxArray[0].hide(true);

		if( LinkMenu.timeOutId ) {
			window.clearTimeout( LinkMenu.timeOutId );
			document.onmousedown = null;
			document.onmousewheel = null;
			LinkMenu.timeOutId = null;
		}
		if( this == LinkMenu.activeMenu ) LinkMenu.activeMenu = null;
		menuIdx = 1;
	}
	for( var m = this.dialogBoxArray.length - 1; m >= menuIdx; m-- ) {
		if( this.dialogBoxArray[m] ) this.dialogBoxArray[m].hide(true);
		if( this.menuListArray[m] ) this.menuListArray[m] = null;
	}
}

LinkMenu.prototype.print = function( menuIdx ) {
	var menuList = this.menuListArray[menuIdx];

	var spacers = 0;
	var html = "<table width='100%' border='0' cellspacing='0' cellpadding='0' "+ this.menuStyle +">";
	for( var idx = 0; idx < menuList.length; idx++ ) {
		var menu = menuList[idx];
		var title = menu[0];

		html += "<tr>";
		if( title == LinkMenu.MENU_SPACER ) {
			spacers++;
			html += "<td colspan='4' style='height: 8px;'>"
				+ "<img src='"+ this.image_spacerb.src +"' style='width: 3px; height: 2px;'>"
				+ "<img src='"+ this.image_spacer.src +"' style='width: 95%; height: 2px;'></td>";
		} else {
			var relativeY = this.height_one * (idx - spacers) + 8 * spacers + this.height_head;
			var link = "onMouseOver='JavaScript:LinkMenu.active("+ menuIdx +","+ idx +","+ relativeY +");'";
			link += " onMouseOut='JavaScript:LinkMenu.deactive("+ menuIdx +","+ idx +");'";

			var hassub = false;
			html += "<td id='_linkmenu_"+ menuIdx +"_"+ idx +"_class' class='linkmenu_class'>";
			if( menu.length > 1 ) {
				link += " style='cursor: hand;'";
				if( menu[1] == "sub" )
					hassub = true;
				else {
					if( menu[1] == "new" ) {
						if( this.image_new && this.image_new.src )
							html += "<img src='"+ this.image_new.src +"'>";
					}
					if( menu.length > 2 ) {
						if( menu[2].match("JavaScript") )
							link += " onClick='"+ menu[2] +"; LinkMenu.deactive("+ menuIdx +","+ idx +"); LinkMenu.hide();'";
						else
							link += " onClick='JavaScript:LinkMenu.deactive("+ menuIdx +","+ idx +"); LinkMenu.click("+ menuIdx +","+ idx +");'";
					}
				}
			}
			html += "</td>";

			if( hassub ) {
				html += "<td id='_linkmenu_"+ menuIdx +"_"+ idx +"' class='linkmenu_title' "+ link +">"+ title +"</td>";
				html += "<td id='_linkmenu_"+ menuIdx +"_"+ idx +"_arr' class='linkmenu_arrow' "+ link +">";
				html += "<img id='_linkmenu_"+ menuIdx +"_"+ idx +"_arrimg' src="+ this.image_arrow.src +"></td>";
			} else
				html += "<td id='_linkmenu_"+ menuIdx +"_"+ idx +"' class='linkmenu_title' colspan='2' "+ link +">"+ title +"</td>";
			html += "<td width='1'></td>";
		}
		html += "</tr>";
	}
	html += "</table>";

	if( !this.dialogBoxArray[menuIdx] )
		this.dialogBoxArray[menuIdx] = new DialogBox( "linkmenu" );
	var height = this.height_one * (menuList.length - spacers) + 8 * spacers + this.height_head + this.height_tail;
	this.dialogBoxArray[menuIdx].resize( this.width, height );
	this.dialogBoxArray[menuIdx].setInnerHTML( html );
}

LinkMenu.prototype.show = function() {
	if( LinkMenu.activeMenu != this ) LinkMenu.hide();

	if( !this.dialogBoxArray[0] ) this.print( 0 );

	var offsetX, offsetY;
	if( event ) {
		offsetX = document.body.scrollLeft + event.clientX;
		offsetY = document.body.scrollTop + event.clientY;
	} else {
		var eventObj = document.createEventObject();
		offsetX = document.body.scrollLeft + eventObj.clientX;
		offsetY = document.body.scrollTop + eventObj.clientY;
	}

	this.dialogBoxArray[0].show();

	var span = this.dialogBoxArray[0].span;
	if( offsetY >= document.body.clientHeight - span.clientHeight )
		offsetY -= span.clientHeight;

	if( offsetX >= document.body.clientWidth - span.clientWidth ) {
		offsetX -= span.clientWidth;
		this.onLeft = false;
	} else
		this.onLeft = true;

	this.dialogBoxArray[0].moveTo( offsetX, offsetY );

	LinkMenu.activeMenu = this;
	if( !LinkMenu.timeOutId ) {
		LinkMenu.timeOutId = window.setTimeout( LinkMenu.hide, LinkMenu.timeOutMS );
		document.onmousedown = LinkMenu.hide;
		document.onmousewheel = LinkMenu.hide;
	}
}

LinkMenu.prototype.showSub = function( menuIdx, menuList, relativeY ) {
	this.menuListArray[menuIdx] = menuList;

	this.print( menuIdx );
	var sub_span = this.dialogBoxArray[menuIdx].span;
	var upp_span = this.dialogBoxArray[menuIdx-1].span;

	var left, top;
	if( this.onLeft ) {
		if( upp_span.style.posLeft > document.body.clientWidth - sub_span.clientWidth * 2 - 10 )
			left = upp_span.style.posLeft - sub_span.clientWidth + 2;
		else
			left = upp_span.style.posLeft + sub_span.clientWidth - 4;
	} else
		left = upp_span.style.posLeft - sub_span.clientWidth + 2;

	if( upp_span.style.posTop + relativeY < document.body.clientHeight - sub_span.clientHeight )
		top = upp_span.style.posTop + relativeY - 1;
	else
		top = upp_span.style.posTop + relativeY + this.height_one - this.height_tail - sub_span.clientHeight + 2;

	sub_dialogBox.moveTo( left, top );
	sub_dialogBox.show();
}
