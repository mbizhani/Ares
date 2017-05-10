package org.devocative.ares.cmd;

import org.devocative.adroit.CalendarUtil;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class CommandCenter {
	private static final Logger logger = LoggerFactory.getLogger(CommandCenter.class);

	private final OServiceInstanceTargetVO targetVO;
	private final CommandCenterResource resource;
	private Exception exception;

	// ------------------------------

	public CommandCenter(OServiceInstanceTargetVO targetVO, CommandCenterResource resource) {
		this.targetVO = targetVO;
		this.resource = resource;
	}

	// ------------------------------

	public Object exec(String commandName, Map<String, String> params) {
		try {
			Object result = resource
				.getCommandService()
				.callCommand(commandName, targetVO.getServiceInstance(), params, resource);
			logger.info("CommandCenter.exec: commandName=[{}}", commandName);
			return result;
		} catch (Exception e) {
			logger.error("CommandCenter.exec: " + commandName, e);
			setException(e);
		}

		return null; //TODO
	}

	// ---------------

	public SshResult ssh(String prompt, String cmd) {
		return ssh(prompt, cmd, false, (String) null);
	}

	public SshResult ssh(String prompt, String cmd, boolean force) {
		return ssh(prompt, cmd, force, (String) null);
	}

	public SshResult ssh(String prompt, String cmd, String... stdin) {
		return ssh(prompt, cmd, false, stdin);
	}

	// Main ssh()
	public SshResult ssh(String prompt, String cmd, boolean force, String... stdin) {
		int exitStatus = -1;
		String result = null;

		OServiceInstanceTargetVO finalTargetVO = targetVO;
		if (!ERemoteMode.SSH.equals(finalTargetVO.getUser().getRemoteMode())) {
			finalTargetVO = resource
				.getCommandService()
				.findOf(finalTargetVO.getId(), ERemoteMode.SSH);
		}

		try {
			ShellCommandExecutor executor = new ShellCommandExecutor(finalTargetVO, resource, prompt, cmd, stdin);

			Thread th = new Thread(executor);
			th.start();
			th.join();

			if (executor.hasException()) {
				throw executor.getException();
			}

			exitStatus = executor.getExitStatus();
			result = executor.getResult().toString();

			logger.info("Executed SSH Command: exitStatus=[{}] cmd=[{}] si=[{}]", exitStatus, cmd, finalTargetVO);

			if (exitStatus != 0) {
				if (force) {
					resource.onResult(new CommandOutput(CommandOutput.Type.LINE, "WARNING: exitStatus: " + exitStatus));
				} else {
					throw new RuntimeException("Invalid ssh command exitStatus: " + exitStatus);
				}
			}
		} catch (Exception e) {
			logger.error("CommandCenter.ssh", e);
			setException(e);
		}

		return new SshResult(result, exitStatus);
	}

	// ---------------

	public Object sql(String prompt, String sql) {
		Object result = null;

		OServiceInstanceTargetVO finalTargetVO = targetVO;
		if (!ERemoteMode.JDBC.equals(finalTargetVO.getUser().getRemoteMode())) {
			finalTargetVO = resource.getCommandService().findOf(finalTargetVO.getId(), ERemoteMode.JDBC);
		}

		try {
			SqlCommandExecutor executor = new SqlCommandExecutor(finalTargetVO, resource, prompt, sql);
			Thread th = new Thread(executor);
			th.start();
			th.join();

			if (executor.hasException()) {
				throw executor.getException();
			}

			result = executor.getResult();
		} catch (Exception e) {
			logger.error("CommandCenter.sql", e);
			setException(e);
		}

		return result;
	}

	// ---------------

	public String now() {
		return now("yyyyMMdd_HHmmss");
	}

	public String now(String format) {
		return CalendarUtil.formatDate(new Date(), format);
	}

	public void userPasswordUpdated(String username, String password) {
		logger.info("CommandCenter.userPasswordUpdated: target=[{}] username=[{}]", targetVO, username);
		resource.getCommandService().userPasswordUpdated(targetVO, username, password);
	}

	public void error(String message) {
		//resultCallBack.onResult(new CommandOutput(CommandOutput.Type.ERROR, "Error: " + message));
		throw new RuntimeException(message);
	}

	public void warn(String message) {
		resource.onResult(new CommandOutput(CommandOutput.Type.LINE, "Warn: " + message));
	}

	public void info(String message) {
		resource.onResult(new CommandOutput(CommandOutput.Type.LINE, "Info: " + message));
	}

	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void log(String log) {
		logger.debug(log);
	}

	// ------------------------------

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
		throw new RuntimeException(exception);
	}
}
