package nabu.utils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.math.BigInteger;
import java.io.IOException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.authenticator.Curve;
import com.webauthn4j.data.attestation.authenticator.EC2COSEKey;
import com.webauthn4j.data.attestation.authenticator.RSACOSEKey;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.validator.exception.ValidationException;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nabu.utils.types.WebAuthnAssertionEnvelope;
import nabu.utils.types.WebAuthnRequest;
import nabu.utils.types.WebAuthnResult;

/** Stateless verifier. The caller must load all expectations from trusted storage and consume the ceremony atomically. */
@WebService
public class WebAuthn {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebAuthn.class);
	private static final ObjectMapper JSON = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
	private static final CBORFactory CBOR = new CBORFactory();
	private static final WebAuthnManager MANAGER = WebAuthnManager.createNonStrictWebAuthnManager();
	private static final ObjectConverter CONVERTER = new ObjectConverter();

	@WebResult(name = "envelope")
	public WebAuthnAssertionEnvelope parseAssertionEnvelope(@WebParam(name = "secret") String secret) {
		try {
			if (secret == null || secret.length() > 32768) {
				throw new IllegalArgumentException("Invalid assertion envelope");
			}
			JsonNode json = parse(secret.getBytes(StandardCharsets.UTF_8));
			String ceremonyId = text(json, "ceremonyId");
			java.util.UUID.fromString(ceremonyId);
			String credentialId = text(json, "credentialId");
			decode(credentialId, 1, 1024);
			String responseJson = text(json, "responseJson");
			if (responseJson.length() > 32768 || !credentialId.equals(text(parse(responseJson.getBytes(StandardCharsets.UTF_8)), "rawId"))) {
				throw new IllegalArgumentException("Invalid assertion envelope");
			}
			WebAuthnAssertionEnvelope result = new WebAuthnAssertionEnvelope();
			result.setCeremonyId(ceremonyId);
			result.setCredentialId(credentialId);
			result.setResponseJson(responseJson);
			return result;
		}
		catch (IOException | IllegalArgumentException e) {
			LOGGER.debug("WebAuthn assertion envelope rejected", e);
			throw new IllegalArgumentException("Invalid assertion envelope", e);
		}
	}

	private static final class CeremonyMismatchException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private CeremonyMismatchException(String message) {
			super(message);
		}
	}

	private static WebAuthnResult invalid(String reason) {
		WebAuthnResult result = new WebAuthnResult();
		result.setValid(false);
		result.setReason(reason);
		return result;
	}

	private static void policy(WebAuthnRequest request) {
		if (request == null || request.getRpId() == null || !request.getRpId().matches("[a-z0-9.-]{1,253}") ||
			request.getOrigin() == null || request.getOrigin().length() > 512) {
			throw new IllegalArgumentException("Invalid server policy");
		}
		try {
			URI origin = new URI(request.getOrigin());
			if (!"https".equals(origin.getScheme()) || origin.getHost() == null || origin.getRawUserInfo() != null ||
				origin.getRawQuery() != null || origin.getRawFragment() != null || !"".equals(origin.getRawPath() == null ? "" : origin.getRawPath()) ||
				!request.getOrigin().equals(origin.toString()) || !request.getRpId().equals(origin.getHost()) &&
					!origin.getHost().endsWith("." + request.getRpId())) {
				throw new IllegalArgumentException("Invalid server origin");
			}
		}
		catch (java.net.URISyntaxException e) {
			LOGGER.warn("Invalid server origin", e);
			throw new IllegalArgumentException("Invalid server origin", e);
		}
		decode(request.getChallenge(), 16, 128);
	}

	private static COSEKey checkKey(COSEKey key) {
		if (key == null) throw new IllegalArgumentException("Missing COSE key");
		if (key.getKeyOps() != null && !key.getKeyOps().isEmpty() || key.getBaseIV() != null || key.getKeyId() != null) {
			throw new IllegalArgumentException("Unsupported key metadata");
		}
		if (key instanceof EC2COSEKey) {
			EC2COSEKey ec = (EC2COSEKey) key;
			if (!COSEAlgorithmIdentifier.ES256.equals(ec.getAlgorithm()) || ec.getCurve() != Curve.SECP256R1 ||
				ec.getX() == null || ec.getY() == null || ec.getX().length != 32 || ec.getY().length != 32 || ec.getD() != null) {
				throw new IllegalArgumentException("Unsupported EC key");
			}
			java.security.spec.ECParameterSpec params = (java.security.spec.ECParameterSpec) Curve.SECP256R1.getParameterSpec();
			java.security.spec.EllipticCurve curve = params.getCurve();
			BigInteger x = new BigInteger(1, ec.getX());
			BigInteger y = new BigInteger(1, ec.getY());
			BigInteger p = ((java.security.spec.ECFieldFp) curve.getField()).getP();
			if (x.signum() == 0 && y.signum() == 0 || x.compareTo(p) >= 0 || y.compareTo(p) >= 0 ||
				!y.multiply(y).mod(p).equals(x.multiply(x).multiply(x).add(curve.getA().multiply(x)).add(curve.getB()).mod(p))) {
				throw new IllegalArgumentException("Invalid EC point");
			}
		}
		else if (key instanceof RSACOSEKey) {
			RSACOSEKey rsa = (RSACOSEKey) key;
			if (!COSEAlgorithmIdentifier.RS256.equals(rsa.getAlgorithm()) || rsa.hasPrivateKey() ||
				rsa.getN() == null || rsa.getE() == null || rsa.getN().length > 1024 || rsa.getE().length > 8 ||
				rsa.getN().length == 0 || rsa.getE().length == 0 || rsa.getD() != null || rsa.getP() != null ||
				rsa.getQ() != null || rsa.getDP() != null || rsa.getDQ() != null || rsa.getQInv() != null) {
				throw new IllegalArgumentException("Unsupported RSA key");
			}
			checkRsa(new BigInteger(1, rsa.getN()), new BigInteger(1, rsa.getE()));
		}
		else {
			throw new IllegalArgumentException("Unsupported key type");
		}
		key.validate();
		if (key.getPublicKey() == null) throw new IllegalArgumentException("Invalid public key");
		return key;
	}

	private static void checkRsa(BigInteger modulus, BigInteger exponent) {
		if (modulus.bitLength() < 2048 || !modulus.testBit(0) || exponent.compareTo(BigInteger.valueOf(3)) < 0 ||
			!exponent.testBit(0) || exponent.compareTo(BigInteger.valueOf(0xffffffffL)) > 0) {
			throw new IllegalArgumentException("Weak RSA key");
		}
	}

	private static COSEKey storedKey(WebAuthnRequest request) throws InvalidKeySpecException, java.security.NoSuchAlgorithmException, IOException {
		if (request.getPublicKeyFormat() == null || request.getPublicKey() == null) {
			throw new IllegalArgumentException("Missing stored key or format");
		}
		byte[] encoded = decode(request.getPublicKey(), 1, 2048);
		if ("COSE".equals(request.getPublicKeyFormat())) {
			checkCbor(encoded);
			return checkKey(CONVERTER.getCborConverter().readValue(encoded, COSEKey.class));
		}
		if ("SPKI".equals(request.getPublicKeyFormat())) {
			try {
				ECPublicKey ec = (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(encoded));
				if (!java.util.Arrays.equals(ec.getEncoded(), encoded)) throw new IllegalArgumentException("Noncanonical SPKI");
				return checkKey(EC2COSEKey.create(ec, COSEAlgorithmIdentifier.ES256));
			}
			catch (InvalidKeySpecException | ClassCastException e) {
				LOGGER.debug("Stored key is not EC SPKI", e);
			}
			RSAPublicKey rsa = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
			if (!java.util.Arrays.equals(rsa.getEncoded(), encoded)) throw new IllegalArgumentException("Noncanonical SPKI");
			checkRsa(rsa.getModulus(), rsa.getPublicExponent());
			return checkKey(RSACOSEKey.create(rsa, COSEAlgorithmIdentifier.RS256));
		}
		throw new IllegalArgumentException("Unsupported key format");
	}

	@WebResult(name = "result")
	public WebAuthnResult verifyRegistration(@WebParam(name = "responseJson") String responseJson,
		@WebParam(name = "challenge") String challenge, @WebParam(name = "rpId") String rpId,
		@WebParam(name = "origin") String origin, @WebParam(name = "requireUserVerification") boolean requireUserVerification) {
		WebAuthnRequest request = new WebAuthnRequest();
		request.setResponseJson(responseJson);
		request.setChallenge(challenge);
		request.setRpId(rpId);
		request.setOrigin(origin);
		request.setRequireUserVerification(requireUserVerification);
		return verifyRegistrationRequest(request);
	}

	private WebAuthnResult verifyRegistrationRequest(WebAuthnRequest request) {
		policy(request);
		if (request.getResponseJson() == null) throw new IllegalArgumentException("Missing responseJson");
		try {
			JsonNode envelope = envelope(request, "webauthn.create");
			byte[] id = decode(text(envelope, "rawId"), 1, 1024);
			byte[] client = decode(text(envelope.path("response"), "clientDataJSON"), 1, 8192);
			byte[] attestation = decode(text(envelope.path("response"), "attestationObject"), 1, 16384);
			checkClient(client, request, "webauthn.create");
			checkCbor(attestation);
			RegistrationData data = MANAGER.validate(new RegistrationRequest(attestation, client),
				new RegistrationParameters(server(request), java.util.Arrays.asList(
					new com.webauthn4j.data.PublicKeyCredentialParameters(com.webauthn4j.data.PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
					new com.webauthn4j.data.PublicKeyCredentialParameters(com.webauthn4j.data.PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)), request.isRequireUserVerification(), true));
			if (!"none".equals(data.getAttestationObject().getFormat())) {
				throw new CeremonyMismatchException("Only none attestation is accepted");
			}
			AuthenticatorData<?> auth = data.getAttestationObject().getAuthenticatorData();
			checkFlags(auth, true);
			AttestedCredentialData credential = auth.getAttestedCredentialData();
			if (credential == null || !MessageDigest.isEqual(id, credential.getCredentialId())) {
				throw new CeremonyMismatchException("Credential ID mismatch");
			}
			COSEKey key = checkKey(credential.getCOSEKey());
			WebAuthnResult result = result(id, auth);
			result.setPublicKey(encode(key.getPublicKey().getEncoded()));
			result.setPublicKeyFormat("SPKI");
			result.setAlgorithm(key.getAlgorithm().equals(COSEAlgorithmIdentifier.ES256) ? "ES256" : "RS256");
			return result;
		}
		catch (ValidationException | CeremonyMismatchException e) {
			LOGGER.debug("Registration verification failed", e);
			return invalid("VERIFICATION_FAILED");
		}
		catch (DataConversionException | IOException | IllegalArgumentException e) {
			LOGGER.debug("Malformed registration response", e);
			return invalid("INVALID_RESPONSE");
		}
	}

	@WebResult(name = "result")
	public WebAuthnResult verifyAssertion(@WebParam(name = "responseJson") String responseJson,
		@WebParam(name = "challenge") String challenge, @WebParam(name = "rpId") String rpId,
		@WebParam(name = "origin") String origin, @WebParam(name = "credentialId") String credentialId,
		@WebParam(name = "publicKey") String publicKey, @WebParam(name = "publicKeyFormat") String publicKeyFormat,
		@WebParam(name = "userHandle") String userHandle, @WebParam(name = "requireUserVerification") boolean requireUserVerification,
		@WebParam(name = "signCount") long signCount) {
		WebAuthnRequest request = new WebAuthnRequest();
		request.setResponseJson(responseJson);
		request.setChallenge(challenge);
		request.setRpId(rpId);
		request.setOrigin(origin);
		request.setCredentialId(credentialId);
		request.setPublicKey(publicKey);
		request.setPublicKeyFormat(publicKeyFormat);
		request.setUserHandle(userHandle);
		request.setRequireUserVerification(requireUserVerification);
		request.setSignCount(signCount);
		return verifyAssertionRequest(request);
	}

	private WebAuthnResult verifyAssertionRequest(WebAuthnRequest request) {
		policy(request);
		if (request.getResponseJson() == null || request.getCredentialId() == null || request.getPublicKey() == null ||
			request.getPublicKeyFormat() == null || request.getSignCount() < 0) throw new IllegalArgumentException("Missing assertion server state");
		decode(request.getCredentialId(), 1, 1024);
		if (request.getUserHandle() != null) decode(request.getUserHandle(), 1, 64);
		COSEKey key;
		try {
			key = storedKey(request);
		}
		catch (InvalidKeySpecException | DataConversionException | IOException | IllegalArgumentException e) {
			LOGGER.warn("Corrupt or unsupported stored WebAuthn public key", e);
			return invalid("INVALID_STORED_KEY");
		}
		catch (java.security.NoSuchAlgorithmException e) {
			LOGGER.error("Missing public key provider", e);
			throw new IllegalStateException("Missing public key provider", e);
		}
		try {
			JsonNode envelope = envelope(request, "webauthn.get");
			byte[] id = decode(text(envelope, "rawId"), 1, 1024);
			if (!MessageDigest.isEqual(id, decode(request.getCredentialId(), 1, 1024))) {
				throw new CeremonyMismatchException("Credential ID mismatch");
			}
			JsonNode response = envelope.path("response");
			byte[] client = decode(text(response, "clientDataJSON"), 1, 8192);
			byte[] authBytes = decode(text(response, "authenticatorData"), 37, 8192);
			byte[] signature = decode(text(response, "signature"), 1, 1024);
			byte[] handle = response.has("userHandle") && !response.get("userHandle").isNull() ? decode(text(response, "userHandle"), 1, 64) : null;
			if (handle != null && (request.getUserHandle() == null || !MessageDigest.isEqual(handle, decode(request.getUserHandle(), 1, 64)))) {
				throw new CeremonyMismatchException("User handle mismatch");
			}
			checkClient(client, request, "webauthn.get");

			AttestedCredentialData credential = new AttestedCredentialData(com.webauthn4j.data.attestation.authenticator.AAGUID.ZERO, id, key);
			AuthenticationData data = MANAGER.validate(new AuthenticationRequest(id, handle, authBytes, client, signature),
				new AuthenticationParameters(server(request), new AuthenticatorImpl(credential, null, request.getSignCount()),
					Collections.singletonList(id), request.isRequireUserVerification(), true));
			AuthenticatorData<?> auth = data.getAuthenticatorData();
			checkFlags(auth, false);
			if (request.getSignCount() < 0 || request.getSignCount() > 0 && auth.getSignCount() == 0 ||
				auth.getSignCount() != 0 && auth.getSignCount() <= request.getSignCount()) {
				throw new CeremonyMismatchException("Signature counter regression");
			}
			WebAuthnResult result = result(id, auth);
			result.setAlgorithm(key.getAlgorithm().equals(COSEAlgorithmIdentifier.ES256) ? "ES256" : "RS256");
			result.setPublicKeyFormat(request.getPublicKeyFormat());
			return result;
		}
		catch (ValidationException | CeremonyMismatchException e) {
			LOGGER.debug("Assertion verification failed", e);
			return invalid("VERIFICATION_FAILED");
		}
		catch (DataConversionException | IOException | IllegalArgumentException e) {
			LOGGER.debug("Malformed assertion response", e);
			return invalid("INVALID_RESPONSE");
		}
	}

	private static WebAuthnResult result(byte[] id, AuthenticatorData<?> auth) {
		WebAuthnResult result = new WebAuthnResult();
		result.setValid(true);
		result.setCredentialId(encode(id));
		result.setSignCount(auth.getSignCount());
		result.setBackupEligible(auth.isFlagBE());
		result.setBackedUp(auth.isFlagBS());
		return result;
	}

	private static void checkFlags(AuthenticatorData<?> auth, boolean registration) {
		if (!auth.isFlagUP() || (auth.getFlags() & 0x22) != 0 || auth.isFlagBS() && !auth.isFlagBE() ||
			registration != auth.isFlagAT()) {
			throw new CeremonyMismatchException("Invalid authenticator flags");
		}
	}

	private static ServerProperty server(WebAuthnRequest request) {
		return new ServerProperty(new Origin(request.getOrigin()), request.getRpId(),
			new DefaultChallenge(decode(request.getChallenge(), 16, 128)), null);
	}

	private static JsonNode envelope(WebAuthnRequest request, String type) throws IOException {
		if (request.getResponseJson().length() > 32768) throw new IllegalArgumentException("Response too large");
		JsonNode json = parse(request.getResponseJson().getBytes(StandardCharsets.UTF_8));
		if (!"public-key".equals(text(json, "type")) || !text(json, "id").equals(text(json, "rawId"))) {
			throw new IllegalArgumentException("Invalid credential envelope");
		}
		decode(text(json, "rawId"), 1, 1024);
		return json;
	}

	private static void checkClient(byte[] client, WebAuthnRequest request, String expectedType) throws IOException {
		JsonNode json = parse(client);
		String type = text(json, "type");
		byte[] challenge = decode(text(json, "challenge"), 16, 128);
		String origin = text(json, "origin");
		if (json.has("crossOrigin") && !json.get("crossOrigin").isBoolean() ||
			json.has("topOrigin") || json.has("tokenBinding")) {
			throw new IllegalArgumentException("Unsupported client data");
		}
		if (!expectedType.equals(type) || !MessageDigest.isEqual(challenge, decode(request.getChallenge(), 16, 128)) ||
			!request.getOrigin().equals(origin) || json.path("crossOrigin").asBoolean(false)) {
			throw new CeremonyMismatchException("Client ceremony mismatch");
		}
	}

	private static void checkCbor(byte[] bytes) throws IOException {
		try (JsonParser parser = CBOR.createParser(bytes)) {
			parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
			int depth = 0;
			boolean seen = false;
			com.fasterxml.jackson.core.JsonToken token;
			while ((token = parser.nextToken()) != null) {
				if (depth == 0 && seen) throw new IllegalArgumentException("Trailing CBOR");
				seen = true;
				if (token == com.fasterxml.jackson.core.JsonToken.START_ARRAY || token == com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
					if (++depth > 16) throw new IllegalArgumentException("CBOR too deep");
				}
				else if (token == com.fasterxml.jackson.core.JsonToken.END_ARRAY || token == com.fasterxml.jackson.core.JsonToken.END_OBJECT) depth--;
			}
			if (depth != 0 || !seen) throw new IllegalArgumentException("Invalid CBOR");
		}
	}

	private static JsonNode parse(byte[] bytes) throws IOException {
		java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		String value = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
		try (JsonParser parser = JSON.getFactory().createParser(value)) {
			JsonNode json = JSON.readTree(parser);
			if (json == null || !json.isObject() || parser.nextToken() != null) {
				throw new IllegalArgumentException("Invalid JSON object");
			}
			return json;
		}
	}

	private static String text(JsonNode json, String key) {
		JsonNode node = json.path(key);
		if (!node.isTextual()) {
			throw new IllegalArgumentException("Missing " + key);
		}
		return node.textValue();
	}

	private static byte[] decode(String value, int min, int max) {
		if (value == null || value.length() > (max * 4 / 3 + 4) || !value.matches("[A-Za-z0-9_-]+")) {
			throw new IllegalArgumentException("Invalid base64url");
		}
		byte[] bytes = Base64.getUrlDecoder().decode(value);
		if (bytes.length < min || bytes.length > max || !encode(bytes).equals(value)) {
			throw new IllegalArgumentException("Noncanonical base64url");
		}
		return bytes;
	}

	private static String encode(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
