package nabu.utils;

import java.lang.Object;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.repository.api.ModifiableServiceRuntimeTrackerProvider;
import be.nabu.eai.repository.util.FlatServiceTrackerWrapper;
import be.nabu.eai.services.api.FlatServiceTracker;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceInterface;
import be.nabu.libs.services.pojo.MethodServiceInterface;
import be.nabu.libs.services.vm.PipelineInterfaceProperty;
import be.nabu.libs.services.vm.api.VMService;

@WebService
public class Runtime {
	
	private static ServiceInterface trackInterface = MethodServiceInterface.wrap(FlatServiceTracker.class, "track"); 

	public static final java.lang.String APPLICATION_REGEX = java.lang.System.getProperty("nabu.system.applicationRegex", "([^.]+).*");
	public static final java.lang.String PROCESS_REGEX = java.lang.System.getProperty("nabu.system.processRegex", "[^.]+\\.processes\\.([^.]+).*");
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "user")
	public String getCurrentUser() {
		return executionContext.getSecurityContext().getPrincipal() == null ? null : executionContext.getSecurityContext().getPrincipal().getName();
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
	
	/**
	 * Allows you to register a service that performs service tracking, it must implement the interface nabu.interfaces.Services.track
	 */
	public void registerServiceTracker(@WebParam(name = "serviceId") String serviceId) {
		DefinedService resolved = executionContext.getServiceContext().getResolver(DefinedService.class).resolve(serviceId);
		if (!(resolved instanceof VMService)) {
			throw new IllegalArgumentException("The given node does not point to a vm service: " + serviceId);
		}
		VMService service = (VMService) resolved;
		if (!trackInterface.equals(ValueUtils.getValue(PipelineInterfaceProperty.getInstance(), service.getPipeline().getProperties()))) {
			throw new IllegalArgumentException("The vm service '" + serviceId + "' does not implement the correct interface: " + trackInterface);
		}
		if (executionContext.getServiceContext().getServiceTrackerProvider() instanceof ModifiableServiceRuntimeTrackerProvider) {
			((ModifiableServiceRuntimeTrackerProvider) executionContext.getServiceContext().getServiceTrackerProvider()).addTracker(
				ServiceRuntime.getRuntime().getService(), 
				new FlatServiceTrackerWrapper(service, executionContext), 
				true
			);
		}
		else {
			throw new IllegalStateException("The current execution context does not allow addition of custom service trackers");
		}
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
}
