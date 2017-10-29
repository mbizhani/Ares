package org.devocative.ares.service.oservice;

import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.ares.AresErrorCode;
import org.devocative.ares.AresException;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsOSIUserService")
public class OSIUserService implements IOSIUserService {
	private static final Logger logger = LoggerFactory.getLogger(OSIUserService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private ISecurityService securityService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OSIUser entity) {
		if (entity.getId() == null && entity.getExecutor()) {
			Long count = persistorService.createQueryBuilder()
				.addSelect("select count(1)")
				.addFrom(OSIUser.class, "ent")
				.addWhere("and ent.executor = true and ent.serviceInstance = :si")
				.addParam("si", entity.getServiceInstance())
				.object();

			if (count > 0) {
				throw new AresException(AresErrorCode.DuplicateExecutor);
			}
		}

		entity.setServer(entity.getServiceInstance().getServer());
		entity.setService(entity.getServiceInstance().getService());
		if (entity.getRowMod() == null) {
			entity.setRowMod(ERowMod.CREATOR);
		}

		persistorService.saveOrUpdate(entity);
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
			.addSelect("select ent")
			.addFrom(OSIUser.class, "ent")
			.applyFilter(OSIUser.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OSIUserFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
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
	public void saveOrUpdate(OSIUser entity, String password) {
		if (password != null) {
			entity.setPassword(StringEncryptorUtil.encrypt(password));
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
	public OSIUser findExecutorForSI(Long serviceInstId) {
		List<OSIUser> list = persistorService.createQueryBuilder()
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.executor = true")
			.addWhere("and ent.enabled = true")
			.addWhere("and ent.serviceInstance.id = :serviceInstId")
			.addParam("serviceInstId", serviceInstId)
			.list();

		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}

	@Override
	public OSIUser findExecutor(Long serverId, ERemoteMode remoteMode) {
		List<OSIUser> list = persistorService.createQueryBuilder()
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.executor = true")
			.addWhere("and ent.enabled = true")
			.addWhere("and ent.server.id = :serverId")
			.addParam("serverId", serverId)
			.addWhere("and ent.remoteMode=:remoteMode")
			.addParam("remoteMode", remoteMode)
			.list();

		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}

	@Override
	public List<OSIUser> findAllowedOnes(ERemoteMode remoteMode) {
		IQueryBuilder queryBuilder = createBaseAllowedUsers()
			.addSelect("select ent")
			.addWhere("and ent.remoteMode = :rm")
			.addParam("rm", remoteMode);

		return queryBuilder.list();
	}

	@Override
	public boolean isOSIUserAllowed(Long osiUserId) {
		IQueryBuilder queryBuilder = createBaseAllowedUsers()
			.addSelect("select count(ent.id)")
			.addWhere("and ent.id=:osiUserId")
			.addParam("osiUserId", osiUserId);

		Long count = queryBuilder.object();
		return count == 1;
	}

	// ------------------------------

	private IQueryBuilder createBaseAllowedUsers() {
		UserVO currentUser = securityService.getCurrentUser();

		IQueryBuilder queryBuilder = persistorService.createQueryBuilder()
			.addFrom(OSIUser.class, "ent")
			.addWhere("and ent.enabled = true")
		;

		if (!currentUser.isAdmin()) {
			queryBuilder
				.addJoin("usr", "ent.allowedUsers", EJoinMode.Left)
				.addJoin("role", "ent.allowedRoles", EJoinMode.Left)

				.addWhere("and ( usr.id=:userId or (ent.rowMod=:creator and ent.creatorUserId=:userId) ")
				.addWhere("or role in (:roles) )")

				.addParam("userId", currentUser.getUserId())
				.addParam("creator", ERowMod.CREATOR)
				.addParam("roles", currentUser.getRoles())
			;
		}

		return queryBuilder;
	}
}