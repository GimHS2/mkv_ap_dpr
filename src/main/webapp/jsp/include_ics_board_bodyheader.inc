<%--
	File Name:	include_ics_board_bodyheader.inc
	Version:	2.2.6c

	Description:
		script
			debugQueryEnable( idx, title, html );
			debugQueryShow( idx );
			openAlertWindow();
			openNewWindow();
			closeAlertWindow();
		script(using)
			getLocationURL();
		style
			table.full_size
			table.msg
			td.title
			td.msg
			td.path
			td.page_env

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/01/30		2.2.6c	pageedit 기능 추가
	jbaek		2016/08/31		2.2.6	alertContent 창의 z-index 우선순위를 높여주어 다른 콘텐트와 중첩되지 않도록 설정.
	GimHS		2015/07/31		2.2.5	modal용 div 태그 추가
	GimHS		2015/05/29		2.2.4	CrossBrowsing 적용: 오타 수정
	GimHS		2015/04/30		2.2.3	reload 버튼 클릭시 Loading 화면이 나오도록 수정
	GimHS		2012/12/31		2.2.2	CrossBrowsing 적용
										  -> 'cursor: hand;' -> 'cursor: pointer;' 스타일 변경
										  -> Debug SQL 기능에서 html으로 request시 parameter가 안넘어가는 문제가 있어 Servlet으로 구현
										  -> style 값 끝에 ";" 추가
	stghr12		2010/07/31		2.2.1	정리
	bvox		2009/10/25		2.2.0	create
--%>

	<style type='text/css'>
		#alertContent {
			width: 320px;
			height: 0px;
			overflow:hidden;
			visibility: hidden;
			position:absolute;
			LEFT:expression((document.body.clientWidth/2)-160);
			z-index: 999;
		}

		body { margin: 0px; }
		div.paragraph {
			width: 300px;
			height: 0.1em;
			overflow:hidden;
			visibility: hidden;
			background-color: #e6e6e6;
			color: #000000;
			padding: 4px;
			border: 4px solid #545454;
			margin: 4px;
			z-index: 10;
			position:absolute;
			font-size: 22px;
			LEFT:expression((document.body.clientWidth/2)-160);
		}

		.ie6divBugFix {
			 position: absolute;
			 top:0; left:0;
			 z-index:-1;
			 filter:mask();
		}
	</style>

	<script type='text/javascript'>
		function closeAlertWindow() {
			var obj = document.getElementById( "alertContent" );
			if( openAlertWindow.startHeight == 3 ) {
				obj.style.visibility = 'hidden';
				obj.style.height = '0px';
				clearInterval( openAlertWindow.interval );
				return;
			}
			openAlertWindow.startHeight -= 3;
			obj.style.height = openAlertWindow.startHeight +'px';
		}

		function openAlertWindow() {
			if( openAlertWindow.startHeight == 123 ) {
				clearInterval( openAlertWindow.interval );
				return;
			}

			var obj = document.getElementById( "alertContent" );
			obj.style.visibility = 'visible';
			openAlertWindow.startHeight += 3;
			obj.style.height = openAlertWindow.startHeight +'px';
		}
		openAlertWindow.startHeight = 0;
		openAlertWindow.interval = 0;

		function openNewWindow() {
			window.open( getLocationURL() );
		}

		function setMessage( message ) {
			document.all.msg.innerHTML = message;
			openAlertWindow.interval = setInterval("openAlertWindow()" , 3);
		}
	</script>

	<div id='alertContent'>
		<table width='100%' height='100%' border='0' cellpadding='0' cellspacing='0' style='background-color:#ffffff; border: 1px solid #1D2028;'>
		<tr height='10'>
			<td style='padding: 3px; background-color: #d2d2d2; border-bottom: solid 1px #595959;'>
				<img src='images/board/ico_alert.gif' align='absmiddle'>&nbsp;&nbsp;
				<font color='red'><mtl:message key="MSG_ICS_BOARD_ALERT"/></font>
			</td>
			<td style='padding: 3px; background-color: #d2d2d2; border-bottom: solid 1px #595959;'>
				<img src='images/board/ico_alert_close.gif' align='right' style='cursor: pointer;'
						onClick='openAlertWindow.interval=setInterval("closeAlertWindow()", 3); return false;'>
			</td>
		</tr>
		<tr height='75'>
			<td style='padding:10px;' id='msg' align='center' colspan='2'><%= HtmlUtility.toHtmlString(htmlpage.getMessage()) %>&nbsp;</td>
		</tr>
		<tr>
			<td height='10' align='center' colspan='2' style='padding:5px'>
				<img src='images/board/ico_alert_confirm.gif' border='0' style='cursor: pointer;'
						onclick='openAlertWindow.interval=setInterval("closeAlertWindow()",3); return false;'/>
			</td>
		</tr>
		</table>
		<iframe class='ie6divBugFix'></iframe>
	</div>


<% if( pageContext.findAttribute("queryStorage") != null ) { %>
	<div id='_page_env_' class='page-function' style='position: absolute; right: 15; z-index: 100;'>
	</div>
	<span id='_debug_query' style='display: none'><%@ include file="include_pub_query.inc" %></span>
	<script type='text/javascript'>
		function DebugQuery() {}

		DebugQuery.size = 0;
		DebugQuery.pageenv = document.getElementById( "_page_env_" );
		DebugQuery.insertPosition = DebugQuery.pageenv.firstChild;

		DebugQuery.append = function( html, title ) {
			if( !html ) return;
			DebugQuery.size++;

			var span = document.createElement( "SPAN" );
			span.id = "_debug_query_" + DebugQuery.size;
			span.style.display = "none";
			span.innerHTML = html;

			var image = document.createElement( "IMG" );
			image.src = "images/lbtn_sql.gif";

			var anchor = document.createElement( "A" );
			anchor.href = "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>query.html?name="+ span.id;
			anchor.target = "winDebug";
			if( title ) anchor.title = title;
			anchor.appendChild( image );

			DebugQuery.pageenv.appendChild( span );
			DebugQuery.pageenv.insertBefore( anchor, DebugQuery.insertPosition );
			DebugQuery.pageenv.insertBefore( document.createTextNode(" "), DebugQuery.insertPosition );
		}

		DebugQuery.append( document.all._debug_query.innerHTML, "SQL" );
	</script>
<% } %>

	<iframe name='subwin_util' style='display: none' src='<%= systemConfig.getProperty("baseURL") %>blank.html'></iframe>

	<div class='content-overlay'>
		<div class='loading'>
			<img src="images/loading.gif">
			<p class='msg'></p>
		</div>
		<div class='sub-content-wrap'></div>
	</div>

	<div class='popup-overlay'>
		<div class='popup confirm'>
			<p class='header-msg'></p>
			<p class='msg'></p>
			<span style='float: right'>
				<button type="button" name='confirm' class='btn btn-primary' style='width: 110px'><mtl:message key="jsp.BTN_OK"/></button>
				<button type="button" name='cancel' class='btn btn-secondary' style='width: 110px'><mtl:message key="jsp.BTN_CANCEL"/></button>
			</span>
		</div>
		<div class='popup alert'>
			<p class='header-msg'></p>
			<p class='msg'></p>
			<span style='float: right'>
				<button type="button" name='confirm' class='btn btn-primary' style='width: 110px'><mtl:message key="jsp.BTN_CONFIRM"/></button>
			</span>
		</div>
	</div>

	<div class='modal'></div>
