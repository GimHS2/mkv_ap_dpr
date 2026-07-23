<%--
	File Name:	include_rbm_frame_bodyheader.inc
	Version:	2.2.0c(dpr)

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
	hankalam	2021/11/30		2.2.0c	create
--%>

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
			<mtl:button type="button" name="cancel" styleClass="btn btn-secondary w110" messageKey="jsp.BTN_CANCEL"/>
			<mtl:button type="button" name="confirm" styleClass="btn btn-primary w110" messageKey="jsp.BTN_OK"/>

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
