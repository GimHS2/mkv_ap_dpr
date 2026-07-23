<%--
	File Name:	include_rbm_listcond.inc
	Version:	2.2.0

	Description:
		script
			changeCodeValueCondition( codeObj, nameObj, disabled );
		script(using)
			Field.lock();
			Field.release();

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	create
--%>

	<script type='text/javascript'>
		$(function() {
			var searchFold = $("input[name=search-fold]");
			if( searchFold.length ) {
				$("div.search-table").wrap( "<div class='search-table-wrap'/>" );
				$("div.search-bottom").prepend( "<div class='table-cell'><div class='accordion-button'></div></div>" );

				if( !searchFold.val() || searchFold.val() == "N" ) {
					$("div.search-table-wrap").css( {"height": "", "overflow": ""} );
					$("div.search-bottom .accordion-button").removeClass( "arrow-down" ).addClass( "arrow-up" );
				} else {
					$("div.search-table-wrap").css( {"height": "80px", "overflow": "hidden"} );
					$("div.search-bottom .accordion-button").removeClass( "arrow-up" ).addClass( "arrow-down" );
				}

				$("div.search-bottom .accordion-button").click( function() {
					var height = $("div.search-table").height();
					if( $(this).hasClass("arrow-up") ) {
						$("div.search-table-wrap").each( function(index, item) {
							if( index < 1 ) {
								$(item).css( {"height": "80px", "overflow": "hidden"} );
							} else {
								$(item).hide();
							}
						});
						$(this).removeClass( "arrow-up" ).addClass( "arrow-down" );
						searchFold.val( "Y" );
					} else {
						$("div.search-table-wrap").each( function(index, item) {
							if( index < 1 ) {
								$(item).css( {"height": "", "overflow": ""} );
							} else {
								$(item).show();
							}
						});
						$(this).removeClass( "arrow-down" ).addClass( "arrow-up" );
						searchFold.val( "N" );
					}
				});
			}
		});

		function changeCodeValueCondition( codeObj, nameObj, disabled ) {
			if( codeObj.value )
				Field.lock( nameObj, disabled );
			else
				Field.release( nameObj );
		}
	</script>

	<%@ include file="include_rbm_form.inc" %>
	<mtl:hidden id="request" key="sort"/>
	<mtl:hidden id="request" key="max"/>
