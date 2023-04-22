package com.rezolvemc.common.util;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

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

    public static List<Class<?>> findAnnotatedClasses(Class<?> annotationClass) {
        Type annotationType = Type.getType(annotationClass);
        List<ModFileScanData> allScanData = ModList.get().getAllScanData();
        Set<Class<?>> pluginClassNames = new LinkedHashSet<>();
        
        for (ModFileScanData scanData : allScanData) {
            Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
            for (ModFileScanData.AnnotationData a : annotations) {
                if (!Objects.equals(a.annotationType(), annotationType))
                    continue;

                String memberName = a.memberName();
                try {
                    pluginClassNames.add(Class.forName(memberName));
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return pluginClassNames.stream().collect(Collectors.toList());
    }
}
