package nabu.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.utils.types.CacheEntryOverview;
import nabu.utils.types.CacheOverview;
import nabu.utils.types.CacheProviderOverview;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.CacheProviderArtifact;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.cache.api.AnnotatableCache;
import be.nabu.libs.cache.api.CacheEntry;
import be.nabu.libs.cache.api.CacheWithHash;
import be.nabu.libs.cache.api.ExplorableCache;
import be.nabu.libs.cache.api.LimitedCache;
import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.libs.types.api.KeyValuePair;

@WebService
public class Cache {
	
	@WebResult(name = "caches")
	public List<CacheProviderOverview> list(@WebParam(name = "cacheProviderId") String cacheProviderId, @WebParam(name = "cacheId") String cacheId) throws IOException {
		List<CacheProviderOverview> providers = new ArrayList<CacheProviderOverview>();
		for (CacheProviderArtifact cacheProvider : EAIResourceRepository.getInstance().getArtifacts(CacheProviderArtifact.class)) {
			if (cacheProviderId != null && !cacheProviderId.equals(cacheProvider.getId())) {
				continue;
			}
			List<CacheOverview> cacheOverviews = new ArrayList<CacheOverview>();
			for (String name : cacheProvider.getCaches()) {
				if (cacheId != null && !cacheId.equals(name)) {
					continue;
				}
				be.nabu.libs.cache.api.Cache cache = cacheProvider.get(name);
				CacheOverview cacheOverview = new CacheOverview();
				cacheOverview.setCacheId(name);
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
						if (cache instanceof CacheWithHash) {
							cacheEntry.setHash(((CacheWithHash) cache).hash(entry.getKey()));
						}
						cacheEntries.add(cacheEntry);
					}
					cacheOverview.setEntries(cacheEntries);
				}
				cacheOverviews.add(cacheOverview);
			}
			CacheProviderOverview cacheProviderOverview = new CacheProviderOverview();
			cacheProviderOverview.setCaches(cacheOverviews);
			cacheProviderOverview.setId(cacheProvider.getId());
			providers.add(cacheProviderOverview);
		}
		return providers;
	}
	
	public void prune(@NotNull @WebParam(name = "cacheProviderId") String cacheProviderId, @WebParam(name = "cacheId") String cacheId) throws IOException {
		Artifact resolve = EAIResourceRepository.getInstance().resolve(cacheProviderId);
		if (resolve instanceof CacheProviderArtifact) {
			if (cacheId != null) {
				be.nabu.libs.cache.api.Cache cache = ((CacheProviderArtifact) resolve).get(cacheId);
				if (cache != null) {
					cache.prune();
				}
			}
			else {
				for (String name : ((CacheProviderArtifact) resolve).getCaches()) {
					be.nabu.libs.cache.api.Cache cache = ((CacheProviderArtifact) resolve).get(name);
					if (cache != null) {
						cache.prune();
					}
				}
			}
		}
	}
	
	public void refresh(@NotNull @WebParam(name = "cacheProviderId") String cacheProviderId, @WebParam(name = "cacheId") String cacheId) throws IOException {
		Artifact resolve = EAIResourceRepository.getInstance().resolve(cacheProviderId);
		if (resolve instanceof CacheProviderArtifact) {
			if (cacheId != null) {
				be.nabu.libs.cache.api.Cache cache = ((CacheProviderArtifact) resolve).get(cacheId);
				if (cache != null) {
					cache.refresh();
				}
			}
			else {
				for (String name : ((CacheProviderArtifact) resolve).getCaches()) {
					be.nabu.libs.cache.api.Cache cache = ((CacheProviderArtifact) resolve).get(name);
					if (cache != null) {
						cache.refresh();
					}
				}
			}
		}
	}
	
	public void clear(@WebParam(name = "cacheProviderId") String cacheProviderId, @WebParam(name = "cacheId") String cacheId, @WebParam(name = "annotations") List<KeyValuePair> properties) throws IOException {
		Map<java.lang.String, java.lang.String> annotations = properties == null ? null : new Properties().toMap(properties);
		if (cacheProviderId != null) {
			Artifact resolve = EAIResourceRepository.getInstance().resolve(cacheProviderId);
			if (resolve instanceof CacheProviderArtifact) {
				clearSingle(cacheId, resolve, annotations);
			}
		}
		else {
			for (CacheProviderArtifact cacheProvider : EAIResourceRepository.getInstance().getArtifacts(CacheProviderArtifact.class)) {
				clearSingle(cacheId, cacheProvider, annotations);
			}
		}
	}

	private void clearSingle(String cacheId, Artifact resolve, Map<java.lang.String, java.lang.String> annotations) throws IOException {
		if (cacheId != null) {
			be.nabu.libs.cache.api.Cache cache = ((CacheProviderArtifact) resolve).get(cacheId);
			if (cache != null) {
				// if you want an annotated clear, don't clear everything
				if (annotations != null && !annotations.isEmpty()) {
					// if we can't do an annotated clear, we clear nothing
					if (cache instanceof AnnotatableCache) {
						((AnnotatableCache) cache).clear(annotations);
					}
				}
				else {
					cache.clear();
				}
			}
		}
		else {
			for (String name : ((CacheProviderArtifact) resolve).getCaches()) {
				be.nabu.libs.cache.api.Cache cache = ((CacheProviderArtifact) resolve).get(name);
				if (cache != null) {
					// if you want an annotated clear, don't clear everything
					if (annotations != null && !annotations.isEmpty()) {
						// if we can't do an annotated clear, we clear nothing
						if (cache instanceof AnnotatableCache) {
							((AnnotatableCache) cache).clear(annotations);
						}
					}
					else {
						cache.clear();
					}
				}
			}
		}
	}
	
	void set(@NotNull @WebParam(name = "cacheId") String cacheId, @WebParam(name = "key") @NotNull java.lang.Object key, @WebParam(name = "value") java.lang.Object value) throws IOException {
		if (value == null) {
			EAIResourceRepository.getInstance().getCacheProvider().get(cacheId).clear(key);
		}
		else {
			EAIResourceRepository.getInstance().getCacheProvider().get(cacheId).put(key, value);
		}
	}
	
	@WebResult(name = "value")
	java.lang.Object get(@NotNull @WebParam(name = "cacheId") String cacheId, @WebParam(name = "key") @NotNull java.lang.Object key) throws IOException {
		return EAIResourceRepository.getInstance().getCacheProvider().get(cacheId).get(key);
	}
}
