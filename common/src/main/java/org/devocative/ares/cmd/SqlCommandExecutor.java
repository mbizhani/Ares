package org.devocative.ares.cmd;

import org.devocative.adroit.sql.NamedParameterStatement;
import org.devocative.adroit.sql.plugin.FilterPlugin;
import org.devocative.adroit.sql.result.EColumnNameCase;
import org.devocative.adroit.sql.result.QueryVO;
import org.devocative.adroit.sql.result.ResultSetProcessor;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.TabularVO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SqlCommandExecutor extends AbstractCommandExecutor {
	private Map<String, Object> params, filter;

	private NamedParameterStatement currentNps;

	public SqlCommandExecutor(OServiceInstanceTargetVO targetVO, CommandCenterResource resource, String prompt,
							  String command, Map<String, Object> params, Map<String, Object> filter) {
		super(targetVO, resource, prompt, command);

		this.params = params;
		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void execute() throws SQLException, ClassNotFoundException {
		Connection connection = resource.createConnection(targetVO, isAdmin());

		logger.info("Execute query: si=[{}] sql=[{}]", targetVO, command);
		String p = String.format("[ %s@%s ]$ %s", getProperUsername(), targetVO.getName(), prompt);
		resource.onResult(new CommandOutput(CommandOutput.Type.PROMPT, p));

		currentNps = new NamedParameterStatement(connection, command)
			.setParameters(params);

		if (filter != null) {
			currentNps.addPlugin(new FilterPlugin().addAll(filter));
		}

		Object result;
		try {
			if (currentNps.execute()) {
				ResultSet rs = currentNps.getResultSet();
				QueryVO queryVO = ResultSetProcessor.process(rs, EColumnNameCase.LOWER);
				result = new TabularVO<>(queryVO.getHeader(), queryVO.getRows());
			} else {
				logger.info("Execute non-select query: update count=[{}]", currentNps.getUpdateCount());
				result = currentNps.getUpdateCount();
			}
		} finally {
			currentNps.close();
		}

		setResult(result);
	}

	@Override
	public void cancel() throws Exception {
		if (currentNps != null) {
			currentNps.cancel();
		}
	}
}
