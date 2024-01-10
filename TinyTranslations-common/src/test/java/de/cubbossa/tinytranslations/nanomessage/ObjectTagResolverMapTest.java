package de.cubbossa.tinytranslations.nanomessage;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ObjectTagResolverMapTest {

	record Person(String name, int age) {}

	record PersonRelations(Person data, List<Person> friends) {}

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
		map.put(List.class, Collections.emptyMap(), l -> Component.text((String) l.stream()
				.map(Object::toString).collect(Collectors.joining(", "))));

		Assertions.assertEquals(
				Component.text("a, b, c"),
				map.resolve(List.of("a", "b", "c"), "list")
		);

		Assertions.assertEquals(
				"hugo",
				map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:name")
		);
		Assertions.assertEquals(
				24,
				map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:age")
		);
		Assertions.assertEquals(
				new Person("hugo", 24),
				map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data")
		);
		Assertions.assertEquals(
				Component.empty(),
				map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friends")
		);
		Assertions.assertNull(
				map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friend")
		);
		Assertions.assertNull(
				map.resolve(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:names")
		);
	}
}