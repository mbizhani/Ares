package org.devocative.ares.service.command;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.adroit.xml.AdroitXStream;
import org.devocative.ares.AresErrorCode;
import org.devocative.ares.AresException;
import org.devocative.ares.cmd.CommandCenter;
import org.devocative.ares.cmd.CommandCenterResource;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.ConfigLob;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.command.ICommandLogService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.ICacheService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.template.IStringTemplate;
import org.devocative.demeter.iservice.template.IStringTemplateService;
import org.devocative.demeter.iservice.template.TemplateEngineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("arsCommandService")
public class CommandService implements ICommandService, IMissedHitHandler<Long, Command> {
	private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

	private XStream xstream;
	private ICache<Long, Command> commandCache;

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private ICacheService cacheService;

	@Autowired
	private IStringTemplateService stringTemplateService;

	@Autowired
	private IOServiceInstanceService serviceInstanceService;

	@Autowired
	private IOSIUserService osiUserService;

	@Autowired
	private ICommandLogService commandLogService;

	@Autowired
	private ISecurityService securityService;

	// ------------------------------

	@Override
	public void saveOrUpdate(Command entity) {
		persistorService.saveOrUpdate(entity);
		commandCache.remove(entity.getId());
		stringTemplateService.clearCacheFor("CMD_" + entity.getId());
	}

	@Override
	public Command load(Long id) {
		return commandCache.get(id);
	}

	@Override
	public List<Command> list() {
		return persistorService.list(Command.class);
	}

	@Override
	public List<Command> search(CommandFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Command.class, "ent")
			.applyFilter(Command.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(CommandFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(Command.class, "ent")
			.applyFilter(Command.class, "ent", filter)
			.object();
	}

	@Override
	public List<OService> getServiceList() {
		return persistorService.list(OService.class);
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
	public void initCommandService() {
		xstream = new AdroitXStream();
		xstream.processAnnotations(XCommand.class);

		commandCache = cacheService.create("ARS_COMMAND", 50);
		commandCache.setMissedHitHandler(this);
	}

	// IMissedHitHandler
	@Override
	public Command loadForCache(Long key) {
		Command command = persistorService
			.createQueryBuilder()
			.addFrom(Command.class, "ent")
			.addJoin("cfg", "ent.config", EJoinMode.LeftFetch)
			.addWhere("and ent.id = :id")
			.addParam("id", key)
			.object();

		command.setXCommand(loadXCommand(command));

		return command;
	}

	@Override
	public void checkAndSave(OService oService, XCommand xCommand) {
		Command command = loadByNameAndOService(oService.getId(), xCommand.getName());

		if (command == null) {
			ConfigLob lob = new ConfigLob();
			lob.setValue(xstream.toXML(xCommand));
			persistorService.saveOrUpdate(lob);

			command = new Command();
			command.setName(xCommand.getName());
			command.setService(oService);
			command.setConfig(lob);
			command.setListView(false); //TODO
			persistorService.saveOrUpdate(command);

			logger.info("Command not found and created: {} for {}", xCommand.getName(), oService.getName());
		} else {
			command.getConfig().setValue(xstream.toXML(xCommand));
			persistorService.saveOrUpdate(command.getConfig());

			logger.info("Command [{}] updated for OService [{}]", xCommand.getName(), oService.getName());
		}
	}

	@Override
	public Object executeCommand(Long commandId, OServiceInstance serviceInstance, Map<String, Object> params, ICommandResultCallBack callBack) throws Exception {
		Long start = System.currentTimeMillis();

		Command command = load(commandId);
		CommandCenterResource resource = new CommandCenterResource(this, callBack);
		Long logId = commandLogService.insertLog(command, serviceInstance, params);

		logger.info("Start command execution: cmd=[{}] si=[{}] currentUser=[{}] logId=[{}]",
			command.getName(), serviceInstance, securityService.getCurrentUser(), logId);

		Exception error = null;
		try {
			return executeCommand(command, serviceInstance, params, resource);
		} catch (Exception e) {
			error = e;
			throw e;
		} finally {
			Long dur = ((System.currentTimeMillis() - start) / 1000);
			logger.info("Finish command execution: cmd=[{}] si=[{}] currentUser=[{}] dur=[{}] logId=[{}]",
				command.getName(), serviceInstance, securityService.getCurrentUser(), dur, logId);
			commandLogService.updateLog(logId, dur, error);
			resource.closeAll();
		}
	}

	@Override
	public Object callCommand(String command, OServiceInstance serviceInstance, Map<String, Object> params, CommandCenterResource resource) throws Exception {
		Command cmd = loadByNameAndOService(serviceInstance.getService().getId(), command);
		if (cmd == null) {
			throw new AresException(AresErrorCode.CommandNotFound, command);
		}
		return executeCommand(cmd, serviceInstance, params, resource);
	}

	@Override
	public void userPasswordUpdated(OServiceInstanceTargetVO targetVO, String username, String password) {
		OSIUserFVO fvo = new OSIUserFVO();
		fvo.setServiceInstance(Collections.singletonList(targetVO.getServiceInstance()));
		fvo.setUsername(username);
		List<OSIUser> search = osiUserService.search(fvo, 1, 1);

		if (search.size() == 1) {
			OSIUser user = search.get(0);
			osiUserService.saveOrUpdate(user, password);
		}
	}

	@Override
	public OServiceInstanceTargetVO findOf(Long serviceInstanceId, ERemoteMode remoteMode) {
		return serviceInstanceService.getTargetVOByServer(serviceInstanceId, remoteMode);
	}

	// ------------------------------

	private Object executeCommand(Command command, OServiceInstance serviceInstance, Map<String, Object> params, CommandCenterResource resource) throws Exception {
		logger.debug("CommandService.executeCommand: currentUser=[{}] cmd=[{}]", securityService.getCurrentUser(), command.getName());

		OServiceInstanceTargetVO targetVO = serviceInstanceService.getTargetVO(serviceInstance.getId());

		Map<String, Object> cmdParams = new HashMap<>();
		cmdParams.putAll(params);

		cmdParams.put("$cmd", new CommandCenter(targetVO, resource, params));
		//cmdParams.put("$params", params);
		cmdParams.put("target", targetVO);

		CmdRunner runner = new CmdRunner(command.getId(), command.getXCommand().getBody(), cmdParams);
		runner.run();

		Object result = runner.getResult();
		Exception error = runner.getError();
		if (error != null) {
			throw error;
		}

		return result;
	}

	private Command loadByNameAndOService(Long serviceId, String name) {
		Long cmdId = persistorService.createQueryBuilder()
			.addSelect("select ent.id")
			.addFrom(Command.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.addWhere("and ent.service.id = :serviceId")
			.addParam("serviceId", serviceId)
			.object();

		if (cmdId != null) {
			return load(cmdId);
		}

		return null;
	}

	private XCommand loadXCommand(Command command) {
		return (XCommand) xstream.fromXML(command.getConfig().getValue());
	}

// ------------------------------

	private class CmdRunner /*implements Runnable*/ {
		private Long cmdId;
		private String cmd;
		private Map<String, Object> params;
		private Object result;
		private Exception error;

		public CmdRunner(Long cmdId, String cmd, Map<String, Object> params) {
			this.cmdId = cmdId;
			this.cmd = cmd;
			this.params = params;
		}

		//@Override
		public void run() {
			try {
				IStringTemplate template = stringTemplateService.create("CMD_" + cmdId, cmd, TemplateEngineType.GroovyShell);
				result = template.process(params);

				persistorService.endSession();
			} catch (Exception e) {
				logger.error("CommandService.CmdRunner: ", e);
				error = e;
			}
		}

		public Object getResult() {
			return result;
		}

		public Exception getError() {
			return error;
		}
	}
}