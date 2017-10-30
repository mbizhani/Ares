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
import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.cmd.CommandOutput;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.CommandQVO;
import org.devocative.ares.vo.TabularVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.ares.vo.xml.XParam;
import org.devocative.ares.vo.xml.XParamType;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.dpage.command.PrepCommandFormDPage;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.DTaskBehavior;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.panel.FileStoreUploadPanel;
import org.devocative.wickomp.WebUtil;
import org.devocative.wickomp.async.IAsyncResponse;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WSelectionInputAjaxUpdatingBehavior;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandExecPanel extends DPanel implements IAsyncResponse {
	private static final long serialVersionUID = 6094381569285095361L;
	private static final Logger logger = LoggerFactory.getLogger(CommandExecPanel.class);

	private Long commandId;
	private Map<String, Object> params = new HashMap<>();
	private List<OServiceInstance> targetServiceInstances = new ArrayList<>();

	private String commandName;
	private Long osiUserId;

	private DTaskBehavior taskBehavior;
	private WebMarkupContainer tabs, log, tabular;
	private List<WSelectionInput> guestInputList = new ArrayList<>();

	@Inject
	private ICommandService commandService;

	@Inject
	private IOServiceInstanceService serviceInstanceService;

	@Inject
	private IOServerService serverService;

	@Inject
	private IOSIUserService osiUserService;

	// ------------------------------

	public CommandExecPanel(String id, Long commandId) {
		super(id);

		this.commandId = commandId;
	}

	public CommandExecPanel(String id, String commandName, Long osiUserId) {
		super(id);

		this.commandName = commandName;
		this.osiUserId = osiUserId;
	}

	// ------------------------------

	@Override
	public void onAsyncResult(IPartialPageRequestHandler handler, Object result) {
		logger.debug("onAsyncResult: {}", result);

		CommandOutput line = (CommandOutput) result;
		if (line.getType() != CommandOutput.Type.TABULAR) {
			String str = Strings.escapeMarkup(line.getOutput().toString().trim(), false, true).toString();
			str = str.replaceAll("[\n]", "<br/>");
			str = str.replaceAll("[\r]", "");

			String script = String.format("$('#%s').append(\"<div class='ars-cmd-%s'>%s</div>\");",
				log.getMarkupId(),
				line.getType().name().toLowerCase(),
				str);
			handler.appendJavaScript(script);
			handler.appendJavaScript(String.format("$('#%1$s').scrollTop($('#%1$s')[0].scrollHeight);", log.getMarkupId()));
		} else {
			renderTabular((TabularVO) line.getOutput(), handler);
		}
	}

	@Override
	public void onAsyncError(IPartialPageRequestHandler handler, Exception error) {
		StringWriter out = new StringWriter();
		error.printStackTrace(new PrintWriter(out));
		String script = String.format("$('#%s').append('<div>%s</div>');", log.getMarkupId(), out.toString());
		handler.appendJavaScript(script);
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WModalWindow window = new WModalWindow("window");
		add(window);

		taskBehavior = new DTaskBehavior(this);
		add(taskBehavior);

		Command command;
		if (commandId != null) {
			command = commandService.load(commandId);
			targetServiceInstances.addAll(serviceInstanceService.findListForCommandExecution(command.getServiceId()));
		} else {
			OSIUser osiUser = osiUserService.load(osiUserId);
			if (osiUser == null) {
				throw new RuntimeException("OSIUser not found: " + osiUserId); //TODO
			}
			command = commandService.loadByNameAndOService(commandName, osiUser.getServiceId());
			if (command == null) {
				throw new RuntimeException(String.format("Command not found: name=%s serviceId=%s", commandName, osiUser.getServiceId()));
			}
			commandId = command.getId();
			targetServiceInstances.add(osiUser.getServiceInstance());
			params.put("target", osiUser.getServiceInstance());
		}

		XCommand xCommand = command.getXCommand();
		final boolean hasGuest = xCommand.checkHasGuest();

		List<XParam> xParams = new ArrayList<>();
		xParams.add(
			new XParam()
				.setName("target")
				.setType(XParamType.Service)
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

				switch (xParam.getType()) {
					case Guest:
						WSelectionInput guestSelectionInput = new WSelectionInput(xParam.getName(), new ArrayList(), false);
						guestInputList.add(guestSelectionInput);
						fieldFormItem = guestSelectionInput;
						//TODO defaultValue
						break;

					case Server:
						fieldFormItem = new WSelectionInput(xParam.getName(), serverService.findServersAsVM(), false);
						break;

					case Service:
						//WSelectionInput selectionInput = new WSelectionInput(xParam.getName(), serviceInstanceService.findListForCommandExecution(targetServiceId), false);
						//TODO only work for target param
						WSelectionInput selectionInput = new WSelectionInput(xParam.getName(), targetServiceInstances, false);

						if (hasGuest) {
							selectionInput.addToChoices(new WSelectionInputAjaxUpdatingBehavior() {
								private static final long serialVersionUID = -2226097679754487094L;

								@Override
								protected void onUpdate(AjaxRequestTarget target) {
									OServiceInstance serviceInstance = (OServiceInstance) getComponent().getDefaultModelObject();
									List<KeyValueVO<String, String>> guestsOf = serverService.findGuestsOf(serviceInstance.getServerId());
									for (WSelectionInput guestInput : guestInputList) {
										guestInput.updateChoices(target, guestsOf);
									}
								}
							});
						}
						fieldFormItem = selectionInput;
						//TODO defaultValue
						break;

					case File:
						fieldFormItem = new FileStoreUploadPanel(xParam.getName(), false);
						break;

					case Boolean:
						fieldFormItem = new WBooleanInput(xParam.getName());
						if (xParam.getDefaultValue() != null) {
							params.put(xParam.getName(), xParam.getDefaultValueObject());
						}
						break;

					default:
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
				if (serviceInstance == null) {
					error("'target' is required");
				}
				Map<String, Object> cmdParams = new HashMap<>();
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					if (entry.getValue() instanceof KeyValueVO) {
						KeyValueVO vo = (KeyValueVO) entry.getValue();
						cmdParams.put(entry.getKey(), vo.getKey());
					} else {
						cmdParams.put(entry.getKey(), entry.getValue());
					}
				}

				commandService.executeCommandTask(
					new CommandQVO(commandId, serviceInstance, cmdParams).setOsiUserId(osiUserId),
					taskBehavior);
			}
		});

		form.add(new DAjaxButton("saveAsPrepCommand", new Model<>("Save as PrepCommand"), AresIcon.SAVE) {
			private static final long serialVersionUID = -8824681522137817872L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				OServiceInstance serviceInstance = (OServiceInstance) params.remove("target");
				Map<String, Object> cmdParams = new HashMap<>();
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					if (entry.getValue() instanceof KeyValueVO) {
						KeyValueVO vo = (KeyValueVO) entry.getValue();
						cmdParams.put(entry.getKey(), vo.getKey());
					} else {
						cmdParams.put(entry.getKey(), entry.getValue());
					}
				}

				window.setContent(new PrepCommandFormDPage(window.getContentId(), commandId, serviceInstance.getId(), cmdParams));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.PrepCommandAdd)));

		add(form);

		tabs = new WebMarkupContainer("tabs");
		tabs.setOutputMarkupId(true);
		add(tabs);

		log = new WebMarkupContainer("log");
		log.setOutputMarkupId(true);
		tabs.add(log);

		tabular = new WebMarkupContainer("tabular");
		tabular.setOutputMarkupId(true);
		tabs.add(tabular);
	}

	@Override
	protected void onAfterRender() {
		super.onAfterRender();

		WebUtil.writeJQueryCall(String.format("$('#%s').tabs();", tabs.getMarkupId()), false);
	}

	// ------------------------------

	private void renderTabular(TabularVO<?> tabularVO, IPartialPageRequestHandler handler) {
		String tabId = tabular.getMarkupId() + "-tab";

		StringBuilder builder = new StringBuilder();
		builder.append(String.format("<table id='%s' border='1' style='width:100%%;'><thead><tr>", tabId));
		for (String col : tabularVO.getColumns()) {
			//builder.append(String.format("<th data-options=\\\"field:'%s'\\\">", col.replaceAll("\\W",""))).append(col).append("</th>");
			builder.append("<th>").append(col).append("</th>");
		}
		builder.append("</tr></thead><tbody>");
		for (Map<String, ?> row : tabularVO.getRows()) {
			builder.append("<tr>");
			for (Object cell : row.values()) {
				builder.append("<td>").append(cell != null ? cell : "").append("</td>");
			}
			builder.append("</tr>");
		}
		builder.append("</tbody></table>");

		handler.appendJavaScript(String.format("$('#%s').remove();", tabId));
		handler.appendJavaScript(String.format("$('#%s').html(\"%s\");", tabular.getMarkupId(), builder.toString()));
		//handler.appendJavaScript(String.format("$('#%s').datagrid();", tabId));
		handler.appendJavaScript(String.format("$('#%s').tabs('select', 'Tabular');", tabs.getMarkupId()));
	}
}
