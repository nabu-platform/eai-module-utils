package nabu.utils;

import java.net.URI;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.resources.URIUtils;
import nabu.utils.types.UriComponents;

@WebService
public class Uri {
	
	@WebResult(name = "components")
	public UriComponents toComponents(@WebParam(name = "uri") URI uri) {
		return UriComponents.build(uri);
	}

	@WebResult(name = "uri")
	public String encodeUri(@WebParam(name = "uri") String uri) {
		return URIUtils.encodeURI(uri);
	}
	
	@WebResult(name = "component")
	public String encodeUriComponent(@WebParam(name = "component") String component) {
		return URIUtils.encodeURIComponent(component);
	}
}
