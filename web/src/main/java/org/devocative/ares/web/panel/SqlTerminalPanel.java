package org.devocative.ares.web.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.devocative.adroit.sql.result.RowVO;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.vo.SqlMessageVO;
import org.devocative.ares.web.AresIcon;
import org.devocative.ares.web.TerminalTabInfo;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.DTaskBehavior;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.WebUtil;
import org.devocative.wickomp.async.IAsyncResponse;
import org.devocative.wickomp.form.code.OCode;
import org.devocative.wickomp.form.code.OCodeMode;
import org.devocative.wickomp.form.code.WCodeInput;
import org.devocative.wickomp.grid.*;
import org.devocative.wickomp.html.WAjaxLink;
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
	private DAjaxButton exec;
	private WAjaxLink cancel;

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
			.setSubmitSelection(true)
			.setHeight(OSize.percent(100))
			.setResizable(false);
		sql = new WCodeInput("sql", new Model<>(), oCode);
		sql.setRequired(true);

		Form<Void> form = new Form<>("form");
		form.add(sql);

		form.add(exec = new DAjaxButton("exec", AresIcon.EXECUTE) {
			private static final long serialVersionUID = 1902167679817410777L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);

				exec.setEnabled(false);
				target.add(exec);

				cancel.setEnabled(true);
				target.add(cancel);
			}
		});
		exec.setOutputMarkupId(true);

		form.add(cancel = new WAjaxLink("cancel", AresIcon.STOP_CIRCLE) {
			private static final long serialVersionUID = 599873897224508537L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				terminalConnectionService.sendMessage(connectionId, new SqlMessageVO(SqlMessageVO.MsgType.CANCEL));

				exec.setEnabled(true);
				target.add(exec);
			}
		});
		cancel.setEnabled(false);

		form.add(new WebMarkupContainer("clear").add(new AttributeModifier("onclick", sql.getClearJSCall())));

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

		connectionId = terminalConnectionService.createTerminal(osiUserId, null, dtb);
		if (tabId != null) {
			send(this, Broadcast.BUBBLE, new TerminalTabInfo(tabId, connectionId));
		}
	}

	@Override
	protected void onAfterRender() {
		super.onAfterRender();

		String scriptF9 = String.format("$('#%s').keydown(function(e){if(e.keyCode==120) $('#%s').click();});",
			sql.getMarkupId(), exec.getMarkupId());
		WebUtil.writeJQueryCall(scriptF9, true);
	}

	// --------------- IGridAsyncDataSource<RowVO>

	@Override
	public void asyncList(long pageIndex, long pageSize, List<WSortField> sortFields) {
		String sqlTxt = sql.getModelObject();
		logger.debug("SqlTerminalPanel: Client send query, sql=[{}]", sqlTxt);
		terminalConnectionService.sendMessage(connectionId, new SqlMessageVO(sqlTxt, pageIndex, pageSize));
	}

	@Override
	public IModel<RowVO> model(RowVO object) {
		return new WModel<>(object);
	}

	// --------------- ITaskResultCallback

	@Override
	public void onAsyncResult(IPartialPageRequestHandler handler, Object result) {
		cancel.setEnabled(false);
		handler.add(cancel);

		exec.setEnabled(true);
		handler.add(exec);

		List<RowVO> list = (List<RowVO>) result;
		grid.pushData(handler, list, 1000);
	}

	@Override
	public void onAsyncError(IPartialPageRequestHandler handler, Exception e) {
		cancel.setEnabled(false);
		handler.add(cancel);

		exec.setEnabled(true);
		handler.add(exec);

		grid.pushError(handler, e);
	}
}
