package org.devocative.ares.service.terminal;

import org.devocative.adroit.sql.NamedParameterStatement;
import org.devocative.adroit.sql.plugin.EDatabaseType;
import org.devocative.adroit.sql.plugin.PaginationPlugin;
import org.devocative.adroit.sql.result.EColumnNameCase;
import org.devocative.adroit.sql.result.QueryVO;
import org.devocative.adroit.sql.result.ResultSetProcessor;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Scope("prototype")
@Component("arsSqlConnectionDTask")
public class SqlConnectionDTask extends DTask implements ITerminalProcess {
	private static final Logger logger = LoggerFactory.getLogger(SqlConnectionDTask.class);

	private long lastActivityTime;
	private TerminalConnectionVO trmConnVO;
	private BlockingQueue<SqlMessageVO> queue;

	private Connection connection;
	private EDatabaseType databaseType;

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

		queue = new ArrayBlockingQueue<>(10);

		while (true) {
			SqlMessageVO msg = queue.take();
			if (msg.getPageIndex() < 0) { //TODO
				break;
			}

			createConnection();

			try {
				if (msg.getSql() != null) {
					NamedParameterStatement nps = new NamedParameterStatement(connection, msg.getSql());
					nps.addPlugin(new PaginationPlugin(msg.getPageIndex(), msg.getPageSize(), databaseType));
					ResultSet resultSet = nps.executeQuery();
					QueryVO process = ResultSetProcessor.process(resultSet, EColumnNameCase.LOWER);
					sendResult(process.toListOfMap());
					nps.close();
				}
			} catch (SQLException e) {
				logger.error("SqlConnectionDTask: Exec Sql", e);
				sendError(e);
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

	// ---------------

	@Override
	public long getConnectionId() {
		return trmConnVO.getConnectionId();
	}

	@Override
	public void send(Object message) {
		SqlMessageVO msg = (SqlMessageVO) message;
		queue.offer(msg);
	}

	@Override
	public void close() {
		queue.offer(new SqlMessageVO(null, -1, -1));
	}

	@Override
	public long getLastActivityTime() {
		return lastActivityTime;
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
}
