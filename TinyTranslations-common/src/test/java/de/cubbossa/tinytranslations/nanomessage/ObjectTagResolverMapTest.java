package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.AbstractTest;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.TinyTranslations;
import de.cubbossa.tinytranslations.tinyobject.TinyDefault;
import de.cubbossa.tinytranslations.tinyobject.TinyObject;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import de.cubbossa.tinytranslations.tinyobject.TinyProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

class ObjectTagResolverMapTest extends AbstractTest {

    @Test
    void resolve() {

        List<TinyObjectMapping> map = new ArrayList<>();
        map.add(TinyObjectMapping.builder(Person.class)
                .withFallbackConversion(Person::name)
                .with("name", Person::name)
                .with("age", Person::age)
                .build());
        map.add(TinyObjectMapping.builder(Employee.class)
                .with("salary", Employee::salary)
                .build());
        map.add(TinyObjectMapping.builder(PersonRelations.class)
                .with("data", PersonRelations::data)
                .with("friends", PersonRelations::friends)
                .build());
        map.add(TinyObjectMapping.builder(List.class)
                .withFallbackConversion(l -> text((String) l.stream().map(Object::toString).collect(Collectors.joining(", "))))
                .build());
//
//        assertEquals(
//                Component.text("hugo"),
//                MiniMessage.miniMessage().deserialize("<hugo>", ObjectNotationTag.resolver(new Person("hugo", 24), "hugo", map))
//        );
//        assertEquals(
//                text("a, b, c"),
//                MiniMessage.miniMessage().deserialize("<mylist>", ObjectTag.resolver("mylist", List.of("a", "b", "c"), map))
//        );
//
//        assertEquals(
//                text("hugo"),
//                NanoMessage.nanoMessage().deserialize("{hugo.name}", ObjectTag.resolver("hugo", new Person("hugo", 24), map))
//        );
//        assertEquals(
//                text(24),
//                NanoMessage.nanoMessage().deserialize("{hugo.age}", ObjectTag.resolver("hugo", new Person("hugo", 24), map))
//        );
//        assertEquals(
//                Component.text(new Person("hugo", 24).toString()),
//                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data")
//        );
//        assertEquals(
//                empty(),
//                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friends")
//        );
//        assertEquals(
//                text("100"),
//                map.resolveObject(new Employee("hugo", 24, 100), "salary")
//        );
//        assertEquals(
//                text("hugo"),
//                map.resolveObject(new Employee("hugo", 24, 100), "name")
//        );
//        assertNull(
//                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "friend")
//        );
//        assertNull(
//                map.resolveObject(new PersonRelations(new Person("hugo", 24), Collections.emptyList()), "data.names")
//        );
    }

    @Test
    void testAnnotations(@TempDir File file) {
        MessageTranslator global = TinyTranslations.globalTranslator(file);
        MessageTranslator plugin = TinyTranslations.application("plugin2");

        plugin.add(TinyObjectMapping.builder(Person.class)
                .with("name", Person::name)
                .with("age", Person::age)
                .build());

        assertRenderEquals(
                text(100),
                plugin.messageBuilder("test1")
                        .withDefault("{test.salary}")
                        .build()
                        .insertObject("test", new Employer("hugo", 10, 100))
        );
        assertRenderEquals(
                text("dog"),
                plugin.messageBuilder("test2")
                        .withDefault("{test.name}")
                        .build()
                        .insertObject("test", new Animal("dog", "canis lupus familiaris"))
        );
        assertRenderEquals(
                text("dog"),
                plugin.messageBuilder("test3")
                        .withDefault("{test}")
                        .build()
                        .insertObject("test", new Animal("dog", "canis lupus familiaris"))
        );
        assertRenderEquals(
                text("canis lupus familiaris"),
                plugin.messageBuilder("name")
                        .withDefault("{test.latin_name}")
                        .build()
                        .insertObject("test", new Animal("dog", "canis lupus familiaris"))
        );

        plugin.close();
    }

    @Test
    void testOverride(@TempDir File file) {
        MessageTranslator global = TinyTranslations.globalTranslator(file);
        MessageTranslator plugin = TinyTranslations.application("plugin3");

        plugin.add(TinyObjectMapping.builder(List.class)
                .withFallbackConversion(l -> "a")
                .build());
        plugin.add(TinyObjectMapping.builder(ArrayList.class)
                .withFallbackConversion(l -> "b")
                .build());

        assertRenderEquals(
                text("b"),
                plugin.messageBuilder("test123").withDefault("{b}").build()
                                .insertObject("b", new ArrayList<>())
        );

        plugin.add(TinyObjectMapping.builder(ArrayList.class).withFallbackConversion(l -> "b").build());
        plugin.add(TinyObjectMapping.builder(List.class).withFallbackConversion(l -> "a").build());
        plugin.add(TinyObjectMapping.builder(Sel.class).withFallbackConversion(l -> "c").build());
        assertRenderEquals(
                text("c"),
                plugin.messageBuilder("test234").withDefault("{c}").build()
                        .insertObject("c", new Sel())
        );

        plugin.add(TinyObjectMapping.builder(List.class).withFallbackConversion(l -> "a").build());
        assertRenderEquals(
                text("a"),
                plugin.messageBuilder("test234").withDefault("{c}").build()
                        .insertObject("c", new Sel())
        );

        plugin.close();
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