package org.devocative.ares.iservice;

import org.devocative.ares.entity.TerminalConnection;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.vo.filter.TerminalConnectionFVO;
import org.devocative.demeter.entity.User;

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

	Long createShellTerminal(Long osiUserId, IAsyncTextResult textResult);

	void sendMessage(Long connId, String key, Integer specialKey);

	void closeConnection(Long connId);

	void closeIdleConnections();
}