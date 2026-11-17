package nabu.utils.webauthn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import nabu.utils.WebAuthn;
import nabu.utils.types.WebAuthnAssertionEnvelope;
import nabu.utils.types.WebAuthnRequest;
import nabu.utils.types.WebAuthnResult;

public class WebAuthnTest {
	private final WebAuthn service = new WebAuthn();
	private final ObjectMapper cbor = new ObjectMapper(new CBORFactory());
	private final byte[] challenge = new byte[32];
	private final byte[] id = new byte[32];
	private final byte[] handle = new byte[32];
	private KeyPair pair;
	private WebAuthnRequest request;

	@Before
	public void setup() throws Exception {
		java.util.Arrays.fill(challenge, (byte) 17);
		java.util.Arrays.fill(id, (byte) 18);
		java.util.Arrays.fill(handle, (byte) 19);
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec("secp256r1"));
		pair = generator.generateKeyPair();
		request = new WebAuthnRequest();
		request.setChallenge(b64(challenge));
		request.setRpId("example.test");
		request.setOrigin("https://example.test");
		request.setRequireUserVerification(true);
		request.setCredentialId(b64(id));
		request.setUserHandle(b64(handle));
		request.setPublicKey(b64(pair.getPublic().getEncoded()));
		request.setPublicKeyFormat("SPKI");
	}

	@Test
	public void parsesAssertionEnvelope() {
		String response = "{\"rawId\":\"" + b64(id) + "\"}";
		String secret = "{\"ceremonyId\":\"123e4567-e89b-12d3-a456-426614174000\",\"credentialId\":\"" + b64(id) + "\",\"responseJson\":\"" + response.replace("\"", "\\\"") + "\"}";
		WebAuthnAssertionEnvelope result = service.parseAssertionEnvelope(secret);
		assertEquals("123e4567-e89b-12d3-a456-426614174000", result.getCeremonyId());
		assertEquals(response, result.getResponseJson());
		try {
			service.parseAssertionEnvelope(secret.replace("123e4567-e89b-12d3-a456-426614174000", "invalid"));
			org.junit.Assert.fail("Malformed ceremony ID accepted");
		}
		catch (IllegalArgumentException expected) {
			assertNotNull(expected.getCause());
		}
		try {
			service.parseAssertionEnvelope(secret.replaceFirst(java.util.regex.Pattern.quote(b64(id)), b64(handle)));
			org.junit.Assert.fail("Mismatched credential ID accepted");
		}
		catch (IllegalArgumentException expected) {
			assertNotNull(expected.getCause());
		}
	}

	private WebAuthnResult verifyRegistration() {
		return service.verifyRegistration(request.getResponseJson(), request.getChallenge(), request.getRpId(),
			request.getOrigin(), request.isRequireUserVerification());
	}

	private WebAuthnResult verifyAssertion() {
		return service.verifyAssertion(request.getResponseJson(), request.getChallenge(), request.getRpId(),
			request.getOrigin(), request.getCredentialId(), request.getPublicKey(), request.getPublicKeyFormat(),
			request.getUserHandle(), request.isRequireUserVerification(), request.getSignCount());
	}

	private String b64(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
	private byte[] sha(byte[] bytes) throws Exception { return MessageDigest.getInstance("SHA-256").digest(bytes); }
	private String client(String type, String origin) {
		return "{\"type\":\"" + type + "\",\"challenge\":\"" + b64(challenge) + "\",\"origin\":\"" + origin + "\"}";
	}
	private String envelope(String client, String other) {
		return "{\"type\":\"public-key\",\"id\":\"" + b64(id) + "\",\"rawId\":\"" + b64(id) + "\",\"response\":{" +
			"\"clientDataJSON\":\"" + b64(client.getBytes(StandardCharsets.UTF_8)) + "\"," + other + "}}";
	}
	private byte[] auth(int flags, long count) throws Exception {
		return ByteBuffer.allocate(37).put(sha("example.test".getBytes(StandardCharsets.UTF_8))).put((byte) flags).putInt((int) count).array();
	}
	private void assertion(String type, String origin, int flags, boolean validSignature) throws Exception {
		String client = client(type, origin);
		byte[] authenticator = auth(flags, 1);
		Signature signature = Signature.getInstance(pair.getPublic() instanceof java.security.interfaces.RSAPublicKey ? "SHA256withRSA" : "SHA256withECDSA");
		signature.initSign(pair.getPrivate());
		signature.update(authenticator);
		signature.update(sha(client.getBytes(StandardCharsets.UTF_8)));
		byte[] signed = signature.sign();
		if (!validSignature) signed[8] ^= 1;
		request.setResponseJson(envelope(client, "\"authenticatorData\":\"" + b64(authenticator) + "\",\"signature\":\"" + b64(signed) + "\",\"userHandle\":\"" + b64(handle) + "\""));
	}

	private void registration(String type, String origin, int flags) throws Exception {
		Map<Integer, Object> key = keyMap();
		byte[] encodedKey = cbor.writeValueAsBytes(key);
		byte[] auth = ByteBuffer.allocate(37 + 16 + 2 + id.length + encodedKey.length)
			.put(sha("example.test".getBytes(StandardCharsets.UTF_8))).put((byte) flags).putInt(0)
			.put(new byte[16]).putShort((short) id.length).put(id).put(encodedKey).array();
		Map<String, Object> att = new LinkedHashMap<>();
		att.put("fmt", "none");
		att.put("attStmt", new LinkedHashMap<String, Object>());
		att.put("authData", auth);
		request.setResponseJson(envelope(client(type, origin), "\"attestationObject\":\"" + b64(cbor.writeValueAsBytes(att)) + "\""));
	}
	private Map<Integer, Object> keyMap() {
		Map<Integer, Object> key = new LinkedHashMap<>();
		if (pair.getPublic() instanceof java.security.interfaces.RSAPublicKey) {
			java.security.interfaces.RSAPublicKey pub = (java.security.interfaces.RSAPublicKey) pair.getPublic();
			key.put(1, 3);
			key.put(3, -257);
			key.put(-1, pub.getModulus().toByteArray());
			key.put(-2, pub.getPublicExponent().toByteArray());
		}
		else {
			java.security.interfaces.ECPublicKey pub = (java.security.interfaces.ECPublicKey) pair.getPublic();
			key.put(1, 2);
			key.put(3, -7);
			key.put(-1, 1);
			key.put(-2, fixed(pub.getW().getAffineX().toByteArray()));
			key.put(-3, fixed(pub.getW().getAffineY().toByteArray()));
		}
		return key;
	}
	private byte[] fixed(byte[] value) {
		byte[] bytes = new byte[32];
		System.arraycopy(value, Math.max(0, value.length - 32), bytes, Math.max(0, 32 - value.length), Math.min(32, value.length));
		return bytes;
	}
	private void rejected(boolean registration) {
		rejected(registration, null);
	}
	private void rejected(boolean registration, String reason) {
		WebAuthnResult result = registration ? verifyRegistration() : verifyAssertion();
		assertFalse(result.isValid());
		if (reason == null) assertNotNull(result.getReason());
		else assertEquals(reason, result.getReason());
		assertNull(result.getCredentialId());
		assertNull(result.getPublicKey());
		assertNull(result.getAlgorithm());
	}
	private void badPolicy(boolean registration) {
		try {
			if (registration) verifyRegistration();
			else verifyAssertion();
			org.junit.Assert.fail("Expected policy error");
		}
		catch (IllegalArgumentException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	@Test
	public void registrationAndFailures() throws Exception {
		registration("webauthn.create", "https://example.test", 0x45);
		WebAuthnResult result = verifyRegistration();
		assertTrue(result.isValid());
		assertEquals("SPKI", result.getPublicKeyFormat());
		assertEquals("ES256", result.getAlgorithm());
		assertEquals(b64(id), result.getCredentialId());
		assertEquals(b64(pair.getPublic().getEncoded()), result.getPublicKey());
		request.setChallenge(b64(new byte[32])); rejected(true, "VERIFICATION_FAILED");
		request.setChallenge(b64(challenge));
		request.setRpId("wrong.test"); badPolicy(true);
		request.setRpId("example.test");
		request.setOrigin("https://wrong.test"); badPolicy(true);
		request.setOrigin("https://example.test");
		registration("webauthn.get", "https://example.test", 0x45); rejected(true, "VERIFICATION_FAILED");
		registration("webauthn.create", "https://example.test", 0x41); rejected(true);
		request.setResponseJson("{\"type\":\"public-key\",\"type\":\"public-key\"}"); rejected(true, "INVALID_RESPONSE");
		request.setResponseJson(envelope(client("webauthn.create", "https://example.test"), "\"attestationObject\":\"" + b64(new byte[] { (byte) 0xff }) + "\"")); rejected(true);
	}

	@Test
	public void rsaRegistrationAndAssertionFormats() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		pair = generator.generateKeyPair();
		request.setPublicKey(b64(pair.getPublic().getEncoded()));
		registration("webauthn.create", "https://example.test", 0x45);
		WebAuthnResult enrolled = verifyRegistration();
		assertTrue(enrolled.isValid());
		assertEquals("RS256", enrolled.getAlgorithm());
		assertEquals("SPKI", enrolled.getPublicKeyFormat());
		assertEquals(request.getPublicKey(), enrolled.getPublicKey());
		assertion("webauthn.get", "https://example.test", 0x05, true);
		assertTrue(verifyAssertion().isValid());
		request.setPublicKeyFormat("COSE");
		request.setPublicKey(b64(cbor.writeValueAsBytes(keyMap())));
		assertTrue(verifyAssertion().isValid());
		assertion("webauthn.get", "https://example.test", 0x05, false);
		rejected(false);
		request.setPublicKeyFormat("SPKI");
		rejected(false);
	}

	@Test
	public void ecCoseAndInvalidStoredKeys() throws Exception {
		assertion("webauthn.get", "https://example.test", 0x05, true);
		request.setPublicKeyFormat("COSE");
		request.setPublicKey(b64(cbor.writeValueAsBytes(keyMap())));
		assertTrue(verifyAssertion().isValid());
		Map<Integer, Object> wrong = keyMap();
		wrong.put(3, -257);
		request.setPublicKey(b64(cbor.writeValueAsBytes(wrong)));
		assertEquals("INVALID_STORED_KEY", verifyAssertion().getReason());
		request.setPublicKey(b64(new byte[] { (byte) 0xff }));
		assertEquals("INVALID_STORED_KEY", verifyAssertion().getReason());
		request.setPublicKeyFormat("SPKI");
		request.setPublicKey(b64(pair.getPublic().getEncoded()));
		assertTrue(verifyAssertion().isValid());
		request.setPublicKeyFormat("invalid");
		assertEquals("INVALID_STORED_KEY", verifyAssertion().getReason());
	}

	@Test
	public void clientOriginAndCrossOriginReasons() throws Exception {
		registration("webauthn.create", "https://wrong.test", 0x45);
		rejected(true, "VERIFICATION_FAILED");
		assertion("webauthn.get", "https://wrong.test", 0x05, true);
		rejected(false, "VERIFICATION_FAILED");
		String crossOrigin = client("webauthn.get", "https://example.test").replace("}", ",\"crossOrigin\":true}");
		assertion("webauthn.get", "https://example.test", 0x05, true);
		request.setResponseJson(request.getResponseJson().replace(b64(client("webauthn.get", "https://example.test").getBytes(StandardCharsets.UTF_8)),
			b64(crossOrigin.getBytes(StandardCharsets.UTF_8))));
		rejected(false, "VERIFICATION_FAILED");
		String malformed = client("webauthn.get", "https://example.test").replace(b64(challenge), "@@");
		request.setResponseJson(envelope(malformed, "\"authenticatorData\":\"" + b64(auth(0x05, 1)) + "\",\"signature\":\"AQ\""));
		rejected(false, "INVALID_RESPONSE");
	}

	@Test
	public void unsupportedSpkiCurve() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec("secp384r1"));
		request.setPublicKey(b64(generator.generateKeyPair().getPublic().getEncoded()));
		assertion("webauthn.get", "https://example.test", 0x05, true);
		rejected(false, "INVALID_STORED_KEY");
	}

	@Test
	public void weakRsaAndMissingServerState() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(1024);
		pair = generator.generateKeyPair();
		request.setPublicKey(b64(pair.getPublic().getEncoded()));
		assertion("webauthn.get", "https://example.test", 0x05, true);
		assertEquals("INVALID_STORED_KEY", verifyAssertion().getReason());
		request.setPublicKeyFormat("COSE");
		request.setPublicKey(b64(cbor.writeValueAsBytes(keyMap())));
		assertEquals("INVALID_STORED_KEY", verifyAssertion().getReason());
		registration("webauthn.create", "https://example.test", 0x45);
		rejected(true);
		request.setPublicKeyFormat(null);
		badPolicy(false);
		request.setPublicKeyFormat("SPKI");
		request.setChallenge("broken");
		badPolicy(false);
	}

	@Test
	public void assertionAndFailures() throws Exception {
		assertion("webauthn.get", "https://example.test", 0x05, true);
		assertEquals(1, verifyAssertion().getSignCount());
		request.setChallenge(b64(new byte[32])); rejected(false, "VERIFICATION_FAILED");
		request.setChallenge(b64(challenge));
		request.setRpId("wrong.test"); badPolicy(false);
		request.setRpId("example.test");
		request.setOrigin("https://wrong.test"); badPolicy(false);
		request.setOrigin("https://example.test");
		request.setSignCount(1); rejected(false, "VERIFICATION_FAILED");
		request.setSignCount(0);
		assertion("webauthn.create", "https://example.test", 0x05, true); rejected(false, "VERIFICATION_FAILED");
		assertion("webauthn.get", "https://example.test", 0x01, true); rejected(false);
		assertion("webauthn.get", "https://example.test", 0x05, false); rejected(false);
		request.setResponseJson(envelope(client("webauthn.get", "https://example.test"), "\"authenticatorData\":\"" + b64(auth(0x05, 1)) + "\",\"signature\":\"" + b64(new byte[] { 0x30, 0x01, 0x00 }) + "\"")); rejected(false);
		request.setResponseJson("{\"type\":\"public-key\",\"id\":\"@@\"}"); rejected(false, "INVALID_RESPONSE");
		String crossOrigin = client("webauthn.get", "https://example.test").replace("}", ",\"crossOrigin\":true}");
		request.setResponseJson(envelope(crossOrigin, "\"authenticatorData\":\"" + b64(auth(0x05, 1)) + "\",\"signature\":\"AQ\"")); rejected(false, "VERIFICATION_FAILED");
	}
}
