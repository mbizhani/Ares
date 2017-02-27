package org.devocative.ares.service.command;

import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.CommandLog;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.command.ICommandLogService;
import org.devocative.ares.vo.filter.command.CommandLogFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("arsCommandLogService")
public class CommandLogService implements ICommandLogService {
	private static final Logger logger = LoggerFactory.getLogger(CommandLogService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(CommandLog entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public CommandLog load(Long id) {
		return persistorService.get(CommandLog.class, id);
	}

	@Override
	public List<CommandLog> list() {
		return persistorService.list(CommandLog.class);
	}

	@Override
	public List<CommandLog> search(CommandLogFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(CommandLog.class, "ent")
			.applyFilter(CommandLog.class, "ent", filter)
			.setOrderBy("ent.creationDate desc")
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(CommandLogFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(CommandLog.class, "ent")
			.applyFilter(CommandLog.class, "ent", filter)
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
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	// ==============================

	@Override
	public void insertLog(Command command, OServiceInstance serviceInstance, Map<String, ?> params, String error) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, ?> entry : params.entrySet()) {
			builder
				.append(entry.getKey())
				.append("=")
				.append(entry.getValue())
				.append(";");
		}

		CommandLog log = new CommandLog();
		log.setCommand(command);
		log.setServiceInstance(serviceInstance);
		log.setParams(builder.toString());
		log.setSuccessful(error == null);
		log.setError(error);

		saveOrUpdate(log);
		persistorService.commitOrRollback();
	}
}