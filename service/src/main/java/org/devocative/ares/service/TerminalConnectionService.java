//overwrite
package org.devocative.ares.service;

import org.devocative.ares.entity.TerminalConnection;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.vo.filter.TerminalConnectionFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsTerminalConnectionService")
public class TerminalConnectionService implements ITerminalConnectionService {
	private static final Logger logger = LoggerFactory.getLogger(TerminalConnectionService.class);

	@Autowired
	private IPersistorService persistorService;

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
}