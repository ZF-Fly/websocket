package com.zf.microservice.wsxch.restapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 反射处理工具类
 */
public class ReflectionUtil {

    public static Object invoke(Object object, String methodName) {
        Object result = null;

        if (object != null) {
            try {
                Class objectClass = object.getClass();

                Method method = objectClass.getDeclaredMethod(methodName);
                if (method != null) {
                    result = method.invoke(object);
                }
            } catch (Exception e) {
                LOGGER.error("invoke method failure", e);
            }
        }

        return result;
    }

    public static Boolean isSampleType(Field field) {
        Class fieldClass = field.getType();

        if (fieldClass.isPrimitive()) {
            return true;
        }

        if (Number.class.isAssignableFrom(fieldClass)) {
            return true;
        }

        if (Boolean.class.isAssignableFrom(fieldClass)) {
            return true;
        }

        if (Character.class.isAssignableFrom(fieldClass)) {
            return true;
        }

        if (Date.class.isAssignableFrom(fieldClass)) {
            return true;
        }

        if (String.class.isAssignableFrom(fieldClass)) {
            return true;
        }

        return false;
    }

    public static <E extends Serializable> E clone(E source) {
        E target = null;

        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;

        ObjectInputStream objectInputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();

            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(source);
            objectOutputStream.close();

            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

            objectInputStream = new ObjectInputStream(byteArrayInputStream);

            target = (E) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            GC.closeAll(objectInputStream, objectOutputStream, byteArrayInputStream, byteArrayOutputStream);
        }

        return target;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

}
