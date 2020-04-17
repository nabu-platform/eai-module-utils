package nabu.utils.types;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.eai.repository.api.Feature;

@XmlRootElement
public class FeatureList {
	private List<Feature> enabled, disabled;
	private Date lastModified;

	public List<Feature> getEnabled() {
		return enabled;
	}

	public void setEnabled(List<Feature> enabled) {
		this.enabled = enabled;
	}

	public List<Feature> getDisabled() {
		return disabled;
	}

	public void setDisabled(List<Feature> disabled) {
		this.disabled = disabled;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
}
