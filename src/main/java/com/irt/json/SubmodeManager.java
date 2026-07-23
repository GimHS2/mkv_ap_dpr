/*
 *	File Name:	SubmodeManager.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.json;

import com.irt.servlet.PageConfig;

public class SubmodeManager {

	public void startSubmode( PageConfig htmlpage, String submode ) {
		htmlpage.setProperty("submode", submode);
	}

	public String endSubmodeAndDispose( PageConfig htmlpage ) {
		try {
			return htmlpage.getProperty("submode");
		} finally {
			htmlpage.setProperty("submode", null);
		}
	}

}
