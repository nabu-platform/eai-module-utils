package nabu.utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.artifacts.api.FeaturedArtifact;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.FeaturedExecutionContext;
import nabu.utils.types.FeatureDescription;

@WebService
public class Feature {
	
	private ExecutionContext executionContext;

	// you can toggle a global feature, it is in memory so must be toggled after startup
	public void toggleGlobal(@NotNull @WebParam(name = "feature") java.lang.String feature, @WebParam(name = "enabled") Boolean enabled) {
		EAIResourceRepository.getInstance().toggleGlobalFeature(feature, enabled != null && enabled);
	}
	
	@WebResult(name = "previousValue")
	public boolean toggle(@NotNull @WebParam(name = "feature") java.lang.String feature, @WebParam(name = "enabled") Boolean enabled) {
		if (executionContext instanceof FeaturedExecutionContext) {
			boolean previousValue = ((FeaturedExecutionContext) executionContext).getEnabledFeatures().contains(feature);
			if (enabled != null && enabled) {
				if (!((FeaturedExecutionContext) executionContext).getEnabledFeatures().contains(feature)) {
					((FeaturedExecutionContext) executionContext).getEnabledFeatures().add(feature);
				}
			}
			else {
				((FeaturedExecutionContext) executionContext).getEnabledFeatures().remove(feature);
			}
			return previousValue;
		}
		else {
			throw new IllegalStateException("Not a featured execution context");
		}
	}
	
	@WebResult(name = "features")
	public java.util.List<FeatureDescription> scan(@WebParam(name = "id") java.lang.String id) throws IOException, ParseException {
		java.util.Map<java.lang.String, FeatureDescription> map = new HashMap<java.lang.String, FeatureDescription>();
		scan(id, map);
		return new ArrayList<FeatureDescription>(map.values());
	}
	
	private void scan(java.lang.String id, java.util.Map<java.lang.String, FeatureDescription> map) {
		for (FeaturedArtifact artifact : EAIResourceRepository.getInstance().getArtifacts(FeaturedArtifact.class)) {
			if (artifact != null && (id == null || artifact.getId().equals(id) || artifact.getId().startsWith(id + "."))) {
				addToMap(map, artifact);
			}
		}
	}

	private void addToMap(java.util.Map<java.lang.String, FeatureDescription> map, FeaturedArtifact artifact) {
		for (be.nabu.libs.artifacts.api.Feature feature : artifact.getAvailableFeatures()) {
			if (!map.containsKey(feature.getName())) {
				FeatureDescription description = new FeatureDescription();
				description.setName(feature.getName());
				description.setArtifacts(new ArrayList<java.lang.String>());
				map.put(feature.getName(), description);
			}
			if (feature.getDescription() != null) {
				map.get(feature.getName()).setDescription(feature.getDescription());
			}
			map.get(feature.getName()).getArtifacts().add(artifact.getId());
		}
	}
	
	

	@WebResult(name = "features")
	public java.util.List<FeatureDescription> list(@NotNull @WebParam(name = "id") java.lang.String id, @WebParam(name = "recursive") Boolean recursive) throws IOException, ParseException {
		java.util.Map<java.lang.String, FeatureDescription> map = new HashMap<java.lang.String, FeatureDescription>();
		list(map, id, recursive, new ArrayList<java.lang.String>());
		return new ArrayList<FeatureDescription>(map.values());
	}
	
	private void list(java.util.Map<java.lang.String, FeatureDescription> map, @NotNull @WebParam(name = "id") java.lang.String id, @WebParam(name = "recursive") Boolean recursive, @WebParam(name = "blacklist") java.util.List<java.lang.String> blacklist) throws IOException, ParseException {
		if (id != null) {
			Entry entry = EAIResourceRepository.getInstance().getEntry(id);
			if (entry != null && entry.isNode()) {
				Artifact artifact = entry.getNode().getArtifact();
				if (artifact instanceof FeaturedArtifact) {
					addToMap(map, (FeaturedArtifact) artifact);
				}
				if (recursive != null && recursive && (blacklist == null || !blacklist.contains(id))) {
					blacklist.add(id);
					for (java.lang.String reference : EAIResourceRepository.getInstance().getReferences(id)) {
						list(map, reference, recursive, blacklist);
					}
				}
			}
		}
	}
}
