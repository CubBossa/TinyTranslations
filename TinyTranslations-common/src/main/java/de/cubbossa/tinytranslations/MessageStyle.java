package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.tag.NanoResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

public interface MessageStyle {

    String getKey();

    NanoResolver getResolver();

    @Nullable String getStringBackup();
}
