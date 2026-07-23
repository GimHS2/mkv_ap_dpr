<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<style text="text/css">
* {
	box-sizing: border-box;
	-moz-box-sizing: border-box;
}

.htmlbuttons {
	text-align: center;
	margin-bottom: 20px;
}

.book {
	margin: 0;
	padding: 0;
	background-color: #FAFAFA;
	font: 12pt "Tahoma";
}

.page {
	display: block;
	width: 297mm;
	height: 200mm;
	margin: 0cm auto;
	border: 1px #D3D3D3 solid;
	border-radius: 5px;
	background: white;
	box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}

@page {
	size: landscape;
	margin: 0;
}

html {
	height: 0;
}

@media print {
	.page {
		margin: 0;
		border: initial;
		border-radius: initial;
		width: initial;
		min-height: initial;
		box-shadow: initial;
		background: initial;
		page-break-after: always;
	}
	@page {
		size: landscape;
		margin: 0.25cm;
	}
	.htmlbuttons {
		visibility: hidden;
		display: none;
		height: 0;
	}
}
</style>
<style type="text/css">
div.page > img {
	width: 100%;
	height: 100%;
	max-height: 100%;
	max-width: 100%;
}

.button {
	font: bold 11px Arial;
	text-decoration: none;
	background-color: #EEEEEE;
	color: #333333;
	padding: 2px 6px 2px 6px;
	border-top: 1px solid #CCCCCC;
	border-right: 1px solid #333333;
	border-bottom: 1px solid #333333;
	border-left: 1px solid #CCCCCC;
}
</style>
<script type="text/javascript">
	function pdfFileDownload() {
		var fileKey = "${pdfDownloadFileKey}";

		var url = "${pdfDownloadFileUrl}?fileKey=" + fileKey;

		return window.open( url, '_blank' );
	}
</script>
</head>
<body>
	<div class="htmlbuttons">
		<button onclick="JavaScript: pdfFileDownload();">${labels.btnPdfDownload}</button>
		<button onclick="JavaScript: window.print();">${labels.btnPrint}</button>
	</div>
	<div class="book">
		<#list pdfBase64Images as base64Image>
		<div class="page">
			<img src="${base64Image}" alt="" />
		</div>
		</#list>
	</div>
</body>
</html>
