package de.cubbossa.tinytranslations.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListSection {

	private int offset;
	private int range;

	private ListSection(int offset, int range) {
		this.offset = offset;
		this.range = range;
	}

	public static ListSection range(int offset, int range) {
		return new ListSection(offset, range);
	}

	public static ListSection paged(int page, int size) {
		return new ListSection(page * size, size);
	}

	public <E> List<E> apply(List<E> list) {
		int max = list.size();
		return list.subList(Integer.max(0, Integer.min(offset, max)), Integer.max(0, Integer.min(offset + range, max)));
	}

	public int getPage() {
		return offset / range;
	}

	public int getMaxPages(int size) {
		return (int) Math.ceil(size / (double) range);
	}
}
