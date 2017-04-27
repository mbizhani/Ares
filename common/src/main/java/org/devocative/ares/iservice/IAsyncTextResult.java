package org.devocative.ares.iservice;

import java.io.Serializable;

public interface IAsyncTextResult extends Serializable {
	void onMessage(String text);
}
