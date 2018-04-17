package org.devocative.ares.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EBasicDiscriminator {
	FUNCTION(1, "Function"),
	ENVIRONMENT(2, "Environment"),
	LOCATION(3, "Location"),
	COMPANY(4, "Company");

	// ------------------------------

	private Integer id;
	private String name;

	// ------------------------------

	EBasicDiscriminator(Integer id, String name) {
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

	public static List<EBasicDiscriminator> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EBasicDiscriminator, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EBasicDiscriminator attribute) {
			return attribute != null ? attribute.getId() : null;
		}

		@Override
		public EBasicDiscriminator convertToEntityAttribute(Integer dbData) {
			for (EBasicDiscriminator literal : values()) {
				if (literal.getId().equals(dbData)) {
					return literal;
				}
			}
			return null;
		}
	}
}
