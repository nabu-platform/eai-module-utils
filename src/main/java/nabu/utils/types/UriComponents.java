package nabu.utils.types;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "uriComponents")
@XmlType(propOrder = { "scheme", "schemeSpecificPart", "authority", "userInfo", "host", "port", "path", "query", "fragment" })
public class UriComponents {
	private String scheme, schemeSpecificPart, authority, userInfo, host, path, query, fragment;
	private Integer port;
	
	public static UriComponents build(URI uri) {
		UriComponents components = new UriComponents();
		components.setAuthority(uri.getAuthority());
		components.setFragment(uri.getFragment());
		components.setQuery(uri.getQuery());
		components.setScheme(uri.getScheme());
		components.setSchemeSpecificPart(uri.getSchemeSpecificPart());
		components.setUserInfo(uri.getUserInfo());
		components.setHost(uri.getHost());
		components.setPort(uri.getPort() == -1 ? null : uri.getPort());
		components.setPath(uri.getPath());
		return components;
	}
	
	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	public String getSchemeSpecificPart() {
		return schemeSpecificPart;
	}
	public void setSchemeSpecificPart(String schemeSpecificPart) {
		this.schemeSpecificPart = schemeSpecificPart;
	}
	public String getAuthority() {
		return authority;
	}
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	public String getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getFragment() {
		return fragment;
	}
	public void setFragment(String fragment) {
		this.fragment = fragment;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
}
