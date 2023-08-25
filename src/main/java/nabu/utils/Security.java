package nabu.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;
import javax.xml.XMLConstants;
import javax.xml.crypto.MarshalException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.VirusInfection;
import be.nabu.eai.repository.api.VirusScanner;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.security.BCSecurityUtils;
import be.nabu.utils.security.DigestAlgorithm;
import be.nabu.utils.security.MacAlgorithm;
import be.nabu.utils.security.PBEAlgorithm;
import be.nabu.utils.security.Rfc2898DeriveBytes;
import be.nabu.utils.security.SecurityUtils;
import nabu.utils.types.SignatureResult;

@WebService
public class Security {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@WebResult(name = "result")
	public VirusInfection scanForVirus(@WebParam(name = "scannerId") java.lang.String scannerId, @WebParam(name = "stream") InputStream input) throws ServiceException {
		VirusScanner scanner;
		if (scannerId == null) {
			java.lang.String context = ServiceUtils.getServiceContext(ServiceRuntime.getRuntime());
			scanner = EAIResourceRepository.getInstance().resolveFor(context, VirusScanner.class);
		}
		else {
			scanner = (VirusScanner) EAIResourceRepository.getInstance().resolve(scannerId);
		}
		// if you want to be able to scan "best effort" (so basically if you have configured one), use features to toggle it
		// as an example if you want a framework that performs a virus scan if available -> hide it behind a feature
		if (scanner == null) {
			throw new IllegalStateException("No virus scanner found");
		}
		return input == null ? null : scanner.scan(input);
	}
	
	@WebResult(name = "bytes")
	public byte [] digest(@WebParam(name = "stream") InputStream input, @WebParam(name = "algorithm") DigestAlgorithm algorithm) throws NoSuchAlgorithmException, IOException {
		return input == null ? null : SecurityUtils.digest(input, algorithm);
	}
	
	public java.lang.String mac(@WebParam(name = "key") byte[] key, @WebParam(name = "stream") InputStream content, @WebParam(name = "algorithm") MacAlgorithm algorithm) throws NoSuchAlgorithmException, IOException, InvalidKeyException, IllegalStateException {
		return SecurityUtils.encodeMac(key, content, algorithm.name());
	}
	
	@WebResult(name = "hash")
	public java.lang.String hash(@WebParam(name = "string") java.lang.String content, @NotNull @WebParam(name = "algorithm") DigestAlgorithm algorithm) throws NoSuchAlgorithmException, IOException {
		return content == null ? null : SecurityUtils.hash(content, algorithm);
	}
	
	@WebResult(name = "valid")
	public java.lang.Boolean validateHash(@WebParam(name = "string") java.lang.String content, @NotNull @WebParam(name = "hash") java.lang.String hash, @NotNull @WebParam(name = "algorithm") DigestAlgorithm algorithm) throws NoSuchAlgorithmException, IOException {
		try {
			return content == null || hash == null ? false : SecurityUtils.check(content, hash, algorithm);
		}
		catch (Exception e) {
			logger.warn("Could not validate hash", e);
			return false;
		}
	}
	
	@WebResult(name = "encrypted")
	public InputStream pbeEncrypt(@WebParam(name = "data") InputStream input, @WebParam(name = "password") java.lang.String password, @WebParam(name = "algorithm") PBEAlgorithm algorithm) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidParameterSpecException {
		byte[] bytes = IOUtils.toBytes(IOUtils.wrap(input));
		return new ByteArrayInputStream(SecurityUtils.pbeEncrypt(bytes, password, algorithm).getBytes("ASCII"));
	}
	
	@WebResult(name = "decrypted")
	public InputStream pbeDecrypt(@WebParam(name = "encrypted") InputStream input, @WebParam(name = "password") java.lang.String password, @WebParam(name = "algorithm") PBEAlgorithm algorithm) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] bytes = IOUtils.toBytes(IOUtils.wrap(input));
		return new ByteArrayInputStream(SecurityUtils.pbeDecrypt(new java.lang.String(bytes, "ASCII"), password, algorithm));
	}
	
	@WebResult(name = "key")
	public Key parseKeyPem(@WebParam(name = "pem") byte[] pem) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, IOException {
		return BCSecurityUtils.parseKeyPem(new StringReader(new java.lang.String(pem, "ASCII")));
	}
	
	@WebResult(name = "certificate")
	public Certificate parseSerializedCertificate(@WebParam(name = "serialized") java.lang.String serialized) throws CertificateException, IOException {
		return SecurityUtils.decodeCertificate(serialized);
	}
	
	@WebResult(name = "signatureResult")
	public SignatureResult xmlSignatureValidate(@WebParam(name = "xml") java.lang.String xml, @WebParam(name = "signaturePath") java.lang.String signaturePath, @WebParam(name = "certificate") Certificate certificate) throws MarshalException, SAXException, IOException, ParserConfigurationException {
		SignatureResult result = new SignatureResult();
		Document document = toDocument(xml, Charset.forName("UTF-8"), true);
		result.setValid(SecurityUtils.verifyXml(document.getDocumentElement(), signaturePath, certificate.getPublicKey()));
		return result;
	}
	
	private static Document toDocument(InputStream xml, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// no DTD
		factory.setValidating(false);
		factory.setNamespaceAware(namespaceAware);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		// allow no external access, as defined http://docs.oracle.com/javase/7/docs/api/javax/xml/XMLConstants.html#FEATURE_SECURE_PROCESSING an empty string means no protocols are allowed
		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		}
		catch (Exception e) {
			// not supported in later versions..............
		}
		return factory.newDocumentBuilder().parse(xml);
	}
	
	private static Document toDocument(java.lang.String xml, Charset encoding, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {
		InputStream input = new ByteArrayInputStream(xml.getBytes(encoding));
		return toDocument(input, namespaceAware);
	}
	
	@WebResult(name = "encoded")
	public java.lang.String pbkdf2Generate(@WebParam(name = "content") java.lang.String content) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		return Rfc2898DeriveBytes.generateCryptoHash(content);
	}
	
	@WebResult(name = "valid")
	public java.lang.Boolean pbkdf2Validate(@WebParam(name = "content") java.lang.String content, @WebParam(name = "encoded") java.lang.String encoded) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		return Rfc2898DeriveBytes.validateCryptoHash(encoded, content);
	}
}
