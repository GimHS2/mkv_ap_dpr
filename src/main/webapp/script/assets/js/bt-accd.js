/*
  Name:	bt-accd.js
  Version:	2.2.0

  Description:

  Note:
	Accd

  Modified	(YYYY/MM/DD)	Ver		Content
  jbaek		2020/03/30		2.2.0	create
*/


(function(self){
	var Accd = {
		isParentTabPage: function( cgroup ) {
			return _.includes(cgroup.parentNode.classList, "tab-page");
		},
		findTabPage: function ( cgroup ) {
			if( !Accd.isParentTabPage(cgroup) ) return;
			var tabPage;
			for( var t = 0; t < TabPane.tabPanes.length; t++ ) {
				for( var i = 0; i < TabPane.tabPanes[t].pages.length; i++ ) {
					if( TabPane.tabPanes[t].pages[i].element === cgroup.parentNode ) {
						tabPage = TabPane.tabPanes[t].pages[i];
						break;
					}
				}
				if( tabPage )
					break;
			}
			return tabPage;
		},
		getTabPageTitles: function( tabPage ) {
			if( ! (tabPage instanceof TabPage) ) return [];
			return _.map(tabPage.tabPane.element.querySelectorAll(".tabpane_tabrow .tab"), function(tab){
				return tab.textContent;
			});
		},
		getContentGroupMap: function() {
			return _.groupBy(document.querySelectorAll(".content_group"), function(cgrp){
				if( _.includes(cgrp.classList, "tabpane") ) {
					return "tabpane";
				} else if( _.includes(cgrp.classList, "tabpane_tabrow") ) {
					return "tabrow";
				} else {
					return "cgroup";
				}
			});
		},
		getSubPageTitle: function( subpage ) {
			if( _.includes(subpage.classList, "tab-page") ) {
				return Accd.getTabPageTitles(subpage.tabPage)[subpage.tabPage.index];
			} else {
				var desc = subpage.querySelector(".content_group_description");
				if( desc )
					return desc.textContent;
				else if( subpage.getAttribute("data-description") ) {
					return subpage.getAttribute("data-description");
				}
				else {
					var cgrpType = Accd.getContentGroupType( subpage );
					switch(cgrpType){
					case 'fieldset':
					case 'content':
						return "Content";
					case 'list':
						return "List";
					case 'search':
						return "Search";
					}
				}
			}
		},
		getContentGroupType: function( cgroup ) {
			var cgrpType = _.find(cgroup.classList, function(className){
				return ( className.match(/_content$/) ) ;
			});
			return ( cgrpType ? cgrpType.replace(/_content$/, "") : "" );
		},

		strHash: function( str ) {
			var hash = 0, i, chr;
			if (str.length === 0) return hash;
			for (i = 0; i < str.length; i++) {
				chr	  = str.charCodeAt(i);
				hash  = ((hash << 5) - hash) + chr;
				hash |= 0; // Convert to 32bit integer
			}
			return hash;
		},
		getPeek: function( subpage ) {
			var $cardPeek;
				$cardPeek = $("<span class='accd-peek mb-0 ml-auto d-flex flex-wrap justify-content-between'>");
			var peekData = $(subpage).find("[data-accd-peek-keys]").attr("data-accd-peek-keys");
			var peekObjs = _.map((peekData ? peekData.split(",") : null), function(key){
				var el = document.querySelector("#val_"+key);
				return ( el ? el : document.querySelector("[name="+key+"]") );
			});
			var getPeekValue = function( peekObj ) {
				if( peekObj ) {
					if( peekObj.tagName === "SPAN" && _.includes(peekObj.classList,"value") ) {
						return peekObj.textContent;
					} else {
						return Field.getValue(peekObj);
					}
				}
				return "";
			};
			var getSubjectObject = function( peekObj ) {
				var name = (Field.isArray(peekObj) ? peekObj[0].name : peekObj.name);
				if( !name && peekObj.id && peekObj.id.match(/^val_/) ) {
					name = peekObj.id.replace(/^val_/, "");
				}
				return document.getElementById( "title_"+  name);
			};
			var getPeekSubject = function( peekObj ) {
				var subjectObj = getSubjectObject(peekObj||"");
				var subject;
				if( subjectObj )
					subject = (subjectObj ? subjectObj.textContent : "");
				return subject;
			};
			var $peeks = _.map(peekObjs, function(peekObj){
				var value = getPeekValue( peekObj );
				var subject = getPeekSubject( peekObj );
				var $subject = $("<span>").addClass("-btx badge badge-light d-none d-sm-inline-block").text(
					(subject? subject+" : " : "")
				);
				var $value = $("<span>").text(value);
				var $peek = $("<span>").attr("title", subject).addClass("-btx p-0 py-1 flex-fill")
					.append($subject)
					.append($value);
				return $peek.tooltip().get();
			});
			if( $peeks && $peeks.length > 0 ) {
				_.each($peeks, function($peek){
					$cardPeek.append( $peek );
				});
			}
			return ( $cardPeek.length > 0 ? $cardPeek[0] : null );
		},
		updatePeek: function( subpage ) {
			var $cardHeader = $(subpage).closest(".card.content_group_accd").find(".card-header");
			var cardPeek = this.getPeek( subpage );
			if( $cardHeader.length > 0 ) {
				if( $cardHeader.find(".accd-peek").length > 0 ) {
					$cardHeader.find(".accd-peek").replaceWith( cardPeek );
				} else {
					$cardHeader.append( cardPeek );
				}
			}
			return cardPeek;
		},
		toAccordion: function( subpages ) {
			if( !subpages || _.isEmpty(subpages) ) return;
			var titleObj = document.querySelector("#content_title .title");
			if( !titleObj ) {
				console.log("titleObj is mandatory");
				return;
			}
			var isTabPage = _.includes(subpages[0].classList, "tab-page");
			var titleText = (titleObj ? titleObj.textContent : "page");

			var accordionId;
			if( isTabPage ) {
				accordionId = $(subpages[0]).parent().closest(".tabpane").attr("id");
			} else {
				accordionId = "accordion_"+Accd.strHash(titleText);
			}
			var accordion = document.querySelector("#"+ accordionId+ ".accordion");
			if( accordion ) {
				console.log("accordion exists already.");
				return accordion;
			}
			// accordion = document.createElement("div");
			var menu = this.createAccordionMenu( subpages );
			if( menu ) {
				this.applyAccordionMenu( menu );
			}
			_.each(subpages, function(subpage){
				var contentId = subpage.getAttribute("id");
				var $card = $(subpage).closest(".card.content_group_accd");
				if( $card.length > 0 && $card.attr("data-content-id") === contentId ) {
					console.log(contentId+"[WARN] content group accordion already setup");
					return;
				}
				// console.log("subpage:"+ contentId, subpage);

				var cardHeader = document.createElement("div");
				var cardHeaderId = contentId + "_header";
				var $cardHeader = $("<div class='-btx card-header py-0 py-sm-1 px-1 px-sm-4'>").attr("id", cardHeaderId);
				//
				var cardHeaderContent = document.querySelector("[id="+cardHeaderId+"].card-header .card-header-content")||document.createElement("div");
				//
				var cardHeaderHead = document.querySelector("[id="+cardHeaderId+"].card-header .accd-title")|| document.createElement("h2");
				cardHeaderHead.classList.toggle("accd-title", true);
				cardHeaderHead.classList.toggle("mb-0", true);
				cardHeaderHead.classList.toggle("d-flex", true);
				var cardHeaderBtn = document.querySelector("button[data-target='"+"#"+contentId+"']") || document.createElement("button");
				cardHeaderBtn.classList.toggle("btn", true);
				cardHeaderBtn.classList.toggle("btn-link", true);
				cardHeaderBtn.classList.toggle("w-100", true);
				cardHeaderBtn.setAttribute("type", "button");
				cardHeaderBtn.setAttribute("data-toggle", "collapse");
				cardHeaderBtn.setAttribute("data-target", "#"+contentId);
				cardHeaderBtn.setAttribute("aria-expanded", true);
				cardHeaderBtn.setAttribute("aria-controls", contentId);
				var $spanHeaderText = $("<span class='-btx p-0 mr-auto'>").text( Accd.getSubPageTitle(subpage) );
				var $spanToggle = $("<span class='-btx p-0 ml-auto'><i class='fa fa-chevron-right fa-1 pull-right'/></span>");
				var $spanHeader = $("<span class='-btx d-flex'>");
				$spanHeader.append( $spanHeaderText );
				$spanHeader.append( $spanToggle );
				$spanHeader.appendTo( cardHeaderBtn );
				cardHeaderHead.appendChild( cardHeaderBtn );
				cardHeaderContent.appendChild( cardHeaderHead );
				var cardHeaderPeek = Accd.getPeek( subpage );
				cardHeaderContent.appendChild( cardHeaderPeek );
				$cardHeader.append( cardHeaderContent );
				//
				subpage.classList.toggle("collapse", true);
				subpage.classList.toggle("show", true);
				subpage.setAttribute("aria-labelledby", cardHeaderId);
				// subpage.setAttribute("data-parent", "#"+accordionId);
				// 'data-parent' attr is bootstrap attribute: if 'data-parent' exists only single panel can be opened.
				// 'data-parent-link' attr is accordition.js attribute: relationship between parent child.
				subpage.setAttribute("data-parent-link", "#"+accordionId);

				var $subpage = $(subpage);
				$subpage.on("hidden.bs.collapse", function(evt){
					var cgId = evt.target.id;
					var $card = $(evt.target).closest(".card.content_group_accd[data-content-id='"+cgId+"']");
					if( $card.length > 0 )
						$card.removeClass("opened");
				});
				$subpage.on("shown.bs.collapse", function(evt){
					var cgId = evt.target.id;
					var $card = $(evt.target).closest(".card.content_group_accd[data-content-id='"+cgId+"']");
					if( $card.length > 0 )
						$card.addClass("opened");
				});
				$subpage.wrapAll("<div class='-btx card-body px-0 py-0 content_group_accd_body'>");
				$subpage.css("display", "")
					.addClass("content_group_accd_content")
					.closest(".card-body.content_group_accd_body")
					.wrapAll("<div class='-btx card content_group_accd opened' data-content-id='"+contentId+"'>");
				$card = $(subpage).parent().closest(".card.content_group_accd[data-content-id='"+contentId+"']");
				// console.log("found card:", $card);
				$card.prepend( $cardHeader );
			});

			if( isTabPage ) {
				accordion = $(subpages[0]).parent().closest(".tabpane")[0];
				var tabrow = accordion.querySelector(".tabpane_tabrow");
				if( tabrow ) tabrow.classList.toggle("d-none", true);
			} else {
				accordion = document.querySelector("body");
			}
			accordion.setAttribute("id", accordionId);
			accordion.classList.toggle("accordion", true);
			// Accd.setupAccordionControlButton( accordion, titleObj );
			return accordion;
		},

		setupAccordionControlButton: function( accordion, buttonParent ) {
			var accordionId = accordion.getAttribute("id");

			var accordionBtn = document.createElement("button");
			accordionBtn.setAttribute("id", accordionId+"_toggle");
			accordionBtn.setAttribute("data-accordion-id", accordionId);
			accordionBtn.textContent = "toggle";
			buttonParent.appendChild(accordionBtn);
			$(accordionBtn).click(function(){
				var accordionId = this.getAttribute("data-accordion-id");
				if( $(this).data("closeAll") ) {
					$("#"+accordionId+" .collapse").removeAttr("data-parent").collapse('show');
				} else {
					$("#"+accordionId+" .collapse").attr("data-parent", "#"+accordionId).collapse('hide');
				}
				//save last state
				$(this).data("closeAll", !$(this).data("closeAll"));
			});
		},
		get: function( subpages ) {
			var els = this.getSubpages( subpages );
			return _.map(els, function(subpage){
				return (subpage ? subpage.closest(".card.content_group_accd") : null);
			});
		},
		getSubpages: function( subpages ) {
			var els = _.map((!_.isArray(subpages) ? [subpages]: subpages), function(subpage, i){
				if( subpage instanceof HTMLElement ) {//
					return subpage;
				} else if( _.isString(subpage) )  {//contentId
					return document.querySelector("#"+subpage+".content_group.collapse");
				} else if( _.isNumber(subpage) ) {//integer
					return $(".accordion").find(".content_group_accd .content_group.collapse").eq(subpage)[0];
				}
				return null;
			});
			return els;
		},
		close: function( subpages ) {
			var els = this.getSubpages( subpages );
			if( els ) {
				var accd = document.querySelector(".accordion");
				if( accd ) {
					var accdId = accd.getAttribute("id");
					// var cgs = _.map(accd.querySelectorAll(".content_group_accd_content.collapse"), function(subpage){
					// 	return subpage;
					// });
					// _.each(cgs, function(subpage){
					// 	if( _.includes(els, o) ) {
					// 		$(this).collapse('hide');

					// });
					$(accd).find(".content_group_accd_content.collapse").each(function(i,o){
						if( _.includes(els, o) ) {
							$(this).collapse('hide');
						} else {
						}
					});
					var checkExists = setInterval(function(evt){
						var $cgs = $(accd).find(".content_group_accd_content_accd_content.collapse:not(.show)")
						.filter(function(i,o){
							return _.includes(els, o);
						});
						if( $cgs.length === els.length ) {
							clearInterval(checkExists);
						}
					});
				}
			}
			return this;
		},
		show: function( subpage ) {
			var el = ( subpage instanceof HTMLElement ? subpage
					   : (_.isString(subpage) ? document.getElementById(subpage)
						  : _.isNumber(subpage) ? $(".accordion .content_group.collapse").eq(subpage)[0] : null) );
			if( el ) {
				var $card = $(el).closest(".card");
				if( $card.length > 0 ) {
					var $open = $($(this).attr('data-parent-link')).find('.collapse.show');
					var additionalOffset = 0;
					if( $card.prevAll().filter($open.closest('.card')).length !== 0 ) {
						additionalOffset = $open.height();
					}
					$('html,body').animate({
						scrollTop: $card.offset().top - additionalOffset
					}, 500);
				}
			}
		},
		open: function( subpages ) {
			var els = this.getSubpages(subpages);
			if( els ) {
				var accd = document.querySelector(".accordion");
				if( accd ) {
					var accdId = accd.getAttribute("id");
					$(accd).find(".content_group.collapse").each(function(i,o){
						if( _.includes(els, o) ) {
							if( els.length > 1 ) {
								// $(this).removeAttr("data-parent").collapse('show');
							} else {
								// $(this).attr("data-parent", "#"+accdId).collapse('show');
							}
							$(this).collapse('show');
						} else {
							// $(this).attr("data-parent", "#"+accdId).collapse('hide');
						}
					});
				}
			}
			return this;
		},
		openOnly: function( subpages ) {
			var els = this.getSubpages(subpages);
			if( els ) {
				var accd = document.querySelector(".accordion");
				if( accd ) {
					var accdId = accd.getAttribute("id");
					var cgIds = _.map(els, function(el){ return (el && el.id); });
					$(accd).find(".content_group.collapse").each(function(i,cg){
						if( _.includes(cgIds, cg.id) ) {
							$(this).attr("data-parent-link", "#"+accdId).collapse('show');
						} else {
							$(this).attr("data-parent-link", "#"+accdId).collapse('hide');
						}
					});
				}
			}
			return this;
		},
		/* open subpages and goto first subpage */
		goto: function( subpages ){
			var els = this.getSubpages(subpages);
			if( els ) {
				if( els.length > 0 ) {
					this.open( _.rest(els) )
						.open( els[0] )
						.show( els[0] );
				}
			}
			return this;
		},
		closeAll: function( ) {
			var accd = document.querySelector(".accordion");
			if( accd ) {
				var accdId = accd.getAttribute("id");
				$("#"+accdId+" .content_group.collapse").attr("data-parent-link", "#"+accdId).collapse('hide');
			}
			return this;
		},
		openAll: function() {
			var accd = document.querySelector(".accordion");
			if( accd ) {
				var accdId = accd.getAttribute("id");
				$("#"+accdId+" .content_group.collapse").removeAttr("data-parent").collapse('show');
			}
			return this;
		},
		applyAccordionMenu: function( menu_el, parent ) {
			parent = parent || document.querySelector("#content_title .title");
			if( parent ) {
				while(parent.childNodes.length> 0 ) {
					parent.removeChild(parent.childNodes[0]);
				}
				parent.appendChild( menu_el );
			}
		},

		setupAccordion: function() {
			var cgrps = Accd.getContentGroupMap().cgroup;
			Accd.toAccordion(cgrps);
			this.initCollapseShowTop();
			return this;
		},

		promiseAccordion: function() {
			return new Promise(function(resolve){
				if( document.querySelector(".accordion") ) {
					return resolve(Accd);
				}

				var willSetuped = Accd.setupAccordion();
				var checkExists = setInterval(function(){
					if( document.querySelector(".accordion") ) {
						resolve( willSetuped );
						clearInterval(checkExists);
					}
				});
			});
		},

		initCollapseShowTop: function() {
			$('.content_group.collapse').on('show.bs.collapse', function(e) {
				var $card = $(this).closest('.card');
				if( $card.length > 0 ) {
					var $open = $($(this).data('parent')).find('.collapse.show');
					var additionalOffset = 0;
					if($card.prevAll().filter($open.closest('.card')).length !== 0) {
						additionalOffset =	$open.height();
					}
					$('html,body').animate({
						scrollTop: $card.offset().top - additionalOffset
					}, 500);
				}
			});
		},

		getAccordionMenuItems: function( subpages ) {
			subpages = ( subpages ? subpages : document.querySelectorAll(".tabpane_tabrow"));
			if( subpages.length == 0 )
				subpages = document.querySelectorAll(".content_group");
			var menuItems =	 _.map(subpages, function(subpage, page_i){
				var $tabTexts = $(subpage).parent().closest(".tabpane").find(".tabpane_tabrow .tab");
				if( $tabTexts.length > 0 ) {
					var tab = $tabTexts[page_i];
					return {tabPageIndex: page_i, tabPageTitle: tab.textContent, tabPageId: subpage.getAttribute("id")};
				} else {
					var cgTitle = this.getSubPageTitle(subpage);
					return {tabPageIndex: page_i, tabPageTitle: cgTitle, tabPageId: subpage.getAttribute("id")};
				}
			}, this);
			return menuItems;
		},

		createAccordionMenu: function( subpages ) {
			var menuItems = this.getAccordionMenuItems( document.querySelectorAll(".tab-page") || document.querySelectorAll(".content_group") );
			var contentTitle = document.querySelector("#content_title td.title");
			if( !contentTitle ) return;
			var span;
			if( contentTitle.querySelector("#content_title_span") ) {
				span = contentTitle.querySelector("#content_title_span").cloneNode(true);
			} else {
				span = document.createElement("span");
				span.setAttribute("id", "content_title_span");
				span.textContent = contentTitle.textContent;
			}
			var button = document.createElement("button");
			var buttonId = "content_title_"+"button";
			button.className = "btn btn-outline-secondary dropdown-toggle";
			button.setAttribute("type", "button");
			button.setAttribute("id", buttonId);
			button.setAttribute("data-toggle", "dropdown");
			button.setAttribute("aria-haspopup", "true");
			button.setAttribute("aria-expanded", "false");
			button.setAttribute("data-title", span.textContent);
			button.appendChild(span);
			var menuDiv = document.createElement("div");
			menuDiv.className = "dropdown-menu";
			menuDiv.setAttribute("aria-labelledby", buttonId);
			_.each(menuItems, function(menuItem){
				var link = document.createElement("a");
				link.className = "dropdown-item";
				link.setAttribute("data-target","[data-content-id='"+menuItem.tabPageId+"']");
				_.each(menuItem, function(v, k){link.dataset[k] = v;});
				link.textContent = menuItem.tabPageTitle;
				link.onclick = function(evt) {
					var tgt = evt.target;
					// Accd.openOnly(menuItem.tabPageId);
					Accd.goto(menuItem.tabPageId);
					$(tgt).closest(".dropdown-menu").find("a.dropdown-item.active").removeClass("active");
					tgt.classList.toggle("active", true);
				};
				menuDiv.appendChild(link);
			});
			var dropdown = document.createElement("div");
			dropdown.className = "dropdown content_group_accd_menu";
			dropdown.appendChild( button );
			dropdown.appendChild( menuDiv );
			return dropdown;
		},
	};
	self.Accd = Accd;
}(self !== undefined ? self : this));

/** to bootstrap acordian */
// function transformTabPane( contentType, contentId ) {
// 	contentType = contentType || "tabpane";
// 	contentId = contentId || "main";
// 	var domId = '#'+contentType+'_'+contentId;
// 	var tabpane = document.querySelector(domId);
// 	if( !tabpane )
// 		return;
// 	var tabpages = tabpane.querySelectorAll(".tab-page");
// 	console.log("tagpages:", tabpages);


// 	var tablabels = tabpane.querySelectorAll(".tabpane_tabrow .tab");
// 	console.log("tablabels:", tablabels);

// 	var tabpaneId = contentType+"_"+contentId;
// 	_.each(tabpages, function( tabpage, i ) {
// 		var cardbodyId = contentType+"_"+contentId+"_"+i;
// 		var tablabel = tablabels[i];
// 		var cardlink = tablabels[i].querySelector("a").cloneNode(true);
// 		var isCollapsed = (_.includes(tablabel.classList, "selected") ? false : true);
// 		cardlink.classList.toggle("card-link", true);
// 		cardlink.classList.toggle("collapsed", isCollapsed);
// 		cardlink.setAttribute("data-toggle", "collapse");
// 		cardlink.setAttribute("href", "#"+cardbodyId);
// 		var cardheader = document.createElement("div");
// 		cardheader.classList.toggle("card-header", true);
// 		cardheader.appendChild(cardlink);

// 		var cardbodyCollapse = document.createElement("div");
// 		cardbodyCollapse.setAttribute("id", cardbodyId);
// 		cardbodyCollapse.classList.toggle("collapse", true);
// 		cardbodyCollapse.classList.toggle("show", !isCollapsed);
// 		cardbodyCollapse.setAttribute("data-parent", "#"+tabpaneId);
// 		tabpage.classList.toggle("card-body", true);
// 		cardbodyCollapse.appendChild( tabpage );

// 		var card = document.createElement("div");
// 		card.classList.toggle("card", true);
// 		card.appendChild(cardheader);
// 		card.appendChild(cardbodyCollapse);

// 		tabpane.append(card);
// 	});

// 	var tabrow = tabpane.querySelector(".tabpane_tabrow");
// 	if( tabrow )
// 		tabrow.classList.toggle("d-none", true);
// };
