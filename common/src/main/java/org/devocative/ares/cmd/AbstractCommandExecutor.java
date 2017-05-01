package org.devocative.ares.cmd;

import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommandExecutor implements Runnable {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractCommandExecutor.class);

	protected OServiceInstanceTargetVO targetVO;
	protected ICommandResultCallBack resultCallBack;
	protected String prompt;
	protected String command;

	// ---------------

	private Object result;
	private Exception exception;

	// ------------------------------

	public AbstractCommandExecutor(OServiceInstanceTargetVO targetVO, ICommandResultCallBack resultCallBack, String prompt, String command) {
		this.targetVO = targetVO;
		this.resultCallBack = resultCallBack;
		this.prompt = prompt;
		this.command = command;
	}

	// ------------------------------

	@Override
	public final void run() {
		try {
			execute();
		} catch (Exception e) {
			exception = e;
		}
	}

	public final Exception getException() {
		return exception;
	}

	public final boolean hasException() {
		return exception != null;
	}

	public final Object getResult() {
		return result;
	}

	protected final void setResult(Object result) {
		this.result = result;
	}

	// ------------------------------

	protected abstract void execute() throws Exception;
}
