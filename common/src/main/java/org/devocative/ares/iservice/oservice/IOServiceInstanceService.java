package org.devocative.ares.iservice.oservice;

import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.oservice.OServiceInstanceFVO;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IEntityService;

import java.util.List;

public interface IOServiceInstanceService extends IEntityService<OServiceInstance> {
	void saveOrUpdate(OServiceInstance entity);

	OServiceInstance load(Long id);

	List<OServiceInstance> list();

	List<OServiceInstance> search(OServiceInstanceFVO filter, long pageIndex, long pageSize);

	long count(OServiceInstanceFVO filter);

	List<OServer> getServerList();

	List<OService> getServiceList();

	List<User> getAllowedUsersList();

	List<Role> getAllowedRolesList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	void clearCache();

	List<KeyValueVO<Long, String>> findListForCommandExecution(Long serviceId);

	void updateProperties(OService oService, OServiceInstance oServiceInstance);

	OServiceInstanceTargetVO getTargetVO(Long serviceInstanceId);

	OServiceInstanceTargetVO getTargetVOByUser(Long osiUserId);

	OServiceInstanceTargetVO getTargetVOByServer(Long serviceInstanceId, ERemoteMode remoteMode);

	List<OServiceInstance> loadByServer(Long serverId);

	OServiceInstance loadByServerAndService(OServer oServer, OService oService);
}