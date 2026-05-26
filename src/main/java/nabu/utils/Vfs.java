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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.AppendableResource;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.ResourceProperties;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;

@WebService
public class Vfs {
	
	@ServiceDescription(comment = "Resolve {uri|a URI}")
	@WebResult(name = "resource")
	public Resource resolve(@WebParam(name = "uri") URI uri) throws IOException {
		return ResourceFactory.getInstance().resolve(uri, null);
	}
	
	@ServiceDescription(comment = "Get child {name|a name} from {resource|a resource}")
	@WebResult(name = "resource")
	public Resource cd(@NotNull @WebParam(name = "resource") Resource resource, @NotNull @WebParam(name = "name") String name) {
		if (!(resource instanceof ResourceContainer)) {
			throw new IllegalArgumentException("The resource is not a container");
		}
		return ((ResourceContainer<?>) resource).getChild(name);
	}
	
	@ServiceDescription(comment = "Remove {name|a name} from {resource|a resource}")
	public void rm(@NotNull @WebParam(name = "resource") Resource resource, @NotNull @WebParam(name = "name") String name) throws IOException {
		if (!(resource instanceof ManageableContainer)) {
			throw new IllegalArgumentException("The resource is not a container");
		}
		((ManageableContainer<?>) resource).delete(name);
	}
	
	@ServiceDescription(comment = "Create directory {name|a name} in {resource|a resource}")
	@WebResult(name = "resource")
	public Resource mkdir(@NotNull @WebParam(name = "resource") Resource resource, @NotNull @WebParam(name = "name") String name) throws IOException {
		if (!(resource instanceof ManageableContainer)) {
			throw new IllegalArgumentException("The resource is not a container");
		}
		return ((ManageableContainer<?>) resource).create(name, Resource.CONTENT_TYPE_DIRECTORY);
	}
	
	@ServiceDescription(comment = "Create {name|a resource} in {resource|a container}")
	@WebResult(name = "resource")
	public Resource touch(@NotNull @WebParam(name = "resource") Resource resource, @NotNull @WebParam(name = "name") String name, @WebParam(name = "contentType") String contentType) throws IOException {
		if (!(resource instanceof ManageableContainer)) {
			throw new IllegalArgumentException("The resource is not a container");
		}
		if (contentType == null) {
			contentType = URLConnection.guessContentTypeFromName(name);
		}
		return ((ManageableContainer<?>) resource).create(name, contentType);
	}
	
	@ServiceDescription(comment = "Read {resource|a resource}")
	@WebResult(name = "stream")
	public InputStream read(@NotNull @WebParam(name = "resource") Resource resource) throws IOException {
		if (!(resource instanceof ReadableResource)) {
			throw new IllegalArgumentException("The resource is not readable");
		}
		return new BufferedInputStream(IOUtils.toInputStream(((ReadableResource) resource).getReadable()));
	}
	
	@ServiceDescription(comment = "Write {stream|a stream} to {resource|a resource}")
	public void write(@NotNull @WebParam(name = "resource") Resource resource, @WebParam(name = "stream") InputStream input, @WebParam(name = "append") Boolean append) throws IOException {
		WritableContainer<ByteBuffer> writable;
		if (append != null && append) {
			if (!(resource instanceof AppendableResource)) {
				throw new IllegalArgumentException("The resource is not appendable");
			}
			writable = ((AppendableResource) resource).getAppendable();
		}
		else {
			if (!(resource instanceof WritableResource)) {
				throw new IllegalArgumentException("The resource is not appendable");
			}
			writable = ((WritableResource) resource).getWritable();
		}
		OutputStream output = new BufferedOutputStream(IOUtils.toOutputStream(writable));
		try {
			int read = 0;
			byte [] buffer = new byte[8096];
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
			}
		}
		finally {
			output.close();
		}
	}

	@ServiceDescription(comment = "Get properties for {resource|a resource}")
	@WebResult(name = "properties")
	public ResourceProperties properties(@NotNull @WebParam(name = "resource") Resource resource) {
		return ResourceUtils.properties(resource);
	}
	
	@ServiceDescription(comment = "List the children of {resource|a resource}")
	@WebResult(name = "children")
	public java.util.List<Resource> ls(@NotNull @WebParam(name = "resource") Resource resource) {
		if (!(resource instanceof ResourceContainer)) {
			throw new IllegalArgumentException("The resource is not a container");
		}
		java.util.List<Resource> children = new ArrayList<Resource>();
		for (Resource child : (ResourceContainer<?>) resource) {
			children.add(child);
		}
		return children;
	}
}
