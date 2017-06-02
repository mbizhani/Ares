package org.devocative.ares.cmd;

import java.util.LinkedHashMap;
import java.util.Map;

public class AresMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 6780186862687557683L;

	@Override
	public AresMap<K, V> clone() {
		AresMap<K, V> cloned = new AresMap<>();
		for (Map.Entry<K, V> entry : this.entrySet()) {
			cloned.put(entry.getKey(), entry.getValue());
		}
		return cloned;
	}

	public String exportStr() {
		return exportStr("", " = ", "\"", "\n");
	}

	public String exportStr(String keyDec, String kvSep, String valDec, String eol) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<K, V> entry : this.entrySet()) {
			builder.append(keyDec).append(entry.getKey()).append(keyDec)
				.append(kvSep)
				.append(valDec).append(entry.getValue()).append(valDec)
				.append(eol);
		}
		return builder.toString().trim();
	}
}
