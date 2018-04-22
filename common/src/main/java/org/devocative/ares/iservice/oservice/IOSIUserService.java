package org.devocative.ares.iservice.oservice;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.*;
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

	void saveOrUpdate(OSIUser entity, String password, boolean userSelfAdd);

	String getPassword(Long userId);

	String getPassword(OSIUser user);

	Map<ESIUserType, OSIUser> findExecutorForSI(Long serviceInstId);

	Map<ESIUserType, OSIUser> findExecutor(Long serverId, ERemoteMode remoteMode);

	Map<ERemoteMode, List<OSIUser>> findAllowed();

	boolean isOSIUserAllowed(Long osiUserId);
}