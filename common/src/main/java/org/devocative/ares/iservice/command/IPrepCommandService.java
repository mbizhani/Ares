package org.devocative.ares.iservice.command;

import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.PrepCommand;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.filter.command.PrepCommandFVO;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;

import java.util.List;
import java.util.Map;

public interface IPrepCommandService {
	void saveOrUpdate(PrepCommand entity);

	PrepCommand load(Long id);

	PrepCommand loadByCode(String code);

	List<PrepCommand> list();

	List<PrepCommand> search(PrepCommandFVO filter, long pageIndex, long pageSize);

	long count(PrepCommandFVO filter);

	List<Command> getCommandList();

	List<OServiceInstance> getServiceInstanceList();

	List<User> getAllowedUsersList();

	List<Role> getAllowedRolesList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	String convertParamsToString(Map<String, ?> params);

	Map<String, String> convertParamsFromString(String params);

	List<PrepCommand> findAllowed();
}