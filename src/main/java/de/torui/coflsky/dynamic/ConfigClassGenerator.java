package de.torui.coflsky.dynamic;

import de.torui.coflsky.config.SettingsCache;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.StubMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates configuration classes at runtime using ByteBuddy.
 * <p>
 * Each generated class is fully annotated and exposes getters for all fields.
 * A simple caching layer avoids redundant class generation.
 */
public final class ConfigClassGenerator {

    private ConfigClassGenerator() {}

    private static final Map<String, Class<?>> CACHE = new ConcurrentHashMap<>();

    /**
     * Generate a configuration POJO with the given fields.
     * The resulting class contains private fields, public getters, and the supplied annotations.
     *
     * @param classNamePrefix prefix for the generated class name (will be suffixed with a random ID)
     * @param fieldDefs       map of field-name -> FieldSpec (type + annotations)
     * @return loaded class
     */
    public static Class<?> generate(String classNamePrefix, Map<String, FieldSpec> fieldDefs) {
        Objects.requireNonNull(classNamePrefix, "classNamePrefix");
        Objects.requireNonNull(fieldDefs, "fieldDefs");
        if (fieldDefs.isEmpty()) throw new IllegalArgumentException("fieldDefs must not be empty");

        String signature = classNamePrefix + fieldDefs.hashCode();
        return CACHE.computeIfAbsent(signature, k -> createClass(classNamePrefix, fieldDefs));
    }

    /**
     * Instantiate generated config class and populate values from SettingsCache.
     */
    public static Object newInstance(Class<?> cfgClass) {
        try {
            Object obj = cfgClass.getDeclaredConstructor().newInstance();
            for (Field field : cfgClass.getDeclaredFields()) {
                field.setAccessible(true);
                String key = field.getName();
                Object value = SettingsCache.get(key);
                if (value != null && field.getType().isInstance(value)) {
                    field.set(obj, value);
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create dynamic config instance", e);
        }
    }

    /* internal */
    private static Class<?> createClass(String classNamePrefix, Map<String, FieldSpec> fieldDefs) {
        ByteBuddy bb = new ByteBuddy().with(new NamingStrategy.SuffixingRandom(classNamePrefix));
        DynamicType.Builder<?> builder = bb.subclass(Object.class)
                .defineConstructor(Visibility.PUBLIC)
                .intercept(StubMethod.INSTANCE);

        for (Map.Entry<String, FieldSpec> entry : fieldDefs.entrySet()) {
            String name = entry.getKey();
            FieldSpec spec = entry.getValue();
            builder = builder.defineField(name, spec.getType(), Visibility.PRIVATE)
                    .annotateField(spec.getAnnotations())
                    .defineMethod(getter(name), spec.getType(), Visibility.PUBLIC)
                    .intercept(FieldAccessor.ofField(name))
                    .annotateMethod(spec.getAnnotations());
        }

        return builder.make()
                .load(ConfigClassGenerator.class.getClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST)
                .getLoaded();
    }

    private static String getter(String field) {
        return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
    }

    /** Lightweight description of a field specification */
    public static class FieldSpec {
        private final Class<?> type;
        private final Annotation[] annotations;

        public FieldSpec(Class<?> type, Annotation... annotations) {
            this.type = type;
            this.annotations = annotations != null ? annotations : new Annotation[0];
        }

        public Class<?> getType() { return type; }
        public Annotation[] getAnnotations() { return annotations; }
    }
}
