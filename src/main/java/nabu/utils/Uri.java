package nabu.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.utils.KeyValuePairImpl;
import nabu.utils.types.UriComponents;

@WebService
public class Uri {
	
	@WebResult(name = "components")
	public UriComponents toComponents(@WebParam(name = "uri") URI uri) {
		return UriComponents.build(uri);
	}
	
	@WebResult(name = "uri")
	public URI getChild(@WebParam(name = "parent") URI uri, @WebParam(name = "child") String child) {
		return URIUtils.getChild(uri, child);
	}
	
	@WebResult(name = "uri")
	public URI fromComponents(@WebParam(name = "components") UriComponents components) throws URISyntaxException {
		// reset default ports to null, we don't need to mention them
		if ("https".equalsIgnoreCase(components.getScheme()) && components.getPort() != null && components.getPort() == 443) {
			components.setPort(null);
		}
		if ("http".equalsIgnoreCase(components.getScheme()) && components.getPort() != null && components.getPort() == 80) {
			components.setPort(null);
		}
		return new URI(
			components.getScheme(),
			components.getUserInfo(),
			components.getHost(),
			components.getPort() == null ? -1 : components.getPort(),
			components.getPath(),
			components.getQuery(),
			components.getFragment()
		);
	}

	@WebResult(name = "uri")
	public String encodeUri(@WebParam(name = "uri") String uri, @WebParam(name = "includeEncoded") Boolean includeEncoded) {
		return URIUtils.encodeURI(uri, includeEncoded == null ? true : includeEncoded);
	}
	
	@WebResult(name = "component")
	public String encodeUriComponent(@WebParam(name = "component") String component, @WebParam(name = "includeEncoded") Boolean includeEncoded) {
		return URIUtils.encodeURIComponent(component, includeEncoded == null ? true : includeEncoded);
	}
	
	@WebResult(name = "properties")
	public java.util.List<KeyValuePair> getQueryProperties(@WebParam(name = "uri") URI uri) {
		if (uri == null) {
			return null;
		}
		List<KeyValuePair> properties = new ArrayList<KeyValuePair>();
		Map<java.lang.String, List<java.lang.String>> queryProperties = URIUtils.getQueryProperties(uri);
		for (Map.Entry<java.lang.String, List<java.lang.String>> entry : queryProperties.entrySet()) {
			if (entry.getValue() != null) {
				for (String value : entry.getValue()) {
					properties.add(new KeyValuePairImpl(entry.getKey(), value));
				}
			}
		}
		return properties;
	}
	
	@WebResult(name = "normalized")
	public URI normalize(@WebParam(name = "uri") URI uri) {
		return URIUtils.normalize(uri);
	}
}
