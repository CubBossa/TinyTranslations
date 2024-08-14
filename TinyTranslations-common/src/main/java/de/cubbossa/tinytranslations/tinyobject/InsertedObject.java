package de.cubbossa.tinytranslations.tinyobject;

import java.util.Collection;

public record InsertedObject(String descriptor, Object object, Collection<TinyObjectMapping> resolvers) {
}
