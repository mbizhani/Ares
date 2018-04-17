package org.devocative.ares.entity.command;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EViewMode {
	NORMAL(1, "Normal"),
	HIDDEN(2, "Hidden"),
	LIST(3, "List");

	// ------------------------------

	private Integer id;
	private String name;

	// ------------------------------

	EViewMode(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	// ------------------------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<EViewMode> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EViewMode, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EViewMode attribute) {
			return attribute != null ? attribute.getId() : null;
		}

		@Override
		public EViewMode convertToEntityAttribute(Integer dbData) {
			for (EViewMode literal : values()) {
				if (literal.getId().equals(dbData)) {
					return literal;
				}
			}

			return null;
		}
	}
}
