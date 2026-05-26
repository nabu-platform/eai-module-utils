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

import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.text.ParseException;

import javax.crypto.SecretKey;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.api.Comment;
import be.nabu.libs.http.jwt.JWTBody;
import be.nabu.libs.http.jwt.JWTUtils;
import be.nabu.libs.http.jwt.enums.JWTAlgorithm;
import be.nabu.libs.services.api.ServiceDescription;

@WebService
public class Jwt {
	
	@ServiceDescription(comment = "Unmarshal JWT {content|content} with {key|a key}")
	@WebResult(name = "unmarshalled")
	public JWTBody unmarshal(
			@WebParam(name = "key") Key key,
			@WebParam(name = "content") java.lang.String content) throws KeyStoreException, IOException, ParseException {
		return JWTUtils.decode(key, content);
	}
	
	@Comment(title = "Note that the timestamps are expressed in seconds.")
	@ServiceDescription(comment = "Marshal JWT {content|content} with {key|a key} and {algorithm|auto}")
	@WebResult(name = "marshalled")
	public java.lang.String marshal(
			@WebParam(name = "key") Key key, 
			@WebParam(name = "content") JWTBody body,
			@WebParam(name = "algorithm") JWTAlgorithm algorithm) throws KeyStoreException, IOException {
		// instead of opting for the most secure, we balance overall jwt token size with security
		// if you want the more secure algorithms, set it explicitly
		if (algorithm == null) {
			algorithm = key instanceof SecretKey ? JWTAlgorithm.HS256 : JWTAlgorithm.RS256;
		}
		return JWTUtils.encode(key, body, algorithm);
	}
}
