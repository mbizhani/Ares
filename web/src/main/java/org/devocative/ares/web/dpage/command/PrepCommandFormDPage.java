package org.devocative.ares.web.dpage.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.command.PrepCommand;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.command.IPrepCommandService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
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
import java.util.Map;

public class PrepCommandFormDPage extends DPage {
	private static final long serialVersionUID = -1162490096L;

	@Inject
	private IPrepCommandService prepCommandService;

	@Inject
	private ICommandService commandService;

	@Inject
	private IOServiceInstanceService serviceInstanceService;

	private PrepCommand entity;

	// ------------------------------

	public PrepCommandFormDPage(String id, Long commandId, Long serviceInstanceId, Map<String, ?> cmdParams) {
		super(id, Collections.<String>emptyList());

		entity = new PrepCommand();
		entity.setCommand(commandService.load(commandId));
		entity.setServiceInstance(serviceInstanceService.load(serviceInstanceId));
		entity.setParams(prepCommandService.convertParamsToString(cmdParams));
	}

	// ---------------

	/*public PrepCommandFormDPage(String id) {
		this(id, new PrepCommand());
	}*/

	// Main Constructor - For Ajax Call
	public PrepCommandFormDPage(String id, PrepCommand entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public PrepCommandFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			prepCommandService.load(Long.valueOf(params.get(0))) :
			new PrepCommand();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("PrepCommand.name")));
		floatTable.add(new WBooleanInput("enabled")
			.setRequired(true)
			.setLabel(new ResourceModel("PrepCommand.enabled")));
		floatTable.add(new WSelectionInput("command", Collections.singletonList(entity.getCommand()), false)
			.setRequired(true)
			.setLabel(new ResourceModel("PrepCommand.command")));
		floatTable.add(new WSelectionInput("serviceInstance", Collections.singletonList(entity.getServiceInstance()), false)
			.setRequired(true)
			.setLabel(new ResourceModel("PrepCommand.serviceInstance")));
		floatTable.add(new WSelectionInput("allowedUsers", prepCommandService.getAllowedUsersList(), true)
			.setLabel(new ResourceModel("PrepCommand.allowedUsers")));
		floatTable.add(new WSelectionInput("allowedRoles", prepCommandService.getAllowedRolesList(), true)
			.setLabel(new ResourceModel("PrepCommand.allowedRoles")));

		Form<PrepCommand> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);
		form.add(new TextArea<>("params")
			.setLabel(new ResourceModel("PrepCommand.params")));

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = 651391736L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				prepCommandService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(PrepCommandFormDPage.this, target)) {
					UrlUtil.redirectTo(PrepCommandListDPage.class);
				}
			}
		});
		add(form);
	}
}