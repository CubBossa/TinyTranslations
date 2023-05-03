package de.cubbossa.translations;

public class LanguageFileException extends RuntimeException {

	public LanguageFileException(String message) {
		super(message);
	}

	public LanguageFileException(Exception e) {
		super(e);
	}
}
