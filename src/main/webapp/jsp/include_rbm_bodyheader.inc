<%--
	File Name:	include_rbm_bodyheader.inc
	Version:	2.2.2c(dpr)

	Description:
		script
			debugQueryEnable( idx, title, html );
			debugQueryShow( idx );
			openNewWindow();
		script(using)
			getLocationURL();
		style
			pre#msg
			table.msg
			td.title
			td.path
			td.page_env

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.2c	신규 UI/UX 적용
	jbaek		2019/01/30		2.2.2c	pageedit 기능 추가.
	lsinji		2009/01/02		2.2.2	iframe.src를 blank.html로 설정
	stghr12		2008/03/31		2.2.1	table.full_size -> width='100%'
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
										td#msg -> pre#msg
	stghr12		2007/04/30		2.1.1	windowType "inner" 처리
	stghr12		2006/12/01		2.1.0	제목이 들어간 table에 id부여: content_title
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/03		1.0.0	create
--%>


<script type='text/javascript'>
	$(function() {
		var title;
	<% if( htmlpage.getWindowType() == null || !"sub".equals(htmlpage.getWindowType().split("@")[0]) ) { %>
		title = "<%= HtmlUtility.toScriptString( htmlpage.getTitle() ) %>";
	<% } else { %>
		title = "<%= HtmlUtility.toScriptString( htmlpage.getSubTitle() ) %>";
	<% } %>

		if( window.name === "sub-content" ) {
			title = "<%= HtmlUtility.toScriptString( htmlpage.getTitle() ) %>";
		}
	});
	function openNewWindow() {
		window.open( getLocationURL() );
	}
</script>

<div class='content-title'>
<%
	if( htmlpage.getWindowType() == null || !"sub".equals(htmlpage.getWindowType().split("@")[0]) ) {
		String[] path = (String[]) pageContext.findAttribute( "path" );
		if( path != null ) {
			out.print( "<div class='title-path'>" );
			for( int i = 0; i < path.length; i++ ) {
				if( i < path.length - 1 ) {
					out.print( "<span>" + path[i] + "</span> / " );
				} else {
					out.print( path[i] );
				}
			}
			out.println( "</div>" );
		}
	}
%>
	<div class='title'>
	<% if( htmlpage.getWindowType() == null || !"sub".equals(htmlpage.getWindowType().split("@")[0]) ) { %>
		<h1><%= HtmlUtility.toHtmlString( htmlpage.getTitle() ) %></h1>
	<% } else {
		String title = htmlpage.getSubTitle();
		if( title == null || title.trim().length() < 1 ) {
			title = htmlpage.getTitle();
		}
		title = HtmlUtility.toHtmlString( title );
	%>
		<h2><%= title %></h2>
	<% } %>
		<div id='_page_env_' class='page-function'>
		<% if( sessionMng.isSystemAdmin() ) { %>
		<% if( ( htmlpage.getWindowType() == null || !"sub".equals(htmlpage.getWindowType().split("@")[0]) ) && "GET".equals(request.getMethod()) ) { %>
			<mtl:button type="s_button" onClick="JavaScript: toggleLoading(true); location.replace(getLocationURL());"
					titleKey="jsp.include_rbm_bodyheader.RELOAD" messageKey="jsp.BTN_RELOAD"/>
			<mtl:button type="s_button" onClick="JavaScript:openNewWindow();"
					titleKey="jsp.include_rbm_bodyheader.OPEN_NEW_WINDOW" messageKey="jsp.BTN_NEW_WINDOW"/>
		<% } %>
		<% } %>
		</div>
	</div>
</div>

<% if( pageContext.findAttribute("queryStorage") != null ) { %>
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
			anchor.href = "<%= systemConfig.getClassURL() %>/Query?name="+ span.id;
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
			<mtl:button type="button" name="cancel" styleClass="btn btn-secondary w110" messageKey="jsp.BTN_CANCEL"/>
			<mtl:button type="button" name="confirm" styleClass="btn btn-primary w110" messageKey="jsp.BTN_OK"/>

		</span>
	</div>
	<div class='popup dialog'>
		<p class='header-msg'></p>
		<p class='msg'></p>
		<span style='float: right'>
			<mtl:button type="button" name="confirm" styleClass="btn btn-primary w110" messageKey="jsp.BTN_YES"/>
			<mtl:button type="button" name="cancel" styleClass="btn btn-secondary w110" messageKey="jsp.BTN_NO"/>
		</span>
	</div>
	<div class='popup alert'>
		<p class='header-msg'></p>
		<p class='msg'></p>
		<span style='float: right'>
			<mtl:button type="button" name="confirm" styleClass="btn btn-primary w110" messageKey="jsp.BTN_CONFIRM"/>
		</span>
	</div>
</div>
