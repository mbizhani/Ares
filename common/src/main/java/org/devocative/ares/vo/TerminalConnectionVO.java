package org.devocative.ares.vo;

public class TerminalConnectionVO {
	private Long connectionId;
	private OServiceInstanceTargetVO targetVO;

	public TerminalConnectionVO(Long connectionId, OServiceInstanceTargetVO targetVO) {
		this.connectionId = connectionId;
		this.targetVO = targetVO;
	}

	public Long getConnectionId() {
		return connectionId;
	}

	public OServiceInstanceTargetVO getTargetVO() {
		return targetVO;
	}
}
