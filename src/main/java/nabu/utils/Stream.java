package nabu.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebService;

import be.nabu.utils.io.IOUtils;

@WebService
public class Stream {
	
	public byte [] toBytes(@WebParam(name = "stream") InputStream input) throws IOException {
		return IOUtils.toBytes(IOUtils.wrap(input));
	}
	
	public String toString(@WebParam(name = "stream") InputStream input, @WebParam(name = "charset") Charset charset) throws IOException {
		return new String(toBytes(input), charset);
	}
	
	public void close(@WebParam(name = "closeable") Closeable closeable) throws IOException {
		closeable.close();
	}
	
	
}
