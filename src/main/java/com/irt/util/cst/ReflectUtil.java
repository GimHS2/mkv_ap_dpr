/*
 *	File Name:	ReflectUtil.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/01/30		2.2.0c	create
 *
**/

package com.irt.util.cst;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Utility methods using "java.lang.reflect".
 *
 */
public class ReflectUtil {

	public static Method[] getAccessibleMethods( Class<?> clazz ) {
		List<Method> result = new ArrayList<Method>();
		while( clazz != null ) {
			for( Method method : clazz.getDeclaredMethods() ) {
				int modifiers = method.getModifiers();
				if( Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers) ) {
					result.add(method);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return result.toArray(new Method[result.size()]);
	}

	@SuppressWarnings( "unchecked" )
	public static <T> T getAnnotationValue( Class<?> clazz, Class<? extends Annotation> annotationClass, String element ) throws Exception {
		Annotation annotation = clazz.getAnnotation(annotationClass);
		Method method = annotationClass.getMethod(element, (Class[])null);
		if( annotation == null )
			return ( (T)method.getDefaultValue() );
		return ( (T)method.invoke(annotation, (Object[])null) );
	}

	/**
	 * forced to setAccessible(true) to access the non-accessible field.
	 *
	 * @param clazz
	 * @param obj
	 * @return map {"fieldName" : "fieldValue"}
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Map<String, Object> getAllDeclaredFieldObjects( Class<?> clazz, Object obj )
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> fieldMap = new java.util.HashMap<String, Object>();

		// this
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for( java.lang.reflect.Field field : fields ) {
			field.setAccessible(true);
			fieldMap.put(field.getName(), field.get(obj));
		}

		Object tmp = null;
		Class<?> tmpc = clazz.getSuperclass();

		// parents
		while( tmpc != null ) {
			tmp = tmpc.cast(obj);
			fields = tmpc.getDeclaredFields();
			if( tmp != null && fields != null ) {
				for( java.lang.reflect.Field field : fields ) {
					field.setAccessible(true);
					fieldMap.put(field.getName(), field.get(tmp));
				}
			}
			tmpc = tmpc.getSuperclass();
		}

		return fieldMap;
	}

	public static boolean setDeclaredFieldObject( Class<?> clazz, Object classObj, String fieldName, Object fieldObj )
			throws IllegalArgumentException, IllegalAccessException {
		Field java_field = ReflectUtil.getDeclaredField(clazz, classObj, fieldName);
		if( java_field == null )
			return false;

		java_field.setAccessible(true);
		java_field.set(classObj, fieldObj);
		return true;
	}

	public static Field getDeclaredField( Class<?> clazz, Object classObj, String fieldName )
			throws IllegalArgumentException, IllegalAccessException {
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for( java.lang.reflect.Field field : fields ) {
			if( field.getName().equals(fieldName) ) {
				return field;
			}
		}

		return null;
	}

	/**
	 * 
	 * @param clazz
	 *            : CAUTION!!! needs the exact class( will not find super class at all )
	 * @param classObj
	 * @param fieldName
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object getDeclaredFieldObject( Class<?> clazz, Object classObj, String fieldName )
			throws IllegalArgumentException, IllegalAccessException {
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for( java.lang.reflect.Field field : fields ) {
			if( field.getName().equals(fieldName) ) {
				field.setAccessible(true);
				return field.get(classObj);
			}
		}

		return null;
	}

	public static Map<String, Field> getDeclaredFields( Class<?> clazz, Object obj )
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Field> fieldMap = new java.util.HashMap<String, Field>();

		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for( java.lang.reflect.Field field : fields ) {
			field.setAccessible(true);
			fieldMap.put(field.getName(), field);
		}

		return fieldMap;
	}

	public static Map<String, Object> getDeclaredFieldObjects( Class<?> clazz, Object obj )
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> fieldMap = new java.util.HashMap<String, Object>();

		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for( java.lang.reflect.Field field : fields ) {
			field.setAccessible(true);
			fieldMap.put(field.getName(), field.get(obj));
		}

		return fieldMap;
	}

	public static Map<String, Object> getDeclaredFieldObjectsByPattern( Class<?> clazz, Object obj, String prefix )
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> fieldMap = new java.util.HashMap<String, Object>();

		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for( java.lang.reflect.Field field : fields ) {
			if( field.getName().startsWith(prefix) ) {
				fieldMap.put(field.getName(), field.get(obj));
			}
		}
		return fieldMap;
	}

	public static Map<String, Object> getDeclaredStaticFieldObjectsByPattern( Class<?> clazz, String prefix )
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> fieldMap = new java.util.HashMap<String, Object>();

		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for( java.lang.reflect.Field field : fields ) {
			if( field.getName().startsWith(prefix) ) {
				fieldMap.put(field.getName(), field.get(null));
			}
		}
		return fieldMap;
	}

	public static Object invokeMethod( Object obj, String methodName, int paramCount, Object... params ) {
		Method method;
		Object requiredObj = null;
		Object[] parameters = new Object[paramCount];
		Class<?>[] classArray = new Class<?>[paramCount];
		for( int i = 0; i < paramCount; i++ ) {
			parameters[i] = params[i];
			classArray[i] = params[i].getClass();
		}
		try {
			method = obj.getClass().getDeclaredMethod(methodName, classArray);
			method.setAccessible(true);
			requiredObj = method.invoke(obj, params);
		} catch( NoSuchMethodException e ) {
			e.printStackTrace();
		} catch( IllegalArgumentException e ) {
			e.printStackTrace();
		} catch( IllegalAccessException e ) {
			e.printStackTrace();
		} catch( InvocationTargetException e ) {
			e.printStackTrace();
		}

		return requiredObj;
	}

	public static class FieldUtil {

		/**
		 * Sets a field value on a given object
		 *
		 * @param targetObject
		 *            the object to set the field value on
		 * @param fieldName
		 *            exact name of the field
		 * @param fieldValue
		 *            value to set on the field
		 * @return true if the value was successfully set, false otherwise
		 */
		public static boolean setFieldValue( Object targetObject, String fieldName, Object fieldValue ) {
			Field field;
			try {
				field = targetObject.getClass().getDeclaredField(fieldName);
			} catch( NoSuchFieldException e ) {
				field = null;
			}
			Class superClass = targetObject.getClass().getSuperclass();
			while( field == null && superClass != null ) {
				try {
					field = superClass.getDeclaredField(fieldName);
				} catch( NoSuchFieldException e ) {
					superClass = superClass.getSuperclass();
				}
			}
			if( field == null ) {
				return false;
			}
			field.setAccessible(true);
			try {
				field.set(targetObject, fieldValue);
				return true;
			} catch( IllegalAccessException e ) {
				return false;
			}
		}

		public static boolean setStaticFieldValue( Class targetClass, String fieldName, Object fieldValue ) {
			Field field;
			try {
				field = targetClass.getDeclaredField(fieldName);
			} catch( NoSuchFieldException e ) {
				field = null;
			}
			Class superClass = targetClass.getSuperclass();
			while( field == null && superClass != null ) {
				try {
					field = superClass.getDeclaredField(fieldName);
				} catch( NoSuchFieldException e ) {
					superClass = superClass.getSuperclass();
				}
			}
			if( field == null ) {
				return false;
			}
			field.setAccessible(true);
			try {
				field.set(null, fieldValue);
				return true;
			} catch( IllegalAccessException e ) {
				return false;
			}
		}

		public static Object getFieldValue( Object targetObject, String fieldName ) {
			Field field;
			try {
				field = targetObject.getClass().getDeclaredField(fieldName);
			} catch( NoSuchFieldException e ) {
				field = null;
			}
			Class superClass = targetObject.getClass().getSuperclass();
			while( field == null && superClass != null ) {
				try {
					field = superClass.getDeclaredField(fieldName);
				} catch( NoSuchFieldException e ) {
					superClass = superClass.getSuperclass();
				}
			}
			if( field == null ) {
				return null;
			}
			field.setAccessible(true);
			try {
				return field.get(targetObject);
			} catch( IllegalAccessException e ) {
				return null;
			}
		}

		public static Object getStaticFieldValue( Class targetClass, String fieldName ) {
			Field field;
			try {
				field = targetClass.getDeclaredField(fieldName);
			} catch( NoSuchFieldException e ) {
				field = null;
			}
			Class superClass = targetClass.getSuperclass();
			while( field == null && superClass != null ) {
				try {
					field = superClass.getDeclaredField(fieldName);
				} catch( NoSuchFieldException e ) {
					superClass = superClass.getSuperclass();
				}
			}
			if( field == null ) {
				return null;
			}
			field.setAccessible(true);
			try {
				return field.get(null);
			} catch( IllegalAccessException e ) {
				return null;
			}
		}

	}
}
