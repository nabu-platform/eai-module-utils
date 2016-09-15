package nabu.utils.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cache")
public class CacheOverview {
	private String cacheId;
	private long currentCacheSize, maxCacheSize, maxEntrySize;
	private List<CacheEntryOverview> entries;
	
	public String getCacheId() {
		return cacheId;
	}
	public void setCacheId(String cacheId) {
		this.cacheId = cacheId;
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
