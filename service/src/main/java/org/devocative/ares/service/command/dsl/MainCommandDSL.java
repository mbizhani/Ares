package org.devocative.ares.service.command.dsl;

import groovy.lang.Closure;
import org.devocative.adroit.CalendarUtil;
import org.devocative.ares.cmd.CommandCenter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainCommandDSL {
	private CommandCenter commandCenter;

	public MainCommandDSL(CommandCenter commandCenter) {
		this.commandCenter = commandCenter;
	}

	// DSL

	public Map<CharSequence, Object> inputs(CharSequence... params) {
		Map<String, Object> inParams = commandCenter.getParams();

		Map<CharSequence, Object> result = new HashMap<>();
		for (CharSequence param : params) {
			if (inParams.containsKey(param.toString())) {
				result.put(param, inParams.get(param.toString()));
			}
		}
		return result;
	}

	public Object ssh(Closure closure) {
		MapOfClosureDelegate delegate = new MapOfClosureDelegate();
		Closure rehydrate = closure.rehydrate(delegate, commandCenter.getParams(), null);
		rehydrate.setResolveStrategy(Closure.DELEGATE_FIRST);
		rehydrate.call();
		Map<String, Object> clsAsMap = delegate.getClosureAsMap();

		String prompt = clsAsMap.containsKey("prompt") ? clsAsMap.get("prompt").toString() : null;
		String cmd = clsAsMap.containsKey("cmd") ? clsAsMap.get("cmd").toString() : null;
		Boolean force = clsAsMap.containsKey("force") ? (Boolean) clsAsMap.get("force") : null;
		String[] stdin = clsAsMap.containsKey("stdin") ? (String[]) clsAsMap.get("stdin") : null;
		return commandCenter.ssh(prompt, cmd, force, stdin);
	}

	public Object db(Closure closure) {
		MapOfClosureDelegate delegate = new MapOfClosureDelegate();
		Closure rehydrate = closure.rehydrate(delegate, commandCenter.getParams(), null);
		rehydrate.setResolveStrategy(Closure.DELEGATE_FIRST);
		rehydrate.call();
		Map<String, Object> clsAsMap = delegate.getClosureAsMap();

		String prompt = clsAsMap.containsKey("prompt") ? clsAsMap.get("prompt").toString() : null;
		String query = clsAsMap.containsKey("query") ? clsAsMap.get("query").toString() : null;
		Map<String, Object> params = clsAsMap.containsKey("params") ? (Map<String, Object>) clsAsMap.get("params") : null;
		Map<String, Object> filters = clsAsMap.containsKey("filters") ? (Map<String, Object>) clsAsMap.get("filters") : null;
		Object queryResult = commandCenter.sql(prompt, query, params, filters);
		if (clsAsMap.containsKey("result")) {
			Closure result = (Closure) clsAsMap.get("result");
			return result.call(queryResult);
		} else {
			return queryResult;
		}
	}

	public void userPasswordUpdated(CharSequence username, CharSequence password) {
		commandCenter.userPasswordUpdated(username.toString(), password.toString());
	}

	public void error(CharSequence message) {
		commandCenter.error(message.toString());
	}

	public String now() {
		return now("yyyyMMdd_HHmmss");
	}

	public String now(String format) {
		return CalendarUtil.formatDate(new Date(), format);
	}

	// other

	public CommandCenter getCommandCenter() {
		return commandCenter;
	}
}
