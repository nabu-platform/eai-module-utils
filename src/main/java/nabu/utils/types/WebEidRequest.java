package nabu.utils.types;

import java.security.cert.Certificate;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/** All fields except tokenJson must come from server-controlled policy and ceremony storage. */
@XmlRootElement(name = "webEidRequest")
public class WebEidRequest {
	private String tokenJson, challenge, origin, ocspEndpoint;
	private Certificate issuerCertificate;
	private List<String> allowedPolicies;
	private boolean offlinePolicy;

	public String getTokenJson() { return tokenJson; }
	public void setTokenJson(String value) { tokenJson = value; }
	public String getChallenge() { return challenge; }
	public void setChallenge(String value) { challenge = value; }
	public String getOrigin() { return origin; }
	public void setOrigin(String value) { origin = value; }
	public Certificate getIssuerCertificate() { return issuerCertificate; }
	public void setIssuerCertificate(Certificate value) { issuerCertificate = value; }
	public String getOcspEndpoint() { return ocspEndpoint; }
	public void setOcspEndpoint(String value) { ocspEndpoint = value; }
	public List<String> getAllowedPolicies() { return allowedPolicies; }
	public void setAllowedPolicies(List<String> value) { allowedPolicies = value; }
	public boolean isOfflinePolicy() { return offlinePolicy; }
	public void setOfflinePolicy(boolean value) { offlinePolicy = value; }
}
