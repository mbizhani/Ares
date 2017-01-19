//overwrite
package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WNumberInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OServiceInstanceFormDPage extends DPage {
	private static final long serialVersionUID = 379911560L;

	@Inject
	private IOServiceInstanceService oServiceInstanceService;

	private OServiceInstance entity;

	// ------------------------------

	public OServiceInstanceFormDPage(String id) {
		this(id, new OServiceInstance());
	}

	// Main Constructor - For Ajax Call
	public OServiceInstanceFormDPage(String id, OServiceInstance entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public OServiceInstanceFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			oServiceInstanceService.load(Long.valueOf(params.get(0))) :
			new OServiceInstance();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("OServiceInstance.name")));
		floatTable.add(new WNumberInput("port", Integer.class)
			.setLabel(new ResourceModel("OServiceInstance.port")));
		floatTable.add(new WSelectionInput("server", oServiceInstanceService.getServerList(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("OServiceInstance.server")));
		floatTable.add(new WSelectionInput("service", oServiceInstanceService.getServiceList(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("OServiceInstance.service")));
		floatTable.add(new WSelectionInput("related", oServiceInstanceService.getRelatedList(), true)
			.setLabel(new ResourceModel("OServiceInstance.related")));

		Form<OServiceInstance> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = 1826139504L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				oServiceInstanceService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(OServiceInstanceFormDPage.this, target)) {
					UrlUtil.redirectTo(OServiceInstanceListDPage.class);
				}
			}
		});
		add(form);
	}
}