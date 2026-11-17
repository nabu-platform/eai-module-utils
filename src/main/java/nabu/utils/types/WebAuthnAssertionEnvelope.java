package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "webAuthnAssertionEnvelope")
public class WebAuthnAssertionEnvelope {
	private String ceremonyId, credentialId, responseJson;

	public String getCeremonyId() { return ceremonyId; }
	public void setCeremonyId(String value) { ceremonyId = value; }
	public String getCredentialId() { return credentialId; }
	public void setCredentialId(String value) { credentialId = value; }
	public String getResponseJson() { return responseJson; }
	public void setResponseJson(String value) { responseJson = value; }
}
