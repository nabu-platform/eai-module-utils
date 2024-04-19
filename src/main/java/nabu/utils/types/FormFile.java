package nabu.utils.types;

import java.io.InputStream;

public class FormFile {
	private InputStream content;
	private java.lang.String fileName, key;
	private java.lang.String contentType;
	public InputStream getContent() {
		return content;
	}
	public void setContent(InputStream content) {
		this.content = content;
	}
	public java.lang.String getFileName() {
		return fileName;
	}
	public void setFileName(java.lang.String fileName) {
		this.fileName = fileName;
	}
	public java.lang.String getKey() {
		return key;
	}
	public void setKey(java.lang.String key) {
		this.key = key;
	}
	public java.lang.String getContentType() {
		return contentType;
	}
	public void setContentType(java.lang.String contentType) {
		this.contentType = contentType;
	}
}
