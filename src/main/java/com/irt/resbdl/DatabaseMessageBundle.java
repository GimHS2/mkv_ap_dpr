/*
 *	File Name:	DatabaseMessageBundle.java
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


package com.irt.resbdl;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;


public class DatabaseMessageBundle extends ResourceBundle {//@formatter:on

	Logger logger = Logger.getLogger(getClass());

	Map<String, Object> lookup;

	public DatabaseMessageBundle( DatabaseResourceRepository resMessage, String baseName, Locale locale ) {
		super();
		this.lookup = new HashMap<String, Object>();

		try {
			for( Map.Entry<String, Object> pair : resMessage.findByBaseName(baseName, locale) ) {
				lookup.put(pair.getKey(), pair.getValue());
			}
		} catch( SQLException ignored ) {
			logger.warn(ignored);
		}
	}

	public DatabaseMessageBundle( DatabaseResourceRepository resMessage, String bundleName, String format, ClassLoader loader, boolean reload ) {
		super();
		this.lookup = new HashMap<String, Object>();

		try {
			List<Map.Entry<String, Object>> list = null;
			try {
				list = resMessage.findByBundle(bundleName);
			} catch( SQLException sqlEx ) {
				throw sqlEx;
			} catch( Exception ex ) {
				logger.info("err: " + ex);
			}

			for( Map.Entry<String, Object> pair : list ) {
				lookup.put(pair.getKey(), pair.getValue());
			}
		} catch( SQLException ignored ) {
			logger.warn(ignored);
		}
	}

	@Override
	public Enumeration<String> getKeys() {
		ResourceBundle parent = this.parent;

		return new ResourceBundleEnumeration(lookup.keySet(), ( parent != null ) ? parent.getKeys() : null);
	}

	@Override
	protected Object handleGetObject( String key ) {
		if( key == null )
			throw new NullPointerException();

		return lookup.get(key);
	}

	/**
	 * find this then parent
	 *
	 * @param fallbackResourceBundle
	 */
	public void setFallbackParent( ResourceBundle fallbackResourceBundle ) {
		setParent(fallbackResourceBundle);
	}

}
