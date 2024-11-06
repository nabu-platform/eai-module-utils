/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
	
	private static boolean useOldUriBuilding = Boolean.parseBoolean(System.getProperty("useOldUriBuilding", "false"));
	
	@WebResult(name = "components")
	public UriComponents toComponents(@WebParam(name = "uri") URI uri) {
		return uri == null ? null : UriComponents.build(uri);
	}
	
	@WebResult(name = "uri")
	public URI getChild(@WebParam(name = "parent") URI uri, @WebParam(name = "child") String child) {
		return URIUtils.getChild(uri, child);
	}
	
	@WebResult(name = "uri")
	public URI fromComponents(@WebParam(name = "components") UriComponents components) throws URISyntaxException {
		if (components == null) {
			return null;
		}
		// reset default ports to null, we don't need to mention them
		if ("https".equalsIgnoreCase(components.getScheme()) && components.getPort() != null && components.getPort() == 443) {
			components.setPort(null);
		}
		if ("http".equalsIgnoreCase(components.getScheme()) && components.getPort() != null && components.getPort() == 80) {
			components.setPort(null);
		}
		if (useOldUriBuilding) {
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
		else {
			return URIUtils.buildUri(components.getScheme(), components.getUserInfo(), components.getAuthority(), components.getHost(), components.getPort(), components.getPath(), components.getQuery(), components.getFragment());
		}
	}

	@WebResult(name = "uri")
	public String encodeUri(@WebParam(name = "uri") String uri, @WebParam(name = "includeEncoded") Boolean includeEncoded) {
		return URIUtils.encodeURI(uri, includeEncoded == null ? true : includeEncoded);
	}
	
	@WebResult(name = "uri")
	public String decodeUri(@WebParam(name = "uri") String uri) {
		return URIUtils.decodeURI(uri);
	}
	
	@WebResult(name = "url")
	public String encodeUrl(@WebParam(name = "url") String uri) {
		return URIUtils.encodeURL(uri);
	}
	
	@WebResult(name = "url")
	public String decodeUrl(@WebParam(name = "url") String uri) {
		return URIUtils.decodeURL(uri);
	}
	
	@WebResult(name = "component")
	public String encodeUriComponent(@WebParam(name = "component") String component, @WebParam(name = "includeEncoded") Boolean includeEncoded) {
		return URIUtils.encodeURIComponent(component, includeEncoded == null ? true : includeEncoded);
	}
	
	@WebResult(name = "component")
	public String decodeUriComponent(@WebParam(name = "component") String component) {
		return URIUtils.decodeURIComponent(component);
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
