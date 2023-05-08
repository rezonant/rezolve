package org.torchmc.util;

import java.util.Objects;

public class Values {
    public static <T> T coalesce(T a, T b) {
        if (a == null)
            return b;
        else
            return a;
    }

    public static boolean isEmpty(String value) {
        return Objects.equals(value, "");
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || isEmpty(value);
    }

    public static boolean instanceOf(Class<?> subclass, Class<?> superclass) {
        return superclass.isAssignableFrom(subclass);
    }

}
