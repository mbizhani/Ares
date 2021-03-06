package org.devocative.ares.web.dpage.oservice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.ares.AresPrivilegeKey;
import org.devocative.ares.entity.oservice.ERemoteMode;
import org.devocative.ares.entity.oservice.ESIUserType;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.iservice.oservice.IOSIUserService;
import org.devocative.ares.vo.filter.oservice.OSIUserFVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.dpage.OServerFormDPage;
import org.devocative.demeter.entity.ERowMode;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.demeter.web.component.grid.ORowModeAjaxColumn;
import org.devocative.demeter.web.component.grid.ORowModeChangeAjaxColumn;
import org.devocative.demeter.web.model.DEntityLazyLoadModel;
import org.devocative.wickomp.WebUtil;
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
import org.devocative.wickomp.grid.toolbar.OExportExcelButton;
import org.devocative.wickomp.grid.toolbar.OGridGroupingButton;
import org.devocative.wickomp.html.Anchor;
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.icon.FontAwesome;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.IStyler;
import org.devocative.wickomp.opt.OSize;
import org.devocative.wickomp.opt.OStyle;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class OSIUserListDPage extends DPage implements IGridDataSource<OSIUser> {
	private static final long serialVersionUID = -2116475023L;

	@Inject
	private IOSIUserService oSIUserService;

	private OSIUserFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<OSIUser> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public OSIUserListDPage(String id) {
		this(id, Collections.<String>emptyList(), new OSIUserFVO());
	}

	// Panel Call - Open Filter
	public OSIUserListDPage(String id, OSIUserFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public OSIUserListDPage(String id, List<String> params) {
		this(id, params, new OSIUserFVO());
	}

	// Main Constructor
	private OSIUserListDPage(String id, List<String> params, OSIUserFVO filter) {
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
			private static final long serialVersionUID = -1828616844L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new OSIUserFormDPage(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(AresPrivilegeKey.OSIUserAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("username")
			.setLabel(new ResourceModel("OSIUser.username", "username")));
		floatTable.add(new WSelectionInput("type", ESIUserType.list(), true)
			.setLabel(new ResourceModel("OSIUser.type", "type")));
		floatTable.add(new WBooleanInput("enabled")
			.setLabel(new ResourceModel("OSIUser.enabled", "enabled")));
		floatTable.add(new WSelectionInput("remoteMode", ERemoteMode.list(), true)
			.setLabel(new ResourceModel("OSIUser.remoteMode", "remoteMode")));
		floatTable.add(new WSelectionInput("serviceInstance", oSIUserService.getServiceInstanceList(), true)
			.setLabel(new ResourceModel("OSIUser.serviceInstance", "serviceInstance")));
		floatTable.add(new WSelectionInput("server", oSIUserService.getServerList(), true)
			.setLabel(new ResourceModel("OSIUser.server", "server")));
		floatTable.add(new WSelectionInput("service", oSIUserService.getServiceList(), true)
			.setLabel(new ResourceModel("OSIUser.service", "service")));
		floatTable.add(new WSelectionInput("allowedUsers", oSIUserService.getAllowedUsersList(), true)
			.setLabel(new ResourceModel("OSIUser.allowedUsers", "allowedUsers")));
		floatTable.add(new WSelectionInput("allowedRoles", oSIUserService.getAllowedRolesList(), true)
			.setLabel(new ResourceModel("OSIUser.allowedRoles", "allowedRoles")));
		floatTable.add(new WSelectionInput("rowMode", ERowMode.list(), true)
			.setLabel(new ResourceModel("entity.rowMode", "rowMode"))
			.setVisible(getCurrentUser().isRoot()));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate", "creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", oSIUserService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser", "creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate", "modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", oSIUserService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser", "modifierUser")));

		Form<OSIUserFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), AresIcon.SEARCH) {
			private static final long serialVersionUID = -1151843879L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<OSIUser> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("OSIUser.username", "username"), "username"));
		columnList.add(new OPropertyColumn<OSIUser>(new ResourceModel("OSIUser.type", "type"), "type")
			.setCellStyler((IStyler<OSIUser> & Serializable) (bean, id) -> {
				String color;
				switch (bean.getType()) {
					case Normal:
						color = "inherit";
						break;
					case Executor:
						color = "#32cd32";
						break;
					case Admin:
						color = "red";
						break;
					default:
						color = "inherit";
				}
				return OStyle.style("color: " + color);
			}));
		columnList.add(new OPropertyColumn<OSIUser>(new ResourceModel("OSIUser.enabled", "enabled"), "enabled")
			.setFormatter(OBooleanFormatter.bool())
			.setCellStyler((IStyler<OSIUser> & Serializable) (bean, id) -> OStyle.style(bean.getEnabled() ? "color: #32cd32" : "color: red")));
		columnList.add(new OPropertyColumn<>(new ResourceModel("OSIUser.remoteMode", "remoteMode"), "remoteMode"));
		columnList.add(new OAjaxLinkColumn<OSIUser>(new ResourceModel("OSIUser.siName", "SI Name"), "serviceInstance.name") {
			@Override
			public void onClick(AjaxRequestTarget target, IModel<OSIUser> rowData) {
				window.setContent(new OServiceInstanceFormDPage(window.getContentId(), rowData.getObject().getServiceInstance())
					.setReadOnly(!hasPermission(AresPrivilegeKey.OServiceInstanceEdit)));
				window.show(target);
			}

			@Override
			protected void fillAnchor(Anchor anchor, OSIUser bean, String id, int colNo, String url) {
				if (bean.getServiceInstance().getName() == null) {
					anchor.addChild(AresIcon.EXTERNAL_LINK.setTooltip(null));
				}
				super.fillAnchor(anchor, bean, id, colNo, url);
			}
		});
		columnList.add(new OAjaxLinkColumn<OSIUser>(new ResourceModel("OSIUser.server", "server"), "server") {
			@Override
			public void onClick(AjaxRequestTarget target, IModel<OSIUser> rowData) {
				window.setContent(new OServerFormDPage(window.getContentId(), rowData.getObject().getServer())
					.setReadOnly(!hasPermission(AresPrivilegeKey.OServerEdit)));
				window.show(target);
			}
		});
		columnList.add(new OPropertyColumn<>(new ResourceModel("OSIUser.service", "service"), "service"));
		columnList.add(new OPropertyColumn<OSIUser>(new ResourceModel("OSIUser.allowedUsers", "allowedUsers"), "allowedUsers")
			.setWidth(OSize.fixed(200)));
		columnList.add(new OPropertyColumn<OSIUser>(new ResourceModel("OSIUser.allowedRoles", "allowedRoles"), "allowedRoles")
			.setWidth(OSize.fixed(200)));
		if (getCurrentUser().isRoot()) {
			columnList.add(new OPropertyColumn<>(new ResourceModel("entity.rowMode", "rowMode"), "rowMode"));
		}
		columnList.add(new OPropertyColumn<OSIUser>(new ResourceModel("entity.creationDate", "creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser", "creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<OSIUser>(new ResourceModel("entity.modificationDate", "modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser", "modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<OSIUser>(new ResourceModel("entity.version", "version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(AresPrivilegeKey.OSIUserEdit)) {
			columnList.add(new OEditAjaxColumn<OSIUser>() {
				private static final long serialVersionUID = 635615474L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<OSIUser> rowData) {
					window.setContent(new OSIUserFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		if (getCurrentUser().isRoot()) {
			columnList.add(new ORowModeChangeAjaxColumn<>(window));
		}

		if (hasPermission(AresPrivilegeKey.OSIUserShowPassword)) {
			columnList.add(new ORowModeAjaxColumn<OSIUser>(new Model<>(), AresIcon.EYE.setTooltip(new ResourceModel("OSIUser.showPassword", "Show Password"))) {
				private static final long serialVersionUID = 635615474L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<OSIUser> rowData) {
					String password = oSIUserService.getPassword(rowData.getObject().getId());
					WebUtil.copyToClipboard(password, target);
				}
			}.setField("VIEW_PASS"));
		}

		OGrid<OSIUser> oGrid = new OGrid<>();
		oGrid
			.setColumns(columnList)
			.setMultiSort(false)
			.addToolbarButton(new OGridGroupingButton<>(new FontAwesome("expand"), new FontAwesome("compress")))
			.addToolbarButton(new OExportExcelButton<>(new FontAwesome("file-excel-o", new Model<>("Export to excel")).setColor("green"), this))
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

	public OSIUserListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public OSIUserListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public OSIUserListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public OSIUserListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public OSIUserListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public OSIUserListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public OSIUserListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<OSIUser> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return oSIUserService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return oSIUserService.count(filter);
	}

	@Override
	public IModel<OSIUser> model(OSIUser object) {
		return new DEntityLazyLoadModel<>(object.getId(), oSIUserService);
	}
}