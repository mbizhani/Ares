package org.devocative.ares.service.oservice;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsOSIUserService")
public class OSIUserService implements IOSIUserService {
	private static final Logger logger = LoggerFactory.getLogger(OSIUserService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OSIUser entity) {
		entity.setServer(entity.getServiceInstance().getServer());
		entity.setService(entity.getServiceInstance().getService());

		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OSIUser load(Long id) {
		return persistorService.get(OSIUser.class, id);
	}

	@Override
	public List<OSIUser> list() {
		return persistorService.list(OSIUser.class);
	}

	@Override
	public List<OSIUser> search(OSIUserFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OSIUser.class, "ent")
			.applyFilter(OSIUser.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OSIUserFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OSIUser.class, "ent")
			.applyFilter(OSIUser.class, "ent", filter)
			.object();
	}

	@Override
	public List<OServiceInstance> getServiceInstanceList() {
		return persistorService.list(OServiceInstance.class);
	}

	@Override
	public List<OServer> getServerList() {
		return persistorService.list(OServer.class);
	}

	@Override
	public List<OService> getServiceList() {
		return persistorService.list(OService.class);
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

	public OSIUser findAdminForSI(Long serviceInstId) {
		List<OSIUser> list = persistorService.createQueryBuilder()
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.admin = true")
			.addWhere("and ent.enabled = true")
			.addWhere("and ent.serviceInstance.id = :serviceInstId")
			.addParam("serviceInstId", serviceInstId)
			.list();

		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
}