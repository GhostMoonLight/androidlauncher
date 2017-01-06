package com.android.launcher3.plugin;

import java.lang.reflect.Field;

public class ReflexUtil {

	/**
	 * 反射获取类对象中指定字段的对象
	 * @param className      获取字节码的类名
	 * @param fieldName      字段名
	 * @param obj            该类的对象
	 * @return
	 */
	public static Object getField(String className, String fieldName, Object obj ){
		Object o=null;
		try {
			Field field = Class.forName(className).getDeclaredField(fieldName);
			field.setAccessible(true);
			o = field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  o;
	}

	/**
	 * 反射执行类的静态方法
	 * @param className                 获取字节码的类名
	 * @param methodName                方法名
	 * @param parameterClassTypes       参数类型集合  无参数传new Class[] {}
	 * @param parameterObjects          参数集合      无参数传new Object[] {}
	 * @return
	 */
	public static Object executeMethodReflect(String className, String methodName,  Class[] parameterClassTypes, Object[] parameterObjects){
		Object o = null;
        try {
            o = Class.forName(className).getMethod(methodName, parameterClassTypes).invoke(null, parameterObjects);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return o;
	}

	/**
	 * 通过反射给某个字段赋值
	 * @param className    获取字节码的类名
	 * @param fieldName    字段名
	 * @param obj          该类的对象
     * @param value        值
     */
	public static void setFieldValue(String className, String fieldName, Object obj, Object value){
		try {
			Field field = Class.forName(className).getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
