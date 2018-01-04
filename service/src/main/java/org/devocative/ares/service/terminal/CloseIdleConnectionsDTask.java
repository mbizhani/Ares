package org.devocative.ares.service.terminal;

import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.demeter.iservice.task.DTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("arsCloseIdleConnectionsDTask")
public class CloseIdleConnectionsDTask extends DTask {

	@Autowired
	private ITerminalConnectionService terminalConnectionService;

	@Override
	public void init() {
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() {
		terminalConnectionService.closeIdleConnections();
	}

	@Override
	public void cancel() throws Exception {
	}
}
