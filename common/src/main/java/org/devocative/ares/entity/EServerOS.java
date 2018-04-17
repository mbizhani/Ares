package org.devocative.ares.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EServerOS {
	LINUX(1, "Linux"),
	ESXi(2, "ESXi"),
	WINDOWS(3, "Windows"),
	ORACLE_VM(4, "Oracle VM");

	// ------------------------------

	private Integer id;
	private String name;

	// ------------------------------

	EServerOS(Integer id, String name) {
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

	public static List<EServerOS> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EServerOS, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EServerOS attribute) {
			return attribute != null ? attribute.getId() : null;
		}

		@Override
		public EServerOS convertToEntityAttribute(Integer dbData) {
			for (EServerOS literal : values()) {
				if (literal.getId().equals(dbData)) {
					return literal;
				}
			}
			return null;
		}
	}
}
