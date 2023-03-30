package com.astronautlabs.mc.rezolve.common.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class RezolveReflectionUtil {
    /**
     * Obtain all @Repeatable annotations of the given type across an entire class heirarchy
     * @param klass
     * @return
     * @param <A>
     */
    public static <A extends Annotation> List<A> getHeirarchicalAnnotations(Class<?> klass, Class<A> annotationClass) {
        List<A> list = new ArrayList<>();
        getHeirarchicalAnnotations(klass, annotationClass, list);
        return list;
    }

    private static <A extends Annotation> void getHeirarchicalAnnotations(Class<?> klass, Class<A> annotationClass, List<A> annotations) {
        annotations.addAll(List.of(klass.getAnnotationsByType(annotationClass)));
        if (klass.getSuperclass() != null)
            getHeirarchicalAnnotations(klass.getSuperclass(), annotationClass, annotations);
    }

    public static <T> T getStaticField(Class klass, String fieldName) {
        try {
            var field = klass.getField(fieldName);
            return (T)field.get(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
