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

	private final JSch J_SCH = new JSch();
	private final Map<Long, Session> SSH = new HashMap<>();
	private final Map<Long, Connection> DB_CONN = new HashMap<>();

	// ---------------

	private final ICommandService commandService;
	private final IOServerService serverService;
	private final IOServiceInstanceService serviceInstanceService;
	private final ICommandResultCallBack resultCallBack;

	// ------------------------------

	public CommandCenterResource(
		ICommandService commandService,
		IOServerService serverService,
		IOServiceInstanceService serviceInstanceService,
		ICommandResultCallBack resultCallBack) {

		this.commandService = commandService;
		this.serverService = serverService;
		this.serviceInstanceService = serviceInstanceService;
		this.resultCallBack = resultCallBack;
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

	// ---------------

	public void onResult(CommandOutput lineOfResult) {
		resultCallBack.onResult(lineOfResult);
	}

	public Session createSession(OServiceInstanceTargetVO targetVO) throws JSchException {

		if (!SSH.containsKey(targetVO.getId())) {
			logger.info("Try to get SSH connection: {}", targetVO.getName());
			resultCallBack.onResult(new CommandOutput(CommandOutput.Type.PROMPT, "connecting ..."));

			Session session = J_SCH.getSession(targetVO.getUsername(), targetVO.getAddress(), targetVO.getPort());
			session.setPassword(targetVO.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000); // making a connection with timeout.
			SSH.put(targetVO.getId(), session);
		}

		return SSH.get(targetVO.getId());
	}

	public Connection createConnection(OServiceInstanceTargetVO targetVO) throws ClassNotFoundException, SQLException {
		if (!DB_CONN.containsKey(targetVO.getId())) {
			Class.forName(targetVO.getProp().get("driver"));
			Connection connection = DriverManager.getConnection(targetVO.getConnection(), targetVO.getUsername(), targetVO.getPassword());
			DB_CONN.put(targetVO.getId(), connection);
		}

		return DB_CONN.get(targetVO.getId());
	}

	public void closeAll() {
		for (Session session : SSH.values()) {
			session.disconnect();
		}

		for (Connection connection : DB_CONN.values()) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("CommandCenterResource.closeAll", e);
			}
		}
	}
}
