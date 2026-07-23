<%--
	File Name:	include_rbm_catelink.inc
	Version:	2.2.0

	Description:
		<head>에 추가

		script
			prefix
			categoryLinkActive();
			categoryLinkDeActive();
			categoryLinkHide();
			categoryLinkMenu( elementId );
		script(using)
			DialogBox
			Styles.changeDisplay();
			categoryLinkReq( code );

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
	stghr12		2007/04/30		2.1.0	create
--%>

<script type='text/javascript'>
	var categorySelectBox = null;

	function categoryLinkActive() {
		document.onmousedown = null;
		document.onmousewheel = null;
	}

	function categoryLinkDeActive() {
		document.onmousedown = categoryLinkHide;
		document.onmousewheel = categoryLinkHide;
	}

	function categoryLinkHide() {
		document.onmousedown = null;
		document.onmousewheel = null;

		categorySelectBox.hide( true );
		categorySelectBox = null;
	}

	function categoryLinkMenu( elementId ) {
		var innerHTML = "";
		var categoryObj = document.getElementById( elementId );

		if( elementId ) {
			if( !categoryObj ) return;
		} else {
			categoryLinkReq("");
			return;
		}

		if( categoryObj.options.length >= 10 )
			innerHTML += "<select name='categoryCode' onClick='categoryLinkReq(this.value);' size='10'>";
		else if( categoryObj.options.length <= 5 )
			innerHTML += "<select name='categoryCode' onClick='categoryLinkReq(this.value);' size='5'>";
		else
			innerHTML += "<select name='categoryCode' onClick='categoryLinkReq(this.value);' size='"+ categoryObj.options.length +"'>";
		innerHTML += categoryObj.innerHTML;
		innerHTML += "</select>";

		categorySelectBox = new DialogBox();
		categorySelectBox.setInnerHTML( innerHTML );
		categorySelectBox.resize( categoryObj.clientWidth, categoryObj.clientHeight );
		if( event ) {
			categorySelectBox.moveTo( document.body.scrollLeft + event.clientX, document.body.scrollTop + event.clientY );
			event.returnValue= false;
		} else {
			var eventObj = document.createEventObject();
			categorySelectBox.moveTo( document.body.scrollLeft + eventObj.clientX, document.body.scrollTop + eventObj.clientY );
		}
		categorySelectBox.show();
		categorySelectBox.span.onmouseover = categoryLinkActive;
		categorySelectBox.span.onmouseout = categoryLinkDeActive;
		categoryLinkDeActive();
	}
</script>
