package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

@WebService
public class String {
	
	@WebResult(name = "content")
	public java.lang.String replace(@WebParam(name = "content") java.lang.String content, @WebParam(name = "match") java.lang.String find, @WebParam(name = "replace") java.lang.String replace, @WebParam(name = "useRegex") Boolean useRegex) {
		if (content == null) {
			return null;
		}
		else if (find == null) {
			return content;
		}
		if (useRegex == null) {
			useRegex = false;
		}
		if (replace == null) {
			replace = "";
		}
		return useRegex ? content.replaceAll(find, replace) : content.replace(find, replace);
	}
	
	@WebResult(name = "bytes")
	public byte [] toBytes(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return string == null ? null : string.getBytes(charset == null ? Charset.defaultCharset() : charset);
	}
	
	@WebResult(name = "stream")
	public InputStream toStream(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return string == null ? null : new ByteArrayInputStream(toBytes(string, charset));
	}
	
	@WebResult(name = "parts")
	public List<java.lang.String> split(@WebParam(name = "string") java.lang.String string, @NotNull @WebParam(name = "separator") java.lang.String separator) {
		return string == null ? null : new ArrayList<java.lang.String>(Arrays.asList(string.split(separator)));
	}
	
	@WebResult(name = "string")
	public java.lang.String join(@WebParam(name = "parts") List<java.lang.String> strings, @WebParam(name = "separator") java.lang.String separator) {
		if (strings == null) {
			return null;
		}
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
	
	@WebResult(name = "upper")
	public java.lang.String upper(@WebParam(name = "string") java.lang.String string) {
		return string == null ? null : string.toUpperCase();
	}
	
	@WebResult(name = "lower")
	public java.lang.String lower(@WebParam(name = "string") java.lang.String string) {
		return string == null ? null : string.toLowerCase();
	}
	
	@WebResult(name = "substring")
	public java.lang.String substring(@WebParam(name = "string") java.lang.String string, @WebParam(name = "start") Integer start, @WebParam(name = "stop") Integer stop) {
		if (string == null || (start == null && stop == null)) {
			return string;
		}
		if (start == null) {
			start = 0;
		}
		if (stop == null) {
			stop = string.length();
		}
		return string.substring(start, stop);
	}
	
	@WebResult(name = "bytes")
	public byte[] fromHexString(@WebParam(name = "hexString") java.lang.String string) throws IOException {
		return new BigInteger(string, 16).toByteArray();
	}
	
	@WebResult(name = "formatted")
	public java.lang.String format(@WebParam(name = "template") java.lang.String template, @WebParam(name = "parameters") List<java.lang.Object> parameters) {
		if (template == null) {
			return null;
		}
		return java.lang.String.format(template, parameters == null ? new Object[0] : parameters.toArray());
	}
}
