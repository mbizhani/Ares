package org.devocative.ares.cmd;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CommandCenter {
	private static final Logger logger = LoggerFactory.getLogger(CommandCenter.class);

	private static final JSch J_SCH = new JSch();

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

	public Object exec(String commandName, Map<String, Object> params) {
		return exec(commandName, targetVO, params);
	}

	public Object exec(String commandName, OServiceInstanceTargetVO target, Map<String, Object> params) {
		return null;
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
		int exitStatus = 0;
		StringBuilder result = new StringBuilder();

		try {
			if (!SSH.containsKey(targetVO.getId())) {
				logger.info("Try to get SSH connection: {}", targetVO.getFullName());

				Session session = J_SCH.getSession(targetVO.getUser().getUsername(), targetVO.getAddress(), targetVO.getPort());
				session.setPassword(targetVO.getUser().getPassword());
				session.setConfig("StrictHostKeyChecking", "no");
				session.connect(30000); // making a connection with timeout.

				SSH.put(targetVO.getId(), session);
				logger.info("Successful SSH connection: {}", targetVO.getFullName());
			}

			Session session = SSH.get(targetVO.getId());

			logger.info("Sending SSH Command: cmd=[{}] si=[{}]", cmd, targetVO);

			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(cmd);
			channelExec.setInputStream(null);
			channelExec.setErrStream(null);

			InputStream in = channelExec.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			InputStream err = channelExec.getErrStream();
			BufferedReader errBr = new BufferedReader(new InputStreamReader(err));

			OutputStream out = null;
			if (stdin != null) {
				out = channelExec.getOutputStream();
			}

			channelExec.connect();

			if (out != null) {
				for (String s : stdin) {
					out.write((s + "\n").getBytes());
				}
				out.flush();
			}

			while (true) {
				String line;
				while ((line = br.readLine()) != null) {
					resultCallBack.onResult(line);
					logger.debug("\tResult = {}", line);
					result.append(line).append("\n");
				}
				if (channelExec.isClosed()) {
					exitStatus = channelExec.getExitStatus();
					break;
				}
			}

			while (true) {
				String line;
				while ((line = errBr.readLine()) != null) {
					resultCallBack.onResult(line);
					logger.error("\tResult = {}", line);
				}
				if (channelExec.isClosed()) {
					break;
				}
			}

			channelExec.disconnect();

			logger.info("Executed SSH Command: exitStatus=[{}] cmd=[{}] si=[{}]", exitStatus, cmd, targetVO);

			if (exitStatus != 0 && !force) {
				throw new RuntimeException("Invalid ssh command exitStatus: " + exitStatus);
			}
		} catch (Exception e) {
			logger.error("CommandCenter.ssh", e);
			setException(e);
		}

		return new SshResult(result.toString(), exitStatus);
	}

	// ---------------

	public Object sql(String sql) {
		return sql(sql, targetVO);
	}

	public Object sql(String sql, OServiceInstanceTargetVO targetVO) {
		if (!DB_CONN.containsKey(targetVO.getId())) {
			Connection connection = commandService.getConnection(targetVO);
			DB_CONN.put(targetVO.getId(), connection);
		}

		try {
			Connection connection = DB_CONN.get(targetVO.getId());
			Statement statement = connection.createStatement();
			if (statement.execute(sql)) {
				ResultSet rs = statement.getResultSet();
				//TODO
			} else {
				logger.info("Execute non-select query: update count=[{}]", statement.getUpdateCount());
			}
			statement.close();
			return null;
		} catch (SQLException e) {
			logger.error("CommandCenter.sql", e);
			setException(e);
		}

		return null;
	}

	// ---------------

	public String now(String format, String locale) {
		return "";
	}

	public OServiceInstanceTargetVO findRelated(String type) {
		return null;
	}

	public void error(String message) {

	}

	public void warn(String message) {

	}

	public void info(String message) {

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
