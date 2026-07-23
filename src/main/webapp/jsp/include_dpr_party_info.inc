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

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="partyCode"/></td>
			<td class='content2'><mtl:value id="_record" key="partyCode"/></td>
			<td class='subject'><mtl:title key="status"/></td>
			<td class='content2'><mtl:valuef id="_record" format="${PUB_STATUS_@status}"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="partyName"/></td>
			<td class='content1'><mtl:value id="_record" key="partyName"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:message key="FIELD_DPR_COUNTRY_COUNTRYKEY"/></td>
			<td class='content1'><mtl:valuef id="_record" format="$S{[:countryKey;$S{] :countryISOName}}"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="postalCode"/></td>
			<td class='content1'><mtl:value id="_record" key="postalCode"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="city"/></td>
			<td class='content1'><mtl:value id="_record" key="city"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="extraAddress1"/></td>
			<td class='content1'><mtl:value id="_record" key="extraAddress1"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="extraAddress2"/></td>
			<td class='content1'><mtl:value id="_record" key="extraValue2"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:message key="FIELD_DPR_PARTY_CONTACTUSER_NAME"/></td>
			<td class='content1'><mtl:value id="_record" key="contactUserName"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="divisionCode" mandatory="false"/></td>
			<td class='content3'><mtl:valuef id="_record" format="$S{[:divisionCode;$S{] :divisionName}}"/></td>
			<td class='subject'><mtl:title key="distributionChannelCode" mandatory="false"/></td>
			<td class='content3_c'><mtl:valuef id="_record" format="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"/></td>
			<td class='subject'><mtl:title key="organizationCode" mandatory="false"/></td>
			<td class='content3'><mtl:valuef id="_record" format="$S{[:organizationCode;$S{] :organizationName}}"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="regionCode" mandatory="false"/></td>
			<td class='content3'><mtl:valuef id="_record" format="$S{[:regionCode;$S{] :regionName}}"/></td>
			<td class='subject'><mtl:title key="customerGroupCode" mandatory="false"/></td>
			<td class='content3_2'><mtl:valuef id="_record" format="$S{[:customerGroupCode;$S{] :customerGroupName}}"/></td>
		</tr>
		</table>

		<table class='line_content' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='subject'><mtl:title key="districtCode" mandatory="false"/></td>
			<td class='content3'><mtl:valuef id="_record" format="$S{[:districtCode;$S{] :districtName}}"/></td>
			<td class='subject'><mtl:title key="officeCode" mandatory="false"/></td>
			<td class='content3_c'><mtl:valuef id="_record" format="$S{[:officeCode;$S{] :officeName}}"/></td>
			<td class='subject'><mtl:title key="groupCode" mandatory="false"/></td>
			<td class='content3'><mtl:valuef id="_record" format="$S{[:groupCode;$S{] :groupName}}"/></td>
		</tr>
		</table>
