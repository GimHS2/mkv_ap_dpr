<%--
	File Name:	include_dpr_order_cond.inc
	Version:	2.2.5

	Description:
		script
			changeCodeValue( codeObj, nameObj );
			checkSearchCond();
			initSearchCond();
			resetSearchCond();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/06/30		2.2.5	OrderType 추가
	jbaek		2019/05/30		2.2.4	PackDeal 추가
	jbaek		2015/04/30		2.2.3	Cross Browsing 적용: 1개의 td에 여러 select 박스 적용.
	jbaek		2014/09/30		2.2.2	Cross Browsing 적용
	jbaek		2013/05/30		2.2.1	HeaderStatus, LineItemStatus 기능 추가
	guksm		2008/09/26		2.2.0	create
--%>

	<script type='text/javascript'>
		var requestNumber = "<%= request.getParameter( "documentNumber" ) %>";

		$( function() {
			$("input[type=text].date").blur( function() {
				setTimeout( function() {
					if( Field.checkMultiMandatory([frmCond.startOrderDate, frmCond.endOrderDate], false) ) {
						Field.checkDateRange( frmCond.startOrderDate, frmCond.endOrderDate );
					}
				}, 500 );
			});
		});

		function checkSearchCond() {
		<% if( com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDetailCondition") ) { %>
			if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
		<% } %>
			if( !Field.checkMultiMandatory([frmCond.startOrderDate, frmCond.endOrderDate], false) ) return false;
			if( !Field.checkDateRange(frmCond.startOrderDate, frmCond.endOrderDate) ) return false;

			disableBlankInput( frmCond, true );
			return submitInput();
		}

	<% if( com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDetailCondition") ) { %>
		function readConditionReq( type ) {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=rtp";
			readPartyAttributeReq( url, type );
		}
	<% } %>
	</script>

	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<mtl:hidden id="request" key="sort"/>
			<mtl:hidden id="request" key="max"/>
			<input type='hidden' name='vtype' value='header'/>

			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="startOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_STARTDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="startOrderDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="endOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_ENDDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="endOrderDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="orderNumberType" descriptionKey="FIELD_DPR_ORDER_ORDERNUMBER"/></div>
						<div class='field field-multiple'>
							<mtl:select id="condition" key="orderNumberType" prefixKey="jsp.include_dpr_order_cond.DPR_ORDER_NUMBER_TYPE_"
									codeValues="O,P" searchable="false" width="135"/>
							<mtl:text id="condition" key="documentNumber" placeholder="jsp.MSG_TEXT_PLACEHOLDER_ORDERNUMBER"
									style="position: absolute; width: calc(100% - 130px); left: 130px;"/>
						</div>
					</div>
					<div class='cell'>
					<% if( com.irt.dpr.Country.isFeature((String)((java.util.Map)request.getAttribute("condition")).get("organizationCode"), "usePackDeal") ) { %>
						<div class='field-title'><mtl:title key="dealCode" descriptionKey="FIELD_DPR_PACKDEAL"/></div>
						<div class='field field-multiple'>
							<mtl:text id="condition" key="dealCode" placeholder="jsp.MSG_TEXT_PLACEHOLDER_PACKDEALCODE"
									style="width: calc(100% - 142px);"/>
							<mtl:select id="condition" key="packdealInd" nullValueKey="FIELD_DPR_PACKDEAL_DEALIND" hasBlank="true"
									prefixKey="PUB_WHETHER_" codeValues="Y,N" listNameFormat="$f{pure(dealCode)}"
									searchable="false" width="135"/>
						</div>
					<% } else { %>
						<div class='field-title'><mtl:title key="billingNumber" descriptionKey="FIELD_DPR_ORDER_BILLINGNUMBER"/></div>
						<div class='field'><mtl:text id="condition" key="billingNumber"/></div>
					<% } %>
					</div>
				</div>

				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="status" descriptionKey="FIELD_DPR_ORDER_STATUS"/></div>
						<div class='field field-multiple'>
							<mtl:select id="condition" key="status" nullValueKey="jsp.dpr_order_cond.MSG_ORDER_STATUS" hasBlank="true"
									prefixKey="DPR_ORDER_STATUS_" defaultValue="CD" codeValues="WK,ER,SG,SD,CG,CD,DE"
									searchable="false" width="135"/>
							<mtl:select id="condition" key="orderStatus" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasBlank="true"
									prefixKey="DPR_ORDER_ORDERSTATUS_" codeValues="PC,IN,VP,SH,CP,CO,CL,CE,DE"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORDERFLAG" width="calc(100% - 142px)"/>
						</div>
					</div>
				<% if( com.irt.dpr.Country.isFeature((String)((java.util.Map)request.getAttribute("condition")).get("organizationCode"), "useDangerousItem") ) { %>
					<div class='cell'>
						<div class='field-title'><mtl:title key="orderType" descriptionKey="jsp.MSG_ORDER_CATEGORY"/></div>
						<div class='field'>
							<mtl:select id="condition" key="orderType" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" hasBlank="true"
									prefixKey="DPR_ORDER_ORDERTYPE_" codeValues="NO,DA" searchable="false"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORDERTYPE"/>
						</div>
					</div>
				<% } %>
				<% if( com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDetailCondition") ) { %>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD;SHIP\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/>
						</div>
						<div class='field'>
						<%
							Object _distChannelCodes = conditionMap.get( "distributionChannelCode" );
							String[] condDistributionChannelCodes;
							if( _distChannelCodes instanceof Object[] ) {
								condDistributionChannelCodes = new String[ ((Object[])_distChannelCodes).length ];
								int i = 0;
								for( Object o : (Object[])_distChannelCodes ) {
									condDistributionChannelCodes[i++] = (String)o;
								}
							} else {
								condDistributionChannelCodes = new String[1];
								condDistributionChannelCodes[0] = (String)_distChannelCodes;
							}
							for( Map<String, Object> channelMap : distributionChannels ) {
								String code = (String)channelMap.get( "distributionChannelCode" );
								String name = (String)channelMap.get( "distributionChannelName" );
								out.print( "<input type='checkbox' id='distributionChannelCode_" + code + "'" );
								out.print( " name='distributionChannelCode' class='mandatory' value='" + code + "'");
								out.print( " onclick='JavaScript: readConditionReq(\"SO;SG;SOLD\");'" );
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
						%>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_PARTY_SALESOFFICE_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
									listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
									modified="readConditionReq(\"SG;SOLD;SHIP\");"/>
						</div>
					</div>
				<% } else { %>
					<div class='cell'></div>
					<div class='cell'></div>
				<% } %>
				</div>

			<% if( com.irt.dpr.Country.isFeature( sessionMng.getExtraValue(), "useDetailCondition") ) { %>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="groupCode" descriptionKey="FIELD_DPR_PARTY_SALESGROUP_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
									listId="groups" listCodeKey="groupCode" listNameFormat="$S{[:groupCode;$S{] :groupName}}"
									modified="readConditionReq(\"SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"
									modified="readConditionReq();"/>
						</div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
			<% } %>
			</div>
			<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>
		</mtl:contentGroup>
	</mtl:form>
