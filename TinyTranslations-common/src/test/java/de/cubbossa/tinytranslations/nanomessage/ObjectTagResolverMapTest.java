package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.tinyobject.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObjectTagResolverMapTest {

    @Test
    void resolve() {

        TinyObjectTagResolver map = new TinyObjectTagResolverImpl();
        map.add(TinyObjectResolver.builder(Person.class)
                .with("name", Person::name)
                .with("age", Person::age)
                .build());
        map.add(TinyObjectResolver.builder(Employee.class)
                .with("salary", Employee::salary)
                .build());
        map.add(TinyObjectResolver.builder(PersonRelations.class)
                .with("data", PersonRelations::data)
                .with("friends", PersonRelations::friends)
                .build());
        map.add(TinyObjectResolver.builder(List.class)
                .withFallback(l -> text((String) l.stream().map(Object::toString).collect(Collectors.joining(", "))))
                .build());

        assertEquals(
                text("a, b, c"),
                map.resolveObject(List.of("a", "b", "c"), "")
        );

        assertEquals(
                text("hugo"),
                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:name")
        );
        assertEquals(
                text(24),
                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:age")
        );
        assertEquals(
                Component.text(new Person("hugo", 24).toString()),
                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data")
        );
        assertEquals(
                empty(),
                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friends")
        );
        assertEquals(
                text("100"),
                map.resolveObject(new Employee("hugo", 24, 100), "salary")
        );
        assertEquals(
                text("hugo"),
                map.resolveObject(new Employee("hugo", 24, 100), "name")
        );
        assertNull(
                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friend")
        );
        assertNull(
                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data:names")
        );
    }

    @Test
    void testAnnotations() {

        TinyObjectTagResolver map = new TinyObjectTagResolverImpl();
        map.add(TinyObjectResolver.builder(Person.class)
                .with("name", Person::name)
                .with("age", Person::age)
                .build());

        assertEquals(
                text(100),
                map.resolveObject(new Employer("hugo", 10, 100), "salary")
        );
        assertEquals(
                text("dog"),
                map.resolveObject(new Animal("dog", "canis lupus familiaris"), "name")
        );
        assertEquals(
                text("dog"),
                map.resolveObject(new Animal("dog", "canis lupus familiaris"), "")
        );
        assertEquals(
                text("canis lupus familiaris"),
                map.resolveObject(new Animal("dog", "canis lupus familiaris"), "latin_name")
        );
    }

    @Test
    void testOverride() {

        TinyObjectTagResolver map = new TinyObjectTagResolverImpl();
        map.add(TinyObjectResolver.builder(List.class)
                .withFallback(l -> "a")
                .build());
        map.add(TinyObjectResolver.builder(ArrayList.class)
                .withFallback(l -> "b")
                .build());
        assertEquals(
                text("b"),
                map.resolveObject(new ArrayList<>(), "")
        );
        map = new TinyObjectTagResolverImpl();
        map.add(TinyObjectResolver.builder(ArrayList.class).withFallback(l -> "b").build());
        map.add(TinyObjectResolver.builder(List.class).withFallback(l -> "a").build());
        map.add(TinyObjectResolver.builder(Sel.class).withFallback(l -> "c").build());
        assertEquals(
                text("c"),
                map.resolveObject(new Sel(), "")
        );
    }

    static class Sel extends ArrayList<Integer> {}

    @Accessors(fluent = true)
    @Getter
    @AllArgsConstructor
    @ToString
    class Person {
        private String name;
        private int age;
    }

    @Accessors(fluent = true)
    @Getter
    class Employee extends Person {
        private int salary;

        public Employee(String name, int age, int salary) {
            super(name, age);
            this.salary = salary;
        }
    }

    @TinyObject
    class Employer extends Person {
        @TinyProperty
        private int salary;

        public Employer(String name, int age, int salary) {
            super(name, age);
            this.salary = salary;
        }
    }

    record PersonRelations(Person data, List<Person> friends) {
    }

    @TinyObject
    @AllArgsConstructor
    static class Animal {
        @TinyDefault
        @TinyProperty(name = "name")
        private String englishName;
        @TinyProperty(name = "latin_name")
        private String nomenclature;
    }
}