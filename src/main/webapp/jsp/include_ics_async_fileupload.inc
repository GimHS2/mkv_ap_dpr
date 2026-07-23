<%--
	File Name:	include_ics_async_fileupload.inc
	Version:	2.2.1

	Description:
		script

		script(using)
	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2010/07/31		2.2.1	Á¤¸®
	lsinji		2009/10/25		2.2.0	create
--%>

<script type='text/javascript'>
	// async file upload
	AFU = {
		frame : function( parms ) {
			var frameId = "f"+ Math.floor(Math.random() * 99999);
			var oDIV = document.createElement( "DIV" );
			oDIV.innerHTML = "<iframe style='display: none;' src='about:blank' id='"+ frameId +"' name='"+ frameId +"' onLoad='JavaScript: AFU.loaded(\""+ frameId +"\");'></iframe>";
			document.body.appendChild( oDIV );

			var oFrame = document.getElementById( frameId );
			if( parms && typeof(parms.onComplete) == "function" )
				oFrame.onComplete = parms.onComplete;

			return frameId;
		},

		form : function( oForm, targetName ) {
			oForm.setAttribute( "target", targetName );
		},

		submit : function( oForm, parms ) {
			AFU.form( oForm, AFU.frame(parms) );
			if( parms && typeof(parms.onStart) == "function" )
				return parms.onStart();
			else
				return true;
		},

		loaded : function( id ) {
			var oFrame = document.getElementById( id );
			var frameDocument;
			if ( oFrame.contentDocument )
				frameDocument = oFrame.contentDocument;
			else if( oFrame.contentWindow )
				frameDocument = oFrame.contentWindow.document;
			else
				frameDocument = window.frames[id].document;

			if( typeof(frameDocument) == "undefined" || frameDocument.location.href == "about:blank" )
				return;

			if( typeof(oFrame.onComplete) == "function" )
				oFrame.onComplete( frameDocument.body.innerHTML );
		}
	}
</script>
