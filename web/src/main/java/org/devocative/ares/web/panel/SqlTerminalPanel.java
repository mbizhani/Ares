package org.devocative.ares.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.devocative.adroit.sql.result.RowVO;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.vo.SqlMessageVO;
import org.devocative.ares.web.TerminalTabInfo;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.DTaskBehavior;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.async.IAsyncResponse;
import org.devocative.wickomp.form.WAjaxButton;
import org.devocative.wickomp.form.code.OCode;
import org.devocative.wickomp.form.code.OCodeMode;
import org.devocative.wickomp.form.code.WCodeInput;
import org.devocative.wickomp.grid.*;
import org.devocative.wickomp.html.WEasyLayout;
import org.devocative.wickomp.opt.OSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class SqlTerminalPanel extends DPanel implements IGridAsyncDataSource<RowVO>, IAsyncResponse {
	private static final Logger logger = LoggerFactory.getLogger(SqlTerminalPanel.class);

	private static final long serialVersionUID = 4232841785147166549L;

	private Long osiUserId;
	private String tabId;
	private Long connectionId;

	private WCodeInput sql;
	private WDataGrid<RowVO> grid;

	@Inject
	private ITerminalConnectionService terminalConnectionService;

	// ------------------------------

	public SqlTerminalPanel(String id, Long osiUserId, String tabId) {
		super(id);

		this.osiUserId = osiUserId;
		this.tabId = tabId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WEasyLayout layout = new WEasyLayout("layout");
		add(layout);

		OCode oCode = new OCode(OCodeMode.SQL)
			.setHeight(OSize.percent(100))
			.setResizable(false);
		sql = new WCodeInput("sql", new Model<>(), oCode);
		Form<Void> form = new Form<>("form");
		form.add(sql);
		form.add(new WAjaxButton("exec") {
			private static final long serialVersionUID = 1902167679817410777L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		layout.add(form);

		OGrid<RowVO> oGrid = new OGrid<>();
		oGrid
			.setPagingBarLayout(Arrays.asList(OPagingButtons.list, OPagingButtons.first, OPagingButtons.prev, OPagingButtons.next))
			.setFit(true)
		;

		grid = new WDataGrid<>("grid", oGrid, this);
		grid
			.setAutomaticColumns(true)
			.setIgnoreDataSourceCount(true)
			.setEnabled(false);
		layout.add(grid);

		DTaskBehavior dtb = new DTaskBehavior(this);
		add(dtb);

		connectionId = terminalConnectionService.createTerminal(osiUserId, dtb);
		if (tabId != null) {
			send(this, Broadcast.BUBBLE, new TerminalTabInfo(tabId, connectionId));
		}
	}

	// --------------- IGridAsyncDataSource<RowVO>

	@Override
	public void asyncList(long pageIndex, long pageSize, List<WSortField> sortFields) {
		String sql = this.sql.getModelObject();
		logger.debug("SqlTerminalPanel: Client send query, sql=[{}]", sql);
		terminalConnectionService.sendMessage(connectionId, new SqlMessageVO(sql, pageIndex, pageSize));
	}

	@Override
	public IModel<RowVO> model(RowVO object) {
		return new WModel<>(object);
	}

	// --------------- ITaskResultCallback

	@Override
	public void onAsyncResult(IPartialPageRequestHandler handler, Object result) {
		List<RowVO> list = (List<RowVO>) result;
		grid.pushData(handler, list, 1000);
	}

	@Override
	public void onAsyncError(IPartialPageRequestHandler handler, Exception e) {
		grid.pushError(handler, e);
	}
}
