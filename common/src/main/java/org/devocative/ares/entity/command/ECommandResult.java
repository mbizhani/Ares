package org.devocative.ares.entity.command;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum ECommandResult {
	UNKNOWN(0, "Unknown"),
	RUNNING(1, "Running"),
	SUCCESSFUL(2, "Successful"),
	ERROR(3, "Error");

	// ------------------------------

	private Integer id;
	private String name;

	// ------------------------------

	ECommandResult(Integer id, String name) {
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

	public static List<ECommandResult> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<ECommandResult, Integer> {
		@Override
		public Integer convertToDatabaseColumn(ECommandResult attribute) {
			return attribute != null ? attribute.getId() : null;
		}

		@Override
		public ECommandResult convertToEntityAttribute(Integer dbData) {
			for (ECommandResult literal : values()) {
				if (literal.getId().equals(dbData)) {
					return literal;
				}
			}
			return null;
		}
	}
}
