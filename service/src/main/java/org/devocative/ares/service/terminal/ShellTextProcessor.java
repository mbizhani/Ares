package org.devocative.ares.service.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellTextProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ShellTextProcessor.class);

	private long connId;
	private String currentUser;

	private boolean doStart = false, clientSent = false, isPrompt = true;
	private StringBuilder cmdBuilder = new StringBuilder();
	private int lastSpecialKey, cursor = 0;

	// ------------------------------

	public ShellTextProcessor(long connId, String currentUser) {
		this.connId = connId;
		this.currentUser = currentUser;
	}

	// ------------------------------

	public void onClientText(String msg) {
		doStart = true;
		clientSent = true;
	}

	public void onClientSpecialKey(int specialKey) {
		doStart = true;
		lastSpecialKey = specialKey;
	}

	public synchronized void onServerText(String msg) {
		try {
			processServerText(msg);
		} catch (Exception e) {
			cmdBuilder = new StringBuilder();
			cursor = 0;
			logger.error("ShellProcessor", e);
		}

		lastSpecialKey = -1;
		clientSent = false;
	}

	// ------------------------------

	private void processServerText(String msg) {
		if (doStart) {
			if (isPrompt) {
				EShellSpecialKey lastOne = EShellSpecialKey.findShellSpecialKey(lastSpecialKey);
				switch (lastOne) {
					case UP:
					case DOWN:
						msg = msg.trim();
						cmdBuilder = new StringBuilder(msg);
						cursor = msg.length();
						break;

					case ENTER:
					case CTR_D:
						String cmd = lastOne != EShellSpecialKey.CTR_D ? cmdBuilder.toString() : "logout";
						logger.info("### {cmd:'{}', connId:'{}', user:'{}'}", cmd, connId, currentUser);
						cmdBuilder = new StringBuilder();
						checkPrompt(msg);
						cursor = 0;
						break;

					case TAB:
						if (!msg.startsWith("\n") && !msg.startsWith("\r") && msg.trim().length() > 0) {
							cmdBuilder.append(msg);
						}
						break;

					case BACKSPACE:
						if (cursor > 0) {
							cmdBuilder.deleteCharAt(cursor - 1);
							cursor--;
						}
						break;

					case LEFT:
						if (cursor > 0) {
							cursor--;
						}
						break;

					case RIGHT:
						if (cursor < cmdBuilder.length()) {
							cursor++;
						}
						break;

					case HOME:
						cursor = 0;
						break;

					case END:
						cursor = cmdBuilder.length();
						break;

					case CTR_U:
						cmdBuilder = new StringBuilder();
						cursor = 0;
						break;

					default:
						if (clientSent) {
							cmdBuilder.insert(cursor, msg);
							cursor += msg.length();
						}
				}
			} else {
				checkPrompt(msg);
			}
		}
	}

	private void checkPrompt(String msg) {
		String trim = msg.trim();
		isPrompt = trim.endsWith("$") || trim.endsWith("#");
	}
}
