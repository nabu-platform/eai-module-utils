package nabu.utils.types;

public class TypeResult {
	private Object object, value;
	private String path;
	
	public TypeResult(Object object, String path, Object value) {
		this.object = object;
		this.path = path;
		this.value = value;
	}
	public TypeResult() {
		// autoconstruct
	}
	
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
