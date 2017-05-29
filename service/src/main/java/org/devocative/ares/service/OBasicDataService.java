package org.devocative.ares.service;

import org.devocative.ares.entity.EBasicDiscriminator;
import org.devocative.ares.entity.OBasicData;
import org.devocative.ares.iservice.IOBasicDataService;
import org.devocative.ares.vo.filter.OBasicDataFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsOBasicDataService")
public class OBasicDataService implements IOBasicDataService {
	private static final Logger logger = LoggerFactory.getLogger(OBasicDataService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OBasicData entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OBasicData load(Long id) {
		return persistorService.get(OBasicData.class, id);
	}

	@Override
	public List<OBasicData> list() {
		return persistorService.list(OBasicData.class);
	}

	@Override
	public List<OBasicData> search(OBasicDataFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OBasicData.class, "ent")
			.applyFilter(OBasicData.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OBasicDataFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OBasicData.class, "ent")
			.applyFilter(OBasicData.class, "ent", filter)
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

	@Override
	public List<OBasicData> listByDiscriminator(EBasicDiscriminator discriminator) {
		return persistorService.createQueryBuilder()
			.addFrom(OBasicData.class, "ent")
			.addWhere("and ent.discriminator = :discriminator")
			.addParam("discriminator", discriminator)
			.list();
	}
}