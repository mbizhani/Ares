package org.devocative.ares.service.command;

import org.devocative.adroit.date.EUniDateField;
import org.devocative.adroit.date.UniDate;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.CommandLog;
import org.devocative.ares.entity.command.ECommandResult;
import org.devocative.ares.entity.command.PrepCommand;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.command.ICommandLogService;
import org.devocative.ares.iservice.command.IPrepCommandService;
import org.devocative.ares.vo.filter.command.CommandLogFVO;
import org.devocative.demeter.entity.EFileStorage;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.FileStoreHandler;
import org.devocative.demeter.iservice.IApplicationLifecycle;
import org.devocative.demeter.iservice.IFileStoreService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@Service("arsCommandLogService")
public class CommandLogService implements ICommandLogService, IApplicationLifecycle {
	private static final Logger logger = LoggerFactory.getLogger(CommandLogService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IPrepCommandService prepCommandService;

	@Autowired
	private IFileStoreService fileStoreService;

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
	public List<PrepCommand> getPrepCommandList() {
		return persistorService.list(PrepCommand.class);
	}

	@Override
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	// ==============================

	@Transactional
	@Override
	public void init() {
		persistorService
			.createQueryBuilder()
			.addSelect("update CommandLog ent set ent.duration=-1, ent.result=:res where ent.duration is null")
			.addParam("res", ECommandResult.UNKNOWN)
			.update();
	}

	@Override
	public void shutdown() {
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Fourth;
	}

	// ---------------

	@Transactional
	@Override
	public Long insertLog(Long commandId, Long serviceInstanceId, Map<String, ?> params, Long prepCommandId) {
		CommandLog log = new CommandLog();
		log.setCommandId(commandId);
		log.setServiceInstanceId(serviceInstanceId);
		log.setParams(prepCommandService.convertParamsToString(params));
		log.setResult(ECommandResult.RUNNING);
		log.setPrepCommandId(prepCommandId);

		saveOrUpdate(log);

		return log.getId();
	}

	@Transactional
	@Override
	public void updateLog(Long logId, Long duration, String errMsg, String commandOutput) {
		CommandLog log = load(logId);
		String logFileId = null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(baos);
			gzip.write(commandOutput.getBytes());
			gzip.close();

			final String logFileName = String.format("%s-%s.gz", log.getCommand().getName(), logId);
			final FileStoreHandler handler = fileStoreService.create(logFileName, EFileStorage.DISK, EMimeType.GZIP,
				UniDate.now().update(EUniDateField.DATE, 7).toDate(), String.valueOf(logId));
			handler.write(baos.toByteArray());
			handler.close();

			logFileId = handler.getFileStore().getFileId();
		} catch (IOException e) {
			logger.error("updateLog", e);
		} finally {
			log.setResult(errMsg == null ? ECommandResult.SUCCESSFUL : ECommandResult.ERROR);
			log.setDuration(duration);
			log.setError(errMsg);
			log.setLogFileId(logFileId);

			saveOrUpdate(log);
		}
	}
}