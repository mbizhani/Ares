//overwrite
package org.devocative.ares.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OServerFormDPage extends DPage {
	private static final long serialVersionUID = 1159607449L;

	@Inject
	private IOServerService oServerService;

	private OServer entity;

	// ------------------------------

	public OServerFormDPage(String id) {
		this(id, new OServer());
	}

	// Main Constructor - For Ajax Call
	public OServerFormDPage(String id, OServer entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public OServerFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			oServerService.load(Long.valueOf(params.get(0))) :
			new OServer();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("OServer.name")));
		floatTable.add(new WTextInput("address")
			.setRequired(true)
			.setLabel(new ResourceModel("OServer.address")));

		Form<OServer> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), AresIcon.SAVE) {
			private static final long serialVersionUID = -1427451135L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				oServerService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(OServerFormDPage.this, target)) {
					UrlUtil.redirectTo(OServerListDPage.class);
				}
			}
		});
		add(form);
	}
}