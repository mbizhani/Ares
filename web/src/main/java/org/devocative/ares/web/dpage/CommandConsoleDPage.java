package org.devocative.ares.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.devocative.ares.entity.command.PrepCommand;
import org.devocative.ares.iservice.command.IPrepCommandService;
import org.devocative.ares.web.panel.CommandExecPanel;
import org.devocative.demeter.web.DPage;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.tab.OTab;
import org.devocative.wickomp.html.tab.WTabbedPanel;

import javax.inject.Inject;
import java.util.List;

public class CommandConsoleDPage extends DPage {
	private static final long serialVersionUID = 8265688866989412018L;

	@Inject
	private IPrepCommandService prepCommandService;

	private String prepCommandAsParam;
	private WTabbedPanel tabPanel;

	// ------------------------------

	public CommandConsoleDPage(String id, List<String> params) {
		super(id, params);

		if (params.size() > 0) {
			prepCommandAsParam = params.get(0);
		}
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<PrepCommand> allowed = prepCommandService.findAllowed();

		tabPanel = new WTabbedPanel("tabPanel");
		if (prepCommandAsParam != null) {
			for (PrepCommand cmd : allowed) {
				if (cmd.getCode().equals(prepCommandAsParam)) {
					tabPanel.addTab(new CommandExecPanel(tabPanel.getTabContentId(), cmd), new Model<>(cmd.getName()));
					break;
				}
			}
		}
		add(tabPanel);

		add(new ListView<PrepCommand>("commands", allowed) {
			private static final long serialVersionUID = -771588095063507634L;

			@Override
			protected void populateItem(ListItem<PrepCommand> item) {
				PrepCommand prepCommand = item.getModelObject();
				IModel<String> title = new Model<>(prepCommand.getName());

				item.add(new WAjaxLink("command", title) {
					private static final long serialVersionUID = -360097665014494986L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						CommandExecPanel panel = new CommandExecPanel(tabPanel.getTabContentId(), prepCommand);
						tabPanel.addTab(target, panel, new OTab(title, true));
					}
				});
			}
		});
	}
}
