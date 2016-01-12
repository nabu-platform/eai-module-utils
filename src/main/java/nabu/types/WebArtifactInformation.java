package nabu.types;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "webArtifact")
@XmlType(propOrder = { "realm", "path", "charset", "host", "aliases", "port", "secure", "properties" })
public class WebArtifactInformation {
	private String realm, path;
	private Charset charset;
	private String host;
	private List<String> aliases;
	private Integer port;
	private Boolean secure;
	private List<Property> properties;

	public String getRealm() {
		return realm;
	}
	public void setRealm(String realm) {
		this.realm = realm;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Charset getCharset() {
		return charset;
	}
	public void setCharset(Charset charset) {
		this.charset = charset;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public Boolean getSecure() {
		return secure;
	}
	public void setSecure(Boolean secure) {
		this.secure = secure;
	}
	public List<Property> getProperties() {
		if (properties == null) {
			properties = new ArrayList<Property>();
		}
		return properties;
	}
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public List<String> getAliases() {
		return aliases;
	}
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
}