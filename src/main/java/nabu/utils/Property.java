package nabu.utils;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.lang.String;

@XmlRootElement
@XmlType(propOrder = { "key", "value" })
public class Property {

	private String key, value;
	
	public Property() {
		// automatic creation
	}
	
	public Property(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@NotNull
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
