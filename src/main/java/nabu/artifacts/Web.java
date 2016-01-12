package nabu.artifacts;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.types.Property;
import nabu.types.WebArtifactInformation;
import be.nabu.eai.repository.artifacts.web.WebArtifact;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.ResourceFilter;
import be.nabu.libs.services.api.ExecutionContext;

@WebService
public class Web {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "information")
	public WebArtifactInformation information(@NotNull @WebParam(name = "artifactId") String id) throws IOException {
		if (id != null) {
			WebArtifact resolved = executionContext.getServiceContext().getResolver(WebArtifact.class).resolve(id);
			if (resolved != null) {
				WebArtifactInformation information = new WebArtifactInformation();
				information.setRealm(resolved.getRealm());
				information.setCharset(resolved.getConfiguration().getCharset() == null ? Charset.defaultCharset() : Charset.forName(resolved.getConfiguration().getCharset()));
				if (resolved.getConfiguration().getVirtualHost() != null) {
					information.setHost(resolved.getConfiguration().getVirtualHost().getConfiguration().getHost());
					information.setAliases(resolved.getConfiguration().getVirtualHost().getConfiguration().getAliases());
					information.setPort(resolved.getConfiguration().getVirtualHost().getConfiguration().getServer() == null ? null : resolved.getConfiguration().getVirtualHost().getConfiguration().getServer().getConfiguration().getPort());
					information.setSecure(resolved.getConfiguration().getVirtualHost().getConfiguration().getServer() == null ? null : resolved.getConfiguration().getVirtualHost().getConfiguration().getServer().getConfiguration().getKeystore() != null);
				}
				information.setPath(resolved.getConfiguration().getPath());
				Map<String, String> properties = resolved.getProperties();
				for (String key : properties.keySet()) {
					information.getProperties().add(new Property(key, properties.get(key)));
				}
				return information;
			}
		}
		return null;
	}

	@WebResult(name = "resources")
	public List<String> resources(@NotNull @WebParam(name = "artifactId") String id, @WebParam(name = "regex") final String regex) {
		List<String> resources = new ArrayList<String>();
		if (id != null) {
			WebArtifact resolved = executionContext.getServiceContext().getResolver(WebArtifact.class).resolve(id);
			if (resolved != null) {
				for (ResourceContainer<?> root : resolved.getResourceHandler().getRoots()) {
					List<String> find = find(root, new ResourceFilter() {
						@Override
						public boolean accept(Resource resource) {
							return !(resource instanceof ResourceContainer) && (regex == null || resource.getName().matches(regex));
						}
					}, true, null);
					for (String resource : find) {
						resources.add("resources/" + resource);
					}
				}
			}
		}
		return resources;
	}

	public static List<String> find(ResourceContainer<?> container, ResourceFilter filter, boolean recursive, String path) {
		List<String> result = new ArrayList<String>();
		for (Resource child : container) {
			String childPath = path == null ? child.getName() : path + "/" + child.getName();
			if (filter.accept(child))
				result.add(childPath);
			if (recursive && child instanceof ResourceContainer)
				result.addAll(find((ResourceContainer<?>) child, filter, recursive, childPath));
		}
		return result;
	}
}
