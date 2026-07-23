<%--
	File Name:	include_dpr_order_header.inc
	Version:	2.2.4

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.4	신규 UI/UX 적용
	hankalam	2020/06/30		2.2.3	����Ÿ�� �ʵ� �߰�
	jbaek		2018/10/30		2.2.2	isChinaCountry() ����
	jbaek		2012/08/30		2.2.1	rdd Logic ����: China country�� defaultRDD Į���� ���
	lsinji		2008/09/26		2.2.0	create
--%>
<%
	String organizationCode = null;
	if( headerMap != null ) {
		organizationCode = (String)headerMap.get( "organizationCode" );
	}
%>

	<mtl:contentGroup groupId="order_header" type="content" descriptionKey="jsp.dpr_order_input.GRP_ORDER_HEADER">
		<div class='info-table table-fixed'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'><mtl:title key="orderDate" descriptionKey="jsp.dpr_order_input.FIELD_ORDER_DATE" mandatory="false"/></div>
					<div class='field-info'><mtl:value id="header" key="orderDate"/> <mtl:value id="header" key="orderDateDOW"/></div>
				</div>
				<div class='cell'>
					<div class='field-title'><mtl:title key="organizationCode" descriptionKey="jsp.FIELD_DPR_SALES_ORGANIZATION" mandatory="false"/></div>
					<div class='field-info'><mtl:valuef id="header" key="organizationCode" format="$S{[:organizationCode;$S{]:organizationName}}"/></div>
				</div>
				<div class='cell'>
					<div class='field-title'>
					<% if( !com.irt.dpr.Country.isFeature(organizationCode, "useInputRDD") && !"Y".equals(useSelectRdd) ) {%>
						<mtl:title key="inDateDefault" mandatory="false"/>
					<% } else { %>
						<mtl:title key="inDate" mandatory="false"/>
					<% } %>
					</div>
					<div class='field-info'>
					<% if( com.irt.dpr.Country.KOREA_ORGANIZATION.equals(organizationCode) ) { %>
						<mtl:value id="header" key="inDate"/>
						<span style='font-weight: bold; color: #f06908'> <mtl:value id="header" key="inDateDOW"/></span>
					<% } else { %>
						<mtl:value id="header" key="inDate"/> <mtl:value id="header" key="inDateDOW"/>
					<% } %>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'><mtl:title key="inDateSimulation" mandatory="false"/></div>
					<div class='field-info'><mtl:value id="header" key="inDateSimulation"/> <mtl:value id="header" key="inDateSimulationDOW"/></div>
				</div>
			</div>

			<div class='row'>
				<div class='cell'>
					<div class='field-title'><mtl:title key="soldPartyCode" descriptionKey="jsp.dpr_order_input.FIELD_SOLD_PARTY" mandatory="false"/></div>
					<div class='field-info' style='white-space: nowrap;'><mtl:valuef id="header" key="soldPartyCode" format="$S{[:soldPartyCode;$S{]:soldPartyName}}"/></div>
				</div>
				<div class='cell'></div>
				<div class='cell'>
					<div class='field-title'><mtl:title key="shipPartyCode" descriptionKey="jsp.dpr_order_input.FIELD_SHIP_PARTY" mandatory="false"/></div>
					<div class='field-info' style='white-space: nowrap;'><mtl:valuef id="header" key="shipPartyCode" format="$S{[:shipPartyCode;$S{]:shipPartyName}}"/></div>
				</div>
				<div class='cell'></div>
			<% if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) { %>
				<div class='cell'>
					<div class='field-title'><mtl:title key="orderType" descriptionKey="jsp.MSG_ORDER_CATEGORY" mandatory="false"/></div>
					<div class='field-info'><mtl:valuef id="header" key="orderType" format="${DPR_ORDER_ORDERTYPE_@orderType}"/></div>
				</div>
			<% } %>
				<div class='cell'>
					<div class='field-title'><mtl:title key="status" mandatory="false"/></div>
					<div class='field-info'><mtl:valuef id="header" format="${DPR_ORDER_STATUS_@status}"/></div>
				</div>
			<% if( !com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) { %>
				<div class='cell'></div>
			<% } %>
			</div>
		</div>
	</mtl:contentGroup>
