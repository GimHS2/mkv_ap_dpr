<%--
	File Name:	dpr_item_tree.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2026/03/31		2.2.1	selectItem(): requst url에 distributionChannelCode 파라미터 추가
	hankalam	2021/11/30		2.2.0	신규 UI/UX 적용
	hankalam	2020/10/30		2.0.5	itemName 에 encodeURIComponent 추가
	jbaek		2019/07/30		2.0.4	브랜드 검색 조건 추가.
	jbaek		2017/06/30		2.0.3	itemConsumerEANCode 검색 기능 추가. 브랜드 검색 조건 추가.
	jbaek		2015/06/30		2.0.2	오타 수정.
	jbaek		2014/09/30		2.0.1	Product Hierarchy Level 기능 개발
	guksm		2008/09/26		2.0.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8' %>
<%@ page import="com.irt.html.*,java.util.List,java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
%>
<head>
<%@ include file="include_rbm_header.inc" %>
<%@ include file="include_dpr_itemtree.inc" %>
<script type="text/javascript">
	var checkmenu = new CheckTree('checkmenu');

	$(function() {
		if( parent.$("iframe.main-content") ) {
			$(".frame-content").innerHeight( parent.$(".main-content").contents().find( ".frame-content-wrap").innerHeight() );
		}
	});

	function bodyLoad() {
		if( parent.$(".content-overlay .loading").is(":visible") ) {
			toggleLoadingParent( false );
		}
		treeOpenAll();
	}

	function treeOpenAll() {
		$("#tree-checkmenu").find("li[name^=li-lvl].plus").click();
	}

	function selectItem( selectedValues ) {
		if( parent && typeof parent.window.main_content != "undefined" && parent.window.main_content ) {
			if( typeof parent.window.main_content.getLocationURL == "undefined" )
				return;

			var url = parent.window.main_content.getLocationURL();
			if( url ) {
				url = url.substring( 0, url.indexOf("?") );
				var itemCode, distributionChannelCode;
				var itemCode = selectedValues;

				if( !itemCode || itemCode == null ) {
					customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_VALUE" encodeScript="true"/>" } );
					return;
				}

				url = replaceQueryValue( url, "itemCode", encodeURIComponent(itemCode) );
				var selectedValues = CheckBox.getValues( frmMain.distributionChannelCode );
				if( selectedValues != null ) {
					url = replaceQueryValue( url, "distributionChannelCode", null );
					for( var v = 0; v < selectedValues.length; v++ ) {
						url += "&distributionChannelCode="+ selectedValues[v];
					}
				}
				url = replaceQueryValue( url, "organizationCode", encodeURIComponent("<mtl:value id="condition" key="organizationCode"/>") );
				url = replaceQueryValue( url, "partyCode", frmMain.partyCode.value );
				url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
				url = replaceQueryValue( url, "vtype", "" );

				//parent.window.main_content.windowOpen( url );
				windowOpen( url, "main_content" );
			} else {
				return;
			}
		} else {
				return;
		}
	}

	function itemSearch( condType ) {
		var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=tree&type=itm";

		url = replaceQueryValue( url, "organizationCode", "<mtl:value id="request" key="organizationCode"/>" );
		if( condType == "NEW" )
			url = replaceQueryValue( url, "newItemInd", "Y" );
		else if( condType == "PRM" )
			url = replaceQueryValue( url, "promotionItemInd", "Y" );

		url = getBaseCondition( url );

		windowSelfOpen( url );
	}

	function itemSearchDetail() {
		var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=tree&type=itm";
		url = getBaseCondition( url );

		if( frmMain.itemCode.value ) {
			url = replaceQueryValue( url, "itemCode", encodeURIComponent(frmMain.itemCode.value) );
		} else {
			url = replaceQueryValue( url, "itemName", encodeURIComponent(frmMain.itemName.value) );
		}

		windowSelfOpen( url );
	}

	function showDetailSearch( display ) {
		if( typeof display != "undefined" )
			Styles.changeDisplay( document.all.content_cond_search_item, display );
		else {
			if( document.all.content_cond_search_item.style.display == "" )
				Styles.changeDisplay( document.all.content_cond_search_item, false );
			else {
				Styles.changeDisplay( document.all.content_cond_search_item, true );
			}
		}
	}

	function getBaseCondition( url, obj ) {
		if( obj == null || typeof obj == "undefined" ) {
			url = replaceQueryValue( url, "organizationCode", frmMain.organizationCode.value );
			url += "&" + CheckBox.getQueryValue( frmMain.distributionChannelCode, "distributionChannelCode" );
			url = replaceQueryValue( url, "partyCode", frmMain.partyCode.value );
		} else {
			if( !Field.checkMandatory(obj) ) return;

			url = replaceQueryValue( url, "organizationCode", frmMain.organizationCode.value );
			url += "&" + CheckBox.getQueryValue( frmMain.distributionChannelCode, "distributionChannelCode" );
			if( obj == frmMain.partyCode )
				url = replaceQueryValue( url, "partyCode", frmMain.partyCode.value );
		}
		url = replaceQueryValue( url, "type", "itm" );
		url = replaceQueryValue( url, "btype", "ord" );

		return url;
	}

	function itemBaseSearch( obj ) {
		var url = getBaseCondition( "<%= systemConfig.getClassURL() %>/DPRItem?mode=tree", obj );
		if( typeof url == "undefined" || url == null ) return;

		windowSelfOpen( url );
	}

	function treeShowByBrand( el ) {
		if( el ) {
			var brandCode = Field.getValue(el);
			if( brandCode ) {
				$("#tree-checkmenu :input").filter(function(i){ return $(this).attr("data-brand-code") != brandCode; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("brand-show").addClass("brand-hide"); });

				var foundCnt = 0;
				$("#tree-checkmenu :input").filter(function(i){ return $(this).attr("data-brand-code") == brandCode; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("brand-hide").addClass("brand-show"); foundCnt++; });
				if( foundCnt == 0 ) {
					$("#tree-checkmenu :input")
						.each(function(i,o){ $(this).parent().closest("li").removeClass("brand-show").addClass("brand-hide"); });
				}

				$("#tree-checkmenu .brand-hide").hide();
				$("#tree-checkmenu .brand-show").show();
				treeOpenAll();
			} else {
				$("#tree-checkmenu .brand-hide").removeClass("brand-hide").show();
				$("#tree-checkmenu .brand-show").removeClass("brand-show");
			}
		}
	}

	function treeShowByFilter( el ) {
		if( el ) {
			var filterValue = Field.getValue( el );
			if( filterValue == "N" ) {
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-newitem") != "Y"; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });

				var foundCnt = 0;
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-newitem") == "Y"; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("item-hide").addClass("item-show"); foundCnt++; });
				if( foundCnt == 0 ) {
					$("#tree-checkmenu :input")
						.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });
				}

				$("#tree-checkmenu .item-hide").hide();
				$("#tree-checkmenu .item-show").show();
				treeOpenAll();
			} else if( filterValue == "P" ) {
				$("#tree-checkmenu :input")
				.filter(function(i){ return $(this).attr("data-promotion") != "Y"; })
				.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });

				var foundCnt = 0;
				$("#tree-checkmenu :input")
					.filter(function(i){ return $(this).attr("data-promotion") == "Y"; })
					.each(function(i,o){ $(this).parent().closest("li").removeClass("item-hide").addClass("item-show"); foundCnt++; });
				if( foundCnt == 0 ) {
					$("#tree-checkmenu :input")
						.each(function(i,o){ $(this).parent().closest("li").removeClass("item-show").addClass("item-hide"); });
				}

				$("#tree-checkmenu .item-hide").hide();
				$("#tree-checkmenu .item-show").show();
				treeOpenAll();
			} else {
				$("#tree-checkmenu .item-hide").removeClass("item-hide").show();
				$("#tree-checkmenu .item-show").removeClass("item-show");
			}
		}
	}

	function readConditionReq( type ) {
		var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=rtp";
		url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
		readPartyAttributeReq( url, type, frmMain );
	}
</script>
</head>

<body class='content' style='padding-right: 8px; overflow: hidden;'>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_list.inc" %>
	<iframe name='subwin_util' style='display: none' src='<%= systemConfig.getProperty("baseURL") %>blank.html'></iframe>
	<mtl:contentGroup groupId="tree" type="content" styleClass="frame-content">
		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
			<div class='top-wrap'>
				<h2><mtl:message key="jsp.dpr_item_tree.SUBTITLE_SKU_LIST"/></h2>
				<div id='tree_cond' style='margin-bottom: 10px'>
					<div class='search-table table-fixed'>
						<div class='row'>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'>
									<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
								</div>
								<div class='field'>
									<mtl:select id="condition" key="organizationCode" mandatory="true"  searchable="false"
											listId="organizations" listCodeKey="organizationCode"
											listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SOLD\");"/>
								</div>
							</div>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'>
									<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/>
								</div>
								<div class='field'>
								<%
									Object _distChannelCodes = condition.get( "distributionChannelCode" );
									String[] condDistributionChannelCodes;
									if( _distChannelCodes instanceof String[] ) {
										condDistributionChannelCodes = (String[])_distChannelCodes;
									} else {
										condDistributionChannelCodes = new String[1];
										condDistributionChannelCodes[0] = (String)_distChannelCodes;
									}
									for( Map<String, Object> channelMap : distributionChannels ) {
										String code = (String)channelMap.get( "distributionChannelCode" );
										String name = (String)channelMap.get( "distributionChannelName" );
										out.print( "<input type='checkbox' id='distributionChannelCode_" + code + "'" );
										out.print( " name='distributionChannelCode' class='mandatory' value='" + code + "'");
										out.print( " onclick='JavaScript: readConditionReq(\"SOLD\");'" );
										for( String channelCode : condDistributionChannelCodes ) {
											if( code.equals(channelCode) ) {
												out.print( " checked " );
											}
										}
										out.print( ">" );
										out.print( "<label for='distributionChannelCode_" + code + "'>" );
										out.print( "<span>" + name + "</span>" );
										out.print( "</label>" );
									}
								%>
								</div>
							</div>
						</div>
					</div>
					<div class='search-table table-fixed'>
						<div class='row'>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'>
									<mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE" mandatory="true"/>
								</div>
								<div class='field'>
									<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="true"
											hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
											listId="authParties" listCodeKey="partyCode" listNameFormat="[$f{pure(partyCode)}] $H{partyName}"
											modified="itemBaseSearch(this);"/>
								</div>
							</div>
						</div>
					</div>
				</div>
				<mtl:contains id="records">
				<div id='tree_search' style='margin-bottom: 10px; display: none;'>
					<div class='search-table table-fixed'>
						<div class='row'>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'><mtl:message key="jsp.dpr_orderitem_tree.FIELD_MATERIAL"/></div>
								<div class='field'>
									<mtl:text id="request" key="itemCode"/>
								</div>
							</div>
							<div class='cell' style='padding-bottom: 10px;'>
								<div class='field-title'><mtl:message key="jsp.dpr_item_tree.FIELD_UPC_CODE"/></div>
								<div class='field'>
									<mtl:text id="request" key="itemConsumerEANCode"/>
								</div>
							</div>
						</div>
					</div>
					<div class='search-bottom'>
						<div class='table-cell search-button'>
							<mtl:button type="reset"/>
							<mtl:button type="button" onClick="JavaScript: toggleTreeFunction(\"tree_search\")" icon="images/ico_close.png" messageKey="jsp.BTN_CLOSE"/>
						</div>
					</div>
				</div>
				</mtl:contains>
				<div class='tree-menu table' style='height: 48px;'>
					<div class='table-row'>
						<mtl:contains id="records">
						<div class='table-cell align-left' style='vertical-align: middle;'>
							<mtl:button type="button" icon="images/ico_search.png" onClick="JavaScript: toggleTreeFunction(\"tree_search\");" messageKey="jsp.BTN_SEARCH"/>
						</div>
						<div class='table-cell align-right' style='vertical-align: middle;'>
							<mtl:select key="itemFilter" nullValueKey="jsp.MSG_SELECT_ALL_ITEMS" hasBlank="true" width="auto" searchable="false"
									prefixKey="jsp.dpr_item_tree.MSG_ITEM_FILTER_" codeValues="N,P"  modified="treeShowByFilter(this);"/>
						</div>
						</mtl:contains>
					</div>
				</div>

			</div>
			<mtl:contains id="records">
			<script type="text/javascript">
				$(function() {
					var topHeight = $("#content_tree .top-wrap").outerHeight( true );
					$("#content_list_itemtreegroup").css( "height", "calc(100% - " + topHeight + "px)" );

					$("#tree_search input[name=itemCode]").off( "keyup" ).on( "keyup", function(e) {
						if( e.keyCode !== 38 && e.keyCode !== 40 && e.keyCode !== 13 ) {
							treeOpenAll();
							var value = $(this).val().toLowerCase();
							$("#content_list_itemtree li.normal").filter( function() {
								var visible = $(this).text().toLowerCase().indexOf( value ) > -1;
								$(this).toggle( visible );
							});

							$("#regularItemsPool li").filter( function() {
								var visible = $(this).text().toLowerCase().indexOf( value ) > -1;
								$(this).toggle( visible );
							});
						}
					});

					$("#tree_search input[name=itemConsumerEANCode]").off( "keyup" ).on( "keyup", function(e) {
						if( e.keyCode !== 38 && e.keyCode !== 40 && e.keyCode !== 13 ) {
							treeOpenAll();
							var value = $(this).val().toLowerCase();
							$("#content_list_itemtree li.normal").filter( function() {
								var visible = ( $(this).find( "input" ).data( "upc" ) + "" ).toLowerCase().indexOf( value ) > -1;
								$(this).toggle( visible );
							});

							$("#regularItemsPool li").filter( function() {
								var visible = ( $(this).find( "input" ).data( "upc" ) + "" ).toLowerCase().indexOf( value ) > -1;
								$(this).toggle( visible );
							});
						}
					});
				});

				function toggleTreeFunction( id ) {
					if( "tree_search" === id ) {
						if( $("#tree_search").is(":visible") ) {
							$("#tree_search").hide();
						} else {
							$("#tree_search").show();
						}
					}
					var topHeight = $("#content_tree .top-wrap").outerHeight( true );
					$("#content_list_itemtreegroup").css( "height", "calc(100% - " + topHeight + "px)" );
				}
			</script>
			</mtl:contains>

			<div id='content_list_itemtreegroup' style='min-height: 350px;'>
				<mtl:containsElse id="records">
					<pre id='msg'><mtl:contains id="property" key="treeMsg"><%= property.getProperty("treeMsg") %></mtl:contains></pre>
				</mtl:containsElse>
				<mtl:contains id="records">
					<div class='table subtitle'>
						<div class='table-cell'><h3><mtl:message key="jsp.dpr_item_tree.MSG_PRODUCT_LIST"/></h3></div>
						<div class='table-cell align-right'>
							<mtl:select key="brandCode" nullValueKey="MSG_PUB_SELECT@FIELD_DPR_BRAND_CODE"
									hasBlank="true" listId="brands" listCodeKey="brandCode" width="auto" searchable="false" customOption="smallSelectmenuOptions"
									listNameFormat="$S{[:brandCode;] $S{:brandName}}" modified="treeShowByBrand(this);"/>
						</div>
					</div>
					<ul id='content_list_itemtree'>
					<%

						String cateCode = null;
						String oldCateCode = null;

						String defaultCateCode = null;
						String parentCateCode = null;
						String oldParentCateCode = null;

						String childCateCode = null;
						String oldChildCateCode = null;

						String cateName = null;
						String childCateName = null;
						String parentCateName = null;

						String leafCateCode = null;

						String currClassCodeStr = null;

						int classCode = 0;
						int oldClassCode = 0;
						int defaultClassCode = 0;
						int parentClassCode = 0;
						int childClassCode = 0;

						int count = 0;
						int childCateCount = 0;
						int oldChildCateCount = 0;
						int parentCateCount = 0;

						List<Map<String, Object>> recordList = (List)pageContext.findAttribute( "records" );

						if( recordList != null && recordList.size() > 0 ) {
							out.println( "<ul id='tree-checkmenu' class='tree_content'>" );

							for( Map<String, Object> recordMap : recordList ) {
								defaultCateCode = (String)recordMap.get( "defaultCateCode" );
								cateCode = (String)recordMap.get( "cateCode" );
								String defaultClassCodeStr = (String)recordMap.get( "defaultClassCode" );
								defaultClassCode = Integer.valueOf( defaultClassCodeStr );
								java.math.BigDecimal classCodeBD = (java.math.BigDecimal)recordMap.get( "currClassCode" );
								classCode = Integer.valueOf( classCodeBD.intValue() );

								if( classCode < 1 )
									classCode = 1;
								parentClassCode = classCode -1;
								if( parentClassCode < 1 )
									parentClassCode = 1;
								childClassCode = classCode +1;
								if( childClassCode > 5 )
									childClassCode = 6;

								cateName = (String)recordMap.get("N"+classCode);
								childCateCode = (String)recordMap.get("C"+childClassCode);
								childCateName = (String)recordMap.get("N"+childClassCode);

								parentCateCode = (String)recordMap.get("C"+parentClassCode);
								parentCateName = (String)recordMap.get("N"+parentClassCode);

								leafCateCode = (String)recordMap.get("C6");

								if( cateCode == null || cateCode.length() == 0 ) continue;

								if( !cateCode.equals(oldCateCode) ) {// new cateCode
									if( !childCateCode.equals( oldChildCateCode ) ) {
										if( oldCateCode != null )
											out.println( "</ul></li>" );

										if( childCateCode == null || childCateCode.length() == 0 ) continue;

										if( childCateCode != null && defaultClassCode == oldClassCode )
											out.println( "</ul></li>" );
									}

									if( defaultClassCode == classCode ) {
										out.println( "<li id='show-"+ cateCode + "' name='li-lvl-1' title='"+ "default-lv1-"+ cateCode +"'>" );
										out.print( "<div class='cate-title'>" );

										if( cateName != null )
											out.println( cateName );
										else
											out.println( cateCode + " - Products" );
										out.println( "<span id='count-"+ cateCode +"' class='count'></span>" );
										out.println( "</div>" );
										out.println( "<ul id='tree-"+ cateCode +"'>" );

										out.println( "<li id='show-"+ childCateCode +"' name='li-lvl-2'  title='"+ "default-lv2-"+ childCateCode +"'>" );
										out.print( "<div class='cate-sub-title'>" );
										if( childCateName != null )
											out.println( childCateName );
										else
											out.println( childCateCode + " - Products" );
										out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );
										out.println( "</div>" );
										out.println( "<ul id='tree-"+ childCateCode +"'>" );
									} else {
										out.println( "<li id='show-"+ childCateCode + "' name='li-lvl-2' title='"+ "cond-"+ childCateCode +"'>" );
										out.println( parentCateName + " - " );
										if( childCateName != null )
											out.println( childCateName );
										else
											out.println( childCateCode + " - Products" );
										out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );
										out.println( "</div>" );
										out.println( "<ul id='tree-"+ childCateCode +"'>" );
									}
								} else { //new childCateCode
									if( !childCateCode.equals( oldChildCateCode ) ) {
										if( defaultClassCode == classCode ) {

											if( cateCode.equals(oldCateCode) ) {
												if( childCateCode != null )
													out.println( "</ul></li>" );

												out.println( "<li id='show-"+ childCateCode +"' name='li-lvl-2' title='"+ "default-lv2-"+ childCateCode +"'>" );
												out.print( "<div class='cate-sub-title'>" );

												if( childCateName != null )
													out.println( childCateName );
												else
													out.println( childCateCode + " - Products" );
												out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );
												out.println( "</div>" );
												out.println( "<ul id='tree-"+ childCateCode +"'>" );
											}
										} else { // hierarchy condition

											if( childCateCode != null )
												out.println( "</ul></li>" );

											out.println( "<li id='show-"+ childCateCode +"' name='li-lvl-2' title='"+ "cond"+ childCateCode +"'>" );
											out.print( "<div class='cate-sub-title'>" );
											out.println( parentCateName + " - " );

											if( childCateName != null )
												out.println( childCateName );
											else
												out.println( childCateCode + " - Products" );
											out.println( "<span id='count-"+ childCateCode +"' class='count'></span>" );
											out.println( "</div>" );
											out.println( "<ul id='tree-"+ childCateCode +"'>" );
										}
									}
								}

								com.irt.data.format.RecordFormat codePattern = com.irt.data.format.PatternRecordFormat.getInstance( "$f{pure(itemCode)}");
								String displayItemName = ( recordMap.get("itemName") == null ? "" : (String)recordMap.get("itemName") );
								String newItemInd = (String)recordMap.get("newItemInd");
								String promotionItemInd = (String)recordMap.get("promotionItemInd");
								String itemConsumerEANCode = (String)recordMap.get( "itemConsumerEANCode" );
								StringBuffer sbuf = new StringBuffer();

								sbuf.append( "<li name='li-lvl-2' class='normal' " +"' title='"+ leafCateCode + "'>" );
								sbuf.append( "<input type='radio' name='checkItem' id='treeindex_"+ String.valueOf(count) +"' class='small'" )
								.append( " data-brand-code='" + HtmlUtility.toHtmlString(recordMap.get("brandCode")) + "'" )
								.append( " data-upc='" + itemConsumerEANCode + "'" );
								if( "Y".equals(newItemInd) ) {
									sbuf.append( " data-newitem='Y'" );
								}
								if( "Y".equals(promotionItemInd) ) {
									sbuf.append( " data-promotion='Y'" );
								}
								sbuf.append( " value='"+ recordMap.get("itemCode") + "' onClick='JavaScript:selectItem(this.value);'/>" );

								sbuf.append( "<label for='treeindex_"+ String.valueOf(count) +"' style='display: flex;'>" );
								if( "Y".equals(newItemInd) )
									sbuf.append( "<div class='new-item'></div>" );
								if( "Y".equals(promotionItemInd) )
									sbuf.append( "<div class='promotion-item'></div>" );

								sbuf.append( "<span name='checkItemsLabel'>" );
								sbuf.append( "("+ codePattern.format(recordMap, msghandler) + ") "+ displayItemName );
								sbuf.append( "</span>" );
								sbuf.append( "</label>" );
								sbuf.append( "</li>" );

								out.println( sbuf.toString() );

								count++;
								oldCateCode = cateCode;
								oldChildCateCode = childCateCode;
								oldClassCode = classCode;
							}

							if( count > 0 ) {
								out.println( "</ul></li>" );
							}

							out.println( "</ul>" );
						}
					%>
					</ul>
				</mtl:contains>
			</div>
		</mtl:form>
	</mtl:contentGroup>
</body>
</mtl:html>
