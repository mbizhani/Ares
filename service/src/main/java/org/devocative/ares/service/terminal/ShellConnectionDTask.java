package org.devocative.ares.service.terminal;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.devocative.adroit.ConfigUtil;
import org.devocative.ares.AresConfigKey;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.SshMessageVO;
import org.devocative.ares.vo.TerminalConnectionVO;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

@Scope("prototype")
@Component("arsShellConnectionDTask")
public class ShellConnectionDTask extends DTask<String> implements ITerminalProcess {
	private static final Logger logger = LoggerFactory.getLogger(ShellConnectionDTask.class);

	@Autowired
	private ITerminalConnectionService terminalConnectionService;

	@Autowired
	private ISecurityService securityService;

	private long connId;
	private SshMessageVO initConfig;
	private OServiceInstanceTargetVO targetVO;

	private JSch jsch;
	private Session session;
	private ChannelShell channel;
	private PrintStream commander;
	private ShellTextProcessor processor;
	private long lastActivityTime;

	// ------------------------------

	@Override
	public void init() {
		jsch = new JSch();
		TerminalConnectionVO terminalConnectionVO = (TerminalConnectionVO) getInputData();
		connId = terminalConnectionVO.getConnectionId();
		initConfig = (SshMessageVO) terminalConnectionVO.getInitConfig();
		targetVO = terminalConnectionVO.getTargetVO();
		lastActivityTime = System.currentTimeMillis();
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() throws Exception {
		logger.info("ShellConnectionDTask: starting SSH currentUser=[{}] connId=[{}] osiUser=[{}]",
			securityService.getCurrentUser(), connId, targetVO.getUser().getUsername());

		try {
			session = jsch.getSession(targetVO.getUsername(), targetVO.getAddress(), targetVO.getPort());

			session.setPassword(targetVO.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000);

			channel = (ChannelShell) session.openChannel("shell");
			channel.setPtyType("xterm");
			if (initConfig != null && initConfig.getSize() != null) {
				SshMessageVO.Size size = initConfig.getSize();
				channel.setPtySize(size.getCols(), size.getRows(), size.getWidth(), size.getHeight());
			}
			processor = new ShellTextProcessor(connId, securityService.getCurrentUser().getUsername());

			InputStream in = channel.getInputStream();
			OutputStream out = channel.getOutputStream();

			channel.connect();

			commander = new PrintStream(out, true);

			new SshServerReader(in).run();
		} finally {
			terminalConnectionService.closeConnection(connId);
		}
	}

	@Override
	public void cancel() throws Exception {
		if (channel != null && channel.isConnected()) {
			channel.disconnect();
		}
		if (session != null && session.isConnected()) {
			session.disconnect();
		}
		sendResult("\n\nTerminal Closed!");
	}

	// ---------------

	@Override
	public long getConnectionId() {
		return connId;
	}

	@Override
	public void send(Object message) {
		SshMessageVO sshMsg = (SshMessageVO) message;
		lastActivityTime = System.currentTimeMillis();
		//logger.debug("ShellConnectionDTask.send: txt={} webSpecialKey={}", txt, specialKey);
		try {
			if (sshMsg.getText() != null) {
				processor.onClientText(sshMsg.getText());
				commander.print(sshMsg.getText());
			} else if (sshMsg.getSpecialKey() != null) {
				processor.onClientSpecialKey(sshMsg.getSpecialKey());
				byte[] shellCode = EShellSpecialKey.findShellCode(sshMsg.getSpecialKey());
				if (shellCode != null) {
					commander.write(shellCode);
				}
			} else if (sshMsg.getSize() != null) {
				SshMessageVO.Size size = sshMsg.getSize();
				channel.setPtySize(size.getCols(), size.getRows(), size.getWidth(), size.getHeight());
			} else {
				throw new RuntimeException("Invalid SshMessageVO");
			}
		} catch (IOException e) {
			logger.error("ShellConnectionDTask.send: connId=" + connId, e);

			if (channel.isClosed()) {
				logger.warn("Channel closed: connId=[{}]", connId);
				terminalConnectionService.closeConnection(connId);
			}
		}
	}

	@Override
	public long getLastActivityTime() {
		return lastActivityTime;
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	// ------------------------------

	private class SshServerReader implements Runnable {
		private InputStream in;

		public SshServerReader(InputStream in) {
			this.in = in;
		}

		@Override
		public void run() {
			try {
				while (true) {
					int read;
					byte[] buff = new byte[1024];
					while ((read = in.read(buff)) != -1) {
						if (ConfigUtil.getBoolean(AresConfigKey.ShellResponseResetExpiration)) {
							lastActivityTime = System.currentTimeMillis();
						}

						String line = new String(buff, 0, read);
						processor.onServerText(line);
						sendResult(line);

						/*if (line.startsWith(SUDO_PROMPT) && isSudoPasswordMode) {
							System.out.println("SUDO");
							send("qweasd@123");
							send(13);
						}*/
						//Thread.sleep(100);
					}

					if (channel.isClosed()) {
						logger.warn("Channel closed: connId=[{}]", connId);
						terminalConnectionService.closeConnection(connId);
						break;
					}
				}
			} catch (IOException e) {
				sendError(e);

				if (channel.isClosed()) {
					logger.warn("Channel closed: connId=[{}]", connId);
					terminalConnectionService.closeConnection(connId);
				}
			}
		}
	}
}
