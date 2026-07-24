/*
 *	File Name:	RBMWorkbookPicture.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
 *
**/

package com.irt.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture;

public class RBMWorkbookPicture {

	/**
	 * Currently only works for XLSX file extension.
	 *
	 * @param xsheet
	 * @param target : URI the target part. Must be relative to the source root directory of the part.
	 * @param targetMode : {@link TargetMode#EXTERNAL} or {@link TargetMode#INTERNAL}
	 * @param xpict
	 */
	public static void addHyperlinkByImage( XSSFSheet xsheet, URI target, TargetMode targetMode, XSSFPicture xpict ) {
		final String relationshipType_hyperlink = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
		XSSFDrawing xdraw = xsheet.createDrawingPatriarch();

		PackageRelationship rel = xdraw.getPackagePart().addRelationship( target, targetMode, relationshipType_hyperlink );
		xdraw.addRelation( rel.getId(), new POIXMLDocumentPart() );

		org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPictureNonVisual nvPicPr = xpict.getCTPicture().getNvPicPr();
		org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink hlinkClick = nvPicPr.getCNvPr().addNewHlinkClick();
		hlinkClick.setId( rel.getId() );
	}

	public static void addImageOnCell( XSSFSheet sheet, int row, int col, int height, int width, int pictureIndex ) {
		CreationHelper helper = sheet.getWorkbook().getCreationHelper();

		Drawing draw = sheet.createDrawingPatriarch();

		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setAnchorType( ClientAnchor.MOVE_AND_RESIZE );

		// top-left
		anchor.setCol1( col );
		anchor.setRow1( row );

		// bottom-right
		anchor.setCol2( col );
		anchor.setRow2( row );

		// dx = left + wanted width
		anchor.setDx2( Units.toEMU( width ) );
		// dy = top + wanted height
		anchor.setDy2( Units.toEMU( height ) );

		draw.createPicture( anchor, pictureIndex );
	}

	/**
	 * @param workbook
	 * @param imageInClasspath
	 * @return the index to this picture (1 based)
	 * @throws IOException
	 */
	public static int addPictureData( Workbook wb, ClassLoader classLoader, String imageInClasspath ) throws IOException {
		// ClassPathResource resource = new ClassPathResource( "com/irt/dpr/DPRBillingReport_External.png" );
		if( imageInClasspath == null || !imageInClasspath.matches("^com/irt/dpr/[A-Za-z0-9_-]+\\.(?i:png|jpg|jpeg|gif|bmp)$") )
			throw new IOException( "Invalid classpath resource path: " + imageInClasspath );

		InputStream inputStream = null;
		try {
			inputStream = classLoader.getResourceAsStream( imageInClasspath );
			if( inputStream == null )
				throw new IOException( "Classpath resource not found: " + imageInClasspath );
			byte[] pictureData = IOUtil.readFully( inputStream );
			String extension = StringUtil.getFileExtension( imageInClasspath );
			int typeIdx = getPictureTypeByExtension( extension );

			return wb.addPicture( pictureData, typeIdx );
		} finally {
			if( inputStream != null )
				inputStream.close();
		}
	}

	public static int addPictureData( Workbook wb, String externalPath ) throws IOException {
		// ClassPathResource resource = new ClassPathResource( "com/irt/dpr/DPRBillingReport_External.png" );
		InputStream inputStream = null;
		try {
			inputStream = new java.io.FileInputStream(externalPath);
			byte[] pictureData = IOUtil.readFully( inputStream );
			String extension = StringUtil.getFileExtension( externalPath );
			int typeIdx = getPictureTypeByExtension( extension );

			return wb.addPicture( pictureData, typeIdx );
		} finally {
			if( inputStream != null )
				inputStream.close();
		}
	}

	public static CTOneCellAnchor createOneCellAnchor( XSSFDrawing drawing, XSSFClientAnchor anchor ) {
		final int pixel2emu = 12700;
		CTOneCellAnchor ctAnchor = drawing.getCTDrawing().addNewOneCellAnchor();

		long cx = ( anchor.getTo().getRowOff() - anchor.getFrom().getRowOff() ) * pixel2emu;
		long cy = ( anchor.getTo().getColOff() - anchor.getFrom().getColOff() ) * pixel2emu;
		CTPositiveSize2D size = CTPositiveSize2D.Factory.newInstance();
		size.setCx( cx );
		size.setCy( cy );
		ctAnchor.setExt( size );

		ctAnchor.setFrom( anchor.getFrom() );
		CTMarker m = ctAnchor.getFrom();
		m.setColOff( m.getColOff() * pixel2emu );
		m.setRowOff( m.getRowOff() * pixel2emu );
		ctAnchor.addNewClientData();
		try {
			Method mt = XSSFClientAnchor.class.getDeclaredMethod( "setFrom", CTMarker.class );
			mt.setAccessible( true );
			mt.invoke( anchor, ctAnchor.getFrom() );
		} catch( Exception e ) {
			throw new RuntimeException( "handle me", e );
		}

		return ctAnchor;
	}

	public static Picture createPicture( Sheet sheet, ClientAnchor anchor, int pictureIndex, boolean resizeToOriginal ) {
		return createPicture( sheet, anchor, pictureIndex, 1.0 );
	}

	public static Picture createPicture( Sheet sheet, ClientAnchor anchor, int pictureIndex, double resizeScale ) {
		Drawing draw = sheet.createDrawingPatriarch();
		if( resizeScale > 0 ) {
			Picture pict = draw.createPicture( anchor, pictureIndex );
			pict.resize( resizeScale );
			return pict;
		} else {
			return draw.createPicture( anchor, pictureIndex );
		}
	}

	public static XSSFPicture createPicture( XSSFClientAnchor anchor, int pictureIndex, XSSFDrawing drawing )
			throws Exception {
		Method m = XSSFDrawing.class.getDeclaredMethod( "addPictureReference", int.class );
		m.setAccessible( true );
		PackageRelationship rel = (PackageRelationship)m.invoke( drawing, (Integer)pictureIndex );

		long shapeId = 1000 + drawing.getCTDrawing().sizeOfOneCellAnchorArray();
		CTOneCellAnchor ctAnchor = createOneCellAnchor( drawing, anchor );
		CTPicture ctShape = ctAnchor.addNewPic();

		m = XSSFPicture.class.getDeclaredMethod( "prototype" );
		m.setAccessible( true );
		CTPicture ctp = (CTPicture)m.invoke( null );
		ctShape.set( ctp );
		ctShape.getNvPicPr().getCNvPr().setId( shapeId );

		Constructor<XSSFPicture> picCon = XSSFPicture.class
				.getDeclaredConstructor( XSSFDrawing.class, CTPicture.class );
		picCon.setAccessible( true );

		XSSFPicture shape = picCon.newInstance( drawing, ctShape );
		Field f = XSSFShape.class.getDeclaredField( "anchor" );
		f.setAccessible( true );
		f.set( shape, anchor );

		m = XSSFPicture.class.getDeclaredMethod( "setPictureReference", PackageRelationship.class );
		m.setAccessible( true );
		m.invoke( shape, rel );
		return shape;
	}

	/**
	 * Not All type is difined. if cannot find extension then, returns {@link Workbook#PICTURE_TYPE_PNG}
	 *
	 * @param extension
	 * @return
	 */
	public static int getPictureTypeByExtension( String extension ) {
		String ext = extension;
		if( extension.startsWith( "\\." ) ) {
			ext = extension.substring( 1 );
		}
		int typeIdx = -1;
		if( ext.startsWith( "bmp" ) || ext.startsWith( "dib" ) ) {
			typeIdx = Workbook.PICTURE_TYPE_DIB;
		} else if( ext.startsWith( "jpeg" ) || ext.startsWith( "jpg" ) ) {
			typeIdx = Workbook.PICTURE_TYPE_JPEG;
		} else if( ext.startsWith( "png" ) ) {
			typeIdx = Workbook.PICTURE_TYPE_PNG;
		} else if( ext.startsWith( "pict" ) || ext.startsWith( "pct" ) || ext.startsWith( "pic" ) ) {
			typeIdx = Workbook.PICTURE_TYPE_PICT;
		} else if( ext.startsWith( "emf" ) || ext.startsWith( "emz" ) ) {
			typeIdx = Workbook.PICTURE_TYPE_EMF;
		} else if( ext.startsWith( "wmf" ) || ext.startsWith( "wmz" ) ) {
			typeIdx = Workbook.PICTURE_TYPE_WMF;
		} else {
			typeIdx = Workbook.PICTURE_TYPE_PNG;
		}
		return typeIdx;
	}

}
