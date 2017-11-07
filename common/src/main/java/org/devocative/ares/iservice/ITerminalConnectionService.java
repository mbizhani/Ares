package org.devocative.ares.iservice;

import org.devocative.ares.entity.TerminalConnection;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.vo.filter.TerminalConnectionFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.task.ITaskResultCallback;

import java.util.List;

public interface ITerminalConnectionService {
	void saveOrUpdate(TerminalConnection entity);

	TerminalConnection load(Long id);

	List<TerminalConnection> list();

	List<TerminalConnection> search(TerminalConnectionFVO filter, long pageIndex, long pageSize);

	long count(TerminalConnectionFVO filter);

	List<OSIUser> getTargetList();

	List<User> getCreatorUserList();

	// ==============================

	Long createTerminal(Long osiUserId, Object initConfig, ITaskResultCallback callback);

	void sendMessage(Long connId, Object message);

	void closeConnection(Long connId);

	void closeIdleConnections();
}