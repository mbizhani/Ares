package org.devocative.ares.service.oservice;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.xml.AdroitXStream;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceProperty;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.oservice.IOServicePropertyService;
import org.devocative.ares.iservice.oservice.IOServiceService;
import org.devocative.ares.vo.filter.oservice.OServiceFVO;
import org.devocative.ares.vo.xml.*;
import org.devocative.demeter.entity.ERoleMode;
import org.devocative.demeter.entity.ERowMode;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IRoleService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("arsOServiceService")
public class OServiceService implements IOServiceService {
	private static final Logger logger = LoggerFactory.getLogger(OServiceService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IOServicePropertyService propertyService;

	@Autowired
	private ICommandService commandService;

	@Autowired
	private IRoleService roleService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OService entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OService load(Long id) {
		return persistorService.get(OService.class, id);
	}

	@Override
	public OService loadByName(String name) {
		return persistorService
			.createQueryBuilder()
			.addFrom(OService.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.object();
	}

	@Override
	public List<OService> list() {
		return persistorService.list(OService.class);
	}

	@Override
	public List<OService> search(OServiceFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OService.class, "ent")
			.applyFilter(OService.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OServiceFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OService.class, "ent")
			.applyFilter(OService.class, "ent", filter)
			.object();
	}

	@Override
	public List<OServiceProperty> getPropertiesList() {
		return persistorService.list(OServiceProperty.class);
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

	@Transactional
	@Override
	public void importFile(InputStream in) {
		logger.info("OServiceService.importFile()");

		Map<String, Command> commandMap = new HashMap<>();
		List<Command> list = commandService.list();
		for (Command command : list) {
			commandMap.put(String.format("%s_%s", command.getServiceIdSafely(), command.getName()), command);
		}

		XStream xstream = new AdroitXStream();
		xstream.processAnnotations(XOperation.class);

		XOperation xOperation = (XOperation) xstream.fromXML(in);
		for (XService xService : xOperation.getServices()) {
			if (xService.getName() == null) {
				throw new RuntimeException("Invalid Service XML: No service name!");
			}

			OService oService = loadByName(xService.getName());
			if (oService == null) {
				oService = new OService();
				oService.setName(xService.getName());

				roleService.createOrUpdate(xService.getName(), ERowMode.ADMIN, ERoleMode.NORMAL);
				roleService.createOrUpdate(xService.getName() + "Admin", ERowMode.ADMIN, ERoleMode.MAIN);
				roleService.createOrUpdate(xService.getName() + "SI", ERowMode.ADMIN, ERoleMode.NORMAL);

				logger.info("OService not found and created: {}", xService.getName());
			} else {
				logger.info("OService loaded: {}", oService.getName());
			}

			if (xService.getConnectionPattern() != null) {
				oService.setConnectionPattern(xService.getConnectionPattern());
			}
			if (xService.getAdminPort() != null) {
				oService.setAdminPort(xService.getAdminPort());
			}
			if (xService.getPorts() != null) {
				oService.setPorts(xService.getPorts());
			}
			if (xService.getUsernameRegEx() != null) {
				oService.setUsernameRegEx(xService.getUsernameRegEx());
			}
			saveOrUpdate(oService);

			if (xService.getProperties() != null) {
				for (XProperty xProperty : xService.getProperties()) {
					propertyService.checkAndSave(oService, xProperty.getName(), xProperty.getRequired(), xProperty.getValue());
				}
			}

			Map<String, XValidation> validationMap = new HashMap<>();
			if (xService.getValidations() != null) {
				for (XValidation xValidation : xService.getValidations()) {
					validationMap.put(xValidation.getName(), xValidation);
				}
			}

			for (XCommand xCommand : xService.getCommands()) {
				String key = String.format("%s_%s", oService.getId(), xCommand.getName());
				commandService.checkAndSave(oService, xCommand, commandMap.get(key), validationMap);
			}
		}

		//TODO persistorService.commitOrRollback();
		commandService.clearCache();
	}
}