package org.devocative.ares.iservice.oservice;

import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceProperty;
import org.devocative.ares.vo.filter.oservice.OServicePropertyFVO;
import org.devocative.demeter.entity.User;

import java.util.List;

public interface IOServicePropertyService {
	void saveOrUpdate(OServiceProperty entity);

	OServiceProperty load(Long id);

	List<OServiceProperty> list();

	List<OServiceProperty> search(OServicePropertyFVO filter, long pageIndex, long pageSize);

	long count(OServicePropertyFVO filter);

	List<OService> getServiceList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	void checkAndSave(OService oService, String propertyName, Boolean required);
}