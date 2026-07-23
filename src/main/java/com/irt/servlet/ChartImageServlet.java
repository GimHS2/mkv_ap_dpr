/*
 *	File Name:	ChartImageServlet.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.1c	WebServlet annotation 추가
 *	stghr12		2008/03/31		2.2.1	saveImage( HttpServletRequest, BufferedImage, String ) -> saveImage( HttpServletRequest, BufferedImage, File )
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.servlet;

import java.awt.image.BufferedImage;
import java.util.Set;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 *
 */
@javax.servlet.annotation.WebServlet( name="ChartImageServlet", urlPatterns= { "/servlet/ChartImageServlet" } )
public class ChartImageServlet extends javax.servlet.http.HttpServlet {
	private final static int DEFAULT_BUFFER_SIZE		= 1024;

	private static boolean removing					= false;
	private static Set imageSet						= java.util.Collections.synchronizedSet( new java.util.HashSet<ChartImageServlet.ImageObject>() );

	public void destroy() {
		removeExpired();
	}

	public void doGet( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException {
		String imageKey = req.getParameter( "image" );
		if( imageKey == null ) return;

		ChartImageServlet.ImageObject imageObj = null;
		try {
			imageObj = (ChartImageServlet.ImageObject)req.getSession().getAttribute( imageKey );
		} catch( ClassCastException castEx ) {}

		res.setContentType("image/png");
		OutputStream outputStream = res.getOutputStream();
		try {
			if( imageObj != null ) {
				File imageFile = imageObj.getFile();
				BufferedImage image = imageObj.getBufferedImage();

				if( image != null ) {
					com.keypoint.PngEncoder encoder = new com.keypoint.PngEncoder(image, false, 0, 9);
					outputStream.write( encoder.pngEncode() );
				} else if( imageFile != null ) {
					byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
					InputStream inputStream = new FileInputStream( imageFile );
					try {
						int length;
						while( (length = inputStream.read(buffer)) >= 0 )
							outputStream.write( buffer, 0, length );
					} finally {
						try { inputStream.close(); } catch( Exception ex ) {}
					}
				}
				imageObj.timemillis = System.currentTimeMillis();
			}
			outputStream.flush();
		} finally {
			try { outputStream.close(); } catch( Exception ex ) {}
			removeExpired();
		}
	}

	public static void removeExpired() {
		if( !removing ) {
			try {
				removing = true;
				long timemillis = System.currentTimeMillis();
				for( java.util.Iterator<ChartImageServlet.ImageObject> iterator = imageSet.iterator(); iterator.hasNext(); ) {
					ChartImageServlet.ImageObject imageObj = iterator.next();
					if( timemillis >= imageObj.timemillis ) {
						File imageFile = imageObj.getFile();

						if( imageFile != null && imageFile.exists() ) imageFile.delete();
						imageObj.clear();
						iterator.remove();
					}
				}
			} finally {
				removing = false;
			}
		}
	}

	public static String saveImage( HttpServletRequest req, BufferedImage image, SystemConfig systemConfig ) throws IOException {
		if( "memory".equals(systemConfig.getProperty("chartImage.savingType", "file")) )
			return saveImage( req, image );
		else
			return saveImage( req, image, systemConfig.getTemporaryDirectory() );
	}

	public static String saveImage( HttpServletRequest req, BufferedImage image ) {
		return saveImage( req, new ChartImageServlet.ImageObject(System.currentTimeMillis(), image) );
	}

	public static String saveImage( HttpServletRequest req, BufferedImage image, File temporaryDirectory ) throws IOException {
		File imageFile = File.createTempFile( "chartimage_", ".png", temporaryDirectory );

		if( image != null ) {
			com.keypoint.PngEncoder encoder = new com.keypoint.PngEncoder(image, false, 0, 9);
			OutputStream outputStream = new FileOutputStream( imageFile );
			try {
				outputStream.write( encoder.pngEncode() );
			} finally {
				outputStream.close();
			}
		}
		return saveImage( req, new ChartImageServlet.ImageObject(System.currentTimeMillis() + 20000, imageFile) );
	}

	private static String saveImage( HttpServletRequest req, ChartImageServlet.ImageObject imageObj ) {
		HttpSession session = req.getSession();

		String imageKey = null;
		long timemillis = System.currentTimeMillis();
		synchronized( session ) {
			for( int k = 0; true; k++ ) {
				Object object = session.getAttribute( imageKey = ("chartimage"+ k) );
				if( object == null ) break;
			}
			session.setAttribute( imageKey, imageObj );
		}
		if( imageSet == null )
			imageSet = java.util.Collections.synchronizedSet( new java.util.HashSet<ChartImageServlet.ImageObject>() );
		imageSet.add( imageObj );

		return imageKey;
	}

	/**
	 *
	 */
	static class ImageObject {
		long timemillis;
		File imageFile;
		BufferedImage image;

		public ImageObject( long timemillis, File imageFile ) {
			this.timemillis = timemillis;
			this.imageFile = imageFile;
		}

		public ImageObject( long timemillis, BufferedImage image ) {
			this.timemillis = timemillis;
			this.image = image;
		}

		public void clear() {
			this.image = null;
			this.imageFile = null;
		}

		public BufferedImage getBufferedImage() {
			return image;
		}

		public File getFile() {
			return imageFile;
		}
	}
}
