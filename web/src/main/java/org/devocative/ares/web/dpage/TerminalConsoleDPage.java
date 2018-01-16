package org.devocative.ares.web.dpage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
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
import java.util.*;

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
			.setGlobalHotkeyEnabled(true)
			.setCloseTabKeyCode(190)
			.setOnSelect("function(title,index){$(this).tabs('getTab', index).find('textarea').focus();}") //NOTE: WOW! I don't know how it worked!
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

		Map<ERemoteMode, List<OSIUser>> allowedOnes = osiUserService.findAllowed();

		add(new ListView<ERemoteMode>("terminals", new ArrayList<>(allowedOnes.keySet())) {
			private static final long serialVersionUID = 1416116347227236048L;

			@Override
			protected void populateItem(ListItem<ERemoteMode> item) {
				ERemoteMode remoteMode = item.getModelObject();
				item.add(new AttributeModifier("title", remoteMode.getName()));

				item.add(new ListView<OSIUser>("terminal", new ArrayList<>(allowedOnes.get(remoteMode))) {
					private static final long serialVersionUID = -6470792116062021736L;

					@Override
					protected void populateItem(ListItem<OSIUser> item) {
						OSIUser osiUser = item.getModelObject();
						IModel<String> title = new Model<>(osiUser.toString());

						item.add(new WAjaxLink("osiUser", title) {
							private static final long serialVersionUID = -2582918546272510666L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								DPanel panel;
								String tabId = UUID.randomUUID().toString().replaceAll("[-]", "");

								if (ERemoteMode.SSH.equals(remoteMode)) {
									logger.info("Creating ShellTerminalPanel: OSIUser=[{}] tabId=[{}]", osiUser.toString(), tabId);
									panel = new ShellTerminalPanel(tabPanel.getTabContentId(), osiUser, tabId);
								} else if (ERemoteMode.JDBC.equals(remoteMode)) {
									logger.info("Creating SqlTerminalPanel: OSIUser=[{}] tabId=[{}]", osiUser.toString(), tabId);
									panel = new SqlTerminalPanel(tabPanel.getTabContentId(), osiUser.getId(), tabId);
								} else {
									throw new RuntimeException("Invalid Remote Mode: " + remoteMode);
								}

								tabPanel.addTab(target, panel, new OTab(title, true).setTabId(tabId));
							}
						});

					}
				});
			}
		});

		add(new Label("message", "No Accessible Terminal!").setVisible(allowedOnes.isEmpty()));
	}
}
