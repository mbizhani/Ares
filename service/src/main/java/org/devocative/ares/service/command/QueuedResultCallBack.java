package org.devocative.ares.service.command;

import org.devocative.adroit.ConfigUtil;
import org.devocative.ares.AresConfigKey;
import org.devocative.ares.cmd.CommandOutput;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class QueuedResultCallBack implements Runnable, ICommandResultCallBack {
	private static final Logger logger = LoggerFactory.getLogger(QueuedResultCallBack.class);

	private long lastExec = Long.MAX_VALUE;
	private BlockingQueue<CommandOutput> queue = new LinkedBlockingQueue<>();

	private final ICommandResultCallBack target;

	QueuedResultCallBack(ICommandResultCallBack target) {
		this.target = target;
	}

	@Override
	public void run() {
		final int DELAY = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedDuration);
		final Integer EXCEEDED = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedExceededSize);
		final Integer OMIT_LIMIT = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedExceededOmit);
		final Integer OMIT_SKIP = ConfigUtil.getInteger(AresConfigKey.SendOutDelayedExceededOmitSkip) - 1;

		logger.info("QueuedResultCallBack: delay=[{}] exceeded=[{}] omit.limit=[{}] omit.skip=[{}]",
			DELAY, EXCEEDED, OMIT_LIMIT, OMIT_SKIP);

		try {
			int skipToTrash = 0;

			while (true) {
				final CommandOutput lineOfResult = queue.take();

				target.onResult(lineOfResult);

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
					target.onResult(new CommandOutput(CommandOutput.Type.WARN, String.format("Fast Output Generation: [%s] lines omitted!", toTrash)));
					skipToTrash = OMIT_SKIP;
				}

				lastExec = System.currentTimeMillis();
			}
		} catch (InterruptedException e) {
			logger.error("QueuedResultCallBack: ", e);
		}
	}

	@Override
	public void onResult(CommandOutput lineOfResult) {
		queue.offer(lineOfResult);
	}
}
