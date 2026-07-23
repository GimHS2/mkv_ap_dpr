/*
 *	File Name:	MenuHtml.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.cst;

import com.irt.util.IPredicate;
import com.irt.util.Predicate;
import com.irt.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import j2html.TagCreator;
import j2html.tags.ContainerTag;


/**
 *	To generate menu structure output from db records
 *
 *
 *
 *
	css example
	<style>
	.menuhtml ul { padding:0; margin:0;  }
	.menuhtml li { list-style:none; }
	.menuhtml li > ul { display: none; }
	.menuhtml li:hover > ul { display: block; }
	.menuhtml .menu-lvl1 li { margin-right: 10px; display: inline; position:relative; }
	.menuhtml .menu-lvl2 { position: absolute; }
	.menuhtml .menu-lvl2 li { position: relative; }
	.menuhtml .menu-lvl3 { position: absolute; top:0px; left: 50px; }
	</style>

	html structure example
	<pre>
	<div id='ICSBoard_FQ_CN' class='menuhtml'>
		<ul class='menu-top menu-level-0'>
			<a class='menu-top-link menu-link' href='#'>ssss</a>
			<li><a class='menu-link' href='#'>ssss</a>
				<ul class='menu-level-1'>
					<li><a class='menu-link' href='#'>aaaaa</a></li>
					<li><a class='menu-link' href='#'>bbbbb</a>
						<ul class='menu-level-2'>
							<li><a class='menu-link' href='#'>xxxx</a>
								<ul class='menu-level-3'></ul>
							</li>
							<li><a class='menu-link' href='#'>yyyy</a>
								<ul class='menu-level-3'></ul>
							</li>
							<li><a class='menu-link' href='#'>zzzz</a>
								<ul class='menu-level-3'></ul>
							</li>
						</ul>
					</li>
				</ul>
			</li>
			<li><a class='menu-link' href='#'>ttttt</a>
				<ul class='menu-level-1'></ul>
			</li>
			<li><a class='menu-link' href='#'>uuuuu</a>
				<ul class='menu-level-1'></ul>
			</li>
		</ul>
	</div>
	</pre>
 *
 */
public class MenuHtml {

	/**
	 * in javascript if href has "JavaScript:void(0)", then it does not work as link.
	 */
	protected final static String JSSTR_VOID_FUNCTION = "JavaScript:void(0)";

	public static class Converter {

		private final static String HRCY_LEVEL_DIVIDER = "#";

		private final static int HRCY_LEVEL_DIVIDER_ARRIDX = 0;

		private final static String HRCY_SEQ_DIVIDER = "[";

		private final static int HRCY_SEQ_DIVIDER_ARRIDX = 1;

		private static Integer toInteger( Object bigDecObj ) {
			return ( (BigDecimal)bigDecObj ).intValueExact();
		}

		public static List<MenuHtml> getMenuHtmlList( List<Map<String, Object>> menuRecords ) {
			List<MenuHtml> menuList = new ArrayList<MenuHtml>();
			MenuHtml menuitem = null;
			for( Map<String, Object> map : menuRecords ) {
				String menuMessageKey = (String)map.get("menuMessageKey");
				String menuMessage = (String)map.get("menuMessage");

				String menuHrcy = (String)map.get("menuHrcy");
				int menuLevel = toInteger(map.get("menuLevel"));
				int menuSeq = toInteger(map.get("menuSeq"));

				menuitem = MenuHtml.builder()
						.withMenuId((String)map.get("menuId"))
						.withMenuLevel(menuLevel)
						// .withMenuClass( toInteger( map.get( "menuClass" ) ) )
						.withMenuSeq(menuSeq)
						.withMenuHref((String)map.get("menuHref"))
						.withMenuMessageKey(menuMessageKey)
						.withMenuMessage(menuMessage)
						.withMenuLocale((String)map.get("menuLocale"))
						.withMenuHrcy(menuHrcy)
						.withParentMenuHrcy(MenuCode.getParentMenuHrcy(map))
						.build();
				if( menuitem.isTop() ) {
					if( JSSTR_VOID_FUNCTION.equals(menuitem.getMenuHref()) ) {
						throw new RuntimeException("something wrong investigate!!!");
					}
				}
				menuList.add(menuitem);
			}
			return menuList;
		}

		public static String getMenuHtml( List<Map<String, Object>> menuRecords ) {
			ContainerTag menuhtmlTag = getMenuHtmlTag(getMenuHtmlList(menuRecords));
			return menuhtmlTag.toString();
		}

		public static String getParentMenuHrcy( Map<String, Object> map ) {
			String menuHrcyCode = (String)map.get("menuHrcy");
			String[] levels = getHrcyCodes(menuHrcyCode);
			String[] parentLevels = java.util.Arrays.copyOf(levels, levels.length - 1);
			String parentMenuHrcyCode = getHrcyCode(parentLevels);
			return parentMenuHrcyCode;
		}

		public static String getHrcyCode( String[] levels ) {
			return StringUtil.strJoin(levels, HRCY_LEVEL_DIVIDER);
		}

		private static String[] getHrcyCodes( String menuHrcyCode ) {
			String[] levels = menuHrcyCode.split(HRCY_LEVEL_DIVIDER);
			return levels;
		}

		static ContainerTag getMenuHtmlTag( List<MenuHtml> menuList ) {
			MenuHtml top = MenuHtml.getMenuTop(menuList);

			ContainerTag topul = top.toHtmlTag();
			List<MenuHtml> topsub = getChilds(menuList, top.getMenuHrcy());
			List<MenuHtml> subs;
			for( MenuHtml menuHtml : topsub ) {
				final String phrcy = menuHtml.getMenuHrcy();
				subs = getChilds(menuList, phrcy);
				ContainerTag menuli = menuHtml.toHtmlTag();
				if( !subs.isEmpty() ) {
					ContainerTag subul = getMenuGroupTag(menuList, menuHtml);
					subul.withClass(menuHtml.getMenuGroupClassName());
					topul.with(menuli.with(subul));
				} else {
					topul.with(menuli);
				}
			}
			ContainerTag div = top.wrapTopMenu(topul);
			return div;
		}

		static ContainerTag getMenuGroupTag( List<MenuHtml> menuList, MenuHtml menu ) {
			ContainerTag menuul = j2html.TagCreator.ul();
			final String phrcy = menu.getMenuHrcy();
			List<MenuHtml> subs = getChilds(menuList, phrcy);
			for( MenuHtml menuHtml : subs ) {
				ContainerTag menuli = menuHtml.toHtmlTag();
				if( !subs.isEmpty() ) {
					ContainerTag subul = getMenuGroupTag(menuList, menuHtml);
					subul.withClass(menuHtml.getMenuGroupClassName());
					menuul.with(menuli.with(subul));
				} else {
					menuul.with(menuli);
				}
			}
			return menuul;
		}

		static List<MenuHtml> getChilds( List<MenuHtml> menuList, final String phrcy ) {
			List<MenuHtml> sub = (List<MenuHtml>)Predicate.filter(menuList, new IPredicate<MenuHtml>() {
				@Override
				public boolean apply( MenuHtml type ) {
					return phrcy.equals(type.getParentMenuHrcy());
				}
			});
			return sub;
		}
	}

	public final static String STATUS_NORMAL = "00";

	public final static String STATUS_DISABLE = "99";
	public final static String CSS_CLASS_MENUHTML_SYM = ".menuhtml";

	public final static String CSS_CLASS_MENU_LEVEL_SYM = ".menu-level-";

	public final static int TOP_MENULEVEL = 0;


	public static String calcMenuHrcy( String parentMenuHrcy, int level, int seq ) {
		return parentMenuHrcy + calcMenuHrcyCurrent(level, seq);
	}

	public static String calcMenuHrcyCurrent( int level, int seq ) {
		return "#" + level + "[" + seq;
	}

	public static MenuHtml getMenuTop( List<MenuHtml> menuList ) {
		IPredicate<MenuHtml> pred_top = new IPredicate<MenuHtml>() {
			@Override
			public boolean apply( MenuHtml type ) {
				return type.menuLevel == TOP_MENULEVEL;
			}
		};
		MenuHtml top = Predicate.select(menuList, pred_top);
		return top;
	}

	public static boolean validateMenu( Builder bld ) {
		if( bld.menuLevel == TOP_MENULEVEL ) {
			if( bld.parentMenuHrcy != null )
				return false;
		}

		return false;
	}

	/**
	 * menu level how deep is th level first level or second level ?
	 */
	int menuLevel;

	int menuSeq;

	/**
	 * my current path not implemented.
	 */
	LinkedList<Integer> path;

	String menuHrcy;

	String parentMenuHrcy;

	/**
	 * menu id in html to make it uniq <br>
	 * but db table all related menu record will have this id.
	 */
	String menuId;

	/**
	 * String representation of css selector eg. #id.lvl3
	 */
	String attrs;

	/**
	 * link to <a href=""/>
	 */
	String menuHref;

	/**
	 * messageKey to resolved by {@link com.irt.util.MessageHandler}
	 */
	String menuMessageKey;

	String menuMessage;

	String menuLocale;

	/**
	 * [ 00, 99 ]
	 */
	String status;

	@Generated( "SparkTools" )
	private MenuHtml( Builder builder ) {
		this.menuLevel = builder.menuLevel;
		this.menuSeq = builder.menuSeq;
		this.path = builder.path;
		this.menuHrcy = builder.menuHrcy;
		this.parentMenuHrcy = builder.parentMenuHrcy;
		this.menuId = builder.menuId;
		this.attrs = builder.attrs;
		this.menuHref = builder.menuHref;
		this.menuMessageKey = builder.menuMessageKey;
		this.menuMessage = builder.menuMessage;
		this.menuLocale = builder.menuLocale;
		this.status = builder.status;
	}

	public String getAttrs() {
		return attrs;
	}

	/**
	 *
	 * @return css class selector
	 */
	String getMenuGroupAttr() {
		int lvl = getMenuLevel();
		if( lvl < 0 )
			lvl = 0;
		return CSS_CLASS_MENU_LEVEL_SYM + String.valueOf(lvl);
	}

	/**
	 * @return css classname
	 */
	String getMenuGroupClassName() {
		return CSS_CLASS_MENU_LEVEL_SYM.replaceFirst(".", "") + String.valueOf(getMenuLevel());
	}

	public String getHref() {
		return menuHref;
	}

	@Deprecated
	private ContainerTag getLinkIfHref() {
		return getLinkIfHref(null);
	}

	private ContainerTag getLinkIfHref( Map<String, String> messages ) {
		String messageValue = tryGetMessage(messages);
		if( messageValue == null )
			messageValue = "";

		ContainerTag alink = j2html.TagCreator.a(messageValue);
		if( hasValidHref() ) {
			alink.attr("href", getMenuHref());
			alink.withClass("menu-link");
		} else {
			alink.attr("href", JSSTR_VOID_FUNCTION);
			alink.withClass("menu-link has-not-menu-link");
		}
		if( StringUtil.isNullOrEmpty(getMenuMessage()) ) {
			alink.withText(getMenuMessage());
		}

		return alink;
	}

	public int getMenuSeq() {
		return menuSeq;
	}

	/**
	 * li and under ul
	 *
	 * @param childMenus
	 * @return
	 */
	public ContainerTag getMenuGroup( List<MenuHtml> childMenus ) {
		ContainerTag li = null;
		ContainerTag ul = j2html.TagCreator.ul(TagCreator.attrs(getMenuGroupAttr()));
		if( childMenus.isEmpty() ) {
			li = j2html.TagCreator.li(getLinkIfHref());
		} else {
			for( MenuHtml menu : childMenus ) {
				ul.with(menu.toHtmlTag());
			}
			li = j2html.TagCreator.li(getLinkIfHref(), ul);
		}
		li.attr("data-has-link", Boolean.toString(hasValidHref()));
		li.attr("data-menu-id", getMenuId());
		li.attr("data-menu-hrcy", getMenuHrcy());
		li.attr("data-menu-level", String.valueOf(getMenuLevel()));
		li.attr("data-menu-seq", String.valueOf(getMenuSeq()));
		li.attr("data-menu-message-key", getMenuMessageKey());
		li.attr("data-menu-message", getMenuMessage());

		return li;
	}

	public String getMenuHrcy() {
		return menuHrcy;
	}

	public String getMenuHref() {
		return menuHref;
	}

	public String getMenuId() {
		return menuId;
	}

	public String getMenuLocale() {
		return menuLocale;
	}

	public int getMenuLevel() {
		return menuLevel;
	}

	public String getMenuMessage() {
		return menuMessage;
	}

	public String getMenuMessageKey() {
		return menuMessageKey;
	}

	public String getParentMenuHrcy() {
		return parentMenuHrcy;
	}

	public LinkedList<Integer> getPath() {
		return path;
	}

	public int getSeq() {
		return menuSeq;
	}

	public String getStatus() {
		return status;
	}

	public String getString() {
		String delim = ".";
		String delimSeq = "[";
		String ret = ""
				+ getMenuId() + delim//
				+ "parent:" + getParentMenuHrcy() + delim//
				+ "code:" + getMenuHrcy() + delim//
				+ getMenuLevel() + delimSeq + getSeq() + delim//
		;

		return ret;
	}

	public boolean isTop() {
		return getMenuLevel() == 0 && getMenuSeq() == 0;
	}

	public boolean hasHref() {
		return getMenuHref() == null ? false : true;
	}

	public boolean hasValidHref() {
		if( hasHref() ) {
			return !getMenuHref().startsWith(JSSTR_VOID_FUNCTION);
		} else {
			return false;
		}
	}

	/**
	 * if top return ul ( top menu element ) else return li( menu element )
	 *
	 * @return
	 */
	public ContainerTag toHtmlTag() {
		if( isTop() ) {
			ContainerTag ul = j2html.TagCreator.ul(TagCreator.attrs(getMenuGroupAttr()));
			ul.attr("data-has-link", Boolean.toString(hasValidHref()));
			ul.attr("data-menu-id", getMenuId());
			ul.attr("data-menu-hrcy", getMenuHrcy());
			ul.attr("data-menu-locale", getMenuLocale());
			ul.attr("data-menu-level", String.valueOf(getMenuLevel()));
			ul.attr("data-menu-seq", String.valueOf(getMenuSeq()));
			ul.attr("data-menu-message", getMenuMessage());
			ul.attr("data-is-menu-top", "true");
			ul.withClass("menu-top menu-level-0");
			if( getMenuMessage() != null && getMenuMessage().length() > 0 ) {
				ContainerTag alink = getLinkIfHref(null);
				alink.withClasses("menu-top-link", "menu-link");
				ul.with(alink);
			}
			return ul;
		} else {
			ContainerTag li = j2html.TagCreator.li(getLinkIfHref(null));
			li.attr("data-has-link", Boolean.toString(hasValidHref()));
			li.attr("data-menu-id", getMenuId());
			li.attr("data-menu-hrcy", getMenuHrcy());
			li.attr("data-menu-level", String.valueOf(getMenuLevel()));
			li.attr("data-menu-seq", String.valueOf(getMenuSeq()));
			li.attr("data-menu-message-key", getMenuMessageKey());
			li.attr("data-menu-message", getMenuMessage());
			return li;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < menuLevel; i++ ) {
			sb.append('\t');
		}
		return sb.toString()
				// + "menuLevel: " + menuLevel
				+ menuHrcy
				+ ", parent: " + parentMenuHrcy
				+ ", msgkey: " + menuMessageKey;
	}

	/**
	 * @param messages
	 * @return try and if null then return menuMessageKey
	 */
	private String tryGetMessage( Map<String, String> messages ) {
		String messageValue = null;
		if( messages != null ) {
			messageValue = messages.get(getMenuMessageKey());
		}
		if( messageValue == null || messageValue.length() == 0 ) {
			messageValue = getMenuMessage();
		}
		return messageValue;
	}

	/**
	 * final div wrapping
	 *
	 * @param topul
	 * @return
	 */
	public ContainerTag wrapTopMenu( ContainerTag topul ) {
		String menuIdForHtml = MenuCode.getMenuIdForHtml(getMenuId());
		String attrs = "#" + menuIdForHtml + CSS_CLASS_MENUHTML_SYM;
		return j2html.TagCreator.div(TagCreator.attrs(attrs), topul);
	}

	/**
	 * Creates builder to build {@link MenuHtml}.
	 *
	 * @return created builder
	 */
	@Generated( "SparkTools" )
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link MenuHtml}.
	 */
	@Generated( "SparkTools" )
	public static final class Builder {
		private int menuLevel;
		private int menuSeq;
		private LinkedList<Integer> path;
		private String menuHrcy;
		private String parentMenuHrcy;
		private String menuId;
		private String attrs;
		private String menuHref;
		private String menuMessageKey;
		private String menuMessage;
		private String menuLocale;
		private String status;

		private Builder() {
		}

		public Builder withMenuLevel( int menuLevel ) {
			this.menuLevel = menuLevel;
			return this;
		}

		public Builder withMenuSeq( int menuSeq ) {
			this.menuSeq = menuSeq;
			return this;
		}

		public Builder withPath( LinkedList<Integer> path ) {
			this.path = path;
			return this;
		}

		public Builder withMenuHrcy( String menuHrcy ) {
			this.menuHrcy = menuHrcy;
			return this;
		}

		public Builder withParentMenuHrcy( String parentMenuHrcy ) {
			this.parentMenuHrcy = parentMenuHrcy;
			return this;
		}

		public Builder withMenuId( String menuId ) {
			this.menuId = menuId;
			return this;
		}

		public Builder withAttrs( String attrs ) {
			this.attrs = attrs;
			return this;
		}

		public Builder withMenuHref( String menuHref ) {
			this.menuHref = menuHref;
			return this;
		}

		public Builder withMenuMessageKey( String menuMessageKey ) {
			this.menuMessageKey = menuMessageKey;
			return this;
		}

		public Builder withMenuMessage( String menuMessage ) {
			this.menuMessage = menuMessage;
			return this;
		}

		public Builder withMenuLocale( String menuLocale ) {
			this.menuLocale = menuLocale;
			return this;
		}

		public Builder withStatus( String status ) {
			this.status = status;
			return this;
		}

		public MenuHtml build() {
			return new MenuHtml(this);
		}
	}

}
