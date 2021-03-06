package org.devocative.ares.service.command.dsl;

import groovy.lang.Closure;
import org.devocative.adroit.date.UniDate;
import org.devocative.ares.cmd.CommandCenter;
import org.devocative.ares.cmd.CommandException;
import org.devocative.ares.cmd.SshResult;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.demeter.entity.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// #TIP
public class MainCommandDSL {
	private static final Logger logger = LoggerFactory.getLogger(MainCommandDSL.class);

	private static final List<String> VALID_SSH_PROPERTIES = Arrays.asList(
		"prompt", "cmd", "stdin",
		"result", "force", "error", "admin");
	private static final List<String> VALID_DB_PROPERTIES = Arrays.asList(
		"prompt", "query", "params", "filters",
		"result", "force", "error", "admin");

	// ------------------------------

	public Object ssh(Closure closure) {
		CommandCenter commandCenter = CommandCenter.get();

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
		Boolean admin = clsAsMap.containsKey("admin") ? (Boolean) clsAsMap.get("admin") : null;

		SshResult sshResult = commandCenter.ssh(prompt, cmd, force, admin, stdin);
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
		CommandCenter commandCenter = CommandCenter.get();

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
		Boolean admin = clsAsMap.containsKey("admin") ? (Boolean) clsAsMap.get("admin") : null;

		try {
			Object queryResult = commandCenter.sql(prompt, query, params, filters, force, admin);
			if (clsAsMap.containsKey("result")) {
				Closure result = (Closure) clsAsMap.get("result");
				return result.call(queryResult);
			} else {
				return queryResult;
			}
		} catch (RuntimeException e) {
			if (e.getCause() instanceof SQLException && clsAsMap.containsKey("error")) {
				$warn(e.getCause().getMessage());
				Closure errorHandler = (Closure) clsAsMap.get("error");
				return errorHandler
					.rehydrate(new OtherCommandsWrapper(this), commandCenter.getParams(), null)
					.call(e.getCause().getMessage());
			}

			throw e;
		}
	}

	// ---------------

	public void $reTarget(OServiceInstanceTargetVO newTargetVO, Closure closure) {
		CommandCenter commandCenter = CommandCenter.get();

		commandCenter.reTarget(newTargetVO);
		closure
			.rehydrate(new OtherCommandsWrapper(this), commandCenter.getParams(), null)
			.call();
		commandCenter.resetTarget();
	}

	public Map<CharSequence, Object> $input(CharSequence... params) {
		CommandCenter commandCenter = CommandCenter.get();

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
		CommandCenter commandCenter = CommandCenter.get();
		return commandCenter.getParam(param.toString());
	}

	public void $scpTo(FileStore fileStore, String destDir) {
		CommandCenter commandCenter = CommandCenter.get();
		commandCenter.scpTo(fileStore, destDir);
	}

	public void $userPasswordUpdated(CharSequence username, CharSequence password) {
		CommandCenter commandCenter = CommandCenter.get();
		commandCenter.userPasswordUpdated(username.toString(), password.toString());
	}

	public void $updateVMServers(CharSequence multiMatchAlg, List<Map<String, String>> servers, Boolean onlyNew) {
		CommandCenter commandCenter = CommandCenter.get();
		commandCenter.updateVMServers(multiMatchAlg.toString(), servers, onlyNew);
	}

	public List<OServer> $checkVMServer(CharSequence multiMatchAlg, CharSequence name, CharSequence vmId, CharSequence address) {
		return CommandCenter.get().checkVMServer(
			multiMatchAlg != null ? multiMatchAlg.toString() : null,
			name != null ? name.toString() : null,
			vmId != null ? vmId.toString() : null,
			address != null ? address.toString() : null
		);
	}

	public void $error(CharSequence message) {
		CommandCenter commandCenter = CommandCenter.get();
		commandCenter.error(message.toString());
	}

	public void $warn(CharSequence message) {
		CommandCenter commandCenter = CommandCenter.get();
		commandCenter.warn(message.toString());
	}

	public String $now() {
		return $now("yyyyMMdd_HHmmss");
	}

	public String $now(CharSequence format) {
		return UniDate.now().format(format.toString());
	}

	public void $log(CharSequence log) {
		logger.info(log.toString());
	}

	public void $sleep(Number millis) {
		try {
			Thread.sleep(millis.longValue());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	// other

	public CommandCenter getCommandCenter() {
		return CommandCenter.get();
	}
}
