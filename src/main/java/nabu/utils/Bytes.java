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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.math.BigInteger;
import java.nio.charset.Charset;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.containers.chars.HexReadableCharContainer;

@WebService
public class Bytes {
	
	@ServiceDescription(comment = "Format a byte array into a string")
	@WebResult(name = "string")
	public String toString(@WebParam(name = "bytes") byte [] bytes, @WebParam(name = "charset") Charset charset) {
		return bytes == null ? null : new String(bytes, charset == null ? Charset.defaultCharset() : charset);
	}

	@ServiceDescription(comment = "Transform a byte array into a byte stream")
	@WebResult(name = "stream")
	public InputStream toStream(@WebParam(name = "bytes") byte [] bytes) {
		return bytes == null ? null : new ByteArrayInputStream(bytes);
	}
	
	@ServiceDescription(comment = "Format a byte array into a hexadecimal string")
	@WebResult(name = "string")
	public java.lang.String toHexString(@WebParam(name = "bytes") byte [] bytes) throws IOException {
		return bytes == null ? null : IOUtils.toString(new HexReadableCharContainer(IOUtils.wrap(bytes, true)));
	}
	
	@ServiceDescription(comment = "Parse a hexadecimal string into bytes")
	@WebResult(name = "bytes")
	public byte[] fromHexString(@WebParam(name = "hexString") java.lang.String string) throws IOException {
		return new BigInteger(string, 16).toByteArray();
	}
	
	@WebResult(name = "size")
	public Integer size(@WebParam(name = "bytes") byte [] bytes) {
		return bytes == null ? null : bytes.length;
	}
}
