package de.cubbossa.translations.persistent;

import java.util.List;

public interface FileEntry {

	String getString();

	String getValue();

	List<String> getComments();
}
