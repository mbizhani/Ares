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

	public SqlCommandExecutor(OServiceInstanceTargetVO targetVO, CommandCenterResource resource, String prompt,
							  String command, Map<String, Object> params, Map<String, Object> filter) {
		super(targetVO, resource, prompt, command);

		this.params = params;
		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void execute() throws SQLException, ClassNotFoundException {
		Object result = null;
		Connection connection = resource.createConnection(targetVO);

		logger.info("Execute query: si=[{}] sql=[{}]", targetVO, command);
		String p = String.format("[ %s@%s ]$ %s", targetVO.getUsername(), targetVO.getName(), prompt);
		resource.onResult(new CommandOutput(CommandOutput.Type.PROMPT, p));

		NamedParameterStatement nps =
			new NamedParameterStatement(connection, command)
				.setParameters(params);

		if (filter != null) {
			nps.addPlugin(new FilterPlugin().addAll(filter));
		}

		try {
			if (nps.execute()) {
				ResultSet rs = nps.getResultSet();
				QueryVO queryVO = ResultSetProcessor.process(rs, EColumnNameCase.LOWER);

				if (queryVO.getHeader().size() == 1 && queryVO.getRows().size() == 1) {
					result = queryVO.getRows().get(0).get(0);
				} else {
					result = new TabularVO<>(queryVO.getHeader(), queryVO.getRows());
				}
			} else {
				logger.info("Execute non-select query: update count=[{}]", nps.getUpdateCount());
				result = nps.getUpdateCount();
			}
			nps.close();
		} catch (SQLException e) {
			if (isForce()) {
				logger.warn("SqlCommandExecutor", e);
			} else {
				throw e;
			}
		}

		setResult(result);
	}
}
