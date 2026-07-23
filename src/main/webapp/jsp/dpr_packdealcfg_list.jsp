<%--
	File Name:	dpr_packdealcfg_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/05/30		2.2.0	create
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
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
	com.irt.data.cols.ColumnList columnList = (com.irt.data.cols.ColumnList)pageContext.findAttribute("columnList");

	String primaryKeysCsv = com.irt.util.StringUtil.strJoin(columnList.getPrimaryFieldKeys(), ",");
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,dealCode" );
		}

		function infoReq( organizationCode, distributionChannelCode, officeCode, groupCode, partyCode, dealCode ) {
			var values = organizationCode + ";" + distributionChannelCode + ";" + officeCode + ";" + groupCode + ";" + partyCode + ";" + dealCode;
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,dealCode", values );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,dealCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			windowOpen( url, "sub-content" );
		}

		function downloadReq() {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,dealCode" );
		}

		function uploadReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function linkMenuReq( itemCode ) {
			if( !linkmenu ) {
				var idx = 0;
				var menu = new Array;

				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_DPR_PACKDEAL_ITEM_LIST" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("ITEM_LIST");' );
				<% if( !"pdcfg".equals(htmlpage.getProperty().getProperty("mngtype")) && sessionMng.isAuthorized("DPR", "DPRPackDealCfg.MNG") ) { %>
				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_DPR_PACKDEAL_CFG_LIST" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("CFG_LIST");' );
				<% } %>
				<% if( !"pdrlt".equals(htmlpage.getProperty().getProperty("mngtype")) && sessionMng.isAuthorized("DPR", "DPRPackDealCfgRlt.LST") ) { %>
				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_DPR_PACKDEAL_CFGRLT_LIST" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("CFGRLT_LIST");' );
				<% } %>

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "ITEM_LIST" )
				linkPackdealItemListReq( linkmenu.params[0], linkmenu.params[1], linkmenu.params[2], linkmenu.params[3], linkmenu.params[4], linkmenu.params[5] );
			else if( menu == "CFGRLT_LIST" )
				linkPackdealCfgRltListReq( linkmenu.params[0], linkmenu.params[1], linkmenu.params[2], linkmenu.params[3], linkmenu.params[4], linkmenu.params[5] );
			else if( menu == "CFG_LIST" )
				linkPackdealCfgListReq( linkmenu.params[0], linkmenu.params[1], linkmenu.params[2], linkmenu.params[3], linkmenu.params[4], linkmenu.params[5] );
		}

		function linkPackdealCfgRltListReq( dealCode, organizationCode, distributionChannelCode, officeCode, groupCode, partyCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPackDealCfgRlt?mode=list";

			url = replaceQueryValue( url, "dealCode", encodeURIComponent(dealCode) );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributonChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			windowSelfOpen( url, getLocationURL("url") );
		}

		function linkPackdealCfgListReq( dealCode, organizationCode, distributionChannelCode, officeCode, groupCode, partyCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPackDealCfg?mode=list";

			url = replaceQueryValue( url, "dealCode", encodeURIComponent(dealCode) );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributonChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			windowSelfOpen( url, getLocationURL("url") );
		}

		function linkPackdealItemListReq( dealCode, organizationCode, distributionChannelCode, officeCode, groupCode, partyCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPackDealItem?mode=list";

			url = replaceQueryValue( url, "dealCode", encodeURIComponent(dealCode) );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", encodeURIComponent(organizationCode) );
			url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(distributionChannelCode) );
			url = replaceQueryValue( url, "officeCode", encodeURIComponent(officeCode) );
			url = replaceQueryValue( url, "groupCode", encodeURIComponent(groupCode) );
			url = replaceQueryValue( url, "partyCode", encodeURIComponent(partyCode) );

			windowSelfOpen( url, getLocationURL("url") );
		}

		function partyInfoReq( partyCode, linkType ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=info";

			url = replaceQueryValue( url, "partyCode", partyCode );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "displayLinkType", linkType );

			windowOpen( url + "&wintype=sub", "sub-content" );
		}

		function partyLinkMenuReq( partyCode, linkType ) {
			var menu = new Array;

			menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_PARTY_INFO" encodeScript="true"/>'
					, 'new', 'JavaScript:partyLinkMenuReqClick("INFO");' );

			linkmenu = createLinkMenu( menu );
			linkmenu.show();
			linkmenu.params = partyLinkMenuReq.arguments;
		}

		function partyLinkMenuReqClick( menu ) {
			if( menu == "INFO" )
				partyInfoReq( linkmenu.params[0], linkmenu.params[1] );
		}

		function releaseField( obj ) {
			var fieldName = obj.name;
			if( !obj.value ) {
				var prefix = "frmCond.condition";
				var nameObj = eval( prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length - 4) + "Name" );
				nameObj.value = "";
				Field.release( nameObj );
			}
		}

		function setTradePartner( type, obj, options ) {
			if( options ) {
				obj.disabled = true;

				while( obj.options.length >= 1 )
					obj.remove( obj.options.length - 1 );

				var optionObj;
				if( obj.options.length < 1 ) {
					optionObj = document.createElement( "option" );
					obj.appendChild( optionObj );
				} else
					optionObj = obj.options[0];

				optionObj.value = "";
				if( "sold" == type )
					optionObj.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_SOLD_BLANK"/>";
				else if( "ship" == type )
					optionObj.innerHTML = "<mtl:message key="jsp.dpr_order_input.MSG_SHIP_BLANK"/>";

				for( var i = 0; i < options.length; i++ ) {
					optionObj = document.createElement( "option" );
					optionObj.value = options[i][0];
					optionObj.innerHTML = options[i][1];
					obj.appendChild( optionObj );
				}
			}
			obj.disabled = false;
		}

		function setTradePartners( type, soldOptions, shipOptions ) {
			if( "sold" == type )
				setTradePartner( type, frmCond.partyCode, soldOptions );
			else if( "ship" == type )
				setTradePartner( type, frmCond.shipPartyCode, shipOptions );
		}

	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list'/>
		<mtl:hidden id="property" key="btype"/>
		<mtl:hidden id="property" key="mngtype"/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="dealCode" descriptionKey="FIELD_DPR_PACKDEAL_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="dealCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="isPackdealDate" descriptionKey="FIELD_DPR_PACKDEAL_DEALIND"/></div>
						<div class='field'>
							<mtl:select id="condition" key="isPackdealDate" prefixKey="PUB_WHETHER_" codeValues="Y,N"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" searchable="false"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_PACKDEALIND"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/>
						</div>
						<div class='field'>
						<%
							Object _distChannelCodes = condition.get( "distributionChannelCode" );
							String[] condDistributionChannelCodes;
							if( _distChannelCodes instanceof Object[] ) {
								condDistributionChannelCodes = new String[ ((Object[])_distChannelCodes).length ];
								int i = 0;
								for( Object o : (Object[])_distChannelCodes ) {
									condDistributionChannelCodes[i++] = (String)o;
								}
							} else {
								condDistributionChannelCodes = new String[1];
								condDistributionChannelCodes[0] = (String)_distChannelCodes;
							}
							for( Map<String, Object> channelMap : distributionChannels ) {
								String code = (String)channelMap.get( "distributionChannelCode" );
								String name = (String)channelMap.get( "distributionChannelName" );
								out.print( "<input type='checkbox' id='distributionChannelCode_" + code + "'" );
								out.print( " name='distributionChannelCode' class='mandatory' value='" + code + "'");
								out.print( " onclick='JavaScript: readConditionReq(\"SO;SG;SOLD\");'" );
								for( String channelCode : condDistributionChannelCodes ) {
									if( code.equals(channelCode) ) {
										out.print( " checked " );
									}
								}
								out.print( ">" );
								out.print( "<label for='distributionChannelCode_" + code + "'>" );
								out.print( "<span>" + name + "</span>" );
								out.print( "</label>" );
							}
						%>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_PARTY_SALESOFFICE_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
									listId="offices" listCodeKey="officeCode"
									listNameFormat="$S{[:officeCode;$S{] :officeName}}"
									modified="readConditionReq(\"SG;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="groupCode" descriptionKey="FIELD_DPR_PARTY_SALESGROUP_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									listId="groups" listCodeKey="groupCode" listNameFormat="$S{[:groupCode;$S{] :groupName}}"
									modified="readConditionReq(\"SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
						</div>
					</div>
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

			<script type='text/javaScript'>
				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;
					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRPackDealCfg?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );

				if( htmlpage.hasManageAuth() )
					listWriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_CHECK );

				if( property.containsKey("listmsg") )
					listWriter.print( out, property.getProperty("listmsg") );
				else
					listWriter.print( out );

			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<% if( listWriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<% if( sessionMng.isAuthorized("DPR", "DPRPackDealCfg.MNG_DEL") ) { %>
							<mtl:button type="delete"/>
						<% } %>
					<% } %>
					<mtl:button type="upload"/>
				<% } %>
				<% if( listWriter.containsData() ) { %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
