package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.nio.charset.Charset;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.containers.chars.HexReadableCharContainer;

@WebService
public class Bytes {
	
	@WebResult(name = "string")
	public String toString(@WebParam(name = "bytes") byte [] bytes, @WebParam(name = "charset") Charset charset) {
		return bytes == null ? null : new String(bytes, charset == null ? Charset.defaultCharset() : charset);
	}

	@WebResult(name = "stream")
	public InputStream toStream(@WebParam(name = "bytes") byte [] bytes) {
		return bytes == null ? null : new ByteArrayInputStream(bytes);
	}
	
	@WebResult(name = "string")
	public java.lang.String toHexString(@WebParam(name = "bytes") byte [] bytes) throws IOException {
		return bytes == null ? null : IOUtils.toString(new HexReadableCharContainer(IOUtils.wrap(bytes, true)));
	}
}
