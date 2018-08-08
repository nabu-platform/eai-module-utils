package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.utils.io.IOUtils;
import be.nabu.utils.security.DigestAlgorithm;
import be.nabu.utils.security.PBEAlgorithm;
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
	
	@WebResult(name = "encrypted")
	public InputStream pbeEncrypt(@WebParam(name = "data") InputStream input, @WebParam(name = "password") java.lang.String password, @WebParam(name = "algorithm") PBEAlgorithm algorithm) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] bytes = IOUtils.toBytes(IOUtils.wrap(input));
		return new ByteArrayInputStream(SecurityUtils.pbeEncrypt(bytes, password, algorithm).getBytes("ASCII"));
	}
	
	@WebResult(name = "decrypted")
	public InputStream pbeDecrypt(@WebParam(name = "encrypted") InputStream input, @WebParam(name = "password") java.lang.String password, @WebParam(name = "algorithm") PBEAlgorithm algorithm) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] bytes = IOUtils.toBytes(IOUtils.wrap(input));
		return new ByteArrayInputStream(SecurityUtils.pbeDecrypt(new java.lang.String(bytes, "ASCII"), password, algorithm));
	}
}
