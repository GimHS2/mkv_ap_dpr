<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>${labels.documentTitleValue?html}</title>
<meta http-equiv="content-type" content="text/html;charset=utf-8"></meta>
<!-- <meta name="viewport" content="width=device-width, initial-scale=1"> -->
<style text="text/css">
@font-face {
	font-family: 'wqy-zenhei', 'WenQuanYi Zen Hei', Helvetica, Arial, sans-serif;
	-fs-pdf-font-embed: embed;
	-fs-pdf-font-encoding: Identity-H;
}

@font-face {
	font-family: 'uming', 'AR PL UMing CN', serif
	-fs-pdf-font-embed: embed;
	-fs-pdf-font-encoding: Identity-H;
}

@font-face {
	font-family: 'ukai', 'AR PL UKai CN', serif
	-fs-pdf-font-embed: embed;
	-fs-pdf-font-encoding: Identity-H;
}

/* fallback for all font-family*/
@font-face {
	font-family: 'wqy-zenhei', 'WenQuanYi Zen Hei',
		Helvetica, Arial, sans-serif, 'Comic Sans MS', 'Courier New', 'Georgia',
		'Lucida Sans Unicode', 'Tahoma', 'Times New Roman', 'Trebuchet MS', 'Verdana';
	-fs-pdf-font-embed: embed;
	-fs-pdf-font-encoding: Identity-H;
}

* {
	font-family: 'wqy-zenhei', 'WenQuanYi Zen Hei', 'Arial', sans-serif;
	-fs-pdf-font-embed: embed;
	-fs-pdf-font-encoding: Identity-H;
}
</style>
<style text="text/css">
.clear {
	clear: both;
}

.underline {
	border-bottom: 1px solid black;
}
</style>
<style text="text/css">
#details-content-main th, td {
	padding: 1px;
}
#details-content-main td.string {
	text-align: left;
	padding-left: 5px;
	padding-right: 5px;
}

#details-content-main td.code {
	text-align: center;
	padding-left: 5px;
	padding-right: 5px;
}

#details-content-main td.number {
	text-align: center;
}

#details-content-main col.description {
	text-align: left;
	padding-left: 5px;
	padding-right: 5px;
}

#details-content-main col.string.col_10char {
	min-width: calc(0.6em * 5);
	max-width: calc(0.6em * 10);
}

#details-content-main col.string.col_9char {
	min-width: calc(0.6em * 4.5);
	max-width: calc(0.6em * 9);
}

#details-content-main col.string.col_8char {
	min-width: calc(0.6em * 4);
	max-width: calc(0.6em * 8);
}

#details-content-main col.string.col_7char {
	min-width: calc(0.6em * 3.5);
	max-width: calc(0.6em * 7);
}

#details-content-main col.string.col_6char {
	min-width: calc(0.6em * 3);
	max-width: calc(0.6em * 6);
}

#details-content-main col.string.col_5char {
	min-width: calc(0.6em * 2.5);
	max-width: calc(0.6em * 5);
}

#details-content-main col.string.col_4char {
	min-width: calc(0.6em * 2);
	max-width: calc(0.6em * 4);
}

#details-content-main col.string.col_3char {
	min-width: calc(0.6em * 1.5);
	max-width: calc(0.6em * 3);
}

#details-content-main col.string.col_2char {
	min-width: calc(0.6em * 1);
	max-width: calc(0.6em * 2);
}

#details-content-main col.string.col_1char {
	min-width: calc(0.6em * 0.5);
}

#details-content-main col.number.col_10char {
	min-width: calc(0.6em * 5);
}

#details-content-main col.number.col_9char {
	min-width: calc(0.6em * 4.5);
}

#details-content-main col.number.col_8char {
	min-width: calc(0.6em * 4);
}

#details-content-main col.number.col_7char {
	min-width: calc(0.6em * 3.5);
}

#details-content-main col.number.col_6char {
	min-width: calc(0.6em * 3);
}

#details-content-main col.number.col_5char {
	min-width: calc(0.6em * 2.5);
}

#details-content-main col.number.col_4char {
	min-width: calc(0.6em * 2);
}

#details-content-main col.number.col_3char {
	min-width: calc(0.6em * 1.5);
}

#details-content-main col.number.col_2char {
	min-width: calc(0.6em * 1);
}

#details-content-main col.number.col_1char {
	min-width: calc(0.6em * 0.5);
}

span.label {

}

span.content.text-right {
	/* text-align on span. parent of the span needs to be defined as display:span; */
	flex: 1;
	text-align: right;
	white-space: nowrap;
}

span.content.text-left {
	text-align: left;
}


#details-content-main table th {
	text-align: center;
}

#details-content-main table {
	table-layout: fixed;
	width: 100%;
	border-top: 1px solid black;
	border-bottom: 1px solid black;
	border-left: 1px solid black;
	border-right: 1px solid black;
	border-collapse: separate;
	border-spacing: 1px;
}



/* pagenate related start */
#details-content-main table {
	-fs-table-paginate: paginate;
	border-spacing: 0;
}

#details-content-main table tr {
	page-break-inside: avoid;
}

/* pagenate related end */



#details-content-main tbody tr.rowspan-none td {
	border-top: 1px solid black;
}
#details-content-main tbody tr.rowspan-start td {
	border-top: 1px solid black;
}
#details-content-main tbody tr.rowspan-end td {
	border-bottom: 1px solid black;
}
#details-content-main tbody tr.rowspan-none td:first-child {
	border-top: 1px solid black;
}
#details-content-main tbody tr.rowspan-start td:first-child {
	vertical-align: top;
	text-align: left;
	border-top: 1px solid black;
	border-bottom: 0px solid black;
}
#details-content-main tbody tr.rowspan-end td:first-child {
	border-top: 0px solid black;
	border-bottom: 1px solid black;
}
#details-content-main tbody tr.rowspan-ing td:first-child {
	border-top: 0px solid black;
	border-bottom: 0px solid black;
}
#details-content-main tbody tr td {
	border-top: 0.2px solid black;
	border-left: 0.2px solid black;
	border-right: 0.2px solid black;
	border-bottom: 0.2px solid black;
}
#details-content-main tfoot tr td {
	border-top: 1px solid black;
	border-left: 0.2px solid black;
	border-right: 0.2px solid black;
	border-bottom: 1px solid black;
}
#details-content-main thead tr th {
	border-top: 1px solid black;
	border-left: 0.2px solid black;
	border-right: 0.2px solid black;
	border-bottom: 1px solid black;
}

/*
#details-content-main tbody tr:nth-child(odd) {
	background: #f5f5f5;
}

#details-content-main tbody tr:nth-child(even) {
	background: white;
}
*/

#details-content-main td {
	border-top: 1px solid #FFFFFF;
	border-bottom: 1px solid #C1C1C1;
	border-left: 0px solid;
	border-right: 1px solid #C1C1C1;
	word-break: break-all;
}

</style>
<style type="text/css">
.column-row {
	display: flex;
}

.column-row-underline {
	flex-grow: 1;
	border-bottom: 1px solid black;
	margin-left: 5px;
	margin-bottom: 3px;
}

.column-group:after {
	content: "";
	display: table;
}

.column-group_2 .column-item {
	float: left;
	width: 46%;
	margin-left: 3px;
	margin-right: 3px;
	padding: 3px;
}

.column-group_3 .column-item {
	float: left;
	width: 31.8%;
	margin-left: 0.5%;
	margin-right: 0.5%;
	padding: 2px;
}

.column-group_4 .column-item {
	float: left;
	width: 24%;
	margin-left: 3px;
	margin-right: 3px;
	padding: 3px;
}

.column-group_5 .column-item {
	float: left;
	width: 19%;
	margin-left: 1px;
	margin-right: 1px;
	padding: 2px;
}

.column-group_6 .column-item {
	float: left;
	width: 16%;
	margin-left: 0.5%;
	margin-right: 0.5%;
	padding: 2px;
}

.column-group_7 .column-item {
	float: left;
	width: 12%;
	margin-left: 0.5%;
	margin-right: 0.5%;
	padding: 2px;
}

.column-group_8 .column-item {
	float: left;
	width: 12%;
	margin-left: 3px;
	margin-right: 3px;
	padding: 3px;
}
</style>
<style type="text/css">
</style>
<style type="text/css">
* {
	box-sizing: border-box;
	-moz-box-sizing: border-box;
}

div.page-content {
	display: block;
	text-align: justify;
}

body {
	margin: 0.25cm;
	margin-left:0.5cm;
	margin-right:0.5cm;
}

@page {
	size: landscape;
	margin: 0.25cm;
	margin-left: 0.5cm;
	margin-right: 0.5cm;
	/**/
	/* make page count from 1 instead of 0.(0 is default in css) */
	counter-reset: page 1;
	counter-increment: page;
}


@page { @top-center { content:element(header);

}

}
@page { @bottom-right { content:element(footer);

}

}
#page-header {
	text-align: left;
	margin-bottom: -0.25cm;
	position: running(header);
}

#page-footer {
	text-align: right;
	margin-top: -0.25cm;
	padding-bottom: 0.10cm;
	position: running(footer);
}

#pagenumber:before {
	content: counter(page);
}

#pagecount:before {
	content: counter(pages);
}


</style>
<style type="text/css">
#header-content * {
	text-align: center;
}

#page-content[data-lang=zh] #details-content-main th {
	word-break: keep-all;
	white-space: nowrap;
}


.page-content {
	page-break-after: avoid;
}

html {
	height: 0;
}
</style>
<#if pagenation.tryTailContent == "Y ">
<style type="text/css">
#header {
	display: none;
}

#header-content {
	display: none;
}

#details * {
	display: none;
}
</style>
</#if>
<style type="text/css">
#details-content-top table {
	width: 100%;
	table-layout: fixed;
	border: none;
}

#details-content-top table td {
	border: none;
}

/* left and right column sizing start */
#details-content-top-left table td:first-child {
	width: auto !important;
}
#details-content-top-left table td {
	width: 70%;
}

/* left and right column sizing end */
#tail-content-top {
	page-break-inside: avoid;
}

#tail-content-bottom {
	page-break-inside: avoid;
}
#tail-content-bottom .board-content p {
	margin: 0;
	padding: 0;
}
</style>
<style type="text/css">
.bold {
	font-weight: bold;
}
#details-content-main .bold {
	font-weight: bold;
}
#details-content-main thead th {
	font-weight: bold;
}
#details-content-main tfoot td {
	font-weight: bold;
}

#page-header {
	font-size: 10px;
}
#page-footer {
	font-size: 10px;
}

#header-content .owner-title-localname {
	font-weight: bold;
	letter-spacing: 4px;
	font-size: 12px;
}
#header-content .owner-title-globalname {
	font-size: 10px;
	font-style: italic;
	font-weight: bold;
}
#header-content .document-title {
	font-size: 16px;
}

#details-content {
	font-size: 10px;
}

#details-content-main thead th {
	font-weight: bold;
	font-size: 12px;
}

#details-content-main tbody td {
	font-weight: normal;
	font-size: 10px;
}

#details-content-main tfoot td {
	font-weight: bold;
	font-size: 12px;
}

#tail-content {
	font-size: 10px;
}

#page-content[data-lang=en] #tail-content {
	font-size: 9px;
}



/* left and right column sizing start */
#tail-content-top td:first-child {
	width: auto !important;
}

#tail-content-top td.width80 {
	width: 80%;
}

#tail-content-top td.width70 {
	width: 70%;
}

#tail-content-top td.width60 {
	width: 60%;
}

#tail-content-top td.width50 {
	width: 50%;
}

#tail-content-top td.width40 {
	width: 40%;
}


#tail-content-top td {
	width: 60%;
}

#header-content table td {
	padding: 0px;
}
#details-content-top table, tr, td {
	border-spacing: 0px;
}
#details-content-main table, tr, td {
	border-spacing: 0px;
}
#details-content-main table tr {
	border-spacing: 1px;
}

#tail-content table, tr, td {
	border-spacing: 0px;
}

table, tr, td {
	boarder-spacing: 0.2px;
	padding: 0px;
}



</style>
</head>
<body>
	<div id="page-header">
		<div class="page-header-content">
			<table style="table-layout: fixed; width: 100%;">
				<tr>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td style="text-align: right;">
						<sup><span>${header.simulationKey}</span></sup>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div id="page-footer">
		<div class="page-footer-content">
			<table style="table-layout: fixed; width: 100%;">
				<tr>
					<td style="text-align: left;">
						<sup>printed: ${(.now?long)?number_to_date?string("yyyy-MM-dd")}</sup>
					</td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td style="text-align: right;">
						<sup><span id="pagenumber"></span> / <span id="pagecount"></span></sup>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div id="page-content" data-lang="${header.displayLanguage}">
		<div id="header">
			<div id="header-content" style="letter-spacing: 2px;">
			<table style="table-layout: fixed; width: 100%;">
				<tr>
				  <td class="owner-title-localname">
					<span>${header.organizationNameLocal?html}</span>
				  </td>
				</tr>
				<tr>
				  <td class="owner-title-globalname">
					<span>${header.organizationNameDefault?html}</span>
				  </td>
				</tr>
				<tr>
				  <td class="document-title">
					<span>${labels.documentTitleValue?html}</span>
				  </td>
				</tr>
			</table>
			</div>
		</div>
		<div id="details">
			<div id="details-content">
			<div id="details-content-top" class="column-group column-group_3">
				<div id="details-content-top-left" class="column-item" style="width: 27%;">
					<div class="column-row" style="padding-left:8px;">
						<table style="table-layout: fixed; width: 100%;">
							<tr>
								<td class="subject bold" >
									<span>${labels.partyNameFull}:</span>
								</td>
								<td class="content width80 underline" style="text-align: left; text-decoration: underline;">
									<span style="text-align: left;">${header.partyNameFull?html}</span>
								</td>
							</tr>
							<tr>
								<td></td>
								<td></td>
							</tr>
						</table>
					</div>
				</div>
				<div id="details-content-top-center" class="column-item" style="width: 47%;">
					<div class="column-row">
						<span>&nbsp;</span>
					</div>
				</div>
				<div id="details-content-top-right" class="column-item" style="width: 21%;">
					<table style="table-layout: fixed; width: 100%;">
						<tbody>
							<tr>
								<td class="subject bold" style="text-align: right;">
									<span>${labels.purchaseOrderNumber}:</span>
								</td>
								<td class="content width80 underline" style="text-align: right; text-decoration: underline;">
									<span>${header.purchaseOrderNumber}</span>
								</td>
							</tr>
							<tr>
								<td class="subject bold" style="text-align: right;">
									<span>${labels.partyNumber}:</span>
								</td>
								<td class="content width80 underline" style="text-align: right; text-decoration: underline;">
									<span>${header.partyNumber}</span>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="clear"></div>
			</div>
			<div id="details-content-main">
				<table>
					<colgroup>
						<col class="col_8char string"></col>
						<col class="col_4char string"></col>
						<col class="description"></col>
						<col class="col_4char string"></col>
						<col class="col_8char string"></col>
						<col class="col_2char string"></col>
						<col class="col_1char number"></col>
						<col class="col_4char number"></col>
						<col class="col_4char number"></col>
						<col class="col_5char number"></col>
						<col class="col_5char number"></col>
						<col class="col_6char number"></col>
					</colgroup>
					<tbody>
						<#assign spans = {}/>
						<#assign spanEnds = {}/>
						<#assign spanCount = 0 />
						<#assign spanStart = -1 />
						<#assign rowGroupPrev = ""/>
						<#list details as detail>
							<#assign spans = spans + {"span"+detail?index : 1} />
							<#assign spanEnds = spanEnds + {"span"+detail?index : 0} />
							<#if (details?size-1) == detail?index >
								<#assign spanEnds = spanEnds + {"span"+detail?index : 1} />
							</#if>

							<#if rowGroupPrev == "" >
								<#assign rowGroupPrev = detail.itemExtraCate />
							</#if>

							<#if detail.itemExtraCate == rowGroupPrev>
								<#if spanStart == -1 >
									<#assign spanStart = (detail?index) />
									<#assign spanEnds = spanEnds + {"span"+detail?index : 1} />
								</#if>

								<#assign spans = spans + {"span"+detail?index : -1} />
								<#assign spanCount = spanCount + 1 />
							<#else>
								<#assign spans = spans + {"span"+(spanStart) : spanCount} />

								<#assign spanCount = 0/>
								<#assign spanStart = -1/>
								<#assign rowGroupPrev = ""/>
							</#if>
						</#list>

						<#assign charLen0 = 0>
						<#assign charLen1 = 0>
						<#assign charLen2 = 0>
						<#list details as detail>

						<#if spans["span"+detail?index] gt 0>
							<#assign rowSpan = spans["span"+detail?index] />
						<#else>
							<#assign rowSpan = -1/>
						</#if>

						<#assign cssSpanEnd = "" />
						<#if spanEnds["span"+detail?index] == 1>
							<#assign cssSpanEnd = "rowspan-end " />
						</#if>

						<#assign cssSpanStart = ""/>
						<#if rowSpan gt 0>
							<#assign cssSpanStart = "rowspan-start "/>
						</#if>
						<#assign cssSpanIng = "" />
						<#if cssSpanStart == "" && cssSpanEnd == "" >
							<#assign cssSpanIng = "rowspan-ing "/>
						</#if>
						<#assign cssSpanNone = ""/>
						<#if cssSpanStart != "" && cssSpanEnd != "" >
							<#assign cssSpanNone = "rowspan-none"/>
							<#assign cssSpanStart = ""/>
							<#assign cssSpanEnd = ""/>
						</#if>

						<!--<tr class="${cssSpanStart}${cssSpanEnd}${cssSpanIng}${cssSpanNone}">-->
						<tr>
							<td class="string bold" data-rowspan="${rowSpan}" data-value="${detail.itemExtraCate?html}">${detail.itemExtraCate?html}</td>
							<#if detail.itemExtraCate?length gt charLen0>
								<#assign charLen0 = detail.itemExtraCate?length>
							</#if>

							<td class="code">${detail.itemNumber}</td>
							<#if detail.itemNumber?length gt charLen1>
								<#assign charLen1 = detail.itemNumber?length>
							</#if>
							<td class="string">${detail.itemExtraDesc?html}</td>
							<#if detail.itemExtraDesc?length gt charLen2>
								<#assign charLen2 = detail.itemExtraDesc?length>
							</#if>
							<td class="code">${detail.itemExtraAbbrev?html}</td>
							<td class="code">${detail.itemExtraSpec?html}</td>
							<td class="code">${detail.uomNameLocal?html}</td>
							<td class="number">
								<span style="display: none;">${detail.uom}</span><span style="display: none;">${detail.caseCount}</span> <span>${detail.lineOrderCaseQty}</span>
							</td>
							<td class="number">
								<span style="display: none;">${detail.price}</span> <span>${detail.lineOrderPieceQty}</span>
							</td>
							<td class="number">${detail.lineOrderPiecePrice}</td>
							<td class="number">${detail.lineOrderValue}</td>
							<td class="number">${detail.lineOrderTax}</td>
							<td class="number">${detail.lineOrderTotal}</td>
						</tr>
						</#list>

						<#if details?size lt pagenation.maxDetailsInPage>
							<#assign firstPageBlankSize = pagenation.maxDetailsInPage - details?size>
						</#if>
						<#list 1..firstPageBlankSize as x>
						<tr>
							<td class="string bold">&nbsp;</td>
							<td class="code"></td>
							<td class="string"></td>
							<td class="code"></td>
							<td class="code"></td>
							<td class="code"></td>
							<td class="number">
								<span style="display: none;"></span>
							</td>
							<td class="number">
								<span style="display: none;"></span>
							</td>
							<td class="number"></td>
							<td class="number"></td>
							<td class="number"></td>
							<td class="number"></td>
						</tr>
						</#list>
					</tbody>
					<thead>
						<tr>
							<th style="width: 8em;">${labels.itemExtraCate}</th>
							<th style="width: 5.9em;">${labels.itemNumber}</th>
							<th style="width: 13em;">${labels.itemExtraDesc}</th>
							<th style="width: 5.4em;">${labels.itemExtraAbbrev}</th>
							<th style="width: 5.6em;">${labels.itemExtraSpec}</th>
							<th style="width: 2.8em;">${labels.uomNameLocal}</th>
							<th style="width: 4.2em;">${labels.lineOrderCaseQty}</th>
							<th style="width: 6.4em;">${labels.lineOrderPieceQty}</th>
							<th style="width: 6.3em;">${labels.lineOrderPiecePrice}</th>
							<th style="width: 8.3em;">${labels.lineOrderValue}</th>
							<th style="width: 6em;">${labels.lineOrderTax}</th>
							<th style="width: 8.2em;">${labels.lineOrderTotal}</th>
						</tr>
					</thead>
					<tfoot>
						<tr>
							<td></td>
							<td></td>
							<td class="string bold">
								<span>${labels.summary}</span>
							</td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td class="number">${header.sumOrderValue}</td>
							<td class="number">${header.sumOrderTax}</td>
							<td class="number">${header.sumOrderTotal}</td>
						</tr>
					</tfoot>
				</table>
			</div>
			</div>
		</div>
		<div id="tail">
			<div style="height: 8px;"></div>
			<div id="tail-content" class="column-group column-group_5">
				<div id="tail-content-top">
					<div id="tail-content-top-left" class="column-item" style="width: 35%;">
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold" style="width: 15em;">
										<span>${labels.organizationRepresentative}:</span>
									</td>
									<td class="content underline width60"></td>
								</tr>
							</table>
						</div>
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.organizationNameLocal}:</span>
									</td>
									<td class="content bold width60">
										<span> ${header.organizationNameDefault?html}</span>
									</td>
								</tr>
							</table>
						</div>
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.organizationAddress}:</span>
									</td>
									<td class="content bold width60">
										<span> ${header.organizationAddressDefault?html}</span>
									</td>
								</tr>
							</table>
						</div>
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.organizationPhone}:</span>
									</td>
									<td class="content width60">
										<span> ${header.organizationPhone?html}</span>
									</td>
								</tr>
							</table>
						</div>
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.organizationFax}:</span>
									</td>
									<td class="content width60">
										<span> ${header.organizationFax?html}</span>
									</td>
								</tr>
							</table>
						</div>
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.organizationPostCode}:</span>
									</td>
									<td class="content width60">
										<span> ${header.organizationPostCode?html}</span>
									</td>
								</tr>
							</table>
						</div>
					</div>
					<div class="column-item" style="width: 6%">
						<div class="column-row">
							<span>&nbsp;</span>
						</div>
					</div>
					<div id="tail-content-top-center" class="column-item" style="width: 25%;">
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.partyRepresentative}:</span>
									</td>
									<td class="content underline width50"></td>
								</tr>
							</table>
						</div>
						<br />
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.partySignDate}:</span>

									</td>
									<td class="content underline width50" style="text-align: center;">
										<span class="bold">${header.orderDate?string('yyyy-MM-dd')}</span>
									</td>
								</tr>
							</table>
						</div>
						<br />
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject bold">
										<span>${labels.partySign}:</span>
									</td>
									<td class="content underline width50"></td>
								</tr>
							</table>
						</div>
					</div>
					<div class="column-item" style="width: 6%">
						<div class="column-row">
							<span>&nbsp;</span>
						</div>
					</div>
					<div id="tail-content-top-right" class="column-item" style="border: solid 2px; width: 24%;">
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject">
										<span>${labels.requestedDeliveryDate}:</span>
									</td>
									<td class="content underline width50"></td>
								</tr>
							</table>
						</div>
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td class="subject">
										<span>${labels.expectedDeliveryDate}:</span>
									</td>
									<td class="content underline width50"></td>
								</tr>
							</table>
							<span class="column-row-underline"></span>
						</div>
						<div class="column-row">
							<table style="table-layout: fixed; width: 100%;">
								<tr>
									<td>
										<span><br/></span>
									</td>
								</tr>
							</table>
						</div>
						<div class="column-row">
							<span>${labels.deliveryAddressNoticeValue}</span>
						</div>
					</div>
				</div>
				<div class="clear"></div>
				<div id="tail-content-bottom" style="padding: 2px; padding-top: 0px;">
					<div class="column-row">
						<table style="table-layout: fixed; width: 100%;">
							<tr>
								<td style="width: 3em; vertical-align:top">
									<div class="bold">${labels.orderComment}:</div>
								</td>
								<td style="width: auto; text-align: left; vertical-align:top;">
									<div class="board-content">${header.orderCommentHtmlString}</div>
								</td>
							</tr>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
