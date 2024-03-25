package de.cubbossa.tinytranslations.util;

import de.cubbossa.tinytranslations.storage.StorageEntry;
import de.cubbossa.tinytranslations.storage.properties.PropertiesUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

public class PropertiesUtilsTest {


    @SneakyThrows
    @Test
    void load() {
        StringReader reader = new StringReader("test : a");
        List<StorageEntry> entries = PropertiesUtils.loadProperties(reader);

        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals(
                new StorageEntry("test", "a", null),
                entries.get(0)
        );
    }

    @SneakyThrows
    @Test
    void loadComments() {
        StringReader reader = new StringReader("""
                				
                # comment 1
                				
                !comment2 : b
                test : a
                """);
        List<StorageEntry> entries = PropertiesUtils.loadProperties(reader);

        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals(
                new StorageEntry("test", "a", "\n comment 1\n\ncomment2 : b"),
                entries.get(0)
        );
    }

    @SneakyThrows
    @Test
    void loadMultiLine() {
        StringReader reader = new StringReader("""
                test : a\\
                       b\\
                       c""");
        List<StorageEntry> entries = PropertiesUtils.loadProperties(reader);

        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals(
                new StorageEntry("test", "a\nb\nc", null),
                entries.get(0)
        );
    }

    @SneakyThrows
    @Test
    void loadMultiLineCommented() {
        StringReader reader = new StringReader("""
                #b
                test : a\\
                #b\\
                c""");
        List<StorageEntry> entries = PropertiesUtils.loadProperties(reader);

        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals(
                new StorageEntry("test", "a\n#b\nc", "b"),
                entries.get(0)
        );
    }

    @Test
    @SneakyThrows
    void writeValue() {
        StringWriter writer = new StringWriter();

        PropertiesUtils.write(writer, List.of(new StorageEntry("test", "a", null)));
        Assertions.assertEquals(
                """
                        test = a
                        """,
                writer.toString()
        );
    }

    @Test
    @SneakyThrows
    void writeComments() {
        StringWriter writer = new StringWriter();

        PropertiesUtils.write(writer, List.of(new StorageEntry("test", "a", "\n test comment\n\n\n!####")));
        Assertions.assertEquals(
                """
                        						
                        # test comment
                        						
                        						
                        #!####
                        test = a
                        """,
                writer.toString()
        );
    }

    @Test
    @SneakyThrows
    void writeMultiline() {
        StringWriter writer = new StringWriter();

        PropertiesUtils.write(writer, List.of(new StorageEntry("test", "a\nb\nc", null)));
        Assertions.assertEquals(
                """
                        test = a\\
                               b\\
                               c
                               """,
                writer.toString()
        );
    }

    @Test
    @SneakyThrows
    void writeMulti() {
        StringWriter writer = new StringWriter();

        PropertiesUtils.write(writer, List.of(
                new StorageEntry("test1", "a\nb\nc", null),
                new StorageEntry("test2", "def", "other")
        ));
        Assertions.assertEquals(
                """
                        test1 = a\\
                                b\\
                                c
                        #other
                        test2 = def
                        """,
                writer.toString()
        );
    }
}
