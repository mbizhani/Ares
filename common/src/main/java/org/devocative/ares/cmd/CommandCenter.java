package org.devocative.ares.cmd;

import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.demeter.entity.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CommandCenter {
	private static final Logger logger = LoggerFactory.getLogger(CommandCenter.class);

	private final OServiceInstanceTargetVO targetVO;
	private final CommandCenterResource resource;
	private Map<String, Object> params;
	private Exception exception;

	// ------------------------------

	public CommandCenter(OServiceInstanceTargetVO targetVO, CommandCenterResource resource, Map<String, Object> params) {
		this.targetVO = targetVO;
		this.resource = resource;
		this.params = params;
	}

	// ------------------------------

	public Object exec(String commandName) {
		return exec(commandName, params);
	}

	public Object exec(String commandName, Map<String, Object> params) {
		try {
			Object result = resource
				.getCommandService()
				.callCommand(new CommandQVO(commandName, targetVO.getServiceInstance(), params), resource);
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
				.getServiceInstanceService()
				.getTargetVOByServer(finalTargetVO.getId(), ERemoteMode.SSH);
		}

		try {
			resource.getCommandService().assertCurrentUser(cmd);

			ShellCommandExecutor executor = new ShellCommandExecutor(finalTargetVO, resource, prompt, cmd, stdin, force);

			resource.getCommandService().assertCurrentUser(cmd);

			Thread th = new Thread(Thread.currentThread().getThreadGroup(), executor);
			th.start();
			th.join();

			resource.getCommandService().assertCurrentUser(cmd);

			if (executor.hasException()) {
				throw executor.getException();
			}

			exitStatus = executor.getExitStatus();
			result = executor.getResult().toString();

			logger.info("Executed SSH Command: exitStatus=[{}] cmd=[{}] si=[{}]", exitStatus, cmd, finalTargetVO);

			// NOTE: calling resource.onResult() in this method causes ThreadLocal variables (e.g. current user) to become null!

			resource.getCommandService().assertCurrentUser(cmd);
		} catch (Exception e) {
			logger.error("CommandCenter.ssh", e);
			setException(e);
		}

		return new SshResult(result, exitStatus);
	}

	// ---------------

	public void scpTo(FileStore fileStore, String destDir) {
		OServiceInstanceTargetVO finalTargetVO = targetVO;
		if (!ERemoteMode.SSH.equals(finalTargetVO.getUser().getRemoteMode())) {
			finalTargetVO = resource
				.getServiceInstanceService()
				.getTargetVOByServer(finalTargetVO.getId(), ERemoteMode.SSH);
		}

		try {
			ScpToExecutor executor = new ScpToExecutor(finalTargetVO, resource, fileStore, destDir);
			Thread th = new Thread(Thread.currentThread().getThreadGroup(), executor);
			th.start();
			th.join();

			if (executor.hasException()) {
				throw executor.getException();
			}

		} catch (Exception e) {
			logger.error("CommandCenter.scpTo", e);
			setException(e);
		}
	}
	// ---------------

	public Object sql(String prompt, String sql) {
		Object result = null;

		OServiceInstanceTargetVO finalTargetVO = targetVO;
		if (!ERemoteMode.JDBC.equals(finalTargetVO.getUser().getRemoteMode())) {
			finalTargetVO = resource.getServiceInstanceService().getTargetVOByServer(finalTargetVO.getId(), ERemoteMode.JDBC);
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

	public void userPasswordUpdated(String username, String password) {
		logger.info("CommandCenter.userPasswordUpdated: target=[{}] username=[{}]", targetVO, username);
		resource.getCommandService().userPasswordUpdated(targetVO, username, password);
	}

	public void checkVMServers(Long hypervisorId, List<Map<String, String>> servers) {
		logger.info("CommandCenter: checkServers hypervisorId=[{}] servers={}", hypervisorId, servers);
		resource.getServerService().checkVMServers(hypervisorId, servers);
	}

	public void updateServer(Long id, String vmId) {
		resource.getServerService().updateVmid(id, vmId);
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

	// ---------------

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
		throw new RuntimeException(exception);
	}
}