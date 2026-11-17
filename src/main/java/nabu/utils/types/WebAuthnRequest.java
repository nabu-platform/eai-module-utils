package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

/** Server-owned expectations must be loaded by the caller, never from the browser. */
@XmlRootElement(name = "webAuthnRequest")
public class WebAuthnRequest {
	private String responseJson, challenge, rpId, origin, credentialId, publicKey, publicKeyFormat, userHandle;
	private boolean requireUserVerification;
	private long signCount;

	public String getResponseJson() { return responseJson; }
	public void setResponseJson(String value) { responseJson = value; }
	public String getChallenge() { return challenge; }
	public void setChallenge(String value) { challenge = value; }
	public String getRpId() { return rpId; }
	public void setRpId(String value) { rpId = value; }
	public String getOrigin() { return origin; }
	public void setOrigin(String value) { origin = value; }
	public String getCredentialId() { return credentialId; }
	public void setCredentialId(String value) { credentialId = value; }
	public String getPublicKey() { return publicKey; }
	public void setPublicKey(String value) { publicKey = value; }
	public String getPublicKeyFormat() { return publicKeyFormat; }
	public void setPublicKeyFormat(String value) { publicKeyFormat = value; }
	public String getUserHandle() { return userHandle; }
	public void setUserHandle(String value) { userHandle = value; }
	public boolean isRequireUserVerification() { return requireUserVerification; }
	public void setRequireUserVerification(boolean value) { requireUserVerification = value; }
	public long getSignCount() { return signCount; }
	public void setSignCount(long value) { signCount = value; }
}
