package org.devocative.ares.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.devocative.ares.cmd.CommandOutput;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.ares.vo.xml.XParam;
import org.devocative.ares.web.AresDModule;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.async.AsyncBehavior;
import org.devocative.wickomp.async.IAsyncResponseHandler;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandExecPanel extends DPanel implements IAsyncResponseHandler {
	private static final long serialVersionUID = 6094381569285095361L;
	private static final Logger logger = LoggerFactory.getLogger(CommandExecPanel.class);

	private Long commandId;
	private Map<String, Object> params = new HashMap<>();

	private AsyncBehavior asyncBehavior;
	private WebMarkupContainer log;

	@Inject
	private ICommandService commandService;

	@Inject
	private IOServiceInstanceService serviceInstanceService;

	public CommandExecPanel(String id, Long commandId) {
		super(id);

		this.commandId = commandId;
	}

	@Override
	public void onAsyncResult(String handlerId, IPartialPageRequestHandler handler, Serializable result) {
		logger.debug("onAsyncResult: {}", result);

		CommandOutput line = (CommandOutput) result;
		CharSequence sequence = Strings.escapeMarkup(line.getLine(), true, true);
		String script = String.format("$('#%s').append(\"<div class='ars-cmd-%s'>%s</div>\");",
			log.getMarkupId(),
			line.getType().name().toLowerCase(),
			sequence);
		handler.appendJavaScript(script);
		handler.appendJavaScript(String.format("$('#%1$s').scrollTop($('#%1$s')[0].scrollHeight);", log.getMarkupId()));
	}

	@Override
	public void onAsyncError(String handlerId, IPartialPageRequestHandler handler, Exception error) {
		StringWriter out = new StringWriter();
		error.printStackTrace(new PrintWriter(out));
		String script = String.format("$('#%s').append('<div>%s</div>');", log.getMarkupId(), out.toString());
		handler.appendJavaScript(script);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		asyncBehavior = new AsyncBehavior(this);
		add(asyncBehavior);

		Command command = commandService.load(commandId);
		XCommand xCommand = command.getXCommand();

		List<XParam> xParams = new ArrayList<>();
		xParams.add(
			new XParam()
				.setName("target")
				.setType("service")
				.setRequired(true)
		);
		xParams.addAll(xCommand.getParams());

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new ListView<XParam>("fields", xParams) {
			private static final long serialVersionUID = -5561490679744721872L;

			@Override
			protected void populateItem(ListItem<XParam> item) {
				RepeatingView view = new RepeatingView("field");

				XParam xParam = item.getModelObject();

				FormComponent fieldFormItem;

				if ("service".equals(xParam.getType())) {
					fieldFormItem = new WSelectionInput(xParam.getName(), serviceInstanceService.list(), false);
				} else {
					fieldFormItem = new WTextInput(xParam.getName());
				}

				view.add(fieldFormItem.setRequired(xParam.getRequired()).setLabel(new Model<>(xParam.getName())));
				item.add(view);
			}
		});

		Form<Map<String, Object>> form = new Form<>("form", new CompoundPropertyModel<>(params));
		form.add(floatTable);
		form.add(new DAjaxButton("execute", new ResourceModel("label.execute", "Exec"), AresIcon.EXECUTE) {
			private static final long serialVersionUID = 8306959811796741L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				OServiceInstance serviceInstance = (OServiceInstance) params.remove("target");
				Map<String, String> cmdParams = new HashMap<>();
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					cmdParams.put(entry.getKey(), (String) entry.getValue());
				}
				asyncBehavior.sendAsyncRequest(AresDModule.EXEC_COMMAND, new CommandQVO(commandId, serviceInstance, cmdParams));
			}
		});
		add(form);

		log = new WebMarkupContainer("log");
		log.setOutputMarkupId(true);
		add(log);
	}
}
