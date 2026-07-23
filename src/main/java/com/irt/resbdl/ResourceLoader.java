/*
 *	File Name:	ResourceLoader.java
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

import com.irt.util.StringUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * ResourceLoader( external file system resource )
 *
 */
public class ResourceLoader extends URLClassLoader implements ChildFirstResourceLoader {

	public static ResourceLoader newInstance( URL url, ClassLoader parent ) {
		return new ResourceLoader(new URL[] { url }, parent);
	}

	public ResourceLoader( URL[] urls, ClassLoader parent ) {
		super(urls, parent);
	}

	@Override
	public URL getChildFirstResource( String name ) {
		URL url = findResource(name);
		if( url != null )
			return url;

		return super.getResource(name);
	}

	public List<File> getOriginalColumnResourcePropertyFiles( String resourceName ) {
		if( resourceName == null )
			return null;

		String[] struct = resourceName.split("\\.");
		java.util.Set<String> rset = new java.util.HashSet<String>();

		List<File> files = new ArrayList<File>();
		if( this.getParent() instanceof java.net.URLClassLoader ) {
			java.net.URL[] urls = ( (java.net.URLClassLoader)this.getParent() ).getURLs();
			for( java.net.URL url : urls ) {
				String urlStr = url.getPath();

				if( urlStr.startsWith("file:") ) {
					urlStr = urlStr.substring(5);
				}
				if( urlStr.endsWith("!/") ) {
					urlStr = urlStr.substring(0, urlStr.length() - 2);
				}
				if( urlStr.endsWith(".jar") ) {
					urlStr = null;
				}

				if( urlStr != null )
					rset.add(urlStr);
			}

			File[] colresbdlFiles = null;
			for( String urlStr : rset ) {
				final String leafFilename = struct[struct.length - 1];
				String[] parentPath = Arrays.copyOf(struct, struct.length - 1);

				File mesgDir = new File(urlStr, StringUtil.strJoin(parentPath, "/"));
				colresbdlFiles = mesgDir.listFiles(new java.io.FilenameFilter() {
					@Override
					public boolean accept( File dir, String name ) {
						if( name != null ) {
							if( name.endsWith(".properties") ) {
								if( name.startsWith(leafFilename) ) {
									return true;
								}
							}
						}
						return false;
					}
				});
				if( colresbdlFiles != null && colresbdlFiles.length > 0 )
					files.addAll(java.util.Arrays.asList(colresbdlFiles));
			}
		}

		return files;
	}

	@Override
	public URL getResource( String name ) {
		return getChildFirstResource(name);
	}

//	public File getResourceRootDirectory() throws URISyntaxException {
//		return new File(getURLs()[0].toURI().getPath());
//	}

}
