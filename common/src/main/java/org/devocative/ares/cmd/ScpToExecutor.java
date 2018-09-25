package org.devocative.ares.cmd;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.devocative.ares.vo.OServiceInstanceTargetVO;
import org.devocative.demeter.entity.FileStore;

import java.io.*;

public class ScpToExecutor extends AbstractExecutor {
	private final FileStore fileStore;
	private final String destDir;

	private Channel channel;

	// ------------------------------

	public ScpToExecutor(OServiceInstanceTargetVO targetVO, CommandCenterResource resource, FileStore fileStore, String destDir) {
		super(targetVO, resource);

		this.fileStore = fileStore;
		this.destDir = destDir;
	}

	// ------------------------------

	@Override
	protected void execute() throws JSchException, IOException {
		logger.info("Scp file [{}] to [{}]", fileStore, targetVO.getAddress());

		Session session = resource.createSession(targetVO, isAdmin());

		//String command = String.format("scp -p -t \"~/%s\"", fileStore.getName());
		String command = "scp -p -t " + destDir;
		channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		if (checkAck(in) != 0) {
			return;
		}

		File _lfile = new File(fileStore.getPath());

		command = "T" + (_lfile.lastModified() / 1000) + " 0";
		// The access time should be sent here,
		// but it is not accessible with JavaAPI ;-<
		command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
		out.write(command.getBytes());
		out.flush();
		if (checkAck(in) != 0) {
			return;
		}

		long fileSize = _lfile.length();
		command = "C0644 " + fileSize + " " + fileStore.getName() + "\n";
		out.write(command.getBytes());
		out.flush();

		if (checkAck(in) != 0) {
			return;
		}

		resource.onResult(new CommandOutput(CommandOutput.Type.PROMPT, "scp " + fileStore.getName()));

		long sent = 0;
		int lastSentPercent = 0;
		FileInputStream fis = new FileInputStream(_lfile);
		byte[] buf = new byte[10240]; //10K
		while (true) {
			int len = fis.read(buf, 0, buf.length);
			if (len <= 0) break;
			out.write(buf, 0, len); //out.flush();
			sent += len;

			int percent = (int) (sent * 100 / fileSize);
			if (percent % 5 == 0) {
				if (lastSentPercent != percent) {
					resource.onResult(new CommandOutput(CommandOutput.Type.LINE, String.format("File sent: %s %%", percent)));
					lastSentPercent = percent;
				}
			}
		}
		fis.close();

		// send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();
		if (checkAck(in) != 0) {
			return;
		}
		out.close();

		channel.disconnect();

		resource.onResult(new CommandOutput(CommandOutput.Type.LINE, "File sent successfully"));
	}

	@Override
	public void cancel() throws Exception {
		if (channel != null && channel.isConnected()) {
			channel.disconnect();
		}
	}

	// ------------------------------

	private int checkAck(InputStream in) throws IOException {
		int b = in.read();
		if (b != 0) {
			resource.onResult(new CommandOutput(CommandOutput.Type.LINE, "checkAck = " + b));
		}
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if (b == 0) return b;
		if (b == -1) return b;

		if (b == 1 || b == 2) {
			StringBuilder sb = new StringBuilder();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			}
			while (c != '\n');
			resource.onResult(new CommandOutput(CommandOutput.Type.ERROR, sb.toString()));
		}
		return b;
	}
}
