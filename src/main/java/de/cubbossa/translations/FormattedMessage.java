package de.cubbossa.translations;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Arrays;

public class FormattedMessage extends Message {

	private final TagResolver[] resolvers;

	public FormattedMessage(String key, TagResolver... resolvers) {
		super(key);
		this.resolvers = resolvers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FormattedMessage)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		FormattedMessage that = (FormattedMessage) o;

		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals(resolvers, that.resolvers);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + Arrays.hashCode(resolvers);
		return result;
	}
}
