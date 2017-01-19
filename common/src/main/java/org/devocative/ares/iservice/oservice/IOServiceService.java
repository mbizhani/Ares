//overwrite
package org.devocative.ares.iservice.oservice;

import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.vo.filter.oservice.OServiceFVO;
import org.devocative.demeter.entity.User;

import java.util.List;

public interface IOServiceService {
	void saveOrUpdate(OService entity);

	OService load(Long id);

	OService loadByName(String name);

	List<OService> list();

	List<OService> search(OServiceFVO filter, long pageIndex, long pageSize);

	long count(OServiceFVO filter);

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================
}