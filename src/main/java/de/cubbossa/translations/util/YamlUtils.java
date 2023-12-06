package de.cubbossa.translations.util;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class YamlUtils {

  public static Map<String, Object> toDotNotation(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> e : map.entrySet()) {
      if (e.getValue() instanceof Map inner) {
        inner = toDotNotation(inner);
        inner.forEach((o, o2) -> {
          result.put(e.getKey() + "." + o, o2);
        });
      } else {
        result.put(e.getKey(), e.getValue());
      }
    }
    return result;
  }

  public static Map<String, Object> fromDotNotation(Map<String, ? extends Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> result = new HashMap<>();
    map.forEach((s, o) -> {
      String[] splits = s.split("\\.");
      LinkedList<String> keys = new LinkedList<>(List.of(Arrays.copyOfRange(splits, 0, splits.length - 1)));
      Map<String, Object> m = result;
      for (String key : keys) {
        Object obj = m.computeIfAbsent(key, s1 -> new HashMap<>());
        if (obj instanceof Map x) {
          m = x;
        } else {
          throw new IllegalStateException("Map contains a value at a tree node that is no leaf.");
        }
      }
      m.put(splits[splits.length - 1], o);
    });
    return result;
  }
}
