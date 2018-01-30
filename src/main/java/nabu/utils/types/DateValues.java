package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DateValues {
	private Integer millisecond, second, minute, hour, day, month, year, dayOfWeek, weekOfYear;

	public Integer getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(Integer milliseconds) {
		this.millisecond = milliseconds;
	}

	public Integer getSecond() {
		return second;
	}

	public void setSecond(Integer seconds) {
		this.second = seconds;
	}

	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minutes) {
		this.minute = minutes;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hours) {
		this.hour = hours;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Integer getWeekOfYear() {
		return weekOfYear;
	}

	public void setWeekOfYear(Integer weekOfYear) {
		this.weekOfYear = weekOfYear;
	}
	
}
