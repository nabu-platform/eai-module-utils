package nabu.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.ResourceProperties;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;

@WebService
public class Resource {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "properties")
	public ResourceProperties properties(@WebParam(name = "uri") URI uri) throws IOException {
		return uri == null ? null : ResourceUtils.properties(ResourceFactory.getInstance().resolve(uri, null));
	}
	
	@WebResult(name = "children")
	public java.util.List<ResourceProperties> list(@WebParam(name = "uri") URI uri) throws IOException {
		if (uri == null) {
			return null;
		}
		java.util.List<ResourceProperties> list = new ArrayList<ResourceProperties>();
		be.nabu.libs.resources.api.Resource resolved = ResourceFactory.getInstance().resolve(uri, null);
		if (resolved instanceof ResourceContainer) {
			for (be.nabu.libs.resources.api.Resource child : (ResourceContainer<?>) resolved) {
				list.add(ResourceUtils.properties(child));
			}
		}
		return list;
	}
	
	@WebResult(name = "stream")
	public InputStream read(@WebParam(name = "uri") URI uri) throws IOException {
		if (uri == null) {
			return null;
		}
		be.nabu.libs.resources.api.Resource resolved = ResourceFactory.getInstance().resolve(uri, executionContext.getSecurityContext().getToken());
		if (resolved == null) {
			throw new FileNotFoundException("Could not find file: " + uri);
		}
		if (!(resolved instanceof ReadableResource)) {
			throw new IOException("The resource is not readable: " + uri);
		}
		return IOUtils.toInputStream(new ResourceReadableContainer((ReadableResource) resolved));
	}

	@WebResult(name = "exists")
	public boolean exists(@WebParam(name = "uri") URI uri) throws IOException {
		if (uri == null) {
			return false;
		}
		return ResourceFactory.getInstance().resolve(uri, executionContext.getSecurityContext().getToken()) != null;
	}
	
	public void write(@WebParam(name = "uri") URI uri, @WebParam(name = "stream") InputStream content) throws IOException {
		if (uri != null || content != null) {
			WritableContainer<ByteBuffer> writableContainer = ResourceUtils.toWritableContainer(uri, executionContext.getSecurityContext().getToken());
			try {
				IOUtils.copyBytes(IOUtils.wrap(content), writableContainer);
			}
			finally {
				writableContainer.close();
			}
		}
	}
	
	public void delete(@WebParam(name = "uri") URI uri) throws IOException {
		if (uri != null) {
			be.nabu.libs.resources.api.Resource resolve = ResourceFactory.getInstance().resolve(uri, null);
			if (resolve != null) {
				ResourceContainer<?> parent = resolve.getParent();
				if (parent instanceof ManageableContainer) {
					((ManageableContainer<?>) parent).delete(resolve.getName());
				}
			}
		}
	}
	
	public void mkdir(@WebParam(name = "uri") URI uri) throws IOException {
		if (uri != null) {
			ResourceUtils.mkdir(uri, executionContext.getSecurityContext().getToken());
		}
	}
}
