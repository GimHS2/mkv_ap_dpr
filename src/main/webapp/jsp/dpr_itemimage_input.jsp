<%--
	File Name:	dpr_itemimage_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
		java.util.Map condition = (java.util.Map)pageContext.findAttribute( "condition" );
		java.util.Map record = (java.util.Map)pageContext.findAttribute( "record" );
		String countryCode = (String)record.get( "countryCode" );
		String itemCode = (String)record.get( "itemCode" );

		String IMG_PARAM = "&countryCode=" + countryCode + "&itemCode=" + itemCode;
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );

			resetForm( frmMain );
			focusForm( frmMain, frmMain.title );
		}

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItemImage?mode=imod";
			url = replaceQueryValue( url, "countryCode", encodeURIComponent("<mtl:value id="record" key="countryCode"/>") );
			url = replaceQueryValue( url, "itemCode", encodeURIComponent("<mtl:value id="record" key="itemCode"/>") );

			windowSelfOpen( url );
		}

		function imageResize( img, maxwidth, maxheight ) {
			var vimg = new Image();
			vimg.src = img.src;

			if( vimg.width > maxwidth && vimg.width > vimg.height * maxwidth / maxheight )
				img.width = maxwidth;
			else if( vimg.height > maxheight )
				img.height = maxheight;
		}

		function frameWindowClose() {
			var reload = false;
		<% if( com.irt.servlet.ServletModel.MODE_MODIFY.equals(htmlpage.getMode())
				|| com.irt.servlet.ServletModel.MODE_MULTIMODIFY.equals(htmlpage.getMode())
				|| com.irt.servlet.ServletModel.MODE_REGIST.equals(htmlpage.getMode())
				|| com.irt.servlet.ServletModel.MODE_UPLOAD.equals(htmlpage.getMode()) ) { %>

			reload = true;
		<% } %>

			if( reload ) {
				if( parent.$("body.frame-content").length && parent.frames["main_content"] ) {
					parent.frames["main_content"].location.reload();
				} else {
					self.opener.location.reload();
				}
			}

			if( window.name == "sub-content" ) {
				parent.$(".content-overlay").fadeOut();
				parent.$(".sub-content-wrap").hide();
				parent.$(".sub-content-wrap").css( "width", "" );
				//parent.$("#sub-content").attr( "src", "<%= systemConfig.getProperty("baseURL") %>blank.html" );
			} else {
				self.close();
			}
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" enctype="multipart/form-data" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='displayType' value='FR'>
			<mtl:hidden id="record" key="itemCode"/>
			<mtl:hidden id="record" key="countryCode"/>

			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode"/></div>
							<div class='field'><span class='info'><mtl:value id="record" key="itemCode"/></span></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
							<div class='field'><span class='info'><mtl:value id="record" key="itemName"/></span></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="imageFileFullName" descriptionKey="jsp.dpr_itemimage.MSG_ITEM_IMAGE_FILENAME"/></div>
							<div class='field'><span class='info'><mtl:valuef id="record" key="imageFileFullName" format="$S{imageFileFullName} $S{(:imageSize; KB)}"/></span></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemImageFile" descriptionKey="jsp.dpr_itemimage.MSG_ITEM_IMAGE_FILE"/></div>
							<div class='field'>
								<input type='file' name='itemImageFile' class='input-field'/>
							</div>
						</div>
					</div>
				</div>

				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
					<% } %>
						<mtl:button type="button" onClick="JavaScript: frameWindowClose();" icon="images/ico_close.png" messageKey="jsp.BTN_CLOSE"/>
					</div>
				</div>
<%--
				<td class='content_image' width='150'>
					<img src='<%= systemConfig.getClassURL() %>/DPRItemImage?mode=img<%=IMG_PARAM%>'
							onLoad='JavaScript:imageResize(this, 100, 100);'/>
				</td></tr>
				</table> --%>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					if( !Field.checkMandatory(frmMain.itemImageFile) ) return false;
					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
