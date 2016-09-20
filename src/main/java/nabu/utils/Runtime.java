package nabu.utils;

import java.lang.Object;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.repository.api.ModifiableServiceRuntimeTrackerProvider;
import be.nabu.eai.repository.util.FlatServiceTrackerWrapper;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;

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
	
	@WebResult(name = "principal")
	public Principal getCurrentPrincipal() {
		return executionContext.getSecurityContext().getToken();
	}
	
	@WebResult(name = "token")
	public Token getCurrentToken() {
		if (executionContext.getSecurityContext().getToken() instanceof Token) {
			return (Token) executionContext.getSecurityContext().getToken();
		}
		return null;
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
	
	/**
	 * Allows you to register a service that performs service tracking, it must implement the interface nabu.interfaces.Services.track
	 */
	public void registerServiceTracker(@WebParam(name = "serviceId") String serviceId, @WebParam(name = "servicesOnly") Boolean servicesOnly, @WebParam(name = "recursive") Boolean recursive) {
		DefinedService resolved = executionContext.getServiceContext().getResolver(DefinedService.class).resolve(serviceId);
		if (executionContext.getServiceContext().getServiceTrackerProvider() instanceof ModifiableServiceRuntimeTrackerProvider) {
			FlatServiceTrackerWrapper runtimeTracker = new FlatServiceTrackerWrapper(resolved, executionContext);
			runtimeTracker.setServicesOnly(servicesOnly == null ? false : servicesOnly);
			((ModifiableServiceRuntimeTrackerProvider) executionContext.getServiceContext().getServiceTrackerProvider()).addTracker(
				ServiceRuntime.getRuntime().getService(), 
				runtimeTracker, 
				recursive == null ? false : recursive
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
