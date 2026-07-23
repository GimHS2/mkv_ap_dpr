<%--
	File Name:	include_usr_user_info.inc
	Version:	2.2.1

	Description:
		script(using)
			userUserInfo();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.1	groupName �߰�
	GimHS		2007/11/30		2.2.0	create
--%>

<div class='search-table'>
	<div class='row'>
		<div class='cell'>
			<div class='field-title'><mtl:message key="FIELD_PARTYID"/></div>
			<div class='field-info'><mtl:valuef id="_record" format="$H{[:partyId;]} $H{partyName}"/></div>
		</div>
		<div class='cell'>
			<div class='field-title'><mtl:message key="FIELD_USERID"/></div>
			<div class='field-info'>
				<a href='JavaScript:userUserInfo("<mtl:value id="_record" key="uniqId"/>", "sub-content");'><mtl:valuef id="_record"
						format="$H{[:userId;]} $H{userName}"/></a>
			</div>
		</div>
		<div class='cell'>
			<div class='field-title'><mtl:message key="FIELD_USR_USERCLASS"/></div>
			<div class='field-info'><mtl:valuef id="_record" format="${USR_USER_CLASS_@userClass}"/></div>
		</div>
		<div class='cell'>
			<div class='field-title'><mtl:message key="FIELD_USR_GROUPNAME"/></div>
			<div class='field-info'><mtl:value id="_record" key="groupName"/></div>
		</div>
	</div>
	<div class='row'>
		<div class='cell'>
			<div class='field-title'><mtl:message key="FIELD_STATUS"/></div>
			<div class='field-info'><mtl:valuef id="_record" format="${USR_USER_STATUS_@status}"/></div>
		</div>
		<div class='cell'>
			<div class='field-title'><mtl:message key="FIELD_USR_SERVICESTARTDATE"/></div>
			<div class='field-info'><mtl:value id="_record" key="serviceStartDate"/></div>
		</div>
		<div class='cell'>
			<div class='field-title'><mtl:message key="FIELD_USR_SERVICEENDDATE"/></div>
			<div class='field-info'><mtl:value id="_record" key="serviceEndDate"/></div>
		</div>
		<div class='cell'></div>
	</div>
</div>
