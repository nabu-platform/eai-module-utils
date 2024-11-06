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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.http.api.HTTPRequest;
import be.nabu.libs.http.core.DefaultHTTPRequest;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.mime.impl.FormatException;
import be.nabu.utils.mime.impl.MimeFormatter;
import be.nabu.utils.mime.impl.MimeHeader;
import be.nabu.utils.mime.impl.PlainMimeContentPart;
import be.nabu.utils.mime.impl.PlainMimeMultiPart;
import nabu.utils.types.FormFile;

@WebService
public class Form {
	@WebResult(name = "request")
	public HTTPRequest newMultiPartRequest(@WebParam(name = "endpoint") URI endpoint, @WebParam(name = "values") java.util.List<KeyValuePair> values, @WebParam(name = "files") java.util.List<FormFile> files) throws IOException, FormatException {
		if (values == null && files == null) {
			return null;
		}
		PlainMimeMultiPart multiPart = new PlainMimeMultiPart(null);
		multiPart.setHeader(new MimeHeader("MIME-Version", "1.0"));
		java.lang.String host = endpoint.getHost();
		if (endpoint.getPort() > 0) {
			host += ":" + endpoint.getPort();
		}
		multiPart.setHeader(new MimeHeader("Host", host));
		multiPart.setHeader(new MimeHeader("Content-Type", "multipart/form-data"));
		
		if (files != null) {
			for (FormFile file : files) {
				if (file.getContent() == null) {
					continue;
				}
				multiPart.addChild(new PlainMimeContentPart(multiPart, IOUtils.wrap(file.getContent()),
					new MimeHeader("Content-Type", file.getContentType() == null ? "application/octet-stream" : file.getContentType()),
					new MimeHeader("Content-Disposition", "form-data; name=\"" + (file.getKey() == null ? "file" : file.getKey()) + "\"; filename=\"" + (file.getFileName() == null ? "unnamed" : file.getFileName()) + "\"")
				));
			}
		}
		
		if (values != null) {
			for (KeyValuePair value : values) {
				java.lang.String raw = value.getValue();
				multiPart.addChild(new PlainMimeContentPart(multiPart, IOUtils.wrap(raw.getBytes(), true),
					new MimeHeader("Content-Disposition", "form-data; name=\"" + value.getKey() + "\"")
				));
			}
		}
		
		java.lang.String path = endpoint.getPath();
		if (endpoint.getQuery() != null) {
			path += "?" + endpoint.getQuery();
		}
		if (endpoint.getFragment() != null) {
			path += "#" + endpoint.getFragment();
		}
		
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		new MimeFormatter().format(multiPart, IOUtils.wrap(byteArrayOutputStream));
//		System.out.println("result is: " + new java.lang.String(byteArrayOutputStream.toByteArray()));
		
		return new DefaultHTTPRequest("POST", path, multiPart);
	}
}
