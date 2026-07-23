/*
 *	File Name:	TimeZoneAdapter.java
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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.TimeZone;

public class TimeZoneAdapter extends TypeAdapter<TimeZone> {

	@Override
	public TimeZone read( JsonReader reader ) throws IOException {
		if( reader.peek() == JsonToken.NULL ) {
			reader.nextNull();
			return null;
		}

		return java.util.TimeZone.getTimeZone(reader.nextString());
	}

	@Override
	public void write( JsonWriter writer, TimeZone value ) throws IOException {
		if( value == null ) {
			writer.nullValue();
			return;
		}

		writer.value(value.getID());
	}
}
