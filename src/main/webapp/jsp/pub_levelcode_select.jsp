<%--
	File Name:	pub_levelcode_select.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2008/03/31		2.2.1	table.list_content_top: absolute외 width 추가
										findAttribute("field") -> findAttribute("codeField")
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										filterValueInput_c() -> callByKeydown(filterReq_c)
										listwriter.setMaximumRows(h) -> listwriter.setScrollHeight(h*24)
										listwriter.setPrintingHeader() 사용
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
										com.irt.html.ListWriter, com.irt.html.ColumnListFactory 적용
										escape() -> encodeURIComponent()
										함수명 변경: filterValueInput0() -> filterValueInput_c(), filterReq0() -> filterReq_c()
	stghr12		2006/02/28		2.0.0	version up(sys_classcode_select.jsp -> pub_levelcode_select.jsp)
	stghr12		2004/06/03		1.0.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	com.irt.sql.HierarchyCodeField field = (com.irt.sql.HierarchyCodeField)pageContext.findAttribute( "codeField" );

	int selectClass;
	String selectClassStr = request.getParameter( "slcls" );
	String nameClass = request.getParameter( "namecls" );
	if( "A".equals(selectClassStr) )
		selectClass = 0;
	else if( "L".equals(selectClassStr) )
		selectClass = field.getLastLevel();
	else if( "P".equals(selectClassStr) )
		selectClass = field.getLastLevel() - 1;
	else {
		try {
			selectClass = Integer.parseInt( selectClassStr );
			if( selectClass > field.getLastLevel() )
				selectClass = field.getLastLevel();
			else if( selectClass < 0 ) {
				selectClass = field.getLastLevel() + selectClass;
				if( selectClass <= 0 ) selectClass = 1;
			}
		} catch( NumberFormatException numEx ) {
			selectClass = field.getLastLevel();
		}
	}

	boolean checkable = false;
	boolean selectable = false;
	boolean lowerable = false;
	java.util.List recordList = (java.util.List)pageContext.findAttribute( "records" );
	if( recordList != null ) {
		for( java.util.Iterator iterator = recordList.iterator(); iterator.hasNext(); ) {
			java.util.Map recordMap = (java.util.Map)iterator.next();
			int classCode = Integer.parseInt( (String)recordMap.get("classCode") );

			if( !selectable && (selectClass == 0 || classCode == selectClass) ) {
				selectable = true;
				if( lowerable ) break;
			}

			if( (selectClass == 0 || classCode < selectClass) && !lowerable ) {
				int lowerCount = 0;

				Object object = recordMap.get( "lowerCount" );
				if( object instanceof Number )
					lowerCount = ((Number)object).intValue();
				else if( object instanceof String )
					lowerCount = Integer.parseInt( (String)object );

				if( lowerCount > 0 ) {
					lowerable = true;
					if( selectable ) break;
				}
			}
		}
	}
	checkable = ( selectable && "chk".equals(request.getParameter("attr")) );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );
			<% if( "A".equals(nameClass) || "L".equals(nameClass) ) { %>
				Select.setElementNames( null, null, null, "code", "name" );
			<% } else { %>
				Select.setElementNames( "code", null, null, "name" );
			<% } %>
			<% if( lowerable ) out.print( "Select.selectFunctionObj = " + (selectable ? " null;" : " lowerReq;") ); %>
		}

		function filterReq_c() {
			var url = replaceQueryValue( getLocationURL(), "skip", null );
			url = replaceQueryValue( url, "filterType", encodeURIComponent(frmMain.filterType.value) );
			url = replaceQueryValue( url, "filterValue", encodeURIComponent(frmMain.filterValue.value) );
			url = replaceQueryValue( url, "focus", "filterValue" );
			if( frmMain.filterType.value == "name.all" )
				url = replaceQueryValue( url, "classCode", "<%= ( selectClass == 0 ? "" : String.valueOf(selectClass) ) %>" );

			location.replace( url, "_self" );
		}

		function linkReq( code ) {
			windowOpen( replaceQueryValue(getLocationURL("filterValue", "slcode"), "code", encodeURIComponent(code)) );
		}

		function lowerReq() {
			var selectedValues = CheckBox.getValues( frmMain.listcheckbox );
			if( selectedValues == null || selectedValues.length > 1 ) {
				alert( "<mtl:message key="MSG_CHOOSE_ONLY_ONE" encodeScript="true"/>" );
				return;
			}
			if( selectedValues[0].split(";")[2] > 0 )
				linkReq( selectedValues[0].split(";")[0] );
			else
				alert( "<mtl:message key="jsp.pub_levelcode_select.MSG_NO_LOWERLEVEL" encodeScript="true"/>" );
			<% if( "A".equals(nameClass) ) { %>
				Select.setValue( selectedValues );
			<% } %>
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
		<%
			java.util.List uppers = (java.util.List)pageContext.findAttribute( "uppers" );
			if( uppers != null ) {
				int lastidx = uppers.size() - 1;
		%>
			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px;'>
			<tr><td class='list_content_top'>
				<a href='JavaScript:linkReq("");' title='<mtl:message key="jsp.pub_levelcode_select.NULLCODE"/>'>
					<mtl:message key="jsp.pub_levelcode_select.NULLCODE"/>
				</a>
				<mtl:loop id="uppers" loopId="loop" loopIndex="index">
					&gt;&gt;
					<% if( index.intValue() == lastidx ) {%>
						<mtl:value id="loop" key="code"/> <mtl:value id="loop" key="name"/>
					<% } else { %>
						<a href='JavaScript:linkReq("<mtl:value id="loop" key="code"/>");'
								title='<mtl:value id="loop" key="name"/> <mtl:message key="jsp.pub_levelcode_select.LOWER"/>'>
							<mtl:value id="loop" key="code"/> <mtl:value id="loop" key="name"/>
						</a>
					<% } %>
				</mtl:loop>
			</td></tr>
			</table>
		<% } %>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'></span>
			</td></tr>
			</table>

			<%
				ColumnListFactory factory = new ColumnListFactory( "columnList" );

				if( property.containsKey("upperCode") )
					factory.appendColumn( "levelCode", "&nbsp;", "width='30' align='center'" );
				else
					factory.appendColumn( "code", "&nbsp;", "width='50' align='center'" );
				factory.appendColumn( "name", "&nbsp;", "class='description'" );

				if( "A".equals(nameClass) || "L".equals(nameClass) )
					factory.setPrimaryFieldKeys( new String[] { "code", "classCode", "lowerCount", "levelCode", "name" } );
				else
					factory.setPrimaryFieldKeys( new String[] { "code", "classCode", "lowerCount", "name" } );

				ListWriter listwriter = new com.irt.custom.SelectListWriter( request, htmlpage, factory.getColumnList() );
				listwriter.setCheckboxType( checkable ? ListWriter.CHECKBOXTYPE_CHECK : ListWriter.CHECKBOXTYPE_RADIO );
				listwriter.setScrollHeight( 288 );
				listwriter.setPrintingHeader( false );
				listwriter.print( out, 3 );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<% if( selectable ) { %>
					<mtl:button type="select" styleClass="btn_list"/>
				<% } %>
				<% if( lowerable ) { %>
					<mtl:button type="button" href="JavaScript:lowerReq();" imageSrc="images/btn_lower.gif" styleClass="btn_list"/>
				<% } %>
				<% if( (selectable || lowerable) && checkable ) { %>
					<mtl:button type="reset" href="JavaScript:Select.reset(frmMain.listcheckbox);" styleClass="btn_list"/>
				<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' align='left' nowrap>
					<select name='filterType'>
						<option value='name'><mtl:message key="jsp.pub_levelcode_select.FIELD_NAME_SELF"/></option>
						<option value='name.all'><mtl:message key="jsp.pub_levelcode_select.FIELD_NAME_ALL"/></option>
					</select>

					<input type='text' name='filterValue' class='length_20' onKeyDown='JavaScript:callByKeydown(filterReq_c);'>
					<a href='JavaScript:filterReq_c();'><img src='images/lbtn_filter.gif' class='tbtn'></a>
				</td>
			</tr>
			</table>
		</mtl:contentGroup>
	</mtl:form>
</body>
</mtl:html>
