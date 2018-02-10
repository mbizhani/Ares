package org.devocative.ares.cmd;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.demeter.entity.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandCenter {
	private static final Logger logger = LoggerFactory.getLogger(CommandCenter.class);

	private static final ThreadLocal<Deque<CommandCenter>> CURRENT = new ThreadLocal<>();

	// ------------------------------

	public static void create() {
		CURRENT.set(new LinkedList<>());
	}

	public static void push(OServiceInstanceTargetVO targetVO, Map<String, Object> params) {
		if (CURRENT.get() == null) {
			throw new RuntimeException("Invalid state for command's stack param: No Deque Object!");
		}

		CURRENT.get().add(new CommandCenter(targetVO, params));
	}

	public static CommandCenter get() {
		if (CURRENT.get() == null) {
			throw new RuntimeException("Invalid state for command's stack param: No Deque Object!");
		} else if (CURRENT.get().size() == 0) {
			throw new RuntimeException("Invalid state for command's stack param: No Entry!");
		}

		return CURRENT.get().peekLast();
	}

	public static void pop() {
		if (CURRENT.get() == null) {
			throw new RuntimeException("Invalid state for command's stack param: No Deque Object!");
		} else if (CURRENT.get().size() == 0) {
			throw new RuntimeException("Invalid state for command's stack param: No Entry!");
		}

		CURRENT.get().pollLast();
	}

	public static void close() {
		if (CURRENT.get().size() != 0) {
			throw new RuntimeException("Invalid state for command's stack param: size=" + CURRENT.get().size());
		}

		CURRENT.remove();
	}

	// ------------------------------

	private OServiceInstanceTargetVO targetVO, origTargetVO;
	private Map<String, Object> params;
	private Exception exception;

	// ------------------------------

	private CommandCenter(OServiceInstanceTargetVO targetVO, Map<String, Object> params) {
		this.targetVO = targetVO;
		this.params = params;
	}

	// ------------------------------

	public Object exec(String commandName) {
		return exec(commandName, params);
	}

	public Object exec(String commandName, Map<String, Object> params) {
		try {
			Object result = CommandCenterResource.get()
				.getCommandService()
				.callCommand(new CommandQVO(commandName, targetVO.getServiceInstance().getId(), params));
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
		assertToContinue();

		int exitStatus = -1;
		String result = null;
		CommandCenterResource resource = CommandCenterResource.get();

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
			resource.setCurrentExecutor(executor);

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
		assertToContinue();
		CommandCenterResource resource = CommandCenterResource.get();

		OServiceInstanceTargetVO finalTargetVO = targetVO;
		if (!ERemoteMode.SSH.equals(finalTargetVO.getUser().getRemoteMode())) {
			finalTargetVO = resource
				.getServiceInstanceService()
				.getTargetVOByServer(finalTargetVO.getId(), ERemoteMode.SSH);
		}

		try {
			ScpToExecutor executor = new ScpToExecutor(finalTargetVO, resource, fileStore, destDir);
			resource.setCurrentExecutor(executor);
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
		assertToContinue();
		CommandCenterResource resource = CommandCenterResource.get();

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

			resource.setCurrentExecutor(executor);

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
		CommandCenterResource.get().getCommandService().userPasswordUpdated(targetVO, username, password);
	}

	public void updateVMServers(List<Map<String, String>> servers, boolean onlyNew) {
		updateVMServers(targetVO.getServerId(), servers, onlyNew);
	}

	public void updateVMServers(Long hypervisorId, List<Map<String, String>> servers, boolean onlyNew) {
		logger.info("CommandCenter: checkServers hypervisorId=[{}] servers={}", hypervisorId, servers);
		List<String> updatedServers = CommandCenterResource.get().getServerService().updateVMServers(hypervisorId, servers, onlyNew);
		CommandCenterResource.get().onResult(new CommandOutput(CommandOutput.Type.LINE, "List of VM(s): " + updatedServers.toString()));
	}

	public OServer checkVMServer(String name, String vmId, String address) {
		return checkVMServer(targetVO.getServerId(), name, vmId, address);
	}

	public OServer checkVMServer(Long hypervisorId, String name, String vmId, String address) {
		return CommandCenterResource.get().getServerService().checkVMServer(hypervisorId, name, vmId, address);
	}

	public void updateServer(Long id, String vmId) {
		CommandCenterResource.get().getServerService().updateVmid(id, vmId);
	}

	public void updateServer(Long hypervisorId, String oldVmId, String newVmId, String newName) {
		CommandCenterResource.get().getServerService().updateServer(hypervisorId, oldVmId, newVmId, newName);
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

	public void assertToContinue() {
		if (!CommandCenterResource.get().isOkToContinue()) {
			error("Canceled");
		}
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