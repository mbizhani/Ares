//overwrite
package org.devocative.ares.web.dpage.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.command.CommandLog;
import org.devocative.ares.iservice.command.ICommandLogService;
import org.devocative.ares.vo.filter.command.CommandLogFVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.formatter.OBooleanFormatter;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class CommandLogListDPage extends DPage implements IGridDataSource<CommandLog> {
	private static final long serialVersionUID = 780730397L;

	@Inject
	private ICommandLogService commandLogService;

	private CommandLogFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<CommandLog> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public CommandLogListDPage(String id) {
		this(id, Collections.<String>emptyList(), new CommandLogFVO());
	}

	// Panel Call - Open Filter
	public CommandLogListDPage(String id, CommandLogFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public CommandLogListDPage(String id, List<String> params) {
		this(id, params, new CommandLogFVO());
	}

	// Main Constructor
	private CommandLogListDPage(String id, List<String> params, CommandLogFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("params")
			.setLabel(new ResourceModel("CommandLog.params")));
		floatTable.add(new WBooleanInput("successful")
			.setLabel(new ResourceModel("CommandLog.successful")));
		floatTable.add(new WTextInput("error")
			.setLabel(new ResourceModel("CommandLog.error")));
		floatTable.add(new WSelectionInput("command", commandLogService.getCommandList(), true)
			.setLabel(new ResourceModel("CommandLog.command")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", commandLogService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));

		Form<CommandLogFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = -1492354939L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<CommandLog> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<CommandLog>(new ResourceModel("CommandLog.params"), "params"));
		columnList.add(new OPropertyColumn<CommandLog>(new ResourceModel("CommandLog.successful"), "successful")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<CommandLog>(new ResourceModel("CommandLog.error"), "error"));
		columnList.add(new OPropertyColumn<CommandLog>(new ResourceModel("CommandLog.command"), "command"));
		columnList.add(new OPropertyColumn<CommandLog>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<CommandLog>(new ResourceModel("entity.creatorUser"), "creatorUser"));

		OGrid<CommandLog> oGrid = new OGrid<>();
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

	public CommandLogListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public CommandLogListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public CommandLogListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public CommandLogListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public CommandLogListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public CommandLogListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public CommandLogListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<CommandLog> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return commandLogService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return commandLogService.count(filter);
	}

	@Override
	public IModel<CommandLog> model(CommandLog object) {
		return new WModel<>(object);
	}
}