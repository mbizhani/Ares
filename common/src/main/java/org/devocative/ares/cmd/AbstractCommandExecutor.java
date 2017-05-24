package org.devocative.ares.cmd;

import org.devocative.ares.vo.OServiceInstanceTargetVO;

public abstract class AbstractCommandExecutor extends AbstractExecutor {
	protected final String prompt;
	protected final String command;

	// ------------------------------

	public AbstractCommandExecutor(OServiceInstanceTargetVO targetVO, CommandCenterResource resource, String prompt, String command) {
		super(targetVO, resource);

		this.prompt = prompt;
		this.command = command;
	}
}
