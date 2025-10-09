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

package nabu.utils;

import java.lang.Object;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.utils.types.ContextInformation;
import nabu.utils.types.ExceptionSummary;
import nabu.utils.types.FeatureList;
import nabu.utils.types.ServiceInstance;
import nabu.utils.types.ValidationSummary;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.eai.api.Hidden;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.FeatureConfigurator;
import be.nabu.eai.repository.api.FeatureDescription;
import be.nabu.eai.repository.api.FeatureProviderService;
import be.nabu.eai.repository.impl.CorrelationIdEnricher;
import be.nabu.eai.repository.impl.RepositoryArtifactResolver;
import be.nabu.libs.artifacts.FeatureImpl;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.artifacts.api.Feature;
import be.nabu.libs.artifacts.api.FeaturedArtifact;
import be.nabu.libs.artifacts.api.InterruptibleArtifact;
import be.nabu.libs.authentication.api.Device;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.authentication.api.WrappedToken;
import be.nabu.libs.authentication.api.principals.DevicePrincipal;
import be.nabu.libs.authentication.impl.ImpersonateToken;
import be.nabu.libs.http.api.HTTPRequest;
import be.nabu.libs.nio.impl.RequestProcessor;
import be.nabu.libs.services.NarrativeParts;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.FeaturedExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceInstanceWithPipeline;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.types.structure.StructureInstance;
import be.nabu.libs.types.utils.KeyValuePairImpl;
import be.nabu.libs.validator.api.Validation;
import be.nabu.utils.mime.api.Header;
import be.nabu.utils.mime.api.ModifiablePart;
import be.nabu.utils.mime.impl.MimeUtils;

@WebService
public class Runtime {
	
//	private static ServiceInterface trackInterface = MethodServiceInterface.wrap(FlatServiceTracker.class, "track"); 

	public static final java.lang.String APPLICATION_REGEX = java.lang.System.getProperty("nabu.system.applicationRegex", "([^.]+).*");
	public static final java.lang.String PROCESS_REGEX = java.lang.System.getProperty("nabu.system.processRegex", "[^.]+\\.processes\\.([^.]+).*");
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "user")
	public String getCurrentUser() {
		return executionContext.getSecurityContext().getToken() == null ? null : executionContext.getSecurityContext().getToken().getName();
	}
	
//	@WebResult(name = "principal")
//	public Principal getCurrentPrincipal() {
//		return executionContext.getSecurityContext().getToken();
//	}
	
	@WebResult(name = "token")
	public Token getCurrentToken() {
		return executionContext.getSecurityContext().getToken();
	}
	
	@WebResult(name = "uptime")
	public Date uptime() {
		return EAIResourceRepository.getInstance().getStarted();
	}
	
	@WebResult(name = "device")
	public Device getCurrentDevice() {
		return getDeviceFromToken(executionContext.getSecurityContext().getToken());
	}
	
	@Deprecated
	@WebResult(name = "token")
	public Token unwrapToken(@WebParam(name = "token") Token token) {
		return token instanceof WrappedToken ? ((WrappedToken) token).getOriginalToken() : null;
	}
	
	@Deprecated
	@WebResult(name = "token")
	public Token wrapToken(@WebParam(name = "token") Token originalToken, @WebParam(name = "name") String name, @WebParam(name = "realm") String realm) {
		return new ImpersonateToken(originalToken, realm, name);
	}

	@WebResult(name = "device")
	public Device getDeviceFromToken(@WebParam(name = "token") Token token) {
		Device device = null;
		if (token != null && token instanceof DevicePrincipal) {
			device = ((DevicePrincipal) token).getDevice();
		}
		if (device == null && token != null && token.getCredentials() != null && !token.getCredentials().isEmpty()) {
			for (Principal credential : token.getCredentials()) {
				if (credential instanceof DevicePrincipal) {
					device = ((DevicePrincipal) credential).getDevice();
					if (device != null) {
						break;
					}
				}
			}
		}
		return device;
	}
	
	@WebResult(name = "realm")
	public String getCurrentRealm() {
		return executionContext.getSecurityContext().getToken() instanceof Token ? ((Token) executionContext.getSecurityContext().getToken()).getRealm() : null;
	}
	
	@WebResult(name = "service")
	public String getService(@WebParam(name = "offset") Integer offset) {
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		if (offset == null) {
			offset = 1;
		}
		while (offset > 0 && runtime.getParent() != null) {
			runtime = runtime.getParent();
			offset--;
		}
		Service service = runtime.getService();
		return service instanceof DefinedService ? ((DefinedService) service).getId() : null;
	}
	
	@WebResult(name = "services")
	public List<String> getServices() {
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		List<String> services = new ArrayList<String>();
		while (runtime != null) {
			if (runtime.getService() instanceof DefinedService) {
				services.add(((DefinedService) runtime.getService()).getId());
			}
			runtime = runtime.getParent();
		}
		// remove the first service, it is _this_ service (nabu.utils.Runtime.getServices)
		if (!services.isEmpty()) {
			services.remove(0);
		}
		return services;
	}

	@WebResult(name = "service")
	public String getRootService() {
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		while (runtime.getParent() != null) {
			runtime = runtime.getParent();
		}
		if (runtime.getService() instanceof DefinedService) {
			return ((DefinedService) runtime.getService()).getId();
		}
		return null;
	}
	
	@WebResult(name = "application")
	public String getApplication() {
		String rootService = getRootService();
		return rootService == null ? null : rootService.replaceAll(APPLICATION_REGEX, "$1");
	}
	
	@WebResult(name = "process")
	public String getProcess() {
		String rootService = getRootService();
		return rootService == null ? null : rootService.replaceAll(PROCESS_REGEX, "$1");
	}
	
	public void setContext(@WebParam(name = "key") String key, @WebParam(name = "value") Object value) {
		if (value == null) {
			ServiceRuntime.getRuntime().getContext().remove(key);
		}
		else {
			ServiceRuntime.getRuntime().getContext().put(key, value);
		}
	}
	
	@WebResult(name = "value")
	public Object getContext(@WebParam(name = "key") String key) {
		return ServiceRuntime.getRuntime().getContext().get(key);
	}
	
	@WebResult(name = "summary")
	public ExceptionSummary summarizeException(@WebParam(name = "exception") Exception exception) {
		return exception == null ? null : ExceptionSummary.build(exception);
	}
	
	@WebResult(name = "summary")
	public ValidationSummary summarizeValidations(@WebParam(name = "validations") List<Validation<?>> validations) {
		return validations == null || validations.isEmpty() ? null : ValidationSummary.build(validations);
	}
	
	@WebResult(name = "information")
	public ContextInformation getContextInformation(@WebParam(name = "id") String id) {
		if (id == null) {
			id = ServiceUtils.getServiceContext(ServiceRuntime.getRuntime());
		}
		// everything in the nabu namespace is the utility package
		if (id.startsWith("nabu.")) {
			ContextInformation contextInformation = new ContextInformation();
			contextInformation.setProject("nabu");
			contextInformation.setProjectType("utility");
			return contextInformation;
		}
		Entry entry = EAIResourceRepository.getInstance().getEntry(id);
		while (entry != null && (entry.getCollection() == null || !entry.getCollection().getType().equalsIgnoreCase("project"))) {
			entry = entry.getParent();
		}
		// if we have an entry at this point, we can get some information about it
		if (entry != null) {
			String subType = entry.getCollection().getSubType();
			if (subType == null) {
				subType = entry.getId().equals("nabu") ? "utility" : "application";
			}
			ContextInformation contextInformation = new ContextInformation();
			contextInformation.setProject(entry.getId());
			contextInformation.setProjectType(subType);
			return contextInformation;
		}
		// for backwards compatibility, we assume an unmarked folder is an application type project
		ContextInformation contextInformation = new ContextInformation();
		contextInformation.setProject(id.replaceAll("^([^.]+)\\..*$", "$1"));
		contextInformation.setProjectType("application");
		return contextInformation;
	}
	
	@WebResult(name = "pipeline")
	public Object getPipeline(@WebParam(name = "offset") Integer offset, @WebParam(name = "serviceId") String serviceId) {
		// if you look with a service filter, you want to find the first hit
		// if you look without, you want to skip at least the runtime for this java service
		if (offset == null) {
			offset = 1;
		}
		// you will set an offset of 0 if you want to find the first instance of a given service, but because of the way the loop works, we need 1
		// this also works if you don't have a service filter, we want to go up x amount of runtimes, and at least one because that is this java service
		else {
			offset++;
		}
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		while (runtime != null && offset > 0) {
			runtime = runtime.getParent();
			if (serviceId != null) {
				if (runtime.getService() instanceof DefinedService && ((DefinedService) runtime.getService()).getId().equals(serviceId)) {
					offset--;
				}
			}
			else {
				offset--;
			}
		}
		if (runtime.getServiceInstance() instanceof ServiceInstanceWithPipeline) {
			return ((ServiceInstanceWithPipeline) runtime.getServiceInstance()).getPipeline();
		}
		else {
			Structure structure = new Structure();
			structure.add(new ComplexElementImpl("input", runtime.getService().getServiceInterface().getInputDefinition(), structure));
			structure.add(new ComplexElementImpl("output", runtime.getService().getServiceInterface().getOutputDefinition(), structure));
			StructureInstance instance = structure.newInstance();
			instance.set("input", runtime.getInput());
			instance.set("output", runtime.getOutput());
			return instance;
		}
	}
	
	public void interrupt(@WebParam(name = "artifactId") String artifactId) {
		Artifact resolve = EAIResourceRepository.getInstance().resolve(artifactId);
		if (resolve instanceof InterruptibleArtifact) {
			((InterruptibleArtifact) resolve).interrupt();
		}
	}
	
	@WebResult(name = "interrupted")
	public boolean interrupted(@WebParam(name = "artifactId") String artifactId) {
		Artifact resolve = EAIResourceRepository.getInstance().resolve(artifactId);
		if (resolve instanceof InterruptibleArtifact) {
			return ((InterruptibleArtifact) resolve).interrupted();
		}
		return false;
	}
	
	public void setServiceContext(@WebParam(name = "context") String context) {
		// the current runtime is the wrapper for this particular method call, if it has no parent, it is not part of a larger managed call
		// we want to set it in global properties then
		if (ServiceRuntime.getRuntime().getParent() == null) {
			ServiceUtils.setServiceContext(null, context);
		}
		else {
			ServiceUtils.setServiceContext(ServiceRuntime.getRuntime(), context);
		}
	}
	
	@WebResult(name = "serviceContext")
	@NotNull
	public String getServiceContext() {
		return ServiceUtils.getServiceContext(ServiceRuntime.getRuntime());
	}
	
	@WebResult(name = "match")
	public Object getForContext(@WebParam(name = "context") String context, @WebParam(name = "objects") List<Object> objects, @WebParam(name = "contextField") String field) {
		if (objects == null) {
			return null;
		}
		return RepositoryArtifactResolver.getContextualFor(context, objects, field);
	}
	
	@Deprecated
	@Hidden
	public void fireEvent(@WebParam(name = "event") java.lang.Object event) {
		if (executionContext.getEventTarget() != null) {
			executionContext.getEventTarget().fire(event, this);
		}
	}
	
	@Deprecated
	@Hidden
	// should implement: be.nabu.eai.server.api.EventHandler.handle
	public void subscribeEvents(@WebParam(name = "serviceId") java.lang.String serviceId) {
		if (executionContext.getServiceContext().getServiceRunner() instanceof be.nabu.eai.server.Server) {
			((be.nabu.eai.server.Server) executionContext.getServiceContext().getServiceRunner()).getProcessor().add(serviceId);
		}
	}
	
	@WebResult(name = "running")
	public List<ServiceInstance> getRunning() {
		List<ServiceInstance> descriptions = new ArrayList<ServiceInstance>();
		List<Long> ids = new ArrayList<Long>();
		for (ServiceRuntime runtime : ServiceRuntime.getRunning()) {
			DefinedService lastService = null;
			ServiceRuntime lastRuntime = null;
			while (runtime != null) {
				if (runtime != null) {
					lastRuntime = runtime;
				}
				if (runtime.getService() instanceof DefinedService) {
					lastService = (DefinedService) runtime.getService();
				}
				runtime = runtime.getParent();
			}
			if (lastService != null && lastRuntime != null && !ids.contains(lastRuntime.getId())) {
				ids.add(lastRuntime.getId());
				ServiceInstance description = new ServiceInstance();
				description.setId(lastRuntime.getId());
				description.setServiceId(lastService.getId());
				Token token = lastRuntime.getExecutionContext().getSecurityContext().getToken();
				if (token != null) {
					description.setAlias(token.getName());
					description.setRealm(token.getRealm());
				}
				description.setStarted(lastRuntime.getStarted());
				descriptions.add(description);
			}
		}
		return descriptions;
	}
	
	@WebResult(name = "correlationId")
	@NotNull
	public String getCorrelationId() {
		return ServiceRuntime.getRuntime().getCorrelationId();
	}
	
	@WebResult(name = "sessionId")
	public String getSessionId() {
		Object currentRequest = RequestProcessor.getCurrentRequest();
		if (currentRequest instanceof HTTPRequest) {
			ModifiablePart content = ((HTTPRequest) currentRequest).getContent();
			if (content != null) {
				Header header = MimeUtils.getHeader("Session-Id", content.getHeaders());
				if (header != null) {
					return header.getValue();
				}
			}
		}
		return null;
	}
	
	@WebResult(name = "conversationId")
	public String getConversationId() {
		return CorrelationIdEnricher.getConversationId();
	}
	
	@WebResult(name = "narrativeId")
	public String getNarrativeId() {
		return ServiceRuntime.getRuntime().getNarrativeId();
	}
	
	// by default it is set in the "current" execution context, you can however set it in a parent execution (e.g. when using service trackers
	public void startNarrative(@WebParam(name = "narrativeId") List<String> narrativeId, @WebParam(name = "values") List<KeyValuePair> values, @WebParam(name = "depth") Integer depth) {
		// we don't want to start the narrative in our own limited runtime but in the parent runtime
		ServiceRuntime runtime = ServiceRuntime.getRuntime().getParent();
		if (runtime != null && depth != null) {
			for (int i = 0; i < depth; i++) {
				runtime = runtime.getParent();
				if (runtime == null) {
					break;
				}
			}
		}
		// if it exists
		if (runtime != null) {
			if (narrativeId != null) {
				for (String id : narrativeId) {
					if (id != null) {
						runtime.startNarrative(id);
					}
				}
			}
			if (values != null) {
				for (KeyValuePair value : values) {
					if (value.getKey() != null && value.getValue() != null) {
						runtime.startNarrative(value.getKey(), value.getValue());
					}
				}
			}
		}
	}
	
	public void stopNarrative(@WebParam(name = "narrativeId") List<String> narrativeId, @WebParam(name = "values") List<KeyValuePair> values, @WebParam(name = "depth") Integer depth) {
		ServiceRuntime runtime = ServiceRuntime.getRuntime().getParent();
		if (runtime != null && depth != null) {
			for (int i = 0; i < depth; i++) {
				runtime = runtime.getParent();
				if (runtime == null) {
					break;
				}
			}
		}
		if (runtime != null) {
			if (narrativeId != null) {
				for (String id : narrativeId) {
					if (id != null) {
						runtime.stopNarrative(id);
					}
				}
			}
			if (values != null) {
				for (KeyValuePair value : values) {
					if (value.getKey() != null) {
						runtime.stopNarrative(value.getKey(), value.getValue());
					}
				}
			}
		}
	}
	
	public static class StandardizedNarrativeParts {
		private List<String> ids;
		private List<KeyValuePair> values;
		public List<String> getIds() {
			return ids;
		}
		public void setIds(List<String> ids) {
			this.ids = ids;
		}
		public List<KeyValuePair> getValues() {
			return values;
		}
		public void setValues(List<KeyValuePair> values) {
			this.values = values;
		}
	}
	
	@WebResult(name = "parts")
	public StandardizedNarrativeParts getNarrativeIdParts() {
		StandardizedNarrativeParts parts = new StandardizedNarrativeParts();
		NarrativeParts narrativeParts = ServiceRuntime.getRuntime().getNarrativeParts();
		if (narrativeParts != null) {
			parts.setIds(narrativeParts.getIds());
			if (narrativeParts.getValues() != null) {
				List<KeyValuePair> keyValues = new ArrayList<KeyValuePair>();
				for (Map.Entry<String, String> entry : narrativeParts.getValues().entrySet()) {
					keyValues.add(new KeyValuePairImpl(entry.getKey(), entry.getValue()));
				}
				parts.setValues(keyValues);
			}
		}
		return parts;
	}
	
	// in the end we didn't need it (yet)
//	public void setCorrelationId(@WebParam(name = "correlationId") String correlationId) {
//		// there is little use for setting the correlation id for _this_ service, it is the runtime of the java call running this particular method
//		// however, we are likely interested in setting the correlation id of the service that called this
//		ServiceRuntime.getRuntime().getParent().setCorrelationId(correlationId);
//	}
	
	private static java.util.Map<String, List<Feature>> features;
	
	private static java.util.Map<String, List<Feature>> getAvailableFeatures() {
		if (features == null) {
			synchronized(Runtime.class) {
				if (features == null) {
					java.util.Map<String, List<Feature>> features = new HashMap<String, List<Feature>>();
					EAIResourceRepository repository = EAIResourceRepository.getInstance();
					for (FeaturedArtifact artifact : repository.getArtifacts(FeaturedArtifact.class)) {
						List<Feature> availableFeatures = artifact.getAvailableFeatures();
						if (availableFeatures != null) {
							features.put(artifact.getId(), availableFeatures);
						}
					}
					Runtime.features = features;
				}
			}
		}
		return features;
	}
	
	@Deprecated
	public void scanFeatures() {
		features = null;
		getAvailableFeatures();
	}
	
	@Deprecated
	public void toggleFeature(@NotNull @WebParam(name = "feature") String feature, @WebParam(name = "enabled") Boolean enabled) {
		if (executionContext instanceof FeaturedExecutionContext) {
			if (enabled != null && enabled) {
				if (!((FeaturedExecutionContext) executionContext).getEnabledFeatures().contains(feature)) {
					((FeaturedExecutionContext) executionContext).getEnabledFeatures().add(feature);
				}
			}
			else {
				((FeaturedExecutionContext) executionContext).getEnabledFeatures().remove(feature);
			}
		}
		else {
			throw new IllegalStateException("Not a featured execution context");
		}
	}
	
	@Deprecated
	@WebResult(name = "features")
	public FeatureList getFeatures(@WebParam(name = "id") String id, @WebParam(name = "token") Token token, @WebParam(name = "enabledOnly") Boolean enabledOnly) {
		FeatureList list = new FeatureList();
		
		List<Feature> enabled = new ArrayList<Feature>();
		List<Feature> disabled = new ArrayList<Feature>();
		list.setEnabled(enabled);
		list.setDisabled(disabled);
		
		if (enabledOnly != null && enabledOnly) {
			EAIResourceRepository repository = EAIResourceRepository.getInstance();
			Date latest = null;
			List<String> enabledRepositoryFeatureNames = repository.getEnabledFeatures(token);
			if (enabledRepositoryFeatureNames != null) {
				for (String feature : enabledRepositoryFeatureNames) {
					enabled.add(new FeatureImpl(feature, null));
				}
			}
			for (FeatureConfigurator configurator : repository.getArtifacts(FeatureConfigurator.class)) {
				List<String> enabledFeatureNames = configurator.getEnabledFeatures(token);
				if (enabledFeatureNames != null) {
					for (String feature : enabledFeatureNames) {
						enabled.add(new FeatureImpl(feature, null));
					}
				}
				if (latest == null || latest.before(configurator.getLastModified())) {
					latest = configurator.getLastModified();
				}
			}
			for (FeatureProviderService provider : repository.getArtifacts(FeatureProviderService.class)) {
				List<FeatureDescription> features = provider.features(token);
				if (features != null) {
					for (FeatureDescription description : features) {
						Boolean featureEnabled = description.getEnabled();
						if (featureEnabled != null && featureEnabled) {
							enabled.add(new FeatureImpl(description.getName(), null));
						}
						Date lastModified = description.getLastModified();
						if (latest == null || latest.before(lastModified)) {
							latest = lastModified;
						}
					}
				}
			}
			if (EAIResourceRepository.isDevelopment()) {
				enabled.add(new FeatureImpl("DEV", null));
			}
			else {
				enabled.add(new FeatureImpl("LIVE", null));
			}
			list.setLastModified(latest);
			return list;
		}
		else {
			java.util.Map<String, Feature> features = new HashMap<String, Feature>();
			EAIResourceRepository repository = EAIResourceRepository.getInstance();
			Map<java.lang.String, List<Feature>> availableFeatures = getAvailableFeatures();
			
			for (String artifactId : availableFeatures.keySet()) {
				if (id == null || artifactId.equals(id) || artifactId.startsWith(id + ".")) {
					for (Feature feature : availableFeatures.get(artifactId)) {
						features.put(feature.getName(), feature);
					}
				}
			}
			List<String> allEnabled = new ArrayList<String>();
			
			List<String> enabledRepositoryFeatures = repository.getEnabledFeatures(token);
			if (enabledRepositoryFeatures != null) {
				allEnabled.addAll(enabledRepositoryFeatures);
			}
			
			for (FeatureConfigurator configurator : repository.getArtifacts(FeatureConfigurator.class)) {
				if (configurator.getContext() != null && !configurator.getContext().trim().isEmpty() && id != null) {
					boolean matches = false;
					for (String context : configurator.getContext().split("[\\s]*,[\\s]*")) {
						if (id.equals(context) || id.startsWith(context + ".")) {
							matches = true;
						}
					}
					if (!matches) {
						continue;
					}
				}
				List<String> enabledFeatures = configurator.getEnabledFeatures(token);
				if (enabledFeatures != null) {
					allEnabled.addAll(enabledFeatures);
				}
				Date lastModified = configurator.getLastModified();
				if (lastModified != null && (list.getLastModified() == null || list.getLastModified().before(lastModified))) {
					list.setLastModified(lastModified);
				}
			}
			for (FeatureProviderService provider : repository.getArtifacts(FeatureProviderService.class)) {
				List<FeatureDescription> result = provider.features(token);
				if (result != null) {
					for (FeatureDescription description : result) {
						Boolean featureEnabled = description.getEnabled();
						if (featureEnabled != null && featureEnabled) {
							if (description.getContext() != null && !description.getContext().trim().isEmpty()) {
								boolean matches = false;
								for (String context : description.getContext().split("[\\s]*,[\\s]*")) {
									if (id.equals(context) || id.startsWith(context + ".")) {
										matches = true;
									}
								}
								if (!matches) {
									continue;
								}
								allEnabled.add(description.getName());
							}
						}
						Date lastModified = description.getLastModified();
						if (lastModified != null && (list.getLastModified() == null || list.getLastModified().before(lastModified))) {
							list.setLastModified(lastModified);
						}
					}
				}
			}
			
			features.put("DEV", new FeatureImpl("DEV", null));
			features.put("LIVE", new FeatureImpl("DEV", null));
			if (EAIResourceRepository.isDevelopment()) {
				allEnabled.add("DEV");
			}
			else {
				allEnabled.add("LIVE");
			}
			
			
			for (String feature : features.keySet()) {
				if (features.containsKey(feature)) {
					if (allEnabled.contains(feature)) {
						enabled.add(features.get(feature));
					}
					else {
						disabled.add(features.get(feature));
					}
				}
			}
		}
		return list;
	}
}
