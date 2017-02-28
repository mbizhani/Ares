package org.devocative.ares.test;

import org.devocative.ares.cmd.CommandCenter;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.command.Command;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.command.ICommandService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.TabularVO;
import org.devocative.ares.vo.filter.command.CommandFVO;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.demeter.entity.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCommandService {
	static ICommandResultCallBack callBack;

	public static void main(String[] args) {

		callBack = new ICommandResultCallBack() {
			@Override
			public void onResult(String lineOfResult) {
				System.out.println("#TEST: " + lineOfResult);
			}
		};

		linux();

		sql();
	}

	private static void sql() {
		OServiceInstance database = new OServiceInstance(
			1521,
			new OServer("My Oracle DB", "172.16.1.133"),
			new OService("Oracle Database")
		);

		Map<String, String> params = new HashMap<>();
		params.put("driver", "oracle.jdbc.driver.OracleDriver");
		params.put("sid", "oradb");

		OSIUser linuxUser = new OSIUser("dm_test", "qazWSX123");
		OServiceInstanceTargetVO targetVO = new OServiceInstanceTargetVO(database, linuxUser, params);
		targetVO.setConnection("jdbc:oracle:thin:@myoracle:1521:oradb");

		CommandCenter cmd = new CommandCenter(new DummyCommandService(), targetVO, callBack);

		cmd.sql("create table t_test (\n" +
			"  id number(19,0),\n" +
			"  c_name varchar2(255 char),\n" +
			"  primary key(id)\n" +
			"  )");

		cmd.sql("insert into t_test(id,c_name) values(1, 'a')");
		cmd.sql("insert into t_test(id,c_name) values(2, 'b')");
		cmd.sql("insert into t_test(id,c_name) values(3, 'c')");

		System.out.println(cmd.sql("select * from t_test"));

		cmd.sql(
			"BEGIN\n" +
				"\tFOR i IN (SELECT us.sequence_name FROM USER_SEQUENCES us) LOOP\n" +
				"\t\tEXECUTE IMMEDIATE 'drop sequence '|| i.sequence_name;\n" +
				"\tEND LOOP;\n" +

				"\tFOR i IN (SELECT ut.table_name FROM USER_TABLES UT) LOOP\n" +
				"\t\tEXECUTE IMMEDIATE 'drop table '|| i.table_name ||' cascade constraints purge';\n" +
				"\tEND LOOP;\n" +
				"END;");

		cmd.closeAll();
	}

	private static void linux() {
		OServiceInstance linux = new OServiceInstance(
			22,
			new OServer("My Oracle Box", "172.16.1.243"),
			new OService("SSH")
		);

		OSIUser linuxUser = new OSIUser("test", "qazWsx@123");
		OServiceInstanceTargetVO targetVO = new OServiceInstanceTargetVO(linux, linuxUser, null);

		CommandCenter cmd = new CommandCenter(null, targetVO, callBack);

		try {
			cmd.ssh("df -h");

			//cmd.ssh("sudo -S su - -c \"service network restart\"", "qazwsx@123");

			cmd.ssh("sudo -S passwd test --stdin", targetVO.getUser().getPassword(), "qazWsx@123");

			TabularVO build = cmd.ssh("osqueryi --csv 'select * from processes'").toTabular("[|]").build();
			System.out.println("build = " + build);
		} catch (Exception e) {
			e.printStackTrace();
		}

		cmd.closeAll();
	}

	private static class DummyCommandService implements ICommandService {
		@Override
		public void saveOrUpdate(Command entity) {
			throw new RuntimeException("NI");
		}

		@Override
		public Command load(Long id) {
			throw new RuntimeException("NI");
		}

		@Override
		public List<Command> list() {
			throw new RuntimeException("NI");
		}

		@Override
		public List<Command> search(CommandFVO filter, long pageIndex, long pageSize) {
			throw new RuntimeException("NI");
		}

		@Override
		public long count(CommandFVO filter) {
			throw new RuntimeException("NI");
		}

		@Override
		public List<OService> getServiceList() {
			throw new RuntimeException("NI");
		}

		@Override
		public List<User> getCreatorUserList() {
			throw new RuntimeException("NI");
		}

		@Override
		public List<User> getModifierUserList() {
			throw new RuntimeException("NI");
		}

		@Override
		public void checkAndSave(OService oService, XCommand xCommand) {
			throw new RuntimeException("NI");
		}

		@Override
		public Object executeCommand(Long commandId, OServiceInstance serviceInstance, Map<String, String> params, ICommandResultCallBack callBack) {
			throw new RuntimeException("NI");
		}

		@Override
		public Connection getConnection(OServiceInstanceTargetVO targetVO) {
			try {
				Class.forName(targetVO.getProp().get("driver"));

				return DriverManager.getConnection(targetVO.getConnection(), targetVO.getUser().getUsername(), targetVO.getUser().getPassword());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
