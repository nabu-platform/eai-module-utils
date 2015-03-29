package nabu.utils;

import java.text.ParseException;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

@WebService
public class Date {
	
	@WebResult(name = "date")
	public java.util.Date now() {
		return new java.util.Date();
	}
	
	@WebResult(name = "date")
	public java.util.Date parse(@WebParam(name = "string") String value, @NotNull @WebParam(name = "properties") DateProperties properties) throws ParseException {
		return value == null ? null : properties.getFormatter().parse(value);
	}

	@WebResult(name = "string")
	public String format(@WebParam(name = "date") java.util.Date value, @NotNull @WebParam(name = "properties") DateProperties properties) {
		return properties.getFormatter().format(value);
	}
	
}
