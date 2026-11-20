package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "webEidResult")
public class WebEidResult {
	private boolean valid;
	private String reason, subject, issuer, serialNumber, notBefore, notAfter, certificateDer, ocspStatus;
	private byte[] ocspResponse;

	public boolean isValid() { return valid; }
	public void setValid(boolean value) { valid = value; }
	public String getReason() { return reason; }
	public void setReason(String value) { reason = value; }
	public String getSubject() { return subject; }
	public void setSubject(String value) { subject = value; }
	public String getIssuer() { return issuer; }
	public void setIssuer(String value) { issuer = value; }
	public String getSerialNumber() { return serialNumber; }
	public void setSerialNumber(String value) { serialNumber = value; }
	public String getNotBefore() { return notBefore; }
	public void setNotBefore(String value) { notBefore = value; }
	public String getNotAfter() { return notAfter; }
	public void setNotAfter(String value) { notAfter = value; }
	public String getCertificateDer() { return certificateDer; }
	public void setCertificateDer(String value) { certificateDer = value; }
	public String getOcspStatus() { return ocspStatus; }
	public void setOcspStatus(String value) { ocspStatus = value; }
	public byte[] getOcspResponse() { return ocspResponse; }
	public void setOcspResponse(byte[] value) { ocspResponse = value; }
}
