package org.devocative.ares.cmd;

import com.jcraft.jsch.ChannelExec;
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
	private final String[] stdin;

	// ---------------

	private int exitStatus = -1;

	// ------------------------------

	public ShellCommandExecutor(OServiceInstanceTargetVO targetVO, CommandCenterResource resource, String prompt, String command, String[] stdin) {
		super(targetVO, resource, prompt, command);
		this.stdin = stdin;
	}

	// ------------------------------

	public int getExitStatus() {
		return exitStatus;
	}

	// ------------------------------

	@Override
	protected void execute() throws JSchException, IOException {
		Session session = resource.createSession(targetVO);

		String finalCmd = command;
		if (targetVO.isSudoer() && !command.startsWith("sudo -S")) {
				/*
				NOTE: in /etc/sudoers the line
				Defaults    requiretty
				must be commented, unless sudo -S does not work!
				*/
			finalCmd = String.format("sudo -S -p '' %s", command);
		}

		logger.info("Sending SSH Command: cmd=[{}] si=[{}]", prompt, targetVO);
		String p = String.format("[ %s@%s ]$ %s", targetVO.getUsername(), targetVO.getName(), prompt);
		resource.onResult(new CommandOutput(CommandOutput.Type.PROMPT, p));

		final ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(finalCmd);

		channelExec.setInputStream(null);
		channelExec.setErrStream(null);

		final InputStream in = channelExec.getInputStream();
		final InputStream err = channelExec.getErrStream();

		OutputStream out = channelExec.getOutputStream();

		channelExec.connect();

		if (targetVO.isSudoer()) {
			out.write((targetVO.getPassword() + "\n").getBytes());
			out.flush();
		}

		if (stdin != null) {
			for (String s : stdin) {
				if (s != null) {
					out.write((s + "\n").getBytes());
					out.flush();
				}
			}
		}

		StringBuilder result = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		Thread th = new Thread(
			Thread.currentThread().getThreadGroup(),
			() -> {
				BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
				while (true) {
					errReader.lines().forEach(line -> {
						resource.onResult(new CommandOutput(CommandOutput.Type.LINE, " - " + line));
						logger.debug("\tErrResult = {}", line);
					});
					if (channelExec.isClosed()) {
						break;
					}
				}
			},
			Thread.currentThread().getName() + "-ErrReader");
		th.start();

		while (true) {
			reader.lines().forEach(line -> {
				resource.onResult(new CommandOutput(CommandOutput.Type.LINE, " . " + line));
				logger.debug("\tResult = {}", line);
				result.append(line).append("\n");
			});
			if (channelExec.isClosed()) {
				break;
			}
		}

		try {
			th.join();
		} catch (InterruptedException e) {
			logger.warn("ShellCommandExecutor.Thread.Join", e);
		}

		exitStatus = channelExec.getExitStatus();
		channelExec.disconnect();

		setResult(result.toString());

		if (exitStatus != 0) {
			if (isForce()) {
				resource.onResult(new CommandOutput(CommandOutput.Type.LINE, "WARNING: exitStatus: " + exitStatus));
			} else {
				throw new RuntimeException("Invalid ssh command exitStatus: " + exitStatus);
			}
		}
	}
}
