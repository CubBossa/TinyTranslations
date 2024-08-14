package de.cubbossa.tinytranslations.tinyobject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class TinyObjectResolverImpl implements TinyObjectResolver {

    public TinyObjectResolverImpl() {
    }

    @Override
    public @Nullable Object resolveObject(@NotNull Object object, Iterable<String> path, Collection<TinyObjectMapping> mappings) {
        Queue<String> queue = new LinkedList<>();
        path.iterator().forEachRemaining(queue::add);
        return resolve(object, queue, mappings);
    }

    private @Nullable Object resolve(@Nullable Object obj, Queue<String> path, Collection<TinyObjectMapping> mappings) {
        List<TinyObjectMapping> m = new ArrayList<>(new LinkedHashSet<>(mappings));
        Collections.reverse(m);

        // convert object step by step until it is componentlike.
        while (obj != null) {

            if (Arrays.stream(obj.getClass().getAnnotations()).anyMatch(annotation -> annotation.annotationType().equals(TinyObject.class))) {
                TinyObjectMapping.Builder<?> builder = TinyObjectMapping.builder(obj.getClass());
                for (Field f : obj.getClass().getDeclaredFields()) {
                    for (Annotation annotation : f.getAnnotations()) {
                        if (annotation.annotationType().equals(TinyProperty.class)) {
                            String name;
                            if (Objects.equals(((TinyProperty) annotation).name(), "$")) {
                                name = f.getName().toLowerCase();
                            } else {
                                name = ((TinyProperty) annotation).name();
                            }
                            builder.with(name, o -> getFieldValue(f, o));
                        } else if (annotation.annotationType().equals(TinyDefault.class)) {
                            builder.withFallbackConversion(o -> getFieldValue(f, o));
                        }
                    }
                }
                m.add(builder.build());
            }

            boolean noneMatch = true;
            for (TinyObjectMapping mapping : m) {
                // check if mapping applies to object
                if (!mapping.matches(obj)) {
                    continue;
                }
                if (path.isEmpty()) {
                    // no further path given. Convert obj to string and string to componentlike
                    obj = mapping.resolve(obj);
                    noneMatch = false;
                    break;
                } else {
                    // if given key can be interpreted resolve. Otherwise, we run in invalid path
                    if (mapping.containsKey(path.peek())) {
                        obj = mapping.resolve(obj, path.poll());
                        noneMatch = false;
                        break;
                    }
                }
            }
            if (noneMatch) {
                if (!path.isEmpty()) {
                    // a path that is not represented in any mapping, therefore null
                    return null;
                } else {
                    break;
                }
            }
        }
        return obj;
    }

    private Object getFieldValue(Field field, Object obj) {
        try {
            if (!field.canAccess(obj)) {
                field.setAccessible(true);
            }
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
