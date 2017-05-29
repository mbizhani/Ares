//overwrite
package org.devocative.ares.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.EBasicDiscriminator;
import org.devocative.ares.entity.OBasicData;
import org.devocative.ares.iservice.IOBasicDataService;
import org.devocative.ares.vo.filter.OBasicDataFVO;
import org.devocative.ares.web.AresIcon;
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
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OBasicDataListDPage extends DPage implements IGridDataSource<OBasicData> {
	private static final long serialVersionUID = 2010616816L;

	@Inject
	private IOBasicDataService oBasicDataService;

	private OBasicDataFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<OBasicData> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public OBasicDataListDPage(String id) {
		this(id, Collections.<String>emptyList(), new OBasicDataFVO());
	}

	// Panel Call - Open Filter
	public OBasicDataListDPage(String id, OBasicDataFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public OBasicDataListDPage(String id, List<String> params) {
		this(id, params, new OBasicDataFVO());
	}

	// Main Constructor
	private OBasicDataListDPage(String id, List<String> params, OBasicDataFVO filter) {
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
			private static final long serialVersionUID = -60973261L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new OBasicDataFormDPage(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.OBasicDataAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("OBasicData.name")));
		floatTable.add(new WSelectionInput("discriminator", EBasicDiscriminator.list(), true)
			.setLabel(new ResourceModel("OBasicData.discriminator")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", oBasicDataService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", oBasicDataService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<OBasicDataFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = 1041557464L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<OBasicData> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<OBasicData>(new ResourceModel("OBasicData.name"), "name"));
		columnList.add(new OPropertyColumn<OBasicData>(new ResourceModel("OBasicData.discriminator"), "discriminator"));
		columnList.add(new OPropertyColumn<OBasicData>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OBasicData>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<OBasicData>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OBasicData>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<OBasicData>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(AresPrivilegeKey.OBasicDataEdit)) {
			columnList.add(new OEditAjaxColumn<OBasicData>() {
				private static final long serialVersionUID = 231622513L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<OBasicData> rowData) {
					window.setContent(new OBasicDataFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		OGrid<OBasicData> oGrid = new OGrid<>();
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

	public OBasicDataListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public OBasicDataListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public OBasicDataListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public OBasicDataListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public OBasicDataListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public OBasicDataListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public OBasicDataListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<OBasicData> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return oBasicDataService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return oBasicDataService.count(filter);
	}

	@Override
	public IModel<OBasicData> model(OBasicData object) {
		return new WModel<>(object);
	}
}