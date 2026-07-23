<%--
	File Name:	include_dpr_party_cond.inc
	Version:	2.2.0

	Description:
		script
			changeCodeValue( codeObj, nameObj );
			checkSearchCond();
			initSearchCond();
			resetSearchCond();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/04/30		2.2.1	partyName -> customerName 으로 변경
	hankalam	2019/06/28		2.2.0	create
--%>

	<script type='text/javaScript'>
		function checkSearchCond() {
			<% if( useDivision ) { %>
				if( !Field.checkMandatory(frmCond.divisionCode) ) return false;
			<% } %>

			if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;
			if( !Field.checkMandatory(frmCond.organizationCode) ) return false;

			return submitInput();
		}

		function readConditionReq( type ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=rtp";
			readPartyAttributeReq( url, type );
		}
	</script>

	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<mtl:hidden id="request" key="mode"/>
			<mtl:hidden id='request' key='slname'/>
			<mtl:hidden id="condition" key="countryCode"/>
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
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
							Object _distChannelCodes = conditionMap.get( "distributionChannelCode" );
							String[] condDistributionChannelCodes;
							if( _distChannelCodes instanceof String[] ) {
								condDistributionChannelCodes = (String[])_distChannelCodes;
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
									listId="offices" listCodeKey="code" listNameFormat="$S{[:code;$S{] :name}}"
									modified="readConditionReq(\"SG;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="groupCode" descriptionKey="FIELD_DPR_PARTY_SALESGROUP_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									listId="groups" listCodeKey="code" listNameFormat="$S{[:code;$S{] :name}}"
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
					<% if( com.irt.dpr.Country.isFeature(organizationCode, "useJdms") ) { %>
						<mtl:select id="condition" key="JDMSIndicator" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
								hasBlank="true" prefixKey="MSG_COND_JDMS_PARTY_" codeValues="Y,N"
								hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_JDMSIND"/>
					<% } %>
					</div>
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
