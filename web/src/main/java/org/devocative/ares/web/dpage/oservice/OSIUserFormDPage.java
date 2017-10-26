package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.validator.WEqualInputValidator;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OSIUserFormDPage extends DPage {
	private static final long serialVersionUID = 917336843L;

	@Inject
	private IOSIUserService oSIUserService;

	private OSIUser entity;

	private WTextInput password;

	// ------------------------------

	public OSIUserFormDPage(String id) {
		this(id, new OSIUser());
	}

	// Main Constructor - For Ajax Call
	public OSIUserFormDPage(String id, OSIUser entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public OSIUserFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			oSIUserService.load(Long.valueOf(params.get(0))) :
			new OSIUser();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("username")
			.setRequired(true)
			.setLabel(new ResourceModel("OSIUser.username")));

		password = new WTextInput("password", new Model<String>(), true);
		floatTable.add(password
			.setRequired(entity.getId() == null)
			.setLabel(new ResourceModel("OSIUser.password")));
		WTextInput password2 = new WTextInput("password2", new Model<String>(), true);
		floatTable.add(password2
			.setRequired(entity.getId() == null)
			.setLabel(new ResourceModel("OSIUser.password2")));

		floatTable.add(new WBooleanInput("executor")
			.setRequired(true)
			.setLabel(new ResourceModel("OSIUser.executor")));
		floatTable.add(new WBooleanInput("enabled")
			.setRequired(true)
			.setLabel(new ResourceModel("OSIUser.enabled")));
		floatTable.add(new WSelectionInput("remoteMode", ERemoteMode.list(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("OSIUser.remoteMode")));
		floatTable.add(new WSelectionInput("serviceInstance", oSIUserService.getServiceInstanceList(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("OSIUser.serviceInstance")));
		floatTable.add(new WSelectionInput("allowedUsers", oSIUserService.getAllowedUsersList(), true)
			.setLabel(new ResourceModel("OSIUser.allowedUsers")));
		floatTable.add(new WSelectionInput("allowedRoles", oSIUserService.getAllowedRolesList(), true)
			.setLabel(new ResourceModel("OSIUser.allowedRoles")));

		Form<OSIUser> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(new WEqualInputValidator(password, password2));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = -1343883661L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				oSIUserService.saveOrUpdate(entity, password.getModelObject());

				if (!WModalWindow.closeParentWindow(OSIUserFormDPage.this, target)) {
					UrlUtil.redirectTo(OSIUserListDPage.class);
				}
			}
		});
		add(form);
	}
}