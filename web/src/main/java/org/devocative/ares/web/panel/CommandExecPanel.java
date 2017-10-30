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
import org.devocative.ares.entity.command.PrepCommand;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.iservice.command.IPrepCommandService;
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
	private Long prepCommandId;
	private Map<String, Object> params = new HashMap<>();
	private Map<String, String> paramsAsStr = new HashMap<>();
	private List<OServiceInstance> targetServiceInstances = new ArrayList<>();

	private Long targetServiceInstanceId;
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
	private IPrepCommandService prepCommandService;

	// ------------------------------

	public CommandExecPanel(String id, Long commandId) {
		super(id);

		this.commandId = commandId;
	}

	public CommandExecPanel(String id, PrepCommand prepCommand) {
		super(id);

		this.prepCommandId = prepCommand.getId();
		this.commandId = prepCommand.getCommandId();
		this.targetServiceInstanceId = prepCommand.getServiceInstanceId();
		this.paramsAsStr.putAll(prepCommandService.convertParamsFromString(prepCommand.getParams()));
	}

	// ------------------------------

	public CommandExecPanel setTargetServiceInstanceId(Long targetServiceInstanceId) {
		this.targetServiceInstanceId = targetServiceInstanceId;
		return this;
	}

	public CommandExecPanel setOsiUserId(Long osiUserId) {
		this.osiUserId = osiUserId;
		return this;
	}

	// ---------------

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

		Command command = commandService.load(commandId);

		if (targetServiceInstanceId != null) {
			OServiceInstance target = serviceInstanceService.load(targetServiceInstanceId);
			params.put("target", target);
			targetServiceInstances.add(target);
		} else {
			targetServiceInstances.addAll(serviceInstanceService.findListForCommandExecution(command.getServiceId()));
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
				String xParamName = xParam.getName();

				FormComponent fieldFormItem;

				switch (xParam.getType()) {
					case Guest:
						WSelectionInput guestSelectionInput = new WSelectionInput(xParamName, new ArrayList(), false);
						guestInputList.add(guestSelectionInput);
						fieldFormItem = guestSelectionInput;
						//TODO defaultValue
						/*TODO if(paramsAsStr.containsKey(xParamName)) {
							params.put(xParamName, ?);
							fieldFormItem.setEnabled(false);
						}*/
						break;

					case Server:
						fieldFormItem = new WSelectionInput(xParamName, serverService.findServersAsVM(), false);
						/*TODO if(paramsAsStr.containsKey(xParamName)) {
							params.put(xParamName, ?);
							fieldFormItem.setEnabled(false);
						}*/
						break;

					case Service:
						//WSelectionInput selectionInput = new WSelectionInput(xParam.getName(), serviceInstanceService.findListForCommandExecution(targetServiceId), false);
						//TODO only work for target param
						WSelectionInput selectionInput = new WSelectionInput(xParamName, targetServiceInstances, false);

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
						/*TODO if(paramsAsStr.containsKey(xParamName)) {
							params.put(xParamName, ?);
							fieldFormItem.setEnabled(false);
						}*/
						break;

					case File:
						fieldFormItem = new FileStoreUploadPanel(xParamName, false);
						/*TODO if(paramsAsStr.containsKey(xParamName)) {
							params.put(xParamName, ?);
							fieldFormItem.setEnabled(false);
						}*/
						break;

					case Boolean:
						fieldFormItem = new WBooleanInput(xParamName);
						if (paramsAsStr.containsKey(xParamName)) {
							params.put(xParamName, Boolean.valueOf(paramsAsStr.get(xParamName)));
							fieldFormItem.setEnabled(false);
						} else if (xParam.getDefaultValue() != null) {
							params.put(xParamName, xParam.getDefaultValueObject());
						}
						break;

					default:
						fieldFormItem = new WTextInput(xParamName);
						if (paramsAsStr.containsKey(xParamName)) {
							params.put(xParamName, paramsAsStr.get(xParamName));
							fieldFormItem.setEnabled(false);
						}
				}
				view.add(fieldFormItem.setRequired(xParam.getRequired()).setLabel(new Model<>(xParamName)));
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
					new CommandQVO(commandId, serviceInstance, cmdParams, prepCommandId).setOsiUserId(osiUserId),
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
		}.setVisible(hasPermission(AresPrivilegeKey.PrepCommandAdd) && prepCommandId == null));

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
