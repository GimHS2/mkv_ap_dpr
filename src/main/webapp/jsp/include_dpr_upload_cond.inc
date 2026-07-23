<%--
	File Name:	include_dpr_upload_cond.inc
	Version:	2.2.4

	Description:
		script
			changeCodeValue( codeObj, nameObj );
			checkSearchCond();
			initSearchCond();
			resetSearchCond();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.4	신규 UI/UX 적용
	hankalam	2020/12/31		2.2.3	ignore count 추가
	jbaek		2015/04/30		2.2.2	CrossBrowsing ����
	jbaek		2014/03/31		2.2.1	CrossBrowsing ����
	keehe		2008/09/26		2.2.0	create
--%>

	<script type='text/javascript'>
		$( function() {
			$("input[type=text].date").blur( function() {
				setTimeout( function() {
					if( Field.checkMultiMandatory([frmCond.startUploadDate, frmCond.endUploadDate], false) ) {
						Field.checkDateRange( frmCond.startUploadDate, frmCond.endUploadDate );
					}
				}, 500 );
			});
		});

		function checkSearchCond() {
			if( !Field.checkMultiMandatory([frmCond.startOrderDate, frmCond.endOrderDate], false) ) return false;
			if( !Field.checkDateRange(frmCond.startOrderDate, frmCond.endOrderDate) ) return false;
			disableBlankInput( frmCond, true );
			return submitInput();
		}
	</script>

	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<%@ include file="include_rbm_form.inc" %>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<mtl:hidden id="request" key="sort"/>
			<mtl:hidden id="request" key="max"/>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="startUploadDate" descriptionKey="jsp.dpr_upload_list.FIELD_START_UPLOADDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="startUploadDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="endUploadDate" descriptionKey="jsp.dpr_upload_list.FIELD_END_UPLOADDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="endUploadDate"/></div>
					</div>
					<div class='cell'>
					<% if( sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin() ) { %>
						<div class='field-title'><mtl:message key="jsp.dpr_upload_list.FILED_UPLOADUSER"/></div>
						<div class='field'>
							<mtl:select id="condition" key="uploadUserId" hasBlank="true"  nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_UPLOAD_USER" searchable="false"
									listId="uploadUser" listCodeKey="uploadUserId" listNameFormat="$S{[:userId;$S{] :userName}}"/>
						</div>
					<% } %>
					</div>
					<div class='cell'></div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="uploadType" descriptionKey="FIELD_DPR_UPLOAD_UPLOADTYPE"/></div>
						<div class='field'>
							<%
								String uploadTypeValues = property.getProperty( "uploadTypeValues" );
							%>
							<mtl:select id="condition" key="uploadType" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_UPLOAD_TYPE" searchable="false"
									prefixKey="DPR_UPLOAD_" codeValues="<%= uploadTypeValues %>"/>
						</div>
					</div>
					<%
						String statusValues;
						if( "SSL".equals(request.getParameter("uploadType")) && sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) {
							statusValues = "RD,CO,IG,ER";
						} else {
							statusValues = "RD,CO,ER";
						}
					%>
					<div class='cell'>
						<div class='field-title'><mtl:title key="status" descriptionKey="jsp.dpr_upload_list.FILED_UPLOADSTATUS"/></div>
						<div class='field'>
							<mtl:select id="condition" key="status" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_UPLOAD_STATUS" searchable="false"
									defaultValue="CO" prefixKey="DPR_UPLOAD_STATUS_" codeValues="<%= statusValues %>"/>
						</div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
			</div>
			<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>
		</mtl:contentGroup>
	</mtl:form>
