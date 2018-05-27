package com.lanxiang.zk.practice.common.utils;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;

import com.lanxiang.zk.practice.common.exception.PracticeException;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class FieldValueTransUtil {

    public static Object transferValue(Field field, String value) throws PracticeException {
        Object result;
        String fieldName = field.getDeclaringClass().getName() + "." + field.getName();
        String typeName = field.getType().getSimpleName();
        try {
            if (StringUtils.equals(typeName, "String")) {
                result = value;
            } else if (StringUtils.equals(typeName, "int") || StringUtils.equals(typeName, "Integer")) {
                result = Integer.valueOf(value);
            } else if (StringUtils.equals(typeName, "long") || StringUtils.equals(typeName, "Long")) {
                result = Long.valueOf(value);
            } else if (StringUtils.equals(typeName, "float") || StringUtils.equals(typeName, "Float")) {
                result = Float.valueOf(value);
            } else if (StringUtils.equals(typeName, "double") || StringUtils.equals(typeName, "Double")) {
                result = Double.valueOf(value);
            } else if (StringUtils.equals(typeName, "boolean") || StringUtils.equals(typeName, "Boolean")) {
                result = Boolean.valueOf(value);
            } else {
                throw new PracticeException("UNEXPECTED FIELD (" + fieldName + ") TYPE : TYPE IS " + typeName);
            }
        } catch (Exception e) {
            throw new PracticeException("transfer value for " + fieldName + " failed", e);
        }
        return result;
    }

}
