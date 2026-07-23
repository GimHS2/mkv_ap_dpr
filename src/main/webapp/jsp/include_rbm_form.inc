<%--
	File Name:	include_rbm_form.inc
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	locale 추가
	stghr12		2007/04/30		2.1.1	idown 추가
	stghr12		2006/12/01		2.1.0	url은 항시 넘기도록 수정
	stghr12		2006/02/28		2.0.0	create
--%>

<% {
	String _mode = htmlpage.getMode();
	if( "reg".equals(_mode) || "ireg".equals(_mode) )
		out.print( "<input type='hidden' name='mode' value='reg'>" );
	else if( "mod".equals(_mode) || "imod".equals(_mode) )
		out.print( "<input type='hidden' name='mode' value='mod'>" );
	else if( "mmod".equals(_mode) || "immod".equals(_mode) )
		out.print( "<input type='hidden' name='mode' value='mmod'>" );
	else if( "down".equals(_mode) || "idown".equals(_mode) )
		out.print( "<input type='hidden' name='mode' value='down'>" );
	else if( "cond".equals(_mode) )
		out.print( "<input type='hidden' name='mode' value='list'>" );

	out.print( "<input type='hidden' name='url' value='"+ HtmlUtility.toHtmlString(htmlpage.getBackURL()) +"'>" );
	if( htmlpage.getSystemMenu() != null )
		out.print( "<input type='hidden' name='menu' value='"+ htmlpage.getSystemMenu() +"'>" );
	if( htmlpage.getWindowType() != null )
		out.print( "<input type='hidden' name='wintype' value='"+ htmlpage.getWindowType() +"'>" );
} %>
	<mtl:hidden id="request" key="locale"/>
