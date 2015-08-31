package nabu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.containers.chars.HexReadableCharContainer;
import be.nabu.utils.security.DigestAlgorithm;
import be.nabu.utils.security.SecurityUtils;

@WebService
public class Digest {
	
	@WebResult(name = "string")
	public java.lang.String toString(@WebParam(name = "digest") byte [] digest) throws IOException {
		return digest == null ? null : IOUtils.toString(new HexReadableCharContainer(IOUtils.wrap(digest, true)));
	}
	@WebResult(name = "bytes")
	public byte [] md5(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.MD5);
	}
	@WebResult(name = "bytes")
	public byte [] sha1(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.SHA1);
	}
	@WebResult(name = "bytes")
	public byte [] sha256(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.SHA256);
	}
	@WebResult(name = "bytes")
	public byte [] sha384(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.SHA384);
	}
	@WebResult(name = "bytes")
	public byte [] sha224(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.SHA224);
	}
	@WebResult(name = "bytes")
	public byte [] sha512(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.SHA512);
	}
	@WebResult(name = "bytes")
	public byte [] gost3411(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.GOST3411);
	}
	@WebResult(name = "bytes")
	public byte [] ripemd160(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.RIPEMD160);
	}
	@WebResult(name = "bytes")
	public byte [] ripemd128(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.RIPEMD128);
	}
	@WebResult(name = "bytes")
	public byte [] ripemd256(@WebParam(name = "stream") InputStream input) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, DigestAlgorithm.RIPEMD256);
	}
}
