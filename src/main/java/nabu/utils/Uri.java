package nabu.utils;

import java.net.URI;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import nabu.utils.types.UriComponents;

@WebService
public class Uri {
	
	@WebResult(name = "components")
	public UriComponents toComponents(@WebParam(name = "uri") URI uri) {
		return UriComponents.build(uri);
	}
	
}
