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
import org.devocative.wickomp.form.*;
import org.devocative.wickomp.form.validator.WPatternValidator;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.WMessager;
import org.devocative.wickomp.html.window.WModalWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class CommandExecPanel extends DPanel implements IAsyncResponse<CommandOutput> {
	private static final long serialVersionUID = 6094381569285095361L;
	private static final Logger logger = LoggerFactory.getLogger(CommandExecPanel.class);
	private static final String TARGET_KEY = "Target";
	private static final String COLOR = "#ffd700";

	private Long commandId;
	private Long prepCommandId;
	private Map<String, Object> params = new HashMap<>();
	private Map<String, String> paramsAsStr = new HashMap<>();
	private List<KeyValueVO<Long, String>> targetServiceInstances = new ArrayList<>();

	private Long targetServiceInstanceId;
	private Long osiUserId;
	private Long serviceId;

	private DTaskBehavior<CommandOutput> taskBehavior;
	private WebMarkupContainer tabs, log, tabular;
	private List<WSelectionInput> guestInputList = new ArrayList<>();
	private DAjaxButton execute;

	private String commandDTaskKey;
	private long startTime;

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
		if (prepCommand.getParams() != null) {
			this.paramsAsStr.putAll(prepCommandService.convertParamsFromString(prepCommand.getParams()));
		}
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
	public void onAsyncResult(IPartialPageRequestHandler handler, CommandOutput result) {
		logger.debug("onAsyncResult: {}", result);


		switch (result.getType()) {
			case START:
				startTime = System.currentTimeMillis();
				String start = String.format("$('#%s').append(\"<div style='background-color:%s;'>00:00:00 --- START ---</div>\");",
					log.getMarkupId()
					, COLOR);
				handler.appendJavaScript(start);
				break;
			case PROMPT:
			case LINE:
			case ERROR:
				String script = String.format("$('#%s').append(\"<div class='ars-cmd-%s'><span style='background-color:%s;'>%s </span> %s</div>\");",
					log.getMarkupId(),
					result.getType().name().toLowerCase(),
					COLOR,
					elapsed(),
					escape(result.getOutput().toString().trim()));
				handler.appendJavaScript(script);
				handler.appendJavaScript(String.format("$('#%1$s').scrollTop($('#%1$s')[0].scrollHeight);", log.getMarkupId()));
				break;
			case TABULAR:
				renderTabular((TabularVO) result.getOutput(), handler);
				break;
			case FINISHED:
				String finished = String.format("$('#%s').append(\"<div style='background-color:%s;'>%s --- END ---</div><div>&nbsp;</div>\");",
					log.getMarkupId(),
					COLOR,
					elapsed());
				handler.appendJavaScript(finished);
				handler.appendJavaScript(String.format("$('#%s').removeAttr('disabled');", execute.getMarkupId()));
				break;
		}
	}

	@Override
	public void onAsyncError(IPartialPageRequestHandler handler, Exception error) {
		StringWriter out = new StringWriter();
		error.printStackTrace(new PrintWriter(out));

		String script = String.format("$('#%s').append('<div>%s</div>');", log.getMarkupId(), escape(out.toString()));
		handler.appendJavaScript(script);
		handler.appendJavaScript(String.format("$('#%s').removeAttr('disabled');", execute.getMarkupId()));
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WModalWindow window = new WModalWindow("window");
		add(window);

		taskBehavior = new DTaskBehavior<>(this);
		add(taskBehavior);

		Command command = commandService.load(commandId);
		serviceId = command.getServiceId();

		if (targetServiceInstanceId != null) {
			OServiceInstance target = serviceInstanceService.load(targetServiceInstanceId);
			KeyValueVO<Long, String> targetKeyValueVO = new KeyValueVO<>(target.getId(), target.toString());
			params.put(TARGET_KEY, targetKeyValueVO);
			targetServiceInstances.add(targetKeyValueVO);
		} else {
			targetServiceInstances.addAll(serviceInstanceService.findListForCommandExecution(serviceId));
		}

		XCommand xCommand = command.getXCommand();
		boolean hasGuest = xCommand.checkHasGuest();

		List<XParam> xParams = new ArrayList<>();
		xParams.add(
			new XParam()
				.setName(TARGET_KEY)
				.setType(XParamType.Service)
				.setRequired(true)
		);
		xParams.addAll(xCommand.getProperParams(getCurrentUser().isAdmin()));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new ListView<XParam>("fields", xParams) {
			private static final long serialVersionUID = -5561490679744721872L;

			@Override
			protected void populateItem(ListItem<XParam> item) {
				XParam xParam = item.getModelObject();
				FormComponent fieldFormItem = createFormItem(xParam, hasGuest);

				RepeatingView view = new RepeatingView("field");
				view.add(fieldFormItem.setRequired(xParam.getRequired()).setLabel(new Model<>(xParam.getName())));
				item.add(view);
			}
		});

		Form<Map<String, Object>> form = new Form<>("form", new CompoundPropertyModel<>(params));
		form.add(floatTable);

		form.add(execute = new DAjaxButton("execute", new ResourceModel("label.execute", "Exec"), AresIcon.EXECUTE) {
			private static final long serialVersionUID = 8306959811796741L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				Map<String, Object> paramsClone = new HashMap<>(params);
				KeyValueVO<Long, String> serviceInstance = (KeyValueVO<Long, String>) paramsClone.remove(TARGET_KEY);

				Map<String, Object> cmdParams = new HashMap<>();
				for (Map.Entry<String, Object> entry : paramsClone.entrySet()) {
					if (entry.getValue() instanceof KeyValueVO) {
						KeyValueVO vo = (KeyValueVO) entry.getValue();
						cmdParams.put(entry.getKey(), vo.getKey());
					} else if (entry.getValue() != null) {
						cmdParams.put(entry.getKey(), entry.getValue());
					}
				}

				commandDTaskKey = commandService.executeCommandTask(
					new CommandQVO(commandId, serviceInstance.getKey(), cmdParams, prepCommandId).setOsiUserId(osiUserId),
					taskBehavior);

				target.appendJavaScript(String.format("$('#%s').tabs('select', 'Console');", tabs.getMarkupId()));
				target.appendJavaScript(String.format("$('#%s').attr('disabled', 'disabled');", execute.getMarkupId()));
			}
		});
		execute.setOutputMarkupId(true);
		if (command.getConfirm()) {
			execute.setConfirmationMessage(new ResourceModel("label.confirm"));
		}

		form.add(new WAjaxLink("cancel", new ResourceModel("label.fa.stop", "Cancel"), AresIcon.STOP_CIRCLE) {
			private static final long serialVersionUID = 3211013451452565219L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					if (commandDTaskKey != null) {
						commandService.cancelCommandTask(commandDTaskKey);
					}
				} catch (Exception e) {
					WMessager.show(e, target);
				}
			}
		});

		form.add(new DAjaxButton("saveAsPrepCommand", new Model<>("Save as PrepCommand"), AresIcon.SAVE) {
			private static final long serialVersionUID = -8824681522137817872L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				Map<String, Object> paramsClone = new HashMap<>(params);
				KeyValueVO<Long, String> serviceInstance = (KeyValueVO<Long, String>) paramsClone.remove(TARGET_KEY);

				Map<String, Object> cmdParams = new HashMap<>();
				for (Map.Entry<String, Object> entry : paramsClone.entrySet()) {
					if (entry.getValue() instanceof KeyValueVO) {
						KeyValueVO vo = (KeyValueVO) entry.getValue();
						cmdParams.put(entry.getKey(), vo.getKey());
					} else if (entry.getValue() != null) {
						cmdParams.put(entry.getKey(), entry.getValue());
					}
				}

				window.setContent(new PrepCommandFormDPage(window.getContentId(), commandId, serviceInstance.getKey(), cmdParams));
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

		WebUtil.writeJQueryCall(String.format("$('#%s').tabs({fit:true, border:false});", tabs.getMarkupId()), false);
	}

	// ------------------------------

	private FormComponent createFormItem(XParam xParam, boolean hasGuest) {
		FormComponent fieldFormItem = null;
		String xParamName = xParam.getName();

		switch (xParam.getType()) {
			case Guest:
				if (paramsAsStr.containsKey(xParamName)) {
					Long serverId = serviceInstanceService.load(targetServiceInstances.get(0).getKey()).getServerId();
					KeyValueVO<String, String> guestOf = serverService.findGuestOf(serverId, paramsAsStr.get(xParamName));
					params.put(xParamName, guestOf);
					fieldFormItem = new WLabelInput(xParamName);
				} else {
					WSelectionInput guestSelectionInput = new WSelectionInput(xParamName, new ArrayList(), false);
					guestInputList.add(guestSelectionInput);
					fieldFormItem = guestSelectionInput;
				}
				break;

			case Server:
				if (paramsAsStr.containsKey(xParamName)) {
					KeyValueVO<Long, String> serverAsVM = serverService.findServerAsVM(Long.valueOf(paramsAsStr.get(xParamName)));
					params.put(xParamName, serverAsVM);
					fieldFormItem = new WLabelInput(xParamName);
				} else {
					fieldFormItem = new WSelectionInput(xParamName, serverService.findServersAsVM(), false);
				}
				break;

			case Service:
				if (TARGET_KEY.equals(xParamName)) {
					if (targetServiceInstanceId == null) {
						WSelectionInput selectionInput = new WSelectionInput(xParamName, targetServiceInstances, false);

						if (hasGuest) {
							selectionInput.addToChoices(new WSelectionInputAjaxUpdatingBehavior() {
								private static final long serialVersionUID = -2226097679754487094L;

								@Override
								protected void onUpdate(AjaxRequestTarget target) {
									KeyValueVO<Long, String> serviceInstance = (KeyValueVO<Long, String>) getComponent().getDefaultModelObject();
									Long serverId = serviceInstanceService.load(serviceInstance.getKey()).getServerId();
									List<KeyValueVO<String, String>> guestsOf = serverService.findGuestsOf(serverId);
									for (WSelectionInput guestInput : guestInputList) {
										guestInput.updateChoices(target, guestsOf);
									}
								}
							});
						}
						fieldFormItem = selectionInput;
					} else {
						fieldFormItem = new WLabelInput(xParamName);
					}
				} else {
					if (paramsAsStr.containsKey(xParamName)) {
						OServiceInstance serviceInstance = serviceInstanceService.load(new Long(paramsAsStr.get(xParamName)));
						params.put(xParamName, new KeyValueVO<>(serviceInstance.getId(), serviceInstance.toString()));
						fieldFormItem = new WLabelInput(xParamName);
					} else {
						List<KeyValueVO<Long, String>> serviceInstances = targetServiceInstances;
						if (serviceInstances.isEmpty()) {
							serviceInstances = serviceInstanceService.findListForCommandExecution(serviceId);
						}
						fieldFormItem = new WSelectionInput(xParamName, serviceInstances, false);
					}
				}
				break;

			case File:
				fieldFormItem = new FileStoreUploadPanel(xParamName, false);
				break;

			case Boolean:
				if (paramsAsStr.containsKey(xParamName)) {
					fieldFormItem = new WLabelInput(xParamName);
					params.put(xParamName, Boolean.valueOf(paramsAsStr.get(xParamName)));
				} else if (xParam.getDefaultValue() != null) {
					fieldFormItem = new WBooleanInput(xParamName);
					params.put(xParamName, xParam.getDefaultValueObject());
				}
				break;

			default:
				if (paramsAsStr.containsKey(xParamName)) {
					String[] parts = paramsAsStr.get(xParamName).split("[|]");
					if (parts.length == 1) {
						fieldFormItem = new WLabelInput(xParamName);
					} else {
						fieldFormItem = new WSelectionInput(xParamName, Arrays.asList(parts), false);
					}
					params.put(xParamName, paramsAsStr.get(parts[0]));
				} else if (xParam.getStringLiterals() != null) {
					List<String> literals = Arrays.asList(xParam.getStringLiterals().split("[|]"));
					fieldFormItem = new WSelectionInput(xParamName, literals, false);
					params.put(xParamName, xParam.getDefaultValueObject());
				} else {
					fieldFormItem = new WTextInput(xParamName);
					if (xParam.getValidRegex() != null) {
						fieldFormItem.add(new WPatternValidator(xParam.getValidRegex()));
					}
					params.put(xParamName, xParam.getDefaultValueObject());
				}
		}
		return fieldFormItem;
	}

	private void renderTabular(TabularVO<?> tabularVO, IPartialPageRequestHandler handler) {
		String tabId = tabular.getMarkupId() + "TAB";

		List<ColVO> colVOs = tabularVO
			.getColumns()
			.stream()
			.map(col -> new ColVO(col, col))
			.collect(Collectors.toList());

		List<Map<String, String>> data = new ArrayList<>();
		for (Map<String, ?> row : tabularVO.getRows()) {
			Map<String, String> rowStr = new HashMap<>();
			for (Map.Entry<String, ?> entry : row.entrySet()) {
				rowStr.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
			}
			data.add(rowStr);
		}

		DataGridVO dataGridVO = new DataGridVO(Collections.singletonList(colVOs), data)
			.setTitle("Total: " + tabularVO.getSize());

		StringBuilder scripts = new StringBuilder();
		scripts.append(String.format("$('#%s').tabs('select', 'Tabular');", tabs.getMarkupId()));
		scripts.append(String.format("$('#%s').empty();", tabular.getMarkupId()));
		scripts.append(String.format("$('#%s').append(\"<table id='%s'></table>\");", tabular.getMarkupId(), tabId));
		scripts.append(String.format("$('#%s').datagrid(%s);", tabId, WebUtil.toJson(dataGridVO)));
		handler.appendJavaScript(scripts.toString());
	}

	private String escape(String out) {
		String str = Strings.escapeMarkup(out, false, true).toString();
		str = str.replaceAll("[\n]", "<br/>");
		str = str.replaceAll("[\r]", "");
		return str;
	}

	private String elapsed() {
		long diffInSeconds = (System.currentTimeMillis() - startTime) / 1000;
		int h = (int) (diffInSeconds / 3600);
		int m = (int) ((diffInSeconds - h * 3600) / 60);
		int s = (int) (diffInSeconds - h * 3600 - m * 60);
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	// ------------------------------

	private class DataGridVO {
		private List<List<ColVO>> columns;
		private List<Map<String, String>> data;
		private String title;

		// ------------------------------

		public DataGridVO(List<List<ColVO>> columns, List<Map<String, String>> data) {
			this.columns = columns;
			this.data = data;
		}

		// ------------------------------

		public List<List<ColVO>> getColumns() {
			return columns;
		}

		public List<Map<String, String>> getData() {
			return data;
		}

		public Boolean getAutoRowHeight() {
			return false;
		}

		public Boolean getFit() {
			return true;
		}

		public Boolean getRownumbers() {
			return true;
		}

		public String getTitle() {
			return title;
		}

		public DataGridVO setTitle(String title) {
			this.title = title;
			return this;
		}
	}

	private class ColVO {
		private String field;
		private String title;

		public ColVO(String field, String title) {
			this.field = field;
			this.title = title;
		}

		public String getField() {
			return field;
		}

		public String getTitle() {
			return title;
		}
	}
}
