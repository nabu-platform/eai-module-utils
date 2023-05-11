package nabu.utils.types;

import java.util.List;

public class TypeInspection {
	private String id;
	private List<String> hierarchy;
	private List<ParameterDescription> parameters;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getHierarchy() {
		return hierarchy;
	}
	public void setHierarchy(List<String> hierarchy) {
		this.hierarchy = hierarchy;
	}
	public List<ParameterDescription> getParameters() {
		return parameters;
	}
	public void setParameters(List<ParameterDescription> parameters) {
		this.parameters = parameters;
	}
}
