<%--
	File Name:	include_pub_linkmenu.inc
	Version:	2.2.0

	Description:
		<head>에 추가

		script
			createLinkMenu( menuList, ... );
		script(using)
			LinkMenu

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										createLinkMenu() 함수 변경
										width 수정: 150 -> 160
	stghr12		2007/04/30		2.1.0	version up(not changed)
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2005/11/30		1.0.0	create
--%>

<script type='text/javascript' src='script/linkmenu.js'></script>
<script type='text/javascript'>
	LinkMenu.menuTimeOutMS = 2000;

	function createLinkMenu( menuList ) {
		if( createLinkMenu.arguments.length > 1 ) {
			var idx = 0;
			var menuList_new = new Array;

			for( var i = 0; i < createLinkMenu.arguments.length; i++ ) {
				var menuList_tmp = createLinkMenu.arguments[i];
				if( menuList_tmp && menuList_tmp.length > 0 ) {
					if( idx > 0 ) menuList_new[idx++] = new Array( LinkMenu.MENU_SPACER );
					for( var v = 0; v < menuList_tmp.length; v++ )
						menuList_new[idx++] = menuList_tmp[v];
				}
			}
			menuList = menuList_new;
		}

		var linkmenu = new LinkMenu( menuList );
/*
		linkmenu.image_arrow.src = "images/ico_linkmenu_arrow.gif";
		linkmenu.image_arrowhl.src = "images/ico_linkmenu_arrowhl.gif";
		linkmenu.image_spacer.src = "images/ico_linkmenu_spacer.gif";
		linkmenu.image_spacerb.src = "images/ico_linkmenu_spacerb.gif";
		linkmenu.image_new.src = "images/ico_linkmenu_newwindow.gif";
*/
		linkmenu.color_bg = "#FFFFFF";
		linkmenu.color_bg_hl = "#F4F4F4";
		linkmenu.color_fg = "#212121";
		linkmenu.color_fg_hl = "#212121";

		linkmenu.width = "auto";
		linkmenu.height_one = "34";
		linkmenu.height_head = 2;
		linkmenu.height_tail = 2;
		linkmenu.menuStyle = "";

		return linkmenu;
	}
</script>
