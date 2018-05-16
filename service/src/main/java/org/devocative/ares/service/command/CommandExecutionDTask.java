package org.devocative.ares.service.command;

import org.devocative.ares.cmd.CommandOutput;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("arsCommandExecutionDTask")
public class CommandExecutionDTask extends DTask<CommandOutput> implements ICommandResultCallBack {
	private static final Logger logger = LoggerFactory.getLogger(CommandExecutionDTask.class);

	private CommandQVO commandQVO;

	@Autowired
	private ICommandService commandService;

	// ------------------------------

	@Override
	public void init() {
		commandQVO = (CommandQVO) getInputData();
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() throws Exception {
		logger.info("CommandExecutionDTask: currentUser=[{}] cmd=[{}]", getCurrentUser(), commandQVO.getCommandId());

		commandService.executeCommand(commandQVO, this);
	}

	@Override
	public void cancel() {
		commandService.cancelCommand(commandQVO.getLogId());
	}

	@Override
	public void onResult(CommandOutput lineOfResult) {
		sendResult(lineOfResult);
	}
}
