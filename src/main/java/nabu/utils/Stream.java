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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.utils.io.IOUtils;

@WebService
public class Stream {

	@ServiceDescription(comment = "Transform a byte stream into a byte array")
	@WebResult(name = "bytes")
	public byte [] toBytes(@WebParam(name = "stream") InputStream input) throws IOException {
		return input == null ? null : IOUtils.toBytes(IOUtils.wrap(input));
	}
	
	@ServiceDescription(comment = "Format an byte stream into a string")
	@WebResult(name = "string")
	public String toString(@WebParam(name = "stream") InputStream input, @WebParam(name = "charset") Charset charset) throws IOException {
		return input == null ? null : new String(toBytes(input), charset == null ? Charset.defaultCharset() : charset);
	}
	
	@ServiceDescription(comment = "Close a closeable object")
	public void close(@WebParam(name = "closeable") java.lang.Object closeable) throws IOException {
		if (closeable instanceof Closeable) {
			((Closeable) closeable).close();
		}
	}
}
