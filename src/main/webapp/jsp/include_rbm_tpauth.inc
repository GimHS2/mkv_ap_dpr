<%--
	File Name:	include_rbm_tpauth.inc
	Version:	2.2.0

	Description:
		<head>¿¡ Ãß°¡

		script
			AUTHLEVLE_*
			AUTHTYPE*
			getAuthLevel( authValue, authType );

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	create
--%>

<script type='text/javascript'>
	var AUTHLEVEL_MANAGE		= 2;
	var AUTHLEVEL_VIEW			= 1;
	var AUTHLEVEL_NOAUTH		= 0;

	var AUTHTYPE1				= 0x10;
	var AUTHTYPE1TP				= 0x11;
	var AUTHTYPE1OPERDAY		= 0x12;
	var AUTHTYPE1ITEM			= 0x13;

	var AUTHTYPE2				= 0x20;
	var AUTHTYPE2SCENARIO		= 0x21;
	var AUTHTYPE2ITEM			= 0x22;
	var AUTHTYPE2SFC			= 0x23;
	var AUTHTYPE2OFC			= 0x24;
	var AUTHTYPE2EVENT			= 0x25;
	var AUTHTYPE2INVENTORY		= 0x26;
	var AUTHTYPE2ORDER			= 0x27;
	var AUTHTYPE2ORDERMANUAL	= 0x28;
	var AUTHTYPE2KPI			= 0x29;

	function getAuthLevel( authValue, authType ) {
		<% if( sessionMng.isAdminUser() ) { %>
			return AUTHLEVEL_MANAGE;
		<% } else { %>
			var index = ( authType & 0x0F );
			if( authValue == "" )
				return AUTHLEVEL_NOAUTH;
			else if( index == 0 )
				return AUTHLEVEL_VIEW;
			else if( authValue.length < index )
				return AUTHLEVEL_NOAUTH;
			else
				return "XVM".indexOf( authValue.charAt(index - 1) );
		<% } %>
	}
</script>
