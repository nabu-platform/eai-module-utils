package nabu.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.utils.io.IOUtils;

@WebService
public class Stream {
	
	@WebResult(name = "bytes")
	public byte [] toBytes(@WebParam(name = "stream") InputStream input) throws IOException {
		return input == null ? null : IOUtils.toBytes(IOUtils.wrap(input));
	}
	
	@WebResult(name = "string")
	public String toString(@WebParam(name = "stream") InputStream input, @WebParam(name = "charset") Charset charset) throws IOException {
		return input == null ? null : new String(toBytes(input), charset == null ? Charset.defaultCharset() : charset);
	}
	
	public void close(@WebParam(name = "closeable") Closeable closeable) throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}
}
