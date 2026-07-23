<%--
	File Name:	include_ics_board_comment.inc
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2012/12/31		2.2.2	CrossBrowsing 적용: 'cursor: hand;' -> 'cursor: pointer;' 스타일 변경
	stghr12		2010/07/31		2.2.1	정리
	lsinji		2009/10/25		2.2.0	create
--%>

<script type='text/javascript'>
	var commentRequest;

	function changeCommentStatus( type, commentNumber, html ) {
		var commentTextareaObj = document.getElementById( "comm_content_textarea" );
		if( commentTextareaObj ) {
			var commentId = commentTextareaObj.commentNumber;
			if( commentTextareaObj.type == "modify" ) {
				document.getElementById( "comm_content_"+ commentId ).style.display = "";
				document.getElementById( "comm_content_area_"+ commentId ).removeChild( commentTextareaObj );
				document.getElementById( "comm_fcmsg_modify_"+ commentId ).innerHTML
						= "<mtl:message key="MSG_ICS_BOARD_COMMENT_FC_MODIFY" encodeScript="true"/>";
			} else {
				document.getElementById( "comm_reply_area_"+ commentId ).innerHTML = "";
				document.getElementById( "comm_fcmsg_reply_"+ commentId ).innerHTML
						= "<mtl:message key="MSG_ICS_BOARD_COMMENT_FC_REPLY" encodeScript="true"/>";
			}
			if( commentTextareaObj.type == type && commentTextareaObj.commentNumber == commentNumber ) return;
		}

		if( type == "modify" ) {
			var commentAreaObj = document.getElementById( "comm_content_area_"+ commentNumber );

			document.getElementById( "comm_content_"+ commentNumber ).style.display = "none";
			document.getElementById( "comm_fcmsg_modify_"+ commentNumber ).innerHTML
					= "<mtl:message key="MSG_ICS_BOARD_COMMENT_FC_MODIFY_CANCEL" encodeScript="true"/>";
			commentAreaObj.innerHTML = commentAreaObj.innerHTML + html;
		} else {
			document.getElementById( "comm_reply_area_"+ commentNumber ).innerHTML = html;
			document.getElementById( "comm_fcmsg_reply_"+ commentNumber ).innerHTML
					= "<mtl:message key="MSG_ICS_BOARD_COMMENT_FC_REPLY_CANCEL" encodeScript="true"/>";
		}
		Field.setCursor( document.getElementById("commentContent") );
	}

	function deleteComment( commentObj ) {
		var commentNumber = commentObj.id.substring( commentObj.id.lastIndexOf("_") + 1 );

		if( commentRequest != null && typeof commentRequest.readyState != 'undefined' ) {
			alert( "<mtl:message key="MSG_ICS_REQUEST_ALREADY_USED" encodeScript="true"/>" );
			return;
		}

		if( confirm( "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" ) ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=comm&type=delete&wintype=sub";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "boardNumber", "<mtl:value id="record" key="boardNumber"/>" );
			url = replaceQueryValue( url, "commentNumber", commentNumber );

			commentRequest = createXMLRequest();
			if( commentRequest != null ) {
				commentRequest.onreadystatechange = requestComplete;
				commentRequest.open( "GET", url, true );
				commentRequest.send( null );
			}
		}
	}

	function modifyComment( commentObj ) {
		var commentNumber = commentObj.id.substring( commentObj.id.lastIndexOf("_") + 1 );

		var contentObj = document.getElementById( "comm_content_"+ commentNumber );
		var script = "JavaScript:updateComment(\"modify\", "+ commentNumber +");";
		var innerText =
			"<span id='comm_content_textarea' commentNumber='"+ commentNumber +"' type='modify'>"
			+ "<textarea id='commentContent' rows='5' wrap='physical' style='width: 85%'>"+ contentObj.innerHTML +"</textarea>"
			+ "&nbsp;&nbsp;&nbsp;"
			+ "<img src='images/board/btn_comment.gif' onClick='"+ script +"' style='cursor: pointer;'>";
			+ "</span>";

		changeCommentStatus( "modify", commentNumber, innerText );
	}

	function replyComment( commentObj ) {
		var commentNumber = commentObj.id.substring( commentObj.id.lastIndexOf("_") + 1 );

		var script = "JavaScript:updateComment(\"reply\", "+ commentNumber +");";
		var innerText =
			"<span id='comm_content_textarea' commentNumber='"+ commentNumber +"' type='reply'>"
			+ "<textarea id='commentContent' rows='5' wrap='physical' style='width: 85%; top: -20px'></textarea>"
			+ "&nbsp;&nbsp;&nbsp;"
			+ "<img src='images/board/btn_comment_reply.gif' onClick='"+ script +"' style='cursor: pointer;'>";
			+ "</span>";

		changeCommentStatus( "reply", commentNumber, innerText );
	}

	function requestComplete() {
		if( commentRequest.readyState == 4 ) {
			if( typeof commentRequest.responseText != 'undefined' ) {
				var html = commentRequest.responseText;
				if( html != null && html.indexOf("<html>") > 0 )
					showResponse( html );
				else if( html != null )
					document.getElementById("commentList").innerHTML = commentRequest.responseText;
			}

			commentRequest = null;
		}
	}

	function updateComment( type, commentNumber ) {
		if( commentRequest != null && typeof commentRequest.readyState != 'undefined' ) {
			alert( "<mtl:message key="MSG_ICS_REQUEST_ALREADY_USED" encodeScript="true"/>" );
			return;
		}

		var commentObj = document.getElementById( type == "regist" ? "newContent" : "commentContent" );
		if( commentObj.value == "" ) {
			if( type == "modify" )
				alert("<mtl:message key="MSG_ICS_BOARD_ENTER_COMMENT_MODIFY" encodeScript="true"/>");
			else
				alert("<mtl:message key="MSG_ICS_BOARD_ENTER_COMMENT" encodeScript="true"/>");
			commentObj.focus();
			return;
		}
		if( commentObj.value.length > 1024 ) {
			alert("<mtl:message key="ERR_ICS_BOARD_COMMENT_LENGTH_LIMIT" encodeScript="true"/>");
			return;
		}

		var url = "<%= htmlpage.getRequestURL() %>?mode=comm&type="+ type +"&wintype=sub";
		url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
		url = replaceQueryValue( url, "boardNumber", "<mtl:value id="record" key="boardNumber"/>" );
		if( type == "modify" )
			url = replaceQueryValue( url, "commentNumber", commentNumber );
		else if( type == "reply" )
			url = replaceQueryValue( url, "originalCommentNumber", commentNumber );
		url = replaceQueryValue( url, "content", encodeURIComponent(commentObj.value) );

		commentRequest = createXMLRequest();
		if( commentRequest != null ) {
			commentRequest.onreadystatechange = requestComplete;
			if( url.length > <%= HtmlPage.MAX_URL_LENGTH %> ) {
				var urls = url.split( "?" );

				commentRequest.open( "POST", urls[0], true );
				commentRequest.setRequestHeader( "Content-Type", "application/x-www-form-urlencoded; charset=utf-8" );
				commentRequest.send( urls[1] );
			} else {
				commentRequest.open( "GET", url, true );
				commentRequest.send( null );
			}
		}
	}
</script>
