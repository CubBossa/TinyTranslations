package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.tag.NanoResolver;

public interface StyleDeserializer {

    NanoResolver deserialize(String key, String string);
}
