package org.devocative.ares.test;

import org.devocative.ares.cmd.CommandCenter;
import org.devocative.ares.cmd.ICommandResultCallBack;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.vo.OServiceInstanceTargetVO;

public class TestCommandService {
	public static void main(String[] args) {

		ICommandResultCallBack callBack = new ICommandResultCallBack() {
			@Override
			public void onResult(String lineOfResult) {
				System.out.println("#TEST: " + lineOfResult);
			}
		};

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

			cmd.ssh("sudo -S passwd test --stdin", "qazWsx@123", "qazWsx@123");
		} catch (Exception e) {
			e.printStackTrace();
		}

		cmd.closeAll();
	}
}
