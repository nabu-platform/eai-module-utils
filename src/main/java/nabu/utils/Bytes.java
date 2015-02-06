package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.String;
import java.nio.charset.Charset;

import javax.jws.WebService;

@WebService
public class Bytes {
	
	public String toString(byte [] bytes, Charset charset) {
		return new String(bytes, charset);
	}
	
	public InputStream toStream(byte [] bytes) {
		return new ByteArrayInputStream(bytes);
	}
	
}
