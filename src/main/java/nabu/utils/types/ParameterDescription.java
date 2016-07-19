package nabu.utils.types;

public class ParameterDescription {
	private String name, type, description;
	private boolean isList;
	
	public ParameterDescription() {
		// auto construct
	}

	public ParameterDescription(String name, String type, String description, boolean isList) {
		this.name = name;
		this.type = type;
		this.description = description;
		this.isList = isList;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isList() {
		return isList;
	}
	public void setList(boolean isList) {
		this.isList = isList;
	}
}
