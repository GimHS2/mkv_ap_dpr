<%--
	File Name:	include_pub_input.inc
	Version:	2.2.0

	Description:
		<head>?? ???

		script
			Field.alertError( elementObj, message, ... );
			Field.checkConditionFormat( elementObj, subjectTitle );
			Field.checkDateFormat( elementObj, subjectTitle );
			Field.checkMandatory( elementObj, subjectTitle );
			Field.checkNumberFormat( elementObj, real, minus, subjectTitle );
			Field.checkNumberRange( elementObj, minValue, maxValue, rangeType, subjectTitle );
			Field.checkTimeFormat( elementObj, subjectTitle );
		script(using)
			DateField.convertValueToDate( value );
			Field.focus( elementObj );
			Field.getValue( elementObj );
			Field.getSubjectObject( elementObj );

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
										Field.checkConditionFormat(), Field.checkTimeFormat() 추가
										Field.checkNumberFormat()에서 실수여부와 마이너스여부에 대한 오류메시지 변경
										Field.checkNumberRange()에서 messageKey 변경
	stghr12		2006/07/07		2.0.1	rangeType 추가
	stghr12		2006/03/31		2.0.0	create
--%>

	<script type='text/javascript' src='script/input.js'></script>
	<script type='text/javascript'>
		function makeMessage( message, args ) {
			if( Array.isArray(args) ) {
				for( var i = 1; i <= args.length; i++ )
					message = message.replace( new RegExp("%ARG_"+ (i) +"%"), args[i - 1] );
			} else {
				message = message.replace( new RegExp("%ARG_1%"), args );
			}
			return message;
		}

		function checkMandatoryInForm( form ) {
			if( $(form).length ) {
				var elements = $(form).find( "input[type=text][data-mandatory=true], select.mandatory" );
				return Field.checkMultiMandatory( elements, true );
			}

			return true;
		}

		Field.alertError = function( elementObj, message ) {
			Field.focus( elementObj );
			if( elementObj.select ) elementObj.select();
			if( Field.alertError.arguments.length > 2 ) {
				var args = Field.alertError.arguments;
				message = makeMessage( message, args );
			}
			var messages = { "header" : message };
			customPopup.alert( messages );

			return false;
		}

		Field.checkConditionFormat = function( elementObj, subjectTitle ) {
			if( elementObj.value ) {
				var validation = true;

				var values0 = elementObj.value.split( "," );
				for( var i = 0; i < values0.length && validation; i++ ) {
					var values1 = values0[i].split( "/" );
					var values2 = values1[0].split( "-" );
					if( values1.length > 2 || values2.length > 2 )
						validation = false;
					else if( values1.length == 2 && values2.length == 1 )
						validation = false;
					else if( values2[0].length == 0 || (values2.length == 2 && values2[1].length == 0) )
						validation = false;
				}
				if( validation ) return true;

				if( !subjectTitle ) {
					var subjectObj = Field.getSubjectObject( elementObj );
					if( subjectObj ) subjectTitle = subjectObj.innerHTML;
				}
				if( subjectTitle ) {
					var message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_CONDITIONVALUE_FORMAT", "%ARG_1%") ) %>";
					return Field.alertError( elementObj, message, subjectTitle );
				} else
					return Field.alertError( elementObj, "<mtl:message key="ERR_INVALID_CONDITIONVALUE_FORMAT" encodeScript="true"/>" );
			}

		}

		Field.checkDateFormat = function( elementObj, isAlert, subjectTitle ) {
			var hasError = false;
			if( elementObj.value && DateField.convertValueToDate(elementObj.value) == null ) {
				if( isAlert ) {
					if( !subjectTitle ) {
						var subjectObj = Field.getSubjectObject( elementObj );
						if( subjectObj ) subjectTitle = subjectObj.innerHTML;
					}
					if( subjectTitle ) {
						var message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_DATE", "%ARG_1%") ) %>";
						return Field.alertError( elementObj, message, subjectTitle );
					} else
						return Field.alertError( elementObj, "<mtl:message key="ERR_INVALID_DATE" encodeScript="true"/>" );
				}
				hasError = true;
			}
			if( !isAlert ) {
				toggleFieldErrorMessage( elementObj, hasError, "<mtl:message key="ERR_INVALID_DATE" encodeScript="true"/>" );
			}

			return !hasError;
		}

		// isAnd : true(AND) or false(OR)
		// true 일 경우 모든 엘리먼트 mandatory 검사
		// false 일 경우 엘리먼트중 하나라도 mandatory true 면 true return
		Field.checkMultiMandatory = function( elements, isAnd, isAlert ) {
			var hasError = false;
			if( elements ) {
				if( !isAnd ) {
					$.each( elements, function( index, item) {
						if( Field.getValue(item).length > 0 ) {
							hasError = false;
							return false;
						} else {
							hasError = true;
						}
					});
				}
				if( isAnd || hasError ) {
					$.each( elements, function(index, item) {
						if( !Field.checkMandatory(item, isAlert) ) {
							hasError = true;
							if( isAlert ) {
								return false;
							}
						}
					});
				}
			}

			return !hasError;
		}

		Field.checkDateRange = function( fromDateObj, toDateObj, isAlert ) {
			var hasError = false;
			if( !Field.checkDateFormat(fromDateObj, isAlert) ) {
				if( isAlert ) {
					return false;
				} else {
					hasError = true;
				}
			}
			if( !Field.checkDateFormat(toDateObj, isAlert) ) {
				if( isAlert ) {
					return false;
				} else {
					hasError = true;
				}
			}
			if( hasError ) return false;

			hasError = false;
			var fromDateValue = $(fromDateObj).val();
			var toDateValue = $(toDateObj).val();

			if( !fromDateValue || !toDateValue ) return true;

			var fromDate = new Date( fromDateValue );
			var toDate = new Date( toDateValue );
			var message, fromDateMessage, toDateMessage;

			if( fromDate > toDate ) {
				var fromDateTitle, toDateTitle;
				var fromDateSubjectObj = Field.getSubjectObject( fromDateObj );
				var toDateSubjectObj = Field.getSubjectObject( toDateObj );

				if( fromDateSubjectObj ) {
					fromDateTitle = fromDateSubjectObj.innerHTML;
				}
				if( toDateSubjectObj ) {
					toDateTitle = toDateSubjectObj.innerHTML;
				}
				if( !fromDateTitle ) {
					fromDateTitle = "<mtl:message key="jsp.FIELD_DPR_STARTDATE" encodeScript="true"/>";
				}
				if( !toDateTitle ) {
					toDateTitle = "<mtl:message key="jsp.FIELD_DPR_ENDDATE" encodeScript="true"/>";
				}

				message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_DATESCOPE", new String[] {"%ARG_1%", "%ARG_2%"}) ) %>";
				if( isAlert ) {
					return Field.alertError( fromDateObj, message, fromDateTitle, toDateTitle );
				}
				fromDateMessage = makeMessage( message, [ fromDateTitle, toDateTitle ] );
				toDateMessage = makeMessage( message, [ toDateTitle, fromDateTitle ] );
				hasError = true;
			}

			if( !isAlert ) {
				toggleFieldErrorMessage( fromDateObj, hasError, fromDateMessage );
				toggleFieldErrorMessage( toDateObj, hasError, toDateMessage );
			}
			return !hasError;
		}

		Field.checkMandatory = function( elementObj, isAlert, subjectTitle ) {
			var hasError = false;
			if( Field.getValue(elementObj).length == 0 ) {
				if( isAlert ) {
					if( !subjectTitle ) {
						var subjectObj = Field.getSubjectObject( elementObj );
						if( subjectObj ) subjectTitle = subjectObj.innerHTML;
					}

					if( subjectTitle ) {
						var message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_CANNOT_NULL", "%ARG_1%") ) %>";
						return Field.alertError( elementObj, message, subjectTitle );
					} else {
						return Field.alertError( elementObj, "<mtl:message key="ERR_CANNOT_NULL" encodeScript="true"/>" );
					}
				}
				hasError = true;
			}
			if( !isAlert ) {
				toggleFieldErrorMessage( elementObj, hasError, "<mtl:message key="jsp.MSG_ERR_INPUT_TEXT_MANDATORY"/>" );
			}

			return !hasError;
		}

		Field.checkNumberFormat = function( elementObj, isAlert, real, minus, subjectTitle ) {
			var hasError = false;
			var value = Field.getValue( elementObj );
			var message;
			if( value.length > 0 ) {
				var error = 0;
				if( isFinite(value) ) {
					value = eval( value );
					if( !real && Math.ceil(value) != value )
						error = 1;
					else if( !minus && value < 0 )
						error = 2;
				} else
					error = 3;

				if( error > 0 ) {
					if( !subjectTitle ) {
						var subjectObj = Field.getSubjectObject( elementObj );
						if( subjectObj ) subjectTitle = subjectObj.innerHTML;
					}
					if( subjectTitle ) {
						if( error == 1 ) {
							message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_NUMBER_REAL", "%ARG_1%") ) %>";
						} else if( error == 2 ) {
							message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_NUMBER_MINUS", "%ARG_1%") ) %>";
						} else {
							message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_NUMBER", "%ARG_1%") ) %>";
						}
						if( isAlert ) {
							return Field.alertError( elementObj, message, subjectTitle );
						}
						message = makeMessage( message, subjectTitle );
					} else {
						if( error == 1 )
							message = "<mtl:message key="ERR_INVALID_NUMBER_REAL" encodeScript="true"/>";
						else if( error == 2 )
							message = "<mtl:message key="ERR_INVALID_NUMBER_MINUS" encodeScript="true"/>";
						else if( error == 3 )
							message = "<mtl:message key="ERR_INVALID_NUMBER" encodeScript="true"/>";

						if( isAlert ) {
							return Field.alertError( elementObj, message );
						}
					}
					hasError = true;
				}
			}
			if( !isAlert ) {
				toggleFieldErrorMessage( elementObj, hasError, message );
			}

			return !hasError;
		}

		Field.checkNumberRange = function( elementObj, isAlert, minValue, maxValue, rangeType, subjectTitle ) {
			var hasError = false;
			var value = Field.getValue( elementObj );
			if( value.length > 0 ) {
				if( !subjectTitle ) {
					var subjectObj = Field.getSubjectObject( elementObj );
					if( subjectObj ) subjectTitle = subjectObj.innerHTML;
				}

				if( isFinite(value) ) {
					value = eval( value );
					if( minValue != null && (value < minValue || (value == minValue && (rangeType & 0x01) == 0)) ) {
						if( subjectTitle ) {
							var message;
							if( (rangeType & 0x01) == 0 )
								message = "<%= HtmlUtility.toScriptString(
										msghandler.getMessage("ERR_INVALID_NUMBERRANGE_MIN", new String[] {"%ARG_1%", "%ARG_2%"}) ) %>";
							else
								message = "<%= HtmlUtility.toScriptString(
										msghandler.getMessage("ERR_INVALID_NUMBERRANGE_MINEQ", new String[] {"%ARG_1%", "%ARG_2%"}) ) %>";

							if( isAlert ) {
								return Field.alertError( elementObj, message, subjectTitle, minValue );
							}
							message = makeMessage( message, subjectTitle, minValue );
						} else {
							message = "<mtl:message key="ERR_INVALID_NUMBERRANGE_MIN" encodeScript="true"/>";
							if( isAlert ) {
								return Field.alertError( elementObj, message );
							}
						}
						hasError = true;
					} else if( maxValue != null && (value > maxValue || (value == maxValue && (rangeType & 0x02) == 0)) ) {
						if( subjectTitle ) {
							var message;
							if( (rangeType & 0x02) == 0 )
								message = "<%= HtmlUtility.toScriptString(
										msghandler.getMessage("ERR_INVALID_NUMBERRANGE_MAX", new String[] {"%ARG_1%", "%ARG_2%"}) ) %>";
							else
								message = "<%= HtmlUtility.toScriptString(
										msghandler.getMessage("ERR_INVALID_NUMBERRANGE_MAXEQ", new String[] {"%ARG_1%", "%ARG_2%"}) ) %>";

							if( isAlert ) {
								return Field.alertError( elementObj, message, subjectTitle, maxValue );
							}
							message = makeMessage( message, subjectTitle, maxValue );
						} else {
							message = "<mtl:message key="ERR_INVALID_NUMBERRANGE_MAX" encodeScript="true"/>";
							if( isAlert ) {
								return Field.alertError( elementObj, message );
							}
						}
						hasError = true;
					}
				} else {
					if( isAlert ) {
						if( subjectTitle ) {
							var message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_NUMBER", "%ARG_1%") ) %>";
							return Field.alertError( elementObj, message, subjectTitle );
						} else
							return Field.alertError( elementObj, "<mtl:message key="ERR_INVALID_NUMBER" encodeScript="true"/>" );
					}
					hasError = true;
					message = "<mtl:message key="ERR_INVALID_NUMBER" encodeScript="true"/>";
				}
			}
			if( !isAlert ) {
				toggleFieldErrorMessage( elementObj, hasError, message );
			}
			return !hasError;
		}

		Field.checkTimeFormat = function( elementObj, subjectTitle ) {
			if( elementObj.value && !TimeField.checkValue(elementObj.value) ) {
				if( !subjectTitle ) {
					var subjectObj = Field.getSubjectObject( elementObj );
					if( subjectObj ) subjectTitle = subjectObj.innerHTML;
				}
				if( subjectTitle ) {
					var message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("ERR_INVALID_TIME", "%ARG_1%") ) %>";
					return Field.alertError( elementObj, message, subjectTitle );
				} else
					return Field.alertError( elementObj, "<mtl:message key="ERR_INVALID_TIME" encodeScript="true"/>" );
			}

			return true;
		}

		function readPartyAttributeReq( url, type, form ) {
			if( !type ) return;
			if( !form ) {
				form = frmCond;
			}
			if( $(form.organizationCode).length && !Field.checkMandatory(form.organizationCode) ) {
				toggleDisabled( true, [ form.distributionChannelCode, form.officeCode, form.groupCode
										, form.partyCode, form.soldPartyCode, form.shipPartyCode ] );

				return;
			} else if( $(form.distributionChannelCode).hasClass("mandatory") && $(form.distributionChannelCode).length && !Field.checkMandatory(form.distributionChannelCode) ) {
				toggleDisabled( false, form.distributionChannelCode );
				toggleDisabled( true, [ form.officeCode, form.groupCode
						, form.partyCode, form.soldPartyCode, form.shipPartyCode ] );

				return;
			}

			url = replaceQueryValue( url, "ctype", type );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			if( $(form.organizationCode).length ) {
				url = replaceQueryValue( url, "organizationCode", $(form.organizationCode).val() );
			}
			url = replaceQueryValue( url, "divisionCode", "<mtl:value id="condition" key="division"/>" );

			if( $(form.distributionChannelCode).length ) {
				if( $(form.distributionChannelCode).singleSelectmenu("instance") ) {
					if( $(form.distributionChannelCode).singleSelectmenu("option", "disabled") == false ) {
						url = replaceQueryValue( url, "distributionChannelCode", $(form.distributionChannelCode).val() );
					}
				} else {
					if( $(form.distributionChannelCode).attr("disabled") != "disabled" ) {
						$(form).find( "input[name=distributionChannelCode]:checked" ).each( function() {
							url += "&distributionChannelCode=" + $(this).val();
						});
					}
				}
			}
			if( $(form.officeCode).length && $(form.officeCode).val().length > 0 ) {
				url = replaceQueryValue( url, "officeCode", $(form.officeCode).val() );
			}
			if( $(form.groupCode).length && $(form.groupCode).val().length > 0 ) {
				url = replaceQueryValue( url, "groupCode", $(form.groupCode).val() );
			}
			if( $(form.plantCode).length && $(form.plantCode).val().length > 0 ) {
				url = replaceQueryValue( url, "plantCode", $(form.plantCode).val() );
			}
			if( $(form.partyCode).length && $(form.partyCode).val().length > 0 ) {
				url = replaceQueryValue( url, "partyCode", $(form.partyCode).val() );
			}
			if( $(form.soldPartyCode).length && $(form.soldPartyCode).val().length > 0 ) {
				url = replaceQueryValue( url, "partyCode", $(form.soldPartyCode).val() );
			}

			var attributeTypes = type.split( ";" );
			$.each( attributeTypes, function(index, type) {
				switch( type ) {
				case "DC":
					toggleDisabled( true, form.distributionChannelCode );
					break;
				case "SO":
					toggleDisabled( true, form.officeCode );
					break;
				case "SG":
					toggleDisabled( true, form.groupCode );
					break;
				case "SOLD":
					toggleDisabled( true, form.partyCode );
					toggleDisabled( true, form.soldPartyCode );
					break;
				case "SHIP":
					toggleDisabled( true, form.shipPartyCode );
					break;
				case "PT":
					toggleDisabled( true, form.plantCode );
					break;
				}
			});

			windowOpen( url, "clsName" );
		}
	</script>
