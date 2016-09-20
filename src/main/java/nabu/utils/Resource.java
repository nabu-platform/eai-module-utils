package nabu.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;

@WebService
public class Resource {
	
	private ExecutionContext executionContext;
	
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
	
	public void mkdir(@WebParam(name = "uri") URI uri) throws IOException {
		if (uri != null) {
			ResourceUtils.mkdir(uri, executionContext.getSecurityContext().getToken());
		}
	}
}
