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

@WebService
public class Jwt {
	
	@WebResult(name = "unmarshalled")
	public JWTBody unmarshal(
			@WebParam(name = "key") Key key,
			@WebParam(name = "content") java.lang.String content) throws KeyStoreException, IOException, ParseException {
		return JWTUtils.decode(key, content);
	}
	
	@Comment(title = "Note that the timestamps are expressed in seconds.")
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
