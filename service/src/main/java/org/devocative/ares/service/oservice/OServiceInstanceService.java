package org.devocative.ares.service.oservice;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.OSIPropertyValue;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.entity.oservice.OServiceProperty;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.oservice.OServiceInstanceFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.template.IStringTemplate;
import org.devocative.demeter.iservice.template.IStringTemplateService;
import org.devocative.demeter.iservice.template.TemplateEngineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("arsOServiceInstanceService")
public class OServiceInstanceService implements IOServiceInstanceService {
	private static final Logger logger = LoggerFactory.getLogger(OServiceInstanceService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IStringTemplateService stringTemplateService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OServiceInstance entity) {
		if(entity.getName() == null) {
			entity.setName(String.format("%s@%s", entity.getService(), entity.getServer()));
		}
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OServiceInstance load(Long id) {
		OServiceInstance oServiceInstance = persistorService.get(OServiceInstance.class, id);
		updateProperties(oServiceInstance.getService(), oServiceInstance);
		return oServiceInstance;
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
	public List<OServiceInstance> getRelatedList() {
		return persistorService.list(OServiceInstance.class);
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
	public void updateProperties(OService oService, OServiceInstance oServiceInstance) {
		List<OSIPropertyValue> propertyValues = oServiceInstance.getPropertyValues();
		if (oService.equals(oServiceInstance.getService())) {
			for (OServiceProperty property : oService.getProperties()) {
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
		} else {
			propertyValues.clear();
			for (OServiceProperty property : oService.getProperties()) {
				propertyValues.add(new OSIPropertyValue(property, oServiceInstance));
			}
		}
	}

	public OServiceInstanceTargetVO getTargetVO(Long id) {
		OServiceInstance serviceInstance = load(id);

		Map<String, String> props = new HashMap<>();
		List<OSIPropertyValue> properties = serviceInstance.getPropertyValues();
		for (OSIPropertyValue propertyValue : properties) {
			props.put(propertyValue.getProperty().getName(), propertyValue.getValue());
		}

		OServiceInstanceTargetVO targetVO = new OServiceInstanceTargetVO(serviceInstance, null, props); //TODO

		if (serviceInstance.getService().getConnectionPattern() != null) {
			Map<String, Object> params = new HashMap<>();
			params.put("target", targetVO);
			IStringTemplate template = stringTemplateService.create(serviceInstance.getService().getConnectionPattern(), TemplateEngineType.Groovy);
			String connection = (String) template.process(params);
			targetVO.setConnection(connection);
		}
		return targetVO;
	}
}