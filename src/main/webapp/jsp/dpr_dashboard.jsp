<%--
	File Name:	dpr_dashboard.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2023/07/27		2.2.1	Credit Info ±â´É °³¹ß
	hankalam	2021/11/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	String PARAM = htmlpage.getLocale().getLanguage();
	PARAM = ( PARAM == null || PARAM.length() == 0 ? "&menu=portal" : "&menu=portal&locale="+ PARAM );

	String organizationCode = sessionMng.getExtraValue();
	if( organizationCode == null ) organizationCode = "";
	String partyId = sessionMng.getPartyId();
	if( partyId == null ) partyId = "";
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<style type="text/css">
		div.notice-item .category {
			width: auto;
			padding: 0 20px;
			vertical-align: center;
			text-align: right;
		}

		.category .category-shape.system {
			background-color: #D51900;
			color: #FFFFFF;
		}

		.category .category-shape.headword-1 {
			background-color: #D3E3FF;
			color: #007AC6;
		}

		.category .category-shape.headword-2 {
			background-color: #e2d3ff;
			color: #5e00c6;
		}

		.category .category-shape.headword-3 {
			background-color: #d3ffe1;
			color: #1b9505;
		}

		.category .category-shape.headword-4 {
			background-color: #ffebd3;
			color: #958b1a;
		}

		.category .category-shape.headword-5 {
			background-color: #ffd3fc;
			color: #c60089;
		}

		.category .category-shape.headword-6 {
			background-color: #d0d58c;
			color: #646821;
		}

		.category .category-shape.headword-7 {
			background-color: #cdcdcd;
			color: #565656;
		}

		.category .category-shape.headword-8 {
			background-color: #ffabab;
			color: #870977;
		}

		.category .category-shape.headword-9 {
			background-color: #90d3be;
			color: #317245;
		}

		.category .category-shape.headword-0 {
			background-color: #ffcf46;
			color: #747215;
		}

		.wrap-credit-status.list_content_data_scroll {
			border-bottom: 0px solid #C6C6C6
		}
		.wrap-credit-status tr.list_content {
			border: 1px solid #D8D8D8;
		}
		.wrap-credit-status .field-title {
			margin-bottom: 1px;
		}
		.wrap-credit-status .field {
			line-height: 24px;
		}
	</style>

	<script type='text/javascript'>
		$( function() {
			$( ".tooltip" ).tooltip( {
				position: { my: "left-15 bottom-15", at: "right center" }
			}).css( "cursor", "pointer" );

			var orderHeight = $("#content_orderList").innerHeight();
			var creditStatusHeight = 0;
			var sitelinkHeight = 0;
			var margin = 0;
			if( $("#content_creditStatus").length ) {
				creditStatusHeight = $("#content_creditStatus").innerHeight();
				margin = 15;
			}
			if( $("#content_weblink").length ) {
				sitelinkHeight = $("#content_weblink").innerHeight();
				margin = 15;
			}

			var boardHeight = $("#content_board").innerHeight();
			var orderHeight = $("#content_orderList").innerHeight();
			if( creditStatusHeight > 0 && sitelinkHeight > 0 ) {
				if( creditStatusHeight > sitelinkHeight ) {
					sitelinkHeight = creditStatusHeight;
				} else {
					creditStatusHeight = sitelinkHeight;
				}
				if( boardHeight > orderHeight ) {
					orderHeight = boardHeight;
				} else {
					boardHeight = orderHeight;
				}
				$("#content_board").innerHeight( boardHeight );
				$("#content_orderList").innerHeight( orderHeight );
				$("#content_weblink").innerHeight( sitelinkHeight );
				$("#content_creditStatus").innerHeight( creditStatusHeight );
			} else {
				var leftHeight;
				if( creditStatusHeight > 0 ) {
					leftHeight = orderHeight + creditStatusHeight + margin;
				} else if( sitelinkHeight > 0 ) {
					leftHeight = orderHeight + sitelinkHeight + margin;
				} else {
					leftHeight = orderHeight;
				}

				if( leftHeight > boardHeight ) {
					$("#content_board").innerHeight( leftHeight );
				} else {
					$("#content_orderList").innerHeight( orderHeight + (boardHeight - (leftHeight)) );
				}
			}


			if( leftHeight > boardHeight ) {
				$("#content_board").innerHeight( leftHeight );
			} else {
				$("#content_orderList").innerHeight( orderHeight + (boardHeight - (leftHeight)) );
			}

			if( "<%= property.getProperty("errorMessage") %>" != "null" ) {
				var messages = { "header" : "<%= property.getProperty("errorMessage") %>" };
				customPopup.alert( messages );
			}

			$(".category" ).each( function(idx, obj) {
				var width;
				if( $(obj).find( "span" ).length ) {
					width = $(obj).find( "span" ).outerWidth();
				} else {
					width = 40;
				}
				$(obj).width( width );
			})
		});

		function confirmSiteLinkReq( linkURL ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRSiteLink?mode=wcf";
			url = replaceQueryValue( url, "requestURL", linkURL );
			url = attachDefaultParameter( url, false );

			windowOpen( url, "sub-content" );
		}

		function siteLinkListReq() {
			windowSelfOpen( "<%= systemConfig.getClassURL() %>/DPRSiteLink?mode=list" );
		}

		function countrySystemNoticeReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRDashboard?mode=ninfo";
			windowOpen( url, "sub-content" );
		}

		function systemNoticeReq( boardClassCode, boardNumber) {
			var url = "<%= systemConfig.getClassURL() %>/DPRBoardNotice?mode=info";
			url = replaceQueryValue( url, "boardClassCode", boardClassCode );
			url = replaceQueryValue( url, "boardNumber", boardNumber );
			url = replaceQueryValue( url, "type", "dashboard" );
			windowOpen( url, "sub-content" );
		}

		function noticeReq( boardClassCode, boardNumber) {
			var url = "<%= systemConfig.getClassURL() %>/DPRBoard?mode=frm<%= PARAM %>";
			url = replaceQueryValue( url, "boardClassCode", boardClassCode );
			url = replaceQueryValue( url, "boardNumber", boardNumber );
			windowSelfOpen( url );
		}

		function noticeListReq( boardClassCode ) {
			if( typeof boardClassCode == "undefined" ) {
				boardClassCode = "NO";
				var partyId = "<%= partyId %>";
				var organizationCode = "<%= organizationCode %>";
				if( organizationCode != "null" && organizationCode != "" ) boardClassCode += ( "."+ organizationCode );
				else {
					if( partyId != null && partyId != "" )
						boardClassCode += ( "."+ partyId );
					else {
						customPopup.alert( { "header" : "<%= msghandler.getMessage("ERR_NEEDED_SELECT_ORGANIZATION") %>" } );
						return false;
					}
				}
			}

			windowSelfOpen( "<%= systemConfig.getClassURL() %>/DPRBoard?mode=frm&boardClassCode=" + boardClassCode );
		}

		function orderStatusReq( orderKey, status ) {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=info";
			url = replaceQueryValue( url, "orderKey", orderKey );
			url = replaceQueryValue( url, "status", status );

			windowSelfOpen( url, getLocationURL() );
		}

		function orderStatusListReq() {
			parent.menuMouseOver( "order" );
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=list&status=CD&recentCount=10";
			windowSelfOpen( url );
		}

	<% if( com.irt.dpr.Country.isFeature(organizationCode, "useLogisticsQuery") ) { %>
		function logisticsQueryReq( deliveryNumber ) {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=loqi";
			url = replaceQueryValue( url, "deliveryNumber", deliveryNumber );
			windowOpen( url +"&wintype=sub", "clsMng" );
		}
	<% } %>
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<div class='table table-fixed' style='min-width: 920px;'>
		<div class='row'>
			<div class='cell' style='padding-right: 10px;'>

				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useCreditStatus") ) { %>
				<mtl:contains id="creditStatusList">
					<div class='content' style='min-height:50px;'>
						<mtl:contentGroup groupId="creditStatus" type="list" style="width: 100%; min-width: 400px; margin-top: 5px; margin-bottom: 20px;">
							<h2><mtl:message key="jsp.dpr_dashboard.MSG_TITLE_CREDIT_STATUS"/></h2>

							<div class='wrap-credit-status list_content_data_scroll' style='height:53px; overflow-y:auto;'>
								<mtl:loop id="creditStatusList" loopId="loop" loopIndex="index">

									<% String creditRiskRedStyle = ""; %>
									<mtl:ifvalue id="loop" key="creditRiskInd" value="Y">
										<% creditRiskRedStyle = "color:red;font-weight:bold;"; %>
									</mtl:ifvalue>

									<table class='list_content_data' cellspacing="0" cellpadding="0" rules="rows">
										<tbody>
											<tr class='list_content'>
												<td class='w45 align-left'>
													<div class='line-cell ' style='width:40px;height:40px;'>
														<img id='creditStatusImg' src="images/credit_status.png" style="padding-right:0px; max-width:100%;max-height:100%;">
													</div>
												</td>
												<td>
													<div class='field-title' style='white-space:normal;'>
														<span id="title_creditPartyCode">
															<mtl:valuef id="loop" format="${creditPartyCode} ${creditPartyName}"/>
														</span>
													</div>
												</td>
												<td class='align-center'>
													<div class='field-title'>
														<span id="title_creditLimit">
															<mtl:message key="FIELD_DPR_PARTY_CREDIT_CREDIT_LIMIT"/>
														</span>
													</div>
													<div class='field'>
														<span><mtl:valuef id="loop" format="$f{decode(creditCurrency,KRW,${creditLimitCrcy#NF.INTEGER},${creditLimit#NF.FLOAT2})}"/>
															<sub style='font-size:11px;'><mtl:valuef id="loop" format="$S{creditCurrency}"/></sub>
														</span>
													</div>
												</td>
												<td class='align-center'>
													<div class='field-title'>
														<span id="title_creditExposure">
															<mtl:message key="FIELD_DPR_PARTY_CREDIT_CREDIT_EXPOSURE"/>
														</span>
													</div>
													<div class='field' style='<%= creditRiskRedStyle %>'>
														<span><mtl:valuef id="loop" format="$f{decode(creditCurrency,KRW,${creditExposureCrcy#NF.INTEGER},${creditExposure#NF.FLOAT2})}"/>
															<sub style='font-size:11px;'><mtl:valuef id="loop" format="$S{creditCurrency}"/></sub>
														</span>
													</div>
												</td>
												<td class='align-center'>
													<div class='field-title'>
														<span id="title_accountReceivable"> <mtl:message key="FIELD_DPR_PARTY_CREDIT_ACCOUNT_RECEIVABLE"/> </span>
													</div>
													<div class='field'>
														<span><mtl:valuef id="loop" format="$f{decode(creditCurrency,KRW,${accountReceivableCrcy#NF.INTEGER},${accountReceivable#NF.FLOAT2})}"/>
															<sub style='font-size:11px;'><mtl:valuef id="loop" format="$S{creditCurrency}"/></sub>
														</span>
													</div>
												</td>
											</tr>
										</tbody>
									</table>

								</mtl:loop>
							</div><!--scroll-->

						</mtl:contentGroup>
					</div><!--content-->
				</mtl:contains>
				<% } %>

				<mtl:contentGroup groupId="orderList" type="list" style="width: 100%; min-width: 400px">
					<div class='content-title'>
						<div class='title'>
							<h2><mtl:message key="TITLE_DPR_ORDERSTATUS_"/></h2>
							<div class='page-function'>
								<mtl:button type="button" styleClass="btn-text" icon="images/ico_arrow_right_on.png"
										iconPosition="right" onClick="JavaScript: orderStatusListReq();" messageKey="jsp.BTN_VIEWMORE"/>
							</div>
						</div>
					</div>

					<%
						com.irt.data.cols.ColumnList orderColumnList = (com.irt.data.cols.ColumnList)request.getAttribute( "orderColumnList" );
						ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage, orderColumnList, "orderList" ) {
							public void printHeaderCell( JspWriter out, com.irt.data.cols.Column column, int rowspan ) throws java.io.IOException {
								String fieldKey = column.getFieldKey();

								if( "deliveryNumber2".equals(fieldKey) ) {
									StringBuffer attributeBuffer = new StringBuffer();
									String columnAttr = (String)column.getColumnAttr();
									if( columnAttr == null ) columnAttr = "";

									if( rowspan > 1 )
										attributeBuffer.append( " rowspan='"+ rowspan +"'" );

									String helpMessage = msghandler.getMessage( "jsp.dpr_order_list.MSG_LOGISTICS_TRACKING" );
									out.print( "<th"+ attributeBuffer.toString() +" "+ columnAttr + ">" );
									out.print( "<div class='field-title'>" + column.getColumnTitle(null, msghandler) + "</div>" );
									out.print( "<div class='icon tooltip'> " );
									out.print( "<img src='images/ico_info_small.png' title='" + helpMessage + "'></div>" );
									out.print( "</div>" );
									out.print( "</th>" );
									return;
								}
								super.printHeaderCell(out, column, rowspan);
							}
						};
						listWriter.setCheckboxTypeAndNumbering( com.irt.custom.ListWriter.CHECKBOXTYPE_NUMBER );
						listWriter.setNumbering( false );
						listWriter.print( out );
					%>
				</mtl:contentGroup>
			<% if( !com.irt.dpr.Country.isFeature(organizationCode, "useCreditStatus") ) { %>
				<mtl:contains id="sites">
					<mtl:contentGroup groupId="weblink" type="list" style="width: 100%; min-width: 400px; margin-top: 5px; margin-bottom: 20px;">
						<h2><mtl:message key="jsp.dpr_main_page.MSG_TITLE_SITELINK"/></h2>
						<div class='table table-fixed'>
							<mtl:loop id="sites" loopId="loop" loopIndex="index">
							<div class='row'>
								<div class='cell w40p'>
									<a href='JavaScript: confirmSiteLinkReq("<%= loop.get("linkURL") %>");'><mtl:value id="loop" key="description"/></a>
								</div>
								<div class='cell w40p'>
									<a href='JavaScript: confirmSiteLinkReq("<%= loop.get("linkURL") %>");'><mtl:value id="loop" key="linkURL"/></a>
								</div>
								<div class='cell align-right'>
									<mtl:valuef id="loop" format="${updateDateTime~0~10}"/>
								</div>
							</div>
							</mtl:loop>
						</div>
					</mtl:contentGroup>
				</mtl:contains>
			<% } %>
			</div>
			<div class='cell' style='padding-left: 10px;'>
				<mtl:contentGroup groupId="board" type="list" style="width: 100%; min-width: 400px;">
					<div class='content-title'>
						<div class='title'>
							<h2><mtl:message key="TITLE_DPR_ANNOUNCEMENTS"/></h2>
							<div class='page-function'>
								<mtl:button type="button" styleClass="btn-text" icon="images/ico_arrow_right_on.png"
										iconPosition="right" onClick="JavaScript: noticeListReq();" messageKey="jsp.BTN_VIEWMORE"/>
							</div>
						</div>
					</div>
				<%
					List<Map<String, Object>> notices = (List<Map<String, Object>>)pageContext.findAttribute( "notices" );
					String language = htmlpage.getLocale().getLanguage();
					if( notices != null ) {
						for( Map<String, Object> item : notices ) {

							String extraClass = "";
							String extraClass2 = "";
							String typeMessage = null;
							String boardType = (String)item.get( "boardType" );
							boolean isSystemNotice = false;

							if( "S".equals(item.get("boardType2")) || "CS".equals(item.get("boardType2")) ) {
								extraClass += " emergency unread";
								extraClass2 += " system";
								typeMessage = msghandler.getMessage( "jsp.dpr_dashboard.MSG_CATEGORY_SYSTEM" );
								if( "S".equals(item.get("boardType2")) ) {
									isSystemNotice = true;
								}
							} else if( "N".equals(boardType) ) {
								extraClass += " important unread";
								String headwordCode = (String)item.get( "headwordCode" );
								if( headwordCode != null ) {
									String headwordSeq = (String)item.get( "headwordSeq" );
									extraClass2 += " headword-" + headwordSeq;
									String headwordName = (String)item.get( "headwordName" );
									typeMessage = headwordName;
								}
							} else {
								extraClass += " notice unread";
								String headwordCode = (String)item.get( "headwordCode" );
								if( headwordCode != null ) {
									String headwordSeq = (String)item.get( "headwordSeq" );
									extraClass2 += " headword-" + headwordSeq;
									String headwordName = (String)item.get( "headwordName" );
									typeMessage = headwordName;
								}
							}
							out.print( "<div class='notice-item table'" );
							out.print( " onclick='JavaScript: " );
							if( "CS".equals(item.get("boardType2")) ) {
								out.print( " countrySystemNoticeReq" );
							} else if( isSystemNotice ) {
								out.print( " systemNoticeReq" );
							} else {
								out.print( " noticeReq" );
							}
							out.println( "(\"" + item.get("boardClassCode") + "\", \"" + item.get("boardNumber") + "\")'>" );

							out.println( "	<div class='cell icon" + extraClass + "'><div style='width: 46px'></div></div>" );
							out.println( "	<div class='cell content'>" );
							out.print( "		<div class='title'>" );
							if( isSystemNotice ) {
								String title = (String)item.get( "title_" + language );
								if( title == null ) {
									title = (String)item.get( "title_en" );
								}
								out.print( title );
							} else {
								out.print( item.get("title") );
							}
							out.println( "</div>" );

							String truncateContent;
							if( isSystemNotice ) {
								truncateContent = (String)item.get( "content_" + language );
								if( truncateContent == null ) {
									truncateContent = (String)item.get( "content_en" );
								}
							} else {
								truncateContent = (String)item.get( "content" );
							}

							if( truncateContent != null ) {
								truncateContent = com.irt.util.StringUtil.evalPlaceholder( truncateContent, item, "{{", "}}" );
								truncateContent = truncateContent.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "").replaceAll( "&nbsp;|\\n", " " );
								int endIdx = truncateContent.length() < 80 ? truncateContent.length() : 80;
								truncateContent = truncateContent.substring( 0, endIdx );
							}
							if( truncateContent != null && truncateContent.length() > 0 ) {
								out.println( "		<div class='main-text'>" + truncateContent + "</div>" );
							}
							out.println( "	</div>" );
							out.println( "	<div class='cell category'>" );
							if( typeMessage != null ) {
								out.print( "		<span class='category-shape" + extraClass2 + "'>" );
								out.print( typeMessage );
								out.println( "</span>" );
							}
							out.println( "	</div>" );
							out.println( "	<div class='cell date'>" );
							out.println( com.irt.data.format.PatternRecordFormat.getInstance("${updateDateTime~0~10}").format(item, msghandler) );
							out.println( "	</div>" );
							out.println( "</div>" );
						}
					}
				%>
				</mtl:contentGroup>
			<% if( com.irt.dpr.Country.isFeature(organizationCode, "useCreditStatus") ) { %>
				<mtl:contains id="sites">
					<mtl:contentGroup groupId="weblink" type="list" style="width: 100%; min-width: 400px; margin-top: 5px; margin-bottom: 20px;">
						<h2><mtl:message key="jsp.dpr_main_page.MSG_TITLE_SITELINK"/></h2>
						<div class='table table-fixed'>
							<mtl:loop id="sites" loopId="loop" loopIndex="index">
							<div class='row'>
								<div class='cell w40p'>
									<a href='JavaScript: confirmSiteLinkReq("<%= loop.get("linkURL") %>");'><mtl:value id="loop" key="description"/></a>
								</div>
								<div class='cell w40p'>
									<a href='JavaScript: confirmSiteLinkReq("<%= loop.get("linkURL") %>");'><mtl:value id="loop" key="linkURL"/></a>
								</div>
								<div class='cell align-right'>
									<mtl:valuef id="loop" format="${updateDateTime~0~10}"/>
								</div>
							</div>
							</mtl:loop>
						</div>
					</mtl:contentGroup>
				</mtl:contains>
			<% } %>
			</div>
		</div>
	</div>


	<%@ include file="include_dpr_tail.inc" %>
</body>
</mtl:html>
