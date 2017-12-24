package org.devocative.ares.service.command.dsl;

import groovy.lang.Closure;
import org.devocative.adroit.CalendarUtil;
import org.devocative.ares.cmd.CommandCenter;
import org.devocative.ares.cmd.CommandException;
import org.devocative.ares.cmd.SshResult;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.demeter.entity.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MainCommandDSL {
	private static final Logger logger = LoggerFactory.getLogger(MainCommandDSL.class);

	private static final List<String> VALID_SSH_PROPERTIES = Arrays.asList(
		"prompt", "cmd", "stdin",
		"result", "force", "error");
	private static final List<String> VALID_DB_PROPERTIES = Arrays.asList(
		"prompt", "query", "params", "filters",
		"result", "force", "error");

	private CommandCenter commandCenter;

	// ------------------------------

	public MainCommandDSL(CommandCenter commandCenter) {
		this.commandCenter = commandCenter;
	}

	// ------------------------------

	public Object ssh(Closure closure) {
		MapOfClosureDelegate delegate = new MapOfClosureDelegate();
		Closure rehydrate = closure.rehydrate(delegate, commandCenter.getParams(), null);
		rehydrate.setResolveStrategy(Closure.DELEGATE_FIRST);
		rehydrate.call();
		Map<String, Object> clsAsMap = delegate.getClosureAsMap();

		for (String key : clsAsMap.keySet()) {
			if (!VALID_SSH_PROPERTIES.contains(key)) {
				throw new CommandException("Invalid property for ssh{}: " + key);
			}
		}

		String prompt = clsAsMap.containsKey("prompt") ? clsAsMap.get("prompt").toString() : null;
		String cmd = clsAsMap.containsKey("cmd") ? clsAsMap.get("cmd").toString() : null;
		Boolean force = clsAsMap.containsKey("force") ? (Boolean) clsAsMap.get("force") : clsAsMap.containsKey("error");
		String stdin = clsAsMap.containsKey("stdin") ? clsAsMap.get("stdin").toString() : null;
		SshResult sshResult = commandCenter.ssh(prompt, cmd, force, stdin);
		if (sshResult.getExitStatus() == 0 && clsAsMap.containsKey("result")) {
			Closure result = (Closure) clsAsMap.get("result");
			return result.call(sshResult);
		} else {
			if (sshResult.getExitStatus() != 0 && clsAsMap.containsKey("error")) {
				Closure errorHandler = (Closure) clsAsMap.get("error");
				return errorHandler
					.rehydrate(new OtherCommandsWrapper(this), commandCenter.getParams(), null)
					.call(sshResult);
			}
			return "";
		}
	}

	public Object db(Closure closure) {
		MapOfClosureDelegate delegate = new MapOfClosureDelegate();
		Closure rehydrate = closure.rehydrate(delegate, commandCenter.getParams(), null);
		rehydrate.setResolveStrategy(Closure.DELEGATE_FIRST);
		rehydrate.call();
		Map<String, Object> clsAsMap = delegate.getClosureAsMap();

		for (String key : clsAsMap.keySet()) {
			if (!VALID_DB_PROPERTIES.contains(key)) {
				throw new CommandException("Invalid property for db{}: " + key);
			}
		}

		String prompt = clsAsMap.containsKey("prompt") ? clsAsMap.get("prompt").toString() : null;
		String query = clsAsMap.containsKey("query") ? clsAsMap.get("query").toString() : null;
		Map<String, Object> params = clsAsMap.containsKey("params") ? (Map<String, Object>) clsAsMap.get("params") : null;
		Map<String, Object> filters = clsAsMap.containsKey("filters") ? (Map<String, Object>) clsAsMap.get("filters") : null;
		Boolean force = clsAsMap.containsKey("force") ? (Boolean) clsAsMap.get("force") : null;
		Object queryResult = commandCenter.sql(prompt, query, params, filters, force);
		if (clsAsMap.containsKey("result")) {
			Closure result = (Closure) clsAsMap.get("result");
			return result.call(queryResult);
		} else {
			return queryResult;
		}
	}

	// ---------------

	public void $reTarget(OServiceInstanceTargetVO newTargetVO, Closure closure) {
		commandCenter.reTarget(newTargetVO);
		closure
			.rehydrate(new OtherCommandsWrapper(this), commandCenter.getParams(), null)
			.call();
		commandCenter.resetTarget();
	}

	public Map<CharSequence, Object> $input(CharSequence... params) {
		Map<String, Object> inParams = commandCenter.getParams();

		Map<CharSequence, Object> result = new HashMap<>();
		for (CharSequence param : params) {
			String[] split = param.toString().split("[>]");
			String input = split[0].trim();
			if (inParams.get(input) != null) {
				if (split.length == 1) {
					result.put(input, inParams.get(input));
				} else {
					result.put(split[1].trim(), inParams.get(input));
				}
			}
		}
		return result;
	}

	public Object $param(CharSequence param) {
		return commandCenter.getParam(param.toString());
	}

	public void $scpTo(FileStore fileStore, String destDir) {
		commandCenter.scpTo(fileStore, destDir);
	}

	public void $userPasswordUpdated(CharSequence username, CharSequence password) {
		commandCenter.userPasswordUpdated(username.toString(), password.toString());
	}

	public void $checkVMServers(List<Map<String, String>> servers) {
		commandCenter.checkVMServers(servers);
	}

	public void $error(CharSequence message) {
		commandCenter.error(message.toString());
	}

	public String $now() {
		return $now("yyyyMMdd_HHmmss");
	}

	public String $now(CharSequence format) {
		return CalendarUtil.formatDate(new Date(), format.toString());
	}

	public void $log(CharSequence log) {
		logger.debug(log.toString());
	}

	public void $sleep(long seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	// other

	public CommandCenter getCommandCenter() {
		return commandCenter;
	}
}
