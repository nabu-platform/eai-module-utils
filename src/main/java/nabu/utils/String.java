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

package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.api.NamingConvention;
import be.nabu.libs.services.api.ServiceDescription;

@WebService
public class String {

	@WebResult(name = "padded")
	@ServiceDescription(comment = "Pad {content|the content} with {pad|a pad} on the left (by default) to {length|a given length}")
	public java.lang.String pad(@WebParam(name = "content") java.lang.String content, @WebParam(name = "pad") java.lang.String pad, @WebParam(name = "length") java.lang.Integer length, @WebParam(name = "leftAlign") Boolean leftAlign) {
		if (length == null) {
			return null;
		}
		if (content == null) {
			content = "";
		}
		if (pad == null || pad.isEmpty()) {
			pad = " ";
		}
		while (content.length() < length) {
			int padLength = java.lang.Math.min(pad.length(), length - content.length());
			if (padLength < pad.length()) {
				if (leftAlign != null && leftAlign) {
					pad = pad.substring(0, padLength);
				}
				else {
					pad = pad.substring(pad.length() - padLength);
				}
			}
			if (leftAlign != null && leftAlign) {
				content += pad;
			}
			else {
				content = pad + content;
			}
		}
		return content;
	}
	
	@WebResult(name = "reversed")
	public java.lang.String reverse(@WebParam(name = "content") java.lang.String content) {
		return content == null ? null : new StringBuilder(content).reverse().toString();
	}
	
	@ServiceDescription(comment = "Apply a naming convention to a string")
	@WebResult(name = "conventionized")
	public java.lang.String conventionize(@WebParam(name = "content") java.lang.String content, @WebParam(name = "from") NamingConvention from, @WebParam(name = "to") NamingConvention to) {
		if (content == null) {
			return null;
		}
		if (to == null) {
			to = NamingConvention.LOWER_CAMEL_CASE;
		}
		return to.apply(content, from == null ? NamingConvention.LOWER_CAMEL_CASE : from);
	}
	
	@ServiceDescription(comment = "Get the total size of a string")
	@WebResult(name = "size")
	public java.lang.Integer size(@WebParam(name = "string") java.lang.String content) {
		return content != null ? content.length() : null;
	}

	@ServiceDescription(comment = "Find matching parts of a string")
	@WebResult(name = "matches")
	public List<java.lang.String> find(@WebParam(name = "content") java.lang.String content, @WebParam(name = "find") java.lang.String find) {
		final Pattern pattern = Pattern.compile(find);
		List<java.lang.String> matches = new ArrayList<java.lang.String>();
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			if (matcher.groupCount() > 0) {
				matches.add(matcher.group(1));
			}
			else {
				matches.add(matcher.group());
			}
		}
		return matches;
	}
	
	@ServiceDescription(comment = "Replace {match|part} {content|in a string} with {replace|nothing}")
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
	
	@ServiceDescription(comment = "Parse a string into a byte array")
	@WebResult(name = "bytes")
	public byte [] toBytes(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return string == null ? null : string.getBytes(charset == null ? Charset.defaultCharset() : charset);
	}
	
	@ServiceDescription(comment = "Parse a string into a byte stream")
	@WebResult(name = "stream")
	public InputStream toStream(@WebParam(name = "string") java.lang.String string, @WebParam(name = "charset") Charset charset) {
		return string == null ? null : new ByteArrayInputStream(toBytes(string, charset));
	}
	
	@ServiceDescription(comment = "Split {string|a string} into multiple parts using {separator|a regex separator}")
	@WebResult(name = "parts")
	public List<java.lang.String> split(@WebParam(name = "string") java.lang.String string, @NotNull @WebParam(name = "separator") java.lang.String separator) {
		return string == null ? null : new ArrayList<java.lang.String>(Arrays.asList(string.split(separator)));
	}
	
	@ServiceDescription(comment = "Join {parts|multiple parts} into a single string using {separator|a seperator}")
	@WebResult(name = "string")
	public java.lang.String join(@WebParam(name = "parts") List<java.lang.String> strings, @WebParam(name = "separator") java.lang.String separator) {
		if (strings == null) {
			return null;
		}
		if (separator == null) {
			separator = "";
		}
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (int i = 0; i < strings.size(); i++) {
			// ignore null values
			if (strings.get(i) == null) {
				continue;
			}
			if (isFirst) {
				isFirst = false;
			}
			else {
				builder.append(separator);
			}
			builder.append(strings.get(i));
		}
		return isFirst ? null : builder.toString();
	}
	
	@ServiceDescription(comment = "Uppercase all the letters in a string")
	@WebResult(name = "upper")
	public java.lang.String upper(@WebParam(name = "string") java.lang.String string) {
		return string == null ? null : string.toUpperCase();
	}
	
	@ServiceDescription(comment = "Lowercase all the letters in a string")
	@WebResult(name = "lower")
	public java.lang.String lower(@WebParam(name = "string") java.lang.String string) {
		return string == null ? null : string.toLowerCase();
	}
	
	private static final Pattern DIACRITICS_AND_FRIENDS = 
	        Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
	
	@ServiceDescription(comment = "Normalize {string|the characters in a string}")
	@WebResult(name = "normalized")
	public java.lang.String normalize(@WebParam(name = "string") java.lang.String string) {
		if (string == null) {
            return null;
        }
		
        // decompose characters into base characters and combining marks
		// for example é becomes e and ´
        java.lang.String normalized = Normalizer.normalize(string, Normalizer.Form.NFD);
        
        // remove the combining marks
        return DIACRITICS_AND_FRIENDS.matcher(normalized).replaceAll("");
	}
	
	@ServiceDescription(comment = "Get part of a string")
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
	
	@ServiceDescription(comment = "Parse a hexadecimal string into bytes")
	@WebResult(name = "bytes")
	@Deprecated(since = "2025-08-12T13:50:19")
	public byte[] fromHexString(@WebParam(name = "hexString") java.lang.String string) throws IOException {
		return new BigInteger(string, 16).toByteArray();
	}
	
	@ServiceDescription(comment = "Format a string using the given parameters")
	@WebResult(name = "formatted")
	public java.lang.String format(@WebParam(name = "template") java.lang.String template, @WebParam(name = "parameters") List<java.lang.Object> parameters) {
		if (template == null) {
			return null;
		}
		return java.lang.String.format(template, parameters == null ? new Object[0] : parameters.toArray());
	}
	
	@ServiceDescription(comment = "Trim the whitespace on each side of the string")
	@WebResult(name = "trimmed")
	public java.lang.String trim(@WebParam(name = "string") java.lang.String string) {
		return string == null ? null : string.trim();
	}
	
	@WebResult(name = "groups")
	public List<java.lang.String> regexGroups(@WebParam(name = "string") java.lang.String string, @NotNull @WebParam(name = "regex") java.lang.String regex) {
		if (string == null) {
			return null;
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		List<java.lang.String> list = new ArrayList<java.lang.String>();
		if (matcher.matches()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				list.add(matcher.group(i));
			}
		}
		return list;
	}
}
