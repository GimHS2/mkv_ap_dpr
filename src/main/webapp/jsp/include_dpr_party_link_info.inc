<%--
	File Name:	include_dpr_party_info.inc
	Version:	2.2.0

	Description:
		script
			changeCodeValue( codeObj, nameObj );
			checkSearchCond();
			initSearchCond();
			resetSearchCond();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.0	create
--%>
		<h3><mtl:value id="_record" key="linkPartyCode"/> ( <mtl:value id="_record" key="linkPartyName"/> )</h3>
		<div class='info-table table-fixed' style='margin-top: 25px;'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:title key="linkPartyCode" descriptionKey="FIELD_DPR_PARTY_CODE" mandatory="true"/>
					</div>
					<div class='field-info'><mtl:value id="_record" key="linkPartyCode"/></div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_STATUS"/>
					</div>
					<div class='field-info'><mtl:valuef id="_record" format="${PUB_STATUS_@linkStatus}"/></div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_COUNTRY_COUNTRYKEY"/>
					</div>
					<div class='field-info'><mtl:valuef id="_record" format="$S{[:countryKey;$S{] :countryISOName}}"/></div>
				</div>
			</div>
		</div>
		<div class='info-table table-fixed'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:title key="linkPartyName" descriptionKey="FIELD_DPR_PARTY_SALES_PARTYNAME" mandatory="true"/>
					</div>
					<div class='field-info'><mtl:value id="_record" key="linkPartyName"/></div>
				</div>
			</div>
		</div>
		<div class='info-table table-fixed'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALES_CITY"/>
					</div>
					<div class='field-info'><mtl:value id="_record" key="city"/></div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALES_POSTALCODE"/>
					</div>
					<div class='field-info'><mtl:value id="_record" key="postalCode"/></div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_CONTACTUSER_NAME"/>
					</div>
					<div class='field-info'><mtl:value id="_record" key="contactUserName"/></div>
				</div>
			</div>
		</div>
		<div class='info-table table-fixed'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALES_EXTRAADDRESS1"/>
					</div>
					<div class='field-info'><mtl:value id="_record" key="extraAddress1"/></div>
				</div>
			</div>
		</div>
		<div class='info-table table-fixed'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALES_EXTRAADDRESS2"/>
					</div>
					<div class='field-info'><mtl:value id="_record" key="extraAddress2"/></div>
				</div>
			</div>
		</div>
		<div class='info-table table-fixed'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_DIVISION_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:value id="_record" key="linkDivisionCode"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_DISTRIBUTIONCHANNEL_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="$S{[:linkDistributionChannelCode;$S{] :distributionChannelName}}"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALESORGANIZATION_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="$S{[:linkOrganizationCode;$S{] :organizationName}}"/>
					</div>
				</div>
			</div>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_REGION_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="$S{[:linkRegionCode;$S{] :regionName}}"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_CUSTOMERGROUP_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="$S{[:linkCustomerGroupCode;$S{] :customerGroupName}}"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALES_DELIVERYPLANT"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="[$f{pure(linkDeliveryPlant)}]"/>
					</div>
				</div>
			</div>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALESDISTRICT_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="$S{[:linkDistrictCode;$S{] :districtName}}"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALESOFFICE_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="$S{[:linkOfficeCode;$S{] :officeName}}"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'>
						<mtl:message key="FIELD_DPR_PARTY_SALESGROUP_CODE"/>
					</div>
					<div class='field-info'>
						<mtl:valuef id="_record" format="$S{[:linkGroupCode;$S{] :groupName}}"/>
					</div>
				</div>
			</div>
		</div>
