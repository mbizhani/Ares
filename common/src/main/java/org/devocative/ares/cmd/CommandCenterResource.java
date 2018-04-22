package org.devocative.ares.cmd;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CommandCenterResource {
	private static final Logger logger = LoggerFactory.getLogger(CommandCenterResource.class);

	private static final ThreadLocal<CommandCenterResource> CURRENT = new ThreadLocal<>();

	// ------------------------------

	public static void create(ICommandService commandService,
							  IOServerService serverService,
							  IOServiceInstanceService serviceInstanceService,
							  ICommandResultCallBack resultCallBack,
							  Long logId) {
		CURRENT.set(new CommandCenterResource(commandService, serverService, serviceInstanceService, resultCallBack, logId));
	}

	public static CommandCenterResource get() {
		return CURRENT.get();
	}

	public static void close() {
		CURRENT.get().closeAll();
		CURRENT.remove();
	}

	// ------------------------------

	private final JSch J_SCH = new JSch();
	private final Map<Long, Session> SSH = new HashMap<>();
	private final Map<Long, Connection> DB_CONN = new HashMap<>();

	// ---------------

	private final ICommandService commandService;
	private final IOServerService serverService;
	private final IOServiceInstanceService serviceInstanceService;
	private final ICommandResultCallBack resultCallBack;
	private final Long logId;

	// ------------------------------

	private CommandCenterResource(
		ICommandService commandService,
		IOServerService serverService,
		IOServiceInstanceService serviceInstanceService,
		ICommandResultCallBack resultCallBack,
		Long logId) {

		this.commandService = commandService;
		this.serverService = serverService;
		this.serviceInstanceService = serviceInstanceService;
		this.resultCallBack = resultCallBack;
		this.logId = logId;
	}

	// ------------------------------

	public ICommandService getCommandService() {
		return commandService;
	}

	public IOServerService getServerService() {
		return serverService;
	}

	public IOServiceInstanceService getServiceInstanceService() {
		return serviceInstanceService;
	}

	public boolean isOkToContinue() {
		return commandService.isOkToContinue(logId);
	}

	public void setCurrentExecutor(AbstractExecutor current) {
		commandService.setCurrentExecutor(logId, current);
	}

	// ---------------

	public void onResult(CommandOutput lineOfResult) {
		resultCallBack.onResult(lineOfResult);
	}

	public Session createSession(OServiceInstanceTargetVO targetVO, boolean admin) throws JSchException {
		Long key = admin ? targetVO.getAdmin().getId() : targetVO.getUser().getId();

		if (!SSH.containsKey(key)) {
			logger.info("Try to get SSH connection: {} by user {}", targetVO.getName(), targetVO.getUsername());
			resultCallBack.onResult(new CommandOutput(CommandOutput.Type.PROMPT, "connecting ..."));

			String username = targetVO.getUsername();
			String password = targetVO.getPassword();

			if (admin) {
				if (targetVO.getAdmin() == null) {
					throw new RuntimeException("No SSH Admin Executor for " + targetVO.getName());
				}

				username = targetVO.getAdmin().getUsername();
				password = targetVO.getAdminPassword();
			}


			Session session = J_SCH.getSession(username, targetVO.getAddress(), targetVO.getPort());
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000); // making a connection with timeout.
			SSH.put(key, session);
		}

		return SSH.get(key);
	}

	public Connection createConnection(OServiceInstanceTargetVO targetVO, boolean admin) throws ClassNotFoundException, SQLException {
		Long key = admin ? targetVO.getAdmin().getId() : targetVO.getUser().getId();

		if (!DB_CONN.containsKey(key)) {
			Class.forName(targetVO.getProp().get("driver"));

			String username = targetVO.getUsername();
			String password = targetVO.getPassword();

			if (admin) {
				if (targetVO.getAdmin() == null) {
					throw new RuntimeException("No SQL Admin Executor for " + targetVO.getName());
				}

				username = targetVO.getAdmin().getUsername();
				password = targetVO.getAdminPassword();
			}

			Connection connection = DriverManager.getConnection(targetVO.getConnection(), username, password);
			DB_CONN.put(key, connection);
		}

		return DB_CONN.get(key);
	}

	private void closeAll() {
		for (Session session : SSH.values()) {
			try {
				session.disconnect();
			} catch (Exception e) {
				logger.error("CommandCenterResource.closeAll: SSH", e);
			}
		}

		for (Connection connection : DB_CONN.values()) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("CommandCenterResource.closeAll: SQL", e);
			}
		}
	}
}
