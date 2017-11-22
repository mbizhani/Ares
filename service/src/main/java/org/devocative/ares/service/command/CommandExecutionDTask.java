package org.devocative.ares.service.command;

import org.devocative.ares.cmd.CommandOutput;
import org.devocative.ares.cmd.ConsoleResultProcessing;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.TabularVO;
import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("arsCommandExecutionDTask")
public class CommandExecutionDTask extends DTask implements ICommandResultCallBack {
	private static final Logger logger = LoggerFactory.getLogger(CommandExecutionDTask.class);

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
			logger.info("CommandExecutionDTask: currentUser=[{}] cmd=[{}]", getCurrentUser(), commandQVO.getCommandId());

			Object result = commandService.executeCommand(commandQVO, this);
			if (result != null) {
				if (result instanceof TabularVO) {
					onResult(new CommandOutput(CommandOutput.Type.TABULAR, result));
				} else if (result instanceof ConsoleResultProcessing) {
					ConsoleResultProcessing processing = (ConsoleResultProcessing) result;
					TabularVO build = processing.build();
					onResult(new CommandOutput(CommandOutput.Type.TABULAR, build));
				} else {
					onResult(new CommandOutput(CommandOutput.Type.PROMPT, String.format("Final Result: %s", result.toString())));
				}
			} else {
				onResult(new CommandOutput(CommandOutput.Type.PROMPT, "Finished"));
			}
		} catch (Exception e) {
			onResult(new CommandOutput(CommandOutput.Type.ERROR, e.getMessage()));
			logger.error("CommandExecutionDTask:", e);
		}
	}

	@Override
	public void onResult(CommandOutput lineOfResult) {
		sendResult(lineOfResult);
	}
}
