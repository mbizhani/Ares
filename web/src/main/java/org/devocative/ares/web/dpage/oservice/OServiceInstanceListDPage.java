package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.vo.filter.oservice.OServiceInstanceFVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.demeter.web.model.DEntityLazyLoadModel;
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
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class OServiceInstanceListDPage extends DPage implements IGridDataSource<OServiceInstance> {
	private static final long serialVersionUID = 1641066990L;

	@Inject
	private IOServiceInstanceService oServiceInstanceService;

	private OServiceInstanceFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<OServiceInstance> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public OServiceInstanceListDPage(String id) {
		this(id, Collections.<String>emptyList(), new OServiceInstanceFVO());
	}

	// Panel Call - Open Filter
	public OServiceInstanceListDPage(String id, OServiceInstanceFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public OServiceInstanceListDPage(String id, List<String> params) {
		this(id, params, new OServiceInstanceFVO());
	}

	// Main Constructor
	private OServiceInstanceListDPage(String id, List<String> params, OServiceInstanceFVO filter) {
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
			private static final long serialVersionUID = 641718193L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new OServiceInstanceFormDPage(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.OServiceInstanceAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("OServiceInstance.name")));
		floatTable.add(new WNumberRangeInput("port", Integer.class)
			.setLabel(new ResourceModel("OServiceInstance.port")));
		floatTable.add(new WSelectionInput("server", oServiceInstanceService.getServerList(), true)
			.setLabel(new ResourceModel("OServiceInstance.server")));
		floatTable.add(new WSelectionInput("service", oServiceInstanceService.getServiceList(), true)
			.setLabel(new ResourceModel("OServiceInstance.service")));
		floatTable.add(new WSelectionInput("allowedUsers", oServiceInstanceService.getAllowedUsersList(), true)
			.setLabel(new ResourceModel("OServiceInstance.allowedUsers")));
		floatTable.add(new WSelectionInput("allowedRoles", oServiceInstanceService.getAllowedRolesList(), true)
			.setLabel(new ResourceModel("OServiceInstance.allowedRoles")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", oServiceInstanceService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", oServiceInstanceService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<OServiceInstanceFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = 2018179286L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<OServiceInstance> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("OServiceInstance.name"), "name"));
		columnList.add(new OPropertyColumn<OServiceInstance>(new ResourceModel("OServiceInstance.port"), "port")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("OServiceInstance.server"), "server"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("OServiceInstance.service"), "service"));
		columnList.add(new OPropertyColumn<OServiceInstance>(new ResourceModel("OServiceInstance.propertyValues"), "propertyValues")
			.setWidth(OSize.fixed(300)));
		columnList.add(new OPropertyColumn<OServiceInstance>(new ResourceModel("OServiceInstance.allowedUsers"), "allowedUsers")
			.setWidth(OSize.fixed(200)));
		columnList.add(new OPropertyColumn<OServiceInstance>(new ResourceModel("OServiceInstance.allowedRoles"), "allowedRoles")
			.setWidth(OSize.fixed(200)));
		columnList.add(new OPropertyColumn<OServiceInstance>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<OServiceInstance>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<OServiceInstance>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(AresPrivilegeKey.OServiceInstanceEdit)) {
			columnList.add(new OEditAjaxColumn<OServiceInstance>() {
				private static final long serialVersionUID = 746039663L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<OServiceInstance> rowData) {
					window.setContent(new OServiceInstanceFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		if (hasPermission(AresPrivilegeKey.OSIUserAdd)) {
			columnList.add(new OAjaxLinkColumn<OServiceInstance>(new Model<>(), AresIcon.ADD_USER.setTooltip(new Model<>("Add Service Instance User"))) {
				private static final long serialVersionUID = 2390378199618608413L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<OServiceInstance> rowData) {
					window.setContent(new OSIUserFormDPage(window.getContentId(), new OSIUser(rowData.getObject())));
					window.show(target);
				}
			});
		}


		OGrid<OServiceInstance> oGrid = new OGrid<>();
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

	public OServiceInstanceListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public OServiceInstanceListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public OServiceInstanceListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public OServiceInstanceListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public OServiceInstanceListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public OServiceInstanceListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public OServiceInstanceListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<OServiceInstance> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return oServiceInstanceService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return oServiceInstanceService.count(filter);
	}

	@Override
	public IModel<OServiceInstance> model(OServiceInstance object) {
		return new DEntityLazyLoadModel<>(object.getId(), oServiceInstanceService);
	}
}