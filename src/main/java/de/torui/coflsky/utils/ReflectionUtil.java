package de.torui.coflsky.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ReflectionUtil {

    public static MethodHandle getField(Class<?> clazz, String... names) {
        Field f = null;
        for (String name : names) {
            try {
                f = clazz.getDeclaredField(name);
                break;
            } catch (NoSuchFieldException e) {
            }
        }
        if (f == null)
            throw new RuntimeException("Could not find any of fields " + Arrays.toString(names) + " on class " + clazz);
        f.setAccessible(true);
        try {
            return MethodHandles.publicLookup().unreflectGetter(f);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
