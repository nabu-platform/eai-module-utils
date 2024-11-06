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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import be.nabu.libs.resources.api.AccessTrackingResource;
import be.nabu.libs.resources.api.AppendableResource;
import be.nabu.libs.resources.api.FiniteResource;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.RenameableResource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.ResourceProperties;
import be.nabu.libs.resources.api.ResourceResolver;
import be.nabu.libs.resources.api.TimestampedResource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.resources.file.FileDirectory;
import be.nabu.libs.resources.file.FileItem;
import be.nabu.libs.resources.impl.ResourcePropertiesImpl;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;
import be.nabu.utils.mime.impl.MimeUtils;

@WebService
public class Resource {
	
	private final class FileVisitorImplementation implements FileVisitor<Path> {
		private final java.lang.String fileFilter, directoryFilter;
		private final Boolean recursive;
		private final Path startingDir;
		private int found = 0, skipped, checked;
		private Integer expectedGroupSize;
		private Integer limit;
		private java.lang.String groupRegex;
		private int overshootFactor;
		private Map<java.lang.String, List<ResourceProperties>> groups;
		private Integer currentFound;
		
		private FileVisitorImplementation(java.lang.String fileFilter, java.lang.String directoryFilter, Boolean recursive, Path startingDir, java.lang.String groupRegex, Integer limit, Integer expectedGroupSize, java.util.Map<java.lang.String, java.util.List<ResourceProperties>> groups, int currentFound) {
			this.fileFilter = fileFilter;
			this.directoryFilter = directoryFilter;
			this.recursive = recursive;
			this.startingDir = startingDir;
			this.groupRegex = groupRegex;
			this.limit = limit;
			this.expectedGroupSize = expectedGroupSize;
			this.groups = groups;
			this.currentFound = currentFound;
			// we overshoot intended group size to balance memory usage of tracking all groups vs higher chance of completing a group early on
			// if we just track all groups in say 200.000 files, we might get a lot of groups before we complete enough
			// if we track the exact amount of groups and we get unlucky in the order they are delivered, we might take a while to complete those exact groups
			// this is only relevant if we have an expected group size to aim for
			this.overshootFactor = 3;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
			// if we have a directory filter, apply it
			if (directoryFilter != null && !startingDir.equals(directory)) {
				java.lang.String directoryName = directory.getFileName().toString();
				// if we don't match, just skip it
				if (!directoryName.matches(directoryFilter)) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				// we want to add it to the resultlist
				else {
					List<ResourceProperties> groupList = groups.get("$default");
					// if the group does not exist yet, check if we still want to track it
					if (groupList == null) {
						groupList = new ArrayList<ResourceProperties>();
						groups.put("$default", groupList);
					}
					ResourcePropertiesImpl properties = new ResourcePropertiesImpl();
					properties.setName(directoryName);
					properties.setContentType(be.nabu.libs.resources.api.Resource.CONTENT_TYPE_DIRECTORY);
					properties.setReadable(true);
					properties.setWritable(true);
					properties.setListable(attrs.isDirectory());
					properties.setSize(attrs.size());
					properties.setLastModified(new Date(attrs.lastModifiedTime().toMillis()));
					properties.setLastAccessed(new Date(attrs.lastAccessTime().toMillis()));
					URI uri = directory.toUri();
					try {
						properties.setUri(new URI("file", null, null, -1, uri.getPath(), uri.getQuery(), uri.getFragment()));
					}
					catch (URISyntaxException e) {
						// should not occur...?
						throw new RuntimeException(e);
					}
					groupList.add(properties);
				}
			}
			return startingDir.equals(directory) || (recursive != null && recursive) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
			checked++;
			// if we match the original filter, we might be interested
			java.lang.String fileName = path.getFileName().toString();
			if ((fileFilter == null && directoryFilter == null) || (fileFilter != null && fileName.matches(fileFilter))) {
				List<ResourceProperties> groupList = null;
				
				if (groupRegex != null) {
					java.lang.String group = fileName.replaceAll(groupRegex, "$1");
					groupList = groups.get(group);
					// if the group does not exist yet, check if we still want to track it
					if (groupList == null) {
						// we only need to overshoot if we want to "prematurely" stop scanning if we have full groups
						// this can only be done if we have an expected group size
						if (limit == null || (expectedGroupSize == null && groups.size() < limit) || (expectedGroupSize != null && groups.size() < limit * overshootFactor)) {
							groupList = new ArrayList<ResourceProperties>();
							groups.put(group, groupList);
						}
					}
				}
				else {
					groupList = groups.get("$default");
					// if the group does not exist yet, check if we still want to track it
					if (groupList == null) {
						groupList = new ArrayList<ResourceProperties>();
						groups.put("$default", groupList);
					}
				}
				// if we are intrigued, add this file to the group
				if (groupList != null) {
					ResourcePropertiesImpl properties = new ResourcePropertiesImpl();
					properties.setName(fileName);
					properties.setContentType(URLConnection.guessContentTypeFromName(fileName));
					properties.setReadable(true);
					properties.setWritable(true);
					properties.setListable(attrs.isDirectory());
					properties.setSize(attrs.size());
					properties.setLastModified(new Date(attrs.lastModifiedTime().toMillis()));
					properties.setLastAccessed(new Date(attrs.lastAccessTime().toMillis()));
					URI uri = path.toUri();
					try {
						properties.setUri(new URI("file", null, null, -1, uri.getPath(), uri.getQuery(), uri.getFragment()));
					}
					catch (URISyntaxException e) {
						// should not occur...?
						throw new RuntimeException(e);
					}
					groupList.add(properties);
					// if we have an expected group size and we have hit the exact size (they _can_ be larger and we don't want to count the same group multiple times)
					// then we update the "found" as in we have "found" an entire group
					// if we are scanning for "full" groups without an expected size, we never know when a group is full so we _have_ to scan them all
					if (expectedGroupSize != null && groupList.size() == expectedGroupSize) {
						found++;
					}
					// if we do no grouping at all, every item is a hit
					else if (groupRegex == null) {
						found++;
					}
				}
				else {
					skipped++;
				}
			}
			// if we have a limit and we have found enough full groups, we terminate the listing
			return limit != null && found + currentFound >= limit ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
			throw exception;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		public int getFound() {
			return found;
		}

		public int getSkipped() {
			return skipped;
		}
		public int getChecked() {
			return checked;
		}
	}
	@WebResult(name = "properties")
	public ResourceProperties properties(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") Principal principal) throws IOException {
		be.nabu.libs.resources.api.Resource resolve = uri == null ? null : ResourceFactory.getInstance().resolve(uri, principal);
		try {
			return resolve == null ? null : ResourceUtils.properties(resolve);
		}
		finally {
			if (resolve instanceof Closeable) {
				((Closeable) resolve).close();
			}
		}
	}
	
	@WebResult(name = "properties")
	public ResourceProperties guess(@WebParam(name = "uri") URI uri) throws IOException {
		ResourcePropertiesImpl properties = new ResourcePropertiesImpl();
		properties.setName(uri.getPath().replaceAll("^.*?/([^/]+)$", "$1"));
		properties.setContentType(URLConnection.guessContentTypeFromName(properties.getName()));
		return properties;
	}

	@WebResult(name = "children")
	public java.util.List<ResourceProperties> list(@WebParam(name = "uri") URI uri, @WebParam(name = "recursive") Boolean recursive, @WebParam(name = "fileFilter") java.lang.String fileFilter, @WebParam(name = "directoryFilter") java.lang.String directoryFilter, @WebParam(name = "principal") Principal principal, @WebParam(name = "limit") Integer limit, @WebParam(name = "groupRegex") java.lang.String groupRegex, @WebParam(name = "expectedGroupSize") Integer expectedGroupSize) throws IOException {
		if (uri == null) {
			return null;
		}
		java.util.Map<java.lang.String, java.util.List<ResourceProperties>> groups = new HashMap<java.lang.String, java.util.List<ResourceProperties>>();
		java.util.List<ResourceProperties> list = new ArrayList<ResourceProperties>();
		be.nabu.libs.resources.api.Resource parent = ResourceFactory.getInstance().resolve(uri, principal);
		try {
			list(recursive, fileFilter, directoryFilter, list, parent, uri, limit, groupRegex, expectedGroupSize, groups, 0);
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


	private int list(final Boolean recursive, final java.lang.String fileFilter, final java.lang.String directoryFilter, final java.util.List<ResourceProperties> list, final be.nabu.libs.resources.api.Resource parent, final URI uri, final Integer limit, final java.lang.String groupRegex, final Integer expectedGroupSize, final java.util.Map<java.lang.String, java.util.List<ResourceProperties>> groups, int currentFound) {
		// if we are using a file system a
		if (parent instanceof FileDirectory) {
			final Path startingDir = ((FileDirectory) parent).getFile().toPath();
			try {
				FileVisitorImplementation visitor = new FileVisitorImplementation(fileFilter, directoryFilter, recursive, startingDir, groupRegex, limit, expectedGroupSize, groups, currentFound);
				Files.walkFileTree(startingDir, visitor);
//				System.out.println("Found " + visitor.getFound() + ", skipped " + visitor.getSkipped() + ", checked " + visitor.getChecked());
				List<List<ResourceProperties>> matrix = new ArrayList<List<ResourceProperties>>(groups.values());
				// return the biggest groups up to limit (if set)
				if (limit != null) {
					Collections.sort(matrix, new Comparator<List<ResourceProperties>>() {
						@Override
						public int compare(List<ResourceProperties> o1, List<ResourceProperties> o2) {
							return o2.size() - o1.size();
						}
					});
//					System.out.println("Groups are: " + groups.keySet());
					int counter = 0;
					for (List<ResourceProperties> single : matrix) {
						list.addAll(single);
						if (++counter >= limit) {
							break;
						}
					}
				}
				else {
					for (List<ResourceProperties> single : matrix) {
						list.addAll(single);
					}
				}
				return currentFound + visitor.getFound();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else if (parent instanceof ResourceContainer) {
			for (be.nabu.libs.resources.api.Resource child : (ResourceContainer<?>) parent) {
				// if you don't fill in any filter, we assume you are interested in all files
				if (child instanceof ReadableResource && (fileFilter == null && directoryFilter == null) || (fileFilter != null && child.getName().matches(fileFilter))) {
					list.add(ResourceUtils.properties(child, uri));
					currentFound++;
				}
				if (child instanceof ResourceContainer) {
					if (directoryFilter != null && child.getName().matches(directoryFilter)) {
						list.add(ResourceUtils.properties(child, uri));
						currentFound++;
					}
					if (recursive != null && recursive) {
						currentFound = list(recursive, fileFilter, directoryFilter, list, child, URIUtils.getChild(uri, child.getName()), limit, groupRegex, expectedGroupSize, groups, currentFound);
					}
				}
				if (limit != null && currentFound >= limit) {
					// if we overshot, trim it down
					if (currentFound > limit) {
						list.retainAll(list.subList(0, limit));
					}
					break;
				}
			}
		}
		return currentFound;
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

	public void rename(@WebParam(name = "uri") URI uri, @WebParam(name = "principal") Principal principal, @WebParam(name = "newName") java.lang.String newName) throws IOException {
		if (uri == null) {
			return;
		}
		be.nabu.libs.resources.api.Resource resolve = ResourceFactory.getInstance().resolve(uri, principal);
		if (resolve instanceof RenameableResource) {
			((RenameableResource) resolve).rename(newName);
		}
		else if (resolve.getParent() instanceof ManageableContainer) {
			ManageableContainer<?> parent = (ManageableContainer<?>) resolve.getParent();
			ResourceUtils.copy(resolve, parent, newName);
			parent.delete(resolve.getName());
		}
		else {
			throw new IllegalStateException("Can not rename resource, it is not renameable and the parent is not modifiable: " + uri);
		}
		if (resolve instanceof Closeable) {
			((Closeable) resolve).close();
		}
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
