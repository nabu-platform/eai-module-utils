package nabu.utils;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.utils.types.DateProperties;

@WebService(name = "date")
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
	
	@WebResult(name = "date")
	public java.util.Date increment(@WebParam(name = "start") java.util.Date start, @WebParam(name = "increment") Long increment, @WebParam(name = "unit") TimeUnit unit) {
		if (start == null) {
			start = new java.util.Date();
		}
		if (unit == null) {
			unit = TimeUnit.MILLISECONDS;
		}
		if (increment == null) {
			increment = 1l;
		}
		return new java.util.Date(start.getTime() + TimeUnit.MILLISECONDS.convert(increment, unit));
	}
	
	@WebResult(name = "dates")
	public java.util.List<java.util.Date> range(@WebParam(name = "start") java.util.Date start, @WebParam(name = "end") java.util.Date end, @WebParam(name = "increment") Long increment, @WebParam(name = "unit") TimeUnit unit, @WebParam(name = "startInclusive") Boolean startInclusive, @WebParam(name = "endInclusive") Boolean endInclusive) {
		if (start == null) {
			start = new java.util.Date();
		}
		if (end == null) {
			end = new java.util.Date();
		}
		if (startInclusive == null) {
			startInclusive = true;
		}
		if (endInclusive == null) {
			endInclusive = false;
		}
		if (unit == null) {
			unit = TimeUnit.MILLISECONDS;
		}
		if (increment == null) {
			increment = 1l;
		}
		java.util.List<java.util.Date> dates = new java.util.ArrayList<java.util.Date>();
		boolean isFirst = true;
		while (!start.after(end)) {
			if (start.equals(end) && (endInclusive || isFirst)) {
				dates.add(start);
			}
			else if (startInclusive || !isFirst) {
				dates.add(start);
			}
			start = new java.util.Date(start.getTime() + TimeUnit.MILLISECONDS.convert(increment, unit));
			isFirst = false;
		}
		return dates;
	}
}
