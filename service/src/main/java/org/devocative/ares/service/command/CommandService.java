package org.devocative.ares.service.command;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.adroit.xml.AdroitXStream;
import org.devocative.ares.cmd.CommandCenter;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.ConfigLob;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.command.ICommandLogService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.service.oservice.OServiceInstanceService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
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
import java.sql.Connection;
import java.sql.DriverManager;
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
	private OServiceInstanceService serviceInstanceService;

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
		Command command = loadByNameAndOService(oService, xCommand.getName());

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
	public Object executeCommand(Long commandId, OServiceInstance serviceInstance, Map<String, String> params, ICommandResultCallBack callBack) throws Exception {
		Object result = null;
		Exception error = null;

		Command command = load(commandId);
		XCommand xCommand = command.getXCommand();

		try {
			OServiceInstanceTargetVO targetVO = serviceInstanceService.getTargetVO(serviceInstance.getId());

			logger.info("ExecuteCommand: cmd=[{}] si=[{}] user=[{}] executor=[{}] params=#[{}]",
				command.getName(), serviceInstance, securityService.getCurrentUser(), targetVO.getUsername(), targetVO.getProp());

			Map<String, Object> cmdParams = new HashMap<>();
			cmdParams.putAll(params);

			CommandCenter center = new CommandCenter(this, targetVO, callBack);
			cmdParams.put("$cmd", center);
			cmdParams.put("target", targetVO);

			CmdRunner runner = new CmdRunner(command.getId(), xCommand.getBody(), cmdParams);
			Thread th = new Thread(runner);
			th.start();
			th.join();

			result = runner.getResult();
			error = runner.getError();
			if (error != null) {
				throw error;
			}
		} finally {
			commandLogService.insertLog(command, serviceInstance, params, error);
		}

		return result;
	}

	@Override
	public Connection getConnection(OServiceInstanceTargetVO targetVO) {
		try {
			Class.forName(targetVO.getProp().get("driver"));
			return DriverManager.getConnection(targetVO.getConnection(), targetVO.getUsername(), targetVO.getPassword());
		} catch (Exception e) {
			logger.error("getConnection", e);
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	private Command loadByNameAndOService(OService oService, String name) {
		return persistorService.createQueryBuilder()
			.addFrom(Command.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.addWhere("and ent.service.id = :serviceId")
			.addParam("serviceId", oService.getId())
			.object();
	}

	private XCommand loadXCommand(Command command) {
		return (XCommand) xstream.fromXML(command.getConfig().getValue());
	}

	// ------------------------------

	private class CmdRunner implements Runnable {
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

		@Override
		public void run() {
			try {
				IStringTemplate template = stringTemplateService.create("CMD_" + cmdId, cmd, TemplateEngineType.GroovyShell);
				result = template.process(params);
			} catch (Exception e) {
				logger.error("CmdRunner: ", e);
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