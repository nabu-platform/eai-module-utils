package nabu.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.ResourceRoot;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;

@WebService
public class File {
	
	@WebResult(name = "stream")
	public InputStream read(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") SimplePrincipal principal) throws IOException {
		ResourceRoot resolved = ResourceFactory.getInstance().resolve(uri, principal);
		if (resolved == null) {
			throw new FileNotFoundException("Could not find file: " + uri);
		}
		if (!(resolved instanceof ReadableResource)) {
			throw new IOException("The resource is not readable: " + uri);
		}
		return IOUtils.toInputStream(((ReadableResource) resolved).getReadable());
	}
	
	public void write(@WebParam(name = "uri") URI uri, @WebParam(name = "stream") InputStream content, @WebParam(name = "principal") SimplePrincipal principal) throws IOException {
		WritableContainer<ByteBuffer> writableContainer = ResourceUtils.toWritableContainer(uri, principal);
		try {
			IOUtils.copyBytes(IOUtils.wrap(content), writableContainer);
		}
		finally {
			writableContainer.close();
		}
	}
	
	public void mkdir(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") SimplePrincipal principal) throws IOException {
		ResourceUtils.mkdir(uri, principal);
	}
}
