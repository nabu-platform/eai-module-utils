package nabu.utils;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Before;
import org.junit.Test;

import nabu.utils.types.WebEidRequest;
import nabu.utils.types.WebEidResult;

public class WebEidTest {
	private final BouncyCastleProvider provider = new BouncyCastleProvider();
	private final Clock clock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC);
	private KeyPair issuerKey, userKey;
	private X509Certificate issuer, user;
	private WebEidRequest request;
	private final WebEid service = new WebEid();

	@Before
	public void setup() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		issuerKey = generator.generateKeyPair();
		userKey = generator.generateKeyPair();
		issuer = certificate(issuerKey, issuerKey, "CN=Issuer", "CN=Issuer", true);
		user = certificate(userKey, issuerKey, "CN=Issuer", "CN=User", false);
		request = new WebEidRequest();
		request.setOrigin("https://example.org");
		request.setChallenge(Base64.getEncoder().encodeToString(new byte[32]));
		request.setIssuerCertificate(issuer);
		request.setAllowedPolicies(Collections.singletonList("1.2.3.4"));
		request.setOfflinePolicy(true);
		request.setTokenJson(token(request.getChallenge()));
	}

	private X509Certificate certificate(KeyPair keys, KeyPair signer, java.lang.String issuerName, java.lang.String subject, boolean ca) throws Exception {
		X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuerName),
				BigInteger.valueOf(ca ? 1 : 2), Date.from(clock.instant().minusSeconds(86400)),
				Date.from(clock.instant().plusSeconds(315360000)), new X500Name(subject), keys.getPublic());
		builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(ca));
		builder.addExtension(Extension.keyUsage, true, new KeyUsage(ca ? KeyUsage.keyCertSign : KeyUsage.digitalSignature));
		if (!ca) {
			builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));
			builder.addExtension(Extension.certificatePolicies, false,
					new CertificatePolicies(new PolicyInformation(new ASN1ObjectIdentifier("1.2.3.4"))));
		}
		return new JcaX509CertificateConverter().setProvider(provider).getCertificate(builder.build(
				new JcaContentSignerBuilder("SHA256withRSA").setProvider(provider).build(signer.getPrivate())));
	}

	private java.lang.String token(java.lang.String challenge) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(userKey.getPrivate());
		signature.update(digest.digest(request.getOrigin().getBytes(StandardCharsets.UTF_8)));
		signature.update(digest.digest(challenge.getBytes(StandardCharsets.UTF_8)));
		return "{\"format\":\"web-eid:1.0\",\"algorithm\":\"RS256\",\"unverifiedCertificate\":\""
				+ Base64.getUrlEncoder().withoutPadding().encodeToString(user.getEncoded()) + "\",\"signature\":\""
				+ Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign()) + "\"}";
	}

	private WebEidResult verify() throws Exception {
		return service.verify(request, issuer, Collections.singleton("1.2.3.4"), URI.create("https://ocsp.example.org"),
				(endpoint, bytes) -> { throw new java.io.IOException("OCSP unavailable"); }, clock);
	}

	@Test
	public void offlineProofAndMetadata() throws Exception {
		WebEidResult result = verify();
		assertTrue(result.isValid());
		assertEquals("NOT_CHECKED", result.getOcspStatus());
		assertNull(result.getOcspResponse());
		assertEquals(user.getSubjectX500Principal().getName(), result.getSubject());
		assertArrayEquals(user.getEncoded(), Base64.getDecoder().decode(result.getCertificateDer()));
		request.setChallenge(Base64.getEncoder().encodeToString(new byte[33]));
		assertEquals("VERIFICATION_FAILED", verify().getReason());
	}

	@Test
	public void malformedAndUnsupportedTokens() throws Exception {
		java.lang.String original = request.getTokenJson();
		for (java.lang.String json : new java.lang.String[] { original.replace("RS256", "PS256"), original.replace("\"RS256\"", "12"),
				original.replace("\"format\"", "\"other\""), original.replace("\"signature\"", "\"format\""),
				original.substring(0, original.length() - 1) + ",\"extra\":true}", original + "{}" }) {
			request.setTokenJson(json);
			assertFalse(verify().isValid());
		}
	}

	@Test
	public void directCertificateInvocation() throws Exception {
		assertTrue(service.validate(request).isValid());
		request.setIssuerCertificate(null);
		try {
			service.validate(request);
			fail("Expected missing issuer rejection");
		}
		catch (IllegalArgumentException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	@Test
	public void onlineNeverFallsBackToOffline() throws Exception {
		request.setOfflinePolicy(false);
		try {
			verify();
			fail("Expected OCSP failure");
		}
		catch (java.io.IOException expected) {
			assertEquals("OCSP unavailable", expected.getMessage());
		}
		try {
			service.verify(request, issuer, Collections.singleton("1.2.3.4"), null, null, clock);
			fail("Expected missing responder rejection");
		}
		catch (IllegalArgumentException expected) {
			assertNotNull(expected.getMessage());
		}
	}
}
