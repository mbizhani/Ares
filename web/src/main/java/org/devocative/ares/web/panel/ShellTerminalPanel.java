package org.devocative.ares.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.devocative.ares.iservice.IAsyncTextResult;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.TerminalTabInfo;
import org.devocative.demeter.web.DPanel;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WTerminal;
import org.devocative.wickomp.html.window.WModalWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ShellTerminalPanel extends DPanel {
	private static final long serialVersionUID = 6542154194145516263L;

	private static final Logger logger = LoggerFactory.getLogger(ShellTerminalPanel.class);

	private Long osiUserId;
	private String tabId;

	private Long connectionId;
	private WTerminal wTerminal;
	private WModalWindow window;

	private AsyncTextResult asyncTextResult;

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

		asyncTextResult = new AsyncTextResult();

		wTerminal = new WTerminal("wTerminal") {
			private static final long serialVersionUID = -4033298732740362278L;

			@Override
			protected void onConnect() {
				try {
					connectionId = terminalConnectionService.createTerminal(osiUserId, asyncTextResult);
					logger.info("ShellTerminalPanel Created: OSIUserId=[{}] ConnectionId=[{}]", osiUserId, connectionId);

					if (tabId != null) {
						send(ShellTerminalPanel.this, Broadcast.BUBBLE, new TerminalTabInfo(tabId, connectionId));
					}
				} catch (Exception e) {
					logger.error("ShellTerminalPanel.onConnect: ", e);
					asyncTextResult.onMessage("\n\nERR: " + e.getMessage());
				}
			}

			@Override
			protected void onMessage(String key, Integer specialKey) {
				try {
					terminalConnectionService.sendMessage(connectionId, key, specialKey);
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

	private class AsyncTextResult implements IAsyncTextResult {
		private static final long serialVersionUID = -727437925474317808L;

		@Override
		public void onMessage(String text) {
			//logger.debug("AsyncTextResult.text: {}", text);
			wTerminal.push(text);
		}
	}
}
