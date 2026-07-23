<%--
	File Name:	include_pub_passwd.inc
	Version:	2.2.0

	Description:
		<head>�� �߰�

		script
			checkPassword( password, password_old, ... );

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"�� "<script type='text/javascript'>"�� ����
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) ó��
	stghr12		2006/02/28		2.0.0	create
--%>

<script type='text/javascript'>
	function checkPassword( password, password_old ) {
		if( password.length < 6 ) {
			customPopup.alert( { "header" : "<mtl:message key="jsp.include_pub_passwd.MSG_TOO_SHORT" encodeScript="true"/>" } );
			return false;
		} else if( password_old == password ) {
			customPopup.alert( { "header" : "<mtl:message key="jsp.include_pub_passwd.MSG_NEED_CHANGE" encodeScript="true"/>" } );
			return false;
		}
		for( var i = 2; i < checkPassword.arguments.length; i++ ) {
			if( password.indexOf(checkPassword.arguments[i]) >= 0 || checkPassword.arguments[i].indexOf(password) >= 0 ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.include_pub_passwd.MSG_TOO_EASY" encodeScript="true"/>" } );
				return false;
			}
		}

		var cnt = 0;
		var bool_a = false;
		var bool_n = false;
		for( var i = 0; i < password.length; i++ ) {
			if( password.charAt(i) >= '0' && password.charAt(i) <= '9' )
				bool_n = true;
			else
				bool_a = true;
			if( i == 0 || password.charAt(i) != password.charAt(i-1) )
				cnt = 0;
			else if( ++cnt > 2 ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.include_pub_passwd.MSG_TOO_MANY_CHAR_REPEAT" encodeScript="true"/>" } );
				return false;
			}
		}
		if( !bool_n || !bool_a ) {
			customPopup.alert( { "header" : "<mtl:message key="jsp.include_pub_passwd.MSG_NEED_CHAR_NUMBER" encodeScript="true"/>" } );
			return false;
		}

		return true;
	}
</script>
