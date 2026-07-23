<%--
	File Name:	include_ics_board_editor.inc
	Version:	2.2.5

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2018/04/30		2.2.5	change ckeditor default url
	jbaek		2016/08/31		2.2.4	add mediumeditor, ckeditor
	GimHS		2015/05/29		2.2.3	CrossBrowsing ����: ��Ÿ ����
	kls1989		2012/12/31		2.2.2	CrossBrowsing ����: ����÷�� width ����
	GimHS		2012/12/31		2.2.2	CrossBrowsing ����: 'cursor: hand;' -> 'cursor: pointer;' ��Ÿ�� ����
	stghr12		2010/07/31		2.2.1	����
	lsinji		2009/10/25		2.2.0	create
--%>

	<%	String defaultEditorType = com.irt.rbm.RBMSystem.getSystemEnv("ICS", "Editor;default", "mediumeditor");
		String editorType = request.getParameter("editorType");//*requested editorType
		if( editorType == null || editorType.length() == 0 ) {
			editorType = defaultEditorType;
		} else if( !("ckeditor".equals(editorType) || "mediumeditor".equals(editorType)) ) { %>
			<script type='text/javascript'>
			customPopup.alert( { "header" : "<%=msghandler.getMessage("MSG_ICS_EDITOR_INVALID_PARAMETER_EDITORTYPE")%>" } );
			location.href = replaceQueryValue(location.href, 'editorType', '');
			</script>
	<%	} %>
	<%	java.util.Locale locale = htmlpage.getLocale();
		String ckeditorURL = null;
		String customCKConfigURL = null;
		String[] fontsArray = null;
		String meThemeName = null;

		if( "ckeditor".equals(editorType) ) {
			ckeditorURL = com.irt.rbm.RBMSystem.getSystemEnv("ICS", "CKEditor;ckeditorURL"
				, systemConfig.getBaseURL(locale) +"../ckeditor/" );
			if( ckeditorURL.charAt( ckeditorURL.length() -1 ) != '/' )
				ckeditorURL = ckeditorURL+"/";

			customCKConfigURL = com.irt.rbm.RBMSystem.getSystemEnv("ICS", "CKEditor;customCKConfigURL"
				, systemConfig.getBaseURL(locale) +"../ckeditor/customCKConfig.js" );
		} else if( "mediumeditor".equals(editorType) ) {
			String defaultFontsAvailable = "Arial,Helvetica,Times New Roman,dotum,gulim,NanumGothic";
			String fontsAvailable = com.irt.rbm.RBMSystem.getSystemEnv("ICS", "Fonts;"+locale.getLanguage()
				, defaultFontsAvailable);
			fontsArray = fontsAvailable.split(",");
			for(int i=0; i< fontsArray.length; i++) fontsArray[i] = "'"+ fontsArray[i].trim() +"'";
			meThemeName = com.irt.rbm.RBMSystem.getSystemEnv("ICS", "MediumEditor;theme", "default");
		}
	%>

	<% if( "ckeditor".equals(editorType ) ) { %>
	<script type='text/javascript' src='<%= ckeditorURL %>ckeditor.js'></script>
	<% } else if( "mediumeditor".equals(editorType ) ) { %>
	<%-- <link type='text/css' rel='stylesheet' href='<%= systemConfig.getClassURL()+"/../" %>html/fonts/font-awesome.min.css'> --%>
	<link type='text/css' rel='stylesheet' href='script/mediumeditor/css/medium-editor.min.css'>
	<link type='text/css' rel='stylesheet' href='script/mediumeditor/css/medium-editor-insert-plugin.min.css'>
	<link type='text/css' rel='stylesheet' href='script/mediumeditor/css/themes/<%=meThemeName%>.min.css'>

	<script type='text/javascript' src='script/mediumeditor/medium-editor.min.js'></script>
	<script type='text/javascript' src="script/mediumeditor/handlebars.runtime.min.js"></script>
	<script type='text/javascript' src="script/mediumeditor/jquery-sortable-min.js"></script>
	<script type='text/javascript' src="script/mediumeditor/jquery.ui.widget.js"></script>
	<script type='text/javascript' src="script/mediumeditor/jquery.iframe-transport.js"></script>
	<script type='text/javascript' src="script/mediumeditor/jquery.fileupload.js"></script>
	<script type='text/javascript' src="script/mediumeditor/canvas-to-blob.min.js"></script>
	<script type='text/javascript' src='script/mediumeditor/medium-editor-insert-plugin.ics.js'></script>
	<% } %>

	<style type="text/css">
		@import url(https://fonts.googleapis.com/earlyaccess/nanumgothic.css);
		@font-face { font-family: 'Nanum Gothic', '&#45208;&#45588;&#44256;&#46357;', sans-serif; }
	</style>


<%----------------------------------------------------------------------------------------------------
--	Attach
--		Attach.MAX_SIZE
--		Attach.INPUT_ID
--		Attach.DISPLAY_ID
--		Attach.attachList
----------------------------------------------------------------------------------------------------%>
	<script type='text/javascript'>
		function Attach( id, fileName ) {
			this.id = id;
			this.fileName = fileName;
			this.html = "&nbsp;<font color='blue'>"+ fileName +"</font>"
					+ " <a onclick='JavaScript: Attach.remove("+ Attach.attachList.length +");' style='cursor: pointer;' >"
					+ "<img src='images/board/ico_attach_delete.gif' align='absmiddle'></a>";
			this.span = document.getElementById( Attach.INPUT_ID + Attach.attachList.length );
			this.display = true;
		}
		Attach.MAX_SIZE = -1;
		Attach.INPUT_ID = null;
		Attach.DISPLAY_ID = null;
		Attach.attachList = new Array();

		Attach.appendFile = function() {
			var idx = Attach.attachList.length + 1;
			var span = document.getElementById( Attach.INPUT_ID + (idx-1) );
			if( span ) span.style.display = "none";

			var attachHTML =
				"<span id='"+ Attach.INPUT_ID +"_"+ idx +"'>"
				+ "<input type='file' name='file"+ idx +"' onClick='JavaScript: return Attach.checkSize();' onChange='JavaScript: Attach.attach(this);' size='1' style='width: 80px; cursor: pointer;'>"
				+ "</span>";

			document.getElementById( Attach.INPUT_ID ).insertAdjacentHTML( "afterEnd", attachHTML );
		}

		Attach.attach = function( fileObj ) {
			Attach.attachList.push( new Attach( null, fileObj.value ) );
			Attach.appendFile();
			Attach.print();
		}

		Attach.checkSize = function() {
			if( Attach.MAX_SIZE >= 0 && Attach.getSize() >= Attach.MAX_SIZE ) {
				var message = "<%= HtmlUtility.toScriptString( msghandler.getMessage("jsp.include_ics_board_editor.MSG_EXCEED_MAX_ATTACH_COUNT", "%ARG_0%") ) %>";
				customPopup.alert( { "header" : message.replace(new RegExp("%ARG_0%"), Attach.MAX_SIZE) } );

				return false;
			}

			return true;
		}

		Attach.getSize = function() {
			var count = 0;

			for( var i = 0; i < Attach.attachList.length; i++ )
				if( Attach.attachList[i].display )
					count++;

			return count;
		}

		Attach.print = function() {
			var count = 0;
			var innerHTML = "";

			for( var i = 0; i < Attach.attachList.length; i++ ) {
				if( Attach.attachList[i].display ) {
					count++;
					innerHTML += "<b>"+ count +"</b>."+ Attach.attachList[i].html +"<br>";
				}
			}
			if( count == 0 )
				innerHTML = "<mtl:message key="MSG_ICS_NO_ATTACHFILE" encodeScript="true"/>";

			document.getElementById( Attach.DISPLAY_ID ).innerHTML = innerHTML;
		}

		Attach.remove = function( idx ) {
			Attach.attachList[idx].display = false;
			if( Attach.attachList[idx].span )
				Attach.attachList[idx].span.innerHTML = "";
			Attach.print();
		}
	</script>


<%----------------------------------------------------------------------------------------------------
--	JSON serializeObject
----------------------------------------------------------------------------------------------------%>
	<script type='text/javascript'>
		$.fn.serializeObject=function(){
			"use strict";var a={},b=function(b,c){var d=a[c.name];
			"undefined"!=typeof d&&d!==null?$.isArray(d)?d.push(c.value):a[c.name]=[d,c.value]:a[c.name]=c.value};
			return $.each(this.serializeArray(),b),a
		};
	</script>


<%----------------------------------------------------------------------------------------------------
--	BoardEditor
----------------------------------------------------------------------------------------------------%>
	<script type='text/javascript'>
		function BoardEditor() {};

		BoardEditor.appendHtmlToEditor = function( html ) {
			if( !BoardEditor.checkEnableEditor() ) return;

			if( BoardEditor.getEditorType() == 'ckeditor' ) {
				//ck QuickImageInsert
				var oEditor = CKEDITOR.instances.content.editable();
				oEditor.insertHtml( html );
			}
		}

		BoardEditor.checkEnableEditor = function() {
			if( typeof MediumEditor != 'undefined' || typeof CKEDITOR != 'undefined' )
				return true;
			else
				customPopup.alert( { "header" : "<mtl:message key="MSG_ICS_EDITOR_NOT_LOADED" encodeScript="true"/>" } );
			return false;
		}

		BoardEditor.createEditor = function( name, width, height ) {
			if( !BoardEditor.checkEnableEditor() ) return;

			var actionType = 'create';
			if( BoardEditor.getEditorType(actionType) == 'ckeditor' ) {
				return BoardEditor.createCKEditor( name, width, height );
			} else if( BoardEditor.getEditorType(actionType) == 'mediumeditor' ) {
				return BoardEditor.createMediumEditor();
			}
		}

		BoardEditor.createCKEditor = function( name, width, height ) {
			var $uploadForm = $('[name=frmImage]').clone().attr('name', 'dgfrmImage');
			$uploadForm.find('#box_upload_image').remove();
			$uploadForm.append( $('<input/>').attr('type', 'file').attr('name', 'imagefile')
				.attr('onchange', 'JavaScript: BoardEditorImage.regist(this, \'ckImageUpload\')') );

			if( $uploadForm.length == 1 ) {
				var uploadForm = $uploadForm[0].outerHTML;
				CKEDITOR.on('dialogDefinition', function (ev) {
					var dialogName = ev.data.name;
					var dialogDefinition = ev.data.definition;
					if (dialogName == 'image') {
						dialogDefinition.removeContents('Upload');
						dialogDefinition.addContents({ title: "<mtl:message key='MSG_ICS_BOARD_CK_TAB_IMAGEUPLOAD_TITLE' encodeScript='true'/>",
							id: "upload",
							label: "<mtl:message key='MSG_ICS_BOARD_CK_TAB_IMAGEUPLOAD_FORM_LABEL' encodeScript='true'/>",
							elements: [{ type: "html",
							html: uploadForm }]
						});
					}
				});
			}

			editor = CKEDITOR.replace( name, {
				width: width,
				height: height,
				customConfig: "<%=customCKConfigURL%>",
				language:"<%= htmlpage.getLocale() %>",
			});

			return editor;
		}

		BoardEditor.mediumEditorConfig = function() {
			var FontSizeChangeButton = MediumEditor.Extension.extend({
				action: 'fontSize',
				changeType: 'clear',//can be [ clear, plus, minus ]. (clear = default = 4)
				name: '',
				aria: '',
				namePattern: 'fontsize',
				ariaPattern: 'FontSize',

				init: function() {
					this.button = this.document.createElement('button');
					this.button.classList.add('medium-editor-action');

					if( this.changeType === 'plus' ) {
						this.name = this.name ? this.name : this.namePattern+'plus';
						this.aria = this.aria ? this.aria : this.ariaPattern+'Plus';
						this.button.title = this.aria;
						this.button.innerHTML = '<i class="fa fa-text-height"></i><i class="fa fa-plus" style="letter-spacing:-10px;">+</i>';
					} else if ( this.changeType === 'minus' ) {
						this.name = this.name ? this.name : this.namePattern+'minus';
						this.aria = this.aria ? this.aria : this.ariaPattern+'Minus';
						this.button.title = this.aria;
						this.button.innerHTML = '<i class="fa fa-text-height"></i><i class="fa fa-minus" style="letter-spacing:-10px;">-</i>';
					} else {
						this.name = this.name ? this.name : this.namePattern+'clear';
						this.aria = this.aria ? this.aria : this.ariaPattern+'Clear';
						this.button.title = this.aria;
						this.button.innerHTML = '<i class="fa fa-text-height"></i><i class="fa fa-circle" style="letter-spacing:-10px;">0</i>';
					}
					this.on(this.button, 'click', this.handleClick.bind(this));
				},
				getButton: function() {
					return this.button;
				},
				clearFontSize: function () {
					MediumEditor.selection.getSelectedElements(this.document).forEach(function (el) {
						if (el.nodeName.toLowerCase() === 'font' && el.hasAttribute('size')) {
							el.removeAttribute('size');
						}
					});
				},
				handleClick: function (event) {
					// Get fontsize of current selection (convert to string since IE returns this as number)
					var size = this.document.queryCommandValue('fontSize') + '';
					if( size === '' || size == "null" ) size = 4;//*default
					if( this.changeType === 'plus' ) {
						if( 7-size > 0 ) size++;
					} else if( this.changeType === 'minus' ){
						if( 1-size < 0 ) size--;
					}
					if (size === '4' || this.changeType === 'clear' ) {
						this.clearFontSize();
					} else {
						this.execAction('fontSize', { size: size, value: size });
					}
					return false;
				},
			});

			return {
				buttonLabels: 'fontawesome',
				anchorPreview: {
					showOnEmptyLinks: false
				},
				placeholder: { text: "<mtl:message key='MSG_ICS_BOARD_ME_PLACEHOLDERTEXT' encodeScript='true'/>" },
				fontName: {
					aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_FONTNAME' encodeScript='true'/>",
					fonts: <%= java.util.Arrays.toString(fontsArray) %>
				},
				toolbar: {
					buttons: [
					{ name:'bold', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_BOLD' encodeScript='true'/>"},
					{ name:'italic', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_ITALIC' encodeScript='true'/>"},
					{ name:'underline', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_UNDERLINE' encodeScript='true'/>"},
					{ name:'strikethrough', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_STRIKETHROUGH' encodeScript='true'/>"},
					{ name:'fontname' }, //*fontname toolbar name defined as 'fontName'
					{ name:'fontsizeplus' },
					{ name:'fontsizeclear' },
					{ name:'fontsizeminus' },
					{ name:'anchor', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_ANCHOR' encodeScript='true'/>",
						placeholderText: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_ANCHOR_PLACEHOLDERTEXT' encodeScript='true'/>"
					},
					{ name:'justifyLeft', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_JUSTIFYLEFT' encodeScript='true'/>"},
					{ name:'justifyCenter', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_JUSTIFYCENTER' encodeScript='true'/>"},
					{ name:'justifyRight', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_JUSTIFYRIGHT' encodeScript='true'/>"},
					{ name:'justifyFull', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_JUSTIFYFULL' encodeScript='true'/>"},
					{ name:'superscript', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_SUPERSCRIPT' encodeScript='true'/>"},
					{ name:'subscript', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_SUBSCRIPT' encodeScript='true'/>"},
					{ name:'orderedlist', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_ORDEREDLIST' encodeScript='true'/>"},
					{ name:'unorderedlist', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_UNORDEREDLIST' encodeScript='true'/>"},
					{ name:'outdent', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_OUTDENT' encodeScript='true'/>"},
					{ name:'indent', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_INDENT' encodeScript='true'/>"},
					{ name:'h1', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_H1' encodeScript='true'/>"},
					{ name:'h2', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_H2' encodeScript='true'/>"},
					{ name:'h3', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_H3' encodeScript='true'/>"}
					],
				},
				extensions: {
					'imageDragging': {},
					'fontsizeplus': new FontSizeChangeButton({changeType:'plus', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_FONTSIZEPLUS' encodeScript='true'/>" }),
					'fontsizeclear': new FontSizeChangeButton({changeType:'clear', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_FONTSIZECLEAR' encodeScript='true'/>" }),
					'fontsizeminus': new FontSizeChangeButton({changeType:'minus', aria: "<mtl:message key='MSG_ICS_BOARD_ME_TOOLBAR_FONTSIZEMINUS' encodeScript='true'/>" }),
				},
				checkLinkFormat: function (value) {
					var re = /^(https?|ftps?|rtmpt?):\/\/|mailto:/;
					return (re.test(value) ? '' : 'http://') + value;
				}
			};
		};

		BoardEditor.createMediumEditor = function() {
			$('textarea[name=content]').addClass('editable');

			var editor = new MediumEditor('.editable', BoardEditor.mediumEditorConfig());

			$('.editable').mediumInsert({
				editor: editor, addons: {
					images: {
						preview: true,
						showOnEmptyLinks: false,
						fileUploadOptions: {
							url: "<%= systemConfig.getClassURL()%>/ICSBoardAttach",
							formData: $.extend(
								$('[name=frmImage]').serializeObject(), { reqUploadFn: 'mediumInsert',
									imgAttachManageUrl: BoardEditorImage.getImageURL(
											$('[name=frmImage]').serializeObject().attachManageKey, null ) }
							),
							type: 'POST',
							autoupload: true,
							acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
							done: function(e, data) {},
							success: function(e, data) {},
							always: function(e, data) {}
						},
						uploadCompleted: function($el, data) {
							var attachManageKey = data.jqXHR.responseJSON.files[0].attachManageKey;
							var attachNumber = data.jqXHR.responseJSON.files[0].attachNumber;
							var imgId = attachManageKey + ';' + attachNumber;

							//$el is <figure/>
							if( $el.length > 0 ) {
								var $img = $el.find('img');
								$img.attr( 'id', imgId );

								//debugTrap
								if( $el.length > 1 )
									console.log( 'uploadCompleted(): $el has many img for imgId: '+ imgId + ' length: '+ $el.length );
							} else {
								//debugTrap
								console.log( 'uploadCompleted(): cannot find $el for imgId: '+ imgId );
							}
						},
						deleteScript: "<%= systemConfig.getClassURL() %>/ICSBoardAttach",
						deleteMethod: "POST",
						fileDeleteOptions: { mode: "del", attachManageKey: frmMain.attachManageKey.value },
						styles: {
							wide: { added: function($place) { $place.find('figure').andSelf().attr('unselectable', 'on'); } },
							left: { added: function($place) { $place.find('figure').andSelf().attr('unselectable', 'on'); } },
							right: { added: function($place) { $place.find('figure').andSelf().attr('unselectable', 'on'); } },
							grid: { added: function($place) { $place.find('figure').andSelf().attr('unselectable', 'on'); } },
						},
						captionPlaceholder: "<mtl:message key='MSG_ICS_BOARD_ME_IMAGES_CAPTION_PHOLDERTEXT' encodeScript='true'/>"
					},
					embeds: {
						placeholder: "<mtl:message key='MSG_ICS_BOARD_ME_EMBEDS_PHOLDERTEXT' encodeScript='true'/>",
						captionPlaceholder: "<mtl:message key='MSG_ICS_BOARD_ME_EMBEDS_CAPTION_PHOLDERTEXT' encodeScript='true'/>"
					}
				}
				});

				window.mediumEditor = editor;
			return editor;
		}

		BoardEditor.getEditorType = function(actionType) {
			var editorType = "<%= editorType %>";//*deploytime config
			if( actionType == 'create' ) {
				return editorType;
			} else if( BoardEditor.checkEnableEditor() ) {//*returns runtime config
				if( typeof window.mediumEditor != 'undefined'
					&& typeof window.mediumEditor.elements[0] != 'undefined'
					&& window.mediumEditor.elements[0].getAttribute('name') == 'content' ) {
					return 'mediumeditor';
				} else if( typeof CKEDITOR != undefined
					&& typeof CKEDITOR.instances['content'] != 'undefined' ) {
					return 'ckeditor';
				}
			} else
				return 'plain';
		}

		BoardEditor.getImageElementFromEditor = function() {
			if( !BoardEditor.checkEnableEditor() ) return;

			if( BoardEditor.getEditorType() == 'ckeditor' ) {
				var editorInstance = CKEDITOR.instances.content.editable();
				return editorInstance.getElementsByTag( "IMG" ).$;
			} else if( BoardEditor.getEditorType() == 'mediumeditor' ) {
				return $(window.mediumEditor.origElements).find('img');
			}

			return null;
		}

		BoardEditor.removeElementOnEditor = function( elementId ) {
			if( !BoardEditor.checkEnableEditor() ) return;

			if( BoardEditor.getEditorType() == 'ckeditor' ) {
				var oEditor = CKEDITOR.instances['content'];
				if( oEditor != '' && typeof oEditor != 'undefined' && oEditor.mode == 'wysiwyg' ) {
					var element = oEditor.document.$.getElementById( elementId );
					if( element )
						element.parentNode.removeChild( element );
				}
			}
		}

		BoardEditor.submitReady = function() {
			if( BoardEditor.getEditorType() == 'mediumeditor' ) {
				var contentObjVal = window.mediumEditor.serialize()[ window.mediumEditor.elements[0].id ].value;
				var textareaSelector = 'textarea[medium-editor-textarea-id='+ window.mediumEditor.elements[0].id +']';
				$( textareaSelector ).val( contentObjVal );
			}
		}
	</script>


<%----------------------------------------------------------------------------------------------------
--	BoardEditorImage
----------------------------------------------------------------------------------------------------%>
	<script type='text/javascript'>
		function BoardEditorImage() {};

		BoardEditorImage.imageFile;
		BoardEditorImage.request;
		BoardEditorImage.registType;

		BoardEditorImage.regist = function( imageFile, actionType ) {
			BoardEditorImage.imageFile = imageFile;
			BoardEditorImage.registType = actionType || 'insertHTML';
			if( AFU.submit( imageFile.form, {'onStart' : BoardEditorImage.registReady, 'onComplete' : BoardEditorImage.registComplete} ) )
			imageFile.form.submit();
		}

		BoardEditorImage.getImageURL = function( attachManageKey, attachNumber ) {
			var url = "<%= systemConfig.getClassURL() %>/ICSBoardAttach?mode=show";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "attachManageKey", attachManageKey );
			url = replaceQueryValue( url, "attachNumber", attachNumber );

			return url;
		}

		BoardEditorImage.registReady = function() {
			var imageFile = BoardEditorImage.imageFile;
			if( imageFile.value ) {
				var fileName = imageFile.value;
				if( !fileName.match(/\.(jpg)$/i) && !fileName.match(/\.(jpeg)$/i) && !fileName.match(/\.(gif)$/i) && !fileName.match(/\.(png)$/i) ) {
					customPopup.alert( { "header" : "<mtl:message key="ERR_ICS_NOT_SUPPORT_IMAGE_EXTENSION" encodeScript="true"/>" } );
					return false;
				}
			} else {
				customPopup.alert( { "header" : "<mtl:message key="MSG_ICS_BOARD_SELECT_ATTACH_IMAGE" encodeScript="true"/>" } );
				return false;
			}

			return true;
		}

		BoardEditorImage.registComplete = function( responseText ) {
			BoardEditorImage.imageFile.form.reset();

			if( responseText != '' && typeof(responseText) != 'undefined' ) {
				if( responseText.indexOf("HTTP Status 500") > 0)
					customPopup.alert( { "header" : "<mtl:message key="jsp.include_ics_board_editor.MSG_REQUEST_IMG_HTTP_500" encodeScript="true"/>" } );
				else if( responseText.indexOf("HTTP Status 404") > 0)
					customPopup.alert( { "header" : "<mtl:message key="jsp.include_ics_board_editor.MSG_REQUEST_IMG_HTTP_404" encodeScript="true"/>" } );
				else if( responseText.indexOf("HTTP Status 403") > 0)
					customPopup.alert( { "header" : "<mtl:message key="jsp.include_ics_board_editor.MSG_REQUEST_IMG_HTTP_403" encodeScript="true"/>" } );
				else if( responseText.indexOf("IMAGE: ") == 0 ) {
					var values = responseText.substring( "IMAGE: ".length ).split( ";" );

					if( BoardEditorImage.registType == 'ckImageUpload' ) {
						var curDialog = CKEDITOR.dialog.getCurrent();
						curDialog.getContentElement('info', 'txtUrl').setValue( BoardEditorImage.getImageURL(values[0], values[1]) );
						curDialog.getContentElement('advanced', 'linkId').setValue( values[0] +';'+ values[1] );
						curDialog.getContentElement('advanced', 'linkId').getInputElement().setAttribute('readOnly', true);
						curDialog.selectPage('info');
					} else if( BoardEditorImage.registType == 'insertHTML' ) {
						var html = "<img id='"+ values[0] +";"+ values[1] +"' src='"+ BoardEditorImage.getImageURL(values[0], values[1]) +"'>";
						BoardEditor.appendHtmlToEditor( html );
					}
				}
			}
		}

		BoardEditorImage.remove = function( attachManageKey, attachNumber ) {
			if( BoardEditorImage.request != null && typeof BoardEditorImage.request.readyState != 'undefined' ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_ICS_REQUEST_ALREADY_USED" encodeScript="true"/>" } );
				return;
			}
			BoardEditorImage.request = createXMLRequest();
			if( commentRequest != null ) {
				var url = "<%= systemConfig.getClassURL() %>/ICSBoardAttach";
				url = replaceQueryValue( url, "mode", "del" );
				url = replaceQueryValue( url, "attachManageKey", attachManageKey );
				url = replaceQueryValue( url, "attachNumber", attachNumber );

				request.onreadystatechange = BoardEditorImage.removeComplete;
				request.open( "GET", url, true );
				request.send( null );
			}
		}

		BoardEditorImage.removeComplete = function() {
			if( BoardEditorImage.request.readyState == 4 ) {
				var responseText = BoardEditorImage.request.responseText;

				if( responseText != null && responseText.indexOf("<html>") > 0 )
					showResponse( responseText );
				else if( responseText.indexOf("IMAGE: ") == 0 ) {
					var values = responseText.substring( "IMAGE: ".length ).split( ";" );
					BoardEditor.removeElementOnEditor( values[0] +";"+ values[1] );
				} else
					showResponse( responseText );
			}
		}
	</script>
