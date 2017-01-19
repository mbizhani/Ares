//overwrite
package org.devocative.ares.service.oservice;

import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.iservice.oservice.IOServiceService;
import org.devocative.ares.vo.filter.oservice.OServiceFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsOServiceService")
public class OServiceService implements IOServiceService {

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OService entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OService load(Long id) {
		return persistorService.get(OService.class, id);
	}

	@Override
	public OService loadByName(String name) {
		return persistorService
			.createQueryBuilder()
			.addFrom(OService.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.object();
	}

	@Override
	public List<OService> list() {
		return persistorService.list(OService.class);
	}

	@Override
	public List<OService> search(OServiceFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OService.class, "ent")
			.applyFilter(OService.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OServiceFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OService.class, "ent")
			.applyFilter(OService.class, "ent", filter)
			.object();
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