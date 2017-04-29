package org.devocative.ares.cmd;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ShellCommandExecutor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ShellCommandExecutor.class);

	private OServiceInstanceTargetVO targetVO;
	private JSch jSch;
	private Session session;
	private ICommandResultCallBack resultCallBack;
	private String cmd;
	private String[] stdin;

	// ---------------

	private StringBuilder result = new StringBuilder();
	private int exitStatus = -1;
	private Exception exception;

	// ------------------------------

	public ShellCommandExecutor(OServiceInstanceTargetVO targetVO, JSch jSch, Session session, ICommandResultCallBack resultCallBack, String cmd, String[] stdin) {
		this.targetVO = targetVO;
		this.jSch = jSch;
		this.session = session;
		this.resultCallBack = resultCallBack;
		this.cmd = cmd;
		this.stdin = stdin;
	}

	// ------------------------------

	@Override
	public void run() {
		try {
			execute();
		} catch (Exception e) {
			exception = e;
		}
	}

	public Session getSession() {
		return session;
	}

	public String getResult() {
		return result.toString();
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public Exception getException() {
		return exception;
	}

	public boolean hasException() {
		return exception != null;
	}

	// ------------------------------

	private void execute() throws JSchException, IOException {
		if (session == null) {
			logger.info("Try to get SSH connection: {}", targetVO.getName());
			resultCallBack.onResult(new CommandOutput(CommandOutput.Type.PROMPT, "connecting ..."));

			session = jSch.getSession(targetVO.getUsername(), targetVO.getAddress(), targetVO.getPort());
			session.setPassword(targetVO.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000); // making a connection with timeout.
		}

		String finalCmd = cmd;
		if (targetVO.isSudoer() && !cmd.startsWith("sudo -S")) {
				/*
				NOTE: in /etc/sudoers the line
				Defaults    requiretty
				must be commented, unless sudo -S does not work!
				*/
			finalCmd = String.format("sudo -S -p '' %s", cmd);
			cmd = String.format("sudo -S %s", cmd);
		}

		logger.info("Sending SSH Command: cmd=[{}] si=[{}]", finalCmd, targetVO);
		String prompt = String.format("[%s@%s]$ %s", targetVO.getUsername(), targetVO.getAddress(), cmd);
		resultCallBack.onResult(new CommandOutput(CommandOutput.Type.PROMPT, prompt));

		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(finalCmd);
		channelExec.setInputStream(null);
		channelExec.setErrStream(null);

		InputStream in = channelExec.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		InputStream err = channelExec.getErrStream();
		BufferedReader errBr = new BufferedReader(new InputStreamReader(err));

		OutputStream out = channelExec.getOutputStream();

		channelExec.connect();

		if (targetVO.isSudoer()) {
			out.write((targetVO.getPassword() + "\n").getBytes());
			out.flush();
		}

		for (String s : stdin) {
			if (s != null) {
				out.write((s + "\n").getBytes());
				out.flush();
			}
		}

		while (true) {
			String line;
			while ((line = br.readLine()) != null) {
				resultCallBack.onResult(new CommandOutput(line));
				logger.debug("\tResult = {}", line);
				result.append(line).append("\n");
			}
			if (channelExec.isClosed()) {
				exitStatus = channelExec.getExitStatus();
				break;
			}
		}

		while (true) {
			String line;
			while ((line = errBr.readLine()) != null) {
				resultCallBack.onResult(new CommandOutput(CommandOutput.Type.ERROR, line));
				logger.error("\tResult = {}", line);
			}
			if (channelExec.isClosed()) {
				break;
			}
		}

		channelExec.disconnect();
	}
}
