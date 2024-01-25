package de.cubbossa.tinytranslations.nanomessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ObjectTagResolverMap {

    private final Map<Class<?>, Map<String, Function<?, ?>>> productions = new LinkedHashMap<>();

    public ObjectTagResolverMap() {
        put(String.class, Collections.emptyMap(), Component::text);
    }

    public <T> void put(Class<T> type, Map<String, Function<T, ?>> productions) {
        put(type, productions, null);
    }

    public <T> void put(Class<T> type, Map<String, Function<T, ?>> productions, @Nullable Function<T, ComponentLike> fallback) {
        var inner = this.productions.computeIfAbsent(type, aClass -> new LinkedHashMap<>());
        productions.forEach((s, stringObjectFunction) -> inner.putAll(productions));
        inner.put("", fallback);
    }

    public @Nullable Object resolve(Object obj, String path) {
        Queue<String> pathQueue = path.isEmpty()
                ? new LinkedList<>()
                : new LinkedList<>(Arrays.stream(path.split(":")).toList());
        return resolve(obj, pathQueue);
    }

    public @Nullable Object resolve(@Nullable Object o, Queue<String> path) {
        while (!(o instanceof ComponentLike) && o != null) {
            boolean anymatch = false;
            for (Map.Entry<Class<?>, Map<String, Function<?, ?>>> e : productions.entrySet()) {
                if (e.getKey().isAssignableFrom(o.getClass())) {
                    anymatch = true;
                    if (path.isEmpty()) {
                        // default transformation
                        var fun = (Function<Object, Object>) e.getValue().get("");
                        if (fun != null) {
                            o = fun.apply(o);
                        } else {
                            o = o.toString();
                        }
                    } else {
                        Function<Object, Object> fun = (Function<Object, Object>) e.getValue().get(path.peek());
                        if (fun == null) {
                            fun = (Function<Object, Object>) e.getValue().get("");
                            if (fun == null) {
                                o = null;
                            } else {
                                o = fun.apply(o);
                            }
                        } else {
                            path.poll();
                            o = fun.apply(o);
                        }
                    }
                    break;
                }
            }
            if (!anymatch) {
                o = Objects.toString(o);
            }
        }
        return o;
    }

}
