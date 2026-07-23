/*
 *	File Name:	BundleUtil.java
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

package com.irt.util;

import com.irt.util.cst.ReflectUtil;

import java.util.ResourceBundle;

public class BundleUtil {

	public static ResourceBundle[] getBundlesUnsafely( MessageHandler msghandler ) {
		ResourceBundle[] bundles = null;
		try {
			if( msghandler instanceof com.irt.util.MessageBundle ) {
				bundles = (ResourceBundle[])ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "bundles");
			} else {
				MessageHandler mm = (MessageHandler)ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "msghandler");
				bundles = (ResourceBundle[])ReflectUtil.getDeclaredFieldObject(mm.getClass(), mm, "bundles");
			}
		} catch( IllegalArgumentException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IllegalAccessException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bundles;
	}

	public static String[] getBundleBaseNamesUnsafely( MessageHandler msghandler ) {
		String[] sourceNames = null;
		try {
			if( msghandler instanceof com.irt.util.MessageBundle ) {
				sourceNames = (String[])ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "bundleBaseNames");
			} else {
				MessageHandler mm = (MessageHandler)ReflectUtil.getDeclaredFieldObject(msghandler.getClass(), msghandler, "msghandler");
				sourceNames = (String[])ReflectUtil.getDeclaredFieldObject(mm.getClass(), mm, "bundleBaseNames");
			}
		} catch( IllegalArgumentException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IllegalAccessException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sourceNames;
	}

}
