<%--
	File Name:	rbm_uploadlog_list.jsp
	Version:	2.2.9c(mpf)

	Description:

	Note:
		systemConfig.getProperty()
			"uploadPath"

	Modified	(YYYY/MM/DD)	Ver		Content
	hjj4060		2019/03/29		2.2.9c	화이자 디자인 적용
	GimHS		2013/12/31		2.2.9	"close" button에 href="JavaScript:windowClose(true);" 추가
	GimHS		2012/12/31		2.2.8	CrossBrowsing 적용
										  -> 표시건수 select 박스가 아래로 내려가는 문제 해결
										  -> 리스트 좌측 상단에 있는 검색 Select 박스 일렬로 안나오는 문제 해결
	stghr12		2011/06/30		2.2.7	pageEncoding="euc-kr" 추가
	stghr12		2011/03/31		2.2.6	uploadType 조건 처리방식 수정
	stghr12		2010/03/31		2.2.5	messageKey 변경: MSG_PUB_SELECT 사용
	stghr12		2009/10/31		2.2.4	<mtl:select> 변경사항 적용
	stghr12		2009/08/31		2.2.3	_ALL -> _SELECT
	stghr12		2009/01/10		2.2.2	error파일 다운로드 추가
	stghr12		2008/11/14		2.2.1	LinkMenu 오류수정: 'new' -> 'self'
	stghr12		2008/03/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map logMap = (java.util.Map)pageContext.findAttribute( "record_log" );
	java.util.Map conditionMap = (java.util.Map)pageContext.findAttribute( "condition" );
	if( conditionMap.containsKey("uploadType") ) {
		Object uploadType = conditionMap.get( "uploadType" );
		if( !(uploadType instanceof Object[]) )
			conditionMap.put( "_uploadType", uploadType );
	}

	String uploadPath = systemConfig.getProperty( "uploadPath" );
	if( "".equals(uploadPath) ) uploadPath = null;

	boolean isS3Stroage = "Y".equals( systemConfig.getProperty("s3Storage") );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;
		var windowWidth = 1100;

		$(function() {
			if( window.name == "sub-content" ) {
				$("body").removeClass().addClass( "sub-content" );
				$(".content_group").addClass( "none-card" );
			}
		});

		function detailListReq( logId ) {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?mode=list&logId="+ encodeURIComponent(logId), getLocationURL() );
		}

		function downloadReq( logId, downloadType ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=down&logId="+ encodeURIComponent(logId);
			windowOpen( replaceQueryValue(url, "downloadType", downloadType) );
		}

		function linkMenuReq( gln, buyerGln, sellerGln ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.rbm_uploadlog_list.LMENU_LOGDETAIL" encodeScript="true"/>'
						, 'self', 'JavaScript:linkMenuReqClick("LIST");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "LIST" ) {
				detailListReq( linkmenu.params[0] );
			}
		}
	</script>
</head>

<body class='content'>
	<div id='content-wrap' class='content-wrap'>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
			<mtl:contains id="record_log" copyId="_record">
				<mtl:contentGroup groupId="condition" type="search">
					<div class='search-table'>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_UPLOADTYPE"/></div>
								<div class='field-info'><mtl:value id="_record" key="uploadTypeName"/></div>
							</div>
							<div class='cell'>
								<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_DATETIME"/></div>
								<div class='field-info'>
									<mtl:valuef id="_record" format="${startDateTime}${ - :endDateTime~11~19}"/>
								</div>
							</div>
							<div class='cell'>
							</div>
						</div>
					</div>
					<div class='search-table'>
						<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_MESSAGE"/></div>
								<div class='field-info'><mtl:value id="_record" key="message"/></div>
							</div>
							<mtl:contains id="_record" key="uploadFileName">
							<div class='cell'>
								<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_UPLOADFILENAME"/></div>
								<div class='field-info'>
								<% if( (isS3Stroage || uploadPath != null) && logMap.get("fileName") != null ) { %>
									<a href='JavaScript:downloadReq("<mtl:value id="_record" key="logId"/>");'
											title='<mtl:message key="jsp.MSG_DOWNLOAD"/>'><mtl:value id="_record" key="uploadFileName"/></a>
									<% if( logMap.get("errorCount") != null && ((Number)logMap.get("errorCount")).intValue() > 0 ) { %>
										(
										<mtl:message key="jsp.rbm_uploadlog_list.FIELD_ERROR_FILE"/> :
										<a href='JavaScript:downloadReq("<mtl:value id="_record" key="logId"/>", "err");'
												title='<mtl:message key="jsp.MSG_DOWNLOAD"/>'><mtl:value id="_record" key="uploadFileName"/>.err</a>
										)
									<% } %>
								<% } else { %>
									<mtl:value id="_record" key="uploadFileName"/>
								<% } %>
								</div>
							</div>
							</mtl:contains>
						</div>
					</div>
				</mtl:contentGroup>
			</mtl:contains>

			<mtl:contentGroup groupId="list" type="list">
				<div class='list-menu'>
				<% if( logMap != null ) { %>
					<mtl:select id="condition" key="status" prefixKey="RBM_UPLOADLOG_LINESTATUS_" codeValues="CP,WN,ER"
							hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_RBM_UPLOADLOG_LINESTATUS" modified="JavaScript:listLink(this);"
							searchable="false" width="auto"/>
				<% } else { %>
					<mtl:select id="condition" key="_uploadType" listId="uploadtypes" listCodeKey="code" listNameFormat="${RBM_UPLOADLOG_@code}"
							hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_RBM_UPLOADLOG_UPLOADTYPE" ignoreNotListedValue="true"
							modified="JavaScript:listLink(this, \"uploadType\");" searchable="false" width="auto"/>
					<mtl:select id="condition" key="status" prefixKey="RBM_UPLOADLOG_STATUS_" codeValues="RU,CP,ER"
							hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_RBM_UPLOADLOG_STATUS" modified="JavaScript:listLink(this);"
							searchable="false" width="auto"/>
				<% } %>
				</div>

				<%
					ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
					if( logMap != null ) listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NONE );
					listwriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="return"/>
						<mtl:button type="close_if" onClick="JavaScript: windowClose(true);"/>
					</div>
					<div id='list_page' class='page'>
						<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
					</div>
				</div>
			</mtl:contentGroup>
		<% if( htmlpage.getWindowType() == null || !"sub".equals(htmlpage.getWindowType().split("@")[0]) ) { %>
			<%@ include file="include_dpr_tail.inc" %>
		<% } %>
		</mtl:form>
	</div>
</body>
</mtl:html>
