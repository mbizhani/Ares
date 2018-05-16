package org.devocative.ares.service.command;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.adroit.xml.AdroitXStream;
import org.devocative.ares.AresConfigKey;
import org.devocative.ares.AresErrorCode;
import org.devocative.ares.AresException;
import org.devocative.ares.cmd.*;
import org.devocative.ares.cmd.CommandOutput.Type;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.CommandCfgLob;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
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
import org.devocative.ares.vo.TabularVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.ares.vo.xml.XParam;
import org.devocative.ares.vo.xml.XParamType;
import org.devocative.ares.vo.xml.XValidation;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.ICacheService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.DTaskResult;
import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.demeter.iservice.task.ITaskService;
import org.devocative.demeter.iservice.template.IStringTemplate;
import org.devocative.demeter.iservice.template.IStringTemplateService;
import org.devocative.demeter.iservice.template.TemplateEngineType;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("arsCommandService")
public class CommandService implements ICommandService, IMissedHitHandler<Long, Command> {
	private static final Logger logger = LoggerFactory.getLogger(CommandService.class);
	private static final String CMD_PREFIX_STR_TEMPLATE = "CMD_";

	private XStream xstream;
	private ICache<Long, Command> commandCache;
	private CCUtil singleInstOfUtil = new CCUtil();

	private final Map<Long, Boolean> runningCommands = new ConcurrentHashMap<>();
	private final Map<Long, AbstractExecutor> currentExecutorForCommands = new ConcurrentHashMap<>();
	private final Map<String, Integer> noOfRunningCommandsForServiceInstance = new ConcurrentHashMap<>();

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
			.addJoin("service", "ent.service", EJoinMode.LeftFetch)
			.addWhere("and ent.id = :id", "id", key)
			.object();

		command.setXCommand(loadXCommand(command));

		return command;
	}

	// ---------------

	@Override
	public void checkAndSave(OService oService, XCommand xCommand, Command command, Map<String, XValidation> validationMap) {
		//Command command = loadByNameAndOService(oService.getId(), xCommand.getName());

		if (xCommand.getParams() != null) {
			for (XParam xParam : xCommand.getParams()) {

				if (xParam.getType() != null && xParam.getType() != XParamType.String) {
					if (xParam.getValidRegex() != null || xParam.getValidRef() != null) {
						throw new RuntimeException(
							String.format("Can't use validation for non-string param: cmd=%s param=%s",
								xCommand.getName(), xParam.getName()));
					}
				}

				if (xParam.getValidRef() != null && xParam.getValidRegex() == null) {
					if (validationMap.containsKey(xParam.getValidRef())) {
						XValidation xValidation = validationMap.get(xParam.getValidRef());
						xParam.setValidRegex(xValidation.getRegex());
					} else {
						throw new RuntimeException(
							String.format("Validation reference not found: cmd=%s param=%s ref=%s",
								xCommand.getName(), xParam.getName(), xParam.getValidRef()));
					}
				}
			}
		}

		if (command == null) {
			CommandCfgLob lob = new CommandCfgLob();
			lob.setValue(xstream.toXML(xCommand));
			persistorService.saveOrUpdate(lob);

			command = new Command();
			command.setName(xCommand.getName());
			command.setService(oService);
			command.setConfig(lob);
			command.setViewMode(xCommand.getViewModeSafely());
			command.setExecLimit(xCommand.getExecLimit());
			command.setConfirm(xCommand.getConfirmSafely());
			persistorService.saveOrUpdate(command);

			prepCommandService.saveByCommand(command);

			logger.info("Command not found and created: {} for {}", xCommand.getName(), oService.getName());
		} else {
			command.setXCommand(xCommand);
			command.setViewMode(xCommand.getViewModeSafely());
			command.setExecLimit(xCommand.getExecLimit());
			command.setConfirm(xCommand.getConfirmSafely());
			saveOrUpdate(command);

			logger.info("Command [{}] updated for OService [{}]", xCommand.getName(), oService.getName());
		}
	}

	@Override
	public String executeCommandTask(CommandQVO commandQVO, ITaskResultCallback callback) {
		assertCommandExecLimit(commandQVO.getCommandId(), commandQVO.getServiceInstanceId());

		Long logId = commandLogService.insertLog(commandQVO.getCommandId(), commandQVO.getServiceInstanceId(),
			commandQVO.getParams(), commandQVO.getPrepCommandId());
		commandQVO.setLogId(logId);
		runningCommands.put(logId, true);

		DTaskResult start = taskService.start(CommandExecutionDTask.class, logId, commandQVO, callback);
		return start.getTaskInstance().getKey();
	}

	/*
	Main Command Exec Method
	 */
	@Override
	public void executeCommand(CommandQVO commandQVO, ICommandResultCallBack callBack) throws Exception {
		final Command command = load(commandQVO.getCommandId());
		final OServiceInstance serviceInstance = serviceInstanceService.load(commandQVO.getServiceInstanceId());
		final Long logId = commandQVO.getLogId();

		final StringBuilder buff = new StringBuilder();

		Thread thread = null;
		if (ConfigUtil.getBoolean(AresConfigKey.SendOutDelayedEnabled)) {
			QueuedResultCallBack queued = new QueuedResultCallBack(callBack);
			thread = new Thread(queued, Thread.currentThread().getName() + "-OUT");
			thread.start();

			callBack = new BufferedResultCallBack(queued, buff);
		} else {
			callBack = new BufferedResultCallBack(callBack, buff);
		}

		CommandCenter.create();
		CommandCenterResource.create(this, serverService, serviceInstanceService, callBack, logId);

		final Long start = System.currentTimeMillis();
		final UserVO curUser = securityService.getCurrentUser();

		logger.info("Start command execution: cmd=[{}] si=[{}] currentUser=[{}] logId=[{}]",
			command.getName(), serviceInstance, curUser, logId);

		String errMsg = null;
		try {
			callBack.onResult(new CommandOutput(Type.START));
			final Object result = executeCommand(command, commandQVO);

			if (result != null) {
				if (result instanceof TabularVO) {
					callBack.onResult(new CommandOutput(Type.TABULAR, result));
				} else if (result instanceof ConsoleResultProcessing) {
					ConsoleResultProcessing processing = (ConsoleResultProcessing) result;
					TabularVO build = processing.build();
					callBack.onResult(new CommandOutput(Type.TABULAR, build));
				} else {
					String resultAsStr = result.toString();
					if (result.getClass().isArray()) {
						Object[] arr = (Object[]) result;
						resultAsStr = Arrays.toString(arr);
					}
					callBack.onResult(new CommandOutput(Type.PROMPT, String.format("Final Return: %s", resultAsStr)));
				}
			}
		} catch (Exception e) {
			logger.error("CommandService.executeCommand ", e);

			Throwable th = e;
			while (th.getCause() != null) {
				th = th.getCause();
			}

			errMsg = String.format("%s (%s)",
				th.getMessage() != null ? th.getMessage().trim() : "-",
				th.getClass().getSimpleName());

			callBack.onResult(new CommandOutput(Type.ERROR, errMsg));
		} finally {
			callBack.onResult(new CommandOutput(Type.FINISHED));

			runningCommands.remove(logId);
			currentExecutorForCommands.remove(logId);
			countDownCommandExec(commandQVO.getCommandId(), commandQVO.getServiceInstanceId());
			CommandCenterResource.close();
			CommandCenter.close();

			final Long dur = ((System.currentTimeMillis() - start) / 1000);
			logger.info("Finish command execution: cmd=[{}] si=[{}] currentUser=[{}] dur=[{}] logId=[{}]",
				command.getName(), serviceInstance, curUser, dur, logId);
			commandLogService.updateLog(logId, dur, errMsg, buff.toString());

			if (thread != null) {
				thread.join();
			}
		}
	}

	@Override
	public Object callCommand(CommandQVO commandQVO) throws Exception {
		OServiceInstance serviceInstance = serviceInstanceService.load(commandQVO.getServiceInstanceId());
		Command cmd = loadByNameAndOService(commandQVO.getCommandName(), serviceInstance.getService().getId());

		if (cmd == null) {
			cmd = loadByName(commandQVO.getCommandName());
		}

		if (cmd == null) {
			throw new AresException(AresErrorCode.CommandNotFound, commandQVO.getCommandName());
		}

		if (ConfigUtil.getBoolean(AresConfigKey.ConsiderInnerCommandForLimit)) {
			assertCommandExecLimit(cmd.getId(), serviceInstance.getId());
		}

		try {
			return executeCommand(cmd, commandQVO);
		} finally {
			if (ConfigUtil.getBoolean(AresConfigKey.ConsiderInnerCommandForLimit)) {
				countDownCommandExec(cmd.getId(), serviceInstance.getId());
			}
		}
	}

	@Override
	public void cancelCommandTask(String key) {
		taskService.stop(key);
	}

	@Override
	public void cancelCommand(Long logId) {
		if (runningCommands.containsKey(logId)) {
			runningCommands.put(logId, false);

			if (currentExecutorForCommands.containsKey(logId)) {
				try {
					currentExecutorForCommands.get(logId).cancel();
				} catch (Exception e) {
					logger.error("cancelCommand: {}", logId, e);
					throw new RuntimeException(e); //TODO
				} finally {
					currentExecutorForCommands.remove(logId);
				}

			}
		} else {
			throw new RuntimeException("The command is not running anymore!"); //TODO
		}
	}

	@Override
	public boolean isOkToContinue(Long logId) {
		return runningCommands.get(logId);
	}

	@Override
	public void setCurrentExecutor(Long logId, AbstractExecutor current) {
		currentExecutorForCommands.put(logId, current);
	}

	@Override
	public void userPasswordUpdated(OServiceInstanceTargetVO targetVO, String username, String password) {
		OSIUserFVO fvo = new OSIUserFVO();
		fvo.setServiceInstance(Collections.singletonList(targetVO.getServiceInstance()));
		fvo.setUsername(username);
		List<OSIUser> search = osiUserService.search(fvo, 1, 1);

		if (search.size() == 1) {
			OSIUser user = search.get(0);
			osiUserService.saveOrUpdate(user, password, false);
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
			.addWhere("and ent.name = :name", "name", name)
			.addWhere("and ent.service.id = :serviceId", "serviceId", serviceId)
			.object();

		if (cmdId != null) {
			return load(cmdId);
		}

		return null;
	}

	// ------------------------------

	// Main Execute Command
	private Object executeCommand(Command command, CommandQVO commandQVO) throws Exception {
		logger.debug("CommandService.executeCommand: currentUser=[{}] cmd=[{}]", securityService.getCurrentUser(), command.getName());

		if (!command.getEnabled()) {
			logger.warn("Disabled Command: [{}]", command.getName());
			throw new RuntimeException("Disabled Command: " + command.getName()); //TODO
		}

		Map<String, Object> params = commandQVO.getParams();
		XCommand xCommand = command.getXCommand();
		for (XParam xParam : xCommand.getParams()) {
			if (xParam.getDefaultValue() != null && !params.containsKey(xParam.getName())) {
				params.put(xParam.getName(), xParam.getDefaultValueObject());
			}

			/*
			NOTE When a command call another command and pass the parameters such as 'Server' or 'Service, the first command
			     has loaded the real OServer or OServiceInstanceTargetVO and add them to real params. So the type must be checked!
			 */
			Object paramValue = params.get(xParam.getName());
			if (paramValue != null) {
				if (xParam.getType() == null || xParam.getType() == XParamType.String) {
					if (xParam.getValidRegex() != null) {
						String paramValueAsStr = (String) paramValue;
						if (!paramValueAsStr.matches(xParam.getValidRegex())) {
							throw new RuntimeException(String.format("Invalid string input: cmd=%s param=%s regex=%s",
								xCommand.getName(), xParam.getName(), xParam.getValidRegex()));
						}
					}
				} else if (xParam.getType() == XParamType.Server) {
					if (paramValue instanceof Long) {
						OServer oServer = serverService.load((Long) paramValue);
						params.put(xParam.getName(), oServer);
					} else if (!(paramValue instanceof OServer)) {
						throw new RuntimeException(String.format("Invalid param 'OServer' type for [%s]: %s", xParam.getName(), paramValue.getClass()));
					}
				} else if (xParam.getType() == XParamType.Service) {
					if (paramValue instanceof Long) {
						Long otherServiceInstanceId = (Long) paramValue;
						OServiceInstanceTargetVO otherServiceInstance = serviceInstanceService.getTargetVO(otherServiceInstanceId);
						params.put(xParam.getName(), otherServiceInstance);
					} else if (!(paramValue instanceof OServiceInstanceTargetVO)) {
						throw new RuntimeException(String.format("Invalid param 'OServiceInstanceTargetVO' type for [%s]: %s", xParam.getName(), paramValue.getClass()));
					}
				}
			} else if (xParam.getRequired() != null && xParam.getRequired()) {
				throw new RuntimeException(String.format("Required param value: cmd=%s param=%s",
					xCommand.getName(), xParam.getName()));
			}
		}

		OServiceInstanceTargetVO targetVO;
		if (commandQVO.getOsiUserId() == null) {
			targetVO = serviceInstanceService.getTargetVO(commandQVO.getServiceInstanceId());
		} else {
			targetVO = serviceInstanceService.getTargetVOByUser(commandQVO.getOsiUserId());
		}

		CommandCenter.push(targetVO, params);

		Map<String, Object> cmdParams = new HashMap<>();
		cmdParams.putAll(params);
		cmdParams.put("target", targetVO);
		cmdParams.put("$util", singleInstOfUtil); // TODO DEPRECATED!
		cmdParams.put(IStringTemplate.GROOVY_DELEGATE_KEY, new OtherCommandsWrapper(new MainCommandDSL()));

		CmdRunner runner = new CmdRunner(command.getId(), xCommand.getBody(), cmdParams);
		runner.run();

		CommandCenter.pop();

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
			.addWhere("and ent.name = :name", "name", name)
			.object();

		if (cmdId != null) {
			return load(cmdId);
		}

		return null;
	}

	private synchronized void assertCommandExecLimit(Long cmdId, Long serviceInstId) {
		String cmdSrvInstKey = String.format("%s_%s", cmdId, serviceInstId);
		if (!noOfRunningCommandsForServiceInstance.containsKey(cmdSrvInstKey)) {
			noOfRunningCommandsForServiceInstance.put(cmdSrvInstKey, 0);
		}

		int count = noOfRunningCommandsForServiceInstance.get(cmdSrvInstKey);
		count++;

		int limit = ConfigUtil.getInteger(AresConfigKey.GeneralCommandExecLimit);
		Command command = load(cmdId);
		if (command.getExecLimit() != null) {
			limit = Math.min(limit, command.getExecLimit());
		}

		if (count > limit) {
			throw new AresException(AresErrorCode.CommandExecLimitViolation, command.getName());
		} else {
			noOfRunningCommandsForServiceInstance.put(cmdSrvInstKey, count);
		}
	}

	private synchronized void countDownCommandExec(Long cmdId, Long serviceInstId) {
		String cmdSrvInstKey = String.format("%s_%s", cmdId, serviceInstId);
		int count = noOfRunningCommandsForServiceInstance.get(cmdSrvInstKey);
		noOfRunningCommandsForServiceInstance.put(cmdSrvInstKey, --count);
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
				logger.warn("CommandService.CmdRunner: err={}", e.getMessage());
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