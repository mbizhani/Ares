//overwrite
package org.devocative.ares.service.command;

import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("arsCommandService")
public class CommandService implements ICommandService {

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(Command entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public Command load(Long id) {
		return persistorService.get(Command.class, id);
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
}