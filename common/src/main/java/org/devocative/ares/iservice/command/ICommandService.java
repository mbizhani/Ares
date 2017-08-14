package org.devocative.ares.iservice.command;

import org.devocative.ares.cmd.CommandCenterResource;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IEntityService;
import org.devocative.demeter.iservice.task.ITaskResultCallback;

import java.util.List;

public interface ICommandService extends IEntityService<Command> {
	void saveOrUpdate(Command entity);

	Command load(Long id);

	List<Command> list();

	List<Command> search(CommandFVO filter, long pageIndex, long pageSize);

	long count(CommandFVO filter);

	List<OService> getServiceList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	void checkAndSave(OService oService, XCommand xCommand, Command command);

	void executeCommandTask(CommandQVO commandQVO, ITaskResultCallback callback);

	Object executeCommand(CommandQVO commandQVO, ICommandResultCallBack callBack) throws Exception;

	Object callCommand(CommandQVO commandQVO, CommandCenterResource resource) throws Exception;

	void userPasswordUpdated(OServiceInstanceTargetVO targetVO, String username, String password);

	void assertCurrentUser(String log);

	void clearCache();

	Command loadByNameAndOService(String name, Long serviceId);
}