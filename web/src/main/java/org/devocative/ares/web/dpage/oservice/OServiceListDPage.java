package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.iservice.oservice.IOServiceService;
import org.devocative.ares.vo.filter.oservice.OServiceFVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.panel.OServiceAndCommandBatchPanel;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.form.range.WNumberRangeInput;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.formatter.ONumberFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OServiceListDPage extends DPage implements IGridDataSource<OService> {
	private static final long serialVersionUID = 1900387587L;

	@Inject
	private IOServiceService oServiceService;

	private OServiceFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<OService> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public OServiceListDPage(String id) {
		this(id, Collections.<String>emptyList(), new OServiceFVO());
	}

	// Panel Call - Open Filter
	public OServiceListDPage(String id, OServiceFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public OServiceListDPage(String id, List<String> params) {
		this(id, params, new OServiceFVO());
	}

	// Main Constructor
	private OServiceListDPage(String id, List<String> params, OServiceFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WModalWindow window = new WModalWindow("window");
		add(window);

		add(new WAjaxLink("add", AresIcon.ADD) {
			private static final long serialVersionUID = -1685214074L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				//window.setContent(new OServiceFormDPage(window.getContentId()));
				window.setContent(new OServiceAndCommandBatchPanel(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.OServiceAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("OService.name")));
		floatTable.add(new WTextInput("connectionPattern")
			.setLabel(new ResourceModel("OService.connectionPattern")));
		floatTable.add(new WNumberRangeInput("adminPort", Integer.class)
			.setLabel(new ResourceModel("OService.adminPort")));
		floatTable.add(new WTextInput("ports")
			.setLabel(new ResourceModel("OService.ports")));
		floatTable.add(new WSelectionInput("properties", oServiceService.getPropertiesList(), true)
			.setLabel(new ResourceModel("OService.properties")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", oServiceService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", oServiceService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<OServiceFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = -780735381L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<OService> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("OService.name"), "name"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("OService.connectionPattern"), "connectionPattern"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("OService.adminPort"), "adminPort")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("OService.ports"), "ports"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("OService.properties"), "properties"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<OService>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(AresPrivilegeKey.OServiceEdit)) {
			columnList.add(new OEditAjaxColumn<OService>() {
				private static final long serialVersionUID = 1621478020L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<OService> rowData) {
					window.setContent(new OServiceFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		OGrid<OService> oGrid = new OGrid<>();
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

	public OServiceListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public OServiceListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public OServiceListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public OServiceListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public OServiceListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public OServiceListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public OServiceListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<OService> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return oServiceService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return oServiceService.count(filter);
	}

	@Override
	public IModel<OService> model(OService object) {
		return new WModel<>(object);
	}
}