package nabu.utils.types;

import be.nabu.libs.types.base.Scope;

public class ParameterDescription {
	private String name, type, typeName, description, pattern, collectionName;
	private boolean isList, isOptional, simple, generated, identifiable, primary;
	private Scope scope;
	private Integer minimum, maximum;
	
	public ParameterDescription() {
		// auto construct
	}

	public ParameterDescription(String name, String type, String typeName, String description, boolean isList, boolean isOptional, boolean isSimple, boolean isGenerated, boolean identifiable) {
		this.name = name;
		this.type = type;
		this.typeName = typeName;
		this.description = description;
		this.isList = isList;
		this.isOptional = isOptional;
		this.simple = isSimple;
		this.generated = isGenerated;
		this.identifiable = identifiable;
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

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Integer getMinimum() {
		return minimum;
	}

	public void setMinimum(Integer minimum) {
		this.minimum = minimum;
	}

	public Integer getMaximum() {
		return maximum;
	}

	public void setMaximum(Integer maximum) {
		this.maximum = maximum;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public boolean isIdentifiable() {
		return identifiable;
	}

	public void setIdentifiable(boolean identifiable) {
		this.identifiable = identifiable;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

}
