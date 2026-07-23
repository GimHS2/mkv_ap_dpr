<%--
	File Name:	include_ics_ajax.inc
	Version:	2.2.1

	Description:
		<head>에 추가

		script
			createXMLRequest();
			showResponse( responseText );

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2010/07/31		2.2.1	정리
	lsinji		2009/10/25		2.2.0	create
--%>

<script type='text/javascript'>
	function createXMLRequest() {
		var request;

		try {
			request = new XMLHttpRequest();
		} catch( microsoft ) {
			try {
				request = new ActiveXObject( "Msxml2.XMLHTTP" );
			} catch( otherversion ) {
				try {
					request = new ActiveXObject( "Microsoft.XMLHTTP" );
				} catch( failed ) {
					request = null;
				}
			}
		}

		if( request == null )
			alert( "<mtl:message key="ERR_ICS_NOT_SUPPORT_AJAX"/>" );

		return request;
	}

	function showResponse( responseText ) {
		var winObj = window.open( "about:blank", "_blank", "width=50, height=50, resizable=false, toolbar=no, menubar=no, status=no" );
		winObj.document.write( responseText );
		if( winObj.onLoad ) winObj.onLoad();
	}
</script>
