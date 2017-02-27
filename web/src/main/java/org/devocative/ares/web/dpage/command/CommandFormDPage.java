//overwrite
package org.devocative.ares.web.dpage.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class CommandFormDPage extends DPage {
	private static final long serialVersionUID = 1891977795L;

	@Inject
	private ICommandService commandService;

	private Command entity;

	// ------------------------------

	public CommandFormDPage(String id) {
		this(id, new Command());
	}

	// Main Constructor - For Ajax Call
	public CommandFormDPage(String id, Command entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public CommandFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			commandService.load(Long.valueOf(params.get(0))) :
			new Command();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("Command.name")));
		floatTable.add(new WBooleanInput("listView")
			.setRequired(true)
			.setLabel(new ResourceModel("Command.listView")));
		floatTable.add(new WSelectionInput("service", commandService.getServiceList(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("Command.service")));

		Form<Command> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = -1231242837L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				commandService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(CommandFormDPage.this, target)) {
					UrlUtil.redirectTo(CommandListDPage.class);
				}
			}
		});
		add(form);
	}
}