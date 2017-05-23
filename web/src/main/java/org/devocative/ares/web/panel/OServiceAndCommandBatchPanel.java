package org.devocative.ares.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.devocative.ares.iservice.oservice.IOServiceService;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WFileInput;
import org.devocative.wickomp.html.WMessager;

import javax.inject.Inject;
import java.io.IOException;

public class OServiceAndCommandBatchPanel extends DPanel {
	private static final long serialVersionUID = -1812184098730263917L;

	@Inject
	private IOServiceService oServiceService;

	private WebMarkupContainer output; //TODO
	private WFileInput file;

	public OServiceAndCommandBatchPanel(String id) {
		super(id);

		output = new WebMarkupContainer("output");
		output.setOutputMarkupId(true);
		add(output);

		file = new WFileInput("file");
		file.setRequired(true);

		Form<Void> form = new Form<>("form");
		form.add(file);
		form.add(new DAjaxButton("upload", AresIcon.UPLOAD) {
			private static final long serialVersionUID = 4607559495169959293L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				try {
					oServiceService.importFile(file.getFileUpload().getInputStream());
					WMessager.show("Info", "Import Successfully", target);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		add(form);
	}
}
