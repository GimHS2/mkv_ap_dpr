<%--
	File Name:	dpr_item_input.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2018/10/30		2.2.2	isChinaCountry() 삭제
	song7981	2016/04/25		2.2.1	shelflife 정보 추가(중국만 보이도록)
	lsinji		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='utf-8' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	String countryCode = property.getProperty( "countryCode" );
	String itemCode = property.getProperty( "itemCode" );

	String organizationCode = sessionMng.getExtraValue();

	String IMG_PARAM = "&countryCode=" + countryCode + "&itemCode=" + itemCode;
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_list.inc" %>
	<script type='text/javascript'>
		var windowWidth = 900;

		$.fn.__tabs = $.fn.tabs;
		$.fn.tabs = function (a, b, c, d, e, f) {
			var base = location.href.replace(/#.*$/, '');
			$('ul>li>a[href^="#"]', this).each(function () {
				var href = $(this).attr('href');
				$(this).attr('href', base + href);
			});
			$(this).__tabs(a, b, c, d, e, f);
		};

		$(function() {
			$("#tabs-item-base").find( ".field-info" ).each( function(index, item) {
				if( $(item).text().trim() === "" ) {
					$(item).text( "–" );
				}
			});

			$("#tabs-item-main").tabs( {
				classes: {
					"ui-tabs": "tabs-main",
					"ui-tabs-nav": "tabs-nav",
					"ui-tabs-tab": "tabs-tab",
				}
			});

			if( window.name != "sub-content" ) {
				syncFrameHeight();

				$(window).resize( function () {
					syncFrameHeight( true );
				});

				if( parent.$(".content-overlay .loading").is(":visible") ) {
					toggleLoadingParent( false );
				}
			} else {
				$("body").removeClass().addClass( "sub-content" );
				$("#frame-content-wrap").removeClass().addClass( "content-wrap" );
				$("#tabpane_main").addClass( "none-card" );
			}

			$("#tabs-item-base img").on( "error", function() {
				$(this).attr( "src", "images/no_image.png" );
				$("button[name=btnDelete]").hide();
			});

			$(".tabs-title.item-uom").on( "click", function() {
				$(".frame-content").innerHeight( parent.$("iframe.menu-content").contents().find(".frame-content").innerHeight() );
			});
			$(".tabs-title.item-info").on( "click", function() {
				$(".frame-content").css( "height", "" );
			});
		});

		function syncFrameHeight( windowResize ) {
			if( parent.$("iframe.menu-content") ) {
				var height = $(".frame-content-wrap").height();
				parent.$("iframe.menu-content").contents().find( ".frame-content" ).innerHeight( height );
				//$(".frame-content").innerHeight( parent.$("iframe.menu-content").contents().find(".frame-content").innerHeight() );
				parent.$("div.content").height( height + 30 );
				if( ieBrowser && !windowResize ) {
					parent.$("iframe.menu-content").css( "height", height );
					parent.$("iframe.main-content").height( height );

					setTimeout( function() {
						parent.$("iframe.menu-content").css( "height", "100%" );
						parent.$("iframe.main-content").css( "height", "100%" );
					}, 150 );

				}
			}
		}

		function imageResize( img, maxwidth, maxheight ) {
			var vimg = new Image();
			vimg.src = img.src;
			if( vimg.width > maxwidth && vimg.width > vimg.height * maxwidth / maxheight )
				img.width = maxwidth;
			else if( vimg.height > maxheight )
				img.height = maxheight;
		}

		function deleteImage() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItemImage?mode=del";

			var countryCode = "<mtl:value id="record" key="countryCode"/>";
			var itemCode = "<mtl:value id="record" key="itemCode"/>";

			url = replaceQueryValue( url, "countryCode", countryCode );
			url = replaceQueryValue( url, "itemCode", itemCode );

			url = attachDefaultParameter( url );
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			if( checkURLLength(url) ){
				var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" };
				customPopup.confirm( messages, function(res) {
					if( res ) {
						windowSelfOpen( url );
					}
				});
			}
		}

		function modifyDescription() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=mod";
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="record" key="countryCode"/>" );
			url = replaceQueryValue( url, "itemCode", "<mtl:value id="record" key="itemCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "editableIntro", encodeURIComponent(frmMain.editableIntro.value) );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="request" key="partyCode"/>" );
			url = attachDefaultParameter( url );

			var partyCode = "<mtl:value id="record" key="partyCode"/>";
			if( partyCode )
				url = replaceQueryValue( url, "partyCode", partyCode );

			windowSelfOpen( url );
		}

		function registImage() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItemImage?mode=ireg&wintype=sub";

			var countryCode = "<mtl:value id="record" key="countryCode"/>";
			var itemCode = "<mtl:value id="record" key="itemCode"/>";

			url = replaceQueryValue( url, "countryCode", countryCode );
			url = replaceQueryValue( url, "itemCode", itemCode );

			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content' style='padding-left: 8px; overflow: hidden;'>
	<%@ include file="include_rbm_frame_bodyheader.inc" %>
	<div id='frame-content-wrap' class='frame-content-wrap'>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<mtl:hidden id="record" key="itemCode"/>
			<%@ include file="include_rbm_form.inc" %>

		<% if( "frm".equals(property.get("vtype")) ) { %>
			<mtl:contentGroup groupId="main" type="tabpane" styleClass="frame-content" style="height: 650px;">
				<h2><mtl:message key="jsp.dpr_item_input.SUBTITLE_MATERIAL_INFO"/></h2>
				<div class='table w100p' style='height: 100%;'>
					<div class='table-cell align-center' style='vertical-align: middle;'>
						<img src='images/product_view.png'>
						<div class='blank-msg'><%= property.getProperty("infomsg") %></div>
					</div>
				</div>
			</mtl:contentGroup>
		<% } else { %>
			<mtl:contentGroup groupId="main" type="tabpane" styleClass="frame-content">
				<h2><mtl:message key="jsp.dpr_item_input.SUBTITLE_MATERIAL_INFO"/></h2>
				<div id='tabs-item-main'>
					<ul>
						<li><a href='#tabs-item-base' class='tabs-title item-info'><mtl:message key="jsp.GRP_DPR_ITEM_INFO"/></a></li>
						<li><a href='#tabs-item-uom' class='tabs-title item-uom'><mtl:message key="jsp.GRP_DPR_ITEM_UOM"/></a></li>
					</ul>
					<div id='tabs-item-base'>
						<div class='group-wrap'>
							<h3><mtl:message key="jsp.GRP_DPR_ITEM_BASE_INFO"/></h3>
							<div>
								<img src='<%= systemConfig.getClassURL() %>/DPRItemImage?mode=img<%=IMG_PARAM%>'
										onLoad='JavaScript:imageResize(this, 182, 182);'
										style='margin-right: 10px; vertical-align: top'/>
								<div style='display: inline-block; width:calc(100% - 200px);'>
									<div class='info-table table-fixed'>
										<div class='row'>
											<div class='cell'>
												<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_CODE"/></div>
												<div class='field-info'><mtl:valuef id="record" format="$f{pure(itemCode)}"/></div>
											</div>
											<div class='cell'>
												<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_NAME"/></div>
												<div class='field-info' style='white-space: nowrap;'><mtl:value id="record" key="itemName"/></div>
											</div>
											<div class='cell'></div>
											<div class='cell'></div>
										</div>
									</div>
									<div class='info-table table-fixed'>
										<div class='row'>
											<div class='cell'>
												<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_DESCRIPTION"/></div>
												<div class='field-info'><mtl:value id="record" key="editableIntro"/></div>
											</div>
										</div>
									</div>
									<div class='info-table table-fixed'>
										<div class='row'>
											<div class='cell'>
												<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_NEWITEM_INDICATOR"/></div>
												<div class='field-info'><mtl:value id="record" key="newItemInd"/></div>
											</div>
											<div class='cell'>
												<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_PROMOTION_INDICATOR"/></div>
												<div class='field-info'><mtl:value id="record" key="promotionInd"/></div>
											</div>
											<div class='cell'>
												<div class='field-title'><mtl:message key="jsp.dpr_item_info.FILED_DPR_ITEM_PRICE"/></div>
												<div class='field-info'>
													<mtl:value id="record" key="price"/> <mtl:value id="record" key="priceCurrencyName"/>
												</div>
											</div>
											<div class='cell'>
											<% if( com.irt.dpr.Country.isFeature(organizationCode, "useExtra1") ) { %>
												<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_SHELFLIFE"/></div>
												<div class='field-info'><mtl:value id="record" key="shelfLife"/></div>
											<% } %>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class='group-line'></div>

						<div class='group-wrap'>
							<h3><mtl:message key="jsp.GRP_DPR_ITEM_HIERARCHY_INFO"/></h3>
							<div class='info-table table-fixed'>
								<div class='row'>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL1_NAME"/></div>
										<div class='field-info'><mtl:valuef id="record" key="productHR1Name"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL2_NAME"/></div>
										<div class='field-info'><mtl:valuef id="record" key="productHR2Name"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL3_NAME"/></div>
										<div class='field-info'><mtl:valuef id="record" key="productHR3Name"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL4_NAME"/></div>
										<div class='field-info'><mtl:valuef id="record" key="productHR4Name"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL5_NAME"/></div>
										<div class='field-info'><mtl:valuef id="record" key="productHR5Name"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL6_NAME"/></div>
										<div class='field-info'><mtl:valuef id="record" key="productHR6Name"/></div>
									</div>
								</div>
							</div>
						</div>
						<div class='group-line'></div>

						<div class='group-wrap'>
							<h3><mtl:message key="jsp.GRP_DPR_ITEM_GROUPING_INFO"/></h3>
							<div class='info-table table-fixed'>
								<div class='row'>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_BASEPRODUCT"/></div>
										<div class='field-info'><mtl:valuef id="record" key="baseProductName"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_MEGABRAND"/></div>
										<div class='field-info'><mtl:valuef id="record" key="megaBrandName"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_BRAND"/></div>
										<div class='field-info'><mtl:valuef id="record" key="brandName"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_VARIANT"/></div>
										<div class='field-info'><mtl:valuef id="record" key="variantName"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_PUTUP"/></div>
										<div class='field-info'><mtl:valuef id="record" key="putupName"/></div>
									</div>
								</div>
							</div>
						</div>
						<div class='list-function'>
							<div class='button'>
							<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT && htmlpage.hasManageAuth() ) { %>
								<mtl:button type="return"/>
							<%--<mtl:button type="editable" onClick="JavaScript:modifyDescription();"/>--%>
							<%--<mtl:button type="reset" styleClass="btn_page"/> --%>
								<mtl:button type="registImg" onClick="JavaScript: registImage();" messageKey="jsp.BTN_REGIST_IMAGE"/>
								<mtl:button type="deleteImg" name="btnDelete" onClick="JavaScript: deleteImage();" messageKey="jsp.BTN_DELETE_IMAGE"/>
							<% } %>
								<mtl:button type="close_if"/>
							</div>
							<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT && htmlpage.hasManageAuth() ) { %>
							<script type='text/javascript'>
								function checkInput() {
									return submitInput();
								}
							</script>
							<% } %>
						</div>
					</div>

					<div id="tabs-item-uom">
						<mtl:contentGroup groupId="uom" type="list" styleClass="list_content none-card" style='padding-top: 30px;'>
							<%
								ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage, "records_uom" );
								listWriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NUMBER );
								listWriter.print( out );
							%>
						</mtl:contentGroup>
						<div class='list-function'>
							<div class='button'>
							<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT && htmlpage.hasManageAuth() ) { %>
								<mtl:button type="return"/>
							<% } %>
								<mtl:button type="close_if"/>
							</div>
						</div>
					</div>
				</div>
			</mtl:contentGroup>
		<% } %>
		</mtl:form>
	</div>
</body>
</mtl:html>
