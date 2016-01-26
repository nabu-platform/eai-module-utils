package nabu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.repository.artifacts.keystore.DefinedKeyStore;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.mime.api.ContentPart;
import be.nabu.utils.mime.api.Header;
import be.nabu.utils.mime.api.ModifiablePart;
import be.nabu.utils.mime.api.Part;
import be.nabu.utils.mime.api.MultiPart;
import be.nabu.utils.mime.impl.FormatException;
import be.nabu.utils.mime.impl.MimeHeader;
import be.nabu.utils.mime.impl.MimeUtils;
import be.nabu.utils.mime.impl.PlainMimeContentPart;
import be.nabu.utils.mime.impl.PlainMimeEmptyPart;
import be.nabu.utils.mime.impl.PlainMimeMultiPart;
import be.nabu.utils.mime.impl.PullableMimeFormatter;
import be.nabu.utils.security.SignatureType;

@WebService
public class Mime {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "headers")
	public List<Header> getHeaders(@WebParam(name = "headers") List<Header> headers, @WebParam(name = "name") @NotNull String name) {
		if (headers == null) {
			return null;
		}
		Header[] result = MimeUtils.getHeaders(name, headers.toArray(new Header[headers.size()]));
		return result == null ? null : new ArrayList<Header>(Arrays.asList(result));
	}
	
	@WebResult(name = "result")
	public boolean hasChildren(@WebParam(name = "part") Part part) {
		return part instanceof MultiPart;
	}
	
	@WebResult(name = "result")
	public boolean hasContent(@WebParam(name = "part") Part part) {
		return part instanceof ContentPart;
	}
	
	@WebResult(name = "content")
	public InputStream getContent(@WebParam(name = "part") Part part) throws IOException {
		return part instanceof ContentPart ? IOUtils.toInputStream(((ContentPart) part).getReadable()) : null;
	}
	
	@WebResult(name = "parts")
	public List<Part> getChildren(@WebParam(name = "part") Part part) {
		if (!(part instanceof MultiPart)) {
			return null;
		}
		List<Part> children = new ArrayList<Part>();
		for (Part child : (MultiPart) part) {
			children.add(child);
		}
		return children;
	}
	
	@WebResult(name = "header")
	public Header newHeader(@WebParam(name = "name") @NotNull String name, @WebParam(name = "value") @NotNull String value, @WebParam(name = "comments") List<String> comments) {
		return new MimeHeader(name, value, comments == null ? new String[0] : comments.toArray(new String[comments.size()]));
	}
	
	@WebResult(name = "part")
	public Part newContentPart(@WebParam(name = "content") @NotNull InputStream content, @WebParam(name = "headers") List<Header> headers) {
		ModifiablePart part = new PlainMimeContentPart(null, IOUtils.wrap(content));
		if (headers != null && !headers.isEmpty()) {
			part.setHeader(headers.toArray(new Header[headers.size()]));
		}
		return part;
	}
	
	@WebResult(name = "part")
	public Part newEmptyPart(@WebParam(name = "headers") List<Header> headers) {
		ModifiablePart part = new PlainMimeEmptyPart(null);
		if (headers != null && !headers.isEmpty()) {
			part.setHeader(headers.toArray(new Header[headers.size()]));
		}
		return part;
	}
	
	@WebResult(name = "stream")
	public InputStream format(@NotNull @WebParam(name = "part") Part part, @WebParam(name = "allowBinary") Boolean allowBinary, @WebParam(name = "chunkSize") Integer chunkSize) throws IOException, FormatException {
		PullableMimeFormatter mimeFormatter = new PullableMimeFormatter();
		mimeFormatter.setAllowBinary(allowBinary == null || allowBinary);
		if (chunkSize != null) {
			mimeFormatter.setChunkSize(chunkSize);
		}
		mimeFormatter.format(part);
		return IOUtils.toInputStream(mimeFormatter, true);
	}
	
	@WebResult(name = "part")
	public Part newMultiPart(@NotNull @WebParam(name = "parts") List<Part> parts, @WebParam(name = "headers") List<Header> headers) {
		PlainMimeMultiPart part = new PlainMimeMultiPart(null);
		if (headers != null && !headers.isEmpty()) {
			part.setHeader(headers.toArray(new Header[headers.size()]));
		}
		if (parts != null && !parts.isEmpty()) {
			for (Part child : parts) {
				part.addChild(child);
			}
		}
		return part;
	}
	
	@WebResult(name = "part")
	public Part encrypt(@WebParam(name = "part") @NotNull Part part, @WebParam(name = "keystoreId") @NotNull String keystoreId, @WebParam(name = "certificateAliases") @NotNull List<String> certificateAliases) throws KeyStoreException, IOException {
		if (part == null) {
			throw new NullPointerException("You must provide a part to sign");
		}
		if (keystoreId == null) {
			throw new NullPointerException("You must provide a keystore that contains the required certificates");
		}
		if (certificateAliases == null || certificateAliases.isEmpty()) {
			throw new NullPointerException("You must provide aliases for the certificates you want to include");
		}
		DefinedKeyStore keystore = executionContext.getServiceContext().getResolver(DefinedKeyStore.class).resolve(keystoreId);
		if (keystore == null) {
			throw new IllegalArgumentException("Invalid keystore id: " + keystoreId);
		}
		List<X509Certificate> recipients = new ArrayList<X509Certificate>();
		for (String alias : certificateAliases) {
			X509Certificate certificate = keystore.getKeyStore().getCertificate(alias);
			if (certificate == null) {
				throw new IllegalArgumentException("Could not find certificate for alias: " + alias);
			}
			recipients.add(certificate);
		}
		return MimeUtils.encrypt(part, recipients.toArray(new X509Certificate[recipients.size()]));
	}
	
	@WebResult(name = "part")
	public Part sign(@NotNull @WebParam(name = "part") Part part, @WebParam(name = "signatureType") SignatureType signatureType, @WebParam(name = "keystoreId") @NotNull String keystoreId, @WebParam(name = "certificateAliases") @NotNull List<String> certificateAliases) throws KeyStoreException, IOException {
		if (part == null) {
			throw new NullPointerException("You must provide a part to sign");
		}
		if (keystoreId == null) {
			throw new NullPointerException("You must provide a keystore that contains the required certificates");
		}
		if (certificateAliases == null) {
			throw new NullPointerException("You must provide aliases for the certificates you want to include");
		}
		if (signatureType == null) {
			signatureType = SignatureType.SHA512WITHRSA;
		}
		DefinedKeyStore keystore = executionContext.getServiceContext().getResolver(DefinedKeyStore.class).resolve(keystoreId);
		if (keystore == null) {
			throw new IllegalArgumentException("Invalid keystore id: " + keystoreId);
		}
		return MimeUtils.sign(part, signatureType, keystore.getKeyStore(), certificateAliases == null ? new String[0] : certificateAliases.toArray(new String[certificateAliases.size()]));
	}
}
