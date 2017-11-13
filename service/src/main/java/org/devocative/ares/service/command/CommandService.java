package org.devocative.ares.service.command;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.adroit.xml.AdroitXStream;
import org.devocative.ares.AresErrorCode;
import org.devocative.ares.AresException;
import org.devocative.ares.cmd.CCUtil;
import org.devocative.ares.cmd.CommandCenter;
import org.devocative.ares.cmd.CommandCenterResource;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.ConfigLob;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.iservice.command.ICommandLogService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.command.IPrepCommandService;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.service.command.dsl.MainCommandDSL;
import org.devocative.ares.service.command.dsl.OtherCommandsWrapper;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.ares.vo.xml.XParam;
import org.devocative.ares.vo.xml.XParamType;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.ICacheService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.demeter.iservice.task.ITaskService;
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
	private static final String CMD_PREFIX_STR_TEMPLATE = "CMD_";

	private XStream xstream;
	private ICache<Long, Command> commandCache;
	private CCUtil singleInstOfUtil = new CCUtil();

	// ---------------

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private ICacheService cacheService;

	@Autowired
	private IStringTemplateService stringTemplateService;

	@Autowired
	private ITaskService taskService;

	@Autowired
	private IOServiceInstanceService serviceInstanceService;

	@Autowired
	private IOSIUserService osiUserService;

	@Autowired
	private ICommandLogService commandLogService;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private IOServerService serverService;

	@Autowired
	private IPrepCommandService prepCommandService;

	// ------------------------------

	@Override
	public void saveOrUpdate(Command entity) {
		entity.getConfig().setValue(xstream.toXML(entity.getXCommand()));
		persistorService.saveOrUpdate(entity.getConfig());
		persistorService.saveOrUpdate(entity);

		commandCache.remove(entity.getId());
		stringTemplateService.clearCacheFor(CMD_PREFIX_STR_TEMPLATE + entity.getId());
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

	// ---------------

	@Override
	public void checkAndSave(OService oService, XCommand xCommand, Command command) {
		//Command command = loadByNameAndOService(oService.getId(), xCommand.getName());

		if (command == null) {
			ConfigLob lob = new ConfigLob();
			lob.setValue(xstream.toXML(xCommand));
			persistorService.saveOrUpdate(lob);

			command = new Command();
			command.setName(xCommand.getName());
			command.setService(oService);
			command.setConfig(lob);
			command.setListView(xCommand.getListViewSafely());
			persistorService.saveOrUpdate(command);

			prepCommandService.saveByCommand(command);

			logger.info("Command not found and created: {} for {}", xCommand.getName(), oService.getName());
		} else {
			command.getConfig().setValue(xstream.toXML(xCommand));
			persistorService.saveOrUpdate(command.getConfig());

			logger.info("Command [{}] updated for OService [{}]", xCommand.getName(), oService.getName());
		}
	}

	@Override
	public void executeCommandTask(CommandQVO commandQVO, ITaskResultCallback callback) {
		taskService.start(CommandExecutionDTask.class, commandQVO, callback);
	}

	@Override
	public Object executeCommand(CommandQVO commandQVO, ICommandResultCallBack callBack) throws Exception {
		Long start = System.currentTimeMillis();

		Command command = load(commandQVO.getCommandId());
		CommandCenterResource resource = new CommandCenterResource(this, serverService, serviceInstanceService, callBack);
		Long logId = commandLogService.insertLog(command, commandQVO.getServiceInstance(), commandQVO.getParams(), commandQVO.getPrepCommandId());

		logger.info("Start command execution: cmd=[{}] si=[{}] currentUser=[{}] logId=[{}]",
			command.getName(), commandQVO.getServiceInstance(), securityService.getCurrentUser(), logId);

		Exception error = null;
		try {
			return executeCommand(command, commandQVO, resource);
		} catch (Exception e) {
			error = e;
			throw e;
		} finally {
			Long dur = ((System.currentTimeMillis() - start) / 1000);
			logger.info("Finish command execution: cmd=[{}] si=[{}] currentUser=[{}] dur=[{}] logId=[{}]",
				command.getName(), commandQVO.getServiceInstance(), securityService.getCurrentUser(), dur, logId);
			commandLogService.updateLog(logId, dur, error);
			resource.closeAll();
		}
	}

	@Override
	public Object callCommand(CommandQVO commandQVO, CommandCenterResource resource) throws Exception {
		Command cmd = loadByNameAndOService(commandQVO.getCommandName(), commandQVO.getServiceInstance().getService().getId());

		if (cmd == null) {
			cmd = loadByName(commandQVO.getCommandName());
		}

		if (cmd == null) {
			throw new AresException(AresErrorCode.CommandNotFound, commandQVO.getCommandName());
		}
		return executeCommand(cmd, commandQVO, resource);
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
	public void assertCurrentUser(String log) {
		if (securityService.getCurrentUser() == null) {
			throw new RuntimeException("No Current User: log = " + log);
		}
	}

	@Override
	public void clearCache() {
		for (Long key : commandCache.getKeys()) {
			stringTemplateService.clearCacheFor(CMD_PREFIX_STR_TEMPLATE + key);
		}
		commandCache.clear();
	}

	@Override
	public Command loadByNameAndOService(String name, Long serviceId) {
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

	// ------------------------------

	private Object executeCommand(Command command, CommandQVO commandQVO, CommandCenterResource resource) throws Exception {
		logger.debug("CommandService.executeCommand: currentUser=[{}] cmd=[{}]", securityService.getCurrentUser(), command.getName());

		if (!command.getEnabled()) {
			logger.warn("Disabled Command: [{}]", command.getName());
			throw new RuntimeException("Disabled Command: " + command.getName());
		}

		Map<String, Object> params = commandQVO.getParams();
		XCommand xCommand = command.getXCommand();
		for (XParam xParam : xCommand.getParams()) {
			if (xParam.getDefaultValue() != null && !params.containsKey(xParam.getName())) {
				params.put(xParam.getName(), xParam.getDefaultValueObject());
			}

			if (xParam.getType() == XParamType.Server) {
				Object paramValue = params.get(xParam.getName());
				OServer oServer = serverService.load(Long.valueOf(paramValue.toString()));
				params.put(xParam.getName(), oServer);
			}
		}

		OServiceInstanceTargetVO targetVO;
		if (commandQVO.getOsiUserId() == null) {
			targetVO = serviceInstanceService.getTargetVO(commandQVO.getServiceInstance().getId());
		} else {
			targetVO = serviceInstanceService.getTargetVOByUser(commandQVO.getOsiUserId());
		}

		Map<String, Object> cmdParams = new HashMap<>();
		cmdParams.putAll(params);

		CommandCenter commandCenter = new CommandCenter(targetVO, resource, params);
		cmdParams.put("target", targetVO);
		cmdParams.put("$util", singleInstOfUtil);
		cmdParams.put("$cmd", commandCenter);
		cmdParams.put("DELEGATE", new OtherCommandsWrapper(new MainCommandDSL(commandCenter)));

		CmdRunner runner = new CmdRunner(command.getId(), xCommand.getBody(), cmdParams);
		runner.run();

		Object result = runner.getResult();
		Exception error = runner.getError();
		if (error != null) {
			throw error;
		}

		return result;
	}

	private XCommand loadXCommand(Command command) {
		return (XCommand) xstream.fromXML(command.getConfig().getValue());
	}

	private Command loadByName(String name) {
		Long cmdId = persistorService.createQueryBuilder()
			.addSelect("select ent.id")
			.addFrom(Command.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.object();

		if (cmdId != null) {
			return load(cmdId);
		}

		return null;
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
				//TODO
				String b4 = "String.metaClass.find = {String regEx, int group, String defVal = null -> return $util.find(delegate, regEx, group, defVal)}\n";
				IStringTemplate template = stringTemplateService.create(CMD_PREFIX_STR_TEMPLATE + cmdId, b4 + cmd, TemplateEngineType.GroovyDelegatingScript);
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