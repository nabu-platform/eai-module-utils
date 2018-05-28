package nabu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.utils.security.DigestAlgorithm;
import be.nabu.utils.security.SecurityUtils;

@WebService
public class Security {
	
	@WebResult(name = "bytes")
	public byte [] digest(@WebParam(name = "stream") InputStream input, @WebParam(name = "algorithm") DigestAlgorithm algorithm) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, algorithm);
	}
	
	@WebResult(name = "hash")
	public java.lang.String hash(@WebParam(name = "string") java.lang.String content, @NotNull @WebParam(name = "algorithm") DigestAlgorithm algorithm) throws NoSuchAlgorithmException, IOException {
		return content == null ? null : SecurityUtils.hash(content, algorithm);
	}
	
	@WebResult(name = "valid")
	public java.lang.Boolean validateHash(@WebParam(name = "string") java.lang.String content, @NotNull @WebParam(name = "hash") java.lang.String hash, @NotNull @WebParam(name = "algorithm") DigestAlgorithm algorithm) throws NoSuchAlgorithmException, IOException {
		return content == null ? false : SecurityUtils.check(content, hash, algorithm);
	}
}
