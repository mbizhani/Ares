//overwrite
package org.devocative.ares.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.EBasicDiscriminator;
import org.devocative.ares.entity.OBasicData;
import org.devocative.ares.iservice.IOBasicDataService;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OBasicDataFormDPage extends DPage {
	private static final long serialVersionUID = 749461386L;

	@Inject
	private IOBasicDataService oBasicDataService;

	private OBasicData entity;

	// ------------------------------

	public OBasicDataFormDPage(String id) {
		this(id, new OBasicData());
	}

	// Main Constructor - For Ajax Call
	public OBasicDataFormDPage(String id, OBasicData entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public OBasicDataFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			oBasicDataService.load(Long.valueOf(params.get(0))) :
			new OBasicData();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("OBasicData.name")));
		floatTable.add(new WSelectionInput("discriminator", EBasicDiscriminator.list(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("OBasicData.discriminator")));

		Form<OBasicData> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = 849517682L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				oBasicDataService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(OBasicDataFormDPage.this, target)) {
					UrlUtil.redirectTo(OBasicDataListDPage.class);
				}
			}
		});
		add(form);
	}
}