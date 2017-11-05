//overwrite
package org.devocative.ares.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.EServerOS;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.vo.filter.OServerFVO;
import org.devocative.ares.web.AresIcon;
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
import org.devocative.wickomp.opt.IStyler;
import org.devocative.wickomp.opt.OSize;
import org.devocative.wickomp.opt.OStyle;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class OServerListDPage extends DPage implements IGridDataSource<OServer> {
	private static final long serialVersionUID = -1874204417L;

	@Inject
	private IOServerService oServerService;

	private OServerFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<OServer> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public OServerListDPage(String id) {
		this(id, Collections.<String>emptyList(), new OServerFVO());
	}

	// Panel Call - Open Filter
	public OServerListDPage(String id, OServerFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public OServerListDPage(String id, List<String> params) {
		this(id, params, new OServerFVO());
	}

	// Main Constructor
	private OServerListDPage(String id, List<String> params, OServerFVO filter) {
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
			private static final long serialVersionUID = 1460678018L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new OServerFormDPage(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.OServerAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("OServer.name")));
		floatTable.add(new WTextInput("address")
			.setLabel(new ResourceModel("OServer.address")));
		floatTable.add(new WSelectionInput("function", oServerService.getFunctionList(), true)
			.setLabel(new ResourceModel("OServer.function")));
		floatTable.add(new WNumberRangeInput("counter", Integer.class)
			.setLabel(new ResourceModel("OServer.counter")));
		floatTable.add(new WSelectionInput("environment", oServerService.getEnvironmentList(), true)
			.setLabel(new ResourceModel("OServer.environment")));
		floatTable.add(new WSelectionInput("location", oServerService.getLocationList(), true)
			.setLabel(new ResourceModel("OServer.location")));
		floatTable.add(new WSelectionInput("company", oServerService.getCompanyList(), true)
			.setLabel(new ResourceModel("OServer.company")));
		floatTable.add(new WTextInput("vmId")
			.setLabel(new ResourceModel("OServer.vmId")));
		floatTable.add(new WSelectionInput("serverOS", EServerOS.list(), true)
			.setLabel(new ResourceModel("OServer.serverOS")));
		floatTable.add(new WSelectionInput("hypervisor", oServerService.getHypervisorList(), true)
			.setLabel(new ResourceModel("OServer.hypervisor")));
		floatTable.add(new WSelectionInput("owner", oServerService.getOwnerList(), true)
			.setLabel(new ResourceModel("OServer.owner")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", oServerService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", oServerService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<OServerFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = -1235411353L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<OServer> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.name"), "name"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.address"), "address"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.function"), "function"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.counter"), "counter")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.environment"), "environment"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.location"), "location"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.company"), "company"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.vmId"), "vmId"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.serverOS"), "serverOS"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.hypervisor"), "hypervisor"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("OServer.owner"), "owner"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<OServer>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(AresPrivilegeKey.OServerEdit)) {
			columnList.add(new OEditAjaxColumn<OServer>() {
				private static final long serialVersionUID = 585801344L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<OServer> rowData) {
					window.setContent(new OServerFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		OGrid<OServer> oGrid = new OGrid<>();
		oGrid
			.setColumns(columnList)
			.setMultiSort(false)
			.setRowStyler((IStyler<OServer> & Serializable) (bean, id) ->
				OStyle.style(bean.getHypervisorId() == null ? "color:blue" : null))
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

	public OServerListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public OServerListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public OServerListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public OServerListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public OServerListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public OServerListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public OServerListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<OServer> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return oServerService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return oServerService.count(filter);
	}

	@Override
	public IModel<OServer> model(OServer object) {
		return new WModel<>(object);
	}
}