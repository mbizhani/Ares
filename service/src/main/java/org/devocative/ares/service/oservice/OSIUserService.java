package org.devocative.ares.service.oservice;

import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.ares.AresConfigKey;
import org.devocative.ares.AresErrorCode;
import org.devocative.ares.AresException;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.*;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.demeter.DBConstraintViolationException;
import org.devocative.demeter.entity.ERowMode;
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

@Service("arsOSIUserService")
public class OSIUserService implements IOSIUserService {
	private static final Logger logger = LoggerFactory.getLogger(OSIUserService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private IRoleService roleService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OSIUser entity) {
		if (entity.getId() == null && entity.getType() != ESIUserType.Normal) {
			Long count = persistorService.createQueryBuilder()
				.addSelect("select count(1)")
				.addFrom(OSIUser.class, "ent")
				.addWhere("and ent.type = :type and ent.serviceInstance = :si")
				.addParam("type", entity.getType())
				.addParam("si", entity.getServiceInstance())
				.object();

			if (count > 0) {
				throw new AresException(AresErrorCode.DuplicateExecutor);
			}
		}
		entity.setServer(entity.getServiceInstance().getServer());
		entity.setService(entity.getServiceInstance().getService());

		String usernameRegEx = entity.getService().getUsernameRegEx();
		if (usernameRegEx == null) {
			usernameRegEx = ConfigUtil.getString(AresConfigKey.SIUserUsernameRegEx);
		}
		if (!entity.getUsername().matches(usernameRegEx)) {
			throw new AresException(AresErrorCode.InvalidServiceInstanceUsername, usernameRegEx);
		}

		if (entity.getRowMode() == null) {
			entity.setRowMode(ERowMode.ROLE);
		}

		if (entity.getAllowedRoles() == null) {
			entity.setAllowedRoles(new ArrayList<>());
		}
		List<Role> allowedRoles = entity.getAllowedRoles();

		String[] roleNames = new String[]{entity.getService().getName() + "Admin"};
		for (String roleName : roleNames) {
			Role role = roleService.loadByName(roleName);
			if (!allowedRoles.contains(role)) {
				allowedRoles.add(role);
			}
		}

		try {
			persistorService.saveOrUpdate(entity);
		} catch (DBConstraintViolationException e) {
			if (e.isConstraint(OSIUser.UQ_CONST)) {
				throw new AresException(AresErrorCode.DuplicateUsername, entity.getUsername());
			}
		}
	}

	@Override
	public OSIUser load(Long id) {
		return persistorService.get(OSIUser.class, id);
	}

	@Override
	public List<OSIUser> list() {
		return persistorService.list(OSIUser.class);
	}

	@Override
	public List<OSIUser> search(OSIUserFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select distinct ent")
			.addFrom(OSIUser.class, "ent")
			.applyFilter(OSIUser.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OSIUserFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(distinct ent.id)")
			.addFrom(OSIUser.class, "ent")
			.applyFilter(OSIUser.class, "ent", filter)
			.object();
	}

	@Override
	public List<OServiceInstance> getServiceInstanceList() {
		return persistorService.list(OServiceInstance.class);
	}

	@Override
	public List<OServer> getServerList() {
		return persistorService.list(OServer.class);
	}

	@Override
	public List<OService> getServiceList() {
		return persistorService.list(OService.class);
	}

	@Override
	public List<User> getAllowedUsersList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<Role> getAllowedRolesList() {
		return persistorService.list("from Role ent order by ent.name");
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
	public void saveOrUpdate(OSIUser entity, String password, boolean userSelfAdd) {
		if (password != null) {
			entity.setPassword(StringEncryptorUtil.encrypt(password));
		}

		if (entity.getId() == null && userSelfAdd) {
			entity.setRowMode(ERowMode.CREATOR);
		}

		saveOrUpdate(entity);
	}

	@Override
	public String getPassword(Long userId) {
		String password = persistorService.createQueryBuilder()
			.addSelect("select ent.password")
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.id = :id")
			.addParam("id", userId)
			.object();

		return StringEncryptorUtil.decrypt(password);
	}

	@Override
	public String getPassword(OSIUser user) {
		return StringEncryptorUtil.decrypt(user.getPassword());
	}

	@Override
	public Map<ESIUserType, OSIUser> findExecutorForSI(Long serviceInstId) {
		List<OSIUser> list = persistorService.createQueryBuilder()
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.type in (:types)", "types", ESIUserType.listOfExec())
			.addWhere("and ent.serviceInstance.id = :serviceInstId", "serviceInstId", serviceInstId)
			.addWhere("and ent.enabled = true")
			.list();

		Map<ESIUserType, OSIUser> result = new HashMap<>();
		for (OSIUser osiUser : list) {
			result.put(osiUser.getType(), osiUser);
		}

		return result;
	}

	@Override
	public Map<ESIUserType, OSIUser> findExecutor(Long serverId, ERemoteMode remoteMode) {
		List<OSIUser> list = persistorService.createQueryBuilder()
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.type in (:types)", "types", ESIUserType.listOfExec())
			.addWhere("and ent.server.id = :serverId", "serverId", serverId)
			.addWhere("and ent.remoteMode = :rm", "rm", remoteMode)
			.addWhere("and ent.enabled = true")
			.list();

		Map<ESIUserType, OSIUser> result = new HashMap<>();
		for (OSIUser osiUser : list) {
			result.put(osiUser.getType(), osiUser);
		}

		return result;
	}

	@Override
	public Map<ERemoteMode, List<OSIUser>> findAllowed() {
		Map<ERemoteMode, List<OSIUser>> result = new LinkedHashMap<>();

		for (ERemoteMode remoteMode : ERemoteMode.list()) {
			List<OSIUser> list = createBaseAllowedUsers()
				.addSelect("select ent")
				.addWhere("and ent.remoteMode = :rm", "rm", remoteMode)
				.setOrderBy("ent.remoteMode")
				.list();

			if (list.size() > 0) {
				result.put(remoteMode, list);
			}
		}

		return result;
	}

	@Override
	public boolean isOSIUserAllowed(Long osiUserId) {
		IQueryBuilder queryBuilder = createBaseAllowedUsers()
			.addSelect("select count(ent.id)")
			.addWhere("and ent.id = :osiUserId")
			.addParam("osiUserId", osiUserId);

		Long count = queryBuilder.object();
		return count == 1;
	}

	// ------------------------------

	private IQueryBuilder createBaseAllowedUsers() {
		UserVO currentUser = securityService.getCurrentUser();

		IQueryBuilder queryBuilder = persistorService.createQueryBuilder()
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.enabled = true");

		if (!currentUser.isAdmin()) {
			queryBuilder
				.addJoin("usr", "ent.allowedUsers", EJoinMode.Left)
				.addJoin("role", "ent.allowedRoles", EJoinMode.Left)

				.addWhere("and ( usr.id = :userId or (ent.rowMode = :creator and ent.creatorUserId = :userId)")
				.addWhere("or role in (:roles) )")

				.addParam("userId", currentUser.getUserId())
				.addParam("creator", ERowMode.CREATOR)
				.addParam("roles", currentUser.getRoles())
			;
		}

		return queryBuilder;
	}
}