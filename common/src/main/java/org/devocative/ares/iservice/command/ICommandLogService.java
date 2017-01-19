//overwrite
package org.devocative.ares.iservice.command;

import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.command.CommandLog;
import org.devocative.ares.vo.filter.command.CommandLogFVO;
import org.devocative.demeter.entity.User;

import java.util.List;

public interface ICommandLogService {
	void saveOrUpdate(CommandLog entity);

	CommandLog load(Long id);

	List<CommandLog> list();

	List<CommandLog> search(CommandLogFVO filter, long pageIndex, long pageSize);

	long count(CommandLogFVO filter);

	List<Command> getCommandList();

	List<User> getCreatorUserList();

	// ==============================
}