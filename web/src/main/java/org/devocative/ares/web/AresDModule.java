package org.devocative.ares.web;

import org.devocative.demeter.iservice.task.ITaskService;
import org.devocative.demeter.web.WebDModule;
import org.devocative.wickomp.async.AsyncToken;
import org.devocative.wickomp.async.IAsyncRequestHandler;

import javax.inject.Inject;

public class AresDModule extends WebDModule {
	public static final String EXEC_COMMAND = "EXEC_COMMAND";

	@Inject
	private ITaskService taskService;

	@Override
	public void init() {
		registerAsyncHandler(EXEC_COMMAND, new IAsyncRequestHandler() {
			@Override
			public void onRequest(AsyncToken asyncToken, Object requestPayLoad) {
				taskService.start("arsCommandExecutionDTask", asyncToken, requestPayLoad, AresDModule.this);
			}
		});

	}
}
