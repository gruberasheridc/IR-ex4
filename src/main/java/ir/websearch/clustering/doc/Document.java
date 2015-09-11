package ir.websearch.clustering.doc;

public class Document {
	
	public final static String FILE_NUM_FIELD = "fileNum";
	public final static String ORIG_CLUSTER_FIELD = "origCluster";
	public final static String TITLE_FIELD = "title";
	public final static String TEXT_FIELD = "text";

	private final String fileNum;
	private final String origCluster;
	private final String title;
	private final String text;
	
	public String getFileNum() {
		return fileNum;
	}

	public String getOrigCluster() {
		return origCluster;
	}

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	public static class Builder {
		private String fileNum;
		private String origCluster;
		private String title;
		private String text;

		public Builder fileNum(String fileNum) {
			this.fileNum = fileNum;
			return this;
		}

		public Builder origCluster(String origCluster) {
			this.origCluster = origCluster;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder text(String text) {
			this.text = text;
			return this;
		}

		public Document build() {
			return new Document(this);
		}
	}

	private Document(Builder builder) {
		this.fileNum = builder.fileNum;
		this.origCluster = builder.origCluster;
		this.title = builder.title;
		this.text = builder.text;
	}
}
