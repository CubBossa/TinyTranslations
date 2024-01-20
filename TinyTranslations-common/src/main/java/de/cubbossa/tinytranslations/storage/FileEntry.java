package de.cubbossa.tinytranslations.storage;

import java.util.List;

public interface FileEntry {

	String getString();

	String getValue();

	List<String> getComments();
}
