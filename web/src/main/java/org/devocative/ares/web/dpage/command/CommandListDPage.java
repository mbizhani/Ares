package org.devocative.ares.web.dpage.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.panel.CommandExecPanel;
import org.devocative.ares.web.panel.OServiceAndCommandBatchPanel;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.demeter.web.model.DEntityLazyLoadModel;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.form.range.WNumberRangeInput;
import org.devocative.wickomp.formatter.OBooleanFormatter;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.formatter.ONumberFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class CommandListDPage extends DPage implements IGridDataSource<Command> {
	private static final long serialVersionUID = -1141834071L;

	@Inject
	private ICommandService commandService;

	private CommandFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<Command> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public CommandListDPage(String id) {
		this(id, Collections.<String>emptyList(), new CommandFVO());
	}

	// Panel Call - Open Filter
	public CommandListDPage(String id, CommandFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public CommandListDPage(String id, List<String> params) {
		this(id, params, new CommandFVO());
	}

	// Main Constructor
	private CommandListDPage(String id, List<String> params, CommandFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WModalWindow window = new WModalWindow("window");
		add(window);

		add(new WAjaxLink("add", AresIcon.EXPORT_IMPORT) {
			private static final long serialVersionUID = 8369836L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//window.setContent(new CommandFormDPage(window.getContentId()));
				window.setContent(new OServiceAndCommandBatchPanel(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.CommandAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("Command.name")));
		floatTable.add(new WBooleanInput("enabled")
			.setLabel(new ResourceModel("Command.enabled")));
		floatTable.add(new WBooleanInput("listView")
			.setLabel(new ResourceModel("Command.listView")));
		floatTable.add(new WNumberRangeInput("execLimit", Integer.class)
			.setLabel(new ResourceModel("Command.execLimit")));
		floatTable.add(new WSelectionInput("service", commandService.getServiceList(), true)
			.setLabel(new ResourceModel("Command.service")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", commandService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", commandService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<CommandFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = -1039203055L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<Command> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("Command.name"), "name"));
		columnList.add(new OPropertyColumn<Command>(new ResourceModel("Command.enabled"), "enabled")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<Command>(new ResourceModel("Command.listView"), "listView")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<Command>(new ResourceModel("Command.execLimit"), "execLimit")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("Command.service"), "service"));
		columnList.add(new OPropertyColumn<Command>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<Command>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<Command>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(AresPrivilegeKey.CommandEdit)) {
			columnList.add(new OEditAjaxColumn<Command>() {
				private static final long serialVersionUID = -1600839638L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<Command> rowData) {
					window.setContent(new CommandFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});

			columnList.add(new OAjaxLinkColumn<Command>(new Model<>(), AresIcon.EXECUTE) {
				private static final long serialVersionUID = 1205302042L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<Command> rowData) {
					window.setContent(new CommandExecPanel(window.getContentId(), rowData.getObject().getId()));
					window.show(new Model<>("Command Exec: " + rowData.getObject().getName()), target);
				}
			}.setField("EXECUTE"));
		}

		OGrid<Command> oGrid = new OGrid<>();
		oGrid
			.setColumns(columnList)
			.setMultiSort(false)
			.setHeight(gridHeight)
			.setWidth(gridWidth)
			.setFit(gridFit);

		grid = new WDataGrid<>("grid", oGrid, this);
		add(grid);

		// ---------------

		form.setVisible(formVisible);
		grid.setEnabled(gridEnabled || !formVisible);

		if (invisibleFormItems != null) {
			for (String formItem : invisibleFormItems) {
				floatTable.get(formItem).setVisible(false);
			}
		}

		if (removeColumns != null) {
			for (String column : removeColumns) {
				columnList.removeColumn(column);
			}
		}
	}

	// ------------------------------

	public CommandListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public CommandListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public CommandListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public CommandListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public CommandListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public CommandListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public CommandListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<Command> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return commandService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return commandService.count(filter);
	}

	@Override
	public IModel<Command> model(Command object) {
		return new DEntityLazyLoadModel<>(object.getId(), commandService);
	}
}