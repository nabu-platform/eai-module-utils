package nabu.utils.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Table {
	private String schema, name;
	private List<ParameterDescription> fields;
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ParameterDescription> getFields() {
		return fields;
	}
	public void setFields(List<ParameterDescription> fields) {
		this.fields = fields;
	}
}
