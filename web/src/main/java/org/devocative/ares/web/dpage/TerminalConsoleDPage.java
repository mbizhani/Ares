package org.devocative.ares.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.web.TerminalTabInfo;
import org.devocative.ares.web.panel.ShellTerminalPanel;
import org.devocative.ares.web.panel.SqlTerminalPanel;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DPanel;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.tab.OTab;
import org.devocative.wickomp.html.tab.OTabbedPanel;
import org.devocative.wickomp.html.tab.WTabbedPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TerminalConsoleDPage extends DPage {
	private static final long serialVersionUID = -2200966618262815151L;

	private static final Logger logger = LoggerFactory.getLogger(TerminalConsoleDPage.class);

	@Inject
	private IOSIUserService osiUserService;

	@Inject
	private ITerminalConnectionService terminalConnectionService;

	private WTabbedPanel tabPanel;
	private Map<String, Long> tabId2ConnId = new HashMap<>();

	// ------------------------------

	public TerminalConsoleDPage(String id, List<String> params) {
		super(id, params);
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		OTabbedPanel oTabbedPanel = new OTabbedPanel();
		oTabbedPanel
			.setFit(true);
		tabPanel = new WTabbedPanel("tabPanel", oTabbedPanel) {
			private static final long serialVersionUID = 3302863870173680811L;

			@Override
			protected void onTabClose(AjaxRequestTarget target, OTab closedTab) {
				String tabId = closedTab.getTabId();
				Long connId = tabId2ConnId.get(tabId);
				terminalConnectionService.closeConnection(connId);
				tabId2ConnId.remove(tabId);
				logger.info("Closing Terminal Tab: tabId=[{}] connId=[{}]", tabId, connId);
			}

			@Override
			public void onEvent(IEvent<?> event) {
				Object payload = event.getPayload();
				if (event.getType() == Broadcast.BUBBLE && payload instanceof TerminalTabInfo) {
					event.dontBroadcastDeeper();
					TerminalTabInfo tabInfo = (TerminalTabInfo) payload;
					logger.info("Registering tab with terminal connection: {}", tabInfo);
					tabId2ConnId.put(tabInfo.getTabId(), tabInfo.getConnectionId());
				}
			}
		};
		add(tabPanel);

		List<OSIUser> allowedOnes = osiUserService.findAllowedOnes(ERemoteMode.SSH);
		add(new ListView<OSIUser>("sshConnections", allowedOnes) {
			private static final long serialVersionUID = -2856412103432642301L;

			@Override
			protected void populateItem(ListItem<OSIUser> item) {
				OSIUser osiUser = item.getModelObject();
				IModel<String> title = new Model<>(osiUser.toString());

				item.add(new WAjaxLink("osiUser", title) {
					private static final long serialVersionUID = -360097665014494986L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						String tabId = UUID.randomUUID().toString().replaceAll("[-]", "");
						logger.info("Creating ShellTerminalPanel: OSIUser=[{}] tabId=[{}]", osiUser.toString(), tabId);
						DPanel panel = new ShellTerminalPanel(tabPanel.getTabContentId(), osiUser, tabId);
						tabPanel.addTab(target, panel, new OTab(title, true).setTabId(tabId));
					}
				});
			}
		});

		allowedOnes = osiUserService.findAllowedOnes(ERemoteMode.JDBC);
		add(new ListView<OSIUser>("jdbcConnections", allowedOnes) {
			private static final long serialVersionUID = -2856412103432642301L;

			@Override
			protected void populateItem(ListItem<OSIUser> item) {
				OSIUser osiUser = item.getModelObject();

				final Long osiUserId = osiUser.getId();
				final IModel<String> title = new Model<>(osiUser.toString());

				item.add(new WAjaxLink("osiUser", title) {
					private static final long serialVersionUID = -360097665014494986L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						String tabId = UUID.randomUUID().toString().replaceAll("[-]", "");
						logger.info("Creating SqlTerminalPanel: OSIUser=[{}] tabId=[{}]", title.getObject(), tabId);
						DPanel panel = new SqlTerminalPanel(tabPanel.getTabContentId(), osiUserId, tabId);
						tabPanel.addTab(target, panel, new OTab(title, true).setTabId(tabId));
					}
				});
			}
		});

		//add(new Label("message", "No allowed terminal!").setVisible(allowedOnes.isEmpty()));
	}
}
