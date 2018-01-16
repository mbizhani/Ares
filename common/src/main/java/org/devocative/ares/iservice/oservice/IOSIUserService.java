package org.devocative.ares.iservice.oservice;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IEntityService;

import java.util.List;
import java.util.Map;

public interface IOSIUserService extends IEntityService<OSIUser> {
	void saveOrUpdate(OSIUser entity);

	OSIUser load(Long id);

	List<OSIUser> list();

	List<OSIUser> search(OSIUserFVO filter, long pageIndex, long pageSize);

	long count(OSIUserFVO filter);

	List<OServiceInstance> getServiceInstanceList();

	List<OServer> getServerList();

	List<OService> getServiceList();

	List<User> getAllowedUsersList();

	List<Role> getAllowedRolesList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	void saveOrUpdate(OSIUser entity, String password);

	String getPassword(Long userId);

	String getPassword(OSIUser user);

	OSIUser findExecutorForSI(Long serviceInstId);

	OSIUser findExecutor(Long serverId, ERemoteMode remoteMode);

	Map<ERemoteMode, List<OSIUser>> findAllowed();

	boolean isOSIUserAllowed(Long osiUserId);
}