//overwrite
package org.devocative.ares.service;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.vo.filter.OServerFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsOServerService")
public class OServerService implements IOServerService {
	private static final Logger logger = LoggerFactory.getLogger(OServerService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OServer entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OServer load(Long id) {
		return persistorService.get(OServer.class, id);
	}

	@Override
	public OServer loadByName(String name) {
		return persistorService
			.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.object();
	}

	@Override
	public OServer loadByAddress(String address) {
		return persistorService
			.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.address = :address")
			.addParam("address", address)
			.object();
	}

	@Override
	public List<OServer> list() {
		return persistorService.list(OServer.class);
	}

	@Override
	public List<OServer> search(OServerFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OServer.class, "ent")
			.applyFilter(OServer.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OServerFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OServer.class, "ent")
			.applyFilter(OServer.class, "ent", filter)
			.object();
	}

	@Override
	public List<OServer> getHypervisorList() {
		return persistorService.list(OServer.class);
	}

	@Override
	public List<User> getOwnerList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> getModifierUserList() {
		return persistorService.list(User.class);
	}

	// ==============================
}