package nabu.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.utils.types.CacheEntryOverview;
import nabu.utils.types.CacheOverview;
import be.nabu.eai.repository.EAIRepositoryCacheProvider;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.cache.api.CacheEntry;
import be.nabu.libs.cache.api.ExplorableCache;
import be.nabu.libs.cache.api.LimitedCache;

@WebService
public class Cache {
	
	public List<CacheOverview> getCaches() throws IOException {
		List<CacheOverview> cacheOverviews = new ArrayList<CacheOverview>();
		if (EAIResourceRepository.getInstance().getCacheProvider() instanceof EAIRepositoryCacheProvider) {
			EAIRepositoryCacheProvider cacheProvider = (EAIRepositoryCacheProvider) EAIResourceRepository.getInstance().getCacheProvider();
			for (String name : cacheProvider.getCaches()) {
				be.nabu.libs.cache.api.Cache cache = cacheProvider.get(name);
				CacheOverview cacheOverview = new CacheOverview();
				cacheOverview.setServiceId(name);
				if (cache instanceof LimitedCache) {
					cacheOverview.setMaxCacheSize(((LimitedCache) cache).getMaxTotalSize());
					cacheOverview.setCurrentCacheSize(((LimitedCache) cache).getCurrentSize());
					cacheOverview.setMaxEntrySize(((LimitedCache) cache).getMaxEntrySize());
				}
				if (cache instanceof ExplorableCache) {
					List<CacheEntryOverview> cacheEntries = new ArrayList<CacheEntryOverview>();
					for (CacheEntry entry : ((ExplorableCache) cache).getEntries()) {
						CacheEntryOverview cacheEntry = new CacheEntryOverview();
						cacheEntry.setLastAccessed(entry.getLastAccessed());
						cacheEntry.setLastModified(entry.getLastModified());
						cacheEntry.setSize(entry.getSize());
						cacheEntries.add(cacheEntry);
					}
					cacheOverview.setEntries(cacheEntries);
				}
				cacheOverviews.add(cacheOverview);
			}
		}
		return cacheOverviews;
	}
	
	public void prune(@WebParam(name = "serviceId") @NotNull String serviceId) throws IOException {
		be.nabu.libs.cache.api.Cache cache = EAIResourceRepository.getInstance().getCacheProvider().get(serviceId);
		if (cache != null) {
			cache.prune();
		}
	}
	
	public void refresh(@WebParam(name = "serviceId") @NotNull String serviceId) throws IOException {
		be.nabu.libs.cache.api.Cache cache = EAIResourceRepository.getInstance().getCacheProvider().get(serviceId);
		if (cache != null) {
			cache.refresh();
		}
	}
	
	public void clear(@WebParam(name = "serviceId") @NotNull String serviceId) throws IOException {
		be.nabu.libs.cache.api.Cache cache = EAIResourceRepository.getInstance().getCacheProvider().get(serviceId);
		if (cache != null) {
			cache.clear();
		}
	}
	
	public void set(@NotNull @WebParam(name = "cacheId") String cacheId, @WebParam(name = "key") @NotNull java.lang.Object key, @WebParam(name = "value") java.lang.Object value) throws IOException {
		if (value == null) {
			EAIResourceRepository.getInstance().getCacheProvider().get(cacheId).clear(key);
		}
		else {
			EAIResourceRepository.getInstance().getCacheProvider().get(cacheId).put(key, value);
		}
	}
	
	@WebResult(name = "value")
	public java.lang.Object get(@NotNull @WebParam(name = "cacheId") String cacheId, @WebParam(name = "key") @NotNull java.lang.Object key) throws IOException {
		return EAIResourceRepository.getInstance().getCacheProvider().get(cacheId).get(key);
	}
}
