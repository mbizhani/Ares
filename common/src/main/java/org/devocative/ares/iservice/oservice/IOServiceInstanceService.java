package org.devocative.ares.iservice.oservice;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.oservice.OServiceInstanceFVO;
import org.devocative.demeter.entity.User;

import java.util.List;

public interface IOServiceInstanceService {
	void saveOrUpdate(OServiceInstance entity);

	OServiceInstance load(Long id);

	List<OServiceInstance> list();

	List<OServiceInstance> search(OServiceInstanceFVO filter, long pageIndex, long pageSize);

	long count(OServiceInstanceFVO filter);

	List<OServer> getServerList();

	List<OService> getServiceList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	void updateProperties(OService oService, OServiceInstance oServiceInstance);

	OServiceInstanceTargetVO getTargetVO(Long serviceInstanceId);
}