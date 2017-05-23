package org.devocative.ares.iservice.command;

import org.devocative.ares.cmd.CommandCenterResource;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IEntityService;

import java.util.List;
import java.util.Map;

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

	void checkAndSave(OService oService, XCommand xCommand);

	Object executeCommand(Long commandId, OServiceInstance serviceInstance, Map<String, Object> params, ICommandResultCallBack callBack) throws Exception;

	Object callCommand(String command, OServiceInstance serviceInstance, Map<String, Object> params, CommandCenterResource resource) throws Exception;

	void userPasswordUpdated(OServiceInstanceTargetVO targetVO, String username, String password);

	void assertCurrentUser(String log);
}