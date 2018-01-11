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

import java.util.Arrays;

@Scope("prototype")
@Component("arsCommandExecutionDTask")
public class CommandExecutionDTask extends DTask<CommandOutput> implements ICommandResultCallBack {
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

			onResult(new CommandOutput(CommandOutput.Type.START));

			Object result = commandService.executeCommand(commandQVO, this);
			if (result != null) {
				if (result instanceof TabularVO) {
					onResult(new CommandOutput(CommandOutput.Type.TABULAR, result));
				} else if (result instanceof ConsoleResultProcessing) {
					ConsoleResultProcessing processing = (ConsoleResultProcessing) result;
					TabularVO build = processing.build();
					onResult(new CommandOutput(CommandOutput.Type.TABULAR, build));
				} else {
					String resultAsStr = result.toString();
					if (result.getClass().isArray()) {
						Object[] arr = (Object[]) result;
						resultAsStr = Arrays.toString(arr);
					}

					onResult(new CommandOutput(CommandOutput.Type.PROMPT, String.format("Final Return: %s", resultAsStr)));
				}
			}
		} catch (Exception e) {
			Throwable th = e;

			while (th.getCause() != null) {
				th = th.getCause();
			}

			String errMsg = String.format("%s (%s)",
				th.getMessage() != null ? th.getMessage().trim() : "-",
				th.getClass().getSimpleName());

			onResult(new CommandOutput(CommandOutput.Type.ERROR, errMsg));
			logger.error("CommandExecutionDTask: ", e);
		}

		onResult(new CommandOutput(CommandOutput.Type.FINISHED));
	}

	@Override
	public void cancel() throws Exception {
		commandService.cancelCommand(commandQVO.getLogId());
	}

	@Override
	public void onResult(CommandOutput lineOfResult) {
		sendResult(lineOfResult);
	}
}
