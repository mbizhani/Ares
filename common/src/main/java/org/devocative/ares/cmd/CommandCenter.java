package org.devocative.ares.cmd;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.devocative.adroit.CalendarUtil;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommandCenter {
	private static final Logger logger = LoggerFactory.getLogger(CommandCenter.class);

	private final JSch J_SCH = new JSch();

	private ICommandService commandService;
	private OServiceInstanceTargetVO targetVO;
	private ICommandResultCallBack resultCallBack;

	private Map<Long, Session> SSH = new HashMap<>();
	private Map<Long, Connection> DB_CONN = new HashMap<>();

	private Exception exception;

	// ------------------------------

	public CommandCenter(ICommandService commandService, OServiceInstanceTargetVO targetVO, ICommandResultCallBack resultCallBack) {
		this.commandService = commandService;
		this.targetVO = targetVO;
		this.resultCallBack = resultCallBack;
	}

	// ------------------------------

	public Object exec(String commandName, Map<String, String> params) {
		return exec(commandName, targetVO, params);
	}

	public Object exec(String commandName, OServiceInstanceTargetVO target, Map<String, String> params) {
		try {
			Object result = commandService.executeCommand(commandName, target.getServiceInstance(), params, resultCallBack);
			logger.info("CommandCenter.exec: commandName=[{}}", commandName);
			return result;
		} catch (Exception e) {
			logger.error("CommandCenter.exec: " + commandName, e);
			setException(e);
		}

		return null; //TODO
	}

	// ---------------

	public SshResult ssh(String cmd) {
		return ssh(cmd, targetVO, false, (String) null);
	}

	public SshResult ssh(String cmd, boolean force) {
		return ssh(cmd, targetVO, force, (String) null);
	}

	public SshResult ssh(String cmd, String... stdin) {
		return ssh(cmd, targetVO, false, stdin);
	}

	public SshResult ssh(String cmd, boolean force, String... stdin) {
		return ssh(cmd, targetVO, force, stdin);
	}

	public SshResult ssh(String cmd, OServiceInstanceTargetVO targetVO) {
		return ssh(cmd, targetVO, false, (String) null);
	}

	public SshResult ssh(String cmd, OServiceInstanceTargetVO targetVO, boolean force) {
		return ssh(cmd, targetVO, force, (String) null);
	}

	// Main ssh()
	public SshResult ssh(String cmd, OServiceInstanceTargetVO targetVO, boolean force, String... stdin) {
		int exitStatus = -1;
		String result = null;

		try {
			ShellCommandExecutor executor = new ShellCommandExecutor(
				targetVO, resultCallBack, cmd, J_SCH, SSH.get(targetVO.getId()), stdin);

			Thread th = new Thread(executor);
			th.start();
			th.join();

			if (executor.hasException()) {
				throw executor.getException();
			}

			exitStatus = executor.getExitStatus();
			result = executor.getResult().toString();
			SSH.put(targetVO.getId(), executor.getSession());

			logger.info("Executed SSH Command: exitStatus=[{}] cmd=[{}] si=[{}]", exitStatus, cmd, targetVO);

			if (exitStatus != 0) {
				if (force) {
					resultCallBack.onResult(new CommandOutput(CommandOutput.Type.LINE, "WARNING: exitStatus: " + exitStatus));
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

	public Object sql(String sql) {
		return sql(sql, targetVO);
	}

	public Object sql(String sql, OServiceInstanceTargetVO targetVO) {
		Object result = null;

		try {
			SqlCommandExecutor executor = new SqlCommandExecutor(targetVO, resultCallBack, sql, DB_CONN.get(targetVO.getId()));
			Thread th = new Thread(executor);
			th.start();
			th.join();

			if (executor.hasException()) {
				throw executor.getException();
			}

			result = executor.getResult();
			DB_CONN.put(targetVO.getId(), executor.getConnection());
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

	public OServiceInstanceTargetVO findOf(String remoteMode) {
		return commandService.findOf(targetVO.getId(), remoteMode);
	}

	public void userPasswordUpdated(String username, String password) {
		userPasswordUpdated(targetVO, username, password);
	}

	public void userPasswordUpdated(OServiceInstanceTargetVO targetVO, String username, String password) {
		logger.info("CommandCenter.userPasswordUpdated: target=[{}] username=[{}]", targetVO, username);
		commandService.userPasswordUpdated(targetVO, username, password);
	}

	public void error(String message) {
		//resultCallBack.onResult(new CommandOutput(CommandOutput.Type.ERROR, "Error: " + message));
		throw new RuntimeException(message);
	}

	public void warn(String message) {
		resultCallBack.onResult(new CommandOutput(CommandOutput.Type.LINE, "Warn: " + message));
	}

	public void info(String message) {
		resultCallBack.onResult(new CommandOutput(CommandOutput.Type.LINE, "Info: " + message));
	}

	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
		closeAll();
		throw new RuntimeException(exception);
	}

	public void closeAll() {
		for (Session session : SSH.values()) {
			session.disconnect();
		}

		for (Connection connection : DB_CONN.values()) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("CommandCenter.closeAll", e);
			}
		}
	}
}
