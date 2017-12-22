package org.devocative.ares.vo;

public class SqlMessageVO {
	public enum MsgType {EXEC, CANCEL, TERMINATE}

	// ------------------------------

	private MsgType type;
	private String sql;
	private long pageIndex;
	private long pageSize;

	// ------------------------------

	public SqlMessageVO(MsgType type) {
		this.type = type;
	}

	public SqlMessageVO(String sql, long pageIndex, long pageSize) {
		this.type = MsgType.EXEC;
		this.sql = sql;
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

	// ------------------------------

	public MsgType getType() {
		return type;
	}

	public String getSql() {
		return sql;
	}

	public long getPageIndex() {
		return pageIndex;
	}

	public long getPageSize() {
		return pageSize;
	}
}
