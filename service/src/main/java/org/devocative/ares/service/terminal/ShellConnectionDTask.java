package org.devocative.ares.service.terminal;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.devocative.ares.iservice.IAsyncTextResult;
import org.devocative.ares.iservice.ITerminalConnectionService;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.ares.vo.ShellConnectionVO;
import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;

@Scope("prototype")
@Component("arsShellConnectionDTask")
public class ShellConnectionDTask extends DTask implements ITerminalProcess {
	private static final Logger logger = LoggerFactory.getLogger(ShellConnectionDTask.class);

	@Autowired
	private ITerminalConnectionService connectionService;

	private Long connId;
	private OServiceInstanceTargetVO targetVO;
	private IAsyncTextResult asyncTextResult;

	private JSch jsch;
	private Session session;
	private ChannelShell channel;
	private PrintStream commander;
	private ShellTextProcessor processor;

	// ------------------------------

	@Override
	public void init() {
		jsch = new JSch();
		ShellConnectionVO shellConnectionVO = (ShellConnectionVO) getInputData();
		connId = shellConnectionVO.getConnectionId();
		targetVO = shellConnectionVO.getTargetVO();
		asyncTextResult = shellConnectionVO.getTextResult();
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() {
		try {
			session = jsch.getSession(targetVO.getUsername(), targetVO.getAddress(), targetVO.getPort());

			session.setPassword(targetVO.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000);

			channel = (ChannelShell) session.openChannel("shell");
			channel.setPtyType("xterm");
			processor = new ShellTextProcessor();

			InputStream in = channel.getInputStream();
			OutputStream out = channel.getOutputStream();

			channel.connect();

			commander = new PrintStream(out, true);

			new SshServerReader(in).run();
		} catch (Exception e) {
			asyncTextResult.onMessage("ERROR: " + e.getMessage());
		}
	}

	// ---------------

	@Override
	public void send(String txt, Integer specialKey) {
		//logger.debug("ShellConnectionDTask.send: txt={} webSpecialKey={}", txt, specialKey);
		try {
			if (txt != null) {
				processor.onClientText(txt);
				commander.print(txt);
			} else {
				processor.onClientSpecialKey(specialKey);
				byte[] shellCode = EShellSpecialKey.findShellCode(specialKey);
				if (shellCode != null) {
					commander.write(shellCode);
				}
			}
		} catch (IOException e) {
			logger.error("ShellConnectionDTask.send: connId=" + connId, e);

			if (channel.isClosed()) {
				logger.warn("Channel closed: connId=[{}]", connId);
				connectionService.closeConnection(connId);
			}
		}
	}

	@Override
	public void close() {
		if (channel.isConnected()) {
			channel.disconnect();
		}
		if (session.isConnected()) {
			session.disconnect();
		}
	}

	// ------------------------------

	private class SshServerReader implements Runnable {
		private InputStream in;

		public SshServerReader(InputStream in) {
			this.in = in;
		}

		@Override
		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			try {
				while (true) {
					char[] buff = new char[1024];
					int read;
					while ((read = br.read(buff)) != -1) {
						String line = new String(buff, 0, read);
						//logger.debug("Ssh Server Res: {}", line);
						processor.onServerText(line);
						asyncTextResult.onMessage(line);

						/*if (line.startsWith(SUDO_PROMPT) && isSudoPasswordMode) {
							System.out.println("SUDO");
							send("qweasd@123");
							send(13);
						}*/
						//Thread.sleep(100);
					}

					if (channel.isClosed()) {
						logger.warn("Channel closed: connId=[{}]", connId);
						connectionService.closeConnection(connId);
						break;
					}
				}
			} catch (IOException e) {
				asyncTextResult.onMessage("ERR: " + e.getMessage());

				if (channel.isClosed()) {
					logger.warn("Channel closed: connId=[{}]", connId);
					connectionService.closeConnection(connId);
				}
			}
		}
	}
}
