package nabu.utils;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import nabu.interfaces.Services;
import nabu.utils.internal.FlowServiceTracker;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.services.MultipleServiceRuntimeTracker;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.vm.PipelineInterfaceProperty;
import be.nabu.libs.services.vm.api.VMService;

import java.lang.String;
import java.lang.Object;

@WebService
public class Service {
	
	private ExecutionContext executionContext;
	
	/**
	 * Allows you to register a service that performs service tracking, it must implement the interface nabu.interfaces.Services.track
	 */
	public void registerServiceTracker(@WebParam(name = "serviceId") String serviceId) {
		DefinedService resolved = executionContext.getServiceContext().getResolver(DefinedService.class).resolve(serviceId);
		if (!(resolved instanceof VMService)) {
			throw new IllegalArgumentException("The given node does not point to a vm service: " + serviceId);
		}
		VMService service = (VMService) resolved;
		String iface = Services.class.getName() + ".track";
		if (!iface.equalsIgnoreCase(ValueUtils.getValue(PipelineInterfaceProperty.getInstance(), service.getPipeline().getProperties()))) {
			throw new IllegalArgumentException("The vm service '" + serviceId + "' does not implement the correct interface: " + iface);
		}
		// the current runtime is the one that is calling this service "registerServiceTracker", need the parent runtime
		ServiceRuntime runtime = ServiceRuntime.getRuntime().getParent();
		// include the original runtime tracker as well, it could be for trace mode or the like
		runtime.setRuntimeTracker(new MultipleServiceRuntimeTracker(
			runtime.getRuntimeTracker(),
			new FlowServiceTracker(service)
		));
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
