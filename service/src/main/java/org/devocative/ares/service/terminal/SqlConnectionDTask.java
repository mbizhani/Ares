package org.devocative.ares.service.terminal;

import org.devocative.adroit.sql.NamedParameterStatement;
import org.devocative.adroit.sql.plugin.EDatabaseType;
import org.devocative.adroit.sql.plugin.PaginationPlugin;
import org.devocative.adroit.sql.result.EColumnNameCase;
import org.devocative.adroit.sql.result.QueryVO;
import org.devocative.adroit.sql.result.ResultSetProcessor;
import org.devocative.adroit.sql.result.RowVO;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.vo.SqlMessageVO;
import org.devocative.ares.vo.TerminalConnectionVO;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Scope("prototype")
@Component("arsSqlConnectionDTask")
public class SqlConnectionDTask extends DTask implements ITerminalProcess {
	private static final Logger logger = LoggerFactory.getLogger(SqlConnectionDTask.class);

	private static final Pattern COMMENT_PATTERN = Pattern.compile("(?s)('.*?')|(\".*?\")|(/\\*.*?\\*/|--.*?([\r\n]|$))");

	private long lastActivityTime;
	private TerminalConnectionVO trmConnVO;
	private BlockingQueue<SqlMessageVO> queue;

	private Connection connection;
	private EDatabaseType databaseType;
	private NamedParameterStatement currentNps;

	private AtomicBoolean running = new AtomicBoolean(false);

	@Autowired
	private ITerminalConnectionService terminalConnectionService;

	@Autowired
	private ISecurityService securityService;

	// ------------------------------

	@Override
	public void init() throws Exception {
		trmConnVO = (TerminalConnectionVO) getInputData();
		lastActivityTime = System.currentTimeMillis();
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() throws Exception {
		logger.info("SqlConnectionDTask: connecting to DB currentUser=[{}] connId=[{}} osiUser=[{}]",
			securityService.getCurrentUser(), trmConnVO.getConnectionId(), trmConnVO.getTargetVO().getUser().getUsername());

		queue = new ArrayBlockingQueue<>(1);

		while (true) {
			SqlMessageVO msg = queue.take();
			if (msg.getType() == SqlMessageVO.MsgType.TERMINATE) {
				break;
			}

			running.set(true);

			createConnection();

			try {
				if (msg.getSql() != null) {
					String sql = removeComments(msg.getSql());
					logger.debug("SqlConnectionDTask: final sql = {}", sql);
					currentNps = new NamedParameterStatement(connection, sql);

					if (sql.toLowerCase().startsWith("select")) {
						currentNps.addPlugin(new PaginationPlugin(msg.getPageIndex(), msg.getPageSize(), databaseType));
						ResultSet resultSet = currentNps.executeQuery();
						QueryVO process = ResultSetProcessor.process(resultSet, EColumnNameCase.LOWER);
						sendResult(process.toListOfMap());
					} else {
						int updateCount = currentNps.executeUpdate();
						RowVO rowVO = new RowVO();
						rowVO.put("UpdateCount", updateCount);
						sendResult(Collections.singletonList(rowVO));
					}
				}
			} catch (SQLException e) {
				logger.error("SqlConnectionDTask: Exec Sql", e);
				sendError(e);
			} finally {
				if (currentNps != null) {
					try {
						currentNps.close();
					} catch (SQLException e) {
						logger.warn("Current NPS Close", e);
					}
					currentNps = null;

					running.set(false);
				}
			}
		}

		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error("SqlConnectionDTask.close()", e);
		}
		terminalConnectionService.closeConnection(getConnectionId());

		logger.info("SqlConnectionDTask: closed connection", databaseType);
	}

	@Override
	public void cancel() throws Exception {
		queue.offer(new SqlMessageVO(SqlMessageVO.MsgType.TERMINATE));
	}

	// ---------------

	@Override
	public long getConnectionId() {
		return trmConnVO.getConnectionId();
	}

	@Override
	public void send(Object message) {
		SqlMessageVO msg = (SqlMessageVO) message;

		if (msg.getType() == SqlMessageVO.MsgType.EXEC) {
			if (running.get()) {
				throw new RuntimeException("There is a running query!");
			} else {
				queue.offer(msg);
			}
		} else if (msg.getType() == SqlMessageVO.MsgType.CANCEL) {
			if (running.get() && currentNps != null) {
				try {
					currentNps.cancel();
				} catch (SQLException e) {
					logger.warn("Current NPS Cancel", e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public long getLastActivityTime() {
		return lastActivityTime;
	}

	@Override
	public boolean isBusy() {
		return running.get();
	}

	// ------------------------------

	private void createConnection() throws ClassNotFoundException, SQLException {
		if (connection == null) {
			Class.forName(trmConnVO.getTargetVO().getProp().get("driver"));
			connection = DriverManager.getConnection(
				trmConnVO.getTargetVO().getConnection(),
				trmConnVO.getTargetVO().getUsername(),
				trmConnVO.getTargetVO().getPassword());

			databaseType = PaginationPlugin.findDatabaseType(connection);

			logger.info("SqlConnectionDTask: connected to DB type=[{}]", databaseType);
		}
	}

	private static String removeComments(String sql) {
		StringBuffer buffer = new StringBuffer();
		Matcher matcher = COMMENT_PATTERN.matcher(sql);
		while (matcher.find()) {
			if (matcher.group(3) != null) {
				matcher.appendReplacement(buffer, "");
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString().trim();
	}
}
