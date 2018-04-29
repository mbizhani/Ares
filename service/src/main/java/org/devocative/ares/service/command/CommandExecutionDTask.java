package org.devocative.ares.service.command;

import org.devocative.adroit.ConfigUtil;
import org.devocative.ares.AresConfigKey;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Scope("prototype")
@Component("arsCommandExecutionDTask")
public class CommandExecutionDTask extends DTask<CommandOutput> implements ICommandResultCallBack {
	private static final Logger logger = LoggerFactory.getLogger(CommandExecutionDTask.class);

	private CommandQVO commandQVO;

	private ICommandResultCallBack resultCallBack = this;


	@Autowired
	private ICommandService commandService;

	// ------------------------------

	public CommandExecutionDTask setResultCallBack(ICommandResultCallBack resultCallBack) {
		this.resultCallBack = resultCallBack;
		return this;
	}

	// ---------------

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

		Thread thread = null;
		if (ConfigUtil.getBoolean(AresConfigKey.SendOutDelayedEnabled)) {
			CommandDelayedResultCallBack callBack = new CommandDelayedResultCallBack();

			thread = new Thread(callBack, Thread.currentThread().getName() + "-OUT");
			thread.start();

			resultCallBack = callBack;
		}

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

		if (thread != null) {
			thread.join();
		}
	}

	@Override
	public void cancel() {
		commandService.cancelCommand(commandQVO.getLogId());
	}

	@Override
	public void onResult(CommandOutput lineOfResult) {
		resultCallBack.onResult(lineOfResult);
	}

	// ------------------------------

	private class CommandDelayedResultCallBack implements Runnable, ICommandResultCallBack {
		private BlockingQueue<CommandOutput> queue = new LinkedBlockingQueue<>();

		private long lastExec = Long.MAX_VALUE;

		@Override
		public void run() {
			final int DELAY = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedDuration);
			final Integer EXCEEDED = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedExceededSize);
			final Integer OMIT_LIMIT = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedExceededOmit);
			final Integer OMIT_SKIP = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedExceededOmitSkip) - 1;

			logger.info("CommandDelayedResultCallBack: delay=[{}] exceeded=[{}] omit.limit=[{}] omit.skip=[{}]",
				DELAY, EXCEEDED, OMIT_LIMIT, OMIT_SKIP);

			try {
				int skipToTrash = 0;

				while (true) {
					final CommandOutput lineOfResult = queue.take();

					sendResult(lineOfResult);

					if (lineOfResult.getType() == CommandOutput.Type.FINISHED) {
						break;
					}

					final long diff = System.currentTimeMillis() - lastExec;
					if (diff < DELAY && diff > -1) {
						Thread.sleep(DELAY - diff);
					}

					int toTrash = 0;
					while (queue.size() > EXCEEDED && toTrash < OMIT_LIMIT && skipToTrash == 0) {
						queue.take();
						toTrash++;
					}

					if (skipToTrash > 0) {
						skipToTrash--;
					}

					if (toTrash > 0) {
						sendResult(new CommandOutput(CommandOutput.Type.WARN, String.format("Fast Output Generation: [%s] lines omitted!", toTrash)));
						skipToTrash = OMIT_SKIP;
					}

					lastExec = System.currentTimeMillis();
				}
			} catch (InterruptedException e) {
				logger.error("CommandDelayedResultCallBack: ", e);
			}
		}

		@Override
		public void onResult(CommandOutput lineOfResult) {
			queue.offer(lineOfResult);
		}
	}
}
