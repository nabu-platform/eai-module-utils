package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class String {
	
	@WebResult(name = "bytes")
	public byte [] toBytes(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return string.getBytes(charset);
	}
	
	@WebResult(name = "stream")
	public InputStream toStream(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return new ByteArrayInputStream(toBytes(string, charset));
	}
}
