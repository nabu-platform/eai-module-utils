package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class String {
	
	@WebResult(name = "bytes")
	public byte [] toBytes(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return string == null ? null : string.getBytes(charset == null ? Charset.defaultCharset() : charset);
	}
	
	@WebResult(name = "stream")
	public InputStream toStream(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return string == null ? null : new ByteArrayInputStream(toBytes(string, charset));
	}
	
	@WebResult(name = "parts")
	public List<java.lang.String> split(@WebParam(name = "string") java.lang.String string, @WebParam(name = "separator") java.lang.String separator) {
		return new ArrayList<java.lang.String>(Arrays.asList(string.split(separator)));
	}
	
	@WebResult(name = "string")
	public java.lang.String join(@WebParam(name = "parts") List<java.lang.String> strings, @WebParam(name = "separator") java.lang.String separator) {
		if (separator == null) {
			separator = "";
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strings.size(); i++) {
			if (i > 0) {
				builder.append(separator);
			}
			builder.append(strings.get(i));
		}
		return builder.toString();
	}
}
