package nabu.utils.types;

public class ParameterDescription {
	private String name, type, typeName, description;
	private boolean isList, isOptional, simple, generated;
	
	public ParameterDescription() {
		// auto construct
	}

	public ParameterDescription(String name, String type, String typeName, String description, boolean isList, boolean isOptional, boolean isSimple, boolean isGenerated) {
		this.name = name;
		this.type = type;
		this.typeName = typeName;
		this.description = description;
		this.isList = isList;
		this.isOptional = isOptional;
		this.simple = isSimple;
		this.generated = isGenerated;
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
	public boolean isOptional() {
		return isOptional;
	}
	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public boolean isSimple() {
		return simple;
	}

	public void setSimple(boolean simple) {
		this.simple = simple;
	}

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
}
