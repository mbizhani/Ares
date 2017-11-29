package org.devocative.ares.cmd;

import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.demeter.entity.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandCenter {
	private static final Logger logger = LoggerFactory.getLogger(CommandCenter.class);

	private final CommandCenterResource resource;
	private OServiceInstanceTargetVO targetVO, origTargetVO;
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
				.callCommand(new CommandQVO(commandName, targetVO.getServiceInstance().getId(), params), resource);
			logger.info("CommandCenter.exec: commandName=[{}}", commandName);
			return result;
		} catch (Exception e) {
			logger.error("CommandCenter.exec: " + commandName, e);
			setException(e);
		}

		return null; //TODO
	}

	// ---------------

	public SshResult ssh(String prompt, String cmd, Boolean force, String... stdin) {
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

			ShellCommandExecutor executor = new ShellCommandExecutor(finalTargetVO, resource, prompt, cmd, stdin);
			if (force != null) {
				executor.setForce(force);
			}

			resource.getCommandService().assertCurrentUser(cmd);

			Thread th = new Thread(
				Thread.currentThread().getThreadGroup(),
				executor,
				Thread.currentThread().getName() + "-ShellCommandExecutor");
			th.start();
			th.join();

			resource.getCommandService().assertCurrentUser(cmd);

			if (executor.hasException()) {
				throw executor.getException();
			}

			exitStatus = executor.getExitStatus();
			result = executor.getResult().toString().trim();

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
			Thread th = new Thread(
				Thread.currentThread().getThreadGroup(),
				executor,
				Thread.currentThread().getName() + "-ScpToExecutor");
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

	public Object sql(String prompt, String sql, Map<String, Object> params, Map<String, Object> filter, Boolean force) {
		Object result = null;

		OServiceInstanceTargetVO finalTargetVO = targetVO;
		if (!ERemoteMode.JDBC.equals(finalTargetVO.getUser().getRemoteMode())) {
			finalTargetVO = resource.getServiceInstanceService().getTargetVOByServer(finalTargetVO.getId(), ERemoteMode.JDBC);
		}

		try {
			SqlCommandExecutor executor = new SqlCommandExecutor(finalTargetVO, resource, prompt, sql, params, filter);
			if (force != null) {
				executor.setForce(force);
			}

			Thread th = new Thread(
				Thread.currentThread().getThreadGroup(),
				executor,
				Thread.currentThread().getName() + "-SqlCommandExecutor");
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

	public void checkVMServers(List<Map<String, String>> servers) {
		checkVMServers(targetVO.getServerId(), servers);
	}

	public void checkVMServers(Long hypervisorId, List<Map<String, String>> servers) {
		logger.info("CommandCenter: checkServers hypervisorId=[{}] servers={}", hypervisorId, servers);
		resource.getServerService().checkVMServers(hypervisorId, servers);
	}

	public void updateServer(Long id, String vmId) {
		resource.getServerService().updateVmid(id, vmId);
	}

	public void updateServer(Long hypervisorId, String oldVmId, String newVmId, String newName) {
		resource.getServerService().updateServer(hypervisorId, oldVmId, newVmId, newName);
	}

	public void error(String message) {
		throw new CommandException(message);
	}

	public void reTarget(OServiceInstanceTargetVO newTargetVO) {
		logger.info("ReTarget: from=[{}] to=[{}]", targetVO, newTargetVO);

		origTargetVO = targetVO;
		targetVO = newTargetVO;
	}

	public void resetTarget() {
		logger.info("ResetTarget: from=[{}] to=[{}]", targetVO, origTargetVO);
		targetVO = origTargetVO;
		origTargetVO = null;
	}

	// ---------------

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
		throw new RuntimeException(exception);
	}

	public Map<String, Object> getParams() {
		Map<String, Object> result = new HashMap<>();
		result.putAll(params);
		result.put("target", targetVO);
		return result;
	}

	public Object getParam(String param) {
		return params.get(param);
	}

	public boolean hasParam(String param) {
		return params.containsKey(param);
	}
}