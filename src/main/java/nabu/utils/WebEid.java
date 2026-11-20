package nabu.utils;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.nabu.utils.security.webeid.WebEidOcsp;
import be.nabu.utils.security.webeid.WebEidValidator;
import nabu.utils.types.WebEidRequest;
import nabu.utils.types.WebEidResult;

/** Stateless verifier: the caller owns challenge issuance, session binding, expiry and atomic consumption. */
@WebService
public class WebEid {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebEid.class);
	private static final ObjectMapper JSON = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);

	@WebResult(name = "result")
	public WebEidResult validate(@WebParam(name = "request") WebEidRequest request) throws IOException {
		if (request == null || request.getOrigin() == null || request.getChallenge() == null || request.getChallenge().length() < 44
				|| request.getChallenge().length() > 128 || !(request.getIssuerCertificate() instanceof X509Certificate)
				|| request.getAllowedPolicies() == null
				|| request.getAllowedPolicies().isEmpty()) {
			throw new IllegalArgumentException("Missing Web eID server policy");
		}
		Set<java.lang.String> policies = new HashSet<>(request.getAllowedPolicies());
		for (java.lang.String policy : policies) {
			if (policy == null || !policy.matches("[0-9]+(\\.[0-9]+)+") || policy.length() > 128) {
				throw new IllegalArgumentException("Invalid Web eID policy OID");
			}
		}
		URI endpoint = null;
		if (!request.isOfflinePolicy()) {
			if (request.getOcspEndpoint() == null || request.getOcspEndpoint().length() > 2048) {
				throw new IllegalArgumentException("OCSP responder required");
			}
			endpoint = URI.create(request.getOcspEndpoint());
			if (endpoint.getHost() == null || endpoint.getUserInfo() != null || endpoint.getFragment() != null
					|| endpoint.getRawQuery() != null || endpoint.getPort() == 0 || endpoint.getPort() > 65535
					|| !("https".equals(endpoint.getScheme()) || "http".equals(endpoint.getScheme()))) {
				throw new IllegalArgumentException("Invalid OCSP responder");
			}
		}
		return verify(request, (X509Certificate) request.getIssuerCertificate(), policies, endpoint,
				request.isOfflinePolicy() ? null : new WebEidOcsp.HttpTransport(), Clock.systemUTC());
	}

	WebEidResult verify(WebEidRequest request, X509Certificate issuer, Set<java.lang.String> policies, URI endpoint,
			WebEidOcsp.Transport transport, Clock clock) throws IOException {
		WebEidValidator validator = new WebEidValidator(request.getOrigin(), issuer, policies, endpoint, transport, clock, !request.isOfflinePolicy());
		WebEidResult result = new WebEidResult();
		WebEidValidator.Token token;
		try {
			if (request.getTokenJson() == null || request.getTokenJson().length() > 32768) {
				throw new IllegalArgumentException("Invalid token size");
			}
			try (JsonParser parser = JSON.getFactory().createParser(request.getTokenJson())) {
				parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
				JsonNode json = JSON.readTree(parser);
				if (json == null || !json.isObject() || json.size() != 4 || parser.nextToken() != null) {
					throw new IllegalArgumentException("Invalid token structure");
				}
				java.lang.String[] fields = { "format", "algorithm", "unverifiedCertificate", "signature" };
				for (java.lang.String field : fields) {
					if (!json.path(field).isTextual()) {
						throw new IllegalArgumentException("Invalid token field");
					}
				}
				token = new WebEidValidator.Token(json.get("format").textValue(), json.get("algorithm").textValue(),
						json.get("unverifiedCertificate").textValue(), json.get("signature").textValue());
			}
		}
		catch (IllegalArgumentException | IOException e) {
			LOGGER.debug("Malformed Web eID token", e);
			result.setReason("INVALID_RESPONSE");
			return result;
		}
		try {
			WebEidValidator.Result verified = validator.validate(token, request.getChallenge());
			result.setSubject(verified.certificate.getSubjectX500Principal().getName());
			result.setIssuer(verified.certificate.getIssuerX500Principal().getName());
			result.setSerialNumber(verified.certificate.getSerialNumber().toString(16));
			result.setNotBefore(Instant.ofEpochMilli(verified.certificate.getNotBefore().getTime()).toString());
			result.setNotAfter(Instant.ofEpochMilli(verified.certificate.getNotAfter().getTime()).toString());
			result.setCertificateDer(Base64.getEncoder().encodeToString(verified.certificate.getEncoded()));
			result.setOcspStatus(verified.ocsp.name());
			result.setOcspResponse(verified.ocspResponse);
			result.setValid(true);
		}
		catch (GeneralSecurityException e) {
			LOGGER.debug("Web eID verification failed", e);
			result.setReason("VERIFICATION_FAILED");
		}
		return result;
	}
}
