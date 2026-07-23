<%--
	File Name:	include_pub_select.inc
	Version:	2.2.0

	Description:
		<head>�� �߰�

		request.getParameter()
			"slidx"
			"slname"
			"closewin"
		script
			Select
			Select.selectFunctionObj					// �������� �� �����ϴ� function �⺻������ selectReq()
			Select.reset( checkObj );
			Select.setElementNames( ... );
			Select.setSelectName( selectName );
			Select.setValue( selectedValues, functionName, elementNames );
			selectOne( idx );
			selectReq();
		script(using)
			windowClose();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"�� "<script type='text/javascript'>"�� ����
	stghr12		2007/04/30		2.1.1	"noclosewin" parameter ó�� �߰�
										selectIndex ����ϴ� �κ� �Ϻ� ����
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) ó��
										Select.setValue(): frmMain ������ frmCond �˻�
	stghr12		2006/02/28		2.0.0	create
--%>

<% {
	String _selectName = request.getParameter( "slname" );
	String _selectNameC = _selectName;
	if( _selectName == null || _selectName.length() == 0 )
		_selectName = _selectNameC = "";
	else if( _selectNameC != null && _selectNameC.length() > 0 )
		_selectNameC = Character.toUpperCase(_selectNameC.charAt(0)) + _selectNameC.substring( 1 );

	int _selectIndex = -1;
	try {
		_selectIndex = Integer.parseInt( request.getParameter("slidx") );
	} catch( NumberFormatException numEx ) {}
%>

<script type='text/javascript'>
	function Select() {}

	Select.selectName = "<%= HtmlUtility.toScriptString(_selectName) %>";
	Select.capitalizedSelectName = "<%= HtmlUtility.toScriptString(_selectNameC) %>";
	Select.elementNames = null;
	Select.selectFunctionObj = null;

	Select.reset = function( checkObj ) {
		if( checkObj.length ) {
			for( var i = 0; i < checkObj.length; i++ )
				if( checkObj[i].checked )
					checkObj[i].click();
		} else if( checkObj.checked )
			checkObj.click();
	}

	Select.setElementNames = function() {
		Select.elementNames = null;
		var args = Select.setElementNames.arguments;
		if( args.length >= 1 ) {
			Select.elementNames = new Array();

			var selectName = Select.selectName;
			if( selectName.length == 0 ) {
				for( var i = 0; i < args.length; i++ )
					Select.elementNames[i] = args[i];
			} else {
				if( selectName.charAt(selectName.length - 1) == "_" ) {
					for( var i = 0; i < args.length; i++ )
						if( args[i] != null ) Select.elementNames[i] = selectName + args[i];
				} else {
					for( var i = 0; i < args.length; i++ )
						if( args[i] != null ) Select.elementNames[i] = selectName + args[i].substring(0, 1).toUpperCase() + args[i].substring(1);
				}
			}
		}
	}

	Select.setSelectName = function( selectName ) {
		Select.selectName = selectName;
		if( selectName.length == 0 )
			Select.capitalizedSelectName = selectName;
		else
			Select.capitalizedSelectName = selectName.substring(0, 1).toUpperCase() + selectName.substring(1);
	}

	Select.setValue = function( selectedValues, functionName, elementNames ) {
		if( Select.setValue.arguments.length <= 1 ) {
			if( Select.capitalizedSelectName ) functionName = "set"+ Select.capitalizedSelectName;
			return Select.setValue( selectedValues, functionName, Select.elementNames );
		}
		if( !selectedValues || selectedValues.length == 0 ) return false;

		var windowObj = window.opener;
		var windowName = "window.opener";
		if( !windowObj ) {
			windowObj = window.parent;
			windowName = "window.parent";
		}

		if( functionName ) {
			var functionObj = eval( windowName +"."+ functionName );
			if( functionObj ) {
				if( <%= _selectIndex %> < 0 )
					functionObj( selectedValues );
				else
					functionObj( selectedValues, <%= _selectIndex %> );
				return true;
			}
		}

		var formName;
		if( windowObj.frmCond )
			formName = "frmCond";
		else if( windowObj.frmMain )
			formName = "frmMain";
		else if( windowObj.document.forms.length > 0 )
			formName = windowObj.document.forms[0].name;
		if( !formName ) return false;
		if( !elementNames ) return false;

		var elementObjs = new Array( elementNames.length );
		for( var i = 0; i < elementNames.length; i++ )
			elementObjs[i] = ( elementNames[i] ? eval(windowName +"."+ formName +"."+ elementNames[i]) : null );

		var selectIndex = <%= _selectIndex %>;
		var values = selectedValues[0].split( ";", elementObjs.length );
		for( var v = 0; v < values.length; v++ ) {
			var elementObj;
			if( elementObj = elementObjs[v] ) {
				if( selectIndex == 0 ) {
					if( elementObjs[v][selectIndex] ) elementObj = elementObjs[v][selectIndex];
				} else if( selectIndex > 0 )
					elementObj = elementObjs[v][selectIndex];

				if( elementObj ) {
					elementObj.value = values[v];
					if( elementObj.onchange ) elementObj.onchange();
				}
			}
		}

		return true;
	}
</script>

<script type='text/javascript'>
	function selectOne( idx ) {
		var checkObj = ( idx > 0 || frmMain.listcheckbox.length ) ? frmMain.listcheckbox[idx] : frmMain.listcheckbox;
		if( checkObj.type == 'radio' ) {
			if( !checkObj.checked )
				checkObj.click();
			else if( Select.selectFunctionObj )
				Select.selectFunctionObj();
		} else
			checkObj.click();
	}

	function selectReq() {
		var selectedValues = CheckBox.getValues( frmMain.listcheckbox );
		if( selectedValues == null ) {
			customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_ONE" encodeScript="true"/>" } );
			return;
		}
		Select.setValue( selectedValues );
		<% if( request.getParameter("noclosewin") == null ) { %>
			windowClose();
		<% } %>
	}

	Select.selectFunctionObj = selectReq;
</script>
<% } %>
