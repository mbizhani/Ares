package org.devocative.ares.service.oservice;

import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.ares.AresErrorCode;
import org.devocative.ares.AresException;
import org.devocative.ares.entity.EServerOS;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.*;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.oservice.OServiceInstanceFVO;
import org.devocative.demeter.DBConstraintViolationException;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.ICacheService;
import org.devocative.demeter.iservice.IRoleService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.devocative.demeter.iservice.template.IStringTemplate;
import org.devocative.demeter.iservice.template.IStringTemplateService;
import org.devocative.demeter.iservice.template.TemplateEngineType;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("arsOServiceInstanceService")
public class OServiceInstanceService implements IOServiceInstanceService, IMissedHitHandler<Long, OServiceInstance> {
	private static final Logger logger = LoggerFactory.getLogger(OServiceInstanceService.class);

	private ICache<Long, OServiceInstance> serviceInstanceCache;

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IOSIUserService siUserService;

	@Autowired
	private IStringTemplateService stringTemplateService;

	@Autowired
	private ICacheService cacheService;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private IRoleService roleService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OServiceInstance entity) {
		try {
			if (entity.getAllowedRoles() == null) {
				entity.setAllowedRoles(new ArrayList<>());
			}
			List<Role> allowedRoles = entity.getAllowedRoles();

			String[] roleNames = new String[]{
				entity.getService().getName() + "SI",
				entity.getService().getName() + "Admin"};

			for (String roleName : roleNames) {
				Role role = roleService.loadByName(roleName);
				if (!allowedRoles.contains(role)) {
					allowedRoles.add(role);
				}
			}

			entity = persistorService.merge(entity);
			serviceInstanceCache.remove(entity.getId());
		} catch (DBConstraintViolationException e) {
			if (e.isConstraint(OServiceInstance.UQ_CONST)) {
				throw new AresException(AresErrorCode.DuplicateServiceInstance, entity.getName());
			}
		}
	}

	@Override
	public OServiceInstance load(Long id) {
		return serviceInstanceCache.get(id);
	}

	@Override
	public List<OServiceInstance> list() {
		return persistorService.list(OServiceInstance.class);
	}

	@Override
	public List<OServiceInstance> search(OServiceInstanceFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OServiceInstance.class, "ent")
			.applyFilter(OServiceInstance.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OServiceInstanceFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OServiceInstance.class, "ent")
			.applyFilter(OServiceInstance.class, "ent", filter)
			.object();
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

	@PostConstruct
	public void initOServiceInstanceService() {
		serviceInstanceCache = cacheService.create("ARS_SRV_INST", 50);
		serviceInstanceCache.setMissedHitHandler(this);
	}

	@Override
	public OServiceInstance loadForCache(Long key) {
		OServiceInstance oServiceInstance = persistorService.get(OServiceInstance.class, key);

		//NOTE: this line is called to prevent damn LazyException!!!
		oServiceInstance.getPropertyValues().size();

		updateProperties(oServiceInstance.getService(), oServiceInstance);
		return oServiceInstance;
	}

	@Override
	public void clearCache() {
		serviceInstanceCache.clear();
	}

	@Override
	public List<KeyValueVO<Long, String>> findListForCommandExecution(Long serviceId) {
		IQueryBuilder queryBuilder = persistorService.createQueryBuilder()
			.addSelect("select ent.id, ent.name, ent.server.name, ent.service.name")
			.addFrom(OServiceInstance.class, "ent")
			.addWhere("and ent.service.id=:serviceId")
			.addParam("serviceId", serviceId);

		UserVO currentUser = securityService.getCurrentUser();
		if (!currentUser.isAdmin()) {
			queryBuilder
				.addJoin("usr", "ent.allowedUsers", EJoinMode.Left)
				.addJoin("role", "ent.allowedRoles", EJoinMode.Left)

				.addWhere("and (usr.id = :userId or role in (:roles))")
				.addParam("userId", currentUser.getUserId())
				.addParam("roles", currentUser.getRoles());
		}

		List<Object[]> list = queryBuilder.list();

		List<KeyValueVO<Long, String>> result = new ArrayList<>();
		for (Object[] serviceInstance : list) {
			String siName = (String) serviceInstance[1];
			String siServerName = (String) serviceInstance[2];
			String siServiceName = (String) serviceInstance[3];
			String caption = siName == null ?
				String.format("%s(%s)", siServerName, siServiceName) :
				String.format("%s@%s(%s)", siName, siServerName, siServiceName);
			result.add(new KeyValueVO<>((Long) serviceInstance[0], caption));
		}
		return result;
	}

	@Override
	public void updateProperties(OService oService, OServiceInstance oServiceInstance) {
		List<OSIPropertyValue> propertyValues = oServiceInstance.getPropertyValues();

		if (propertyValues == null && oService.getProperties() != null) {
			propertyValues = new ArrayList<>();
			oServiceInstance.setPropertyValues(propertyValues);
		}

		if (oService.equals(oServiceInstance.getService())) {
			for (OServiceProperty property : oService.getProperties()) {
				if (property.getValue() != null) {
					continue;
				}

				boolean foundProp = false;

				for (OSIPropertyValue propertyValue : propertyValues) {
					if (propertyValue.getProperty().equals(property)) {
						foundProp = true;
						break;
					}
				}

				if (!foundProp) {
					propertyValues.add(new OSIPropertyValue(property, oServiceInstance));
				}
			}
		} else if (propertyValues != null) {
			propertyValues.clear();
			for (OServiceProperty property : oService.getProperties()) {
				if (property.getValue() == null) {
					propertyValues.add(new OSIPropertyValue(property, oServiceInstance));
				}
			}
		}
	}

	@Override
	public OServiceInstanceTargetVO getTargetVO(Long serviceInstanceId) {
		OSIUser executorForSI = siUserService.findExecutorForSI(serviceInstanceId);

		if (executorForSI == null) {
			throw new AresException(AresErrorCode.ExecutorUserNotFound);
		}

		return createTargetVO(executorForSI);
	}

	@Override
	public OServiceInstanceTargetVO getTargetVOByUser(Long osiUserId) {
		OSIUser user = siUserService.load(osiUserId);
		return createTargetVO(user);
	}

	@Override
	public OServiceInstanceTargetVO getTargetVOByServer(Long serviceInstanceId, ERemoteMode remoteMode) {
		OServiceInstance serviceInstance = load(serviceInstanceId);
		OSIUser executor = siUserService.findExecutor(serviceInstance.getServer().getId(), remoteMode);

		if (executor == null) {
			throw new AresException(AresErrorCode.ExecutorUserNotFound);
		}

		return createTargetVO(executor)
			.setSudoer(true); //TODO Why?
	}

	@Override
	public List<OServiceInstance> loadByServer(Long serverId) {
		return persistorService.createQueryBuilder()
			.addFrom(OServiceInstance.class, "ent")
			.addWhere("and ent.serverId = :serverId")
			.addParam("serverId", serverId)
			.list();
	}

	@Override
	public OServiceInstance loadByServerAndService(OServer oServer, OService oService) {
		return persistorService.createQueryBuilder()
			.addFrom(OServiceInstance.class, "ent")
			.addWhere("and ent.server = :server")
			.addWhere("and ent.service = :service")
			.addParam("server", oServer)
			.addParam("service", oService)
			.object();
	}

	// ------------------------------

	private OServiceInstanceTargetVO createTargetVO(OSIUser user) {
		OServiceInstance serviceInstance = load(user.getServiceInstanceId());

		Map<String, String> props = new HashMap<>();
		for (OSIPropertyValue propertyValue : serviceInstance.getPropertyValues()) {
			props.put(propertyValue.getProperty().getName(), propertyValue.getValue());
		}
		for (OServiceProperty property : serviceInstance.getService().getProperties()) {
			if (property.getValue() != null) {
				props.put(property.getName(), property.getValue());
			}
		}

		String password = siUserService.getPassword(user);
		OServiceInstanceTargetVO targetVO = new OServiceInstanceTargetVO(serviceInstance, user, password, props)
			.setSudoer(
				!"root".equals(user.getUsername()) &&
					serviceInstance.getServer().getServerOS().equals(EServerOS.LINUX)
			); //TODO

		if (serviceInstance.getService().getConnectionPattern() != null) {
			Map<String, Object> params = new HashMap<>();
			params.put("target", targetVO);
			IStringTemplate template = stringTemplateService.create(serviceInstance.getService().getConnectionPattern(), TemplateEngineType.GroovyTemplate);
			String connection = (String) template.process(params);
			targetVO.setConnection(connection);
		}

		return targetVO;
	}
}