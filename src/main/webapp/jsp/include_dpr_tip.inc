<%--
	File Name:	include_dpr_tip.inc
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/10/30		2.2.0	IE10 ?? ????? ??.
	stghr12		2004/06/03		1.0.0	create
--%>

<style type='text/css'><!--
	table.password_tip {
		border: 1px solid #B45500;
		width: 270px;
	}

	ul.password_tip {
		background: #FEFFED;
		border: 0px solid #959595;
		width: 270px;
		margin: 0;
		padding: 8px 5px 5px;
	}

	ul.password_tip li {
		color: #666666;
		margin-bottom: 3px;
		margin-left: 17px;
		padding-bottom: 2px;
	}

	ul.password_tip li span {
		font-weight: bold;
	}
//--></style>

<script type='text/javascript'>
	function ToolTip() {}
	ToolTip.dialogBox = null;

	ToolTip.hide = function() {
		if( ToolTip.dialogBox != null )
			ToolTip.dialogBox.hide();
	}

	ToolTip.print = function() {
		var html = "";

		html += "<table class='password_tip' cellspaceing='0' cellpadding='0'>";
		html += "<tr><td>";
		html += "<ul class='password_tip'>";
		html += "<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_1"/>";
		html += "<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_2"/>";
		html += "<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_3"/>";
		html += "<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_4" encodeScript="true"/>";
		html += "<li><mtl:message key="MSG_PASSWORD_EXPLICATE" />";
		html += "</ul></td></tr></table>";

		if( !ToolTip.dialogBox )
			ToolTip.dialogBox = new DialogBox( "tooltip" );

		ToolTip.dialogBox.setInnerHTML( html );
	}

	ToolTip.show = function() {
        ToolTip.print();

        var offsetX, offsetY;
		offsetX = document.body.scrollLeft + event.clientX;
		offsetY = document.body.scrollTop + event.clientY;

        var WIDTH = 270, HEIGHT = 190;
        var MARGIN_X = 15, MARGIN_Y = 15;

        offsetY = ( offsetY < 80 + MARGIN_Y ? MARGIN_Y : offsetY + 10 );
        if( offsetY + HEIGHT + MARGIN_Y > document.body.offsetHeight ) {
            offsetY = document.body.offsetHeight - HEIGHT;
            if( offsetY < 0 )
                offsetY = 0;
            else if( offsetY > MARGIN_Y*2 )
                offsetY -= MARGIN_Y;
            else
                offsetY /= 2;
        }

        offsetX = ( offsetX < 80 + MARGIN_X ? MARGIN_X : offsetX - 30 );
        if( offsetX + WIDTH + MARGIN_X + 15 > document.body.offsetWidth ) {
            offsetX = offsetX - WIDTH;
            if( offsetX < 0 ) {
                offsetX = ( document.body.offsetWidth - 15 - WIDTH ) / 2;
                if( offsetX < 0 ) offsetX = 0;
            } else if( offsetX < MARGIN_X )
                offsetX = MARGIN_X;
        }

        ToolTip.dialogBox.moveTo( offsetX, offsetY );
        ToolTip.dialogBox.show();
        ToolTip.dialogBox.resize( document.getElementsByClassName("password_tip")[0].offsetWidth - 3, offsetY = document.getElementsByClassName("password_tip")[0].offsetHeight -3 );
    }

	function openToolTip() {
		ToolTip.show();
	}

	function hideToolTip() {
		ToolTip.hide();
	}
</script>
