package org.devocative.ares.service.command;

import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.EViewMode;
import org.devocative.ares.entity.command.PrepCommand;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.command.IPrepCommandService;
import org.devocative.ares.iservice.oservice.IOServiceService;
import org.devocative.ares.vo.filter.command.PrepCommandFVO;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IRoleService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("arsPrepCommandService")
public class PrepCommandService implements IPrepCommandService {
	private static final Logger logger = LoggerFactory.getLogger(PrepCommandService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IOServiceService serviceService;

	// ------------------------------

	@Override
	public void saveOrUpdate(PrepCommand entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public PrepCommand load(Long id) {
		return persistorService.get(PrepCommand.class, id);
	}

	@Override
	public PrepCommand loadByCode(String code) {
		return persistorService
			.createQueryBuilder()
			.addFrom(PrepCommand.class, "ent")
			.addWhere("and ent.code = :code")
			.addParam("code", code)
			.object();
	}

	@Override
	public List<PrepCommand> list() {
		return persistorService.list(PrepCommand.class);
	}

	@Override
	public List<PrepCommand> search(PrepCommandFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(PrepCommand.class, "ent")
			.applyFilter(PrepCommand.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(PrepCommandFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(PrepCommand.class, "ent")
			.applyFilter(PrepCommand.class, "ent", filter)
			.object();
	}

	@Override
	public List<Command> getCommandList() {
		return persistorService.list(Command.class);
	}

	@Override
	public List<OServiceInstance> getServiceInstanceList() {
		return persistorService.list(OServiceInstance.class);
	}

	@Override
	public List<User> getAllowedUsersList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<Role> getAllowedRolesList() {
		return persistorService.list(Role.class);
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
	public String convertParamsToString(Map<String, ?> params) {
		StringBuilder builder = new StringBuilder();
		Map<String, ?> sortedParams = new TreeMap<>(params);
		for (Map.Entry<String, ?> entry : sortedParams.entrySet()) {
			builder
				.append(entry.getKey())
				.append("=")
				.append(entry.getValue())
				.append(";;");
		}
		return builder.toString();
	}

	@Override
	public Map<String, String> convertParamsFromString(String params) {
		Map<String, String> result = new HashMap<>();

		String[] paramsArr = params.split(";;");
		for (String param : paramsArr) {
			int idx = param.indexOf("=");
			String key = param.substring(0, idx);
			String value = param.substring(idx + 1);
			result.put(key, value);
		}
		return result;
	}

	@Override
	public Map<OService, List<PrepCommand>> findAllowed() {
		Map<OService, List<PrepCommand>> result = new HashMap<>();

		UserVO currentUser = securityService.getCurrentUser();

		IQueryBuilder queryBuilder = persistorService.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(PrepCommand.class, "ent")
			.addJoin("cmd", "ent.command")
			.addWhere("and ent.enabled = true")
			.addWhere("and cmd.enabled = true")
			.addWhere("and cmd.service = :service")
			.addWhere("and cmd.viewMode in :viewMode")
			.addParam("viewMode", Arrays.asList(EViewMode.NORMAL, EViewMode.LIST))
			.setOrderBy("ent.name");

		if (!currentUser.isAdmin()) {
			queryBuilder
				.addJoin("usr", "ent.allowedUsers", EJoinMode.Left)
				.addJoin("role", "ent.allowedRoles", EJoinMode.Left)

				.addWhere("and (usr.id = :userId or role in (:roles))")
				.addParam("userId", currentUser.getUserId())
				.addParam("roles", currentUser.getRoles())
			;
		}

		List<OService> services = serviceService.list();
		for (OService service : services) {
			List<PrepCommand> list = queryBuilder
				.addParam("service", service)
				.list();
			result.put(service, list);
		}

		return result;
	}

	@Override
	public void saveByCommand(Command command) {
		Role role = roleService.loadByName(command.getService().getName());

		PrepCommand prepCommand = new PrepCommand();
		prepCommand.setName(command.getName());
		prepCommand.setCode("c" + command.getId());
		prepCommand.setCommand(command);

		if (role != null) {
			prepCommand.setAllowedRoles(Collections.singletonList(role));
		}

		saveOrUpdate(prepCommand);
	}
}