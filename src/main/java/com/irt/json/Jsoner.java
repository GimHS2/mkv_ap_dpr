/*
 *	File Name:	Jsoner.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/10/30		2.2.0c	create
 *
**/

package com.irt.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.irt.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;
import org.json.XML;

public class Jsoner {

	private static Jsoner jsoner = null;

	public static Jsoner getInstance() {
		if( jsoner == null )
			jsoner = new Jsoner();

		return jsoner;
	}

	/**
	 * 주의: xml의 XSD상에는 multiple이지만 xml 문서에서는 element가 single인 경우에 json은 이 데이터를 리스트로 인식해야 하는데 map으로 인식해버릴 수도 있음.
	 * 
	 * @param interestPath
	 *            : json path for interested (eg. "/biztalk_1/body/doc:SalesOrder.Simulate.Response")
	 * @param safeName
	 *            : replace "." to "_" for some safe naming.( for apache drill )
	 */
	public static JSONObject getJsonFromXml( java.io.Reader reader, String interestPath, boolean safeName ) {
		if( interestPath == null )
			throw new IllegalStateException("pathName is mandatory.");

		JSONObject xmlJsonObj = XML.toJSONObject(reader);

		String[] paths = interestPath.split("/");

		org.json.JSONObject wrapper = new JSONObject();
		if( paths != null && paths.length == 0 ) {// ROOT "when interstPath is /"
			wrapper = xmlJsonObj;
		} else {
			String interest_Name = paths[paths.length - 1];

			if( safeName ) {
				interest_Name = interest_Name.replaceAll("\\.", "_");
			}
			wrapper.put(interest_Name, xmlJsonObj.query(interestPath));
		}

		return wrapper;
	}

	public static JSONObject getJsonFromXml( String xmlString, String interestPath, boolean safeName ) {
		return getJsonFromXml(new StringReader(xmlString), interestPath, safeName);
	}

	public static Jsoner getNewInstance() {
		return new Jsoner();
	}

	public static String getPureColumnKey( String columnKey ) {
		if( columnKey != null && columnKey.endsWith(".millis") ) {
			return columnKey.replaceAll(".millis", "");
		}

		return columnKey;
	}

	public static void writeJson( Map<String, Object> wrapper, String outputPath ) throws IOException {
		writeJson(new org.json.JSONObject(wrapper), outputPath);
	}

	public static void writeJson( org.json.JSONObject wrapper, String outputPath ) throws IOException {
		writeJson(wrapper.toString(), outputPath);
	}

	public static void writeJson( String jsonString, String outputPath ) throws IOException {
		ByteArrayInputStream bais2 = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));
		BufferedInputStream bis2 = new BufferedInputStream(bais2);
		FileOutputStream fos2 = new FileOutputStream(outputPath);
		FileUtil.copyFileContent(bis2, fos2);
		bis2.close();
		fos2.close();
	}

	GsonBuilder builder;

	public Jsoner() {
		GsonBuilder b = new GsonBuilder();

		b.registerTypeAdapter(TimeZone.class, new TimeZoneAdapter());

		builder = b;
	}

	public Map fromJson( String jsonString ) {
		return getGson().fromJson(jsonString, java.util.Map.class);
	}

	public <T> T fromJson( String jsonString, Class<T> classOfT ) {
		return getGson().fromJson(jsonString, classOfT);
	}

	public <T> T fromJson( String jsonString, java.lang.reflect.Type typeOfT ) {
		return getGson().fromJson(jsonString, typeOfT);
	}

	private Gson getGson() {
		return builder.create();
	}

	public void setPrettyPrinting() {
		builder.setPrettyPrinting();
	}

	public String toJson( Map obj ) {
		return getGson().toJson(obj, Map.class);
	}

	public String toJson( Object obj ) {
		return getGson().toJson(obj);
	}

	public void writeListToFile( List<Map<String, Object>> list, File file ) throws IOException {
		java.io.FileWriter fw = new java.io.FileWriter(file);
		getGson().toJson(list, List.class, fw);
		fw.close();
	}

}
