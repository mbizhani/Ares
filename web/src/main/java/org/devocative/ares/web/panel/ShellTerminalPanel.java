package org.devocative.ares.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.devocative.ares.iservice.ITerminalConnectionService;
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

public class ShellTerminalPanel extends DPanel implements ITaskResultCallback {
	private static final long serialVersionUID = 6542154194145516263L;

	private static final Logger logger = LoggerFactory.getLogger(ShellTerminalPanel.class);

	private Long osiUserId;
	private String tabId;

	private Long connectionId;
	private WTerminal wTerminal;
	private WModalWindow window;

	@Inject
	private ITerminalConnectionService terminalConnectionService;

	public ShellTerminalPanel(String id, Long osiUserId, String tabId) {
		super(id);

		this.osiUserId = osiUserId;
		this.tabId = tabId;

		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		window = new WModalWindow("window");
		add(window);

		wTerminal = new WTerminal("wTerminal") {
			private static final long serialVersionUID = -4033298732740362278L;

			@Override
			protected void onConnect() {
				try {
					connectionId = terminalConnectionService.createTerminal(osiUserId, ShellTerminalPanel.this);
					logger.info("ShellTerminalPanel Created: OSIUserId=[{}] ConnectionId=[{}]", osiUserId, connectionId);

					if (tabId != null) {
						send(ShellTerminalPanel.this, Broadcast.BUBBLE, new TerminalTabInfo(tabId, connectionId));
					}
				} catch (Exception e) {
					logger.error("ShellTerminalPanel.onConnect: ", e);
					ShellTerminalPanel.this.onTaskError(null, e);
				}
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
		add(wTerminal);

		add(new WAjaxLink("fileUpload", AresIcon.UPLOAD) {
			private static final long serialVersionUID = 5443160474136357945L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new CommandExecPanel(window.getContentId(), "fileUpload", osiUserId));
				window.show(target);
			}
		});
	}

	@Override
	public void onTaskResult(Object id, Object result) {
		wTerminal.push(result.toString());
	}

	@Override
	public void onTaskError(Object id, Exception e) {
		wTerminal.push("ERR: " + e.getMessage());
	}
}
