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

import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.libs.types.base.Duration;
import nabu.utils.types.DateProperties;
import nabu.utils.types.DateValues;

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
	
	@ServiceDescription(comment = "Generate a timestamp containing the current date")
	@WebResult(name = "date")
	@NotNull
	public java.util.Date now() {
		return new java.util.Date();
	}
	
	@ServiceDescription(comment = "Parse {string|a string} into a date object")
	@WebResult(name = "date")
	public java.util.Date parse(@WebParam(name = "string") String value, @NotNull @WebParam(name = "properties") DateProperties properties) throws ParseException {
		return value == null ? null : properties.getFormatter().parse(value);
	}

	@ServiceDescription(comment = "Format {date|a date} object into a string")
	@WebResult(name = "string")
	public String format(@WebParam(name = "date") java.util.Date value, @NotNull @WebParam(name = "properties") DateProperties properties) {
		return properties.getFormatter().format(value == null ? new java.util.Date() : value);
	}
	
	@ServiceDescription(comment = "Increment {start|a date} with {duration|a certain duration}")
	@WebResult(name = "date")
	public java.util.Date incrementDuration(@WebParam(name = "start") java.util.Date start, @WebParam(name = "times") Integer times, @WebParam(name = "duration") Duration duration, @WebParam(name = "timezone") TimeZone timezone) {
		if (duration == null) {
			return start;
		}
//		Instant instant = Instant.ofEpochMilli(start.getTime());
		if (times == null) {
			times = 1;
		}
		Calendar calendar = timezone == null ? Calendar.getInstance() : Calendar.getInstance(timezone);
		if (start != null) {
			calendar.setTime(start);
		}
		
		if (duration.getYears() != 0) {
			calendar.add(Calendar.YEAR, times * duration.getYears());
		}
		if (duration.getMonths() != 0) {
			calendar.add(Calendar.MONTH, times * duration.getMonths());
		}
		if (duration.getDays() != 0) {
			calendar.add(Calendar.DAY_OF_MONTH, times * duration.getDays());
		}
		if (duration.getHours() != 0) {
			calendar.add(Calendar.HOUR, times * duration.getHours());
		}
		if (duration.getMinutes() != 0) {
			calendar.add(Calendar.MINUTE, times * duration.getMinutes());
		}
		if (duration.getSeconds() != 0.0) {
			double seconds = duration.getSeconds() * times;
			double milliseconds = seconds - java.lang.Math.floor(seconds);
			seconds -= milliseconds;
			if (seconds != 0.0) {
				calendar.add(Calendar.SECOND, (int) seconds);
			}
			if (milliseconds != 0.0) {
				calendar.add(Calendar.MILLISECOND, (int) (milliseconds * 1000));
			}
		}
		return calendar.getTime();
//		java.time.Duration javaDuration = duration.toJavaDuration();
//		Period javaPeriod = duration.toJavaPeriod();
//		for (int i = 0; i < times; i++) {
//			if (subtract) {
//				if (javaDuration != null) {
//					instant = (Instant) javaDuration.subtractFrom(instant);	
//				}
//				if (javaPeriod != null) {
//					instant = (Instant) javaPeriod.subtractFrom(instant);
//				}
//			}
//			else {
//				if (javaDuration != null) {
//					instant = (Instant) javaDuration.addTo(instant);	
//				}
//				if (javaPeriod != null) {
//					instant = (Instant) javaPeriod.addTo(instant);
//				}
//			}
//		}
//		return new java.util.Date(instant.toEpochMilli());
	}
	
	@ServiceDescription(comment = "Increment {start|a date} with {increment|a certain amount} {unit|of a certain time unit}")
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
		return unit.increment(start, increment, timezone);
	}
	
	@ServiceDescription(comment = "Generate a range of dates based on a time increment")
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
	
	@ServiceDescription(comment = "Generate a range of dates based on a duration")
	@WebResult(name = "dates")
	public java.util.List<java.util.Date> rangeDuration(@WebParam(name = "start") java.util.Date start, @WebParam(name = "end") java.util.Date end, @NotNull @WebParam(name = "duration") Duration duration, @WebParam(name = "startInclusive") Boolean startInclusive, @WebParam(name = "endInclusive") Boolean endInclusive, @WebParam(name = "timezone") TimeZone timezone) {
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
			start = incrementDuration(start, 1, duration, timezone);
			isFirst = false;
		}
		return dates;
	}
	
	@ServiceDescription(comment = "Calculate the time difference between {start|a start date} and {end|an end date}")
	@WebResult(name = "diff")
	public double diff(@WebParam(name = "start") java.util.Date start, @WebParam(name = "end") java.util.Date end, @WebParam(name = "unit") ExtendedTimeUnit unit, @WebParam(name = "absolute") Boolean absolute, @WebParam(name = "timezone") TimeZone timezone, @WebParam(name = "round") Boolean round) {
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
		if (absolute != null && absolute) {
			value = java.lang.Math.abs(value);
		}
		// always floor the value, if for example there is 1.8 years difference, we actually have 1 year and a number of months, not 2 years
		if (round != null && round) {
			value = value < 0 ? java.lang.Math.ceil(value) : java.lang.Math.floor(value);
		}
		return value;
	}
	
	@ServiceDescription(comment = "Transform {date|a date} into a timestamp")
	@WebResult(name = "timestamp")
	public Long toTimestamp(@WebParam(name = "date") java.util.Date date, @WebParam(name = "asSeconds") java.lang.Boolean asSeconds) {
		Long timestamp = date == null ? null : date.getTime();
		if (timestamp != null && asSeconds != null && asSeconds) {
			timestamp /= 1000;
		}
		return timestamp;
	}
	
	@ServiceDescription(comment = "Transform {timestamp|a timestamp} into a date")
	@WebResult(name = "date")
	public java.util.Date fromTimestamp(@WebParam(name = "timestamp") Long timestamp, @WebParam(name = "asSeconds") java.lang.Boolean asSeconds) {
		if (timestamp != null && asSeconds != null && asSeconds) {
			timestamp *= 1000;
		}
		return timestamp == null ? null : new java.util.Date(timestamp);
	}
	
	@ServiceDescription(comment = "Parse {date|a date} into its separate values")
	@WebResult(name = "values")
	public DateValues toValues(@WebParam(name = "date") java.util.Date date, @WebParam(name = "timezone") TimeZone timezone) {
		Calendar calendar = Calendar.getInstance(timezone != null ? timezone : TimeZone.getDefault());
		calendar.setTime(date == null ? new java.util.Date() : date);
		DateValues values = new DateValues();
		values.setYear(calendar.get(Calendar.YEAR));
		values.setMonth(calendar.get(Calendar.MONTH) + 1);
		values.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		values.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		values.setMinute(calendar.get(Calendar.MINUTE));
		values.setSecond(calendar.get(Calendar.SECOND));
		values.setMillisecond(calendar.get(Calendar.MILLISECOND));
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		values.setDayOfWeek(dayOfWeek == 1 ? 7 : dayOfWeek - 1);
		values.setWeekOfYear(calendar.get(Calendar.WEEK_OF_YEAR));
		return values;
	}
	
	@ServiceDescription(comment = "Format a date from its separate values")
	@WebResult(name = "date")
	public java.util.Date fromValues(@WebParam(name = "values") DateValues values, @WebParam(name = "timezone") TimeZone timezone) {
		Calendar calendar = Calendar.getInstance(timezone != null ? timezone : TimeZone.getDefault());
		// the day of week is 1-based, 1 being sunday by default
		// put these two at the top so they are overridden by specific date/month values
		if (values.getDayOfWeek() != null) {
			calendar.set(Calendar.DAY_OF_WEEK, values.getDayOfWeek() == 7 ? 1 : values.getDayOfWeek() + 1);
		}
		if (values.getWeekOfYear() != null) {
			calendar.set(Calendar.WEEK_OF_YEAR, values.getWeekOfYear());
		}
		if (values.getYear() != null) {
			calendar.set(Calendar.YEAR, values.getYear());
		}
		if (values.getMonth() != null) {
			calendar.set(Calendar.MONTH, values.getMonth() - 1);
		}
		if (values.getDay() != null) {
			calendar.set(Calendar.DATE, values.getDay());
		}
		if (values.getHour() != null) {
			calendar.set(Calendar.HOUR_OF_DAY, values.getHour());
		}
		if (values.getMinute() != null) {
			calendar.set(Calendar.MINUTE, values.getMinute());
		}
		if (values.getSecond() != null) {
			calendar.set(Calendar.SECOND, values.getSecond());
		}
		if (values.getMillisecond() != null) {
			calendar.set(Calendar.MILLISECOND, values.getMillisecond());
		}
		return calendar.getTime();
	}
	
	@ServiceDescription(comment = "Normalize a date")
	@WebResult(name = "normalized")
	public DateValues normalize(@WebParam(name = "values") DateValues values, @WebParam(name = "timezone") TimeZone timezone) {
		return toValues(fromValues(values, timezone), timezone);
	}
	
	@ServiceDescription(comment = "Transform between time units")
	@WebResult(name = "amount")
	public Long toTimeUnit(@WebParam(name = "amount") Long amount, @WebParam(name = "fromTimeUnit") TimeUnit fromTimeUnit, @NotNull @WebParam(name = "toTimeUnit") TimeUnit toTimeUnit) {
		if (amount == null) {
			return null;
		}
		if (fromTimeUnit == null) {
			fromTimeUnit = TimeUnit.MILLISECONDS;
		}
		return toTimeUnit.convert(amount, fromTimeUnit);
	}
}
