package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.libs.types.api.TypedKeyValuePair;

@XmlRootElement
public class TypedProperty extends Property implements TypedKeyValuePair {

	private String type;
	
	@Override
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
