<%--
	File Name:	dpr_moqitemcfg_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/06/30		2.2.1	alMoqMonth 항목 mandatory = false 로 변경
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
	List<Map<String, Object>> items = (List<Map<String, Object>>)pageContext.findAttribute( "items" );
	org.json.simple.JSONArray itemArray = null;
	if( items != null ) {
		itemArray = com.irt.util.Utility.convertListToJson( items );
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<%
	boolean imod = "imod".equals(htmlpage.getMode());
	%>

	<script type='text/javascript'>
		$(function() {
			var items = <%= itemArray %>;
			var codeItems = $.map( items, function(item) {
				return {
					label: item.itemCode + "|" + item.itemName,
					itemCode: item.itemCode,
					itemName: item.itemName
				};
			});

			if( $("input[name=itemCode]").length ) {
				$("input[name=itemCode]").autocomplete( {
					minLength: 2,
					source: codeItems,
					focus: function( event,ui ) { return false; },
					select: function( event, ui ) {
						$("input[name=itemCode]").val( ui.item.itemCode );
						return false;
					},
					position: { my : "left bottom", at: "left top" }
				}).autocomplete("instance")._renderItem=function( ul, item ) {
					var disp = "<div>("+ item.itemCode + ") " + item.itemName+ "</div>"
					return $("<li>").append( disp ).appendTo( ul );
				};
			}
		});

		function changeGroupList( value ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=cond&datatype=SG";
			if( value == "" )
				value = "null";
			else
				url = replaceQueryValue( url, "officeCode", encodeURIComponent(value) );

			windowOpen( url, "clsName" );
		}

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRMoqItemCfg?mode=imod";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "officeCode", "<mtl:value id="record" key="officeCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "groupCode", "<mtl:value id="record" key="groupCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="record" key="partyCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "itemCode", "<mtl:value id="record" key="itemCode" encodeScript="true"/>" );

			windowSelfOpen( url );
		}

		function selectDPRPartyReq( naming ) {
			var query = "";
			if( frmMain.distributionChannelCode && frmMain.distributionChannelCode.value ) {
				query += ( "&distributionChannelCode="+ frmMain.distributionChannelCode.value );
			}
			if( frmMain.officeCode && frmMain.officeCode.value && frmMain.officeCode.value != "0" ) {
				query += ( "&officeCode="+ frmMain.officeCode.value );
			}
			if( frmMain.groupCode && frmMain.groupCode.value && frmMain.groupCode.value != "0" ) {
				query += ( "&groupCode="+ frmMain.groupCode.value );
			}
			if( naming ) {
				query += ( "&partyCode="+ frmMain.partyCode.value );
				return _selectDPRParty( "party", "D", ""+query, null, null, naming );
			} else {
				_selectDPRParty( "party", "D", ""+query, null, null, naming );
			}
		}

		function _selectDPRItem( slname, namecls, attr, value, winname , naming) {
			var url = classURL +"DPRItem?slname="+ slname;
			url += ( namecls ? "&namecls="+ namecls : "" );
			url += ( attr ? "&attr="+ attr : "" );


			var query = "";
			if( frmMain.organizationCode && frmMain.organizationCode.value && frmMain.organizationCode.value != "0" ) {
				query += ( "&organizationCode="+ frmMain.organizationCode.value );
				url = replaceQueryValue(url, "oitmHierIndex", "1");
				url = replaceQueryValue(url, "btype", "ord");
			}
			if( frmMain.distributionChannelCode && frmMain.distributionChannelCode.value && frmMain.distributionChannelCode.value != "0" ) {
				query += ( "&distributionChannelCode="+ frmMain.distributionChannelCode.value );
				url = replaceQueryValue(url, "oitmHierIndex", "2");
				url = replaceQueryValue(url, "btype", "ord");
			}
			if( frmMain.officeCode && frmMain.officeCode.value && frmMain.officeCode.value != "0" ) {
				query += ( "&officeCode="+ frmMain.officeCode.value );
				url = replaceQueryValue(url, "btype", "ord");
				url = replaceQueryValue(url, "oitmHierIndex", "3");
			}
			if( frmMain.groupCode && frmMain.groupCode.value && frmMain.groupCode.value != "0" ) {
				query += ( "&groupCode="+ frmMain.groupCode.value );
				url = replaceQueryValue(url, "btype", "ord");
				url = replaceQueryValue(url, "oitmHierIndex", "4");
			}
			url += ""+ query;

			if( frmMain.partyCode && frmMain.partyCode.value && frmMain.partyCode.value != "0" ) {
				url += ( "&btype=ord" );
				url += ( "&partyCode="+ frmMain.partyCode.value );
				url = replaceQueryValue(url, "oitmHierIndex", "5");
			} else {
				if( getQueryValue(url, "btype") != "ord" )
					url += ( "&btype=itm" );
			}

			if( value && naming )
				url += "&code=" + value;

			if( naming ) {
				url += "&itemCode="+ frmMain.itemCode.value;
				windowOpen( url +"&mode=name", winname ? winname : "clsName" );
			} else
				windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

			return false;
		}

		function selectDPRItemReq( naming ) {
			if( naming ) {
				return _selectDPRItem( "item", "D", "", null, null, naming );
			} else {
				_selectDPRItem( "item", "D", "", null, null, naming );
			}
		}

	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_pub_calendar.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>

			<mtl:contentGroup groupId="main" type="content">
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
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD\");"/>
							<% } %>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
								<mtl:hidden id="record" key="distributionChannelCode"/>
							<% } else { %>
								<mtl:select id="record" key="distributionChannelCode" mandatory="false" listId="distributionChannels" listCodeKey="distributionChannelCode"
										hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO;SG;SOLD\");"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="officeCode" format="$S{[:officeCode;]$S{ :officeName}}"/>
								<mtl:hidden id="record" key="officeCode"/>
							<% } else { %>
								<mtl:select id="record" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="false"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
										listId="offices" listCodeKey="code" listNameFormat="$S{[:code;$S{] :name}}"
										modified="readConditionReq(\"SG;SOLD\");"/>
							<% } %>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="groupCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="groupCode" format="$S{[:groupCode;]$S{ :groupName}}"/>
								<mtl:hidden id="record" key="groupCode"/>
							<% } else { %>
								<mtl:select id="record" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="false"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
										listId="groups" listCodeKey="code" listNameFormat="$S{[:code;$S{] :name}}"
										modified="readConditionReq(\"SOLD\");"/>
							<% } %>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode" mandatory="false"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="partyCode" format="$S{[:partyCode;]$S{ :partyName}}"/>
								<mtl:hidden id="record" key="partyCode"/>
							<% } else { %>
								<mtl:select id="record" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="false"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode"/></div>
							<div class='field'><mtl:text id="record" key="itemCode"/></div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
						<mtl:ifvalue key="dateValueType" id="request" notValue="M">
							<mtl:hidden id="record" key="pdMoqDay"/>
							<div class='field-title'><mtl:title key="alMoqDay"/></div>
							<div class='field'><mtl:text id="record" key="alMoqDay"/></div>
						</mtl:ifvalue>
						</div>
						<div class='cell'>
						<mtl:ifvalue key="dateValueType" id="request" notValue="D">
							<mtl:hidden id="record" key="pdMoqMonth"/>
							<div class='field-title'><mtl:title key="alMoqMonth"/></div>
							<div class='field'><mtl:text id="record" key="alMoqMonth"/></div>
						</mtl:ifvalue>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
					<% } else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
						<mtl:button type="modify"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>

					if( frmMain.partyCode && frmMain.partyCode.value ) {
						if( !Field.checkMandatory(frmMain.distributionChannelCode) ) return false;
					}
					if( frmMain.groupCode && frmMain.groupCode.value ) {
						if( !Field.checkMandatory(frmMain.distributionChannelCode) ) return false;
					}
					if( frmMain.officeCode && frmMain.officeCode.value ) {
						if( !Field.checkMandatory(frmMain.distributionChannelCode) ) return false;
					}

					if( (frmMain.alMoqDay.value > frmMain.alMoqMonth.value) ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_MOQITEMVALUE_SHOULD_DAY_LT_MONTH" encodeScript="true"/>" } );
						return false;
					}

					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRMoqItemCfg?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>

