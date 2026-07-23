/*
 *	File Name:	LocationUtil.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class LocationUtil {//@formatter:on

	public static String absPath( final String base, final String relPath ) {
		// win :: C:/ or C:\ nix :: /
		if( relPath.startsWith("/") || relPath.matches("^.\\:.*") || relPath.startsWith("file://") ) {
			System.err.println("WARN: " + TraceHelper.getClassMethodName(TraceHelper.CURRENT) + ":: relPath already absolute path.");
			return base;
		} else {
			return new File(base, relPath).getAbsolutePath();
		}
	}

	public static String basename( final String absPath, final String fileSep ) {
		return absPath.substring(absPath.lastIndexOf(fileSep) + 1);
	}

	public static String basePath( final String absPath, final String relPath ) {
		if( absPath.contains(relPath) ) {
			return absPath.substring(0, absPath.length() - relPath.length() - 1);
		} else {
			return absPath;
		}
	}

	/**
	 * @return classpath root from caller perspective
	 */
	public static String getClasspathRoot( final Class<?> clazz ) {
		return getClasspathRoot(clazz, TraceHelper.CALLER);
	}

	/**
	 * @param clazz
	 *            : loaded class
	 * @param searchDepth
	 *            : to find pkgnamepath
	 * @return [ for war, fullpath of WEB-INF/lib or WEB-INF/classes ]
	 *         [ for jar, fullpath of . ( current running dir ) ]
	 */
	public static String getClasspathRoot( final Class<?> clazz, int searchDepth ) {
		// below always uses "/" as seperator ( eg. /C:/webapps/dynamic/WEB-INF/classes or lib/custom.jar )
		String classOrJarFullPath = null;
		try {
			classOrJarFullPath = new java.net.URI(clazz.getProtectionDomain().getCodeSource().getLocation().toString()).getPath();
		} catch( URISyntaxException ignored ) {
		}

		String classpathRoot = ".";
		if( classOrJarFullPath.lastIndexOf(".jar") == classOrJarFullPath.length() - 4 ) {
			String jarname = basename(classOrJarFullPath, "/");
			classpathRoot = basePath(classOrJarFullPath, jarname);
		} else if( classOrJarFullPath.lastIndexOf(".class") == classOrJarFullPath.length() - 6 ) {
			classOrJarFullPath = classOrJarFullPath.substring(0, classOrJarFullPath.length() - 6);
			String pkgpath = getPkgnameToPkgpath(TraceHelper.getClassName(searchDepth + 1));
			classpathRoot = basePath(classOrJarFullPath, pkgpath);
		} else {
			try {
				classpathRoot = new File(classpathRoot).getCanonicalPath();
			} catch( IOException e ) {
				classpathRoot = new File(classpathRoot).getAbsolutePath().replaceAll("\\.$", "");
			}
		}
		return classpathRoot;
	}

	/**
	 * @param pkgnamePath
	 *            : eg. com.irt.util.Utility
	 * @return pkgpath : eg. com/irt/util/Utility
	 */
	public static String getPkgnameToPkgpath( final String pkgnamePath ) {
		if( pkgnamePath.contains("/") || pkgnamePath.contains("\\") ) {
			System.err.println("WARN: " + TraceHelper.getClassMethodName(TraceHelper.CURRENT)
					+ ":: pkgpath should be com.irt.util ... pattern. but received " + pkgnamePath);
			return pkgnamePath;
		} else {
			return pkgnamePath.replaceAll("\\.", "/");
		}
	}

	/**
	 * @return classpath root from caller perspective
	 */
	public static String getWarRoot( final Class<?> clazz ) throws FileNotFoundException {
		return getWarRoot(clazz, TraceHelper.CALLER);
	}

	/**
	 * For servlet 2.5+ use ServletContext instead.
	 *
	 * @param clazz
	 *            : loaded class
	 * @param searchDepth
	 *            : to find loaded class's pkgnamepath {@link TraceHelper#CALLER} or {@link TraceHelper#CURRENT}
	 * @return fullpath of WEB-INF's parent dir( eg. C:\webapps\dynamic )
	 * @throws FileNotFoundException
	 */
	public static String getWarRoot( final Class<?> clazz, int searchDepth ) throws FileNotFoundException {
		String path = getClasspathRoot(clazz, searchDepth + 1);
		File classpathRootFile = new File(path);
		if( "WEB-INF".equals(classpathRootFile.getParentFile().getName()) ) {
			return classpathRootFile.getParentFile().getParentFile().getAbsolutePath();
		} else {
			throw new FileNotFoundException(clazz.getName() + " cannot find WEB-INF folder.(maybe tried from jar?)");
		}
	}

	public static boolean hasParentPathForm( String path ) {
		if( path == null )
			return false;

		return path.contains("..");
	}

	public static URL getChildURL( URL parent, String pathName ) throws MalformedURLException {
		if( pathName == null )
			pathName = "";
		if( !pathName.endsWith("/") )
			pathName = pathName + "/";

		pathName = FileUtil.backslashToslash(pathName);
		if( pathName.startsWith("/") )
			pathName = pathName.substring(1, pathName.length());

		return new URL(parent + pathName);
	}

	public static boolean isAbsolutePathForm( String path ) {
		if( path == null )
			return false;

		if( path.startsWith("/") || path.matches("^.\\:.*") || path.startsWith("file://") ) {
			return true;
		} else {
			return false;
		}
	}

	public static java.net.URL resolveClasspath( String path ) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		// String pkgPath = LocationUtil.getPkgnameToPkgpath(refClazz.getPackage().getName());

		if( !path.startsWith("/")
				|| !path.startsWith("\\.") ) {
			path = "/" + path;
		}
		return cl.getResource(path);
	}
}
