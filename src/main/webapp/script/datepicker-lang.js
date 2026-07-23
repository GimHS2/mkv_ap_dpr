/* Korean initialisation for the jQuery calendar extension. */
/* Written by DaeKwon Kang (ncrash.dk@gmail.com), Edited by Genie and Myeongjin Lee. */
( function( factory ) {
	"use strict";

	if ( typeof define === "function" && define.amd ) {

		// AMD. Register as an anonymous module.
		define( [ "../widgets/datepicker" ], factory );
	} else {

		// Browser globals
		factory( jQuery.datepicker );
	}
} )( function( datepicker ) {
"use strict";

datepicker.regional = {
	closeText: "닫기",
	prevText: "이전달",
	nextText: "다음달",
	currentText: "오늘",
	monthNames: [ "01", "02", "03", "4월", "5월", "6월",
	"7월", "8월", "09", "10월", "11월", "12월" ],
	monthNamesShort: [ "01", "02", "03", "04", "05", "06",
	"07", "08", "09", "10", "11", "12" ],
	dayNames: [ "일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일" ],
	dayNamesShort: [ "일", "월", "화", "수", "목", "금", "토" ],
	dayNamesMin: [ "일", "월", "화", "수", "목", "금", "토" ],
	weekHeader: "주",
	dateFormat: "yy. m. d.",
	firstDay: 0,
	isRTL: false,
	showMonthAfterYear: true,
	//yearSuffix: "년"
	};
datepicker.setDefaults( datepicker.regional );

return datepicker.regional.ko;

} );
