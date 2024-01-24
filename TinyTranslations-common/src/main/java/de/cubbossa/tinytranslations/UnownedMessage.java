package de.cubbossa.tinytranslations;

public interface UnownedMessage extends Message {

    boolean isOwned();

    Message setOwner(MessageTranslator translator);

    Message unwrap();
}
