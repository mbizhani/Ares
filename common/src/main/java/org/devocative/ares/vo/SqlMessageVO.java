package org.devocative.ares.vo;

public class SqlMessageVO {
	private String sql;
	private long pageIndex;
	private long pageSize;

	public SqlMessageVO(String sql, long pageIndex, long pageSize) {
		this.sql = sql;
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
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
