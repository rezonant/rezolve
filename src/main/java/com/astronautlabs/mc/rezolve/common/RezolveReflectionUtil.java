package com.astronautlabs.mc.rezolve.common;

public class RezolveReflectionUtil {
    public static <T> T getStaticField(Class klass, String fieldName) {
        try {
            var field = klass.getField(fieldName);
            return (T)field.get(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
