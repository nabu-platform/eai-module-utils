package nabu.utils;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.lang.String;

import be.nabu.libs.resources.api.principals.BasicPrincipal;

@XmlRootElement
@XmlType(propOrder = { "name", "password" })
public class SimplePrincipal implements BasicPrincipal {

	private String name, password;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
