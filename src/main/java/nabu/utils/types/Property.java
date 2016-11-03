package nabu.utils.types;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import be.nabu.libs.types.utils.KeyValuePairImpl;

import java.lang.String;

@XmlRootElement
@XmlType(propOrder = { "key", "value" })
public class Property extends KeyValuePairImpl {

	public Property() {
		// automatic creation
	}
	
	public Property(String key, String value) {
		super(key, value);
	}

	@NotNull
	public String getKey() {
		return super.getKey();
	}

	@Override
	public String getValue() {
		return super.getValue();
	}

	@Override
	public void setKey(String key) {
		super.setKey(key);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
	}

}
