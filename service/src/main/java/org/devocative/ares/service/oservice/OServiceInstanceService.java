//overwrite
package org.devocative.ares.service.oservice;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.filter.oservice.OServiceInstanceFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsOServiceInstanceService")
public class OServiceInstanceService implements IOServiceInstanceService {
	private static final Logger logger = LoggerFactory.getLogger(OServiceInstanceService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OServiceInstance entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OServiceInstance load(Long id) {
		return persistorService.get(OServiceInstance.class, id);
	}

	@Override
	public List<OServiceInstance> list() {
		return persistorService.list(OServiceInstance.class);
	}

	@Override
	public List<OServiceInstance> search(OServiceInstanceFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OServiceInstance.class, "ent")
			.applyFilter(OServiceInstance.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OServiceInstanceFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OServiceInstance.class, "ent")
			.applyFilter(OServiceInstance.class, "ent", filter)
			.object();
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
	public List<OServiceInstance> getRelatedList() {
		return persistorService.list(OServiceInstance.class);
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