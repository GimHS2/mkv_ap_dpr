<% if( "faq".equals(htmlpage.getMode()) || "faqmng".equals(htmlpage.getMode()) ) { %>

<%-- <link rel='stylesheet' href='<%= systemConfig.getClassURL()+"/../" %>html/fonts/font-awesome.min.css' /> --%>
<link rel='stylesheet' href='style/buttons.css' />
<link rel='stylesheet' href='style/menuhtml.css' />
<style id="jquery-sortable-style">
body.dragging, body.dragging * {
	cursor: move !important;
}

.dragged {
	position: absolute;
	opacity: 0.5;
	z-index: 2000;
}

/* placeholder is temp red dot moving around */
.menu-wrapper li.placeholder {
	position: relative;
	margin: 0;
	padding: 0;
	border: none;
}
.menu-wrapper li.placeholder:before {
	position: absolute;
	content: "";
	width: 0;
	height: 0;
	margin-top: -5px;
	left: -5px;
	top: -4px;
	border: 5px solid transparent;
	border-left-color: red;
	border-right: none;
}
</style>

<script type='text/javascript' src='script/jquery-noty.js'></script>
<script type='text/javascript' src='script/jquery-sortable.js'></script>
<script type='text/javascript' src='script/menuhtml.js'></script>
<script id="menu-loader" type="text/javascript">
	$(document).ready(function(){
		//remove duplicate content-wrapper
		if( $(".content-wrapper").length > 1 ) {
			$(".content-wrapper")
				.find(".content-wrapper")
				.removeClass("content-wrapper");
		}
	});

	var registerLoadLink = function() {
		$(".menu-wrapper .menuhtml a.menu-link").each(function(idx, obj){
			if( !$(obj).hasClass("loadlink") ) {
				if( !$(obj).hasClass('has-not-menu-link') ) {
					$(obj).addClass("loadlink");
				}

//					if( $(obj).is('a:not([href^="#"])') ) {
//						$(obj).addClass("loadlink");
//					}
			}
		});
	};

	//add className to add custom event
	$(document).ready(function(){
		registerLoadLink();
	});
	$(".menu-wrapper ul.menuhtml").on("change", function(event){
		registerLoadLink();
	});

	//add event on document ready
	$(document).ready(function() {
		$(function() {
			$(".menu-wrapper .loadlink").click(function(event) {
				event.preventDefault();//to override default behavior
				$(".content-wrapper").load(
					$(this).attr("href")
				);
			});
		});
	});
</script>

<% } %>

<script id="menu-loader" type="text/javascript">
var registerMenuWrapResizable = function(wrapper) {
	var wrapper = wrapper || $(".menu-wrapper");
	if( !wrapper.hasClass("ui-resizable") ) {
		$(".menu-wrapper").resizable({handles: 'e'});
		$('.menu-wrapper').resize(function(){
			$('.content-wrapper').width($("body").width()-$(".menu-wrapper").width());
		});
	}
};

var appendMenuWrapper = function() {
	var wrapper = $(".menu-wrapper");
	if( wrapper.length == 0 ) {
		wrapper = $(document.createElement('div'))
			.addClass("menu-wrapper");
		wrapper.appendTo('body');
	}
	registerMenuWrapResizable(wrapper);
};
$(document).ready(function(){
	appendMenuWrapper();
});

var injectLink = function() {
	$(".menu-wrapper").find("a.menu-link, a.menu-top-link").click(function(event) {
		event.preventDefault();//to override default behavior

		MenuHtml.setMenuLinkActive( $(event.target).parent() );

		var href = $(this).attr("href");
		var useIframe = false;
		if( true == useIframe ) {
			$("body").find(".content-wrapper").remove();
			$("body").append($("<iframe id='theiframe'>"));
			$("#theiframe").attr("src", $(this).attr("href"));
		} else {
			$(".content-wrapper").load(
				$(this).attr("href")
			);
		}
	});
};
</script>
