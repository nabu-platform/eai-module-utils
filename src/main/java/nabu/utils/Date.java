package nabu.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.utils.types.DateProperties;

@WebService(name = "date")
public class Date {
	
	public enum ExtendedTimeUnit {
		MILLISECONDS(TimeUnit.MILLISECONDS, Calendar.MILLISECOND, 1),
		SECONDS(TimeUnit.SECONDS, Calendar.SECOND, 1),
		MINUTES(TimeUnit.MINUTES, Calendar.MINUTE, 1),
		HOURS(TimeUnit.HOURS, Calendar.HOUR, 1),
		DAYS(TimeUnit.DAYS, Calendar.DATE, 1),
		WEEKS(TimeUnit.DAYS, Calendar.WEEK_OF_YEAR, 1),
		MONTHS(null, Calendar.MONTH, 1),
		QUARTERS(null, Calendar.MONTH, 3),
		YEARS(null, Calendar.YEAR, 1)
		;
		
		private TimeUnit timeUnit;
		private int calendarField;
		private int factor;

		private ExtendedTimeUnit(TimeUnit timeUnit, int calendarField, int factor) {
			this.timeUnit = timeUnit;
			this.calendarField = calendarField;
			this.factor = factor;
		}
		
		public java.util.Date increment(java.util.Date date, int amount, TimeZone timezone) {
			Calendar calendar = Calendar.getInstance(timezone);
			calendar.setTime(date);
			calendar.add(calendarField, amount * factor);
			return calendar.getTime();
		}
		
		public double diff(java.util.Date start, java.util.Date stop, TimeZone timezone) {
			if (timeUnit != null) {
				return timeUnit.convert(stop.getTime() - start.getTime(), TimeUnit.MILLISECONDS);
			}
			else {
				int sign = 1;
				if (start.after(stop)) {
					sign = -1;
					java.util.Date tmp = stop;
					stop = start;
					start = tmp;
				}
				Calendar startCalendar = Calendar.getInstance(timezone);
				startCalendar.setTime(start);
				Calendar stopCalendar = Calendar.getInstance(timezone);
				stopCalendar.setTime(stop);
				
				int years = stopCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
				double result;
				if (calendarField == Calendar.MONTH || calendarField == Calendar.YEAR) {
					int monthDiff;
					// same year, just diff months
					if (years == 0) {
						monthDiff = stopCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
					}
					else {
						// 0-based
						int stopMonthToStart = stopCalendar.get(Calendar.MONTH) + 1;
						int startMonthToEnd = 11 - startCalendar.get(Calendar.MONTH);
						// substract one from the years because we calculate that based on the two month offsets, otherwise 2015-12 and 2016-01 would be 13 months apart
						monthDiff = ((years - 1) * 12) + stopMonthToStart + startMonthToEnd;
					}
					result = calendarField == Calendar.YEAR ? ((double) monthDiff) / (12 * factor) : ((double) monthDiff) / factor;
				}
				else if (calendarField == Calendar.WEEK_OF_YEAR) {
					int weekDiff;
					if (years == 0) {
						weekDiff = stopCalendar.get(Calendar.WEEK_OF_YEAR) - startCalendar.get(Calendar.WEEK_OF_YEAR); 
					}
					else {
						int stopWeekToStart = stopCalendar.get(Calendar.WEEK_OF_YEAR);
						int startWeekToEnd = 52 - startCalendar.get(Calendar.WEEK_OF_YEAR);
						weekDiff = ((years - 1) * 52) + stopWeekToStart + startWeekToEnd;
					}
					result = ((double) weekDiff) / factor;
				}
				else {
					throw new RuntimeException("Unknown calendar field");
				}
				return result * sign;
			}
		}
	}
	
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
	public java.util.Date increment(@WebParam(name = "start") java.util.Date start, @WebParam(name = "increment") Integer increment, @WebParam(name = "unit") ExtendedTimeUnit unit, @WebParam(name = "timezone") TimeZone timezone) {
		if (start == null) {
			start = new java.util.Date();
		}
		if (unit == null) {
			unit = ExtendedTimeUnit.MILLISECONDS;
		}
		if (increment == null) {
			increment = 1;
		}
		if (timezone == null) {
			timezone = TimeZone.getDefault();
		}
		return unit.increment(new java.util.Date(), increment, timezone);
	}
	
	@WebResult(name = "dates")
	public java.util.List<java.util.Date> range(@WebParam(name = "start") java.util.Date start, @WebParam(name = "end") java.util.Date end, @WebParam(name = "increment") Integer increment, @WebParam(name = "unit") ExtendedTimeUnit unit, @WebParam(name = "startInclusive") Boolean startInclusive, @WebParam(name = "endInclusive") Boolean endInclusive, @WebParam(name = "timezone") TimeZone timezone) {
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
			unit = ExtendedTimeUnit.MILLISECONDS;
		}
		if (increment == null) {
			increment = 1;
		}
		if (timezone == null) {
			timezone = TimeZone.getDefault();
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
			start = unit.increment(start, increment, timezone);
			isFirst = false;
		}
		return dates;
	}
	
	@WebResult(name = "diff")
	public double diff(@WebParam(name = "start") java.util.Date start, @WebParam(name = "end") java.util.Date end, @WebParam(name = "unit") ExtendedTimeUnit unit, @WebParam(name = "absolute") Boolean absolute, @WebParam(name = "timezone") TimeZone timezone) {
		if (start == null) {
			start = new java.util.Date();
		}
		if (end == null) {
			end = new java.util.Date();
		}
		if (unit == null) {
			unit = ExtendedTimeUnit.MILLISECONDS;
		}
		if (timezone == null) {
			timezone = TimeZone.getDefault();
		}
		double value = unit.diff(start, end, timezone);
		return absolute != null && absolute ? java.lang.Math.abs(value) : value;
	}
}
