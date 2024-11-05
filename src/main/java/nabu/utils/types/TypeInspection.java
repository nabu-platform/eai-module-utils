package nabu.utils.types;

import java.util.List;

public class TypeInspection {
	private String id;
	private List<String> hierarchy;
	private List<ParameterDescription> parameters;
	private boolean simple, complex, list;
	private String name;
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
	public boolean isSimple() {
		return simple;
	}
	public void setSimple(boolean simple) {
		this.simple = simple;
	}
	public boolean isComplex() {
		return complex;
	}
	public void setComplex(boolean complex) {
		this.complex = complex;
	}
	public boolean isList() {
		return list;
	}
	public void setList(boolean list) {
		this.list = list;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
