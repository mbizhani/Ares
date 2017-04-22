package org.devocative.ares.service.command;

import org.devocative.ares.cmd.CommandOutput;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.demeter.iservice.task.DTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("arsCommandExecutionDTask")
public class CommandExecutionDTask extends DTask implements ICommandResultCallBack {

	private CommandQVO commandQVO;

	@Autowired
	private ICommandService commandService;

	@Override
	public void init() {
		commandQVO = (CommandQVO) getInputData();
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() {
		try {
			commandService.executeCommand(commandQVO.getCommandId(), commandQVO.getServiceInstance(), commandQVO.getParams(), this);
		} catch (Exception e) {
			onResult(new CommandOutput(CommandOutput.Type.ERROR, e.getMessage()));
		}
	}

	@Override
	public void onResult(CommandOutput lineOfResult) {
		setResult(lineOfResult);
	}
}
