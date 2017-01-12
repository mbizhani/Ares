package org.devocative.ares.module;

import org.devocative.demeter.imodule.DModule;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;

public class AresDModule implements DModule {
	@Override
	public void init() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Low;
	}
}
