package nabu.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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
	
	public InputStream read(URI uri, SimplePrincipal principal) throws IOException {
		ResourceRoot resolved = ResourceFactory.getInstance().resolve(uri, principal);
		if (resolved == null) {
			throw new FileNotFoundException("Could not find file: " + uri);
		}
		if (!(resolved instanceof ReadableResource)) {
			throw new IOException("The resource is not readable: " + uri);
		}
		return IOUtils.toInputStream(((ReadableResource) resolved).getReadable());
	}
	
	public void write(URI uri, InputStream content, SimplePrincipal principal) throws IOException {
		WritableContainer<ByteBuffer> writableContainer = ResourceUtils.toWritableContainer(uri, principal);
		try {
			IOUtils.copyBytes(IOUtils.wrap(content), writableContainer);
		}
		finally {
			writableContainer.close();
		}
	}
	
	public void mkdir(URI uri, SimplePrincipal principal) throws IOException {
		ResourceUtils.mkdir(uri, principal);
	}
}
