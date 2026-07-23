<%--
	File Name:	include_usr_party_info.inc
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	FIELD_USR_PARTYID -> FIELD_PARTYID
	stghr12		2007/04/30		2.1.0	ediId 삭제, </td> 추가
	stghr12		2006/02/28		2.0.0	create
--%>

	<table class='line_content' cellspacing='0' cellpadding='0'>
	<tr>
		<td class='subject'><mtl:message key="FIELD_PARTYID"/></td>
		<td class='content2'><mtl:valuef id="_record" format="$H{[:partyId;]} $H{partyName}"/></td>
		<td class='subject'><mtl:message key="FIELD_USR_PARTYCLASS"/></td>
		<td class='content2'><mtl:valuef id="_record" format="${USR_PARTY_CLASS_@partyClass}"/></td>
	</tr>
	</table>

	<table class='line_content' cellspacing='0' cellpadding='0'>
	<tr>
		<td class='subject'><mtl:message key="FIELD_USR_PARTYGLN"/></td>
		<td class='content2'><mtl:value id="_record" key="partyGln"/></td>
		<td class='subject'><mtl:message key="FIELD_STATUS"/></td>
		<td class='content2'><mtl:valuef id="_record" format="${USR_PARTY_STATUS_@status}"/></td>
	</tr>
	</table>

	<table class='line_content' cellspacing='0' cellpadding='0'>
	<tr>
		<td class='subject'><mtl:message key="FIELD_USR_SERVICESTARTDATE"/></td>
		<td class='content2'><mtl:value id="_record" key="serviceStartDate"/></td>
		<td class='subject'><mtl:message key="FIELD_USR_SERVICEENDDATE"/></td>
		<td class='content2'><mtl:value id="_record" key="serviceEndDate"/></td>
	</tr>
	</table>
