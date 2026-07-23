/*
 *	File Name:	MultipartHttpRequest.java
 *	Version:	2.2.3c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/04/30		2.2.3c	detectEncoding() : 버퍼 80 -> 85로 변경
 *	dudwls3720	2024/07/31		2.2.2c	readNextPart() : 파일이름에 .. 검사, 파일 업로드 10M 제한되도록 변경
 *	jbaek		2018/04/30		2.2.1c	getDetectedEncoding()
 *	stghr12		2008/03/31		2.2.1	생성자에서 saveDirectory type을 String에서 File로 변경
 *	stghr12		2007/11/30		2.2.0	getFilesystemName() 삭제
 *										maxPostSize: int -> long으로 변경
 *	stghr12		2006/04/30		2.1.0	업로드파일 관리방법 수정 File.createTempFile() 사용
 *	stghr12		2006/09/15		2.0.1	Encoding 변경(ISO-8859-1 -> UTF-8)
 *	stghr12		2006/02/28		2.0.0	create(MultipartRequest -> MultipartHttpRequest, HttpServletRequestWrapper 사용)
 *
**/

package com.irt.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * ContentType이 multipart/form-data인 ServletRequest에서 file처리를 하는 Class
 */
public class MultipartHttpRequest extends javax.servlet.http.HttpServletRequestWrapper {
	private final static long DEFAULT_MAX_POST_SIZE		= 0x1000000; // 1MB
	private File saveDirectory;
	private Map files, parameters;

	public MultipartHttpRequest( HttpServletRequest request, File saveDirectory ) throws IOException {
		this( request, saveDirectory, DEFAULT_MAX_POST_SIZE );
	}

	public MultipartHttpRequest( HttpServletRequest request, File saveDirectory, long maxPostSize ) throws IllegalArgumentException, IOException {
		super( request );

		parameters = new java.util.Hashtable();
		files = new java.util.Hashtable();

		if( request == null ) throw new IllegalArgumentException( "request cannot be null" );
		if( saveDirectory == null ) throw new IllegalArgumentException( "saveDirectory cannot be null" );
		if( maxPostSize <= 0 ) throw new IllegalArgumentException( "maxPostSize must be positive" );

		this.saveDirectory = saveDirectory;
		if( !this.saveDirectory.isDirectory() ) throw new IllegalArgumentException( "Not a directory: " + saveDirectory );
		if( !this.saveDirectory.canWrite() ) throw new IllegalArgumentException( "Not writable: " + saveDirectory );

		readRequest( request, maxPostSize );

		for( java.util.Iterator iterator = parameters.keySet().iterator(); iterator.hasNext(); ) {
			String key = (String)iterator.next();
			List list = (List)parameters.get( key );

			parameters.put( key, list.toArray(new String[list.size()]) );
		}
	}

	/**
	 * 임시로 만든 파일을 모두 삭제한다. 반드시 call해 주어야 한다.
	 */
	public void close() {
		String[] files = getFileNames();
		if( files != null ) {
			for( int f = 0; f < files.length; f++ ) {
				File file = getFile( files[f] );
				if( file != null ) file.delete();
			}
		}
	}

	private String extractBoundary( String line ) {
		int index = line.indexOf( "boundary=" );
		if( index == -1 ) return null;
		return "--" + line.substring( index+9 );
	}

	private String extractContentType( String line ) throws IOException {
		String contentType = null;
		String origline = line;

		line = origline.toLowerCase();
		if( line.startsWith("content-type") ) {
			int start = line.indexOf( " " );
			if( start == -1 )
				throw new IOException( "Content type corrupt: "+ origline );
			contentType = line.substring( start + 1 );
		} else if( line.length() != 0 )
			throw new IOException( "Malformed line after disposition: "+ origline );

		return contentType;
	}

	private String[] extractDispositionInfo( String line ) throws IOException {
		String retval[] = new String[3];
		String origline = line;

		line = origline.toLowerCase();
		int start = line.indexOf( "content-disposition: " );
		int end = line.indexOf( ";" );
		if( start == -1 || end == -1 )
			throw new IOException( "Content disposition corrupt: "+ origline );

		String disposition = line.substring( start + 21, end );
		if( !disposition.equals("form-data") )
			throw new IOException( "Invalid content disposition: "+ disposition );

		start = line.indexOf( "name=\"", end );
		end = line.indexOf( "\"", start + 7 );
		if( start == -1 || end == -1 )
			throw new IOException( "Content disposition corrupt: "+ origline );
		String name = origline.substring( start + 6, end );

		String filename = null;
		start = line.indexOf( "filename=\"", end + 2 );
		end = line.indexOf( "\"", start + 10 );
		if( start != -1 && end != -1 ) {
			filename = origline.substring( start + 10, end );
			int slash = Math.max( filename.lastIndexOf(47), filename.lastIndexOf(92) );
			if( slash > -1 )
				filename = filename.substring( slash + 1 );
			if( filename.equals("") )
				filename = "unknown";
		}
		retval[0] = disposition;
		retval[1] = name;
		retval[2] = filename;

		return retval;
	}

	public File getFile( String name ) {
		UploadedFile file = (UploadedFile)files.get( name );
		return ( file == null ? null : file.file );
	}

	/**
	 *
	 * @param name
	 * @return if detect confidence is less than 80% then return null;
	 * @throws IOException
	 */
	public String getDetectedEncoding( String name ) throws IOException {
		UploadedFile file = (UploadedFile)files.get( name );
		return file.detectEncoding();
	}

	public String getFileContentType( String name ) {
		UploadedFile file = (UploadedFile)files.get( name );
		return ( file == null ? null : file.type );
	}

	public String[] getFileNames() {
		if( files.size() == 0 ) return null;
		return (String[])files.keySet().toArray( new String[files.size()] );
	}

	public String getInputFileName( String name ) {
		UploadedFile file = (UploadedFile)files.get( name );
		return ( file == null ? null : file.inputname );
	}

	@Override
	public javax.servlet.ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getParameter( String name ) {
		String[] array = (String[])parameters.get( name );
		return ( array == null ? null : array[0] );
	}

	@Override
	public Map getParameterMap() {
		return java.util.Collections.unmodifiableMap( parameters );
	}

	@Override
	public java.util.Enumeration getParameterNames() {
		return ((java.util.Hashtable)parameters).keys();
	}

	@Override
	public String[] getParameterValues( String name ) {
		return (String[])parameters.get( name );
	}

	@Override
	public java.io.BufferedReader getReader() {
		return null;
	}

	void readAndSaveFile( InputStreamHandler in, File file ) throws IOException {
		java.io.FileOutputStream fos = new java.io.FileOutputStream( file );
		java.io.BufferedOutputStream out = new java.io.BufferedOutputStream( fos, 8192 );
		byte bbuf[] = new byte[0x19000];
		boolean rnflag = false;
		int result;
		while( (result = in.readLine(bbuf, 0, bbuf.length)) != -1 ) {
			if( result > 2 && bbuf[0] == 45 && bbuf[1] == 45 ) {
				String line = new String( bbuf, 0, result, "ISO-8859-1" );
				if( line.startsWith(in.boundary) )
					break;
			}
			if( rnflag ) {
				out.write(13);
				out.write(10);
				rnflag = false;
			}
			if( result >= 2 && bbuf[result - 2] == 13 && bbuf[result - 1] == 10 ) {
				out.write( bbuf, 0, result - 2 );
				rnflag = true;
			} else
				out.write( bbuf, 0, result );
		}
		out.flush();
		out.close();
		fos.close();
	}

	void readFile( InputStreamHandler in ) throws IOException {
		byte bbuf[] = new byte[0x19000];
		int result;
		while( (result = in.readLine(bbuf, 0, bbuf.length)) != -1 ) {
			if( result > 2 && bbuf[0] == 45 && bbuf[1] == 45 ) {
				String line = new String( bbuf, 0, result, "ISO-8859-1" );
				if( line.startsWith(in.boundary) )
					break;
			}
		}
	}

	boolean readNextPart( InputStreamHandler in ) throws IOException {
		String line = in.readLine();
		if( line == null ) return true;

		String dispInfo[] = extractDispositionInfo( line );
		String disposition = dispInfo[0];
		String name = dispInfo[1];
		String filename = dispInfo[2];

		line = in.readLine();
		if( line == null ) return true;

		String contentType = extractContentType( line );
		if( contentType != null ) {
			line = in.readLine();
			if( line == null || line.length() > 0 )
				throw new IOException( "Malformed line after content type: "+ line );
		} else
			contentType = "application/octet-stream";

		if( filename == null ) {
			String value = readParameter( in );
			List list = (List)(parameters.get(name));

			if( list == null )
				parameters.put( name, list = new java.util.ArrayList() );
			list.add( value );
		} else {
			if( "unknown".equals(filename) )
				readFile( in );
			else {
				if( filename.indexOf("..") > 0 )
					throw new IOException( "invalid file name." );

				File tempfile = File.createTempFile( "requestfile", null, saveDirectory );
				readAndSaveFile( in, tempfile );
				files.put( name, new UploadedFile(filename, tempfile, contentType) );
				if( (tempfile.length() / 1024 / 1024) > 10 )
					throw new IOException( "Upload file size exceeds limit of 10M." );
			}
		}

		return false;
	}

	String readParameter( InputStreamHandler in ) throws IOException {
		StringBuffer sbuf = new StringBuffer();
		String line;

		while( (line = in.readLine()) != null ) {
			if( line.startsWith(in.boundary) )
				break;
			sbuf.append( line +"\r\n" );
		}

		if( sbuf.length() == 0 )
			return null;
		else {
			sbuf.setLength( sbuf.length() - 2 );
			return sbuf.toString();
		}
	}

	void readRequest( HttpServletRequest req, long maxPostSize ) throws IOException {
		String type = req.getContentType();
		if( type == null || !type.toLowerCase().startsWith("multipart/form-data") )
			throw new IOException( "Posted content type isn't multipart/form-data" );
		int length = req.getContentLength();
		if( length > maxPostSize )
			throw new IOException( "Posted content length of "+ length +" exceeds limit of "+ maxPostSize );

		String boundary = extractBoundary( type );
		if( boundary == null )
			throw new IOException( "Separation boundary was not specified" );
		InputStreamHandler in = new InputStreamHandler( req.getInputStream(), boundary, length );

		String line = in.readLine();
		if( line == null ) throw new IOException( "Corrupt form data: premature ending" );
		if( !line.startsWith(boundary) ) throw new IOException( "Corrupt form data: no leading boundary" );
		for( boolean done = false; !done; done = readNextPart(in) );
	}

	class InputStreamHandler {
		javax.servlet.ServletInputStream in;
		String boundary;
		int totalRead, totalExpected;
		byte[] buf = new byte[8192];

		public InputStreamHandler( javax.servlet.ServletInputStream in, String boundary, int totalExpected ) {
			this.in = in;
			this.boundary = boundary;
			this.totalExpected = totalExpected;
		}

		public String readLine() throws IOException {
			StringBuffer sbuf = new StringBuffer();
			int result;
			do {
				result = readLine( buf, 0, buf.length );
				if( result != -1 )
					sbuf.append( new String(buf, 0, result, "UTF-8") );
			} while( result == buf.length );

			if( sbuf.length() == 0 )
				return null;
			else {
				sbuf.setLength( sbuf.length() - 2 );
				return sbuf.toString();
			}
		}

		public int readLine( byte b[], int off, int len ) throws IOException {
			if( totalRead >= totalExpected ) return -1;

			int result = in.readLine(b, off, len);
			if( result > 0 )
				totalRead += result;

			return result;
		}
	}

	class UploadedFile {
		private String inputname, type;
		private File file;

		UploadedFile( String inputname, File file, String type ) {
			this.inputname = inputname;
			this.file = file;
			this.type = type;
		}

		public String detectEncoding() throws IOException {
			if( file == null ) return null;

			java.io.FileInputStream fis = new java.io.FileInputStream(file);
			java.io.BufferedInputStream buffer = new java.io.BufferedInputStream(fis);

			String ret = null;
			try {
				ret = com.irt.util.CharsetUtil.detectEncoding(buffer, 85);
			} finally {
				buffer.close();
				fis.close();
			}
			return ret;
		}
	}
}
