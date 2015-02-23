package nabu.utils;

import java.util.UUID;
import java.lang.String;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;

@WebService
public class System {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "uuid")
	public String uuid() {
		return UUID.randomUUID().toString();
	}
	
	@WebResult(name = "host")
	public String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
	
	@WebResult(name = "user")
	public String getCurrentUser() {
		return executionContext.getSecurityContext().getPrincipal() == null ? null : executionContext.getSecurityContext().getPrincipal().getName();
	}
	
	@WebResult(name = "service")
	public String getService() {
		Service service = ServiceRuntime.getRuntime().getService();
		return service instanceof DefinedService ? ((DefinedService) service).getId() : null;
	}
	
	@WebResult(name = "application")
	public String getApplication() {
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		while (runtime.getParent() != null) {
			runtime = runtime.getParent();
		}
		if (runtime.getService() instanceof DefinedService) {
			return ((DefinedService) runtime.getService()).getId().replaceAll("([^.]+).*", "$1");
		}
		return null;
	}
}
