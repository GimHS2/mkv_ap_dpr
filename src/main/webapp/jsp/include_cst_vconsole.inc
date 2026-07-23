<%--
	File Name:	include_cst_vconsole.inc
	Version:	2.2.0

	Description:
		vconsole for debugging

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/03/30		2.2.0	create
--%>


<% if( sessionMng.isSystemAdmin()
		&& ( htmlpage.getUserAgentString().indexOf("MSIE 8.0") < 0 )// no support ie8
		&& (
// 				htmlpage.isMobileAgent()
true
				|| htmlpage.getUserAgentString().indexOf("Edge") >= 0 )
		) { %>
	<script type='text/javascript' src='script/assets/js/vconsole.min.js'></script>
	<script type='text/javascript' id='vconsole.init'>
		/* var isChrome = /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor); */
		var isBrowserGoogle = (navigator.vendor ? navigator.vendor.match(/Google Inc/) : false);
		var isPlatformLinux = navigator.platform.match(/Linux/);
		var isPlatformWindows = navigator.platform.match(/Win/);
		var isPlatformMac = navigator.platform.match(/Mac/);
		var isEdge = navigator.userAgent.match(/Edge/);// somewhat edge developer tool cannot opened now.
		var isAndroid = navigator.userAgent.match(/Android/i);
		var isIos = navigator.userAgent.match(/iPhone|iPad|iPod/i);
		if( window.VConsole && !window.vConsole
				&& ( !(
						isPlatformLinux
						|| isPlatformWindows
// 						|| isPlatformMac // cannot detect mac desktop and ios userAgent is desktop
						)
					|| isEdge )
				&& ( isAndroid
						|| isIos
						|| isEdge )
			) {
			var vConsole = new VConsole();
			console.log("vconsole enabled("+vConsole.version+")", vConsole);
			if( window.ClassWatcher ) {
				window.addEventListener("load", function(){
					var vCon = document.querySelector("#__vconsole");
					if( vCon ) {
						var vConClassFunc = {
							"added" : function() {
								console.log("vconsole opened. win-w:"+$(window).width()+" win-h:"+$(window).height());
							},
							"removed" : function() {
								console.log("vconsole hidden.");
							}
						};
						var vConWatcher = new ClassWatcher(vCon, "vc-toggle", vConClassFunc.added, vConClassFunc.removed);
					};
				});
			}
		};
	</script>
<% } %>
