package nabu.utils.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CacheProviderOverview {
	private String id;
	private List<CacheOverview> caches;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<CacheOverview> getCaches() {
		return caches;
	}
	public void setCaches(List<CacheOverview> caches) {
		this.caches = caches;
	}
}
