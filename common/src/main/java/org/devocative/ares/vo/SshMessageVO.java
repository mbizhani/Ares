package org.devocative.ares.vo;

public class SshMessageVO {
	private String text;
	private Integer specialKey;

	public SshMessageVO(String text, Integer specialKey) {
		this.text = text;
		this.specialKey = specialKey;
	}

	public String getText() {
		return text;
	}

	public Integer getSpecialKey() {
		return specialKey;
	}
}
