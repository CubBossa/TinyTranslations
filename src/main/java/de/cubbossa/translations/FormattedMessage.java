package de.cubbossa.translations;

import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Arrays;
import java.util.stream.Stream;

public class FormattedMessage extends Message {

	@Getter
	private final TagResolver[] resolvers;

	public FormattedMessage(String key, TagResolver... resolvers) {
		super(key);
		this.resolvers = resolvers;
	}

	@Override
	public FormattedMessage format(TagResolver... resolvers) {
		return new FormattedMessage(getKey(), Stream.concat(Arrays.stream(this.resolvers), Arrays.stream(resolvers)).toArray(TagResolver[]::new));
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
