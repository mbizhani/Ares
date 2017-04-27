package org.devocative.ares.service;

import org.devocative.ares.entity.TerminalConnection;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.iservice.IAsyncTextResult;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.service.terminal.ITerminalProcess;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.ShellConnectionVO;
import org.devocative.ares.vo.filter.TerminalConnectionFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.DTaskResult;
import org.devocative.demeter.iservice.task.ITaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("arsTerminalConnectionService")
public class TerminalConnectionService implements ITerminalConnectionService {
	private static final Logger logger = LoggerFactory.getLogger(TerminalConnectionService.class);

	private static final Map<Long, ITerminalProcess> CONNECTIONS = new ConcurrentHashMap<>();

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IOServiceInstanceService serviceInstanceService;

	@Autowired
	private ITaskService taskService;

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
	public Long createShellTerminal(Long osiUserId, IAsyncTextResult textResult) {
		OServiceInstanceTargetVO targetVOByUser = serviceInstanceService.getTargetVOByUser(osiUserId);

		TerminalConnection connection = new TerminalConnection();
		connection.setTarget(targetVOByUser.getUser());
		connection.setActive(true);

		saveOrUpdate(connection);
		persistorService.commitOrRollback();

		ITerminalProcess process = null;
		if (ERemoteMode.SSH.equals(targetVOByUser.getUser().getRemoteMode())) {
			ShellConnectionVO vo = new ShellConnectionVO(connection.getId(), targetVOByUser, textResult);
			DTaskResult result = taskService.start("arsShellConnectionDTask", connection.getId(), vo, null);
			process = (ITerminalProcess) result.getTaskInstance();
		}

		if (process != null) {
			CONNECTIONS.put(connection.getId(), process);
		}

		return connection.getId();
	}

	@Override
	public void sendMessage(Long connId, String key, Integer specialKey) {
		//logger.debug("sendMessage: connId={}", connId);

		if (CONNECTIONS.containsKey(connId)) {
			ITerminalProcess process = CONNECTIONS.get(connId);
			process.send(key, specialKey);
		} else {
			logger.warn("Sending message to invalid connection: connId=[{}]", connId);
		}
	}

	@Override
	public synchronized void closeConnection(Long connId) {
		if (CONNECTIONS.containsKey(connId)) {
			logger.info("Closing Terminal Connection: {}", connId);

			CONNECTIONS.get(connId).close();
			CONNECTIONS.remove(connId);

			TerminalConnection connection = load(connId);
			connection.setActive(false);
			connection.setDisconnection(new Date());
			saveOrUpdate(connection);
			persistorService.commitOrRollback();
		} else {
			logger.warn("Closing invalid connection: connId=[{}]", connId);
		}
	}
}