package de.cubbossa.tinytranslations.util;

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
		List<Entry> entries = PropertiesUtils.loadProperties(reader);

		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(
				new Entry("test", "a", Collections.emptyList()),
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
		List<Entry> entries = PropertiesUtils.loadProperties(reader);

		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(
				new Entry("test", "a", List.of("", "# comment 1", "", "!comment2 : b")),
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
		List<Entry> entries = PropertiesUtils.loadProperties(reader);

		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(
				new Entry("test", "a\nb\nc", Collections.emptyList()),
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
		List<Entry> entries = PropertiesUtils.loadProperties(reader);

		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(
				new Entry("test", "a\n#b\nc", List.of("#b")),
				entries.get(0)
		);
	}

	@Test
	@SneakyThrows
	void writeValue() {
		StringWriter writer = new StringWriter();

		PropertiesUtils.write(writer, List.of(new Entry("test", "a", Collections.emptyList())));
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

		PropertiesUtils.write(writer, List.of(new Entry("test", "a", List.of(
				"", "! test comment", "#", "", "!!####"
		))));
		Assertions.assertEquals(
				"""
												
						! test comment
						#
												
						!!####
						test = a
						""",
				writer.toString()
		);
	}

	@Test
	@SneakyThrows
	void writeMultiline() {
		StringWriter writer = new StringWriter();

		PropertiesUtils.write(writer, List.of(new Entry("test", "a\nb\nc", Collections.emptyList())));
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
				new Entry("test1", "a\nb\nc", Collections.emptyList()),
				new Entry("test2", "def", List.of("#other"))
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
