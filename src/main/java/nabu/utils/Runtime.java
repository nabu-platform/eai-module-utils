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
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceInstanceWithPipeline;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.types.structure.StructureInstance;
import be.nabu.libs.validator.api.Validation;

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
		ServiceUtils.setServiceContext(ServiceRuntime.getRuntime(), context);
	}
	
	@WebResult(name = "serviceContext")
	@NotNull
	public String getServiceContext() {
		return ServiceUtils.getServiceContext(ServiceRuntime.getRuntime());
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
	
	public void scanFeatures() {
		features = null;
		getAvailableFeatures();
	}
	
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
