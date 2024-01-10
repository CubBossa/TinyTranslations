package de.cubbossa.tinytranslations.nanomessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ObjectTagResolverMap {

	private final Map<Class<?>, Map<String, Function<?, ?>>> productions = new LinkedHashMap<>();

	public ObjectTagResolverMap() {

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
		Queue<String> pathQueue = new LinkedList<>(Arrays.stream(path.split(":")).toList());
		return resolve(obj, pathQueue);
	}

	public @Nullable Object resolve(@Nullable Object obj, Queue<String> path) {
		if (obj == null) {
			return null;
		}
		while (!path.isEmpty() && path.peek().isEmpty()) {
			path.poll();
		}
		for (Map.Entry<Class<?>, Map<String, Function<?, ?>>> e : productions.entrySet()) {
			if (e.getKey().isAssignableFrom(obj.getClass())) {
				if (path.isEmpty()) {
					var fun = (Function<Object, Object>) e.getValue().get("");
					if (fun == null) {
						return obj;
					}
					return fun.apply(obj);
				}
				Function<Object, Object> fun = (Function<Object, Object>) e.getValue().get(path.peek());
				if (fun == null) {
					fun = (Function<Object, Object>) e.getValue().get("");
					if (fun == null) {
						return null;
					}
				}
				path.poll();
				return resolve(fun.apply(obj), path);
			}
		}
		return obj;
	}

}
