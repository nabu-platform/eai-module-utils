package nabu.utils;

import java.lang.Object;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import nabu.utils.types.ExceptionSummary;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import be.nabu.libs.authentication.api.Device;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.authentication.api.WrappedToken;
import be.nabu.libs.authentication.api.principals.DevicePrincipal;
import be.nabu.libs.authentication.impl.ImpersonateToken;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceInstanceWithPipeline;

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
		return ExceptionSummary.build(exception);
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
		return runtime.getServiceInstance() instanceof ServiceInstanceWithPipeline ? ((ServiceInstanceWithPipeline) runtime.getServiceInstance()).getPipeline() : null;
	}
}
