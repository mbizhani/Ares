package org.devocative.ares.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.TerminalConnection;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.vo.filter.TerminalConnectionFVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.formatter.OBooleanFormatter;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class TerminalConnectionListDPage extends DPage implements IGridDataSource<TerminalConnection> {
	private static final long serialVersionUID = -190508577L;

	@Inject
	private ITerminalConnectionService terminalConnectionService;

	private TerminalConnectionFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<TerminalConnection> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public TerminalConnectionListDPage(String id) {
		this(id, Collections.<String>emptyList(), new TerminalConnectionFVO());
	}

	// Panel Call - Open Filter
	public TerminalConnectionListDPage(String id, TerminalConnectionFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public TerminalConnectionListDPage(String id, List<String> params) {
		this(id, params, new TerminalConnectionFVO());
	}

	// Main Constructor
	private TerminalConnectionListDPage(String id, List<String> params, TerminalConnectionFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WBooleanInput("active")
			.setLabel(new ResourceModel("TerminalConnection.active")));
		floatTable.add(new WDateRangeInput("disconnection")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("TerminalConnection.disconnection")));
		floatTable.add(new WSelectionInput("target", terminalConnectionService.getTargetList(), true)
			.setLabel(new ResourceModel("TerminalConnection.target")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", terminalConnectionService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));

		Form<TerminalConnectionFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = -1780353209L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<TerminalConnection> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new Model<>("ID"), "id"));
		columnList.add(new OPropertyColumn<TerminalConnection>(new ResourceModel("TerminalConnection.active"), "active")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<TerminalConnection>(new ResourceModel("TerminalConnection.disconnection"), "disconnection")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("TerminalConnection.target"), "target"));
		columnList.add(new OPropertyColumn<TerminalConnection>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));

		if (hasPermission(AresPrivilegeKey.StopTerminalConnection)) {
			columnList.add(new OAjaxLinkColumn<TerminalConnection>(new Model<>(), AresIcon.STOP_CIRCLE) {
					private static final long serialVersionUID = -2270296993745682208L;

					@Override
					public void onClick(AjaxRequestTarget target, IModel<TerminalConnection> rowData) {
						terminalConnectionService.closeConnection(rowData.getObject().getId());
					}

					@Override
					public boolean onCellRender(TerminalConnection bean, String id) {
						return bean.getActive();
					}
				}.setConfirmMessage(getString("label.confirm"))
					.setField("STOP_CONN")
			);
		}

		OGrid<TerminalConnection> oGrid = new OGrid<>();
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

	public TerminalConnectionListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public TerminalConnectionListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public TerminalConnectionListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public TerminalConnectionListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public TerminalConnectionListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public TerminalConnectionListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public TerminalConnectionListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<TerminalConnection> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return terminalConnectionService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return terminalConnectionService.count(filter);
	}

	@Override
	public IModel<TerminalConnection> model(TerminalConnection object) {
		return new WModel<>(object);
	}
}