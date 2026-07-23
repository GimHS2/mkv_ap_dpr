<%--
	File Name:	dpr_mastermng_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/04/30		2.2.0	create

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
	List<Map<String, Object>> categories = (List<Map<String, Object>>)pageContext.findAttribute( "categories" );
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );

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
%>

<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;


		function releaseField( obj ) {
			var fieldName = obj.name;
			if( !obj.value ) {
				var prefix = "frmCond.condition";
				var nameObj = eval( prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length - 4) + "Name" );
				nameObj.value = "";
				Field.release( nameObj );
			}
		}

		function toggleStateZero( el, toggleTgtName ) {
			if( el.checked == true) {
				var found = $(frmCond[toggleTgtName]['options']).filter(function(i) { return this.value == "0" } );
				if( found.length == 0 )
					$(frmCond[toggleTgtName]).append( new Option("0", "0") );
				frmCond[toggleTgtName].value = "0";
				frmCond[toggleTgtName].disabled = true;
			} else {
				if( frmCond[toggleTgtName].value == "0" )
					frmCond[toggleTgtName].value = "";
				frmCond[toggleTgtName].disabled = false;
			}
		}

		function pickUomCode( el ) {
			if( el && el.value ) {
				$(frmCond.uomCode).val( el.value );
			}
		}

		function changeManageType( mngtype ) {
			var url = replaceQueryValue(getLocationURL("url"), "mode", "list");
			url = replaceQueryValue(url, "mngtype", mngtype);

			<% if( "I".equals(htmlpage.getProperty().getProperty("mastertype")) ) { %>
				if( mngtype !== "itmsales" ) {
					url = replaceQueryValue(url, "distributionChannelCode", "");
				}
			<% } %>

			windowOpen( url );
		}

		<%
			com.irt.data.cols.ColumnList columnList = (com.irt.data.cols.ColumnList)request.getAttribute("columnList");
			String primaryCsv = "";
			if( columnList != null ) {
				String[] primaryFieldKeys = columnList.getPrimaryFieldKeys();
				if( primaryFieldKeys != null && primaryFieldKeys.length > 0 ) {
					primaryCsv = com.irt.util.StringUtil.strJoin(primaryFieldKeys, "," );
				}
			}

			// for masterCode like "regionCode;countryKey"
			if( "MasterDesc".equals(htmlpage.getProperty().getProperty("mngtypeName")) ) {
				primaryCsv += ",_masterCodePart2";
			}
		%>

		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "mngtype", "<mtl:value id="property" key="mngtype"/>" )
			requestDelete( url, frmMain.listcheckbox, "<%=primaryCsv%>" );
		}

		function modifyReq() {
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "mngtype", "<mtl:value id="property" key="mngtype"/>" );
			requestModify( url, frmMain.listcheckbox, "<%=primaryCsv%>" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "mngtype", "<mtl:value id="property" key="mngtype"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );

			if( frmCond.languageCode )
				url = replaceQueryValue( url, "languageCode", frmCond.languageCode.value );

			windowOpen( url, "sub-content" );
		}

		function downloadReq() {
// 			if( confirm("<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>") )
// 				windowOpen( replaceQueryValue( getLocationURL("url"), "mode", "down" ) );
			var url = getLocationURL("url");
			url = replaceQueryValue( url, "mngtype", "<mtl:value id="property" key="mngtype"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			requestDownload( url, frmMain.listcheckbox, "<%=primaryCsv%>" );
		}

		function uploadReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "mngtype", "<mtl:value id="property" key="mngtype"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );

			url = replaceQueryValue( url, "hiddenKeys", "mngtype" );
			windowOpen( url, "sub-content" );
		}
	</script>

	<script>
		var categories = new Array();

		function changeProductHierarchy( level, obj ) {
			var fieldName = obj.name;
			var pos = fieldName.indexOf( "_" );
			var prefixKey = fieldName.substring( 0, pos );

			for( var i = level + 1; i <= 6; i++ ) {
				var hierarchyObj = eval( "frmCond." + prefixKey + "_" + (i) );

				initProductHierarchy( eval(hierarchyObj) );
				Styles.changeDisplay( hierarchyObj, "" );
			}

			var value = obj.value;
			if( !obj.value ) return;

			var lowerObj = eval( "frmCond." + prefixKey + "_" + (level + 1) );
			makeProductHierachy( level + 1, obj.value, null, lowerObj );
			if( typeof lowerObj != "undefined" )
				Styles.changeDisplay( lowerObj, "Y" );
		}

		function getCodeLevel( code ) {
			if( code.length() == 1 )
				return 1;
			else if( code.length() == 2 )
				return 2;
			else if( code.length() == 6 )
				return 3;
			else if( code.length() == 10 )
				return 4;
			else if( code.length() == 14 )
				return 5;
			else if( code.length() == 18 )
				return 6;
		}

		function initProductHierarchy( obj ) {
			for( var i = 1; i < obj.length; i++ ) {
				obj.remove( i );
			}

			Styles.changeDisplay( obj, "" );
			Field.modified( obj );
		}

		function makeProductHierachy( level, parentHierarchyCode, hierarchyCode, obj ) {
			if( !obj || typeof obj == "undefined" ) {
				obj = eval( "frmCond.productHierarchyCode_" + level );
				if( !obj || typeof obj == "undefined" ) return;
			}

			var idx = 0;
			while( true ) {
				if( categories == null || idx >= categories.length ) break;

				var category = categories[ idx++ ];
				var classCode = category[0];
				if( classCode == level ) {
					var _parentHierarchyCode = category[1];
					if( classCode == 1 || parentHierarchyCode == _parentHierarchyCode ) {
						var element = document.createElement( "OPTION" );
						element.value = category[2];
						element.text = category[3];
						if( hierarchyCode == category[2] )
							element.selected = "true";

						obj.add( element );
					}
				} else if( classCode > level ) {
					break;
				}
			}
		}

		function initSearchCondHierarchy() {
			var parameterHierarchyCodes = new Array();
			<%
					int idx = 0;
					if( categories != null ) {
						for( Map<String, Object> map : categories ) {
							String classCode = (String)map.get( "classCode" );
							String code = (String)map.get( "code" );
							String parentCode = (String)map.get( "parentCode" );
							String name = (String)map.get( "name" );

							out.print( "categories["+ idx++ + "] = new Array(\"" + classCode + "\", \"" + parentCode + "\""
									+ ", \"" + code + "\", \"" + name + "\" );" );

							out.println();
						}
					}

					idx = 0;
					for( int i = 0; i < 6; i++ ) {
						String key = "productHierarchyCode_" + (i+1);
						if( condition.containsKey(key) )
							out.print( "parameterHierarchyCodes[" + i + "] = \"" + HtmlUtility.toScriptString(condition.get(key)) + "\";" );
						else
							out.print( "parameterHierarchyCodes[" + i + "] = \"\";" );

						out.println();
					}
			%>

			var parentHierarchyCode = null;
			for( var i = 0; i < 6; i++ ) {
				var level = i + 1;

				var hierarchyObj = eval("frmCond.productHierarchyCode_" + (level) );
				if( i == 0 ) {
					makeProductHierachy( level, parentHierarchyCode, parameterHierarchyCodes[i], hierarchyObj );
					changeProductHierarchy( level, hierarchyObj );
				} else if( parameterHierarchyCodes[i] ) {
					for( var j = 0; j < hierarchyObj.length; j++ ) {
						if( hierarchyObj[j].value == parameterHierarchyCodes[i] ) {
							hierarchyObj[j].selected = "true";
							hierarchyObj.value = hierarchyObj[j].value;

							break;
						}
					}

					changeProductHierarchy( level, hierarchyObj );
				}

				parentHierarchyCode = parameterHierarchyCodes[i];
			}
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:hidden id="property" key="mngtype"/>
		<input type='hidden' name='mode' value='list'/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div id='messagebar'></div>
			<mtl:ifvalue id="property" key="mngtypeName" value="MasterDesc">
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="languageCode" descriptionKey="FIELD_DPR_LANGUAGECODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="languageCode" listId="languages" listNameFormat="$S{[:code;$S{] :name}}" searchable="false"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="masterType" descriptionKey="FIELD_DPR_ITEM_MASTER_TYPE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="masterType" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_MASTERTYPE"
									prefixKey="DPR_MASTER_TYPE_" codeValues="CG,RG,SD,DC,SO,SF,SG,BP,MB,BR,VA,PU,PC"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="masterCode" descriptionKey="FIELD_DPR_MASTER_CODE"/></div>
						<div class='field'>
							<mtl:text id="condition" key="masterCode"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="masterName" descriptionKey="FIELD_DPR_MASTER_NAME"/></div>
						<div class='field'>
							<mtl:text id="condition" key="masterName"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ProductCategory">
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="displayLanguage" descriptionKey="jsp.dpr_upload_input.FIELD_DPR_MASTER_LANGUAGECODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="displayLanguage" listId="languages" listNameFormat="$S{[:code;$S{] :name}}" searchable="false"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="pcateCode" descriptionKey="FIELD_DPR_MASTER_CODE"/></div>
						<div class='field'>
							<mtl:text id="condition" key="pcateCode"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="pcateName" descriptionKey="FIELD_DPR_MASTER_NAME"/></div>
						<div class='field'>
							<mtl:text id="condition" key="pcateName"/>
						</div>
					</div>
					<div class='cell'></div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mastertype" value="I">
			<div class='search-table'>
				<div class='row'>
					<mtl:ifvalue id="property" key="mngtypeName" value="ItemMasterDesc">
					<div class='cell'>
						<div class='field-title'><mtl:title key="languageCode" descriptionKey="FIELD_DPR_LANGUAGECODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="languageCode" listId="languages" listNameFormat="$S{[:code;$S{] :name}}" searchable="false"/>
						</div>
					</div>
					</mtl:ifvalue>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'>
							<mtl:text id="condition" key="itemCode"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="conditionItemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
						<div class='field'>
							<mtl:text id="condition" key="conditionItemName"/>
						</div>
					</div>
					<div class='cell'></div>
					<mtl:ifvalue id="property" key="mngtypeName" notValue="ItemMasterDesc">
					<div class='cell'></div>
					</mtl:ifvalue>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ItemMaster">
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemType" descriptionKey="FIELD_DPR_ITEM_MASTER_TYPE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="itemType" listNameFormat="$S{:itemType;}" listCodeKey="itemType"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_MATERIALTYPE"
									className="com.irt.dpr.ItemMaster" condition="distinctingValue=Y"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="baseUnitofMeasure" descriptionKey="FIELD_DPR_ITEM_MASTER_BASEUOM"/></div>
						<div class='field'>
							<mtl:select id="condition" key="baseUnitofMeasure" listNameFormat="$S{:baseUnitofMeasure;}" listCodeKey="baseUnitofMeasure"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_BASEUOM"
									className="com.irt.dpr.ItemMaster" condition="distinctingValue=Y"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="salesUnit" descriptionKey="FIELD_DPR_ITEM_MASTER_SALES_UNIT"/></div>
						<div class='field'>
							<mtl:select id="condition" key="salesUnit" listNameFormat="$S{:salesUnit;}" listCodeKey="salesUnit"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SALESUNIT"
									className="com.irt.dpr.ItemMaster" condition="distinctingValue=Y"/>
						</div>
					</div>
					<div class='cell'></div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ItemMasterUom">
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="uomCode" descriptionKey="FIELD_DPR_ITEMUOM_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="uomCode" listId="uoms"
									listNameFormat="$S{[:uomCode;$S{] :uomName}}" listCodeKey="uomCode"
									hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_DPR_ITEMUOM_CODE" width="auto"></mtl:select>

							<mtl:radio id="request" key="uomCodePop" mandatory="false" codeValues="CSE,PC,DZ"
								modified="JavaScript:pickUomCode(this);"/>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mngtypeName" value="ItemMasterSales">
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC\");"/>
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
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
			</div>
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="mastertype" value="P">
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/></div>
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
					<div class='cell'>
						<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_PARTY_SALESOFFICE_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
									listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
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
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
						</div>
					</div>
					<div class='cell'>
						<mtl:ifvalue id="property" key="mngtypeName" value="PartyFunction">
						<div class='field-title'><mtl:message key="jsp.dpr_mastermng_input.MSG_DPR_PARTY_FUNCTION_LINK_PARTY_CODE"/></div>
						<div class='field'>
							<mtl:text id="condition" key="linkPartyCode"/>
						</div>
						</mtl:ifvalue>
					</div>
					<div class='cell'>
						<mtl:ifvalue id="property" key="mngtypeName" value="PartyFunction">
						<div class='field-title'><mtl:message key="jsp.dpr_mastermng_input.MSG_DPR_PARTY_FUNCTION_LINK_PARTY_NAME"/></div>
						<div class='field'>
							<mtl:text id="condition" key="linkPartyName"/>
						</div>
						</mtl:ifvalue>
					</div>
					<div class='cell'></div>
				</div>
			</div>
			</mtl:ifvalue>
<%--
		<mtl:ifvalue id="property" key="mastertype" value="I">
			<mtl:ifvalue id="condition" key="distributionChannelCode" notValue="">
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/></div>
						<div class='field'>
							<select name='productHierarchyCode_1' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 1, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL1"/></option>
							</select>
							<span id='content_producthierarchy_level2'>
								<select name='productHierarchyCode_2' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 2, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL2"/></option>
								</select>
							</span>

							<span id='content_producthierarchy_level3'>
								<select name='productHierarchyCode_3' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 3, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL3"/></option>
								</select>
							</span>

							<span id='content_producthierarchy_level4'>
								<select name='productHierarchyCode_4' class='content_o '
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 4, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL4"/></option>
								</select>
							</span>

							<span id='content_producthierarchy_level5'>
								<select name='productHierarchyCode_5' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 5, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL5"/></option>
								</select>
							</span>

							<span id='content_producthierarchy_level6'>
								<select name='productHierarchyCode_6' class='content_o'
									onChange='JavaScript:Field.modified(this); JavaScript:changeProductHierarchy( 6, this );'>
								<option value=''><mtl:message key="MSG_PRODUCT_HIERARCHY_LEVEL6"/></option>
								</select>
							</span>
						</div>
					</div>
				</div>
			</div>
			</mtl:ifvalue>
		</mtl:ifvalue>
 --%>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				function checkSearchCond() {
					if( frmCond.organizationCode )
						if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( frmCond.distributionChannelCode )
						if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;
					disableBlankInput( frmCond, true );
				}

				function readConditionReq( type, hasMngtype ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=rtp";
					url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
					url = replaceQueryValue( url, "mngtype", "<mtl:value id="property" key="mngtype"/>" );
					readPartyAttributeReq( url, type );
				}

				function initSearchCond() {
					<% if( "I".equals(htmlpage.getProperty().getProperty("mastertype")) ) { %>
						if( frmCond.distributionChannelCode && frmCond.distributionChannelCode.value ) {
							initSearchCondHierarchy();
						}
					<% } %>
				}
				attachWindowEvent( "load", initSearchCond );
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<div class='list-menu'>
				<mtl:select id="request" key="mngtype" defaultValue="mstdesc" hasBlank="false"
						listId="mngtypes" listCodeKey="code" listNameFormat="$S{name}"
						modified="JavaScript:changeManageType(this.value);" searchable="false" width="auto"/>
			</div>

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
						<mtl:ifvalue id="property" key="mngtypeName" notValue="PartyFunction">
							<mtl:ifvalue id="property" key="mngtypeName" notValue="ProductCategory">
								<mtl:button type="modify"/>
							</mtl:ifvalue>
						</mtl:ifvalue>
						<mtl:button type="delete"/>
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
