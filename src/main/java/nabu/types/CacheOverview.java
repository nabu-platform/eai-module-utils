package nabu.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cache")
public class CacheOverview {
	private String serviceId;
	private long currentCacheSize, maxCacheSize, maxEntrySize;
	private List<CacheEntryOverview> entries;
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public long getCurrentCacheSize() {
		return currentCacheSize;
	}
	public void setCurrentCacheSize(long currentCacheSize) {
		this.currentCacheSize = currentCacheSize;
	}
	public long getMaxCacheSize() {
		return maxCacheSize;
	}
	public void setMaxCacheSize(long maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}
	public long getMaxEntrySize() {
		return maxEntrySize;
	}
	public void setMaxEntrySize(long maxEntrySize) {
		this.maxEntrySize = maxEntrySize;
	}
	public List<CacheEntryOverview> getEntries() {
		return entries;
	}
	public void setEntries(List<CacheEntryOverview> entries) {
		this.entries = entries;
	}
}
