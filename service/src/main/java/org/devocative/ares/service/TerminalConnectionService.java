package org.devocative.ares.service;

import org.devocative.ares.AresErrorCode;
import org.devocative.ares.AresException;
import org.devocative.ares.entity.TerminalConnection;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.service.terminal.ITerminalProcess;
import org.devocative.ares.service.terminal.ShellConnectionDTask;
import org.devocative.ares.service.terminal.SqlConnectionDTask;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.TerminalConnectionVO;
import org.devocative.ares.vo.filter.TerminalConnectionFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.DTaskResult;
import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.demeter.iservice.task.ITaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("arsTerminalConnectionService")
public class TerminalConnectionService implements ITerminalConnectionService {
	private static final Logger logger = LoggerFactory.getLogger(TerminalConnectionService.class);

	private static final Map<Long, ITerminalProcess> CONNECTIONS = new ConcurrentHashMap<>();

	private Long lastIdleCheck;

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IOServiceInstanceService serviceInstanceService;

	@Autowired
	private ITaskService taskService;

	@Autowired
	private IOSIUserService osiUserService;

	// ------------------------------

	@Override
	public void saveOrUpdate(TerminalConnection entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public TerminalConnection load(Long id) {
		return persistorService.get(TerminalConnection.class, id);
	}

	@Override
	public List<TerminalConnection> list() {
		return persistorService.list(TerminalConnection.class);
	}

	@Override
	public List<TerminalConnection> search(TerminalConnectionFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(TerminalConnection.class, "ent")
			.applyFilter(TerminalConnection.class, "ent", filter)
			.setOrderBy("ent.creationDate desc")
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(TerminalConnectionFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(TerminalConnection.class, "ent")
			.applyFilter(TerminalConnection.class, "ent", filter)
			.object();
	}

	@Override
	public List<OSIUser> getTargetList() {
		return persistorService.list(OSIUser.class);
	}

	@Override
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	// ==============================

	@Override
	public Long createTerminal(Long osiUserId, Object initConfig, ITaskResultCallback callback) {
		if (!osiUserService.isOSIUserAllowed(osiUserId)) {
			throw new AresException(AresErrorCode.TerminalConnectionAccessViolation);
		}

		OServiceInstanceTargetVO targetVOByUser = serviceInstanceService.getTargetVOByUser(osiUserId);

		TerminalConnection connection = new TerminalConnection();
		connection.setTarget(targetVOByUser.getUser());
		connection.setActive(true);

		saveOrUpdate(connection);
		persistorService.commitOrRollback();

		DTaskResult result = null;
		TerminalConnectionVO vo = new TerminalConnectionVO(connection.getId(), initConfig, targetVOByUser);
		if (ERemoteMode.SSH.equals(targetVOByUser.getUser().getRemoteMode())) {
			result = taskService.start(ShellConnectionDTask.class, connection.getId(), vo, callback);
		} else if (ERemoteMode.JDBC.equals(targetVOByUser.getUser().getRemoteMode())) {
			result = taskService.start(SqlConnectionDTask.class, connection.getId(), vo, callback);
		}

		if (result != null) {
			CONNECTIONS.put(connection.getId(), (ITerminalProcess) result.getTaskInstance());
		}

		return connection.getId();
	}

	@Override
	public void sendMessage(Long connId, Object message) {
		//logger.debug("sendMessage: connId={}", connId);

		if (connId != null && CONNECTIONS.containsKey(connId)) {
			ITerminalProcess process = CONNECTIONS.get(connId);
			process.send(message);
		} else {
			logger.warn("Sending message to invalid connection: connId=[{}]", connId);
		}
	}

	@Override
	public synchronized void closeConnection(Long connId) {
		if (connId != null && CONNECTIONS.containsKey(connId)) {
			logger.info("Closing Terminal Connection: {}", connId);

			CONNECTIONS.get(connId).close();
			CONNECTIONS.remove(connId);

			TerminalConnection connection = load(connId);
			connection.setActive(false);
			connection.setDisconnection(new Date());
			saveOrUpdate(connection);
			persistorService.commitOrRollback();
		}
	}

	@Override
	public void closeIdleConnections() {
		if (lastIdleCheck == null) {
			lastIdleCheck = System.currentTimeMillis();
			logger.info("TerminalConnection: idle check init [{}]", new Date());
		} else {
			logger.info("TerminalConnection: idle check [{}]", new Date());

			List<ITerminalProcess> processes = new ArrayList<>(CONNECTIONS.values());

			long now = System.currentTimeMillis();
			long diff = now - lastIdleCheck;
			for (ITerminalProcess process : processes) {
				long lastActivityTime = process.getLastActivityTime();
				if ((now - lastActivityTime) > diff) {
					logger.info("TerminalConnection: idle connection [{}]", process.getConnectionId());
					closeConnection(process.getConnectionId());
				}
			}
			lastIdleCheck = now;
		}
	}
}