package org.devocative.ares.iservice.command;

import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.demeter.entity.User;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface ICommandService {
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

	Object executeCommand(String command, OServiceInstance serviceInstance, Map<String, String> params, ICommandResultCallBack callBack) throws Exception;

	Object executeCommand(Long commandId, OServiceInstance serviceInstance, Map<String, String> params, ICommandResultCallBack callBack) throws Exception;

	Connection getConnection(OServiceInstanceTargetVO targetVO);
}