<%--
	File Name:	dpr_atreebute_party.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
	String[] attributeTypes = (String[])pageContext.findAttribute( "attributeTypes" );
	Object distributionChannels = pageContext.findAttribute( "distributionChannels" );
%>
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<script type='text/javascript' src='script/common.js'></script>
	<script type='text/javascript' src='script/utils.js'></script>
	<script type='text/javascript' src='script/jquery.min.js'></script>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			<%
				for( String type : attributeTypes ) {
					switch( type ) {
						case "DC": {
							out.println( "replaceCheckbox( parent.$(\"input[type=checkbox][name=distributionChannelCode]\").parent(), $(\"#check_distChannelCode\") );" );
							out.println( "replaceSelect( parent.$(\"#distributionChannelCode\"), $(\"#distributionChannelCode\") );" );
							break;
						}
						case "SO": {
							out.println( "replaceSelect( parent.$(\"#officeCode\"), $(\"#officeCode\") );" );
							break;
						}
						case "SG": {
							out.println( "replaceSelect( parent.$(\"#groupCode\"), $(\"#groupCode\") );" );
							break;
						}
						case "SOLD": {
							out.println( "replaceSelect( parent.$(\"#partyCode\"), $(\"#partyCode\") );" );
							out.println( "replaceSelect( parent.$(\"#soldPartyCode\"), $(\"#soldPartyCode\") );" );
							break;
						}
						case "SHIP": {
							out.println( "replaceSelect( parent.$(\"#shipPartyCode\"), $(\"#shipPartyCode\") );" );
							break;
						}
						case "PT": {
							out.println( "replaceSelect( parent.$(\"#plantCode\"), $(\"#plantCode\") );" );
						}
					}
				}
			%>

			var windowObj = window.opener;
			if( !windowObj )
				windowObj = window.parent;

			<%
				String types = "";
				for( String t : attributeTypes ) {
					types += t + ";";
				}
				types = types.substring( 0, types.length() - 1 );
			%>
			if( windowObj.attributePartyCallback )
				windowObj.attributePartyCallback( "<%= types %>" );
		}

		function replaceSelect( targetObj, replacement ) {
			if( targetObj.length ) {
				targetObj.empty();
				targetObj.append( replacement.find("option") );
				targetObj.find("option").first().attr( "selected", "selected" );
				if( targetObj.singleSelectmenu("instance") ) {
					targetObj.singleSelectmenu( "refresh" );
					targetObj.singleSelectmenu( "option", "disabled", false );
				} else {
					targetObj.attr( "disabled", false );
				}
			}
		}

		function replaceCheckbox( targetParentObj, replacement ) {
			targetParentObj.empty();
			targetParentObj.append( replacement.children() );
		}
	</script>
</head>

<body onload="JavaScript:bodyLoad();">
	<%@ include file="include_rbm_listcond.inc" %>
	<mtl:select id="condition" key="distributionChannelCode" mandatory="true" hasBlank="true"
			nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="distributionChannels" listCodeKey="distributionChannelCode"
			hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION" customSelect="false"
			listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"/>

	<mtl:select id="condition" key="officeCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
			hasBlank="true" customSelect="false" listId="offices" listCodeKey="officeCode"
			hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
			listNameFormat="$S{[:officeCode;$S{] :officeName}}"/>

	<mtl:select id="condition" key="groupCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
			hasBlank="true" customSelect="false" listId="groups" listCodeKey="groupCode"
			hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
			listNameFormat="$S{[:groupCode;$S{] :groupName}}" />

	<mtl:select id="condition" key="partyCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
			hasBlank="true" customSelect="false" listId="soldParties" listCodeKey="linkPartyCode"
			hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
			listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>

	<mtl:select id="condition" key="soldPartyCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
			hasBlank="true" customSelect="false" listId="soldParties" listCodeKey="linkPartyCode"
			hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
			listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>

	<mtl:select id="condition" key="shipPartyCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
			hasBlank="true" customSelect="false" listId="shipParties" listCodeKey="linkPartyCode"
			hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
			listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>

	<mtl:select id="condition" key="plantCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
			customSelect="false" hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_PLANT"
			listId="plants" listCodeKey="linkPlantCode" listNameFormat="$S{[:linkPlantCode;$S{] :linkPlantName}}"/>

	<div id='check_distChannelCode'>
<%
	if( distributionChannels != null ) {
		Object _distChannelCodes = condition.get( "distributionChannelCode" );
		String[] condDistributionChannelCodes;
		if( _distChannelCodes instanceof String[] ) {
			condDistributionChannelCodes = (String[])_distChannelCodes;
		} else {
			condDistributionChannelCodes = new String[1];
			condDistributionChannelCodes[0] = (String)_distChannelCodes;
		}

		for( Map<String, Object> channelMap : (List<Map<String, Object>>)distributionChannels ) {
			String code = (String)channelMap.get( "distributionChannelCode" );
			String name = (String)channelMap.get( "distributionChannelName" );
			out.print( "<input type='checkbox' id='distributionChannelCode_" + code + "'" );
			out.print( " name='distributionChannelCode' class='mandatory' value='" + code + "'");
			out.print( " onclick='JavaScript: readConditionReq(\"SO;SG;SOLD;SHIP;PLANT\");'" );
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
	}
%>
	</div>
</body>
</mtl:html>
