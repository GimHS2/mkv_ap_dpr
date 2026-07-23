<%--
	File Name:	include_dpr_tail.inc
	Version:	2.3.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	yjkdev21	2026/03/31		2.3.0	한국에서 언어 설정에 맞게 파일 나오도록 변경
	dudwls3720	2024/07/31		2.2.9	otCookielawUuids에 jnj 도메인 -> kenvue 도메인으로 변경
	hankalam	2021/11/30		2.2.8	신규 UI/UX 적용
	jbaek		2021/05/30		2.2.7	Policy 변경
	hankalam	2020/06/30		2.2.6	TermAndCondition 항목 추가
	jbaek		2018/10/30		2.2.5	LastUpdateDate Date format 변경
	jbaek		2018/04/30		2.2.4	change ContactUs url to JNJ standard contact us site.
	jbaek		2015/07/30		2.2.3	Standard Footer 변경
	jbaek		2015/04/30		2.2.2	CrossBrowsing 적용: footer class 적용으로 footer감안한 화면 사이즈 구함.
	jbaek		2012/08/30		2.2.1	China Country의 기본 Org를 HOCO로 변경
	lsinji		2008/09/26		2.2.0	create
--%>
<%
	String defaultLegal = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Legal;Default" );
	String defaultPrivacy = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Privacy;Default" );
	String defaultContact = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "ContactUs;Default" );
	String defaultTermAndCondition = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "TermAndCondition;Default" );
	String defaultCookiePolicy = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "CookiePolicy;Default" );
	String _organizationCode = sessionMng.getExtraValue();
	if( _organizationCode == null && "JNJAP_CN".equals(sessionMng.getPartyId()) )
		_organizationCode = com.irt.dpr.Country.HOCO_ORGANIZATION;

	String legal = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Legal;" + _organizationCode, defaultLegal );
	String privacy = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Privacy;" + _organizationCode, defaultPrivacy );
	String contact = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "ContactUs;" + _organizationCode, defaultContact );
	String termAndCondition = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "TermAndCondition;" + _organizationCode, defaultTermAndCondition );
	String cookiePolicy = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "CookiePolicy;" + _organizationCode, defaultCookiePolicy );

	String defaultLocale = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Locale;" + _organizationCode );

	java.util.Locale _locale = htmlpage.getLocale();
	if( defaultLocale != null && defaultLocale.length() > 0 )
		_locale = new java.util.Locale( defaultLocale );

	String _language = htmlpage.getLocale().getLanguage();

	if( "2400".equals(_organizationCode) ) {
		if( !("en".equals(_language) || "th".equals(_language)) )
			_language = "en";
		cookiePolicy = "tail/cookiePolicy_"+ _language +"-TH.html";
		privacy = "tail/privacyNotice_"+ _language +"-TH.html";
		legal = "tail/legalNotice_"+ _language +"-TH.html";
	} else if( "320S".equals(_organizationCode) ) {
		if( !("en".equals(_language) || "ko".equals(_language)) )
			_language = "en";
		privacy = "tail/privacyNotice_"+ _language +".html";
		legal = "tail/legalNotice_"+ _language +".html";
		termAndCondition = "tail/termAndCondition_"+ _language +".html";
	}

	final java.util.Map<String, String> contactUrls = new java.util.HashMap<String, String>(){{
		   put("awswjbsgpl000k.com", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
		   put("localhost", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
		   put("www.qa.apackenvuedportal.com", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
		   put("qa.apackenvuedportal.com", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
		   put("www.apackenvuedportal.com", "https://www.ccc-consumercarecenter.com/UCUConfiguration?id=a075800002r0nA0");
		   put("apackenvuedportal.com", "https://www.ccc-consumercarecenter.com/UCUConfiguration?id=a075800002r0nA0");
	}};
	String standardContactUrl = contactUrls.get(systemConfig.getDomain());

	String lastUpdated = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "LastUpdateDate" );
	String lastUpdateDate = null;
	if( lastUpdated == null ) {
		lastUpdateDate = new java.text.SimpleDateFormat("dd MMM yyyy.", new java.util.Locale("en"))
			.format(com.irt.data.Date.getInstance(new java.util.Date()));
	} else if( lastUpdated.length() == 10 && lastUpdated.contains("-") ) {
		lastUpdateDate = new java.text.SimpleDateFormat("dd MMM yyyy.", new java.util.Locale("en"))
			.format(com.irt.data.Date.getInstance(lastUpdated));
	} else {
		lastUpdateDate = lastUpdated;
	}

	com.irt.util.MessageHandler _msghandler = systemConfig.getMessageHandler( _locale );
%>

<script type='text/javascript'>
	function legaNotice() {
		window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= legal %>", "winNotice"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
				+ " width=810, height=700" );
	}

	function privacyNotice() {
		window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= privacy %>", "winNotice"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
				+ " width=810, height=700" );
	}

	function contactUs() {
		<% if( standardContactUrl != null ) { %>
			window.open( "<%= standardContactUrl %>", "winNotice"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
				+ " width=810, height=400" );
		<% } else { %>
			window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= contact %>", "winNotice"
				, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
					+ " width=810, height=400" );
		<% } %>
	}

	function teamAndCondition() {
		window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= termAndCondition %>", "winNotice"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
				+ " width=810, height=700" );
	}

	function cookiePolicy() {
		window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= cookiePolicy %>", "winNotice"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
				+ " width=810, height=700" );
	}
</script>

<% if( cookiePolicy != null ) { %>
<!-- OneTrust Cookies Consent Notice start for Sitename -->
<%
final java.util.Map<String, String> otCookielawUuids = new java.util.HashMap<String, String>(){{
    put("awswjbsgpl000k.com", "6b66a09c-70bc-483a-9d2a-526ed6723070-test");
    put("localhost", "6b66a09c-70bc-483a-9d2a-526ed6723070-test");
    put("www.qa.apackenvuedportal.com", "6b66a09c-70bc-483a-9d2a-526ed6723070-test");
    put("qa.apackenvuedportal.com", "6b66a09c-70bc-483a-9d2a-526ed6723070-test");
    put("www.apackenvuedportal.com", "1e45ad57-3e0a-4686-82de-ffdff47b8d0e");
    put("apackenvuedportal.com", "1e45ad57-3e0a-4686-82de-ffdff47b8d0e");
}};
%>
<% if( otCookielawUuids.get(systemConfig.getDomain()) != null ) { %>
<script src='https://cdn.cookielaw.org/scripttemplates/otSDKStub.js' data-document-language='true' type='text/javascript' charset='UTF-8' data-domain-script="<%= otCookielawUuids.get(systemConfig.getDomain()) %>" ></script>
<script type="text/javascript">
function OptanonWrapper() { }
</script>

<% } %>
<!-- OneTrust Cookies Consent Notice end for Sitename -->
<% } %>

<div class='copyright'>
	<div class='link'>
		<a href='JavaScript:legaNotice();'><mtl:message key="MSG_PUB_TAIL_LEGALNOTICE"/></a>
		| <a href='JavaScript:privacyNotice();'><mtl:message key="MSG_PUB_TAIL_PRIVACYNOTICE"/></a>
	<% if( termAndCondition != null ) { %>
		| <a href='JavaScript:teamAndCondition();'><mtl:message key="MSG_PUB_TAIL_TERMANDCONDITION"/></a>
	<% } %>
	<% if( cookiePolicy != null ) { %>
		| <a href='JavaScript:cookiePolicy();'><mtl:message key="MSG_PUB_TAIL_COOKIEPOLICY"/></a>
		| <a id="ot-sdk-btn" class="ot-sdk-show-settings"><mtl:message key="MSG_PUB_TAIL_COOKIESETTINGS"/></a>
	<% } %>
		| <a href='<%= standardContactUrl %>' target='_blank'><mtl:message key="MSG_PUB_TAIL_CONTACTUS"/></a>
		<br>
	</div>
	<div>
		<mtl:message key="MSG_PUB_TAIL_LICENSE"/><br>
		<mtl:message key="MSG_PUB_TAIL_STATEMENT"/><br>
		<mtl:message key="MSG_PUB_TAIL_GEOGRAPHIC"/>
		<%= msghandler.getMessage( "MSG_PUB_TAIL_LASTUPDATE", lastUpdateDate ) %>
		<mtl:message key="MSG_PUB_TAIL_DEPENDENCY_BROSWER"/>
	</div>
</div>
</div>
