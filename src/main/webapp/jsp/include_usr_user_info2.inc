<%--
	File Name:	include_usr_user_info2.inc
	Version:	2.2.0

	Description:
		script(using)
			userUserInfo();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.0	create
--%>

	<mtl:form name="frmMain" fieldSetId="fieldSet">
		<div class='table table-fixed' style='min-width: 920px;'>
			<div class='row'>
				<div class='cell' style='padding-right: 10px;'>
					<mtl:contentGroup groupId="userInfo" type="content" styleClass="card">
						<h2 style='margin-bottom: 25px;'><mtl:message key="jsp.usr_user_info.SUBTITLE_USER_INFO"/></h2>
						<div>
							<img src='images/user_profile.png' style='margin-right: 25px; vertical-align: top'/>
							<div style='display: inline-block; width:calc(100% - 200px);'>
								<div class='info-table table-fixed'>
									<div class='row'>
										<div class='line-cell w140'>
											<div class='field-title'><mtl:title key="partyId" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:value id="record" key="partyName"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell'>
											<div class='field-title'><mtl:title key="userId" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:value id="record" key="userId"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell line-cell-btn'>
											<div class='field-title'><mtl:title key="userName" mandatory="false"/></div>
										</div>
										<div class='line-cell line-cell-btn'>
											<div class='field-info'>
												<mtl:value id="record" key="userName"/>
												<mtl:button type="button" icon="images/ico_edit.png" messageKey="jsp.BTN_EDIT"
														onClick="JavaScript: toggleInput(\"inputUserName\");" style="margin-left: 25px;"/>
											</div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell'>
											<div class='field-title'><mtl:title key="userClass" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:valuef id="record" key="userClass"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell'>
											<div class='field-title'><mtl:title key="status" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:valuef id="record" key="status"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell line-cell-btn'>
											<div class='field-title'><mtl:title key="password" descriptionKey="FIELD_PASSWORD" mandatory="false"/></div>
										</div>
										<div class='line-cell line-cell-btn'>
											<div class='field-info'>
												<input type='password' class='none-style' style='font-size: 16px' value='123456789' size="8" disabled>
												<mtl:button type="button" icon="images/ico_edit.png" messageKey="jsp.BTN_EDIT"
														onClick="JavaScript: toggleInput(\"inputPassword\");" style="margin-left: 25px;"/>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</mtl:contentGroup>
				</div>

				<div class='cell' style='padding-left: 10px;'>
					<mtl:contentGroup groupId="userDetailInfo" type="content" styleClass="card">
						<div class='content-title'>
							<div class='title'>
								<h2 style='margin-bottom: 25px;'><mtl:message key="jsp.usr_user_info.SUBTITLE_USER_DETAIL_INFO"/></h2>
								<div class='page-function'>
									<mtl:button type="button" icon="images/ico_edit.png" messageKey="jsp.BTN_EDIT"
											onClick="JavaScript: toggleInput(\"inputDetail\");"/>
								</div>
							</div>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='line-cell w210'>
									<div class='field-title'><mtl:title key="groupId" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" format="$H{groupName}"/></div>
								</div>
								<div class='line-cell w160'>
									<div class='field-title'><mtl:title key="availAccessCount" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" format="${USR_AVAILACCESSCOUNT_@availAccessCount}"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="serviceStartDate" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="serviceStartDate"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="serviceEndDate" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="serviceEndDate"/></div>
								</div>
							</div>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='line-cell w210'>
									<div class='field-title'><mtl:message key="jsp.usr_user_input.MSG_USR_DEFAULT_SALES_ORGANIZATION"/></div>
								</div>
								<div class='line-cell'>
								<% String countryDefaultOrgCode = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "PartyDefaultSorg;"+sessionMng.getPartyId(), ""); %>
									<div class='field-info'>
										<mtl:select id="record" key="extraValue4" hasBlank="true"
											nullValueKey="jsp.dpr_item_tree.MSG_ORGANIZATION_BLANK"
											defaultValue="<%=countryDefaultOrgCode%>"
											listId="organizations" listCodeKey="organizationCode" listNameFormat="$S{[:organizationCode;$S{]:organizationName}}"/>
									</div>
								</div>
								<div class='line-cell w160'></div>
								<div class='line-cell'></div>
							</div>
						<% if( !"JNJAP_KR".equals(sessionMng.getPartyId()) || sessionMng.isSystemAdmin() ) { %>
							<mtl:loop id="organizations" loopId="_record" maxRows="4" loopIndex="index">
						<%
							int orgCnt = ((java.util.List)request.getAttribute("organizations")).size();
							int contentCntInRow = 2; // How many fieldSets in a Row
							int fieldSetDefaultCnt = 2;// NAME : VALUE
							int content = contentCntInRow;
							int colspan = 0;
							if( orgCnt%contentCntInRow != 0 && orgCnt == index+1 ) {
								content = 1;
								colspan = contentCntInRow * fieldSetDefaultCnt - content;
							}

							if( (index+1)%2 != 0 ) { %>
							<div class='row'>
							<% } %>
								<div class='line-cell'>
									<div class='field-title'>
										<%= msghandler.getMessage( "jsp.usr_user_input.USER_EMPLOYEE_ID", (String)_record.get("organizationCode") ) %>
									</div>
								</div>
								<div class='line-cell'>
									<div class='field-info'>
										<mtl:value id="_record" key="employeeId"/>
									</div>
								</div>
							<% if( (index+1)%2 == 0 ) { %>
							</div>
							<% } %>
							</mtl:loop>

							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="department" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="department"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="position" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="position"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="email" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="email"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="mobilephone" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="mobilephone"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="telephone" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="telephone"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="fax" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="fax"/></div>
								</div>
							</div>
						<% } %>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='line-cell w210'>
									<div class='field-title'><mtl:title key="description" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="description"/></div>
								</div>
							</div>
						</div>
					</mtl:contentGroup>
				</div>
			</div>
		</div>
	</mtl:form>

	<div id='inner_overlay' class='inner-overlay'>
		<div class='inner-content-wrap'>
			<div id='inputUserName'>
				<mtl:form name="frmUserName" fieldSetId="fieldSet" onSubmit="JavaScript:return userNameCheckInput();">
					<input type='hidden' name='url'/>
					<input type='hidden' name='mode' value='usrname'>
					<input type='hidden' name='partyId' value='<%= sessionMng.getPartyId() %>'>
					<input type='hidden' name='userId' value='<%= sessionMng.getUserId() %>'>
					<mtl:contentGroup groupId="userName" type="list" styleClass="inner-content">
						<h2><mtl:message key="jsp.usr_user_info.SUBTITLE_CHANGE_USERNAME"/></h2>
						<div class='info-table'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key="userName"/>
									</div>
									<div class='field'>
										<mtl:text id="record" key="userName" onlyInput="true"/>
									</div>
								</div>
							</div>
						</div>
						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="cancel" name="cancel"/>
								<mtl:button type="save"/>
							</div>
						</div>
					</mtl:contentGroup>
				</mtl:form>
			</div>

			<div id='inputPassword'>
				<mtl:form name="frmPassword" method="post" fieldSetId="fieldSet" onSubmit="JavaScript:return passwordCheckInput();">
					<input type='hidden' name='url'/>
					<input type='hidden' name='mode' value='passwd'>
					<input type='hidden' name='partyId' value='<%= sessionMng.getPartyId() %>'>
					<input type='hidden' name='userId' value='<%= sessionMng.getUserId() %>'>
					<input type='hidden' name='locale' value='<%= htmlpage.getLocale() %>'>
					<mtl:contentGroup groupId="password" type="list" styleClass="inner-content">
						<h2><mtl:message key="jsp.usr_user_info.SUBTITLE_CHANGE_PASSWORD"/></h2>
						<div class='info-table'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key="password" name="presentPassword" mandatory="true" descriptionKey="FIELD_USR_PRESENT_PASSWORD"/>
									</div>
									<div class='field'>
										<input type='password' name='presentPassword' class='input-field' maxlength='20' style='width: calc(100% - 100px);'>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="password"/></div>
									<div class='field'>
										<input type='password' name='password' class='input-field' maxlength='20' style='width: calc(100% - 100px);'>
										<img name='password_tooltip' src='images/ico_info.png' style='vertical-align: middle; margin-left: 5px;' title='테스트중입니다'>
										<div id='content_id_password_tip' style='display: none;'>
											<ul>
												<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_1"/></li>
												<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_2"/></li>
												<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_3"/></li>
												<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_4"/></li>
												<li><mtl:message key="MSG_PASSWORD_EXPLICATE"/></li>
											</ul>
										</div>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="password" name="password2" descriptionKey="FIELD_USR_PASSWORD2"/></div>
									<div class='field'>
										<input type='password' name='password2' class='input-field' maxlength='20' style='width: calc(100% - 100px);'>
									</div>
								</div>
							</div>
						</div>
						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="cancel" name="cancel"/>
								<mtl:button type="save"/>
							</div>
						</div>
					</mtl:contentGroup>
				</mtl:form>
			</div>

			<div id='inputDetail'>
				<mtl:form name="frmDetail" method="post" fieldSetId="fieldSet" onSubmit="JavaScript:return detailCheckInput();">
					<%@ include file="include_rbm_form.inc" %>
					<mtl:contains id="record" key="gln">
						<mtl:hidden id="record" key="gln" name="countryCode"/>
					</mtl:contains>
					<mtl:hidden id="record" key="extraValue1"/>
					<input type='hidden' name='url'/>
					<input type='hidden' name='mode' value='mod'>
					<input type='hidden' name='type' value='info'>
					<mtl:hidden id="record" key="partyId"/>
					<mtl:hidden id="record" key="userId"/>
					<mtl:hidden id="record" key="userName"/>
					<mtl:hidden id="record" key="userType"/>
					<mtl:hidden id="record" key="status"/>
					<mtl:contentGroup groupId="detail" type="list" styleClass="inner-content"  style='overflow: auto'>
						<h2><mtl:message key="jsp.usr_user_info.SUBTITLE_CHANGE_DETAIL"/></h2>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell w210'>
									<div class='field-title'><mtl:title key="groupId"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("groupId") && pageContext.findAttribute("groups") != null ) { %>
										<mtl:select id="record" key="groupId" format="$H{groupName}" listId="groups"
												listCodeKey="groupId" listNameFormat="$H{groupName}" onlyInput="true"/>
									<% } else { %>
										<mtl:select id="record" key="groupId" format="$H{groupName}" readonly="true" onlyInput="true"/>
									<% } %>
									</div>
								</div>
								<div class='cell w160'>
									<div class='field-title'><mtl:title key="availAccessCount"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("availAccessCount") ) { %>
										<mtl:select id="record" key="availAccessCount" prefixKey="USR_AVAILACCESSCOUNT_"
												codeValues="<%= fieldOption.getVaildAccessCountValues() %>" onlyInput="true"/>
									<% } else { %>
										<mtl:select id="record" key="availAccessCount" prefixKey="USR_AVAILACCESSCOUNT_" readonly="true" onlyInput="true"/>
									<% } %>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell w210'>
									<div class='field-title'><mtl:title key="serviceStartDate"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("serviceStartDate") ) { %>
										<mtl:date id="record" key="serviceStartDate" onlyInput="true"/>
									<% } else { %>
										<mtl:text id="record" key="serviceStartDate" readonly="true" onlyInput="true"/>
									<% } %>
									</div>
								</div>
								<div class='cell w160'>
									<div class='field-title'><mtl:title key="serviceEndDate"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("serviceStartDate") ) { %>
										<mtl:date id="record" key="serviceEndDate" onlyInput="true"/>
									<% } else { %>
										<mtl:text id="record" key="serviceEndDate" readonly="true" onlyInput="true"/>
									<% } %>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell w210'>
									<div class='field-title'><mtl:message key="jsp.usr_user_input.MSG_USR_DEFAULT_SALES_ORGANIZATION"/></div>
								</div>
								<div class='cell'>
								<% String countryDefaultOrgCode = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "PartyDefaultSorg;"+sessionMng.getPartyId(), ""); %>
									<div class='field'>
										<mtl:select id="record" key="extraValue4" defaultValue="<%=countryDefaultOrgCode%>" onlyInput="true"
												listId="organizations" listCodeKey="organizationCode" listNameFormat="$S{[:organizationCode;$S{]:organizationName}}"/>
									</div>
								</div>
								<div class='cell w160'></div>
								<div class='cell'></div>
							</div>

						<% if( !"JNJAP_KR".equals(sessionMng.getPartyId()) || sessionMng.isSystemAdmin() ) { %>
							<mtl:loop id="organizations" loopId="_record" maxRows="4" loopIndex="index">
						<%
							int orgCnt = ((java.util.List)request.getAttribute("organizations")).size();
							int contentCntInRow = 2; // How many fieldSets in a Row
							int fieldSetDefaultCnt = 2;// NAME : VALUE
							int content = contentCntInRow;
							int colspan = 0;
							if( orgCnt%contentCntInRow != 0 && orgCnt == index+1 ) {
								content = 1;
								colspan = contentCntInRow * fieldSetDefaultCnt - content;
							}

							if( (index+1)%2 != 0 ) { %>
							<div class='row'>
							<% } %>
								<div class='cell'>
									<div class='field-title'>
										<%= msghandler.getMessage( "jsp.usr_user_input.USER_EMPLOYEE_ID", (String)_record.get("organizationCode") ) %>
									</div>
								</div>
								<div class='cell'>
									<div class='field'>
										<mtl:text id="_record" key="employeeId" readonly="<%= !isCountryAdmin %>" onlyInput="true"/>
										<mtl:hidden id="_record" key="organizationCode"/>
									</div>
								</div>
							<% if( (index+1)%2 == 0 ) { %>
							</div>
							<% } %>
							</mtl:loop>

							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="department" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="department" onlyInput="true"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="position" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="position" onlyInput="true"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="email" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="email" onlyInput="true"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="mobilephone" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="mobilephone" onlyInput="true"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="telephone" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="telephone" onlyInput="true"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="fax" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="fax" onlyInput="true"/></div>
								</div>
							</div>
						<% } %>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell w210' style='vertical-align: middle;'>
									<div class='field-title'><mtl:title key="description"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:textarea id="record" key="description" rows="4" onlyInput="true"/></div>
								</div>
							</div>
						</div>

						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="cancel" name="cancel"/>
								<mtl:button type="save"/>
							</div>
						</div>
					</mtl:contentGroup>
				</mtl:form>
			</div>
		</div>
	</div>
