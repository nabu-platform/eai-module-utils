package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "webAuthnResult")
public class WebAuthnResult {
	private String credentialId, publicKey, publicKeyFormat, algorithm, reason;
	private boolean valid;
	private long signCount;
	private boolean backupEligible, backedUp;

	public boolean isValid() { return valid; }
	public void setValid(boolean value) { valid = value; }
	public String getReason() { return reason; }
	public void setReason(String value) { reason = value; }
	public String getAlgorithm() { return algorithm; }
	public void setAlgorithm(String value) { algorithm = value; }
	public String getPublicKeyFormat() { return publicKeyFormat; }
	public void setPublicKeyFormat(String value) { publicKeyFormat = value; }
	public String getCredentialId() { return credentialId; }
	public void setCredentialId(String value) { credentialId = value; }
	public String getPublicKey() { return publicKey; }
	public void setPublicKey(String value) { publicKey = value; }
	public long getSignCount() { return signCount; }
	public void setSignCount(long value) { signCount = value; }
	public boolean isBackupEligible() { return backupEligible; }
	public void setBackupEligible(boolean value) { backupEligible = value; }
	public boolean isBackedUp() { return backedUp; }
	public void setBackedUp(boolean value) { backedUp = value; }
}
