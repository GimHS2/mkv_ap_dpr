<%--
	File Name:	include_pub_calendar.inc
	Version:	2.2.2

	Description:
		<head>에 추가

		script
			Calendar
			Calendar.click( y, m, d );
			Calendar.hide();
			Calendar.link( y, m, focus );
			Calendar.mousemove( id, styleClass );
			Calendar.print();
			Calendar.show( clickFunctionObj, selectingType, selectedDate, minimumDate, maximumDate );
			openCalendar( elementName, minimumDate, maximumDate );
			openCalendar.click( date );
			openCalendarWeek( elementObj );
			openCalendarWeek.click( date );
		script(using)
			DateField.getDateValue();
			DateField.setDate();
			DateWeek
			DialogBox
	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/03/31		2.2.2	CrossBrowsing 적용
	stghr12		2008/03/31		2.2.1	style 적용방법 변경
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										요일표시방식 변경
	stghr12		2007/04/30		2.1.1	주 선택지원 추가
										Date.getYear() -> Date.getFullYear()
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
										DateField.modified -> DateField.modifiedByName
										Calendar.show(): 달력 위치 잡는 로직 수정
	stghr12		2006/02/28		2.0.0	version up(include_pub_inpututil_cal.inc -> include_pub_calendar.inc)
	stghr12		2004/06/03		1.0.0	create
--%>

<script type='text/javascript'>
	$( function() {
		setDatePicker();
	});

	function setDatePicker() {
		$("input[type=text].date").datepicker( {
			prevText: "<mtl:message key="jsp.DATEPICKER_PREV_MONTH"/>",
			nextText: "<mtl:message key="jsp.DATEPICKER_NEXT_MONTH"/>",
			monthNames: [ "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" ],
			monthNamesShort: [ "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" ],
			dayNames: [
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SUNDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_MONDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_TUSEDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_WEDNESDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_THURSDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_FRIDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SATURDAY"/>",
			],
			dayNamesMin: [
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_SUNDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_MONDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_TUSEDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_WEDNESDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_THURSDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_FRIDAY"/>",
				"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_SATURDAY"/>",
			],
			dateFormat: "<mtl:message key="jsp.DATEPICKER_DATE_FORMAT"/>",
			firstDay: 0,
			isRTL: false,
			showMonthAfterYear: true,
			showOtherMonths: true,
			selectOtherMonths: true,
			changeMonth: true,
			changeYear: true,
			yearRange: "c-3:c+3",
			beforeShow: function( input, inst ) {
				$.datepicker._pos = $.datepicker._findPos( input );
				$.datepicker._pos[ 1 ] += input.offsetHeight + 5;
			}
		});
	}
</script>
