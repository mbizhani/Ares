package org.devocative.ares.service.command;

import org.devocative.adroit.CalendarUtil;
import org.devocative.ares.cmd.CommandOutput;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class BufferedResultCallBack implements ICommandResultCallBack {
	private static final Logger logger = LoggerFactory.getLogger(BufferedResultCallBack.class);

	private final ICommandResultCallBack target;
	private final StringBuilder buffer;
	private long startTime;

	// ------------------------------

	BufferedResultCallBack(ICommandResultCallBack target, StringBuilder buffer) {
		this.target = target;
		this.buffer = buffer;
	}

	// ------------------------------

	@Override
	public void onResult(CommandOutput result) {
		try {
			switch (result.getType()) {
				case START:
					startTime = System.currentTimeMillis();

					buffer
						.append("[ 00:00:00 START - ")
						.append(CalendarUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"))
						.append("]\n");
					break;
				case PROMPT:
				case TABULAR:
				case LINE:
				case ERROR:
				case WARN:
				case FINISHED:
					buffer
						.append("[ ")
						.append(elapsed())
						.append(" ")
						.append(result.getType())
						.append(" ] ")
						.append(result.getOutput())
						.append("\n");
					break;
			}
		} catch (Exception e) {
			logger.warn("BufferedResultCallBack", e);
		}

		target.onResult(result);
	}

	// ------------------------------

	private String elapsed() {
		long diffInSeconds = (System.currentTimeMillis() - startTime) / 1000;
		int h = (int) (diffInSeconds / 3600);
		int m = (int) ((diffInSeconds - h * 3600) / 60);
		int s = (int) (diffInSeconds - h * 3600 - m * 60);
		return String.format("%02d:%02d:%02d", h, m, s);
	}
}
