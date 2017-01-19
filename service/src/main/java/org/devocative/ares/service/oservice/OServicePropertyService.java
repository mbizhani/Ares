//overwrite
package org.devocative.ares.service.oservice;

import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceProperty;
import org.devocative.ares.iservice.oservice.IOServicePropertyService;
import org.devocative.ares.vo.filter.oservice.OServicePropertyFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsOServicePropertyService")
public class OServicePropertyService implements IOServicePropertyService {

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OServiceProperty entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OServiceProperty load(Long id) {
		return persistorService.get(OServiceProperty.class, id);
	}

	@Override
	public List<OServiceProperty> list() {
		return persistorService.list(OServiceProperty.class);
	}

	@Override
	public List<OServiceProperty> search(OServicePropertyFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OServiceProperty.class, "ent")
			.applyFilter(OServiceProperty.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OServicePropertyFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OServiceProperty.class, "ent")
			.applyFilter(OServiceProperty.class, "ent", filter)
			.object();
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
}