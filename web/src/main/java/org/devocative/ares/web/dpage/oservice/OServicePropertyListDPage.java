//overwrite
package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.entity.oservice.OServiceProperty;
import org.devocative.ares.iservice.oservice.IOServicePropertyService;
import org.devocative.ares.vo.filter.oservice.OServicePropertyFVO;
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

public class OServicePropertyListDPage extends DPage implements IGridDataSource<OServiceProperty> {
	private static final long serialVersionUID = -6827858L;

	@Inject
	private IOServicePropertyService oServicePropertyService;

	private OServicePropertyFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<OServiceProperty> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public OServicePropertyListDPage(String id) {
		this(id, Collections.<String>emptyList(), new OServicePropertyFVO());
	}

	// Panel Call - Open Filter
	public OServicePropertyListDPage(String id, OServicePropertyFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public OServicePropertyListDPage(String id, List<String> params) {
		this(id, params, new OServicePropertyFVO());
	}

	// Main Constructor
	private OServicePropertyListDPage(String id, List<String> params, OServicePropertyFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WModalWindow window = new WModalWindow("window");
		window.getOptions().setHeight(OSize.percent(80)).setWidth(OSize.percent(80));
		add(window);

		add(new WAjaxLink("add", AresIcon.ADD) {
			private static final long serialVersionUID = -272660879L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new OServicePropertyFormDPage(window.getContentId()));
				window.show(target);
			}
		});

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("OServiceProperty.name")));
		floatTable.add(new WBooleanInput("required")
			.setLabel(new ResourceModel("OServiceProperty.required")));
		floatTable.add(new WSelectionInput("service", oServicePropertyService.getServiceList(), true)
			.setLabel(new ResourceModel("OServiceProperty.service")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", oServicePropertyService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", oServicePropertyService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<OServicePropertyFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = -526780522L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<OServiceProperty> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("OServiceProperty.name"), "name"));
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("OServiceProperty.required"), "required")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("OServiceProperty.service"), "service"));
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<OServiceProperty>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		columnList.add(new OAjaxLinkColumn<OServiceProperty>(new Model<String>(), AresIcon.EDIT) {
			private static final long serialVersionUID = 1793825567L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<OServiceProperty> rowData) {
				window.setContent(new OServicePropertyFormDPage(window.getContentId(), rowData.getObject()));
				window.show(target);
			}
		}.setField("EDIT"));

		OGrid<OServiceProperty> oGrid = new OGrid<>();
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

	public OServicePropertyListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public OServicePropertyListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public OServicePropertyListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public OServicePropertyListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public OServicePropertyListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public OServicePropertyListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public OServicePropertyListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<OServiceProperty> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return oServicePropertyService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return oServicePropertyService.count(filter);
	}

	@Override
	public IModel<OServiceProperty> model(OServiceProperty object) {
		return new WModel<>(object);
	}
}