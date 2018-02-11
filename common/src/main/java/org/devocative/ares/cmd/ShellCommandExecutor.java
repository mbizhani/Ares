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
 *
 * Based on https://stackoverflow.com/questions/26403422/jsch-interrupt-command-executing, to interrupt the current
 * command, the pty=true and out.write(3)!
 */
public class ShellCommandExecutor extends AbstractCommandExecutor {
	private final String[] stdin;
	private ChannelExec channelExec;
	private OutputStream out;

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
		if (targetVO.isSudoer() && !command.trim().startsWith("sudo")) {
				/*
				NOTE: in /etc/sudoers the line
				Defaults    requiretty
				must be commented, unless sudo -S does not work!

				REFERENCE: https://stackoverflow.com/questions/5560442/how-to-run-two-commands-in-sudo
				*/
			finalCmd = String.format("sudo -p '' -s <<EOF\n%s\nEOF", command);
		}

		logger.info("Sending SSH Command: cmd=[{}] si=[{}]", prompt, targetVO);
		String p = String.format("[ %s@%s ]$ %s", targetVO.getUsername(), targetVO.getName(), prompt);
		resource.onResult(new CommandOutput(CommandOutput.Type.PROMPT, p));

		channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(finalCmd);
		channelExec.setPty(true); //https://stackoverflow.com/questions/26403422/jsch-interrupt-command-executing

		channelExec.setInputStream(null);
		channelExec.setErrStream(null);

		final InputStream in = channelExec.getInputStream();
		final InputStream err = channelExec.getErrStream();

		out = channelExec.getOutputStream();

		channelExec.connect();

		if (targetVO.isSudoer()) {
			out.write((targetVO.getPassword() + "\n").getBytes());
			out.flush();
		}

		if (stdin != null) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
				// NOTE: When channelExec.setPty(true), the password for sudo is written in InputStream, so try to prevent it!
				if (!line.equals(targetVO.getPassword())) {
					resource.onResult(new CommandOutput(CommandOutput.Type.LINE, " . " + line));
					logger.debug("\tResult = {}", line);
					result.append(line).append("\n");
				}
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
				throw new RuntimeException("Bad Shell Command Exit Status: " + exitStatus);
			}
		}
	}

	@Override
	public void cancel() throws Exception {
		if (channelExec != null && channelExec.isConnected()) {
			out.write(3); // https://stackoverflow.com/questions/26403422/jsch-interrupt-command-executing
			out.flush();
			channelExec.disconnect();
		}
	}
}
