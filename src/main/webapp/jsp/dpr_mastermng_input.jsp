<%--
	File Name:	dpr_mastermng_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/04/30		2.2.0	create
--%>

<%@page import="com.irt.dpr.ProductHierarchy"%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>
<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<%
	String[] altKeys = (String[])request.getAttribute("altKeys");
	String altKeyCsv = "";
	if( altKeys != null && altKeys.length > 0 ) {
		altKeyCsv = com.irt.util.StringUtil.strJoin(altKeys, ",");
	}

	String supportLocale = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;"+ sessionMng.getPartyId(), "en");
	String supportLocaleLabel = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocaleLabel;"+ sessionMng.getPartyId(), "en");
	String[] locales = supportLocale.split(",");
	String[] localeLabels = supportLocaleLabel.split(",");
	java.util.List languages = new java.util.ArrayList();
	for( int i=0; i < locales.length; i++ ) {
		java.util.Map map = new java.util.HashMap();
		map.put("code", locales[i]);
		map.put("name", localeLabels[i]);
		languages.add( map );
	}
	request.setAttribute("languages", languages);

	boolean imod = "imod".equals(htmlpage.getMode());

	java.util.Map<String, Object> recordMap = (java.util.Map<String, Object>)request.getAttribute("record");
	String organizationCode = null;
	if( recordMap != null )
		organizationCode = (String)recordMap.get("organizationCode");
	if( organizationCode == null )
		organizationCode = request.getParameter("organizationCode");
	%>

	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 600 );
			resetForm( frmMain );
			focusForm( frmMain );

			<% if( "imod".equals(htmlpage.getMode()) ) { %>
				applyAltKeys();
			<% } %>

			<%
			String defaultLinkTypeCsv = null;
			if( recordMap == null )
				defaultLinkTypeCsv = null;
			else if( recordMap.get("defaultLinkType") instanceof String ) {
				defaultLinkTypeCsv = (String)recordMap.get("defaultLinkType");
			} else if( recordMap.get("defaultLinkType") instanceof String[] ) {
				defaultLinkTypeCsv = com.irt.util.StringUtil.strJoin((String[])recordMap.get("defaultLinkType"), ",");
			}

			String[] defaultLinkType = (defaultLinkTypeCsv == null ? ("AG,RE,RG,WE").split(",") : defaultLinkTypeCsv.split(","));
			java.util.Map<String,Object> defaultLinkTypeMap = new java.util.HashMap<String,Object>();
			if( "PartySales".equals(htmlpage.getProperty().getProperty("mngtypeName")) ) {
				if( "ireg".equals(htmlpage.getMode()) || "imod".equals(htmlpage.getMode()) ) {
					for( String ltype : defaultLinkType ) {
						defaultLinkTypeMap.put(ltype, ltype);
						out.println( "frmMain.defaultLinkType_"+ ltype +".defaultChecked = true;" );
						out.println( "CheckBox.change( frmMain.defaultLinkType_"+ ltype +", true );" );
					}
				} else if( "info".equals(htmlpage.getMode()) ) {
					for( String ltype : defaultLinkType ) {
						defaultLinkTypeMap.put(ltype, ltype);
					}
				}
			%>
				changeDefaultLinkType();
			<% } %>

			<% if( "ProductCategory".equals(htmlpage.getProperty().getProperty("mngtypeName")) ) { %>
				changeClassCode();
			<% } %>
		}

		function applyAltKeys() {
			var altKeyCsv = "<%=altKeyCsv%>";
			if( altKeyCsv ) {
				var altKeys = altKeyCsv.split(",");

				return $.map(altKeys, function(i,o){
					if( frmMain[o] ) {
						if( $(frmMain[o]).prop("tagName") == "INPUT" ) {
							frmMain[o].readonly = false;
						}
					}
					return frmMain[o];
				});
			}
		}

		function togglePartyLinkType( el ) {
			var val = Field.getValue( el );
			var mandaOrOptKeys = ["officeCode", "officeName", "groupCode", "groupName", "districtCode", "customerGroupCode"];
			if( "Y" == val ) {
				$.each(mandaOrOptKeys, function(i,o){
					var el = frmMain[o];
					if( el ) {
						$(el).closest("table.line_content").css("display", "none").prop("disabled", "disabled");
						Field.changeMandatory( el , true );
					}
				});
			} else {
				$.each(mandaOrOptKeys, function(i,o){
					var el = frmMain[o];
					if( el ) {
						$(el).closest("table.line_content").css("display", "").prop("disabled", "");
						Field.changeMandatory( el , true );
					}
				});
			}
		}

		function changeDefaultLinkType( obj ) {
			var checkedValue = $(frmMain.defaultLinkType).map(function(i,m) { if( m.checked == true ) return m.value; } ).get();

			if( checkedValue && checkedValue.length == 1 && checkedValue[0] == "WE" ) {
				if( frmMain.officeCode )
					Field.changeMandatory( frmMain.officeCode, false );
				if( frmMain.groupCode )
					Field.changeMandatory( frmMain.groupCode, false );
				if( frmMain.districtCode )
					Field.changeMandatory( frmMain.districtCode, false );
				if( frmMain.customerGroupCode )
					Field.changeMandatory( frmMain.customerGroupCode, false );

				frmMain.officeCode.value = "";
				frmMain.groupCode.value = "";
				frmMain.districtCode.value = "";
				frmMain.customerGroupCode.value = "";
			} else {
				if( frmMain.officeCode )
					Field.changeMandatory( frmMain.officeCode, true );
				if( frmMain.groupCode )
					Field.changeMandatory( frmMain.groupCode, true );
				if( frmMain.districtCode )
					Field.changeMandatory( frmMain.districtCode, true );
				if( frmMain.customerGroupCode )
					Field.changeMandatory( frmMain.customerGroupCode, true );

				if( !frmMain.officeCode.value )
					frmMain.officeCode.value = frmMain.officeCode.defaultValue;
				if( !frmMain.groupCode.value )
					frmMain.groupCode.value = frmMain.groupCode.defaultValue;
				if( !frmMain.districtCode.value )
					frmMain.districtCode.value = frmMain.districtCode.defaultValue;
				if( !frmMain.customerGroupCode.value )
					frmMain.customerGroupCode.value = frmMain.customerGroupCode.defaultValue;
			}
		}


	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>

		<mtl:hidden id="property" key="mngtype"></mtl:hidden>

		<mtl:contentGroup groupId="main" type="content">

			<mtl:ifvalue id="property" key="mngtypeName" value="ProductCategory">
			<%
				com.irt.sql.HierarchyCodeField codeField = (com.irt.sql.HierarchyCodeField)pageContext.findAttribute( "codeField" );
			%>
			<script type="text/javascript">
				function changeClassCode() {
					if( !frmMain.classCode ) return;

					switch( frmMain.classCode.value ) {
					<%
						int level = codeField.getLastLevel();
						for( int l = 0; l < level; l++ )
							out.println( "case '"+ (l+1) +"': frmMain.code.maxLength = "+ codeField.getLength(l+1) +"; break;" );
					%>
					}
				}
			</script>

			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="classCode"/></div>
						<div class='field'>
							<mtl:select id="record" key="classCode" modified="JavaScript:changeClassCode(); Field.modified(this);" searchable="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="code"/></div>
						<div class='field'>
							<mtl:text id="record" key="code"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="MasterDesc">
			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="languageCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:code;$S{] :name}}"/>
							<mtl:hidden id="record" key="distributionChannelCode"/>
						<% } else { %>
							<mtl:select id="request" key="languageCode" listId="languages" listNameFormat="$S{[:code;$S{] :name}}" searchable="false"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="masterType"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="masterType" format="${DPR_MASTER_TYPE_@masterType}"/>
							<mtl:hidden id="record" key="masterType"/>
						<% } else { %>
							<mtl:select id="record" key="masterType" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_MASTERTYPE"
									prefixKey="DPR_MASTER_TYPE_" codeValues="CG,RG,SD,DC,SO,SF,SG,BP,MB,BR,VA,PU,PC"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="masterCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="masterCode" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="masterName"/></div>
						<div class='field'>
							<mtl:text id="record" key="masterName" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ItemMasterDesc">
			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="languageCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:code;$S{] :name}}"/>
							<mtl:hidden id="record" key="distributionChannelCode"/>
						<% } else { %>
							<mtl:select id="request" key="languageCode" listId="languages" listNameFormat="$S{[:code;$S{] :name}}" searchable="false"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="itemCode" mandatory="true" readonly="<%=imod%>"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemName"/></div>
						<div class='field'>
							<mtl:text id="record" key="itemName" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ItemMaster">
			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="itemCode" mandatory="true" readonly="<%=imod%>"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemType"/></div>
						<div class='field'>
							<mtl:text id="record" key="itemType" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="baseUnitofMeasure"/></div>
						<div class='field'>
							<mtl:text id="record" key="baseUnitofMeasure" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="salesUnit"/></div>
						<div class='field'>
							<mtl:text id="record" key="salesUnit" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="productCategoryCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="productCategoryCode" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ItemMasterSales">
			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
							<mtl:hidden id="record" key="organizationCode"/>
						<% } else { %>
							<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC\");"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
							<mtl:hidden id="record" key="distributionChannelCode"/>
						<% } else { %>
							<mtl:select id="record" key="distributionChannelCode" mandatory="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
									listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="itemCode" mandatory="true" readonly="<%=imod%>"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="salesUnit"/></div>
						<div class='field'>
							<mtl:text id="record" key="salesUnit" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="productCategoryCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="productCategoryCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="megaBrandCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="megaBrandCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="brandCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="brandCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="baseProductCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="baseProductCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="putupCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="putupCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="variantCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="variantCode" readonly="false"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ItemMasterUom">
			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="itemCode" mandatory="true" readonly="<%=imod%>"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="uomCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="uomCode" mandatory="true" readonly="<%=imod%>"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="uomName"/></div>
						<div class='field'>
							<mtl:text id="record" key="uomName" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="packSize"/></div>
						<div class='field'>
							<mtl:text id="record" key="packSize" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="eanCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="eanCode" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="width"/></div>
						<div class='field'>
							<mtl:text id="record" key="width" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="height"/></div>
						<div class='field'>
							<mtl:text id="record" key="height" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="length"/></div>
						<div class='field'>
							<mtl:text id="record" key="length" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="diameter"/></div>
						<div class='field'>
							<mtl:text id="record" key="diameter" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="volume"/></div>
						<div class='field'>
							<mtl:text id="record" key="volume" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="weight"/></div>
						<div class='field'>
							<mtl:text id="record" key="weight" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="lengthUnit"/></div>
						<div class='field'>
							<mtl:text id="record" key="lengthUnit" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="volumeUnit"/></div>
						<div class='field'>
							<mtl:text id="record" key="volumeUnit" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="weightUnit"/></div>
						<div class='field'>
							<mtl:text id="record" key="weightUnit" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="variantCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="variantCode" readonly="false"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="PartySales">
			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
							<mtl:hidden id="record" key="organizationCode"/>
							<mtl:hidden id="record" key="organizationName"></mtl:hidden>
						<% } else { %>
							<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC\");"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="defaultLinkType" descriptionKey="jsp.dpr_mastermng_input.MSG_DPR_PARTY_FUNCTION_DEFAULTTYPE"/></div>
						<div class='field'>
							<mtl:hidden id="record" key="defaultLinkType" name="defaultLinkType_orig"/>
							<span id="checkbox_defaultLinkType_AG">
								<mtl:check id="record" key="defaultLinkType" descriptionKey="MSG_FIELD_DPR_PARTY_TYPE_AG" checkValue="AG" defaultValue="AG"
										modified="JavaScript: changeDefaultLinkType(this);"/>
								<%=HtmlUtility.toHtmlString((defaultLinkTypeMap.get("AG")==null ? "": "("+defaultLinkTypeMap.get("AG")+")"))%>
							</span>
							<span id="checkbox_defaultLinkType_RE">
								<mtl:check id="record" key="defaultLinkType" descriptionKey="MSG_FIELD_DPR_PARTY_TYPE_RE" checkValue="RE" defaultValue="RE"
										modified="JavaScript: changeDefaultLinkType(this);"/>
								<%=HtmlUtility.toHtmlString((defaultLinkTypeMap.get("RE")==null ? "": "("+defaultLinkTypeMap.get("RE")+")"))%>
							</span>
							<span id="checkbox_defaultLinkType_RG">
								<mtl:check id="record" key="defaultLinkType" descriptionKey="MSG_FIELD_DPR_PARTY_TYPE_RG" checkValue="RG" defaultValue="RG"
										modified="JavaScript: changeDefaultLinkType(this);"/>
								<%=HtmlUtility.toHtmlString((defaultLinkTypeMap.get("RG")==null ? "": "("+defaultLinkTypeMap.get("RG")+")"))%>
							</span>
							<span id="checkbox_defaultLinkType_WE">
								<mtl:check id="record" key="defaultLinkType" descriptionKey="MSG_FIELD_DPR_PARTY_TYPE_WE" checkValue="WE" defaultValue="WE"
										modified="JavaScript: changeDefaultLinkType(this);"/>
								<%=HtmlUtility.toHtmlString((defaultLinkTypeMap.get("WE")==null ? "": "("+defaultLinkTypeMap.get("WE")+")"))%>
							</span>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
							<mtl:hidden id="record" key="distributionChannelCode"/>
						<% } else { %>
							<mtl:select id="record" key="distributionChannelCode" mandatory="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
									listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="partyCode" mandatory="true" readonly="<%=imod%>"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyName"/></div>
						<div class='field'>
							<mtl:text id="record" key="partyName" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="regionCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
							<mtl:hidden id="record" key="distributionChannelCode"/>
						<% } else { %>
							<mtl:select id="record" key="regionCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_REGIONCD"
									hasBlank="true" listId="regions" listCodeKey="regionCode" listNameFormat="$S{[:regionCode;$S{] :regionName}}"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="officeCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="officeCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="groupCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="groupCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="districtCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="districtCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="customerGroupCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="customerGroupCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="deliveryPlant"/></div>
						<div class='field'>
							<mtl:text id="record" key="deliveryPlant" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="transportZone"/></div>
						<div class='field'>
							<mtl:text id="record" key="transportZone" mandatory="true" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="districtCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="districtCode" readonly="false"/>
						</div>
					</div>
				</div>
			<% if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") ) { %>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="allowUOM"/></div>
						<div class='field'>
							<mtl:text id="record" key="allowUOM"/>
						</div>
					</div>
				</div>
			<% } %>
				<mtl:ifvalue id="property" key="useExtraOptionalInfo" value="Y">
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="city"/></div>
						<div class='field'>
							<mtl:text id="record" key="city" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="addressCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="addressCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="extraAddress1"/></div>
						<div class='field'>
							<mtl:text id="record" key="extraAddress1" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="extraAddress2"/></div>
						<div class='field'>
							<mtl:text id="record" key="extraAddress2" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="postalCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="postalCode" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="tax1"/></div>
						<div class='field'>
							<mtl:text id="record" key="tax1" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="tax2"/></div>
						<div class='field'>
							<mtl:text id="record" key="tax2" readonly="false"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="postalCode"/></div>
						<div class='field'>
							<mtl:text id="record" key="postalCode" readonly="false"/>
						</div>
					</div>
				</div>
				</mtl:ifvalue>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="PartyFunction">
			<div class='info-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
							<mtl:hidden id="record" key="organizationCode"/>
							<mtl:hidden id="record" key="organizationName"></mtl:hidden>
						<% } else { %>
							<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC\");"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row' style='display: none;'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="countryKey"/></div>
						<div class='field'>
							<mtl:text id="record" key="countryKey" readonly="true"/>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
							<mtl:hidden id="record" key="distributionChannelCode"/>
						<% } else { %>
							<mtl:select id="record" key="distributionChannelCode" mandatory="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
									listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="partyCode" format="$S{[:partyCode;]$S{ :partyName}}"/>
							<mtl:hidden id="record" key="partyCode"/>
							<mtl:hidden id="record" key="partyName"/>
						<% } else { %>
							<mtl:text id="record" key="partyCode"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="linkType" descriptionKey="jsp.dpr_mastermng_input.MSG_DPR_PARTY_FUNCTION_TYPE"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="linkType" format="$S{[:linkType;]$S{ :linkTypeName}}"/>
							<mtl:hidden id="record" key="linkType"/>
						<% } else { %>
							<mtl:select id="record" key="linkType" prefixKey="MSG_FIELD_DPR_PARTY_TYPE_" codeValues="WE,RE,RG"
									modified="JavaScript:togglePartyLinkType(this);" searchable="false"/>
						<% } %>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="linkPartyCode" descriptionKey="jsp.dpr_mastermng_input.MSG_DPR_PARTY_FUNCTION_LINK_PARTY_CODE"/></div>
						<div class='field'>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							<mtl:valuef id="record" key="linkPartyCode" format="$S{[:linkPartyCode;]$S{ :linkPartyName}}"/>
							<mtl:hidden id="record" key="linkPartyCode"/>
							<mtl:hidden id="record" key="linkPartyName"/>
						<% } else { %>
							<mtl:text id="record" key="linkPartyCode"/>
						<% } %>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>
			<div class='info-bottom'>
				<div class='table-cell info-button'>
					<mtl:button type="close_if"/>
					<mtl:button type="return"/>
				<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
					<mtl:button type="reset"/>
					<mtl:button type="save"/>
				<% } %>
				</div>
			</div>
		</mtl:contentGroup>


		<script type='text/javascript'>
			function checkInput() {
				<%= htmlpage.getValidationScript() %>

				<% if( "PartySales".equals(htmlpage.getProperty().getProperty("mngtypeName")) ) { %>

				var soldCheckbox = $(frmMain.defaultLinkType).filter(function(i) { return frmMain.defaultLinkType[i].value == "AG" } )[0];
				var shipCheckbox = $(frmMain.defaultLinkType).filter(function(i) { return frmMain.defaultLinkType[i].value == "WE" } )[0];
				if( shipCheckbox.checked == false && !Field.checkMandatory( soldCheckbox, "<mtl:message key="MSG_FIELD_DPR_PARTY_TYPE_AG" encodeScript="true"/>" ) ) return false;

				<% } %>

				return submitInput();
			}

		</script>
	</mtl:form>
</body>
</mtl:html>

