//overwrite
package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.iservice.oservice.IOServiceService;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WNumberInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OServiceFormDPage extends DPage {
	private static final long serialVersionUID = 639232157L;

	@Inject
	private IOServiceService oServiceService;

	private OService entity;

	// ------------------------------

	public OServiceFormDPage(String id) {
		this(id, new OService());
	}

	// Main Constructor - For Ajax Call
	public OServiceFormDPage(String id, OService entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public OServiceFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			oServiceService.load(Long.valueOf(params.get(0))) :
			new OService();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("OService.name")));
		floatTable.add(new WTextInput("connectionPattern")
			.setLabel(new ResourceModel("OService.connectionPattern")));
		floatTable.add(new WNumberInput("adminPort", Integer.class)
			.setLabel(new ResourceModel("OService.adminPort")));
		floatTable.add(new WTextInput("ports")
			.setLabel(new ResourceModel("OService.ports")));

		Form<OService> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = -972775163L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				oServiceService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(OServiceFormDPage.this, target)) {
					UrlUtil.redirectTo(OServiceListDPage.class);
				}
			}
		});
		add(form);
	}
}