<%--
	File Name:	include_dpr_passwd.inc
	Version:	2.2.0

	Description:
		<head>�� �߰�

		script
			checkPassword( password, password_old, ... );

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.0	create
--%>

<script type='text/javascript'>
	function checkPassword( password, password_old ) {
		var bitmap = 0;
        for( var i = 0; i < password.length; i++ ) {
            var ch = password.charAt(i);
            if( "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(ch) >= 0 )
                bitmap |= 0x01;
            else if( "abcdefghijklmnopqrstuvwxyz".indexOf(ch) >= 0 )
                bitmap |= 0x02;
            else if( "123456789".indexOf(ch) >= 0 )
                bitmap |= 0x04;
            else if( "{}[],.<>;:'\"?/|\\`~!@#$%^&*()_-+=".indexOf(ch) >= 0 )
                bitmap |= 0x08;
        }

		var complexity = 0;
		if( (bitmap & 0x01) > 0 ) complexity++;
		if( (bitmap & 0x02) > 0 ) complexity++;
		if( (bitmap & 0x04) > 0 ) complexity++;
		if( (bitmap & 0x08) > 0 ) complexity++;

		if( password.length < 6 ) {
			customPopup.alert( { "header" : "<mtl:message key="ERR_PASSWORD_MINIMUM_LENGTH" encodeScript="true"/>" } );
			return false;
		} else if( password.length < 8 ) {
			if( complexity < 3 ) {
				customPopup.alert( { "header" : "<mtl:message key="ERR_PASSWORD_MINLENGTH_COMPLEXITY_6" encodeScript="true"/>" } );
				return false;
			}
		} else if( password_old == password ) {
			customPopup.alert( { "header" :  "<mtl:message key="jsp.include_pub_passwd.MSG_NEED_CHANGE" encodeScript="true"/>" } );
			return false;
		} else {
			if( complexity < 2 ) {
				customPopup.alert( { "header" : "<mtl:message key="ERR_PASSWORD_MINLENGTH_COMPLEXITY_8" encodeScript="true"/>" } );
				return false;
			}
		}

		for( var i = 2; i < checkPassword.arguments.length; i++ ) {
			if( password.indexOf(checkPassword.arguments[i]) >= 0 || checkPassword.arguments[i].indexOf(password) >= 0 ) {
				customPopup.alert( { "header" : "<mtl:message key="ERR_PASSWORD_INCLUDING_USERINFO" encodeScript="true"/>" } );
				return false;
			}
		}

		var cnt = 0;
		for( var i = 0; i < password.length; i++ ) {
			if( i == 0 || password.charAt(i) != password.charAt(i-1) )
				cnt = 0;
			else if( ++cnt > 2 ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.include_pub_passwd.MSG_TOO_MANY_CHAR_REPEAT" encodeScript="true"/>" } );
				return false;
			}
		}

		return true;
	}
</script>
