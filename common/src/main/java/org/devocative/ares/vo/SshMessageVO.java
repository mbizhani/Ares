package org.devocative.ares.vo;

public class SshMessageVO {
	private String text;
	private Integer specialKey;
	private Size size;

	// ------------------------------

	public SshMessageVO(String text, Integer specialKey) {
		this.text = text;
		this.specialKey = specialKey;
	}

	public SshMessageVO(int cols, int rows, int width, int height) {
		this.size = new Size(cols, rows, width, height);
	}

	// ------------------------------

	public String getText() {
		return text;
	}

	public Integer getSpecialKey() {
		return specialKey;
	}

	public Size getSize() {
		return size;
	}

	// ------------------------------

	public class Size {
		private int cols, rows, width, height;

		private Size(int cols, int rows, int width, int height) {
			this.cols = cols;
			this.rows = rows;
			this.width = width;
			this.height = height;
		}

		public int getCols() {
			return cols;
		}

		public int getRows() {
			return rows;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}
}
