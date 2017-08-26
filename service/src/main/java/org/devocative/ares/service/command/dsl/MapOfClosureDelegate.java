package org.devocative.ares.service.command.dsl;

import groovy.util.Proxy;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapOfClosureDelegate extends Proxy {
	private Map<String, Object> closureAsMap = new LinkedHashMap<>();

	public Map<String, Object> getClosureAsMap() {
		return closureAsMap;
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		Object[] argsArr = (Object[]) args;
		if (argsArr.length == 1) {
			closureAsMap.put(name, argsArr[0]);
		} else {
			closureAsMap.put(name, argsArr);
		}
		return null;
	}
}
