package org.devocative.ares.service.command;

import org.devocative.ares.entity.command.Command;
import org.devocative.demeter.iservice.task.DTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("arsCommandExecutionDTask")
public class CommandExecutionDTask extends DTask {
	private Command command;

	@Override
	public void init() {
		command = (Command) getInputData();
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() {

	}
}
