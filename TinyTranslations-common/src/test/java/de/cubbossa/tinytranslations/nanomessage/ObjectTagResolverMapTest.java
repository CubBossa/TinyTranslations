package de.cubbossa.tinytranslations.nanomessage;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

class ObjectTagResolverMapTest {

    @Test
    void resolve() {

        ObjectTagResolverMap map = new ObjectTagResolverMap();
        map.put(Person.class, Map.of(
                "name", Person::name,
                "age", Person::age
        ));
        map.put(PersonRelations.class, Map.of(
                "data", PersonRelations::data,
                "friends", PersonRelations::friends
        ));
        map.put(List.class, Collections.emptyMap(), l -> text((String) l.stream()
                .map(Object::toString).collect(Collectors.joining(", "))));

        Assertions.assertEquals(
                text("a, b, c"),
                map.resolve(List.of("a", "b", "c"), "list")
        );

        Assertions.assertEquals(
                text("hugo"),
                map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:name")
        );
        Assertions.assertEquals(
                text(24),
                map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:age")
        );
        Assertions.assertEquals(
                Component.text(new Person("hugo", 24).toString()),
                map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data")
        );
        Assertions.assertEquals(
                empty(),
                map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friends")
        );
        Assertions.assertNull(
                map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friend")
        );
        Assertions.assertNull(
                map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:names")
        );
    }

    record Person(String name, int age) {
    }

    record PersonRelations(Person data, List<Person> friends) {
    }
}