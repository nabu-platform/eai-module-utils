package nabu.utils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;

import be.nabu.utils.security.KeyPairType;
import be.nabu.utils.security.SignatureType;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class SecuritySignatureTest {
	@Test
	public void testEs256Service() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec("secp256r1"));
		KeyPair pair = generator.generateKeyPair();
		KeyPair other = generator.generateKeyPair();
		byte[] data = "signed content".getBytes(StandardCharsets.UTF_8);
		Signature signer = Signature.getInstance("SHA256withECDSA");
		signer.initSign(pair.getPrivate());
		signer.update(data);
		byte[] signature = signer.sign();
		Security service = new Security();

		assertTrue(service.verifySignature(new ByteArrayInputStream(data), signature,
			service.parsePublicKey(KeyPairType.EC, pair.getPublic().getEncoded()), SignatureType.SHA256WITHECDSA));
		assertFalse(service.verifySignature(new ByteArrayInputStream("tampered content".getBytes(StandardCharsets.UTF_8)), signature,
			service.parsePublicKey(KeyPairType.EC, pair.getPublic().getEncoded()), SignatureType.SHA256WITHECDSA));
		assertFalse(service.verifySignature(new ByteArrayInputStream(data), signature,
			service.parsePublicKey(KeyPairType.EC, other.getPublic().getEncoded()), SignatureType.SHA256WITHECDSA));
	}

	@Test(expected = InvalidKeySpecException.class)
	public void testMalformedSpkiService() throws Exception {
		new Security().parsePublicKey(KeyPairType.EC, new byte[] { 1, 2, 3 });
	}
}
