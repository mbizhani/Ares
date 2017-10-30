package org.devocative.ares.web.dpage.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.command.PrepCommand;
import org.devocative.ares.iservice.command.IPrepCommandService;
import org.devocative.ares.vo.filter.command.PrepCommandFVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.panel.CommandExecPanel;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.formatter.ONumberFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class PrepCommandListDPage extends DPage implements IGridDataSource<PrepCommand> {
	private static final long serialVersionUID = 98665334L;

	@Inject
	private IPrepCommandService prepCommandService;

	private PrepCommandFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<PrepCommand> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public PrepCommandListDPage(String id) {
		this(id, Collections.<String>emptyList(), new PrepCommandFVO());
	}

	// Panel Call - Open Filter
	public PrepCommandListDPage(String id, PrepCommandFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public PrepCommandListDPage(String id, List<String> params) {
		this(id, params, new PrepCommandFVO());
	}

	// Main Constructor
	private PrepCommandListDPage(String id, List<String> params, PrepCommandFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WModalWindow window = new WModalWindow("window");
		add(window);

		/*add(new WAjaxLink("add", AresIcon.ADD) {
			private static final long serialVersionUID = -977379527L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new PrepCommandFormDPage(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.PrepCommandAdd)));*/

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("PrepCommand.name")));
		floatTable.add(new WTextInput("params")
			.setLabel(new ResourceModel("PrepCommand.params")));
		floatTable.add(new WSelectionInput("command", prepCommandService.getCommandList(), true)
			.setLabel(new ResourceModel("PrepCommand.command")));
		floatTable.add(new WSelectionInput("serviceInstance", prepCommandService.getServiceInstanceList(), true)
			.setLabel(new ResourceModel("PrepCommand.serviceInstance")));
		floatTable.add(new WSelectionInput("allowedUsers", prepCommandService.getAllowedUsersList(), true)
			.setLabel(new ResourceModel("PrepCommand.allowedUsers")));
		floatTable.add(new WSelectionInput("allowedRoles", prepCommandService.getAllowedRolesList(), true)
			.setLabel(new ResourceModel("PrepCommand.allowedRoles")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", prepCommandService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", prepCommandService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<PrepCommandFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = 843431518L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<PrepCommand> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("PrepCommand.name"), "name"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("PrepCommand.params"), "params"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("PrepCommand.command"), "command"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("PrepCommand.serviceInstance"), "serviceInstance"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("PrepCommand.allowedUsers"), "allowedUsers"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("PrepCommand.allowedRoles"), "allowedRoles"));
		columnList.add(new OPropertyColumn<PrepCommand>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<PrepCommand>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<PrepCommand>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(AresPrivilegeKey.PrepCommandEdit)) {
			columnList.add(new OEditAjaxColumn<PrepCommand>() {
				private static final long serialVersionUID = 956590839L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<PrepCommand> rowData) {
					window.setContent(new PrepCommandFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		columnList.add(new OAjaxLinkColumn<PrepCommand>(new Model<>(), AresIcon.EXECUTE) {
			private static final long serialVersionUID = 7423601479360699023L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<PrepCommand> rowData) {
				window.setContent(new CommandExecPanel(window.getContentId(), rowData.getObject().getId()));
				window.show(new Model<>("PrepCommand Exec: " + rowData.getObject().getName()), target);
			}
		}.setField("EXECUTE"));

		OGrid<PrepCommand> oGrid = new OGrid<>();
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

	public PrepCommandListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public PrepCommandListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public PrepCommandListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public PrepCommandListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public PrepCommandListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public PrepCommandListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public PrepCommandListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<PrepCommand> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return prepCommandService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return prepCommandService.count(filter);
	}

	@Override
	public IModel<PrepCommand> model(PrepCommand object) {
		return new WModel<>(object);
	}
}