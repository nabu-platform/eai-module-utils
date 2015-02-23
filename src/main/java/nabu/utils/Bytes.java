package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.String;
import java.nio.charset.Charset;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class Bytes {
	
	@WebResult(name = "string")
	public String toString(@WebParam(name = "bytes") byte [] bytes, @WebParam(name = "charset") Charset charset) {
		return new String(bytes, charset);
	}

	@WebResult(name = "stream")
	public InputStream toStream(@WebParam(name = "bytes") byte [] bytes) {
		return new ByteArrayInputStream(bytes);
	}
	
}
