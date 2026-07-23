<%--
	File Name:	include_cst_event_header.inc
	Version:	1.0.0c

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2018/12/30		1.0.0c	create
--%>

<script type='text/javascript'>
	window.enableSnowplow = false;
	<% if( "Y".equals(systemConfig.getProperty("useJsSnowplow")) ) { %>
			window.enableSnowplow = true;
	<% } %>
</script>





<script type="text/javascript" src="script/evm/element-xpath.min.js" ></script>
<!--script type='text/javascript' src="script/evm/pouchdb.min.js"></script-->
<!--script type='text/javascript' src="script/evm/html2canvas.min.js"></script-->
<script type='text/javascript' src="script/evm/tableex.js"></script>

<script type='text/javascript'>
	/**
	 * Convert a base64 string in a Blob according to the data and contentType.
	 * 
	 * @param b64Data {String} Pure base64 string without contentType
	 * @param contentType {String} the content type of the file i.e (image/jpeg - image/png - text/plain)
	 * @param sliceSize {Int} SliceSize to process the byteCharacters
	 * @see http://stackoverflow.com/questions/16245767/creating-a-blob-from-a-base64-string-in-javascript
	 * @return Blob
	 */
	function b64toBlob(b64Data, contentType, sliceSize) {
        contentType = contentType || '';
        sliceSize = sliceSize || 512;

        var byteCharacters = atob(b64Data);
        var byteArrays = [];

        for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
            var slice = byteCharacters.slice(offset, offset + sliceSize);

            var byteNumbers = new Array(slice.length);
            for (var i = 0; i < slice.length; i++) {
                byteNumbers[i] = slice.charCodeAt(i);
            }

            var byteArray = new Uint8Array(byteNumbers);

            byteArrays.push(byteArray);
        }

		var blob = new Blob(byteArrays, {type: contentType});
		return blob;
	}

</script>

<script type='text/javascript'>
	if( window['enableSnowplow'] == true ) {
		;(function(p,l,o,w,i,n,g){if(!p[i]){p.GlobalSnowplowNamespace=p.GlobalSnowplowNamespace||[];
			p.GlobalSnowplowNamespace.push(i);p[i]=function(){(p[i].q=p[i].q||[]).push(arguments)
			};p[i].q=p[i].q||[];n=l.createElement(o);g=l.getElementsByTagName(o)[0];n.async=1;
			n.src=w;g.parentNode.insertBefore(n,g)}}(window,document,"script","script/evm/sp.js","snowplow"));

		var collector_uri = location.hostname +':'+ location.port;
		//	var collector_uri = '192.168.0.83:3000';
		//	var collector_uri = '192.168.0.83:invalid';
		var app_id = 'dpr';
		var cookie_domain = location.hostname;
		//	var cookie_domain = '127.0.0.1';
		window.snowplow('newTracker', 'cf', collector_uri, { // Initialise a tracker
			appId: app_id,
															 cookieDomain: cookie_domain,
															 platform: 'web'
		});

		window.snowplow('trackPageView');

		window.snowplow(function(){
			var cf = this.cf;
			var domainUserInfo = cf.getDomainUserInfo();
			var domainUserId = cf.getDomainUserId();
			console.log("domainUserInfo: "+ domainUserInfo);
			console.log("domainUserId: "+ domainUserId);
		});
	}
</script>
<script type='text/javascript'>

	function EventMon() {}

	/**
	 * @param {Event} event
	 */
	EventMon.consoleLogEventHandler = function(event) {
		// Do whatever you want here... 
		var xp = getElementXpath(event.toElement);
		console.log("my custom handler: ", event.type, event.toElement, event, "xpath: ", xp);
	}

	EventMon.log4jsLogEventHandler = function(event) {
		if( window.log4javascript ) {
			// Do whatever you want here... 
			var xp = getElementXpath(event.toElement);
			log4javascript.getLogger("client").trace("event.type: ", event.type, "event.element: ", event.toElement, "event.object: ", event, "event.xpath: ", xp);
		}
	}

	/**
	 * @param {Object} object
	 * @param {Function} callback
	 */
	EventMon.startMonitorEvents = function(object, callback) {
		if (!object || !object.addEventListener || !object.removeEventListener)
			return;

		var types = this.getEventsForMonitor();
		for (var i = 0; i < types.length; ++i) {
			object.removeEventListener(types[i], callback, false);
			object.addEventListener(types[i], callback, false);
		}
	}

	EventMon.getEventsForMonitor = function() {
		var result = [];

		// mouse-all
		//result.splice(0, 0, "mousedown", "mouseup", "click", "dblclick", "mousemove", "mouseover", "mouseout", "mousewheel");

		// mouse-simple
		//result.splice(0, 0, "click", "dblclick", "mouseover", "mousewheel");

		// mouse-click
		//result.splice(0, 0, "click", "dblclick");

		// key
		result.splice(0, 0, "keydown", "keyup", "keypress", "textInput");
		// touch
		result.splice(0, 0, "touchstart", "touchmove", "touchend", "touchcancel");
		// control
		result.splice(0, 0, "resize", "scroll", "zoom", "focus", "blur", "select", "change", "submit", "reset");
		// others
		result.splice(0, 0, "load", "unload", "abort", "error", "select", "change", "submit", "reset", "focus", "blur", "resize", "scroll", "search", "devicemotion", "deviceorientation");

		return result;
	}


	<% if( "Y".equals(systemConfig.getProperty("traceJS")) ) { %>
	EventMon.startMonitorEvents(window, EventMon.log4jsLogEventHandler);
	<% } %>

</script>

<!--script type='text/javascript' src="script/evm/evm.js"></script-->
<!--
	 <script type='text/javascript'>

	 if( window['enableMondb'] ) {
	 window.snowplow(function() {

	 if( true )
	 return;

	 var cf = this.cf;

	 /*
	 0: A string set to '1' if this is the user's first session and '0' otherwise
	 1: The domain user ID
	 2: The timestamp at which the cookie was created
	 3: The number of times the user has visited the site
	 4: The timestamp for the current visit
	 5: The timestamp of the last visit
	 6: The session id
	 */
	 var domainUserInfo = cf.getDomainUserInfo();

	 var domainUserId = cf.getDomainUserId();
	 var userFingerprint = cf.getUserFingerprint();

	 mondb.get({_id: saveId}).then(function(doc){
	 console.log(doc._id, doc);
	 });

	 });
	 }
	 </script>
-->
