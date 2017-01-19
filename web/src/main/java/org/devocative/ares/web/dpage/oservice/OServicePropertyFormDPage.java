//overwrite
package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.oservice.OServiceProperty;
import org.devocative.ares.iservice.oservice.IOServicePropertyService;
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

public class OServicePropertyFormDPage extends DPage {
	private static final long serialVersionUID = -1267983288L;

	@Inject
	private IOServicePropertyService oServicePropertyService;

	private OServiceProperty entity;

	// ------------------------------

	public OServicePropertyFormDPage(String id) {
		this(id, new OServiceProperty());
	}

	// Main Constructor - For Ajax Call
	public OServicePropertyFormDPage(String id, OServiceProperty entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public OServicePropertyFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			oServicePropertyService.load(Long.valueOf(params.get(0))) :
			new OServiceProperty();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("OServiceProperty.name")));
		floatTable.add(new WBooleanInput("required")
			.setRequired(true)
			.setLabel(new ResourceModel("OServiceProperty.required")));
		floatTable.add(new WSelectionInput("service", oServicePropertyService.getServiceList(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("OServiceProperty.service")));

		Form<OServiceProperty> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = -718820304L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				oServicePropertyService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(OServicePropertyFormDPage.this, target)) {
					UrlUtil.redirectTo(OServicePropertyListDPage.class);
				}
			}
		});
		add(form);
	}
}