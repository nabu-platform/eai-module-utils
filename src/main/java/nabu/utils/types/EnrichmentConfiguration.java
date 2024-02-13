package nabu.utils.types;

import java.util.List;

import be.nabu.libs.types.api.KeyValuePair;

public class EnrichmentConfiguration {
	private List<KeyValuePair> enrich, persist;

	public List<KeyValuePair> getEnrich() {
		return enrich;
	}
	public void setEnrich(List<KeyValuePair> enrich) {
		this.enrich = enrich;
	}
	public List<KeyValuePair> getPersist() {
		return persist;
	}
	public void setPersist(List<KeyValuePair> persist) {
		this.persist = persist;
	}
}