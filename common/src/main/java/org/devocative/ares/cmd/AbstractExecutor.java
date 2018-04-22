package org.devocative.ares.cmd;

import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExecutor implements Runnable {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractCommandExecutor.class);

	protected final OServiceInstanceTargetVO targetVO;
	protected final CommandCenterResource resource;

	// ---------------

	private boolean force = false;
	private boolean admin = false;
	private Object result;
	private Exception exception;

	// ------------------------------

	public AbstractExecutor(OServiceInstanceTargetVO targetVO, CommandCenterResource resource) {
		this.targetVO = targetVO;
		this.resource = resource;
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

	// ---------------

	public boolean isForce() {
		return force;
	}

	public AbstractExecutor setForce(boolean force) {
		this.force = force;
		return this;
	}

	public boolean isAdmin() {
		return admin;
	}

	public AbstractExecutor setAdmin(boolean admin) {
		this.admin = admin;
		return this;
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

	// ------------------------------

	protected final void setResult(Object result) {
		this.result = result;
	}

	protected final String getProperUsername() {
		return isAdmin() ? targetVO.getAdmin().getUsername() + "(A)" : targetVO.getUsername();
	}

	// ------------------------------

	public abstract void cancel() throws Exception;

	// ------------------------------

	protected abstract void execute() throws Exception;
}
