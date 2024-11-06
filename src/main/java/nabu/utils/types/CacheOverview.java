/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
