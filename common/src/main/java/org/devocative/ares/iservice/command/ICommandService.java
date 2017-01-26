package org.devocative.ares.iservice.command;

import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.demeter.entity.User;

import java.util.List;

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
}