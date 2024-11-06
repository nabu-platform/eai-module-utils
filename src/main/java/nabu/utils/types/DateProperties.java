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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.lang.String;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import be.nabu.libs.types.simple.Date.XSDFormat;
import be.nabu.libs.types.utils.DateTimeFormat;
import be.nabu.libs.types.utils.TimeFormat;

@XmlRootElement
@XmlType(propOrder = { "format", "timezone", "language" })
public class DateProperties {
	private TimeZone timezone;
	private String language, format;
	
	private DateFormat formatter;
	
	public TimeZone getTimezone() {
		return timezone;
	}
	public void setTimezone(TimeZone timezone) {
		this.timezone = timezone;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	@NotNull
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	@XmlTransient
	public DateFormat getFormatter() {
		if (format == null) {
			throw new NullPointerException("The format can not be null");
		}
		if (formatter == null) {
			synchronized(this) {
				if (formatter == null) {
					Locale locale = language != null ? new Locale(language) : Locale.getDefault();
					XSDFormat xsdFormat = XSDFormat.getXSDFormat(format);
					DateFormat formatter;
					if (xsdFormat != null) {
						switch(xsdFormat) {
							case DATE_TIME:
								formatter = new DateTimeFormat(locale);
							break;
							case TIME:
								formatter = new TimeFormat(locale);
							break;
							default:
								formatter = locale == null ? new SimpleDateFormat(xsdFormat.getFormat()) : new SimpleDateFormat(xsdFormat.getFormat(), locale);
						}
					}
					else {
						formatter = locale == null ? new SimpleDateFormat(format) : new SimpleDateFormat(format, locale);
					}
					formatter.setTimeZone(timezone == null ? TimeZone.getDefault() : timezone);
					this.formatter = formatter;
				}
			}
		}
		return formatter;
	}
}