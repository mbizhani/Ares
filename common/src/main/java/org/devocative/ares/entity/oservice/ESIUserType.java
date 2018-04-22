package org.devocative.ares.entity.oservice;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum ESIUserType {
	Normal(1, "Normal"),
	Executor(2, "Executor"),
	Admin(3, "Admin"),;

	// ------------------------------

	private Integer id;
	private String name;

	// ------------------------------

	ESIUserType(Integer id, String name) {
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

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ESIUserType> list() {
		return Arrays.asList(values());
	}

	public static List<ESIUserType> listOfExec() {
		return Arrays.asList(Executor, Admin);
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<ESIUserType, Integer> {
		@Override
		public Integer convertToDatabaseColumn(ESIUserType attribute) {
			return attribute != null ? attribute.getId() : null;
		}

		@Override
		public ESIUserType convertToEntityAttribute(Integer dbData) {
			for (ESIUserType literal : values()) {
				if (literal.getId().equals(dbData)) {
					return literal;
				}
			}

			return null;
		}
	}
}
