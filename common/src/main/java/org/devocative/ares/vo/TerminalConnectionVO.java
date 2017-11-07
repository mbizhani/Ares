package org.devocative.ares.vo;

public class TerminalConnectionVO {
	private Long connectionId;
	private OServiceInstanceTargetVO targetVO;
	private Object initConfig;

	public TerminalConnectionVO(Long connectionId, Object initConfig, OServiceInstanceTargetVO targetVO) {
		this.connectionId = connectionId;
		this.targetVO = targetVO;
		this.initConfig = initConfig;
	}

	public Long getConnectionId() {
		return connectionId;
	}

	public Object getInitConfig() {
		return initConfig;
	}

	public OServiceInstanceTargetVO getTargetVO() {
		return targetVO;
	}
}
