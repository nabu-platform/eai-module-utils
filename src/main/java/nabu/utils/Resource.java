package nabu.utils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.api.Hidden;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.events.ResourceEvent;
import be.nabu.eai.repository.events.ResourceEvent.ResourceState;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.ResourceWritableContainer;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.AppendableResource;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.ResourceProperties;
import be.nabu.libs.resources.api.ResourceResolver;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.resources.impl.ResourcePropertiesImpl;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;

@WebService
public class Resource {
	
	@WebResult(name = "properties")
	public ResourceProperties properties(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") Principal principal) throws IOException {
		be.nabu.libs.resources.api.Resource resolve = ResourceFactory.getInstance().resolve(uri, principal);
		try {
			return uri == null ? null : ResourceUtils.properties(resolve);
		}
		finally {
			if (resolve instanceof Closeable) {
				((Closeable) resolve).close();
			}
		}
	}
	
	@WebResult(name = "children")
	public java.util.List<ResourceProperties> list(@WebParam(name = "uri") URI uri, @WebParam(name = "recursive") Boolean recursive, @WebParam(name = "fileFilter") java.lang.String fileFilter, @WebParam(name = "principal") Principal principal) throws IOException {
		if (uri == null) {
			return null;
		}
		java.util.List<ResourceProperties> list = new ArrayList<ResourceProperties>();
		be.nabu.libs.resources.api.Resource parent = ResourceFactory.getInstance().resolve(uri, principal);
		try {
			list(recursive, fileFilter, list, parent, uri);
			// this is of the format repository:artifactId:/path/to/stuff
			// however, if we pass it in the above, it will not have a path and end up with repository:/myresource.txt for example
			if ("repository".equals(uri.getScheme())) {
				java.lang.String fullUri = uri.toASCIIString();
				for (ResourceProperties properties : list) {
					if (properties instanceof ResourcePropertiesImpl) {
						if ("repository".equals(properties.getUri().getScheme())) {
							try {
								((ResourcePropertiesImpl) properties).setUri(new URI(fullUri.replaceAll("[/]+$", "") + properties.getUri().getPath()));
							}
							catch (URISyntaxException e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
		}
		finally {
			if (parent instanceof Closeable) {
				((Closeable) parent).close();
			}
		}
		// rewrite for repository access, for example resources distributed in nar files are not directly accessible otherwise
		URI repositoryRoot = ResourceUtils.getURI(EAIResourceRepository.getInstance().getRoot().getContainer());
		if (repositoryRoot != null) {
			java.lang.String repositoryUri = repositoryRoot.toASCIIString();
			if (!repositoryUri.endsWith("/")) {
				repositoryUri += "/";
			}
			for (int j = 0; j < list.size(); j++) {
				ResourceProperties property = list.get(j);
				// if it is in the repository, let's check if it's an artifact
				java.lang.String propertyUri = property.getUri().toASCIIString();
				if (property instanceof ResourcePropertiesImpl && propertyUri.startsWith(repositoryUri)) {
					Entry entry = EAIResourceRepository.getInstance().getRoot();
					java.lang.String [] specificParts = propertyUri.substring(repositoryUri.length()).split("/");
					for (int i = 0; i < specificParts.length; i++) {
						java.lang.String part = specificParts[i];
						// if we have nar file, it will be mounted as a regular artifact
						if (part.endsWith(".nar")) {
							part = part.substring(0, part.length() - ".nar".length());
						}
						// if we dive into the reserved folders, we have our match
						if (EAIResourceRepository.PUBLIC.equals(part) || EAIResourceRepository.PROTECTED.equals(part) || EAIResourceRepository.PRIVATE.equals(part)) {
							java.lang.String resultingUri = "repository:" + entry.getId() + ":";
							for (int k = i; k < specificParts.length; k++) {
								resultingUri += "/" + specificParts[k];
							}
							try {
								((ResourcePropertiesImpl) property).setUri(new URI(resultingUri));
							}
							catch (URISyntaxException e) {
								throw new RuntimeException(e);
							}
							break;
						}
						Entry child = entry.getChild(part);
						// if we can't find a child by that name, we are on to something else
						if (child == null) {
							break;
						}
						else {
							entry = child;
						}
					}
				}
			}
		}
		return list;
	}

	private void list(Boolean recursive, java.lang.String fileFilter, java.util.List<ResourceProperties> list, be.nabu.libs.resources.api.Resource parent, URI uri) {
		if (parent instanceof ResourceContainer) {
			for (be.nabu.libs.resources.api.Resource child : (ResourceContainer<?>) parent) {
				if (fileFilter == null || child.getName().matches(fileFilter)) {
					list.add(ResourceUtils.properties(child, uri));
				}
				if (recursive != null && recursive && child instanceof ResourceContainer) {
					list(recursive, fileFilter, list, child, URIUtils.getChild(uri, child.getName()));
				}
			}
		}
	}
	
	@WebResult(name = "stream")
	public InputStream read(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") Principal principal) throws IOException {
		if (uri == null) {
			return null;
		}
		be.nabu.libs.resources.api.Resource resolved = ResourceFactory.getInstance().resolve(uri, principal);
		if (resolved == null) {
			throw new FileNotFoundException("Could not find file: " + uri);
		}
		if (!(resolved instanceof ReadableResource)) {
			throw new IOException("The resource is not readable: " + uri);
		}
		return new BufferedInputStream(IOUtils.toInputStream(new ResourceReadableContainer((ReadableResource) resolved)));
	}

	@WebResult(name = "exists")
	public boolean exists(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") Principal principal) throws IOException {
		if (uri == null) {
			return false;
		}
		be.nabu.libs.resources.api.Resource resolve = ResourceFactory.getInstance().resolve(uri, principal);
		if (resolve instanceof Closeable) {
			((Closeable) resolve).close();
		}
		return resolve != null;
	}
	
	public void write(@WebParam(name = "uri") URI uri, @WebParam(name = "stream") InputStream content, @WebParam(name = "append") Boolean append, @WebParam(name = "principal") Principal principal) throws IOException {
		if (uri != null || content != null) {
			// for optimal signalling to connected systems we need to know if the file already existed (update) or was created in the process
			// this may require different actions from people watching the file system
			ResourceState state = ResourceState.UPDATE;
			be.nabu.libs.resources.api.Resource resource = ResourceFactory.getInstance().resolve(uri, principal);
			if (resource == null) {
				resource = ResourceUtils.touch(uri, principal);
				state = ResourceState.CREATE;
			}
			if (resource == null) {
				throw new FileNotFoundException("Could not find or create the resource " + ResourceUtils.cleanForLogging(uri));
			}
			if (append != null && append && !(resource instanceof AppendableResource)) {
				throw new IOException("The resource at " + ResourceUtils.cleanForLogging(uri) + " is not appendable");
			}
			else if (!(resource instanceof WritableResource)) {
				throw new IOException("The resource at " + ResourceUtils.cleanForLogging(uri) + " is not writable");
			}
			WritableContainer<ByteBuffer> writableContainer = append != null && append
				? ((AppendableResource) resource).getAppendable()
				: new ResourceWritableContainer((WritableResource) resource);
			try {
				IOUtils.copyBytes(IOUtils.wrap(content), writableContainer);
				trigger(uri, state);
			}
			finally {
				writableContainer.close();
				resource.close();
			}
		}
	}
	
	public void delete(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") Principal principal) throws IOException {
		if (uri != null) {
			be.nabu.libs.resources.api.Resource resolve = ResourceFactory.getInstance().resolve(uri, principal);
			if (resolve != null) {
				ResourceContainer<?> parent = resolve.getParent();
				try {
					if (parent instanceof ManageableContainer) {
						((ManageableContainer<?>) parent).delete(resolve.getName());
						trigger(uri, ResourceState.DELETE);
					}
				}
				finally {
					if (parent instanceof Closeable) {
						((Closeable) parent).close();
					}
				}
			}
		}
	}
	
	public void mkdir(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") Principal principal) throws IOException {
		if (uri != null) {
			ResourceContainer<?> directory = ResourceUtils.mkdir(uri, principal);
			if (directory instanceof Closeable) {
				((Closeable) directory).close();
			}
			trigger(uri, ResourceState.CREATE);
		}
	}
	
	@Hidden
	public void registerResolver(@WebParam(name = "resolver") java.lang.String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass(name);
		java.lang.Object newInstance = loadClass.newInstance();
		if (newInstance instanceof ResourceResolver) {
			ResourceFactory.getInstance().addResourceResolver((ResourceResolver) newInstance);
		}
	}
	
	private void trigger(URI uri, ResourceState state) {
		if ("repository".equals(uri.getScheme())) {
			try {
				URI childUri = new URI(URIUtils.encodeURI(uri.getSchemeSpecificPart()));
				trigger(state, childUri.getScheme(), childUri.getPath());
			}
			catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
	}
	private void trigger(ResourceState state, java.lang.String artifactId, java.lang.String path) {
		ResourceEvent event = new ResourceEvent();
		event.setState(state);
		event.setArtifactId(artifactId);
		event.setPath(path);
		EAIResourceRepository.getInstance().getEventDispatcher().fire(event, this);
	}
}
