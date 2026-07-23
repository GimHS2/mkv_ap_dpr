/*
 *	File Name:	PropertySetter.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/08/31		2.2.2	setProperties(): throw IllegalArgumentException 처리
 *	stghr12		2010/08/31		2.2.1	setProperties(): aaa.bbb.key도 지원(aaa.key만 지원했음)
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
public class PropertySetter {
	Object object;
	PropertyDescriptor[] descriptors;

	public PropertySetter( Object object ) throws IntrospectionException {
		this.object = object;
		this.descriptors = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
	}

	public static Object convertArg( String value, Class type ) throws NumberFormatException {
		if( value == null ) return null;

		value = value.trim();
		if( String.class.isAssignableFrom(type) )
			return value;
		else if( Integer.TYPE.isAssignableFrom(type) )
			return new Integer(value);
		else if( Long.TYPE.isAssignableFrom(type) )
			return new Long(value);
		else if( Float.TYPE.isAssignableFrom(type) )
			return new Float(value);
		else if( Double.TYPE.isAssignableFrom(type) )
			return new Double(value);
		else if( Boolean.TYPE.isAssignableFrom(type) )
			return Boolean.valueOf(value);
		else
			return null;
	}

	public PropertyDescriptor getPropertyDescriptor( String name ) {
		for( int i = 0; i < descriptors.length; i++ ) {
			if( name.equals(descriptors[i].getName()) )
				return descriptors[i];
		}

		return null;
	}

	public static void setProperties( Object object, Properties properties ) throws IntrospectionException, InvocationTargetException {
		setProperties( object, properties, null );
	}

	public static void setProperties( Object object, Properties properties, String prefix ) throws IntrospectionException, InvocationTargetException {
		(new PropertySetter(object)).setProperties( properties, prefix );
	}

	public void setProperties( Properties properties ) throws IntrospectionException, InvocationTargetException {
		setProperties( properties, (String)null );
	}

	public void setProperties( Properties properties, String prefix ) throws IntrospectionException, InvocationTargetException {
		int length = ( prefix == null ? 0 : prefix.length() );

		Set<String> objectNameSet = new java.util.HashSet<String>();
		for( Map.Entry entry : properties.entrySet() ) {
			String key = (String)entry.getKey();

			if( prefix == null || key.startsWith(prefix) ) {
				int index = key.indexOf( '.', length );
				if( index > 0 ) {
					if( index > length )
						objectNameSet.add( key.substring(length, index) );
				} else
					setProperty( key.substring(length), (String)entry.getValue() );
			}
		}

		for( String objectName : objectNameSet ) {
			PropertyDescriptor descriptor = getPropertyDescriptor( Introspector.decapitalize(objectName) );
			if( descriptor == null )
				throw new IllegalArgumentException( "cannot find get-method of '"+ objectName +"'." );

			Method getter = descriptor.getReadMethod();
			if( getter == null )
				throw new IllegalArgumentException( "cannot find get-method of '"+ objectName +"'." );

			try {
				Object object_new = getter.invoke( object );
				String prefix_new = ( prefix == null ? objectName +"." : prefix + objectName +"." );

				setProperties( object_new, properties, prefix_new );
			} catch( IllegalAccessException accessEx ) {
				throw new IllegalArgumentException( "error while getting object of '"+ objectName +"' : "+ accessEx.getMessage() );
			}
		}
	}

	public boolean setProperty( String name, String value ) throws InvocationTargetException {
		if( value == null ) return false;

		name = Introspector.decapitalize( name );

		PropertyDescriptor descriptor = getPropertyDescriptor( name );
		if( descriptor == null )
			throw new IllegalArgumentException( "cannot find write-method of '"+ name +"'." );

		Method setter = descriptor.getWriteMethod();
		if( setter == null )
			throw new IllegalArgumentException( "cannot find write-method of '"+ name +"'." );

		Class[] paramTypes = setter.getParameterTypes();
		if( paramTypes.length != 1 )
			throw new IllegalArgumentException( "cannot find write-method of '"+ name +"'." );

		Object arg;
		try {
			arg = convertArg( value, paramTypes[0] );
			if( arg == null )
				throw new IllegalArgumentException( "'"+ name +"'s value cannot be null." );
		} catch( NumberFormatException numberEx ) {
			throw new IllegalArgumentException( "error while setting value of '"+ name +"' : "+ numberEx.getMessage() );
		}

		try {
			setter.invoke( object, new Object[] { arg } );
		} catch( IllegalAccessException accessEx ) {
			throw new IllegalArgumentException( "error while setting value of '"+ name +"' : "+ accessEx.getMessage() );
		}

		return true;
	}
}
