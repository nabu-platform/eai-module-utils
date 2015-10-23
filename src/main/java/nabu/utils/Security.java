package nabu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.utils.security.DigestAlgorithm;
import be.nabu.utils.security.SecurityUtils;

@WebService
public class Security {
	
	@WebResult(name = "bytes")
	public byte [] digest(@WebParam(name = "stream") InputStream input, @WebParam(name = "algorithm") DigestAlgorithm algorithm) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, algorithm);
	}
	
}
