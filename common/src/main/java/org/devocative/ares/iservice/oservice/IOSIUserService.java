//overwrite
package org.devocative.ares.iservice.oservice;

import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.demeter.entity.User;

import java.util.List;

public interface IOSIUserService {
	void saveOrUpdate(OSIUser entity);

	OSIUser load(Long id);

	List<OSIUser> list();

	List<OSIUser> search(OSIUserFVO filter, long pageIndex, long pageSize);

	long count(OSIUserFVO filter);

	List<OServiceInstance> getServiceInstanceList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================
}