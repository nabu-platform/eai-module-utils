/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
