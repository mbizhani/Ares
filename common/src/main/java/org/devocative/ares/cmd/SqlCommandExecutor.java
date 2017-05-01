package org.devocative.ares.cmd;

import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.TabularVO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlCommandExecutor extends AbstractCommandExecutor {

	public SqlCommandExecutor(OServiceInstanceTargetVO targetVO, CommandCenterResource resource, String prompt, String command) {
		super(targetVO, resource, prompt, command);
	}

	// ------------------------------

	@Override
	protected void execute() throws SQLException, ClassNotFoundException {
		Object result;
		Connection connection = resource.createConnection(targetVO);

		logger.info("Execute query: si=[{}] sql=[{}]", targetVO, command);
		String p = String.format("[ %s@%s ]$ %s", targetVO.getUsername(), targetVO.getName(), prompt);
		resource.onResult(new CommandOutput(CommandOutput.Type.PROMPT, p));

		Statement statement = connection.createStatement();
		if (statement.execute(command)) {
			ResultSet rs = statement.getResultSet();
			ResultSetMetaData metaData = rs.getMetaData();

			List<String> columns = new ArrayList<>();
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				columns.add(metaData.getColumnName(i).toLowerCase());
			}

			List<List<Object>> rows = new ArrayList<>();
			while (rs.next()) {
				List<Object> row = new ArrayList<>();
				for (int i = 0; i < columns.size(); i++) {
					String column = columns.get(i);
					Object value;
					switch (metaData.getColumnType(i + 1)) {
						case Types.DATE:
							value = rs.getDate(column);
							break;
						case Types.TIME:
							value = rs.getTime(column);
							break;
						case Types.TIMESTAMP:
							value = rs.getTimestamp(column);
							break;
						default:
							value = rs.getObject(column);
					}
					row.add(value);
				}
				rows.add(row);
			}

			if (columns.size() == 1 && rows.size() == 1) {
				result = rows.get(0).get(0);
			} else {
				result = new TabularVO<>(columns, rows);
			}
		} else {
			logger.info("Execute non-select query: update count=[{}]", statement.getUpdateCount());
			result = statement.getUpdateCount();
		}
		statement.close();

		setResult(result);
	}
}
