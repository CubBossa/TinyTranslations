package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

public interface MessageStyle {

    String getKey();

    TagResolver getResolver();

    @Nullable String getStringBackup();
}
