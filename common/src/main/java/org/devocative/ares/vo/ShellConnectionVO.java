package org.devocative.ares.vo;

import org.devocative.ares.iservice.IAsyncTextResult;

public class ShellConnectionVO {
	private Long connectionId;
	private OServiceInstanceTargetVO targetVO;
	private IAsyncTextResult textResult;

	public ShellConnectionVO(Long connectionId, OServiceInstanceTargetVO targetVO, IAsyncTextResult textResult) {
		this.connectionId = connectionId;
		this.targetVO = targetVO;
		this.textResult = textResult;
	}

	public Long getConnectionId() {
		return connectionId;
	}

	public OServiceInstanceTargetVO getTargetVO() {
		return targetVO;
	}

	public IAsyncTextResult getTextResult() {
		return textResult;
	}
}
