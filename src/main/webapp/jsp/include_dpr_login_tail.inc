<%--
	File Name:	include_dpr_login_tail.inc
	Version:	2.2.8

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content'
	dudwls3720	2025/02/28		2.2.8	legaNotice() : 캔뷰 legal-notice 링크로 이동하도록 변경
	dudwls3720	2024/07/31		2.2.7	otCookielawUuids에 jnj 도메인 -> kenvue 도메인으로 변경
	hankalam	2021/11/30		2.2.6	신규 UI/UX 적용
	jbaek		2021/05/30		2.2.5	Policy 변경
	hankalam	2020/06/30		2.2.4	TermAndCondition 항목 추가
	jbaek		2018/10/30		2.2.3	LastUpdateDate Date format 변경
	jbaek		2018/04/30		2.2.2	change ContactUs url to JNJ standard contact us site.
	jbaek		2015/07/30		2.2.1	Standard Footer 변경
	lsinji		2008/09/26		2.2.0	create
--%>

<%
	String _language = htmlpage.getLocale().getLanguage();
	if( !"en".equals(_language) && !"zh".equals(_language) && !"zf".equals(_language) && !"th".equals(_language) && !"ko".equals(_language) )
		_language = "en";
	String legal = "tail/legalNotice_"+ _language +".html";
	String privacy = "tail/privacyNotice_"+ _language +".html";
	String contact = "tail/contactUs_"+ _language +".html";
	String termAndCondition = null;
	String cookiePolicy = null;
	if( "ko".equals(_language) ) {
		termAndCondition = "tail/termAndCondition_"+ _language +".html";
	}
	if( "th".equals(_language) ) {
		if( !("en".equals(_language) || "th".equals(_language)) )
			_language = "en";
		cookiePolicy = "tail/cookiePolicy_"+ _language +"-TH.html";
		privacy = "tail/privacyNotice_"+ _language +"-TH.html";
		legal = "tail/legalNotice_"+ _language +"-TH.html";
	}

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

	final java.util.Map<String, String> contactUrls = new java.util.HashMap<String, String>(){{
	   put("awswjbsgpl000k.com", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
	   put("localhost", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
	   put("www.qa.apackenvuedportal.com", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
	   put("qa.apackenvuedportal.com", "https://qa-universalcontactus.cs83.force.com/UCUConfiguration?id=a074E0000030dlgQAA");
	   put("www.apackenvuedportal.com", "https://www.ccc-consumercarecenter.com/UCUConfiguration?id=a075800002r0nA0");
	   put("apackenvuedportal.com", "https://www.ccc-consumercarecenter.com/UCUConfiguration?id=a075800002r0nA0");
	}};
	String standardContactUrl = contactUrls.get(systemConfig.getDomain());
%>
<script type='text/javascript'>


	function legaNotice() {
	<% if( _language == "zh" ) { %>
	window.open( "https://www.kenvue.com/zh-cn/legal-notice", "winNotice"
		, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
			+ " width=810, height=700" );
	<% } else { %>
	window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= legal%>", "winNotice"
		, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
			+ " width=810, height=700" );
	<% } %>
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
					+ " width=810, height=700" );
		<% } else { %>
			window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= contact %>", "winNotice"
				, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
					+ " width=810, height=700" );
		<% } %>
	}
<% if( termAndCondition != null ) { %>
	function teamAndCondition() {
		window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= termAndCondition %>", "winNotice"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
				+ " width=810, height=700" );
	}
<% } %>
<% if( cookiePolicy != null ) { %>
	function cookiePolicy() {
		window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %><%= cookiePolicy %>", "winNotice"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=yes"
				+ " width=810, height=700" );
	}
<% } %>
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
	</div>
	<div>
		<mtl:message key="MSG_PUB_TAIL_LICENSE"/><br>
		<mtl:message key="MSG_PUB_TAIL_STATEMENT"/><br>
		<mtl:message key="MSG_PUB_TAIL_GEOGRAPHIC"/>
		<%= msghandler.getMessage( "MSG_PUB_TAIL_LASTUPDATE", lastUpdateDate ) %>
		<mtl:message key="MSG_PUB_TAIL_DEPENDENCY_BROSWER"/>
	</div>
</div>
