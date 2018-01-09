package org.devocative.ares.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.model.Model;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.SshMessageVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.TerminalTabInfo;
import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.demeter.web.DPanel;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WTerminal;
import org.devocative.wickomp.html.window.WModalWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ShellTerminalPanel extends DPanel implements ITaskResultCallback<String> {
	private static final long serialVersionUID = 6542154194145516263L;

	private static final Logger logger = LoggerFactory.getLogger(ShellTerminalPanel.class);

	private OSIUser osiUser;
	private String tabId;
	private Long fileUploadCommandId;

	private Long connectionId;
	private WTerminal wTerminal;
	private WModalWindow window;

	@Inject
	private ITerminalConnectionService terminalConnectionService;

	@Inject
	private ICommandService commandService;

	public ShellTerminalPanel(String id, OSIUser osiUser, String tabId) {
		super(id);

		this.osiUser = osiUser;
		this.tabId = tabId;

		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		window = new WModalWindow("window");
		add(window);

		if (fileUploadCommandId == null) {
			Command fileUpload = commandService.loadByNameAndOService("fileUpload", osiUser.getServiceId());
			if (fileUpload != null) {
				fileUploadCommandId = fileUpload.getId();
			} else {
				logger.warn("'fileUpload' command not found for serviceId=[{}], OSIUser=[{}]", osiUser.getServiceId(), osiUser);
			}
		}

		wTerminal = new WTerminal("wTerminal") {
			private static final long serialVersionUID = -4033298732740362278L;

			@Override
			protected void onConnect(int cols, int rows, int width, int height) {
				try {
					SshMessageVO vo = new SshMessageVO(cols, rows, width, height);
					connectionId = terminalConnectionService.createTerminal(osiUser.getId(), vo, ShellTerminalPanel.this);
					logger.info("ShellTerminalPanel Created: OSIUser=[{}] ConnectionId=[{}]", osiUser, connectionId);

					if (tabId != null) {
						send(ShellTerminalPanel.this, Broadcast.BUBBLE, new TerminalTabInfo(tabId, connectionId));
					}
				} catch (Exception e) {
					logger.error("ShellTerminalPanel.onConnect: ", e);
					ShellTerminalPanel.this.onTaskError(null, e);
				}
			}

			@Override
			protected void onResize(int cols, int rows, int width, int height) {
				terminalConnectionService.sendMessage(connectionId, new SshMessageVO(cols, rows, width, height));
			}

			@Override
			protected void onMessage(String key, Integer specialKey) {
				try {
					terminalConnectionService.sendMessage(connectionId, new SshMessageVO(key, specialKey));
				} catch (Exception e) {
					logger.error("ShellTerminalPanel.onMessage: key=[{}] specialKey=[{}] connId=[{}]",
						key, specialKey, connectionId, e);
				}
			}

			@Override
			protected void onClose() {
				terminalConnectionService.closeConnection(connectionId);
			}
		};
		wTerminal
			.setCharWidth(9)
			.setCharHeight(16);
		add(wTerminal);

		add(new WAjaxLink("fileUpload", AresIcon.UPLOAD) {
			private static final long serialVersionUID = 5443160474136357945L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(
					new CommandExecPanel(window.getContentId(), fileUploadCommandId)
						.setOsiUserId(osiUser.getId())
						.setTargetServiceInstanceId(osiUser.getServiceInstanceId())
				);
				window.show(new Model<>("fileUpload"), target);
			}
		}.setEnabled(fileUploadCommandId != null));
	}

	@Override
	public void onTaskResult(Object id, String result) {
		wTerminal.push(result);
	}

	@Override
	public void onTaskError(Object id, Exception e) {
		wTerminal.push("ERR: " + e.getMessage());
	}
}
