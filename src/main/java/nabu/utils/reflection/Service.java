package nabu.utils.reflection;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.services.DefinedServiceResolverFactory;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.mask.MaskedContent;

@WebService
public class Service {
	
	@WebResult(name = "typeInstance")
	public Object newServiceInput(@WebParam(name = "serviceId") String id) {
		if (id != null) {
			return null;
		}
		DefinedService service = DefinedServiceResolverFactory.getInstance().getResolver().resolve(id);
		if (service == null) {
			throw new IllegalArgumentException("Service not found: " + id);
		}
		return service.getServiceInterface().getInputDefinition().newInstance();
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "output")
	public Object invoke(@WebParam(name = "serviceId") String id, @WebParam(name = "input") Object input) throws ServiceException {
		if (id == null) {
			return null;
		}
		DefinedService service = DefinedServiceResolverFactory.getInstance().getResolver().resolve(id);
		if (service == null) {
			throw new IllegalArgumentException("Service not found: " + id);
		}
		ComplexContent serviceInput;
		if (input != null) {
			if (!(input instanceof ComplexContent)) {
				input = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(input);
				if (input == null) {
					throw new IllegalArgumentException("Can not wrap input");
				}
			}
			serviceInput = new MaskedContent((ComplexContent) input, service.getServiceInterface().getInputDefinition());
		}
		else {
			serviceInput = service.getServiceInterface().getInputDefinition().newInstance();
		}
		ServiceRuntime runtime = new ServiceRuntime(service, ServiceRuntime.getRuntime().getExecutionContext());
		return runtime.run(serviceInput);
	}
}
