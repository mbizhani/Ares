package org.devocative.ares.cmd;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.devocative.ares.vo.OServiceInstanceTargetVO;

import java.io.*;

/**
 * Since in JSch library, when Channel.disconnect() is called,
 * the current thread is modified and the data in ThreadLocal, like currentUser
 * are deleted. So by this class, ssh command execution is isolated!
 */
public class ShellCommandExecutor extends AbstractCommandExecutor {
	private JSch jSch;
	private Session session;
	private String[] stdin;

	// ---------------

	private int exitStatus = -1;

	// ------------------------------

	public ShellCommandExecutor(OServiceInstanceTargetVO targetVO, ICommandResultCallBack resultCallBack, String command, JSch jSch, Session session, String[] stdin) {
		super(targetVO, resultCallBack, command);

		this.jSch = jSch;
		this.session = session;
		this.stdin = stdin;
	}

	// ------------------------------

	public Session getSession() {
		return session;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	// ------------------------------

	@Override
	protected void execute() throws JSchException, IOException {
		if (session == null) {
			logger.info("Try to get SSH connection: {}", targetVO.getName());
			resultCallBack.onResult(new CommandOutput(CommandOutput.Type.PROMPT, "connecting ..."));

			session = jSch.getSession(targetVO.getUsername(), targetVO.getAddress(), targetVO.getPort());
			session.setPassword(targetVO.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000); // making a connection with timeout.
		}

		String finalCmd = command;
		if (targetVO.isSudoer() && !command.startsWith("sudo -S")) {
				/*
				NOTE: in /etc/sudoers the line
				Defaults    requiretty
				must be commented, unless sudo -S does not work!
				*/
			finalCmd = String.format("sudo -S -p '' %s", command);
			command = String.format("sudo -S %s", command);
		}

		logger.info("Sending SSH Command: cmd=[{}] si=[{}]", finalCmd, targetVO);
		String prompt = String.format("[%s@%s]$ %s", targetVO.getUsername(), targetVO.getAddress(), command);
		resultCallBack.onResult(new CommandOutput(CommandOutput.Type.PROMPT, prompt));

		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(finalCmd);
		//channelExec.setInputStream(null);
		//channelExec.setErrStream(null);

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

		StringBuilder result = new StringBuilder();
		while (true) {
			char[] buff = new char[1024];
			int read;
			while ((read = br.read(buff)) != -1) {
				String line = new String(buff, 0, read);
				resultCallBack.onResult(new CommandOutput(line));
				logger.debug("\tResult = {}", line);
				result.append(line).append("\n");
			}
			if (channelExec.isClosed()) {
				break;
			}
		}

		while (true) {
			char[] buff = new char[1024];
			int read;
			while ((read = errBr.read(buff)) != -1) {
				String line = new String(buff, 0, read);
				resultCallBack.onResult(new CommandOutput(CommandOutput.Type.LINE, line));
				logger.debug("\tResult = {}", line);
			}
			if (channelExec.isClosed()) {
				break;
			}
		}

		exitStatus = channelExec.getExitStatus();
		channelExec.disconnect();

		setResult(result.toString());
	}
}
