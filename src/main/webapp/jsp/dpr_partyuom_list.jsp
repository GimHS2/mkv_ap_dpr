<%--
	File Name:	dpr_partyuom_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/04/30		2.2.1	DPRParty select() -> cond() mode º¯°æ.
	hankalam	2017/02/28		2.2.0	create
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
	java.util.Map conditionMap = (java.util.Map)pageContext.findAttribute( "condition" );
	java.util.List<java.util.Map<String, Object>> uomList = (java.util.List<java.util.Map<String, Object>>)request.getAttribute("uomList");
	boolean useDivision = (com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;division") == null);
	boolean useDistributionChannel = (com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;distributionChannel") == null);
	boolean readOnly = com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(sessionMng.getGroupClass());
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		function checkInput() {
			var uomLength = <%= uomList.size() %>;

			if( Field.isArray(frmMain.uom_partyCode) ) {
				for( var i = 0; i < frmMain.tmp_uomInd.length / uomLength; i++ ) {
					var allowUOM = "";
					for( var j = 0; j < uomLength; j++ ) {
						var tmpIdx = i * uomLength + j;
						allowUOM += (frmMain.tmp_uomInd[tmpIdx].checked ? frmMain.tmp_uomInd[tmpIdx].value + "," : "");
					}
					allowUOM = allowUOM.substring(0, allowUOM.length -1 );
					if( allowUOM == "" || allowUOM.indexOf(",") > 0 ) {
						frmMain.tmp_uomInd.disabled = false;
						return Field.alertError( allowUOM
								, "<mtl:message key="jsp.dpr_partyuom_list.MSG_SAVE_ERROR" encodeScript="true"/>\nParty Code : " + frmMain.uom_partyCode[i].value );
					}

					frmMain.uom_allowUOM[i].value = allowUOM;
				}
			}

			frmMain.url.value = getLocationURL();
			return submitInput();
		}

		function uomCheckInd( row, uom ) {
			uom.checked = true;
			var uomName;
			<mtl:loop id="uomList" loopId="loop">
				uomName = "<mtl:value id="loop" key="uom"/>";
				if( uom.value != uomName )
					$("#" + uomName.toLowerCase() + "_uom_" + row )[0].checked = false;
					<%-- frmMain.<%= ((String)loop.get( "uom" )).toLowerCase() %>_uom[row].checked = false; --%>
			</mtl:loop>
		}

		function colCheckAll( value ) {
			var uomLength = <%= uomList.size() %>;

			for( var i = 0; i < frmMain.tmp_uomInd.length; i++ ) {
				if( frmMain.tmp_uomInd[i].value == value )
					frmMain.tmp_uomInd[i].checked = true;
				else
					frmMain.tmp_uomInd[i].checked = false;
			}
		}

		function downloadReq() {
			customPopup.confirm( { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" }, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					windowOpen( url );
				}
			});
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyUOM?mode=iup&wintype=sub";
			windowOpen( url, "clsMng" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="distributionChannelCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="distributionChannels" listCodeKey="distributionChannelCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
									listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_PARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyName" descriptionKey="FIELD_DPR_PARTY_NAME"/></div>
						<div class='field'><mtl:text id="condition" key="partyName"/></div>
					</div>
				</div>
			</div>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRPartyUOM?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return checkInput();">
		<mtl:contentGroup groupId="list" type="list">
			<div id='messagebar'></div>
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<input type='hidden' name='mode' value='mod'>
			<input type='hidden' name='url'>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage ) {
					int beforeRow = 0;

					public void printHeaderCell( JspWriter out, com.irt.data.cols.Column column, int rowspan ) throws java.io.IOException {
						StringBuffer attributeBuffer = new StringBuffer();
						String columnAttr = (String)column.getColumnAttr();
						if( columnAttr == null ) columnAttr = "";

						if( rowspan > 1 )
							attributeBuffer.append( " rowspan='"+ rowspan +"'" );

						String fieldKey = column.getFieldKey();

						if( fieldKey.endsWith("_uom") ) {
							out.print( "<th"+ attributeBuffer.toString() +" "+ columnAttr
									+ "onClick='JavaScript:colCheckAll(\"" + fieldKey.replace("_uom", "").toUpperCase()
									+ "\")'" + ">" );
							out.print( column.getColumnTitle(null, msghandler) );
							out.print( "</th>" );
						} else {
							super.printHeaderCell(out, column, rowspan);
						}
					}

					public String getColumnValue( com.irt.data.cols.Column column, java.util.Map recordMap, int row, int col ) {
						String fieldKey = column.getFieldKey();
						if( fieldKey.endsWith("_uom") && htmlpage.hasManageAuth() ) {
							String value = HtmlUtility.toHtmlString( recordMap.get(fieldKey) );
							StringBuffer sbuf = new StringBuffer();
							if( beforeRow == row ) {
								sbuf.append( "<input type='hidden' name='uom_allowUOM'>" )
									.append( "<input type='hidden' name='uom_partyCode' value='"+ (String)recordMap.get("partyCode") +"'>" )
									.append( "<input type='hidden' name='uom_divisionCode' value='"+ (String)recordMap.get("divisionCode") +"'>" )
									.append( "<input type='hidden' name='uom_distributionChannelCode' value='"+ (String)recordMap.get("distributionChannelCode") +"'>" )
									.append( "<input type='hidden' name='uom_organizationCode' value='"+ (String)recordMap.get("organizationCode") +"'>" );
								beforeRow++;
							}
							sbuf.append( "<div><input type='checkbox' id='" + fieldKey + "_" + row + "' onclick='JavaScript:uomCheckInd(" + row + ", this )' name='tmp_uomInd' value='"
											+ fieldKey.replace("_uom", "").toUpperCase() + "'" + (!"N".equals(value) ? " checked" : "")
											+ "/><label for='" + fieldKey + "_" + row + "'></label></div>" );

//							sbuf.append( "<input type='checkbox' onclick='JavaScript:uomCheckInd("+row+", this )' id='"+fieldKey+"' name='tmp_uomInd' value='" + fieldKey.replace("_uom", "").toUpperCase() + "'" + (!"N".equals(value) ? " checked" : "") +"/>" );

							return sbuf.toString();
						/*
						} else if( "checkAll".equals(fieldKey) && htmlpage.hasManageAuth() ) {
							StringBuffer sbuf = new StringBuffer();
							sbuf.append( "<a href='JavaScript:rowCheckAll("+row+");'><img src='images/ico_check.gif'></a>" );
							return sbuf.toString();
						*/
						} else
							return super.getColumnValue( column, recordMap, row, col );
					}

				};
				listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NUMBER );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listwriter.containsData() ) { %>
					<% if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="save"/>
						<mtl:button type="upload"/>
					<% } %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>

		</mtl:contentGroup>
		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
