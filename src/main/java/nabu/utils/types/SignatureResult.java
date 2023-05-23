package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "signatureResult")
public class SignatureResult {
	private boolean valid;

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
