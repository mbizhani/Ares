package org.devocative.ares.cmd;

import org.devocative.adroit.date.UniDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CCUtil {
	private static final Logger logger = LoggerFactory.getLogger(CCUtil.class);

	public String now() {
		return now("yyyyMMdd_HHmmss");
	}

	public String now(String format) {
		return UniDate.now().format(format);
	}

	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public String find(String str, String regex, int group) {
		return find(str, regex, group, null);
	}

	public String find(String str, String regex, int group, String deValue) {
		Matcher matcher = Pattern.compile(regex).matcher(str);
		if (matcher.find()) {
			return matcher.group(group);
		}

		return deValue;
	}

	public String substring(String str, String token, int count) {
		if (count == -1) {
			int idx = str.lastIndexOf(token);
			if (idx > 0) {
				return str.substring(0, idx);
			}
		}
		return null;
	}

	public void log(String log) {
		logger.debug(log);
	}
}
